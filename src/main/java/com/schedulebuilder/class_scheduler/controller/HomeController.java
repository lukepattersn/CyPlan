package com.schedulebuilder.class_scheduler.controller;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schedulebuilder.class_scheduler.model.Course;
import com.schedulebuilder.class_scheduler.model.Pair;
import com.schedulebuilder.class_scheduler.model.ScheduleBuilder;
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

    // Add lists to store course and section indices
    private List<Integer> courseIndices = new ArrayList<>();
    private List<Integer> sectionIndices = new ArrayList<>();

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

            // Add the course and section indices to the model (for display)
            model.addAttribute("courseIndices", courseIndices);
            model.addAttribute("sectionIndices", sectionIndices);

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

                // Get the schedule from ScheduleBuilder (this is a string)
                String schedule = ScheduleBuilder.buildClosestNonOverlappingSchedule(courses);

                // Parse the schedule string to extract course and section indices
                parseScheduleString(schedule);

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

    private void parseScheduleString(String schedule) {
        // Regular expression to match the course and section index
        Pattern pattern = Pattern.compile("Course (\\d+): SectionIndex (\\d+)");
        Matcher matcher = pattern.matcher(schedule);

        while (matcher.find()) {
            // Extract the course index and section index
            int courseIndex = Integer.parseInt(matcher.group(1));
            int sectionIndex = Integer.parseInt(matcher.group(2));

            // Store the course and section indices in the lists
            courseIndices.add(courseIndex);
            sectionIndices.add(sectionIndex);
        }
    }

    private static void parseScheduleString(String schedule, List<Integer> courseIndices, List<Integer> sectionIndices) {
        // Regular expression to match the course and section index
        Pattern pattern = Pattern.compile("Course (\\d+): SectionIndex (\\d+)");
        Matcher matcher = pattern.matcher(schedule);

        while (matcher.find()) {
            // Extract the course index and section index
            int courseIndex = Integer.parseInt(matcher.group(1));
            int sectionIndex = Integer.parseInt(matcher.group(2));

            // Store the course and section indices in the lists
            courseIndices.add(courseIndex);
            sectionIndices.add(sectionIndex);
        }
    }

    public List<Integer> getCourseIndices() {
        return new ArrayList<>(courseIndices); // Return a copy to preserve encapsulation
    }

    public List<Integer> getSectionIndices() {
        return new ArrayList<>(sectionIndices); // Return a copy to preserve encapsulation
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

//    public static void main(String[] args) {
//        // Example courses and sections
//        Course course1 = new Course("CS101", "Intro to Computer Science", "AHH");
//        course1.addSection(new Section("MWF", 30, "Dr. Smith", "CS101", "9:00 AM", "10:00 AM", "A"));
//        course1.addSection(new Section("MWF", 25, "Dr. Jones", "CS101", "10:30 AM", "11:30 AM", "B"));
//
//        Course course2 = new Course("MATH101", "Calculus I", "AHHH");
//        course2.addSection(new Section("TR", 20, "Dr. Taylor", "MATH101", "12:00 PM", "1:30 PM", "C"));
//        course2.addSection(new Section("TR", 18, "Dr. Brown", "MATH101", "11:00 AM", "12:30 PM", "D"));
//
//        Course course3 = new Course("ENG101", "English Literature", "AHH");
//        course3.addSection(new Section("MW", 40, "Dr. Green", "ENG101", "2:00 PM", "3:30 PM", "E"));
//        course3.addSection(new Section("MW", 35, "Dr. White", "ENG101", "1:00 PM", "2:30 PM", "F"));
//
//        List<Course> courses = new ArrayList<>();
//        courses.add(course1);
//        courses.add(course2);
//        courses.add(course3);
//
//        // Call the schedule builder method and get the generated schedule
//        String schedule = ScheduleBuilder.buildClosestNonOverlappingSchedule(courses);
//
//        // Parse the schedule string to extract the course and section indices
//        List<Integer> courseIndices = new ArrayList<>();
//        List<Integer> sectionIndices = new ArrayList<>();
//        parseScheduleString(schedule, courseIndices, sectionIndices);
//
//        // Print out the parsed indices
//        System.out.println("Generated Schedule (Course Index: SectionIndex):");
//        for (int i = 0; i < courseIndices.size(); i++) {
//            System.out.println("Course " + courseIndices.get(i) + ": SectionIndex " + sectionIndices.get(i));
//        }
//    }
}