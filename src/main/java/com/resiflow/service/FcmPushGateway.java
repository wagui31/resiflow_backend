package com.resiflow.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.ErrorCode;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import com.resiflow.config.PushProperties;
import com.resiflow.entity.PushTokenPlatform;
import com.resiflow.entity.UserPushToken;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.push", name = "enabled", havingValue = "true")
public class FcmPushGateway implements PushGateway {

    private static final Logger LOGGER = LoggerFactory.getLogger(FcmPushGateway.class);
    private static final String APP_NAME = "resiflow-push";
    private static final String ANDROID_CHANNEL_ID = "resiflow_notifications";

    private final PushProperties pushProperties;

    private volatile FirebaseMessaging firebaseMessaging;
    private volatile boolean firebaseInitializationAttempted;

    public FcmPushGateway(final PushProperties pushProperties) {
        this.pushProperties = pushProperties;
    }

    @Override
    public PushSendResult send(final PushMessage pushMessage, final List<UserPushToken> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return PushSendResult.skipped(0);
        }

        FirebaseMessaging messaging = resolveFirebaseMessaging();
        if (messaging == null) {
            LOGGER.warn(
                    "Push dispatch skipped because Firebase Admin is not configured. title={} recipients={}",
                    pushMessage.title(),
                    tokens.size()
            );
            return PushSendResult.skipped(tokens.size());
        }

        List<Message> messages = new ArrayList<>(tokens.size());
        for (UserPushToken token : tokens) {
            messages.add(buildMessage(pushMessage, token));
        }

        try {
            BatchResponse batchResponse = messaging.sendEach(messages);
            return toSendResult(tokens, batchResponse);
        } catch (FirebaseMessagingException exception) {
            throw new IllegalStateException("Failed to dispatch push notifications through Firebase", exception);
        }
    }

    private FirebaseMessaging resolveFirebaseMessaging() {
        FirebaseMessaging local = firebaseMessaging;
        if (local != null) {
            return local;
        }
        if (firebaseInitializationAttempted) {
            return null;
        }
        synchronized (this) {
            if (firebaseMessaging != null) {
                return firebaseMessaging;
            }
            if (firebaseInitializationAttempted) {
                return null;
            }
            firebaseInitializationAttempted = true;
            firebaseMessaging = initializeFirebaseMessaging();
            return firebaseMessaging;
        }
    }

    private FirebaseMessaging initializeFirebaseMessaging() {
        PushProperties.Firebase firebaseProperties = pushProperties.getFirebase();
        try (InputStream credentialsStream = openCredentialsStream(firebaseProperties)) {
            if (credentialsStream == null) {
                LOGGER.error(
                        "Firebase push is enabled but no service account credentials were provided. "
                                + "Set APP_PUSH_FIREBASE_CREDENTIALS_FILE or APP_PUSH_FIREBASE_CREDENTIALS_BASE64."
                );
                return null;
            }

            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);
            FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder().setCredentials(credentials);
            if (hasText(firebaseProperties.getProjectId())) {
                optionsBuilder.setProjectId(firebaseProperties.getProjectId().trim());
            }

            FirebaseApp app = FirebaseApp.getApps().stream()
                    .filter(candidate -> APP_NAME.equals(candidate.getName()))
                    .findFirst()
                    .orElseGet(() -> FirebaseApp.initializeApp(optionsBuilder.build(), APP_NAME));
            LOGGER.info("Firebase Admin push gateway initialized with appName={}", app.getName());
            return FirebaseMessaging.getInstance(app);
        } catch (IOException exception) {
            LOGGER.error("Unable to initialize Firebase Admin push gateway", exception);
            return null;
        }
    }

    private InputStream openCredentialsStream(final PushProperties.Firebase firebaseProperties) throws IOException {
        if (firebaseProperties == null) {
            return null;
        }
        if (hasText(firebaseProperties.getCredentialsBase64())) {
            byte[] decoded = Base64.getDecoder().decode(firebaseProperties.getCredentialsBase64().trim());
            return new ByteArrayInputStream(decoded);
        }
        if (hasText(firebaseProperties.getCredentialsFile())) {
            return Files.newInputStream(Path.of(firebaseProperties.getCredentialsFile().trim()));
        }
        return null;
    }

    private Message buildMessage(final PushMessage pushMessage, final UserPushToken token) {
        Message.Builder builder = Message.builder()
                .setToken(token.getToken())
                .setNotification(Notification.builder()
                        .setTitle(pushMessage.title())
                        .setBody(pushMessage.body())
                        .build())
                .putAllData(pushMessage.data() == null ? Map.of() : pushMessage.data());

        if (token.getPlatform() == PushTokenPlatform.ANDROID) {
            builder.setAndroidConfig(AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .setNotification(AndroidNotification.builder()
                            .setChannelId(ANDROID_CHANNEL_ID)
                            .setSound("default")
                            .build())
                    .build());
        } else if (token.getPlatform() == PushTokenPlatform.IOS) {
            builder.setApnsConfig(ApnsConfig.builder()
                    .setAps(Aps.builder().setSound("default").build())
                    .build());
        }

        return builder.build();
    }

    private PushSendResult toSendResult(final List<UserPushToken> tokens, final BatchResponse batchResponse) {
        int acceptedCount = batchResponse.getSuccessCount();
        List<Long> invalidTokenIds = new ArrayList<>();
        List<SendResponse> responses = batchResponse.getResponses();
        for (int index = 0; index < responses.size() && index < tokens.size(); index++) {
            SendResponse response = responses.get(index);
            if (response.isSuccessful()) {
                continue;
            }
            FirebaseMessagingException exception = response.getException();
            if (exception != null && isInvalidTokenError(exception)) {
                invalidTokenIds.add(tokens.get(index).getId());
            }
        }
        return new PushSendResult(tokens.size(), acceptedCount, List.copyOf(invalidTokenIds));
    }

    private boolean isInvalidTokenError(final FirebaseMessagingException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        if (errorCode == ErrorCode.INVALID_ARGUMENT || errorCode == ErrorCode.NOT_FOUND) {
            return true;
        }
        String message = exception.getMessage();
        if (message == null) {
            return false;
        }
        String normalizedMessage = message.toLowerCase();
        return normalizedMessage.contains("registration token is not a valid fcm registration token")
                || normalizedMessage.contains("requested entity was not found")
                || normalizedMessage.contains("not a valid fcm registration token");
    }

    private boolean hasText(final String value) {
        return value != null && !value.trim().isEmpty();
    }
}
