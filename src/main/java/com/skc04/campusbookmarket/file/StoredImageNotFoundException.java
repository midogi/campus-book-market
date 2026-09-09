package com.skc04.campusbookmarket.file;

/** DB 또는 URL이 가리키는 이미지 파일을 디스크에서 찾지 못했을 때 발생한다. */
public class StoredImageNotFoundException extends RuntimeException {

    public StoredImageNotFoundException(String storedName) {
        super("저장된 이미지를 찾을 수 없습니다. storedName=" + storedName);
    }
}
