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

        // First, process courses to identify which ones have labs
        Map<Course, List<Section>> regularSections = new HashMap<>();
        Map<Course, List<Section>> labSections = new HashMap<>();

        for (Course course : courses) {
            regularSections.put(course, new ArrayList<>());
            labSections.put(course, new ArrayList<>());

            // Separate regular sections and lab sections
            for (Section section : course.getSections()) {
                if (isLabSection(section.getSectionNumber())) {
                    labSections.get(course).add(section);
                } else {
                    regularSections.get(course).add(section);
                }
            }
        }

        generateSchedulesRecursive(courses, 0, new HashMap<>(), schedules, maxSchedules, regularSections, labSections);
        return schedules;
    }

    private static void generateSchedulesRecursive(List<Course> courses, int courseIndex,
                                                   Map<Course, Section> currentSchedule, List<Map<Course, Section>> schedules,
                                                   int maxSchedules, Map<Course, List<Section>> regularSections,
                                                   Map<Course, List<Section>> labSections) {

        if (schedules.size() >= maxSchedules) {
            return;
        }

        if (courseIndex == courses.size()) {
            if (!hasConflicts(currentSchedule)) {
                schedules.add(new HashMap<>(currentSchedule));
            }
            return;
        }

        Course course = courses.get(courseIndex);
        List<Section> regulars = regularSections.get(course);
        List<Section> labs = labSections.get(course);
        boolean hasLab = !labs.isEmpty();

        // For each regular section
        for (Section regularSection : regulars) {
            currentSchedule.put(course, regularSection);

            if (hasLab) {
                // If course has labs, we MUST include one with the regular section
                boolean foundValidLab = false;

                // Try each lab section with this regular section
                for (Section labSection : labs) {
                    Course labCourse = new Course(course.getCourseId() + " Lab",
                            course.getCourseName() + " Lab",
                            "Lab section for " + course.getCourseId());

                    // Add lab temporarily to check conflicts
                    currentSchedule.put(labCourse, labSection);

                    if (!hasConflicts(currentSchedule)) {
                        // Valid lab found, continue with next course
                        foundValidLab = true;
                        generateSchedulesRecursive(courses, courseIndex + 1, currentSchedule,
                                schedules, maxSchedules, regularSections, labSections);
                    }

                    // Remove lab to try next one
                    currentSchedule.remove(labCourse);
                }

                // If no valid lab was found for this regular section, skip this regular section
                if (!foundValidLab) {
                    currentSchedule.remove(course);
                    continue;
                }
            } else {
                // No lab required, continue with next course
                generateSchedulesRecursive(courses, courseIndex + 1, currentSchedule,
                        schedules, maxSchedules, regularSections, labSections);
            }

            currentSchedule.remove(course);
        }
    }

    private static boolean isLabSection(String sectionNumber) {
        return sectionNumber.matches(".*[A-Za-z].*");
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
