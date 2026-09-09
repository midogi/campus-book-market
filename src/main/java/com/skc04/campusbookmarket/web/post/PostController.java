package com.skc04.campusbookmarket.web.post;

import com.skc04.campusbookmarket.file.InvalidImageException;
import com.skc04.campusbookmarket.member.domain.Member;
import com.skc04.campusbookmarket.post.domain.TradePost;
import com.skc04.campusbookmarket.post.domain.TradePostSearchType;
import com.skc04.campusbookmarket.post.domain.TradePostSort;
import com.skc04.campusbookmarket.post.domain.TradeStatus;
import com.skc04.campusbookmarket.post.service.PostNotFoundException;
import com.skc04.campusbookmarket.post.service.TradePostPage;
import com.skc04.campusbookmarket.post.service.TradePostService;
import com.skc04.campusbookmarket.web.interceptor.LoginRequired;
import com.skc04.campusbookmarket.web.post.form.PostCreateForm;
import com.skc04.campusbookmarket.web.post.form.PostUpdateForm;
import com.skc04.campusbookmarket.web.session.SessionConst;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

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
            BindingResult bindingResult,
            @SessionAttribute(SessionConst.LOGIN_MEMBER) Member loginMember
    ) {
        // 검증 실패 시 새 요청을 만들지 않아야 입력값과 오류 정보가 그대로 유지된다.
        if (bindingResult.hasErrors()) {
            return "posts/new";
        }

        TradePost savedPost;
        try {
            savedPost = tradePostService.create(
                    form.getTitle(),
                    form.getPrice(),
                    loginMember,
                    form.getDescription(),
                    form.getImageFile()
            );
        } catch (InvalidImageException exception) {
            bindingResult.rejectValue(
                    "imageFile",
                    "validation.post.image.invalid",
                    exception.getMessage()
            );
            return "posts/new";
        }

        // PRG 패턴으로 등록 POST의 새로고침 중복 실행을 방지한다.
        return "redirect:/posts/" + savedPost.getId();
    }

    @LoginRequired
    @GetMapping("/{postId}/edit")
    public String updateForm(
            @PathVariable Long postId,
            @SessionAttribute(SessionConst.LOGIN_MEMBER) Member loginMember,
            Model model
    ) {
        TradePost post = findOwnedPostById(postId, loginMember.getId());

        PostUpdateForm form = new PostUpdateForm();
        form.setTitle(post.getTitle());
        form.setPrice(post.getPrice());
        form.setDescription(post.getDescription());

        model.addAttribute("postId", postId);
        model.addAttribute("currentImageStoredName", post.getImageStoredName());
        model.addAttribute("postUpdateForm", form);
        return "posts/edit";
    }

    @LoginRequired
    @PostMapping("/{postId}/edit")
    public String update(
            @PathVariable Long postId,
            @Valid @ModelAttribute("postUpdateForm") PostUpdateForm form,
            BindingResult bindingResult,
            @SessionAttribute(SessionConst.LOGIN_MEMBER) Member loginMember,
            Model model
    ) {
        // 입력 오류가 있더라도 다른 회원의 수정 화면을 보여 주지 않도록 권한을 먼저 확인한다.
        TradePost currentPost = findOwnedPostById(postId, loginMember.getId());
        model.addAttribute("postId", postId);
        model.addAttribute(
                "currentImageStoredName",
                currentPost.getImageStoredName()
        );

        if (bindingResult.hasErrors()) {
            return "posts/edit";
        }

        TradePost updatedPost;
        try {
            updatedPost = tradePostService.update(
                            postId,
                            loginMember.getId(),
                            form.getTitle(),
                            form.getPrice(),
                            form.getDescription(),
                            form.getImageFile(),
                            form.isRemoveImage()
                    )
                    .orElseThrow(() -> new PostNotFoundException(postId));
        } catch (InvalidImageException exception) {
            bindingResult.rejectValue(
                    "imageFile",
                    "validation.post.image.invalid",
                    exception.getMessage()
            );
            return "posts/edit";
        }

        // 수정 POST를 마친 뒤 상세 GET 요청으로 전환한다.
        return "redirect:/posts/" + updatedPost.getId();
    }

    @LoginRequired
    @PostMapping("/{postId}/status")
    public String updateStatus(
            @PathVariable Long postId,
            @RequestParam TradeStatus status,
            @SessionAttribute(SessionConst.LOGIN_MEMBER) Member loginMember
    ) {
        TradePost updatedPost = tradePostService.updateStatus(
                        postId,
                        loginMember.getId(),
                        status
                )
                .orElseThrow(() -> new PostNotFoundException(postId));
        return "redirect:/posts/" + updatedPost.getId();
    }

    @LoginRequired
    @PostMapping("/{postId}/delete")
    public String delete(
            @PathVariable Long postId,
            @SessionAttribute(SessionConst.LOGIN_MEMBER) Member loginMember
    ) {
        boolean deleted = tradePostService.delete(postId, loginMember.getId());

        if (!deleted) {
            throw new PostNotFoundException(postId);
        }

        return "redirect:/posts";
    }

    @GetMapping("/{postId}")
    public String detail(
            @PathVariable Long postId,
            @SessionAttribute(
                    name = SessionConst.LOGIN_MEMBER,
                    required = false
            )
            Member loginMember,
            Model model
    ) {
        TradePost post = findPostById(postId);

        model.addAttribute("post", post);
        model.addAttribute(
                "canManage",
                loginMember != null && post.isWrittenBy(loginMember.getId())
        );
        return "posts/detail";
    }

    private TradePost findPostById(Long postId) {
        // 저장소의 빈 Optional을 웹 계층의 404 응답 의미로 변환한다.
        return tradePostService.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
    }

    private TradePost findOwnedPostById(Long postId, Long memberId) {
        return tradePostService.findOwnedById(postId, memberId)
                .orElseThrow(() -> new PostNotFoundException(postId));
    }
}
