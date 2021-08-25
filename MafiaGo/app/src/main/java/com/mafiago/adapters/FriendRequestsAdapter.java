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

import androidx.appcompat.app.AppCompatActivity;

import com.example.mafiago.R;
import com.mafiago.MainActivity;
import com.mafiago.fragments.GameFragment;
import com.mafiago.fragments.PrivateMessagesFragment;
import com.mafiago.models.FriendModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.mafiago.MainActivity.socket;

public class FriendRequestsAdapter extends BaseAdapter {


    public ArrayList<FriendModel> list_requests;
    public Context context;
    public LayoutInflater layout;

    public FriendRequestsAdapter(ArrayList<FriendModel> list_requests, Context context)
    {
        this.list_requests = list_requests;
        this.context = context;
        layout=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = layout.inflate(R.layout.item_friend_request, null);

        CircleImageView IV_avatar = view.findViewById(R.id.itemFriendRequest_IV_avatar);
        TextView TV_nick = view.findViewById(R.id.itemFriendRequest_TV_nick);
        ImageView IV_online = view.findViewById(R.id.itemFriendRequest_IV_online);
        Button btn_accept = view.findViewById(R.id.itemFriendRequest_btn_accept);
        Button btn_dismiss = view.findViewById(R.id.itemFriendRequest_btn_dismiss);
        Button btn_cancel = view.findViewById(R.id.itemFriendRequest_btn_cancel);

        if (list_requests.get(position).avatar != null) {
            IV_avatar.setImageBitmap(fromBase64(list_requests.get(position).avatar));
        }

        TV_nick.setText(list_requests.get(position).nick);

        if (list_requests.get(position).online) {
            IV_online.setVisibility(View.VISIBLE);
        }

        if (!list_requests.get(position).is_my_request)
        {
            btn_accept.setVisibility(View.VISIBLE);
            btn_dismiss.setVisibility(View.VISIBLE);
            btn_accept.setOnClickListener(v -> {
                JSONObject json = new JSONObject();
                try {
                    json.put("nick", MainActivity.NickName);
                    json.put("session_id", MainActivity.Session_id);
                    json.put("user_id_2", list_requests.get(position).user_id_2);
                    json.put("accept", true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                socket.emit("accept_friend_request", json);
                Log.d("kkk", "Socket_отправка - accept_friend_request - "+ json.toString());
                list_requests.remove(position);
                this.notifyDataSetChanged();
            });
            btn_dismiss.setOnClickListener(v -> {
                JSONObject json = new JSONObject();
                try {
                    json.put("nick", MainActivity.NickName);
                    json.put("session_id", MainActivity.Session_id);
                    json.put("user_id_2", list_requests.get(position).user_id_2);
                    json.put("accept", false);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                socket.emit("accept_friend_request", json);
                Log.d("kkk", "Socket_отправка - accept_friend_request - "+ json.toString());
                list_requests.remove(position);
                this.notifyDataSetChanged();
            });
        }
        else
        {
            btn_cancel.setVisibility(View.VISIBLE);
            btn_cancel.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                View viewQuestion = layout.inflate(R.layout.dialog_ok_no, null);
                builder.setView(viewQuestion);
                AlertDialog alert = builder.create();
                TextView TV_text = viewQuestion.findViewById(R.id.dialogOkNo_text);
                Button btn_yes = viewQuestion.findViewById(R.id.dialogOkNo_btn_yes);
                Button btn_no = viewQuestion.findViewById(R.id.dialogOkNo_btn_no);
                TV_text.setText("Вы уверены, что хотите удалить запрос в друзья?");
                btn_yes.setOnClickListener(v1 -> {
                    JSONObject json = new JSONObject();
                    try {
                        json.put("nick", MainActivity.NickName);
                        json.put("session_id", MainActivity.Session_id);
                        json.put("user_id_2", list_requests.get(position).user_id_2);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    socket.emit("delete_friend_request", json);
                    Log.d("kkk", "Socket_отправка - delete_friend_request - "+ json.toString());
                    list_requests.remove(position);
                    alert.cancel();
                    this.notifyDataSetChanged();
                });
                btn_no.setOnClickListener(v12 -> {
                    alert.cancel();
                });
                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alert.show();
            });
        }
        return view;
    }

    @Override
    public int getCount() {
        return list_requests.size();
    }

    @Override
    public Object getItem(int position) {
        return list_requests.get(position);
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
