package com.skc04.campusbookmarket.post.repository;

import com.skc04.campusbookmarket.post.domain.TradePost;
import java.util.List;
import java.util.Optional;

public interface TradePostRepository {

    List<TradePost> findAll();

    Optional<TradePost> findById(Long id);

    TradePost save(String title, long price, String sellerName, String description);

    Optional<TradePost> update(
            Long id,
            String title,
            long price,
            String sellerName,
            String description
    );

    boolean deleteById(Long id);
}
