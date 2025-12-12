package com.studyplanner.command;

import java.util.Stack;

/**
 * Manager for handling command execution, undo, and redo operations.
 */
public class CommandManager {
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
        if (!commandHistory.isEmpty()) {
            Command command = commandHistory.pop();
            command.undo();
            undoHistory.push(command);
        } else {
            System.out.println("No commands to undo.");
        }
    }
    
    public void redo() {
        if (!undoHistory.isEmpty()) {
            Command command = undoHistory.pop();
            command.execute();
            commandHistory.push(command);
        } else {
            System.out.println("No commands to redo.");
        }
    }
}