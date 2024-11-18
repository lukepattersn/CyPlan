package com.schedulebuilder.class_scheduler.model;

import java.util.*;

public class ScheduleBuilder {
    private static final int ABSOLUTE_MAX_SCHEDULES = 100;

    public static List<Map<Course, Section>> generateNonConflictingSchedules(List<Course> courses, int maxSchedules) {
        maxSchedules = Math.min(maxSchedules, ABSOLUTE_MAX_SCHEDULES);
        List<Map<Course, Section>> schedules = new ArrayList<>();
        generateSchedulesRecursive(new HashMap<>(), new ArrayList<>(courses), schedules, maxSchedules);
        
        // Sort by score and limit to maxSchedules
        schedules.sort((s1, s2) -> calculateScheduleScore(s2) - calculateScheduleScore(s1));
        return schedules.subList(0, Math.min(schedules.size(), maxSchedules));
    }

    private static void generateSchedulesRecursive(
            Map<Course, Section> currentSchedule,
            List<Course> remainingCourses,
            List<Map<Course, Section>> schedules,
            int maxSchedules) {
        
        if (schedules.size() >= maxSchedules) {
            return;
        }

        if (remainingCourses.isEmpty()) {
            schedules.add(new HashMap<>(currentSchedule));
            return;
        }

        Course course = remainingCourses.get(0);
        List<Course> nextRemainingCourses = remainingCourses.subList(1, remainingCourses.size());

        for (Section section : course.getSections()) {
            Map<Course, Section> newSchedule = new HashMap<>(currentSchedule);
            newSchedule.put(course, section);
            
            if (!hasConflicts(newSchedule)) {
                generateSchedulesRecursive(newSchedule, nextRemainingCourses, schedules, maxSchedules);
            }
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
        // Skip conflict check if either section is online
        if (section1.getTimeStart().equals("N/A") || section2.getTimeStart().equals("N/A")) {
            return false;
        }

        // Check day overlap
        String[] days1 = section1.getDaysOfTheWeek().split(",");
        String[] days2 = section2.getDaysOfTheWeek().split(",");
        
        boolean daysOverlap = false;
        for (String day1 : days1) {
            for (String day2 : days2) {
                if (day1.trim().equals(day2.trim())) {
                    // If days overlap, check time overlap
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
        int start1Minutes = convertTimeToMinutes(start1);
        int end1Minutes = convertTimeToMinutes(end1);
        int start2Minutes = convertTimeToMinutes(start2);
        int end2Minutes = convertTimeToMinutes(end2);
        
        return (start1Minutes < end2Minutes && start2Minutes < end1Minutes);
    }

    private static int convertTimeToMinutes(String time) {
        try {
            String[] parts = time.split(" ");
            String[] timeParts = parts[0].split(":");
            int hours = Integer.parseInt(timeParts[0]);
            int minutes = Integer.parseInt(timeParts[1]);
            
            if (parts[1].equals("PM") && hours != 12) {
                hours += 12;
            } else if (parts[1].equals("AM") && hours == 12) {
                hours = 0;
            }
            
            return hours * 60 + minutes;
        } catch (Exception e) {
            return 0; // Return 0 for invalid times
        }
    }

    private static int calculateScheduleScore(Map<Course, Section> schedule) {
        int score = 0;
        List<Section> sections = new ArrayList<>(schedule.values());
        
        // Sort sections by day and time
        sections.sort((s1, s2) -> {
            int dayCompare = s1.getDaysOfTheWeek().compareTo(s2.getDaysOfTheWeek());
            if (dayCompare != 0) return dayCompare;
            return s1.getTimeStart().compareTo(s2.getTimeStart());
        });

        // Prefer schedules with fewer gaps
        for (int i = 0; i < sections.size() - 1; i++) {
            int gap = getTimeDifference(sections.get(i).getTimeEnd(), sections.get(i + 1).getTimeStart());
            if (gap <= 15) score += 10;      // Ideal gap
            else if (gap <= 30) score += 5;  // Acceptable gap
            else score -= (gap / 30);        // Penalize large gaps
        }

        // Prefer balanced days
        Map<String, Integer> classesPerDay = new HashMap<>();
        for (Section section : sections) {
            for (String day : section.getDaysOfTheWeek().split(",")) {
                classesPerDay.merge(day.trim(), 1, Integer::sum);
            }
        }
        int maxClassesInDay = classesPerDay.values().stream().mapToInt(i -> i).max().orElse(0);
        score += (30 - (maxClassesInDay * 5));

        // Prefer reasonable start times (9AM-3PM)
        for (Section section : sections) {
            if (isReasonableStartTime(section.getTimeStart())) {
                score += 5;
            }
        }

        return score;
    }

    private static boolean isReasonableStartTime(String time) {
        if (time.equals("N/A")) return false;
        
        try {
            int minutes = convertTimeToMinutes(time);
            int hour = minutes / 60;
            return (hour >= 9 && hour <= 15); // 9 AM to 3 PM
        } catch (Exception e) {
            return false;
        }
    }

    private static int getTimeDifference(String time1, String time2) {
        if (time1.equals("N/A") || time2.equals("N/A")) return 0;
        
        int minutes1 = convertTimeToMinutes(time1);
        int minutes2 = convertTimeToMinutes(time2);
        return Math.abs(minutes2 - minutes1);
    }
}
