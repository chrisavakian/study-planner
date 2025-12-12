package com.studyplanner.command;

import com.studyplanner.factory.TaskFactory;
import com.studyplanner.factory.TaskType;
import com.studyplanner.models.Student;
import com.studyplanner.models.Task;
import java.time.LocalDateTime;

/**
 * Command to add a task to the student's task list.
 */
public class AddTaskCommand implements Command {
    private Student student;
    private String title;
    private LocalDateTime deadline;
    private int effort;
    private TaskType taskType;
    private Task createdTask;
    private boolean executed;

    public AddTaskCommand(Student student, String title, LocalDateTime deadline, int effort) {
        this(student, title, deadline, effort, null); // Default to no specific task type
    }

    public AddTaskCommand(Student student, String title, LocalDateTime deadline, int effort, TaskType taskType) {
        this.student = student;
        this.title = title;
        this.deadline = deadline;
        this.effort = effort;
        this.taskType = taskType;
        this.executed = false;
    }

    @Override
    public void execute() {
        if (!executed) {
            if (taskType != null) {
                // Use factory to create task with type
                Task factoryTask = TaskFactory.createTask(taskType, title, deadline, effort);
                // Add the factory-created task to student using the new method
                this.createdTask = student.addExistingTask(factoryTask);
            } else {
                // Create task normally without factory
                this.createdTask = student.addTask(title, deadline, effort);
            }
            this.executed = true;
        }
    }

    @Override
    public void undo() {
        if (executed && createdTask != null) {
            // Remove the task from the student's list
            boolean removed = student.removeTask(createdTask);
            if (removed) {
                System.out.println("Task '" + title + "' has been removed (undo complete).");
            } else {
                System.out.println("Could not remove task '" + title + "', may have already been removed.");
            }
            this.executed = false;
        }
    }

    @Override
    public boolean isUndoable() {
        return executed && createdTask != null;
    }
}