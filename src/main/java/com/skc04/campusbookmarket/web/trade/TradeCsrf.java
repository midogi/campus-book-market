package com.skc04.campusbookmarket.web.trade;

import com.skc04.campusbookmarket.web.session.SessionConst;
import com.skc04.campusbookmarket.post.service.PostAccessDeniedException;
import jakarta.servlet.http.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.HandlerInterceptor;

/** 세션 로그인만으로는 외부 사이트의 위조 POST를 막을 수 없어 거래 변경 폼에 토큰을 둔다. */
@ControllerAdvice
public class TradeCsrf implements HandlerInterceptor {
    public static final String TOKEN = "TRADE_CSRF";

    @ModelAttribute("tradeCsrf")
    public String token(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) return "";
        synchronized (session) {
            if (session.getAttribute(TOKEN) == null) session.setAttribute(TOKEN, UUID.randomUUID().toString());
            return (String) session.getAttribute(TOKEN);
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!"POST".equals(request.getMethod())) return true;
        HttpSession session = request.getSession(false);
        String expected = session == null ? null : (String) session.getAttribute(TOKEN);
        String supplied = request.getParameter("_tradeCsrf");
        if (expected == null || supplied == null || !MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8))) {
            throw new PostAccessDeniedException();
        }
        return true;
    }
}
