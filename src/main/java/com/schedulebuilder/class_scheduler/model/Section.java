package com.schedulebuilder.class_scheduler.model;

import java.util.List;

public class Section {
    private String daysOfTheWeek; // e.g., "TR" -> "Tuesday, Thursday"
    private int openSeats;
    private String instructor;
    private String courseId;
    private String timeStart; // e.g., "1:10 PM"
    private String timeEnd;   // e.g., "2:00 PM"
    private String sectionNumber; // e.g., 1, 2, 3, A, B, C
    private String description; // Course description

    // Constructor
    public Section(String daysOfTheWeek, int openSeats, String instructor, String courseId, String timeStart, String timeEnd, String sectionNumber) {
        this.daysOfTheWeek = daysOfTheWeek;
        this.openSeats = openSeats;
        this.instructor = instructor;
        this.courseId = courseId;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.sectionNumber = sectionNumber;
    }

    // Getters and Setters
    public String getDaysOfTheWeek() {
        return daysOfTheWeek;
    }

    public void setDaysOfTheWeek(String daysOfTheWeek) {
        this.daysOfTheWeek = daysOfTheWeek;
    }

    public int getOpenSeats() {
        return openSeats;
    }

    public void setOpenSeats(int openSeats) {
        this.openSeats = openSeats;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(String timeStart) {
        this.timeStart = timeStart;
    }

    public String getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(String timeEnd) {
        this.timeEnd = timeEnd;
    }

    public String getSectionNumber() {
        return sectionNumber;
    }

    public void setSectionNumber(String sectionNumber) {
        this.sectionNumber = sectionNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Section{" +
                "daysOfTheWeek='" + daysOfTheWeek + '\'' +
                ", openSeats=" + openSeats +
                ", instructor='" + instructor + '\'' +
                ", courseId='" + courseId + '\'' +
                ", timeStart='" + timeStart + '\'' +
                ", timeEnd='" + timeEnd + '\'' +
                ", sectionNumber='" + sectionNumber + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    public int getIndex(List<Section> sections) {
        return sections.indexOf(this);
    }
}