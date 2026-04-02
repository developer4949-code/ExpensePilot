package com.debiprasaddas.expensepilot.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.debiprasaddas.expensepilot.data.entity.GoalEntity;

@Dao
public interface GoalDao {

    @Query("SELECT * FROM goals WHERE goalMonth = :month AND goalYear = :year LIMIT 1")
    LiveData<GoalEntity> getGoalForMonthLive(int month, int year);

    @Query("SELECT * FROM goals WHERE goalMonth = :month AND goalYear = :year LIMIT 1")
    GoalEntity getGoalForMonthSync(int month, int year);

    @Insert
    long insert(GoalEntity goalEntity);

    @Update
    void update(GoalEntity goalEntity);
}
