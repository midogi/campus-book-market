package com.skc04.campusbookmarket.trade.domain;

import com.skc04.campusbookmarket.member.domain.Member;
import com.skc04.campusbookmarket.post.domain.TradePost;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/** 신청 자체가 거래 내역이다. 완료 후 별도 이력 테이블로 복사하지 않는다. */
@Entity
@Table(name = "trade_request")
public class TradeRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // 회원/게시글은 다른 거래도 공유하므로 REMOVE cascade를 설정하지 않는다.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private TradePost post;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Member buyer;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private Member seller;
    // 게시글을 나중에 수정하더라도 당시 신청 조건은 바뀌지 않는 스냅샷이다.
    @Column(nullable = false, length = 100)
    private String title;
    @Column(nullable = false)
    private long price;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestStatus status;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected TradeRequest() { }

    public TradeRequest(TradePost post, Member buyer, Member seller) {
        this.post = post;
        this.buyer = buyer;
        this.seller = seller;
        this.title = post.getTitle();
        this.price = post.getPrice();
        this.status = RequestStatus.REQUESTED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = createdAt;
    }

    public void reserve() { transition(RequestStatus.REQUESTED, RequestStatus.RESERVED); }
    public void complete() { transition(RequestStatus.RESERVED, RequestStatus.COMPLETED); }
    public void reject() { transition(RequestStatus.REQUESTED, RequestStatus.REJECTED); }
    public void cancel() {
        if (status != RequestStatus.REQUESTED && status != RequestStatus.RESERVED) {
            throw new IllegalStateException("Only active requests can be cancelled");
        }
        status = RequestStatus.CANCELLED;
        updatedAt = LocalDateTime.now();
    }

    private void transition(RequestStatus expected, RequestStatus next) {
        if (status != expected) throw new IllegalStateException("Invalid trade transition");
        status = next;
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public TradePost getPost() { return post; }
    public Member getBuyer() { return buyer; }
    public Member getSeller() { return seller; }
    public String getTitle() { return title; }
    public long getPrice() { return price; }
    public RequestStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
