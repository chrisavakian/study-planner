package com.studyplanner.tests;

import com.studyplanner.models.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StudentTest {

    public static void main(String[] args) {
        StudentTest test = new StudentTest();
        System.out.println("Running Student tests...");
        
        test.test_addTask_withValidInputs();
        test.test_addTask_withEmptyTitle();
        test.test_addTask_withNullTitle();
        test.test_addTask_withPastDeadline();
        test.test_addTask_withNegativeEffort();
        test.test_setAvailability_withValidSchedule();
        test.test_setAvailability_withOverlappingTimes();
        test.test_viewSchedule_whenNoScheduleExists();
        test.test_markSessionComplete_forValidSession();
        test.test_markSessionComplete_forInvalidSession();
        test.test_markSessionComplete_forNullSchedule();
        
        System.out.println("All Student tests completed!");
    }

    void test_addTask_withValidInputs() {
        Student student = new Student("S001", "John Doe");
        Task task = student.addTask("Math Homework", LocalDateTime.now().plusDays(2), 3);
        
        if (student.getTaskList().size() == 1 &&
            "Math Homework".equals(task.getTitle()) &&
            task.getEffort() == 3) {
            System.out.println("PASS: test_addTask_withValidInputs");
        } else {
            System.out.println("FAIL: test_addTask_withValidInputs");
        }
    }

    void test_addTask_withEmptyTitle() {
        Student student = new Student("S001", "John Doe");
        boolean exceptionThrown = false;
        try {
            student.addTask("", LocalDateTime.now().plusDays(2), 3);
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        }
        
        if (exceptionThrown) {
            System.out.println("PASS: test_addTask_withEmptyTitle");
        } else {
            System.out.println("FAIL: test_addTask_withEmptyTitle");
        }
    }

    void test_addTask_withNullTitle() {
        Student student = new Student("S001", "John Doe");
        boolean exceptionThrown = false;
        try {
            student.addTask(null, LocalDateTime.now().plusDays(2), 3);
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        }
        
        if (exceptionThrown) {
            System.out.println("PASS: test_addTask_withNullTitle");
        } else {
            System.out.println("FAIL: test_addTask_withNullTitle");
        }
    }

    void test_addTask_withPastDeadline() {
        Student student = new Student("S001", "John Doe");
        boolean exceptionThrown = false;
        try {
            student.addTask("Past Task", LocalDateTime.now().minusDays(1), 2);
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        }
        
        if (exceptionThrown) {
            System.out.println("PASS: test_addTask_withPastDeadline");
        } else {
            System.out.println("FAIL: test_addTask_withPastDeadline");
        }
    }

    void test_addTask_withNegativeEffort() {
        Student student = new Student("S001", "John Doe");
        boolean exceptionThrown = false;
        try {
            student.addTask("Negative Effort Task", LocalDateTime.now().plusDays(2), -1);
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        }
        
        if (exceptionThrown) {
            System.out.println("PASS: test_addTask_withNegativeEffort");
        } else {
            System.out.println("FAIL: test_addTask_withNegativeEffort");
        }
    }

    void test_setAvailability_withValidSchedule() {
        Student student = new Student("S001", "John Doe");
        List<Availability> availability = new ArrayList<>();
        availability.add(new Availability(java.time.DayOfWeek.MONDAY, 
                                         java.time.LocalTime.of(9, 0), 
                                         java.time.LocalTime.of(12, 0)));
        availability.add(new Availability(java.time.DayOfWeek.WEDNESDAY, 
                                         java.time.LocalTime.of(14, 0), 
                                         java.time.LocalTime.of(17, 0)));

        boolean exceptionThrown = false;
        try {
            student.setAvailability(availability);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        
        if (!exceptionThrown && student.getAvailability().size() == 2) {
            System.out.println("PASS: test_setAvailability_withValidSchedule");
        } else {
            System.out.println("FAIL: test_setAvailability_withValidSchedule");
        }
    }

    void test_setAvailability_withOverlappingTimes() {
        Student student = new Student("S001", "John Doe");
        List<Availability> availability = new ArrayList<>();
        availability.add(new Availability(java.time.DayOfWeek.MONDAY, 
                                         java.time.LocalTime.of(9, 0), 
                                         java.time.LocalTime.of(12, 0)));
        availability.add(new Availability(java.time.DayOfWeek.MONDAY, 
                                         java.time.LocalTime.of(11, 0), 
                                         java.time.LocalTime.of(14, 0)));

        boolean exceptionThrown = false;
        try {
            student.setAvailability(availability);
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        }
        
        if (exceptionThrown) {
            System.out.println("PASS: test_setAvailability_withOverlappingTimes");
        } else {
            System.out.println("FAIL: test_setAvailability_withOverlappingTimes");
        }
    }

    void test_viewSchedule_whenNoScheduleExists() {
        Student student = new Student("S001", "John Doe");
        Schedule schedule = student.viewSchedule();
        
        if (schedule == null) {
            System.out.println("PASS: test_viewSchedule_whenNoScheduleExists");
        } else {
            System.out.println("FAIL: test_viewSchedule_whenNoScheduleExists");
        }
    }

    void test_markSessionComplete_forValidSession() {
        Student student = new Student("S001", "John Doe");
        // Create a schedule with a session first
        Schedule schedule = new Schedule(java.time.LocalDate.now());
        Session session = new Session(LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        schedule.addSession(session);
        student.setSchedule(schedule);

        boolean exceptionThrown = false;
        try {
            student.markSessionComplete(session);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        
        if (!exceptionThrown && session.isCompleted()) {
            System.out.println("PASS: test_markSessionComplete_forValidSession");
        } else {
            System.out.println("FAIL: test_markSessionComplete_forValidSession");
        }
    }

    void test_markSessionComplete_forInvalidSession() {
        Student student = new Student("S001", "John Doe");
        Schedule schedule = new Schedule(java.time.LocalDate.now());
        Session session = new Session(LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        schedule.addSession(session);
        student.setSchedule(schedule);

        Session otherSession = new Session(LocalDateTime.now(), LocalDateTime.now().plusHours(1));

        boolean exceptionThrown = false;
        try {
            student.markSessionComplete(otherSession);
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        }
        
        if (exceptionThrown) {
            System.out.println("PASS: test_markSessionComplete_forInvalidSession");
        } else {
            System.out.println("FAIL: test_markSessionComplete_forInvalidSession");
        }
    }

    void test_markSessionComplete_forNullSchedule() {
        Student student = new Student("S001", "John Doe");
        Session session = new Session(LocalDateTime.now(), LocalDateTime.now().plusHours(1));

        boolean exceptionThrown = false;
        try {
            student.markSessionComplete(session);
        } catch (IllegalStateException e) {
            exceptionThrown = true;
        }
        
        if (exceptionThrown) {
            System.out.println("PASS: test_markSessionComplete_forNullSchedule");
        } else {
            System.out.println("FAIL: test_markSessionComplete_forNullSchedule");
        }
    }
}