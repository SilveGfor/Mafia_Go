package com.mafiago.adapters;

import android.content.Context;
import android.graphics.Color;
import android.media.Image;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.mafiago.R;
import com.mafiago.MainActivity;
import com.mafiago.fragments.CreateRoomFragment;
import com.mafiago.models.RoleModel;
import com.mafiago.models.UserModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.mafiago.MainActivity.socket;

public class UsersInRoomAdapter extends BaseAdapter {
    public ArrayList<UserModel> list_users;
    public Context context;
    public LayoutInflater layout;

    public UsersInRoomAdapter(ArrayList<UserModel> list_users, Context context) {
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
        View view;

        view = layout.inflate(R.layout.item_user_in_room, null);

        TextView TV_nick = view.findViewById(R.id.itemUserInRoom_TV_nick);
        TextView TV_alive = view.findViewById(R.id.itemUserInRoom_TV_alive);

        TV_nick.setText(list_users.get(position).getNick());
        if (list_users.get(position).getAlive())
        {
            TV_alive.setText("Жив");
        }
        else
        {
            TV_alive.setText("Мёртв");
        }

        TV_nick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final JSONObject json = new JSONObject();
                try {
                    json.put("nick", MainActivity.NickName);
                    json.put("session_id", MainActivity.Session_id);
                    json.put("info_nick", TV_nick.getText());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                socket.emit("get_profile", json);
                Log.d("kkk", "Socket_отправка - get_profile - "+ json.toString());
            }
        });

        return view;
    }
}
