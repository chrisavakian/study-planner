package com.studyplanner.command;

import com.studyplanner.models.Student;
import com.studyplanner.models.Session;

/**
 * Command to mark a session as complete.
 */
public class CompleteSessionCommand implements Command {
    private Student student;
    private Session session;
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
        if (!executed && !session.isCompleted()) {
            student.markSessionComplete(session);
            this.executed = true;
        }
    }

    @Override
    public void undo() {
        if (executed && !wasCompleted) {
            // Reset the session to incomplete state using the setter method
            session.setCompleted(false);
            System.out.println("Session completion has been undone. Session is now marked as incomplete.");
            this.executed = false;
        }
    }

    @Override
    public boolean isUndoable() {
        // Session can be undone if it was originally incomplete and has now been executed
        return executed && !wasCompleted;
    }
}