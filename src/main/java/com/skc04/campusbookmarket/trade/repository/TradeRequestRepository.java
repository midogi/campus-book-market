package com.skc04.campusbookmarket.trade.repository;

import com.skc04.campusbookmarket.trade.domain.RequestStatus;
import com.skc04.campusbookmarket.trade.domain.TradeRequest;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TradeRequestRepository extends JpaRepository<TradeRequest, Long> {
    boolean existsByPostId(Long postId);
    boolean existsByPostIdAndStatusIn(Long postId, Collection<RequestStatus> statuses);
    boolean existsByPostIdAndBuyerIdAndStatusIn(Long postId, Long buyerId, Collection<RequestStatus> statuses);
    List<TradeRequest> findByPostIdAndStatus(Long postId, RequestStatus status);

    // 잠금 전에 엔티티를 읽으면 오래된 상태를 1차 캐시에 담을 수 있어 FK만 조회한다.
    @Query("select r.post.id from TradeRequest r where r.id = :id")
    Optional<Long> findPostIdById(Long id);

    // to-one 관계만 함께 조회하므로 컬렉션 페치 조인의 페이징 문제를 피한다.
    @EntityGraph(attributePaths = {"post", "buyer", "seller"})
    Page<TradeRequest> findByPostIdOrderByIdDesc(Long postId, Pageable pageable);

    @EntityGraph(attributePaths = {"post", "buyer", "seller"})
    @Query("select r from TradeRequest r where r.buyer.id = :memberId or r.seller.id = :memberId order by r.id desc")
    Page<TradeRequest> findHistory(Long memberId, Pageable pageable);
}
