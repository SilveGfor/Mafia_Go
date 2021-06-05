package com.example.mafiago.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.mafiago.MainActivity;
import com.example.mafiago.R;
import com.example.mafiago.models.PrivateMessageModel;

import java.util.ArrayList;

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

                    txt_time.setText(list_mess.get(position).time);
                    txt_mess.setText(list_mess.get(position).message);
                }
                else
                {
                    view = layout.inflate(R.layout.item_other_private_message_usual, null);

                    TextView txt_time = view.findViewById(R.id.item_other_private_message_usual_time);
                    TextView txt_mess = view.findViewById(R.id.item_other_private_message_usual_message);

                    txt_time.setText(list_mess.get(position).time);
                    txt_mess.setText(list_mess.get(position).message);
                }
                /*
                String color= "#FFFFFF";
                switch (list_mess.get(position).type)
                {
                    case "alive":
                        color = "#FFFFFF";
                        break;
                    case "dead":
                        color = "#999999";
                        break;
                    case "last_message":
                        color = "#008800";
                        break;
                }

                txt_nick.setTextColor(Color.parseColor(color));
                txt_time.setTextColor(Color.parseColor(color));
                txt_mess.setTextColor(Color.parseColor(color));
                 */
                break;
            case "AnswerMes":
                if (list_mess.get(position).nickName.equals(MainActivity.NickName)) {
                    view = layout.inflate(R.layout.item_my_private_message_answer, null);

                    TextView txt_time = view.findViewById(R.id.item_my_private_message_answer_time);
                    TextView txt_mess = view.findViewById(R.id.item_my_private_message_answer_message);

                    txt_time.setText(list_mess.get(position).time);
                    txt_mess.setText(list_mess.get(position).message);

                    TextView txt_answer_nick = view.findViewById(R.id.item_my_private_message_answer_answer_nick);
                    TextView txt_answer_mes = view.findViewById(R.id.item_my_private_message_answer_answer_message);
                    TextView txt_answer_time = view.findViewById(R.id.item_my_private_message_answer_answer_time);

                    txt_time.setText(list_mess.get(position).time);
                    txt_mess.setText(list_mess.get(position).message);

                    int id = list_mess.get(position).answerId;

                    txt_answer_nick.setText(list_mess.get(id).nickName);
                    txt_answer_mes.setText(list_mess.get(id).message);
                    txt_answer_time.setText(list_mess.get(id).time);
                }
                else
                {
                    view = layout.inflate(R.layout.item_other_private_message_answer, null);

                    TextView txt_time = view.findViewById(R.id.item_other_private_message_answer_time);
                    TextView txt_mess = view.findViewById(R.id.item_other_private_message_answer_message);

                    txt_time.setText(list_mess.get(position).time);
                    txt_mess.setText(list_mess.get(position).message);

                    TextView txt_answer_nick = view.findViewById(R.id.item_other_private_message_answer_answer_nick);
                    TextView txt_answer_mes = view.findViewById(R.id.item_other_private_message_answer_answer_message);
                    TextView txt_answer_time = view.findViewById(R.id.item_other_private_message_answer_answer_time);

                    txt_time.setText(list_mess.get(position).time);
                    txt_mess.setText(list_mess.get(position).message);

                    int id = list_mess.get(position).answerId;

                    txt_answer_nick.setText(list_mess.get(id).nickName);
                    txt_answer_mes.setText(list_mess.get(id).message);
                    txt_answer_time.setText(list_mess.get(id).time);
                }
                break;
        }
        return view;
    }
}
