package com.skc04.campusbookmarket.post.repository;

import com.skc04.campusbookmarket.member.domain.Member;
import com.skc04.campusbookmarket.post.domain.TradePost;
import com.skc04.campusbookmarket.post.domain.TradePostSearchType;
import com.skc04.campusbookmarket.post.domain.TradePostSort;
import com.skc04.campusbookmarket.post.domain.TradeStatus;
import java.util.List;
import java.util.Optional;

/**
 * 거래 게시글 영속성 기능의 계약이다.
 *
 * <p>서비스 계층은 JDBC나 JPA 같은 구현 기술을 알지 않고 이 인터페이스에만
 * 의존하므로, 저장 기술을 교체해도 비즈니스 코드는 영향을 받지 않는다.</p>
 */
public interface TradePostRepository {

    /** 모든 게시글을 ID 오름차순으로 조회한다. */
    List<TradePost> findAll();

    /** 제목과 거래 상태를 기준으로 게시글을 검색한다. */
    List<TradePost> search(String keyword, TradeStatus status);

    /** 제목·거래 상태·정렬 조건을 기준으로 게시글을 검색한다. */
    List<TradePost> search(String keyword, TradeStatus status, TradePostSort sort);

    /** 화면에서 선택한 검색 필드와 페이징 조건까지 적용해 게시글을 조회한다. */
    List<TradePost> search(
            String keyword,
            TradePostSearchType searchType,
            TradeStatus status,
            TradePostSort sort,
            int limit,
            int offset
    );

    /** 검색 조건에 맞는 전체 게시글 수를 반환한다. */
    long count(
            String keyword,
            TradePostSearchType searchType,
            TradeStatus status
    );

    /** 기본 키로 게시글을 조회하며, 없으면 빈 Optional을 반환한다. */
    Optional<TradePost> findById(Long id);

    /** 새 게시글을 저장하고 DB에서 생성된 ID가 반영된 엔티티를 반환한다. */
    TradePost save(String title, long price, Member seller, String description);

    /** 게시글 내용을 수정하며, 대상이 없으면 빈 Optional을 반환한다. */
    Optional<TradePost> update(
            Long id,
            String title,
            long price,
            String description
    );

    /** 거래 상태를 변경하며, 대상이 없으면 빈 Optional을 반환한다. */
    Optional<TradePost> updateStatus(Long id, TradeStatus status);

    /** 게시글을 삭제하고 실제 삭제 여부를 반환한다. */
    boolean deleteById(Long id);
}
