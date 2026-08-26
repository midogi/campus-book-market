package com.skc04.campusbookmarket.post.domain;

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
