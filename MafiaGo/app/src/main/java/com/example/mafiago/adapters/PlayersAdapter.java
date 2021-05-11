package com.example.mafiago.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

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
        if (context != null) {
            layout=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;

        view = layout.inflate(R.layout.item_user, null);
        TextView TV_nick = view.findViewById(R.id.Item_user_TV_nick);
        ImageView IV_action = view.findViewById(R.id.Item_user_IV_Animation);
        ImageView IV_role = view.findViewById(R.id.Item_user_IV_role);
        switch (list_users.get(position).getRole())
        {
            case "none":
                IV_role.setImageResource(R.drawable.anonim);
                break;
            case "citizen":
                if (list_users.get(position).getAlive()) { IV_role.setImageResource(R.drawable.citizen_alive); }
                else { IV_role.setImageResource(R.drawable.citizen_dead); }
                break;
            case "mafia":
                if (list_users.get(position).getAlive()) { IV_role.setImageResource(R.drawable.mafia_alive); }
                else { IV_role.setImageResource(R.drawable.mafia_dead); }
                break;
            case "sheriff":
                if (list_users.get(position).getAlive()) { IV_role.setImageResource(R.drawable.sheriff_alive); }
                else { IV_role.setImageResource(R.drawable.sheriff_dead); }
                break;
            case "doctor":
                if (list_users.get(position).getAlive()) { IV_role.setImageResource(R.drawable.doctor_alive); }
                else { IV_role.setImageResource(R.drawable.doctor_dead); }
                break;
            case "lover":
                if (list_users.get(position).getAlive()) { IV_role.setImageResource(R.drawable.lover_alive); }
                else { IV_role.setImageResource(R.drawable.lover_dead); }
                break;
        }

        if (list_users.get(position).getAnimation())
        {
            IV_action.setVisibility(View.VISIBLE);
            switch (list_users.get(position).getAnimation_type()) {
                case "voting":
                    IV_action.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_voting));
                    break;
                case "lover":
                    IV_action.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_lover));
                    break;
                case "mafia":
                    IV_action.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_mafia));
                    break;
                case "doctor":
                    IV_action.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_doctor));
                    break;
                case "sheriff":
                    IV_action.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_sheriff));
                    break;
            }
            //анимация
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.voting);
            IV_action.startAnimation(animation);
        }

        TV_nick.setText(list_users.get(position).getNick());
        return view;
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