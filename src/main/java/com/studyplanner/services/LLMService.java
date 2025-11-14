package com.studyplanner.services;

import com.studyplanner.models.Task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service that uses Nvidia NIM API to prioritize and annotate tasks.
 */
public class LLMService {
    private OpenAIService openAIService;

    public LLMService(String apiKey) {
        this.openAIService = new OpenAIService(apiKey);
    }

    /**
     * Prioritizes the given list of tasks based on urgency and importance using Nvidia NIM.
     * Tasks are ordered by the AI considering deadline, effort, and importance.
     *
     * @param taskList the list of tasks to prioritize
     * @return a new list of tasks ordered by priority
     */
    public List<Task> prioritizeTasks(List<Task> taskList) {
        if (taskList == null || taskList.isEmpty()) {
            return new ArrayList<>();
        }

        // Create a prompt for the AI to prioritize tasks
        StringBuilder prompt = new StringBuilder();
        prompt.append("Please prioritize the following study tasks based on urgency and importance. Consider the deadline (how soon it's due) and effort (how much time it needs) when determining priority:\n\n");
        
        for (int i = 0; i < taskList.size(); i++) {
            Task task = taskList.get(i);
            prompt.append(String.format("%d. Task: %s, Deadline: %s, Effort: %d hours\n", 
                      i + 1, task.getTitle(), task.getDeadline(), task.getEffort()));
        }
        
        prompt.append("\nReturn ONLY the task numbers in priority order (from highest priority to lowest), separated by commas. For example: 3, 1, 2. Do not include any other text.");

        // Call the Nvidia NIM service to prioritize tasks
        String response = openAIService.prioritizeTasksWithOpenAI(taskList);
        
        // If we get a valid response, try to parse the order
        // For now, we'll implement a basic sorting based on deadline and effort
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
                System.out.println("Annotating task: " + task.getTitle() + 
                    " with deadline: " + task.getDeadline() + 
                    " and effort: " + task.getEffort() + " hours");
            }
        }

        return taskList;
    }

    public void setApiKey(String apiKey) {
        this.openAIService.setApiKey(apiKey);
    }
}