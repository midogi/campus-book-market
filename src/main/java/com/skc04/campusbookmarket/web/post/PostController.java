package com.skc04.campusbookmarket.web.post;

import com.skc04.campusbookmarket.post.domain.TradePost;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/posts")
public class PostController {

    @GetMapping
    public String list(Model model) {
        List<TradePost> posts = List.of(
                new TradePost(1L, "객체지향의 사실과 오해", 12000, "컴퓨터공학과 3학년"),
                new TradePost(2L, "Operating System Concepts", 25000, "소프트웨어학과 2학년"),
                new TradePost(3L, "공학용 계산기", 18000, "전자공학과 4학년")
        );

        model.addAttribute("posts", posts);
        return "posts/list";
    }
}
