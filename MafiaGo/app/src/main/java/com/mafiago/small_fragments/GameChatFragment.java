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
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

import static com.mafiago.MainActivity.USERS;
import static com.mafiago.MainActivity.f;
import static com.mafiago.MainActivity.socket;

public class GameChatFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";

    private int mPage;
    ArrayList<MessageModel> list_chat = new ArrayList<>();

    public int num = -1;
    public boolean Once = true;

    ListView LV_chat;
    EditText ET_message;
    Button btnSend;
    RelativeLayout RL_send;
    TextView TV_answerMes;
    TextView TV_toDeadChat;
    TextView TV_BI;
    ImageView IV_toDeadChatArrow;

    public int messages_can_write = 10;
    int answer_id = -1;

    public Player player;

    boolean poisoner = false;

    public MessageAdapter messageAdapter;

    public int FirstVisibleItem = 0, VisibleItemsCount = 0,TotalItemsCount = 0;


    public static GameChatFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        GameChatFragment fragment = new GameChatFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPage = getArguments().getInt(ARG_PAGE);
        }
    }

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.item_chat, container, false);
        LV_chat = view.findViewById(R.id.itemChat_LV_chat);
        ET_message = view.findViewById(R.id.itemChat_ET_message);
        RL_send = view.findViewById(R.id.itemChat_RL_send);
        btnSend = view.findViewById(R.id.itemChat_btn_send);
        TV_answerMes = view.findViewById(R.id.itemChat_TV_answerMes);
        TV_toDeadChat = view.findViewById(R.id.itemChat_TV_toDeadChat);
        TV_BI = view.findViewById(R.id.itemChat_TV_BI);
        IV_toDeadChatArrow = view.findViewById(R.id.itemChat_TV_toDeadChatArrow);


        player = new Player(MainActivity.NickName, MainActivity.Session_id, MainActivity.Game_id, MainActivity.Role);

        messageAdapter = new MessageAdapter(list_chat, getContext());
        LV_chat.setAdapter(messageAdapter);
        if (mPage == 1)
        {
            socket.on("get_in_room", onGetInRoom);
            socket.on("user_message", onNewMessage);
            socket.on("leave_room", onLeaveUser);
            socket.on("system_message", onSystemMessage);
            socket.on("ban_user_in_room", OnBanUserInRoom);
            socket.on("host_info", OnHostInfo);
            socket.on("role_action", onRoleAction);
        }
        else
        {
            Log.e("kkk", "onCreateGameChatFragment");
            Log.e("kkk", "num = " + num);
            num = -1;
            Log.e("kkk", "num = " + num);
            socket.on("get_in_room", onGetInDeadRoom);
            socket.on("user_message", onNewDiedMessage);
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
                            if (poisoner) {
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
                                    TV_answerMes.setVisibility(View.GONE);
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
                                    TV_answerMes.setVisibility(View.GONE);
                                    ET_message.setText("");
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
                            TV_answerMes.setVisibility(View.GONE);
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
                TV_answerMes.setVisibility(View.GONE);
                ET_message.setText("");
            }
        });

        LV_chat.setOnScrollListener(new AbsListView.OnScrollListener() {
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                FirstVisibleItem = firstVisibleItem;
                VisibleItemsCount = visibleItemCount;
                TotalItemsCount = totalItemCount;
            }
        });

        LV_chat.setOnItemClickListener((parent, view12, position, id) -> {
            Log.d("kkk", "----");
            Log.d("kkk", "position - " + position + " ' " + list_chat.get(position).mesType);
            Log.d("kkk", "----");
            if(list_chat.get(position).mesType.equals("UsersMes") || list_chat.get(position).mesType.equals("AnswerMes"))
            {
                answer_id = list_chat.get(position).num;
                TV_answerMes.setText(list_chat.get(position).nickName + ": " + list_chat.get(position).message);
                TV_answerMes.setVisibility(View.VISIBLE);
            }
        });

        TV_answerMes.setOnClickListener(v -> {
            answer_id = -1;
            TV_answerMes.setVisibility(View.GONE);
        });
        return view;
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
                        USERS.put(nick, avatar);
                        MessageModel messageModel = new MessageModel(test_num, nick + " вошёл(-а) в чат", time, nick, "ConnectMes", avatar);
                        Log.e("kkk", num + "get_in_room GameChatFragment - " + " Длина listchat = " + list_chat.size() + " /  testnum = " + test_num + " / num = " + num + "/ " + data);
                        boolean good = true;
                        for (int i = 0; i < list_chat.size(); i++)
                        {
                            if (list_chat.get(i).num == test_num)
                            {
                                good = false;
                                break;
                            }
                        }
                        if (test_num != num && good) {
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

    private Emitter.Listener onGetInDeadRoom = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String nick;
                    String avatar;
                    try {

                        nick = data.getString("nick");
                        avatar = data.getString("avatar");
                        USERS.put(nick, avatar);
                        Log.e("kkk", "getInDeadRoom GameChatFragment " + USERS.length() + args[0]);
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
                        status = data.getString("status");
                        if (status.equals("last_message") && nick.equals(player.getNick()))
                        {
                            TV_toDeadChat.setVisibility(View.VISIBLE);
                            IV_toDeadChatArrow.setVisibility(View.VISIBLE);
                            ET_message.setVisibility(View.INVISIBLE);
                            RL_send.setVisibility(View.INVISIBLE);
                            TV_BI.setVisibility(View.INVISIBLE);
                        }
                        if (!status.equals("dead"))
                        {
                            String avatar = "";
                            for (Iterator iterator = USERS.keys(); iterator.hasNext(); ) {
                                nick_from_iterator = (String) iterator.next();
                                if (nick.equals(nick_from_iterator)) {
                                    avatar = USERS.getString(nick);
                                    break;
                                }
                            }
                            test_num = data.getInt("num");
                            Log.d("kkk", "new_message GameChatFragment - " + " Длина listchat = " + list_chat.size() + " /  testnum = " + test_num + " / num = " + num + "/ " + data);
                            boolean good = true;
                            for (int i = 0; i < list_chat.size(); i++)
                            {
                                if (list_chat.get(i).num == test_num)
                                {
                                    good = false;
                                    break;
                                }
                            }
                            if (test_num != num && good) {
                                if (test_num > num) {
                                    num = test_num;
                                    time = data.getString("time");
                                    time = getDate(time);
                                    message = data.getString("message");
                                    link = data.getInt("link");
                                    MessageModel messageModel;
                                    if (link == -1) {
                                        messageModel = new MessageModel(test_num, message, time, nick, "UsersMes", status, main_role, avatar);
                                    } else {
                                        messageModel = new MessageModel(test_num, message, time, nick, "AnswerMes", status, link, main_role, avatar);
                                    }
                                    list_chat.add(messageModel);
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
                                }
                                messageAdapter.notifyDataSetChanged();
                                if (TotalItemsCount < FirstVisibleItem + VisibleItemsCount + 3) {
                                    LV_chat.setSelection(messageAdapter.getCount() - 1);
                                }
                            } else {
                                Log.d("kkk", "Сообщение забанено!");
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

    private Emitter.Listener onNewDiedMessage = new Emitter.Listener() {
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
                        status = data.getString("status");
                        test_num = data.getInt("num");
                        Log.e("kkk", "newDiedMessage GameChatFragment - " + " Длина listchat = " + list_chat.size() + " /  testnum = " + test_num + " / num = " + num + "/ " + data);
                        if (status.equals("dead"))
                        {
                            String avatar = "";
                            Log.e("kkk", String.valueOf(USERS.length()));
                            for (Iterator iterator = USERS.keys(); iterator.hasNext(); ) {
                                nick_from_iterator = (String) iterator.next();
                                if (nick.equals(nick_from_iterator)) {
                                    avatar = USERS.getString(nick);
                                    break;
                                }
                            }
                            if (test_num != num) {
                                if (test_num > num) {
                                    num = test_num;
                                    time = data.getString("time");
                                    time = getDate(time);
                                    message = data.getString("message");
                                    link = data.getInt("link");
                                    MessageModel messageModel;
                                    if (link == -1) {
                                        messageModel = new MessageModel(test_num, message, time, nick, "UsersMes", status, main_role, avatar);
                                    } else {
                                        messageModel = new MessageModel(test_num, message, time, nick, "AnswerMes", status, link, main_role, avatar);
                                    }
                                    Log.e("kkk", "all is good1");
                                    list_chat.add(messageModel);
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
                                    Log.e("kkk", "all is good2");
                                    for (int i = 0; i < list_chat.size(); i++) {
                                        if (test_num < list_chat.get(i).num) {
                                            Log.e("kkk", "all is good3" + i);
                                            list_chat.add(i, messageModel);
                                            break;
                                        }
                                    }
                                }
                                messageAdapter.notifyDataSetChanged();
                                if (TotalItemsCount < FirstVisibleItem + VisibleItemsCount + 3) {
                                    LV_chat.setSelection(messageAdapter.getCount() - 1);
                                }
                            } else {
                                Log.d("kkk", "Сообщение забанено!");
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
                        USERS.remove(nick);
                        Log.d("kkk", "leave_user GameChatFragment - " + " Длина listchat = " + list_chat.size() + " /  testnum = " + test_num + " / num = " + num + "/ " + data);
                    } catch (JSONException e) {
                        return;
                    }
                    String avatar = "";
                    for (Iterator iterator = USERS.keys(); iterator.hasNext();)
                    {
                        nick_from_iterator = (String) iterator.next();
                        if (nick.equals(nick_from_iterator))
                        {
                            try {
                                avatar = USERS.getString(nick);
                            } catch (JSONException e) {

                            }
                            break;
                        }
                    }

                    MessageModel messageModel = new MessageModel(test_num, nick + " вышел(-а) из чата", time, nick, "DisconnectMes", avatar);

                    boolean good = true;
                    for (int i = 0; i < list_chat.size(); i++)
                    {
                        if (list_chat.get(i).num == test_num)
                        {
                            good = false;
                            break;
                        }
                    }
                    if (test_num != num && good) {
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
                        Log.d("kkk", "system message GameChatFragment - " + " Длина listchat = " + list_chat.size() + " /  testnum = " + test_num + " / num = " + num + " , status - " + status + "/" +  data);
                        boolean good = true;
                        for (int i = 0; i < list_chat.size(); i++)
                        {
                            if (list_chat.get(i).num == test_num)
                            {
                                good = false;
                                break;
                            }
                        }
                        if (test_num != num && good) {
                            switch (status)
                            {
                                case "game_over":
                                    TV_toDeadChat.setVisibility(View.VISIBLE);
                                    IV_toDeadChatArrow.setVisibility(View.VISIBLE);
                                    ET_message.setVisibility(View.INVISIBLE);
                                    RL_send.setVisibility(View.INVISIBLE);
                                    TV_BI.setVisibility(View.INVISIBLE);
                                    num = -1;
                                    socket.off("get_in_room");
                                    socket.off("user_message");
                                    socket.off("leave_room");
                                    socket.off("system_message");
                                    socket.off("ban_user_in_room");
                                    socket.off("host_info");
                                    socket.off("role_action");
                                    messageModel = new MessageModel(test_num, message, time, "Server", "SystemMes");
                                    break;
                                case "dead_user":
                                    data2 = data.getJSONObject("message");
                                    message = data2.getString("message");
                                    nick = data2.getString("nick");
                                    messageModel = new MessageModel(test_num, message, time, "Server", "KillMes");
                                    break;
                                case "role_action_mafia":
                                    data2 = data.getJSONObject("message");
                                    mafia_nick = data2.getString("mafia_nick");
                                    user_nick = data2.getString("user_nick");
                                    String avatar = "";
                                    String nick_from_iterator = "";
                                    for (Iterator iterator = USERS.keys(); iterator.hasNext(); ) {
                                        nick_from_iterator = (String) iterator.next();
                                        if (mafia_nick.equals(nick_from_iterator)) {
                                            avatar = USERS.getString(mafia_nick);
                                            break;
                                        }
                                    }
                                    messageModel = new MessageModel(test_num, mafia_nick + " голосует за " + user_nick, time, mafia_nick, "VotingMes", avatar);
                                    break;
                                case "voting":
                                    data2 = data.getJSONObject("message");
                                    voter = data2.getString("voter");
                                    user_nick = data2.getString("user_nick");
                                    String nick_from_iterator2;
                                    String avatar2 = "";
                                    for (Iterator iterator = USERS.keys(); iterator.hasNext();)
                                    {
                                        nick_from_iterator2 = (String) iterator.next();
                                        if (voter.equals(nick_from_iterator2))
                                        {
                                            avatar2 = USERS.getString(voter);
                                            break;
                                        }
                                    }
                                    messageModel = new MessageModel(test_num,voter + " голосует за " + user_nick, time, voter, "VotingMes", avatar2);
                                    break;
                                case "time_info":
                                    messageModel = new MessageModel(test_num, message, time, "Server", "SystemMes");
                                    break;
                                case "journalist":
                                    data2 = data.getJSONObject("message");
                                    message = data2.getString("message");
                                    messageModel = new MessageModel(test_num, message, time, "Server", "JournalistMes");
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
                                LV_chat.setSelection(messageAdapter.getCount() - 1);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener OnBanUserInRoom = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject data = (JSONObject) args[0];
                Log.d("kkk", "принял - ban_user_in_room GameChatFragment - " + data);
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
                MessageModel messageModel = new MessageModel(test_num, player.getHost_nick(), time, nick, "KickMes");

                boolean good = true;
                for (int i = 0; i < list_chat.size(); i++)
                {
                    if (list_chat.get(i).num == test_num)
                    {
                        good = false;
                        break;
                    }
                }
                if (test_num != test_num && good) {
                    if (test_num > num) {
                        num = test_num;
                        list_chat.add(messageModel);
                    } else {
                        for (int i = 0; i < list_chat.size(); i++) {
                            if (test_num > list_chat.get(i).num) {
                                list_chat.add(i, messageModel);
                                break;
                            }
                        }
                    }
                    messageAdapter.notifyDataSetChanged();
                    if (TotalItemsCount < FirstVisibleItem + VisibleItemsCount + 3) {
                        LV_chat.setSelection(messageAdapter.getCount() - 1);
                    }
                }
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
                                break;
                            case "lover":
                                break;
                            case "sheriff":
                                break;
                            case "bodyguard":
                                break;
                            case "poisoner":
                                poisoner = true;
                                break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    /*******************************
     *                             *
     *       SOCKETS end           *
     *                             *
     *******************************/

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