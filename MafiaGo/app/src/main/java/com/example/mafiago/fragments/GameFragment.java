package com.example.mafiago.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.widget.AdapterView;
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
import com.example.mafiago.models.MessageModel;
import com.example.mafiago.models.Player;
import com.example.mafiago.models.UserModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.emitter.Emitter;
import static  com.example.mafiago.MainActivity.socket;

public class GameFragment extends Fragment {
    public ListView listView_chat;
    public GridView gridView_users;

    public CardView cardAnswer;

    public TextView timer;
    public TextView day_time;
    public TextView influence;
    public TextView answer_nick;
    public TextView answer_mes;

    public Player player;

    public EditText sendText;

    public FloatingActionButton btnSend;

    public Button btnExit;
    public Button btnDeleteAnswer;

    ArrayList<MessageModel> list_chat = new ArrayList<>();
    ArrayList<UserModel> list_users = new ArrayList<>();

    int answer_id = -1;
    public boolean Not_First = false;

    int num = -1;

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
        sendText = view.findViewById(R.id.InputMes);
        influence = view.findViewById(R.id.influence);

        btnSend = view.findViewById(R.id.btnSendMes);
        btnDeleteAnswer = view.findViewById(R.id.btnDeleteAnswer);
        btnExit = view.findViewById(R.id.btnExitChat);

        timer = view.findViewById(R.id.timer);
        day_time = view.findViewById(R.id.day_time);

        player = new Player(MainActivity.NickName, MainActivity.Session_id, MainActivity.Game_id);

        cardAnswer.setVisibility(View.GONE);
        influence.setVisibility(View.GONE);

        SocketTask socketTask = new SocketTask();
        socketTask.execute();


        final JSONObject json3 = new JSONObject();
        try {
            json3.put("nick", player.getNick());
            json3.put("session_id", player.getSession_id());
            json3.put("room", player.getRoom_num());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("get_in_room", json3);
        Log.d("kkk", "Socket_отправка - get_in_room"+ json3.toString());

        final JSONObject json = new JSONObject();
        try {
            json.put("nick", player.getNick());
            json.put("room", player.getRoom_num());
            json.put("last_message_num", num);
            json.put("session_id", player.getSession_id());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("connect_to_room", json);
        Log.d("kkk", "connect_to_room - " + json);


        //StartAnimation("mafia");
        //StopAnimation();


        player.setTime("night_love");
        player.setRole("lover");
        player.setCan_click(true);


        btnSend.setOnClickListener(v -> {

            final Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.bounce);

            // amplitude 0.2 and frequency 20
            BounceInterpolator interpolator = new BounceInterpolator();
            animation.setInterpolator(interpolator);

            btnSend.startAnimation(animation);

            if (player.Can_write()) {
                final JSONObject json2 = new JSONObject();
                try {
                    json2.put("nick", player.getNick());
                    json2.put("session_id", player.getSession_id());
                    json2.put("room", player.getRoom_num());
                    json2.put("message", sendText.getText().toString());
                    json2.put("link", answer_id);
                    answer_id = -1;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("kkk", "Socket_отправка user_message - " + json2.toString());
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
                sendText.setText("");
            }
            else
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Вы не можете сейчас отправлять сообщения!")
                        .setMessage("")
                        .setIcon(R.drawable.ic_error)
                        .setCancelable(false)
                        .setNegativeButton("Ок",
                                (dialog, id) -> dialog.cancel());
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final JSONObject json2 = new JSONObject();
                try {
                    json2.put("nick", MainActivity.NickName);
                    json2.put("session_id",  MainActivity.Session_id);
                    json2.put("room", player.getRoom_num());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("kkk", "Socket_отправка_leave_user - "+ json2.toString());
                socket.emit("leave_room", json2);
                sendText.setText("");
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
            }
        });

        gridView_users.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String nick = list_users.get(position).getNick();
                Log.d("kkk", "Нажатие на ник: " + list_users.get(position).getNick());
                if (player.Can_click())
                {
                    switch (player.getTime())
                    {
                        case "lobby":
                            sendText.setText(sendText.getText() + nick);
                            break;
                        case "night_love":
                            switch (player.getRole())
                            {
                                case "lover":
                                    RoleAction(nick);
                                    break;
                                case "doctor_of_easy_virtue":
                                    player.setVoted_at_night(true);
                                    RoleAction(nick);
                                    break;
                                default:
                                    Log.d("kkk", "В " + player.getTime() + " - нельзя активировать роль " + player.getRole());
                            }
                            break;
                        case "night_other":
                            switch (player.getRole())
                            {
                                case "sheriff":
                                    RoleAction(nick);
                                    break;
                                case "doktor":
                                    RoleAction(nick);
                                    break;
                                case "doctor_of_easy_virtue":
                                    RoleAction(nick);
                                    break;
                                case "mafia":
                                    RoleAction(nick);
                                    break;
                                default:
                                    Log.d("kkk", "В " + player.getTime() + " - нельзя активировать роль " + player.getRole());
                            }
                            break;
                        case "day":
                            break;
                        case "voting":
                            break;
                        default:
                            Log.d("kkk", "Что-то пошло не так. Такого времени дня не может быть!");
                    }
                    StopAnimation();
                }
                else
                {
                    sendText.setText(sendText.getText() + nick);
                }

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
            socket.on("role_action", onRoleAction);
            socket.on("role_action_mafia", onRoleActionMafia);
            socket.on("role_action_sheriff", onRoleActionSheriff);
            socket.on("system_message", onSystemMessage);
            socket.on("user_error", onUserError);
            socket.on("mafias", onMafias);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d("kkk", "onPostExecute");
        }
    }

