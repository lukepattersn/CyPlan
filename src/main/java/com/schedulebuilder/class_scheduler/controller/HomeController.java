package com.schedulebuilder.class_scheduler.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schedulebuilder.class_scheduler.model.Course;
import com.schedulebuilder.class_scheduler.model.ScheduleBuilder;
import com.schedulebuilder.class_scheduler.model.Section;
import com.schedulebuilder.class_scheduler.model.AcademicPeriod;
import com.schedulebuilder.class_scheduler.service.ApiService;
import com.schedulebuilder.class_scheduler.service.CourseService;
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
    
    @Autowired
    private CourseService courseService;

    @SuppressWarnings("unchecked")
    private List<Map<Course, List<Section>>> getSchedulesFromSession(HttpSession session) {
        return (List<Map<Course, List<Section>>>) session.getAttribute("generatedSchedules");
    }

    @GetMapping("/")
    public String home(@RequestParam(required = false, defaultValue = "ACADEMIC_PERIOD-2025Fall") String academicPeriod,
                       Model model,
                       HttpSession session) {
        try {
            // Fetch available academic periods
            List<AcademicPeriod> academicPeriods = apiService.fetchAcademicPeriods();
            model.addAttribute("academicPeriods", academicPeriods);
            model.addAttribute("selectedAcademicPeriod", academicPeriod);

            // Store current academic period in session
            session.setAttribute("currentAcademicPeriod", academicPeriod);

            // Find the current academic period details for calendar dates
            AcademicPeriod currentPeriod = academicPeriods.stream()
                    .filter(period -> period.getId().equals(academicPeriod))
                    .findFirst()
                    .orElse(academicPeriods.get(0)); // Default to first period if not found
            
            model.addAttribute("currentPeriodStartDate", currentPeriod.getStartDate());
            model.addAttribute("currentPeriodEndDate", currentPeriod.getEndDate());

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
            List<Map<Course, List<Section>>> schedules = getSchedulesFromSession(session);
            Integer currentIndex = (Integer) session.getAttribute("currentScheduleIndex");

            model.addAttribute("scheduleCount", schedules != null ? schedules.size() : 0);
            model.addAttribute("currentScheduleIndex", currentIndex != null ? currentIndex : 0);

        } catch (Exception e) {
            model.addAttribute("academicPeriods", Collections.emptyList());
            model.addAttribute("departments", Collections.singletonList("Error fetching departments."));
            model.addAttribute("courses", new ArrayList<>());
            e.printStackTrace();
        }

        return "index";
    }

    @PostMapping("/addCourse")
    public String addCourse(@RequestParam String department,
                            @RequestParam String courseId,
                            @RequestParam(required = false) String academicPeriodId,
                            RedirectAttributes redirectAttributes,
                            HttpSession session) {
        try {
            // Use academic period from session if not provided
            if (academicPeriodId == null || academicPeriodId.isEmpty()) {
                academicPeriodId = (String) session.getAttribute("currentAcademicPeriod");
                if (academicPeriodId == null) {
                    academicPeriodId = "ACADEMIC_PERIOD-2025Fall";
                }
            }

            // Fetch and parse courses for the given department and courseId
            String coursesJson = apiService.fetchCourses(academicPeriodId, department, courseId);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(coursesJson);
            JsonNode dataNode = rootNode.path("data");

            if (!dataNode.isArray() || dataNode.size() == 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "No courses found for the given input.");
                return "redirect:/";
            }

            // Use CourseService to parse courses
            List<Course> newCourses = courseService.parseCourses(dataNode);

            // Retrieve existing courses from session or create a new list
            List<Course> sessionCourses = (List<Course>) session.getAttribute("courses");
            if (sessionCourses == null) {
                sessionCourses = new ArrayList<>();
            }

            // Check for duplicates before adding
            for (Course newCourse : newCourses) {
                boolean exists = sessionCourses.stream()
                        .anyMatch(existingCourse -> existingCourse.getCourseId().equals(newCourse.getCourseId()));
                if (!exists) {
                    sessionCourses.add(newCourse);
                }
            }

            session.setAttribute("courses", sessionCourses);

            // Clear any previously generated schedules
            session.removeAttribute("generatedSchedules");
            session.removeAttribute("currentScheduleIndex");
            session.removeAttribute("selectedSections");

            redirectAttributes.addFlashAttribute("successMessage", "Course successfully added!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error fetching courses. Please try again later.");
            e.printStackTrace();
        }

        return "redirect:/";
    }

    @PostMapping("/changeAcademicPeriod")
    public String changeAcademicPeriod(@RequestParam String academicPeriod,
                                       RedirectAttributes redirectAttributes,
                                       HttpSession session) {
        try {
            // Clear all session data when changing academic period
            session.removeAttribute("courses");
            session.removeAttribute("generatedSchedules");
            session.removeAttribute("currentScheduleIndex");
            session.removeAttribute("selectedSections");
            
            // Set new academic period
            session.setAttribute("currentAcademicPeriod", academicPeriod);
            
            redirectAttributes.addFlashAttribute("successMessage", "Academic period changed successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error changing academic period.");
        }
        
        return "redirect:/?academicPeriod=" + academicPeriod;
    }

    @GetMapping("/generateSchedules")
    public String generateSchedules(HttpSession session, RedirectAttributes redirectAttributes) {
        List<Course> courses = (List<Course>) session.getAttribute("courses");
        if (courses == null || courses.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "No courses added to generate schedules.");
            return "redirect:/";
        }

        try {
            // Generate schedules with improved logic
            List<Map<Course, List<Section>>> schedules = ScheduleBuilder.generateNonConflictingSchedules(courses, 100);
            
            if (schedules.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "No valid schedules found. This may be due to conflicting times, insufficient commute time, or missing required recitations/labs.");
            } else {
                session.setAttribute("generatedSchedules", schedules);
                session.setAttribute("currentScheduleIndex", 0);

                // Flatten sections for display (handle multiple sections per course)
                Map<Course, List<Section>> firstSchedule = schedules.get(0);
                List<Section> allSections = new ArrayList<>();
                for (Map.Entry<Course, List<Section>> entry : firstSchedule.entrySet()) {
                    allSections.addAll(entry.getValue());
                }
                session.setAttribute("selectedSections", allSections);

                redirectAttributes.addFlashAttribute("successMessage", 
                    "Generated " + schedules.size() + " valid schedule(s) with proper commute time and recitation requirements.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error generating schedules: " + e.getMessage());
            e.printStackTrace();
        }

        return "redirect:/";
    }

    @PostMapping("/nextSchedule")
    public String nextSchedule(HttpSession session, RedirectAttributes redirectAttributes) {
        List<Map<Course, List<Section>>> schedules = getSchedulesFromSession(session);
        Integer currentScheduleIndex = (Integer) session.getAttribute("currentScheduleIndex");

        if (schedules == null || schedules.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "No schedules available.");
            return "redirect:/";
        }

        currentScheduleIndex = (currentScheduleIndex + 1) % schedules.size();
        session.setAttribute("currentScheduleIndex", currentScheduleIndex);

        Map<Course, List<Section>> nextSchedule = schedules.get(currentScheduleIndex);
        List<Section> allSections = new ArrayList<>();
        for (Map.Entry<Course, List<Section>> entry : nextSchedule.entrySet()) {
            allSections.addAll(entry.getValue());
        }
        session.setAttribute("selectedSections", allSections);

        redirectAttributes.addFlashAttribute("successMessage", "Switched to the next schedule.");
        return "redirect:/";
    }

    @PostMapping("/previousSchedule")
    public String previousSchedule(HttpSession session, RedirectAttributes redirectAttributes) {
        List<Map<Course, List<Section>>> schedules = getSchedulesFromSession(session);
        Integer currentScheduleIndex = (Integer) session.getAttribute("currentScheduleIndex");

        if (schedules == null || schedules.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "No schedules available.");
            return "redirect:/";
        }

        // Decrement index and handle wrap-around
        currentScheduleIndex = (currentScheduleIndex - 1 + schedules.size()) % schedules.size();
        session.setAttribute("currentScheduleIndex", currentScheduleIndex);

        Map<Course, List<Section>> previousSchedule = schedules.get(currentScheduleIndex);
        List<Section> allSections = new ArrayList<>();
        for (Map.Entry<Course, List<Section>> entry : previousSchedule.entrySet()) {
            allSections.addAll(entry.getValue());
        }
        session.setAttribute("selectedSections", allSections);

        redirectAttributes.addFlashAttribute("successMessage", "Switched to the previous schedule.");
        return "redirect:/";
    }

    @PostMapping("/removeCourse")
    public String removeCourse(@RequestParam String department,
                               @RequestParam String courseId,
                               RedirectAttributes redirectAttributes,
                               HttpSession session) {
        try {
            @SuppressWarnings("unchecked")
            List<Course> courses = (List<Course>) session.getAttribute("courses");

            if (courses != null) {
                String fullCourseId = department + " " + courseId;
                boolean removed = courses.removeIf(c -> c.getCourseId().equals(fullCourseId));

                if (removed) {
                    session.setAttribute("courses", courses);

                    // Clear ALL schedule-related attributes
                    session.removeAttribute("selectedSections");
                    session.removeAttribute("generatedSchedules");
                    session.removeAttribute("currentScheduleIndex");

                    redirectAttributes.addFlashAttribute("successMessage", "Course removed successfully");
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", "Course not found");
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error removing course: " + e.getMessage());
        }
        return "redirect:/";
    }
}
