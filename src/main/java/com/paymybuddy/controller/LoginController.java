package com.paymybuddy.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    @GetMapping("/profile")
    public String getProfile() {
        return "Welcome to Pay my Buddy";
    }
}

