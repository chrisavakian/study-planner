package com.studyplanner.models;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Represents available time slots for scheduling study sessions.
 */
public class Availability {
    private static final String START_AFTER_END_ERROR = "Start time cannot be after end time";
    private static final String END_BEFORE_START_ERROR = "End time cannot be before start time";

    private DayOfWeek day;
    private LocalTime start;
    private LocalTime end;

    /**
     * Constructs a new Availability with the specified day and time range.
     *
     * @param day   the day of the week
     * @param start the start time of availability
     * @param end   the end time of availability
     */
    public Availability(DayOfWeek day, LocalTime start, LocalTime end) {
        validateTimeRange(start, end);
        this.day = day;
        this.start = start;
        this.end = end;
    }

    private void validateTimeRange(LocalTime start, LocalTime end) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException(START_AFTER_END_ERROR);
        }
    }

    /**
     * Provides scheduling constraints for this availability slot.
     *
     * @return a string representation of the constraints
     */
    public String provideConstraints() {
        return "Available on " + day + " from " + start + " to " + end;
    }

    // Getters and setters
    public DayOfWeek getDay() {
        return day;
    }

    public void setDay(DayOfWeek day) {
        this.day = day;
    }

    public LocalTime getStart() {
        return start;
    }

    public void setStart(LocalTime start) {
        validateTimeRange(start, this.end);
        this.start = start;
    }

    public LocalTime getEnd() {
        return end;
    }

    public void setEnd(LocalTime end) {
        validateTimeRange(this.start, end);
        this.end = end;
    }
}