package com.mafiago.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.mafiago.R;
import com.mafiago.models.FriendModel;

import java.util.ArrayList;

public class FriendsAdapter extends BaseAdapter {

    public ArrayList<FriendModel> list_friends;
    public Context context;
    public LayoutInflater layout;

    public FriendsAdapter(ArrayList<FriendModel> list_friends, Context context)
    {
        this.list_friends = list_friends;
        this.context = context;
        layout=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        view = layout.inflate(R.layout.item_friend, null);

        TextView txt_nick = view.findViewById(R.id.Item_friend_nick);
        TextView txt_messages = view.findViewById(R.id.Item_friend_messages);

        ImageView online = view.findViewById(R.id.Item_friend_IV_on_off);

        txt_nick.setText(list_friends.get(position).getNick());
        txt_messages.setText("15 новых сообщений");

        if (list_friends.get(position).getOnline()) {
            online.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_online));
        }
        else
        {
            online.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_offline));
        }
        return view;
    }

    @Override
    public int getCount() {
        return list_friends.size();
    }

    @Override
    public Object getItem(int position) {
        return list_friends.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
