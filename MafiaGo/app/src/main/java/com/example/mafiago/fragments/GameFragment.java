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
import android.widget.ImageView;
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
import java.util.Iterator;

import io.socket.emitter.Emitter;
import static  com.example.mafiago.MainActivity.socket;

public class GameFragment extends Fragment {
    public ListView listView_chat;
    public GridView gridView_users;

    public CardView cardAnswer;

    public TextView timer;
    public TextView day_time;
    public TextView answer_nick;
    public TextView answer_mes;
    public TextView room_name;

    public ImageView IV_influence_doctor;
    public ImageView IV_influence_lover;
    public ImageView IV_influence_sheriff;
    public ImageView IV_role;

    public Player player;

    public EditText sendText;

    public FloatingActionButton btnSend;

    public Button btnExit;
    public Button btnDeleteAnswer;

    ArrayList<MessageModel> list_chat = new ArrayList<>();
    ArrayList<UserModel> list_users = new ArrayList<>();

    int answer_id = -1;
    public int StopTimer = 0;

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
        room_name = view.findViewById(R.id.fragmentGame_TV_room_name);

        btnSend = view.findViewById(R.id.btnSendMes);
        btnDeleteAnswer = view.findViewById(R.id.btnDeleteAnswer);
        btnExit = view.findViewById(R.id.btnExitChat);

        timer = view.findViewById(R.id.timer);
        day_time = view.findViewById(R.id.day_time);

        IV_influence_doctor = view.findViewById(R.id.IV_influence_doctor);
        IV_influence_lover = view.findViewById(R.id.IV_influence_lover);
        IV_influence_sheriff = view.findViewById(R.id.IV_influence_sheriff);
        IV_role = view.findViewById(R.id.IV_role);

        IV_influence_doctor.setVisibility(View.GONE);
        IV_influence_lover.setVisibility(View.GONE);
        IV_influence_sheriff.setVisibility(View.GONE);

        player = new Player(MainActivity.NickName, MainActivity.Session_id, MainActivity.Game_id);

