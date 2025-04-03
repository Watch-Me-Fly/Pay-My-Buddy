package com.paymybuddy.controller.pages;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Log4j2
public class TransactionPageController {

    @GetMapping("/user/transactions")
    public String showTransactionsPage() {
        log.info("show transactions page");
        return "/user/transactions.html";
    }

}
