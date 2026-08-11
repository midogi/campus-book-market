package com.skc04.campusbookmarket.post.repository;

import com.skc04.campusbookmarket.post.domain.TradePost;
import com.skc04.campusbookmarket.post.domain.TradeStatus;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class MemoryTradePostRepository implements TradePostRepository {

    private final Map<Long, TradePost> storage = new LinkedHashMap<>();
    private long nextId = 4L;

    public MemoryTradePostRepository() {
        storage.put(1L, new TradePost(1L, "객체지향의 사실과 오해", 12000,
                "컴퓨터공학과 3학년", "필기 흔적이 조금 있지만 전체적으로 깨끗합니다."));
        storage.put(2L, new TradePost(2L, "Operating System Concepts", 25000,
                "소프트웨어학과 2학년", "표지에 약간의 사용감이 있고 내부는 깨끗합니다."));
        storage.put(3L, new TradePost(3L, "공학용 계산기", 18000,
                "전자공학과 4학년", "정상 작동하며 배터리도 함께 드립니다."));
    }

    @Override
    public List<TradePost> findAll() {
        return List.copyOf(storage.values());
    }

    @Override
    public Optional<TradePost> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public TradePost save(String title, long price, String sellerName, String description) {
        Long id = nextId++;
        TradePost post = new TradePost(id, title, price, sellerName, description);

        storage.put(id, post);
        return post;
    }

    @Override
    public Optional<TradePost> update(
            Long id,
            String title,
            long price,
            String sellerName,
            String description
    ) {
        TradePost post = storage.get(id);
        if (post == null) {
            return Optional.empty();
        }

        TradePost updatedPost = new TradePost(
                id,
                title,
                price,
                sellerName,
                description,
                post.getStatus(),
                post.getCreatedAt(),
                LocalDateTime.now()
        );
        storage.put(id, updatedPost);
        return Optional.of(updatedPost);
    }

    @Override
    public Optional<TradePost> updateStatus(Long id, TradeStatus status) {
        TradePost post = storage.get(id);
        if (post == null) {
            return Optional.empty();
        }

        TradePost updatedPost = new TradePost(
                post.getId(),
                post.getTitle(),
                post.getPrice(),
                post.getSellerName(),
                post.getDescription(),
                status,
                post.getCreatedAt(),
                LocalDateTime.now()
        );

        storage.put(id, updatedPost);
        return Optional.of(updatedPost);
    }

    @Override
    public boolean deleteById(Long id) {
        return storage.remove(id) != null;
    }
}
