package com.schedulebuilder.class_scheduler.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schedulebuilder.class_scheduler.model.Course;
import com.schedulebuilder.class_scheduler.model.ScheduleBuilder;
import com.schedulebuilder.class_scheduler.model.Section;
import com.schedulebuilder.class_scheduler.service.ApiService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
public class HomeController {

    @Autowired
    private ApiService apiService;

    @SuppressWarnings("unchecked")
    private List<Map<Course, Section>> getSchedulesFromSession(HttpSession session) {
        return (List<Map<Course, Section>>) session.getAttribute("generatedSchedules");
    }

    @GetMapping("/")
    public String home(@RequestParam(required = false, defaultValue = "ACADEMIC_PERIOD-2025Spring") String academicPeriod,
                       Model model,
                       HttpSession session) {
        try {
            // Fetch departments
            String departmentsJson = apiService.fetchDepartments(academicPeriod);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(departmentsJson);
            List<String> departments = new ArrayList<>();

            rootNode.path("data").forEach(departmentNode -> departments.add(departmentNode.asText()));

            model.addAttribute("departments", departments);

            // Get courses and selected sections from session
            @SuppressWarnings("unchecked")
            List<Course> courses = (List<Course>) session.getAttribute("courses");
            if (courses == null) {
                courses = new ArrayList<>();
            }
            model.addAttribute("courses", courses);

            @SuppressWarnings("unchecked")
            List<Section> selectedSections = (List<Section>) session.getAttribute("selectedSections");
            if (selectedSections == null) {
                selectedSections = new ArrayList<>();
            }
            model.addAttribute("selectedSections", selectedSections);

            // Add schedule navigation information
            List<Map<Course, Section>> schedules = getSchedulesFromSession(session);
            Integer currentIndex = (Integer) session.getAttribute("currentScheduleIndex");

            model.addAttribute("scheduleCount", schedules != null ? schedules.size() : 0);
            model.addAttribute("currentScheduleIndex", currentIndex != null ? currentIndex : 0);

        } catch (Exception e) {
            model.addAttribute("departments", Collections.singletonList("Error fetching departments."));
            model.addAttribute("courses", new ArrayList<>());
            e.printStackTrace();
        }

        return "index";
    }

    @PostMapping("/addCourse")
    public String addCourse(@RequestParam String department,
                            @RequestParam String courseId,
                            @RequestParam(required = false, defaultValue = "ACADEMIC_PERIOD-2025Spring") String academicPeriodId,
                            RedirectAttributes redirectAttributes,
                            HttpSession session) {
        try {
            // Fetch and parse courses for the given department and courseId
            String coursesJson = apiService.fetchCourses(academicPeriodId, department, courseId);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(coursesJson);
            JsonNode dataNode = rootNode.path("data");

            if (!dataNode.isArray() || dataNode.size() == 0) {
                // Add error message if no courses found
                redirectAttributes.addFlashAttribute("errorMessage", "No courses found for the given input.");
                return "redirect:/";
            }

            List<Course> newCourses = new ArrayList<>();
            for (JsonNode courseNode : dataNode) {
                String courseIdParsed = courseNode.path("courseId").asText();
                String courseName = courseNode.path("courseName").asText();
                String description = courseNode.path("description").asText();

                // Create the course object
                Course course = new Course(courseIdParsed, courseName, description);

                // Parse sections for the course
                JsonNode sectionsNode = courseNode.path("sections");
                if (sectionsNode.isArray()) {
                    for (JsonNode sectionNode : sectionsNode) {
                        String meetingPatterns = sectionNode.path("meetingPatterns").asText("");
                        String daysOfTheWeek = "Online";
                        String timeStart = "N/A";
                        String timeEnd = "N/A";

                        if (!meetingPatterns.isEmpty()) {
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

                        Section section = new Section(
                                daysOfTheWeek,
                                sectionNode.path("openSeats").asInt(),
                                sectionNode.path("instructor").asText(),
                                courseIdParsed,
                                timeStart,
                                timeEnd,
                                sectionNode.path("number").asText()
                        );
                        course.addSection(section);
                    }
                }

                newCourses.add(course);
            }

            // Retrieve existing courses from session or create a new list
            List<Course> sessionCourses = (List<Course>) session.getAttribute("courses");
            if (sessionCourses == null) {
                sessionCourses = new ArrayList<>();
            }

            // Add new courses to the session course list
            sessionCourses.addAll(newCourses);
            session.setAttribute("courses", sessionCourses);

            // Clear any previously generated schedules
            session.removeAttribute("generatedSchedules");
            session.removeAttribute("currentScheduleIndex");

            redirectAttributes.addFlashAttribute("successMessage", "Course successfully added!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error fetching courses. Please try again later.");
            e.printStackTrace();
        }

        // Redirect to the home page to display the updated list
        return "redirect:/";
    }


    @GetMapping("/generateSchedules")
    public String generateSchedules(HttpSession session, RedirectAttributes redirectAttributes) {
        List<Course> courses = (List<Course>) session.getAttribute("courses");
        if (courses == null || courses.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "No courses added to generate schedules.");
            return "redirect:/";
        }

        List<Map<Course, Section>> schedules = ScheduleBuilder.generateNonConflictingSchedules(courses, 10);
        if (schedules.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "No possible schedules found.");
        } else {
            session.setAttribute("generatedSchedules", schedules);
            session.setAttribute("currentScheduleIndex", 0);

            Map<Course, Section> firstSchedule = schedules.get(0);
            session.setAttribute("selectedSections", new ArrayList<>(firstSchedule.values()));

            redirectAttributes.addFlashAttribute("successMessage", "Schedules generated successfully.");
        }

