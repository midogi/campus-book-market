package com.skc04.campusbookmarket.web.post;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.skc04.campusbookmarket.config.WebConfig;
import com.skc04.campusbookmarket.member.domain.Member;
import com.skc04.campusbookmarket.post.domain.TradePost;
import com.skc04.campusbookmarket.post.domain.TradePostSearchType;
import com.skc04.campusbookmarket.post.domain.TradePostSort;
import com.skc04.campusbookmarket.post.domain.TradeStatus;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PostController.class)
@Import({WebConfig.class, LoginCheckInterceptor.class})
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TradePostService tradePostService;

    private MockHttpSession loginSession;

    @BeforeEach
    void setUpLoginSession() {
        loginSession = new MockHttpSession();
        loginSession.setAttribute(
                SessionConst.LOGIN_MEMBER,
                new Member("midogi", "encoded-password", "김동민")
        );
    }

    @Test
    void list() throws Exception {
        List<TradePost> posts = List.of(samplePost());
        TradePostPage postPage = new TradePostPage(posts, 1, 10, 1);
        given(tradePostService.search(
                "", TradePostSearchType.TITLE, null, TradePostSort.LATEST, 1
        ))
                .willReturn(postPage);

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
                .andExpect(content().string(containsString("name=\"sort\"")));

        verify(tradePostService).search(
                "", TradePostSearchType.TITLE, null, TradePostSort.LATEST, 1
        );
    }

    @Test
    void listSearchesByKeywordStatusAndSortOnRequestedPage() throws Exception {
        List<TradePost> posts = List.of(samplePost());
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
                .andExpect(model().attributeExists(
                        "searchTypes", "tradeStatuses", "sortOptions"
                ))
                .andExpect(content().string(containsString("searchType=SELLER")))
                .andExpect(content().string(containsString("page=1")));

        verify(tradePostService).search(
                "Spring",
                TradePostSearchType.SELLER,
                TradeStatus.SOLD,
                TradePostSort.PRICE_DESC,
                2
        );
    }

    @Test
    void detail() throws Exception {
        TradePost post = samplePost();
        given(tradePostService.findById(1L)).willReturn(Optional.of(post));

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attribute("post", post));
    }

    @Test
    void detailReturnsNotFoundWhenPostDoesNotExist() throws Exception {
        given(tradePostService.findById(999L)).willReturn(Optional.empty());

        mockMvc.perform(get("/posts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createForm() throws Exception {
        mockMvc.perform(get("/posts/new").session(loginSession))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/new"))
                .andExpect(model().attributeExists("postCreateForm"));
    }

    @Test
    void createFormUsesEnglishMessages() throws Exception {
        mockMvc.perform(get("/posts/new")
                        .session(loginSession)
                        .locale(Locale.ENGLISH))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/new"))
                .andExpect(content().string(containsString("Create Post")))
                .andExpect(content().string(containsString("Seller")))
                .andExpect(content().string(containsString("Back to List")));
    }

    @Test
    void create() throws Exception {
        TradePost savedPost = samplePost();
        given(tradePostService.create(
                "Spring Basics", 15000L, "Student Seller", "Clean copy"
        )).willReturn(savedPost);

        mockMvc.perform(post("/posts")
                        .session(loginSession)
                        .param("title", "Spring Basics")
                        .param("price", "15000")
                        .param("sellerName", "Student Seller")
                        .param("description", "Clean copy"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/1"));
    }

    @Test
    void createRejectsInvalidInput() throws Exception {
        mockMvc.perform(post("/posts")
                        .session(loginSession)
                        .param("title", " ")
                        .param("price", "-1")
                        .param("sellerName", " ")
                        .param("description", " "))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/new"))
                .andExpect(model().attributeHasFieldErrors(
                        "postCreateForm", "title", "price", "sellerName", "description"
                ));

        verify(tradePostService, never()).create(
                anyString(), anyLong(), anyString(), anyString()
        );
    }

    @Test
    void updateForm() throws Exception {
        TradePost post = samplePost();
        given(tradePostService.findById(1L)).willReturn(Optional.of(post));

        mockMvc.perform(get("/posts/1/edit").session(loginSession))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/edit"))
                .andExpect(model().attribute("postId", 1L))
                .andExpect(model().attributeExists("postUpdateForm"));
    }

    @Test
    void update() throws Exception {
        TradePost existingPost = samplePost();
        TradePost updatedPost = new TradePost(
                1L, "Updated Title", 20000L, "Updated Seller", "Updated description"
        );
        given(tradePostService.findById(1L)).willReturn(Optional.of(existingPost));
        given(tradePostService.update(
                1L, "Updated Title", 20000L, "Updated Seller", "Updated description"
        )).willReturn(Optional.of(updatedPost));

        mockMvc.perform(post("/posts/1/edit")
                        .session(loginSession)
                        .param("title", "Updated Title")
                        .param("price", "20000")
                        .param("sellerName", "Updated Seller")
                        .param("description", "Updated description"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/1"));
    }

    @Test
    void updateRejectsInvalidInput() throws Exception {
        given(tradePostService.findById(1L)).willReturn(Optional.of(samplePost()));

        mockMvc.perform(post("/posts/1/edit")
                        .session(loginSession)
                        .param("title", " ")
                        .param("price", "-1")
                        .param("sellerName", " ")
                        .param("description", " "))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/edit"))
                .andExpect(model().attribute("postId", 1L))
                .andExpect(model().attributeHasFieldErrors(
                        "postUpdateForm", "title", "price", "sellerName", "description"
                ));

        verify(tradePostService, never()).update(
                anyLong(), anyString(), anyLong(), anyString(), anyString()
        );
    }

    @Test
    void delete() throws Exception {
        given(tradePostService.delete(1L)).willReturn(true);

        mockMvc.perform(post("/posts/1/delete").session(loginSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));
    }

    @Test
    void deleteReturnsNotFoundWhenPostDoesNotExist() throws Exception {
        given(tradePostService.delete(999L)).willReturn(false);

        mockMvc.perform(post("/posts/999/delete").session(loginSession))
                .andExpect(status().isNotFound());
    }

    private TradePost samplePost() {
        return new TradePost(
                1L, "Spring Basics", 15000L, "Student Seller", "Clean copy"
        );
    }

    @Test
    void updateStatus() throws Exception {
        TradePost post = new TradePost(
                1L,
                "Spring Basics",
                15000L,
                "Student Seller",
                "Clean copy",
                TradeStatus.RESERVED
        );

        given(tradePostService.updateStatus(1L, TradeStatus.RESERVED))
                .willReturn(Optional.of(post));

        mockMvc.perform(post("/posts/1/status")
                        .session(loginSession)
                        .param("status", "RESERVED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/1"));

        verify(tradePostService)
                .updateStatus(1L, TradeStatus.RESERVED);

    }

    @Test
    void updateStatusReturnsNotFoundWhenPostDoesNotExist() throws Exception {
        given(tradePostService.updateStatus(999L, TradeStatus.SOLD))
                .willReturn(Optional.empty());

        mockMvc.perform(post("/posts/999/status")
                        .session(loginSession)
                        .param("status", "SOLD"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createFormRedirectsToLoginWhenLoggedOut() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
