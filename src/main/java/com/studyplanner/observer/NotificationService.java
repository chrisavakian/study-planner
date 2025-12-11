package com.studyplanner.observer;

import com.studyplanner.models.Session;
import com.studyplanner.models.Task;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Service that handles notifications for various study events.
 */
public class NotificationService implements StudyObserver {
    private static final long DAYS_TO_WARN = 2; // Warn 2 days before deadline

    @Override
    public void onSessionCompleted(Session session) {
        System.out.println("✅ Session completed: " + session.getTask().getTitle() +
                " at " + session.getEndTime());
    }

    @Override
    public void onTaskAdded(Task task) {
        System.out.println("📋 New task added: " + task.getTitle() +
                " (Due: " + task.getDeadline() + ")");
    }

    @Override
    public void onTaskDeadlineApproaching(Task task) {
        long daysUntilDeadline = ChronoUnit.DAYS.between(LocalDateTime.now(), task.getDeadline());
        System.out.println("⚠️  Deadline approaching for: " + task.getTitle() +
                " (" + daysUntilDeadline + " days left)");
    }
}