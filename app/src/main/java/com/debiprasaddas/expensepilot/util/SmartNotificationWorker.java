package com.debiprasaddas.expensepilot.util;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.debiprasaddas.expensepilot.R;
import com.debiprasaddas.expensepilot.auth.AuthGateActivity;
import com.debiprasaddas.expensepilot.data.dao.GoalDao;
import com.debiprasaddas.expensepilot.data.dao.TransactionDao;
import com.debiprasaddas.expensepilot.data.db.AppDatabase;
import com.debiprasaddas.expensepilot.data.entity.GoalEntity;
import com.debiprasaddas.expensepilot.data.entity.TransactionEntity;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SmartNotificationWorker extends Worker {

    private static final String CHANNEL_ID = "smart_reminders";
    private static final String PREFS_NAME = "expensepilot_notifications";
    private static final String KEY_LAST_NOTIFICATION_KEY = "last_notification_key";
    private static final String KEY_LAST_NOTIFICATION_AT = "last_notification_at";

    public SmartNotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        if (!NotificationSettingsManager.isEnabled(context)) {
            return Result.success();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return Result.success();
        }

        AppDatabase database = AppDatabase.getInstance(context);
        TransactionDao transactionDao = database.transactionDao();
        GoalDao goalDao = database.goalDao();

        List<TransactionEntity> transactions = transactionDao.getAllTransactionsSync();
        Calendar calendar = Calendar.getInstance();
        GoalEntity goalEntity = goalDao.getGoalForMonthSync(calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));

        NotificationPayload payload = buildPayload(transactions, goalEntity);
        if (payload == null || shouldSkip(context, payload.key)) {
            return Result.success();
        }

        createChannel(context);
        Intent launchIntent = new Intent(context, AuthGateActivity.class);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                100,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.app_logo)
                .setContentTitle(payload.title)
                .setContentText(payload.message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(payload.message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat.from(context).notify(payload.notificationId, builder.build());
        remember(context, payload.key);
        return Result.success();
    }

    private NotificationPayload buildPayload(List<TransactionEntity> transactions, GoalEntity goalEntity) {
        boolean activityRemindersEnabled = NotificationSettingsManager.isActivityRemindersEnabled(getApplicationContext());
        boolean overspendingAlertsEnabled = NotificationSettingsManager.isOverspendingAlertsEnabled(getApplicationContext());
        boolean goalNudgesEnabled = NotificationSettingsManager.isGoalNudgesEnabled(getApplicationContext());
        boolean streakRemindersEnabled = NotificationSettingsManager.isStreakRemindersEnabled(getApplicationContext());

        if ((transactions == null || transactions.isEmpty()) && activityRemindersEnabled) {
            return new NotificationPayload("first-log", 1, "Start your first money log", "Add one transaction today so ExpensePilot can begin tracking your spending rhythm.");
        }
        if (transactions == null || transactions.isEmpty()) {
            return null;
        }

        FinanceAnalytics.InsightSnapshot insightSnapshot = FinanceAnalytics.buildInsights(transactions);
        FinanceAnalytics.DashboardSummary dashboardSummary = FinanceAnalytics.calculateDashboard(transactions, goalEntity);
        List<FinanceAnalytics.CategoryTotal> categories = FinanceAnalytics.categoryBreakdown(transactions);
        String topCategory = categories.isEmpty() ? "spending" : categories.get(0).category;

        long lastTransactionTime = transactions.get(0).getDate();
        long hoursSinceLastLog = (System.currentTimeMillis() - lastTransactionTime) / (60 * 60 * 1000);
        if (activityRemindersEnabled && hoursSinceLastLog >= 30) {
            return new NotificationPayload("log-reminder", 2, "Keep your money story updated", "You haven't logged a transaction in a while. Add today's entries to keep insights sharp.");
        }

        if (overspendingAlertsEnabled && insightSnapshot.lastWeek > 0 && insightSnapshot.thisWeek > insightSnapshot.lastWeek * 1.2d) {
            return new NotificationPayload("overspend-" + weekKey(), 3, "Spending spike detected", "This week's spend is running higher than last week. Check " + topCategory + " first to bring it back under control.");
        }

        if (goalNudgesEnabled && goalEntity != null && goalEntity.getTargetAmount() > 0) {
            Calendar calendar = Calendar.getInstance();
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            int totalDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            double expectedProgress = (dayOfMonth / (double) totalDays) * 100d;
            if (dashboardSummary.goalProgress + 12d < expectedProgress) {
                return new NotificationPayload("goal-nudge-" + monthKey(), 4, "Savings goal needs a nudge", "You are a bit behind your monthly goal. A small transfer today can help recover your savings pace.");
            }
        }

        if (streakRemindersEnabled && insightSnapshot.noSpendStreak <= 1 && insightSnapshot.thisWeek > 0) {
            return new NotificationPayload("streak-rescue-" + dayKey(), 5, "Protect your streak today", "Try a low-spend day today. Avoiding one impulsive " + topCategory.toLowerCase(Locale.ENGLISH) + " expense can restart momentum.");
        }

        return null;
    }

    private boolean shouldSkip(Context context, String key) {
        SharedPreferences preferences = preferences(context);
        String lastKey = preferences.getString(KEY_LAST_NOTIFICATION_KEY, "");
        long lastAt = preferences.getLong(KEY_LAST_NOTIFICATION_AT, 0L);
        long now = System.currentTimeMillis();
        if (key.equals(lastKey) && now - lastAt < 12L * 60L * 60L * 1000L) {
            return true;
        }
        return now - lastAt < 4L * 60L * 60L * 1000L;
    }

    private void remember(Context context, String key) {
        preferences(context).edit()
                .putString(KEY_LAST_NOTIFICATION_KEY, key)
                .putLong(KEY_LAST_NOTIFICATION_AT, System.currentTimeMillis())
                .apply();
    }

    private SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager == null) {
                return;
            }
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(context.getString(R.string.notification_channel_description));
            notificationManager.createNotificationChannel(channel);
        }
    }

    private String weekKey() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.WEEK_OF_YEAR);
    }

    private String monthKey() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1);
    }

    private String dayKey() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
    }

    private static class NotificationPayload {
        final String key;
        final int notificationId;
        final String title;
        final String message;

        NotificationPayload(String key, int notificationId, String title, String message) {
            this.key = key;
            this.notificationId = notificationId;
            this.title = title;
            this.message = message;
        }
    }
}
