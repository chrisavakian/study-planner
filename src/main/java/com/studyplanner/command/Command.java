package com.studyplanner.command;

/**
 * Command interface for implementing the Command pattern.
 */
public interface Command {
    void execute();
    void undo();
    boolean isUndoable();
}