package com.skc04.campusbookmarket.post.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.skc04.campusbookmarket.post.domain.TradePost;
import java.util.List;
import org.junit.jupiter.api.Test;

class MemoryTradePostRepositoryTest {

    @Test
    void save_assignsId_andStoresPost() {
        MemoryTradePostRepository repository = new MemoryTradePostRepository();

        TradePost savedPost = repository.save(
                "스프링 입문",
                15000,
                "컴퓨터공학과 3학년",
                "필기 없이 깨끗합니다."
        );

        assertEquals(4L, savedPost.getId());
        assertEquals(4, repository.findAll().size());

        TradePost foundPost = repository.findById(savedPost.getId()).orElseThrow();
        assertEquals("스프링 입문", foundPost.getTitle());
        assertEquals(15000, foundPost.getPrice());
        assertEquals("컴퓨터공학과 3학년", foundPost.getSellerName());
        assertEquals("필기 없이 깨끗합니다.", foundPost.getDescription());
    }

    @Test
    void update_replacesExistingPost_andPreservesId() {
        MemoryTradePostRepository repository = new MemoryTradePostRepository();

        TradePost updatedPost = repository.update(
                        1L,
                        "수정한 제목",
                        20000,
                        "소프트웨어학과 3학년",
                        "수정한 상품 설명"
                )
                .orElseThrow();

        assertEquals(1L, updatedPost.getId());
        assertEquals(3, repository.findAll().size());

        TradePost foundPost = repository.findById(1L).orElseThrow();
        assertEquals("수정한 제목", foundPost.getTitle());
        assertEquals(20000, foundPost.getPrice());
        assertEquals("소프트웨어학과 3학년", foundPost.getSellerName());
        assertEquals("수정한 상품 설명", foundPost.getDescription());
    }

    @Test
    void findById_returnsEmpty_whenPostDoesNotExist() {
        MemoryTradePostRepository repository = new MemoryTradePostRepository();

        assertTrue(repository.findById(999L).isEmpty());
    }

    @Test
    void searchByTitle_returnsOnlyMatchingPosts() {
        MemoryTradePostRepository repository = new MemoryTradePostRepository();

        List<TradePost> results = repository.searchByTitle("operating");

        assertEquals(1, results.size());
        assertEquals("Operating System Concepts", results.get(0).getTitle());
    }

    @Test
    void update_returnsEmpty_whenPostDoesNotExist() {
        MemoryTradePostRepository repository = new MemoryTradePostRepository();

        assertTrue(repository.update(999L, "없는 게시글", 10000, "판매자", "설명").isEmpty());
    }

    @Test
    void deleteById_removesExistingPost() {
        MemoryTradePostRepository repository = new MemoryTradePostRepository();

        boolean deleted = repository.deleteById(1L);

        assertTrue(deleted);
        assertTrue(repository.findById(1L).isEmpty());
        assertEquals(2, repository.findAll().size());
    }

    @Test
    void deleteById_returnsFalse_whenPostDoesNotExist() {
        MemoryTradePostRepository repository = new MemoryTradePostRepository();

        boolean deleted = repository.deleteById(999L);

        assertFalse(deleted);
        assertEquals(3, repository.findAll().size());
    }
}
