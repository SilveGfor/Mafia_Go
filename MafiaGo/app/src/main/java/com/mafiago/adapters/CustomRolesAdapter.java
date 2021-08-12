package com.mafiago.adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mafiago.R;
import com.mafiago.models.RoleModel;

import org.json.JSONException;

import java.util.ArrayList;

public class CustomRolesAdapter extends BaseAdapter {
    public ArrayList<RoleModel> list_roles;
    public Context context;
    public LayoutInflater layout;
    public int min_people;

    public CustomRolesAdapter(ArrayList<RoleModel> list_roles, Context context)
    {
        this.list_roles = list_roles;
        this.context = context;
        layout=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return list_roles.size();
    }

    @Override
    public Object getItem(int position) {
        return list_roles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        view = layout.inflate(R.layout.item_custom_role, null);

        ImageView IV_role = view.findViewById(R.id.ItemCustomRole_IV_role);
        LinearLayout LL_line = view.findViewById(R.id.itemCustomRole_LL);

        if (position < min_people)
        {
            LL_line.setBackgroundColor(Color.parseColor("#4D8D58"));
        }

        switch (list_roles.get(position).role)
        {
            case CITIZEN:
                IV_role.setImageResource(R.drawable.citizen_alive);
                break;
            case SHERIFF:
                IV_role.setImageResource(R.drawable.sheriff_alive);
                break;
            case DOCTOR:
                IV_role.setImageResource(R.drawable.doctor_alive);
                break;
            case LOVER:
                IV_role.setImageResource(R.drawable.lover_alive);
                break;
            case MAFIA_DON:
                IV_role.setImageResource(R.drawable.mafia_don_alive);
                break;
            case MAFIA:
                IV_role.setImageResource(R.drawable.mafia_alive);
                break;
            case JOURNALIST:
                IV_role.setImageResource(R.drawable.journalist_alive);
                break;
            case MANIAC:
                IV_role.setImageResource(R.drawable.maniac_alive);
                break;
            case TERRORIST:
                IV_role.setImageResource(R.drawable.terrorist_alive);
                break;
            case BODYGUARD:
                IV_role.setImageResource(R.drawable.bodyguard_alive);
                break;
            case POISONER:
                IV_role.setImageResource(R.drawable.poisoner_alive);
                break;
            case DOCTOR_OF_EASY_VIRTUE:
                IV_role.setImageResource(R.drawable.doctor_of_easy_virtue_alive);
                break;
        }

        return view;
    }
}
