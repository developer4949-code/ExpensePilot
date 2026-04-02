package com.debiprasaddas.expensepilot.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.debiprasaddas.expensepilot.MainActivity;
import com.google.firebase.auth.FirebaseAuth;

public class AuthGateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        Intent intent = new Intent(this, auth.getCurrentUser() == null ? LoginActivity.class : MainActivity.class);
        startActivity(intent);
        finish();
    }
}
