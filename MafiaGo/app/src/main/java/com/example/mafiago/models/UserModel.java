package com.example.mafiago.models;

public class UserModel {
    private int image;
    public String nick, animation_type;
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
        animation_type = "voting";
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

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }
}
