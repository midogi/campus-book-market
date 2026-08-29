package com.skc04.campusbookmarket.web.member;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.skc04.campusbookmarket.member.domain.Member;
import com.skc04.campusbookmarket.member.service.DuplicateLoginIdException;
import com.skc04.campusbookmarket.member.service.MemberService;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MemberController.class)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @Test
    void createForm() throws Exception {
        mockMvc.perform(get("/members/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("members/new"))
                .andExpect(model().attributeExists("memberCreateForm"));
    }

    @Test
    void createFormUsesEnglishMessages() throws Exception {
        mockMvc.perform(get("/members/new").locale(Locale.ENGLISH))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sign Up")))
                .andExpect(content().string(containsString("Confirm Password")));
    }

    @Test
    void create() throws Exception {
        Member savedMember = new Member(
                "midogi",
                "encoded-password",
                "김동민"
        );
        given(memberService.join("midogi", "password123", "김동민"))
                .willReturn(savedMember);

        mockMvc.perform(post("/members")
                        .param("loginId", "midogi")
                        .param("password", "password123")
                        .param("passwordConfirm", "password123")
                        .param("name", "김동민"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(memberService).join("midogi", "password123", "김동민");
    }

    @Test
    void createRejectsInvalidInput() throws Exception {
        mockMvc.perform(post("/members")
                        .param("loginId", "a")
                        .param("password", "short")
                        .param("passwordConfirm", "short")
                        .param("name", " "))
                .andExpect(status().isOk())
                .andExpect(view().name("members/new"))
                .andExpect(model().attributeHasFieldErrors(
                        "memberCreateForm",
                        "loginId",
                        "password",
                        "name"
                ));

        verify(memberService, never()).join(
                anyString(), anyString(), anyString()
        );
    }

    @Test
    void createRejectsPasswordMismatch() throws Exception {
        mockMvc.perform(post("/members")
                        .param("loginId", "midogi")
                        .param("password", "password123")
                        .param("passwordConfirm", "different123")
                        .param("name", "김동민"))
                .andExpect(status().isOk())
                .andExpect(view().name("members/new"))
                .andExpect(model().attributeHasFieldErrors(
                        "memberCreateForm",
                        "passwordConfirm"
                ));

        verify(memberService, never()).join(
                anyString(), anyString(), anyString()
        );
    }

    @Test
    void createRejectsDuplicatedLoginId() throws Exception {
        given(memberService.join("midogi", "password123", "김동민"))
                .willThrow(new DuplicateLoginIdException("midogi"));

        mockMvc.perform(post("/members")
                        .param("loginId", "midogi")
                        .param("password", "password123")
                        .param("passwordConfirm", "password123")
                        .param("name", "김동민"))
                .andExpect(status().isOk())
                .andExpect(view().name("members/new"))
                .andExpect(model().attributeHasFieldErrors(
                        "memberCreateForm",
                        "loginId"
                ));
    }
}