        cardAnswer.setVisibility(View.GONE);
        room_name.setText(MainActivity.RoomName);

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
                if (player.getTime().equals("lobby")) {
                    final JSONObject json2 = new JSONObject();
                    try {
                        json2.put("nick", MainActivity.NickName);
                        json2.put("session_id", MainActivity.Session_id);
                        json2.put("room", player.getRoom_num());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.d("kkk", "Socket_отправка_leave_user - " + json2.toString());
                    socket.emit("leave_room", json2);
                }
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
                                case "doctor":
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
                            Voting(nick);
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
            socket.on("leave_room", onLeaveUser);
            socket.on("timer", onTimer);
            socket.on("time", onTime);
            socket.on("role", onRole);
            socket.on("restart", onRestart);
            socket.on("role_action", onRoleAction);
            socket.on("know_role", onKnowRole);
            socket.on("system_message", onSystemMessage);
            socket.on("user_error", onUserError);
            socket.on("mafias", onMafias);
            socket.on("get_my_game_info", onGetMyGameInfo);
            socket.on("success_get_in_room", onSuccessGetInRoom);
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
                    int test_num;
                    try {
                        test_num = data.getInt("num");
                        nick = data.getString("nick");
                        time = data.getString("time");
                        MessageModel messageModel = new MessageModel(test_num, "", time.substring(11,16), nick, "DisconnectMes");

                        if (test_num > num)
                        {
                            num = test_num;
                            list_chat.add(messageModel);
                        }
                        else
                        {
                            for (int i = 0; i < list_chat.size(); i++)
                            {
                                if (test_num > list_chat.get(i).num)
                                {
                                    list_chat.add(i, messageModel);
                                }
                            }
                        }

                        Log.d("kkk", num + "  onLeaveUser2  " + test_num);


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
                    int test_num;
                    int link;
                    try {
                        nick = data.getString("nick");
                        test_num = data.getInt("num");
                        Log.d("kkk", "Длина listchat = " + list_chat.size() + " /  testnum = " + test_num + " / num = " + num);
                        if (test_num >= num)
                        {
                            num = test_num;
                            time = data.getString("time");
                            message = data.getString("message");
                            status = data.getString("status");
                            link = data.getInt("link");
                            if(link == -1)
                            {
                                Log.d("kkk", "UsersMes + " + nick + " - " + message);
                                MessageModel messageModel = new MessageModel(test_num, message, time.substring(11,16), nick, "UsersMes", status);
                                list_chat.add(messageModel);
                                MessageAdapter messageAdapter = new MessageAdapter(list_chat, getContext());
                                listView_chat.setAdapter(messageAdapter);
                                listView_chat.setSelection(messageAdapter.getCount() - 1);
                            }
                            else
                            {
                                Log.d("kkk", "AnswerMes ; " + " ; link = " + link);
                                MessageModel messageModel = new MessageModel(test_num, message, time.substring(11,16), nick, "AnswerMes", list_chat.get(link).answerNick, list_chat.get(link).message, list_chat.get(link).answerTime, link);
                                list_chat.add(messageModel);
                                MessageAdapter messageAdapter = new MessageAdapter(list_chat, getContext());
                                listView_chat.setAdapter(messageAdapter);
                                listView_chat.setSelection(messageAdapter.getCount() - 1);
                            }
                        }
                        else
                        {
                            time = data.getString("time");
                            message = data.getString("message");
                            status = data.getString("status");
                            link = data.getInt("link");
                            if(link == -1)
                            {
                                Log.d("kkk", "UsersMes + " + nick + " - " + message);
                                MessageModel messageModel = new MessageModel(test_num, message, time.substring(11,16), nick, "UsersMes", status);
                                for (int i = 0; i < list_chat.size(); i++)
                                {
                                    Log.d("kkk", "i = " + i + " ; test_num = " + test_num + " ; list_chat.get(i).num = " + list_chat.get(i).num + " ; длина списка " + list_chat.size());
                                    if (test_num < list_chat.get(i).num)
                                    {
                                        Log.d("kkk", "GOOD " + i);
                                        list_chat.add(i, messageModel);
                                        break;
                                    }
                                }
                                MessageAdapter messageAdapter = new MessageAdapter(list_chat, getContext());
                                listView_chat.setAdapter(messageAdapter);
                                listView_chat.setSelection(messageAdapter.getCount() - 1);
                            }
                            else
                            {
                                Log.d("kkk", "AnswerMes");
                                MessageModel messageModel = new MessageModel(test_num, message, time.substring(11,16), nick, "AnswerMes", list_chat.get(link).answerNick, list_chat.get(link).message, list_chat.get(link).answerTime, link);
                                for (int i = 0; i < list_chat.size(); i++)
                                {
                                    if (test_num > list_chat.get(i).num)
                                    {
                                        list_chat.add(i, messageModel);
                                    }
                                }
                                MessageAdapter messageAdapter = new MessageAdapter(list_chat, getContext());
                                listView_chat.setAdapter(messageAdapter);
                                listView_chat.setSelection(messageAdapter.getCount() - 1);
                            }
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
                        final JSONObject json2 = new JSONObject();
                        try {
                            json2.put("nick", player.getNick());
                            json2.put("room", player.getRoom_num());
                            json2.put("last_message_num", num);
                            json2.put("last_dead_message_num", -1);
                            json2.put("session_id", player.getSession_id());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        socket.emit("connect_to_room", json2);
                        Log.d("kkk", "CONNECT");
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
                    Log.d("kkk", "Принял - get_in_room: " + args[0]);
                    JSONObject data = (JSONObject) args[0];
                    String nick;
                    String time;
                    int test_num;
                    try {
                        test_num = data.getInt("num");
                        nick = data.getString("nick");
                        Log.d("kkk", "Длина listchat " + list_chat.size());
                        if (test_num > num)
                        {
                            num = test_num;
                            time = data.getString("time");
                            Log.d("kkk", test_num + "  onGetInRoom1  " + num);

                            MessageModel messageModel = new MessageModel(test_num, "", time.substring(11,16), nick, "ConnectMes");
                            list_chat.add(messageModel);
                        }
                        else
                        {
                            time = data.getString("time");
                            Log.d("kkk", test_num + "  onGetInRoom2  " + num);

                            MessageModel messageModel = new MessageModel(test_num, "", time.substring(11,16), nick, "ConnectMes");
                            for (int i = 0; i < list_chat.size(); i++)
                            {
                                if (test_num > list_chat.get(i).num)
                                {
                                    list_chat.add(i, messageModel);
                                }
                            }
                        }
                        MessageAdapter messageAdapter = new MessageAdapter(list_chat, getContext());
                        listView_chat.setAdapter(messageAdapter);

                        list_users.add(new UserModel(nick, "none"));
                        PlayersAdapter playersAdapter = new PlayersAdapter(list_users, getContext());
                        gridView_users.setAdapter(playersAdapter);

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
                        if (!time.equals("stop"))
                        {
                            //TODO: починить таймер
                            if (StopTimer == 1)
                            {
                                timer.setText("--");
                                StopTimer = 0;
                            }
                            timer.setText(time);
                        }
                        else {
                            StopTimer = 1;
                            timer.setText("--");
                        }
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
                                IV_influence_lover.setVisibility(View.GONE);
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
                                    case "doctor":
                                        StartAnimation("doctor");
                                        break;
                                    case "mafia":
                                        StartAnimation("mafia");
                                        break;
                                    case "mafia_don":
                                        StartAnimation("mafia");
                                        break;
                                    default:
                                        Log.d("kkk", "В " + player.getTime() + " - нельзя активировать роль " + player.getRole());
                                }
                                break;
                            case "day":
                                IV_influence_doctor.setVisibility(View.GONE);
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
                        player.setRole(role);
                        switch (player.getRole())
                        {
                            case "none":
                                IV_role.setImageResource(R.drawable.anonim);
                                break;
                            case "citizen":
                                IV_role.setImageResource(R.drawable.citizen_alive);
                                break;
                            case "mafia":
                                IV_role.setImageResource(R.drawable.mafia_alive);
                                break;
                            case "sheriff":
                                IV_role.setImageResource(R.drawable.sheriff_alive);
                                break;
                            case "doctor":
                                IV_role.setImageResource(R.drawable.doctor_alive);
                                break;
                            case "lover":
                                IV_role.setImageResource(R.drawable.ic_lover);
                                break;
                        }
                        Log.d("kkk", "Socket_принять - role " + role);
                        AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                        builder.setTitle("Вам выдали роль!").setMessage("Ваша роль - " + role).setIcon(R.drawable.icon_mir).setPositiveButton("ОК", new DialogInterface.OnClickListener() {
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
                        switch (role)
                        {
                            case "doctor":
                                IV_influence_doctor.setVisibility(View.VISIBLE);
                                break;
                            case "lover":
                                IV_influence_lover.setVisibility(View.VISIBLE);
                                break;
                            case "sheriff":
                                IV_influence_sheriff.setVisibility(View.VISIBLE);
                                break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener onKnowRole = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String role, nick;
                    try {
                        Log.d("kkk", "Socket_принять - know_role " + args[0]);
                        role = data.getString("role");
                        nick = data.getString("nick");
                        for (int i = 0; i < list_users.size(); i++)
                        {
                            if (list_users.get(i).getNick().equals(nick))
                            {
                                list_users.get(i).setRole(role);
                            }
                        }
                        PlayersAdapter playersAdapter = new PlayersAdapter(list_users, getContext());
                        gridView_users.setAdapter(playersAdapter);
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
                    String message, time, status, mafia_nick, user_nick, voter, nick, role;
                    int test_num;
                    JSONObject data2;

                    try {
                        status = data.getString("status");
                        test_num = data.getInt("num");
                        time = data.getString("time");
                        message = data.getString("message");
                        MessageModel messageModel = new MessageModel(test_num, "Ошибка вывода сообщения", time.substring(11, 16), "Server", "SystemMes");;
                        MessageAdapter messageAdapter;
                        switch (status)
                        {
                            case "game_over":
                                messageModel = new MessageModel(test_num, message, time.substring(11, 16), "Server", "SystemMes");
                                break;
                            case "dead_user":
                                Log.d("kkk", "1");
                                data2 = data.getJSONObject("message");
                                message = data2.getString("message");
                                nick = data2.getString("nick");
                                role = data2.getString("role");
                                for (int i = 0; i < list_users.size(); i++)
                                {
                                    if (list_users.get(i).getNick().equals(nick))
                                    {
                                        list_users.get(i).setRole(role);
                                        list_users.get(i).setAlive(false);
                                    }
                                }
                                PlayersAdapter playersAdapter = new PlayersAdapter(list_users, getContext());
                                gridView_users.setAdapter(playersAdapter);
                                messageModel = new MessageModel(test_num, message, time.substring(11, 16), "Server", "SystemMes");
                                break;
                            case "role_action_mafia":
                                data2 = data.getJSONObject("message");
                                mafia_nick = data2.getString("mafia_nick");
                                user_nick = data2.getString("user_nick");
                                messageModel = new MessageModel(test_num, "Голосует за " + user_nick, time.substring(11,16), mafia_nick, "VotingMes");
                                break;
                            case "voting":
                                Log.d("kkk", message);
                                data2 = data.getJSONObject("message");
                                voter = data2.getString("voter");
                                user_nick = data2.getString("user_nick");
                                messageModel = new MessageModel(test_num,"Голосует за " + user_nick, time.substring(11,16), voter, "VotingMes");
                                break;
                        }

                        Log.d("kkk", "Длина listchat = " + list_chat.size() + " /  testnum = " + test_num + " / num = " + num);
                        //если num из data больше нашего num, то просто вставляем сообщение в список на 1 место, else вставляем сообщение на нужное место
                        if (test_num >= num) {
                            num = test_num;
                            list_chat.add(messageModel);
                        }
                        else
                        {
                            for (int i = 0; i < list_chat.size(); i++)
                            {
                                if (test_num > list_chat.get(i).num)
                                {
                                    list_chat.add(i, messageModel);
                                }
                            }
                        }
                        messageAdapter = new MessageAdapter(list_chat, getContext());
                        listView_chat.setAdapter(messageAdapter);
                        listView_chat.setSelection(messageAdapter.getCount() - 1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    Log.d("kkk", "system message - " + data);
                }
            });
        }
    };

    private final Emitter.Listener onUserError = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if (getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String error;
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    try {
                        error = data.getString("error");
                        AlertDialog alert;
                        switch (error) {
                            case "game_has_been_started":
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

                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
                                break;
                            case "max_people_in_room":
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
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
                                break;
                            case "you_are_playing_in_other_room":
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
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
                                break;
                            case "game_is_over":
                                builder.setTitle("Извините, но игра закончена")
                                        .setMessage("")
                                        .setIcon(R.drawable.ic_error)
                                        .setCancelable(false)
                                        .setNegativeButton("Ок",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.cancel();
                                                    }
                                                });
                                break;
                        }
                        alert = builder.create();
                        alert.show();
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
                    JSONObject data2;
                    String nick, role;
                    try {
                        data2 = data.getJSONObject("mafias");
                        for (Iterator iterator = data2.keys(); iterator.hasNext();)
                        {
                            nick = (String) iterator.next();
                            role = data2.getString(nick);
                            for (int i = 0; i < list_users.size(); i++)
                            {
                                if (list_users.get(i).getNick().equals(nick))
                                {
                                    list_users.get(i).setRole(role);
                                }
                            }
                            PlayersAdapter playersAdapter = new PlayersAdapter(list_users, getContext());
                            gridView_users.setAdapter(playersAdapter);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.d("kkk", "Socket_принять - mafias - " + args[0]);
                }
            });
        }
    };  

    private Emitter.Listener onGetMyGameInfo = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String role, status, influences, time;
                    boolean can_act, can_vote, last_message;
                    try {
                        role = data.getString("role");
                        status = data.getString("status");
                        time = data.getString("time");
                        can_vote = data.getBoolean("can_vote");
                        can_act = data.getBoolean("can_act");
                        day_time.setText(time);
                        player.setTime(time);
                        player.setRole(role);
                        player.setStatus(status);
                        if (player.getStatus().equals("alive"))
                        {
                            switch (player.getRole())
                            {
                                case "none":
                                    IV_role.setImageResource(R.drawable.anonim);
                                    break;
                                case "citizen":
                                    IV_role.setImageResource(R.drawable.citizen_alive);
                                    break;
                                case "mafia":
                                    IV_role.setImageResource(R.drawable.mafia_alive);
                                    break;
                                case "sheriff":
                                    IV_role.setImageResource(R.drawable.sheriff_alive);
                                    break;
                                case "doctor":
                                    IV_role.setImageResource(R.drawable.doctor_alive);
                                    break;
                                case "lover":
                                    IV_role.setImageResource(R.drawable.ic_lover);
                                    break;
                            }

                            player.setCan_write(false);
                            switch (player.getTime())
                            {
                                case "night_love":
                                    IV_influence_lover.setVisibility(View.GONE);
                                    switch (player.getRole())
                                    {
                                        case "mafia":
                                            player.setCan_write(true);
                                            break;
                                        case "mafia_don":
                                            player.setCan_write(true);
                                            break;
                                        case "lover":
                                            if (can_act) StartAnimation(role);
                                    }
                                    break;
                                case "night_other":
                                    switch (player.getRole())
                                    {
                                        case "mafia":
                                        case "mafia_don":
                                        case "doctor":
                                        case "sheriff":
                                            if (can_act) StartAnimation(role);
                                            break;
                                    }
                                    break;
                                case "day":
                                    IV_influence_doctor.setVisibility(View.GONE);
                                    player.setVoted_at_night(false);
                                    player.setCan_write(true);
                                    break;
                                case "voting":
                                    if (can_vote) StartAnimation("voting");
                                    break;
                            }
                        }
                        else
                        {
                            switch (player.getRole())
                            {
                                case "none":
                                    IV_role.setImageResource(R.drawable.anonim);
                                    break;
                                case "citizen":
                                    IV_role.setImageResource(R.drawable.citizen_dead);
                                    break;
                                case "mafia":
                                    IV_role.setImageResource(R.drawable.mafia_dead);
                                    break;
                                case "sheriff":
                                    IV_role.setImageResource(R.drawable.sheriff_dead);
                                    break;
                                case "doctor":
                                    IV_role.setImageResource(R.drawable.doctor_dead);
                                    break;
                                case "lover":
                                    IV_role.setImageResource(R.drawable.ic_lover);
                                    break;
                            }

                            player.setCan_write(true);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.d("kkk", "Socket_принять - get_my_game_info - " + args[0]);
                }
            });
        }
    };

