package com.skc04.campusbookmarket.post.service;

import com.skc04.campusbookmarket.post.domain.TradePost;
import com.skc04.campusbookmarket.post.repository.TradePostRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class TradePostService {

    private final TradePostRepository tradePostRepository;

    public TradePostService(TradePostRepository tradePostRepository) {
        this.tradePostRepository = tradePostRepository;
    }

    public List<TradePost> findAll() {
        return tradePostRepository.findAll();
    }

    public Optional<TradePost> findById(Long postId) {
        return tradePostRepository.findById(postId);
    }

    public TradePost create(String title, long price, String sellerName, String description) {
        return tradePostRepository.save(title, price, sellerName, description);
    }

    public Optional<TradePost> update(
            Long postId,
            String title,
            long price,
            String sellerName,
            String description
    ) {
        return tradePostRepository.update(postId, title, price, sellerName, description);
    }

    public boolean delete(Long postId) {
        return tradePostRepository.deleteById(postId);
    }
}
