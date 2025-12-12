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
    private final Student student;
    private final String title;
    private final LocalDateTime deadline;
    private final int effort;
    private final TaskType taskType;
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
            this.createdTask = createAndAddTask();
            this.executed = true;
        }
    }

    private Task createAndAddTask() {
        if (isTaskTypeSpecified()) {
            return addFactoryCreatedTask();
        } else {
            return addRegularTask();
        }
    }

    private boolean isTaskTypeSpecified() {
        return taskType != null;
    }

    private Task addFactoryCreatedTask() {
        Task factoryTask = TaskFactory.createTask(taskType, title, deadline, effort);
        return student.addExistingTask(factoryTask);
    }

    private Task addRegularTask() {
        return student.addTask(title, deadline, effort);
    }

    @Override
    public void undo() {
        if (executed && createdTask != null) {
            performUndo();
            this.executed = false;
        }
    }

    private void performUndo() {
        boolean removed = student.removeTask(createdTask);
        if (removed) {
            System.out.println("Task '" + title + "' has been removed (undo complete).");
        } else {
            System.out.println("Could not remove task '" + title + "', may have already been removed.");
        }
    }

    @Override
    public boolean isUndoable() {
        return executed && createdTask != null;
    }
}