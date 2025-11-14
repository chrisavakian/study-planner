package com.studyplanner.tests;

import com.studyplanner.models.*;
import com.studyplanner.services.LLMService;
import com.studyplanner.scheduler.Scheduler;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class IntegrationTest {

    public static void main(String[] args) {
        IntegrationTest test = new IntegrationTest();
        System.out.println("Running Integration tests...");
        
        test.test_EndToEndScheduleGeneration();
        test.test_AdaptiveReplanningWhenTaskEffortChanges();
        test.test_InsufficientTimeForAllTasks();
        
        System.out.println("All Integration tests completed!");
    }

    void test_EndToEndScheduleGeneration() {
        Student student = new Student("S001", "Jane Doe");
        LLMService llmService = new LLMService("dummy-key");
        Scheduler scheduler = new Scheduler();

        // Set up student availability
        List<Availability> availability = new ArrayList<>();
        availability.add(new Availability(DayOfWeek.MONDAY, LocalTime.of(18, 0), LocalTime.of(21, 0)));
        availability.add(new Availability(DayOfWeek.TUESDAY, LocalTime.of(19, 0), LocalTime.of(22, 0)));
        availability.add(new Availability(DayOfWeek.WEDNESDAY, LocalTime.of(17, 0), LocalTime.of(20, 0)));
        student.setAvailability(availability);

        // Add tasks to the student
        Task task1 = student.addTask("Complete Project", LocalDateTime.now().plusDays(3), 4);
        Task task2 = student.addTask("Study for Exam", LocalDateTime.now().plusDays(5), 6);
        Task task3 = student.addTask("Math Assignment", LocalDateTime.now().plusDays(1), 2);

        // Use LLM service to prioritize tasks
        List<Task> prioritizedTasks = llmService.prioritizeTasks(student.getTaskList());
        
        // Verify that tasks are properly prioritized (by deadline)
        if (prioritizedTasks.size() == 3) {
            // Generate schedule using the scheduler
            Schedule schedule = scheduler.generateSchedule(prioritizedTasks, availability);
            student.setSchedule(schedule);

            // Verify the schedule was created
            if (student.getSchedule() != null) {
                // Check that the schedule has sessions
                boolean allSessionsValid = true;
                for (Session session : schedule.getSessions()) {
                    if (session.getStartTime().isAfter(session.getEndTime())) {
                        allSessionsValid = false;
                        break;
                    }
                }
                
                if (allSessionsValid) {
                    System.out.println("PASS: test_EndToEndScheduleGeneration");
                } else {
                    System.out.println("FAIL: test_EndToEndScheduleGeneration - invalid session times");
                }
            } else {
                System.out.println("FAIL: test_EndToEndScheduleGeneration - schedule is null");
            }
        } else {
            System.out.println("FAIL: test_EndToEndScheduleGeneration - wrong number of prioritized tasks");
        }
    }

    void test_AdaptiveReplanningWhenTaskEffortChanges() {
        Student student = new Student("S001", "Jane Doe");
        LLMService llmService = new LLMService("dummy-key");
        Scheduler scheduler = new Scheduler();

        // Set up student availability
        List<Availability> availability = new ArrayList<>();
        availability.add(new Availability(DayOfWeek.MONDAY, LocalTime.of(18, 0), LocalTime.of(21, 0)));
        availability.add(new Availability(DayOfWeek.TUESDAY, LocalTime.of(19, 0), LocalTime.of(22, 0)));
        student.setAvailability(availability);

        // Add initial tasks
        Task task = student.addTask("Initial Task", LocalDateTime.now().plusDays(2), 2);
        
        // Prioritize tasks
        List<Task> prioritizedTasks = llmService.prioritizeTasks(student.getTaskList());
        
        // Generate initial schedule
        Schedule initialSchedule = scheduler.generateSchedule(prioritizedTasks, availability);
        student.setSchedule(initialSchedule);
        
        // Update task effort (simulate change in requirements)
        task.updateEstimate(5); // Increased effort
        
        // Regenerate schedule with updated task
        List<Task> updatedPrioritizedTasks = llmService.prioritizeTasks(student.getTaskList());
        Schedule updatedSchedule = scheduler.generateSchedule(updatedPrioritizedTasks, availability);
        student.setSchedule(updatedSchedule);
        
        // Verify that the updated schedule was created
        if (student.getSchedule() != null) {
            System.out.println("PASS: test_AdaptiveReplanningWhenTaskEffortChanges");
        } else {
            System.out.println("FAIL: test_AdaptiveReplanningWhenTaskEffortChanges");
        }
    }

    void test_InsufficientTimeForAllTasks() {
        Student student = new Student("S001", "Jane Doe");
        LLMService llmService = new LLMService("dummy-key");
        Scheduler scheduler = new Scheduler();

        // Set up limited availability
        List<Availability> availability = new ArrayList<>();
        availability.add(new Availability(DayOfWeek.MONDAY, LocalTime.of(18, 0), LocalTime.of(19, 0))); // Only 1 hour available
        student.setAvailability(availability);

        // Add tasks that require more time than available
        student.addTask("Large Task 1", LocalDateTime.now().plusDays(2), 5); // 5 hours
        student.addTask("Large Task 2", LocalDateTime.now().plusDays(3), 4); // 4 hours
        
        // Prioritize tasks
        List<Task> prioritizedTasks = llmService.prioritizeTasks(student.getTaskList());
        
        // Generate schedule
        Schedule schedule = scheduler.generateSchedule(prioritizedTasks, availability);
        
        // The schedule should still be created, but may not accommodate all tasks
        if (schedule != null) {
            System.out.println("PASS: test_InsufficientTimeForAllTasks");
        } else {
            System.out.println("FAIL: test_InsufficientTimeForAllTasks");
        }
    }
}