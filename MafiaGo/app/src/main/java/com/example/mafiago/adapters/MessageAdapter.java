 package com.example.mafiago.adapters;

 import android.content.Context;
 import android.graphics.Color;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;

 import com.example.mafiago.MainActivity;
 import com.example.mafiago.R;
 import com.example.mafiago.models.MessageModel;
 import com.romainpiel.shimmer.Shimmer;
 import com.romainpiel.shimmer.ShimmerTextView;

 import org.json.JSONException;
 import org.json.JSONObject;

 import java.util.ArrayList;

 import static com.example.mafiago.MainActivity.socket;

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
        switch (list_mess.get(position).MesType) {
            case "UsersMes":
                view = layout.inflate(R.layout.item_message, null);

                ImageView IV_avatar = view.findViewById(R.id.item_message_avatar);

                IV_avatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final JSONObject json = new JSONObject();
                        try {
                            json.put("nick", MainActivity.NickName);
                            json.put("session_id", MainActivity.Session_id);
                            json.put("info_nick", list_mess.get(position).nickName);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        socket.emit("get_profile", json);
                        Log.d("kkk", "Socket_отправка - get_profile - "+ json.toString());
                    }
                });

                ShimmerTextView txt_nick = view.findViewById(R.id.mesNick);
                TextView txt_time = view.findViewById(R.id.mesTime);
                TextView txt_mess = view.findViewById(R.id.mesText);

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

                //TODO: сделать сияние только админам
                if (list_mess.get(position).nickName.equals("SilveGfor"))
                {
                    Shimmer shimmer = new Shimmer();
                    //shimmer.start(txt_nick);
                }

                txt_nick.setTextColor(Color.parseColor(color));
                txt_time.setTextColor(Color.parseColor(color));
                txt_mess.setTextColor(Color.parseColor(color));

                txt_nick.setText(list_mess.get(position).nickName);
                txt_time.setText(list_mess.get(position).time);
                txt_mess.setText(list_mess.get(position).message);
                break;

            case "DisconnectMes":
                view = layout.inflate(R.layout.item_connect_disconnect, null);

                TextView txt_disconnect_mes = view.findViewById(R.id.mesConnect);
                TextView txt_disconnect_time = view.findViewById(R.id.mesTimeConnect);

                txt_disconnect_mes.setTextColor(Color.parseColor("#FF0000"));

                txt_disconnect_mes.setText(list_mess.get(position).nickName + " вышел(-а) из чата");
                txt_disconnect_time.setText(list_mess.get(position).time);
                break;

            case "ConnectMes":
                view = layout.inflate(R.layout.item_connect_disconnect, null);

                TextView txt_connect_mes = view.findViewById(R.id.mesConnect);
                TextView txt_connect_time = view.findViewById(R.id.mesTimeConnect);

                txt_connect_mes.setTextColor(Color.parseColor("#08FB00"));

                txt_connect_mes.setText(list_mess.get(position).nickName + " вошёл(-а) в чат");
                txt_connect_time.setText(list_mess.get(position).time);
                break;
            case "VotingMes":
                view = layout.inflate(R.layout.item_message, null);

                txt_nick = view.findViewById(R.id.mesNick);
                txt_time = view.findViewById(R.id.mesTime);
                txt_mess = view.findViewById(R.id.mesText);

                if (list_mess.get(position).nickName.equals("SilveGfor"))
                {
                    Shimmer shimmer = new Shimmer();
                    //shimmer.start(txt_nick);
                }

                txt_nick.setText(list_mess.get(position).nickName);
                txt_time.setText(list_mess.get(position).time);
                txt_mess.setText(list_mess.get(position).message);

                txt_mess.setTextColor(Color.parseColor("#FFFF00"));
                txt_time.setTextColor(Color.parseColor("#FFFF00"));
                txt_nick.setTextColor(Color.parseColor("#FFFF00"));
                break;
            case "AnswerMes":
                view = layout.inflate(R.layout.item_answer_message, null);
                txt_nick = view.findViewById(R.id.mesNick);
                txt_time = view.findViewById(R.id.mesTime);
                txt_mess = view.findViewById(R.id.mesText);
                ShimmerTextView txt_answer_nick = view.findViewById(R.id.answerNick);
                TextView txt_answer_mes = view.findViewById(R.id.answerText);
                TextView txt_answer_time = view.findViewById(R.id.answerTime);

                txt_nick.setText(list_mess.get(position).nickName);
                txt_time.setText(list_mess.get(position).time);
                txt_mess.setText(list_mess.get(position).message);

                if (list_mess.get(position).nickName.equals("SilveGfor"))
                {
                    Shimmer shimmer = new Shimmer();
                    //shimmer.start(txt_nick);
                }

                int id = list_mess.get(position).answerId;

                if (list_mess.get(id).nickName.equals("SilveGfor"))
                {
                    Shimmer shimmer = new Shimmer();
                    //shimmer.start(txt_answer_nick);
                }

                txt_answer_nick.setText(list_mess.get(id).nickName);
                txt_answer_mes.setText(list_mess.get(id).message);
                txt_answer_time.setText(list_mess.get(id).time);
                break;
            case "SystemMes":
                view = layout.inflate(R.layout.item_connect_disconnect, null);

                TextView txt_system_mes = view.findViewById(R.id.mesConnect);
                TextView txt_system_time = view.findViewById(R.id.mesTimeConnect);

                txt_system_mes.setTextColor(Color.parseColor("#FF0000"));

                txt_system_mes.setText(list_mess.get(position).message);
                txt_system_time.setText(list_mess.get(position).time);
                break;
            case "KickMes":
                view = layout.inflate(R.layout.item_connect_disconnect, null);

                txt_connect_mes = view.findViewById(R.id.mesConnect);
                txt_connect_time = view.findViewById(R.id.mesTimeConnect);

                txt_connect_mes.setTextColor(Color.parseColor("#FF0000"));

                txt_connect_mes.setText(list_mess.get(position).message + " кикнул(-а) " + list_mess.get(position).nickName);
                txt_connect_time.setText(list_mess.get(position).time);
                break;
        }

        return view;
    }
}