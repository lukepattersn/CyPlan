package com.schedulebuilder.class_scheduler.model;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

public class ScheduleBuilder {
    private static final Logger logger = Logger.getLogger(ScheduleBuilder.class.getName());
    private static final int ABSOLUTE_MAX_SCHEDULES = 100;
    private static final int MINIMUM_COMMUTE_TIME_MINUTES = 10;
    private static final int IDEAL_GAP_MIN = 10;
    private static final int IDEAL_GAP_MAX = 30;
    private static final int ACCEPTABLE_GAP_MAX = 60;
    private static final int REASONABLE_START_HOUR_MIN = 9;
    private static final int REASONABLE_START_HOUR_MAX = 15;
    private static final String LECTURE_FORMAT = "Lecture";
    private static final String LABORATORY_FORMAT = "Laboratory";
    private static final String RECITATION_FORMAT = "Recitation";
    private static final String DISCUSSION_FORMAT = "Discussion";
    private static final String ONLINE_TIME = "N/A";

    public static List<Map<Course, List<Section>>> generateNonConflictingSchedules(List<Course> courses, int maxSchedules) {
        return generateNonConflictingSchedules(courses, maxSchedules, null, true);
    }

    public static List<Map<Course, List<Section>>> generateNonConflictingSchedules(List<Course> courses, int maxSchedules, SchedulePreferences preferences) {
        return generateNonConflictingSchedules(courses, maxSchedules, preferences, true);
    }

    public static List<Map<Course, List<Section>>> generateNonConflictingSchedules(List<Course> courses, int maxSchedules, SchedulePreferences preferences, boolean uniqueSchedulesOnly) {
        maxSchedules = Math.min(maxSchedules, ABSOLUTE_MAX_SCHEDULES);

        // Pre-process courses to ensure recitation requirements are met
        List<Course> validCourses = validateAndFilterCourses(courses);

        if (validCourses.isEmpty()) {
            return new ArrayList<>(); // Return empty if no valid courses
        }

        // Adjust generation multiplier based on whether we're deduplicating
        // If deduplicating, generate 3x to have options after removing duplicates
        // If not deduplicating, just generate the requested amount
        int generationTarget = uniqueSchedulesOnly ? maxSchedules * 3 : maxSchedules;

        List<Map<Course, List<Section>>> schedules = new ArrayList<>();
        generateSchedulesRecursive(new HashMap<>(), new ArrayList<>(validCourses), schedules, generationTarget, preferences);

        // Conditionally deduplicate schedules that differ only by location/instructor
        List<Map<Course, List<Section>>> finalSchedules = schedules;
        if (uniqueSchedulesOnly) {
            finalSchedules = deduplicateSchedules(schedules);
            logger.log(Level.INFO, "Deduplication: " + schedules.size() + " schedules reduced to " + finalSchedules.size() + " unique schedules");
        } else {
            logger.log(Level.INFO, "Deduplication disabled: showing all " + schedules.size() + " schedule variations");
        }

        // Sort by score (including preferences) and limit to maxSchedules
        finalSchedules.sort((s1, s2) -> calculateScheduleScore(s2, preferences) - calculateScheduleScore(s1, preferences));
        return finalSchedules.subList(0, Math.min(finalSchedules.size(), maxSchedules));
    }

    /**
     * Removes duplicate schedules that differ only by location or instructor
     * Keeps the first occurrence (highest scoring) of each unique day/time pattern
     */
    private static List<Map<Course, List<Section>>> deduplicateSchedules(List<Map<Course, List<Section>>> schedules) {
        Map<String, Map<Course, List<Section>>> uniqueSchedulesMap = new LinkedHashMap<>();

        for (Map<Course, List<Section>> schedule : schedules) {
            String scheduleSignature = generateScheduleSignature(schedule);

            // Only keep the first schedule with this signature
            if (!uniqueSchedulesMap.containsKey(scheduleSignature)) {
                uniqueSchedulesMap.put(scheduleSignature, schedule);
            }
        }

        return new ArrayList<>(uniqueSchedulesMap.values());
    }

