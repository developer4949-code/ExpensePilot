package com.debiprasaddas.expensepilot.util;

import com.debiprasaddas.expensepilot.BuildConfig;
import com.debiprasaddas.expensepilot.data.entity.GoalEntity;
import com.debiprasaddas.expensepilot.data.entity.TransactionEntity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

public class AiRecommendationService {

    public RecommendationResult generateAdvice(List<TransactionEntity> transactions, GoalEntity goalEntity) {
        String fallback = buildFallbackAdvice(transactions, goalEntity);
        if (BuildConfig.HF_API_TOKEN == null || BuildConfig.HF_API_TOKEN.trim().isEmpty()) {
            return new RecommendationResult(fallback, true);
        }

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL("https://router.huggingface.co/v1/chat/completions").openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(20000);
            connection.setReadTimeout(30000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", "Bearer " + BuildConfig.HF_API_TOKEN);
            connection.setRequestProperty("Content-Type", "application/json");

            JSONObject body = new JSONObject();
            body.put("model", BuildConfig.HF_MODEL);
            body.put("stream", false);
            body.put("max_tokens", 180);

            JSONArray messages = new JSONArray();
            messages.put(new JSONObject()
                    .put("role", "system")
                    .put("content", "You are a concise fintech coach for a mobile finance app. Give 2 or 3 actionable recommendations in under 70 words total. Use plain, encouraging language. Avoid markdown."));
            messages.put(new JSONObject()
                    .put("role", "user")
                    .put("content", buildPrompt(transactions, goalEntity)));
            body.put("messages", messages);

            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(body.toString().getBytes(StandardCharsets.UTF_8));
            }

            int code = connection.getResponseCode();
            InputStream stream = code >= 200 && code < 300 ? connection.getInputStream() : connection.getErrorStream();
            String response = read(stream);
            if (code < 200 || code >= 300) {
                return new RecommendationResult(fallback, true);
            }

            JSONObject jsonObject = new JSONObject(response);
            JSONArray choices = jsonObject.optJSONArray("choices");
            if (choices == null || choices.length() == 0) {
                return new RecommendationResult(fallback, true);
            }
            JSONObject message = choices.getJSONObject(0).getJSONObject("message");
            String content = message.optString("content", "").trim();
            if (content.isEmpty()) {
                return new RecommendationResult(fallback, true);
            }
            return new RecommendationResult(content, false);
        } catch (Exception exception) {
            return new RecommendationResult(fallback, true);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String buildPrompt(List<TransactionEntity> transactions, GoalEntity goalEntity) {
        FinanceAnalytics.DashboardSummary dashboardSummary = FinanceAnalytics.calculateDashboard(transactions, goalEntity);
        FinanceAnalytics.InsightSnapshot insightSnapshot = FinanceAnalytics.buildInsights(transactions);
        List<FinanceAnalytics.CategoryTotal> categories = FinanceAnalytics.categoryBreakdown(transactions);
        String topCategory = categories.isEmpty() ? "None yet" : categories.get(0).category + " at " + FormatterUtils.currency(categories.get(0).amount);
        String goal = goalEntity == null ? "No savings goal set." : "Savings goal is " + FormatterUtils.currency(goalEntity.getTargetAmount()) + " and current balance is " + FormatterUtils.currency(dashboardSummary.balance) + ".";

        return String.format(
                Locale.ENGLISH,
                "Create short recommendations for a personal finance app user. Income: %s. Expenses: %s. Balance: %s. Top expense category: %s. This week spend: %s. Last week spend: %s. Weekly trend: %s. No-spend streak: %d days. %s",
                FormatterUtils.currency(dashboardSummary.income),
                FormatterUtils.currency(dashboardSummary.expenses),
                FormatterUtils.currency(dashboardSummary.balance),
                topCategory,
                FormatterUtils.currency(insightSnapshot.thisWeek),
                FormatterUtils.currency(insightSnapshot.lastWeek),
                insightSnapshot.weeklyComparisonText,
                insightSnapshot.noSpendStreak,
                goal
        );
    }

    private String buildFallbackAdvice(List<TransactionEntity> transactions, GoalEntity goalEntity) {
        FinanceAnalytics.InsightSnapshot insightSnapshot = FinanceAnalytics.buildInsights(transactions);
        List<FinanceAnalytics.CategoryTotal> categories = FinanceAnalytics.categoryBreakdown(transactions);
        String categoryText = categories.isEmpty() ? "your most frequent spends" : categories.get(0).category;
        if (goalEntity != null && insightSnapshot.noSpendStreak >= 2) {
            return "Your streak is building momentum. Keep the next 48 hours low-spend and move any small savings straight toward your " + FormatterUtils.currency(goalEntity.getTargetAmount()) + " goal. Watch " + categoryText + " most closely.";
        }
        if (insightSnapshot.thisWeek > insightSnapshot.lastWeek && insightSnapshot.lastWeek > 0) {
            return "This week is running hotter than last week. Review " + categoryText + " first and cap one avoidable purchase today to quickly bring spending back under control.";
        }
        return "You are trending in the right direction. Keep expenses intentional, especially in " + categoryText + ", and consider moving a fixed amount into savings right after income lands.";
    }

    private String read(InputStream inputStream) throws Exception {
        if (inputStream == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    public static class RecommendationResult {
        public final String text;
        public final boolean fallback;

        public RecommendationResult(String text, boolean fallback) {
            this.text = text;
            this.fallback = fallback;
        }
    }
}
