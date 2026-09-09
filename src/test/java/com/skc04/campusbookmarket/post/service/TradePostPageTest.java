package com.skc04.campusbookmarket.post.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class TradePostPageTest {

    @Test
    void calculatesTotalPagesAndPageWindow() {
        TradePostPage page = new TradePostPage(List.of(), 6, 10, 120);

        assertEquals(12, page.getTotalPages());
        assertEquals(List.of(4, 5, 6, 7, 8), page.getPageNumbers());
        assertEquals(5, page.getPreviousPage());
        assertEquals(7, page.getNextPage());
        assertFalse(page.isFirst());
        assertFalse(page.isLast());
    }

    @Test
    void emptyResultIsBothFirstAndLastPage() {
        TradePostPage page = new TradePostPage(List.of(), 1, 10, 0);

        assertEquals(0, page.getTotalPages());
        assertTrue(page.getPageNumbers().isEmpty());
        assertTrue(page.isFirst());
        assertTrue(page.isLast());
    }
}
