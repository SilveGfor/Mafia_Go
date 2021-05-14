package com.example.mafiago.models;

import com.example.mafiago.enums.Role;

public class UserModel {
    private String nick;
    Role animation_type;
    private boolean alive;
    private Role role;

    public UserModel(String nick, Role role) {
        this.nick = nick;
        alive = true;
        animation_type = Role.NONE;
        this.role = role;
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
