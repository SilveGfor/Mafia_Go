package com.mafiago.adapters;

import android.app.AlertDialog;
import android.content.Context;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mafiago.R;
import com.mafiago.MainActivity;
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

        TextView TV_nick = view.findViewById(R.id.ItemFriend_TV_nick);
        TextView TV_message = view.findViewById(R.id.ItemFriend_TV_lastMessage);
        TextView TV_time = view.findViewById(R.id.ItemFriend_TV_MessageTime);
        TextView TV_block = view.findViewById(R.id.ItemFriend_btn_block);
        TextView TV_mesCount = view.findViewById(R.id.ItemFriend_TV_messagesCount);
        ImageView IV_avatar = view.findViewById(R.id.ItemFriend_IV_avatar);
        ImageView IV_online = view.findViewById(R.id.itemPrivateChat_IV_online);
        ImageView IV_is_read = view.findViewById(R.id.itemPrivateChat_IV_readed);

        if (list_friends.get(position).getAvatar() != null) {
            IV_avatar.setImageBitmap(fromBase64(list_friends.get(position).getAvatar()));
        }

        if (list_friends.get(position).my_message) {
            if (list_friends.get(position).is_read)
            {
                IV_is_read.setImageResource(R.drawable.two_tips);
            }
            else
            {
                IV_is_read.setImageResource(R.drawable.one_tip);
            }
        }
        else
        {
            if (!list_friends.get(position).is_read)
            {
                TV_nick.setTextColor(Color.parseColor("#FFFFFF"));
                TV_message.setTextColor(Color.parseColor("#FFFFFF"));
            }
            IV_is_read.setVisibility(View.INVISIBLE);
        }

        TV_time.setText(list_friends.get(position).time);
        TV_nick.setText(list_friends.get(position).getNick());
        TV_message.setText(list_friends.get(position).getLast_message());

        if (list_friends.get(position).getBlocked()) {
            TV_nick.setTextColor(Color.parseColor("#F05941"));
            TV_message.setTextColor(Color.parseColor("#F05941"));
            if (list_friends.get(position).i_blocked)
            {
                TV_block.setVisibility(View.VISIBLE);
                TV_block.setText("разблокировать");
                TV_block.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        View viewQuestion = layout.inflate(R.layout.dialog_ok_no, null);
                        builder.setView(viewQuestion);
                        AlertDialog alert = builder.create();
                        TextView TV_text = viewQuestion.findViewById(R.id.dialogOkNo_text);
                        Button btn_yes = viewQuestion.findViewById(R.id.dialogOkNo_btn_yes);
                        Button btn_no = viewQuestion.findViewById(R.id.dialogOkNo_btn_no);
                        TV_text.setText("Вы точно хотите заблокировать чат с игроком " + MainActivity.NickName_2 + "?");
                        btn_yes.setOnClickListener(v1 -> {
                            alert.cancel();
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
                            TV_nick.setTextColor(Color.parseColor("#FFFFFF"));
                            TV_message.setTextColor(Color.parseColor("#FFFFFF"));
                            TV_block.setVisibility(View.GONE);
                        });
                        btn_no.setOnClickListener(v12 -> {
                            alert.cancel();
                        });
                        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        alert.show();
                    }
                });
            }
            else
            {
                TV_block.setVisibility(View.VISIBLE);
                TV_block.setText("заблокирован");
            }
        }


        if (list_friends.get(position).getOnline()) {
            IV_online.setVisibility(View.VISIBLE);
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
