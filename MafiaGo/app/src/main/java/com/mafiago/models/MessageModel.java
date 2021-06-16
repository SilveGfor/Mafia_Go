package com.mafiago.models;

public class MessageModel {
    public  String message;
    public  String time;
    public  String nickName;
    public String MesType;
    public String type;
    public int answerId;
    public int num;

    public MessageModel()
    {
    }

    public MessageModel(int num, String message, String time, String nickName, String MesType, String type) {
        this.num = num;
        this.message = message;
        this.time = time;
        this.nickName = nickName;
        this.MesType = MesType;
        this.type = type;
    }
    public MessageModel(int num, String message, String time, String nickName, String MesType) {
        this.num = num;
        this.message = message;
        this.time = time;
        this.nickName = nickName;
        this.MesType = MesType;
    }
    public MessageModel(int num, String message, String time, String nickName, String MesType, String type, int answerId) {
        this.num = num;
        this.message = message;
        this.time = time;
        this.nickName = nickName;
        this.MesType = MesType;
        this.type = type;
        this.answerId = answerId;
    }
}
