package com.skc04.campusbookmarket.member.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.skc04.campusbookmarket.member.domain.Member;
import com.skc04.campusbookmarket.member.repository.SpringDataMemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private SpringDataMemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberService memberService;

    @Test
    void joinEncodesPasswordAndSavesMember() {
        given(memberRepository.existsByLoginId("midogi"))
                .willReturn(false);
        given(passwordEncoder.encode("password123"))
                .willReturn("encoded-password");
        given(memberRepository.save(any(Member.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        Member result = memberService.join(
                "  midogi  ",
                "password123",
                "  김동민  "
        );

        assertEquals("midogi", result.getLoginId());
        assertEquals("encoded-password", result.getPasswordHash());
        assertEquals("김동민", result.getName());

        verify(passwordEncoder).encode("password123");
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void joinRejectsDuplicatedLoginId() {
        given(memberRepository.existsByLoginId("midogi"))
                .willReturn(true);

        assertThrows(
                DuplicateLoginIdException.class,
                () -> memberService.join(
                        "midogi",
                        "password123",
                        "김동민"
                )
        );

        verify(passwordEncoder, never()).encode(anyString());
        verify(memberRepository, never()).save(any(Member.class));
    }
}
