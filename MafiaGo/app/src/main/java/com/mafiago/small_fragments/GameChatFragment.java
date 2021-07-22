package com.mafiago.small_fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.mafiago.R;
import com.mafiago.MainActivity;
import com.mafiago.adapters.MessageAdapter;
import com.mafiago.enums.Time;
import com.mafiago.models.MessageModel;
import com.mafiago.models.Player;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import io.socket.emitter.Emitter;

import static com.mafiago.MainActivity.f;
import static com.mafiago.MainActivity.socket;

public class GameChatFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";

    private int mPage;
    ArrayList<MessageModel> list_chat = new ArrayList<>();

    public int num = -1;

    ListView LV_chat;
    EditText ET_message;
    Button btnSend;
    RelativeLayout RL_send;

    JSONObject users = new JSONObject();

    public int messages_can_write = 10;
    int answer_id = -1;

    public Player player;

    public MessageAdapter messageAdapter;

    public int FirstVisibleItem = 0, VisibleItemsCount = 0,TotalItemsCount = 0;


    public static GameChatFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        GameChatFragment fragment = new GameChatFragment();
        fragment.setArguments(args);
        Log.d("kkk","newInstance - " + page);
        return fragment;
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("kkk","onCreate");
        if (getArguments() != null) {
            mPage = getArguments().getInt(ARG_PAGE);
            Log.d("kkk","onCreate args != 0, = " + mPage);
        }
    }

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.item_chat, container, false);
        LV_chat = view.findViewById(R.id.itemChat_LV_chat);
        ET_message = view.findViewById(R.id.itemChat_ET_message);
        RL_send = view.findViewById(R.id.itemChat_RL_send);
        btnSend = view.findViewById(R.id.itemChat_btn_send);

        player = new Player(MainActivity.NickName, MainActivity.Session_id, MainActivity.Game_id, MainActivity.Role);

        messageAdapter = new MessageAdapter(list_chat, getContext());
        LV_chat.setAdapter(messageAdapter);
        Log.d("kkk", "onCreateView - страница - " + String.valueOf(mPage));
        if (mPage == 1)
        {
            socket.on("get_in_room", onGetInRoom);
            socket.on("user_message", onNewMessage);
            socket.on("leave_room", onLeaveUser);
        }
        else
        {
            RL_send.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.died_button));
        }

        btnSend.setOnClickListener(v -> {
            String input = String.valueOf(ET_message.getText());
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
                            //if (IV_influence_poisoner.getVisibility() != View.VISIBLE) {
                                if (messages_can_write > 0) {
                                    messages_can_write--;
                                    final Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.bounce);

                                    // amplitude 0.2 and frequency 20
                                    android.view.animation.BounceInterpolator interpolator = new android.view.animation.BounceInterpolator();
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
                                    //cardAnswer.setVisibility(View.GONE);
                                    ET_message.setText("");
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
                                /*
                            } else {
                                if (messages_can_write == 10) {
                                    messages_can_write--;
                                    final Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.bounce);

                                    // amplitude 0.2 and frequency 20
                                    android.view.animation.BounceInterpolator interpolator = new android.view.animation.BounceInterpolator();
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
                            */
                        } else {
                            final Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.bounce);

                            // amplitude 0.2 and frequency 20
                            android.view.animation.BounceInterpolator interpolator = new BounceInterpolator();
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
                            //cardAnswer.setVisibility(View.GONE);
                            ET_message.setText("");
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
                //cardAnswer.setVisibility(View.GONE);
                ET_message.setText("");
            }
        });
        return view;
    }

    private Emitter.Listener onGetInRoom = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Log.d("kkk", "Принял - get_in_room: " + args[0]);
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
                        Log.d("kkk", "get_in_room GameChatFragment - " + " Длина listchat = " + list_chat.size() + " /  testnum = " + test_num + " / num = " + num + "/ " + data);
                        if (test_num != num) {
                            for (int i = 0; i < list_chat.size(); i++) {
                                if (list_chat.get(i).message.equals(nick + " вышел(-а) из чата") || list_chat.get(i).message.equals(nick + " вошёл(-а) в чат")) {
                                    list_chat.remove(i);
                                }
                            }
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
                            messageAdapter.notifyDataSetChanged();
                            if (TotalItemsCount < FirstVisibleItem + VisibleItemsCount + 3) {
                                LV_chat.setSelection(messageAdapter.getCount() - 1);
                            }
                        }
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
                        Log.d("kkk", "new_message GameChatFragment - " + " Длина listchat = " + list_chat.size() + " /  testnum = " + test_num + " / num = " + num + "/ " + data);
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
                                LV_chat.setSelection(messageAdapter.getCount() - 1);
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
                    String nick_from_iterator;
                    try {
                        test_num = data.getInt("num");
                        nick = data.getString("nick");
                        time = data.getString("time");
                        time = getDate(time);
                        users.remove(nick);
                        Log.d("kkk", "leave_user GameChatFragment - " + " Длина listchat = " + list_chat.size() + " /  testnum = " + test_num + " / num = " + num + "/ " + data);
                    } catch (JSONException e) {
                        return;
                    }
                    String avatar = "";
                    for (Iterator iterator = users.keys(); iterator.hasNext();)
                    {
                        nick_from_iterator = (String) iterator.next();
                        if (nick.equals(nick_from_iterator))
                        {
                            try {
                                avatar = users.getString(nick);
                            } catch (JSONException e) {

                            }
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

                        messageAdapter.notifyDataSetChanged();
                        if (TotalItemsCount < FirstVisibleItem + VisibleItemsCount + 3) {
                            LV_chat.setSelection(messageAdapter.getCount() - 1);
                        }
                    }
                }
            });
        }
    };

    public String getDate(String ourDate) {
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