        return "redirect:/";
    }

    @PostMapping("/nextSchedule")
    public String nextSchedule(HttpSession session, RedirectAttributes redirectAttributes) {
        List<Map<Course, Section>> schedules = getSchedulesFromSession(session);
        Integer currentScheduleIndex = (Integer) session.getAttribute("currentScheduleIndex");

        if (schedules == null || schedules.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "No schedules available.");
            return "redirect:/";
        }

        currentScheduleIndex = (currentScheduleIndex + 1) % schedules.size();
        session.setAttribute("currentScheduleIndex", currentScheduleIndex);

        Map<Course, Section> nextSchedule = schedules.get(currentScheduleIndex);
        session.setAttribute("selectedSections", new ArrayList<>(nextSchedule.values()));

        redirectAttributes.addFlashAttribute("successMessage", "Switched to the next schedule.");
        return "redirect:/";
    }

    @PostMapping("/previousSchedule")
    public String previousSchedule(HttpSession session, RedirectAttributes redirectAttributes) {
        List<Map<Course, Section>> schedules = getSchedulesFromSession(session);
        Integer currentScheduleIndex = (Integer) session.getAttribute("currentScheduleIndex");

        if (schedules == null || schedules.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "No schedules available.");
            return "redirect:/";
        }

        // Decrement index and handle wrap-around
        currentScheduleIndex = (currentScheduleIndex - 1 + schedules.size()) % schedules.size();
        session.setAttribute("currentScheduleIndex", currentScheduleIndex);

        Map<Course, Section> previousSchedule = schedules.get(currentScheduleIndex);
        session.setAttribute("selectedSections", new ArrayList<>(previousSchedule.values()));

        redirectAttributes.addFlashAttribute("successMessage", "Switched to the previous schedule.");
        return "redirect:/";
    }

    private String convertDaysOfWeek(String daysOfTheWeek) {
        return daysOfTheWeek
                .replace("M", "Monday,")
                .replace("T", "Tuesday,")
                .replace("W", "Wednesday,")
                .replace("R", "Thursday,")
                .replace("F", "Friday,")
                .replace("S", "Saturday,")
                .replace("U", "Sunday,")
                .replaceAll(",+$", ""); // Remove trailing comma
    }

}
