package com.debiprasaddas.expensepilot.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.debiprasaddas.expensepilot.data.entity.TransactionEntity;

import java.util.List;

@Dao
public interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    LiveData<List<TransactionEntity>> getAllTransactions();

    @Query("SELECT * FROM transactions WHERE (:type = 'ALL' OR type = :type) AND ((:query = '') OR LOWER(category) LIKE '%' || LOWER(:query) || '%' OR LOWER(IFNULL(note, '')) LIKE '%' || LOWER(:query) || '%') ORDER BY date DESC, id DESC")
    LiveData<List<TransactionEntity>> searchTransactions(String query, String type);

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    TransactionEntity getTransactionByIdSync(long id);

    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    List<TransactionEntity> getAllTransactionsSync();

    @Insert
    long insert(TransactionEntity transaction);

    @Update
    void update(TransactionEntity transaction);

    @Delete
    void delete(TransactionEntity transaction);

    @Query("SELECT COUNT(*) FROM transactions")
    int getCount();
}
