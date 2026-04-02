package com.debiprasaddas.expensepilot.data.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "goals", indices = {@Index(value = {"goalMonth", "goalYear"}, unique = true)})
public class GoalEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String title;
    private double targetAmount;
    private int goalMonth;
    private int goalYear;
    private long createdAt;

    public GoalEntity(String title, double targetAmount, int goalMonth, int goalYear, long createdAt) {
        this.title = title;
        this.targetAmount = targetAmount;
        this.goalMonth = goalMonth;
        this.goalYear = goalYear;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(double targetAmount) {
        this.targetAmount = targetAmount;
    }

    public int getGoalMonth() {
        return goalMonth;
    }

    public void setGoalMonth(int goalMonth) {
        this.goalMonth = goalMonth;
    }

    public int getGoalYear() {
        return goalYear;
    }

    public void setGoalYear(int goalYear) {
        this.goalYear = goalYear;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
