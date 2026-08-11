package com.skc04.campusbookmarket.post.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.skc04.campusbookmarket.post.domain.TradePost;
import com.skc04.campusbookmarket.post.domain.TradePostSort;
import com.skc04.campusbookmarket.post.domain.TradeStatus;
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
    void searchTrimsKeywordAndPassesStatus() {
        List<TradePost> posts = List.of(samplePost());
        given(tradePostRepository.search("Spring", TradeStatus.SALE))
                .willReturn(posts);

        List<TradePost> result = tradePostService.search(
                "  Spring  ",
                TradeStatus.SALE
        );

        assertEquals(posts, result);
        verify(tradePostRepository).search("Spring", TradeStatus.SALE);
    }

    @Test
    void searchConvertsNullKeywordToEmptyString() {
        List<TradePost> posts = List.of(samplePost());
        given(tradePostRepository.search("", null)).willReturn(posts);

        List<TradePost> result = tradePostService.search(null, null);

        assertEquals(posts, result);
        verify(tradePostRepository).search("", null);
    }

    @Test
    void searchCalculatesPageOffsetAndMetadata() {
        List<TradePost> posts = List.of(samplePost());
        given(tradePostRepository.count("Spring", TradeStatus.SALE))
                .willReturn(25L);
        given(tradePostRepository.search(
                "Spring",
                TradeStatus.SALE,
                TradePostSort.PRICE_DESC,
                10,
                10
        )).willReturn(posts);

        TradePostPage result = tradePostService.search(
                "  Spring  ",
                TradeStatus.SALE,
                TradePostSort.PRICE_DESC,
                2
        );

        assertEquals(posts, result.getPosts());
        assertEquals(2, result.getCurrentPage());
        assertEquals(10, result.getPageSize());
        assertEquals(25L, result.getTotalElements());
        assertEquals(3, result.getTotalPages());
        verify(tradePostRepository).search(
                "Spring",
                TradeStatus.SALE,
                TradePostSort.PRICE_DESC,
                10,
                10
        );
    }

    @Test
    void searchUsesDefaultsAndClampsPageToAvailableRange() {
        List<TradePost> posts = List.of(samplePost());
        given(tradePostRepository.count("", null)).willReturn(12L);
        given(tradePostRepository.search(
                "",
                null,
                TradePostSort.LATEST,
                10,
                10
        )).willReturn(posts);

        TradePostPage result = tradePostService.search(null, null, null, 99);

        assertEquals(2, result.getCurrentPage());
        assertEquals(2, result.getTotalPages());
        verify(tradePostRepository).search(
                "",
                null,
                TradePostSort.LATEST,
                10,
                10
        );
    }

    private TradePost samplePost() {
        return new TradePost(
                1L,
                "Spring Basics",
                15000L,
                "Student Seller",
                "Clean copy",
                TradeStatus.SALE
        );
    }
}
