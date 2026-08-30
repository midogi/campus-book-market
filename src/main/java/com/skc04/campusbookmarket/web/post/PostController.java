package com.skc04.campusbookmarket.web.post;

import com.skc04.campusbookmarket.post.domain.TradePost;
import com.skc04.campusbookmarket.post.domain.TradePostSearchType;
import com.skc04.campusbookmarket.post.domain.TradePostSort;
import com.skc04.campusbookmarket.post.domain.TradeStatus;
import com.skc04.campusbookmarket.post.service.TradePostPage;
import com.skc04.campusbookmarket.post.service.TradePostService;
import com.skc04.campusbookmarket.web.interceptor.LoginRequired;
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

/** 거래 게시글의 목록·등록·조회·수정·삭제 화면 요청을 처리한다. */
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
            @RequestParam(defaultValue = "TITLE") TradePostSearchType searchType,
            @RequestParam(required = false) TradeStatus status,
            @RequestParam(defaultValue = "LATEST") TradePostSort sort,
            @RequestParam(defaultValue = "1") int page,
            Model model
    ) {
        // 서비스가 검색 결과와 페이지 계산을 마치면 컨트롤러는 화면 데이터만 구성한다.
        TradePostPage postPage = tradePostService.search(
                keyword,
                searchType,
                status,
                sort,
                page
        );

        model.addAttribute("posts", postPage.getPosts());
        model.addAttribute("postPage", postPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("searchTypes", TradePostSearchType.values());
        model.addAttribute("selectedSearchType", searchType);
        model.addAttribute("tradeStatuses", TradeStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("sortOptions", TradePostSort.values());
        model.addAttribute("selectedSort", sort);
        return "posts/list";
    }

    @LoginRequired
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("postCreateForm", new PostCreateForm());
        return "posts/new";
    }

    @LoginRequired
    @PostMapping
    public String create(
            @Valid @ModelAttribute("postCreateForm") PostCreateForm form,
            BindingResult bindingResult
    ) {
        // 검증 실패 시 새 요청을 만들지 않아야 입력값과 오류 정보가 그대로 유지된다.
        if (bindingResult.hasErrors()) {
            return "posts/new";
        }

        TradePost savedPost = tradePostService.create(
                form.getTitle(),
                form.getPrice(),
                form.getSellerName(),
                form.getDescription()
        );

        // PRG 패턴으로 등록 POST의 새로고침 중복 실행을 방지한다.
        return "redirect:/posts/" + savedPost.getId();
    }

    @LoginRequired
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

    @LoginRequired
    @PostMapping("/{postId}/edit")
    public String update(
            @PathVariable Long postId,
            @Valid @ModelAttribute("postUpdateForm") PostUpdateForm form,
            BindingResult bindingResult,
            Model model
    ) {
        // 존재하지 않는 게시글의 수정 화면 요청은 먼저 404로 처리한다.
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

        // 수정 POST를 마친 뒤 상세 GET 요청으로 전환한다.
        return "redirect:/posts/" + updatedPost.getId();
    }

    @LoginRequired
    @PostMapping("/{postId}/status")
    public String updateStatus(@PathVariable Long postId, @RequestParam TradeStatus status) {
        TradePost updatedPost = tradePostService.updateStatus(postId, status)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return "redirect:/posts/" + updatedPost.getId();
    }

    @LoginRequired
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
        // 저장소의 빈 Optional을 웹 계층의 404 응답 의미로 변환한다.
        return tradePostService.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
