package com.resiflow.service;

import com.resiflow.config.PushProperties;
import com.resiflow.entity.UserPushToken;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.push", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LoggingPushGateway implements PushGateway {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingPushGateway.class);

    private final PushProperties pushProperties;

    public LoggingPushGateway(final PushProperties pushProperties) {
        this.pushProperties = pushProperties;
    }

    @Override
    public PushSendResult send(final PushMessage pushMessage, final List<UserPushToken> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return PushSendResult.skipped(0);
        }
        if (!pushProperties.isEnabled()) {
            LOGGER.info(
                    "Push dispatch skipped because app.push.enabled=false notificationTitle={} recipients={}",
                    pushMessage.title(),
                    tokens.size()
            );
            return PushSendResult.skipped(tokens.size());
        }
        LOGGER.warn(
                "Push dispatch requested for {} token(s), but no provider implementation is configured yet. title={}",
                tokens.size(),
                pushMessage.title()
        );
        return PushSendResult.skipped(tokens.size());
    }
}
