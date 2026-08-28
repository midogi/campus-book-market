package com.skc04.campusbookmarket.member.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.skc04.campusbookmarket.member.domain.Member;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class SpringDataMemberRepositoryTest {

    @Autowired
    private SpringDataMemberRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void saveAndFindByLoginId() {
        Member member = new Member(
                "midogi",
                "encoded-password",
                "김동민"
        );

        Member savedMember = repository.save(member);

        entityManager.flush();
        entityManager.clear();

        Optional<Member> result =
                repository.findByLoginId("midogi");

        assertTrue(result.isPresent());

        Member foundMember = result.orElseThrow();

        assertNotNull(foundMember.getId());
        assertEquals(savedMember.getId(), foundMember.getId());
        assertEquals("midogi", foundMember.getLoginId());
        assertEquals("encoded-password", foundMember.getPasswordHash());
        assertEquals("김동민", foundMember.getName());
        assertNotNull(foundMember.getCreatedAt());
        assertNotNull(foundMember.getUpdatedAt());
    }

    @Test
    void existsByLoginId() {
        assertFalse(repository.existsByLoginId("midogi"));

        repository.save(
                new Member(
                        "midogi",
                        "encoded-password",
                        "김동민"
                )
        );

        entityManager.flush();
        entityManager.clear();

        assertTrue(repository.existsByLoginId("midogi"));
        assertFalse(repository.existsByLoginId("another-user"));
    }
}