package com.schedulebuilder.class_scheduler.controller;
import com.schedulebuilder.class_scheduler.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @Autowired
    private ApiService apiService;

    @GetMapping("/")
    public String home(@RequestParam(required = false, defaultValue = "ACADEMIC_PERIOD-2025Spring") String academicPeriod, Model model) {
        try {
            String departments = apiService.fetchDepartments(academicPeriod);
            model.addAttribute("departments", departments);
        } catch (Exception e) {
            model.addAttribute("departments", "Error fetching departments. Please try again later.");
            e.printStackTrace();
        }
        return "index";
    }
}