    /**
     * Generate a unique signature for a schedule based on courseId, days, times, and format
     * Excludes section number, location, and instructor to identify duplicates
     */
    private static String generateScheduleSignature(Map<Course, List<Section>> schedule) {
        StringBuilder signature = new StringBuilder();

        // Sort courses by courseId for consistent signatures
        List<Course> sortedCourses = new ArrayList<>(schedule.keySet());
        sortedCourses.sort(Comparator.comparing(Course::getCourseId));

        for (Course course : sortedCourses) {
            signature.append(course.getCourseId()).append(":");

            List<Section> sections = schedule.get(course);
            // Sort sections by time/day for consistency (NOT by section number)
            sections.sort((s1, s2) -> {
                int dayCompare = s1.getDaysOfTheWeek().compareTo(s2.getDaysOfTheWeek());
                if (dayCompare != 0) return dayCompare;
                int timeCompare = s1.getTimeStart().compareTo(s2.getTimeStart());
                if (timeCompare != 0) return timeCompare;
                return s1.getTimeEnd().compareTo(s2.getTimeEnd());
            });

            for (Section section : sections) {
                // Include format, days, and times - but NOT section number, location, or instructor
                String format = section.getInstructionalFormat();
                signature.append(format != null ? format : "").append("-");
                signature.append(section.getDaysOfTheWeek()).append("-");
                signature.append(section.getTimeStart()).append("-");
                signature.append(section.getTimeEnd()).append("|");
            }
            signature.append(";");
        }

        return signature.toString();
    }

    /**
     * Validates courses and ensures those requiring recitations have them available
     */
    private static List<Course> validateAndFilterCourses(List<Course> courses) {
        List<Course> validCourses = new ArrayList<>();
        
        for (Course course : courses) {
            boolean hasPrimary = false;
            boolean hasSecondary = false;
            
            // Check what types of sections are available
            for (Section section : course.getSections()) {
                if (isSecondarySection(section)) {
                    hasSecondary = true;
                } else {
                    hasPrimary = true;
                }
            }
            
            // Only include courses that have at least one section type
            if (hasPrimary || hasSecondary) {
                validCourses.add(course);
                logger.log(Level.INFO, "Course " + course.getCourseId() + 
                          " included with primary=" + hasPrimary + ", secondary=" + hasSecondary);
            } else {
                logger.log(Level.INFO, "Excluding course " + course.getCourseId() + " - no valid sections found");
            }
        }
        
        return validCourses;
    }

    private static void generateSchedulesRecursive(
            Map<Course, List<Section>> currentSchedule,
            List<Course> remainingCourses,
            List<Map<Course, List<Section>>> schedules,
            int maxSchedules,
            SchedulePreferences preferences) {
        
        if (schedules.size() >= maxSchedules) {
            return;
        }

        if (remainingCourses.isEmpty()) {
            // Validate that the schedule meets all requirements before adding
            if (isValidCompleteSchedule(currentSchedule)) {
                logger.log(Level.INFO, "Adding valid schedule with " + currentSchedule.size() + " courses");
                schedules.add(new HashMap<>(currentSchedule));
            } else {
                logger.log(Level.WARNING, "Schedule failed validation - not adding to results");
            }
            return;
        }

        Course course = remainingCourses.get(0);
        List<Course> nextRemainingCourses = remainingCourses.subList(1, remainingCourses.size());

        // Generate all valid section combinations for courses with recitation requirements
        List<List<Section>> sectionCombinations = generateSectionCombinations(course);
        logger.log(Level.INFO, "Course " + course.getCourseId() + " has " + sectionCombinations.size() + " section combinations");

        for (List<Section> sectionCombo : sectionCombinations) {
            Map<Course, List<Section>> newSchedule = new HashMap<>(currentSchedule);

            // Add the section combination for this course
            newSchedule.put(course, new ArrayList<>(sectionCombo));

            // Check if this combination conflicts with existing schedule
            if (!hasConflicts(newSchedule)) {
                generateSchedulesRecursive(newSchedule, nextRemainingCourses, schedules, maxSchedules, preferences);
            }
        }
    }

