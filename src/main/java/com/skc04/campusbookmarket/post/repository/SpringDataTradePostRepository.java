package com.skc04.campusbookmarket.post.repository;

import com.skc04.campusbookmarket.post.domain.TradePost;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA가 기본 CRUD 구현과 메서드 이름 기반 조회 쿼리를 생성하는 저장소다.
 */
public interface SpringDataTradePostRepository extends JpaRepository<TradePost, Long> {

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select p from TradePost p where p.id = :id")
    java.util.Optional<TradePost> findByIdForUpdate(Long id);

    List<TradePost> findByTitleContainingIgnoreCaseOrderByIdDesc(String keyword);
}
