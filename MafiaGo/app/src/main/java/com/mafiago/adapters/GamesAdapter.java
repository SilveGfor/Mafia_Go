package com.mafiago.adapters;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.mafiago.R;
import com.mafiago.models.RoomModel;

import java.util.ArrayList;

public class GamesAdapter extends BaseAdapter {
    public ArrayList<RoomModel> list_room;
    public Context context;
    public LayoutInflater layout;

    public GamesAdapter(ArrayList<RoomModel> list_room, Context context)
    {
        this.list_room = list_room;
        this.context = context;
         layout=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount()
    {
        return list_room.size();
    }
    @Override
    public Object getItem(int position)
    {
        return list_room.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        view = layout.inflate(R.layout.item_game, null);

        ProgressBar PB_users = view.findViewById(R.id.Item_game_progressBar);

        ImageView btn_info = view.findViewById(R.id.ItemGame_btn_info);

        PB_users.setMax(list_room.get(position).max_people);
        PB_users.setProgress(list_room.get(position).num_people);

        TextView txt_room_name = view.findViewById(R.id.NameRoom);
        TextView txt_is_on = view.findViewById(R.id.ItemGame_is_on);
        TextView txt_min_max_people = view.findViewById(R.id.MaxMinPeople);
        TextView txt_num_people = view.findViewById(R.id.NumPeople);

        //txt_connect_mes.setTextColor(Color.parseColor("#FF0000"));

        btn_info.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View view_listOfUsers = layout.inflate(R.layout.dialog_list_of_users_in_room, null);
            builder.setView(view_listOfUsers);

            ListView LV_users = view_listOfUsers.findViewById(R.id.dialogListOfUsersInRoom_LV);

            UsersInRoomAdapter usersInRoomAdapter = new UsersInRoomAdapter(list_room.get(position).list_users, context);
            LV_users.setAdapter(usersInRoomAdapter);


            AlertDialog alert = builder.create();
            alert.show();
        });

        if (list_room.get(position).is_on)
        {
            txt_is_on.setText("Игра идёт");
            txt_is_on.setTextColor(Color.parseColor("#F44336"));
        }
        else
        {
            txt_is_on.setText("Набор в комнату");
            txt_is_on.setTextColor(Color.parseColor("#4CAF50"));
        }

        txt_room_name.setText(list_room.get(position).name);
        txt_min_max_people.setText("от " + list_room.get(position).min_people + " до " + list_room.get(position).max_people);
        txt_num_people.setText("Игроки: " + list_room.get(position).num_people);
        return view;
    }
}