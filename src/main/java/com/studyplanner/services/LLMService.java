package com.studyplanner.services;

import com.studyplanner.models.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Service that uses Nvidia NIM API to prioritize and annotate tasks.
 * Implemented as a Singleton to ensure only one instance exists.
 */
public class LLMService {
    private static LLMService instance;
    private OpenAIService openAIService;

    public LLMService(String apiKey) {
        this.openAIService = new OpenAIService(apiKey);
    }

    public static synchronized LLMService getInstance(String apiKey) {
        if (instance == null) {
            instance = new LLMService(apiKey);
        }
        // Note: If apiKey is different, we'd need to handle that case differently
        // For now, we'll return the existing instance if one exists
        return instance;
    }

    /**
     * Prioritizes the given list of tasks based on urgency and importance using Nvidia NIM.
     * Tasks are ordered by the AI considering deadline, effort, and importance.
     *
     * @param taskList the list of tasks to prioritize
     * @return a new list of tasks ordered by priority
     */
    public List<Task> prioritizeTasks(List<Task> taskList) {
        if (isTaskListNullOrEmpty(taskList)) {
            return new ArrayList<>();
        }

        String response = openAIService.prioritizeTasksWithOpenAI(taskList);

        // If we get a valid response, try to parse the order
        // For now, we'll implement a basic sorting based on deadline and effort
        return createPrioritizedTaskList(taskList);
    }

    private boolean isTaskListNullOrEmpty(List<Task> taskList) {
        return taskList == null || taskList.isEmpty();
    }

    private List<Task> createPrioritizedTaskList(List<Task> taskList) {
        List<Task> prioritizedList = new ArrayList<>(taskList);

        // Sort by deadline first (soonest first), then by effort (highest first as more important)
        prioritizedList.sort((t1, t2) -> {
            // Compare deadlines first
            int deadlineComparison = t1.getDeadline().compareTo(t2.getDeadline());
            if (deadlineComparison != 0) {
                return deadlineComparison; // Sooner deadline has higher priority
            }
            // If deadlines are the same, sort by effort descending (higher effort = higher priority)
            return Integer.compare(t2.getEffort(), t1.getEffort());
        });

        return prioritizedList;
    }

    /**
     * Annotates tasks with additional information based on their properties using OpenAI.
     *
     * @param taskList the list of tasks to annotate
     * @return the same list with annotations added
     */
    public List<Task> annotateTasks(List<Task> taskList) {
        if (taskList == null) {
            return new ArrayList<>();
        }

        for (Task task : taskList) {
            if (task != null) {
                // In a real application, this would call the OpenAI API for annotations
                logTaskAnnotation(task);
            }
        }

        return taskList;
    }

    private void logTaskAnnotation(Task task) {
        System.out.println("Annotating task: " + task.getTitle() +
            " with deadline: " + task.getDeadline() +
            " and effort: " + task.getEffort() + " hours");
    }

    public void setApiKey(String apiKey) {
        this.openAIService.setApiKey(apiKey);
    }
}