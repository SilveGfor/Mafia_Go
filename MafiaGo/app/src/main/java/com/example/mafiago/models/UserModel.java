package com.example.mafiago.models;

public class UserModel {
    private String nick, animation_type, role;
    private boolean animation;
    private boolean alive;

    UserModel()
    {
    }

    public UserModel(String nick, String role) {
        this.nick = nick;
        animation = false;
        alive = true;
        animation_type = "voting";
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAnimation_type() {
        return animation_type;
    }

    public void setAnimation_type(String animation_type) {
        this.animation_type = animation_type;
    }

    public boolean getAnimation() {
        return animation;
    }

    public void setAnimation(boolean animation) {
        if (alive) this.animation = animation;
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
