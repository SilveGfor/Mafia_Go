package com.example.mafiago.models;

public class UserModel {
    private int image;
    public  String nick;

    UserModel()
    {
    }

    public UserModel(String nick, int image) {
        this.nick = nick;
        this.image = image;
    }


    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }
}
