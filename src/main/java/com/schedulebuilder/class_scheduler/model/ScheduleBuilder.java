package com.schedulebuilder.class_scheduler.model;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ScheduleBuilder {

    public static List<Map<Course, Section>> generateNonConflictingSchedules(List<Course> courses, int numberOfSchedules) {
        List<Map<Course, Section>> schedules = new ArrayList<>();

        // Get all possible combinations of sections
        List<List<Section>> allSections = new ArrayList<>();
        for (Course course : courses) {
            allSections.add(course.getSections());
        }

        // Generate all possible combinations
        List<List<Section>> allCombinations = cartesianProduct(allSections);

        // Shuffle the combinations to randomize the schedules
        Collections.shuffle(allCombinations);

        // DateTimeFormatter for parsing time strings
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

        for (List<Section> combination : allCombinations) {
            if (schedules.size() >= numberOfSchedules) {
                break;
            }

            if (isNonConflicting(combination, timeFormatter)) {
                Map<Course, Section> schedule = new HashMap<>();
                for (int i = 0; i < courses.size(); i++) {
                    schedule.put(courses.get(i), combination.get(i));
                }
                schedules.add(schedule);
            }
        }

        return schedules;
    }

    private static boolean isNonConflicting(List<Section> sections, DateTimeFormatter timeFormatter) {
        // Check for time conflicts between sections
        for (int i = 0; i < sections.size(); i++) {
            Section s1 = sections.get(i);
            if (s1.getTimeStart().equals("N/A") || s1.getTimeEnd().equals("N/A")) {
                continue; // Skip sections without time
            }
            List<String> days1 = Arrays.asList(s1.getDaysOfTheWeek().split(", "));

            LocalTime start1 = LocalTime.parse(s1.getTimeStart(), timeFormatter);
            LocalTime end1 = LocalTime.parse(s1.getTimeEnd(), timeFormatter);

            for (int j = i + 1; j < sections.size(); j++) {
                Section s2 = sections.get(j);
                if (s2.getTimeStart().equals("N/A") || s2.getTimeEnd().equals("N/A")) {
                    continue; // Skip sections without time
                }
                List<String> days2 = Arrays.asList(s2.getDaysOfTheWeek().split(", "));

                // Check if the sections occur on the same days
                Set<String> commonDays = new HashSet<>(days1);
                commonDays.retainAll(days2);
                if (!commonDays.isEmpty()) {
                    LocalTime start2 = LocalTime.parse(s2.getTimeStart(), timeFormatter);
                    LocalTime end2 = LocalTime.parse(s2.getTimeEnd(), timeFormatter);

                    // Check for time overlap
                    if (start1.isBefore(end2) && start2.isBefore(end1)) {
                        // Time conflict detected
                        return false;
                    }
                }
            }
        }
        return true; // No conflicts
    }

    // Utility method to compute cartesian product of lists
    private static List<List<Section>> cartesianProduct(List<List<Section>> lists) {
        List<List<Section>> resultLists = new ArrayList<>();
        if (lists.isEmpty()) {
            resultLists.add(new ArrayList<>());
            return resultLists;
        } else {
            List<Section> firstList = lists.get(0);
            List<List<Section>> remainingLists = cartesianProduct(lists.subList(1, lists.size()));
            for (Section condition : firstList) {
                for (List<Section> remainingList : remainingLists) {
                    ArrayList<Section> resultList = new ArrayList<>();
                    resultList.add(condition);
                    resultList.addAll(remainingList);
                    resultLists.add(resultList);
                }
            }
        }
        return resultLists;
    }
}
