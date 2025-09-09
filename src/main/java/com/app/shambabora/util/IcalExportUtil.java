package com.app.shambabora.util;

import com.app.shambabora.modules.recordskeeping.dto.FarmActivityResponse;
import com.app.shambabora.modules.recordskeeping.dto.ActivityReminderResponse;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class IcalExportUtil {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    public static String generateIcal(FarmActivityResponse activity, List<ActivityReminderResponse> reminders) {
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR\n");
        sb.append("VERSION:2.0\n");
        sb.append("PRODID:-//ShambaBora//EN\n");

        // Main activity event
        sb.append("BEGIN:VEVENT\n");
        sb.append("UID:activity-" + activity.getId() + "@shambabora\n");
        sb.append("DTSTAMP:" + activity.getCreatedAt().format(DATE_TIME_FORMATTER) + "\n");
        sb.append("DTSTART;VALUE=DATE:" + activity.getActivityDate().format(DateTimeFormatter.BASIC_ISO_DATE) + "\n");
        sb.append("SUMMARY:" + activity.getActivityType() + " - " + activity.getCropType() + "\n");
        if (activity.getDescription() != null) {
            sb.append("DESCRIPTION:" + activity.getDescription().replace("\n", " ") + "\n");
        }
        sb.append("END:VEVENT\n");

        // Reminders as events
        for (ActivityReminderResponse reminder : reminders) {
            sb.append("BEGIN:VEVENT\n");
            sb.append("UID:reminder-" + reminder.getId() + "@shambabora\n");
            sb.append("DTSTAMP:" + reminder.getCreatedAt().format(DATE_TIME_FORMATTER) + "\n");
            sb.append("DTSTART:" + reminder.getReminderDateTime().format(DATE_TIME_FORMATTER) + "\n");
            sb.append("SUMMARY:Reminder - " + reminder.getMessage() + "\n");
            sb.append("DESCRIPTION:Repeat: " + reminder.getRepeatInterval() + "\n");
            sb.append("END:VEVENT\n");
        }

        sb.append("END:VCALENDAR\n");
        return sb.toString();
    }
} 