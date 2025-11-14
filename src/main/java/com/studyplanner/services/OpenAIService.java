package com.studyplanner.services;

import com.studyplanner.models.Task;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service to interact with Nvidia NIM API for task prioritization and annotation.
 */
public class OpenAIService {
    private String apiKey;
    private static final String NVIDIA_NIM_API_URL = "https://integrate.api.nvidia.com/v1/chat/completions";

    public OpenAIService(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Sends a request to Nvidia NIM API to prioritize tasks.
     * 
     * @param tasks The list of tasks to prioritize
     * @return A list of tasks ordered by priority
     */
    public String prioritizeTasksWithOpenAI(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return "[]";
        }

        // Create a prompt for Nvidia NIM
        StringBuilder prompt = new StringBuilder();
        prompt.append("Please prioritize the following study tasks based on urgency and importance:\n");
        
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            prompt.append(String.format("%d. Task: %s, Deadline: %s, Effort: %d hours\n", 
                      i + 1, task.getTitle(), task.getDeadline(), task.getEffort()));
        }
        
        prompt.append("\nReturn the tasks in order of priority, with the most urgent and important task first. Only respond with the reordered list, nothing else.");

        return callNvidiaNIM(prompt.toString());
    }

    /**
     * Sends a request to Nvidia NIM API to annotate tasks.
     * 
     * @param tasks The list of tasks to annotate
     * @return Annotations for the tasks
     */
    public String annotateTasksWithOpenAI(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return "No tasks to annotate";
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("Please provide study tips and annotations for the following tasks:\n");
        
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            prompt.append(String.format("%d. Task: %s, Deadline: %s, Effort: %d hours\n", 
                      i + 1, task.getTitle(), task.getDeadline(), task.getEffort()));
        }
        
        prompt.append("\nProvide helpful study tips and annotations for each task.");

        return callNvidiaNIM(prompt.toString());
    }

    /**
     * Makes the actual API call to Nvidia NIM.
     * 
     * @param prompt The prompt to send to Nvidia NIM
     * @return The response from Nvidia NIM
     */
    private String callNvidiaNIM(String prompt) {
        try {
            URL url = new URL(NVIDIA_NIM_API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            // Set up the connection
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setDoOutput(true);

            // Create JSON payload (using openai/gpt-oss-120b model as per example)
            String jsonPayload = String.format(
                "{%n" +
                "  \"model\": \"openai/gpt-oss-120b\",%n" +
                "  \"messages\": [%n" +
                "    {\"role\": \"user\", \"content\": \"%s\"}%n" +
                "  ],%n" +
                "  \"temperature\": 1,%n" +
                "  \"top_p\": 1,%n" +
                "  \"max_tokens\": 4096,%n" +
                "  \"stream\": false%n" +
                "}",
                prompt.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
            );

            // Send the request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Get the response
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line).append("\n");
                }
                reader.close();
                
                // Extract the message content from the response
                return extractMessageContent(response.toString());
            } else {
                return "Error: " + responseCode + " - " + connection.getResponseMessage();
            }
        } catch (Exception e) {
            return "Error calling Nvidia NIM API: " + e.getMessage();
        }
    }

    /**
     * Extracts the message content from Nvidia NIM API response.
     * 
     * @param jsonResponse The JSON response from Nvidia NIM
     * @return The extracted message content
     */
    private String extractMessageContent(String jsonResponse) {
        try {
            // Using regex to extract content from JSON response
            Pattern pattern = Pattern.compile("\"content\":\"([^\"]*)\"");
            Matcher matcher = pattern.matcher(jsonResponse);
            
            if (matcher.find()) {
                return matcher.group(1).replace("\\n", "\n").replace("\\\"", "\"");
            }
        } catch (Exception e) {
            // If parsing fails, return the full response
            return jsonResponse;
        }
        return jsonResponse;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}