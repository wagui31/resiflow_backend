package com.resiflow.dto;

public class PublicAppConfigResponse {

    private final CaptchaConfigResponse captcha;

    public PublicAppConfigResponse(final CaptchaConfigResponse captcha) {
        this.captcha = captcha;
    }

    public CaptchaConfigResponse getCaptcha() {
        return captcha;
    }

    public static PublicAppConfigResponse fromCaptchaConfig(final boolean registerEnabled, final String siteKey) {
        return new PublicAppConfigResponse(new CaptchaConfigResponse(registerEnabled, siteKey));
    }

    public static class CaptchaConfigResponse {

        private final boolean registerEnabled;
        private final String siteKey;

        public CaptchaConfigResponse(final boolean registerEnabled, final String siteKey) {
            this.registerEnabled = registerEnabled;
            this.siteKey = siteKey;
        }

        public boolean isRegisterEnabled() {
            return registerEnabled;
        }

        public String getSiteKey() {
            return siteKey;
        }
    }
}
