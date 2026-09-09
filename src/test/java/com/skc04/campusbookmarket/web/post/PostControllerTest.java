package com.skc04.campusbookmarket.web.post;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.skc04.campusbookmarket.config.WebConfig;
import com.skc04.campusbookmarket.file.InvalidImageException;
import com.skc04.campusbookmarket.member.domain.Member;
import com.skc04.campusbookmarket.post.domain.TradePost;
import com.skc04.campusbookmarket.post.domain.TradePostSearchType;
import com.skc04.campusbookmarket.post.domain.TradePostSort;
import com.skc04.campusbookmarket.post.domain.TradeStatus;
import com.skc04.campusbookmarket.post.service.PostAccessDeniedException;
import com.skc04.campusbookmarket.post.service.TradePostPage;
import com.skc04.campusbookmarket.post.service.TradePostService;
import com.skc04.campusbookmarket.web.interceptor.LoginCheckInterceptor;
import com.skc04.campusbookmarket.web.session.SessionConst;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PostController.class)
@Import({WebConfig.class, LoginCheckInterceptor.class})
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TradePostService tradePostService;

    private Member writer;
    private Member otherMember;
    private MockHttpSession writerSession;
    private MockHttpSession otherMemberSession;

    @BeforeEach
    void setUpLoginSessions() {
        writer = member(1L, "writer", "학생 판매자");
        otherMember = member(2L, "other", "다른 회원");
        writerSession = loginSession(writer);
        otherMemberSession = loginSession(otherMember);
    }

    @Test
    void listRemainsPublic() throws Exception {
        List<TradePost> posts = List.of(samplePost(writer));
        TradePostPage postPage = new TradePostPage(posts, 1, 10, 1);
        given(tradePostService.search(
                "", TradePostSearchType.TITLE, null, TradePostSort.LATEST, 1
        )).willReturn(postPage);

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", posts))
                .andExpect(model().attribute("postPage", postPage))
                .andExpect(model().attribute("keyword", ""))
                .andExpect(model().attribute(
                        "selectedSearchType", TradePostSearchType.TITLE
                ))
                .andExpect(model().attribute("selectedSort", TradePostSort.LATEST))
                .andExpect(model().attributeExists(
                        "searchTypes", "tradeStatuses", "sortOptions"
                ))
                .andExpect(content().string(containsString("name=\"searchType\"")))
                .andExpect(content().string(containsString("name=\"sort\"")))
                .andExpect(content().string(not(containsString(
                        "href=\"/posts/new\""
                ))));

        verify(tradePostService).search(
                "", TradePostSearchType.TITLE, null, TradePostSort.LATEST, 1
        );
    }

    @Test
    void listSearchesByKeywordStatusAndSortOnRequestedPage() throws Exception {
        List<TradePost> posts = List.of(samplePost(writer));
        TradePostPage postPage = new TradePostPage(posts, 2, 10, 11);
        given(tradePostService.search(
                "Spring",
                TradePostSearchType.SELLER,
                TradeStatus.SOLD,
                TradePostSort.PRICE_DESC,
                2
        )).willReturn(postPage);

        mockMvc.perform(get("/posts")
                        .param("keyword", "Spring")
                        .param("searchType", "SELLER")
                        .param("status", "SOLD")
                        .param("sort", "PRICE_DESC")
                        .param("page", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", posts))
                .andExpect(model().attribute("postPage", postPage))
                .andExpect(model().attribute("keyword", "Spring"))
                .andExpect(model().attribute(
                        "selectedSearchType", TradePostSearchType.SELLER
                ))
                .andExpect(model().attribute("selectedStatus", TradeStatus.SOLD))
                .andExpect(model().attribute("selectedSort", TradePostSort.PRICE_DESC))
                .andExpect(content().string(containsString("searchType=SELLER")))
                .andExpect(content().string(containsString("page=1")));
    }

    @Test
    void detailRemainsPublicAndHidesChangeControls() throws Exception {
        TradePost post = samplePost(writer);
        given(tradePostService.findById(1L)).willReturn(Optional.of(post));

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attribute("post", post))
                .andExpect(model().attribute("canManage", false))
                .andExpect(content().string(not(containsString("/posts/1/edit"))))
                .andExpect(content().string(not(containsString("/posts/1/status"))))
                .andExpect(content().string(not(containsString("/posts/1/delete"))));
    }

    @Test
    void detailReturnsNotFoundWhenPostDoesNotExist() throws Exception {
        given(tradePostService.findById(999L))
                .willReturn(Optional.empty());

        mockMvc.perform(get("/posts/999").locale(Locale.KOREAN))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"))
                .andExpect(content().string(
                        containsString("게시글을 찾을 수 없습니다")
                ));
    }

    @Test
    void notFoundPageUsesEnglishMessages() throws Exception {
        given(tradePostService.findById(999L))
                .willReturn(Optional.empty());

        mockMvc.perform(get("/posts/999").locale(Locale.ENGLISH))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"))
                .andExpect(content().string(containsString("Post Not Found")))
                .andExpect(content().string(containsString(
                        "This post does not exist or has been deleted."
                )));
    }

    @Test
    void writerSeesChangeControlsOnDetail() throws Exception {
        TradePost post = samplePost(writer);
        given(tradePostService.findById(1L)).willReturn(Optional.of(post));

        mockMvc.perform(get("/posts/1").session(writerSession))
                .andExpect(status().isOk())
                .andExpect(model().attribute("canManage", true))
                .andExpect(content().string(containsString("/posts/1/edit")))
                .andExpect(content().string(containsString("/posts/1/requests")))
                .andExpect(content().string(not(containsString("/posts/1/status"))))
                .andExpect(content().string(containsString("/posts/1/delete")));
    }

    @Test
    void otherMemberDoesNotSeeChangeControlsOnDetail() throws Exception {
        TradePost post = samplePost(writer);
        given(tradePostService.findById(1L)).willReturn(Optional.of(post));

        mockMvc.perform(get("/posts/1").session(otherMemberSession))
                .andExpect(status().isOk())
                .andExpect(model().attribute("canManage", false))
                .andExpect(content().string(not(containsString("/posts/1/edit"))))
                .andExpect(content().string(not(containsString("/posts/1/delete"))));
    }

    @Test
    void createForm() throws Exception {
        mockMvc.perform(get("/posts/new").session(writerSession))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/new"))
                .andExpect(model().attributeExists("postCreateForm"));
    }

    @Test
    void createFormUsesEnglishMessages() throws Exception {
        mockMvc.perform(get("/posts/new")
                        .session(writerSession)
                        .locale(Locale.ENGLISH))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/new"))
                .andExpect(content().string(containsString("Create Post")))
                .andExpect(content().string(containsString("Description")))
                .andExpect(content().string(containsString("Back to List")));
    }

    @Test
    void createUsesLoginMemberAsWriter() throws Exception {
        TradePost savedPost = samplePost(writer);
        given(tradePostService.create(
                "Spring Basics", 15000L, writer, "Clean copy", null
        )).willReturn(savedPost);

        mockMvc.perform(post("/posts")
                        .session(writerSession)
                        .param("title", "Spring Basics")
                        .param("price", "15000")
                        .param("description", "Clean copy"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/1"));

        verify(tradePostService).create(
                "Spring Basics", 15000L, writer, "Clean copy", null
        );
    }

    @Test
    void createAcceptsRepresentativeImage() throws Exception {
        TradePost savedPost = samplePost(writer);
        MockMultipartFile image = new MockMultipartFile(
                "imageFile",
                "book.png",
                "image/png",
                new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47}
        );
        given(tradePostService.create(
                "Spring Basics", 15000L, writer, "Clean copy", image
        )).willReturn(savedPost);

        mockMvc.perform(multipart("/posts")
                        .file(image)
                        .session(writerSession)
                        .param("title", "Spring Basics")
                        .param("price", "15000")
                        .param("description", "Clean copy"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/1"));

        verify(tradePostService).create(
                "Spring Basics", 15000L, writer, "Clean copy", image
        );
    }

    @Test
    void createShowsImageValidationError() throws Exception {
        given(tradePostService.create(
                "Spring Basics", 15000L, writer, "Clean copy", null
        )).willThrow(new InvalidImageException("지원하지 않는 이미지입니다."));

        mockMvc.perform(post("/posts")
                        .session(writerSession)
                        .param("title", "Spring Basics")
                        .param("price", "15000")
                        .param("description", "Clean copy"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/new"))
                .andExpect(model().attributeHasFieldErrors(
                        "postCreateForm", "imageFile"
                ));
    }

    @Test
    void createRejectsInvalidInput() throws Exception {
        mockMvc.perform(post("/posts")
                        .session(writerSession)
                        .param("title", " ")
                        .param("price", "-1")
                        .param("description", " "))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/new"))
                .andExpect(model().attributeHasFieldErrors(
                        "postCreateForm", "title", "price", "description"
                ));

        verify(tradePostService, never()).create(
                anyString(), anyLong(), any(Member.class), anyString(),
                nullable(org.springframework.web.multipart.MultipartFile.class)
        );
    }

    @Test
    void writerCanOpenUpdateForm() throws Exception {
        TradePost post = samplePost(writer);
        given(tradePostService.findOwnedById(1L, writer.getId()))
                .willReturn(Optional.of(post));

        mockMvc.perform(get("/posts/1/edit").session(writerSession))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/edit"))
                .andExpect(model().attribute("postId", 1L))
                .andExpect(model().attributeExists("postUpdateForm"));
    }

    @Test
    void writerCanUpdatePost() throws Exception {
        TradePost existingPost = samplePost(writer);
        TradePost updatedPost = samplePost(writer);
        updatedPost.updateDetails("Updated Title", 20000L, "Updated description");
        given(tradePostService.findOwnedById(1L, writer.getId()))
                .willReturn(Optional.of(existingPost));
        given(tradePostService.update(
                1L,
                writer.getId(),
                "Updated Title",
                20000L,
                "Updated description",
                null,
                false
        )).willReturn(Optional.of(updatedPost));

        mockMvc.perform(post("/posts/1/edit")
                        .session(writerSession)
                        .param("title", "Updated Title")
                        .param("price", "20000")
                        .param("description", "Updated description"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/1"));
    }

    @Test
    void updateRejectsInvalidInput() throws Exception {
        given(tradePostService.findOwnedById(1L, writer.getId()))
                .willReturn(Optional.of(samplePost(writer)));

        mockMvc.perform(post("/posts/1/edit")
                        .session(writerSession)
                        .param("title", " ")
                        .param("price", "-1")
                        .param("description", " "))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/edit"))
                .andExpect(model().attribute("postId", 1L))
                .andExpect(model().attributeHasFieldErrors(
                        "postUpdateForm", "title", "price", "description"
                ));

        verify(tradePostService, never()).update(
                anyLong(), anyLong(), anyString(), anyLong(), anyString(),
                nullable(org.springframework.web.multipart.MultipartFile.class),
                org.mockito.ArgumentMatchers.anyBoolean()
        );
    }

    @Test
    void otherMemberCannotUpdatePost() throws Exception {
        given(tradePostService.findOwnedById(1L, otherMember.getId()))
                .willThrow(new PostAccessDeniedException());

        mockMvc.perform(post("/posts/1/edit")
                        .session(otherMemberSession)
                        .locale(Locale.KOREAN)
                        .param("title", "Hacked Title")
                        .param("price", "1")
                        .param("description", "Hacked"))
                .andExpect(status().isForbidden())
                .andExpect(view().name("error/403"))
                .andExpect(content().string(containsString(
                        "접근 권한이 없습니다"
                )));

        verify(tradePostService, never()).update(
                anyLong(), anyLong(), anyString(), anyLong(), anyString(),
                nullable(org.springframework.web.multipart.MultipartFile.class),
                org.mockito.ArgumentMatchers.anyBoolean()
        );
    }

    @Test
    void writerCanDeletePost() throws Exception {
        given(tradePostService.delete(1L, writer.getId())).willReturn(true);

        mockMvc.perform(post("/posts/1/delete").session(writerSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));
    }

    @Test
    void otherMemberCannotDeletePost() throws Exception {
        given(tradePostService.delete(1L, otherMember.getId()))
                .willThrow(new PostAccessDeniedException());

        mockMvc.perform(post("/posts/1/delete").session(otherMemberSession))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteReturnsNotFoundWhenPostDoesNotExist() throws Exception {
        given(tradePostService.delete(999L, writer.getId())).willReturn(false);

        mockMvc.perform(post("/posts/999/delete").session(writerSession))
                .andExpect(status().isNotFound());
    }

    @Test
    void writerCanUpdateStatus() throws Exception {
        TradePost post = samplePost(writer);
        post.changeStatus(TradeStatus.RESERVED);
        given(tradePostService.updateStatus(
                1L, writer.getId(), TradeStatus.RESERVED
        )).willReturn(Optional.of(post));

        mockMvc.perform(post("/posts/1/status")
                        .session(writerSession)
                        .param("status", "RESERVED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/1"));
    }

    @Test
    void updateStatusReturnsNotFoundWhenPostDoesNotExist() throws Exception {
        given(tradePostService.updateStatus(
                999L, writer.getId(), TradeStatus.SOLD
        )).willReturn(Optional.empty());

        mockMvc.perform(post("/posts/999/status")
                        .session(writerSession)
                        .param("status", "SOLD"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createFormRedirectsToLoginWhenLoggedOut() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(
                        "/login?redirectURL=%2Fposts%2Fnew"
                ));
    }

    @Test
    void updateFormRedirectsToLoginWhenLoggedOut() throws Exception {
        mockMvc.perform(get("/posts/1/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(
                        "/login?redirectURL=%2Fposts%2F1%2Fedit"
                ));

        verify(tradePostService, never()).findOwnedById(anyLong(), anyLong());
    }

    @Test
    void deleteRedirectsToLoginWhenLoggedOut() throws Exception {
        mockMvc.perform(post("/posts/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(
                        "/login?redirectURL=%2Fposts"
                ));

        verify(tradePostService, never()).delete(anyLong(), anyLong());
    }

    private MockHttpSession loginSession(Member member) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_MEMBER, member);
        return session;
    }

    private Member member(Long id, String loginId, String name) {
        Member member = new Member(loginId, "encoded-password", name);
        ReflectionTestUtils.setField(member, "id", id);
        return member;
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
}
