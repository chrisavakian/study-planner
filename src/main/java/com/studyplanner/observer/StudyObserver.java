package com.studyplanner.observer;

import com.studyplanner.models.Session;
import com.studyplanner.models.Task;

/**
 * Observer interface for study-related notifications.
 */
public interface StudyObserver {
    void onSessionCompleted(Session session);
    void onTaskAdded(Task task);
    void onTaskDeadlineApproaching(Task task);
}