package com.skc04.campusbookmarket.post.service;

/** 로그인 회원이 다른 회원의 게시글을 변경하려 할 때 사용하는 403 예외다. */
public class PostAccessDeniedException extends RuntimeException {

    public PostAccessDeniedException() {
        super("게시글을 변경할 권한이 없습니다.");
    }
}
