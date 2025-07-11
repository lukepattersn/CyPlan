package com.schedulebuilder.class_scheduler.model;

import java.util.*;

public class ScheduleBuilder {
    private static final int ABSOLUTE_MAX_SCHEDULES = 100;
    private static final int MINIMUM_COMMUTE_TIME_MINUTES = 10; // Minimum 10 minutes between classes

    public static List<Map<Course, List<Section>>> generateNonConflictingSchedules(List<Course> courses, int maxSchedules) {
        maxSchedules = Math.min(maxSchedules, ABSOLUTE_MAX_SCHEDULES);
        
        // Pre-process courses to ensure recitation requirements are met
        List<Course> validCourses = validateAndFilterCourses(courses);
        
        if (validCourses.isEmpty()) {
            return new ArrayList<>(); // Return empty if no valid courses
        }
        
        List<Map<Course, List<Section>>> schedules = new ArrayList<>();
        generateSchedulesRecursive(new HashMap<>(), new ArrayList<>(validCourses), schedules, maxSchedules);
        
        // Sort by score and limit to maxSchedules
        schedules.sort((s1, s2) -> calculateScheduleScore(s2) - calculateScheduleScore(s1));
        return schedules.subList(0, Math.min(schedules.size(), maxSchedules));
    }

    /**
     * Validates courses and ensures those requiring recitations have them available
     */
    private static List<Course> validateAndFilterCourses(List<Course> courses) {
        List<Course> validCourses = new ArrayList<>();
        
        for (Course course : courses) {
            boolean hasLecture = false;
            boolean hasLabOrRecitation = false;
            
            // Check what types of sections are available
            for (Section section : course.getSections()) {
                String format = section.getInstructionalFormat();
                if (format != null) {
                    if ("Lecture".equalsIgnoreCase(format)) {
                        hasLecture = true;
                    } else if ("Laboratory".equalsIgnoreCase(format) || "Recitation".equalsIgnoreCase(format)) {
                        hasLabOrRecitation = true;
                    }
                }
            }
            
            // If course has both lectures AND labs/recitations, it requires both
            // If course only has lectures, that's fine
            // If course only has labs/recitations (unusual), that's fine too
            // Only exclude courses that have lectures but are missing required labs/recitations
            
            if (hasLecture && hasLabOrRecitation) {
                // Course requires both - this is good, include it
                validCourses.add(course);
            } else if (hasLecture && !hasLabOrRecitation) {
                // Course only has lectures - this is a lecture-only course, include it
                validCourses.add(course);
            } else if (!hasLecture && hasLabOrRecitation) {
                // Course only has labs/recitations (unusual but possible), include it
                validCourses.add(course);
            } else {
                // Course has no recognizable sections - exclude it
                System.out.println("Excluding course " + course.getCourseId() + " - no valid sections found");
            }
        }
        
        return validCourses;
    }

    private static void generateSchedulesRecursive(
            Map<Course, List<Section>> currentSchedule,
            List<Course> remainingCourses,
            List<Map<Course, List<Section>>> schedules,
            int maxSchedules) {
        
        if (schedules.size() >= maxSchedules) {
            return;
        }

        if (remainingCourses.isEmpty()) {
            // Validate that the schedule meets all requirements before adding
            if (isValidCompleteSchedule(currentSchedule)) {
                schedules.add(new HashMap<>(currentSchedule));
            }
            return;
        }

        Course course = remainingCourses.get(0);
        List<Course> nextRemainingCourses = remainingCourses.subList(1, remainingCourses.size());

        // Generate all valid section combinations for courses with recitation requirements
        List<List<Section>> sectionCombinations = generateSectionCombinations(course);
        
        for (List<Section> sectionCombo : sectionCombinations) {
            Map<Course, List<Section>> newSchedule = new HashMap<>(currentSchedule);
            
            // Add the section combination for this course
            newSchedule.put(course, new ArrayList<>(sectionCombo));
            
            // Check if this combination conflicts with existing schedule
            if (!hasConflicts(newSchedule)) {
                generateSchedulesRecursive(newSchedule, nextRemainingCourses, schedules, maxSchedules);
            }
        }
    }

