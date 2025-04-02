package com.paymybuddy.controller.pages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class LoginPageController {

    private static final Logger logger = LoggerFactory.getLogger(LoginPageController.class);

    @GetMapping("/login")
    public String showLoginPage() {
        logger.info("Login page requested");
        return "login.html";
    }

}

