package com.mafiago.small_fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.inputmethod.InputMethodManager;
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
import com.mafiago.enums.Role;
import com.mafiago.enums.Time;
import com.mafiago.models.MessageModel;
import com.mafiago.models.Player;
import com.mafiago.models.UserModel;

import org.json.JSONArray;
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
    ArrayList<UserModel> list_users = new ArrayList<>();

    public int num = -1;
    public boolean Once = true;

    ListView LV_chat;
    public EditText ET_message;
    Button btnSend;
    RelativeLayout RL_send;
    TextView TV_answerMes;
    TextView TV_toDeadChat;
    TextView TV_deadChat;
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
        View view;
        if (mPage == 1)
        {
            view = inflater.inflate(R.layout.item_chat, container, false);
            LV_chat = view.findViewById(R.id.itemChat_LV_chat);
            ET_message = view.findViewById(R.id.itemChat_ET_message);
            RL_send = view.findViewById(R.id.itemChat_RL_send);
            btnSend = view.findViewById(R.id.itemChat_btn_send);
            TV_answerMes = view.findViewById(R.id.itemChat_TV_answerMes);
            TV_toDeadChat = view.findViewById(R.id.itemChat_TV_toDeadChat);
            TV_deadChat = view.findViewById(R.id.itemChat_TV_deadChat);
            TV_BI = view.findViewById(R.id.itemChat_TV_BI);
            IV_toDeadChatArrow = view.findViewById(R.id.itemChat_TV_toDeadChatArrow);

            socket.on("get_in_room", onGetInRoom);
            socket.on("user_message", onNewMessage);
            socket.on("leave_room", onLeaveUser);
            socket.on("system_message", onSystemMessage);
            socket.on("ban_user_in_room", OnBanUserInRoom);
            socket.on("host_info", OnHostInfo);
            socket.on("role_action", onRoleAction);
            socket.on("success_get_in_room_observer", onSuccessGetInRoomObserver);
            socket.on("time", onTime);
            socket.on("role", onRole);
            socket.on("get_my_game_info", onGetMyGameInfo);
        }
        else
        {
            view = inflater.inflate(R.layout.item_chat, container, false);
            LV_chat = view.findViewById(R.id.itemChat_LV_chat);
            ET_message = view.findViewById(R.id.itemChat_ET_message);
            RL_send = view.findViewById(R.id.itemChat_RL_send);
            btnSend = view.findViewById(R.id.itemChat_btn_send);
            TV_answerMes = view.findViewById(R.id.itemChat_TV_answerMes);
            TV_toDeadChat = view.findViewById(R.id.itemChat_TV_toDeadChat);
            TV_deadChat = view.findViewById(R.id.itemChat_TV_deadChat);
            TV_BI = view.findViewById(R.id.itemChat_TV_BI);
            IV_toDeadChatArrow = view.findViewById(R.id.itemChat_TV_toDeadChatArrow);

            num = -1;
            Log.e("kkk", "2");
            socket.on("get_in_room", onGetInDeadRoom);
            socket.on("user_message", onNewDiedMessage);
            TV_deadChat.setVisibility(View.VISIBLE);
            TV_BI.setVisibility(View.GONE);
            RL_send.setVisibility(View.GONE);
            ET_message.setVisibility(View.GONE);
            RL_send.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.died_button));
        }

        player = new Player(MainActivity.NickName, MainActivity.Session_id, MainActivity.Game_id, MainActivity.Role);

        messageAdapter = new MessageAdapter(list_chat, getContext());
        LV_chat.setAdapter(messageAdapter);

        RL_send.setOnClickListener(v -> {
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
            if (player.getStatus().equals("alive") || player.getTime() == Time.LOBBY) {
                if (player.Can_write()) {
                    if (!input.equals("") && !input.equals("/n")) {
                        if (player.getTime() == Time.DAY) {
                            if (!poisoner) {
                                if (messages_can_write > 0) {
                                    messages_can_write--;

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
                                    Log.d("kkk", "Socket_???????????????? user_message - " + json2.toString());
                                    socket.emit("user_message", json2);
                                    answer_id = -1;
                                    TV_answerMes.setVisibility(View.GONE);
                                    ET_message.setText("");

                                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                                    //Find the currently focused view, so we can grab the correct window token from it.
                                    View view2 = getActivity().getCurrentFocus();
                                    //If no view currently has focus, create a new one, just so we can grab a window token from it
                                    if (view2 == null) {
                                        view2 = new View(getActivity());
                                    }
                                    imm.hideSoftInputFromWindow(view2.getWindowToken(), 0);
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                    builder.setTitle("???? ???? ???????????? ??????????!")
                                            .setMessage("???????????? ???????????????????? ???????????? 10 ?????????????????? ?? ????????!")
                                            .setIcon(R.drawable.ic_error)
                                            .setCancelable(false)
                                            .setNegativeButton("????",
                                                    (dialog, id) -> dialog.cancel());
                                    AlertDialog alert = builder.create();
                                    alert.show();
                                }

                            } else {
                                if (messages_can_write == 10) {
                                    messages_can_write--;

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
                                    Log.d("kkk", "Socket_???????????????? user_message - " + json2.toString());
                                    socket.emit("user_message", json2);
                                    answer_id = -1;
                                    TV_answerMes.setVisibility(View.GONE);
                                    ET_message.setText("");

                                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                                    //Find the currently focused view, so we can grab the correct window token from it.
                                    View view2 = getActivity().getCurrentFocus();
                                    //If no view currently has focus, create a new one, just so we can grab a window token from it
                                    if (view2 == null) {
                                        view2 = new View(getActivity());
                                    }
                                    imm.hideSoftInputFromWindow(view2.getWindowToken(), 0);
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                    builder.setTitle("?????? ????????????????!")
                                            .setMessage("???? ???? ???????????? ???????????? ???????????? 1 ?????????????????? ?? ????????!")
                                            .setIcon(R.drawable.ic_error)
                                            .setCancelable(false)
                                            .setNegativeButton("????",
                                                    (dialog, id) -> dialog.cancel());
                                    AlertDialog alert = builder.create();
                                    alert.show();
                                }
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
                            Log.d("kkk", "Socket_???????????????? user_message - " + json2.toString());
                            socket.emit("user_message", json2);
                            answer_id = -1;
                            TV_answerMes.setVisibility(View.GONE);
                            ET_message.setText("");

                            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                            //Find the currently focused view, so we can grab the correct window token from it.
                            View view2 = getActivity().getCurrentFocus();
                            //If no view currently has focus, create a new one, just so we can grab a window token from it
                            if (view2 == null) {
                                view2 = new View(getActivity());
                            }
                            imm.hideSoftInputFromWindow(view2.getWindowToken(), 0);
                        }
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("????????????!")
                                .setMessage("???????????? ???????????????????? ???????????? ??????????????????!")
                                .setIcon(R.drawable.ic_error)
                                .setCancelable(false)
                                .setNegativeButton("????",
                                        (dialog, id) -> dialog.cancel());
                        AlertDialog alert = builder.create();
                        alert.show();

                    }
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("???? ???? ???????????? ???????????? ???????????????????? ??????????????????!")
                            .setMessage("")
                            .setIcon(R.drawable.ic_error)
                            .setCancelable(false)
                            .setNegativeButton("????",
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
                Log.d("kkk", "Socket_???????????????? user_message - " + json2.toString());
                socket.emit("user_message", json2);
                answer_id = -1;
                TV_answerMes.setVisibility(View.GONE);
                ET_message.setText("");
                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
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
            if(list_chat.get(position).mesType.equals("UsersMes") || list_chat.get(position).mesType.equals("AnswerMes"))
            {
                if (player.getStatus().equals("alive") || mPage == 2) {
                    answer_id = list_chat.get(position).num;
                    TV_answerMes.setText(list_chat.get(position).nickName + ": " + list_chat.get(position).message);
                    TV_answerMes.setVisibility(View.VISIBLE);
                }
            }
        });

        TV_answerMes.setOnClickListener(v -> {
            answer_id = -1;
            TV_answerMes.setVisibility(View.GONE);
        });
        return view;
    }

    public Bitmap fromBase64(String image) {
        // ???????????????????? ???????????? Base64 ?? ???????????? ????????????
        byte[] decodedString = Base64.decode(image, Base64.DEFAULT);

        // ???????????????????? ???????????? ???????????? ?? ??????????????????????
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        // ???????????????? ?????????????????????? ?? ImageView
        return decodedByte;
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
                    //Log.d("kkk", "???????????? - get_in_room: " + args[0]);
                    JSONObject data = (JSONObject) args[0];
                    String nick;
                    String time;
                    String avatar;
                    String status;
                    String user_color;
                    int test_num;
                    int room_id;
                    try {
                        room_id = data.getInt("room");
                        if (room_id == player.getRoom_num()) {
                            test_num = data.getInt("num");
                            status = data.getString("user_status");
                            time = data.getString("time");
                            time = getDate(time);
                            nick = data.getString("nick");
                            avatar = data.getString("avatar");
                            user_color = data.getString("user_color");
                            list_users.add(new UserModel(nick, avatar, status, user_color));
                            Log.e("kkk", "GF, ???????????????? ????????????????????????");
                            MessageModel messageModel = new MessageModel(test_num, nick + " ??????????(-??) ?? ??????", time, nick, "ConnectMes", fromBase64(avatar));
                            Log.d("kkk", num + "get_in_room GameChatFragment - " + " ?????????? listchat = " + list_chat.size() + " /  testnum = " + test_num + " / num = " + num + "/ " + data);
                            if (test_num != num) {
                                for (int i = 0; i < list_chat.size(); i++) {
                                    if (list_chat.get(i).message.equals(nick + " ??????????(-??) ???? ????????")) {
                                        list_chat.remove(i);
                                        Log.e("g", "GCF, i = " + i + " ???????????? ?????????????????? ?? ????????????");
                                        break;
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
                                        list_chat.get(i).avatar = fromBase64(avatar);
                                        list_chat.get(i).status_text = status;
                                        list_chat.get(i).user_color = user_color;
                                    }
                                }
                                messageAdapter.notifyDataSetChanged();
                                if (TotalItemsCount < FirstVisibleItem + VisibleItemsCount + 3) {
                                    LV_chat.setSelection(messageAdapter.getCount() - 1);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
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
                    String status;
                    String user_color;
                    try {

                        nick = data.getString("nick");
                        avatar = data.getString("avatar");
                        status = data.getString("user_status");
                        user_color = data.getString("user_color");
                        list_users.add(new UserModel(nick, avatar, status, user_color));
                        Log.e("kkk", "getInDeadRoom GameChatFragment " + user_color + args[0]);
                    } catch (JSONException e) {
                        e.printStackTrace();
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
                    String status_text = "";
                    String user_color = "";
                    String nick_from_iterator;
                    int test_num;
                    int room_id;
                    int link;
                    try {
                        //status_text = data.getString("role");
                        nick = data.getString("nick");
                        status = data.getString("status");
                        room_id = data.getInt("room");
                        if (room_id == player.getRoom_num()) {
                            if (status.equals("last_message") && nick.equals(player.getNick())) {
                                TV_toDeadChat.setVisibility(View.VISIBLE);
                                IV_toDeadChatArrow.setVisibility(View.VISIBLE);
                                ET_message.setVisibility(View.INVISIBLE);
                                RL_send.setVisibility(View.INVISIBLE);
                                TV_BI.setVisibility(View.INVISIBLE);
                                answer_id = -1;
                                player.setStatus("dead");
                                TV_answerMes.setVisibility(View.GONE);
                            }
                            if (!status.equals("dead")) {
                                String avatar = "";
                                for (int i = 0; i < list_users.size(); i++) {
                                    if (nick.equals(list_users.get(i).getNick())) {
                                        avatar = list_users.get(i).getAvatar();
                                        status_text = list_users.get(i).status;
                                        user_color = list_users.get(i).user_color;
                                        break;
                                    }
                                }
                                test_num = data.getInt("num");
                                Log.d("kkk", "new_message GameChatFragment - " + " ?????????? listchat = " + list_chat.size() + " /  testnum = " + test_num + " / num = " + num + "/ " + data);
                                boolean good = true;
                                for (int i = 0; i < list_chat.size(); i++) {
                                    if (list_chat.get(i).num == test_num) {
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
                                            messageModel = new MessageModel(test_num, message, time, nick, "UsersMes", status, status_text, fromBase64(avatar), user_color);
                                        } else {
                                            messageModel = new MessageModel(test_num, message, time, nick, "AnswerMes", status, link, status_text, fromBase64(avatar), user_color);
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
                                            messageModel = new MessageModel(test_num, message, time, nick, "UsersMes", status, status_text, fromBase64(avatar), user_color);
                                        } else {
                                            messageModel = new MessageModel(test_num, message, time, nick, "AnswerMes", status, link, status_text, fromBase64(avatar), user_color);
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
                                    Log.d("kkk", "?????????????????? ????????????????!");
                                }
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
                    String status_text = "";
                    String user_color = "";
                    String nick_from_iterator;
                    int test_num;
                    int link;
                    try {
                        //status_text = data.getString("role");
                        nick = data.getString("nick");
                        status = data.getString("status");
                        test_num = data.getInt("num");
                        Log.e("kkk", "newDiedMessage GameChatFragment - " + " ?????????? listchat = " + list_chat.size() + " /  testnum = " + test_num + " / num = " + num + "/ " + data);
                        if (status.equals("last_message") && nick.equals(player.getNick()))
                        {
                            TV_deadChat.setVisibility(View.GONE);
                            TV_BI.setVisibility(View.VISIBLE);
                            RL_send.setVisibility(View.VISIBLE);
                            ET_message.setVisibility(View.VISIBLE);
                            ET_message.setText("");
                        }
                        if (status.equals("dead"))
                        {
                            String avatar = "";
                            for (int i = 0; i < list_users.size(); i++) {
                                if (nick.equals(list_users.get(i).getNick())) {
                                    avatar = list_users.get(i).getAvatar();
                                    status_text = list_users.get(i).status;
                                    user_color = list_users.get(i).user_color;
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
                                        messageModel = new MessageModel(test_num, message, time, nick, "UsersMes", status, status_text, fromBase64(avatar), user_color);
                                    } else {
                                        messageModel = new MessageModel(test_num, message, time, nick, "AnswerMes", status, link, status_text, fromBase64(avatar), user_color);
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
                                        messageModel = new MessageModel(test_num, message, time, nick, "UsersMes", status, status_text, fromBase64(avatar), user_color);
                                    } else {
                                        messageModel = new MessageModel(test_num, message, time, nick, "AnswerMes", status, link, status_text, fromBase64(avatar), user_color);
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
                                Log.d("kkk", "?????????????????? ????????????????!");
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
                    int room_id = -1;
                    String nick_from_iterator;
                    try {
                        room_id = data.getInt("room");
                        test_num = data.getInt("num");
                        nick = data.getString("nick");
                        time = data.getString("time");
                        time = getDate(time);
                        Log.d("kkk", "leave_user GameChatFragment - " + " ?????????? listchat = " + list_chat.size() + " /  testnum = " + test_num + " / num = " + num + "/ " + data);
                    } catch (JSONException e) {
                        return;
                    }
                    if (room_id == player.getRoom_num()) {
                        String avatar = "";

                        MessageModel messageModel = new MessageModel(test_num, nick + " ??????????(-??) ???? ????????", time, nick, "DisconnectMes", fromBase64(avatar));

                        if (test_num != num) {
                            for (int i = 0; i < list_chat.size(); i++) {
                                if (list_chat.get(i).message.equals(nick + " ??????????(-??) ?? ??????")) {
                                    list_chat.remove(i);
                                    break;
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
                            for (int i = 0; i < list_users.size(); i++)
                            {
                                if (list_users.get(i).getNick().equals(nick))
                                {
                                    list_users.remove(i);
                                    break;
                                }
                            }

                            messageAdapter.notifyDataSetChanged();
                            if (TotalItemsCount < FirstVisibleItem + VisibleItemsCount + 3) {
                                LV_chat.setSelection(messageAdapter.getCount() - 1);
                            }
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
                    int test_num, room_id;
                    JSONObject data2;
                    Log.d("kkk", "system message GameChatFragment - " + data);
                    try {
                        room_id = data.getInt("room");
                        if (room_id == player.getRoom_num()) {
                            status = data.getString("status");
                            test_num = data.getInt("num");
                            time = data.getString("time");
                            time = getDate(time);
                            message = data.getString("message");
                            MessageModel messageModel = new MessageModel(test_num, "???????????? ???????????? ??????????????????", time, "Server", "SystemMes");
                            boolean good = true;
                            for (int i = 0; i < list_chat.size(); i++) {
                                if (list_chat.get(i).num == test_num) {
                                    good = false;
                                    break;
                                }
                            }
                            if (test_num != num && good) {
                                switch (status) {
                                    case "game_over":
                                        TV_toDeadChat.setVisibility(View.VISIBLE);
                                        IV_toDeadChatArrow.setVisibility(View.VISIBLE);
                                        ET_message.setVisibility(View.INVISIBLE);
                                        RL_send.setVisibility(View.INVISIBLE);
                                        TV_BI.setVisibility(View.INVISIBLE);
                                        num = -1;
                                        answer_id = -1;
                                        TV_answerMes.setVisibility(View.GONE);
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
                                        if (nick.equals(player.getNick())) {
                                            player.setCan_write(true);
                                            player.setStatus("dead");
                                        }
                                        if (data2.has("nick_2")) {
                                            if (data2.getString("nick_2").equals(player.getNick())) {
                                                player.setCan_write(true);
                                                player.setStatus("dead");
                                            }
                                        }
                                        messageModel = new MessageModel(test_num, message, time, "Server", "KillMes");
                                        break;
                                    case "role_action_mafia":
                                        data2 = data.getJSONObject("message");
                                        mafia_nick = data2.getString("mafia_nick");
                                        user_nick = data2.getString("user_nick");
                                        String avatar = "";

                                        String status_text = "";
                                        for (int i = 0; i < list_users.size(); i++) {
                                            if (mafia_nick.equals(list_users.get(i).getNick())) {
                                                avatar = list_users.get(i).getAvatar();
                                                status_text = list_users.get(i).status;
                                                break;
                                            }
                                        }
                                        messageModel = new MessageModel(test_num, "???????????????? ???? " + user_nick, time, mafia_nick, "VotingMes", fromBase64(avatar));
                                        break;
                                    case "voting":
                                        data2 = data.getJSONObject("message");
                                        voter = data2.getString("voter");
                                        user_nick = data2.getString("user_nick");
                                        avatar = "";
                                        status_text = "";
                                        for (int i = 0; i < list_users.size(); i++) {
                                            if (voter.equals(list_users.get(i).getNick())) {
                                                avatar = list_users.get(i).getAvatar();
                                                status_text = list_users.get(i).status;
                                                break;
                                            }
                                        }
                                        messageModel = new MessageModel(test_num, "???????????????? ???? " + user_nick, time, voter, "VotingMes", fromBase64(avatar));
                                        break;
                                    case "time_info":
                                        messageModel = new MessageModel(test_num, message, time, "Server", "SystemMes");
                                        break;
                                    case "journalist":
                                        data2 = data.getJSONObject("message");
                                        message = data2.getString("message");
                                        messageModel = new MessageModel(test_num, message, time, "Server", "JournalistMes");
                                        break;
                                    case "re_voting":
                                        message = data.getString("message");
                                        messageModel = new MessageModel(test_num, message, time, "Server", "SystemMes");
                                        break;
                                }

                                //???????? num ???? data ???????????? ???????????? num, ???? ???????????? ?????????????????? ?????????????????? ?? ???????????? ???? 1 ??????????, else ?????????????????? ?????????????????? ???? ???????????? ??????????

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
                Log.d("kkk", "???????????? - ban_user_in_room GameChatFragment - " + data);
                String nick = "";
                String host_nick = "";
                String time = "";
                int test_num = -1;
                try {
                    test_num = data.getInt("num");
                    nick = data.getString("nick");
                    host_nick = data.getString("host_nick");
                    time = data.getString("time");
                    time = getDate(time);
                } catch (JSONException e) {
                    return;
                }
                MessageModel messageModel = new MessageModel(test_num, host_nick + " ????????????(-??) " + nick, time, nick, "KickMes");

                boolean good = true;
                for (int i = 0; i < list_chat.size(); i++)
                {
                    if (list_chat.get(i).num == test_num)
                    {
                        good = false;
                        break;
                    }
                }
                if (num != test_num && good) {
                    for (int i = 0; i < list_chat.size(); i++) {
                        if (list_chat.get(i).message.equals(nick + " ??????????(-??) ?? ??????")) {
                            list_chat.remove(i);
                        }
                    }
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

    private Emitter.Listener OnHostInfo = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject data = (JSONObject) args[0];
                Log.d("kkk", "???????????? - host_info - " + data);
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

    private Emitter.Listener onRoleAction = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject data = (JSONObject) args[0];
                String role;
                try {
                    role = data.getString("role");
                    Log.d("kkk", "Socket_?????????????? - role_action " + args[0]);
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
    };

    private Emitter.Listener onSuccessGetInRoomObserver = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ET_message.setVisibility(View.INVISIBLE);
                    RL_send.setVisibility(View.INVISIBLE);
                    TV_BI.setVisibility(View.INVISIBLE);
                    player.is_observer = true;
                }
            });
        }
    };

    //?????????????????? ?????????? ??????
    private Emitter.Listener onTime = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(() -> {
                JSONObject data = (JSONObject) args[0];
                String time;
                int room_id;
                try {
                    room_id = data.getInt("room");
                    if (room_id == player.getRoom_num()) {
                        time = data.getString("time");
                        switch (time) {
                            case "lobby":
                                player.setTime(Time.LOBBY);
                                break;
                            case "night_love":
                                player.setTime(Time.NIGHT_LOVE);
                                break;
                            case "night_other":
                                player.setTime(Time.NIGHT_OTHER);
                                break;
                            case "day":
                                player.setTime(Time.DAY);
                                messages_can_write = 10;
                                break;
                            case "voting":
                                messages_can_write = 10;
                                player.setTime(Time.VOTING);
                                break;
                        }

                        player.setCan_write(false);
                        if (player.getStatus().equals("alive")) {
                            switch (player.getTime()) {
                                case NIGHT_LOVE:
                                    switch (player.getRole()) {
                                        case MAFIA:
                                            player.setCan_write(true);
                                            break;
                                        case MAFIA_DON:
                                            player.setCan_write(true);
                                            break;
                                    }
                                    break;
                                case NIGHT_OTHER:
                                    break;
                                case DAY:
                                    player.setCan_write(true);
                                    break;
                                case VOTING:
                                    break;
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        }
    };

    private Emitter.Listener onGetMyGameInfo = new Emitter.Listener()  {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!player.is_observer) {
                        JSONObject data = (JSONObject) args[0];
                        String role = "", status = "", time = "", nick;
                        boolean can_act = false, can_vote = false, last_message = false, can_skip_day = true;
                        boolean sheriff = false, doctor = false, lover = false, bodyguard = false, poisoner = false;
                        int voted_number;
                        Log.d("kkk", "Socket_?????????????? - get_my_game_info in GameChat Fragment - " + args[0]);
                        JSONObject data2;
                        JSONObject influences;
                        try {
                            if (data.has("messages_counter")) {
                                messages_can_write = data.getInt("messages_counter");
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
                            player.healed_yourself = data.getBoolean("heal_yourself");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        switch (time) {
                            case "lobby":
                                player.setTime(Time.LOBBY);
                                break;
                            case "night_love":
                                player.setTime(Time.NIGHT_LOVE);
                                break;
                            case "night_other":
                                player.setTime(Time.NIGHT_OTHER);
                                break;
                            case "day":
                                player.setTime(Time.DAY);
                                break;
                            case "voting":
                                player.setTime(Time.VOTING);
                                break;
                        }

                        player.setRole(ConvertToRole(role));
                        player.setStatus(status);

                        if (player.getStatus().equals("alive")) {
                            player.setCan_write(false);
                            switch (player.getTime()) {
                                case NIGHT_LOVE:
                                    switch (player.getRole()) {
                                        case MAFIA:
                                            player.setCan_write(true);
                                            break;
                                        case MAFIA_DON:
                                            player.setCan_write(true);
                                            break;
                                    }
                                    break;
                                case NIGHT_OTHER:
                                    break;
                                case DAY:
                                    player.setCan_write(true);
                                    break;
                                case VOTING:
                            }
                        } else {
                            player.setCan_write(true);
                        }
                    }
                }
            });
        }
    };

    //?????????????????? ????????
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
                        player.setRole(ConvertToRole(role));
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

    //???????????????????????????? String ?? Role
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
}