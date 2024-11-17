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

/**
 * HomeController handles user requests and connects them to the ApiService.
 * It prepares data for the frontend and displays it using Thymeleaf templates.
 */
@Controller
public class HomeController {

    @Autowired
    private ApiService apiService; // Service for API calls

    private List<Course> courses = new ArrayList<>(); // Stores courses fetched from the API

    /**
     * Loads the homepage with a list of departments and any flash messages.
     *
     * @param academicPeriod The academic period to load departments for (default is "ACADEMIC_PERIOD-2025Spring").
     * @param model          The Model object used to pass data to the Thymeleaf template.
     * @return The name of the Thymeleaf template to display ("index").
     */
    @GetMapping("/")
    public String home(@RequestParam(required = false, defaultValue = "ACADEMIC_PERIOD-2025Spring") String academicPeriod,
                       Model model) {
        try {
            // Fetch the raw JSON response from the API
            String departmentsJson = apiService.fetchDepartments(academicPeriod);

            // Parse the JSON response
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(departmentsJson);
            JsonNode departmentsNode = rootNode.path("data");

            // Extract department names
            List<String> departments = new ArrayList<>();
            if (departmentsNode.isArray()) {
                for (JsonNode departmentNode : departmentsNode) {
                    departments.add(departmentNode.asText());
                }
            }

            // Add departments to the model for rendering in the dropdown
            model.addAttribute("departments", departments);
        } catch (Exception e) {
            // Handle errors gracefully by displaying a fallback error message
            model.addAttribute("departments", Collections.singletonList("Error fetching departments. Please try again later."));
            e.printStackTrace();
        }

        return "index"; // Render the "index" page
    }

    /**
     * Processes a course search request, fetches courses and sections, and redirects back to the homepage
     * with appropriate flash messages.
     *
     * @param department      The department selected by the user.
     * @param courseId        The course ID entered by the user.
     * @param academicPeriodId The academic period (default is "ACADEMIC_PERIOD-2025Spring").
     * @param redirectAttributes Used to pass flash messages to the redirected view.
     * @return A redirect to the homepage ("/").
     */
    @PostMapping("/")
    public String searchCourses(
            @RequestParam String department,
            @RequestParam String courseId,
            @RequestParam(required = false, defaultValue = "ACADEMIC_PERIOD-2025Spring") String academicPeriodId,
            RedirectAttributes redirectAttributes) {
        try {
            // Call the API to fetch courses
            String coursesJson = apiService.fetchCourses(academicPeriodId, department, courseId);

            // Parse the JSON response into Course and Section objects
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(coursesJson);
            JsonNode dataNode = rootNode.path("data");

            // Clear the existing courses list before adding new data
            courses.clear();

            if (dataNode.isArray() && dataNode.size() > 0) {
                // Iterate through the response and populate the courses list
                for (JsonNode courseNode : dataNode) {
                    String courseIdParsed = courseNode.path("courseId").asText();
                    String courseName = courseNode.path("courseName").asText();

                    // Create a new Course object
                    Course course = new Course(courseIdParsed, courseName);

                    // Parse sections within each course
                    JsonNode sectionsNode = courseNode.path("sections");
                    if (sectionsNode.isArray()) {
                        for (JsonNode sectionNode : sectionsNode) {
                            String meetingPatterns = sectionNode.path("meetingPatterns").asText();
                            int openSeats = sectionNode.path("openSeats").asInt();
                            String instructor = sectionNode.path("instructor").asText();
                            String sectionNumber = sectionNode.path("number").asText();

                            // Parse meeting patterns
                            String[] meetingParts = meetingPatterns.split("\\|");
                            String daysOfTheWeek = meetingParts[0].trim();
                            String[] times = meetingParts[1].split("-");
                            String timeStart = times[0].trim();
                            String timeEnd = times[1].trim();

                            // Convert shorthand days to full names
                            daysOfTheWeek = convertDaysOfWeek(daysOfTheWeek);

                            // Create a Section object
                            Section section = new Section(daysOfTheWeek, openSeats, instructor, courseIdParsed, timeStart, timeEnd, sectionNumber);

                            // Add the section to the course
                            course.addSection(section);
                        }
                    }

                    // Add the course to the list of courses
                    courses.add(course);
                }

                // Flash a success message if courses are found
                redirectAttributes.addFlashAttribute("successMessage", "Courses successfully retrieved!");
            } else {
                // Flash an error message if no courses are found
                redirectAttributes.addFlashAttribute("errorMessage", "No courses found for the given input.");
            }
        } catch (Exception e) {
            // Handle API or parsing errors
            redirectAttributes.addFlashAttribute("errorMessage", "Error fetching courses. Please try again later.");
            e.printStackTrace(); // Log the error for debugging
        }

        return "redirect:/"; // Redirect to the homepage
    }

    /**
     * Converts shorthand day names (e.g., "MWF") to full names (e.g., "Monday Wednesday Friday").
     *
     * @param shorthand The shorthand day names (e.g., "MWF").
     * @return The full day names (e.g., "Monday Wednesday Friday").
     */
    private String convertDaysOfWeek(String shorthand) {
        shorthand = shorthand.toUpperCase(); // Ensure uppercase for consistent parsing
        return shorthand
                .replace("M", "Monday ")
                .replace("T", "Tuesday ")
                .replace("W", "Wednesday ")
                .replace("R", "Thursday ")
                .replace("F", "Friday ")
                .trim(); // Remove trailing spaces
    }
}
