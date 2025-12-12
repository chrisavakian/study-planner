package com.studyplanner.scheduler;

import com.studyplanner.models.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler that uses different scheduling strategies based on user preference.
 */
public class Scheduler implements ISchedulerStrategy {
    private ISchedulerStrategy strategy;

    public Scheduler() {
        // Default to priority-based scheduling
        this.strategy = new PrioritySchedulerStrategy();
    }

    public Scheduler(ISchedulerStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Sets the scheduling strategy to use.
     *
     * @param strategy the scheduling strategy to use
     */
    public void setStrategy(ISchedulerStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Generates a schedule based on the selected strategy.
     *
     * @param prioritizedList the list of tasks ordered by priority
     * @param availability    the student's available time slots
     * @return a Schedule object with planned sessions
     */
    @Override
    public Schedule generateSchedule(List<Task> prioritizedList, List<Availability> availability) {
        return strategy.generateSchedule(prioritizedList, availability);
    }

    /**
     * Generates a schedule based on the prioritized list of tasks and student availability
     * using the current strategy.
     *
     * @param prioritizedList the list of tasks ordered by priority
     * @param availability    the student's available time slots
     * @return a Schedule object with planned sessions
     */
    public Schedule generateScheduleWithCurrentStrategy(List<Task> prioritizedList, List<Availability> availability) {
        return generateSchedule(prioritizedList, availability);
    }
}