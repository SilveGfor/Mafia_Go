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
import androidx.core.content.ContextCompat;

import com.example.mafiago.R;
import com.mafiago.MainActivity;
import com.mafiago.fragments.MenuFragment;
import com.mafiago.models.PrivateChatModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.mafiago.MainActivity.socket;

public class PrivateChatsAdapter extends BaseAdapter {

    public ArrayList<PrivateChatModel> list_friends;
    public Context context;
    public LayoutInflater layout;

    public PrivateChatsAdapter(ArrayList<PrivateChatModel> list_friends, Context context)
    {
        this.list_friends = list_friends;
        this.context = context;
        layout=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = layout.inflate(R.layout.item_private_chat, null);

        TextView txt_nick = view.findViewById(R.id.Item_friend_nick);
        TextView txt_messages = view.findViewById(R.id.Item_friend_messages);
        ImageView IV_ban_unban_chat = view.findViewById(R.id.itemPrivateChat_IV_ban_chat);
        ImageView IV_avatar = view.findViewById(R.id.Item_friend_avatar);

        ImageView online = view.findViewById(R.id.Item_friend_IV_on_off);

        if (list_friends.get(position).getAvatar() != null) {
            IV_avatar.setImageBitmap(fromBase64(list_friends.get(position).getAvatar()));
        }

        txt_nick.setText(list_friends.get(position).getNick());
        txt_messages.setText(list_friends.get(position).getLast_message());

        if (list_friends.get(position).getBlocked()) {
            IV_ban_unban_chat.setImageResource(R.drawable.ic_change);
            IV_ban_unban_chat.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Разблокировка чата!")
                        .setMessage("Вы точно хотите разблокировать чат с игроком " + list_friends.get(position).getNick() + "?")
                        .setIcon(R.drawable.ic_info)
                        .setCancelable(false)
                        .setNegativeButton("Да", (dialog, id) -> {
                            dialog.cancel();
                            final JSONObject json = new JSONObject();
                            try {
                                json.put("nick", MainActivity.NickName);
                                json.put("session_id", MainActivity.Session_id);
                                json.put("user_id", MainActivity.User_id);
                                json.put("user_id_2", list_friends.get(position).getUser_id_2());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            socket.emit("unlock_chat", json);
                            Log.d("kkk", "Socket_отправка - unlock_chat - " + json.toString());
                            list_friends.remove(position);
                            this.notifyDataSetChanged();
                        })
                        .setPositiveButton(" Нет", (dialog, id) -> {
                            dialog.cancel();
                        });
                AlertDialog alert = builder.create();
                alert.show();
            });
        }
        else
        {
            IV_ban_unban_chat.setImageResource(R.drawable.ic_ban);
            IV_ban_unban_chat.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Блокировка чата!")
                        .setMessage("Вы точно хотите заблокировать чат с игроком " + list_friends.get(position).getNick() + "?")
                        .setIcon(R.drawable.ic_info)
                        .setCancelable(false)
                        .setNegativeButton("Да", (dialog, id) -> {
                            dialog.cancel();
                            final JSONObject json = new JSONObject();
                            try {
                                json.put("nick", MainActivity.NickName);
                                json.put("session_id", MainActivity.Session_id);
                                json.put("user_id", MainActivity.User_id);
                                json.put("user_id_2", list_friends.get(position).getUser_id_2());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            socket.emit("block_chat", json);
                            Log.d("kkk", "Socket_отправка - block_chat - " + json.toString());
                            list_friends.remove(position);
                            this.notifyDataSetChanged();
                                })
                        .setPositiveButton(" Нет", (dialog, id) -> {
                            dialog.cancel();
                        });
                AlertDialog alert = builder.create();
                alert.show();
            });
        }

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

    public Bitmap fromBase64(String image) {
        // Декодируем строку Base64 в массив байтов
        byte[] decodedString = Base64.decode(image, Base64.DEFAULT);

        // Декодируем массив байтов в изображение
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        // Помещаем изображение в ImageView
        return decodedByte;
    }
}
