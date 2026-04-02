package com.debiprasaddas.expensepilot.util;

import androidx.annotation.NonNull;

import com.debiprasaddas.expensepilot.data.entity.GoalEntity;
import com.debiprasaddas.expensepilot.data.entity.TransactionEntity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class FinanceAnalytics {

    private FinanceAnalytics() {
    }

    public static DashboardSummary calculateDashboard(List<TransactionEntity> transactions, GoalEntity goalEntity) {
        double income = 0;
        double expenses = 0;
        for (TransactionEntity transaction : safeList(transactions)) {
            if ("INCOME".equalsIgnoreCase(transaction.getType())) {
                income += transaction.getAmount();
            } else {
                expenses += transaction.getAmount();
            }
        }
        double balance = income - expenses;
        double goalProgress = 0;
        double goalTarget = goalEntity == null ? 0 : goalEntity.getTargetAmount();
        if (goalTarget > 0) {
            goalProgress = Math.min(100d, Math.max(0d, (balance / goalTarget) * 100d));
        }
        return new DashboardSummary(balance, income, expenses, goalTarget, goalProgress);
    }

    public static List<TransactionEntity> recentTransactions(List<TransactionEntity> transactions, int count) {
        List<TransactionEntity> copy = new ArrayList<>(safeList(transactions));
        copy.sort((first, second) -> Long.compare(second.getDate(), first.getDate()));
        return copy.size() > count ? copy.subList(0, count) : copy;
    }

    public static List<CategoryTotal> categoryBreakdown(List<TransactionEntity> transactions) {
        Map<String, Double> totals = new HashMap<>();
        double max = 0;
        for (TransactionEntity transaction : safeList(transactions)) {
            if (!"EXPENSE".equalsIgnoreCase(transaction.getType())) {
                continue;
            }
            double updated = totals.containsKey(transaction.getCategory())
                    ? totals.get(transaction.getCategory()) + transaction.getAmount()
                    : transaction.getAmount();
            totals.put(transaction.getCategory(), updated);
            max = Math.max(max, updated);
        }

        List<CategoryTotal> result = new ArrayList<>();
        for (Map.Entry<String, Double> entry : totals.entrySet()) {
            int percent = max == 0 ? 0 : (int) Math.round((entry.getValue() / max) * 100d);
            result.add(new CategoryTotal(entry.getKey(), entry.getValue(), percent));
        }
        result.sort((first, second) -> Double.compare(second.amount, first.amount));
        return result;
    }

    public static List<TrendPoint> monthlyExpenseTrend(List<TransactionEntity> transactions, int monthCount) {
        List<TrendPoint> result = new ArrayList<>();
        Calendar cursor = Calendar.getInstance();
        cursor.set(Calendar.DAY_OF_MONTH, 1);
        cursor.set(Calendar.HOUR_OF_DAY, 0);
        cursor.set(Calendar.MINUTE, 0);
        cursor.set(Calendar.SECOND, 0);
        cursor.set(Calendar.MILLISECOND, 0);
        cursor.add(Calendar.MONTH, -(monthCount - 1));

        for (int i = 0; i < monthCount; i++) {
            Calendar start = (Calendar) cursor.clone();
            Calendar end = (Calendar) cursor.clone();
            end.add(Calendar.MONTH, 1);
            double total = 0;
            for (TransactionEntity transaction : safeList(transactions)) {
                if (!"EXPENSE".equalsIgnoreCase(transaction.getType())) {
                    continue;
                }
                if (transaction.getDate() >= start.getTimeInMillis() && transaction.getDate() < end.getTimeInMillis()) {
                    total += transaction.getAmount();
                }
            }
            result.add(new TrendPoint(monthLabel(start), total));
            cursor.add(Calendar.MONTH, 1);
        }
        return result;
    }

    public static List<TrendPoint> dailyExpenseTrend(List<TransactionEntity> transactions, int dayCount) {
        List<TrendPoint> result = new ArrayList<>();
        Calendar cursor = Calendar.getInstance();
        cursor.set(Calendar.HOUR_OF_DAY, 0);
        cursor.set(Calendar.MINUTE, 0);
        cursor.set(Calendar.SECOND, 0);
        cursor.set(Calendar.MILLISECOND, 0);
        cursor.add(Calendar.DAY_OF_YEAR, -(dayCount - 1));

        for (int i = 0; i < dayCount; i++) {
            Calendar start = (Calendar) cursor.clone();
            Calendar end = (Calendar) cursor.clone();
            end.add(Calendar.DAY_OF_YEAR, 1);
            double total = 0;
            for (TransactionEntity transaction : safeList(transactions)) {
                if (!"EXPENSE".equalsIgnoreCase(transaction.getType())) {
                    continue;
                }
                if (transaction.getDate() >= start.getTimeInMillis() && transaction.getDate() < end.getTimeInMillis()) {
                    total += transaction.getAmount();
                }
            }
            result.add(new TrendPoint(dayLabel(start), total));
            cursor.add(Calendar.DAY_OF_YEAR, 1);
        }
        return result;
    }

    public static InsightSnapshot buildInsights(List<TransactionEntity> transactions) {
        List<CategoryTotal> categories = categoryBreakdown(transactions);
        String highestCategory = categories.isEmpty() ? "No expense data yet" : categories.get(0).category;
        double highestAmount = categories.isEmpty() ? 0 : categories.get(0).amount;

        double thisWeek = weeklyExpense(transactions, 0);
        double lastWeek = weeklyExpense(transactions, 1);
        String comparison = lastWeek == 0
                ? "This is your first tracked week of expenses."
                : (thisWeek <= lastWeek
                ? String.format(Locale.ENGLISH, "Spending is down %d%% vs last week.", Math.round(((lastWeek - thisWeek) / lastWeek) * 100))
                : String.format(Locale.ENGLISH, "Spending is up %d%% vs last week.", Math.round(((thisWeek - lastWeek) / lastWeek) * 100)));

        return new InsightSnapshot(highestCategory, highestAmount, thisWeek, lastWeek, comparison, noSpendStreak(transactions));
    }

    public static int noSpendStreak(List<TransactionEntity> transactions) {
        Calendar cursor = Calendar.getInstance();
        cursor.set(Calendar.HOUR_OF_DAY, 0);
        cursor.set(Calendar.MINUTE, 0);
        cursor.set(Calendar.SECOND, 0);
        cursor.set(Calendar.MILLISECOND, 0);
        int streak = 0;
        while (streak < 30) {
            long dayStart = cursor.getTimeInMillis();
            Calendar end = (Calendar) cursor.clone();
            end.add(Calendar.DAY_OF_YEAR, 1);
            boolean spent = false;
            for (TransactionEntity transaction : safeList(transactions)) {
                if ("EXPENSE".equalsIgnoreCase(transaction.getType())
                        && transaction.getDate() >= dayStart
                        && transaction.getDate() < end.getTimeInMillis()) {
                    spent = true;
                    break;
                }
            }
            if (spent) {
                break;
            }
            streak++;
            cursor.add(Calendar.DAY_OF_YEAR, -1);
        }
        return streak;
    }

    private static double weeklyExpense(List<TransactionEntity> transactions, int weekOffset) {
        Calendar start = Calendar.getInstance();
        start.set(Calendar.DAY_OF_WEEK, start.getFirstDayOfWeek());
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        start.add(Calendar.WEEK_OF_YEAR, -weekOffset);

        Calendar end = (Calendar) start.clone();
        end.add(Calendar.WEEK_OF_YEAR, 1);

        double total = 0;
        for (TransactionEntity transaction : safeList(transactions)) {
            if ("EXPENSE".equalsIgnoreCase(transaction.getType())
                    && transaction.getDate() >= start.getTimeInMillis()
                    && transaction.getDate() < end.getTimeInMillis()) {
                total += transaction.getAmount();
            }
        }
        return total;
    }

    @NonNull
    private static List<TransactionEntity> safeList(List<TransactionEntity> transactions) {
        return transactions == null ? Collections.emptyList() : transactions;
    }

    private static String monthLabel(Calendar calendar) {
        return new java.text.SimpleDateFormat("MMM", Locale.ENGLISH).format(calendar.getTime());
    }

    private static String dayLabel(Calendar calendar) {
        return new java.text.SimpleDateFormat("EE", Locale.ENGLISH).format(calendar.getTime());
    }

    public static class DashboardSummary {
        public final double balance;
        public final double income;
        public final double expenses;
        public final double goalTarget;
        public final double goalProgress;

        public DashboardSummary(double balance, double income, double expenses, double goalTarget, double goalProgress) {
            this.balance = balance;
            this.income = income;
            this.expenses = expenses;
            this.goalTarget = goalTarget;
            this.goalProgress = goalProgress;
        }
    }

    public static class CategoryTotal {
        public final String category;
        public final double amount;
        public final int progressPercent;

        public CategoryTotal(String category, double amount, int progressPercent) {
            this.category = category;
            this.amount = amount;
            this.progressPercent = progressPercent;
        }
    }

    public static class TrendPoint {
        public final String label;
        public final double value;

        public TrendPoint(String label, double value) {
            this.label = label;
            this.value = value;
        }
    }

    public static class InsightSnapshot {
        public final String highestCategory;
        public final double highestCategoryAmount;
        public final double thisWeek;
        public final double lastWeek;
        public final String weeklyComparisonText;
        public final int noSpendStreak;

        public InsightSnapshot(String highestCategory, double highestCategoryAmount, double thisWeek, double lastWeek, String weeklyComparisonText, int noSpendStreak) {
            this.highestCategory = highestCategory;
            this.highestCategoryAmount = highestCategoryAmount;
            this.thisWeek = thisWeek;
            this.lastWeek = lastWeek;
            this.weeklyComparisonText = weeklyComparisonText;
            this.noSpendStreak = noSpendStreak;
        }
    }
}
