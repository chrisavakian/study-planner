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
 * Strategy that creates consistent time blocks for focused study sessions.
 */
public class TimeBlockSchedulerStrategy implements ISchedulerStrategy {
    private static final LocalTime DEFAULT_PREFERRED_START_TIME = LocalTime.of(9, 0);

    @Override
    public Schedule generateSchedule(List<Task> prioritizedList, List<Availability> availability) {
        if (prioritizedList == null || availability == null) {
            return new Schedule(LocalDate.now());
        }

        Schedule schedule = createWeeklySchedule();
        placeSessionsInTimeBlocks(prioritizedList, availability, schedule);

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
     * Places sessions in consistent time blocks across the week.
     *
     * @param taskList     List of tasks ordered by priority
     * @param availability the available time slots
     * @param schedule     the schedule to add sessions to
     */
    private void placeSessionsInTimeBlocks(List<Task> taskList, List<Availability> availability, Schedule schedule) {
        List<LocalDate> weekDays = generateWeekDays(schedule);

        // Try to create consistent time blocks for focused study
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

        return tryPlaceSessionWithPreferredTime(session, sessionDurationHours, task, day, dayAvailability, schedule) ||
               tryPlaceSessionInGaps(session, sessionDurationHours, task, day, dayAvailability, schedule);
    }

    private boolean tryPlaceSessionWithPreferredTime(Session session, long sessionDurationHours, Task task,
                                                   LocalDate day, Availability dayAvailability,
                                                   Schedule schedule) {
        LocalTime preferredStart = determinePreferredStartTime(dayAvailability);
        LocalDateTime dayStart = LocalDateTime.of(day, preferredStart);
        LocalDateTime dayEnd = LocalDateTime.of(day, dayAvailability.getEnd());

        LocalDateTime currentTime = dayStart;
        while (currentTime.plusHours(sessionDurationHours).isBefore(dayEnd)) {
            Session scheduledSession = new Session(currentTime, currentTime.plusHours(sessionDurationHours), task);

            if (!hasOverlap(scheduledSession, schedule)) {
                schedule.addSession(scheduledSession);
                return true;
            }

            // Move to next potential time block (e.g., 1 hour later)
            currentTime = currentTime.plusHours(1);

            // If moved past available time, break to try next day
            if (currentTime.plusHours(sessionDurationHours).isAfter(dayEnd)) {
                break;
            }
        }
        return false; // Couldn't place with preferred time
    }

    private LocalTime determinePreferredStartTime(Availability dayAvailability) {
        LocalTime preferredStart = DEFAULT_PREFERRED_START_TIME;
        // Adjust the preferred start time based on day availability
        if (dayAvailability.getStart().isAfter(preferredStart)) {
            preferredStart = dayAvailability.getStart();
        }
        return preferredStart;
    }

    private boolean tryPlaceSessionInGaps(Session session, long sessionDurationHours, Task task,
                                        LocalDate day, Availability dayAvailability,
                                        Schedule schedule) {
        // Find any available gap in the day
        List<Session> existingSessions = getDailySessions(schedule, day);
        sortSessionsByStartTime(existingSessions);

        LocalDateTime searchTime = LocalDateTime.of(day, dayAvailability.getStart());

        for (Session existingSession : existingSessions) {
            if (tryPlaceSessionBeforeExisting(searchTime, sessionDurationHours, task, existingSession, schedule)) {
                return true;
            }

            // Move search time to after the existing session
            searchTime = existingSession.getEndTime();
        }

        // Try to place at the end of the day
        return tryPlaceSessionAtEndOfDay(searchTime, sessionDurationHours, task, day, dayAvailability, schedule);
    }

    private boolean tryPlaceSessionBeforeExisting(LocalDateTime searchTime, long sessionDurationHours, Task task,
                                                Session existingSession, Schedule schedule) {
        LocalDateTime sessionEnd = searchTime.plusHours(sessionDurationHours);

        // Check if there's enough time before the existing session
        if (sessionEnd.isBefore(existingSession.getStartTime()) || sessionEnd.isEqual(existingSession.getStartTime())) {
            Session scheduledSession = new Session(searchTime, sessionEnd, task);
            if (!hasOverlap(scheduledSession, schedule)) {
                schedule.addSession(scheduledSession);
                return true;
            }
        }
        return false;
    }

    private boolean tryPlaceSessionAtEndOfDay(LocalDateTime searchTime, long sessionDurationHours, Task task,
                                            LocalDate day, Availability dayAvailability,
                                            Schedule schedule) {
        LocalDateTime dayEnd = LocalDateTime.of(day, dayAvailability.getEnd());
        LocalDateTime sessionEnd = searchTime.plusHours(sessionDurationHours);

        if (sessionEnd.isBefore(dayEnd) || sessionEnd.isEqual(dayEnd)) {
            Session scheduledSession = new Session(searchTime, sessionEnd, task);
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