package com.skc04.campusbookmarket.file;

/** 사용자가 업로드한 파일이 게시글 이미지 규칙을 만족하지 않을 때 발생한다. */
public class InvalidImageException extends RuntimeException {

    public InvalidImageException(String message) {
        super(message);
    }
}
