package com.mafiago.models;

public class FriendModel {
    public String nick;
    public boolean online;
    public String user_id_2;
    public String avatar;
    public String room;
    public int room_num;
    public int min_people; //Это про комнату, в которой друг
    public int max_people; //Это про комнату, в которой друг
    public boolean is_friend_request;
    public boolean is_my_request;
    public boolean has_password;

    //Друг в комнате
    public FriendModel(String nick, String user_id_2, boolean online, String avatar, String room, int room_num, int min_people, int max_people, boolean has_password) {
        this.nick = nick;
        this.online = online;
        this.user_id_2 = user_id_2;
        this.avatar = avatar;
        this.room = room;
        this.room_num = room_num;
        this.min_people = min_people;
        this.max_people = max_people;
        this.has_password = has_password;
    }
    //Друг не в комнате
    public FriendModel(String nick, String user_id_2, boolean online, String avatar) {
        this.nick = nick;
        this.online = online;
        this.user_id_2 = user_id_2;
        this.avatar = avatar;
        is_friend_request = false;
        room = "";
        room_num = -1;
    }
    //Запрос на дружбу
    public FriendModel(String nick, String user_id_2, boolean online, String avatar, boolean is_my_request) {
        this.nick = nick;
        this.online = online;
        this.user_id_2 = user_id_2;
        this.avatar = avatar;
        this.is_my_request = is_my_request;
        room = "";
        room_num = -1;
    }
}