    private Emitter.Listener onSuccessGetInRoom = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    final JSONObject json = new JSONObject();
                    try {
                        json.put("nick", player.getNick());
                        json.put("room", player.getRoom_num());
                        json.put("last_message_num", num);
                        json.put("last_dead_message_num", -1);
                        json.put("session_id", player.getSession_id());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    socket.emit("connect_to_room", json);
                    Log.d("kkk", "connect_to_room - " + json);
                }
            });
        }
    };

    /*******************************
     *                             *
     *       SOCKETS end           *
     *                             *
     *******************************/

    //запуск анимации
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
    //конец анимации
    public void StopAnimation() {
        player.setCan_click(false);
        for (int i = 0; i < list_users.size(); i++)
        {
            list_users.get(i).setAnimation(false);
            PlayersAdapter playersAdapter = new PlayersAdapter(list_users, getContext());
            gridView_users.setAdapter(playersAdapter);
        }
    }
    //действие роли
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
    //дневное голосование
    public void Voting(String nick) {
        final JSONObject json3 = new JSONObject();
        try {
            json3.put("nick", player.getNick());
            json3.put("session_id", player.getSession_id());
            json3.put("room", player.getRoom_num());
            json3.put("influence_on_nick", nick);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("voting", json3);
        Log.d("kkk", "Socket_отправка - voting"+ json3.toString());
    }
}