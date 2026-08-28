package com.skc04.campusbookmarket.post.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.skc04.campusbookmarket.config.QuerydslConfig;
import com.skc04.campusbookmarket.post.domain.TradePost;
import com.skc04.campusbookmarket.post.domain.TradePostSearchType;
import com.skc04.campusbookmarket.post.domain.TradePostSort;
import com.skc04.campusbookmarket.post.domain.TradeStatus;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

/** JPA 매핑과 저장소 쿼리를 내장 H2 데이터베이스에서 검증한다. */
@DataJpaTest
@Import({
        JpaTradePostRepository.class,
        QuerydslConfig.class
})
class JpaTradePostRepositoryTest {

    @Autowired
    private JpaTradePostRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void saveAndFindById() {
        TradePost savedPost = repository.save(
                "스프링 JPA",
                18000,
                "김동민",
                "JPA 학습용 게시글"
        );

        assertNotNull(savedPost.getId());

        flushAndClear();

        TradePost foundPost = repository.findById(savedPost.getId())
                .orElseThrow();

        assertEquals(savedPost.getId(), foundPost.getId());
        assertEquals("스프링 JPA", foundPost.getTitle());
        assertEquals(18000, foundPost.getPrice());
        assertEquals("김동민", foundPost.getSellerName());
        assertEquals("JPA 학습용 게시글", foundPost.getDescription());
        assertEquals(TradeStatus.SALE, foundPost.getStatus());
        assertNotNull(foundPost.getCreatedAt());
        assertNotNull(foundPost.getUpdatedAt());
    }

    @Test
    void updateUsesDirtyChecking() {
        TradePost savedPost = repository.save(
                "수정 전 제목",
                10000,
                "수정 전 판매자",
                "수정 전 설명"
        );
        Long postId = savedPost.getId();

        flushAndClear();

        LocalDateTime originalCreatedAt = repository.findById(postId)
                .orElseThrow()
                .getCreatedAt();
        entityManager.clear();

        repository.update(
                postId,
                "수정 후 제목",
                20000,
                "수정 후 판매자",
                "수정 후 설명"
        ).orElseThrow();

        flushAndClear();

        TradePost foundPost = repository.findById(postId).orElseThrow();
        assertEquals("수정 후 제목", foundPost.getTitle());
        assertEquals(20000, foundPost.getPrice());
        assertEquals("수정 후 판매자", foundPost.getSellerName());
        assertEquals("수정 후 설명", foundPost.getDescription());
        assertEquals(originalCreatedAt, foundPost.getCreatedAt());
        assertNotNull(foundPost.getUpdatedAt());
    }

    @Test
    void updateStatusUsesDirtyChecking() {
        TradePost savedPost = repository.save(
                "상태 변경 게시글",
                15000,
                "김동민",
                "판매 상태를 변경합니다."
        );

        repository.updateStatus(savedPost.getId(), TradeStatus.SOLD)
                .orElseThrow();

        flushAndClear();

        TradePost foundPost = repository.findById(savedPost.getId()).orElseThrow();
        assertEquals(TradeStatus.SOLD, foundPost.getStatus());
        assertNotNull(foundPost.getUpdatedAt());
    }

    @Test
    void deleteRemovesPost() {
        TradePost savedPost = repository.save(
                "삭제할 게시글",
                5000,
                "김동민",
                "삭제 테스트"
        );

        boolean deleted = repository.deleteById(savedPost.getId());

        flushAndClear();

        assertTrue(deleted);
        assertTrue(repository.findById(savedPost.getId()).isEmpty());
    }

    @Test
    void updateStatusAndDeleteFailWhenPostDoesNotExist() {
        assertTrue(repository.updateStatus(999999L, TradeStatus.SOLD).isEmpty());
        assertFalse(repository.deleteById(999999L));
    }

