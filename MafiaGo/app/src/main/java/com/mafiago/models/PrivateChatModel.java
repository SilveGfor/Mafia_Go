package com.mafiago.models;

public class PrivateChatModel {
    private String nick;
    private boolean online;
    private boolean blocked;
    public boolean i_blocked;
    private String last_message;
    private String user_id_2;
    private String avatar;

    public PrivateChatModel(String nick, String last_message, String user_id_2, boolean online, boolean blocked, String avatar, boolean i_blocked) {
        this.nick = nick;
        this.last_message = last_message;
        this.online = online;
        this.user_id_2 = user_id_2;
        this.blocked = blocked;
        this.avatar = avatar;
        this.i_blocked = i_blocked;
    }

    public String getAvatar() {
        return avatar;
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
