package com.mafiago.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.mafiago.R;
import com.mafiago.MainActivity;
import com.mafiago.fragments.GameFragment;
import com.mafiago.fragments.MenuFragment;
import com.mafiago.models.FriendModel;
import com.mafiago.models.PrivateChatModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.mafiago.MainActivity.socket;

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
        View view = layout.inflate(R.layout.item_friend, null);

        TextView txt_nick = view.findViewById(R.id.itemFriend_nick);
        ImageView IV_avatar = view.findViewById(R.id.itemFriend_avatar);

        ImageView online = view.findViewById(R.id.itemFriend_IV_on_off);
        ImageView IV_delete = view.findViewById(R.id.itemFriend_IV_delete);

        CardView CV_room = view.findViewById(R.id.itemFriend_CV_room);
        CardView CV_accept = view.findViewById(R.id.itemFriend_CV_accept);
        CardView CV_cancel = view.findViewById(R.id.itemFriend_CV_cancel);

        TextView TV_room_name = view.findViewById(R.id.itemFriend_TV_room_name);

        if (list_friends.get(position).avatar != null) {
            IV_avatar.setImageBitmap(fromBase64(list_friends.get(position).avatar));
        }

        txt_nick.setText(list_friends.get(position).nick);

        if (list_friends.get(position).online) {
            online.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_online));
        }
        else
        {
            online.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_offline));
        }

        if (!list_friends.get(position).is_friend_request)
        {
            IV_delete.setVisibility(View.VISIBLE);
            IV_delete.setOnClickListener(v -> {
                JSONObject json = new JSONObject();
                try {
                    json.put("nick", MainActivity.NickName);
                    json.put("session_id", MainActivity.Session_id);
                    json.put("user_id_2", list_friends.get(position).user_id_2);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                socket.emit("delete_friend", json);
                Log.d("kkk", "Socket_отправка - delete_friend - "+ json.toString());
                list_friends.remove(position);
                this.notifyDataSetChanged();
            });
            if (list_friends.get(position).room_num != -1)
            {
                CV_room.setVisibility(View.VISIBLE);
                TV_room_name.setText(list_friends.get(position).room);
                CV_room.setOnClickListener(v -> {
                    MainActivity.Game_id = list_friends.get(position).room_num;
                    MainActivity.RoomName = list_friends.get(position).room;
                    AppCompatActivity activity = (AppCompatActivity) view.getContext();
                    activity.getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GameFragment()).commit();
                });
            }
        }
        else
        {
            CV_accept.setVisibility(View.VISIBLE);
            CV_cancel.setVisibility(View.VISIBLE);
            CV_accept.setOnClickListener(v -> {
                JSONObject json = new JSONObject();
                try {
                    json.put("nick", MainActivity.NickName);
                    json.put("session_id", MainActivity.Session_id);
                    json.put("user_id_2", list_friends.get(position).user_id_2);
                    json.put("accept", true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                socket.emit("accept_friend_request", json);
                Log.d("kkk", "Socket_отправка - accept_friend_request - "+ json.toString());
                list_friends.remove(position);
                this.notifyDataSetChanged();
            });
            CV_cancel.setOnClickListener(v -> {
                JSONObject json = new JSONObject();
                try {
                    json.put("nick", MainActivity.NickName);
                    json.put("session_id", MainActivity.Session_id);
                    json.put("user_id_2", list_friends.get(position).user_id_2);
                    json.put("accept", false);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                socket.emit("accept_friend_request", json);
                Log.d("kkk", "Socket_отправка - accept_friend_request - "+ json.toString());
                list_friends.remove(position);
                this.notifyDataSetChanged();
            });
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

    public Bitmap fromBase64(String image) {
        // Декодируем строку Base64 в массив байтов
        byte[] decodedString = Base64.decode(image, Base64.DEFAULT);

        // Декодируем массив байтов в изображение
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        // Помещаем изображение в ImageView
        return decodedByte;
    }
}