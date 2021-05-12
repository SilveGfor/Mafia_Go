package com.example.mafiago.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.mafiago.R;
import com.example.mafiago.models.RoomModel;

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
    public long getItemId(int position)
    {
        return position;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;


        view = layout.inflate(R.layout.item_game, null);

        ProgressBar PB_users = view.findViewById(R.id.Item_game_progressBar);

        PB_users.setMax(list_room.get(position).max_people);
        PB_users.setProgress(list_room.get(position).num_people);

        TextView txt_room_name = view.findViewById(R.id.NameRoom);
        TextView txt_min_max_people = view.findViewById(R.id.MaxMinPeople);
        TextView txt_num_people = view.findViewById(R.id.NumPeople);

        //txt_connect_mes.setTextColor(Color.parseColor("#FF0000"));

        txt_room_name.setText(list_room.get(position).name);
        txt_min_max_people.setText(list_room.get(position).min_people + "/" + list_room.get(position).max_people);
        txt_num_people.setText("Игроки: " + list_room.get(position).num_people);
        return view;
    }
}