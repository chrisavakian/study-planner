package com.studyplanner.models;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a study task with title, deadline, effort, course, and sessions.
 */
public class Task {
    private static final int MAX_SESSION_DURATION_HOURS = 2;
    private String title;
    private LocalDateTime deadline;
    private int effort; // effort in hours
    private Course course;
    private List<Session> sessions;

    /**
     * Constructs a new Task with the specified title, deadline, and effort.
     *
     * @param title    the title of the task
     * @param deadline the deadline for the task
     * @param effort   the estimated effort in hours
     */
    public Task(String title, LocalDateTime deadline, int effort) {
        this.title = title;
        this.deadline = deadline;
        this.effort = effort;
        this.sessions = new ArrayList<>();
    }

    /**
     * Splits the task into sessions based on the effort.
     * Each session will be at most 2 hours long.
     *
     * @return a list of sessions representing the task
     */
    public List<Session> splitIntoSessions() {
        sessions.clear();
        int remainingEffort = this.effort;

        while (remainingEffort > 0) {
            int sessionDuration = calculateSessionDuration(remainingEffort);
            Session session = createSessionPlaceholder(sessionDuration);
            sessions.add(session);
            remainingEffort -= sessionDuration;
        }

        return sessions;
    }

    private int calculateSessionDuration(int remainingEffort) {
        return Math.min(MAX_SESSION_DURATION_HOURS, remainingEffort);
    }

    private Session createSessionPlaceholder(int sessionDuration) {
        // Create a session placeholder without specific time (time will be assigned by scheduler)
        // Use a reference time for duration calculation
        LocalDateTime sessionStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
        LocalDateTime sessionEnd = sessionStart.plusHours(sessionDuration);
        return new Session(sessionStart, sessionEnd, this); // Link session to this task
    }

    /**
     * Updates the estimated effort for the task.
     *
     * @param newEffort the new estimated effort in hours
     * @throws IllegalArgumentException if newEffort is negative
     */
    public void updateEstimate(int newEffort) {
        validateEffort(newEffort);
        this.effort = newEffort;
    }

    private void validateEffort(int newEffort) {
        if (newEffort < 0) {
            throw new IllegalArgumentException("Effort cannot be negative");
        }
    }

    // Getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public int getEffort() {
        return effort;
    }

    public void setEffort(int effort) {
        this.effort = effort;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public List<Session> getSessions() {
        return sessions;
    }

    public void setSessions(List<Session> sessions) {
        this.sessions = sessions;
    }
}