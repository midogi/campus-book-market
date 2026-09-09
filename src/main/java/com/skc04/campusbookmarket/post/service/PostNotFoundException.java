package com.skc04.campusbookmarket.post.service;

/** 요청한 거래 게시글이 존재하지 않을 때 사용하는 예외다. */
public class PostNotFoundException extends RuntimeException {

    public PostNotFoundException(Long postId) {
        super("게시글을 찾을 수 없습니다. postId=" + postId);
    }
}
