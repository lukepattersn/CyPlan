package com.schedulebuilder.class_scheduler.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.schedulebuilder.class_scheduler.model.Course;
import com.schedulebuilder.class_scheduler.model.Section;
import com.schedulebuilder.class_scheduler.model.SectionType;
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
            // Try "courseNumber" first, then fall back to "number"
            String courseId = courseNode.path("courseNumber").asText("");
            if (courseId.isEmpty()) {
                courseId = courseNode.path("number").asText("");
            }
            String courseName = courseNode.path("title").asText("");
            String description = courseNode.path("description").asText("");

            if (courseId.isEmpty()) {
                logger.log(Level.WARNING, "Course missing courseNumber/number, skipping");
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
            // Try courseNumber first, then fall back to courseId
            String courseId = sectionNode.path("courseNumber").asText("");
            if (courseId.isEmpty()) {
                courseId = sectionNode.path("courseId").asText("");
            }
            String sectionNumber = sectionNode.path("number").asText("");
            String instructors = sectionNode.path("instructors").asText("TBA");
            String deliveryMode = sectionNode.path("deliveryMode").asText("In-Person");
            int openSeats = sectionNode.path("openSeats").asInt(0);
            String credits = sectionNode.path("credits").asText("0");

            // Validate required fields
            if (courseId.isEmpty() || sectionNumber.isEmpty()) {
                logger.log(Level.WARNING, "Section missing required fields: courseId=" + courseId + ", number=" + sectionNumber);
                return null;
            }

            // Handle sections with missing time/day information
            boolean hasValidDays = daysOfTheWeek != null && !daysOfTheWeek.equals("N/A") && !daysOfTheWeek.isEmpty();
            boolean hasValidTime = timeStart != null && !timeStart.equals("N/A") && timeEnd != null && !timeEnd.equals("N/A");
            boolean isOnline = deliveryMode != null && deliveryMode.equalsIgnoreCase("Online");

            // Determine section type
            SectionType sectionType;

            // If online course, set appropriate values
            if (isOnline) {
                sectionType = SectionType.ONLINE;
                if (!hasValidDays) {
                    daysOfTheWeek = "Online";
                    logger.log(Level.INFO, "Section " + courseId + " Section " + sectionNumber + " is online - setting days to Online");
                }
                if (!hasValidTime) {
                    timeStart = "Online";
                    timeEnd = "Online";
                    logger.log(Level.INFO, "Section " + courseId + " Section " + sectionNumber + " is online - setting times to Online");
                }
            } else if (!hasValidDays || !hasValidTime) {
                // For in-person courses with missing data, set to TBD
                sectionType = SectionType.TBD;
                if (!hasValidDays) {
                    daysOfTheWeek = "TBD";
                    logger.log(Level.INFO, "Section " + courseId + " Section " + sectionNumber + " has no meeting days - setting to TBD");
                }

                if (!hasValidTime) {
                    timeStart = "TBD";
                    timeEnd = "TBD";
                    logger.log(Level.INFO, "Section " + courseId + " Section " + sectionNumber + " has no meeting times - setting to TBD");
                }
            } else {
                // Valid in-person section with complete schedule information
                sectionType = SectionType.IN_PERSON;
            }

            Section section = new Section(
                    daysOfTheWeek,
                    openSeats,
                    instructors,
                    courseId,
                    timeStart,
                    timeEnd,
                    sectionNumber,
                    instructionalFormat,
                    location,
                    deliveryMode,
                    credits
            );

            // Set the section type
            section.setSectionType(sectionType);

            return section;
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
                    .replace("M", "Mon,")
                    .replace("T", "Tue,")
                    .replace("W", "Wed,")
                    .replace("R", "Thu,")
                    .replace("F", "Fri,")
                    .replace("S", "Sat,")
                    .replace("U", "Sun,")
                    .replaceAll(",+$", ""); // Remove trailing comma
            
            return converted.isEmpty() ? "Online" : converted;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error converting days of week: " + daysOfTheWeek, e);
            return "Online";
        }
    }
}
