package com.mafiago.models;

import com.mafiago.enums.Role;

public class UserModel {
    private String nick;
    private String avatar;
    public String status;
    public String user_color;
    Role animation_type;
    private boolean alive, checked;
    private Role role;
    private int voting_number;

    public UserModel(String nick, Role role, String avatar) {
        this.nick = nick;
        alive = true;
        animation_type = Role.NONE;
        this.role = role;
        voting_number = 0;
        checked = false;
        this.avatar = avatar;
    }

    public UserModel(String nick, Boolean alive) {
        this.nick = nick;
        this.alive = alive;
    }

    //для SmallChat
    public UserModel(String nick, String avatar, String status, String user_color) {
        this.nick = nick;
        this.avatar = avatar;
        this.status = status;
        this.user_color = user_color;
    }

    public String getAvatar() {
        return avatar;
    }

    public boolean getChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public void setVoting_number(int voting_number) {
        this.voting_number = voting_number;
    }

    public int getVoting_number() {
        return voting_number;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Role getAnimation_type() {
        return animation_type;
    }

    public void setAnimation_type(Role animation_type) {
        this.animation_type = animation_type;
    }

    public boolean getAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public String getNick() {
        return nick;
    }
}
