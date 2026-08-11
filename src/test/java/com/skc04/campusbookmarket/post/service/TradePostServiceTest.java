package com.skc04.campusbookmarket.post.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.skc04.campusbookmarket.post.domain.TradePost;
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
