package com.example.mafiago.models;

public class UserModel {
    private int image;
    public String nick;
    private boolean animation;
    private boolean alive;

    UserModel()
    {
    }

    public UserModel(String nick, int image) {
        this.nick = nick;
        this.image = image;
        animation = false;
        alive = true;
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

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }
}
