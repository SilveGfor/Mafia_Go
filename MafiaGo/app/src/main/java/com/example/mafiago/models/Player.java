package com.example.mafiago.models;

import android.util.Log;

public class Player {
    private String nick, role, status, session_id, time;

    public Player(String nick, String session_id)
    {
        this.nick = nick;
        this.session_id = session_id;
        role = "";
        time = "lobby";
        status = "alive";
    }

    public String getNick() {
        return nick;
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

    public void setRole(String role) {
        if (this.role.equals("")) this.role = role;
        else Log.d("kkk", "Нельзя задать роль! Она уже есть!");
    }

    public void setStatus(String status) {
        if (!this.status.equals("died")) this.status = status;
        else Log.d("kkk", "Человек уже мёртв! Нельзя его воскресить!");
    }
}