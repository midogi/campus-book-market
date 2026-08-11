package com.skc04.campusbookmarket.post.service;

import com.skc04.campusbookmarket.post.domain.TradePost;
import com.skc04.campusbookmarket.post.domain.TradePostSort;
import com.skc04.campusbookmarket.post.domain.TradeStatus;
import com.skc04.campusbookmarket.post.repository.TradePostRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;


@Service
public class TradePostService {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final TradePostRepository tradePostRepository;

    public TradePostService(TradePostRepository tradePostRepository) {
        this.tradePostRepository = tradePostRepository;
    }

    public List<TradePost> findAll() {
        return tradePostRepository.findAll();
    }

    public List<TradePost> search(String keyword, TradeStatus status) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        return tradePostRepository.search(normalizedKeyword, status);
    }

    public List<TradePost> search(
            String keyword,
            TradeStatus status,
            TradePostSort sort
    ) {
        String normalizedKeyword = normalizeKeyword(keyword);
        TradePostSort normalizedSort = normalizeSort(sort);
        return tradePostRepository.search(normalizedKeyword, status, normalizedSort);
    }

    public TradePostPage search(
            String keyword,
            TradeStatus status,
            TradePostSort sort,
            int requestedPage
    ) {
        String normalizedKeyword = normalizeKeyword(keyword);
        TradePostSort normalizedSort = normalizeSort(sort);
        long totalElements = tradePostRepository.count(normalizedKeyword, status);
        int totalPages = calculateTotalPages(totalElements);
        int currentPage = normalizePage(requestedPage, totalPages);
        int offset = (currentPage - 1) * DEFAULT_PAGE_SIZE;

        List<TradePost> posts = tradePostRepository.search(
                normalizedKeyword,
                status,
                normalizedSort,
                DEFAULT_PAGE_SIZE,
                offset
        );

        return new TradePostPage(
                posts,
                currentPage,
                DEFAULT_PAGE_SIZE,
                totalElements
        );
    }

    public Optional<TradePost> findById(Long postId) {
        return tradePostRepository.findById(postId);
    }

    public TradePost create(String title, long price, String sellerName, String description) {
        return tradePostRepository.save(title, price, sellerName, description);
    }

    public Optional<TradePost> update(
            Long postId,
            String title,
            long price,
            String sellerName,
            String description
    ) {
        return tradePostRepository.update(postId, title, price, sellerName, description);
    }

    public Optional<TradePost> updateStatus(Long postId, TradeStatus status) {
        return tradePostRepository.updateStatus(postId, status);
    }

    public boolean delete(Long postId) {
        return tradePostRepository.deleteById(postId);
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim();
    }

    private TradePostSort normalizeSort(TradePostSort sort) {
        return sort == null ? TradePostSort.LATEST : sort;
    }

    private int calculateTotalPages(long totalElements) {
        return (int) Math.ceil((double) totalElements / DEFAULT_PAGE_SIZE);
    }

    private int normalizePage(int requestedPage, int totalPages) {
        int positivePage = Math.max(1, requestedPage);
        return totalPages == 0 ? 1 : Math.min(positivePage, totalPages);
    }
}
