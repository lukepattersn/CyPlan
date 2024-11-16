package com.schedulebuilder.class_scheduler.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ApiService {

    public String fetchDepartments(String academicPeriod) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.classes.iastate.edu/api/departments?academicPeriod=" + academicPeriod;
        try {
            return restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            System.err.println("Error fetching departments from API: " + e.getMessage());
            e.printStackTrace();
            return "Error fetching departments. Please check the logs for details.";
        }
    }
}
