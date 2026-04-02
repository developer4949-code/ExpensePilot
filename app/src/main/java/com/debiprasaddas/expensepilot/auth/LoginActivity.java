package com.debiprasaddas.expensepilot.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.debiprasaddas.expensepilot.R;
import com.debiprasaddas.expensepilot.MainActivity;
import com.debiprasaddas.expensepilot.databinding.ActivityLoginBinding;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth auth;
    private FirebaseAnalytics analytics;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        analytics = FirebaseAnalytics.getInstance(this);

        binding.buttonLogin.setOnClickListener(v -> attemptLogin());
        binding.buttonToSignup.setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
            overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_fade_out);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (auth.getCurrentUser() != null) {
            openHome();
        }
    }

    private void attemptLogin() {
        clearErrors();
        String email = textOf(binding.editEmail);
        String password = textOf(binding.editPassword);

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.layoutEmail.setError("Enter a valid email");
            return;
        }
        if (password.length() < 6) {
            binding.layoutPassword.setError("Password must be at least 6 characters");
            return;
        }

        setLoading(true);
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        analytics.logEvent(FirebaseAnalytics.Event.LOGIN, null);
                        openHome();
                    } else {
                        Toast.makeText(this, task.getException() == null ? "Login failed. Please try again." : task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void openHome() {
        Intent intent = new Intent(this, PostLoginLoadingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_fade_out);
        finish();
    }

    private void setLoading(boolean isLoading) {
        binding.progressAuth.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.buttonLogin.setEnabled(!isLoading);
        binding.buttonToSignup.setEnabled(!isLoading);
        binding.editEmail.setEnabled(!isLoading);
        binding.editPassword.setEnabled(!isLoading);
    }

    private void clearErrors() {
        binding.layoutEmail.setError(null);
        binding.layoutPassword.setError(null);
    }

    private String textOf(android.widget.EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}
