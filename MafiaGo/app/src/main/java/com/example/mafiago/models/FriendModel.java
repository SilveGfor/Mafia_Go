package com.example.mafiago.models;

public class FriendModel {
    private String nick;
    private boolean online;
    private String last_message;
    private String user_id_2;

    public FriendModel(String nick, String last_message, String user_id_2, boolean online) {
        this.nick = nick;
        this.last_message = last_message;
        this.online = online;
        this.user_id_2 = user_id_2;
    }

    public String getUser_id_2() {
        return user_id_2;
    }

    public String getLast_message() {
        return last_message;
    }

    public String getNick() {
        return nick;
    }

    public boolean getOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}