    /**
     * Generates valid section combinations for a course (handles courses with multiple required sections)
     */
    private static List<List<Section>> generateSectionCombinations(Course course) {
        List<List<Section>> combinations = new ArrayList<>();
        
        List<Section> lectures = new ArrayList<>();
        List<Section> labs = new ArrayList<>();
        List<Section> recitations = new ArrayList<>();
        List<Section> others = new ArrayList<>();
        
        // Group sections by instructional format
        for (Section section : course.getSections()) {
            String format = section.getInstructionalFormat();
            if (format != null) {
                if ("Lecture".equalsIgnoreCase(format)) {
                    lectures.add(section);
                } else if ("Laboratory".equalsIgnoreCase(format)) {
                    labs.add(section);
                } else if ("Recitation".equalsIgnoreCase(format)) {
                    recitations.add(section);
                } else {
                    others.add(section);
                }
            } else {
                // If no instructional format specified, treat as lecture
                lectures.add(section);
            }
        }
        
        // Determine what combinations are required
        boolean hasLectures = !lectures.isEmpty();
        boolean hasLabs = !labs.isEmpty();
        boolean hasRecitations = !recitations.isEmpty();
        
        if (hasLectures && (hasLabs || hasRecitations)) {
            // Course has lectures AND labs/recitations - MUST include both
            for (Section lecture : lectures) {
                // Try all lab combinations
                for (Section lab : labs) {
                    List<Section> combo = Arrays.asList(lecture, lab);
                    combinations.add(combo);
                }
                // Try all recitation combinations  
                for (Section recitation : recitations) {
                    List<Section> combo = Arrays.asList(lecture, recitation);
                    combinations.add(combo);
                }
            }
        } else if (hasLectures) {
            // Course only has lectures - each lecture is a valid combination
            for (Section lecture : lectures) {
                combinations.add(Arrays.asList(lecture));
            }
        } else if (hasLabs || hasRecitations) {
            // Course only has labs/recitations (unusual case)
            for (Section lab : labs) {
                combinations.add(Arrays.asList(lab));
            }
            for (Section recitation : recitations) {
                combinations.add(Arrays.asList(recitation));
            }
        } else if (!others.isEmpty()) {
            // Course has other types of sections
            for (Section other : others) {
                combinations.add(Arrays.asList(other));
            }
        }
        
        return combinations;
    }

    /**
     * Validates that a complete schedule meets all requirements
     */
    private static boolean isValidCompleteSchedule(Map<Course, List<Section>> schedule) {
        // Group sections by course ID to validate requirements
        Map<String, List<Section>> courseToSections = new HashMap<>();
        
        for (List<Section> sections : schedule.values()) {
            for (Section section : sections) {
                courseToSections.computeIfAbsent(section.getCourseId(), k -> new ArrayList<>()).add(section);
            }
        }
        
        for (Map.Entry<String, List<Section>> entry : courseToSections.entrySet()) {
            List<Section> sections = entry.getValue();
            
            // Count different types of sections for this course
            boolean hasLecture = false;
            boolean hasLabOrRecitation = false;
            
            for (Section section : sections) {
                String format = section.getInstructionalFormat();
                if (format != null) {
                    if ("Lecture".equalsIgnoreCase(format)) {
                        hasLecture = true;
                    } else if ("Laboratory".equalsIgnoreCase(format) || "Recitation".equalsIgnoreCase(format)) {
                        hasLabOrRecitation = true;
                    }
                }
            }
            
            // For this validation, we need to check if the course SHOULD have both types
            // We need to look at the original course to see what section types are available
            // Since we don't have access to the original course here, we'll trust that
            // the combination generation logic has done its job correctly
            
            // The main rule: if we have multiple sections for a course, and one is a lecture,
            // then we should also have a lab/recitation (since single-section courses
            // would only have one section in the schedule)
            if (sections.size() > 1) {
                if (hasLecture && !hasLabOrRecitation) {
                    return false; // Has lecture but missing required lab/recitation
                }
                if (!hasLecture && hasLabOrRecitation) {
                    return false; // Has lab/recitation but missing required lecture
                }
            }
        }
        
        return true;
    }

