package com.schedulebuilder.class_scheduler.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index"; // Corresponds to src/main/resources/templates/index.html
    }
}
