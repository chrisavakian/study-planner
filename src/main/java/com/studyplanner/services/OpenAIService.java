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
    private static final String MODEL_NAME = "openai/gpt-oss-120b";
    private static final String EMPTY_TASKS_RESPONSE = "[]";
    private static final String NO_TASKS_TO_ANNOTATE_RESPONSE = "No tasks to annotate";
    private static final int MAX_TOKENS = 4096;
    private static final double TEMPERATURE = 1.0;
    private static final double TOP_P = 1.0;

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
        if (isTaskListNullOrEmpty(tasks)) {
            return EMPTY_TASKS_RESPONSE;
        }

        String prompt = buildPrioritizationPrompt(tasks);
        return callNvidiaNIM(prompt);
    }

    private boolean isTaskListNullOrEmpty(List<Task> tasks) {
        return tasks == null || tasks.isEmpty();
    }

    private String buildPrioritizationPrompt(List<Task> tasks) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Please prioritize the following study tasks based on urgency and importance:\n");

        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            prompt.append(String.format("%d. Task: %s, Deadline: %s, Effort: %d hours\n",
                      i + 1, task.getTitle(), task.getDeadline(), task.getEffort()));
        }

        prompt.append("\nReturn the tasks in order of priority, with the most urgent and important task first. Only respond with the reordered list, nothing else.");

        return prompt.toString();
    }

    /**
     * Sends a request to Nvidia NIM API to annotate tasks.
     *
     * @param tasks The list of tasks to annotate
     * @return Annotations for the tasks
     */
    public String annotateTasksWithOpenAI(List<Task> tasks) {
        if (isTaskListNullOrEmpty(tasks)) {
            return NO_TASKS_TO_ANNOTATE_RESPONSE;
        }

        String prompt = buildAnnotationPrompt(tasks);
        return callNvidiaNIM(prompt);
    }

    private String buildAnnotationPrompt(List<Task> tasks) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Please provide study tips and annotations for the following tasks:\n");

        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            prompt.append(String.format("%d. Task: %s, Deadline: %s, Effort: %d hours\n",
                      i + 1, task.getTitle(), task.getDeadline(), task.getEffort()));
        }

        prompt.append("\nProvide helpful study tips and annotations for each task.");

        return prompt.toString();
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
            HttpURLConnection connection = createConnection(url);

            String jsonPayload = buildJsonPayload(prompt);
            sendRequest(connection, jsonPayload);

            return processResponse(connection);
        } catch (Exception e) {
            return "Error calling Nvidia NIM API: " + e.getMessage();
        }
    }

    private HttpURLConnection createConnection(URL url) throws java.io.IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set up the connection
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setDoOutput(true);

        return connection;
    }

    private String buildJsonPayload(String prompt) {
        return String.format(
            "{%n" +
            "  \"model\": \"%s\",%n" +
            "  \"messages\": [%n" +
            "    {\"role\": \"user\", \"content\": \"%s\"}%n" +
            "  ],%n" +
            "  \"temperature\": %f,%n" +
            "  \"top_p\": %f,%n" +
            "  \"max_tokens\": %d,%n" +
            "  \"stream\": false%n" +
            "}",
            MODEL_NAME,
            formatPromptForJson(prompt),
            TEMPERATURE,
            TOP_P,
            MAX_TOKENS
        );
    }

    private String formatPromptForJson(String prompt) {
        return prompt.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    private void sendRequest(HttpURLConnection connection, String jsonPayload) throws java.io.IOException {
        // Send the request
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonPayload.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
    }

    private String processResponse(HttpURLConnection connection) throws java.io.IOException {
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String rawResponse = readResponse(connection);
            // Extract the message content from the response
            return extractMessageContent(rawResponse);
        } else {
            return "Error: " + responseCode + " - " + connection.getResponseMessage();
        }
    }

    private String readResponse(HttpURLConnection connection) throws java.io.IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line).append("\n");
        }
        reader.close();

        return response.toString();
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