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
    private static final Scanner SCANNER = new Scanner(System.in);
    private static final CommandManager COMMAND_MANAGER = new CommandManager();
    private static final String WELCOME_MESSAGE = "=== Smart Study Planner ===\n";
    private static final String EXIT_MESSAGE = "Thank you for using Smart Study Planner!";
    private static final String INVALID_OPTION_MESSAGE = "Invalid option. Please try again.";
    private static final String INPUT_NAME_PROMPT = "Enter your name: ";
    private static final String INPUT_ID_PROMPT = "Enter your student ID: ";
    private static final String AVAILABILITY_SETUP_MESSAGE = "\nLet's set up your weekly availability.";
    private static final String TIME_FORMAT_EXAMPLE = "HH:MM format, e.g. 09:00";
    private static final String DATE_TIME_FORMAT_PATTERN = "yyyy-MM-dd HH:mm";
    private static final String TASK_DATE_FORMAT_PATTERN = "MMM dd, HH:mm";
    private static final String WEEK_DATE_FORMAT_PATTERN = "MMM dd, yyyy";

    public static void main(String[] args) {
        displayWelcomeMessage();

        Student student = collectStudentInformation();
        setupNotificationService(student);

        LLMService llmService = initializeLLMService();
        configureStudentAvailability(student);

        runMainMenuLoop(student, llmService);

        SCANNER.close();
    }

    private static void displayWelcomeMessage() {
        System.out.println(WELCOME_MESSAGE);
    }

    private static Student collectStudentInformation() {
        String name = getUserInput(INPUT_NAME_PROMPT).trim();
        String id = getUserInput(INPUT_ID_PROMPT).trim();
        return new Student(id, name);
    }

    private static void setupNotificationService(Student student) {
        NotificationService notificationService = new NotificationService();
        student.addObserver(notificationService);
        System.out.println("\nWelcome, " + student.getName() + "!\n");
    }

    private static LLMService initializeLLMService() {
        // Hardcoded API key
        String apiKey = "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";
        return LLMService.getInstance(apiKey);
    }

    private static void configureStudentAvailability(Student student) {
        List<Availability> availability = getAvailabilityFromUser();
        student.setAvailability(availability);
    }

    private static void runMainMenuLoop(Student student, LLMService llmService) {
        boolean isRunning = true;
        while (isRunning) {
            displayMainMenuOptions();

            String choice = getUserInput("Choose an option: ").trim();
            isRunning = processMenuOption(choice, student, llmService);
        }
    }

    private static void displayMainMenuOptions() {
        System.out.println("\n=== Main Menu ===");
        System.out.println("1. Add a task");
        System.out.println("2. View current tasks");
        System.out.println("3. Generate schedule");
        System.out.println("4. View schedule");
        System.out.println("5. Mark session as complete");
        System.out.println("6. Undo last action");
        System.out.println("7. Redo last action");
        System.out.println("8. Exit");
    }

    private static boolean processMenuOption(String choice, Student student, LLMService llmService) {
        switch (choice) {
            case "1":
                addTaskToStudent(student);
                return true;
            case "2":
                viewTasks(student);
                return true;
            case "3":
                generateSchedule(student, llmService);
                return true;
            case "4":
                viewSchedule(student);
                return true;
            case "5":
                markSessionComplete(student);
                return true;
            case "6":
                COMMAND_MANAGER.undo();
                return true;
            case "7":
                COMMAND_MANAGER.redo();
                return true;
            case "8":
                System.out.println(EXIT_MESSAGE);
                return false;
            default:
                System.out.println(INVALID_OPTION_MESSAGE);
                return true;
        }
    }

    private static List<Availability> getAvailabilityFromUser() {
        List<Availability> availability = new ArrayList<>();
        System.out.println(AVAILABILITY_SETUP_MESSAGE);

        for (DayOfWeek day : DayOfWeek.values()) {
            Availability dayAvailability = collectAvailabilityForDay(day);
            if (dayAvailability != null) {
                availability.add(dayAvailability);
            }
        }

        return availability;
    }

    private static Availability collectAvailabilityForDay(DayOfWeek day) {
        System.out.println("\n" + capitalizeFirst(day.toString()) + ":");
        String response = getUserInput("  Available? (y/n): ").trim().toLowerCase();

        if (isUserAvailable(response)) {
            return createAvailabilityForDay(day);
        }
        return null;
    }

    private static boolean isUserAvailable(String response) {
        return response.equals("y") || response.equals("yes");
    }

    private static Availability createAvailabilityForDay(DayOfWeek day) {
        String startTimeStr = getUserInput("  Start time (" + TIME_FORMAT_EXAMPLE + "): ").trim();
        String endTimeStr = getUserInput("  End time (" + TIME_FORMAT_EXAMPLE + "): ").trim();

        try {
            // Allow both H:MM and HH:MM formats by padding with zero if needed
            startTimeStr = padTimeWithZero(startTimeStr);
            endTimeStr = padTimeWithZero(endTimeStr);

            LocalTime startTime = LocalTime.parse(startTimeStr);
            LocalTime endTime = LocalTime.parse(endTimeStr);

            if (startTime.isAfter(endTime)) {
                System.out.println("  Error: Start time cannot be after end time. Skipping this day.");
                return null;
            }

            System.out.println("  Added availability: " + startTime + " - " + endTime);
            return new Availability(day, startTime, endTime);
        } catch (DateTimeParseException e) {
            System.out.println("  Error: Invalid time format. Please use HH:MM format (e.g. 09:00). Skipping this day.");
            return null;
        }
    }

    private static void addTaskToStudent(Student student) {
        System.out.println("\n=== Add a Task ===");
        String title = getUserInput("Task title: ").trim();

        if (title.isEmpty()) {
            System.out.println("Task title cannot be empty!");
            return;
        }

        TaskType taskType = selectTaskType();
        LocalDateTime deadline = getTaskDeadline();
        if (deadline == null) {
            return; // Error message already printed
        }

        int effort = getTaskEffort();
        if (effort <= 0) {
            return; // Error message already printed
        }

        // Use Command pattern with task type
        Command addTaskCommand = new AddTaskCommand(student, title, deadline, effort, taskType);
        COMMAND_MANAGER.executeCommand(addTaskCommand);
    }

    private static TaskType selectTaskType() {
        System.out.println("Select task type:");
        System.out.println("1. Assignment");
        System.out.println("2. Exam");
        System.out.println("3. Project");
        System.out.println("4. Review");
        String typeChoice = getUserInput("Enter choice (1-4): ").trim();

        switch (typeChoice) {
            case "1":
                return TaskType.ASSIGNMENT;
            case "2":
                return TaskType.EXAM;
            case "3":
                return TaskType.PROJECT;
            case "4":
                return TaskType.REVIEW;
            default:
                System.out.println("Invalid choice, using default type.");
                return TaskType.ASSIGNMENT;
        }
    }

    private static LocalDateTime getTaskDeadline() {
        String deadlineStr = getUserInput("Deadline (YYYY-MM-DD HH:MM format): ").trim();

        try {
            return LocalDateTime.parse(deadlineStr, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT_PATTERN));
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format! Please use YYYY-MM-DD HH:MM format (e.g., 2023-12-25 14:30)");
            return null;
        }
    }

    private static int getTaskEffort() {
        String effortStr = getUserInput("Estimated effort (hours): ").trim();
        try {
            int effort = Integer.parseInt(effortStr);
            if (effort <= 0) {
                System.out.println("Effort must be a positive number!");
                return -1;
            }
            return effort;
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format for effort!");
            return -1;
        }
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
            System.out.println((i + 1) + ". " + createTaskDisplayString(task));
        }
    }

    private static String createTaskDisplayString(Task task) {
        return task.getTitle() +
               " (Deadline: " + task.getDeadline().format(DateTimeFormatter.ofPattern(TASK_DATE_FORMAT_PATTERN)) +
               ", Effort: " + task.getEffort() + " hours)";
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

        ISchedulerStrategy selectedStrategy = selectSchedulingStrategy();
        if (selectedStrategy == null) {
            return; // Error message already printed
        }

        System.out.println("Generating schedule...");
        Schedule schedule = new Scheduler().generateSchedule(prioritizedTasks, student.getAvailability());
        student.setSchedule(schedule);

        System.out.println("Schedule generated successfully!");
    }

    private static ISchedulerStrategy selectSchedulingStrategy() {
        System.out.println("Tasks prioritized! Select scheduling strategy:");
        System.out.println("1. Priority-based (prioritizes urgent tasks)");
        System.out.println("2. Time-block (creates consistent study blocks)");
        System.out.println("3. Balanced (balances urgency with optimal times)");
        String strategyChoice = getUserInput("Choose strategy (1-3): ").trim();

        switch (strategyChoice) {
            case "1":
                return new PrioritySchedulerStrategy();
            case "2":
                return new TimeBlockSchedulerStrategy();
            case "3":
                return new BalancedSchedulerStrategy();
            default:
                System.out.println("Invalid choice, using priority-based strategy.");
                return new PrioritySchedulerStrategy();
        }
    }

    private static void viewSchedule(Student student) {
        Schedule schedule = student.getSchedule();
        if (isScheduleEmpty(schedule)) {
            System.out.println("\nNo schedule generated yet. Generate a schedule first.");
            return;
        }

        displayScheduleHeader(schedule);
        displayScheduleByDay(schedule);
    }

    private static boolean isScheduleEmpty(Schedule schedule) {
        return schedule == null || schedule.getSessions().isEmpty();
    }

    private static void displayScheduleHeader(Schedule schedule) {
        System.out.println("\n=== Weekly Schedule ===");
        System.out.println("Week starting: " +
                           schedule.getWeekStart().format(DateTimeFormatter.ofPattern(WEEK_DATE_FORMAT_PATTERN)));
        System.out.println("Total sessions: " + schedule.getSessions().size());
    }

    private static void displayScheduleByDay(Schedule schedule) {
        for (DayOfWeek day : DayOfWeek.values()) {
            List<Session> dailySessions = schedule.viewDaily(day);
            if (!dailySessions.isEmpty()) {
                System.out.println("\n" + capitalizeFirst(day.toString()) + ":");
                displayDailySessions(dailySessions);
            }
        }
    }

    private static void displayDailySessions(List<Session> dailySessions) {
        for (int i = 0; i < dailySessions.size(); i++) {
            Session session = dailySessions.get(i);
            System.out.println("  " + (i + 1) + ". " + createSessionDisplayString(session));
        }
    }

    private static String createSessionDisplayString(Session session) {
        String taskInfo = session.getTask() != null ? " - " + session.getTask().getTitle() : " (No task assigned)";
        return session.getStartTime().toLocalTime().toString() +
               " - " + session.getEndTime().toLocalTime().toString() +
               taskInfo +
               " (Completed: " + (session.isCompleted() ? "Yes" : "No") + ")";
    }

    private static void markSessionComplete(Student student) {
        Schedule schedule = student.getSchedule();
        if (isScheduleEmpty(schedule)) {
            System.out.println("\nNo schedule available. Generate a schedule first.");
            return;
        }

        List<Session> allSessions = schedule.getSessions();
        displayAvailableSessions(allSessions);

        String input = getUserInput("Enter session number to mark as complete: ").trim();

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
            COMMAND_MANAGER.executeCommand(completeSessionCommand);

            System.out.println("Session marked as complete!");

        } catch (NumberFormatException e) {
            System.out.println("Invalid input! Please enter a number.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void displayAvailableSessions(List<Session> allSessions) {
        System.out.println("\n=== Available Sessions ===");
        for (int i = 0; i < allSessions.size(); i++) {
            Session session = allSessions.get(i);
            String status = session.isCompleted() ? "COMPLETED" : "PENDING";
            System.out.println((i + 1) + ". " + createSessionStatusString(session, status));
        }
    }

    private static String createSessionStatusString(Session session, String status) {
        return session.getStartTime().toLocalDate().getDayOfWeek() +
               " " + session.getStartTime().toLocalTime().toString() +
               "-" + session.getEndTime().toLocalTime().toString() +
               " [" + status + "]";
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

    private static String getUserInput(String prompt) {
        System.out.print(prompt);
        return SCANNER.nextLine();
    }
}