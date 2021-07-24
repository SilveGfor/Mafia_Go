package com.mafiago.models;

public class FineModel {
    String admin_comment;
    String creation_time;
    int exp;
    int hour;
    int money;
    String reason;

    public FineModel(String admin_comment, String creation_time, int exp, int hour, int money, String reason) {
        this.admin_comment = admin_comment;
        this.creation_time = creation_time;
        this.exp = exp;
        this.hour = hour;
        this.money = money;
        this.reason = reason;
    }
}
