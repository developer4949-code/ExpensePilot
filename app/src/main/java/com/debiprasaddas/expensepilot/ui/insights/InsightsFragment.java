package com.debiprasaddas.expensepilot.ui.insights;

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

import com.debiprasaddas.expensepilot.R;
import com.debiprasaddas.expensepilot.data.entity.GoalEntity;
import com.debiprasaddas.expensepilot.data.entity.TransactionEntity;
import com.debiprasaddas.expensepilot.databinding.FragmentInsightsBinding;
import com.debiprasaddas.expensepilot.ui.common.FinanceViewModel;
import com.debiprasaddas.expensepilot.util.FinanceAnalytics;
import com.debiprasaddas.expensepilot.util.FormatterUtils;

import java.util.ArrayList;
import java.util.List;

public class InsightsFragment extends Fragment {

    private FragmentInsightsBinding binding;
    private FinanceViewModel viewModel;
    private List<TransactionEntity> latestTransactions = new ArrayList<>();
    private GoalEntity latestGoal;

    public InsightsFragment() {
        super(R.layout.fragment_insights);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentInsightsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(FinanceViewModel.class);
        viewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            latestTransactions = transactions == null ? new ArrayList<>() : transactions;
            render(latestTransactions);
        });
        viewModel.getCurrentGoal().observe(getViewLifecycleOwner(), goalEntity -> latestGoal = goalEntity);
        viewModel.getAiUiState().observe(getViewLifecycleOwner(), aiUiState -> {
            binding.progressAi.setVisibility(aiUiState.loading ? View.VISIBLE : View.GONE);
            binding.textAiRecommendation.setText(aiUiState.recommendation);
        });
        binding.buttonRefreshAi.setOnClickListener(v -> viewModel.requestAiRecommendations(latestTransactions, latestGoal, true));
    }

    private void render(List<TransactionEntity> transactions) {
        boolean isEmpty = transactions == null || transactions.isEmpty();
        binding.textEmptyInsights.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        if (isEmpty) {
            binding.layoutCategoryRows.removeAllViews();
            binding.trendChart.setPoints(null);
            binding.textTopCategory.setText("No data yet");
            binding.textWeekCompare.setText("Add a few transactions to unlock trend tracking.");
            return;
        }

        FinanceAnalytics.InsightSnapshot snapshot = FinanceAnalytics.buildInsights(transactions);
        binding.textTopCategory.setText(snapshot.highestCategory + " leads this month at " + FormatterUtils.currency(snapshot.highestCategoryAmount));
        binding.textWeekCompare.setText(snapshot.weeklyComparisonText + " This week: " + FormatterUtils.currency(snapshot.thisWeek));
        binding.trendChart.setPoints(FinanceAnalytics.monthlyExpenseTrend(transactions, 6));
        viewModel.requestAiRecommendations(transactions, latestGoal, false);
        renderCategories(FinanceAnalytics.categoryBreakdown(transactions));
    }

    private void renderCategories(List<FinanceAnalytics.CategoryTotal> totals) {
        binding.layoutCategoryRows.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        int limit = Math.min(5, totals.size());
        for (int i = 0; i < limit; i++) {
            FinanceAnalytics.CategoryTotal total = totals.get(i);
            View row = inflater.inflate(R.layout.item_category_progress, binding.layoutCategoryRows, false);
            ((TextView) row.findViewById(R.id.text_category_name)).setText(total.category);
            ((TextView) row.findViewById(R.id.text_category_amount)).setText(FormatterUtils.currency(total.amount));
            ((ProgressBar) row.findViewById(R.id.progress_category)).setProgress(total.progressPercent);
            binding.layoutCategoryRows.addView(row);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
