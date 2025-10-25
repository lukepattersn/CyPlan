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
    private String instructionalFormat; // e.g., "Lecture", "Laboratory", "Recitation"
    private String location; // Room/building information
    private String deliveryMode; // e.g., "Online", "In-Person", "Hybrid"
    private String credits; // Course credits (e.g., "3", "4.5")
    private SectionType sectionType; // Type of section (IN_PERSON, ONLINE, TBD)

    // Constructor
    public Section(String daysOfTheWeek, int openSeats, String instructor, String courseId, 
                   String timeStart, String timeEnd, String sectionNumber) {
        this.daysOfTheWeek = validateAndTrim(daysOfTheWeek, "Days of the Week");
        this.openSeats = Math.max(0, openSeats);
        this.instructor = validateAndTrim(instructor, "Instructor");
        this.courseId = validateAndTrim(courseId, "Course ID");
        this.timeStart = validateAndTrim(timeStart, "Time Start");
        this.timeEnd = validateAndTrim(timeEnd, "Time End");
        this.sectionNumber = validateAndTrim(sectionNumber, "Section Number");
    }

    // Full constructor with all fields
    public Section(String daysOfTheWeek, int openSeats, String instructor, String courseId, 
                   String timeStart, String timeEnd, String sectionNumber, String instructionalFormat, String location, String deliveryMode, String credits) {
        this.daysOfTheWeek = validateAndTrim(daysOfTheWeek, "Days of the Week");
        this.openSeats = Math.max(0, openSeats);
        this.instructor = validateAndTrim(instructor, "Instructor");
        this.courseId = validateAndTrim(courseId, "Course ID");
        this.timeStart = validateAndTrim(timeStart, "Time Start");
        this.timeEnd = validateAndTrim(timeEnd, "Time End");
        this.sectionNumber = validateAndTrim(sectionNumber, "Section Number");
        this.instructionalFormat = instructionalFormat != null ? instructionalFormat.trim() : "Unknown";
        this.location = location != null ? location.trim() : "TBA";
        this.deliveryMode = deliveryMode != null ? deliveryMode.trim() : "In-Person";
        this.credits = credits != null ? credits.trim() : "0";
    }

    // Method to determine if this is a lecture section
    public boolean isLecture() {
        return "Lecture".equalsIgnoreCase(instructionalFormat);
    }

    // Method to determine if this is a recitation section
    public boolean isRecitation() {
        return "Recitation".equalsIgnoreCase(instructionalFormat);
    }

    // Method to determine if this is a lab section
    public boolean isLab() {
        return "Laboratory".equalsIgnoreCase(instructionalFormat) || "Lab".equalsIgnoreCase(instructionalFormat);
    }

    // Getters and Setters
    public String getDaysOfTheWeek() {
        return daysOfTheWeek;
    }

    public void setDaysOfTheWeek(String daysOfTheWeek) {
        this.daysOfTheWeek = validateAndTrim(daysOfTheWeek, "Days of the Week");
    }

    public int getOpenSeats() {
        return openSeats;
    }

    public void setOpenSeats(int openSeats) {
        this.openSeats = Math.max(0, openSeats);
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = validateAndTrim(instructor, "Instructor");
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = validateAndTrim(courseId, "Course ID");
    }

    public String getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(String timeStart) {
        this.timeStart = validateAndTrim(timeStart, "Time Start");
    }

    public String getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(String timeEnd) {
        this.timeEnd = validateAndTrim(timeEnd, "Time End");
    }

    public String getSectionNumber() {
        return sectionNumber;
    }

    public void setSectionNumber(String sectionNumber) {
        this.sectionNumber = validateAndTrim(sectionNumber, "Section Number");
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInstructionalFormat() {
        return instructionalFormat;
    }

    public void setInstructionalFormat(String instructionalFormat) {
        this.instructionalFormat = instructionalFormat != null ? instructionalFormat.trim() : "Unknown";
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location != null ? location.trim() : "TBA";
    }

    public String getDeliveryMode() {
        return deliveryMode;
    }

    public void setDeliveryMode(String deliveryMode) {
        this.deliveryMode = deliveryMode != null ? deliveryMode.trim() : "In-Person";
    }

    public boolean isOnline() {
        return "Online".equalsIgnoreCase(deliveryMode);
    }

    public String getCredits() {
        return credits;
    }

    public void setCredits(String credits) {
        this.credits = credits != null ? credits.trim() : "0";
    }

    public SectionType getSectionType() {
        return sectionType;
    }

    public void setSectionType(SectionType sectionType) {
        this.sectionType = sectionType;
    }

    /**
     * Determines if this section can be scheduled in the calendar.
     * Only IN_PERSON sections with valid times and days are schedulable.
     */
    public boolean isSchedulable() {
        return sectionType == SectionType.IN_PERSON &&
               daysOfTheWeek != null && !daysOfTheWeek.equals("TBD") &&
               timeStart != null && !timeStart.equals("TBD") &&
               timeEnd != null && !timeEnd.equals("TBD");
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
                ", instructionalFormat='" + instructionalFormat + '\'' +
                ", location='" + location + '\'' +
                ", deliveryMode='" + deliveryMode + '\'' +
                ", credits='" + credits + '\'' +
                '}';
    }

    public int getIndex(List<Section> sections) {
        return sections != null ? sections.indexOf(this) : -1;
    }
    
    private String validateAndTrim(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            // Allow "TBD" as a default for missing time/day information
            return "TBD";
        }
        return value.trim();
    }
}