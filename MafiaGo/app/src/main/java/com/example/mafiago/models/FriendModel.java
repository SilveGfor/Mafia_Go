package com.example.mafiago.models;

public class FriendModel {
    private String nick;
    private boolean online;

    public FriendModel(String nick, boolean online) {
        this.nick = nick;
        this.online = online;
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
