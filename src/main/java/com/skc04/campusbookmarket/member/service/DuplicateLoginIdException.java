package com.skc04.campusbookmarket.member.service;

/** 이미 사용 중인 로그인 아이디로 가입을 시도할 때 발생한다. */
public class DuplicateLoginIdException extends RuntimeException {

    public DuplicateLoginIdException(String loginId) {
        super("이미 사용 중인 로그인 아이디입니다: " + loginId);
    }
}
