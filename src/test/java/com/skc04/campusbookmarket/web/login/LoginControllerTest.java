package com.skc04.campusbookmarket.web.login;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.skc04.campusbookmarket.member.domain.Member;
import com.skc04.campusbookmarket.member.service.LoginService;
import com.skc04.campusbookmarket.web.session.SessionConst;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LoginController.class)
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoginService loginService;

    @Test
    void loginForm() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login/login"))
                .andExpect(model().attributeExists("loginForm"))
                .andExpect(model().attribute("redirectURL", "/"))
                .andExpect(content().string(containsString(
                        "name=\"redirectURL\""
                )));
    }

    @Test
    void loginFormKeepsRedirectURL() throws Exception {
        mockMvc.perform(get("/login")
                        .param("redirectURL", "/posts/new"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("redirectURL", "/posts/new"))
                .andExpect(content().string(containsString(
                        "value=\"/posts/new\""
                )));
    }

    @Test
    void loginStoresMemberInSession() throws Exception {
        Member member = sampleMember();
        given(loginService.login("midogi", "password123"))
                .willReturn(Optional.of(member));

        mockMvc.perform(post("/login")
                        .param("loginId", "midogi")
                        .param("password", "password123")
                        .param("redirectURL", "/posts/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/new"))
                .andExpect(request().sessionAttribute(
                        SessionConst.LOGIN_MEMBER,
                        member
                ));
    }

    @Test
    void loginRejectsBlankInput() throws Exception {
        mockMvc.perform(post("/login")
                        .param("loginId", " ")
                        .param("password", " ")
                        .param("redirectURL", "/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("login/login"))
                .andExpect(model().attribute("redirectURL", "/posts/new"))
                .andExpect(model().attributeHasFieldErrors(
                        "loginForm",
                        "loginId",
                        "password"
                ));

        verify(loginService, never()).login(anyString(), anyString());
    }

    @Test
    void loginRejectsWrongCredentials() throws Exception {
        given(loginService.login("midogi", "wrong-password"))
                .willReturn(Optional.empty());

        mockMvc.perform(post("/login")
                        .locale(Locale.KOREAN)
                        .param("loginId", "midogi")
                        .param("password", "wrong-password")
                        .param("redirectURL", "/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("login/login"))
                .andExpect(model().attribute("redirectURL", "/posts/new"))
                .andExpect(model().attributeHasErrors("loginForm"))
                .andExpect(content().string(containsString(
                        "아이디 또는 비밀번호가 맞지 않습니다."
                )));
    }

    @Test
    void loginRejectsExternalRedirectURL() throws Exception {
        Member member = sampleMember();
        given(loginService.login("midogi", "password123"))
                .willReturn(Optional.of(member));

        mockMvc.perform(post("/login")
                        .param("loginId", "midogi")
                        .param("password", "password123")
                        .param("redirectURL", "//evil.example"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void loggedInHeaderShowsMemberAndLogout() throws Exception {
        mockMvc.perform(get("/login")
                        .locale(Locale.KOREAN)
                        .sessionAttr(
                                SessionConst.LOGIN_MEMBER,
                                sampleMember()
                        ))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("김동민님")))
                .andExpect(content().string(containsString("로그아웃")));
    }

    @Test
    void logoutInvalidatesSession() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_MEMBER, sampleMember());

        mockMvc.perform(post("/logout").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        assertThrows(
                IllegalStateException.class,
                () -> session.getAttribute(SessionConst.LOGIN_MEMBER)
        );
    }

    private Member sampleMember() {
        return new Member(
                "midogi",
                "encoded-password",
                "김동민"
        );
    }
}
