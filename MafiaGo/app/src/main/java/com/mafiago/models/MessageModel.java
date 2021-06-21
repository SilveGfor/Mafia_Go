package com.mafiago.models;

public class MessageModel {
    public  String message;
    public  String time;
    public  String nickName;
    public String MesType;
    public String type;
    public String main_role;
    public int answerId;
    public int num;

    public MessageModel(int num, String message, String time, String nickName, String MesType, String type, String main_role) {
        this.num = num;
        this.message = message;
        this.time = time;
        this.nickName = nickName;
        this.MesType = MesType;
        this.type = type;
        this.main_role = main_role;
    }
    public MessageModel(int num, String message, String time, String nickName, String MesType) {
        this.num = num;
        this.message = message;
        this.time = time;
        this.nickName = nickName;
        this.MesType = MesType;
    }
    public MessageModel(int num, String message, String time, String nickName, String MesType, String type, int answerId, String main_role) {
        this.num = num;
        this.message = message;
        this.time = time;
        this.nickName = nickName;
        this.MesType = MesType;
        this.type = type;
        this.answerId = answerId;
        this.main_role = main_role;
    }
}
