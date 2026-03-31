package com.resiflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.captcha")
public record CaptchaProperties(
        boolean enabled,
        String siteKey,
        String secretKey,
        String verifyUrl
) {
}
