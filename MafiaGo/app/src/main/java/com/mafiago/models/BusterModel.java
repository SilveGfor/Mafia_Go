package com.mafiago.models;

public class BusterModel {
    public String type;
    public String time;
    public String object;
    public boolean is_premium;

    public BusterModel(String type, String time, String object) {
        this.type = type;
        this.time = time;
        this.object = object;
    }
    public BusterModel(String type, String time, String object, boolean is_premium) {
        this.type = type;
        this.time = time;
        this.object = object;
        this.is_premium = is_premium;
    }
}
