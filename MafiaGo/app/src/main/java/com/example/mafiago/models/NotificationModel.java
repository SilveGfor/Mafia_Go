package com.example.mafiago.models;

import java.util.ArrayList;

public class NotificationModel {
    public String nick;
    public ArrayList<String> messages;

    public NotificationModel(String nick, ArrayList<String> messages) {
        this.nick = nick;
        this.messages = messages;
    }
}
