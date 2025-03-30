package com.paymybuddy.controller.pages;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Log4j2
public class RelationPageController {

    @GetMapping("/user/relations")
    public String showRelationsPage(Model model) {

        log.info("show relations page");

        return "/user/relation.html";
    }
}