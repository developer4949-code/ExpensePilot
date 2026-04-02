package com.debiprasaddas.expensepilot.util;

import android.content.Context;
import android.content.SharedPreferences;

public final class NotificationSettingsManager {

    private static final String PREFS_NAME = "expensepilot_notification_settings";
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_ACTIVITY_REMINDERS = "activity_reminders";
    private static final String KEY_OVERSPENDING_ALERTS = "overspending_alerts";
    private static final String KEY_GOAL_NUDGES = "goal_nudges";
    private static final String KEY_STREAK_REMINDERS = "streak_reminders";

    private NotificationSettingsManager() {
    }

    public static boolean isEnabled(Context context) {
        return preferences(context).getBoolean(KEY_ENABLED, true);
    }

    public static void setEnabled(Context context, boolean enabled) {
        preferences(context).edit().putBoolean(KEY_ENABLED, enabled).apply();
    }

    public static boolean isActivityRemindersEnabled(Context context) {
        return preferences(context).getBoolean(KEY_ACTIVITY_REMINDERS, true);
    }

    public static void setActivityRemindersEnabled(Context context, boolean enabled) {
        preferences(context).edit().putBoolean(KEY_ACTIVITY_REMINDERS, enabled).apply();
    }

    public static boolean isOverspendingAlertsEnabled(Context context) {
        return preferences(context).getBoolean(KEY_OVERSPENDING_ALERTS, true);
    }

    public static void setOverspendingAlertsEnabled(Context context, boolean enabled) {
        preferences(context).edit().putBoolean(KEY_OVERSPENDING_ALERTS, enabled).apply();
    }

    public static boolean isGoalNudgesEnabled(Context context) {
        return preferences(context).getBoolean(KEY_GOAL_NUDGES, true);
    }

    public static void setGoalNudgesEnabled(Context context, boolean enabled) {
        preferences(context).edit().putBoolean(KEY_GOAL_NUDGES, enabled).apply();
    }

    public static boolean isStreakRemindersEnabled(Context context) {
        return preferences(context).getBoolean(KEY_STREAK_REMINDERS, true);
    }

    public static void setStreakRemindersEnabled(Context context, boolean enabled) {
        preferences(context).edit().putBoolean(KEY_STREAK_REMINDERS, enabled).apply();
    }

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
