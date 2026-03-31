package com.resiflow.controller;

import com.resiflow.config.CaptchaProperties;
import com.resiflow.dto.PublicAppConfigResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PublicConfigController {

    private final CaptchaProperties captchaProperties;

    public PublicConfigController(final CaptchaProperties captchaProperties) {
        this.captchaProperties = captchaProperties;
    }

    @GetMapping("/app-config")
    public ResponseEntity<PublicAppConfigResponse> getAppConfig() {
        String siteKey = captchaProperties.enabled() ? captchaProperties.siteKey() : null;
        return ResponseEntity.ok(PublicAppConfigResponse.fromCaptchaConfig(captchaProperties.enabled(), siteKey));
    }
}
