package com.skc04.campusbookmarket.post.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.skc04.campusbookmarket.post.domain.TradePost;
import com.skc04.campusbookmarket.post.repository.TradePostRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TradePostServiceTest {

    @Mock
    private TradePostRepository tradePostRepository;

    @InjectMocks
    private TradePostService tradePostService;

    @Test
    void searchReturnsAllPostsWhenKeywordIsBlank() {
        List<TradePost> posts = List.of(samplePost());
        given(tradePostRepository.findAll()).willReturn(posts);

        List<TradePost> result = tradePostService.search("   ");

        assertEquals(posts, result);
        verify(tradePostRepository, never()).searchByTitle(anyString());
    }

    @Test
    void searchTrimsKeywordBeforeRepositoryCall() {
        List<TradePost> posts = List.of(samplePost());
        given(tradePostRepository.searchByTitle("Spring")).willReturn(posts);

        List<TradePost> result = tradePostService.search("  Spring  ");

        assertEquals(posts, result);
        verify(tradePostRepository).searchByTitle("Spring");
    }

    private TradePost samplePost() {
        return new TradePost(
                1L, "Spring Basics", 15000L, "Student Seller", "Clean copy"
        );
    }
}
