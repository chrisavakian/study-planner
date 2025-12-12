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
 * Strategy that balances urgent tasks with optimal study times for effectiveness.
 */
public class BalancedSchedulerStrategy implements ISchedulerStrategy {
    private static final LocalTime MORNING_OPTIMAL_TIME = LocalTime.of(10, 0);
    private static final LocalTime AFTERNOON_OPTIMAL_TIME = LocalTime.of(14, 0);

    @Override
    public Schedule generateSchedule(List<Task> prioritizedList, List<Availability> availability) {
        if (prioritizedList == null || availability == null) {
            return new Schedule(LocalDate.now());
        }

        Schedule schedule = createWeeklySchedule();
        placeSessionsBalanced(prioritizedList, availability, schedule);

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
     * Places sessions balancing urgent deadlines with optimal study times.
     *
     * @param taskList     List of tasks ordered by priority
     * @param availability the available time slots
     * @param schedule     the schedule to add sessions to
     */
    private void placeSessionsBalanced(List<Task> taskList, List<Availability> availability, Schedule schedule) {
        List<LocalDate> weekDays = generateWeekDays(schedule);

        // Process each task and its sessions
        for (Task task : taskList) {
            List<Session> sessions = task.splitIntoSessions();

            // Try to place each session of this task
            for (Session session : sessions) {
                long sessionDurationHours = calculateSessionDuration(session);
                attemptSessionPlacement(session, sessionDurationHours, task, weekDays, availability, schedule);
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

    private void attemptSessionPlacement(Session session, long sessionDurationHours, Task task,
                                       List<LocalDate> weekDays, List<Availability> availability,
                                       Schedule schedule) {
        for (LocalDate day : weekDays) {
            if (tryPlaceSessionOnDay(session, sessionDurationHours, task, day, availability, schedule)) {
                return; // Session successfully placed, move to next session
            }
        }
        // If we couldn't place this session, consider it unplaced
    }

    private boolean tryPlaceSessionOnDay(Session session, long sessionDurationHours, Task task,
                                       LocalDate day, List<Availability> availability,
                                       Schedule schedule) {
        Availability dayAvailability = findAvailabilityForDay(day, availability);
        if (dayAvailability == null) {
            return false;
        }

        return tryPlaceSessionAtOptimalTimes(session, sessionDurationHours, task, day, dayAvailability, schedule) ||
               tryPlaceSessionInGaps(session, sessionDurationHours, task, day, dayAvailability, schedule);
    }

    private boolean tryPlaceSessionAtOptimalTimes(Session session, long sessionDurationHours, Task task,
                                                LocalDate day, Availability dayAvailability,
                                                Schedule schedule) {
        List<LocalTime> preferredTimes = determinePreferredTimes(dayAvailability);
        LocalDateTime dayEndTime = LocalDateTime.of(day, dayAvailability.getEnd());

        for (LocalTime preferredTime : preferredTimes) {
            LocalTime actualTime = adjustTimeToAvailability(preferredTime, dayAvailability);

            LocalDateTime sessionStart = LocalDateTime.of(day, actualTime);
            LocalDateTime sessionEnd = sessionStart.plusHours(sessionDurationHours);

            // Check if the session would extend beyond available time
            if (sessionEnd.isAfter(dayEndTime)) {
                continue; // Skip if session would extend beyond available time
            }

            Session scheduledSession = new Session(sessionStart, sessionEnd, task);

            if (!hasOverlap(scheduledSession, schedule)) {
                schedule.addSession(scheduledSession);
                return true;
            }
        }
        return false; // Couldn't place at optimal times
    }

    private List<LocalTime> determinePreferredTimes(Availability dayAvailability) {
        List<LocalTime> preferredTimes = new ArrayList<>();
        preferredTimes.add(dayAvailability.getStart().isAfter(MORNING_OPTIMAL_TIME) ?
                          dayAvailability.getStart() : MORNING_OPTIMAL_TIME); // Morning
        preferredTimes.add(AFTERNOON_OPTIMAL_TIME); // Afternoon
        return preferredTimes;
    }

    private LocalTime adjustTimeToAvailability(LocalTime preferredTime, Availability dayAvailability) {
        // Adjust if preferred time is outside availability
        if (dayAvailability.getStart().isAfter(preferredTime)) {
            return dayAvailability.getStart();
        }
        return preferredTime;
    }

    private boolean tryPlaceSessionInGaps(Session session, long sessionDurationHours, Task task,
                                        LocalDate day, Availability dayAvailability,
                                        Schedule schedule) {
        List<Session> existingSessions = getDailySessions(schedule, day);
        sortSessionsByStartTime(existingSessions);

        LocalDateTime currentTime = LocalDateTime.of(day, dayAvailability.getStart());

        for (Session existingSession : existingSessions) {
            if (tryPlaceSessionInGap(session, sessionDurationHours, task, currentTime, existingSession, schedule)) {
                return true;
            }

            // Move current time to after the existing session
            currentTime = existingSession.getEndTime();
        }

        // Try to place at the end of the day
        return tryPlaceSessionAtEndOfDay(session, sessionDurationHours, task, currentTime, day, dayAvailability, schedule);
    }

    private boolean tryPlaceSessionInGap(Session session, long sessionDurationHours, Task task,
                                       LocalDateTime currentTime, Session existingSession,
                                       Schedule schedule) {
        LocalDateTime sessionEnd = currentTime.plusHours(sessionDurationHours);

        // Check if there's enough time before the existing session
        if (sessionEnd.isBefore(existingSession.getStartTime()) || sessionEnd.isEqual(existingSession.getStartTime())) {
            Session scheduledSession = new Session(currentTime, sessionEnd, task);
            if (!hasOverlap(scheduledSession, schedule)) {
                schedule.addSession(scheduledSession);
                return true;
            }
        }
        return false;
    }

    private boolean tryPlaceSessionAtEndOfDay(Session session, long sessionDurationHours, Task task,
                                           LocalDateTime currentTime, LocalDate day,
                                           Availability dayAvailability, Schedule schedule) {
        LocalDateTime dayEnd = LocalDateTime.of(day, dayAvailability.getEnd());
        LocalDateTime sessionEnd = currentTime.plusHours(sessionDurationHours);

        if (sessionEnd.isBefore(dayEnd) || sessionEnd.isEqual(dayEnd)) {
            Session scheduledSession = new Session(currentTime, sessionEnd, task);
            if (!hasOverlap(scheduledSession, schedule)) {
                schedule.addSession(scheduledSession);
                return true;
            }
        }
        return false;
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