package com.studyplanner.tests;

import com.studyplanner.models.Task;
import com.studyplanner.services.LLMService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LLMServiceTest {

    public static void main(String[] args) {
        LLMServiceTest test = new LLMServiceTest();
        System.out.println("Running LLMService tests...");
        
        test.test_prioritizeTasks_withValidList();
        test.test_prioritizeTasks_withEmptyList();
        test.test_prioritizeTasks_withNullList();

        System.out.println("All LLMService tests completed!");
    }

    void test_prioritizeTasks_withValidList() {
        // For now, this test will verify that the method returns a non-null list
        // since the actual prioritization with OpenAI is complex to test
        LLMService llmService = new LLMService("dummy-key");
        List<Task> tasks = new ArrayList<>();
        tasks.add(new Task("Task 3", LocalDateTime.now().plusDays(3), 3));
        tasks.add(new Task("Task 1", LocalDateTime.now().plusDays(1), 5));
        tasks.add(new Task("Task 2", LocalDateTime.now().plusDays(2), 2));

        List<Task> prioritized = llmService.prioritizeTasks(tasks);

        if (prioritized != null && prioritized.size() == 3) {
            System.out.println("PASS: test_prioritizeTasks_withValidList");
        } else {
            System.out.println("FAIL: test_prioritizeTasks_withValidList");
        }
    }

    void test_prioritizeTasks_withEmptyList() {
        LLMService llmService = new LLMService("dummy-key");
        List<Task> tasks = new ArrayList<>();
        List<Task> prioritized = llmService.prioritizeTasks(tasks);

        if (prioritized != null && prioritized.isEmpty()) {
            System.out.println("PASS: test_prioritizeTasks_withEmptyList");
        } else {
            System.out.println("FAIL: test_prioritizeTasks_withEmptyList");
        }
    }

    void test_prioritizeTasks_withNullList() {
        LLMService llmService = new LLMService("dummy-key");
        List<Task> prioritized = llmService.prioritizeTasks(null);
        
        if (prioritized != null && prioritized.isEmpty()) {
            System.out.println("PASS: test_prioritizeTasks_withNullList");
        } else {
            System.out.println("FAIL: test_prioritizeTasks_withNullList");
        }
    }
}