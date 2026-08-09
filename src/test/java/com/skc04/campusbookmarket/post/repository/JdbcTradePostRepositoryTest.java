package com.skc04.campusbookmarket.post.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.skc04.campusbookmarket.post.domain.TradePost;
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
    }

    @Test
    void updateExistingPost() {
        TradePost savedPost = repository.save("기존 제목", 10000, "판매자", "기존 설명");

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
        assertFalse(repository.deleteById(999999L));
    }
}
