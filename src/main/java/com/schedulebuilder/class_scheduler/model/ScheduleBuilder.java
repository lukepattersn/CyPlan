package com.schedulebuilder.class_scheduler.model;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class ScheduleBuilder {

    public static List<Pair<Integer, String>> buildClosestNonOverlappingSchedule(List<Course> courses) {
        if (courses == null || courses.isEmpty()) {
            return new ArrayList<>();
        }

        List<Pair<Integer, String>> result = new ArrayList<>();
        Random random = new Random();

        // Time formatter to convert between string and LocalTime
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

        // Step 1: Randomly select the first course
        int firstCourseIndex = random.nextInt(courses.size());
        Course firstCourse = courses.get(firstCourseIndex);
        List<Section> firstCourseSections = firstCourse.getSections();

        if (firstCourseSections.isEmpty()) {
            return result; // Return an empty result if the first course has no sections
        }

        // Pick a random section from the first course
        Section firstSection = firstCourseSections.get(random.nextInt(firstCourseSections.size()));
        result.add(new Pair<>(firstCourseIndex, firstSection.getSectionNumber()));

        // Keep track of courses that have been processed
        Set<Integer> processedCourseIndices = new HashSet<>();
        processedCourseIndices.add(firstCourseIndex);

        // Step 2: Find closest non-overlapping sections for the remaining courses
        Section previousSection = firstSection;

        for (int i = 0; i < courses.size(); i++) {
            // Skip the already processed first course
            if (processedCourseIndices.contains(i)) {
                continue;
            }

            Course currentCourse = courses.get(i);
            List<Section> currentSections = currentCourse.getSections();

            if (currentSections.isEmpty()) {
                continue; // Skip courses with no sections
            }

            Section closestSection = null;
            long minTimeDifference = Long.MAX_VALUE;

            LocalTime prevEndTime = LocalTime.parse(previousSection.getTimeEnd(), timeFormatter);

            for (Section section : currentSections) {
                LocalTime startTime = LocalTime.parse(section.getTimeStart(), timeFormatter);
                LocalTime endTime = LocalTime.parse(section.getTimeEnd(), timeFormatter);

                // Check if the current section starts after the previous one ends, or ends before it starts
                if (startTime.isAfter(prevEndTime) || endTime.isBefore(prevEndTime)) {
                    long timeDifference = Math.abs(prevEndTime.toSecondOfDay() - startTime.toSecondOfDay());
                    if (timeDifference < minTimeDifference) {
                        minTimeDifference = timeDifference;
                        closestSection = section;
                    }
                }
            }

            if (closestSection != null) {
                result.add(new Pair<>(i, closestSection.getSectionNumber()));
                previousSection = closestSection; // Update the previous section
                processedCourseIndices.add(i); // Mark this course as processed
            }
        }

        return result;
    }

//    public static void main(String[] args) {
//        // Example usage
//        Course course1 = new Course("CS101", "Intro to Computer Science");
//        course1.addSection(new Section("MWF", 30, "Dr. Smith", "CS101", "9:00 AM", "10:00 AM", "A"));
//        course1.addSection(new Section("MWF", 25, "Dr. Jones", "CS101", "10:30 AM", "11:30 AM", "B"));
//
//        Course course2 = new Course("MATH101", "Calculus I");
//        course2.addSection(new Section("TR", 20, "Dr. Taylor", "MATH101", "12:00 PM", "1:30 PM", "C"));
//        course2.addSection(new Section("TR", 18, "Dr. Brown", "MATH101", "11:00 AM", "12:30 PM", "D"));
//
//        Course course3 = new Course("ENG101", "English Literature");
//        course3.addSection(new Section("MW", 40, "Dr. Green", "ENG101", "2:00 PM", "3:30 PM", "E"));
//        course3.addSection(new Section("MW", 35, "Dr. White", "ENG101", "1:00 PM", "2:30 PM", "F"));
//
//        List<Course> courses = new ArrayList<>();
//        courses.add(course1);
//        courses.add(course2);
//        courses.add(course3);
//
//        List<Pair<Integer, String>> schedule = buildClosestNonOverlappingSchedule(courses);
//
//        System.out.println("Generated Schedule (Course Index, Section Number):");
//        for (Pair<Integer, String> pair : schedule) {
//            System.out.println("Course Index: " + pair.getKey() + ", Section Number: " + pair.getValue());
//        }
//    }
}