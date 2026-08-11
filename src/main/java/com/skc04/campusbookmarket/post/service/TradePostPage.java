package com.skc04.campusbookmarket.post.service;

import com.skc04.campusbookmarket.post.domain.TradePost;
import java.util.ArrayList;
import java.util.List;

public class TradePostPage {

    private static final int PAGE_WINDOW_SIZE = 5;

    private final List<TradePost> posts;
    private final int currentPage;
    private final int pageSize;
    private final long totalElements;

    public TradePostPage(
            List<TradePost> posts,
            int currentPage,
            int pageSize,
            long totalElements
    ) {
        this.posts = List.copyOf(posts);
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
    }

    public List<TradePost> getPosts() {
        return posts;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return (int) Math.ceil((double) totalElements / pageSize);
    }

    public boolean isFirst() {
        return currentPage <= 1;
    }

    public boolean isLast() {
        return getTotalPages() == 0 || currentPage >= getTotalPages();
    }

    public int getPreviousPage() {
        return Math.max(1, currentPage - 1);
    }

    public int getNextPage() {
        return Math.min(getTotalPages(), currentPage + 1);
    }

    public List<Integer> getPageNumbers() {
        int totalPages = getTotalPages();
        if (totalPages == 0) {
            return List.of();
        }

        int startPage = Math.max(1, currentPage - PAGE_WINDOW_SIZE / 2);
        int endPage = Math.min(totalPages, startPage + PAGE_WINDOW_SIZE - 1);
        startPage = Math.max(1, endPage - PAGE_WINDOW_SIZE + 1);

        List<Integer> pageNumbers = new ArrayList<>();
        for (int pageNumber = startPage; pageNumber <= endPage; pageNumber++) {
            pageNumbers.add(pageNumber);
        }
        return pageNumbers;
    }
}
