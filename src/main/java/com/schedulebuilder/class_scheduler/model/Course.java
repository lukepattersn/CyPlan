package com.schedulebuilder.class_scheduler.model;

import java.util.ArrayList;
import java.util.List;

public class Course {
    private String courseId;
    private String courseName;
    private String description;
    private List<Section> sections;

    // Constructor without sections
    public Course(String courseId, String courseName, String description) {
        this.courseId = validateAndTrim(courseId, "Course ID");
        this.courseName = validateAndTrim(courseName, "Course Name");
        this.description = description != null ? description.trim() : "";
        this.sections = new ArrayList<>();
    }

    // Full constructor with sections
    public Course(String courseId, String courseName, String description, List<Section> sections) {
        this.courseId = validateAndTrim(courseId, "Course ID");
        this.courseName = validateAndTrim(courseName, "Course Name");
        this.description = description != null ? description.trim() : "";
        this.sections = sections != null ? sections : new ArrayList<>();
    }

    // No-argument constructor (optional, for frameworks like Hibernate)
    public Course() {
        this.sections = new ArrayList<>(); // Initialize an empty list of sections
    }

    // Getters and Setters
    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = validateAndTrim(courseId, "Course ID");
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = validateAndTrim(courseName, "Course Name");
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description.trim() : "";
    }

    public List<Section> getSections() {
        return sections;
    }


    @Override
    public String toString() {
        return "Course{" +
                "courseId='" + courseId + '\'' +
                ", courseName='" + courseName + '\'' +
                ", description='" + description + '\'' +
                ", sections=" + sections +
                '}';
    }

    public void addSection(Section section) {
        if (section != null) {
            this.sections.add(section);
        }
    }
    
    private String validateAndTrim(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
        return value.trim();
    }
}
