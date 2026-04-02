package com.debiprasaddas.expensepilot.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.debiprasaddas.expensepilot.R;
import com.debiprasaddas.expensepilot.databinding.ActivitySignupBinding;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;

public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    private FirebaseAuth auth;
    private FirebaseAnalytics analytics;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        analytics = FirebaseAnalytics.getInstance(this);

        binding.buttonSignup.setOnClickListener(v -> attemptSignup());
        binding.buttonToLogin.setOnClickListener(v -> finish());
    }

    private void attemptSignup() {
        clearErrors();
        String email = textOf(binding.editEmail);
        String password = textOf(binding.editPassword);
        String confirmPassword = textOf(binding.editConfirmPassword);

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.layoutEmail.setError("Enter a valid email");
            return;
        }
        if (password.length() < 6) {
            binding.layoutPassword.setError("Password must be at least 6 characters");
            return;
        }
        if (!password.equals(confirmPassword)) {
            binding.layoutConfirmPassword.setError("Passwords do not match");
            return;
        }

        setLoading(true);
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        analytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, null);
                        Intent intent = new Intent(this, PostLoginLoadingActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_fade_out);
                        finish();
                    } else {
                        Toast.makeText(this, task.getException() == null ? "Signup failed. Please try again." : task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_slide_out_right);
    }

    private void setLoading(boolean isLoading) {
        binding.progressAuth.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.buttonSignup.setEnabled(!isLoading);
        binding.buttonToLogin.setEnabled(!isLoading);
        binding.editEmail.setEnabled(!isLoading);
        binding.editPassword.setEnabled(!isLoading);
        binding.editConfirmPassword.setEnabled(!isLoading);
    }

    private void clearErrors() {
        binding.layoutEmail.setError(null);
        binding.layoutPassword.setError(null);
        binding.layoutConfirmPassword.setError(null);
    }

    private String textOf(android.widget.EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}
