package com.schedulebuilder.class_scheduler.model;

public class Section {
    private String daysOfTheWeek; // e.g., "TR" -> "Tuesday, Thursday"
    private int openSeats;
    private String instructor;
    private String courseId;
    private String timeStart; // e.g., "1:10 PM"
    private String timeEnd;   // e.g., "2:00 PM"

    // Constructor
    public Section(String daysOfTheWeek, int openSeats, String instructor, String courseId, String timeStart, String timeEnd) {
        this.daysOfTheWeek = daysOfTheWeek;
        this.openSeats = openSeats;
        this.instructor = instructor;
        this.courseId = courseId;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
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

    @Override
    public String toString() {
        return "Section{" +
                "daysOfTheWeek='" + daysOfTheWeek + '\'' +
                ", openSeats=" + openSeats +
                ", instructor='" + instructor + '\'' +
                ", courseId='" + courseId + '\'' +
                ", timeStart='" + timeStart + '\'' +
                ", timeEnd='" + timeEnd + '\'' +
                '}';
    }

}
