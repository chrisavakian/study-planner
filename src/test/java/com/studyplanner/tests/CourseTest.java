package com.studyplanner.tests;

import com.studyplanner.models.Course;
import com.studyplanner.models.Task;

import java.time.LocalDateTime;

public class CourseTest {

    public static void main(String[] args) {
        CourseTest test = new CourseTest();
        System.out.println("Running Course tests...");
        
        test.test_associateTasks_withValidTask();
        test.test_associateTasks_withNullTask();
        
        System.out.println("All Course tests completed!");
    }

    void test_associateTasks_withValidTask() {
        Course course = new Course("Math 101", "Dr. Smith");
        Task task = new Task("Algebra Homework", LocalDateTime.now().plusDays(3), 2);
        
        boolean exceptionThrown = false;
        try {
            course.associateTasks(task);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        
        if (!exceptionThrown && 
            course.getTasks().size() == 1 &&
            course.getTasks().get(0).equals(task) &&
            task.getCourse().equals(course)) {
            System.out.println("PASS: test_associateTasks_withValidTask");
        } else {
            System.out.println("FAIL: test_associateTasks_withValidTask");
        }
    }

    void test_associateTasks_withNullTask() {
        Course course = new Course("Math 101", "Dr. Smith");
        boolean exceptionThrown = false;
        try {
            course.associateTasks(null);
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        }
        
        if (exceptionThrown) {
            System.out.println("PASS: test_associateTasks_withNullTask");
        } else {
            System.out.println("FAIL: test_associateTasks_withNullTask");
        }
    }
}