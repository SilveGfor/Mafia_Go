package com.mafiago.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mafiago.R;
import com.mafiago.models.RoleModel;

import java.util.ArrayList;

public class RoleInRoomAdapter extends BaseAdapter {

    public ArrayList<RoleModel> list_roles;
    public Context context;
    public LayoutInflater layout;

    public RoleInRoomAdapter(ArrayList<RoleModel> list_roles, Context context)
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

        view = layout.inflate(R.layout.item_role_in_room, null);

        ImageView IV_role = view.findViewById(R.id.itemRoleInRoom_IV_role);
        TextView TV_count = view.findViewById(R.id.itemRoleInRoom_TV_count);

        TV_count.setText("x" + list_roles.get(position).count);

        switch (list_roles.get(position).role) {
            case MAFIA:
                IV_role.setImageResource(R.drawable.mafia_round);
                break;
            case SHERIFF:
                IV_role.setImageResource(R.drawable.sheriff_round);
                break;
            case CITIZEN:
                IV_role.setImageResource(R.drawable.citizen_round);
                break;
            case DOCTOR:
                IV_role.setImageResource(R.drawable.doctor_round);
                break;
            case LOVER:
                IV_role.setImageResource(R.drawable.lover_round);
                break;
            case MAFIA_DON:
                IV_role.setImageResource(R.drawable.mafia_don_round);
                break;
            case JOURNALIST:
                IV_role.setImageResource(R.drawable.journalist_round);
                break;
            case MANIAC:
                IV_role.setImageResource(R.drawable.maniac_round);
                break;
            case TERRORIST:
                IV_role.setImageResource(R.drawable.terrorist_round);
                break;
            case BODYGUARD:
                IV_role.setImageResource(R.drawable.bodyguard_round);
                break;
            case POISONER:
                IV_role.setImageResource(R.drawable.poisoner_round);
                break;
            case DOCTOR_OF_EASY_VIRTUE:
                IV_role.setImageResource(R.drawable.doctor_of_easy_virtue_round);
                break;
        }
        return view;
    }
}
