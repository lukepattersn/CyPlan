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

    // Getters and setters
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
}
