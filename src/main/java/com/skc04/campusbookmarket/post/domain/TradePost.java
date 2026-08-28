package com.skc04.campusbookmarket.post.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * 대학생 전공책·학습용품 거래 게시글을 표현하는 JPA 엔티티다.
 *
 * <p>화면 입력 객체와 분리되어 있으며, 게시글의 거래 상태와 수정 시각 같은
 * 도메인 규칙을 스스로 관리한다.</p>
 */
@Entity
@Table(name = "trade_post")
public class TradePost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private long price;

    @Column(name = "seller_name", nullable = false, length = 30)
    private String sellerName;

    @Column(nullable = false, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TradeStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** JDBC RowMapper가 DB의 한 행을 엔티티로 복원할 때 사용하는 생성자다. */
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

    /** 새 게시글을 만들 때 사용하며 ID와 최초 상태·시각은 내부에서 결정한다. */
    public TradePost(
            String title,
            long price,
            String sellerName,
            String description
    ) {
        this(null, title, price, sellerName, description, TradeStatus.SALE);
    }

    /** 기존 JDBC 저장소 및 테스트와의 호환을 위한 생성자다. */
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

    protected TradePost() {
        // JPA가 조회 결과로 엔티티를 생성할 수 있도록 제공한다.
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

    public void updateDetails(
            String title,
            long price,
            String sellerName,
            String description
    ) {
        this.title = title;
        this.price = price;
        this.sellerName = sellerName;
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    /** 거래 상태를 변경하고 마지막 수정 시각을 함께 갱신한다. */
    public void changeStatus(TradeStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("거래 상태는 필수입니다.");
        }
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
}
