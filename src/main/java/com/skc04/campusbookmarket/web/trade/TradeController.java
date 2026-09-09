package com.skc04.campusbookmarket.web.trade;

import com.skc04.campusbookmarket.member.domain.Member;
import com.skc04.campusbookmarket.trade.service.TradeService;
import com.skc04.campusbookmarket.web.interceptor.LoginRequired;
import com.skc04.campusbookmarket.web.session.SessionConst;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** 회원 ID는 요청 파라미터가 아니라 로그인 세션에서만 받는다. 모든 변경 후에는 PRG를 적용한다. */
@Controller
public class TradeController {
    private final TradeService trades;
    public TradeController(TradeService trades) { this.trades = trades; }

    @LoginRequired
    @PostMapping("/posts/{postId}/requests")
    public String apply(@PathVariable Long postId,
            @SessionAttribute(SessionConst.LOGIN_MEMBER) Member member) {
        trades.apply(postId, member.getId());
        return "redirect:/trades";
    }

    @LoginRequired
    @GetMapping("/posts/{postId}/requests")
    public String received(@PathVariable Long postId, @RequestParam(defaultValue = "1") int page,
            @SessionAttribute(SessionConst.LOGIN_MEMBER) Member member, Model model) {
        model.addAttribute("tradePage", trades.received(postId, member.getId(), page));
        model.addAttribute("postId", postId);
        return "trades/list";
    }

    @LoginRequired
    @GetMapping("/trades")
    public String history(@RequestParam(defaultValue = "1") int page,
            @SessionAttribute(SessionConst.LOGIN_MEMBER) Member member, Model model) {
        model.addAttribute("tradePage", trades.history(member.getId(), page));
        return "trades/list";
    }

    @LoginRequired
    @PostMapping("/trades/{id}/reserve")
    public String reserve(@PathVariable Long id, @SessionAttribute(SessionConst.LOGIN_MEMBER) Member member) {
        trades.reserve(id, member.getId());
        return "redirect:/trades";
    }
    @LoginRequired
    @PostMapping("/trades/{id}/complete")
    public String complete(@PathVariable Long id, @SessionAttribute(SessionConst.LOGIN_MEMBER) Member member) {
        trades.complete(id, member.getId());
        return "redirect:/trades";
    }
    @LoginRequired
    @PostMapping("/trades/{id}/cancel")
    public String cancel(@PathVariable Long id, @SessionAttribute(SessionConst.LOGIN_MEMBER) Member member) {
        trades.cancel(id, member.getId());
        return "redirect:/trades";
    }
    @LoginRequired
    @PostMapping("/trades/{id}/reject")
    public String reject(@PathVariable Long id, @SessionAttribute(SessionConst.LOGIN_MEMBER) Member member) {
        trades.reject(id, member.getId());
        return "redirect:/trades";
    }
}