    /**
     * Generates valid section combinations for a course (handles courses with multiple required sections)
     * Improved to handle Discussion sections and use section numbering patterns
     */
    private static List<List<Section>> generateSectionCombinations(Course course) {
        List<List<Section>> combinations = new ArrayList<>();
        
        List<Section> primarySections = new ArrayList<>();  // Main lectures (numeric sections: 1, 2, 3, etc.)
        List<Section> secondarySections = new ArrayList<>(); // Recitations/Discussions/Labs (letter sections: A, B, C, etc.)
        
        // Categorize sections based on multiple criteria
        for (Section section : course.getSections()) {
            if (isSecondarySection(section)) {
                secondarySections.add(section);
            } else {
                primarySections.add(section);
            }
        }
        
        logger.log(Level.FINE, "Course " + course.getCourseId() + 
                   " has " + primarySections.size() + " primary sections and " + 
                   secondarySections.size() + " secondary sections");
        
        // Generate combinations based on what's available
        if (!primarySections.isEmpty() && !secondarySections.isEmpty()) {
            // Course has both primary and secondary sections - need one of each
            for (Section primary : primarySections) {
                for (Section secondary : secondarySections) {
                    // Check if sections are compatible with course context for lab-lecture pairing
                    if (areSectionsCompatible(primary, secondary, course)) {
                        combinations.add(Arrays.asList(primary, secondary));
                    }
                }
            }
            
            // If no compatible combinations found, allow any pairing
            if (combinations.isEmpty()) {
                logger.log(Level.WARNING, "No compatible section pairs found for " + course.getCourseId() + 
                           ", allowing any combination");
                for (Section primary : primarySections) {
                    for (Section secondary : secondarySections) {
                        combinations.add(Arrays.asList(primary, secondary));
                    }
                }
            }
        } else if (!primarySections.isEmpty()) {
            // Only primary sections (lecture-only course)
            for (Section primary : primarySections) {
                combinations.add(Arrays.asList(primary));
            }
        } else if (!secondarySections.isEmpty()) {
            // Only secondary sections (unusual, but possible for some courses)
            for (Section secondary : secondarySections) {
                combinations.add(Arrays.asList(secondary));
            }
        }
        
        // If no combinations were created, add all sections individually as fallback
        if (combinations.isEmpty()) {
            logger.log(Level.WARNING, "No valid combinations for " + course.getCourseId() + 
                       ", adding sections individually");
            for (Section section : course.getSections()) {
                combinations.add(Arrays.asList(section));
            }
        }
        
        return combinations;
    }

    /**
     * Helper method to determine if a section is secondary (recitation/discussion/lab)
     * Updated to handle more section types including Discussion
     */
    private static boolean isSecondarySection(Section section) {
        String sectionNumber = section.getSectionNumber();
        String format = section.getInstructionalFormat();
        
        // Method 1: Check instructional format
        if (format != null) {
            String formatLower = format.toLowerCase();
            // Check for various secondary section types
            if (formatLower.contains("recitation") || 
                formatLower.contains("discussion") || 
                formatLower.contains("laboratory") || 
                formatLower.contains("lab") ||
                formatLower.contains("quiz") ||
                formatLower.contains("workshop") ||
                formatLower.contains("tutorial") ||
                formatLower.contains("studio") ||
                formatLower.contains("seminar")) {
                return true;
            }
            
            // Explicitly marked as lecture means it's primary
            if (format.equalsIgnoreCase("Lecture")) {
                return false;
            }
        }
        
        // Method 2: Check section number pattern
        // Letter sections (A, B, C, etc.) are typically secondary
        if (sectionNumber != null && !sectionNumber.isEmpty()) {
            char firstChar = sectionNumber.charAt(0);
            if (Character.isLetter(firstChar)) {
                return true;
            }
        }
        
        // Method 3: Check for "Arranged" format which often indicates discussion/lab
        if (format != null && format.toLowerCase().contains("arranged")) {
            return true;
        }
        
        return false;
    }

