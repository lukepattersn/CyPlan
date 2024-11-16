package com.schedulebuilder.class_scheduler.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ApiService {

    public String fetchDepartments(String academicPeriod) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.classes.iastate.edu/api/departments?academicPeriod=" + academicPeriod;
        return restTemplate.getForObject(url, String.class);
    }
}
