package com.mafiago.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.mafiago.MainActivity;
import com.example.mafiago.R;
import com.mafiago.models.PrivateMessageModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.mafiago.MainActivity.socket;

public class PrivateMessagesAdapter extends BaseAdapter {
    public ArrayList<PrivateMessageModel> list_mess;
    public Context context;
    public LayoutInflater layout;

    public PrivateMessagesAdapter(ArrayList<PrivateMessageModel> list_mess, Context context)
    {
        this.list_mess = list_mess;
        this.context = context;
        if (context != null) {
            layout=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
    }
    //количество сообщений
    @Override
    public int getCount()
    {
        return list_mess.size();
    }
    @Override
    public Object getItem(int position)
    {
        return list_mess.get(position);
    }
    @Override
    public long getItemId(int position)
    {
        return position;

    }
    //Отображение данных
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        switch (list_mess.get(position).MesType) {
            case "UserMes":
                if (list_mess.get(position).nickName.equals(MainActivity.NickName))
                {
                    view = layout.inflate(R.layout.item_my_private_message_usual, null);

                    TextView txt_time = view.findViewById(R.id.item_my_private_message_usual_time);
                    TextView txt_mess = view.findViewById(R.id.item_my_private_message_usual_message);
                    ImageView IV_readed = view.findViewById(R.id.itemMyPrivateMessageUsual_IV_readed);

                    if (list_mess.get(position).is_read) {
                        IV_readed.setBackground(ContextCompat.getDrawable(context, R.drawable.two_tips));
                    }
                    else {
                        IV_readed.setBackground(ContextCompat.getDrawable(context, R.drawable.one_tip));
                    }

                    txt_time.setText(list_mess.get(position).time);
                    txt_mess.setText(list_mess.get(position).message);
                }
                else
                {
                    view = layout.inflate(R.layout.item_other_private_message_usual, null);

                    TextView txt_time = view.findViewById(R.id.item_other_private_message_usual_time);
                    TextView txt_mess = view.findViewById(R.id.item_other_private_message_usual_message);

                    if (!list_mess.get(position).is_read) {
                        final JSONObject json = new JSONObject();
                        try {
                            json.put("nick", MainActivity.NickName);
                            json.put("session_id", MainActivity.Session_id);
                            json.put("user_id", MainActivity.User_id);
                            json.put("user_id_2", MainActivity.User_id_2);
                            json.put("num", list_mess.get(position).num);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        socket.emit("read_message", json);
                        Log.d("kkk", "Socket_отправка - read_message - "+ json.toString());
                    }

                    txt_time.setText(list_mess.get(position).time);
                    txt_mess.setText(list_mess.get(position).message);
                }
                break;
            case "AnswerMes":
                if (list_mess.get(position).nickName.equals(MainActivity.NickName)) {
                    view = layout.inflate(R.layout.item_my_private_message_answer, null);

                    TextView txt_time = view.findViewById(R.id.item_my_private_message_answer_time);
                    TextView txt_mess = view.findViewById(R.id.item_my_private_message_answer_message);
                    ImageView IV_readed = view.findViewById(R.id.itemMyPrivateMessageAnswer_IV_readed);

                    if (list_mess.get(position).is_read) {
                        IV_readed.setBackground(ContextCompat.getDrawable(context, R.drawable.two_tips));
                    }
                    else {
                        IV_readed.setBackground(ContextCompat.getDrawable(context, R.drawable.one_tip));
                    }

                    txt_time.setText(list_mess.get(position).time);
                    txt_mess.setText(list_mess.get(position).message);

                    TextView txt_answer_nick = view.findViewById(R.id.item_my_private_message_answer_answer_nick);
                    TextView txt_answer_mes = view.findViewById(R.id.item_my_private_message_answer_answer_message);
                    TextView txt_answer_time = view.findViewById(R.id.item_my_private_message_answer_answer_time);

                    txt_time.setText(list_mess.get(position).time);
                    txt_mess.setText(list_mess.get(position).message);

                    int id = list_mess.get(position).answerId;

                    for (int i = 0; i < list_mess.size(); i++)
                    {
                        if (id == list_mess.get(i).num)
                        {
                            txt_answer_nick.setText(list_mess.get(i).nickName);
                            txt_answer_mes.setText(list_mess.get(i).message);
                            txt_answer_time.setText(list_mess.get(i).time);
                        }
                    }
                }
                else
                {
                    view = layout.inflate(R.layout.item_other_private_message_answer, null);

                    TextView txt_time = view.findViewById(R.id.item_other_private_message_answer_time);
                    TextView txt_mess = view.findViewById(R.id.item_other_private_message_answer_message);

                    if (!list_mess.get(position).is_read) {
                        final JSONObject json = new JSONObject();
                        try {
                            json.put("nick", MainActivity.NickName);
                            json.put("session_id", MainActivity.Session_id);
                            json.put("user_id", MainActivity.User_id);
                            json.put("user_id_2", MainActivity.User_id_2);
                            json.put("num", list_mess.get(position).num);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        socket.emit("read_message", json);
                        Log.d("kkk", "Socket_отправка - read_message - "+ json.toString());
                    }

                    txt_time.setText(list_mess.get(position).time);
                    txt_mess.setText(list_mess.get(position).message);

                    TextView txt_answer_nick = view.findViewById(R.id.item_other_private_message_answer_answer_nick);
                    TextView txt_answer_mes = view.findViewById(R.id.item_other_private_message_answer_answer_message);
                    TextView txt_answer_time = view.findViewById(R.id.item_other_private_message_answer_answer_time);

                    txt_time.setText(list_mess.get(position).time);
                    txt_mess.setText(list_mess.get(position).message);

                    int id = list_mess.get(position).answerId;

                    for (int i = 0; i < list_mess.size(); i++)
                    {
                        if (id == list_mess.get(i).num)
                        {
                            txt_answer_nick.setText(list_mess.get(i).nickName);
                            txt_answer_mes.setText(list_mess.get(i).message);
                            txt_answer_time.setText(list_mess.get(i).time);
                        }
                    }
                }
                break;
        }
        return view;
    }
}
