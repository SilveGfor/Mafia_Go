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
import com.mafiago.enums.Role;
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

        ArrayList<String> list_roles = list_room.get(position).list_roles;

        ImageView IV_maniac = view.findViewById(R.id.itemGame_IV_maniac);
        ImageView IV_doctor = view.findViewById(R.id.itemGame_IV_doctor);
        ImageView IV_lover = view.findViewById(R.id.itemGame_IV_lover);
        ImageView IV_mafia_don = view.findViewById(R.id.itemGame_IV_mafia_don);
        ImageView IV_poisoner = view.findViewById(R.id.itemGame_IV_poisoner);
        ImageView IV_journalist = view.findViewById(R.id.itemGame_IV_journalist);
        ImageView IV_terrorist = view.findViewById(R.id.itemGame_IV_terrorist);
        ImageView IV_doctor_of_easy_virtue = view.findViewById(R.id.itemGame_IV_doctor_of_easy_virtue);
        ImageView IV_bodyguard = view.findViewById(R.id.itemGame_IV_bodyguard);

        PB_users.setMax(list_room.get(position).max_people);
        PB_users.setProgress(list_room.get(position).num_people);

        TextView txt_room_name = view.findViewById(R.id.NameRoom);
        TextView txt_is_on = view.findViewById(R.id.ItemGame_is_on);
        TextView txt_min_max_people = view.findViewById(R.id.MaxMinPeople);
        TextView txt_num_people = view.findViewById(R.id.NumPeople);

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

        for (int i = 0; i < list_roles.size(); i++)
        {
            switch (list_roles.get(i))
            {
                case "doctor":
                    IV_doctor.setVisibility(View.VISIBLE);
                case "lover":
                    IV_lover.setVisibility(View.VISIBLE);
                case "mafia_don":
                    IV_mafia_don.setVisibility(View.VISIBLE);
                case "maniac":
                    IV_maniac.setVisibility(View.VISIBLE);
                case "terrorist":
                    IV_terrorist.setVisibility(View.VISIBLE);
                case "bodyguard":
                    IV_bodyguard.setVisibility(View.VISIBLE);
                case "poisoner":
                    IV_poisoner.setVisibility(View.VISIBLE);
                case "journalist":
                    IV_journalist.setVisibility(View.VISIBLE);
                case "doctor_of_easy_virtue":
                    IV_doctor_of_easy_virtue.setVisibility(View.VISIBLE);
            }
        }

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