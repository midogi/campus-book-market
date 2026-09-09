package com.skc04.campusbookmarket.post.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.skc04.campusbookmarket.file.FileStore;
import com.skc04.campusbookmarket.file.StoredFile;
import com.skc04.campusbookmarket.member.domain.Member;
import com.skc04.campusbookmarket.post.domain.TradePost;
import com.skc04.campusbookmarket.post.domain.TradePostSearchType;
import com.skc04.campusbookmarket.post.domain.TradePostSort;
import com.skc04.campusbookmarket.post.domain.TradeStatus;
import com.skc04.campusbookmarket.post.repository.TradePostRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class TradePostServiceTest {

    @Mock
    private TradePostRepository tradePostRepository;

    @Mock
    private FileStore fileStore;

    @Mock
    private com.skc04.campusbookmarket.trade.service.PostTradePolicy tradePolicy;

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
        given(tradePostRepository.count(
                "Spring", TradePostSearchType.SELLER, TradeStatus.SALE
        ))
                .willReturn(25L);
        given(tradePostRepository.search(
                "Spring",
                TradePostSearchType.SELLER,
                TradeStatus.SALE,
                TradePostSort.PRICE_DESC,
                10,
                10
        )).willReturn(posts);

        TradePostPage result = tradePostService.search(
                "  Spring  ",
                TradePostSearchType.SELLER,
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
                TradePostSearchType.SELLER,
                TradeStatus.SALE,
                TradePostSort.PRICE_DESC,
                10,
                10
        );
    }

    @Test
    void searchUsesDefaultsAndClampsPageToAvailableRange() {
        List<TradePost> posts = List.of(samplePost());
        given(tradePostRepository.count(
                "", TradePostSearchType.TITLE, null
        )).willReturn(12L);
        given(tradePostRepository.search(
                "",
                TradePostSearchType.TITLE,
                null,
                TradePostSort.LATEST,
                10,
                10
        )).willReturn(posts);

        TradePostPage result = tradePostService.search(
                null, null, null, null, 99
        );

        assertEquals(2, result.getCurrentPage());
        assertEquals(2, result.getTotalPages());
        verify(tradePostRepository).search(
                "",
                TradePostSearchType.TITLE,
                null,
                TradePostSort.LATEST,
                10,
                10
        );
    }

    @Test
    void createUsesLoginMemberAsWriter() {
        Member writer = member(1L, "writer", "학생 판매자");
        TradePost savedPost = samplePost(writer);
        given(tradePostRepository.save(
                "Spring Basics", 15000L, writer, "Clean copy"
        )).willReturn(savedPost);

        TradePost result = tradePostService.create(
                "Spring Basics", 15000L, writer, "Clean copy"
        );

        assertEquals(savedPost, result);
        verify(tradePostRepository).save(
                "Spring Basics", 15000L, writer, "Clean copy"
        );
    }

    @Test
    void createStoresImageMetadataAndReturnsUpdatedPost() {
        Member writer = member(1L, "writer", "학생 판매자");
        TradePost savedPost = samplePost(writer);
        MultipartFile imageFile = org.mockito.Mockito.mock(MultipartFile.class);
        StoredFile storedFile = new StoredFile("book.png", "uuid.png");
        given(fileStore.store(imageFile)).willReturn(Optional.of(storedFile));
        given(tradePostRepository.save(
                "Spring Basics", 15000L, writer, "Clean copy"
        )).willReturn(savedPost);
        savedPost.changeImage("book.png", "uuid.png");
        given(tradePostRepository.updateImage(
                1L, "book.png", "uuid.png"
        )).willReturn(Optional.of(savedPost));

        TradePost result = tradePostService.create(
                "Spring Basics", 15000L, writer, "Clean copy", imageFile
        );

        assertEquals("uuid.png", result.getImageStoredName());
        verify(tradePostRepository).updateImage(1L, "book.png", "uuid.png");
    }

    @Test
    void createDeletesNewFileWhenDatabaseSaveFails() {
        Member writer = member(1L, "writer", "학생 판매자");
        MultipartFile imageFile = org.mockito.Mockito.mock(MultipartFile.class);
        given(fileStore.store(imageFile))
                .willReturn(Optional.of(new StoredFile("book.png", "uuid.png")));
        given(tradePostRepository.save(
                "Spring Basics", 15000L, writer, "Clean copy"
        )).willThrow(new IllegalStateException("DB failure"));

        assertThrows(IllegalStateException.class, () -> tradePostService.create(
                "Spring Basics", 15000L, writer, "Clean copy", imageFile
        ));

        verify(fileStore).delete("uuid.png");
    }

    @Test
    void writerCanReplacePostImage() {
        Member writer = member(1L, "writer", "학생 판매자");
        TradePost post = samplePost(writer);
        post.changeImage("old.png", "old-uuid.png");
        MultipartFile imageFile = org.mockito.Mockito.mock(MultipartFile.class);
        given(tradePostRepository.findByIdForUpdate(1L)).willReturn(Optional.of(post));
        given(fileStore.store(imageFile))
                .willReturn(Optional.of(new StoredFile("new.png", "new-uuid.png")));
        given(tradePostRepository.update(
                1L, "Updated", 20000L, "Updated description"
        )).willReturn(Optional.of(post));
        given(tradePostRepository.updateImage(
                1L, "new.png", "new-uuid.png"
        )).willReturn(Optional.of(post));

        Optional<TradePost> result = tradePostService.update(
                1L,
                writer.getId(),
                "Updated",
                20000L,
                "Updated description",
                imageFile,
                false
        );

        assertTrue(result.isPresent());
        verify(fileStore).delete("old-uuid.png");
    }

    @Test
    void writerCanUpdatePost() {
        Member writer = member(1L, "writer", "학생 판매자");
        TradePost post = samplePost(writer);
        given(tradePostRepository.findByIdForUpdate(1L)).willReturn(Optional.of(post));
        given(tradePostRepository.update(
                1L, "Updated", 20000L, "Updated description"
        )).willReturn(Optional.of(post));

        Optional<TradePost> result = tradePostService.update(
                1L, writer.getId(), "Updated", 20000L, "Updated description"
        );

        assertTrue(result.isPresent());
        verify(tradePostRepository).update(
                1L, "Updated", 20000L, "Updated description"
        );
    }

    @Test
    void otherMemberCannotUpdatePost() {
        Member writer = member(1L, "writer", "학생 판매자");
        Member otherMember = member(2L, "other", "다른 회원");
        given(tradePostRepository.findByIdForUpdate(1L))
                .willReturn(Optional.of(samplePost(writer)));

        assertThrows(PostAccessDeniedException.class, () ->
                tradePostService.update(
                        1L,
                        otherMember.getId(),
                        "Hacked",
                        1L,
                        "Hacked description"
                )
        );

        verify(tradePostRepository, never()).update(
                1L, "Hacked", 1L, "Hacked description"
        );
    }

    @Test
    void writerCanDeletePost() {
        Member writer = member(1L, "writer", "학생 판매자");
        given(tradePostRepository.findByIdForUpdate(1L))
                .willReturn(Optional.of(samplePost(writer)));
        given(tradePostRepository.deleteById(1L)).willReturn(true);

        boolean deleted = tradePostService.delete(1L, writer.getId());

        assertTrue(deleted);
        verify(tradePostRepository).deleteById(1L);
    }

    @Test
    void otherMemberCannotDeletePost() {
        Member writer = member(1L, "writer", "학생 판매자");
        Member otherMember = member(2L, "other", "다른 회원");
        given(tradePostRepository.findByIdForUpdate(1L))
                .willReturn(Optional.of(samplePost(writer)));

        assertThrows(PostAccessDeniedException.class, () ->
                tradePostService.delete(1L, otherMember.getId())
        );

        verify(tradePostRepository, never()).deleteById(1L);
    }

    @Test
    void otherMemberCannotChangeTradeStatus() {
        Member writer = member(1L, "writer", "학생 판매자");
        Member otherMember = member(2L, "other", "다른 회원");
        given(tradePostRepository.findByIdForUpdate(1L))
                .willReturn(Optional.of(samplePost(writer)));

        assertThrows(PostAccessDeniedException.class, () ->
                tradePostService.updateStatus(
                        1L, otherMember.getId(), TradeStatus.SOLD
                )
        );

        verify(tradePostRepository, never())
                .updateStatus(1L, TradeStatus.SOLD);
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

    private TradePost samplePost(Member seller) {
        TradePost post = new TradePost(
                "Spring Basics",
                15000L,
                seller,
                "Clean copy"
        );
        ReflectionTestUtils.setField(post, "id", 1L);
        return post;
    }

    private Member member(Long id, String loginId, String name) {
        Member member = new Member(loginId, "encoded-password", name);
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }
}
