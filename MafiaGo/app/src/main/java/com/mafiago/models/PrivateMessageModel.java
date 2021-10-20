package com.mafiago.models;

public class PrivateMessageModel {
    public  String message;
    public  String time;
    public  String nickName;
    public String MesType;
    public String type;
    public int answerId;
    public int num;
    public Boolean is_read;

    public PrivateMessageModel(int num, String message, String time, String nickName, String MesType, String type, boolean is_read) {
        this.num = num;
        this.message = message;
        this.time = time;
        this.nickName = nickName;
        this.MesType = MesType;
        this.type = type;
        this.is_read = is_read;
    }

    public PrivateMessageModel(int num, String message, String time, String nickName, String MesType, int answerId, boolean is_read) {
        this.num = num;
        this.message = message;
        this.time = time;
        this.nickName = nickName;
        this.MesType = MesType;
        this.answerId = answerId;
        this.is_read = is_read;
    }
}
