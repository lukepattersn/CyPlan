package com.schedulebuilder.class_scheduler.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScheduleBuilder {

    /**
     * Generates non-conflicting schedules for the given courses.
     *
     * @param courses       List of courses to generate schedules for.
     * @param maxSchedules  Maximum number of schedules to generate.
     * @return List of non-conflicting schedules (maps of Course -> Section).
     */
    public static List<Map<Course, Section>> generateNonConflictingSchedules(List<Course> courses, int maxSchedules) {
        List<Map<Course, Section>> schedules = new ArrayList<>();
        generateSchedulesRecursive(courses, 0, new HashMap<>(), schedules, maxSchedules);
        return schedules;
    }

    private static void generateSchedulesRecursive(List<Course> courses, int courseIndex, Map<Course, Section> currentSchedule,
                                                   List<Map<Course, Section>> schedules, int maxSchedules) {
        if (schedules.size() >= maxSchedules) {
            return; // Stop once the desired number of schedules is reached
        }

        if (courseIndex == courses.size()) {
            // Base case: Add the current schedule if it's valid
            if (!hasConflicts(currentSchedule)) {
                schedules.add(new HashMap<>(currentSchedule));
            }
            return;
        }

        Course course = courses.get(courseIndex);
        for (Section section : course.getSections()) {
            currentSchedule.put(course, section); // Add the section to the current schedule
            generateSchedulesRecursive(courses, courseIndex + 1, currentSchedule, schedules, maxSchedules);
            currentSchedule.remove(course); // Backtrack to try other combinations
        }
    }

    private static boolean hasConflicts(Map<Course, Section> schedule) {
        List<Section> sections = new ArrayList<>(schedule.values());

        for (int i = 0; i < sections.size(); i++) {
            for (int j = i + 1; j < sections.size(); j++) {
                if (sectionsConflict(sections.get(i), sections.get(j))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean sectionsConflict(Section section1, Section section2) {
        // If days of the week overlap
        for (String day1 : section1.getDaysOfTheWeek().split(",")) {
            for (String day2 : section2.getDaysOfTheWeek().split(",")) {
                if (day1.trim().equals(day2.trim())) {
                    // If times overlap
                    if (timesOverlap(section1.getTimeStart(), section1.getTimeEnd(),
                            section2.getTimeStart(), section2.getTimeEnd())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean timesOverlap(String start1, String end1, String start2, String end2) {
        return !(end1.compareTo(start2) <= 0 || start1.compareTo(end2) >= 0);
    }
}
