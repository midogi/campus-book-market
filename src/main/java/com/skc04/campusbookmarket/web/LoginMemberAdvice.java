package com.skc04.campusbookmarket.web;

import com.skc04.campusbookmarket.member.domain.Member;
import com.skc04.campusbookmarket.web.session.SessionConst;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttribute;

@ControllerAdvice
public class LoginMemberAdvice {

    /** 모든 MVC 화면에서 현재 로그인 회원을 같은 이름으로 사용할 수 있게 한다. */
    @ModelAttribute
    public void addLoginMember(
            @SessionAttribute(
                    name = SessionConst.LOGIN_MEMBER,
                    required = false
            )
            Member loginMember,
            Model model
    ) {
        model.addAttribute("loginMember", loginMember);
    }
}
