package com.wedding.app.mywedding.Controller;

import com.wedding.app.mywedding.Repository.TokenRepository;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomeController {


    @Autowired
    TokenRepository tokenRepository;

    @GetMapping
    public String Home() {
        tokenRepository.deleteAllExpiredSince(LocalDateTime.now());
        return "pages/home";
    }

    @GetMapping("/login")
    public String login() {
        
        return "pages/login";
    }

}
