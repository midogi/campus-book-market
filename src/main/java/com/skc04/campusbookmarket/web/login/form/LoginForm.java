package com.skc04.campusbookmarket.web.login.form;

import jakarta.validation.constraints.NotBlank;

/** 로그인 화면에서 전송한 아이디와 비밀번호를 받는 폼 객체다. */
public class LoginForm {

    @NotBlank(message = "{validation.login.loginId.required}")
    private String loginId;

    @NotBlank(message = "{validation.login.password.required}")
    private String password;

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
