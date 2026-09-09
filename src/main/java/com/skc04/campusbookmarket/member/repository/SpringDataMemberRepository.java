package com.skc04.campusbookmarket.member.repository;

import com.skc04.campusbookmarket.member.domain.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** 회원의 기본 CRUD와 로그인 아이디 조회를 제공하는 Spring Data JPA 저장소다. */
public interface SpringDataMemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);
}
