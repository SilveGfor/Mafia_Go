package com.example.mafiago.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.mafiago.R;
import com.example.mafiago.models.UserModel;

import java.util.ArrayList;

public class PlayersAdapter extends BaseAdapter
{

    public ArrayList<UserModel> list_users;
    public Context context;
    public LayoutInflater layout;

    // Конструктор
    public PlayersAdapter(ArrayList<UserModel> list_users, Context context) {

        this.list_users = list_users;
        this.context = context;
        layout=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        /*
        View view = convertView;

        view = layout.inflate(R.layout.item_user, null);
        TextView txt_nick = view.findViewById(R.id.nick);

        //txt_connect_mes.setTextColor(Color.parseColor("#FF0000"));

        txt_nick.setText(list_users.get(position).nick);
        return view;

         */
        Button button;

        if (convertView == null) {
            button = new Button(context);
            button.setText(list_users.get(position).nick);
        } else {
            button = (Button) convertView;
        }
        button.setId(position);

        return button;
    }

    @Override
    public int getCount() {
        return list_users.size();
    }

    @Override
    public Object getItem(int position) {
        return list_users.get(position);
    }


    @Override
    public long getItemId(int position) { return position; }
}