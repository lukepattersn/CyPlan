package com.schedulebuilder.class_scheduler.model;

public class AcademicPeriod {
    private String id;
    private String displayName;
    private String startDate;
    private String endDate;
    private boolean isActive;

    public AcademicPeriod() {
    }

    public AcademicPeriod(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public AcademicPeriod(String id, String displayName, String startDate, String endDate, boolean isActive) {
        this.id = id;
        this.displayName = displayName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive;
    }

    // Parse academic period from the format used in the API
    public static AcademicPeriod fromApiFormat(String periodString) {
        // Example formats:
        // "2025 Fall Semester (08/25/2025-12/19/2025)"
        // "2024-2025 Winter Session (12/23/2024-01/17/2025)"
        
        String id = convertToApiId(periodString);
        return new AcademicPeriod(id, periodString);
    }
    
    private static String convertToApiId(String displayName) {
        // Convert display name to API ID format
        if (displayName.contains("Fall Semester")) {
            String year = displayName.substring(0, 4);
            return "ACADEMIC_PERIOD-" + year + "Fall";
        } else if (displayName.contains("Spring Semester")) {
            String year = displayName.substring(0, 4);
            return "ACADEMIC_PERIOD-" + year + "Spring";
        } else if (displayName.contains("Summer Semester")) {
            String year = displayName.substring(0, 4);
            return "ACADEMIC_PERIOD-" + year + "Summer";
        } else if (displayName.contains("Winter Session")) {
            // Extract the first year from the range
            String yearRange = displayName.substring(0, displayName.indexOf(" "));
            String firstYear = yearRange.split("-")[0];
            return "ACADEMIC_PERIOD-" + firstYear + "Winter";
        }
        
        // Default fallback
        return "ACADEMIC_PERIOD-2025Fall";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return "AcademicPeriod{" +
                "id='" + id + '\'' +
                ", displayName='" + displayName + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", isActive=" + isActive +
                '}';
    }
} 