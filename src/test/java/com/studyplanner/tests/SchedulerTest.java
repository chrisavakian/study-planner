package com.studyplanner.tests;

import com.studyplanner.models.*;
import com.studyplanner.scheduler.Scheduler;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class SchedulerTest {

    public static void main(String[] args) {
        SchedulerTest test = new SchedulerTest();
        System.out.println("Running Scheduler tests...");
        
        test.test_generateSchedule_withSufficientAvailability();
        test.test_generateSchedule_withEmptyTaskList();
        test.test_generateSchedule_withNullInputs();
        test.test_findOverlap_whenConflictExists();
        test.test_findOverlap_whenNoConflictExists();
        test.test_findOverlap_withDifferentDays();
        
        System.out.println("All Scheduler tests completed!");
    }

    void test_generateSchedule_withSufficientAvailability() {
        Scheduler scheduler = new Scheduler();
        List<Availability> availability = new ArrayList<>();
        availability.add(new Availability(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(12, 0)));
        availability.add(new Availability(DayOfWeek.WEDNESDAY, LocalTime.of(14, 0), LocalTime.of(17, 0)));
        availability.add(new Availability(DayOfWeek.FRIDAY, LocalTime.of(10, 0), LocalTime.of(13, 0)));

        List<Task> tasks = new ArrayList<>();
        tasks.add(new Task("Short Task", LocalDateTime.now().plusDays(2), 1));
        tasks.add(new Task("Medium Task", LocalDateTime.now().plusDays(3), 2));

        Schedule schedule = scheduler.generateSchedule(tasks, availability);
        
        if (schedule != null && schedule.getWeekStart() != null) {
            System.out.println("PASS: test_generateSchedule_withSufficientAvailability");
        } else {
            System.out.println("FAIL: test_generateSchedule_withSufficientAvailability");
        }
    }

    void test_generateSchedule_withEmptyTaskList() {
        Scheduler scheduler = new Scheduler();
        List<Availability> availability = new ArrayList<>();
        availability.add(new Availability(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(12, 0)));
        availability.add(new Availability(DayOfWeek.WEDNESDAY, LocalTime.of(14, 0), LocalTime.of(17, 0)));
        availability.add(new Availability(DayOfWeek.FRIDAY, LocalTime.of(10, 0), LocalTime.of(13, 0)));

        List<Task> tasks = new ArrayList<>();
        Schedule schedule = scheduler.generateSchedule(tasks, availability);
        
        if (schedule != null && schedule.getSessions().isEmpty()) {
            System.out.println("PASS: test_generateSchedule_withEmptyTaskList");
        } else {
            System.out.println("FAIL: test_generateSchedule_withEmptyTaskList");
        }
    }

    void test_generateSchedule_withNullInputs() {
        Scheduler scheduler = new Scheduler();
        
        Schedule schedule1 = scheduler.generateSchedule(null, null);
        Schedule schedule2 = scheduler.generateSchedule(new ArrayList<>(), null);
        
        if (schedule1 != null && schedule1.getSessions().isEmpty() &&
            schedule2 != null && schedule2.getSessions().isEmpty()) {
            System.out.println("PASS: test_generateSchedule_withNullInputs");
        } else {
            System.out.println("FAIL: test_generateSchedule_withNullInputs");
        }
    }

    void test_findOverlap_whenConflictExists() {
        Scheduler scheduler = new Scheduler();
        java.time.LocalDate weekStart = java.time.LocalDate.now();
        while (weekStart.getDayOfWeek() != DayOfWeek.MONDAY) {
            weekStart = weekStart.minusDays(1);
        }
        Schedule schedule = new Schedule(weekStart);

        Session session1 = new Session(
            LocalDateTime.of(weekStart, LocalTime.of(10, 0)), 
            LocalDateTime.of(weekStart, LocalTime.of(11, 0))
        );
        Session session2 = new Session(
            LocalDateTime.of(weekStart, LocalTime.of(10, 30)), 
            LocalDateTime.of(weekStart, LocalTime.of(11, 30))
        );
        
        schedule.addSession(session1);

        boolean hasOverlap = scheduler.findOverlap(session2, schedule);
        if (hasOverlap) {
            System.out.println("PASS: test_findOverlap_whenConflictExists");
        } else {
            System.out.println("FAIL: test_findOverlap_whenConflictExists");
        }
    }

    void test_findOverlap_whenNoConflictExists() {
        Scheduler scheduler = new Scheduler();
        java.time.LocalDate weekStart = java.time.LocalDate.now();
        while (weekStart.getDayOfWeek() != DayOfWeek.MONDAY) {
            weekStart = weekStart.minusDays(1);
        }
        Schedule schedule = new Schedule(weekStart);

        Session session1 = new Session(
            LocalDateTime.of(weekStart, LocalTime.of(10, 0)), 
            LocalDateTime.of(weekStart, LocalTime.of(11, 0))
        );
        Session session2 = new Session(
            LocalDateTime.of(weekStart, LocalTime.of(11, 30)), 
            LocalDateTime.of(weekStart, LocalTime.of(12, 30))
        );
        
        schedule.addSession(session1);

        boolean hasOverlap = scheduler.findOverlap(session2, schedule);
        if (!hasOverlap) {
            System.out.println("PASS: test_findOverlap_whenNoConflictExists");
        } else {
            System.out.println("FAIL: test_findOverlap_whenNoConflictExists");
        }
    }

    void test_findOverlap_withDifferentDays() {
        Scheduler scheduler = new Scheduler();
        java.time.LocalDate weekStart = java.time.LocalDate.now();
        while (weekStart.getDayOfWeek() != DayOfWeek.MONDAY) {
            weekStart = weekStart.minusDays(1);
        }
        Schedule schedule = new Schedule(weekStart);

        Session session1 = new Session(
            LocalDateTime.of(weekStart, LocalTime.of(10, 0)), 
            LocalDateTime.of(weekStart, LocalTime.of(11, 0))
        );
        Session session2 = new Session(
            LocalDateTime.of(weekStart.plusDays(1), LocalTime.of(10, 0)), 
            LocalDateTime.of(weekStart.plusDays(1), LocalTime.of(11, 0))
        );
        
        schedule.addSession(session1);

        boolean hasOverlap = scheduler.findOverlap(session2, schedule);
        if (!hasOverlap) {
            System.out.println("PASS: test_findOverlap_withDifferentDays");
        } else {
            System.out.println("FAIL: test_findOverlap_withDifferentDays");
        }
    }
}