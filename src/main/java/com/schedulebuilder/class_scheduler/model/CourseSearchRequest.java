package com.schedulebuilder.class_scheduler.model;

/**
 * This class represents the data we send to the Iowa State University API
 * when searching for courses. This is called a "payload object" because it
 * contains the data (payload) that will be sent in the HTTP POST request.
 */
public class CourseSearchRequest {
    private String academicPeriodId;
    private String department;
    private String courseId;
    private String level;
    private String requirement;
    private String instructor;
    private Boolean openSeats;
    private String[] daysOfTheWeek;
    private String sectionStartDate;
    private String sectionEndDate;
    private String title;
    private String deliveryMode;
    private String[] allowedGradingBases;

    // Constructors
    public CourseSearchRequest() {
        // Default constructor for Spring Boot deserialization
    }

    public CourseSearchRequest(String academicPeriodId, String department, String courseId) {
        this.academicPeriodId = academicPeriodId;
        this.department = department;
        this.courseId = courseId;
        this.level = null;
        this.requirement = null;
        this.instructor = "";
        this.openSeats = false;
        this.daysOfTheWeek = new String[0];
        this.sectionStartDate = null;
        this.sectionEndDate = null;
        this.title = "";
        this.deliveryMode = null;
        this.allowedGradingBases = new String[0];
    }

    // Getters and Setters for all fields
    public String getAcademicPeriodId() {
        return academicPeriodId;
    }

    public void setAcademicPeriodId(String academicPeriodId) {
        this.academicPeriodId = academicPeriodId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getRequirement() {
        return requirement;
    }

    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public Boolean getOpenSeats() {
        return openSeats;
    }

    public void setOpenSeats(Boolean openSeats) {
        this.openSeats = openSeats;
    }

    public String[] getDaysOfTheWeek() {
        return daysOfTheWeek;
    }

    public void setDaysOfTheWeek(String[] daysOfTheWeek) {
        this.daysOfTheWeek = daysOfTheWeek;
    }

    public String getSectionStartDate() {
        return sectionStartDate;
    }

    public void setSectionStartDate(String sectionStartDate) {
        this.sectionStartDate = sectionStartDate;
    }

    public String getSectionEndDate() {
        return sectionEndDate;
    }

    public void setSectionEndDate(String sectionEndDate) {
        this.sectionEndDate = sectionEndDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDeliveryMode() {
        return deliveryMode;
    }

    public void setDeliveryMode(String deliveryMode) {
        this.deliveryMode = deliveryMode;
    }

    public String[] getAllowedGradingBases() {
        return allowedGradingBases;
    }

    public void setAllowedGradingBases(String[] allowedGradingBases) {
        this.allowedGradingBases = allowedGradingBases;
    }

    @Override
    public String toString() {
        return "CourseSearchRequest{" +
                "academicPeriodId='" + academicPeriodId + '\'' +
                ", department='" + department + '\'' +
                ", courseId='" + courseId + '\'' +
                ", level='" + level + '\'' +
                ", requirement='" + requirement + '\'' +
                ", instructor='" + instructor + '\'' +
                ", openSeats=" + openSeats +
                ", daysOfTheWeek=" + (daysOfTheWeek != null ? String.join(", ", daysOfTheWeek) : "[]") +
                ", sectionStartDate='" + sectionStartDate + '\'' +
                ", sectionEndDate='" + sectionEndDate + '\'' +
                ", title='" + title + '\'' +
                ", deliveryMode='" + deliveryMode + '\'' +
                ", allowedGradingBases=" + (allowedGradingBases != null ? String.join(", ", allowedGradingBases) : "[]") +
                '}';
    }
}
