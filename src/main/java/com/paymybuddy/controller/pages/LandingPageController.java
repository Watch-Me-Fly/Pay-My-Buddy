package com.paymybuddy.controller.pages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LandingPageController {

    private static final Logger logger = LoggerFactory.getLogger(LandingPageController.class);

    @GetMapping("/")
    public String showLandingPage() {
        logger.info("Landing page requested");
        return "forward:index.html";
    }

}
