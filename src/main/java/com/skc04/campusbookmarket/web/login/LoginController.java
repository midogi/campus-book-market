package com.skc04.campusbookmarket.web.login;

import com.skc04.campusbookmarket.member.domain.Member;
import com.skc04.campusbookmarket.member.service.LoginService;
import com.skc04.campusbookmarket.web.login.form.LoginForm;
import com.skc04.campusbookmarket.web.session.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @GetMapping("/login")
    public String loginForm(
            @RequestParam(defaultValue = "/") String redirectURL,
            Model model
    ) {
        model.addAttribute("loginForm", new LoginForm());
        model.addAttribute(
                "redirectURL",
                normalizeRedirectURL(redirectURL)
        );

        return "login/login";
    }

    @PostMapping("/login")
    public String login(
            @Valid @ModelAttribute("loginForm") LoginForm form,
            BindingResult bindingResult,
            @RequestParam(defaultValue = "/") String redirectURL,
            HttpServletRequest request,
            Model model
    ) {
        String safeRedirectURL =
                normalizeRedirectURL(redirectURL);

        model.addAttribute("redirectURL", safeRedirectURL);

        if (bindingResult.hasErrors()) {
            return "login/login";
        }

        Optional<Member> loginMember = loginService.login(
                form.getLoginId(),
                form.getPassword()
        );

        if (loginMember.isEmpty()) {
            bindingResult.reject("validation.login.failed");
            return "login/login";
        }

        HttpSession session = request.getSession();
        session.setAttribute(
                SessionConst.LOGIN_MEMBER,
                loginMember.get()
        );

        return "redirect:" + safeRedirectURL;
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }
        return "redirect:/";
    }

    private static String normalizeRedirectURL(String redirectURL) {
        if (redirectURL == null
                || !redirectURL.startsWith("/")
                || redirectURL.startsWith("//")
                || redirectURL.contains("\r")
                || redirectURL.contains("\n")) {
            return "/";
        }

        return redirectURL;
    }
}
