package com.skc04.campusbookmarket.post.domain;

public class TradePost {

    private final Long id;
    private final String title;
    private final long price;
    private final String sellerName;

    public TradePost(Long id, String title, long price, String sellerName) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.sellerName = sellerName;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public long getPrice() {
        return price;
    }

    public String getSellerName() {
        return sellerName;
    }
}