    @Test
    void findAllReturnsPostsInIdOrder() {
        TradePost firstPost = repository.save("첫 번째 게시글", 10000, "판매자1", "설명1");
        TradePost secondPost = repository.save("두 번째 게시글", 20000, "판매자2", "설명2");
        TradePost thirdPost = repository.save("세 번째 게시글", 30000, "판매자3", "설명3");

        flushAndClear();

        List<TradePost> posts = repository.findAll();
        assertEquals(List.of(
                firstPost.getId(),
                secondPost.getId(),
                thirdPost.getId()
        ), posts.stream().map(TradePost::getId).toList());
    }

    @Test
    void searchUsesSelectedFieldAndStatus() {
        TradePost titleMatch = repository.save(
                "김동민의 스프링 책",
                15000,
                "다른 판매자",
                "제목 검색"
        );
        TradePost sellerMatch = repository.save(
                "자바 기초",
                10000,
                "김동민",
                "판매자 검색"
        );
        repository.updateStatus(sellerMatch.getId(), TradeStatus.SOLD).orElseThrow();

        flushAndClear();

        List<TradePost> titleResults = repository.search(
                "김동민", TradePostSearchType.TITLE, null,
                TradePostSort.LATEST, 10, 0
        );
        List<TradePost> sellerResults = repository.search(
                "김동민", TradePostSearchType.SELLER, TradeStatus.SOLD,
                TradePostSort.LATEST, 10, 0
        );

        assertEquals(List.of(titleMatch.getId()), titleResults.stream()
                .map(TradePost::getId)
                .toList());
        assertEquals(List.of(sellerMatch.getId()), sellerResults.stream()
                .map(TradePost::getId)
                .toList());
    }

    @Test
    void searchSortsByPriceAndReturnsRequestedPage() {
        repository.save("비싼 책", 30000, "판매자1", "설명1");
        repository.save("저렴한 책", 10000, "판매자2", "설명2");
        repository.save("중간 가격 책", 20000, "판매자3", "설명3");

        flushAndClear();

        List<TradePost> firstPage = repository.search(
                "", TradePostSearchType.TITLE, null,
                TradePostSort.PRICE_ASC, 2, 0
        );
        List<TradePost> secondPage = repository.search(
                "", TradePostSearchType.TITLE, null,
                TradePostSort.PRICE_ASC, 2, 2
        );

        assertEquals(List.of(10000L, 20000L), firstPage.stream()
                .map(TradePost::getPrice)
                .toList());
        assertEquals(List.of(30000L), secondPage.stream()
                .map(TradePost::getPrice)
                .toList());
    }

    @Test
    void countUsesKeywordAndStatusConditions() {
        repository.save("Spring MVC", 15000, "판매자1", "설명1");
        TradePost soldPost = repository.save("Spring DB", 12000, "판매자2", "설명2");
        repository.save("Java 기초", 10000, "판매자3", "설명3");
        repository.updateStatus(soldPost.getId(), TradeStatus.SOLD).orElseThrow();

        flushAndClear();

        assertEquals(3L, repository.count("", TradePostSearchType.TITLE, null));
        assertEquals(2L, repository.count("spring", TradePostSearchType.TITLE, null));
        assertEquals(1L, repository.count(
                "spring",
                TradePostSearchType.TITLE,
                TradeStatus.SOLD
        ));
    }

    @Test
    void searchRejectsInvalidPagination() {
        assertThrows(IllegalArgumentException.class, () -> repository.search(
                "", TradePostSearchType.TITLE, null,
                TradePostSort.LATEST, 0, 0
        ));
        assertThrows(IllegalArgumentException.class, () -> repository.search(
                "", TradePostSearchType.TITLE, null,
                TradePostSort.LATEST, 10, -1
        ));
    }

    private void flushAndClear() {
        // SQL을 DB에 반영한 뒤 1차 캐시를 비워 다음 조회가 실제 DB를 통과하게 한다.
        entityManager.flush();
        entityManager.clear();
    }
}
