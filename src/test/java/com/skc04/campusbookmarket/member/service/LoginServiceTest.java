package com.skc04.campusbookmarket.member.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.skc04.campusbookmarket.member.domain.Member;
import com.skc04.campusbookmarket.member.repository.SpringDataMemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private SpringDataMemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private LoginService loginService;

    @Test
    void loginReturnsMemberWhenPasswordMatches() {
        Member member = sampleMember();
        given(memberRepository.findByLoginId("midogi"))
                .willReturn(Optional.of(member));
        given(passwordEncoder.matches("password123", "encoded-password"))
                .willReturn(true);

        Optional<Member> result = loginService.login(
                "  midogi  ",
                "password123"
        );

        assertTrue(result.isPresent());
        assertEquals(member, result.get());
        verify(passwordEncoder).matches("password123", "encoded-password");
    }

    @Test
    void loginReturnsEmptyWhenPasswordDoesNotMatch() {
        Member member = sampleMember();
        given(memberRepository.findByLoginId("midogi"))
                .willReturn(Optional.of(member));
        given(passwordEncoder.matches("wrong-password", "encoded-password"))
                .willReturn(false);

        Optional<Member> result = loginService.login(
                "midogi",
                "wrong-password"
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void loginReturnsEmptyWhenMemberDoesNotExist() {
        given(memberRepository.findByLoginId("unknown"))
                .willReturn(Optional.empty());

        Optional<Member> result = loginService.login(
                "unknown",
                "password123"
        );

        assertTrue(result.isEmpty());
    }

    private Member sampleMember() {
        return new Member(
                "midogi",
                "encoded-password",
                "김동민"
        );
    }
}
