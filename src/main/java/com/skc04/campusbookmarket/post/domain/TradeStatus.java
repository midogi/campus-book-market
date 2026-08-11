package com.skc04.campusbookmarket.post.domain;

public enum TradeStatus {

    SALE("판매 중"),
    RESERVED("예약 중"),
    SOLD("판매 완료");

    private final String displayName;

    TradeStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }


}
