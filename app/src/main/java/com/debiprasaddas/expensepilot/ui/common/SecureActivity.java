package com.debiprasaddas.expensepilot.ui.common;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.debiprasaddas.expensepilot.R;
import com.debiprasaddas.expensepilot.util.BiometricLockManager;

public abstract class SecureActivity extends androidx.appcompat.app.AppCompatActivity {

    private boolean biometricPromptShowing;
    private View secureOverlay;
    private View protectedContentView;

    @Override
    public void setContentView(View view) {
        protectedContentView = view;
        FrameLayout root = new FrameLayout(this);
        root.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        root.addView(view, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        secureOverlay = buildSecureOverlay();
        root.addView(secureOverlay, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        super.setContentView(root);
        updateSecurityVisibility();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ensureBiometricAccess();
    }

    protected void ensureBiometricAccess() {
        if (biometricPromptShowing || !BiometricLockManager.shouldPrompt(this)) {
            updateSecurityVisibility();
            return;
        }
        updateSecurityVisibility();
        if (!BiometricLockManager.isBiometricAvailable(this)) {
            BiometricLockManager.setBiometricEnabled(this, false);
            Toast.makeText(this, "Biometric lock is unavailable on this device.", Toast.LENGTH_LONG).show();
            updateSecurityVisibility();
            return;
        }

        biometricPromptShowing = true;
        BiometricPrompt prompt = new BiometricPrompt(this, ContextCompat.getMainExecutor(this), new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                biometricPromptShowing = false;
                BiometricLockManager.clearLockRequired();
                updateSecurityVisibility();
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                biometricPromptShowing = false;
                updateSecurityVisibility();
                moveTaskToBack(true);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(SecureActivity.this, "Biometric not recognized. Try again.", Toast.LENGTH_SHORT).show();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock ExpensePilot")
                .setSubtitle("Use your biometric to reopen the app securely")
                .setNegativeButtonText("Cancel")
                .build();

        prompt.authenticate(promptInfo);
    }

    private void updateSecurityVisibility() {
        boolean locked = BiometricLockManager.shouldPrompt(this);
        if (protectedContentView != null) {
            protectedContentView.setAlpha(locked ? 0f : 1f);
            protectedContentView.setEnabled(!locked);
        }
        if (secureOverlay != null) {
            secureOverlay.setVisibility(locked ? View.VISIBLE : View.GONE);
        }
    }

    private View buildSecureOverlay() {
        FrameLayout overlay = new FrameLayout(this);
        overlay.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_app_gradient));
        overlay.setClickable(true);
        overlay.setFocusable(true);

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER_HORIZONTAL);
        panel.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_card));
        int panelPadding = dp(24);
        panel.setPadding(panelPadding, panelPadding, panelPadding, panelPadding);

        TextView title = new TextView(this);
        title.setText(R.string.unlock_title);
        title.setTextColor(ContextCompat.getColor(this, R.color.ink));
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        title.setGravity(Gravity.CENTER);

        TextView subtitle = new TextView(this);
        subtitle.setText(R.string.unlock_subtitle);
        subtitle.setTextColor(ContextCompat.getColor(this, R.color.ink_muted));
        subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, dp(10), 0, 0);

        TextView badge = new TextView(this);
        badge.setText(R.string.enable_biometric_lock);
        badge.setTextColor(ContextCompat.getColor(this, R.color.primary));
        badge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        badge.setTypeface(Typeface.DEFAULT_BOLD);
        badge.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_chip));
        badge.setPadding(dp(12), dp(8), dp(12), dp(8));

        panel.addView(badge);
        panel.addView(title);
        panel.addView(subtitle);

        FrameLayout.LayoutParams panelParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        int margin = dp(24);
        panelParams.setMargins(margin, 0, margin, 0);
        panelParams.gravity = Gravity.CENTER;
        overlay.addView(panel, panelParams);
        return overlay;
    }

    private int dp(int value) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()
        ));
    }
}
