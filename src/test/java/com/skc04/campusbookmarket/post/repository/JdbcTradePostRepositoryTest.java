package com.skc04.campusbookmarket.post.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.skc04.campusbookmarket.post.domain.TradePost;
import com.skc04.campusbookmarket.post.domain.TradeStatus;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;

@JdbcTest
@Import(JdbcTradePostRepository.class)
class JdbcTradePostRepositoryTest {

    @Autowired
    private JdbcTradePostRepository repository;

    @Test
    void saveAndFindById() {
        TradePost savedPost = repository.save(
                "스프링 입문",
                15000,
                "컴퓨터공학과 3학년",
                "필기 없이 깨끗합니다."
        );

        TradePost foundPost = repository.findById(savedPost.getId()).orElseThrow();

        assertEquals("스프링 입문", foundPost.getTitle());
        assertEquals(15000, foundPost.getPrice());
        assertEquals("컴퓨터공학과 3학년", foundPost.getSellerName());
        assertEquals("필기 없이 깨끗합니다.", foundPost.getDescription());
        assertEquals(TradeStatus.SALE, foundPost.getStatus());
        assertNotNull(foundPost.getCreatedAt());
        assertNotNull(foundPost.getUpdatedAt());
    }

    @Test
    void updateExistingPost() {
        TradePost savedPost = repository.save("기존 제목", 10000, "판매자", "기존 설명");
        repository.updateStatus(savedPost.getId(), TradeStatus.RESERVED).orElseThrow();

        TradePost updatedPost = repository.update(
                        savedPost.getId(),
                        "수정 제목",
                        20000,
                        "수정 판매자",
                        "수정 설명"
                )
                .orElseThrow();

        assertEquals(savedPost.getId(), updatedPost.getId());
        assertEquals("수정 제목", repository.findById(savedPost.getId()).orElseThrow().getTitle());
        assertEquals(TradeStatus.RESERVED, updatedPost.getStatus());
        assertEquals(savedPost.getCreatedAt(), updatedPost.getCreatedAt());
        assertNotNull(updatedPost.getUpdatedAt());
    }

    @Test
    void updateStatusChangesStatusAndPreservesCreatedAt() {
        TradePost savedPost = repository.save("상태 변경 글", 10000, "판매자", "설명");

        TradePost updatedPost = repository.updateStatus(savedPost.getId(), TradeStatus.SOLD)
                .orElseThrow();

        assertEquals(TradeStatus.SOLD, updatedPost.getStatus());
        assertEquals(savedPost.getCreatedAt(), updatedPost.getCreatedAt());
        assertNotNull(updatedPost.getUpdatedAt());
    }

    @Test
    void searchCombinesKeywordAndStatusFilters() {
        repository.save("Spring MVC", 15000, "판매자1", "설명1");
        TradePost springSoldPost = repository.save(
                "Spring DB", 12000, "판매자2", "설명2"
        );
        repository.save("Java 기초", 10000, "판매자3", "설명3");

        repository.updateStatus(springSoldPost.getId(), TradeStatus.SOLD).orElseThrow();

        List<TradePost> allResults = repository.search("", null);
        List<TradePost> keywordResults = repository.search("spring", null);
        List<TradePost> statusResults = repository.search("", TradeStatus.SOLD);
        List<TradePost> combinedResults = repository.search("spring", TradeStatus.SOLD);

        assertEquals(3, allResults.size());
        assertEquals(2, keywordResults.size());
        assertEquals(1, statusResults.size());
        assertEquals(springSoldPost.getId(), statusResults.get(0).getId());
        assertEquals(1, combinedResults.size());
        assertEquals(springSoldPost.getId(), combinedResults.get(0).getId());
    }

    @Test
    void deleteExistingPost() {
        TradePost savedPost = repository.save("삭제할 글", 5000, "판매자", "설명");

        boolean deleted = repository.deleteById(savedPost.getId());

        assertTrue(deleted);
        assertTrue(repository.findById(savedPost.getId()).isEmpty());
    }

    @Test
    void updateAndDeleteReturnFailureWhenPostDoesNotExist() {
        assertTrue(repository.update(999999L, "없는 글", 1000, "판매자", "설명").isEmpty());
        assertTrue(repository.updateStatus(999999L, TradeStatus.SOLD).isEmpty());
        assertFalse(repository.deleteById(999999L));
    }
}
