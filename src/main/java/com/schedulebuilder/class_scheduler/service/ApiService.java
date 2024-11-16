package com.schedulebuilder.class_scheduler.service;

import com.schedulebuilder.class_scheduler.model.CourseSearchRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

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

    public String fetchCourses(String academicPeriodId, String department, String courseId) {
        String url = "https://api.classes.iastate.edu/api/courses/search";

        CourseSearchRequest requestPayload = new CourseSearchRequest(academicPeriodId, department, courseId);

        return "test";
    }
}
