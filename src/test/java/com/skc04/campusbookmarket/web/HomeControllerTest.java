package com.skc04.campusbookmarket.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.skc04.campusbookmarket.member.domain.Member;
import com.skc04.campusbookmarket.web.session.SessionConst;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HomeController.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loggedOutHomeShowsLoginAndJoinOnly() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(content().string(containsString("href=\"/login\"")))
                .andExpect(content().string(containsString("href=\"/members/new\"")))
                .andExpect(content().string(not(containsString("action=\"/logout\""))))
                .andExpect(content().string(not(containsString("href=\"/posts/new\""))));
    }

    @Test
    void loggedInHomeShowsMemberLogoutAndCreateLink() throws Exception {
        Member member = new Member(
                "midogi",
                "encoded-password",
                "김동민"
        );

        mockMvc.perform(get("/")
                        .locale(Locale.KOREAN)
                        .sessionAttr(SessionConst.LOGIN_MEMBER, member))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("김동민님")))
                .andExpect(content().string(containsString("action=\"/logout\"")))
                .andExpect(content().string(containsString("href=\"/posts/new\"")))
                .andExpect(content().string(not(containsString("href=\"/login\""))));
    }
}
