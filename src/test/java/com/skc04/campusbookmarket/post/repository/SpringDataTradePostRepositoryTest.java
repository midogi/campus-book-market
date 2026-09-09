package com.skc04.campusbookmarket.post.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.skc04.campusbookmarket.post.domain.TradePost;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

/** Spring Data JPA가 메서드 이름으로 생성한 제목 검색 쿼리를 검증한다. */
@DataJpaTest
class SpringDataTradePostRepositoryTest {

    @Autowired
    private SpringDataTradePostRepository repository;

    @Test
    void searchByTitleContainingIgnoreCase() {
        TradePost firstPost = repository.save(
                new TradePost(
                        "Spring MVC",
                        15000,
                        "김동민",
                        "MVC 전공책"
                )
        );

        repository.save(
                new TradePost(
                        "운영체제",
                        20000,
                        "이학생",
                        "운영체제 전공책"
                )
        );

        TradePost lastPost = repository.save(
                new TradePost(
                        "spring DB",
                        18000,
                        "박학생",
                        "DB 학습책"
                )
        );

        List<TradePost> result =
                repository.findByTitleContainingIgnoreCaseOrderByIdDesc(
                        "SPRING"
                );

        assertEquals(2, result.size());
        assertEquals(lastPost.getId(), result.get(0).getId());
        assertEquals(firstPost.getId(), result.get(1).getId());
    }
}
