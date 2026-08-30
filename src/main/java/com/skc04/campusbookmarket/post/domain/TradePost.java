package com.skc04.campusbookmarket.post.domain;

import com.skc04.campusbookmarket.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "member_id")
    private Member seller;

    /** JDBC 조회와 권한 비교에서도 작성자 ID를 사용할 수 있게 같은 FK를 읽기 전용으로 매핑한다. */
    @Column(name = "member_id", insertable = false, updatable = false)
    private Long sellerId;

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
        this(
                id,
                null,
                title,
                price,
                sellerName,
                description,
                status,
                createdAt,
                updatedAt
        );
    }

    /** JDBC RowMapper가 작성자 FK까지 포함한 DB 한 행을 복원할 때 사용한다. */
    public TradePost(
            Long id,
            Long sellerId,
            String title,
            long price,
            String sellerName,
            String description,
            TradeStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.sellerId = sellerId;
        this.title = title;
        this.price = price;
        this.sellerName = sellerName;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** 로그인 회원이 새 게시글을 등록할 때 사용한다. 판매자 이름은 세션 회원에게서 정한다. */
    public TradePost(
            String title,
            long price,
            Member seller,
            String description
    ) {
        LocalDateTime now = LocalDateTime.now();
        this.seller = Objects.requireNonNull(seller, "판매자 회원은 필수입니다.");
        this.sellerId = seller.getId();
        this.title = title;
        this.price = price;
        this.sellerName = seller.getName();
        this.description = description;
        this.status = TradeStatus.SALE;
        this.createdAt = now;
        this.updatedAt = now;
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

    public Long getSellerId() {
        if (sellerId != null) {
            return sellerId;
        }
        return seller == null ? null : seller.getId();
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
            String description
    ) {
        this.title = title;
        this.price = price;
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    /** 현재 로그인 회원이 이 게시글을 작성했는지 식별자 값으로 비교한다. */
    public boolean isWrittenBy(Long memberId) {
        return memberId != null && Objects.equals(getSellerId(), memberId);
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
