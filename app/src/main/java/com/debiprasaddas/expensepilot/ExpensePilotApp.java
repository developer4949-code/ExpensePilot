package com.debiprasaddas.expensepilot;

import android.app.Application;

import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.debiprasaddas.expensepilot.util.BiometricLockManager;
import com.debiprasaddas.expensepilot.util.NotificationScheduler;
import com.debiprasaddas.expensepilot.util.ThemeManager;

public class ExpensePilotApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ThemeManager.applySavedTheme(this);
        NotificationScheduler.schedulePeriodic(this);
        NotificationScheduler.scheduleImmediate(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new DefaultLifecycleObserver() {
            @Override
            public void onStop(LifecycleOwner owner) {
                BiometricLockManager.markLockRequired();
            }
        });
    }
}
