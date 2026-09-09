package com.skc04.campusbookmarket.member.service;

import com.skc04.campusbookmarket.member.domain.Member;
import com.skc04.campusbookmarket.member.repository.SpringDataMemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final SpringDataMemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(
            SpringDataMemberRepository memberRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Member join(String loginId, String rawPassword, String name) {
        String normalizedLoginId = loginId.trim();
        String normalizedName = name.trim();

        if (memberRepository.existsByLoginId(normalizedLoginId)) {
            throw new DuplicateLoginIdException(normalizedLoginId);
        }

        String passwordHash =
                passwordEncoder.encode(rawPassword);

        Member member = new Member(
                normalizedLoginId,
                passwordHash,
                normalizedName
        );
        return memberRepository.save(member);
    }
}
