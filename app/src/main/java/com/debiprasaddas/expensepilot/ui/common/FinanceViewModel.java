package com.debiprasaddas.expensepilot.ui.common;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.debiprasaddas.expensepilot.data.entity.GoalEntity;
import com.debiprasaddas.expensepilot.data.entity.TransactionEntity;
import com.debiprasaddas.expensepilot.data.repo.FinanceRepository;
import com.debiprasaddas.expensepilot.util.AiRecommendationService;
import com.debiprasaddas.expensepilot.util.AppExecutors;

import java.util.List;

public class FinanceViewModel extends AndroidViewModel {

    private final FinanceRepository repository;
    private final LiveData<List<TransactionEntity>> allTransactions;
    private final MutableLiveData<FilterState> filters = new MutableLiveData<>(new FilterState("", "ALL"));
    private final LiveData<List<TransactionEntity>> filteredTransactions;
    private final LiveData<GoalEntity> currentGoal;
    private final MutableLiveData<AiUiState> aiUiState = new MutableLiveData<>(AiUiState.idle());
    private final AiRecommendationService aiRecommendationService = new AiRecommendationService();
    private String lastAiCacheKey = "";

    public FinanceViewModel(@NonNull Application application) {
        super(application);
        repository = new FinanceRepository(application);
        allTransactions = repository.getAllTransactions();
        filteredTransactions = Transformations.switchMap(filters, state -> repository.searchTransactions(state.query, state.type));
        currentGoal = repository.getCurrentGoal();
    }

    public LiveData<List<TransactionEntity>> getAllTransactions() {
        return allTransactions;
    }

    public LiveData<List<TransactionEntity>> getFilteredTransactions() {
        return filteredTransactions;
    }

    public LiveData<GoalEntity> getCurrentGoal() {
        return currentGoal;
    }

    public LiveData<AiUiState> getAiUiState() {
        return aiUiState;
    }

    public void setFilters(String query, String type) {
        filters.setValue(new FilterState(query == null ? "" : query, type == null ? "ALL" : type));
    }

    public void insertTransaction(TransactionEntity transactionEntity) {
        repository.insertTransaction(transactionEntity);
    }

    public void updateTransaction(TransactionEntity transactionEntity) {
        repository.updateTransaction(transactionEntity);
    }

    public void deleteTransaction(TransactionEntity transactionEntity) {
        repository.deleteTransaction(transactionEntity);
    }

    public void saveGoal(String title, double targetAmount) {
        repository.saveGoal(title, targetAmount);
    }

    public void loadTransaction(long transactionId, FinanceRepository.TransactionCallback callback) {
        repository.loadTransaction(transactionId, callback);
    }

    public void requestAiRecommendations(List<TransactionEntity> transactions, GoalEntity goalEntity, boolean forceRefresh) {
        String cacheKey = buildCacheKey(transactions, goalEntity);
        AiUiState currentState = aiUiState.getValue();
        if (!forceRefresh && cacheKey.equals(lastAiCacheKey) && currentState != null && !currentState.loading) {
            return;
        }
        lastAiCacheKey = cacheKey;
        aiUiState.setValue(AiUiState.loading());
        AppExecutors.database().execute(() -> {
            AiRecommendationService.RecommendationResult result = aiRecommendationService.generateAdvice(transactions, goalEntity);
            aiUiState.postValue(AiUiState.ready(result.text, result.fallback));
        });
    }

    private String buildCacheKey(List<TransactionEntity> transactions, GoalEntity goalEntity) {
        StringBuilder builder = new StringBuilder();
        builder.append(goalEntity == null ? "no-goal" : goalEntity.getTitle()).append('|');
        builder.append(goalEntity == null ? 0 : goalEntity.getTargetAmount()).append('|');
        if (transactions != null) {
            for (TransactionEntity transaction : transactions) {
                builder.append(transaction.getId()).append(':')
                        .append(transaction.getAmount()).append(':')
                        .append(transaction.getDate()).append(':')
                        .append(transaction.getType()).append(';');
            }
        }
        return builder.toString();
    }

    private static class FilterState {
        final String query;
        final String type;

        FilterState(String query, String type) {
            this.query = query;
            this.type = type;
        }
    }

    public static class AiUiState {
        public final boolean loading;
        public final String recommendation;
        public final boolean fallback;

        private AiUiState(boolean loading, String recommendation, boolean fallback) {
            this.loading = loading;
            this.recommendation = recommendation;
            this.fallback = fallback;
        }

        public static AiUiState idle() {
            return new AiUiState(false, "Add a few transactions to unlock AI-powered recommendations.", true);
        }

        public static AiUiState loading() {
            return new AiUiState(true, "Generating smarter recommendations from your spending patterns…", false);
        }

        public static AiUiState ready(String recommendation, boolean fallback) {
            return new AiUiState(false, recommendation, fallback);
        }
    }
}
