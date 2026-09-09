package com.skc04.campusbookmarket.post.domain;

/** 검색어를 제목과 판매자명 중 어느 필드에 적용할지 결정한다. */
public enum TradePostSearchType {

    TITLE("제목"),
    SELLER("판매자명");

    private final String displayName;

    TradePostSearchType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
