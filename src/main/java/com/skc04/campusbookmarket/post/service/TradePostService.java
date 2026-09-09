package com.skc04.campusbookmarket.post.service;

import com.skc04.campusbookmarket.file.FileStore;
import com.skc04.campusbookmarket.file.StoredFile;
import com.skc04.campusbookmarket.member.domain.Member;
import com.skc04.campusbookmarket.post.domain.TradePost;
import com.skc04.campusbookmarket.post.domain.TradePostSearchType;
import com.skc04.campusbookmarket.post.domain.TradePostSort;
import com.skc04.campusbookmarket.post.domain.TradeStatus;
import com.skc04.campusbookmarket.post.repository.TradePostRepository;
import com.skc04.campusbookmarket.trade.service.PostTradePolicy;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 컨트롤러의 요청을 저장소 작업으로 연결하고 검색·페이징 규칙을 적용한다.
 * 저장 기술은 TradePostRepository 인터페이스 뒤에 숨겨져 있다.
 */
@Service
@Transactional(readOnly = true)
public class TradePostService {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final TradePostRepository tradePostRepository;
    private final FileStore fileStore;
    private final PostTradePolicy tradePolicy;

    public TradePostService(
            TradePostRepository tradePostRepository,
            FileStore fileStore,
            PostTradePolicy tradePolicy
    ) {
        this.tradePostRepository = tradePostRepository;
        this.fileStore = fileStore;
        this.tradePolicy = tradePolicy;
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
        foundPost.ifPresent(tradePolicy::validateEditable);
        return foundPost;
    }

    private Optional<TradePost> findOwnedForUpdate(Long postId, Long memberId) {
        Optional<TradePost> post = tradePostRepository.findByIdForUpdate(postId);
        post.ifPresent(value -> validateOwner(value, memberId));
        return post;
    }

    @Transactional
    public TradePost create(String title, long price, Member seller, String description) {
        return create(title, price, seller, description, null);
    }

    /** 이미지 파일 저장과 게시글 DB 저장을 하나의 등록 작업으로 조정한다. */
    @Transactional
    public TradePost create(
            String title,
            long price,
            Member seller,
            String description,
            MultipartFile imageFile
    ) {
        Optional<StoredFile> storedFile = fileStore.store(imageFile);
        try {
            TradePost savedPost = tradePostRepository.save(
                    title,
                    price,
                    seller,
                    description
            );
            if (storedFile.isEmpty()) {
                return savedPost;
            }

            StoredFile image = storedFile.get();
            return tradePostRepository.updateImage(
                            savedPost.getId(),
                            image.originalName(),
                            image.storedName()
                    )
                    .orElseThrow(() -> new IllegalStateException(
                            "저장한 게시글에 이미지를 연결하지 못했습니다."
                    ));
        } catch (RuntimeException exception) {
            storedFile.ifPresent(file -> fileStore.delete(file.storedName()));
            throw exception;
        }
    }

    @Transactional
    public Optional<TradePost> update(
            Long postId,
            Long memberId,
            String title,
            long price,
            String description
    ) {
        return update(
                postId,
                memberId,
                title,
                price,
                description,
                null,
                false
        );
    }

    /** 내용 수정과 대표 이미지 교체·삭제를 함께 처리한다. */
    @Transactional
    public Optional<TradePost> update(
            Long postId,
            Long memberId,
            String title,
            long price,
            String description,
            MultipartFile imageFile,
            boolean removeImage
    ) {
        Optional<TradePost> foundPost = findOwnedForUpdate(postId, memberId);
        if (foundPost.isEmpty()) {
            return Optional.empty();
        }

        TradePost originalPost = foundPost.get();
        tradePolicy.validateEditable(originalPost);
        String previousStoredName = originalPost.getImageStoredName();
        Optional<StoredFile> newImage = fileStore.store(imageFile);

        try {
            Optional<TradePost> updatedPost = tradePostRepository.update(
                    postId,
                    title,
                    price,
                    description
            );
            if (updatedPost.isEmpty()) {
                newImage.ifPresent(file -> fileStore.delete(file.storedName()));
                return Optional.empty();
            }

            boolean imageChanged = newImage.isPresent() || removeImage;
            if (imageChanged) {
                String originalName = newImage.map(StoredFile::originalName).orElse(null);
                String storedName = newImage.map(StoredFile::storedName).orElse(null);
                updatedPost = tradePostRepository.updateImage(
                        postId,
                        originalName,
                        storedName
                );
                if (updatedPost.isEmpty()) {
                    throw new IllegalStateException("수정한 게시글의 이미지를 변경하지 못했습니다.");
                }
                fileStore.delete(previousStoredName);
            }
            return updatedPost;
        } catch (RuntimeException exception) {
            newImage.ifPresent(file -> fileStore.delete(file.storedName()));
            throw exception;
        }
    }

    @Transactional
    public Optional<TradePost> updateStatus(
            Long postId,
            Long memberId,
            TradeStatus status
    ) {
        Optional<TradePost> foundPost = findOwnedForUpdate(postId, memberId);
        if (foundPost.isEmpty()) {
            return Optional.empty();
        }
        tradePolicy.validateManualStatus(foundPost.get(), status);
        return tradePostRepository.updateStatus(postId, status);
    }

    @Transactional
    public boolean delete(Long postId, Long memberId) {
        Optional<TradePost> foundPost = findOwnedForUpdate(postId, memberId);
        if (foundPost.isEmpty()) {
            return false;
        }
        tradePolicy.validateDeletable(foundPost.get());
        String storedName = foundPost.get().getImageStoredName();
        boolean deleted = tradePostRepository.deleteById(postId);
        if (deleted) {
            fileStore.delete(storedName);
        }
        return deleted;
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
