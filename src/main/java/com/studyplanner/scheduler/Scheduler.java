package com.studyplanner.scheduler;

import com.studyplanner.models.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Scheduler that generates study schedules based on prioritized tasks and availability.
 */
public class Scheduler {

    /**
     * Generates a schedule based on the prioritized list of tasks and student availability.
     *
     * @param prioritizedList the list of tasks ordered by priority
     * @param availability    the student's available time slots
     * @return a Schedule object with planned sessions
     */
    public Schedule generateSchedule(List<Task> prioritizedList, List<Availability> availability) {
        if (prioritizedList == null || availability == null) {
            return new Schedule(LocalDate.now());
        }

        // Create a schedule for the current week
        LocalDate weekStart = LocalDate.now();
        while (weekStart.getDayOfWeek() != DayOfWeek.MONDAY) {
            weekStart = weekStart.minusDays(1);
        }
        
        Schedule schedule = new Schedule(weekStart);
        
        // Place sessions for each task, linking them to their tasks
        placeSessions(prioritizedList, availability, schedule);

        return schedule;
    }

    /**
     * Places sessions into available time slots.
     * This version properly distributes task sessions across available time slots.
     *
     * @param taskList     List of tasks with their associated sessions
     * @param availability the available time slots
     * @param schedule     the schedule to add sessions to
     */
    public void placeSessions(List<Task> taskList, List<Availability> availability, Schedule schedule) {
        // Create a mapping of available time slots by day
        List<LocalDate> weekDays = new ArrayList<>();
        LocalDate currentDay = schedule.getWeekStart();
        for (int i = 0; i < 7; i++) {
            weekDays.add(currentDay.plusDays(i));
        }
        
        // Process each task and its sessions
        for (Task task : taskList) {
            List<Session> sessions = task.splitIntoSessions();
            
            // Try to place each session of this task
            for (Session session : sessions) {
                // Calculate session duration from the time difference in the placeholder session
                long sessionDurationHours = java.time.Duration.between(session.getStartTime(), session.getEndTime()).toHours();
                
                boolean placed = false;
                
                // Try each day of the week in order
                for (LocalDate day : weekDays) {
                    if (placed) break;
                    
                    // Find the availability slot for this day
                    Availability dayAvailability = null;
                    for (Availability avail : availability) {
                        if (avail.getDay() == day.getDayOfWeek()) {
                            dayAvailability = avail;
                            break;
                        }
                    }
                    
                    if (dayAvailability != null) {
                        // Try to place the session in available time
                        LocalDateTime dayStart = LocalDateTime.of(day, dayAvailability.getStart());
                        LocalDateTime dayEnd = LocalDateTime.of(day, dayAvailability.getEnd());
                        
                        // Look for available time slots on this day
                        // First, get all existing sessions on this day
                        List<Session> existingSessions = new ArrayList<>();
                        for (Session existingSession : schedule.getSessions()) {
                            if (existingSession.getStartTime().toLocalDate().equals(day)) {
                                existingSessions.add(existingSession);
                            }
                        }
                        
                        // Sort existing sessions by start time
                        existingSessions.sort((s1, s2) -> 
                            s1.getStartTime().compareTo(s2.getStartTime()));
                        
                        // Try to place the session in gaps between existing sessions
                        LocalDateTime currentTime = dayStart;
                        
                        for (Session existingSession : existingSessions) {
                            if (placed) break;
                            
                            LocalDateTime sessionEnd = currentTime.plusHours(sessionDurationHours);
                            
                            // Check if there's enough time before the existing session
                            if (sessionEnd.isBefore(existingSession.getStartTime()) || 
                                sessionEnd.isEqual(existingSession.getStartTime())) {
                                
                                // Create and add the session
                                Session scheduledSession = new Session(currentTime, sessionEnd, task);
                                if (!findOverlap(scheduledSession, schedule)) {
                                    schedule.addSession(scheduledSession);
                                    placed = true;
                                    break;
                                }
                            }
                            
                            // Move current time to after the existing session
                            currentTime = existingSession.getEndTime();
                        }
                        
                        // If still not placed, try to place at the end of the day
                        if (!placed) {
                            LocalDateTime sessionEnd = currentTime.plusHours(sessionDurationHours);
                            
                            if (sessionEnd.isBefore(dayEnd) || sessionEnd.isEqual(dayEnd)) {
                                // Create and add the session
                                Session scheduledSession = new Session(currentTime, sessionEnd, task);
                                if (!findOverlap(scheduledSession, schedule)) {
                                    schedule.addSession(scheduledSession);
                                    placed = true;
                                }
                            }
                        }
                    }
                }
                
                // If we couldn't place this session, consider it unplaced
            }
        }
    }

    /**
     * Checks if there is an overlap between a session and existing schedule.
     *
     * @param session  the session to check for overlap
     * @param schedule the schedule to check against
     * @return true if there is an overlap, false otherwise
     */
    public boolean findOverlap(Session session, Schedule schedule) {
        for (Session existingSession : schedule.getSessions()) {
            // Check if sessions are on the same day
            if (existingSession.getStartTime().toLocalDate()
                    .isEqual(session.getStartTime().toLocalDate())) {
                
                // Check for time overlap
                if ((session.getStartTime().isAfter(existingSession.getStartTime()) && 
                     session.getStartTime().isBefore(existingSession.getEndTime())) ||
                    (session.getEndTime().isAfter(existingSession.getStartTime()) && 
                     session.getEndTime().isBefore(existingSession.getEndTime())) ||
                    (session.getStartTime().isBefore(existingSession.getStartTime()) && 
                     session.getEndTime().isAfter(existingSession.getEndTime())) ||
                    (session.getStartTime().isEqual(existingSession.getStartTime()) ||
                     session.getEndTime().isEqual(existingSession.getEndTime()))) {
                    return true;
                }
            }
        }
        return false;
    }
}