    /**
     * Check if a primary section and secondary section are compatible
     * Implements specific lab-lecture pairing logic:
     * - For LAB sections: match with lectures only when instructors match (if multiple lab instructors)
     * - For other sections (discussions, studios, etc.): independent sections
     */
    private static boolean areSectionsCompatible(Section primary, Section secondary, Course course) {
        String primaryFormat = primary.getInstructionalFormat();
        String secondaryFormat = secondary.getInstructionalFormat();
        
        // Check if this is specifically a lab-lecture pairing (only "Lab" format gets special treatment)
        boolean isLectureLab = (primaryFormat != null && primaryFormat.equalsIgnoreCase("Lecture")) &&
                               (secondaryFormat != null && secondaryFormat.equalsIgnoreCase("Lab"));
        
        if (isLectureLab) {
            return handleLabLecturePairing(primary, secondary, course);
        }
        
        // For all other section types (discussions, studios, research, experiential, combination, etc.), use flexible pairing
        return handleOtherSectionPairing(primary, secondary);
    }
    
    /**
     * Handle lab-lecture pairing with specific instructor matching logic
     */
    private static boolean handleLabLecturePairing(Section lecture, Section lab, Course course) {
        
        // Get all lab sections and their unique instructors
        Set<String> labInstructors = new HashSet<>();
        for (Section section : course.getSections()) {
            if (isLabSection(section) && section.getInstructor() != null && !section.getInstructor().isEmpty()) {
                labInstructors.add(section.getInstructor());
            }
        }
        
        // If multiple lab instructors, require instructor match
        if (labInstructors.size() > 1) {
            return lecture.getInstructor() != null && lecture.getInstructor().equals(lab.getInstructor());
        }
        
        // If single lab instructor (or no instructor info), allow pairing with any lecture
        return true;
    }
    
    /**
     * Handle pairing for non-lab sections (discussions, studios, recitations, etc.)
     * These are treated as independent sections with flexible pairing
     */
    private static boolean handleOtherSectionPairing(Section primary, Section secondary) {
        // If sections share the same instructor, they're likely linked
        if (primary.getInstructor() != null && primary.getInstructor().equals(secondary.getInstructor())) {
            return true;
        }
        
        // Check if section numbers follow a pattern (e.g., Lecture 1 -> Recitation 1A, 1B, 1C)
        String primaryNum = primary.getSectionNumber();
        String secondaryNum = secondary.getSectionNumber();
        
        if (primaryNum != null && secondaryNum != null) {
            // If secondary starts with primary number (e.g., "1" and "1A")
            if (secondaryNum.startsWith(primaryNum)) {
                return true;
            }
            
            // If they're in the same numeric range (for courses that don't follow strict patterns)
            try {
                // Extract numeric part of primary section
                String primaryNumeric = primaryNum.replaceAll("[^0-9]", "");
                if (!primaryNumeric.isEmpty()) {
                    int primaryInt = Integer.parseInt(primaryNumeric);
                    
                    // Check if secondary section number contains the same base number
                    String secondaryNumeric = secondaryNum.replaceAll("[^0-9]", "");
                    if (!secondaryNumeric.isEmpty()) {
                        int secondaryInt = Integer.parseInt(secondaryNumeric);
                        // They're compatible if in the same "group" (e.g., 1-3 goes with A-C)
                        if (primaryInt == secondaryInt) {
                            return true;
                        }
                    }
                }
            } catch (NumberFormatException e) {
                // Ignore parsing errors, continue with other checks
            }
        }
        
        // For non-lab sections, allow flexible pairing
        return true;
    }
    
