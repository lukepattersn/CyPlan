package com.schedulebuilder.class_scheduler.controller;

import com.schedulebuilder.class_scheduler.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Import for Model
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ApiController {

    @Autowired
    private ApiService apiService;

    @GetMapping("/departments")
    public String getDepartments(@RequestParam String academicPeriod, Model model) {
        // Fetch departments from the service
        String departments = apiService.fetchDepartments(academicPeriod);

        // Add the response to the model
        model.addAttribute("departments", departments);

        // Return the name of the HTML template
        return "index";
    }
}
