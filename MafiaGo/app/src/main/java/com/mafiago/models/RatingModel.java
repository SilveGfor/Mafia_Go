package com.mafiago.models;

import android.graphics.Bitmap;

public class RatingModel {
    public String nick;
    public boolean online;
    public Bitmap avatar;
    public String status;
    public String color;
    public String user_id;
    public int place;
    public String score;

    public RatingModel(String nick, boolean online, Bitmap avatar, String status, String color, String user_id, int place, String score) {
        this.nick = nick;
        this.online = online;
        this.avatar = avatar;
        this.status = status;
        this.color = color;
        this.user_id = user_id;
        this.place = place;
        this.score = score;
    }
}