    /*******************************
     *                             *
     *       SOCKETS start         *
     *                             *
     *******************************/

    private Emitter.Listener onConnectToRoom = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // TODO: доделать событие OnConnectToRoom
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

                        for (int i=list_users.size()-1; i>=0; i--)
                        {
                            if (list_users.get(i).getNick().equals(nick))
                            {
                                Log.d("kkk", "remove" + list_users.get(i).getNick());
                                list_users.remove(i);
                            }
                        }
                        for (int i=list_users.size()-1; i>=0; i--)
                        {
                            Log.d("kkk", list_users.get(i).getNick());
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
                                list_chat.add(num, messageModel);
                                MessageAdapter messageAdapter = new MessageAdapter(list_chat, getContext());
                                listView_chat.setAdapter(messageAdapter);
                                listView_chat.setSelection(messageAdapter.getCount() - 1);
                            }
                            else
                            {
                                Log.d("kkk", "AnswerMes");
                                MessageModel messageModel = new MessageModel(message, time.substring(11,16), nick, "AnswerMes", list_chat.get(link).answerNick, list_chat.get(link).message, list_chat.get(link).answerTime, link);
                                list_chat.add(num, messageModel);
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
                            json2.put("nick", player.getNick());
                            json2.put("room", player.getRoom_num());
                            json2.put("last_message_num", num);
                            json2.put("session_id", player.getSession_id());
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

                            list_users.add(new UserModel(nick));
                            PlayersAdapter playersAdapter = new PlayersAdapter(list_users, getContext());
                            gridView_users.setAdapter(playersAdapter);
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
                        player.setTime(time);
                        day_time.setText(time);
                        Log.d("kkk", "Socket_принять - day_time " + time);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (player.getStatus().equals("alive"))
                    {
                        player.setCan_write(false);
                        StopAnimation();
                        switch (player.getTime())
                        {
                            case "night_love":
                                switch (player.getRole())
                                {
                                    case "lover":
                                        StartAnimation("lover");
                                        break;
                                    case "doctor_of_easy_virtue":
                                        StartAnimation("lover");
                                        break;
                                    case "mafia":
                                        player.setCan_write(true);
                                        break;
                                    case "mafia_don":
                                        player.setCan_write(true);
                                        break;
                                    default:
                                        Log.d("kkk", "В " + player.getTime() + " - нельзя активировать роль " + player.getRole());
                                }
                                break;
                            case "night_other":
                                switch (player.getRole())
                                {
                                    case "doctor_of_easy_virtue":
                                        if (!player.getVoted_at_night()) {
                                            StartAnimation("doctor");
                                        }
                                        break;
                                    case "sheriff":
                                        StartAnimation("sheriff");
                                        break;
                                    case "doktor":
                                        StartAnimation("doctor");
                                        break;
                                    case "mafia":
                                        StartAnimation("mafia");
                                        break;
                                    case "mafi_don":
                                        StartAnimation("mafia");
                                        break;
                                    default:
                                        Log.d("kkk", "В " + player.getTime() + " - нельзя активировать роль " + player.getRole());
                                }
                                break;
                            case "day":
                                player.setVoted_at_night(false);
                                player.setCan_write(true);
                                break;
                            case "voting":
                                StartAnimation("voting");
                                break;
                        }
                    }
                    else
                    {
                        Log.d("kkk", "Вы мертвы :)");
                    }
                }
            });
        }
    };

    //принимает роль
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
                        json2.put("nick", player.getNick());
                        json2.put("room", player.getRoom_num());
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

    private Emitter.Listener onRoleAction = new Emitter.Listener() {
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
                        Log.d("kkk", "Socket_принять - role_action " + args[0]);
                        influence.setText(role);
                        influence.setVisibility(View.VISIBLE);
                        MessageModel messageModel = new MessageModel("На вас походил " + role, "09-55", "System", "UsersMes");
                        list_chat.add(messageModel);
                        MessageAdapter messageAdapter = new MessageAdapter(list_chat, getContext());
                        listView_chat.setAdapter(messageAdapter);
                        listView_chat.setSelection(messageAdapter.getCount() - 1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener onRoleActionMafia = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String mafia_nick;
                    String user_nick;
                    try {
                        mafia_nick = data.getString("mafia_nick");
                        user_nick = data.getString("user_nick");
                        Log.d("kkk", "Socket_принять - role_action_mafia " + args[0]);
                        MessageModel messageModel = new MessageModel(mafia_nick + "проголосовал за " + user_nick, "09-55", "System", "UsersMes");
                        list_chat.add(messageModel);
                        MessageAdapter messageAdapter = new MessageAdapter(list_chat, getContext());
                        listView_chat.setAdapter(messageAdapter);
                        listView_chat.setSelection(messageAdapter.getCount() - 1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener onRoleActionSheriff = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String sheriff_role;
                    try {
                        sheriff_role = data.getString("role");
                        Log.d("kkk", "Socket_принять - role_action_sheriff " + args[0]);
                        MessageModel messageModel = new MessageModel("шериф проверил " + sheriff_role, "09-55", "System", "UsersMes");
                        list_chat.add(messageModel);
                        MessageAdapter messageAdapter = new MessageAdapter(list_chat, getContext());
                        listView_chat.setAdapter(messageAdapter);
                        listView_chat.setSelection(messageAdapter.getCount() - 1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener onSystemMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.d("kkk", "system message - " + String.valueOf(data));
                }
            });
        }
    };

    private Emitter.Listener onUserError = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String error;
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    AlertDialog alert;
                    try {
                        error = data.getString("error");
                        switch (error)
                        {
                            case "game_has_been_started":
                                builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Извините, но игра уже началась")
                                        .setMessage("")
                                        .setIcon(R.drawable.ic_error)
                                        .setCancelable(false)
                                        .setNegativeButton("Ок",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.cancel();
                                                    }
                                                });
                                alert = builder.create();
                                alert.show();
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
                            case "max_people_in_room":
                                builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Извините, но в комнате нет мест")
                                        .setMessage("")
                                        .setIcon(R.drawable.ic_error)
                                        .setCancelable(false)
                                        .setNegativeButton("Ок",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.cancel();
                                                    }
                                                });
                                alert = builder.create();
                                alert.show();
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
                            case "you_are_playing_in_other_room":
                                builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Извините, но вы играете в другой игре")
                                        .setMessage("")
                                        .setIcon(R.drawable.ic_error)
                                        .setCancelable(false)
                                        .setNegativeButton("Ок",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.cancel();
                                                    }
                                                });
                                alert = builder.create();
                                alert.show();
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
                            case "game_is_over":
                                builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Извините, но вы играете в другой игре")
                                        .setMessage("")
                                        .setIcon(R.drawable.ic_error)
                                        .setCancelable(false)
                                        .setNegativeButton("Ок",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.cancel();
                                                    }
                                                });
                                alert = builder.create();
                                alert.show();
                        }
                        Log.d("kkk", "Socket_принять - user_error " + args[0]);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener onMafias = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    //JSONArray
                    // TODO: доделать событие OnMafias
                    Log.d("kkk", "Socket_принять - mafias - " + args[0]);
                }
            });
        }
    };

    /*******************************
     *                             *
     *       SOCKETS end           *
     *                             *
     *******************************/

    public void StartAnimation(String type) {
        player.setCan_click(true);
        for (int i = 0; i < list_users.size(); i++)
        {
            list_users.get(i).setAnimation(true);
            list_users.get(i).setAnimation_type(type);
            PlayersAdapter playersAdapter = new PlayersAdapter(list_users, getContext());
            gridView_users.setAdapter(playersAdapter);
        }
    }
    public void StopAnimation() {
        player.setCan_click(false);
        for (int i = 0; i < list_users.size(); i++)
        {
            list_users.get(i).setAnimation(false);
            PlayersAdapter playersAdapter = new PlayersAdapter(list_users, getContext());
            gridView_users.setAdapter(playersAdapter);
        }
    }
    public void RoleAction(String nick) {
        final JSONObject json3 = new JSONObject();
        try {
            json3.put("nick", player.getNick());
            json3.put("session_id", player.getSession_id());
            json3.put("room", player.getRoom_num());
            json3.put("influence_on_nick", nick);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("role_action", json3);
        Log.d("kkk", "Socket_отправка - role_action"+ json3.toString());
    }
}