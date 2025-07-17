package com.schedulebuilder.class_scheduler.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schedulebuilder.class_scheduler.model.Course;
import com.schedulebuilder.class_scheduler.model.ScheduleBuilder;
import com.schedulebuilder.class_scheduler.model.Section;
import com.schedulebuilder.class_scheduler.model.AcademicPeriod;
import com.schedulebuilder.class_scheduler.model.SchedulePreferences;
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
import java.util.logging.Logger;
import java.util.logging.Level;

@Controller
public class HomeController {
    
    private static final Logger logger = Logger.getLogger(HomeController.class.getName());
    private static final String DEFAULT_ACADEMIC_PERIOD = "ACADEMIC_PERIOD-2025Fall";
    private static final String SESSION_COURSES = "courses";
    private static final String SESSION_GENERATED_SCHEDULES = "generatedSchedules";
    private static final String SESSION_CURRENT_SCHEDULE_INDEX = "currentScheduleIndex";
    private static final String SESSION_SELECTED_SECTIONS = "selectedSections";
    private static final String SESSION_CURRENT_ACADEMIC_PERIOD = "currentAcademicPeriod";

    @Autowired
    private ApiService apiService;
    
    @Autowired
    private CourseService courseService;

    @SuppressWarnings("unchecked")
    private List<Map<Course, List<Section>>> getSchedulesFromSession(HttpSession session) {
        return (List<Map<Course, List<Section>>>) session.getAttribute(SESSION_GENERATED_SCHEDULES);
    }
    
    @SuppressWarnings("unchecked")
    private List<Course> getCoursesFromSession(HttpSession session) {
        List<Course> courses = (List<Course>) session.getAttribute(SESSION_COURSES);
        return courses != null ? courses : new ArrayList<>();
    }
    
    @SuppressWarnings("unchecked")
    private List<Section> getSelectedSectionsFromSession(HttpSession session) {
        List<Section> sections = (List<Section>) session.getAttribute(SESSION_SELECTED_SECTIONS);
        return sections != null ? sections : new ArrayList<>();
    }
    
    private String getCurrentAcademicPeriod(HttpSession session) {
        String period = (String) session.getAttribute(SESSION_CURRENT_ACADEMIC_PERIOD);
        return period != null ? period : DEFAULT_ACADEMIC_PERIOD;
    }
    
    private void clearScheduleSession(HttpSession session) {
        session.removeAttribute(SESSION_GENERATED_SCHEDULES);
        session.removeAttribute(SESSION_CURRENT_SCHEDULE_INDEX);
        session.removeAttribute(SESSION_SELECTED_SECTIONS);
    }

    @GetMapping("/")
    public String home(@RequestParam(required = false, defaultValue = DEFAULT_ACADEMIC_PERIOD) String academicPeriod,
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
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addCourse(@RequestParam String department,
                                                        @RequestParam String courseId,
                                                        @RequestParam(required = false) String academicPeriodId,
                                                        HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
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
                response.put("success", false);
                response.put("message", "Course does not exist.");
                return ResponseEntity.ok(response);
            }

            // Use CourseService to parse courses
            List<Course> newCourses = courseService.parseCourses(dataNode);
            
            // Check if no valid courses were parsed
            if (newCourses.isEmpty()) {
                response.put("success", false);
                response.put("message", "Course does not exist or has no available sections.");
                return ResponseEntity.ok(response);
            }

            // Retrieve existing courses from session or create a new list
            List<Course> sessionCourses = (List<Course>) session.getAttribute("courses");
            if (sessionCourses == null) {
                sessionCourses = new ArrayList<>();
            }

            // Check for duplicates before adding
            int addedCount = 0;
            for (Course newCourse : newCourses) {
                boolean exists = sessionCourses.stream()
                        .anyMatch(existingCourse -> existingCourse.getCourseId().equals(newCourse.getCourseId()));
                if (!exists) {
                    sessionCourses.add(newCourse);
                    addedCount++;
                }
            }
            
            // Check if no new courses were added due to duplicates
            if (addedCount == 0) {
                response.put("success", false);
                response.put("message", "Course is already added to your schedule.");
                return ResponseEntity.ok(response);
            }

            session.setAttribute("courses", sessionCourses);

            // Clear any previously generated schedules
            session.removeAttribute("generatedSchedules");
            session.removeAttribute("currentScheduleIndex");
            session.removeAttribute("selectedSections");

            response.put("success", true);
            response.put("message", "Course successfully added!");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching courses. Please try again later.");
            e.printStackTrace();
            return ResponseEntity.ok(response);
        }
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

    @PostMapping("/generateSchedules")
    public String generateSchedules(@RequestParam(required = false) String preferences,
                                  HttpSession session, RedirectAttributes redirectAttributes) {
        List<Course> courses = (List<Course>) session.getAttribute("courses");
        if (courses == null || courses.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "No courses added to generate schedules.");
            return "redirect:/";
        }

        try {
            // Parse preferences if provided
            SchedulePreferences schedulePreferences = null;
            if (preferences != null && !preferences.trim().isEmpty()) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode preferencesNode = objectMapper.readTree(preferences);
                    
                    schedulePreferences = new SchedulePreferences();
                    
                    // Parse preferred days
                    JsonNode preferredDaysNode = preferencesNode.path("preferredDays");
                    if (preferredDaysNode.isArray()) {
                        List<String> preferredDays = new ArrayList<>();
                        preferredDaysNode.forEach(day -> preferredDays.add(day.asText()));
                        schedulePreferences.setPreferredDays(preferredDays);
                    }
                    
                    // Parse other preferences
                    schedulePreferences.setTimePreference(preferencesNode.path("timePreference").asText(""));
                    schedulePreferences.setGapPreference(preferencesNode.path("gapPreference").asText("none"));
                    schedulePreferences.setScheduleStyle(preferencesNode.path("scheduleStyle").asText(""));
                    
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error parsing preferences, using defaults", e);
                    schedulePreferences = null;
                }
            }
            
            // Generate schedules with preferences
            List<Map<Course, List<Section>>> schedules = ScheduleBuilder.generateNonConflictingSchedules(courses, 100, schedulePreferences);
            
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

                String message = "Generated " + schedules.size() + " valid schedule(s) with proper commute time and recitation requirements.";
                if (schedulePreferences != null && !schedulePreferences.hasNoPreferences()) {
                    message += " Schedules are ordered by preference match - Schedule 1 is the best match for your preferences!";
                } else {
                    message += " Schedules are ordered by overall quality score.";
                }
                redirectAttributes.addFlashAttribute("successMessage", message);
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
        Integer currentScheduleIndex = (Integer) session.getAttribute(SESSION_CURRENT_SCHEDULE_INDEX);

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
        Integer currentScheduleIndex = (Integer) session.getAttribute(SESSION_CURRENT_SCHEDULE_INDEX);

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
