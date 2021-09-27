package com.mafiago.models;

import android.graphics.Bitmap;

public class MessageModel {
    public int num;
    public  String message;
    public  String time;
    public  String nickName;
    public String mesType;
    public String textType;
    public String status_text;
    public String user_color;
    public int answerId;
    public Bitmap avatar;

    //сообщения пользователей
    public MessageModel(int num, String message, String time, String nickName, String mesType, String textType, String status_text, Bitmap avatar, String user_color) {
        this.num = num;
        this.message = message;
        this.time = time;
        this.nickName = nickName;
        this.mesType = mesType;
        this.textType = textType;
        this.status_text = status_text;
        this.avatar = avatar;
        this.user_color = user_color;
    }
    //connect/disconnect сообщения
    public MessageModel(int num, String message, String time, String nickName, String mesType, Bitmap avatar) {
        this.num = num;
        this.message = message;
        this.time = time;
        this.nickName = nickName;
        this.mesType = mesType;
        this.avatar = avatar;
        status_text = "";
        user_color = "";
    }
    //системные сообщения
    public MessageModel(int num, String message, String time, String nickName, String mesType) {
        this.num = num;
        this.message = message;
        this.time = time;
        this.nickName = nickName;
        this.mesType = mesType;
        status_text = "";
        user_color = "";
    }
    //ответы на сообщения
    public MessageModel(int num, String message, String time, String nickName, String mesType, String textType, int answerId, String status_text, Bitmap avatar, String user_color) {
        this.num = num;
        this.message = message;
        this.time = time;
        this.nickName = nickName;
        this.mesType = mesType;
        this.textType = textType;
        this.answerId = answerId;
        this.status_text = status_text;
        this.avatar = avatar;
        this.user_color = user_color;
    }
}
