package com.debiprasaddas.expensepilot.data.repo;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.debiprasaddas.expensepilot.data.dao.GoalDao;
import com.debiprasaddas.expensepilot.data.dao.TransactionDao;
import com.debiprasaddas.expensepilot.data.db.AppDatabase;
import com.debiprasaddas.expensepilot.data.entity.GoalEntity;
import com.debiprasaddas.expensepilot.data.entity.TransactionEntity;
import com.debiprasaddas.expensepilot.util.AppExecutors;
import com.debiprasaddas.expensepilot.util.NotificationScheduler;

import java.util.Calendar;
import java.util.List;

public class FinanceRepository {

    private final Application application;
    private final TransactionDao transactionDao;
    private final GoalDao goalDao;
    private final LiveData<List<TransactionEntity>> allTransactions;

    public FinanceRepository(Application application) {
        this.application = application;
        AppDatabase database = AppDatabase.getInstance(application);
        transactionDao = database.transactionDao();
        goalDao = database.goalDao();
        allTransactions = transactionDao.getAllTransactions();
    }

    public LiveData<List<TransactionEntity>> getAllTransactions() {
        return allTransactions;
    }

    public LiveData<List<TransactionEntity>> searchTransactions(String query, String type) {
        return transactionDao.searchTransactions(query == null ? "" : query.trim(), type == null ? "ALL" : type);
    }

    public LiveData<GoalEntity> getCurrentGoal() {
        Calendar calendar = Calendar.getInstance();
        return goalDao.getGoalForMonthLive(calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));
    }

    public void insertTransaction(TransactionEntity transactionEntity) {
        AppExecutors.database().execute(() -> {
            transactionDao.insert(transactionEntity);
            NotificationScheduler.scheduleImmediate(application);
        });
    }

    public void updateTransaction(TransactionEntity transactionEntity) {
        AppExecutors.database().execute(() -> {
            transactionDao.update(transactionEntity);
            NotificationScheduler.scheduleImmediate(application);
        });
    }

    public void deleteTransaction(TransactionEntity transactionEntity) {
        AppExecutors.database().execute(() -> {
            transactionDao.delete(transactionEntity);
            NotificationScheduler.scheduleImmediate(application);
        });
    }

    public void saveGoal(String title, double targetAmount) {
        AppExecutors.database().execute(() -> {
            Calendar calendar = Calendar.getInstance();
            int month = calendar.get(Calendar.MONTH) + 1;
            int year = calendar.get(Calendar.YEAR);
            GoalEntity existingGoal = goalDao.getGoalForMonthSync(month, year);
            if (existingGoal == null) {
                goalDao.insert(new GoalEntity(title, targetAmount, month, year, System.currentTimeMillis()));
            } else {
                existingGoal.setTitle(title);
                existingGoal.setTargetAmount(targetAmount);
                goalDao.update(existingGoal);
            }
            NotificationScheduler.scheduleImmediate(application);
        });
    }

    public void loadTransaction(long transactionId, TransactionCallback callback) {
        AppExecutors.database().execute(() -> callback.onLoaded(transactionDao.getTransactionByIdSync(transactionId)));
    }

    public interface TransactionCallback {
        void onLoaded(TransactionEntity transactionEntity);
    }
}
