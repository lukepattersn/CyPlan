package com.schedulebuilder.class_scheduler.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        generateSchedulesRecursive(new HashMap<>(), new ArrayList<>(courses), schedules, maxSchedules);

        // Sort schedules by optimization criteria
        Collections.sort(schedules, (s1, s2) -> {
            // Calculate scores for each schedule
            int score1 = calculateScheduleScore(s1);
            int score2 = calculateScheduleScore(s2);
            return score2 - score1; // Higher scores first
        });

        return schedules;
    }

    private static void generateSchedulesRecursive(Map<Course, Section> currentSchedule, List<Course> courses, List<Map<Course, Section>> schedules, int maxSchedules) {
        if (courses.isEmpty()) {
            if (!hasConflicts(currentSchedule)) {
                schedules.add(new HashMap<>(currentSchedule));
            }
            return;
        }

        Course course = courses.get(0);
        List<Section> sections = course.getSections();
        List<Section> regulars = sections.stream()
                .filter(s -> !isLabSection(s.getSectionNumber()))
                .collect(Collectors.toList());
        List<Section> labs = sections.stream()
                .filter(s -> isLabSection(s.getSectionNumber()))
                .collect(Collectors.toList());
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
                        generateSchedulesRecursive(currentSchedule, courses.subList(1, courses.size()), schedules, maxSchedules);
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
                generateSchedulesRecursive(currentSchedule, courses.subList(1, courses.size()), schedules, maxSchedules);
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

    private static int calculateScheduleScore(Map<Course, Section> schedule) {
        int score = 0;

        // Time gap analysis
        List<Section> sections = new ArrayList<>(schedule.values());
        sections.sort((s1, s2) -> {
            // Sort by day, then start time
            int dayCompare = s1.getDaysOfTheWeek().compareTo(s2.getDaysOfTheWeek());
            if (dayCompare != 0) return dayCompare;
            return s1.getTimeStart().compareTo(s2.getTimeStart());
        });

        // Criteria 1: Minimize time gaps between classes (0-40 points)
        for (int i = 0; i < sections.size() - 1; i++) {
            int gap = getTimeDifference(sections.get(i).getTimeEnd(), sections.get(i + 1).getTimeStart());
            if (gap <= 15) score += 10; // Ideal gap
            else if (gap <= 30) score += 5; // Acceptable gap
            else score -= (gap / 30); // Penalize large gaps
        }

        // Criteria 2: Prefer balanced days (0-30 points)
        Map<String, Integer> classesPerDay = new HashMap<>();
        for (Section section : sections) {
            for (String day : section.getDaysOfTheWeek().split(",")) {
                classesPerDay.merge(day.trim(), 1, Integer::sum);
            }
        }
        int maxClassesInDay = classesPerDay.values().stream().mapToInt(i -> i).max().orElse(0);
        score += (30 - (maxClassesInDay * 5)); // Penalize days with too many classes

        // Criteria 3: Prefer reasonable start times (0-30 points)
        for (Section section : sections) {
            String startTime = section.getTimeStart();
            if (isReasonableStartTime(startTime)) score += 5;
        }

        return score;
    }

    private static boolean isReasonableStartTime(String time) {
        // Consider 9AM-3PM as reasonable start times
        try {
            String[] parts = time.split(" ");
            int hour = Integer.parseInt(parts[0].split(":")[0]);
            boolean isPM = parts[1].equals("PM");

            if (!isPM && hour >= 9) return true;
            if (isPM && hour <= 3) return true;
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    private static int getTimeDifference(String time1, String time2) {
        // Convert times to minutes and calculate difference
        // Implementation depends on your time format
        // Return difference in minutes
        return 0; // Implement this based on your time format
    }
}
