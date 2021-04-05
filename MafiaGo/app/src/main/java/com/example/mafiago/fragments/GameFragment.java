package com.example.mafiago.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.mafiago.MainActivity;
import com.example.mafiago.R;
import com.example.mafiago.adapters.MessageAdapter;
import com.example.mafiago.adapters.PlayersAdapter;
import com.example.mafiago.adapters.UsersAdapter;
import com.example.mafiago.models.MessageModel;
import com.example.mafiago.models.UserModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Objects;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class GameFragment extends Fragment {
    public ListView listView_chat;
    public GridView gridView_users;

    public CardView cardAnswer;

    public TextView timer;
    public TextView day_time;
    public TextView answer_nick;
    public TextView answer_mes;

    public EditText editText;

    public FloatingActionButton btnSend;

    public Button btnExit;
    public Button btnDeleteAnswer;

    ArrayList<MessageModel> list_chat = new ArrayList<>();
    ArrayList<UserModel> list_users = new ArrayList<>();

    int room_num = MainActivity.Game_id;
    int answer_id = -1;
    public boolean Not_First = false;

    int num = -1;

    private Socket socket;
    {
        try
        {
            socket = IO.socket("https://" + MainActivity.url);
        }catch (URISyntaxException e){
            throw new RuntimeException();
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_game, container, false);

        listView_chat = view.findViewById(R.id.ListMes);
        gridView_users = view.findViewById(R.id.ListUsers);
        cardAnswer = view.findViewById(R.id.answerCard);
        answer_nick = view.findViewById(R.id.answerNickChat);
        answer_mes = view.findViewById(R.id.answerTextChat);
        editText = view.findViewById(R.id.InputMes);

        btnSend = view.findViewById(R.id.btnSendMes);
        btnDeleteAnswer = view.findViewById(R.id.btnDeleteAnswer);
        btnExit = view.findViewById(R.id.btnExitChat);

        timer = view.findViewById(R.id.timer);
        day_time = view.findViewById(R.id.day_time);

        cardAnswer.setVisibility(View.GONE);

        ArrayList<UserModel> list_users2 = new ArrayList<>();

        list_users2.add(new UserModel("Sil1", R.drawable.citizen));
        list_users2.add(new UserModel("SilVegfor", R.drawable.citizen));
        list_users2.add(new UserModel("Sil3", R.drawable.citizen));
        list_users2.add(new UserModel("SilveGfor", R.drawable.citizen));
        list_users2.add(new UserModel("Sil5", R.drawable.citizen));
        list_users2.add(new UserModel("Sil6", R.drawable.citizen));
        list_users2.add(new UserModel("Sil7", R.drawable.citizen));
        list_users2.add(new UserModel("Sil8", R.drawable.citizen));
        list_users2.add(new UserModel("Sil9", R.drawable.citizen));
        list_users2.add(new UserModel("Sil10", R.drawable.citizen));
        gridView_users.setAdapter(new PlayersAdapter(list_users2, getActivity()));

        socket.connect();

        SocketTask socketTask = new SocketTask();
        socketTask.execute();


        final JSONObject json3 = new JSONObject();
        try {
            json3.put("nick", MainActivity.NickName);
            json3.put("session_id", MainActivity.Session_id);
            json3.put("room", room_num);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("get_in_room", json3);
        Log.d("kkk", "Socket_отправка - get_in_room"+ json3.toString());


        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final JSONObject json2 = new JSONObject();
                try {
                    json2.put("nick", MainActivity.NickName);
                    json2.put("session_id",  MainActivity.Session_id);
                    json2.put("room", room_num);
                    json2.put("message", editText.getText().toString());
                    json2.put("link", answer_id);
                    answer_id = -1;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("kkk", "Socket_отправка - "+ json2.toString());
                socket.emit("user_message", json2);
                answer_id = -1;
                cardAnswer.setVisibility(View.GONE);

                /* Для Офлайн-тестов
                if(answer_id == -1)
                {
                    ChatModel model = new ChatModel(editText.getText().toString(), "2-00", "SilveGfor22", "UsersMes");
                    list_chat.add(model);
                    CustomList customList = new CustomList(list_chat, getContext());
                    listView_chat.setAdapter(customList);
                    listView_chat.setSelection(customList.getCount() - 1);
                }
                else
                {
                    Log.d("kkk", "id - " + answer_id + "  ----  элемент - " + list_chat.get(answer_id).message);
                    ChatModel model = new ChatModel(editText.getText().toString(), "3-00", "SilveGfor22", "AnswerMes", list_chat.get(answer_id).answerNick, list_chat.get(answer_id).message, list_chat.get(answer_id).answerTime, answer_id);
                    list_chat.add(model);
                    CustomList customList = new CustomList(list_chat, getContext());
                    listView_chat.setAdapter(customList);
                    listView_chat.setSelection(customList.getCount() - 1);
                }
                answer_id = -1;
                 */
                editText.setText("");
            }
        });
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final JSONObject json2 = new JSONObject();
                try {
                    json2.put("nick", MainActivity.NickName);
                    json2.put("session_id",  MainActivity.Session_id);
                    json2.put("room", room_num);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("kkk", "Socket_отправка_leave_user - "+ json2.toString());
                socket.emit("leave_room", json2);
                editText.setText("");
                //socket.close();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
            }
        });

        gridView_users.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("kkk", list_users2.get(position).nick);
            }
        });

        listView_chat.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("kkk", "----");
                Log.d("kkk", "position - " + String.valueOf(position));
                Log.d("kkk", "----");
                if(list_chat.get(position).MesType.equals("OtherMes") || list_chat.get(position).MesType.equals("AnswerMes"))
                {
                    answer_id = position;
                }
                answer_id = position;
                answer_nick.setText(list_chat.get(position).nickName);
                answer_mes.setText(list_chat.get(position).message);
                cardAnswer.setVisibility(View.VISIBLE);
            }
        });

        btnDeleteAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                answer_id = -1;
                cardAnswer.setVisibility(View.GONE);
            }
        });



        return view;
    }

    class SocketTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("kkk", "onPreExecute");
        }

        @Override
        protected Void doInBackground(Void... voids) {

            socket.on("connect", onConnect);
            socket.on("disconnect", onDisconnect);
            socket.on("get_in_room", onGetInRoom);
            socket.on("user_message", onNewMessage);
            socket.on("connect_to_room", onConnectToRoom);
            socket.on("leave_room", onLeaveUser);
            socket.on("timer", onTimer);
            socket.on("time", onTime);
            socket.on("role", onRole);
            socket.on("restart", onRestart);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d("kkk", "onPostExecute");
        }
    }

    ////////////////
    ////////////////
    //       SOCKETS start
    ////////////////
    ////////////////

    private Emitter.Listener onConnectToRoom = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("kkk", "data - : " + args[0].toString());
                }
            });
        }
    };

    private Emitter.Listener onLeaveUser = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String nick;
                    String time;
                    try {
                        nick = data.getString("nick");
                        Log.d("kkk", num + "  onLeaveUser1  " + data.getInt("num"));
                        if (data.getInt("num") > num)
                        {
                            num = data.getInt("num");
                        }
                        time = data.getString("time");
                        Log.d("kkk", num + "  onLeaveUser2  " + data.getInt("num"));

                        MessageModel model = new MessageModel("", time.substring(11,16), nick, "DisconnectMes");
                        list_chat.add(model);
                        MessageAdapter messageAdapter = new MessageAdapter(list_chat, getContext());
                        listView_chat.setAdapter(messageAdapter);

                        //UserModel user_model = new UserModel(nick);
                        //list_users.remove(user_model);
                        for (int i=list_users.size()-1; i>=0; i--)
                        {
                            if (list_users.get(i).nick.equals(nick))
                            {
                                Log.d("kkk", "remove" + list_users.get(i).nick);
                                list_users.remove(i);
                            }
                        }
                        for (int i=list_users.size()-1; i>=0; i--)
                        {
                            Log.d("kkk", list_users.get(i).nick);
                        }
                        PlayersAdapter playersAdapter = new PlayersAdapter(list_users, getContext());
                        gridView_users.setAdapter(playersAdapter);
                    } catch (JSONException e) {
                        return;
                    }
                }
            });
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String nick;
                    String message;
                    String time;
                    String status;
                    Log.d("kkk", String.valueOf(data));
                    int link;
                    try {
                        nick = data.getString("nick");
                        if (data.getInt("num") > num)
                        {
                            num = data.getInt("num");
                            time = data.getString("time");
                            message = data.getString("message");
                            status = data.getString("status");
                            link = data.getInt("link");
                            if(link == -1)
                            {
                                Log.d("kkk", "UsersMes");
                                MessageModel messageModel = new MessageModel(message, time.substring(11,16), nick, "UsersMes");
                                //list_chat.add(0, messageModel);
                                list_chat.add(messageModel);
                                MessageAdapter messageAdapter = new MessageAdapter(list_chat, getContext());
                                listView_chat.setAdapter(messageAdapter);
                                listView_chat.setSelection(messageAdapter.getCount() - 1);
                            }
                            else
                            {
                                Log.d("kkk", "AnswerMes");
                                MessageModel messageModel = new MessageModel(message, time.substring(11,16), nick, "AnswerMes", list_chat.get(link).answerNick, list_chat.get(link).message, list_chat.get(link).answerTime, link);
                                list_chat.add(messageModel);
                                MessageAdapter messageAdapter = new MessageAdapter(list_chat, getContext());
                                listView_chat.setAdapter(messageAdapter);
                                listView_chat.setSelection(messageAdapter.getCount() - 1);
                            }
                        }
                        else
                        {
                            Log.d("kkk", "num_message - : " + num);
                        }
                    } catch (JSONException e) {
                        Log.d("kkk", "JSONException");
                        return;
                    }
                }
            });
        }
    };

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (Not_First) {
                        final JSONObject json2 = new JSONObject();
                        try {
                            json2.put("nick", MainActivity.NickName);
                            json2.put("room", room_num);
                            json2.put("last_message_num", num);
                            json2.put("session_id", MainActivity.Session_id);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        socket.emit("connect_to_room", json2);
                        Log.d("kkk", "CONNECT");
                    }
                }
            });
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("kkk", "DISCONNECT");
                }
            });
        }
    };

    private Emitter.Listener onGetInRoom = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("kkk", "Принял - get_in_room: " );
                    JSONObject data = (JSONObject) args[0];
                    String nick;
                    String time;
                    try {
                        nick = data.getString("nick");
                        if (data.getInt("num") > num)
                        {
                            num = data.getInt("num");
                            time = data.getString("time");
                            Log.d("kkk", num + "  onGetInRoom  " + data.getInt("num"));

                            MessageModel messageModel = new MessageModel("", time.substring(11,16), nick, "ConnectMes");
                            list_chat.add(messageModel);
                            MessageAdapter messageAdapter = new MessageAdapter(list_chat, getContext());
                            listView_chat.setAdapter(messageAdapter);
                            Not_First = true;

                            //прошлый вариант
                            //UserModel user_model = new UserModel(nick);
                            //list_users.add(user_model);
                            //UsersAdapter usersAdapter = new UsersAdapter(list_users, getContext());
                            //listView_users.setAdapter(usersAdapter);



                        }
                        else
                        {
                            Log.d("kkk", num + "  onGetInRoom  " + "что-то пошло не так " + data.getInt("num"));
                        }

                    } catch (JSONException e) {
                        return;
                    }
               }
            });
        }
    };

    //принимает таймер
    private Emitter.Listener onTimer = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String time;
                    try {
                        time = data.getString("timer");
                        timer.setText(time);
                        Log.d("kkk", "Socket_принять - Timer " + time);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    //принимает время дня
    private Emitter.Listener onTime = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String time;
                    try {
                        time = data.getString("time");
                        day_time.setText(time);
                        Log.d("kkk", "Socket_принять - day_time " + time);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener onRole = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String role;
                    try {
                        role = data.getString("role");
                        Log.d("kkk", "Socket_принять - role " + role);
                        AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                        builder.setTitle("Вам выдали роль!").setMessage("Неужели вы стали доктором лёгкого поведения! Главное не волнуйтесь, просто такие как вы делятся на 2 типа... Одни из них кстати доктора...").setIcon(R.drawable.icon_mir).setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.create().show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    //запускается при падении сервера, чтобы продолжать игру
    private Emitter.Listener onRestart = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final JSONObject json2 = new JSONObject();
                    try {
                        json2.put("nick", MainActivity.NickName);
                        json2.put("room", room_num);
                        json2.put("last_message_num", num);
                        json2.put("session_id", MainActivity.Session_id);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    socket.emit("connect_to_room", json2);
                    Log.d("kkk", "CONNECT");
                }
            });
        }
    };
    ////////////////
    ////////////////
    //       SOCKETS end
    ////////////////
    ////////////////
}