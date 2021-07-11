package com.mafiago.models;

public class FriendModel {
    public String nick;
    public boolean online;
    public String user_id_2;
    public String avatar;
    public String room;
    public int room_num;
    public boolean is_friend_request;

    //Друг в комнате
    public FriendModel(String nick, String user_id_2, boolean online, String avatar, String room, int room_num) {
        this.nick = nick;
        this.online = online;
        this.user_id_2 = user_id_2;
        this.avatar = avatar;
        this.room = room;
        this.room_num = room_num;
        is_friend_request = false;
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
    public FriendModel(String nick, String user_id_2, boolean online, String avatar, boolean is_friend_request) {
        this.nick = nick;
        this.online = online;
        this.user_id_2 = user_id_2;
        this.avatar = avatar;
        this.is_friend_request = is_friend_request;
        room = "";
        room_num = -1;
    }
}
