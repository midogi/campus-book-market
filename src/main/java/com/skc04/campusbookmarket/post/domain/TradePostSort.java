package com.skc04.campusbookmarket.post.domain;

public enum TradePostSort {

    LATEST("최신순"),
    PRICE_ASC("낮은 가격순"),
    PRICE_DESC("높은 가격순");

    private final String displayName;

    TradePostSort(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
