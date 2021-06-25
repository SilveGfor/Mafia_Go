package com.mafiago.models;

public class PrivateChatModel {
    private String nick;
    private boolean online;
    private boolean blocked;
    private String last_message;
    private String user_id_2;

    public PrivateChatModel(String nick, String last_message, String user_id_2, boolean online, boolean blocked) {
        this.nick = nick;
        this.last_message = last_message;
        this.online = online;
        this.user_id_2 = user_id_2;
        this.blocked = blocked;
    }

    public Boolean getBlocked() {
        return blocked;
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
