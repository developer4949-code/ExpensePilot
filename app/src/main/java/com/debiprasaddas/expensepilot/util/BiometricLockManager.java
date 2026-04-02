package com.debiprasaddas.expensepilot.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.biometric.BiometricManager;

public final class BiometricLockManager {

    private static final String PREFS_NAME = "expense_pilot_security";
    private static final String KEY_ENABLED = "biometric_enabled";
    private static volatile boolean lockRequired = true;

    private BiometricLockManager() {
    }

    public static boolean isBiometricEnabled(Context context) {
        return preferences(context).getBoolean(KEY_ENABLED, false);
    }

    public static void setBiometricEnabled(Context context, boolean enabled) {
        preferences(context).edit().putBoolean(KEY_ENABLED, enabled).apply();
        lockRequired = enabled;
    }

    public static boolean isBiometricAvailable(Context context) {
        int status = BiometricManager.from(context).canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.BIOMETRIC_STRONG
        );
        return status == BiometricManager.BIOMETRIC_SUCCESS;
    }

    public static boolean shouldPrompt(Context context) {
        return isBiometricEnabled(context) && lockRequired;
    }

    public static void markLockRequired() {
        lockRequired = true;
    }

    public static void clearLockRequired() {
        lockRequired = false;
    }

    private static SharedPreferences preferences(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