    /**
     * Check if a section is specifically a lab section (only exact "Lab" format)
     */
    private static boolean isLabSection(Section section) {
        String format = section.getInstructionalFormat();
        return format != null && format.equalsIgnoreCase("Lab");
    }
    

    /**
     * Validates that a complete schedule meets all requirements
     */
    private static boolean isValidCompleteSchedule(Map<Course, List<Section>> schedule) {
        for (Map.Entry<Course, List<Section>> entry : schedule.entrySet()) {
            Course course = entry.getKey();
            List<Section> scheduledSections = entry.getValue();
            
            // Re-categorize the course's sections to check requirements
            boolean courseHasPrimary = false;
            boolean courseHasSecondary = false;
            
            for (Section section : course.getSections()) {
                if (isSecondarySection(section)) {
                    courseHasSecondary = true;
                } else {
                    courseHasPrimary = true;
                }
            }
            
            // If course requires both types, check that both are scheduled
            if (courseHasPrimary && courseHasSecondary) {
                boolean scheduledPrimary = false;
                boolean scheduledSecondary = false;
                
                for (Section section : scheduledSections) {
                    if (isSecondarySection(section)) {
                        scheduledSecondary = true;
                    } else {
                        scheduledPrimary = true;
                    }
                }
                
                if (!scheduledPrimary || !scheduledSecondary) {
                    logger.log(Level.FINE, "Course " + course.getCourseId() + 
                              " missing required section type. Has primary: " + scheduledPrimary + 
                              ", Has secondary: " + scheduledSecondary);
                    return false;
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
        // Skip conflict check if either section is online, N/A, or has TBD time
        if (section1.getTimeStart().equals("N/A") || section2.getTimeStart().equals("N/A") ||
            section1.getTimeStart().equals("TBD") || section2.getTimeStart().equals("TBD") ||
            section1.getTimeStart().equals("Online") || section2.getTimeStart().equals("Online")) {
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
        if (time == null || ONLINE_TIME.equals(time) || "TBD".equals(time) || "Online".equals(time)) {
            return 0;
        }
        
        try {
            String[] parts = time.trim().split(" ");
            if (parts.length != 2) {
                logger.log(Level.WARNING, "Invalid time format: " + time);
                return 0;
            }
            
            String[] timeParts = parts[0].split(":");
            if (timeParts.length != 2) {
                logger.log(Level.WARNING, "Invalid time parts: " + parts[0]);
                return 0;
            }
            
            int hours = Integer.parseInt(timeParts[0]);
            int minutes = Integer.parseInt(timeParts[1]);
            
            // Validate hour and minute ranges
            if (hours < 1 || hours > 12 || minutes < 0 || minutes > 59) {
                logger.log(Level.WARNING, "Invalid hour/minute values: " + hours + ":" + minutes);
                return 0;
            }
            
            String period = parts[1].toUpperCase();
            if ("PM".equals(period) && hours != 12) {
                hours += 12;
            } else if ("AM".equals(period) && hours == 12) {
                hours = 0;
            } else if (!"AM".equals(period) && !"PM".equals(period)) {
                logger.log(Level.WARNING, "Invalid AM/PM indicator: " + parts[1]);
                return 0;
            }
            
            return hours * 60 + minutes;
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Number parsing error for time: " + time, e);
            return 0;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Unexpected error parsing time: " + time, e);
            return 0;
        }
    }

    private static int calculateScheduleScore(Map<Course, List<Section>> schedule) {
        return calculateScheduleScore(schedule, null);
    }

    private static int calculateScheduleScore(Map<Course, List<Section>> schedule, SchedulePreferences preferences) {
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

        // Bonus for having required recitations/labs/discussions
        score += validateRecitationRequirements(schedule) ? 25 : -50;

        // Apply preference-based scoring
        if (preferences != null && !preferences.hasNoPreferences()) {
            score += calculatePreferenceScore(schedule, preferences);
        }

        return score;
    }

    /**
     * Validates that all recitation requirements are met in the schedule
     */
    private static boolean validateRecitationRequirements(Map<Course, List<Section>> schedule) {
        for (Map.Entry<Course, List<Section>> entry : schedule.entrySet()) {
            Course course = entry.getKey();
            List<Section> scheduledSections = entry.getValue();
            
            boolean courseHasPrimary = false;
            boolean courseHasSecondary = false;
            
            // Check what the course offers
            for (Section section : course.getSections()) {
                if (isSecondarySection(section)) {
                    courseHasSecondary = true;
                } else {
                    courseHasPrimary = true;
                }
            }
            
            // If course has both types, verify both are scheduled
            if (courseHasPrimary && courseHasSecondary) {
                boolean scheduledPrimary = false;
                boolean scheduledSecondary = false;
                
                for (Section section : scheduledSections) {
                    if (isSecondarySection(section)) {
                        scheduledSecondary = true;
                    } else {
                        scheduledPrimary = true;
                    }
                }
                
                if (!scheduledPrimary || !scheduledSecondary) {
                    return false;
                }
            }
        }
        
        return true;
    }

    private static boolean isReasonableStartTime(String time) {
        if (time.equals("N/A") || time.equals("TBD") || time.equals("Online")) return false;
        
        try {
            int minutes = convertTimeToMinutes(time);
            int hour = minutes / 60;
            return (hour >= REASONABLE_START_HOUR_MIN && hour <= REASONABLE_START_HOUR_MAX);
        } catch (Exception e) {
            return false;
        }
    }

    private static int getTimeDifference(String time1, String time2) {
        if (time1.equals("N/A") || time2.equals("N/A") ||
            time1.equals("TBD") || time2.equals("TBD") ||
            time1.equals("Online") || time2.equals("Online")) return 0;
        
        int minutes1 = convertTimeToMinutes(time1);
        int minutes2 = convertTimeToMinutes(time2);
        return Math.abs(minutes2 - minutes1);
    }

    /**
     * Calculates score boost based on user preferences
     */
    private static int calculatePreferenceScore(Map<Course, List<Section>> schedule, SchedulePreferences preferences) {
        int preferenceScore = 0;
        List<Section> sections = new ArrayList<>();
        
        for (List<Section> sectionList : schedule.values()) {
            sections.addAll(sectionList);
        }

        // Filter out online sections for day/time preference calculations
        List<Section> inPersonSections = sections.stream()
                .filter(s -> !s.getTimeStart().equals("N/A"))
                .collect(java.util.stream.Collectors.toList());

        // Score based on preferred days
        if (preferences.getPreferredDays() != null && !preferences.getPreferredDays().isEmpty()) {
            for (Section section : inPersonSections) {
                String[] sectionDays = section.getDaysOfTheWeek().split(",");
                for (String day : sectionDays) {
                    if (preferences.isDayPreferred(day.trim())) {
                        preferenceScore += 15; // Bonus for preferred day
                    } else {
                        preferenceScore -= 5; // Small penalty for non-preferred day
                    }
                }
            }
        }

        // Score based on time preferences
        if (!preferences.getTimePreference().isEmpty()) {
            for (Section section : inPersonSections) {
                if (matchesTimePreference(section, preferences)) {
                    preferenceScore += 20; // Bonus for preferred time
                } else {
                    preferenceScore -= 10; // Penalty for non-preferred time
                }
            }
        }

        // Score based on gap preferences
        if (!preferences.getGapPreference().equals("none")) {
            preferenceScore += scoreGapPreference(inPersonSections, preferences);
        }

        // Score based on schedule style preference
        if (!preferences.getScheduleStyle().isEmpty()) {
            preferenceScore += scoreScheduleStyle(inPersonSections, preferences);
        }

        return preferenceScore;
    }

    private static boolean matchesTimePreference(Section section, SchedulePreferences preferences) {
        int startMinutes = convertTimeToMinutes(section.getTimeStart());
        int startHour = startMinutes / 60;

        switch (preferences.getTimePreference().toLowerCase()) {
            case "morning":
                return startHour >= 8 && startHour < 12;
            case "afternoon":
                return startHour >= 12 && startHour < 17;
            case "evening":
                return startHour >= 17 && startHour < 21;
            default:
                return true;
        }
    }

    private static int scoreGapPreference(List<Section> sections, SchedulePreferences preferences) {
        int gapScore = 0;
        
        // Sort sections by day and time
        sections.sort((s1, s2) -> {
            int dayCompare = s1.getDaysOfTheWeek().compareTo(s2.getDaysOfTheWeek());
            if (dayCompare != 0) return dayCompare;
            return Integer.compare(convertTimeToMinutes(s1.getTimeStart()), convertTimeToMinutes(s2.getTimeStart()));
        });

        for (int i = 0; i < sections.size() - 1; i++) {
            Section current = sections.get(i);
            Section next = sections.get(i + 1);
            
            // Only check gaps on the same day
            if (shareCommonDay(current, next)) {
                int gap = getTimeDifference(current.getTimeEnd(), next.getTimeStart());
                int minGap = preferences.getMinGapMinutes();
                int maxGap = preferences.getMaxGapMinutes();
                
                if (gap >= minGap && gap <= maxGap) {
                    gapScore += 15; // Matches preferred gap
                } else {
                    gapScore -= 5; // Doesn't match preferred gap
                }
            }
        }
        
        return gapScore;
    }

    private static int scoreScheduleStyle(List<Section> sections, SchedulePreferences preferences) {
        Map<String, Integer> classesPerDay = new HashMap<>();
        for (Section section : sections) {
            String daysStr = section.getDaysOfTheWeek();
            
            // Skip online classes and classes without specific days from schedule style scoring
            if (daysStr == null || daysStr.equals("N/A") || daysStr.equals("Online") || daysStr.trim().isEmpty()) {
                continue;
            }
            
            String[] days = daysStr.split(",");
            for (String day : days) {
                String dayTrimmed = day.trim();
                // Only count actual weekdays, not online/special values
                if (dayTrimmed.equals("Monday") || dayTrimmed.equals("Tuesday") || 
                    dayTrimmed.equals("Wednesday") || dayTrimmed.equals("Thursday") || 
                    dayTrimmed.equals("Friday")) {
                    classesPerDay.merge(dayTrimmed, 1, Integer::sum);
                }
            }
        }

        int daysWithClasses = classesPerDay.size();
        int maxClassesInDay = classesPerDay.values().stream().mapToInt(i -> i).max().orElse(0);

        // If no in-person classes, don't apply style preference
        if (daysWithClasses == 0) {
            return 0;
        }

        if (preferences.isCompactStylePreferred()) {
            // Prefer fewer days with more classes per day
            return (5 - daysWithClasses) * 10 + maxClassesInDay * 5;
        } else if (preferences.isSpreadStylePreferred()) {
            // Prefer more days with fewer classes per day
            return daysWithClasses * 10 - maxClassesInDay * 5;
        }
        
        return 0;
    }

    private static boolean shareCommonDay(Section section1, Section section2) {
        String[] days1 = section1.getDaysOfTheWeek().split(",");
        String[] days2 = section2.getDaysOfTheWeek().split(",");
        
        for (String day1 : days1) {
            for (String day2 : days2) {
                if (day1.trim().equals(day2.trim())) {
                    return true;
                }
            }
        }
        return false;
    }
}