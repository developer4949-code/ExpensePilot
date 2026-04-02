package com.debiprasaddas.expensepilot.auth;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.debiprasaddas.expensepilot.MainActivity;
import com.debiprasaddas.expensepilot.databinding.ActivityPostLoginLoadingBinding;

public class PostLoginLoadingActivity extends AppCompatActivity {

    private ActivityPostLoginLoadingBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable openHome = () -> {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostLoginLoadingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        startAnimations();
        handler.postDelayed(openHome, 1800);
    }

    private void startAnimations() {
        ObjectAnimator logoScaleX = ObjectAnimator.ofFloat(binding.imageLogo, "scaleX", 0.88f, 1.04f, 0.96f);
        ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(binding.imageLogo, "scaleY", 0.88f, 1.04f, 0.96f);
        ObjectAnimator logoRotation = ObjectAnimator.ofFloat(binding.imageLogo, "rotation", -4f, 4f, 0f);
        ObjectAnimator ringOneScaleX = ObjectAnimator.ofFloat(binding.ringOne, "scaleX", 0.92f, 1.12f);
        ObjectAnimator ringOneScaleY = ObjectAnimator.ofFloat(binding.ringOne, "scaleY", 0.92f, 1.12f);
        ObjectAnimator ringOneAlpha = ObjectAnimator.ofFloat(binding.ringOne, "alpha", 0.8f, 0.2f);
        ObjectAnimator ringTwoScaleX = ObjectAnimator.ofFloat(binding.ringTwo, "scaleX", 0.95f, 1.18f);
        ObjectAnimator ringTwoScaleY = ObjectAnimator.ofFloat(binding.ringTwo, "scaleY", 0.95f, 1.18f);
        ObjectAnimator ringTwoAlpha = ObjectAnimator.ofFloat(binding.ringTwo, "alpha", 0.65f, 0.15f);
        ObjectAnimator orbLargeMove = ObjectAnimator.ofFloat(binding.orbLarge, "translationY", -20f, 24f, -8f);
        ObjectAnimator orbSmallMove = ObjectAnimator.ofFloat(binding.orbSmall, "translationY", 16f, -20f, 8f);

        for (ObjectAnimator animator : new ObjectAnimator[]{logoScaleX, logoScaleY, logoRotation, ringOneScaleX, ringOneScaleY, ringOneAlpha, ringTwoScaleX, ringTwoScaleY, ringTwoAlpha, orbLargeMove, orbSmallMove}) {
            animator.setDuration(1500);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.setRepeatCount(ObjectAnimator.INFINITE);
            animator.setRepeatMode(ObjectAnimator.RESTART);
        }

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
                logoScaleX, logoScaleY, logoRotation,
                ringOneScaleX, ringOneScaleY, ringOneAlpha,
                ringTwoScaleX, ringTwoScaleY, ringTwoAlpha,
                orbLargeMove, orbSmallMove
        );
        animatorSet.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(openHome);
    }
}
