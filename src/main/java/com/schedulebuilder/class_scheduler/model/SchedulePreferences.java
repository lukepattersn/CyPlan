package com.schedulebuilder.class_scheduler.model;

import java.util.List;
import java.util.ArrayList;

public class SchedulePreferences {
    private List<String> preferredDays;
    private String timePreference; // "morning", "afternoon", "evening"
    private String gapPreference; // "none", "minimal", "short", "medium", "long"
    private String scheduleStyle; // "compact", "spread"

    public SchedulePreferences() {
        this.preferredDays = new ArrayList<>();
        this.timePreference = "";
        this.gapPreference = "none";
        this.scheduleStyle = "";
    }

    public SchedulePreferences(List<String> preferredDays, String timePreference, 
                              String gapPreference, String scheduleStyle) {
        this.preferredDays = preferredDays != null ? preferredDays : new ArrayList<>();
        this.timePreference = timePreference != null ? timePreference : "";
        this.gapPreference = gapPreference != null ? gapPreference : "none";
        this.scheduleStyle = scheduleStyle != null ? scheduleStyle : "";
    }

    // Getters and Setters
    public List<String> getPreferredDays() {
        return preferredDays;
    }

    public void setPreferredDays(List<String> preferredDays) {
        this.preferredDays = preferredDays != null ? preferredDays : new ArrayList<>();
    }

    public String getTimePreference() {
        return timePreference;
    }

    public void setTimePreference(String timePreference) {
        this.timePreference = timePreference != null ? timePreference : "";
    }

    public String getGapPreference() {
        return gapPreference;
    }

    public void setGapPreference(String gapPreference) {
        this.gapPreference = gapPreference != null ? gapPreference : "none";
    }

    public String getScheduleStyle() {
        return scheduleStyle;
    }

    public void setScheduleStyle(String scheduleStyle) {
        this.scheduleStyle = scheduleStyle != null ? scheduleStyle : "";
    }

    // Helper methods for time preferences
    public boolean isMorningPreferred() {
        return "morning".equalsIgnoreCase(timePreference);
    }

    public boolean isAfternoonPreferred() {
        return "afternoon".equalsIgnoreCase(timePreference);
    }

    public boolean isEveningPreferred() {
        return "evening".equalsIgnoreCase(timePreference);
    }

    // Helper methods for gap preferences
    public int getMinGapMinutes() {
        switch (gapPreference.toLowerCase()) {
            case "minimal": return 0;
            case "short": return 15;
            case "medium": return 30;
            case "long": return 60;
            default: return 0;
        }
    }

    public int getMaxGapMinutes() {
        switch (gapPreference.toLowerCase()) {
            case "minimal": return 15;
            case "short": return 30;
            case "medium": return 60;
            case "long": return Integer.MAX_VALUE;
            default: return Integer.MAX_VALUE;
        }
    }

    // Helper method for schedule style
    public boolean isCompactStylePreferred() {
        return "compact".equalsIgnoreCase(scheduleStyle);
    }

    public boolean isSpreadStylePreferred() {
        return "spread".equalsIgnoreCase(scheduleStyle);
    }

    // Helper method to check if a day is preferred
    public boolean isDayPreferred(String day) {
        return preferredDays.isEmpty() || preferredDays.contains(day);
    }

    // Helper method to check if preferences are effectively empty
    public boolean hasNoPreferences() {
        return (preferredDays == null || preferredDays.isEmpty()) &&
               (timePreference == null || timePreference.isEmpty()) &&
               (gapPreference == null || gapPreference.isEmpty() || "none".equals(gapPreference)) &&
               (scheduleStyle == null || scheduleStyle.isEmpty());
    }

    @Override
    public String toString() {
        return "SchedulePreferences{" +
                "preferredDays=" + preferredDays +
                ", timePreference='" + timePreference + '\'' +
                ", gapPreference='" + gapPreference + '\'' +
                ", scheduleStyle='" + scheduleStyle + '\'' +
                '}';
    }
}