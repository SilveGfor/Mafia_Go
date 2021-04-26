package com.example.mafiago.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
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
        layout=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;

        view = layout.inflate(R.layout.item_user, null);
        TextView txt_nick = view.findViewById(R.id.nick);
        ImageView IV_action = view.findViewById(R.id.IV_Animation);

        //txt_connect_mes.setTextColor(Color.parseColor("#FF0000"));

        if (list_users.get(position).getAnimation())
        {
            IV_action.setVisibility(View.VISIBLE);
            Log.d("kkk", list_users.get(position).getAnimation_type());
            if (list_users.get(position).getAnimation_type().equals("voting"))
            {
                IV_action.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_voting));
            }
            else if (list_users.get(position).getAnimation_type().equals("lover"))
            {
                IV_action.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_lover));
            }
            else if (list_users.get(position).getAnimation_type().equals("mafia"))
            {
                IV_action.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_mafia));
            }
            else if (list_users.get(position).getAnimation_type().equals("doctor"))
            {
                IV_action.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_doctor));
            }
            else if (list_users.get(position).getAnimation_type().equals("sheriff"))
            {
                IV_action.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_sheriff));
            }
            //анимация
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.voting);
            IV_action.startAnimation(animation);
        }

        txt_nick.setText(list_users.get(position).nick);
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