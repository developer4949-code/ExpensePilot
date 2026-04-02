package com.debiprasaddas.expensepilot.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.debiprasaddas.expensepilot.R;
import com.debiprasaddas.expensepilot.data.entity.GoalEntity;
import com.debiprasaddas.expensepilot.data.entity.TransactionEntity;
import com.debiprasaddas.expensepilot.databinding.FragmentHomeBinding;
import com.debiprasaddas.expensepilot.ui.adapter.TransactionAdapter;
import com.debiprasaddas.expensepilot.ui.common.FinanceViewModel;
import com.debiprasaddas.expensepilot.ui.transactions.AddEditTransactionActivity;
import com.debiprasaddas.expensepilot.util.FinanceAnalytics;
import com.debiprasaddas.expensepilot.util.FormatterUtils;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private FinanceViewModel viewModel;
    private TransactionAdapter adapter;
    private List<TransactionEntity> latestTransactions = new ArrayList<>();
    private GoalEntity latestGoal;

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new TransactionAdapter(new TransactionAdapter.Listener() {
            @Override
            public void onTransactionClick(TransactionEntity transactionEntity) {
                Intent intent = new Intent(requireContext(), AddEditTransactionActivity.class);
                intent.putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_ID, transactionEntity.getId());
                startActivity(intent);
            }

            @Override
            public void onTransactionLongClick(View anchor, TransactionEntity transactionEntity) {
            }
        });
        binding.recyclerRecent.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerRecent.setAdapter(adapter);
        binding.textGreeting.setText(buildGreeting());

        viewModel = new ViewModelProvider(requireActivity()).get(FinanceViewModel.class);
        viewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            latestTransactions = transactions == null ? new ArrayList<>() : transactions;
            render();
        });
        viewModel.getCurrentGoal().observe(getViewLifecycleOwner(), goalEntity -> {
            latestGoal = goalEntity;
            render();
        });
        viewModel.getAiUiState().observe(getViewLifecycleOwner(), aiUiState -> {
            binding.progressAi.setVisibility(aiUiState.loading ? View.VISIBLE : View.GONE);
            binding.textAiRecommendation.setText(aiUiState.recommendation);
        });
    }

    private void render() {
        if (binding == null) {
            return;
        }
        FinanceAnalytics.DashboardSummary summary = FinanceAnalytics.calculateDashboard(latestTransactions, latestGoal);
        FinanceAnalytics.InsightSnapshot insights = FinanceAnalytics.buildInsights(latestTransactions);
        binding.textBalance.setText(FormatterUtils.currency(summary.balance));
        binding.textIncome.setText(FormatterUtils.currency(summary.income));
        binding.textExpense.setText(FormatterUtils.currency(summary.expenses));
        binding.progressGoal.setProgress((int) summary.goalProgress);
        if (latestGoal == null || summary.goalTarget == 0) {
            binding.textGoalSummary.setText("Set a savings target to start tracking your monthly momentum.");
        } else {
            binding.textGoalSummary.setText("Goal: " + FormatterUtils.currency(summary.goalTarget) + " | Progress: " + (int) summary.goalProgress + "%");
        }
        binding.textWeekPulse.setText(insights.weeklyComparisonText + " This week: " + FormatterUtils.currency(insights.thisWeek));
        binding.trendChartWeek.setPoints(FinanceAnalytics.dailyExpenseTrend(latestTransactions, 7));
        viewModel.requestAiRecommendations(latestTransactions, latestGoal, false);

        renderCategoryRows(binding.layoutCategoryBreakdown, FinanceAnalytics.categoryBreakdown(latestTransactions));
        adapter.submitList(FinanceAnalytics.recentTransactions(latestTransactions, 4));
    }

    private void renderCategoryRows(LinearLayout container, List<FinanceAnalytics.CategoryTotal> totals) {
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        if (totals.isEmpty()) {
            TextView textView = new TextView(requireContext());
            textView.setText("Add expenses to see category patterns here.");
            textView.setTextColor(requireContext().getColor(R.color.ink_muted));
            container.addView(textView);
            return;
        }

        int limit = Math.min(4, totals.size());
        for (int i = 0; i < limit; i++) {
            FinanceAnalytics.CategoryTotal total = totals.get(i);
            View row = inflater.inflate(R.layout.item_category_progress, container, false);
            ((TextView) row.findViewById(R.id.text_category_name)).setText(total.category);
            ((TextView) row.findViewById(R.id.text_category_amount)).setText(FormatterUtils.currency(total.amount));
            ((ProgressBar) row.findViewById(R.id.progress_category)).setProgress(total.progressPercent);
            container.addView(row);
        }
    }

    private String buildGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String email = FirebaseAuth.getInstance().getCurrentUser() == null ? "" : FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String firstChunk = email == null || !email.contains("@") ? "there" : email.substring(0, email.indexOf('@'));
        String name = firstChunk.isEmpty() ? "there" : firstChunk.substring(0, 1).toUpperCase(Locale.ENGLISH) + firstChunk.substring(1);
        if (hour < 12) {
            return "Good morning, " + name;
        } else if (hour < 17) {
            return "Good afternoon, " + name;
        }
        return "Good evening, " + name;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
