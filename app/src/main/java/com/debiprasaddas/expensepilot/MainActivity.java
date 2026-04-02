package com.debiprasaddas.expensepilot;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.debiprasaddas.expensepilot.auth.LoginActivity;
import com.debiprasaddas.expensepilot.data.entity.TransactionEntity;
import com.debiprasaddas.expensepilot.databinding.ActivityMainBinding;
import com.debiprasaddas.expensepilot.ui.common.SecureActivity;
import com.debiprasaddas.expensepilot.ui.goals.GoalsFragment;
import com.debiprasaddas.expensepilot.ui.common.FinanceViewModel;
import com.debiprasaddas.expensepilot.ui.home.HomeFragment;
import com.debiprasaddas.expensepilot.ui.insights.InsightsFragment;
import com.debiprasaddas.expensepilot.ui.settings.NotificationSettingsActivity;
import com.debiprasaddas.expensepilot.ui.transactions.AddEditTransactionActivity;
import com.debiprasaddas.expensepilot.ui.transactions.TransactionsFragment;
import com.debiprasaddas.expensepilot.util.BiometricLockManager;
import com.debiprasaddas.expensepilot.util.CsvExporter;
import com.debiprasaddas.expensepilot.util.ThemeManager;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends SecureActivity {

    private ActivityMainBinding binding;
    private final List<TransactionEntity> latestTransactions = new ArrayList<>();
    private int currentTabIndex = 0;
    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> { });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setLogo(R.drawable.app_logo_toolbar);
        binding.toolbar.setLogoDescription(getString(R.string.app_name));

        FinanceViewModel financeViewModel = new ViewModelProvider(this).get(FinanceViewModel.class);
        financeViewModel.getAllTransactions().observe(this, transactions -> {
            latestTransactions.clear();
            if (transactions != null) {
                latestTransactions.addAll(transactions);
            }
        });

        binding.bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                showFragment(new HomeFragment(), "Home", "Your money at a glance");
                return true;
            } else if (item.getItemId() == R.id.nav_transactions) {
                showFragment(new TransactionsFragment(), "Transactions", "Track and refine every move");
                return true;
            } else if (item.getItemId() == R.id.nav_goals) {
                showFragment(new GoalsFragment(), "Goals", "Stay consistent with savings");
                return true;
            } else if (item.getItemId() == R.id.nav_insights) {
                showFragment(new InsightsFragment(), "Insights", "Patterns behind your spending");
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            binding.bottomNav.setSelectedItemId(R.id.nav_home);
        }

        binding.fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, AddEditTransactionActivity.class));
            overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_fade_out);
        });

        requestNotificationPermissionIfNeeded();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void showFragment(Fragment fragment, String title, String subtitle) {
        int nextTabIndex = titleToIndex(title);
        boolean forward = nextTabIndex >= currentTabIndex;
        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .setCustomAnimations(
                        forward ? R.anim.fragment_enter_from_right : R.anim.fragment_enter_from_left,
                        forward ? R.anim.fragment_exit_to_left : R.anim.fragment_exit_to_right
                )
                .replace(R.id.fragment_container, fragment)
                .commit();
        binding.toolbar.setTitle(title);
        binding.toolbar.setSubtitle(subtitle);
        currentTabIndex = nextTabIndex;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar_menu, menu);
        int mode = ThemeManager.getSavedMode(this);
        MenuItem lightItem = menu.findItem(R.id.action_theme_light);
        MenuItem darkItem = menu.findItem(R.id.action_theme_dark);
        MenuItem biometricItem = menu.findItem(R.id.action_biometric_toggle);
        if (lightItem != null) {
            lightItem.setCheckable(true);
            lightItem.setChecked(mode == ThemeManager.MODE_LIGHT);
        }
        if (darkItem != null) {
            darkItem.setCheckable(true);
            darkItem.setChecked(mode == ThemeManager.MODE_DARK);
        }
        if (biometricItem != null) {
            if (!BiometricLockManager.isBiometricAvailable(this)) {
                biometricItem.setTitle(R.string.biometric_lock_unavailable);
                biometricItem.setEnabled(false);
            } else {
                biometricItem.setEnabled(true);
                biometricItem.setTitle(BiometricLockManager.isBiometricEnabled(this)
                        ? R.string.disable_biometric_lock
                        : R.string.enable_biometric_lock);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_sign_out) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        if (item.getItemId() == R.id.action_export) {
            if (latestTransactions.isEmpty()) {
                Toast.makeText(this, R.string.empty_transactions, Toast.LENGTH_SHORT).show();
                return true;
            }
            try {
                startActivity(CsvExporter.createShareIntent(this, latestTransactions));
                Toast.makeText(this, R.string.spending_export_ready, Toast.LENGTH_SHORT).show();
            } catch (Exception exception) {
                Toast.makeText(this, exception.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
            return true;
        }
        if (item.getItemId() == R.id.action_notification_settings) {
            startActivity(new Intent(this, NotificationSettingsActivity.class));
            overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_fade_out);
            return true;
        }
        if (item.getItemId() == R.id.action_biometric_toggle) {
            toggleBiometricLock();
            return true;
        }
        if (item.getItemId() == R.id.action_theme_light) {
            ThemeManager.saveTheme(this, ThemeManager.MODE_LIGHT);
            invalidateOptionsMenu();
            recreate();
            return true;
        }
        if (item.getItemId() == R.id.action_theme_dark) {
            ThemeManager.saveTheme(this, ThemeManager.MODE_DARK);
            invalidateOptionsMenu();
            recreate();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleBiometricLock() {
        if (!BiometricLockManager.isBiometricAvailable(this)) {
            Toast.makeText(this, R.string.biometric_lock_unavailable, Toast.LENGTH_LONG).show();
            return;
        }
        boolean enabled = BiometricLockManager.isBiometricEnabled(this);
        BiometricLockManager.setBiometricEnabled(this, !enabled);
        if (!enabled) {
            ensureBiometricAccess();
            Toast.makeText(this, "Biometric lock enabled", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Biometric lock disabled", Toast.LENGTH_SHORT).show();
        }
        invalidateOptionsMenu();
    }

    private int titleToIndex(String title) {
        if ("Transactions".equals(title)) {
            return 1;
        }
        if ("Goals".equals(title)) {
            return 2;
        }
        if ("Insights".equals(title)) {
            return 3;
        }
        return 0;
    }

    private void requestNotificationPermissionIfNeeded() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
    }
}
