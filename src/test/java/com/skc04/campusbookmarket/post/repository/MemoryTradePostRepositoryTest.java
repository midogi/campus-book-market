package com.skc04.campusbookmarket.post.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.skc04.campusbookmarket.post.domain.TradePost;
import org.junit.jupiter.api.Test;

class MemoryTradePostRepositoryTest {

    @Test
    void save_assignsId_andStoresPost() {
        MemoryTradePostRepository repository = new MemoryTradePostRepository();

        TradePost savedPost = repository.save("스프링 입문", 15000, "컴퓨터공학과 3학년");

        assertEquals(4L, savedPost.getId());
        assertEquals(4, repository.findAll().size());

        TradePost foundPost = repository.findById(savedPost.getId()).orElseThrow();
        assertEquals("스프링 입문", foundPost.getTitle());
        assertEquals(15000, foundPost.getPrice());
        assertEquals("컴퓨터공학과 3학년", foundPost.getSellerName());
    }

    @Test
    void findById_returnsEmpty_whenPostDoesNotExist() {
        MemoryTradePostRepository repository = new MemoryTradePostRepository();

        assertTrue(repository.findById(999L).isEmpty());
    }
}
