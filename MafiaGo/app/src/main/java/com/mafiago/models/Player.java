package com.mafiago.models;

import android.util.Log;

import com.mafiago.enums.Role;
import com.mafiago.enums.Time;

public class Player {
    private String nick, status, session_id, host_nick, main_role;

    private Time time;
    private Role role;
    private  boolean can_click, can_write, voted_at_night, journalist_checked;
    private int room_num, ban_limit;

    public Player(String nick, String session_id, int room_num, String main_role)
    {
        this.nick = nick;
        this.session_id = session_id;
        this.room_num = room_num;
        this.main_role = main_role;
        role = Role.NONE;
        time = Time.LOBBY;
        status = "alive";
        voted_at_night = false;
        can_click = false;
        can_write = true;
        journalist_checked = false;
    }

    public boolean getJournalist_checked() {
        return journalist_checked;
    }

    public void setJournalist_checked(boolean journalist_checked) {
        this.journalist_checked = journalist_checked;
    }


    public void setBan_limit(int ban_limit) {
        this.ban_limit = ban_limit;
    }

    public int getBan_limit() {
        return ban_limit;
    }

    public String getHost_nick() {
        return host_nick;
    }

    public void setHost_nick(String host_nick) {
        this.host_nick = host_nick;
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

    public Time getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }

    public String getSession_id() {
        return session_id;
    }

    public Role getRole() {
        return role;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    public void setCan_click(boolean can_click) {
        this.can_click = can_click;
    }

    public void setRole(Role role) {
        if (this.role == Role.NONE) this.role = role;
        else Log.d("kkk", "Нельзя задать роль! Она уже есть!");
    }

    public void setStatus(String status) {
        if (!this.status.equals("died")) this.status = status;
        else Log.d("kkk", "Человек уже мёртв! Нельзя его воскресить!");
    }
}
