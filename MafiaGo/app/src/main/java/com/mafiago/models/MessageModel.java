package com.mafiago.models;

public class MessageModel {
    public int num;
    public  String message;
    public  String time;
    public  String nickName;
    public String mesType;
    public String textType;
    public String rang;
    public int answerId;
    public String avatar;

    //сообщения пользователей
    public MessageModel(int num, String message, String time, String nickName, String mesType, String textType, String rang, String avatar) {
        this.num = num;
        this.message = message;
        this.time = time;
        this.nickName = nickName;
        this.mesType = mesType;
        this.textType = textType;
        this.rang = rang;
        this.avatar = avatar;
    }
    //connect/disconnect сообщения
    public MessageModel(int num, String message, String time, String nickName, String mesType, String avatar) {
        this.num = num;
        this.message = message;
        this.time = time;
        this.nickName = nickName;
        this.mesType = mesType;
        this.avatar = avatar;
    }
    //системные сообщения
    public MessageModel(int num, String message, String time, String nickName, String mesType) {
        this.num = num;
        this.message = message;
        this.time = time;
        this.nickName = nickName;
        this.mesType = mesType;
    }
    //ответы на сообщения
    public MessageModel(int num, String message, String time, String nickName, String mesType, String textType, int answerId, String rang, String avatar) {
        this.num = num;
        this.message = message;
        this.time = time;
        this.nickName = nickName;
        this.mesType = mesType;
        this.textType = textType;
        this.answerId = answerId;
        this.rang = rang;
        this.avatar = avatar;
    }
}
