package com.skc04.campusbookmarket.web.member.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 회원가입 화면에서 전송한 입력값과 검증 규칙을 담는다. */
public class MemberCreateForm {

    @NotBlank(message = "{validation.member.loginId.required}")
    @Size(
            min = 4,
            max = 20,
            message = "{validation.member.loginId.size}"
    )
    private String loginId;

    @NotBlank(message = "{validation.member.password.required}")
    @Size(
            min = 8,
            max = 72,
            message = "{validation.member.password.size}"
    )
    private String password;

    @NotBlank(message = "{validation.member.passwordConfirm.required}")
    private String passwordConfirm;

    @NotBlank(message = "{validation.member.name.required}")
    @Size(max = 30, message = "{validation.member.name.size}")
    private String name;

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

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
