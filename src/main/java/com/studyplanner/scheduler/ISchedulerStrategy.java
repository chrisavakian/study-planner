package com.studyplanner.scheduler;

import com.studyplanner.models.*;

import java.util.List;

/**
 * Strategy interface for different scheduling algorithms.
 */
public interface ISchedulerStrategy {
    /**
     * Generate a schedule using this strategy.
     *
     * @param prioritizedList the list of tasks ordered by priority
     * @param availability the student's available time slots
     * @return a Schedule object with planned sessions
     */
    Schedule generateSchedule(List<Task> prioritizedList, List<Availability> availability);
}