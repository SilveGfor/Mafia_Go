package com.example.mafiago.adapters;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.mafiago.R;
import com.example.mafiago.models.MessageModel;

import java.util.ArrayList;

public class MessageAdapter extends BaseAdapter {
    public ArrayList<MessageModel> list_mess;
    public Context context;
    public LayoutInflater layout;

    public MessageAdapter(ArrayList<MessageModel> list_mess, Context context)
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
    public View getView(int position, View convertView,
                        ViewGroup parent)
    {
        View view = convertView;
        //проверяем шаблон
        //if(convertView==null) {}
        if (list_mess.get(position).MesType == "UsersMes")
        {
            view = layout.inflate(R.layout.item_message, null);

            TextView txt_nick = view.findViewById(R.id.mesNick);
            TextView txt_time = view.findViewById(R.id.mesTime);
            TextView txt_mess = view.findViewById(R.id.mesText);

            txt_nick.setText(list_mess.get(position).nickName);
            txt_time.setText(list_mess.get(position).time);
            txt_mess.setText(list_mess.get(position).message);
        }
        else if (list_mess.get(position).MesType == "DisconnectMes")
        {
            view = layout.inflate(R.layout.item_connect_disconnect, null);

            TextView txt_connect_mes = view.findViewById(R.id.mesConnect);
            TextView txt_connect_time = view.findViewById(R.id.mesTimeConnect);

            txt_connect_mes.setTextColor(Color.parseColor("#FF0000"));

            txt_connect_mes.setText(list_mess.get(position).nickName + " вышел(-а) из чата");
            txt_connect_time.setText(list_mess.get(position).time);
        }
        else if (list_mess.get(position).MesType == "ConnectMes")
        {
            view = layout.inflate(R.layout.item_connect_disconnect, null);

            TextView txt_connect_mes = view.findViewById(R.id.mesConnect);
            TextView txt_connect_time = view.findViewById(R.id.mesTimeConnect);

            txt_connect_mes.setTextColor(Color.parseColor("#08FB00"));

            txt_connect_mes.setText(list_mess.get(position).nickName + " вошёл(-а) в чат");
            txt_connect_time.setText(list_mess.get(position).time);
        }
        else {

            view = layout.inflate(R.layout.item_answer_message, null);
            TextView txt_nick = view.findViewById(R.id.mesNick);
            TextView txt_time = view.findViewById(R.id.mesTime);
            TextView txt_mess = view.findViewById(R.id.mesText);
            TextView txt_answer_nick = view.findViewById(R.id.answerNick);
            TextView txt_answer_mes = view.findViewById(R.id.answerText);
            TextView txt_answer_time = view.findViewById(R.id.answerTime);

            txt_nick.setText(list_mess.get(position).nickName);
            txt_time.setText(list_mess.get(position).time);
            txt_mess.setText(list_mess.get(position).message);

            int id = list_mess.get(position).answerId;

            txt_answer_nick.setText(list_mess.get(id).nickName);
            txt_answer_mes.setText(list_mess.get(id).message);
            txt_answer_time.setText(list_mess.get(id).time);
        }

        return view;
    }
}