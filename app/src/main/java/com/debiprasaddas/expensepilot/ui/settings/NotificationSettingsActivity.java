package com.debiprasaddas.expensepilot.ui.settings;

import android.os.Bundle;

import androidx.core.app.NotificationManagerCompat;

import com.debiprasaddas.expensepilot.R;
import com.debiprasaddas.expensepilot.databinding.ActivityNotificationSettingsBinding;
import com.debiprasaddas.expensepilot.ui.common.SecureActivity;
import com.debiprasaddas.expensepilot.util.NotificationScheduler;
import com.debiprasaddas.expensepilot.util.NotificationSettingsManager;

public class NotificationSettingsActivity extends SecureActivity {

    private ActivityNotificationSettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setLogo(R.drawable.app_logo_toolbar);
        binding.toolbar.setLogoDescription(getString(R.string.app_name));
        binding.toolbar.setNavigationOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_slide_out_right);
        });

        bindInitialState();
        setupListeners();
        updateDependentState();
    }

    private void bindInitialState() {
        binding.switchAllNotifications.setChecked(NotificationSettingsManager.isEnabled(this));
        binding.switchActivityReminders.setChecked(NotificationSettingsManager.isActivityRemindersEnabled(this));
        binding.switchOverspendingAlerts.setChecked(NotificationSettingsManager.isOverspendingAlertsEnabled(this));
        binding.switchGoalNudges.setChecked(NotificationSettingsManager.isGoalNudgesEnabled(this));
        binding.switchStreakReminders.setChecked(NotificationSettingsManager.isStreakRemindersEnabled(this));
    }

    private void setupListeners() {
        binding.switchAllNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            NotificationSettingsManager.setEnabled(this, isChecked);
            if (!isChecked) {
                NotificationManagerCompat.from(this).cancelAll();
            }
            updateDependentState();
            NotificationScheduler.scheduleImmediate(this);
        });

        binding.switchActivityReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            NotificationSettingsManager.setActivityRemindersEnabled(this, isChecked);
            NotificationScheduler.scheduleImmediate(this);
        });

        binding.switchOverspendingAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            NotificationSettingsManager.setOverspendingAlertsEnabled(this, isChecked);
            NotificationScheduler.scheduleImmediate(this);
        });

        binding.switchGoalNudges.setOnCheckedChangeListener((buttonView, isChecked) -> {
            NotificationSettingsManager.setGoalNudgesEnabled(this, isChecked);
            NotificationScheduler.scheduleImmediate(this);
        });

        binding.switchStreakReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            NotificationSettingsManager.setStreakRemindersEnabled(this, isChecked);
            NotificationScheduler.scheduleImmediate(this);
        });
    }

    private void updateDependentState() {
        boolean enabled = binding.switchAllNotifications.isChecked();
        binding.switchActivityReminders.setEnabled(enabled);
        binding.switchOverspendingAlerts.setEnabled(enabled);
        binding.switchGoalNudges.setEnabled(enabled);
        binding.switchStreakReminders.setEnabled(enabled);

        float alpha = enabled ? 1f : 0.45f;
        binding.cardActivityReminders.setAlpha(alpha);
        binding.cardOverspendingAlerts.setAlpha(alpha);
        binding.cardGoalNudges.setAlpha(alpha);
        binding.cardStreakReminders.setAlpha(alpha);
    }
}
