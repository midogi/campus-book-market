package com.skc04.campusbookmarket.post.service;

import com.skc04.campusbookmarket.member.domain.Member;
import com.skc04.campusbookmarket.post.domain.TradePost;
import com.skc04.campusbookmarket.post.domain.TradePostSearchType;
import com.skc04.campusbookmarket.post.domain.TradePostSort;
import com.skc04.campusbookmarket.post.domain.TradeStatus;
import com.skc04.campusbookmarket.post.repository.TradePostRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 컨트롤러의 요청을 저장소 작업으로 연결하고 검색·페이징 규칙을 적용한다.
 * 저장 기술은 TradePostRepository 인터페이스 뒤에 숨겨져 있다.
 */
@Service
@Transactional(readOnly = true)
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
            TradePostSearchType searchType,
            TradeStatus status,
            TradePostSort sort,
            int requestedPage
    ) {
        // URL 파라미터가 비어 있거나 null이어도 저장소에는 정규화된 값을 전달한다.
        String normalizedKeyword = normalizeKeyword(keyword);
        TradePostSearchType normalizedSearchType = normalizeSearchType(searchType);
        TradePostSort normalizedSort = normalizeSort(sort);
        long totalElements = tradePostRepository.count(
                normalizedKeyword,
                normalizedSearchType,
                status
        );
        int totalPages = calculateTotalPages(totalElements);
        int currentPage = normalizePage(requestedPage, totalPages);
        int offset = (currentPage - 1) * DEFAULT_PAGE_SIZE;

        // 전체 개수는 페이지 정보에, limit/offset 조회 결과는 현재 화면에 사용한다.
        List<TradePost> posts = tradePostRepository.search(
                normalizedKeyword,
                normalizedSearchType,
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

    /** 수정 화면을 열기 전에도 작성자 권한을 검사한다. */
    public Optional<TradePost> findOwnedById(Long postId, Long memberId) {
        Optional<TradePost> foundPost = tradePostRepository.findById(postId);
        foundPost.ifPresent(post -> validateOwner(post, memberId));
        return foundPost;
    }

    @Transactional
    public TradePost create(String title, long price, Member seller, String description) {
        return tradePostRepository.save(title, price, seller, description);
    }

    @Transactional
    public Optional<TradePost> update(
            Long postId,
            Long memberId,
            String title,
            long price,
            String description
    ) {
        Optional<TradePost> foundPost = findOwnedById(postId, memberId);
        if (foundPost.isEmpty()) {
            return Optional.empty();
        }
        return tradePostRepository.update(postId, title, price, description);
    }

    @Transactional
    public Optional<TradePost> updateStatus(
            Long postId,
            Long memberId,
            TradeStatus status
    ) {
        Optional<TradePost> foundPost = findOwnedById(postId, memberId);
        if (foundPost.isEmpty()) {
            return Optional.empty();
        }
        return tradePostRepository.updateStatus(postId, status);
    }

    @Transactional
    public boolean delete(Long postId, Long memberId) {
        Optional<TradePost> foundPost = findOwnedById(postId, memberId);
        if (foundPost.isEmpty()) {
            return false;
        }
        return tradePostRepository.deleteById(postId);
    }

    private void validateOwner(TradePost post, Long memberId) {
        if (!post.isWrittenBy(memberId)) {
            throw new PostAccessDeniedException();
        }
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim();
    }

    private TradePostSearchType normalizeSearchType(TradePostSearchType searchType) {
        return searchType == null ? TradePostSearchType.TITLE : searchType;
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
