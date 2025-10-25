package com.schedulebuilder.class_scheduler.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents the data we send to the Iowa State University API
 * when searching for courses. This is called a "payload object" because it
 * contains the data (payload) that will be sent in the HTTP POST request.
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public class CourseSearchRequest {
    @JsonProperty("academicPeriodId")
    private String academicPeriodId;

    @JsonProperty("courseSubject")
    private String courseSubject;

    @JsonProperty("courseNumber")
    private String courseNumber;

    @JsonProperty("level")
    private String level;

    @JsonProperty("requirement")
    private String requirement;

    @JsonProperty("instructor")
    private String instructor;

    @JsonProperty("semesterTag")
    private String semesterTag;

    @JsonProperty("credits")
    private String credits;

    @JsonProperty("openSeats")
    private Boolean openSeats;

    @JsonProperty("daysOfTheWeek")
    private String[] daysOfTheWeek;

    @JsonProperty("sectionStartDate")
    private String sectionStartDate;

    @JsonProperty("sectionEndDate")
    private String sectionEndDate;

    @JsonProperty("title")
    private String title;

    @JsonProperty("deliveryMode")
    private String deliveryMode;

    @JsonProperty("allowedGradingBases")
    private String[] allowedGradingBases;

    // Constructors
    public CourseSearchRequest() {
        // Default constructor for Spring Boot deserialization
    }

    public CourseSearchRequest(String academicPeriodId, String courseSubject, String courseNumber) {
        this.academicPeriodId = academicPeriodId;
        this.courseSubject = courseSubject;
        this.courseNumber = courseNumber;
        this.level = null;
        this.requirement = null;
        this.instructor = "";
        this.semesterTag = null;
        this.credits = null;
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

    public String getCourseSubject() {
        return courseSubject;
    }

    public void setCourseSubject(String courseSubject) {
        this.courseSubject = courseSubject;
    }

    public String getCourseNumber() {
        return courseNumber;
    }

    public void setCourseNumber(String courseNumber) {
        this.courseNumber = courseNumber;
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

    public String getSemesterTag() {
        return semesterTag;
    }

    public void setSemesterTag(String semesterTag) {
        this.semesterTag = semesterTag;
    }

    public String getCredits() {
        return credits;
    }

    public void setCredits(String credits) {
        this.credits = credits;
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
                ", courseSubject='" + courseSubject + '\'' +
                ", courseNumber='" + courseNumber + '\'' +
                ", level='" + level + '\'' +
                ", requirement='" + requirement + '\'' +
                ", instructor='" + instructor + '\'' +
                ", semesterTag='" + semesterTag + '\'' +
                ", credits='" + credits + '\'' +
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
