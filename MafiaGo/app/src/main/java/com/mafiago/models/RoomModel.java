package com.mafiago.models;

import java.util.ArrayList;

public class RoomModel {
    public  String name;
    public int min_people;
    public int max_people;
    public int num_people;
    public int id;
    public ArrayList<UserModel> list_users;
    public boolean is_on;

    public RoomModel()
    {
    }

    public RoomModel(String name, int min_people, int  max_people, int num_people, int id, ArrayList<UserModel> list_users, boolean is_on) {
        this.name = name;
        this.min_people = min_people;
        this.max_people = max_people;
        this.num_people = num_people;
        this.id = id;
        this.list_users = list_users;
        this.is_on = is_on;
    }
}