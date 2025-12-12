package com.studyplanner.scheduler;

import com.studyplanner.models.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Strategy that creates consistent time blocks for focused study sessions.
 */
public class TimeBlockSchedulerStrategy implements ISchedulerStrategy {

    @Override
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

        // Place sessions using time-block placement
        placeSessionsInTimeBlocks(prioritizedList, availability, schedule);

        return schedule;
    }

    /**
     * Places sessions in consistent time blocks across the week.
     *
     * @param taskList     List of tasks ordered by priority
     * @param availability the available time slots
     * @param schedule     the schedule to add sessions to
     */
    private void placeSessionsInTimeBlocks(List<Task> taskList, List<Availability> availability, Schedule schedule) {
        // Create a mapping of available time slots by day
        List<LocalDate> weekDays = new ArrayList<>();
        LocalDate currentDay = schedule.getWeekStart();
        for (int i = 0; i < 7; i++) {
            weekDays.add(currentDay.plusDays(i));
        }

        // Try to create consistent time blocks for focused study
        for (Task task : taskList) {
            List<Session> sessions = task.splitIntoSessions();

            // Try to place each session of this task
            for (Session session : sessions) {
                // Calculate session duration from the time difference in the placeholder session
                long sessionDurationHours = java.time.Duration.between(session.getStartTime(), session.getEndTime()).toHours();

                boolean placed = false;

                // Try to find consistent blocks across days (e.g., same time each day)
                // First, try to find available consistent blocks
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
                        // For time blocking, try to place sessions at consistent times
                        // Start with morning hours as an example (9 AM - 11 AM)
                        LocalTime preferredStart = LocalTime.of(9, 0);
                        
                        // Adjust the preferred start time based on day availability
                        if (dayAvailability.getStart().isAfter(preferredStart)) {
                            preferredStart = dayAvailability.getStart();
                        }

                        LocalDateTime dayStart = LocalDateTime.of(day, preferredStart);
                        LocalDateTime dayEnd = LocalDateTime.of(day, dayAvailability.getEnd());

                        // Try different time blocks for this day
                        LocalDateTime currentTime = dayStart;

                        while (!placed && currentTime.plusHours(sessionDurationHours).isBefore(dayEnd)) {
                            Session scheduledSession = new Session(currentTime, currentTime.plusHours(sessionDurationHours), task);

                            if (!findOverlap(scheduledSession, schedule)) {
                                schedule.addSession(scheduledSession);
                                placed = true;
                                break;
                            }

                            // Move to next potential time block (e.g., 1 hour later)
                            currentTime = currentTime.plusHours(1);

                            // If moved past available time, break to try next day
                            if (currentTime.plusHours(sessionDurationHours).isAfter(dayEnd)) {
                                break;
                            }
                        }
                        
                        // If we couldn't find a time block yet, try a different approach
                        if (!placed) {
                            // Find any available gap in the day
                            List<Session> existingSessions = new ArrayList<>();
                            for (Session existingSession : schedule.getSessions()) {
                                if (existingSession.getStartTime().toLocalDate().equals(day)) {
                                    existingSessions.add(existingSession);
                                }
                            }

                            // Sort existing sessions by start time
                            existingSessions.sort((s1, s2) ->
                                s1.getStartTime().compareTo(s2.getStartTime()));

                            LocalDateTime searchTime = LocalDateTime.of(day, dayAvailability.getStart());

                            for (Session existingSession : existingSessions) {
                                if (placed) break;

                                LocalDateTime sessionEnd = searchTime.plusHours(sessionDurationHours);

                                // Check if there's enough time before the existing session
                                if (sessionEnd.isBefore(existingSession.getStartTime()) ||
                                    sessionEnd.isEqual(existingSession.getStartTime())) {

                                    // Create and add the session
                                    Session scheduledSession = new Session(searchTime, sessionEnd, task);
                                    if (!findOverlap(scheduledSession, schedule)) {
                                        schedule.addSession(scheduledSession);
                                        placed = true;
                                        break;
                                    }
                                }

                                // Move search time to after the existing session
                                searchTime = existingSession.getEndTime();
                            }

                            // If still not placed, try to place at the end of the day
                            if (!placed) {
                                LocalDateTime sessionEnd = searchTime.plusHours(sessionDurationHours);

                                if (sessionEnd.isBefore(dayEnd) || sessionEnd.isEqual(dayEnd)) {
                                    // Create and add the session
                                    Session scheduledSession = new Session(searchTime, sessionEnd, task);
                                    if (!findOverlap(scheduledSession, schedule)) {
                                        schedule.addSession(scheduledSession);
                                        placed = true;
                                    }
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
    private boolean findOverlap(Session session, Schedule schedule) {
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