package com.mafiago.models;

public class DailyTaskModel {
    public String title;
    public String description;
    public String prizeType;
    public int prize;
    public int progress;
    public int maxProgress;
    public int num;
    public boolean completed;
    public boolean changed;

    public DailyTaskModel(String title, String description, String prizeType, int prize, int progress, int maxProgress, int num, boolean completed) {
        this.title = title;
        this.description = description;
        this.prizeType = prizeType;
        this.prize = prize;
        this.progress = progress;
        this.maxProgress = maxProgress;
        this.num = num;
        this.completed = completed;
        changed = false;
    }
}