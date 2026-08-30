package com.skc04.campusbookmarket.member.service;

import com.skc04.campusbookmarket.member.domain.Member;
import com.skc04.campusbookmarket.member.repository.SpringDataMemberRepository;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LoginService {

    private final SpringDataMemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginService(
            SpringDataMemberRepository memberRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** 아이디와 비밀번호가 모두 일치한 회원만 반환한다. */
    public Optional<Member> login(String loginId, String rawPassword) {
        String normalizedLoginId = loginId.trim();

        return memberRepository.findByLoginId(normalizedLoginId)
                .filter(member -> passwordEncoder.matches(
                        rawPassword,
                        member.getPasswordHash()
                ));
    }
}
