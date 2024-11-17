package com.schedulebuilder.class_scheduler.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.schedulebuilder.class_scheduler.model.CourseSearchRequest;
import com.schedulebuilder.class_scheduler.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * HomeController handles user requests and connects them to the ApiService.
 * It prepares data for the frontend and displays it using Thymeleaf templates.
 */
@Controller
public class HomeController {

    @Autowired
    private ApiService apiService;

    /**
     * Loads the homepage with a list of departments for the user to choose from.
     *
     * @param academicPeriod The academic period to load departments for (default is "ACADEMIC_PERIOD-2025Spring").
     * @param model The Model object used to pass data to the Thymeleaf template.
     * @return The name of the Thymeleaf template to display (in this case, "index").
     */
    @GetMapping("/")
    public String home(@RequestParam(required = false, defaultValue = "ACADEMIC_PERIOD-2025Spring") String academicPeriod, Model model) {
        try {
            // Fetch the raw JSON response
            String departmentsJson = apiService.fetchDepartments(academicPeriod);

            // Parse the JSON response into a list of departments
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(departmentsJson);
            JsonNode departmentsNode = rootNode.path("data");

            // Convert the JSON array into a List of strings
            List<String> departments = new ArrayList<>();
            if (departmentsNode.isArray()) {
                for (JsonNode departmentNode : departmentsNode) {
                    departments.add(departmentNode.asText());
                }
            }

            // Add the parsed list of departments to the model
            model.addAttribute("departments", departments);
        } catch (Exception e) {
            // Handle errors gracefully
            model.addAttribute("departments", Collections.singletonList("Error fetching departments. Please try again later."));
            e.printStackTrace();
        }
        return "index";
    }


    /**
     * Searches for courses based on user input (academic period, department, and course ID).
     *
     * @param academicPeriodId The academic period ID (e.g., "ACADEMIC_PERIOD-2025Spring").
     * @param department The department (e.g., "CPRE - Computer Engineering").
     * @param courseId The course ID (optional).
     * @param model The Model object used to pass data to the Thymeleaf template.
     * @return The name of the Thymeleaf template to display (in this case, "courses").
     */
//    @PostMapping("/search-courses")
//    public String searchCourses(
//            @RequestParam String academicPeriodId,
//            @RequestParam String department,
//            @RequestParam String courseId,
//            Model model) {
//        try {
//            // Fetch the courses based on the input parameters
//            String courses = apiService.fetchCourses(academicPeriodId, department, courseId);
//
//            // Add the fetched courses to the model
//            model.addAttribute("courses", courses);
//
//            // Re-add departments so the dropdown remains populated
//            String departments = apiService.fetchDepartments(academicPeriodId);
//            model.addAttribute("departments", departments);
//        } catch (Exception e) {
//            // Handle errors gracefully
//            model.addAttribute("courses", "Error fetching courses. Please try again later.");
//            e.printStackTrace();
//        }
//
//        return "index"; // Render the same index.html template with updated data
//    }

    // test method for testing api call, returns raw json
    @PostMapping("/")
    public String searchCourses(
            @RequestParam String department,
            @RequestParam String courseId,
            @RequestParam(required = false, defaultValue = "ACADEMIC_PERIOD-2025Spring") String academicPeriodId,
            Model model) {
        try {
            // Fetch the raw JSON response for the selected course
            String coursesJson = apiService.fetchCourses(academicPeriodId, department, courseId);

            // Log the raw JSON response (for debugging)
            System.out.println("Fetched Courses JSON: " + coursesJson);

            // Use ObjectMapper to parse the JSON response and extract the "sections" array
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(coursesJson);
            JsonNode dataNode = rootNode.path("data");

            List<String> sections = new ArrayList<>();
            if (dataNode.isArray() && dataNode.size() > 0) {
                for (JsonNode courseNode : dataNode) {
                    JsonNode sectionsNode = courseNode.path("sections");
                    if (sectionsNode.isArray()) {
                        for (JsonNode sectionNode : sectionsNode) {
                            sections.add(sectionNode.toString());
                        }
                    }
                }
            }

            // Add the extracted sections to the model
            model.addAttribute("courseSections", sections.isEmpty() ? "No sections found." : sections);

            // Re-add departments to keep the dropdown populated
            String departmentsJson = apiService.fetchDepartments(academicPeriodId);
            JsonNode departmentsRootNode = objectMapper.readTree(departmentsJson);
            JsonNode departmentsNode = departmentsRootNode.path("data");
            System.out.println("Extracted Sections: " + sections);

            List<String> departments = new ArrayList<>();
            if (departmentsNode.isArray()) {
                for (JsonNode departmentNode : departmentsNode) {
                    departments.add(departmentNode.asText());
                }
            }
            model.addAttribute("departments", departments);
        } catch (Exception e) {
            // Handle errors gracefully
            model.addAttribute("courseSections", "Error fetching course sections. Please try again later.");
            model.addAttribute("departments", Collections.singletonList("Error fetching departments."));
            e.printStackTrace();
        }


        return "index"; // Render the same index.html template with updated data
    }

}