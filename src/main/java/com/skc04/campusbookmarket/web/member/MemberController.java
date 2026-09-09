package com.skc04.campusbookmarket.web.member;

import com.skc04.campusbookmarket.member.service.DuplicateLoginIdException;
import com.skc04.campusbookmarket.member.service.MemberService;
import com.skc04.campusbookmarket.web.member.form.MemberCreateForm;
import jakarta.validation.Valid;
import java.util.Objects;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute(
                "memberCreateForm",
                new MemberCreateForm()
        );
        return "members/new";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("memberCreateForm") MemberCreateForm form,
            BindingResult bindingResult
    ) {
        if (!Objects.equals(
                form.getPassword(),
                form.getPasswordConfirm()
        )) {
            bindingResult.rejectValue(
                    "passwordConfirm",
                    "validation.member.passwordConfirm.mismatch"
            );
        }

        if (bindingResult.hasErrors()) {
            return "members/new";
        }

        try {
            memberService.join(
                    form.getLoginId(),
                    form.getPassword(),
                    form.getName()
            );
        } catch (DuplicateLoginIdException exception) {
            bindingResult.rejectValue(
                    "loginId",
                    "validation.member.loginId.duplicate"
            );
            return "members/new";
        }

        return "redirect:/";
    }
}
