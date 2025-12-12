package com.studyplanner.factory;

import com.studyplanner.models.Task;
import java.time.LocalDateTime;

/**
 * Factory for creating different types of tasks.
 */
public class TaskFactory {
    public static Task createTask(TaskType type, String title, LocalDateTime deadline, int effort) {
        Task task = new Task(title, deadline, effort);
        
        // Add type-specific prefixes or behaviors
        switch (type) {
            case ASSIGNMENT:
                task.setTitle("[Assignment] " + title);
                break;
            case EXAM:
                task.setTitle("[Exam Prep] " + title);
                break;
            case PROJECT:
                task.setTitle("[Project] " + title);
                break;
            case REVIEW:
                task.setTitle("[Review] " + title);
                break;
            default:
                break;
        }
        
        return task;
    }
}