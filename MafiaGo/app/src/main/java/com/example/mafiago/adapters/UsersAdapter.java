package com.example.mafiago.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.mafiago.R;
import com.example.mafiago.models.UserModel;

import java.util.ArrayList;

public class UsersAdapter extends BaseAdapter {
    public ArrayList<UserModel> list_users;
    public Context context;
    public LayoutInflater layout;

    public UsersAdapter(ArrayList<UserModel> list_users, Context context)
    {
        this.list_users = list_users;
        this.context = context;
        layout=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount()
    {
        return list_users.size();
    }
    @Override
    public Object getItem(int position)
    {
        return list_users.get(position);
    }
    @Override
    public long getItemId(int position)
    {
        return position;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        view = layout.inflate(R.layout.item_user, null);
        TextView txt_nick = view.findViewById(R.id.nick);

        //txt_connect_mes.setTextColor(Color.parseColor("#FF0000"));

        txt_nick.setText(list_users.get(position).nick);
        return view;
    }
}