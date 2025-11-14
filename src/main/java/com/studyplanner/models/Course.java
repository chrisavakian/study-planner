package com.studyplanner.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a course with name, instructor, and associated tasks.
 */
public class Course {
    private String courseName;
    private String instructor;
    private List<Task> tasks;

    /**
     * Constructs a new Course with the specified name and instructor.
     *
     * @param courseName the name of the course
     * @param instructor the instructor of the course
     */
    public Course(String courseName, String instructor) {
        this.courseName = courseName;
        this.instructor = instructor;
        this.tasks = new ArrayList<>();
    }

    /**
     * Associates a task with this course.
     *
     * @param task the task to associate with this course
     * @throws IllegalArgumentException if the task is null
     */
    public void associateTasks(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        this.tasks.add(task);
        task.setCourse(this);
    }

    // Getters and setters
    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }
}