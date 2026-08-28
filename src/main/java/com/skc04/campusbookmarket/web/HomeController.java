package com.skc04.campusbookmarket.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** 루트 URL을 서비스 홈 화면으로 연결한다. */
@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "home";
    }
}
