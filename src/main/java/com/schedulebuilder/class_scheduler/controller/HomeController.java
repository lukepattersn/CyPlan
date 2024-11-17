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

import jakarta.servlet.http.HttpSession; // Ensure using jakarta.servlet for Spring Boot 3.x
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private ApiService apiService;

    @GetMapping("/")
    public String home(@RequestParam(required = false, defaultValue = "ACADEMIC_PERIOD-2025Spring") String academicPeriod,
                       Model model,
                       HttpSession session) {
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

            // Retrieve the list of courses from the session
            List<Course> courses = (List<Course>) session.getAttribute("courses");
            if (courses == null) {
                courses = new ArrayList<>();
                session.setAttribute("courses", courses);
            }
            model.addAttribute("courses", courses);

        } catch (Exception e) {
            // Fallback handling
            model.addAttribute("departments", Collections.singletonList("Error fetching departments. Please try again later."));
            model.addAttribute("courses", new ArrayList<Course>());
            e.printStackTrace();
        }

        return "index";
    }

    @PostMapping("/addCourse")
    public String addCourse(@RequestParam String department,
                            @RequestParam String courseId,
                            @RequestParam(required = false, defaultValue = "ACADEMIC_PERIOD-2025Spring") String academicPeriodId,
                            RedirectAttributes redirectAttributes,
                            Model model,
                            HttpSession session) {
        try {
            // Fetch and parse courses for the given department and courseId
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
                            // Safely handle meetingPatterns
                            String meetingPatterns = null;
                            if (!sectionNode.path("meetingPatterns").isMissingNode()) {
                                meetingPatterns = sectionNode.path("meetingPatterns").asText();
                            }

                            int openSeats = sectionNode.path("openSeats").asInt();
                            String instructor = sectionNode.path("instructor").asText();
                            String sectionNumber = sectionNode.path("number").asText();

                            String daysOfTheWeek = "Online";
                            String timeStart = "N/A";
                            String timeEnd = "N/A";

                            if (meetingPatterns != null && !meetingPatterns.isEmpty()) {
                                String[] meetingParts = meetingPatterns.split("\\|");
                                if (meetingParts.length >= 2) {
                                    daysOfTheWeek = convertDaysOfWeek(meetingParts[0].trim());
                                    String[] times = meetingParts[1].split("-");
                                    if (times.length >= 2) {
                                        timeStart = times[0].trim();
                                        timeEnd = times[1].trim();
                                    }
                                }
                            }

                            Section section = new Section(daysOfTheWeek, openSeats, instructor, courseIdParsed, timeStart, timeEnd, sectionNumber);
                            course.addSection(section);
                        }
                    }
                    courses.add(course);
                }

                // Add success message
                redirectAttributes.addFlashAttribute("successMessage", "Course successfully added!");
            } else {
                // Add error message if no courses found
                redirectAttributes.addFlashAttribute("errorMessage", "No courses found for the given input.");
            }

            // Retrieve the list of courses from the session and add the new courses
            List<Course> sessionCourses = (List<Course>) session.getAttribute("courses");
            if (sessionCourses == null) {
                sessionCourses = new ArrayList<>();
            }
            sessionCourses.addAll(courses);
            session.setAttribute("courses", sessionCourses);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error fetching courses. Please try again later.");
            e.printStackTrace();
        }

        // Redirect to the home page to display the updated list
        return "redirect:/";
    }

    private String convertDaysOfWeek(String daysOfTheWeek) {
        daysOfTheWeek = daysOfTheWeek.toUpperCase(); // Ensure uppercase for consistent parsing
        return daysOfTheWeek
                .replace("M", "Monday")
                .replace("T", "Tuesday")
                .replace("W", "Wednesday")
                .replace("R", "Thursday")
                .replace("F", "Friday")
                .trim();
    }
}
