package com.schedulebuilder.class_scheduler.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schedulebuilder.class_scheduler.model.Course;
import com.schedulebuilder.class_scheduler.model.ScheduleBuilder;
import com.schedulebuilder.class_scheduler.model.Section;
import com.schedulebuilder.class_scheduler.model.SectionType;
import com.schedulebuilder.class_scheduler.model.AcademicPeriod;
import com.schedulebuilder.class_scheduler.model.SchedulePreferences;
import com.schedulebuilder.class_scheduler.model.CourseSearchRequest;
import com.schedulebuilder.class_scheduler.service.ApiService;
import com.schedulebuilder.class_scheduler.service.CourseService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

@Controller
public class HomeController {
    
    private static final Logger logger = Logger.getLogger(HomeController.class.getName());
    private static final String DEFAULT_ACADEMIC_PERIOD = "ACADEMIC_PERIOD-2026Spring";
    private static final String SESSION_COURSES = "courses";
    private static final String SESSION_GENERATED_SCHEDULES = "generatedSchedules";
    private static final String SESSION_CURRENT_SCHEDULE_INDEX = "currentScheduleIndex";
    private static final String SESSION_SELECTED_SECTIONS = "selectedSections";
    private static final String SESSION_CURRENT_ACADEMIC_PERIOD = "currentAcademicPeriod";
    private static final String SESSION_ONLINE_SECTIONS = "onlineSections";
    private static final String SESSION_TBD_SECTIONS = "tbdSections";

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
        session.removeAttribute(SESSION_ONLINE_SECTIONS);
        session.removeAttribute(SESSION_TBD_SECTIONS);
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
    public ResponseEntity<Map<String, Object>> addCourse(@RequestParam(required = false) String courseSubject,
                                                        @RequestParam(required = false) String courseNumber,
                                                        @RequestParam(required = false) String academicPeriodId,
                                                        HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        // Validate input
        if (courseSubject == null || courseSubject.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Course subject is required");
            return ResponseEntity.ok(response);
        }

        if (courseNumber == null || courseNumber.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Course number is required");
            return ResponseEntity.ok(response);
        }

        logger.info("=== Add Course Request ===");
        logger.info("courseSubject: " + courseSubject);
        logger.info("courseNumber: " + courseNumber);
        logger.info("academicPeriodId: " + academicPeriodId);

        try {
            // Use academic period from session if not provided
            if (academicPeriodId == null || academicPeriodId.isEmpty()) {
                academicPeriodId = (String) session.getAttribute("currentAcademicPeriod");
                if (academicPeriodId == null) {
                    academicPeriodId = "ACADEMIC_PERIOD-2025Fall";
                }
            }

            // Update session with the current academic period
            session.setAttribute("currentAcademicPeriod", academicPeriodId);

            // Fetch and parse courses for the given courseSubject and courseNumber
            String coursesJson = apiService.fetchCourses(academicPeriodId, courseSubject, courseNumber);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(coursesJson);
            JsonNode dataNode = rootNode.path("data");

            if (!dataNode.isArray() || dataNode.size() == 0) {
                response.put("success", false);
                response.put("message", "Course does not exist.");
                return ResponseEntity.ok(response);
            }

            // Use CourseService to parse courses
            List<Course> allCourses = courseService.parseCourses(dataNode);
            
            // Smart course number matching
            List<Course> newCourses = new ArrayList<>();

            for (Course course : allCourses) {
                String courseName = course.getCourseId();
                String extractedNumber = courseName.substring(courseName.lastIndexOf(' ') + 1);

                // Check for exact match first (e.g., "2500H" matches "2500H")
                if (courseNumber.equalsIgnoreCase(extractedNumber)) {
                    newCourses.add(course);
                }
                // Check if user entered abbreviated number (e.g., "250" should match only "2500", not "2500H")
                else if (courseNumber.length() == 3 && extractedNumber.length() >= 4) {
                    String expandedCourseNumber = courseNumber + "0"; // "250" becomes "2500"
                    if (extractedNumber.equals(expandedCourseNumber)) { // Exact match only, not startsWith
                        newCourses.add(course);
                    }
                }
                // Check if user entered 2-digit abbreviated number (e.g., "85" should match only "850", not "850H")
                else if (courseNumber.length() == 2 && extractedNumber.length() >= 3) {
                    String expandedCourseNumber = courseNumber + "0"; // "85" becomes "850"
                    if (extractedNumber.equals(expandedCourseNumber)) { // Exact match only, not startsWith
                        newCourses.add(course);
                    }
                }
            }
            
            // Check if no valid courses were found after smart matching
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

            // Auto-generate schedules if there are courses
            try {
                logger.info("Auto-generating schedules for " + sessionCourses.size() + " courses");

                // Separate sections by type: online, TBD, and in-person
                // Only in-person sections participate in schedule generation
                List<Section> onlineSections = new ArrayList<>();
                List<Section> tbdSections = new ArrayList<>();
                List<Course> inPersonCourses = new ArrayList<>();

                for (Course course : sessionCourses) {
                    List<Section> inPersonSections = new ArrayList<>();
                    for (Section section : course.getSections()) {
                        if (section.getSectionType() == SectionType.ONLINE) {
                            onlineSections.add(section);
                        } else if (section.getSectionType() == SectionType.TBD) {
                            tbdSections.add(section);
                        } else if (section.isSchedulable()) {
                            inPersonSections.add(section);
                        }
                    }

                    if (!inPersonSections.isEmpty()) {
                        Course inPersonCourse = new Course(course.getCourseId(), course.getCourseName(), course.getDescription());
                        for (Section section : inPersonSections) {
                            inPersonCourse.addSection(section);
                        }
                        inPersonCourses.add(inPersonCourse);
                    }
                }

                logger.info("Separated " + onlineSections.size() + " online sections and " + tbdSections.size() + " TBD sections from auto-generation");

                // Store online and TBD sections in session so we can add them back when navigating
                session.setAttribute(SESSION_ONLINE_SECTIONS, onlineSections);
                session.setAttribute(SESSION_TBD_SECTIONS, tbdSections);

                // If there are ONLY non-schedulable courses (online/TBD), just display them
                if (inPersonCourses.isEmpty() && (!onlineSections.isEmpty() || !tbdSections.isEmpty())) {
                    logger.info("All courses are non-schedulable (online/TBD) - displaying without schedule generation");
                    List<Section> allNonSchedulable = new ArrayList<>();
                    allNonSchedulable.addAll(onlineSections);
                    allNonSchedulable.addAll(tbdSections);
                    session.setAttribute("selectedSections", allNonSchedulable);
                    session.removeAttribute("generatedSchedules");
                    session.removeAttribute("currentScheduleIndex");
                } else if (!inPersonCourses.isEmpty()) {
                    // Generate schedules for in-person courses
                    List<Map<Course, List<Section>>> schedules = ScheduleBuilder.generateNonConflictingSchedules(inPersonCourses, 100, null);

                    if (!schedules.isEmpty()) {
                        session.setAttribute("generatedSchedules", schedules);
                        session.setAttribute("currentScheduleIndex", 0);

                        // Flatten sections for display (handle multiple sections per course)
                        Map<Course, List<Section>> firstSchedule = schedules.get(0);
                        List<Section> allSections = new ArrayList<>();
                        for (Map.Entry<Course, List<Section>> entry : firstSchedule.entrySet()) {
                            allSections.addAll(entry.getValue());
                        }

                        // Add online and TBD sections back for display
                        allSections.addAll(onlineSections);
                        allSections.addAll(tbdSections);

                        session.setAttribute("selectedSections", allSections);
                        logger.info("Auto-generated " + schedules.size() + " schedules successfully");
                    } else {
                        logger.info("No valid schedules could be generated for in-person courses");
                        // Keep online and TBD sections even if in-person scheduling fails
                        if (!onlineSections.isEmpty() || !tbdSections.isEmpty()) {
                            List<Section> nonSchedulableSections = new ArrayList<>();
                            nonSchedulableSections.addAll(onlineSections);
                            nonSchedulableSections.addAll(tbdSections);
                            session.setAttribute("selectedSections", nonSchedulableSections);
                            logger.info("Displaying " + onlineSections.size() + " online sections and " + tbdSections.size() + " TBD sections only");
                        }
                        session.removeAttribute("generatedSchedules");
                        session.removeAttribute("currentScheduleIndex");
                    }
                } else {
                    // No courses at all
                    session.removeAttribute("generatedSchedules");
                    session.removeAttribute("currentScheduleIndex");
                    session.removeAttribute("selectedSections");
                    session.removeAttribute(SESSION_ONLINE_SECTIONS);
                    session.removeAttribute(SESSION_TBD_SECTIONS);
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error auto-generating schedules, continuing without schedules", e);
                // Don't clear online sections on error - keep them visible
                session.removeAttribute("generatedSchedules");
                session.removeAttribute("currentScheduleIndex");
            }

            response.put("success", true);
            response.put("message", "Course successfully added!");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error adding course", e);
            response.put("success", false);
            response.put("message", "Error fetching courses: " + e.getMessage());
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
            session.removeAttribute(SESSION_ONLINE_SECTIONS);
            session.removeAttribute(SESSION_TBD_SECTIONS);

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
                                  @RequestParam(required = false) String instructorPreferences,
                                  @RequestParam(required = false) String selectedSections,
                                  HttpSession session, RedirectAttributes redirectAttributes) {
        List<Course> courses = (List<Course>) session.getAttribute("courses");
        if (courses == null || courses.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "No courses added to generate schedules.");
            return "redirect:/";
        }

        try {
            logger.info("=== Generate Schedules Request ===");
            if (preferences != null && !preferences.trim().isEmpty()) {
                logger.info("Schedule Preferences: " + preferences);
            }
            if (instructorPreferences != null && !instructorPreferences.trim().isEmpty()) {
                logger.info("Instructor Preferences: " + instructorPreferences);
            }
            if (selectedSections != null && !selectedSections.trim().isEmpty()) {
                logger.info("Selected Sections: " + selectedSections);
            }

            // Apply selected section filtering first
            List<Course> filteredCourses = courses;
            if (selectedSections != null && !selectedSections.trim().isEmpty()) {
                filteredCourses = applySelectedSections(courses, selectedSections);
                logger.info("After section filtering: " + filteredCourses.size() + " courses");
            }

            // Apply instructor preferences to filter course sections
            filteredCourses = applyInstructorPreferences(filteredCourses, instructorPreferences);
            logger.info("After instructor filtering: " + filteredCourses.size() + " courses");

            // Separate sections by type: online, TBD, and in-person
            // Only in-person sections participate in schedule generation
            List<Section> onlineSections = new ArrayList<>();
            List<Section> tbdSections = new ArrayList<>();
            List<Course> inPersonCourses = new ArrayList<>();

            for (Course course : filteredCourses) {
                List<Section> inPersonSections = new ArrayList<>();
                for (Section section : course.getSections()) {
                    if (section.getSectionType() == SectionType.ONLINE) {
                        // Collect online sections separately
                        onlineSections.add(section);
                    } else if (section.getSectionType() == SectionType.TBD) {
                        // Collect TBD sections separately
                        tbdSections.add(section);
                    } else if (section.isSchedulable()) {
                        // Keep schedulable in-person sections for schedule generation
                        inPersonSections.add(section);
                    }
                }

                // Only add course if it has in-person sections
                if (!inPersonSections.isEmpty()) {
                    Course inPersonCourse = new Course(course.getCourseId(), course.getCourseName(), course.getDescription());
                    for (Section section : inPersonSections) {
                        inPersonCourse.addSection(section);
                    }
                    inPersonCourses.add(inPersonCourse);
                }
            }

            logger.info("Separated " + onlineSections.size() + " online sections and " + tbdSections.size() + " TBD sections from schedule generation");
            logger.info("Using " + inPersonCourses.size() + " in-person courses for schedule generation");

            // Store online and TBD sections in session so we can add them back when navigating
            session.setAttribute(SESSION_ONLINE_SECTIONS, onlineSections);
            session.setAttribute(SESSION_TBD_SECTIONS, tbdSections);

            // Use only in-person courses for schedule generation
            filteredCourses = inPersonCourses;

            // Parse preferences if provided
            SchedulePreferences schedulePreferences = null;
            boolean uniqueSchedulesOnly = false; // Default to false (show all schedules)

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
                    schedulePreferences.setGapPreference(preferencesNode.path("gapPreference").asText(""));
                    schedulePreferences.setScheduleStyle(preferencesNode.path("scheduleStyle").asText(""));

                    // Parse uniqueSchedulesOnly preference
                    JsonNode uniqueSchedulesNode = preferencesNode.path("uniqueSchedulesOnly");
                    if (!uniqueSchedulesNode.isMissingNode()) {
                        uniqueSchedulesOnly = uniqueSchedulesNode.asBoolean(false);
                    }

                    logger.info("Parsed schedule preferences: " + schedulePreferences + ", uniqueSchedulesOnly: " + uniqueSchedulesOnly);
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error parsing preferences, using defaults", e);
                    schedulePreferences = null;
                }
            }

            // Generate schedules with preferences using filtered courses
            // Note: The filtered courses already prioritize preferred instructors, so we don't filter out too many options

            // If there are ONLY non-schedulable courses (online/TBD), just display them
            if (filteredCourses.isEmpty() && (!onlineSections.isEmpty() || !tbdSections.isEmpty())) {
                logger.info("All courses are non-schedulable (online/TBD) - displaying without schedule generation");
                List<Section> allNonSchedulable = new ArrayList<>();
                allNonSchedulable.addAll(onlineSections);
                allNonSchedulable.addAll(tbdSections);
                session.setAttribute("selectedSections", allNonSchedulable);
                session.removeAttribute("generatedSchedules");
                session.removeAttribute("currentScheduleIndex");
                String message = "Displaying ";
                if (!onlineSections.isEmpty()) {
                    message += onlineSections.size() + " online course(s)";
                }
                if (!tbdSections.isEmpty()) {
                    if (!onlineSections.isEmpty()) message += " and ";
                    message += tbdSections.size() + " TBD course(s)";
                }
                message += ". These courses don't require scheduling.";
                redirectAttributes.addFlashAttribute("successMessage", message);
            } else if (!filteredCourses.isEmpty()) {
                // Generate schedules for in-person courses
                List<Map<Course, List<Section>>> schedules = ScheduleBuilder.generateNonConflictingSchedules(filteredCourses, 100, schedulePreferences, uniqueSchedulesOnly);

                if (schedules.isEmpty()) {
                    // Keep online and TBD sections even if in-person scheduling fails
                    if (!onlineSections.isEmpty() || !tbdSections.isEmpty()) {
                        List<Section> nonSchedulableSections = new ArrayList<>();
                        nonSchedulableSections.addAll(onlineSections);
                        nonSchedulableSections.addAll(tbdSections);
                        session.setAttribute("selectedSections", nonSchedulableSections);
                        logger.info("Displaying " + onlineSections.size() + " online sections and " + tbdSections.size() + " TBD sections only (no valid in-person schedules)");
                        String message = "No valid schedules found for in-person courses. Displaying ";
                        if (!onlineSections.isEmpty()) {
                            message += onlineSections.size() + " online course(s)";
                        }
                        if (!tbdSections.isEmpty()) {
                            if (!onlineSections.isEmpty()) message += " and ";
                            message += tbdSections.size() + " TBD course(s)";
                        }
                        message += ".";
                        redirectAttributes.addFlashAttribute("errorMessage", message);
                    } else {
                        redirectAttributes.addFlashAttribute("errorMessage",
                            "No valid schedules found. This may be due to conflicting times, insufficient commute time, or missing required recitations/labs.");
                    }
                    session.removeAttribute("generatedSchedules");
                    session.removeAttribute("currentScheduleIndex");
                } else {
                    session.setAttribute("generatedSchedules", schedules);
                    session.setAttribute("currentScheduleIndex", 0);

                    // Flatten sections for display (handle multiple sections per course)
                    Map<Course, List<Section>> firstSchedule = schedules.get(0);
                    List<Section> allSections = new ArrayList<>();
                    for (Map.Entry<Course, List<Section>> entry : firstSchedule.entrySet()) {
                        allSections.addAll(entry.getValue());
                    }

                    // Add online and TBD sections back for display (they don't affect scheduling)
                    allSections.addAll(onlineSections);
                    allSections.addAll(tbdSections);
                    logger.info("Added " + onlineSections.size() + " online sections and " + tbdSections.size() + " TBD sections to display");

                    session.setAttribute("selectedSections", allSections);

                    String message = "Generated " + schedules.size() + " valid schedule(s) with proper commute time and recitation requirements.";
                    if (schedulePreferences != null && !schedulePreferences.hasNoPreferences()) {
                        message += " Schedules are ordered by preference match - Schedule 1 is the best match for your preferences!";
                    } else {
                        message += " Schedules are ordered by overall quality score.";
                    }
                    if (!onlineSections.isEmpty() || !tbdSections.isEmpty()) {
                        message += " (+ ";
                        if (!onlineSections.isEmpty()) {
                            message += onlineSections.size() + " online";
                        }
                        if (!tbdSections.isEmpty()) {
                            if (!onlineSections.isEmpty()) message += " and ";
                            message += tbdSections.size() + " TBD";
                        }
                        message += " course(s))";
                    }
                    redirectAttributes.addFlashAttribute("successMessage", message);
                }
            } else {
                // No courses at all
                redirectAttributes.addFlashAttribute("errorMessage", "No courses to schedule.");
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

        // Add online and TBD sections back for display
        @SuppressWarnings("unchecked")
        List<Section> onlineSections = (List<Section>) session.getAttribute(SESSION_ONLINE_SECTIONS);
        if (onlineSections != null) {
            allSections.addAll(onlineSections);
        }

        @SuppressWarnings("unchecked")
        List<Section> tbdSections = (List<Section>) session.getAttribute(SESSION_TBD_SECTIONS);
        if (tbdSections != null) {
            allSections.addAll(tbdSections);
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

        // Add online and TBD sections back for display
        @SuppressWarnings("unchecked")
        List<Section> onlineSections = (List<Section>) session.getAttribute(SESSION_ONLINE_SECTIONS);
        if (onlineSections != null) {
            allSections.addAll(onlineSections);
        }

        @SuppressWarnings("unchecked")
        List<Section> tbdSections = (List<Section>) session.getAttribute(SESSION_TBD_SECTIONS);
        if (tbdSections != null) {
            allSections.addAll(tbdSections);
        }

        session.setAttribute("selectedSections", allSections);

        redirectAttributes.addFlashAttribute("successMessage", "Switched to the previous schedule.");
        return "redirect:/";
    }

    @GetMapping("/api/academic-periods")
    @ResponseBody
    public ResponseEntity<String> getAcademicPeriodsApi() {
        try {
            List<AcademicPeriod> periods = apiService.fetchAcademicPeriods();
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> response = new HashMap<>();

            List<Map<String, Object>> periodsList = new ArrayList<>();
            for (AcademicPeriod period : periods) {
                Map<String, Object> periodMap = new HashMap<>();
                periodMap.put("id", period.getId());
                periodMap.put("name", period.getDisplayName());
                periodMap.put("isCurrent", period.isActive());
                periodMap.put("startDate", period.getStartDate());
                periodsList.add(periodMap);
            }

            response.put("data", periodsList);
            String json = objectMapper.writeValueAsString(response);

            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(json);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error fetching academic periods", e);
            return ResponseEntity.status(500)
                    .body("{\"error\": \"Failed to fetch academic periods\"}");
        }
    }

    @GetMapping("/api/departments")
    @ResponseBody
    public ResponseEntity<String> getDepartmentsApi(@RequestParam String academicPeriod) {
        try {
            String departmentsJson = apiService.fetchDepartments(academicPeriod);
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(departmentsJson);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error fetching departments", e);
            return ResponseEntity.status(500)
                    .body("{\"error\": \"Failed to fetch departments\"}");
        }
    }

    @PostMapping("/removeCourse")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeCourse(@RequestParam String courseId,
                               HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            @SuppressWarnings("unchecked")
            List<Course> courses = (List<Course>) session.getAttribute("courses");

            if (courses != null) {
                boolean removed = courses.removeIf(c -> c.getCourseId().equals(courseId));

                if (removed) {
                    session.setAttribute("courses", courses);

                    // Clear ALL schedule-related attributes
                    session.removeAttribute("selectedSections");
                    session.removeAttribute("generatedSchedules");
                    session.removeAttribute("currentScheduleIndex");
                    session.removeAttribute(SESSION_ONLINE_SECTIONS);
                    session.removeAttribute(SESSION_TBD_SECTIONS);

                    response.put("success", true);
                    response.put("message", "Course removed successfully");
                } else {
                    response.put("success", false);
                    response.put("message", "Course not found");
                }
            } else {
                response.put("success", false);
                response.put("message", "No courses in session");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error removing course: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * Apply selected section filtering to courses
     * Only include sections that are explicitly selected for each course
     */
    private List<Course> applySelectedSections(List<Course> courses, String selectedSectionsJson) {
        if (selectedSectionsJson == null || selectedSectionsJson.trim().isEmpty()) {
            return courses;
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode selectedSectionsNode = objectMapper.readTree(selectedSectionsJson);

            List<Course> filteredCourses = new ArrayList<>();

            for (Course course : courses) {
                JsonNode courseSections = selectedSectionsNode.path(course.getCourseId());

                if (courseSections.isArray() && courseSections.size() > 0) {
                    // Course has selected sections - filter to only include those
                    // Section IDs are in format "courseId-sectionNumber"
                    Set<String> selectedSectionNumbers = new HashSet<>();
                    courseSections.forEach(node -> {
                        String sectionId = node.asText();
                        // Extract section number from "courseId-sectionNumber" format
                        int lastDash = sectionId.lastIndexOf('-');
                        if (lastDash > 0) {
                            selectedSectionNumbers.add(sectionId.substring(lastDash + 1));
                        }
                    });

                    List<Section> filteredSections = course.getSections().stream()
                        .filter(section -> selectedSectionNumbers.contains(section.getSectionNumber()))
                        .collect(java.util.stream.Collectors.toList());

                    if (!filteredSections.isEmpty()) {
                        Course filteredCourse = new Course(
                            course.getCourseId(),
                            course.getCourseName(),
                            course.getDescription(),
                            filteredSections
                        );
                        filteredCourses.add(filteredCourse);
                        logger.info("Filtered " + course.getCourseId() + " to " + filteredSections.size() + " selected sections");
                    }
                } else {
                    // No filtering for this course - include all sections
                    filteredCourses.add(course);
                }
            }

            return filteredCourses;

        } catch (Exception e) {
            logger.log(Level.WARNING, "Error parsing selected sections, using all courses", e);
            return courses;
        }
    }

    /**
     * Apply instructor preferences to filter course sections
     * Focus on Lecture/Studio instructors but apply filtering to other formats when possible
     */
    private List<Course> applyInstructorPreferences(List<Course> courses, String instructorPreferencesJson) {
        if (instructorPreferencesJson == null || instructorPreferencesJson.trim().isEmpty()) {
            return courses; // No preferences, return all courses unchanged
        }
        
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode preferencesNode = objectMapper.readTree(instructorPreferencesJson);
            
            List<Course> filteredCourses = new ArrayList<>();
            
            for (Course course : courses) {
                JsonNode coursePrefs = preferencesNode.path(course.getCourseId());
                
                if (coursePrefs.isArray() && coursePrefs.size() > 0) {
                    // Course has instructor preferences - filter sections
                    Set<String> preferredInstructors = new HashSet<>();
                    coursePrefs.forEach(node -> preferredInstructors.add(node.asText()));
                    
                    List<Section> filteredSections = new ArrayList<>();
                    
                    for (Section section : course.getSections()) {
                        // Apply filtering logic based on format priority
                        String format = section.getInstructionalFormat();
                        String instructor = section.getInstructor();
                        
                        if (format != null && instructor != null && !instructor.equals("N/A")) {
                            // Always include preferred instructors regardless of format
                            if (preferredInstructors.contains(instructor)) {
                                filteredSections.add(section);
                            }
                            // For primary formats (Lecture/Studio) - include non-preferred only if no preferred options exist
                            else if (format.equalsIgnoreCase("Lecture") || format.equalsIgnoreCase("Studio")) {
                                // Check if any preferred instructors teach this specific primary format
                                boolean hasPreferredForPrimaryFormat = course.getSections().stream()
                                    .anyMatch(s -> s.getInstructionalFormat() != null && 
                                                 s.getInstructionalFormat().equals(format) &&
                                                 s.getInstructor() != null &&
                                                 preferredInstructors.contains(s.getInstructor()));
                                
                                // If no preferred instructors teach this primary format, include all sections of this format
                                if (!hasPreferredForPrimaryFormat) {
                                    filteredSections.add(section);
                                }
                            }
                            // Other formats - include non-preferred only if no preferred options exist for this format
                            else {
                                // Check if any preferred instructors teach this format
                                boolean hasPreferredForThisFormat = course.getSections().stream()
                                    .anyMatch(s -> s.getInstructionalFormat() != null && 
                                                 s.getInstructionalFormat().equals(format) &&
                                                 s.getInstructor() != null &&
                                                 preferredInstructors.contains(s.getInstructor()));
                                
                                // If no preferred instructors teach this format, include all sections of this format
                                if (!hasPreferredForThisFormat) {
                                    filteredSections.add(section);
                                }
                            }
                        } else {
                            // No instructor info or N/A - always include
                            filteredSections.add(section);
                        }
                    }
                    
                    // Only add course if it has sections after filtering
                    if (!filteredSections.isEmpty()) {
                        Course filteredCourse = new Course(course.getCourseId(), course.getCourseName(), course.getDescription(), filteredSections);
                        filteredCourses.add(filteredCourse);
                    }
                } else {
                    // No preferences for this course - include all sections
                    filteredCourses.add(course);
                }
            }
            
            return filteredCourses;
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error parsing instructor preferences, using all courses", e);
            return courses; // Fallback to original courses if parsing fails
        }
    }

}
