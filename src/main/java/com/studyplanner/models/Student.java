package com.studyplanner.models;

import com.studyplanner.observer.StudyObserver;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a student with ID, name, task list, availability, and schedule.
 */
public class Student {
    private String studentID;
    private String name;
    private List<Task> taskList;
    private List<Availability> availability;
    private Schedule schedule;
    private List<StudyObserver> observers;

    /**
     * Constructs a new Student with the specified ID and name.
     *
     * @param studentID the unique ID of the student
     * @param name      the name of the student
     */
    public Student(String studentID, String name) {
        this.studentID = studentID;
        this.name = name;
        this.taskList = new ArrayList<>();
        this.availability = new ArrayList<>();
        this.observers = new ArrayList<>();
    }

    /**
     * Adds a new task to the student's task list.
     *
     * @param title    the title of the task
     * @param deadline the deadline for the task
     * @param effort   the estimated effort in hours
     * @return the created task
     * @throws IllegalArgumentException if title is empty or effort is negative
     */
    public Task addTask(String title, LocalDateTime deadline, int effort) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be empty");
        }
        if (effort < 0) {
            throw new IllegalArgumentException("Task effort cannot be negative");
        }
        if (deadline.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Task deadline cannot be in the past");
        }

        Task task = new Task(title, deadline, effort);
        taskList.add(task);

        // Notify observers that a new task has been added
        notifyTaskAdded(task);

        return task;
    }

    /**
     * Adds an observer to the student.
     *
     * @param observer the observer to add
     */
    public void addObserver(StudyObserver observer) {
        observers.add(observer);
    }

    /**
     * Removes an observer from the student.
     *
     * @param observer the observer to remove
     */
    public void removeObserver(StudyObserver observer) {
        observers.remove(observer);
    }

    /**
     * Notifies all observers that a task has been added.
     *
     * @param task the task that was added
     */
    private void notifyTaskAdded(Task task) {
        for (StudyObserver observer : observers) {
            observer.onTaskAdded(task);
        }

        // Check if the task deadline is approaching and notify if needed
        checkDeadlineApproaching(task);
    }

    /**
     * Checks if the task deadline is approaching and notifies observers.
     *
     * @param task the task to check
     */
    private void checkDeadlineApproaching(Task task) {
        long hoursUntilDeadline = java.time.Duration.between(LocalDateTime.now(), task.getDeadline()).toHours();
        if (hoursUntilDeadline <= 48 && hoursUntilDeadline > 0) { // Within 2 days
            for (StudyObserver observer : observers) {
                observer.onTaskDeadlineApproaching(task);
            }
        }
    }

    /**
     * Sets the student's weekly availability.
     *
     * @param availability the list of available time slots
     * @throws IllegalArgumentException if availability has overlapping times
     */
    public void setAvailability(List<Availability> availability) {
        // Check for overlapping times
        for (int i = 0; i < availability.size(); i++) {
            for (int j = i + 1; j < availability.size(); j++) {
                Availability a1 = availability.get(i);
                Availability a2 = availability.get(j);
                
                if (a1.getDay() == a2.getDay()) {
                    // Check if time ranges overlap
                    if (!(a1.getEnd().compareTo(a2.getStart()) <= 0 || 
                          a2.getEnd().compareTo(a1.getStart()) <= 0)) {
                        throw new IllegalArgumentException("Availability slots cannot overlap on the same day");
                    }
                }
            }
        }
        
        this.availability = availability;
    }

    /**
     * Views the current study schedule.
     *
     * @return the student's schedule or null if not generated yet
     */
    public Schedule viewSchedule() {
        return this.schedule;
    }

    /**
     * Marks a session as complete.
     *
     * @param session the session to mark as complete
     * @throws IllegalArgumentException if the session is not found in the schedule
     */
    public void markSessionComplete(Session session) {
        if (this.schedule == null) {
            throw new IllegalStateException("No schedule exists yet");
        }

        boolean found = false;
        for (Session s : this.schedule.getSessions()) {
            if (s == session) {
                s.complete();
                found = true;
                // Notify observers that a session has been completed
                notifySessionCompleted(session);
                break;
            }
        }

        if (!found) {
            throw new IllegalArgumentException("Session not found in the current schedule");
        }
    }

    /**
     * Notifies all observers that a session has been completed.
     *
     * @param session the session that was completed
     */
    private void notifySessionCompleted(Session session) {
        for (StudyObserver observer : observers) {
            observer.onSessionCompleted(session);
        }
    }

    // Getters and setters
    public String getStudentID() {
        return studentID;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Task> getTaskList() {
        return taskList;
    }

    public void setTaskList(List<Task> taskList) {
        this.taskList = taskList;
    }

    public List<Availability> getAvailability() {
        return availability;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public Schedule getSchedule() {
        return this.schedule;
    }

    /**
     * Removes a task from the student's task list.
     *
     * @param task the task to remove
     * @return true if the task was removed, false if it wasn't in the list
     */
    public boolean removeTask(Task task) {
        boolean removed = taskList.remove(task);
        if (removed) {
            notifyTaskRemoved(task);
        }
        return removed;
    }

    /**
     * Adds an existing task object to the student's task list.
     *
     * @param task the task object to add
     * @return the added task
     */
    public Task addExistingTask(Task task) {
        taskList.add(task);
        notifyTaskAdded(task);
        return task;
    }

    /**
     * Notifies all observers that a task has been removed.
     *
     * @param task the task that was removed
     */
    private void notifyTaskRemoved(Task task) {
        for (StudyObserver observer : observers) {
            // We could implement a onTaskRemoved method in the interface
            // For now, we'll just print a message
            System.out.println("Task removed: " + task.getTitle());
        }
    }
}