package com.studyplanner.models;

import java.time.LocalDateTime;

/**
 * Represents a study session with start time, end time, task, and status.
 */
public class Session {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Task task; // Reference to the task this session is for
    private boolean completed;

    /**
     * Constructs a new Session with the specified start and end times.
     *
     * @param startTime the start time of the session
     * @param endTime   the end time of the session
     */
    public Session(LocalDateTime startTime, LocalDateTime endTime) {
        this(startTime, endTime, null);
    }

    /**
     * Constructs a new Session with the specified start and end times and task.
     *
     * @param startTime the start time of the session
     * @param endTime   the end time of the session
     * @param task      the task this session is for
     */
    public Session(LocalDateTime startTime, LocalDateTime endTime, Task task) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.task = task;
        this.completed = false;
    }

    /**
     * Marks the session as complete.
     */
    public void complete() {
        this.completed = true;
    }

    // Getters and setters
    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}