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

    // This method calls the api to get all the departments
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


    // This method calls the api to fetch courses based off the forum
    public String fetchCourses(String academicPeriodId, String department, String courseId) {
        String url = "https://api.classes.iastate.edu/api/courses/search";

        // create the payload using the CourseSearchRequest class
        CourseSearchRequest requestPayload = new CourseSearchRequest(academicPeriodId, department, courseId);
        System.out.println(requestPayload.toString());

        // set headers to specify JSON content
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        // create the HTTP entity with the payload and headers
        HttpEntity<CourseSearchRequest> requestEntity = new HttpEntity<>(requestPayload, headers);

        // use RestTemplate to send the request
        RestTemplate restTemplate = new RestTemplate();

        try {
            // send the POST request and get the response
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            // Return the response body (raw JSON from the API)
            return response.getBody();
        } catch (Exception e) {
            // log the error for debugging
            System.err.println("Error fetching courses from API: " + e.getMessage());
            e.printStackTrace();

            // return an error message
            return "{\"error\": \"Error fetching courses. Please try again later.\"}";
        }
    }


}
