package com.studyplanner.command;

import java.util.Stack;

/**
 * Manager for handling command execution, undo, and redo operations.
 */
public class CommandManager {
    private static final String NO_COMMANDS_TO_UNDO_MESSAGE = "No commands to undo.";
    private static final String NO_COMMANDS_TO_REDO_MESSAGE = "No commands to redo.";

    private Stack<Command> commandHistory;
    private Stack<Command> undoHistory;

    public CommandManager() {
        this.commandHistory = new Stack<>();
        this.undoHistory = new Stack<>();
    }

    public void executeCommand(Command command) {
        command.execute();
        if (command.isUndoable()) {
            commandHistory.push(command);
            // Clear redo stack when new command is executed
            undoHistory.clear();
        }
    }

    public void undo() {
        if (canUndo()) {
            performUndo();
        } else {
            System.out.println(NO_COMMANDS_TO_UNDO_MESSAGE);
        }
    }

    private boolean canUndo() {
        return !commandHistory.isEmpty();
    }

    private void performUndo() {
        Command command = commandHistory.pop();
        command.undo();
        undoHistory.push(command);
    }

    public void redo() {
        if (canRedo()) {
            performRedo();
        } else {
            System.out.println(NO_COMMANDS_TO_REDO_MESSAGE);
        }
    }

    private boolean canRedo() {
        return !undoHistory.isEmpty();
    }

    private void performRedo() {
        Command command = undoHistory.pop();
        command.execute();
        commandHistory.push(command);
    }
}