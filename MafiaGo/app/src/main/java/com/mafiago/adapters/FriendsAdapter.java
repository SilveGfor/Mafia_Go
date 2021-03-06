package com.mafiago.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.mafiago.R;
import com.mafiago.MainActivity;
import com.mafiago.fragments.GameFragment;
import com.mafiago.fragments.MenuFragment;
import com.mafiago.fragments.PrivateMessagesFragment;
import com.mafiago.models.FriendModel;
import com.mafiago.models.PrivateChatModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

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

        CircleImageView IV_avatar = view.findViewById(R.id.itemFriend_avatar);
        TextView TV_nick = view.findViewById(R.id.itemFriend_nick);
        TextView TV_room = view.findViewById(R.id.itemFriend_TV_room);
        ImageView IV_online = view.findViewById(R.id.itemFriend_IV_online);
        ImageView IV_lock = view.findViewById(R.id.itemFriend_lock);
        Button btn_delete = view.findViewById(R.id.itemFriend_btn_delete);
        Button btn_chat = view.findViewById(R.id.itemFriend_btn_chat);

        if (list_friends.get(position).avatar != null) {
            IV_avatar.setImageBitmap(fromBase64(list_friends.get(position).avatar));
        }

        IV_avatar.setOnClickListener(v -> {
            final JSONObject json = new JSONObject();
            try {
                json.put("nick", MainActivity.NickName);
                json.put("session_id", MainActivity.Session_id);
                json.put("info_nick", list_friends.get(position).nick);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            socket.emit("get_profile", json);
            Log.d("kkk", "Socket_???????????????? - get_profile - "+ json.toString());
        });

        TV_nick.setText(list_friends.get(position).nick);

        if (list_friends.get(position).online) {
            IV_online.setVisibility(View.VISIBLE);
        }

        if (list_friends.get(position).has_password) {
            IV_lock.setVisibility(View.VISIBLE);
        }

        btn_chat.setOnClickListener(v -> {
            MainActivity.User_id_2 = list_friends.get(position).user_id_2;
            MainActivity.NickName_2 = list_friends.get(position).nick;
            MainActivity.bitmap_avatar_2 = fromBase64(list_friends.get(position).avatar);
            AppCompatActivity activity = (AppCompatActivity) view.getContext();
            activity.getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new PrivateMessagesFragment()).commit();
        });

        btn_delete.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View viewQuestion = layout.inflate(R.layout.dialog_ok_no, null);
            builder.setView(viewQuestion);
            AlertDialog alert = builder.create();
            TextView TV_text = viewQuestion.findViewById(R.id.dialogOkNo_text);
            Button btn_yes = viewQuestion.findViewById(R.id.dialogOkNo_btn_yes);
            Button btn_no = viewQuestion.findViewById(R.id.dialogOkNo_btn_no);
            TV_text.setText("???? ??????????????, ?????? ???????????? ?????????????? ???????????????????????? ???? ???????????? ?????????????");
            btn_yes.setOnClickListener(v1 -> {
                JSONObject json = new JSONObject();
                try {
                    json.put("nick", MainActivity.NickName);
                    json.put("session_id", MainActivity.Session_id);
                    json.put("user_id_2", list_friends.get(position).user_id_2);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                socket.emit("delete_friend", json);
                Log.d("kkk", "Socket_???????????????? - delete_friend - "+ json.toString());
                list_friends.remove(position);
                alert.cancel();
                this.notifyDataSetChanged();
            });
            btn_no.setOnClickListener(v12 -> {
                alert.cancel();
            });
            alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alert.show();
        });

        if (list_friends.get(position).room_num != -1)
        {
            TV_room.setVisibility(View.VISIBLE);
            TV_room.setText(list_friends.get(position).room);
            AppCompatActivity activity = (AppCompatActivity) view.getContext();
            TV_room.setOnClickListener(v -> {
                if (!list_friends.get(position).has_password) {
                    MainActivity.Game_id = list_friends.get(position).room_num;
                    MainActivity.RoomName = list_friends.get(position).room;
                    MainActivity.PlayersMinMaxInfo = "???? " + list_friends.get(position).min_people + " ???? " + list_friends.get(position).max_people;

                    activity.getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GameFragment()).commit();
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    View viewDang = layout.inflate(R.layout.dialog_room_password, null);
                    builder.setView(viewDang);
                    EditText ET_password = viewDang.findViewById(R.id.dialogRoomPassword_ET_password);
                    Button btn_join = viewDang.findViewById(R.id.dialogRoomPassword_btn_join);
                    AlertDialog alert = builder.create();
                    btn_join.setOnClickListener(v1 -> {
                        MainActivity.Game_id = list_friends.get(position).room_num;
                        MainActivity.RoomName = list_friends.get(position).room;
                        MainActivity.PlayersMinMaxInfo = "???? " + list_friends.get(position).min_people + " ???? " + list_friends.get(position).max_people;
                        MainActivity.Password = ET_password.getText().toString();
                        Log.d("kkk", "?????????????? ?? ???????? - " + MainActivity.Game_id);
                        alert.cancel();
                        activity.getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GameFragment()).commit();
                    });
                    alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alert.show();
                }
            });
        }

        /*
        if (!list_friends.get(position).is_friend_request)
        {
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
                Log.d("kkk", "Socket_???????????????? - accept_friend_request - "+ json.toString());
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
                Log.d("kkk", "Socket_???????????????? - accept_friend_request - "+ json.toString());
                list_friends.remove(position);
                this.notifyDataSetChanged();
            });
        }
         */

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
        // ???????????????????? ???????????? Base64 ?? ???????????? ????????????
        byte[] decodedString = Base64.decode(image, Base64.DEFAULT);

        // ???????????????????? ???????????? ???????????? ?? ??????????????????????
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        // ???????????????? ?????????????????????? ?? ImageView
        return decodedByte;
    }
}