package com.skc04.campusbookmarket.post.domain;

import java.time.LocalDateTime;

public class TradePost {

    private final Long id;
    private final String title;
    private final long price;
    private final String sellerName;
    private final String description;
    private final TradeStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public TradePost(
            Long id,
            String title,
            long price,
            String sellerName,
            String description,
            TradeStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.sellerName = sellerName;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public TradePost(
            Long id,
            String title,
            long price,
            String sellerName,
            String description
    ) {
        this(id, title, price, sellerName, description, TradeStatus.SALE);
    }

    public TradePost(
            Long id,
            String title,
            long price,
            String sellerName,
            String description,
            TradeStatus status
    ) {
        LocalDateTime now = LocalDateTime.now();
        this.id = id;
        this.title = title;
        this.price = price;
        this.sellerName = sellerName;
        this.description = description;
        this.status = status;
        this.createdAt = now;
        this.updatedAt = now;
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

    public TradeStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
