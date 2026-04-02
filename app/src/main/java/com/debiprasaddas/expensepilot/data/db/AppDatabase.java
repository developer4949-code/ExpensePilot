package com.debiprasaddas.expensepilot.data.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.debiprasaddas.expensepilot.data.dao.GoalDao;
import com.debiprasaddas.expensepilot.data.dao.TransactionDao;
import com.debiprasaddas.expensepilot.data.entity.GoalEntity;
import com.debiprasaddas.expensepilot.data.entity.TransactionEntity;
import com.debiprasaddas.expensepilot.util.AppExecutors;

import java.util.Calendar;

@Database(entities = {TransactionEntity.class, GoalEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract TransactionDao transactionDao();

    public abstract GoalDao goalDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "expense_pilot.db")
                            .fallbackToDestructiveMigration()
                            .addCallback(new SeedCallback())
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static class SeedCallback extends RoomDatabase.Callback {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            AppExecutors.database().execute(() -> {
                if (INSTANCE == null) {
                    return;
                }

                TransactionDao transactionDao = INSTANCE.transactionDao();
                GoalDao goalDao = INSTANCE.goalDao();
                long now = System.currentTimeMillis();
                Calendar calendar = Calendar.getInstance();
                int month = calendar.get(Calendar.MONTH) + 1;
                int year = calendar.get(Calendar.YEAR);

                if (transactionDao.getCount() == 0) {
                    transactionDao.insert(new TransactionEntity(52000, "INCOME", "Salary", daysAgo(12), "Primary salary"));
                    transactionDao.insert(new TransactionEntity(6800, "INCOME", "Freelance", daysAgo(4), "Landing page project"));
                    transactionDao.insert(new TransactionEntity(1250, "EXPENSE", "Food", daysAgo(1), "Cafe and groceries"));
                    transactionDao.insert(new TransactionEntity(2200, "EXPENSE", "Transport", daysAgo(3), "Fuel refill"));
                    transactionDao.insert(new TransactionEntity(9600, "EXPENSE", "Rent", daysAgo(8), "Monthly room rent"));
                    transactionDao.insert(new TransactionEntity(1750, "EXPENSE", "Shopping", daysAgo(6), "Office wear"));
                    transactionDao.insert(new TransactionEntity(899, "EXPENSE", "Entertainment", daysAgo(10), "Movie night"));
                    transactionDao.insert(new TransactionEntity(1450, "EXPENSE", "Health", daysAgo(2), "Medicines"));
                }

                if (goalDao.getGoalForMonthSync(month, year) == null) {
                    goalDao.insert(new GoalEntity("Monthly savings goal", 20000, month, year, now));
                }
            });
        }

        private long daysAgo(int days) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -days);
            return calendar.getTimeInMillis();
        }
    }
}
