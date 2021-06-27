package com.mafiago.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.mafiago.MainActivity;
import com.example.mafiago.R;
import com.mafiago.adapters.MessageAdapter;
import com.mafiago.adapters.PlayersAdapter;
import com.mafiago.classes.OnBackPressedListener;
import com.mafiago.enums.Role;
import com.mafiago.enums.Time;
import com.mafiago.models.MessageModel;
import com.mafiago.models.Player;
import com.mafiago.models.UserModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import io.socket.emitter.Emitter;
import static  com.mafiago.MainActivity.socket;

public class GameFragment extends Fragment implements OnBackPressedListener {
    public ListView listView_chat;
    public GridView gridView_users;

    public CardView cardAnswer;

    public TextView timer;
    public TextView day_time;
    public TextView answer_nick;
    public TextView answer_mes;
    public TextView room_name;
    public TextView voting_number;

    public ImageView IV_influence_doctor;
    public ImageView IV_influence_lover;
    public ImageView IV_influence_sheriff;
    public ImageView IV_influence_bodyguard;
    public ImageView IV_influence_poisoner;
    public ImageView IV_role;

    public Player player;

    public EditText sendText;

    public ImageView btnSend;
    public FloatingActionButton FAB_skip_day;

    public Button btnExit;
    public Button btnDeleteAnswer;

    ArrayList<MessageModel> list_chat = new ArrayList<>();
    ArrayList<UserModel> list_users = new ArrayList<>();

    int answer_id = -1;
    public int StopTimer = 0;
    int messages_can_write = 10;
    public String journalist_check = null;

    int num = -1;

    MessageAdapter messageAdapter;

    public int FirstVisibleItem = 0, VisibleItemsCount = 0,TotalItemsCount = 0;

    public static final String APP_PREFERENCES = "user";
    public static final String APP_PREFERENCES_LAST_ROLE = "role";

