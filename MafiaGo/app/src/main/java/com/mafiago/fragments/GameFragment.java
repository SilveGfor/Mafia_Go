package com.mafiago.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;

import io.socket.emitter.Emitter;

import static android.app.Activity.RESULT_OK;
import static com.mafiago.MainActivity.f;
import static  com.mafiago.MainActivity.socket;
import static com.mafiago.fragments.MenuFragment.GALLERY_REQUEST;

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
    public TextView TV_mafia_count;
    public TextView TV_peaceful_count;

    public ImageView IV_influence_doctor;
    public ImageView IV_influence_lover;
    public ImageView IV_influence_sheriff;
    public ImageView IV_influence_bodyguard;
    public ImageView IV_influence_poisoner;
    public ImageView IV_role;
    public ImageView IV_my_role_animation;

    public Player player;

    public EditText sendText;

    public ImageView btnSend;
    public FloatingActionButton FAB_skip_day;

    public RelativeLayout RL_my_role;

    public Button btnExit;
    public Button btnDeleteAnswer;

    public Animation animation;

    ArrayList<MessageModel> list_chat = new ArrayList<>();
    ArrayList<UserModel> list_users = new ArrayList<>();
    int[] list_mafias = new int[] {0, 0, 0, 0, 0, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9};
    int[] list_peaceful = new int[] {0, 0, 0, 0, 0, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9, 10, 10, 11, 11};

    JSONObject users = new JSONObject();

    int answer_id = -1;
    public int StopTimer = 0;
    int messages_can_write = 10;
    public String journalist_check = null;

    public int mafia_max = 0, mafia_now = 0, peaceful_max = 0, peaceful_now = 0;

    int num = -1;

    ImageView IV_screenshot;


    View view_report;

    public JSONObject json;

    String base64_screenshot = "", report_nick = "", report_id = "";

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
        view_report = getLayoutInflater().inflate(R.layout.dialog_report, null);

        mSettings = getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        listView_chat = view.findViewById(R.id.ListMes);
        gridView_users = view.findViewById(R.id.ListUsers);
        cardAnswer = view.findViewById(R.id.answerCard);
        answer_nick = view.findViewById(R.id.answerNickChat);
        answer_mes = view.findViewById(R.id.answerTextChat);
        sendText = view.findViewById(R.id.InputMes);
        room_name = view.findViewById(R.id.fragmentGame_TV_room_name);
        voting_number = view.findViewById(R.id.fragmentGame_voting_number);
        TV_mafia_count = view.findViewById(R.id.fragmentGame_TV_mafia_count);
        TV_peaceful_count = view.findViewById(R.id.fragmentGame_TV_peaceful_count);

        btnSend = view.findViewById(R.id.btnSendMes);
        btnDeleteAnswer = view.findViewById(R.id.btnDeleteAnswer);
        btnExit = view.findViewById(R.id.btnExitChat);

        FAB_skip_day = view.findViewById(R.id.fragmentGame_FAB_skip_day);

        RL_my_role = view.findViewById(R.id.fragmentGame_RL_my_role);

        timer = view.findViewById(R.id.timer);
        day_time = view.findViewById(R.id.day_time);

        IV_influence_doctor = view.findViewById(R.id.IV_influence_doctor);
        IV_influence_lover = view.findViewById(R.id.IV_influence_lover);
        IV_influence_sheriff = view.findViewById(R.id.IV_influence_sheriff);
        IV_influence_bodyguard = view.findViewById(R.id.IV_influence_bodyguard);
        IV_influence_poisoner = view.findViewById(R.id.IV_influence_poisoner);
        IV_role = view.findViewById(R.id.IV_role);
        IV_my_role_animation = view.findViewById(R.id.fragmentGame_IV_my_role_animation);

        IV_screenshot = view_report.findViewById(R.id.dialogReport_IV_screenshot);

        IV_influence_doctor.setVisibility(View.GONE);
        IV_influence_lover.setVisibility(View.GONE);
        IV_influence_sheriff.setVisibility(View.GONE);
        IV_influence_bodyguard.setVisibility(View.GONE);
        IV_influence_poisoner.setVisibility(View.GONE);
        IV_my_role_animation.setVisibility(View.GONE);

        voting_number.setVisibility(View.GONE);

        TV_mafia_count.setVisibility(View.GONE);
        TV_peaceful_count.setVisibility(View.GONE);

        FAB_skip_day.setVisibility(View.GONE);

        player = new Player(MainActivity.NickName, MainActivity.Session_id, MainActivity.Game_id, MainActivity.Role);

        cardAnswer.setVisibility(View.GONE);

        room_name.setText(MainActivity.RoomName);

        messageAdapter = new MessageAdapter(list_chat, getContext());
        listView_chat.setAdapter(messageAdapter);

        //socket.off("connect");
        //socket.off("disconnect");
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
        socket.off("ban_user_in_room_error");
        socket.off("user_message_delay");
        socket.off("send_complaint");

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
        socket.on("ban_user_in_room_error", OnBanUserInRoomError);
        socket.on("user_message_delay", OnUserMessageDelay);
        socket.on("send_complaint", onSendComplain);

        json = new JSONObject();
        try {
            json.put("nick", player.getNick());
            json.put("session_id", player.getSession_id());
            json.put("room", player.getRoom_num());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("get_in_room", json);
        Log.d("kkk", "Socket_отправка - get_in_room"+ json.toString());

        RL_my_role.setOnClickListener(v -> {
            Log.d("kkk", String.valueOf(player.Can_click()));
            if (player.Can_click() && player.getStatus().equals("alive") && IV_my_role_animation.getVisibility() == View.VISIBLE)
            {
                switch (player.getTime())
                {
                    case LOBBY:
                        sendText.setText(sendText.getText() + player.getNick());
                        sendText.setSelection(sendText.length());
                        break;
                    case NIGHT_LOVE:
                        break;
                    case NIGHT_OTHER:
                        switch (player.getRole())
                        {
                            case DOCTOR:
                                RoleAction(player.getNick());
                                break;
                            case JOURNALIST:
                                if (journalist_check == null)
                                {
                                    journalist_check = player.getNick();
                                    IV_my_role_animation.setVisibility(View.GONE);
                                }
                                else
                                {
                                    final JSONObject json = new JSONObject();
                                    try {
                                        json.put("nick", player.getNick());
                                        json.put("session_id", player.getSession_id());
                                        json.put("room", player.getRoom_num());
                                        json.put("influence_on_nick", journalist_check);
                                        json.put("influence_on_nick_2", player.getNick());
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    socket.emit("role_action", json);
                                    Log.d("kkk", "Socket_отправка - role_action"+ json.toString());

                                    int journalist_checks_count = 0;
                                    player.setJournalist_checked(true);
                                    for (int i = 0; i < list_users.size(); i++)
                                    {
                                        if (list_users.get(i).getNick().equals(journalist_check))
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
                                IV_my_role_animation.setVisibility(View.GONE);
                                break;
                            default:
                                Log.d("kkk", "В " + player.getTime() + " - нельзя активировать роль " + player.getRole());
                        }
                        break;
                    case DAY:
                        break;
                    case VOTING:
                        break;
                    default:
                        Log.d("kkk", "Что-то пошло не так. Такого времени дня не может быть!");
                }
                if (journalist_check == null)
                {
                    IV_my_role_animation.setVisibility(View.GONE);
                    StopAnimation();
                }
            }
            else
            {
                sendText.setText(sendText.getText() + player.getNick());
                sendText.setSelection(sendText.length());
            }
        });

        btnSend.setOnClickListener(v -> {
            String input = String.valueOf(sendText.getText());
            if (input.length() > 300) {
                input = input.substring(0, 301);
            }
            int flag = 0;
            for (int i = 0; i < input.length(); i ++)
            {
                if (Character.isLetter(input.charAt(i))) {
                    for (int j = 0; j < f.length; j++) {
                        if (input.charAt(i) == f[j]) {
                            flag = 1;
                        }
                    }

                    if (flag != 1) {
                        input = input.replace(String.valueOf(input.charAt(i)), "");
                    }
                    flag = 0;
                }
            }
            if (player.getStatus().equals("alive")) {
                if (player.Can_write()) {
                    if (!input.equals("") && !input.equals("/n")) {
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
                                        json2.put("message", input);
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
                                        json2.put("message", input);
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
                                json2.put("message", input);
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
                    json2.put("message", input);
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
                                    if (!player.getJournalist_checked())
                                    {
                                        journalist_checks_count++;
                                    }
                                    for (int i = 0; i < list_users.size(); i++)
                                    {
                                        if (list_users.get(i).getNick().equals(nick) || list_users.get(i).getNick().equals(journalist_check))
                                        {
                                            list_users.get(i).setChecked(true);
                                        }
                                        if (journalist_check.equals(player.getNick()))
                                        {
                                            player.setJournalist_checked(true);
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
                                        player.setJournalist_checked(true);
                                        IV_my_role_animation.setVisibility(View.GONE);
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
                    IV_my_role_animation.setVisibility(View.GONE);
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
            Log.d("kkk", "position - " + position + " ' " + list_chat.get(position).mesType);
            Log.d("kkk", "----");
            if(list_chat.get(position).mesType.equals("UsersMes") || list_chat.get(position).mesType.equals("AnswerMes"))
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
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case GALLERY_REQUEST:
                if(resultCode == RESULT_OK){
                    Uri uri = imageReturnedIntent.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        byte[] bytes = stream.toByteArray();

                        base64_screenshot = Base64.encodeToString(bytes, Base64.DEFAULT);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
                    View view_report = getLayoutInflater().inflate(R.layout.dialog_report, null);
                    builder2.setView(view_report);
                    AlertDialog alert2 = builder2.create();

                    Button btn_add_screenshot = view_report.findViewById(R.id.dialogReport_btn_add_screenshot);
                    Button btn_report = view_report.findViewById(R.id.dialogReport_btn_report);
                    ImageView IV_screenshot = view_report.findViewById(R.id.dialogReport_IV_screenshot);
                    EditText ET_report_message = view_report.findViewById(R.id.dialogReport_ET_report);

                    final String[] reason = {""};

                    RadioGroup radioGroup = view_report.findViewById(R.id.dialogReport_RG);

                    IV_screenshot.setImageURI(null);
                    IV_screenshot.setImageURI(uri);

                    radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(RadioGroup group, int checkedId) {
                            switch (checkedId) {
                                case -1:
                                    break;
                                case R.id.dialogReport_RB_1:
                                    reason[0] = "спам или флуд";
                                    break;
                                case R.id.dialogReport_RB_2:
                                    reason[0] = "размещение материалов рекламного, эротического, порнографического или оскорбительного характера или иной информации, размещение которой запрещено или противоречит нормам действующего законодательства РФ";
                                    break;
                                case R.id.dialogReport_RB_3:
                                    reason[0] = "распространение информации, которая направлена на пропаганду войны, разжигание национальной, расовой или религиозной ненависти и вражды или иной информации, за распространение которой предусмотрена уголовная или административная ответственность";
                                    break;
                                case R.id.dialogReport_RB_4:
                                    reason[0] = "игра против/не в интересах своей команды";
                                    break;
                                case R.id.dialogReport_RB_5:
                                    reason[0] = "фарм (т.е. ведение игры организованной группой лиц, цель которой направлена на быстрое извлечение прибыли вне зависимости от того, кто из участников группы победит)";
                                    break;
                                case R.id.dialogReport_RB_6:
                                    reason[0] = "создание нескольких учётных записей в Приложении, фактически принадлежащих одному и тому же лицу";
                                    break;
                                case R.id.dialogReport_RB_7:
                                    reason[0] = "совершение действий, направленный на введение других Пользователей в заблуждение (не касается игрового процесса)";
                                    break;
                                case R.id.dialogReport_RB_8:
                                    reason[0] = "модератор/администратор злоупотребляет своими полномочиями или положением";
                                    break;
                                case R.id.dialogReport_RB_9:
                                    reason[0] = "другое";
                                    break;

                                default:
                                    break;
                            }
                        }
                    });

                    btn_report.setOnClickListener(v22 -> {
                        final JSONObject json2 = new JSONObject();
                        try {
                            json2.put("nick", MainActivity.NickName);
                            json2.put("session_id", MainActivity.Session_id);
                            json2.put("against_id", report_id);
                            json2.put("against_nick", report_nick);
                            json2.put("reason", reason[0]);
                            json2.put("comment", ET_report_message.getText());
                            json2.put("image", base64_screenshot);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        socket.emit("send_complaint", json2);
                        Log.d("kkk", "Socket_отправка - send_complaint" + json2);
                        alert2.cancel();
                    });

                    btn_add_screenshot.setVisibility(View.GONE);

                    alert2.show();
                }
        }
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
                    String avatar;
                    int test_num;
                    try {
                        test_num = data.getInt("num");
                        time = data.getString("time");
                        time = getDate(time);
                        nick = data.getString("nick");
                        avatar = data.getString("avatar");
                        users.put(nick, avatar);
                        MessageModel messageModel = new MessageModel(test_num, nick + " вошёл(-а) в чат", time, nick, "ConnectMes", avatar);
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
                            for (int i = 0; i < list_chat.size(); i++) {
                                if (list_chat.get(i).nickName.equals(nick)) {
                                    list_chat.get(i).avatar = avatar;
                                }
                            }
                            if (!nick.equals(MainActivity.NickName)) {
                                list_users.add(new UserModel(nick, Role.NONE, avatar));
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
                        time = getDate(time);
                        users.remove(nick);
                        Log.d("kkk", "leave_user - " + " Длина listchat = " + list_chat.size() + " /  testnum = " + test_num + " / num = " + num + "/ " + data);
                    } catch (JSONException e) {
                        return;
                    }
                    String avatar = "";
                    for (int i = 0; i < list_users.size(); i++)
                    {
                        if (list_users.get(i).getNick().equals(nick))
                        {
                             avatar = list_users.get(i).getAvatar();
                             break;
                        }
                    }

                    MessageModel messageModel = new MessageModel(test_num, nick + " вышел(-а) из чата", time, nick, "DisconnectMes", avatar);

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
                    String nick_from_iterator;
                    int test_num;
                    int link;
                    try {
                        main_role = data.getString("role");
                        nick = data.getString("nick");
                        String avatar = "";
                        for (Iterator iterator = users.keys(); iterator.hasNext();)
                        {
                            nick_from_iterator = (String) iterator.next();
                            if (nick.equals(nick_from_iterator))
                            {
                                avatar = users.getString(nick);
                                break;
                            }
                        }
                        test_num = data.getInt("num");
                        Log.d("kkk", "new_message - " + " Длина listchat = " + list_chat.size() + " /  testnum = " + test_num + " / num = " + num + "/ " + data);
                        if (test_num != num) {
                            if (test_num > num) {
                                num = test_num;
                                time = data.getString("time");
                                time = getDate(time);
                                message = data.getString("message");
                                status = data.getString("status");
                                link = data.getInt("link");
                                MessageModel messageModel;
                                if (link == -1) {
                                    messageModel = new MessageModel(test_num, message, time, nick, "UsersMes", status, main_role, avatar);
                                } else {
                                    messageModel = new MessageModel(test_num, message, time, nick, "AnswerMes", status, link, main_role, avatar);
                                }
                                list_chat.add(messageModel);
                                messageAdapter.notifyDataSetChanged();
                            } else {
                                time = data.getString("time");
                                time = getDate(time);
                                message = data.getString("message");
                                status = data.getString("status");
                                link = data.getInt("link");
                                MessageModel messageModel;
                                if (link == -1) {
                                    messageModel = new MessageModel(test_num, message, time, nick, "UsersMes", status, main_role, avatar);
                                } else {
                                    messageModel = new MessageModel(test_num, message, time, nick, "AnswerMes", status, link, main_role, avatar);
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
                    time = getDate(time);
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
                            Log.d("kkk", String.valueOf(IV_my_role_animation.getVisibility()));
                            voting_number.setVisibility(View.GONE);
                            IV_my_role_animation.setVisibility(View.GONE);
                            Log.d("kkk", String.valueOf(IV_my_role_animation.getVisibility()));
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
                            if (TV_mafia_count.getVisibility() != View.VISIBLE)
                            {
                                int players = list_users.size() + 1;
                                mafia_max = list_mafias[players];
                                peaceful_max = list_mafias[players];
                                mafia_now = mafia_max;
                                peaceful_now = peaceful_max;
                                TV_mafia_count.setText("Мафия " + mafia_now + "/" + mafia_max);
                                TV_peaceful_count.setText("Мирные " + peaceful_now + "/" + peaceful_max);
                                TV_mafia_count.setVisibility(View.VISIBLE);
                                TV_peaceful_count.setVisibility(View.VISIBLE);
                            }
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
                                        if (!player.getJournalist_checked()) {
                                            IV_my_role_animation.setImageResource(R.drawable.ic_journalist);
                                            IV_my_role_animation.setVisibility(View.VISIBLE);
                                            animation = AnimationUtils.loadAnimation(getContext(), R.anim.voting);
                                            IV_my_role_animation.startAnimation(animation);
                                        }

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
                        time = getDate(time);
                        message = data.getString("message");
                        MessageModel messageModel = new MessageModel(test_num, "Ошибка вывода сообщения", time, "Server", "SystemMes");
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

                                    messageModel = new MessageModel(test_num, message, time, "Server", "SystemMes");
                                    PlayersAdapter playersAdapter = new PlayersAdapter(list_users, getContext());
                                    gridView_users.setAdapter(playersAdapter);
                                    break;
                                case "dead_user":
                                    data2 = data.getJSONObject("message");
                                    message = data2.getString("message");
                                    nick = data2.getString("nick");
                                    Role role = ConvertToRole(data2.getString("role"));
                                    switch (role) {
                                        case CITIZEN:
                                        case SHERIFF:
                                        case DOCTOR:
                                        case LOVER:
                                        case MANIAC:
                                        case BODYGUARD:
                                        case DOCTOR_OF_EASY_VIRTUE:
                                        case JOURNALIST:
                                            peaceful_now--;
                                            break;
                                        case MAFIA_DON:
                                        case POISONER:
                                        case TERRORIST:
                                        case MAFIA:
                                            mafia_now--;
                                            break;
                                    }
                                    TV_mafia_count.setText("Мафия " + mafia_now + "/" + mafia_max);
                                    TV_peaceful_count.setText("Мирные " + peaceful_now + "/" + peaceful_max);
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
                                        switch (role2) {
                                            case CITIZEN:
                                            case SHERIFF:
                                            case DOCTOR:
                                            case LOVER:
                                            case MANIAC:
                                            case BODYGUARD:
                                            case DOCTOR_OF_EASY_VIRTUE:
                                            case JOURNALIST:
                                                peaceful_now--;
                                                break;
                                            case MAFIA_DON:
                                            case POISONER:
                                            case TERRORIST:
                                            case MAFIA:
                                                mafia_now--;
                                                break;
                                        }
                                        TV_mafia_count.setText("Мафия " + mafia_now + "/" + mafia_max);
                                        TV_peaceful_count.setText("Мирные " + peaceful_now + "/" + peaceful_max);
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
                                    messageModel = new MessageModel(test_num, message, time, "Server", "SystemMes");
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
                                    messageModel = new MessageModel(test_num, "Голосует за " + user_nick, time, mafia_nick, "VotingMes");
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
                                        voting_number.setVisibility(View.VISIBLE);
                                        player.setVoting_number(player.getVoting_number() + 1);
                                        voting_number.setText(String.valueOf(player.getVoting_number()));
                                    }
                                    playersAdapter3 = new PlayersAdapter(list_users, getContext());
                                    gridView_users.setAdapter(playersAdapter3);
                                    messageModel = new MessageModel(test_num,"Голосует за " + user_nick, time, voter, "VotingMes");
                                    break;
                                case "time_info":
                                    messageModel = new MessageModel(test_num, message, time, "Server", "SystemMes");
                                    break;
                                case "journalist":
                                    data2 = data.getJSONObject("message");
                                    message = data2.getString("message");
                                    messageModel = new MessageModel(test_num, message, time, "Server", "SystemMes");
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
                                    else if (journalist_checks.getString(i).equals(player.getNick()))
                                    {
                                        player.setJournalist_checked(true);
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
                                    }
                                    else if (nick.equals(player.getNick()))
                                    {
                                        voting_number.setVisibility(View.VISIBLE);
                                        player.setVoting_number(voted_number);
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
                        time = getDate(time);
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
                            IV_my_role_animation.setVisibility(View.GONE);
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
                    IV_my_role_animation.setVisibility(View.GONE);
                    if (TV_mafia_count.getVisibility() != View.VISIBLE)
                    {
                        int players = list_users.size() + 1;
                        mafia_max = list_mafias[players];
                        peaceful_max = list_mafias[players];
                        mafia_now = mafia_now + mafia_max;
                        peaceful_now = peaceful_now + peaceful_max;
                        TV_mafia_count.setText("Мафия " + mafia_now + "/" + mafia_max);
                        TV_peaceful_count.setText("Мирные " + peaceful_now + "/" + peaceful_max);
                        TV_mafia_count.setVisibility(View.VISIBLE);
                        TV_peaceful_count.setVisibility(View.VISIBLE);
                    }
                    if (player.getStatus().equals("alive")) {
                        player.setCan_write(false);
                        switch (player.getTime()) {
                            case NIGHT_LOVE:
                                IV_influence_poisoner.setVisibility(View.GONE);
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
                                        if (!player.getJournalist_checked())
                                        {
                                            IV_my_role_animation.setImageResource(R.drawable.ic_journalist);
                                            IV_my_role_animation.setVisibility(View.VISIBLE);
                                            animation = AnimationUtils.loadAnimation(getContext(), R.anim.voting);
                                            IV_my_role_animation.startAnimation(animation);
                                        }
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
                int playing_room_num, money = 0, exp = 0, gold = 0;
                boolean online = false;
                JSONObject statistic = new JSONObject();
                int game_counter = 0, max_money_score = 0, max_exp_score = 0;
                String general_pers_of_wins = "", mafia_pers_of_wins = "", peaceful_pers_of_wins = "";

                try {
                    statistic = data.getJSONObject("statistics");
                    game_counter = statistic.getInt("game_counter");
                    if (statistic.has("max_money_score"))
                    {
                        max_money_score = statistic.getInt("max_money_score");
                        max_exp_score = statistic.getInt("max_exp_score");
                        money = data.getInt("money");
                        gold = data.getInt("gold");
                    }
                    general_pers_of_wins = statistic.getString("general_pers_of_wins");
                    mafia_pers_of_wins = statistic.getString("mafia_pers_of_wins");
                    peaceful_pers_of_wins = statistic.getString("peaceful_pers_of_wins");
                    online = data.getBoolean("is_online");
                    nick = data.getString("nick");
                    avatar = data.getString("avatar");
                    user_id_2 = data.getString("user_id");
                    exp = data.getInt("exp");
                    if (data.has("playing_room_num")) playing_room_num = data.getInt("playing_room_num");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                View view_profile = getLayoutInflater().inflate(R.layout.item_profile, null);
                builder.setView(view_profile);

                FloatingActionButton FAB_add_friend = view_profile.findViewById(R.id.Item_profile_add_friend);
                FloatingActionButton FAB_kick = view_profile.findViewById(R.id.Item_profile_kick);
                FloatingActionButton FAB_send_message = view_profile.findViewById(R.id.Item_profile_send_message);
                FloatingActionButton FAB_report = view_profile.findViewById(R.id.Item_profile_complain);
                TextView TV_money = view_profile.findViewById(R.id.ItemProfile_TV_money);
                TextView TV_exp = view_profile.findViewById(R.id.ItemProfile_TV_exp);
                TextView TV_gold = view_profile.findViewById(R.id.ItemProfile_TV_gold);
                ImageView IV_avatar = view_profile.findViewById(R.id.Item_profile_IV_avatar);

                TextView TV_game_counter = view_profile.findViewById(R.id.ItemProfile_TV_game_counter);
                TextView TV_max_money_score = view_profile.findViewById(R.id.ItemProfile_TV_max_money_score);
                TextView TV_max_exp_score = view_profile.findViewById(R.id.ItemProfile_TV_max_exp_score);
                TextView TV_general_pers_of_wins = view_profile.findViewById(R.id.ItemProfile_TV_general_pers_of_wins);
                TextView TV_mafia_pers_of_wins = view_profile.findViewById(R.id.ItemProfile_TV_mafia_pers_of_wins);
                TextView TV_peaceful_pers_of_wins = view_profile.findViewById(R.id.ItemProfile_TV_peaceful_pers_of_wins);

                LinearLayout LL_gold = view_profile.findViewById(R.id.ItemProfile_LL_gold);
                LinearLayout LL_money = view_profile.findViewById(R.id.ItemProfile_LL_money);
                LinearLayout LL_max_money_score = view_profile.findViewById(R.id.ItemProfile_LL_max_money_score);
                LinearLayout LL_max_exp_score = view_profile.findViewById(R.id.ItemProfile_LL_max_exp_score);

                if (avatar != null) {
                    IV_avatar.setImageBitmap(fromBase64(avatar));
                }

                String finalAvatar = avatar;
                IV_avatar.setOnClickListener(v12 -> {
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
                    View view_avatar = getLayoutInflater().inflate(R.layout.dialog_avatar, null);
                    builder2.setView(view_avatar);

                    ImageView IV_dialog_avatar = view_avatar.findViewById(R.id.dialogAvatar_avatar);
                    Button btn_exit_avatar = view_avatar.findViewById(R.id.dialogAvatar_btn_exit);

                    IV_dialog_avatar.setImageBitmap(fromBase64(finalAvatar));

                    AlertDialog alert2 = builder2.create();

                    btn_exit_avatar.setOnClickListener(v13 -> {
                        alert2.cancel();
                    });

                    alert2.show();
                });

                TV_game_counter.setText(String.valueOf(game_counter));
                TV_general_pers_of_wins.setText(general_pers_of_wins);
                TV_mafia_pers_of_wins.setText(mafia_pers_of_wins);
                TV_peaceful_pers_of_wins.setText(peaceful_pers_of_wins);

                if (nick.equals(MainActivity.NickName))
                {
                    TV_max_money_score.setText(String.valueOf(max_money_score));
                    TV_max_exp_score.setText(String.valueOf(max_exp_score));
                    TV_gold.setText(String.valueOf(gold));
                    TV_money.setText(String.valueOf(money));
                }
                else
                {
                    LL_gold.setVisibility(View.INVISIBLE);
                    LL_money.setVisibility(View.INVISIBLE);
                    LL_max_money_score.setVisibility(View.GONE);
                    LL_max_exp_score.setVisibility(View.GONE);
                }
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
                    String finalNick = nick;
                    report_nick = nick;
                    report_id = user_id_2;
                    FAB_send_message.setOnClickListener(v -> {
                        alert.cancel();
                        MainActivity.User_id_2 = finalUser_id_;
                        MainActivity.NickName_2 = finalNick;
                        MainActivity.bitmap_avatar_2 = fromBase64(finalAvatar);
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new PrivateMessagesFragment()).commit();
                    });

                    FAB_report.setOnClickListener(v1 -> {
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
                        View view_report = getLayoutInflater().inflate(R.layout.dialog_report, null);
                        builder2.setView(view_report);
                        AlertDialog alert2 = builder2.create();

                        Button btn_add_screenshot = view_report.findViewById(R.id.dialogReport_btn_add_screenshot);
                        Button btn_report = view_report.findViewById(R.id.dialogReport_btn_report);
                        ImageView IV_screenshot = view_report.findViewById(R.id.dialogReport_IV_screenshot);
                        EditText ET_report_message = view_report.findViewById(R.id.dialogReport_ET_report);

                        final String[] reason = {""};

                        RadioGroup radioGroup = view_report.findViewById(R.id.dialogReport_RG);

                        btn_add_screenshot.setOnClickListener(v2 -> {
                            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                            photoPickerIntent.setType("image/*");
                            startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
                            alert2.cancel();
                        });

                        radioGroup.setVisibility(View.GONE);
                        btn_report.setVisibility(View.GONE);
                        ET_report_message.setVisibility(View.GONE);

                        alert2.show();
                    });

                    FAB_add_friend.setOnClickListener(v1 -> {
                        json = new JSONObject();
                        try {
                            json.put("nick", player.getNick());
                            json.put("session_id", player.getSession_id());
                            json.put("room", player.getRoom_num());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        socket.emit("get_in_room", json);
                        Log.d("kkk", "Socket_отправка - get_in_room"+ json.toString());
                    });
                }
                else
                {
                    FAB_send_message.setVisibility(View.GONE);
                    FAB_add_friend.setVisibility(View.GONE);
                    FAB_report.setVisibility(View.GONE);
                    FAB_kick.setVisibility(View.GONE);
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
                    time = getDate(time);
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
                MessageModel messageModel = new MessageModel(test_num, player.getHost_nick(), time, nick, "KickMes");

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

    private final Emitter.Listener OnBanUserInRoomError = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("kkk", "принял - ban_user_in_room_error");
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Вы не можете выгонять админов из комнаты!")
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
                builder.setTitle("Вs слишком часто отправляете сообщения!")
                        .setMessage("Вы сможете написать следующее сообщение через " + time_to_unmute + " секунд")
                        .setIcon(R.drawable.ic_error)
                        .setCancelable(false)
                        .setNegativeButton("ок",
                                (dialog, id) -> dialog.cancel());
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    };

    private final Emitter.Listener onSendComplain = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject data = (JSONObject) args[0];
                Log.d("kkk", "принял - send_complain - " + data);
                String status = "";
                try {
                    status = data.getString("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (status.equals("complaints_limit_exceeded"))
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Вы превысили лимит жалоб!")
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
                }
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
        IV_my_role_animation.setVisibility(View.GONE);
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

    public String getDate(String ourDate)
    {
        try
        {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date value = formatter.parse(ourDate);

            SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm"); //this format changeable
            dateFormatter.setTimeZone(TimeZone.getDefault());
            ourDate = dateFormatter.format(value);
        }
        catch (Exception e)
        {
            ourDate = "00:00";
        }
        return ourDate;
    }
}