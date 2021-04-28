package com.example.mafiago.models;

import android.util.Log;

public class Player {
    private String nick, role, status, session_id, time;
    private  boolean can_click, can_write, voted_at_night;
    private int room_num;

    public Player(String nick, String session_id, int room_num)
    {
        this.nick = nick;
        this.session_id = session_id;
        this.room_num = room_num;
        role = "";
        time = "lobby";
        status = "alive";
        voted_at_night = false;
        can_click = false;
        can_write = true;
    }

    public boolean getVoted_at_night() {
        return voted_at_night;
    }

    public void setVoted_at_night(boolean voted_at_night) {
        this.voted_at_night = voted_at_night;
    }

    public int getRoom_num() {
        return room_num;
    }

    public void setRoom_num(int room_num) {
        this.room_num = room_num;
    }

    public boolean Can_write() {
        return can_write;
    }

    public void setCan_write(boolean can_write) {
        this.can_write = can_write;
    }

    public String getNick() {
        return nick;
    }

    public boolean Can_click() {
        return can_click;
    }

    public String getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }

    public String getSession_id() {
        return session_id;
    }

    public String getRole() {
        return role;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setCan_click(boolean can_click) {
        this.can_click = can_click;
    }

    public void setRole(String role) {
        if (this.role.equals("")) this.role = role;
        else Log.d("kkk", "Нельзя задать роль! Она уже есть!");
    }

    public void setStatus(String status) {
        if (!this.status.equals("died")) this.status = status;
        else Log.d("kkk", "Человек уже мёртв! Нельзя его воскресить!");
    }
}
