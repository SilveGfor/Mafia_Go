package com.example.mafiago.models;

public class MessageModel {
    public  String message;
    public  String time;
    public  String nickName;
    public String MesType;
    public String answerNick;
    public String answerMes;
    public String answerTime;
    public int answerId;

    public MessageModel()
    {
    }

    public MessageModel(String message, String time, String nickName, String MesType) {
        this.message = message;
        this.time = time;
        this.nickName = nickName;
        this.MesType = MesType;
    }
    public MessageModel(String message, String time, String nickName, String MesType, String answerNick, String answerMes, String answerTime, int answerId) {
        this.message = message;
        this.time = time;
        this.nickName = nickName;
        this.MesType = MesType;
        this.answerNick = answerNick;
        this.answerMes = answerMes;
        this.answerTime = answerTime;
        this.answerId = answerId;
    }
}
