package com.resiflow.dto;

public class ForgotPasswordVerifyCodeRequest {

    private String email;
    private String code;

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }
}
