 package com.mafiago.adapters;

 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.util.Base64;
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

 import org.json.JSONException;
 import org.json.JSONObject;

 import java.util.ArrayList;

 import de.hdodenhof.circleimageview.CircleImageView;

 import static com.mafiago.MainActivity.socket;

 public class MessageAdapter extends BaseAdapter {
    public ArrayList<MessageModel> list_mess;
    public Context context;
    public LayoutInflater layout;

    public static final String APP_PREFERENCES = "user";
    public static final String APP_PREFERENCES_SHOW_ROLE= "show_role";
    private SharedPreferences mSettings;

    public MessageAdapter(ArrayList<MessageModel> list_mess, Context context)
    {
        this.list_mess = list_mess;
        this.context = context;
        if (context != null) {
            layout=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        mSettings = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
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
        switch (list_mess.get(position).mesType) {
            case "UsersMes":
                String color = "#FFFFFF";
                switch (list_mess.get(position).textType) {
                    case "alive":
                    case "dead":
                        color = "#FFFFFF";
                        break;
                    case "last_message":
                        color = "#FFB7AC";
                        break;
                }
                int previous_position = position - 1;
                if (position != 0 && list_mess.get(previous_position).nickName.equals(list_mess.get(position).nickName) && (list_mess.get(previous_position).mesType.equals("UsersMes") || list_mess.get(previous_position).mesType.equals("AnswerMes")))
                {
                    view = layout.inflate(R.layout.item_message_small, null);
                    TextView TV_message = view.findViewById(R.id.itemMessageSmall_message);
                    TV_message.setTextColor(Color.parseColor(color));
                    TV_message.setText(list_mess.get(position).message);
                }
                else {
                    view = layout.inflate(R.layout.item_message, null);

                    CircleImageView IV_avatar = view.findViewById(R.id.itemMessage_avatar);

                    if (list_mess.get(position).avatar != null) {
                        IV_avatar.setImageBitmap(list_mess.get(position).avatar);
                    }

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
                            Log.d("kkk", "Socket_отправка - get_profile - " + json.toString());
                        }
                    });

                    //TextView txt_time = view.findViewById(R.id.itemMessage_time);
                    TextView TV_message = view.findViewById(R.id.itemMessage_message);
                    TextView TV_nick = view.findViewById(R.id.itemMessage_nick);
                    TextView TV_status = view.findViewById(R.id.itemMessage_status);

                    //TODO: сделать сияние только админам
                    if (list_mess.get(position).nickName.equals("SilveGfor")) {
                        Shimmer shimmer = new Shimmer();
                        //shimmer.start(txt_nick);
                    }

                    if (!list_mess.get(position).status_text.equals("")) {
                        TV_nick.setText(list_mess.get(position).nickName + " {" + list_mess.get(position).status_text + "}");
                    } else {
                        TV_nick.setText(list_mess.get(position).nickName);
                    }

                    //txt_time.setTextColor(Color.parseColor(color));
                    TV_message.setTextColor(Color.parseColor(color));
                    TV_nick.setTextColor(Color.parseColor(color));

                    if (!list_mess.get(position).user_color.equals("")) {
                        TV_nick.setTextColor(Color.parseColor(list_mess.get(position).user_color));
                    } else {
                        TV_nick.setTextColor(Color.parseColor(color));
                    }

                    //txt_time.setText(list_mess.get(position).time);

                    TV_message.setText(list_mess.get(position).message);
                }

                break;

            case "DisconnectMes":
                view = layout.inflate(R.layout.item_connect_disconnect, null);

                TextView txt_disconnect_mes = view.findViewById(R.id.mesConnect);
                //TextView txt_disconnect_time = view.findViewById(R.id.mesTimeConnect);

                txt_disconnect_mes.setTextColor(Color.parseColor("#BE3144"));

                txt_disconnect_mes.setText(list_mess.get(position).message);
                //txt_disconnect_time.setText(list_mess.get(position).time);

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
                //TextView txt_connect_time = view.findViewById(R.id.mesTimeConnect);

                txt_connect_mes.setTextColor(Color.parseColor("#45744C"));

                txt_connect_mes.setText(list_mess.get(position).message);
                //txt_connect_time.setText(list_mess.get(position).time);

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

                //txt_time = view.findViewById(R.id.itemMessage_time);
                TextView TV_message = view.findViewById(R.id.itemMessage_message);
                TextView TV_nick = view.findViewById(R.id.itemMessage_nick);
                TextView TV_status = view.findViewById(R.id.itemMessage_status);

                ImageView IV_avatar = view.findViewById(R.id.itemMessage_avatar);

                if (list_mess.get(position).avatar != null) {
                    IV_avatar.setImageBitmap(list_mess.get(position).avatar);
                }

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

                if (list_mess.get(position).nickName.equals("SilveGfor"))
                {
                    Shimmer shimmer = new Shimmer();
                    //shimmer.start(txt_nick);
                }

                if (!list_mess.get(position).status_text.equals(""))
                {
                    TV_nick.setText(list_mess.get(position).nickName + " {" + list_mess.get(position).status_text + "}");
                }
                else
                {
                    TV_nick.setText(list_mess.get(position).nickName);
                }

                //txt_time.setText(list_mess.get(position).time);
                TV_message.setText(list_mess.get(position).message);

                TV_message.setTextColor(Color.parseColor("#AFFFFF"));
                TV_nick.setTextColor(Color.parseColor("#AFFFFF"));
                break;
            case "AnswerMes":
                previous_position = position - 1;
                if (position != 0 && list_mess.get(previous_position).nickName.equals(list_mess.get(position).nickName) && (list_mess.get(previous_position).mesType.equals("UsersMes") || list_mess.get(previous_position).mesType.equals("AnswerMes")))
                {
                    view = layout.inflate(R.layout.item_message_answer_small, null);
                    TV_message = view.findViewById(R.id.itemMessageAnswerSmall_message);
                    TextView TV_answer_message = view.findViewById(R.id.itemAnswerMessageSmall_answerMessage);
                    TextView TV_answer_nick = view.findViewById(R.id.itemAnswerMessageSmall_answerNick);

                    color = "#FFFFFF";
                    switch (list_mess.get(position).textType) {
                        case "alive":
                        case "dead":
                            color = "#FFFFFF";
                            break;
                        case "last_message":
                            color = "#FFB7AC";
                            break;
                    }
                    TV_message.setTextColor(Color.parseColor(color));
                    TV_message.setText(list_mess.get(position).message);

                    int id = list_mess.get(position).answerId;
                    for (int i = 0; i < list_mess.size(); i++) {
                        if (id == list_mess.get(i).num) {
                            if (!list_mess.get(i).status_text.equals("")) {
                                TV_answer_nick.setText(list_mess.get(i).nickName + " {" + list_mess.get(i).status_text + "}");
                            } else {
                                TV_answer_nick.setText(list_mess.get(i).nickName);
                            }
                            TV_answer_message.setText(list_mess.get(i).message);
                            color = "#FFFFFF";
                            switch (list_mess.get(i).textType) {
                                case "alive":
                                case "dead":
                                    color = "#FFFFFF";
                                    break;
                                case "last_message":
                                    color = "#FFB7AC";
                                    break;
                            }

                            if (!list_mess.get(i).user_color.equals("")) {
                                TV_answer_nick.setTextColor(Color.parseColor(list_mess.get(i).user_color));
                            } else {
                                TV_answer_nick.setTextColor(Color.parseColor(color));
                            }
                            TV_answer_message.setTextColor(Color.parseColor(color));
                        }
                    }
                }
                else {
                    view = layout.inflate(R.layout.item_message_answer, null);
                    //txt_time = view.findViewById(R.id.itemAnswerMessage_time);
                    TV_message = view.findViewById(R.id.itemAnswerMessage_message);
                    TV_status = view.findViewById(R.id.itemAnswerMessage_status);
                    TV_nick = view.findViewById(R.id.itemAnswerMessage_nick);

                    TextView TV_answer_message = view.findViewById(R.id.itemAnswerMessage_answerMessage);
                    TextView TV_answer_status = view.findViewById(R.id.itemAnswerMessage_answerStatus);
                    TextView TV_answer_nick = view.findViewById(R.id.itemAnswerMessage_answerNick);

                    int id = list_mess.get(position).answerId;

                    IV_avatar = view.findViewById(R.id.itemAnswerMessage_avatar);

                    if (list_mess.get(position).avatar != null) {
                        IV_avatar.setImageBitmap(list_mess.get(position).avatar);
                    }

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
                        Log.d("kkk", "Socket_отправка - get_profile - " + json.toString());
                    });

                    if (!list_mess.get(position).status_text.equals("")) {
                        TV_nick.setText(list_mess.get(position).nickName + " {" + list_mess.get(position).status_text + "}");
                    } else {
                        TV_nick.setText(list_mess.get(position).nickName);
                    }


                    //txt_time.setText(list_mess.get(position).time);
                    TV_message.setText(list_mess.get(position).message);

                    if (list_mess.get(position).nickName.equals("SilveGfor")) {
                        Shimmer shimmer = new Shimmer();
                        //shimmer.start(txt_nick);
                    }

                    for (int i = 0; i < list_mess.size(); i++) {
                        if (id == list_mess.get(i).num) {
                            if (!list_mess.get(i).status_text.equals("")) {
                                TV_answer_nick.setText(list_mess.get(i).nickName + " {" + list_mess.get(i).status_text + "}");
                            } else {
                                TV_answer_nick.setText(list_mess.get(i).nickName);
                            }
                            TV_answer_message.setText(list_mess.get(i).message);
                            color = "#FFFFFF";
                            switch (list_mess.get(i).textType) {
                                case "alive":
                                case "dead":
                                    color = "#FFFFFF";
                                    break;
                                //case "dead":
                                //color = "#999999";
                                //break;
                                case "last_message":
                                    color = "#FFB7AC";
                                    break;
                            }

                            if (!list_mess.get(i).user_color.equals("")) {
                                TV_answer_nick.setTextColor(Color.parseColor(list_mess.get(i).user_color));
                            } else {
                                TV_answer_nick.setTextColor(Color.parseColor(color));
                            }

                            TV_answer_message.setTextColor(Color.parseColor(color));
                        }
                    }

                    color = "#FFFFFF";
                    switch (list_mess.get(position).textType) {
                        case "alive":
                        case "dead":
                            color = "#FFFFFF";
                            break;
                        //case "dead":
                        //color = "#999999";
                        //break;
                        case "last_message":
                            color = "#FFB7AC";
                            break;
                    }

                    if (!list_mess.get(position).user_color.equals("")) {
                        TV_nick.setTextColor(Color.parseColor(list_mess.get(position).user_color));
                    } else {
                        TV_nick.setTextColor(Color.parseColor(color));
                    }

                    TV_message.setTextColor(Color.parseColor(color));
                    //txt_time.setTextColor(Color.parseColor(color));
                    //txt_answer_mes.setTextColor(Color.parseColor("#3E4A5A"));
                }
                break;
            case "SystemMes":
                view = layout.inflate(R.layout.item_system_message, null);

                TextView txt_system_mes = view.findViewById(R.id.mesConnect);
                //TextView txt_system_time = view.findViewById(R.id.mesTimeConnect);

                txt_system_mes.setTextColor(Color.parseColor("#FFE5A1"));

                txt_system_mes.setText(list_mess.get(position).message);
                //txt_system_time.setText(list_mess.get(position).time);
                break;
            case "JournalistMes":
                view = layout.inflate(R.layout.item_system_message, null);

                TextView txt_jour_mes = view.findViewById(R.id.mesConnect);
                //TextView txt_jour_time = view.findViewById(R.id.mesTimeConnect);

                txt_jour_mes.setTextColor(Color.parseColor("#AFFFFF"));

                txt_jour_mes.setText(list_mess.get(position).message);
                //txt_jour_time.setText(list_mess.get(position).time);
                break;
            case "KillMes":
                view = layout.inflate(R.layout.item_system_message, null);

                TextView txt_kill_mes = view.findViewById(R.id.mesConnect);
                //TextView txt_kill_time = view.findViewById(R.id.mesTimeConnect);

                txt_kill_mes.setTextColor(Color.parseColor("#BE3144"));

                txt_kill_mes.setText(list_mess.get(position).message);
                //txt_kill_time.setText(list_mess.get(position).time);
                break;
            case "KickMes":
                view = layout.inflate(R.layout.item_system_message, null);

                txt_connect_mes = view.findViewById(R.id.mesConnect);
                //txt_connect_time = view.findViewById(R.id.mesTimeConnect);

                txt_connect_mes.setTextColor(Color.parseColor("#BE3144"));

                txt_connect_mes.setText(list_mess.get(position).message);
                //txt_connect_time.setText(list_mess.get(position).time);
                break;
        }

        return view;
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