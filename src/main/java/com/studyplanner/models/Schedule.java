package com.studyplanner.models;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a weekly study schedule with sessions.
 */
public class Schedule {
    private LocalDate weekStart; // Monday of the week
    private List<Session> sessions;

    /**
     * Constructs a new Schedule for the week starting on the specified date.
     *
     * @param weekStart the start date of the week (Monday)
     */
    public Schedule(LocalDate weekStart) {
        this.weekStart = weekStart;
        this.sessions = new ArrayList<>();
    }

    /**
     * Generates a weekly schedule.
     */
    public void generateWeekly() {
        // In a real implementation, this would populate the schedule
        // For now, we just have an empty schedule ready to receive sessions
    }

    /**
     * Views the daily schedule for the specified day.
     *
     * @param day the day of the week to view
     * @return a list of sessions for the specified day
     */
    public List<Session> viewDaily(DayOfWeek day) {
        List<Session> dailySessions = new ArrayList<>();

        for (Session session : sessions) {
            if (isSessionOnDay(session, day)) {
                dailySessions.add(session);
            }
        }

        return dailySessions;
    }

    private boolean isSessionOnDay(Session session, DayOfWeek day) {
        return session.getStartTime().getDayOfWeek() == day;
    }

    // Getters and setters
    public LocalDate getWeekStart() {
        return weekStart;
    }

    public void setWeekStart(LocalDate weekStart) {
        this.weekStart = weekStart;
    }

    public List<Session> getSessions() {
        return sessions;
    }

    public void setSessions(List<Session> sessions) {
        this.sessions = sessions;
    }

    public void addSession(Session session) {
        this.sessions.add(session);
    }
}