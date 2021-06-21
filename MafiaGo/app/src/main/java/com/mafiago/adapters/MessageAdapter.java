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

 import com.mafiago.MainActivity;
 import com.example.mafiago.R;
 import com.mafiago.models.MessageModel;
 import com.romainpiel.shimmer.Shimmer;
 import com.romainpiel.shimmer.ShimmerTextView;

 import org.json.JSONException;
 import org.json.JSONObject;

 import java.util.ArrayList;

 import static com.mafiago.MainActivity.socket;

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
                TextView TV_main_role = view.findViewById(R.id.itemMessage_main_role);

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

                switch (list_mess.get(position).main_role)
                {
                    case "user":
                        break;
                    case "moderator":
                        TV_main_role.setText("модератор");
                        TV_main_role.setTextColor(Color.parseColor("#9C27B0"));
                        break;
                    case "admin":
                        TV_main_role.setText("админ");
                        TV_main_role.setTextColor(Color.parseColor("#9C27B0"));
                        break;
                    case "head_admin":
                        TV_main_role.setText("глав. админ");
                        TV_main_role.setTextColor(Color.parseColor("#9C27B0"));
                        break;
                    case "designer":
                        TV_main_role.setText("дизайнер");
                        TV_main_role.setTextColor(Color.parseColor("#9C27B0"));
                        break;
                    case "developer":
                        TV_main_role.setText("разработчик");
                        TV_main_role.setTextColor(Color.parseColor("#FF0000"));
                        break;
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

                txt_disconnect_mes.setText(list_mess.get(position).message);
                txt_disconnect_time.setText(list_mess.get(position).time);

                view.setOnClickListener(v -> {
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
                });

                break;
            case "ConnectMes":
                view = layout.inflate(R.layout.item_connect_disconnect, null);

                TextView txt_connect_mes = view.findViewById(R.id.mesConnect);
                TextView txt_connect_time = view.findViewById(R.id.mesTimeConnect);

                txt_connect_mes.setTextColor(Color.parseColor("#08FB00"));

                txt_connect_mes.setText(list_mess.get(position).message);
                txt_connect_time.setText(list_mess.get(position).time);

                view.setOnClickListener(v -> {
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
                });

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
                //txt_time.setTextColor(Color.parseColor("#FFFF00"));
                txt_nick.setTextColor(Color.parseColor("#FFFF00"));
                break;
            case "AnswerMes":
                view = layout.inflate(R.layout.item_answer_message, null);
                txt_nick = view.findViewById(R.id.mesNick);
                txt_time = view.findViewById(R.id.mesTime);
                txt_mess = view.findViewById(R.id.mesText);
                TV_main_role = view.findViewById(R.id.itemAnswerMessage_main_role);

                ShimmerTextView txt_answer_nick = view.findViewById(R.id.answerNick);
                TextView txt_answer_mes = view.findViewById(R.id.answerText);
                TextView txt_answer_time = view.findViewById(R.id.answerTime);
                TextView TV_answer_main_role = view.findViewById(R.id.itemAnswerMessage_answer_main_role);

                int id = list_mess.get(position).answerId;

                IV_avatar = view.findViewById(R.id.item_answer_message_avatar);

                IV_avatar.setOnClickListener(v -> {
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
                });

                switch (list_mess.get(position).main_role) {
                    case "user":
                        break;
                    case "moderator":
                        TV_main_role.setText("модератор");
                        TV_main_role.setTextColor(Color.parseColor("#9C27B0"));
                        break;
                    case "admin":
                        TV_main_role.setText("админ");
                        TV_main_role.setTextColor(Color.parseColor("#9C27B0"));
                        break;
                    case "head_admin":
                        TV_main_role.setText("глав. админ");
                        TV_main_role.setTextColor(Color.parseColor("#9C27B0"));
                        break;
                    case "designer":
                        TV_main_role.setText("дизайнер");
                        TV_main_role.setTextColor(Color.parseColor("#9C27B0"));
                        break;
                    case "developer":
                        TV_main_role.setText("разработчик");
                        TV_main_role.setTextColor(Color.parseColor("#FF0000"));
                        break;
                }

                switch (list_mess.get(id).main_role) {
                    case "user":
                        break;
                    case "moderator":
                        TV_answer_main_role.setText("модератор");
                        TV_answer_main_role.setTextColor(Color.parseColor("#9C27B0"));
                        break;
                    case "admin":
                        TV_answer_main_role.setText("админ");
                        TV_answer_main_role.setTextColor(Color.parseColor("#9C27B0"));
                        break;
                    case "head_admin":
                        TV_answer_main_role.setText("глав. админ");
                        TV_answer_main_role.setTextColor(Color.parseColor("#9C27B0"));
                        break;
                    case "designer":
                        TV_answer_main_role.setText("дизайнер");
                        TV_answer_main_role.setTextColor(Color.parseColor("#9C27B0"));
                        break;
                    case "developer":
                        TV_answer_main_role.setText("разработчик");
                        TV_answer_main_role.setTextColor(Color.parseColor("#FF0000"));
                        break;
                }

                txt_nick.setText(list_mess.get(position).nickName);
                txt_time.setText(list_mess.get(position).time);
                txt_mess.setText(list_mess.get(position).message);

                color= "#FFFFFF";
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

                if (list_mess.get(position).nickName.equals("SilveGfor"))
                {
                    Shimmer shimmer = new Shimmer();
                    //shimmer.start(txt_nick);
                }

                for (int i = 0; i < list_mess.size(); i++)
                {
                    if (id == list_mess.get(i).num)
                    {
                        txt_answer_nick.setText(list_mess.get(i).nickName);
                        txt_answer_mes.setText(list_mess.get(i).message);
                        txt_answer_time.setText(list_mess.get(i).time);
                    }
                }

                txt_mess.setTextColor(Color.parseColor(color));
                txt_time.setTextColor(Color.parseColor(color));
                txt_nick.setTextColor(Color.parseColor(color));;
                txt_answer_nick.setTextColor(Color.parseColor(color));
                txt_answer_mes.setTextColor(Color.parseColor(color));
                txt_answer_time.setTextColor(Color.parseColor(color));
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