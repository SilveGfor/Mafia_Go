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
 import android.widget.TextView;

 import com.mafiago.MainActivity;
 import com.example.mafiago.R;
 import com.mafiago.models.MessageModel;
 import com.romainpiel.shimmer.Shimmer;
 import com.romainpiel.shimmer.ShimmerTextView;

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
        boolean showRole = mSettings.getBoolean(APP_PREFERENCES_SHOW_ROLE, true);
        switch (list_mess.get(position).mesType) {
            case "UsersMes":
                view = layout.inflate(R.layout.item_message, null);

                CircleImageView IV_avatar = view.findViewById(R.id.itemMessage_avatar);

                if (list_mess.get(position).avatar != null) {
                    IV_avatar.setImageBitmap(fromBase64(list_mess.get(position).avatar));
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
                        Log.d("kkk", "Socket_отправка - get_profile - "+ json.toString());
                    }
                });

                //TextView txt_time = view.findViewById(R.id.itemMessage_time);
                TextView txt_mess = view.findViewById(R.id.itemMessage_text);
                TextView TV_main_role = view.findViewById(R.id.itemMessage_main_role);

                String color= "#FFFFFF";
                switch (list_mess.get(position).textType)
                {
                    case "alive":
                    case "dead":
                        color = "#FFFFFF";
                        break;
                    //case "dead":
                        //color = "#999999";
                        //break;
                    case "last_message":
                        color = "#F05941";
                        break;
                }

                //TODO: сделать сияние только админам
                if (list_mess.get(position).nickName.equals("SilveGfor"))
                {
                    Shimmer shimmer = new Shimmer();
                    //shimmer.start(txt_nick);
                }

                if (showRole) {
                    switch (list_mess.get(position).rang) {
                        case "user":
                            break;
                        case "moderator":
                            TV_main_role.setVisibility(View.VISIBLE);
                            TV_main_role.setText("модератор");
                            TV_main_role.setTextColor(Color.parseColor("#C71585"));
                            break;
                        case "admin":
                            TV_main_role.setVisibility(View.VISIBLE);
                            TV_main_role.setText("админ");
                            TV_main_role.setTextColor(Color.parseColor("#FF0000"));
                            break;
                        case "head_admin":
                            TV_main_role.setVisibility(View.VISIBLE);
                            TV_main_role.setText("глав. админ");
                            TV_main_role.setTextColor(Color.parseColor("#008B8B"));
                            break;
                        case "designer":
                            TV_main_role.setVisibility(View.VISIBLE);
                            TV_main_role.setText("дизайнер");
                            TV_main_role.setTextColor(Color.parseColor("#8A2BE2"));
                            break;
                        case "developer":
                            TV_main_role.setVisibility(View.VISIBLE);
                            TV_main_role.setText("разработчик");
                            TV_main_role.setTextColor(Color.parseColor("#8B0000"));
                            break;
                    }
                }

                //txt_time.setTextColor(Color.parseColor(color));
                txt_mess.setTextColor(Color.parseColor(color));

                //txt_time.setText(list_mess.get(position).time);
                txt_mess.setText(list_mess.get(position).nickName + ": " + list_mess.get(position).message);
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

                txt_connect_mes.setTextColor(Color.parseColor("#BA2F6E28"));

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
                txt_mess = view.findViewById(R.id.itemMessage_text);

                IV_avatar = view.findViewById(R.id.itemMessage_avatar);

                if (list_mess.get(position).avatar != null) {
                    IV_avatar.setImageBitmap(fromBase64(list_mess.get(position).avatar));
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

                //txt_time.setText(list_mess.get(position).time);
                txt_mess.setText(list_mess.get(position).message);

                txt_mess.setTextColor(Color.parseColor("#FFFF00"));
                break;
            case "AnswerMes":
                view = layout.inflate(R.layout.item_answer_message, null);
                //txt_time = view.findViewById(R.id.itemAnswerMessage_time);
                txt_mess = view.findViewById(R.id.itemAnswerMessage_text);
                TV_main_role = view.findViewById(R.id.itemAnswerMessage_main_role);

                TextView txt_answer_mes = view.findViewById(R.id.itemAnswerMessage_answerText);
                TextView TV_answer_main_role = view.findViewById(R.id.itemAnswerMessage_main_role);

                int id = list_mess.get(position).answerId;

                IV_avatar = view.findViewById(R.id.itemAnswerMessage_avatar);

                if (list_mess.get(position).avatar != null) {
                    IV_avatar.setImageBitmap(fromBase64(list_mess.get(position).avatar));
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

                switch (list_mess.get(position).rang) {
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


                //txt_time.setText(list_mess.get(position).time);
                txt_mess.setText(list_mess.get(position).nickName + ": " + list_mess.get(position).message);

                color= "#FFFFFF";
                switch (list_mess.get(position).textType)
                {
                    case "alive":
                    case "dead":
                        color = "#FFFFFF";
                        break;
                    //case "dead":
                        //color = "#999999";
                        //break;
                    case "last_message":
                        color = "#F05941";
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
                        txt_answer_mes.setText(list_mess.get(i).nickName + ": " + list_mess.get(i).message);
                        switch (list_mess.get(i).rang) {
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
                    }
                }

                txt_mess.setTextColor(Color.parseColor(color));
                //txt_time.setTextColor(Color.parseColor(color));
                txt_answer_mes.setTextColor(Color.parseColor(color));
                break;
            case "SystemMes":
                view = layout.inflate(R.layout.item_connect_disconnect, null);

                TextView txt_system_mes = view.findViewById(R.id.mesConnect);
                //TextView txt_system_time = view.findViewById(R.id.mesTimeConnect);

                txt_system_mes.setTextColor(Color.parseColor("#F05941"));

                txt_system_mes.setText(list_mess.get(position).message);
                //txt_system_time.setText(list_mess.get(position).time);
                break;
            case "JournalistMes":
                view = layout.inflate(R.layout.item_connect_disconnect, null);

                TextView txt_jour_mes = view.findViewById(R.id.mesConnect);
                //TextView txt_jour_time = view.findViewById(R.id.mesTimeConnect);

                txt_jour_mes.setTextColor(Color.parseColor("#F0BF41"));

                txt_jour_mes.setText(list_mess.get(position).message);
                //txt_jour_time.setText(list_mess.get(position).time);
                break;
            case "KillMes":
                view = layout.inflate(R.layout.item_connect_disconnect, null);

                TextView txt_kill_mes = view.findViewById(R.id.mesConnect);
                //TextView txt_kill_time = view.findViewById(R.id.mesTimeConnect);

                txt_kill_mes.setTextColor(Color.parseColor("#BE3144"));

                txt_kill_mes.setText(list_mess.get(position).message);
                //txt_kill_time.setText(list_mess.get(position).time);
                break;
            case "KickMes":
                view = layout.inflate(R.layout.item_connect_disconnect, null);

                txt_connect_mes = view.findViewById(R.id.mesConnect);
                //txt_connect_time = view.findViewById(R.id.mesTimeConnect);

                txt_connect_mes.setTextColor(Color.parseColor("#FF0000"));

                txt_connect_mes.setText(list_mess.get(position).message + " кикнул(-а) " + list_mess.get(position).nickName);
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