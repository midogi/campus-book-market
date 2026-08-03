package com.skc04.campusbookmarket.web.post;

import com.skc04.campusbookmarket.post.domain.TradePost;
import com.skc04.campusbookmarket.post.repository.MemoryTradePostRepository;
import com.skc04.campusbookmarket.web.post.form.PostCreateForm;
import com.skc04.campusbookmarket.web.post.form.PostUpdateForm;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("postCreateForm", new PostCreateForm());
        return "posts/new";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("postCreateForm") PostCreateForm form,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return "posts/new";
        }

        TradePost savedPost = tradePostRepository.save(
                form.getTitle(),
                form.getPrice(),
                form.getSellerName()
        );

        return "redirect:/posts/" + savedPost.getId();
    }

    @GetMapping("/{postId}/edit")
    public String updateForm(@PathVariable Long postId, Model model) {
        TradePost post = findPostById(postId);

        PostUpdateForm form = new PostUpdateForm();
        form.setTitle(post.getTitle());
        form.setPrice(post.getPrice());
        form.setSellerName(post.getSellerName());

        model.addAttribute("postId", postId);
        model.addAttribute("postUpdateForm", form);
        return "posts/edit";
    }

    @PostMapping("/{postId}/edit")
    public String update(
            @PathVariable Long postId,
            @Valid @ModelAttribute("postUpdateForm") PostUpdateForm form,
            BindingResult bindingResult,
            Model model
    ) {
        findPostById(postId);

        if (bindingResult.hasErrors()) {
            model.addAttribute("postId", postId);
            return "posts/edit";
        }

        TradePost updatedPost = tradePostRepository.update(
                        postId,
                        form.getTitle(),
                        form.getPrice(),
                        form.getSellerName()
                )
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return "redirect:/posts/" + updatedPost.getId();
    }

    @GetMapping("/{postId}")
    public String detail(@PathVariable Long postId, Model model) {
        TradePost post = findPostById(postId);

        model.addAttribute("post", post);
        return "posts/detail";
    }

    private TradePost findPostById(Long postId) {
        return tradePostRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
