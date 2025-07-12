package com.schedulebuilder.class_scheduler.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.schedulebuilder.class_scheduler.model.Course;
import com.schedulebuilder.class_scheduler.model.Section;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Service for processing course and section data.
 */
@Service
public class CourseService {
    
    private static final Logger logger = Logger.getLogger(CourseService.class.getName());

    /**
     * Parses courses and their sections from a given JSON node.
     *
     * @param dataNode The JSON node containing course data.
     * @return A list of parsed courses.
     */
    public List<Course> parseCourses(JsonNode dataNode) {
        List<Course> courses = new ArrayList<>();
        
        if (dataNode == null || !dataNode.isArray()) {
            logger.log(Level.WARNING, "Invalid dataNode provided to parseCourses");
            return courses;
        }
        
        for (JsonNode courseNode : dataNode) {
            try {
                Course course = parseSingleCourse(courseNode);
                if (course != null) {
                    courses.add(course);
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error parsing course node", e);
            }
        }
        return courses;
    }
    
    private Course parseSingleCourse(JsonNode courseNode) {
        if (courseNode == null) {
            return null;
        }
        
        try {
            String courseId = courseNode.path("courseId").asText("");
            String courseName = courseNode.path("title").asText("");
            String description = courseNode.path("description").asText("");
            
            if (courseId.isEmpty()) {
                logger.log(Level.WARNING, "Course missing courseId, skipping");
                return null;
            }
            
            Course course = new Course(courseId, courseName, description);

            // Parse sections
            JsonNode sectionsNode = courseNode.path("sections");
            if (sectionsNode.isArray()) {
                for (JsonNode sectionNode : sectionsNode) {
                    try {
                        Section section = parseSection(sectionNode);
                        if (section != null) {
                            course.addSection(section);
                        }
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Error parsing section for course " + courseId, e);
                    }
                }
            }
            
            return course.getSections().isEmpty() ? null : course;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error parsing course", e);
            return null;
        }
    }

    /**
     * Parses a section from a JSON node.
     *
     * @param sectionNode The JSON node containing section data.
     * @return A parsed Section object.
     */
    private Section parseSection(JsonNode sectionNode) {
        if (sectionNode == null) {
            return null;
        }
        
        try {
            String meetingPatterns = sectionNode.path("meetingPatterns").asText("");
            String daysOfTheWeek = "N/A";
            String timeStart = "N/A";
            String timeEnd = "N/A";

            if (!meetingPatterns.isEmpty() && meetingPatterns.contains("|")) {
                String[] parts = meetingPatterns.split("\\|");
                if (parts.length >= 1) {
                    daysOfTheWeek = convertDaysOfWeek(parts[0].trim());
                }
                if (parts.length >= 2) {
                    String[] times = parts[1].trim().split("-");
                    if (times.length == 2) {
                        timeStart = times[0].trim();
                        timeEnd = times[1].trim();
                    }
                }
            }

            // Extract other fields with defaults
            String instructionalFormat = sectionNode.path("instructionalFormat").asText("Unknown");
            String location = sectionNode.path("locations").asText("TBA");
            String courseId = sectionNode.path("courseId").asText("");
            String sectionNumber = sectionNode.path("number").asText("");
            String instructors = sectionNode.path("instructors").asText("TBA");
            String deliveryMode = sectionNode.path("deliveryMode").asText("In-Person");
            int openSeats = sectionNode.path("openSeats").asInt(0);
            
            // Validate required fields
            if (courseId.isEmpty() || sectionNumber.isEmpty()) {
                logger.log(Level.WARNING, "Section missing required fields: courseId=" + courseId + ", number=" + sectionNumber);
                return null;
            }

            return new Section(
                    daysOfTheWeek,
                    openSeats,
                    instructors,
                    courseId,
                    timeStart,
                    timeEnd,
                    sectionNumber,
                    instructionalFormat,
                    location,
                    deliveryMode
            );
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error parsing section", e);
            return null;
        }
    }

    /**
     * Converts day abbreviations to full day names
     * @param daysOfTheWeek Abbreviated day string (e.g., "MWF")
     * @return Full day names separated by commas (e.g., "Monday,Wednesday,Friday")
     */
    private String convertDaysOfWeek(String daysOfTheWeek) {
        if (daysOfTheWeek == null || daysOfTheWeek.trim().isEmpty()) {
            return "Online";
        }
        
        try {
            String converted = daysOfTheWeek.trim()
                    .replace("M", "Monday,")
                    .replace("T", "Tuesday,")
                    .replace("W", "Wednesday,")
                    .replace("R", "Thursday,")
                    .replace("F", "Friday,")
                    .replace("S", "Saturday,")
                    .replace("U", "Sunday,")
                    .replaceAll(",+$", ""); // Remove trailing comma
            
            return converted.isEmpty() ? "Online" : converted;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error converting days of week: " + daysOfTheWeek, e);
            return "Online";
        }
    }
}
