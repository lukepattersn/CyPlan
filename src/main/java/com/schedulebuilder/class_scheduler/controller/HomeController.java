package com.schedulebuilder.class_scheduler.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schedulebuilder.class_scheduler.model.Course;
import com.schedulebuilder.class_scheduler.model.Section;
import com.schedulebuilder.class_scheduler.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private ApiService apiService;

    @GetMapping("/")
    public String home(@RequestParam(required = false, defaultValue = "ACADEMIC_PERIOD-2025Spring") String academicPeriod,
                       @RequestParam(required = false) String department,
                       Model model) {
        try {
            // Fetch departments
            String departmentsJson = apiService.fetchDepartments(academicPeriod);

            // Parse the JSON response
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(departmentsJson);
            JsonNode departmentsNode = rootNode.path("data");

            List<String> departments = new ArrayList<>();
            if (departmentsNode.isArray()) {
                for (JsonNode departmentNode : departmentsNode) {
                    departments.add(departmentNode.asText());
                }
            }

            // Add departments to the model
            model.addAttribute("departments", departments);
            // Add selected department to the model
            model.addAttribute("selectedDepartment", department);
        } catch (Exception e) {
            // Fallback handling
            model.addAttribute("departments", Collections.singletonList("Error fetching departments. Please try again later."));
            model.addAttribute("selectedDepartment", null);
            e.printStackTrace();
        }

        // Ensure `courses` is always added to the model
        if (!model.containsAttribute("courses")) {
            model.addAttribute("courses", new ArrayList<Course>());
        }

        return "index";
    }


    @PostMapping("/")
    public String searchCourses(@RequestParam String department,
                                @RequestParam String courseId,
                                @RequestParam(required = false, defaultValue = "ACADEMIC_PERIOD-2025Spring") String academicPeriodId,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        try {
            // Fetch and parse courses
            String coursesJson = apiService.fetchCourses(academicPeriodId, department, courseId);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(coursesJson);
            JsonNode dataNode = rootNode.path("data");

            List<Course> courses = new ArrayList<>();
            if (dataNode.isArray() && dataNode.size() > 0) {
                for (JsonNode courseNode : dataNode) {
                    String courseIdParsed = courseNode.path("courseId").asText();
                    String courseName = courseNode.path("courseName").asText();
                    String description = courseNode.path("description").asText();

                    Course course = new Course(courseIdParsed, courseName, description);
                    JsonNode sectionsNode = courseNode.path("sections");
                    if (sectionsNode.isArray()) {
                        for (JsonNode sectionNode : sectionsNode) {
                            String meetingPatterns = sectionNode.path("meetingPatterns").asText();
                            int openSeats = sectionNode.path("openSeats").asInt();
                            String instructor = sectionNode.path("instructor").asText();
                            String sectionNumber = sectionNode.path("number").asText();

                            String[] meetingParts = meetingPatterns.split("\\|");
                            String daysOfTheWeek = meetingParts[0].trim();
                            String[] times = meetingParts[1].split("-");
                            String timeStart = times[0].trim();
                            String timeEnd = times[1].trim();

                            daysOfTheWeek = convertDaysOfWeek(daysOfTheWeek);

                            Section section = new Section(daysOfTheWeek, openSeats, instructor, courseIdParsed, timeStart, timeEnd, sectionNumber);
                            course.addSection(section);
                        }
                    }
                    courses.add(course);
                }

                redirectAttributes.addFlashAttribute("successMessage", "Courses successfully retrieved!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "No courses found for the given input.");
            }

            // Add courses to the model
            model.addAttribute("courses", courses);
            // Add selected department to the model to maintain the selection
            model.addAttribute("selectedDepartment", department);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error fetching courses. Please try again later.");
            model.addAttribute("courses", Collections.emptyList());
            model.addAttribute("selectedDepartment", department);
            e.printStackTrace();
        }

        // Also, fetch departments again to ensure the dropdown is populated
        try {
            String departmentsJson = apiService.fetchDepartments(academicPeriodId);

            // Parse the JSON response
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(departmentsJson);
            JsonNode departmentsNode = rootNode.path("data");

            List<String> departments = new ArrayList<>();
            if (departmentsNode.isArray()) {
                for (JsonNode departmentNode : departmentsNode) {
                    departments.add(departmentNode.asText());
                }
            }

            // Add departments to the model
            model.addAttribute("departments", departments);
        } catch (Exception e) {
            // Fallback handling
            model.addAttribute("departments", Collections.singletonList("Error fetching departments. Please try again later."));
            e.printStackTrace();
        }

        // Return the view directly instead of redirecting
        return "index";
    }

    private String convertDaysOfWeek(String daysOfTheWeek) {
        daysOfTheWeek = daysOfTheWeek.toUpperCase(); // Ensure uppercase for consistent parsing
        return daysOfTheWeek
                .replace("M", "Monday ")
                .replace("T", "Tuesday ")
                .replace("W", "Wednesday ")
                .replace("R", "Thursday ")
                .replace("F", "Friday ")
                .trim();
    }
}
