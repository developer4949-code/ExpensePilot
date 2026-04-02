package com.debiprasaddas.expensepilot.ui.goals;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.debiprasaddas.expensepilot.R;
import com.debiprasaddas.expensepilot.data.entity.GoalEntity;
import com.debiprasaddas.expensepilot.data.entity.TransactionEntity;
import com.debiprasaddas.expensepilot.databinding.DialogGoalBinding;
import com.debiprasaddas.expensepilot.databinding.FragmentGoalsBinding;
import com.debiprasaddas.expensepilot.ui.common.FinanceViewModel;
import com.debiprasaddas.expensepilot.util.FinanceAnalytics;
import com.debiprasaddas.expensepilot.util.FormatterUtils;

import java.util.ArrayList;
import java.util.List;

public class GoalsFragment extends Fragment {

    private FragmentGoalsBinding binding;
    private FinanceViewModel viewModel;
    private List<TransactionEntity> latestTransactions = new ArrayList<>();
    private GoalEntity latestGoal;

    public GoalsFragment() {
        super(R.layout.fragment_goals);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGoalsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(FinanceViewModel.class);
        binding.buttonSetGoal.setOnClickListener(v -> showGoalDialog());

        viewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            latestTransactions = transactions == null ? new ArrayList<>() : transactions;
            render();
        });
        viewModel.getCurrentGoal().observe(getViewLifecycleOwner(), goalEntity -> {
            latestGoal = goalEntity;
            render();
        });
    }

    private void render() {
        FinanceAnalytics.DashboardSummary summary = FinanceAnalytics.calculateDashboard(latestTransactions, latestGoal);
        FinanceAnalytics.InsightSnapshot insights = FinanceAnalytics.buildInsights(latestTransactions);
        String title = latestGoal == null ? getString(R.string.goal_title_default) : latestGoal.getTitle();
        binding.textGoalTitle.setText(title);
        if (latestGoal == null) {
            binding.textGoalAmount.setText(getString(R.string.empty_goal));
            binding.progressGoal.setProgress(0);
        } else {
            binding.textGoalAmount.setText("Target " + FormatterUtils.currency(latestGoal.getTargetAmount()) + " | Current balance " + FormatterUtils.currency(summary.balance));
            binding.progressGoal.setProgress((int) summary.goalProgress);
        }
        binding.textStreak.setText(insights.noSpendStreak + " day streak");
        binding.textStreakMessage.setText(insights.noSpendStreak >= 2
                ? "You are protecting your wallet well. Keep today intentional too."
                : "One low-spend day can start a stronger streak this week.");
        binding.textGoalTip.setText(summary.expenses > 0
                ? "Your biggest expense pressure is around " + insights.highestCategory + ". Trimming even 10% there can move this goal noticeably."
                : "Once you log expenses, this space will suggest the best area to trim for faster savings.");
    }

    private void showGoalDialog() {
        DialogGoalBinding dialogBinding = DialogGoalBinding.inflate(LayoutInflater.from(requireContext()));
        dialogBinding.editGoalTitle.setText(latestGoal == null ? getString(R.string.goal_title_default) : latestGoal.getTitle());
        if (latestGoal != null) {
            dialogBinding.editGoalAmount.setText(String.valueOf((int) latestGoal.getTargetAmount()));
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Monthly savings goal")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Save", (dialog, which) -> {
                    String title = dialogBinding.editGoalTitle.getText() == null ? "Monthly savings goal" : dialogBinding.editGoalTitle.getText().toString().trim();
                    String amountText = dialogBinding.editGoalAmount.getText() == null ? "0" : dialogBinding.editGoalAmount.getText().toString().trim();
                    double amount = amountText.isEmpty() ? 0 : Double.parseDouble(amountText);
                    viewModel.saveGoal(title.isEmpty() ? "Monthly savings goal" : title, amount);
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