    private SharedPreferences mSettings;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_game, container, false);

        mSettings = getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        listView_chat = view.findViewById(R.id.ListMes);
        gridView_users = view.findViewById(R.id.ListUsers);
        cardAnswer = view.findViewById(R.id.answerCard);
        answer_nick = view.findViewById(R.id.answerNickChat);
        answer_mes = view.findViewById(R.id.answerTextChat);
        sendText = view.findViewById(R.id.InputMes);
        room_name = view.findViewById(R.id.fragmentGame_TV_room_name);
        voting_number = view.findViewById(R.id.fragmentGame_voting_number);

        btnSend = view.findViewById(R.id.btnSendMes);
        btnDeleteAnswer = view.findViewById(R.id.btnDeleteAnswer);
        btnExit = view.findViewById(R.id.btnExitChat);

        FAB_skip_day = view.findViewById(R.id.fragmentGame_FAB_skip_day);

        timer = view.findViewById(R.id.timer);
        day_time = view.findViewById(R.id.day_time);

        IV_influence_doctor = view.findViewById(R.id.IV_influence_doctor);
        IV_influence_lover = view.findViewById(R.id.IV_influence_lover);
        IV_influence_sheriff = view.findViewById(R.id.IV_influence_sheriff);
        IV_influence_bodyguard = view.findViewById(R.id.IV_influence_bodyguard);
        IV_influence_poisoner = view.findViewById(R.id.IV_influence_poisoner);
        IV_role = view.findViewById(R.id.IV_role);

        IV_influence_doctor.setVisibility(View.GONE);
        IV_influence_lover.setVisibility(View.GONE);
        IV_influence_sheriff.setVisibility(View.GONE);
        IV_influence_bodyguard.setVisibility(View.GONE);
        IV_influence_poisoner.setVisibility(View.GONE);

        voting_number.setVisibility(View.GONE);

        FAB_skip_day.setVisibility(View.GONE);

        player = new Player(MainActivity.NickName, MainActivity.Session_id, MainActivity.Game_id, MainActivity.Role);

        cardAnswer.setVisibility(View.GONE);

        room_name.setText(MainActivity.RoomName);

        messageAdapter = new MessageAdapter(list_chat, getContext());
        listView_chat.setAdapter(messageAdapter);

        socket.off("connect");
        socket.off("disconnect");
        socket.off("get_in_room");
        socket.off("user_message");
        socket.off("leave_room");
        socket.off("timer");
        socket.off("time");
        socket.off("role");
        socket.off("restart");
        socket.off("role_action");
        socket.off("know_role");
        socket.off("system_message");
        socket.off("user_error");
        socket.off("mafias");
        socket.off("get_my_game_info");
        socket.off("success_get_in_room");
        socket.off("get_profile");
        socket.off("host_info");
        socket.off("ban_user_in_room");
        socket.off("user_message_delay");

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
        socket.on("get_profile", OnGetProfile);
        socket.on("host_info", OnHostInfo);
        socket.on("ban_user_in_room", OnBanUserInRoom);
        socket.on("user_message_delay", OnUserMessageDelay);

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
            Log.d("kkk", player.getStatus());
            Log.d("kkk", String.valueOf(player.getStatus().equals("alive")));
            if (sendText.length() < 300) {
                if (player.getStatus().equals("alive")) {
                    if (player.Can_write()) {
                        if (!sendText.getText().toString().equals("")) {
                            if (player.getTime() == Time.DAY) {
                                if (IV_influence_poisoner.getVisibility() != View.VISIBLE) {
                                    if (messages_can_write > 0) {
                                        messages_can_write--;
                                        final Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.bounce);

                                        // amplitude 0.2 and frequency 20
                                        BounceInterpolator interpolator = new BounceInterpolator();
                                        animation.setInterpolator(interpolator);

                                        btnSend.startAnimation(animation);
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
                                    } else {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                        builder.setTitle("Вы не имеете права!")
                                                .setMessage("Нельзя отправлять больше 10 сообщений в день!")
                                                .setIcon(R.drawable.ic_error)
                                                .setCancelable(false)
                                                .setNegativeButton("ок",
                                                        (dialog, id) -> dialog.cancel());
                                        AlertDialog alert = builder.create();
                                        alert.show();
                                    }
                                } else {
                                    if (messages_can_write == 10) {
                                        messages_can_write--;
                                        final Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.bounce);

                                        // amplitude 0.2 and frequency 20
                                        BounceInterpolator interpolator = new BounceInterpolator();
                                        animation.setInterpolator(interpolator);

                                        btnSend.startAnimation(animation);
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
                                    } else {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                        builder.setTitle("Вас отравили!")
                                                .setMessage("Вы не можете писать больше 1 сообщения в день!")
                                                .setIcon(R.drawable.ic_error)
                                                .setCancelable(false)
                                                .setNegativeButton("ок",
                                                        (dialog, id) -> dialog.cancel());
                                        AlertDialog alert = builder.create();
                                        alert.show();
                                    }
                                }
                            } else {
                                final Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.bounce);

                                // amplitude 0.2 and frequency 20
                                BounceInterpolator interpolator = new BounceInterpolator();
                                animation.setInterpolator(interpolator);

                                btnSend.startAnimation(animation);
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
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("Ошибка!")
                                    .setMessage("Нельзя отправлять пустые сообщения!")
                                    .setIcon(R.drawable.ic_error)
                                    .setCancelable(false)
                                    .setNegativeButton("ок",
                                            (dialog, id) -> dialog.cancel());
                            AlertDialog alert = builder.create();
                            alert.show();

                        }
                    } else {
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
                } else {
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
            }
            else
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Ошибка!")
                        .setMessage("Вы не можете отправлять более 300 символов в одном сообщении!")
                        .setIcon(R.drawable.ic_error)
                        .setCancelable(false)
                        .setNegativeButton("Ок",
                                (dialog, id) -> dialog.cancel());
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        btnExit.setOnClickListener(v -> {
            if (player.getTime() == Time.LOBBY) {
                if (!timer.getText().equals("--")) {
                    if (Integer.parseInt(String.valueOf(timer.getText())) > 5) {
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
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
                    }
                    else
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Вы не имеете права!")
                                .setMessage("Нельзя выходить из комнаты за несколько секунд до начала игры!")
                                .setIcon(R.drawable.ic_error)
                                .setCancelable(false)
                                .setNegativeButton("Ок",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                }
                else
                {
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
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
                }
            }
            else
            {
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
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
            }
        });

        gridView_users.setOnItemClickListener((parent, view1, position, id) -> {
            String nick = list_users.get(position).getNick();
            if (player.Can_click() && player.getStatus().equals("alive"))
            {
                switch (player.getTime())
                {
                    case LOBBY:
                        sendText.setText(sendText.getText() + nick);
                        sendText.setSelection(sendText.length());
                        break;
                    case NIGHT_LOVE:
                        switch (player.getRole())
                        {
                            case LOVER:
                                RoleAction(nick);
                                break;
                            case DOCTOR_OF_EASY_VIRTUE:
                                player.setVoted_at_night(true);
                                RoleAction(nick);
                                break;
                        }
                        break;
                    case NIGHT_OTHER:
                        switch (player.getRole())
                        {
                            case SHERIFF:
                                RoleAction(nick);
                                break;
                            case DOCTOR:
                                RoleAction(nick);
                                break;
                            case DOCTOR_OF_EASY_VIRTUE:
                                RoleAction(nick);
                                break;
                            case MAFIA:
                                RoleAction(nick);
                                break;
                            case MAFIA_DON:
                                RoleAction(nick);
                                break;
                            case MANIAC:
                                RoleAction(nick);
                                break;
                            case POISONER:
                                RoleAction(nick);
                                break;
                            case JOURNALIST:
                                if (journalist_check == null)
                                {
                                    journalist_check = nick;
                                    for (int i = 0; i < list_users.size(); i++)
                                    {
                                        if (list_users.get(i).getNick().equals(nick))
                                        {
                                            list_users.get(i).setAnimation_type(Role.NONE);
                                            break;
                                        }
                                    }
                                    PlayersAdapter playersAdapter = new PlayersAdapter(list_users, getContext());
                                    gridView_users.setAdapter(playersAdapter);
                                }
                                else
                                {
                                    final JSONObject json = new JSONObject();
                                    try {
                                        json.put("nick", player.getNick());
                                        json.put("session_id", player.getSession_id());
                                        json.put("room", player.getRoom_num());
                                        json.put("influence_on_nick", journalist_check);
                                        json.put("influence_on_nick_2", nick);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    socket.emit("role_action", json);
                                    Log.d("kkk", "Socket_отправка - role_action"+ json.toString());

                                    int journalist_checks_count = 0;
                                    for (int i = 0; i < list_users.size(); i++)
                                    {
                                        if (list_users.get(i).getNick().equals(nick) || list_users.get(i).getNick().equals(journalist_check))
                                        {
                                            list_users.get(i).setChecked(true);
                                        }
                                        if (!list_users.get(i).getChecked())
                                        {
                                            journalist_checks_count++;
                                        }
                                    }
                                    if (journalist_checks_count < 2)
                                    {
                                        for (int i = 0; i < list_users.size(); i++)
                                        {
                                            list_users.get(i).setChecked(true);
                                        }
                                    }
                                    PlayersAdapter playersAdapter = new PlayersAdapter(list_users, getContext());
                                    gridView_users.setAdapter(playersAdapter);
                                    journalist_check = null;
                                }
                                break;
                            default:
                                Log.d("kkk", "В " + player.getTime() + " - нельзя активировать роль " + player.getRole());
                        }
                        break;
                    case DAY:
                        if (player.getRole() == Role.BODYGUARD) {
                            RoleAction(nick);
                        } else {
                            sendText.setText(sendText.getText() + nick);
                            sendText.setSelection(sendText.length());
                        }
                        break;
                    case VOTING:
                        if (player.getRole() == Role.TERRORIST) {
                            RoleAction(nick);
                        } else {
                            Voting(nick);
                        }
                        break;
                    default:
                        Log.d("kkk", "Что-то пошло не так. Такого времени дня не может быть!");
                }
                if (journalist_check == null)
                {
                    StopAnimation();
                }
            }
            else
            {
                sendText.setText(sendText.getText() + nick);
                sendText.setSelection(sendText.length());
            }
        });

        listView_chat.setOnItemClickListener((parent, view12, position, id) -> {
            Log.d("kkk", "----");
            Log.d("kkk", "position - " + position + " ' " + list_chat.get(position).MesType);
            Log.d("kkk", "----");
            if(list_chat.get(position).MesType.equals("UsersMes") || list_chat.get(position).MesType.equals("AnswerMes"))
            {
                answer_id = list_chat.get(position).num;
                answer_nick.setText(list_chat.get(position).nickName);
                answer_mes.setText(list_chat.get(position).message);
                cardAnswer.setVisibility(View.VISIBLE);
            }
        });

        btnDeleteAnswer.setOnClickListener(v -> {
            answer_id = -1;
            cardAnswer.setVisibility(View.GONE);
        });

        FAB_skip_day.setOnClickListener(v -> {
            FAB_skip_day.setVisibility(View.GONE);
            final JSONObject json2 = new JSONObject();
            try {
                json2.put("nick", MainActivity.NickName);
                json2.put("session_id", MainActivity.Session_id);
                json2.put("room", player.getRoom_num());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("kkk", "Socket_отправка_skip_day - " + json2.toString());
            socket.emit("skip_day", json2);
        });

        listView_chat.setOnScrollListener(new AbsListView.OnScrollListener() {
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                FirstVisibleItem = firstVisibleItem;
                VisibleItemsCount = visibleItemCount;
                TotalItemsCount = totalItemCount;
            }
        });

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

    @Override
    public void onBackPressed() {
        if (player.getTime() == Time.LOBBY) {
            if (!timer.getText().equals("--")) {
                if (Integer.parseInt(String.valueOf(timer.getText())) > 5) {
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
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Вы не имеете права!")
                            .setMessage("Нельзя выходить из комнаты за несколько секунд до начала игры!")
                            .setIcon(R.drawable.ic_error)
                            .setCancelable(false)
                            .setNegativeButton("Ок",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
            else
            {
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
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
            }
        }
        else
        {
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
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
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
                    String nick = "";
                    String time = "";
                    int test_num = -1;
                    try {
                        test_num = data.getInt("num");
                        nick = data.getString("nick");
                        time = data.getString("time");
                        Log.d("kkk", "leave_user - " + " Длина listchat = " + list_chat.size() + " /  testnum = " + test_num + " / num = " + num + "/ " + data);
                    } catch (JSONException e) {
                        return;
                    }
                    MessageModel messageModel = new MessageModel(test_num, nick + " вышел(-а) из чата", time.substring(11,16), nick, "DisconnectMes");

                    if (test_num != num) {
                        if (test_num > num) {
                            num = test_num;
                            list_chat.add(messageModel);
                        } else {
                            for (int i = 0; i < list_chat.size(); i++) {
                                if (test_num < list_chat.get(i).num) {
                                    list_chat.add(i, messageModel);
                                    break;
                                }
                            }
                        }
                        for (int i = 0; i < list_chat.size(); i++) {
                            Log.d("kkk", String.valueOf(list_chat.get(i).message.equals(nick + " вошёл(-а) в чат")));
                            Log.d("kkk", String.valueOf(list_chat.get(i).message));
                            if (list_chat.get(i).message.equals(nick + " вошёл(-а) в чат")) {
                                list_chat.remove(i);
                            }
                        }
                        for (int i=list_users.size()-1; i>=0; i--)
                        {
                            if (list_users.get(i).getNick().equals(nick))
                            {
                                list_users.remove(i);
                                break;
                            }
                        }
                        PlayersAdapter playersAdapter = new PlayersAdapter(list_users, getContext());
                        gridView_users.setAdapter(playersAdapter);

                        messageAdapter.notifyDataSetChanged();
                        if (TotalItemsCount < FirstVisibleItem + VisibleItemsCount + 3) {
                            listView_chat.setSelection(messageAdapter.getCount() - 1);
                        }
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
                    String main_role;
                    int test_num;
                    int link;
                    try {
                        main_role = data.getString("role");
                        nick = data.getString("nick");
                        test_num = data.getInt("num");
                        Log.d("kkk", "new_message - " + " Длина listchat = " + list_chat.size() + " /  testnum = " + test_num + " / num = " + num + "/ " + data);
                        if (test_num != num) {
                            if (test_num > num) {
                                num = test_num;
                                time = data.getString("time");
                                message = data.getString("message");
                                status = data.getString("status");
                                link = data.getInt("link");
                                MessageModel messageModel;
                                if (link == -1) {
                                    messageModel = new MessageModel(test_num, message, time.substring(11, 16), nick, "UsersMes", status, main_role);
                                } else {
                                    messageModel = new MessageModel(test_num, message, time.substring(11, 16), nick, "AnswerMes", status, link, main_role);
                                }
                                list_chat.add(messageModel);
                                messageAdapter.notifyDataSetChanged();
                            } else {
                                time = data.getString("time");
                                message = data.getString("message");
                                status = data.getString("status");
                                link = data.getInt("link");
                                MessageModel messageModel;
                                if (link == -1) {
                                    messageModel = new MessageModel(test_num, message, time.substring(11, 16), nick, "UsersMes", status, main_role);
                                } else {
                                    messageModel = new MessageModel(test_num, message, time.substring(11, 16), nick, "AnswerMes", status, link, main_role);
                                }
                                for (int i = 0; i < list_chat.size(); i++) {
                                    if (test_num < list_chat.get(i).num) {
                                        list_chat.add(i, messageModel);
                                        break;
                                    }
                                }
                                messageAdapter.notifyDataSetChanged();
                            }
                            if (TotalItemsCount < FirstVisibleItem + VisibleItemsCount + 3) {
                                listView_chat.setSelection(messageAdapter.getCount() - 1);
                            }
                        }
                        else
                        {
                            Log.d("kkk", "Сообщение забанено!");
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
                        time = data.getString("time");
                        nick = data.getString("nick");
                        MessageModel messageModel = new MessageModel(test_num, nick + " вошёл(-а) в чат", time.substring(11, 16), nick, "ConnectMes");
                        Log.d("kkk", "get_in_room - " + " Длина listchat = " + list_chat.size() + " /  testnum = " + test_num + " / num = " + num + "/ " + data);
                        if (test_num != num) {
                            if (test_num > num) {
                                num = test_num;
                                list_chat.add(messageModel);
                            } else {
                                for (int i = 0; i < list_chat.size(); i++) {
                                    if (test_num < list_chat.get(i).num) {
                                        list_chat.add(i, messageModel);
                                        break;
                                    }
                                }
                            }
                            if (!nick.equals(MainActivity.NickName)) {
                                list_users.add(new UserModel(nick, Role.NONE));
                                PlayersAdapter playersAdapter = new PlayersAdapter(list_users, getContext());
                                gridView_users.setAdapter(playersAdapter);
                            }
                            for (int i = 0; i < list_chat.size(); i++) {
                                if (list_chat.get(i).message.equals(nick + " вышел(-а) из чата")) {
                                    list_chat.remove(i);
                                }
                            }
                            messageAdapter.notifyDataSetChanged();
                            if (TotalItemsCount < FirstVisibleItem + VisibleItemsCount + 3) {
                                listView_chat.setSelection(messageAdapter.getCount() - 1);
                            }
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
                        if (!time.equals("stop"))
                        {
                            if (StopTimer == 1)
                            {
                                timer.setText("--");
                                StopTimer = 0;
                            }
                            else {
                                timer.setText(time);
                            }
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
            getActivity().runOnUiThread(() -> {
                JSONObject data = (JSONObject) args[0];
                String time;
                try {
                    time = data.getString("time");
                    switch (time)
                    {
                        case "lobby":
                            player.setTime(Time.LOBBY);
                            break;
                        case "night_love":
                            voting_number.setVisibility(View.GONE);
                            player.setVoting_number(0);
                            DeleteNumbersFromVoting();
                            player.setTime(Time.NIGHT_LOVE);
                            break;
                        case "night_other":
                            player.setTime(Time.NIGHT_OTHER);
                            break;
                        case "day":
                            voting_number.setVisibility(View.GONE);
                            player.setVoting_number(0);
                            DeleteNumbersFromVoting();

                            player.setTime(Time.DAY);
                            break;
                        case "voting":
                            FAB_skip_day.setVisibility(View.GONE);
                            player.setTime(Time.VOTING);
                            break;
                    }
                    day_time.setText(time);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (player.getStatus().equals("alive"))
                {
                    player.setCan_write(false);
                    StopAnimation();
                    switch (player.getTime())
                    {
                        case NIGHT_LOVE:
                            IV_influence_poisoner.setVisibility(View.GONE);
                            IV_influence_lover.setVisibility(View.GONE);
                            switch (player.getRole())
                            {
                                case LOVER:
                                    StartAnimation(Role.LOVER);
                                    break;
                                case DOCTOR_OF_EASY_VIRTUE:
                                    StartAnimation(Role.LOVER);
                                    break;
                                case MAFIA:
                                    player.setCan_write(true);
                                    break;
                                case MAFIA_DON:
                                    player.setCan_write(true);
                                    break;
                                default:
                                    Log.d("kkk", "В " + player.getTime() + " - нельзя активировать роль " + player.getRole());
                                    break;
                            }
                            break;
                        case NIGHT_OTHER:
                            if (IV_influence_lover.getVisibility() != View.VISIBLE) {
                                switch (player.getRole()) {
                                    case DOCTOR_OF_EASY_VIRTUE:
                                        if (!player.getVoted_at_night()) {
                                            StartAnimation(Role.DOCTOR);
                                        }
                                        break;
                                    case SHERIFF:
                                        StartAnimation(Role.SHERIFF);
                                        break;
                                    case DOCTOR:
                                        StartAnimation(Role.DOCTOR);
                                        break;
                                    case MAFIA:
                                        StartAnimation(Role.MAFIA);
                                        break;
                                    case MAFIA_DON:
                                        StartAnimation(Role.MAFIA);
                                        break;
                                    case MANIAC:
                                        StartAnimation(Role.MAFIA);
                                        break;
                                    case POISONER:
                                        StartAnimation(Role.POISONER);
                                        break;
                                    case JOURNALIST:
                                        StartAnimation(Role.JOURNALIST);
                                        break;
                                }
                            }
                            break;
                        case DAY:
                            FAB_skip_day.setVisibility(View.VISIBLE);
                            IV_influence_doctor.setVisibility(View.GONE);
                            player.setVoted_at_night(false);
                            player.setCan_write(true);
                            IV_influence_bodyguard.setVisibility(View.GONE);
                            if (player.getRole() == Role.BODYGUARD && IV_influence_lover.getVisibility() != View.VISIBLE) {
                                StartAnimation(Role.BODYGUARD);
                            }
                            break;
                        case VOTING:
                            messages_can_write = 10;
                            if (IV_influence_lover.getVisibility() != View.VISIBLE)
                            {
                                if (player.getRole() == Role.TERRORIST) {
                                    StartAnimation(Role.TERRORIST);
                                } else {
                                    StartAnimation(Role.VOTING);
                                }
                            }
                            break;
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
                    String rus_role = "";
                    try {
                        role = data.getString("role");
                        SharedPreferences.Editor editor = mSettings.edit();
                        editor.putString(APP_PREFERENCES_LAST_ROLE, role);
                        editor.apply();
                        player.setRole(ConvertToRole(role));
                        switch (player.getRole())
                        {
                            case NONE:
                                IV_role.setImageResource(R.drawable.anonim);
                                rus_role = "пустышка";
                                break;
                            case CITIZEN:
                                IV_role.setImageResource(R.drawable.citizen_alive);
                                rus_role = "мирный житель";
                                break;
                            case MAFIA:
                                IV_role.setImageResource(R.drawable.mafia_alive);
                                rus_role = "мафия";
                                break;
                            case SHERIFF:
                                IV_role.setImageResource(R.drawable.sheriff_alive);
                                rus_role = "шериф";
                                break;
                            case DOCTOR:
                                IV_role.setImageResource(R.drawable.doctor_alive);
                                rus_role = "доктор";
                                break;
                            case LOVER:
                                IV_role.setImageResource(R.drawable.lover_alive);
                                rus_role = "любовница";
                                break;
                            case MAFIA_DON:
                                IV_role.setImageResource(R.drawable.mafia_don_alive);
                                rus_role = "дон мафии";
                                break;
                            case MANIAC:
                                IV_role.setImageResource(R.drawable.maniac_alive);
                                rus_role = "маньяк";
                                break;
                            case TERRORIST:
                                IV_role.setImageResource(R.drawable.terrorist_alive);
                                rus_role = "террорист";
                                break;
                            case BODYGUARD:
                                IV_role.setImageResource(R.drawable.bodyguard_alive);
                                rus_role = "телохранитель";
                                break;
                            case DOCTOR_OF_EASY_VIRTUE:
                                IV_role.setImageResource(R.drawable.doctor_of_easy_virtue_alive);
                                rus_role = "доктор лёгкого поведения";
                                break;
                            case POISONER:
                                IV_role.setImageResource(R.drawable.poisoner_alive);
                                rus_role = "отравитель";
                                break;
                            case JOURNALIST:
                                IV_role.setImageResource(R.drawable.journalist_alive);
                                rus_role = "журналист";
                                break;
                        }
                        Log.d("kkk", "Socket_принять - role " + role);
                        AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                        builder.setTitle("Вам выдали роль!").setMessage("Ваша роль - " + rus_role).setIcon(R.drawable.icon_mir).setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();
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
                        json2.put("last_dead_message_num", -1);
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
                                StopAnimation();
                                IV_influence_lover.setVisibility(View.VISIBLE);
                                break;
                            case "sheriff":
                                IV_influence_sheriff.setVisibility(View.VISIBLE);
                                break;
                            case "bodyguard":
                                IV_influence_bodyguard.setVisibility(View.VISIBLE);
                                break;
                            case "poisoner":
                                IV_influence_poisoner.setVisibility(View.VISIBLE);
                                break;
                            default:
                                Log.d("kkk", "ERROR!!! INFLUENCE");
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
                    String nick;
                    try {
                        Log.d("kkk", "Socket_принять - know_role " + args[0]);
                        Role role = ConvertToRole(data.getString("role"));
                        nick = data.getString("nick");
                        for (int i = 0; i < list_users.size(); i++)
                        {
                            if (list_users.get(i).getNick().equals(nick))
                            {
                                list_users.get(i).setRole(role);
                                break;
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
                    String message, time, status, mafia_nick, user_nick, voter, nick;
                    int test_num, money, exp;
                    JSONObject data2;
                    try {
                        status = data.getString("status");
                        test_num = data.getInt("num");
                        time = data.getString("time");
                        message = data.getString("message");
                        MessageModel messageModel = new MessageModel(test_num, "Ошибка вывода сообщения", time.substring(11, 16), "Server", "SystemMes");
                        Log.d("kkk", "system message - " + " Длина listchat = " + list_chat.size() + " /  testnum = " + test_num + " / num = " + num + " , status - " + status + "/" +  data);
                        if (test_num != num) {
                            switch (status)
                            {
                                case "game_over":
                                    money = data.getInt("money");
                                    exp = data.getInt("exp");

                                    AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
                                    View view_end_game = getLayoutInflater().inflate(R.layout.dialog_end_game, null);
                                    builder2.setView(view_end_game);

                                    TextView TV_message = view_end_game.findViewById(R.id.dialogEndGame_TV_message);
                                    TextView TV_money = view_end_game.findViewById(R.id.dialogEndGame_TV_money);
                                    TextView TV_exp = view_end_game.findViewById(R.id.dialogEndGame_TV_exp);

                                    TV_message.setText(message);
                                    TV_money.setText(String.valueOf(money));
                                    TV_exp.setText(String.valueOf(exp));

                                    AlertDialog alert2 = builder2.create();
                                    alert2.show();

                                    messageModel = new MessageModel(test_num, message, time.substring(11, 16), "Server", "SystemMes");
                                    PlayersAdapter playersAdapter = new PlayersAdapter(list_users, getContext());
                                    gridView_users.setAdapter(playersAdapter);
                                    break;
                                case "dead_user":
                                    data2 = data.getJSONObject("message");
                                    message = data2.getString("message");
                                    nick = data2.getString("nick");
                                    Role role = ConvertToRole(data2.getString("role"));
                                    for (int i = 0; i < list_users.size(); i++)
                                    {
                                        if (nick.equals(player.getNick()))
                                        {
                                            player.setStatus("dead");
                                            player.setCan_write(true);
                                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                            builder.setTitle("Вас убили!")
                                                    .setMessage("Но вы всё равно ещё можете отправить последнее сообщение!")
                                                    .setIcon(R.drawable.ic_mafia)
                                                    .setCancelable(false)
                                                    .setNegativeButton("Ок",
                                                            (dialog, id) -> dialog.cancel());
                                            AlertDialog alert = builder.create();
                                            alert.show();
                                            switch (player.getRole()) {
                                                case NONE:
                                                    IV_role.setImageResource(R.drawable.anonim);
                                                    break;
                                                case CITIZEN:
                                                    IV_role.setImageResource(R.drawable.citizen_dead);
                                                    break;
                                                case MAFIA:
                                                    IV_role.setImageResource(R.drawable.mafia_dead);
                                                    break;
                                                case SHERIFF:
                                                    IV_role.setImageResource(R.drawable.sheriff_dead);
                                                    break;
                                                case DOCTOR:
                                                    IV_role.setImageResource(R.drawable.doctor_dead);
                                                    break;
                                                case LOVER:
                                                    IV_role.setImageResource(R.drawable.lover_dead);
                                                    break;
                                                case MAFIA_DON:
                                                    IV_role.setImageResource(R.drawable.mafia_don_dead);
                                                    break;
                                                case MANIAC:
                                                    IV_role.setImageResource(R.drawable.maniac_dead);
                                                    break;
                                                case TERRORIST:
                                                    IV_role.setImageResource(R.drawable.terrorist_dead);
                                                    break;
                                                case BODYGUARD:
                                                    IV_role.setImageResource(R.drawable.bodyguard_dead);
                                                    break;
                                                case DOCTOR_OF_EASY_VIRTUE:
                                                    IV_role.setImageResource(R.drawable.doctor_of_easy_virtue_dead);
                                                    break;
                                                case POISONER:
                                                    IV_role.setImageResource(R.drawable.poisoner_dead);
                                                    break;
                                                case JOURNALIST:
                                                    IV_role.setImageResource(R.drawable.journalist_dead);
                                                    break;
                                            }
                                            break;
                                        }
                                        else if (list_users.get(i).getNick().equals(nick))
                                        {
                                            list_users.get(i).setRole(role);
                                            list_users.get(i).setAlive(false);
                                            break;
                                        }
                                    }
                                    if (data2.has("nick_2")) {
                                        String nick2 = data2.getString("nick_2");
                                        Role role2 = ConvertToRole(data2.getString("role_2"));
                                        for (int i = 0; i < list_users.size(); i++)
                                        {
                                            if (list_users.get(i).getNick().equals(nick2))
                                            {
                                                list_users.get(i).setRole(role2);
                                                list_users.get(i).setAlive(false);
                                            }
                                            else if (nick2.equals(player.getNick()))
                                            {
                                                player.setStatus("dead");
                                                player.setCan_write(true);
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle("Вас убили!")
                                                        .setMessage("Но вы всё равно ещё можете отправить последнее сообщение!")
                                                        .setIcon(R.drawable.ic_mafia)
                                                        .setCancelable(false)
                                                        .setNegativeButton("Ок",
                                                                (dialog, id) -> dialog.cancel());
                                                AlertDialog alert = builder.create();
                                                alert.show();
                                                switch (player.getRole()) {
                                                    case NONE:
                                                        IV_role.setImageResource(R.drawable.anonim);
                                                        break;
                                                    case CITIZEN:
                                                        IV_role.setImageResource(R.drawable.citizen_dead);
                                                        break;
                                                    case MAFIA:
                                                        IV_role.setImageResource(R.drawable.mafia_dead);
                                                        break;
                                                    case SHERIFF:
                                                        IV_role.setImageResource(R.drawable.sheriff_dead);
                                                        break;
                                                    case DOCTOR:
                                                        IV_role.setImageResource(R.drawable.doctor_dead);
                                                        break;
                                                    case LOVER:
                                                        IV_role.setImageResource(R.drawable.lover_dead);
                                                        break;
                                                    case MAFIA_DON:
                                                        IV_role.setImageResource(R.drawable.mafia_don_dead);
                                                        break;
                                                    case MANIAC:
                                                        IV_role.setImageResource(R.drawable.maniac_dead);
                                                        break;
                                                    case TERRORIST:
                                                        IV_role.setImageResource(R.drawable.terrorist_dead);
                                                        break;
                                                    case BODYGUARD:
                                                        IV_role.setImageResource(R.drawable.bodyguard_dead);
                                                        break;
                                                    case DOCTOR_OF_EASY_VIRTUE:
                                                        IV_role.setImageResource(R.drawable.doctor_of_easy_virtue_dead);
                                                        break;
                                                    case POISONER:
                                                        IV_role.setImageResource(R.drawable.poisoner_dead);
                                                        break;
                                                    case JOURNALIST:
                                                        IV_role.setImageResource(R.drawable.journalist_dead);
                                                        break;
                                                }
                                                break;
                                            }
                                        }
                                    }
                                    StopAnimation();
                                    PlayersAdapter playersAdapter2 = new PlayersAdapter(list_users, getContext());
                                    gridView_users.setAdapter(playersAdapter2);
                                    messageModel = new MessageModel(test_num, message, time.substring(11, 16), "Server", "SystemMes");
                                    break;
                                case "role_action_mafia":
                                    data2 = data.getJSONObject("message");
                                    mafia_nick = data2.getString("mafia_nick");
                                    user_nick = data2.getString("user_nick");
                                    for (int i = 0; i < list_users.size(); i++) {
                                        if (list_users.get(i).getNick().equals(user_nick)) {
                                            list_users.get(i).setVoting_number(list_users.get(i).getVoting_number() + 1);
                                            break;
                                        }
                                    }
                                    PlayersAdapter playersAdapter3 = new PlayersAdapter(list_users, getContext());
                                    gridView_users.setAdapter(playersAdapter3);
                                    messageModel = new MessageModel(test_num, "Голосует за " + user_nick, time.substring(11,16), mafia_nick, "VotingMes");
                                    break;
                                case "voting":
                                    data2 = data.getJSONObject("message");
                                    voter = data2.getString("voter");
                                    user_nick = data2.getString("user_nick");
                                    if (!user_nick.equals(player.getNick())) {
                                        for (int i = 0; i < list_users.size(); i++) {
                                            if (list_users.get(i).getNick().equals(user_nick)) {
                                                list_users.get(i).setVoting_number(list_users.get(i).getVoting_number() + 1);
                                            }
                                        }
                                    }
                                    else
                                    {
                                        if (player.getVoting_number() == 0)
                                        {
                                            voting_number.setVisibility(View.VISIBLE);
                                        }
                                        player.setVoting_number(player.getVoting_number() + 1);
                                        voting_number.setText(String.valueOf(player.getVoting_number()));
                                    }
                                    playersAdapter3 = new PlayersAdapter(list_users, getContext());
                                    gridView_users.setAdapter(playersAdapter3);
                                    messageModel = new MessageModel(test_num,"Голосует за " + user_nick, time.substring(11,16), voter, "VotingMes");
                                    break;
                                case "time_info":
                                    messageModel = new MessageModel(test_num, message, time.substring(11,16), "Server", "SystemMes");
                                    break;
                                case "journalist":
                                    data2 = data.getJSONObject("message");
                                    message = data2.getString("message");
                                    messageModel = new MessageModel(test_num, message, time.substring(11,16), "Server", "SystemMes");
                                    break;
                            }

                            //если num из data больше нашего num, то просто вставляем сообщение в список на 1 место, else вставляем сообщение на нужное место

                            if (test_num > num) {
                                num = test_num;
                                list_chat.add(messageModel);
                            } else {
                                for (int i = 0; i < list_chat.size(); i++) {
                                    if (test_num < list_chat.get(i).num) {
                                        list_chat.add(i, messageModel);
                                        break;
                                    }
                                }
                            }
                            messageAdapter.notifyDataSetChanged();
                            if (TotalItemsCount < FirstVisibleItem + VisibleItemsCount + 3) {
                                listView_chat.setSelection(messageAdapter.getCount() - 1);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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
                            case "you_are_playing_in_another_room":
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
                            case "you_are_banned_in_this_room":
                                builder.setTitle("Извините, но вы забанены в этой комнате")
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
                            default:
                                builder.setTitle("Извините, но что-то пошло не так")
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
                    String nick;
                    try {
                        data2 = data.getJSONObject("mafias");
                        for (Iterator iterator = data2.keys(); iterator.hasNext();)
                        {
                            nick = (String) iterator.next();
                            Role role = ConvertToRole(data2.getString(nick));
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
                    String role = "", status = "", time = "", nick;
                    boolean can_act = false, can_vote = false, last_message = false, can_skip_day = true;
                    boolean sheriff = false, doctor = false, lover = false, bodyguard = false, poisoner = false;
                    int voted_number;
                    Log.d("kkk", "Socket_принять - get_my_game_info - " + args[0]);
                    JSONObject data2;
                    JSONObject influences;
                    try {
                        if (data.has("sheriff_checks"))
                        {
                            data2 = data.getJSONObject("sheriff_checks");
                            for (Iterator iterator = data2.keys(); iterator.hasNext();)
                            {
                                nick = (String) iterator.next();
                                Role role2 = ConvertToRole(data2.getString(nick));
                                for (int i = 0; i < list_users.size(); i++)
                                {
                                    if (list_users.get(i).getNick().equals(nick))
                                    {
                                        list_users.get(i).setRole(role2);
                                        break;
                                    }
                                }
                            }
                            PlayersAdapter playersAdapter = new PlayersAdapter(list_users, getContext());
                            gridView_users.setAdapter(playersAdapter);
                        }
                        if (data.has("journalist_checks"))
                        {
                            JSONArray journalist_checks;
                            journalist_checks = data.getJSONArray("journalist_checks");
                            for (int i = 0; i < journalist_checks.length(); i++)
                            {
                                for (int j = 0; j < list_users.size(); j++)
                                {
                                    if (list_users.get(j).getNick().equals(journalist_checks.getString(i)))
                                    {
                                        list_users.get(j).setChecked(true);
                                    }
                                }
                            }
                            PlayersAdapter playersAdapter = new PlayersAdapter(list_users, getContext());
                            gridView_users.setAdapter(playersAdapter);
                        }
                        if (!data.isNull("voting"))
                        {
                            voting_number.setVisibility(View.GONE);
                            player.setVoting_number(0);
                            DeleteNumbersFromVoting();
                            JSONObject voting = data.getJSONObject("voting");
                            for (Iterator iterator = voting.keys(); iterator.hasNext();)
                            {
                                nick = (String) iterator.next();
                                voted_number = voting.getInt(nick);
                                for (int i = 0; i < list_users.size(); i++)
                                {
                                    if (list_users.get(i).getNick().equals(nick))
                                    {
                                        list_users.get(i).setVoting_number(voted_number);
                                        break;
                                    }
                                    else if (list_users.get(i).getNick().equals(player.getNick()))
                                    {
                                        if (player.getVoting_number() == 0)
                                        {
                                            voting_number.setVisibility(View.VISIBLE);
                                        }
                                        player.setVoting_number(player.getVoting_number() + 1);
                                        voting_number.setText(String.valueOf(player.getVoting_number()));
                                    }
                                }
                            }
                            PlayersAdapter playersAdapter = new PlayersAdapter(list_users, getContext());
                            gridView_users.setAdapter(playersAdapter);
                        }
                        else {
                            voting_number.setVisibility(View.GONE);
                            player.setVoting_number(0);
                            DeleteNumbersFromVoting();
                        }
                        can_skip_day = data.getBoolean("can_skip_day");
                        messages_can_write = data.getInt("messages_counter");
                        role = data.getString("role");
                        status = data.getString("status");
                        time = data.getString("time");
                        can_vote = data.getBoolean("can_vote");
                        can_act = data.getBoolean("can_act");
                        influences = data.getJSONObject("influences");
                        sheriff = influences.getBoolean("sheriff");
                        doctor = influences.getBoolean("doctor");
                        lover = influences.getBoolean("lover");
                        bodyguard = influences.getBoolean("bodyguard");
                        poisoner = influences.getBoolean("poisoner");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (sheriff) IV_influence_sheriff.setVisibility(View.VISIBLE);
                    if (doctor) IV_influence_doctor.setVisibility(View.VISIBLE);
                    if (lover) IV_influence_lover.setVisibility(View.VISIBLE);
                    if (bodyguard) IV_influence_bodyguard.setVisibility(View.VISIBLE);
                    if (poisoner) IV_influence_poisoner.setVisibility(View.VISIBLE);

                    day_time.setText(time);
                    player.setRole(ConvertToRole(role));
                    player.setStatus(status);
                    switch (time) {
                        case "lobby":
                            player.setTime(Time.LOBBY);
                            break;
                        case "night_love":
                            voting_number.setVisibility(View.GONE);
                            player.setVoting_number(0);
                            DeleteNumbersFromVoting();
                            player.setTime(Time.NIGHT_LOVE);
                            break;
                        case "night_other":
                            player.setTime(Time.NIGHT_OTHER);
                            break;
                        case "day":
                            voting_number.setVisibility(View.GONE);
                            player.setVoting_number(0);
                            DeleteNumbersFromVoting();
                            player.setTime(Time.DAY);
                            break;
                        case "voting":
                            FAB_skip_day.setVisibility(View.GONE);
                            player.setTime(Time.VOTING);
                            break;
                    }
                    switch (player.getRole()) {
                        case NONE:
                            IV_role.setImageResource(R.drawable.anonim);
                            break;
                        case CITIZEN:
                            if (status.equals("alive")) { IV_role.setImageResource(R.drawable.citizen_alive); }
                            else { IV_role.setImageResource(R.drawable.citizen_dead); }
                            break;
                        case MAFIA:
                            if (status.equals("alive")) { IV_role.setImageResource(R.drawable.mafia_alive); }
                            else { IV_role.setImageResource(R.drawable.mafia_dead); }
                            break;
                        case SHERIFF:
                            if (status.equals("alive")) { IV_role.setImageResource(R.drawable.sheriff_alive); }
                            else { IV_role.setImageResource(R.drawable.sheriff_dead); }
                            break;
                        case DOCTOR:
                            if (status.equals("alive")) { IV_role.setImageResource(R.drawable.doctor_alive); }
                            else { IV_role.setImageResource(R.drawable.doctor_dead); }
                            break;
                        case LOVER:
                            if (status.equals("alive")) { IV_role.setImageResource(R.drawable.lover_alive); }
                            else { IV_role.setImageResource(R.drawable.lover_dead); }
                            break;
                        case MAFIA_DON:
                            if (status.equals("alive")) { IV_role.setImageResource(R.drawable.mafia_don_alive); }
                            else { IV_role.setImageResource(R.drawable.mafia_don_dead); }
                            break;
                        case MANIAC:
                            if (status.equals("alive")) { IV_role.setImageResource(R.drawable.maniac_alive); }
                            else { IV_role.setImageResource(R.drawable.maniac_dead); }
                            break;
                        case TERRORIST:
                            if (status.equals("alive")) { IV_role.setImageResource(R.drawable.terrorist_alive); }
                            else { IV_role.setImageResource(R.drawable.terrorist_dead); }
                            break;
                        case BODYGUARD:
                            if (status.equals("alive")) { IV_role.setImageResource(R.drawable.bodyguard_alive); }
                            else { IV_role.setImageResource(R.drawable.bodyguard_dead); }
                            break;
                        case DOCTOR_OF_EASY_VIRTUE:
                            if (status.equals("alive")) { IV_role.setImageResource(R.drawable.doctor_of_easy_virtue_alive); }
                            else { IV_role.setImageResource(R.drawable.doctor_of_easy_virtue_dead); }
                            break;
                        case POISONER:
                            if (status.equals("alive")) { IV_role.setImageResource(R.drawable.poisoner_alive); }
                            else { IV_role.setImageResource(R.drawable.poisoner_dead); }
                            break;
                        case JOURNALIST:
                            if (status.equals("alive")) { IV_role.setImageResource(R.drawable.journalist_alive); }
                            else { IV_role.setImageResource(R.drawable.journalist_dead); }
                            break;
                    }
                    if (player.getStatus().equals("alive")) {
                        player.setCan_write(false);
                        switch (player.getTime()) {
                            case NIGHT_LOVE:
                                IV_influence_lover.setVisibility(View.GONE);
                                switch (player.getRole()) {
                                    case MAFIA:
                                        player.setCan_write(true);
                                        break;
                                    case MAFIA_DON:
                                        player.setCan_write(true);
                                        break;
                                    case LOVER:
                                        if (can_act) StartAnimation(Role.LOVER);
                                }
                                break;
                            case NIGHT_OTHER:
                                switch (player.getRole()) {
                                    case MAFIA:
                                    case MAFIA_DON:
                                        if (can_act) StartAnimation(Role.MAFIA);
                                        break;
                                    case DOCTOR:
                                        if (can_act) StartAnimation(Role.DOCTOR);
                                        break;
                                    case SHERIFF:
                                        if (can_act) StartAnimation(Role.SHERIFF);
                                        break;
                                    case MANIAC:
                                        if (can_act) StartAnimation(Role.MANIAC);
                                        break;
                                    case DOCTOR_OF_EASY_VIRTUE:
                                        if (can_act) StartAnimation(Role.DOCTOR_OF_EASY_VIRTUE);
                                        break;
                                    case POISONER:
                                        if (can_act) StartAnimation(Role.POISONER);
                                        break;
                                    case JOURNALIST:
                                        if (can_act) StartAnimation(Role.JOURNALIST);
                                        break;
                                    default:
                                        Log.d("kkk", "В " + player.getTime() + " - нельзя активировать роль " + player.getRole());
                                }
                                break;
                            case DAY:
                                FAB_skip_day.setVisibility(View.VISIBLE);
                                IV_influence_doctor.setVisibility(View.GONE);
                                player.setVoted_at_night(false);
                                player.setCan_write(true);
                                IV_influence_bodyguard.setVisibility(View.GONE);
                                if (player.getRole() == Role.BODYGUARD && IV_influence_lover.getVisibility() != View.VISIBLE) {
                                    StartAnimation(Role.BODYGUARD);
                                }
                                break;
                            case VOTING:
                                messages_can_write = 10;
                                if (IV_influence_lover.getVisibility() != View.VISIBLE)
                                {
                                    if (player.getRole() == Role.TERRORIST) {
                                        StartAnimation(Role.TERRORIST);
                                    } else {
                                        StartAnimation(Role.VOTING);
                                    }
                                }
                        }
                    }
                    else {
                        player.setCan_write(true);
                    }
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

    private final Emitter.Listener OnGetProfile = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject data = (JSONObject) args[0];
                Log.d("kkk", "принял - get_profile - " + data);
                String nick = "", user_id_2 = "", avatar = "";
                int playing_room_num = 0, money = 0, exp = 0;
                boolean online = false;
                try {
                    avatar = data.getString("avatar");
                    online = data.getBoolean("is_online");
                    nick = data.getString("nick");
                    user_id_2 = data.getString("user_id");
                    money = data.getInt("money");
                    exp = data.getInt("exp");
                    if (data.has("playing_room_num")) {
                        playing_room_num = data.getInt("playing_room_num");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                View view_profile = getLayoutInflater().inflate(R.layout.item_profile, null);
                builder.setView(view_profile);

                FloatingActionButton FAB_add_friend = view_profile.findViewById(R.id.Item_profile_add_friend);
                FloatingActionButton FAB_kick = view_profile.findViewById(R.id.Item_profile_kick);
                FloatingActionButton FAB_send_message = view_profile.findViewById(R.id.Item_profile_send_message);
                FloatingActionButton FAB_complain = view_profile.findViewById(R.id.Item_profile_complain);
                TextView TV_money = view_profile.findViewById(R.id.ItemProfile_TV_money);
                TextView TV_exp = view_profile.findViewById(R.id.ItemProfile_TV_exp);
                ImageView IV_avatar = view_profile.findViewById(R.id.Item_profile_IV_avatar);

                if (avatar != null) {
                    IV_avatar.setImageBitmap(fromBase64(avatar));
                }

                FAB_kick.setVisibility(View.GONE);

                TV_money.setText(String.valueOf(money));
                TV_exp.setText(String.valueOf(exp));

                TextView TV_nick = view_profile.findViewById(R.id.Item_profile_TV_nick);
                ImageView IV_on_off = view_profile.findViewById(R.id.Item_profile_IV_on_off);

                if (online) IV_on_off.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_online));
                else IV_on_off.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_offline));

                TV_nick.setText(nick);

                AlertDialog alert = builder.create();

                String finalUser_id_ = user_id_2;
                if (!nick.equals(player.getNick())) {
                    if (player.getNick().equals(player.getHost_nick()) && player.getBan_limit() > 0)
                    {
                        String finalNick = nick;
                        FAB_kick.setVisibility(View.VISIBLE);
                        FAB_kick.setOnClickListener(v -> {
                            final JSONObject json = new JSONObject();
                            try {
                                json.put("nick", player.getNick());
                                json.put("session_id", player.getSession_id());
                                json.put("room", player.getRoom_num());
                                json.put("ban_nick", finalNick);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            socket.emit("ban_user_in_room", json);
                            Log.d("kkk", "Socket_отправка - ban_user_in_room - "+ json.toString());
                            player.setBan_limit(player.getBan_limit() - 1);
                        });
                    }
                    else {
                        FAB_kick.setVisibility(View.GONE);
                    }

                    String finalNick1 = nick;
                    FAB_send_message.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alert.cancel();
                            MainActivity.User_id_2 = finalUser_id_;
                            MainActivity.NickName_2 = finalNick1;
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new PrivateMessagesFragment()).commit();
                        }
                    });

                    FAB_complain.setOnClickListener(v -> {

                    });

                    FAB_add_friend.setOnClickListener(v1 -> {
                        //TODO: добавление в друзья
                    });
                }
                else
                {
                    FAB_send_message.setVisibility(View.GONE);
                    FAB_add_friend.setVisibility(View.GONE);
                    FAB_complain.setVisibility(View.GONE);
                }
                alert.show();
            }
        });
    };

    private final Emitter.Listener OnHostInfo = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject data = (JSONObject) args[0];
                Log.d("kkk", "принял - host_info - " + data);
                String host_nick = "";
                int ban_limit = 5;

                try {
                    host_nick = data.getString("host_nick");
                    ban_limit = data.getInt("ban_limit");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                player.setHost_nick(host_nick);
                player.setBan_limit(ban_limit);
            }
        });
    };

    private final Emitter.Listener OnBanUserInRoom = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject data = (JSONObject) args[0];
                Log.d("kkk", "принял - ban_user_in_room - " + data);
                String nick = "";
                String time = "";
                int test_num = -1;
                try {
                    test_num = data.getInt("num");
                    nick = data.getString("nick");
                    time = data.getString("time");
                } catch (JSONException e) {
                    return;
                }
                if (nick.equals(MainActivity.NickName))
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Вас кикнули из комнаты!")
                            .setMessage("")
                            .setIcon(R.drawable.ic_error)
                            .setCancelable(false)
                            .setNegativeButton("Ок",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
                }
                MessageModel messageModel = new MessageModel(test_num, player.getHost_nick(), time.substring(11,16), nick, "KickMes");

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
                            break;
                        }
                    }
                }
                messageAdapter.notifyDataSetChanged();
                if (TotalItemsCount < FirstVisibleItem + VisibleItemsCount + 3) {
                    listView_chat.setSelection(messageAdapter.getCount() - 1);
                }
                for (int i=list_users.size()-1; i>=0; i--)
                {
                    if (list_users.get(i).getNick().equals(nick))
                    {
                        Log.d("kkk", "remove " + list_users.get(i).getNick());
                        list_users.remove(i);
                    }
                }
                PlayersAdapter playersAdapter = new PlayersAdapter(list_users, getContext());
                gridView_users.setAdapter(playersAdapter);
            }
        });
    };

    private final Emitter.Listener OnUserMessageDelay = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //JSONObject data = (JSONObject) args[0];
                Log.d("kkk", "принял - user_message_delay - " + args[0]);
                String time_to_unmute = String.valueOf(args[0]);
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Вас слишком часто отправляете сообщения!")
                        .setMessage("Вы сможете написать следующее сообщение через " + time_to_unmute + "сек.")
                        .setIcon(R.drawable.ic_error)
                        .setCancelable(false)
                        .setNegativeButton("ок",
                                (dialog, id) -> dialog.cancel());
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    };

    /*******************************
     *                             *
     *       SOCKETS end           *
     *                             *
     *******************************/

    //запуск анимации
    public void StartAnimation(Role type) {
        player.setCan_click(true);
        for (int i = 0; i < list_users.size(); i++)
        {
            if (list_users.get(i).getAlive())
            {
                if (type != Role.VOTING)
                {
                    list_users.get(i).setAnimation_type(type);
                }
                else
                {
                    list_users.get(i).setAnimation_type(type);
                }
            }
            PlayersAdapter playersAdapter = new PlayersAdapter(list_users, getContext());
            gridView_users.setAdapter(playersAdapter);
        }
    }
    //конец анимации
    public void StopAnimation() {
        player.setCan_click(false);
        for (int i = 0; i < list_users.size(); i++)
        {
            list_users.get(i).setAnimation_type(Role.NONE);
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
    //конвертировать String в Role
    public Role ConvertToRole(String role) {
        switch (role)
        {
            case "none":
                return Role.NONE;
            case "citizen":
                return Role.CITIZEN;
            case "mafia":
                return Role.MAFIA;
            case "sheriff":
                return Role.SHERIFF;
            case "doctor":
                return Role.DOCTOR;
            case "lover":
                return Role.LOVER;
            case "mafia_don":
                return Role.MAFIA_DON;
            case "maniac":
                return Role.MANIAC;
            case "terrorist":
                return Role.TERRORIST;
            case "bodyguard":
                return Role.BODYGUARD;
            case "poisoner":
                return Role.POISONER;
            case "journalist":
                return Role.JOURNALIST;
            case "doctor_of_easy_virtue":
                return Role.DOCTOR_OF_EASY_VIRTUE;
            default:
                return Role.NONE;
        }
    }
    //Вывод профиля
    public void ShowProfile(String nick) {
        final JSONObject json = new JSONObject();
        try {
            json.put("nick", player.getNick());
            json.put("session_id", player.getSession_id());
            json.put("info_nick", nick);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("get_profile", json);
        Log.d("kkk", "Socket_отправка - get_profile - "+ json.toString());
    }
    //убрать цифры из голосования
    public void DeleteNumbersFromVoting() {
        for (int i = 0; i < list_users.size(); i++)
        {
            list_users.get(i).setVoting_number(0);
            PlayersAdapter playersAdapter = new PlayersAdapter(list_users, getContext());
            gridView_users.setAdapter(playersAdapter);
        }
    }
}