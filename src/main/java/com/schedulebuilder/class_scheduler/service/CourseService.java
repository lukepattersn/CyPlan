package com.schedulebuilder.class_scheduler.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.schedulebuilder.class_scheduler.model.Course;
import com.schedulebuilder.class_scheduler.model.Section;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for processing course and section data.
 */
@Service
public class CourseService {

    /**
     * Parses courses and their sections from a given JSON node.
     *
     * @param dataNode The JSON node containing course data.
     * @return A list of parsed courses.
     */
    public List<Course> parseCourses(JsonNode dataNode) {
        List<Course> courses = new ArrayList<>();
        for (JsonNode courseNode : dataNode) {
            // Parse course fields
            String courseId = courseNode.path("courseId").asText();
            String courseName = courseNode.path("courseName").asText();
            String description = courseNode.path("description").asText();
            Course course = new Course(courseId, courseName, description);

            // Parse sections
            JsonNode sectionsNode = courseNode.path("sections");
            if (sectionsNode.isArray()) {
                for (JsonNode sectionNode : sectionsNode) {
                    Section section = parseSection(sectionNode);
                    course.addSection(section);
                }
            }
            courses.add(course);
        }
        return courses;
    }

    /**
     * Parses a section from a JSON node.
     *
     * @param sectionNode The JSON node containing section data.
     * @return A parsed Section object.
     */
    private Section parseSection(JsonNode sectionNode) {
        String meetingPatterns = sectionNode.path("meetingPatterns").asText(); // Example: "MWF | 1:10 PM - 2:00 PM"
        String daysOfTheWeek = "N/A";
        String timeStart = "N/A";
        String timeEnd = "N/A";

        if (meetingPatterns.contains("|")) {
            String[] parts = meetingPatterns.split("\\|");
            daysOfTheWeek = parts[0].trim(); // Extracts "MWF"
            if (parts.length > 1) {
                String[] times = parts[1].trim().split("-");
                if (times.length == 2) {
                    timeStart = times[0].trim(); // Extracts "1:10 PM"
                    timeEnd = times[1].trim();   // Extracts "2:00 PM"
                }
            }
        }

        return new Section(
                daysOfTheWeek,
                sectionNode.path("openSeats").asInt(),
                sectionNode.path("instructor").asText(),
                sectionNode.path("courseId").asText(),
                timeStart,
                timeEnd,
                sectionNode.path("number").asText()
        );
    }

}
