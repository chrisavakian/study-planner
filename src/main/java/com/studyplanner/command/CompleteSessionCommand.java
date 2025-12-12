package com.studyplanner.command;

import com.studyplanner.models.Student;
import com.studyplanner.models.Session;

/**
 * Command to mark a session as complete.
 */
public class CompleteSessionCommand implements Command {
    private final Student student;
    private final Session session;
    private boolean wasCompleted;
    private boolean executed;

    public CompleteSessionCommand(Student student, Session session) {
        this.student = student;
        this.session = session;
        this.wasCompleted = session.isCompleted();
        this.executed = false;
    }

    @Override
    public void execute() {
        if (canExecute()) {
            performExecute();
        }
    }

    private boolean canExecute() {
        return !executed && !session.isCompleted();
    }

    private void performExecute() {
        student.markSessionComplete(session);
        this.executed = true;
    }

    @Override
    public void undo() {
        if (canUndo()) {
            performUndo();
            this.executed = false;
        }
    }

    private boolean canUndo() {
        return executed && !wasCompleted;
    }

    private void performUndo() {
        // Reset the session to incomplete state using the setter method
        session.setCompleted(false);
        System.out.println("Session completion has been undone. Session is now marked as incomplete.");
    }

    @Override
    public boolean isUndoable() {
        // Session can be undone if it was originally incomplete and has now been executed
        return executed && !wasCompleted;
    }
}