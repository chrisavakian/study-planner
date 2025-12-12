package com.studyplanner.scheduler;

import com.studyplanner.models.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Strategy that prioritizes tasks based on their urgency (deadline proximity).
 */
public class PrioritySchedulerStrategy implements ISchedulerStrategy {

    @Override
    public Schedule generateSchedule(List<Task> prioritizedList, List<Availability> availability) {
        if (prioritizedList == null || availability == null) {
            return new Schedule(LocalDate.now());
        }

        Schedule schedule = createWeeklySchedule();
        placeSessionsByPriority(prioritizedList, availability, schedule);

        return schedule;
    }

    private Schedule createWeeklySchedule() {
        LocalDate weekStart = LocalDate.now();
        while (weekStart.getDayOfWeek() != DayOfWeek.MONDAY) {
            weekStart = weekStart.minusDays(1);
        }
        return new Schedule(weekStart);
    }

    /**
     * Places sessions prioritizing tasks with closer deadlines.
     *
     * @param taskList     List of tasks ordered by priority
     * @param availability the available time slots
     * @param schedule     the schedule to add sessions to
     */
    private void placeSessionsByPriority(List<Task> taskList, List<Availability> availability, Schedule schedule) {
        List<LocalDate> weekDays = generateWeekDays(schedule);

        // Process each task and its sessions
        for (Task task : taskList) {
            List<Session> sessions = task.splitIntoSessions();

            // Try to place each session of this task
            for (Session session : sessions) {
                long sessionDurationHours = calculateSessionDuration(session);
                boolean placed = attemptSessionPlacement(session, sessionDurationHours, weekDays, availability, schedule);

                // If we couldn't place this session, consider it unplaced
            }
        }
    }

    private List<LocalDate> generateWeekDays(Schedule schedule) {
        List<LocalDate> weekDays = new ArrayList<>();
        LocalDate currentDay = schedule.getWeekStart();
        for (int i = 0; i < 7; i++) {
            weekDays.add(currentDay.plusDays(i));
        }
        return weekDays;
    }

    private long calculateSessionDuration(Session session) {
        return ChronoUnit.HOURS.between(session.getStartTime(), session.getEndTime());
    }

    private boolean attemptSessionPlacement(Session session, long sessionDurationHours,
                                          List<LocalDate> weekDays, List<Availability> availability,
                                          Schedule schedule) {
        for (LocalDate day : weekDays) {
            if (attemptPlacementOnDay(session, sessionDurationHours, day, availability, schedule)) {
                return true; // Successfully placed the session
            }
        }
        return false; // Could not place the session
    }

    private boolean attemptPlacementOnDay(Session session, long sessionDurationHours,
                                        LocalDate day, List<Availability> availability,
                                        Schedule schedule) {
        Availability dayAvailability = findAvailabilityForDay(day, availability);
        if (dayAvailability == null) {
            return false;
        }

        LocalDateTime dayStart = LocalDateTime.of(day, dayAvailability.getStart());
        LocalDateTime dayEnd = LocalDateTime.of(day, dayAvailability.getEnd());

        // Get all existing sessions on this day
        List<Session> existingSessions = getDailySessions(schedule, day);
        // Sort existing sessions by start time
        sortSessionsByStartTime(existingSessions);

        // Try to place the session in gaps between existing sessions
        LocalDateTime currentTime = dayStart;

        for (Session existingSession : existingSessions) {
            if (tryPlaceSessionInGap(session, sessionDurationHours, currentTime, existingSession, schedule)) {
                return true;
            }

            // Move current time to after the existing session
            currentTime = existingSession.getEndTime();
        }

        // If still not placed, try to place at the end of the day
        return tryPlaceSessionAtEndOfDay(session, sessionDurationHours, currentTime, dayEnd, schedule);
    }

    private Availability findAvailabilityForDay(LocalDate day, List<Availability> availability) {
        for (Availability avail : availability) {
            if (avail.getDay() == day.getDayOfWeek()) {
                return avail;
            }
        }
        return null;
    }

    private List<Session> getDailySessions(Schedule schedule, LocalDate day) {
        List<Session> dailySessions = new ArrayList<>();
        for (Session existingSession : schedule.getSessions()) {
            if (existingSession.getStartTime().toLocalDate().equals(day)) {
                dailySessions.add(existingSession);
            }
        }
        return dailySessions;
    }

    private void sortSessionsByStartTime(List<Session> existingSessions) {
        existingSessions.sort((s1, s2) -> s1.getStartTime().compareTo(s2.getStartTime()));
    }

    private boolean tryPlaceSessionInGap(Session session, long sessionDurationHours,
                                       LocalDateTime currentTime, Session existingSession,
                                       Schedule schedule) {
        LocalDateTime sessionEnd = currentTime.plusHours(sessionDurationHours);

        // Check if there's enough time before the existing session
        if (isTimeAvailableBeforeExistingSession(sessionEnd, existingSession)) {
            Session scheduledSession = new Session(currentTime, sessionEnd, session.getTask());
            if (!hasOverlap(scheduledSession, schedule)) {
                schedule.addSession(scheduledSession);
                return true;
            }
        }
        return false;
    }

    private boolean isTimeAvailableBeforeExistingSession(LocalDateTime sessionEnd, Session existingSession) {
        return sessionEnd.isBefore(existingSession.getStartTime()) ||
               sessionEnd.isEqual(existingSession.getStartTime());
    }

    private boolean tryPlaceSessionAtEndOfDay(Session session, long sessionDurationHours,
                                           LocalDateTime currentTime, LocalDateTime dayEnd,
                                           Schedule schedule) {
        LocalDateTime sessionEnd = currentTime.plusHours(sessionDurationHours);

        if (sessionEnd.isBefore(dayEnd) || sessionEnd.isEqual(dayEnd)) {
            Session scheduledSession = new Session(currentTime, sessionEnd, session.getTask());
            if (!hasOverlap(scheduledSession, schedule)) {
                schedule.addSession(scheduledSession);
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if there is an overlap between a session and existing schedule.
     *
     * @param session  the session to check for overlap
     * @param schedule the schedule to check against
     * @return true if there is an overlap, false otherwise
     */
    private boolean hasOverlap(Session session, Schedule schedule) {
        for (Session existingSession : schedule.getSessions()) {
            // Check if sessions are on the same day
            if (isOnSameDay(existingSession, session)) {
                // Check for time overlap
                if (hasTimeOverlap(session, existingSession)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isOnSameDay(Session existingSession, Session session) {
        return existingSession.getStartTime().toLocalDate()
                .isEqual(session.getStartTime().toLocalDate());
    }

    private boolean hasTimeOverlap(Session session, Session existingSession) {
        return (session.getStartTime().isAfter(existingSession.getStartTime()) &&
                 session.getStartTime().isBefore(existingSession.getEndTime())) ||
               (session.getEndTime().isAfter(existingSession.getStartTime()) &&
                 session.getEndTime().isBefore(existingSession.getEndTime())) ||
               (session.getStartTime().isBefore(existingSession.getStartTime()) &&
                 session.getEndTime().isAfter(existingSession.getEndTime())) ||
               (session.getStartTime().isEqual(existingSession.getStartTime()) ||
                 session.getEndTime().isEqual(existingSession.getEndTime()));
    }
}