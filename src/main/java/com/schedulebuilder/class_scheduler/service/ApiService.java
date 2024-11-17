package com.schedulebuilder.class_scheduler.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schedulebuilder.class_scheduler.model.CourseSearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

@Service
public class ApiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public ApiService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    // Fetches all departments for the given academic period
    public String fetchDepartments(String academicPeriod) {
        String url = "https://api.classes.iastate.edu/api/departments?academicPeriod=" + academicPeriod;
        try {
            return restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            System.err.println("Error fetching departments from API: " + e.getMessage());
            e.printStackTrace();
            return "{\"error\": \"Error fetching departments. Please try again later.\"}";
        }
    }

    // Fetches courses based on the academic period, department, and course ID
    public String fetchCourses(String academicPeriodId, String department, String courseId) {
        String url = "https://api.classes.iastate.edu/api/courses/search";
        CourseSearchRequest requestPayload = new CourseSearchRequest(academicPeriodId, department, courseId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        HttpEntity<CourseSearchRequest> requestEntity = new HttpEntity<>(requestPayload, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            return response.getBody();
        } catch (Exception e) {
            System.err.println("Error fetching courses from API: " + e.getMessage());
            e.printStackTrace();
            return "{\"error\": \"Error fetching courses. Please try again later.\"}";
        }
    }
}
