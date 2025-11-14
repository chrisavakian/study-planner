package com.studyplanner.tests;

import com.studyplanner.models.Session;
import com.studyplanner.models.Task;

import java.time.LocalDateTime;
import java.util.List;

public class TaskTest {

    public static void main(String[] args) {
        TaskTest test = new TaskTest();
        System.out.println("Running Task tests...");
        
        test.test_splitIntoSessions_forMultiHourTask();
        test.test_splitIntoSessions_forSingleHourTask();
        test.test_updateEstimate_withPositiveEffort();
        test.test_updateEstimate_withZeroEffort();
        test.test_updateEstimate_withNegativeEffort();
        
        System.out.println("All Task tests completed!");
    }

    void test_splitIntoSessions_forMultiHourTask() {
        Task task = new Task("Test Task", LocalDateTime.now().plusDays(5), 4);
        List<Session> sessions = task.splitIntoSessions();
        
        // For a 4-hour task, we expect 2 sessions of 2 hours each
        if (sessions.size() == 2) {
            boolean allCorrectDuration = true;
            for (Session session : sessions) {
                long duration = java.time.Duration.between(session.getStartTime(), session.getEndTime()).toHours();
                if (duration != 2) {
                    allCorrectDuration = false;
                    break;
                }
            }
            if (allCorrectDuration) {
                System.out.println("PASS: test_splitIntoSessions_forMultiHourTask");
            } else {
                System.out.println("FAIL: test_splitIntoSessions_forMultiHourTask - incorrect duration");
            }
        } else {
            System.out.println("FAIL: test_splitIntoSessions_forMultiHourTask - incorrect session count");
        }
    }

    void test_splitIntoSessions_forSingleHourTask() {
        Task shortTask = new Task("Short Task", LocalDateTime.now().plusDays(3), 1);
        List<Session> sessions = shortTask.splitIntoSessions();
        
        // For a 1-hour task, we expect 1 session of 1 hour
        if (sessions.size() == 1) {
            Session session = sessions.get(0);
            long duration = java.time.Duration.between(session.getStartTime(), session.getEndTime()).toHours();
            if (duration == 1) {
                System.out.println("PASS: test_splitIntoSessions_forSingleHourTask");
            } else {
                System.out.println("FAIL: test_splitIntoSessions_forSingleHourTask - incorrect duration");
            }
        } else {
            System.out.println("FAIL: test_splitIntoSessions_forSingleHourTask - incorrect session count");
        }
    }

    void test_updateEstimate_withPositiveEffort() {
        Task task = new Task("Test Task", LocalDateTime.now().plusDays(5), 4);
        task.updateEstimate(6);
        
        if (task.getEffort() == 6) {
            System.out.println("PASS: test_updateEstimate_withPositiveEffort");
        } else {
            System.out.println("FAIL: test_updateEstimate_withPositiveEffort");
        }
    }

    void test_updateEstimate_withZeroEffort() {
        Task task = new Task("Test Task", LocalDateTime.now().plusDays(5), 4);
        boolean exceptionThrown = false;
        try {
            task.updateEstimate(0);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        
        if (!exceptionThrown && task.getEffort() == 0) {
            System.out.println("PASS: test_updateEstimate_withZeroEffort");
        } else {
            System.out.println("FAIL: test_updateEstimate_withZeroEffort");
        }
    }

    void test_updateEstimate_withNegativeEffort() {
        Task task = new Task("Test Task", LocalDateTime.now().plusDays(5), 4);
        boolean exceptionThrown = false;
        try {
            task.updateEstimate(-1);
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        }
        
        if (exceptionThrown) {
            System.out.println("PASS: test_updateEstimate_withNegativeEffort");
        } else {
            System.out.println("FAIL: test_updateEstimate_withNegativeEffort");
        }
    }
}