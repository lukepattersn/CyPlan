package com.schedulebuilder.class_scheduler.model;

/**
 * Enum representing the type of a course section based on its delivery mode and scheduling status.
 * This provides a type-safe way to categorize sections throughout the application.
 */
public enum SectionType {
    /**
     * In-person section with scheduled meeting times and days.
     * These sections participate in the schedule generation algorithm.
     */
    IN_PERSON,

    /**
     * Online section with no fixed meeting times.
     * These sections are displayed separately and do not participate in schedule generation.
     */
    ONLINE,

    /**
     * Section with To-Be-Determined meeting times or days.
     * These sections are displayed separately and do not participate in schedule generation.
     */
    TBD
}
