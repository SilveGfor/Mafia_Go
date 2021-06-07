package com.mafiago.models;

public class PrivateMessageModel {
    public  String message;
    public  String time;
    public  String nickName;
    public String MesType;
    public String answerNick;
    public String answerMes;
    public String answerTime;
    public String type;
    public int answerId;
    public int num;

    public PrivateMessageModel()
    {
    }

    public PrivateMessageModel(int num, String message, String time, String nickName, String MesType, String type) {
        this.num = num;
        this.message = message;
        this.time = time;
        this.nickName = nickName;
        this.MesType = MesType;
        this.type = type;
    }

    public PrivateMessageModel(int num, String message, String time, String nickName, String MesType, String type, String answerNick, String answerMes, String answerTime, int answerId) {
        this.num = num;
        this.message = message;
        this.time = time;
        this.nickName = nickName;
        this.MesType = MesType;
        this.type = type;
        this.answerNick = answerNick;
        this.answerMes = answerMes;
        this.answerTime = answerTime;
        this.answerId = answerId;
    }
}
