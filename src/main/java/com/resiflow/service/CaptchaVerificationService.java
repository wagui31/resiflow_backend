package com.resiflow.service;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.resiflow.config.CaptchaProperties;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
public class CaptchaVerificationService {

    private static final String MISSING_TOKEN_MESSAGE = "Captcha token must not be blank";
    private static final String INVALID_CAPTCHA_MESSAGE = "Captcha validation failed";

    private final CaptchaProperties captchaProperties;
    private final RestClient restClient;

    @Autowired
    public CaptchaVerificationService(
            final CaptchaProperties captchaProperties,
            final RestClient.Builder restClientBuilder
    ) {
        this(captchaProperties, restClientBuilder.build());
    }

    public CaptchaVerificationService(final CaptchaProperties captchaProperties, final RestClient restClient) {
        this.captchaProperties = captchaProperties;
        this.restClient = restClient;
    }

    public void validateRegistrationCaptcha(final String captchaToken) {
        if (!captchaProperties.enabled()) {
            return;
        }
        if (captchaToken == null || captchaToken.trim().isEmpty()) {
            throw new CaptchaValidationException(MISSING_TOKEN_MESSAGE);
        }
        if (isBlank(captchaProperties.secretKey())) {
            throw new IllegalStateException("Captcha secret key must be configured when captcha is enabled");
        }
        if (isBlank(captchaProperties.verifyUrl())) {
            throw new IllegalStateException("Captcha verify URL must be configured when captcha is enabled");
        }

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("secret", captchaProperties.secretKey());
        body.add("response", captchaToken.trim());

        CaptchaVerifyResponse response = restClient.post()
                .uri(captchaProperties.verifyUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(CaptchaVerifyResponse.class);

        if (response == null || !response.success()) {
            throw new CaptchaValidationException(INVALID_CAPTCHA_MESSAGE);
        }
    }

    private boolean isBlank(final String value) {
        return value == null || value.trim().isEmpty();
    }

    record CaptchaVerifyResponse(
            boolean success,
            @JsonAlias("error-codes") List<String> errorCodes
    ) {
    }
}
