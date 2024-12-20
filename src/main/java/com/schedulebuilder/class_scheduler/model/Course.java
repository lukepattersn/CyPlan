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
        this.courseId = courseId;
        this.courseName = courseName;
        this.description = description;
        this.sections = new ArrayList<>(); // Initialize an empty list of sections
    }

    // Full constructor with sections
    public Course(String courseId, String courseName, String description, List<Section> sections) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.description = description;
        this.sections = sections;
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
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
        this.sections.add(section);
    }
}
