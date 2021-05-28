package com.example.mafiago.models;

public class RoomModel {
    public  String name;
    public int min_people;
    public int max_people;
    public int num_people;
    public int id;

    public RoomModel()
    {
    }

    public RoomModel(String name, int min_people, int  max_people, int num_people, int id) {
        this.name = name;
        this.min_people = min_people;
        this.max_people = max_people;
        this.num_people = num_people;
        this.id = id;
    }
}