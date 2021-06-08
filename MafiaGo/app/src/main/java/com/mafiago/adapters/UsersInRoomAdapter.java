package com.mafiago.adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.cardview.widget.CardView;

import com.example.mafiago.R;
import com.mafiago.fragments.CreateRoomFragment;
import com.mafiago.models.RoleModel;
import com.mafiago.models.UserModel;

import org.json.JSONException;

import java.util.ArrayList;

public class UsersInRoomAdapter extends BaseAdapter {
    public ArrayList<UserModel> list_users;
    public Context context;
    public LayoutInflater layout;

    public UsersInRoomAdapter(ArrayList<UserModel> list_users, Context context)
    {
        this.list_users = list_users;
        this.context = context;
        layout=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        view = layout.inflate(R.layout.item_role, null);
        
        return view;
    }
}
