package com.skc04.campusbookmarket.web.post;

import com.skc04.campusbookmarket.post.domain.TradePost;
import com.skc04.campusbookmarket.post.domain.TradeStatus;
import com.skc04.campusbookmarket.post.service.TradePostService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/posts")
public class PostController {

    private final TradePostService tradePostService;

    public PostController(TradePostService tradePostService) {
        this.tradePostService = tradePostService;
    }

    @GetMapping
    public String list(
            @RequestParam(defaultValue = "") String keyword,
            Model model
    ) {
        model.addAttribute("posts", tradePostService.search(keyword));
        model.addAttribute("keyword", keyword);
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

        TradePost savedPost = tradePostService.create(
                form.getTitle(),
                form.getPrice(),
                form.getSellerName(),
                form.getDescription()
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
        form.setDescription(post.getDescription());

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

        TradePost updatedPost = tradePostService.update(
                        postId,
                        form.getTitle(),
                        form.getPrice(),
                        form.getSellerName(),
                        form.getDescription()
                )
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return "redirect:/posts/" + updatedPost.getId();
    }

    @PostMapping("/{postId}/status")
    public String updateStatus(@PathVariable Long postId, @RequestParam TradeStatus status) {
        TradePost updatedPost = tradePostService.updateStatus(postId, status)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return "redirect:/posts/" + updatedPost.getId();
    }

    @PostMapping("/{postId}/delete")
    public String delete(@PathVariable Long postId) {
        boolean deleted = tradePostService.delete(postId);

        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return "redirect:/posts";
    }

    @GetMapping("/{postId}")
    public String detail(@PathVariable Long postId, Model model) {
        TradePost post = findPostById(postId);

        model.addAttribute("post", post);
        return "posts/detail";
    }

    private TradePost findPostById(Long postId) {
        return tradePostService.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