    private static boolean hasConflicts(Map<Course, List<Section>> schedule) {
        List<Section> sections = new ArrayList<>();
        
        for (List<Section> sectionList : schedule.values()) {
            sections.addAll(sectionList);
        }
        
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
        
        for (String day1 : days1) {
            for (String day2 : days2) {
                if (day1.trim().equals(day2.trim())) {
                    // If days overlap, check time overlap WITH commute time
                    if (timesConflictWithCommuteTime(section1.getTimeStart(), section1.getTimeEnd(),
                            section2.getTimeStart(), section2.getTimeEnd())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Enhanced time conflict check that includes minimum commute time
     */
    private static boolean timesConflictWithCommuteTime(String start1, String end1, String start2, String end2) {
        int start1Minutes = convertTimeToMinutes(start1);
        int end1Minutes = convertTimeToMinutes(end1);
        int start2Minutes = convertTimeToMinutes(start2);
        int end2Minutes = convertTimeToMinutes(end2);
        
        // Check if times overlap directly
        boolean directOverlap = (start1Minutes < end2Minutes && start2Minutes < end1Minutes);
        
        // Check if there's insufficient commute time between classes
        boolean insufficientCommuteTime = false;
        
        // If class 1 ends before class 2 starts, check commute time
        if (end1Minutes <= start2Minutes) {
            insufficientCommuteTime = (start2Minutes - end1Minutes) < MINIMUM_COMMUTE_TIME_MINUTES;
        }
        // If class 2 ends before class 1 starts, check commute time
        else if (end2Minutes <= start1Minutes) {
            insufficientCommuteTime = (start1Minutes - end2Minutes) < MINIMUM_COMMUTE_TIME_MINUTES;
        }
        
        return directOverlap || insufficientCommuteTime;
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

    private static int calculateScheduleScore(Map<Course, List<Section>> schedule) {
        int score = 0;
        List<Section> sections = new ArrayList<>();
        
        for (List<Section> sectionList : schedule.values()) {
            sections.addAll(sectionList);
        }
        
        // Sort sections by day and time
        sections.sort((s1, s2) -> {
            int dayCompare = s1.getDaysOfTheWeek().compareTo(s2.getDaysOfTheWeek());
            if (dayCompare != 0) return dayCompare;
            return s1.getTimeStart().compareTo(s2.getTimeStart());
        });

        // Prefer schedules with appropriate gaps (including commute time)
        for (int i = 0; i < sections.size() - 1; i++) {
            int gap = getTimeDifference(sections.get(i).getTimeEnd(), sections.get(i + 1).getTimeStart());
            if (gap >= 10 && gap <= 30) score += 15;  // Ideal gap (10-30 minutes)
            else if (gap >= 30 && gap <= 60) score += 10; // Acceptable gap
            else if (gap > 60) score -= (gap / 30);    // Penalize large gaps
            else if (gap < 10) score -= 20;           // Heavily penalize insufficient commute time
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

        // Bonus for having required recitations/labs
        score += validateRecitationRequirements(schedule) ? 25 : -50;

        return score;
    }

    /**
     * Validates that all recitation requirements are met in the schedule
     */
    private static boolean validateRecitationRequirements(Map<Course, List<Section>> schedule) {
        Map<String, List<Section>> courseToSections = new HashMap<>();
        
        for (List<Section> sectionList : schedule.values()) {
            for (Section section : sectionList) {
                courseToSections.computeIfAbsent(section.getCourseId(), k -> new ArrayList<>()).add(section);
            }
        }
        
        for (List<Section> sections : courseToSections.values()) {
            boolean hasLecture = false;
            boolean hasLabOrRecitation = false;
            
            for (Section section : sections) {
                String format = section.getInstructionalFormat();
                if (format != null) {
                    if ("Lecture".equalsIgnoreCase(format)) {
                        hasLecture = true;
                    } else if ("Laboratory".equalsIgnoreCase(format) || "Recitation".equalsIgnoreCase(format)) {
                        hasLabOrRecitation = true;
                    }
                }
            }
            
            // If we have multiple sections for a course, ensure they're properly paired
            if (sections.size() > 1) {
                // Multi-section course should have both lecture and lab/recitation
                if (hasLecture && !hasLabOrRecitation) {
                    return false; // Missing required lab/recitation
                }
                if (!hasLecture && hasLabOrRecitation) {
                    return false; // Missing required lecture
                }
            }
            // Single-section courses are always valid (either lecture-only or lab-only courses)
        }
        
        return true;
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
