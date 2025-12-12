package com.studyplanner;

import com.studyplanner.command.*;
import com.studyplanner.factory.*;
import com.studyplanner.models.*;
import com.studyplanner.observer.*;
import com.studyplanner.services.LLMService;
import com.studyplanner.scheduler.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Main interactive class for the Smart Study Planner.
 */
public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static CommandManager commandManager = new CommandManager();

    public static void main(String[] args) {
        System.out.println("=== Smart Study Planner ===\n");

        // Get student information
        System.out.print("Enter your name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter your student ID: ");
        String id = scanner.nextLine().trim();

        Student student = new Student(id, name);

        // Add observer for notifications
        NotificationService notificationService = new NotificationService();
        student.addObserver(notificationService);

        System.out.println("\nWelcome, " + student.getName() + "!\n");

        // Hardcoded API key
        String apiKey = "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";
        LLMService llmService = LLMService.getInstance(apiKey);

        // Get availability from user
        List<Availability> availability = getAvailabilityFromUser();
        student.setAvailability(availability);

        // Main menu loop
        boolean running = true;
        while (running) {
            System.out.println("\n=== Main Menu ===");
            System.out.println("1. Add a task");
            System.out.println("2. View current tasks");
            System.out.println("3. Generate schedule");
            System.out.println("4. View schedule");
            System.out.println("5. Mark session as complete");
            System.out.println("6. Undo last action");
            System.out.println("7. Redo last action");
            System.out.println("8. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    addTaskToStudent(student);
                    break;
                case "2":
                    viewTasks(student);
                    break;
                case "3":
                    generateSchedule(student, llmService);
                    break;
                case "4":
                    viewSchedule(student);
                    break;
                case "5":
                    markSessionComplete(student);
                    break;
                case "6":
                    commandManager.undo();
                    break;
                case "7":
                    commandManager.redo();
                    break;
                case "8":
                    running = false;
                    System.out.println("Thank you for using Smart Study Planner!");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }

        scanner.close();
    }

    private static List<Availability> getAvailabilityFromUser() {
        List<Availability> availability = new ArrayList<>();
        System.out.println("\nLet's set up your weekly availability.");

        for (DayOfWeek day : DayOfWeek.values()) {
            System.out.println("\n" + capitalizeFirst(day.toString()) + ":");
            System.out.print("  Available? (y/n): ");
            String response = scanner.nextLine().trim().toLowerCase();
            
            if (response.equals("y") || response.equals("yes")) {
                System.out.print("  Start time (HH:MM format, e.g. 09:00): ");
                String startTimeStr = scanner.nextLine().trim();
                System.out.print("  End time (HH:MM format, e.g. 17:00): ");
                String endTimeStr = scanner.nextLine().trim();

                try {
                    // Allow both H:MM and HH:MM formats by padding with zero if needed
                    startTimeStr = padTimeWithZero(startTimeStr);
                    endTimeStr = padTimeWithZero(endTimeStr);
                    
                    LocalTime startTime = LocalTime.parse(startTimeStr);
                    LocalTime endTime = LocalTime.parse(endTimeStr);
                    
                    if (startTime.isAfter(endTime)) {
                        System.out.println("  Error: Start time cannot be after end time. Skipping this day.");
                        continue;
                    }
                    
                    availability.add(new Availability(day, startTime, endTime));
                    System.out.println("  Added availability: " + startTime + " - " + endTime);
                } catch (DateTimeParseException e) {
                    System.out.println("  Error: Invalid time format. Please use HH:MM format (e.g. 09:00). Skipping this day.");
                }
            }
        }
        
        return availability;
    }

    private static void addTaskToStudent(Student student) {
        System.out.println("\n=== Add a Task ===");
        System.out.print("Task title: ");
        String title = scanner.nextLine().trim();

        if (title.isEmpty()) {
            System.out.println("Task title cannot be empty!");
            return;
        }

        // Task type selection
        System.out.println("Select task type:");
        System.out.println("1. Assignment");
        System.out.println("2. Exam");
        System.out.println("3. Project");
        System.out.println("4. Review");
        System.out.print("Enter choice (1-4): ");
        String typeChoice = scanner.nextLine().trim();

        TaskType taskType;
        switch (typeChoice) {
            case "1":
                taskType = TaskType.ASSIGNMENT;
                break;
            case "2":
                taskType = TaskType.EXAM;
                break;
            case "3":
                taskType = TaskType.PROJECT;
                break;
            case "4":
                taskType = TaskType.REVIEW;
                break;
            default:
                System.out.println("Invalid choice, using default type.");
                taskType = TaskType.ASSIGNMENT;
                break;
        }

        System.out.print("Deadline (YYYY-MM-DD HH:MM format): ");
        String deadlineStr = scanner.nextLine().trim();

        LocalDateTime deadline;
        try {
            deadline = LocalDateTime.parse(deadlineStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format! Please use YYYY-MM-DD HH:MM format (e.g., 2023-12-25 14:30)");
            return;
        }

        System.out.print("Estimated effort (hours): ");
        String effortStr = scanner.nextLine().trim();
        int effort;
        try {
            effort = Integer.parseInt(effortStr);
            if (effort <= 0) {
                System.out.println("Effort must be a positive number!");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format for effort!");
            return;
        }

        // Use Command pattern with task type
        Command addTaskCommand = new AddTaskCommand(student, title, deadline, effort, taskType);
        commandManager.executeCommand(addTaskCommand);
    }

    private static void viewTasks(Student student) {
        List<Task> tasks = student.getTaskList();
        if (tasks.isEmpty()) {
            System.out.println("\nNo tasks added yet.");
            return;
        }
        
        System.out.println("\n=== Your Tasks ===");
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            System.out.println((i + 1) + ". " + task.getTitle() + 
                             " (Deadline: " + task.getDeadline().format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")) + 
                             ", Effort: " + task.getEffort() + " hours)");
        }
    }

    private static void generateSchedule(Student student, LLMService llmService) {
        List<Task> tasks = student.getTaskList();
        if (tasks.isEmpty()) {
            System.out.println("\nNo tasks available to schedule. Add tasks first.");
            return;
        }

        // Use AI service to prioritize tasks
        System.out.println("\nPrioritizing tasks...");
        List<Task> prioritizedTasks = llmService.prioritizeTasks(tasks);

        System.out.println("Tasks prioritized! Select scheduling strategy:");
        System.out.println("1. Priority-based (prioritizes urgent tasks)");
        System.out.println("2. Time-block (creates consistent study blocks)");
        System.out.println("3. Balanced (balances urgency with optimal times)");
        System.out.print("Choose strategy (1-3): ");
        String strategyChoice = scanner.nextLine().trim();

        // Generate schedule with selected strategy
        Scheduler scheduler = new Scheduler();

        switch (strategyChoice) {
            case "1":
                scheduler.setStrategy(new PrioritySchedulerStrategy());
                break;
            case "2":
                scheduler.setStrategy(new TimeBlockSchedulerStrategy());
                break;
            case "3":
                scheduler.setStrategy(new BalancedSchedulerStrategy());
                break;
            default:
                System.out.println("Invalid choice, using priority-based strategy.");
                scheduler.setStrategy(new PrioritySchedulerStrategy());
                break;
        }

        System.out.println("Generating schedule...");
        Schedule schedule = scheduler.generateSchedule(prioritizedTasks, student.getAvailability());
        student.setSchedule(schedule);

        System.out.println("Schedule generated successfully!");
    }

    private static void viewSchedule(Student student) {
        Schedule schedule = student.getSchedule();
        if (schedule == null || schedule.getSessions().isEmpty()) {
            System.out.println("\nNo schedule generated yet. Generate a schedule first.");
            return;
        }
        
        System.out.println("\n=== Weekly Schedule ===");
        System.out.println("Week starting: " + schedule.getWeekStart().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        System.out.println("Total sessions: " + schedule.getSessions().size());
        
        // Group sessions by day
        for (DayOfWeek day : DayOfWeek.values()) {
            List<Session> dailySessions = schedule.viewDaily(day);
            if (!dailySessions.isEmpty()) {
                System.out.println("\n" + capitalizeFirst(day.toString()) + ":");
                for (int i = 0; i < dailySessions.size(); i++) {
                    Session session = dailySessions.get(i);
                    String taskInfo = session.getTask() != null ? " - " + session.getTask().getTitle() : " (No task assigned)";
                    System.out.println("  " + (i + 1) + ". " + 
                                     session.getStartTime().toLocalTime().toString() + 
                                     " - " + session.getEndTime().toLocalTime().toString() + 
                                     taskInfo +
                                     " (Completed: " + (session.isCompleted() ? "Yes" : "No") + ")");
                }
            }
        }
    }

    private static void markSessionComplete(Student student) {
        Schedule schedule = student.getSchedule();
        if (schedule == null || schedule.getSessions().isEmpty()) {
            System.out.println("\nNo schedule available. Generate a schedule first.");
            return;
        }

        // Show available sessions to complete
        List<Session> allSessions = schedule.getSessions();
        System.out.println("\n=== Available Sessions ===");
        for (int i = 0; i < allSessions.size(); i++) {
            Session session = allSessions.get(i);
            String status = session.isCompleted() ? "COMPLETED" : "PENDING";
            System.out.println((i + 1) + ". " +
                             session.getStartTime().toLocalDate().getDayOfWeek() +
                             " " + session.getStartTime().toLocalTime().toString() +
                             "-" + session.getEndTime().toLocalTime().toString() +
                             " [" + status + "]");
        }

        System.out.print("Enter session number to mark as complete: ");
        String input = scanner.nextLine().trim();

        try {
            int sessionIndex = Integer.parseInt(input) - 1;
            if (sessionIndex < 0 || sessionIndex >= allSessions.size()) {
                System.out.println("Invalid session number!");
                return;
            }

            Session sessionToComplete = allSessions.get(sessionIndex);
            if (sessionToComplete.isCompleted()) {
                System.out.println("Session is already marked as complete!");
                return;
            }

            // Use Command pattern to mark session complete
            Command completeSessionCommand = new CompleteSessionCommand(student, sessionToComplete);
            commandManager.executeCommand(completeSessionCommand);

            System.out.println("Session marked as complete!");

        } catch (NumberFormatException e) {
            System.out.println("Invalid input! Please enter a number.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private static String capitalizeFirst(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }
    
    private static String padTimeWithZero(String timeStr) {
        // If the time format is like "5:00", pad the hour part to be "05:00"
        if (timeStr != null && timeStr.length() > 0) {
            if (timeStr.charAt(0) == ':') {
                // Handle case where the user might have made an error
                return timeStr;
            }
            String[] parts = timeStr.split(":");
            if (parts.length == 2) {
                String hour = parts[0];
                String minute = parts[1];
                // Pad hour if it's single digit
                if (hour.length() == 1) {
                    hour = "0" + hour;
                }
                // Pad minute if it's single digit  
                if (minute.length() == 1) {
                    minute = "0" + minute;
                }
                return hour + ":" + minute;
            }
        }
        return timeStr;
    }
}