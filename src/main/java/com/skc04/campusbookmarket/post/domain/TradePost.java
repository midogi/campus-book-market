package com.skc04.campusbookmarket.post.domain;

public class TradePost {

    private final Long id;
    private final String title;
    private final long price;
    private final String sellerName;
    private final String description;

    public TradePost(Long id, String title, long price, String sellerName, String description) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.sellerName = sellerName;
        this.description = description;
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

    public String getDescription() {
        return description;
    }
}

