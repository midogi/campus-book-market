package com.skc04.campusbookmarket.post.repository;

import com.skc04.campusbookmarket.post.domain.TradePost;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MemoryTradePostRepository {

    private final Map<Long, TradePost> storage = new LinkedHashMap<>();
    private long nextId = 4L;

    public MemoryTradePostRepository() {
        storage.put(1L, new TradePost(1L, "객체지향의 사실과 오해", 12000, "컴퓨터공학과 3학년"));
        storage.put(2L, new TradePost(2L, "Operating System Concepts", 25000, "소프트웨어학과 2학년"));
        storage.put(3L, new TradePost(3L, "공학용 계산기", 18000, "전자공학과 4학년"));
    }

    public List<TradePost> findAll() {
        return List.copyOf(storage.values());
    }

    public Optional<TradePost> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public TradePost save(String title, long price, String sellerName) {
        Long id = nextId++;
        TradePost post = new TradePost(id, title, price, sellerName);

        storage.put(id, post);
        return post;
    }

    public Optional<TradePost> update(Long id, String title, long price, String sellerName) {
        if (!storage.containsKey(id)) {
            return Optional.empty();
        }

        TradePost updatedPost = new TradePost(id, title, price, sellerName);
        storage.put(id, updatedPost);
        return Optional.of(updatedPost);
    }
}
