package com.schedulebuilder.class_scheduler.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schedulebuilder.class_scheduler.model.CourseSearchRequest;
import com.schedulebuilder.class_scheduler.model.AcademicPeriod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class ApiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public ApiService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    // Fetches available academic periods
    public List<AcademicPeriod> fetchAcademicPeriods() {
        List<AcademicPeriod> periods = new ArrayList<>();
        String url = "https://api.classes.iastate.edu/api/academic-periods";
        
        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode dataNode = rootNode.path("data");
            
            if (dataNode.isArray()) {
                for (JsonNode periodNode : dataNode) {
                    String id = periodNode.path("id").asText();
                    String name = periodNode.path("name").asText();
                    boolean isCurrent = periodNode.path("isCurrent").asBoolean();
                    long startDateTimestamp = periodNode.path("startDate").asLong();
                    
                    // Convert Unix timestamp to LocalDate
                    LocalDate startDate = Instant.ofEpochSecond(startDateTimestamp)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    
                    // Extract end date from the name using regex
                    String endDateStr = extractEndDateFromName(name);
                    
                    String startDateFormatted = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    
                    periods.add(new AcademicPeriod(id, name, startDateFormatted, endDateStr, isCurrent));
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching academic periods from API: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to hardcoded periods if API fails
            periods.add(new AcademicPeriod("ACADEMIC_PERIOD-2025Fall", "2025 Fall Semester (08/25/2025-12/19/2025)", "2025-08-25", "2025-12-19", true));
            periods.add(new AcademicPeriod("ACADEMIC_PERIOD-2025Spring", "2025 Spring Semester (01/21/2025-05/16/2025)", "2025-01-21", "2025-05-16", false));
            periods.add(new AcademicPeriod("ACADEMIC_PERIOD-2025Summer", "2025 Summer Semester (05/19/2025-08/08/2025)", "2025-05-19", "2025-08-08", false));
            periods.add(new AcademicPeriod("ACADEMIC_PERIOD-2024Winter", "2024-2025 Winter Session (12/23/2024-01/17/2025)", "2024-12-23", "2025-01-17", false));
        }
        
        return periods;
    }
    
    // Helper method to extract end date from the academic period name
    private String extractEndDateFromName(String name) {
        // Pattern to match dates in format (MM/DD/YYYY-MM/DD/YYYY)
        Pattern pattern = Pattern.compile("\\((\\d{2}/\\d{2}/\\d{4})-(\\d{2}/\\d{2}/\\d{4})\\)");
        Matcher matcher = pattern.matcher(name);
        
        if (matcher.find()) {
            String endDateStr = matcher.group(2); // The second date (end date)
            try {
                // Parse MM/DD/YYYY and convert to YYYY-MM-DD
                String[] parts = endDateStr.split("/");
                String month = parts[0];
                String day = parts[1];
                String year = parts[2];
                return year + "-" + String.format("%02d", Integer.parseInt(month)) + "-" + String.format("%02d", Integer.parseInt(day));
            } catch (Exception e) {
                System.err.println("Error parsing end date from name: " + name);
            }
        }
        
        // Fallback if parsing fails
        return "2025-12-31";
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
