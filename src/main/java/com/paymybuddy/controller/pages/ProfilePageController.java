package com.paymybuddy.controller.pages;

import com.paymybuddy.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@Log4j2
@RequiredArgsConstructor
public class ProfilePageController {

    private final UserService userService;

    @GetMapping("/user/profile")
    public String showProfilePage() {

        log.info("show profile page");
        return "/user/profile.html";

    }

}
