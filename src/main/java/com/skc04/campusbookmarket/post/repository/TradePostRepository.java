package com.skc04.campusbookmarket.post.repository;

import com.skc04.campusbookmarket.post.domain.TradePost;
import com.skc04.campusbookmarket.post.domain.TradeStatus;
import java.util.List;
import java.util.Optional;

public interface TradePostRepository {

    List<TradePost> findAll();

    List<TradePost> search(String keyword, TradeStatus status);

    Optional<TradePost> findById(Long id);

    TradePost save(String title, long price, String sellerName, String description);

    Optional<TradePost> update(
            Long id,
            String title,
            long price,
            String sellerName,
            String description
    );

    Optional<TradePost> updateStatus(Long id, TradeStatus status);

    boolean deleteById(Long id);
}
