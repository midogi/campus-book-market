package com.skc04.campusbookmarket.web.post;

import com.skc04.campusbookmarket.post.domain.TradePost;
import com.skc04.campusbookmarket.post.repository.MemoryTradePostRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/posts")
public class PostController {

    private final MemoryTradePostRepository tradePostRepository;

    public PostController(MemoryTradePostRepository tradePostRepository) {
        this.tradePostRepository = tradePostRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("posts", tradePostRepository.findAll());
        return "posts/list";
    }

    @GetMapping("/{postId}")
    public String detail(@PathVariable Long postId, Model model) {
        TradePost post = tradePostRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        model.addAttribute("post", post);
        return "posts/detail";
    }
}
