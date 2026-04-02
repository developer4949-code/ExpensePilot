package com.debiprasaddas.expensepilot.util;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public final class NotificationScheduler {

    private static final String PERIODIC_WORK_NAME = "expensepilot_smart_notifications";
    private static final String IMMEDIATE_WORK_NAME = "expensepilot_smart_notifications_immediate";

    private NotificationScheduler() {
    }

    public static void schedulePeriodic(Context context) {
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(SmartNotificationWorker.class, 8, TimeUnit.HOURS)
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .build())
                .build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
        );
    }

    public static void scheduleImmediate(Context context) {
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SmartNotificationWorker.class)
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .build())
                .build();
        WorkManager.getInstance(context).enqueueUniqueWork(
                IMMEDIATE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                workRequest
        );
    }
}
