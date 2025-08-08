package com.realestate.sellerfunnel.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LogoutController {

    @GetMapping("/logged-out")
    public String logout() {
        return "logged-out";
    }
}