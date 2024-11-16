package com.schedulebuilder.class_scheduler.controller;

import com.schedulebuilder.class_scheduler.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {

    @Autowired
    private ApiService apiService;

    @GetMapping("/departments")
    public String getDepartments(@RequestParam String academicPeriod) {
        return apiService.fetchDepartments(academicPeriod);
    }
}

