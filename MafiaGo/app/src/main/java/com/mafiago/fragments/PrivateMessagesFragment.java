package com.mafiago.fragments;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

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

import com.mafiago.MainActivity;
import com.example.mafiago.R;

import com.mafiago.adapters.PrivateMessagesAdapter;
import com.mafiago.classes.OnBackPressedListener;
import com.mafiago.models.PrivateMessageModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.emitter.Emitter;

import static com.mafiago.MainActivity.socket;

public class PrivateMessagesFragment extends Fragment implements OnBackPressedListener {

    public ListView LV_chat;

    public Button btnSend;
    public Button btnEdit;
    public RelativeLayout RL_send;


    public ImageView IV_avatar;
    public ImageView IV_back;

    public EditText ET_input;

    public TextView TV_nick;
    public TextView TV_answer;

    public PrivateMessagesAdapter messageAdapter;

    public int num = -1;

    public int FirstVisibleItem = 0, VisibleItemsCount = 0,TotalItemsCount = 0;

    ArrayList<PrivateMessageModel> list_messages = new ArrayList<>();
    int answer_id = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_private_chat, container, false);
        LV_chat = view.findViewById(R.id.fragmentPrivateChat_LV_chat);
        IV_back = view.findViewById(R.id.fragmentPrivateChat_IV_back);
        ET_input = view.findViewById(R.id.fragmentPrivateChat_ET_message);

        btnEdit = view.findViewById(R.id.fragmentPrivateChat_btn_edit);
        btnSend = view.findViewById(R.id.fragmentPrivateChat_btn_send);
        RL_send = view.findViewById(R.id.fragmentPrivateChat_RL_send);
        TV_nick = view.findViewById(R.id.fragmentPrivateChat_TV_nick);
        TV_answer = view.findViewById(R.id.fragmentPrivateChat_TV_answerMes);
        IV_avatar = view.findViewById(R.id.fragmentPrivateChat_IV_avatar);

        TV_nick.setText(MainActivity.NickName_2);
        IV_avatar.setImageBitmap(MainActivity.bitmap_avatar_2);

        JSONObject json = new JSONObject();
        try {
            json.put("nick", MainActivity.NickName);
            json.put("session_id", MainActivity.Session_id);
            json.put("user_id", MainActivity.User_id);
            json.put("user_id_2", MainActivity.User_id_2);
            json.put("last_message_num", num);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("get_chat", json);
        Log.d("kkk", "Socket_отправка - get_chat - "+ json.toString());

        messageAdapter = new PrivateMessagesAdapter(list_messages, getContext());
        LV_chat.setAdapter(messageAdapter);

        //socket.off("connect");
        //socket.off("disconnect");
        socket.off("get_chat_info");
        socket.off("delete_message");
        socket.off("edit_message");

        socket.on("connect", OnConnect);
        socket.on("disconnect", OnDisconnect);
        socket.on("get_chat_info", OnGetChatInfo);
        socket.on("chat_message", OnChatMessage);
        socket.on("delete_message", OnDeleteMessage);
        socket.on("edit_message", OnEditMessage);

        LV_chat.setOnItemClickListener((parent, view12, position, id) -> {
            if (list_messages.get(position).nickName.equals(MainActivity.NickName))
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                View view_menu = getLayoutInflater().inflate(R.layout.item_chat_menu, null);
                builder.setView(view_menu);

                AlertDialog alert = builder.create();

                Button btnAnswer = view_menu.findViewById(R.id.item_chat_menu_btn_answer);
                Button btnEditMessage = view_menu.findViewById(R.id.item_chat_menu_btn_edit);
                Button btnDelete = view_menu.findViewById(R.id.item_chat_menu_btn_delete);

                btnAnswer.setOnClickListener(v -> {
                    alert.cancel();;
                    answer_id = position;
                    TV_answer.setText(list_messages.get(position).nickName + ": " + list_messages.get(position).message);
                    TV_answer.setVisibility(View.VISIBLE);
                });

                btnEditMessage.setOnClickListener(v -> {
                    alert.cancel();

                    btnEdit.setVisibility(View.VISIBLE);
                    btnSend.setVisibility(View.GONE);

                    ET_input.setText(list_messages.get(position).message);

                    btnEdit.setOnClickListener(v1 -> {
                        JSONObject json2 = new JSONObject();
                        try {
                            json2.put("nick", MainActivity.NickName);
                            json2.put("session_id", MainActivity.Session_id);
                            json2.put("user_id", MainActivity.User_id);
                            json2.put("user_id_2", MainActivity.User_id_2);
                            json2.put("message", ET_input.getText());
                            json2.put("num", list_messages.get(position).num);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        btnSend.setVisibility(View.VISIBLE);
                        btnEdit.setVisibility(View.GONE);
                        ET_input.setText("");
                        socket.emit("edit_message", json2);
                        Log.d("kkk", "Socket_отправка - edit_message - "+ json2.toString());
                    });
                });

                btnDelete.setOnClickListener(v -> {
                    alert.cancel();
                    JSONObject json2 = new JSONObject();
                    try {
                        json2.put("nick", MainActivity.NickName);
                        json2.put("session_id", MainActivity.Session_id);
                        json2.put("user_id", MainActivity.User_id);
                        json2.put("user_id_2", MainActivity.User_id_2);
                        json2.put("num", list_messages.get(position).num);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    socket.emit("delete_message", json2);
                    Log.d("kkk", "Socket_отправка - delete_message - "+ json2.toString());
                });
                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alert.show();
            }
            else {
                answer_id = position;
                TV_answer.setText(list_messages.get(position).nickName + ": " + list_messages.get(position).message);
                TV_answer.setVisibility(View.VISIBLE);
            }
        });

        btnSend.setOnClickListener(v -> {
            String input = String.valueOf(ET_input.getText());
            if (input.length() > 700) {
                input = input.substring(0, 701);
            }
            if (!input.equals("") && !input.equals("/n")) {
                final Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.bounce);

                // amplitude 0.2 and frequency 20
                BounceInterpolator interpolator = new BounceInterpolator();
                animation.setInterpolator(interpolator);

                btnSend.startAnimation(animation);
                final JSONObject json2 = new JSONObject();
                try {
                    json2.put("nick", MainActivity.NickName);
                    json2.put("session_id", MainActivity.Session_id);
                    json2.put("user_id", MainActivity.User_id);
                    json2.put("user_id_2", MainActivity.User_id_2);
                    json2.put("message", input);
                    json2.put("link", answer_id);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("kkk", "Socket_отправка chat_message - " + json2.toString());
                socket.emit("chat_message", json2);
                answer_id = -1;
                TV_answer.setVisibility(View.GONE);
                ET_input.setText("");
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
        });

        IV_back.setOnClickListener(v -> {
            MainActivity.User_id_2 = "";
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new PrivateChatsFragment()).commit();
        });

        TV_answer.setOnClickListener(v -> {
            answer_id = -1;
            TV_answer.setVisibility(View.GONE);
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

        return view;
    }

    @Override
    public void onBackPressed() {
        MainActivity.User_id_2 = "";
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new PrivateChatsFragment()).commit();
    }

    private Emitter.Listener OnConnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject json = new JSONObject();
                    try {
                        json.put("nick", MainActivity.NickName);
                        json.put("session_id", MainActivity.Session_id);
                        json.put("user_id", MainActivity.User_id);
                        json.put("user_id_2", MainActivity.User_id_2);
                        json.put("last_message_num", num);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    socket.emit("get_chat", json);
                    Log.d("kkk", "CONNECT");
                }
            });
        }
    };

    private Emitter.Listener OnDisconnect = new Emitter.Listener() {
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

    private Emitter.Listener OnGetChatInfo = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];


                    Log.d("kkk", "принял - get_chat_info - " + data);
                }
            });
        }
    };

    private Emitter.Listener OnChatMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.d("kkk", "принял - chat_message - " + data);
                    String nick = "", message = "", status = "", edited_time = "", time = "", user_id_2 = "";
                    Boolean is_read = false;
                    int link = -1;
                    try {
                        nick = data.getString("nick");
                        num = data.getInt("num");
                        if (data.has("is_read")) {
                            is_read = data.getBoolean("is_read");
                        }
                        else
                        {
                            is_read = true;
                        }
                        user_id_2 = data.getString("user_id");
                        time = data.getString("time");
                        message = data.getString("message");
                        status = data.getString("status");
                        link = data.getInt("link");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (MainActivity.User_id_2.equals(user_id_2) || MainActivity.NickName.equals(nick)) {
                        if (link == -1) {
                            Log.d("kkk", "UserMes + " + nick + " - " + message);
                            list_messages.add(new PrivateMessageModel(num, message, time.substring(11, 16), nick, "UserMes", status, is_read));
                        } else {
                            Log.d("kkk", "AnswerMes ; " + " ; link = " + link);
                            list_messages.add(new PrivateMessageModel(num, message, time.substring(11, 16), nick, "AnswerMes", status, list_messages.get(link).answerNick, list_messages.get(link).message, list_messages.get(link).answerTime, link, is_read));
                        }
                        messageAdapter.notifyDataSetChanged();
                        if (TotalItemsCount < FirstVisibleItem + VisibleItemsCount + 2) {
                            LV_chat.setSelection(messageAdapter.getCount() - 1);
                        }
                    }
                    else
                    {
                        Log.d("kkk", "GG");
                    }
                }
            });
        }
    };

    private Emitter.Listener OnDeleteMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.d("kkk", "принял - delete_message - " + data);
                    String nick = "", message = "", status = "", edited_time = "", time = "", user_id_1 = "", user_id_2 = "";
                    int test_num = -1;

                    //TODO: edited_time довыводить
                    try {
                        user_id_1 = data.getString("user_id");
                        user_id_2 = data.getString("user_id_2");
                        test_num = data.getInt("mes_num");
                        time = data.getString("time");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if ((user_id_1.equals(MainActivity.User_id) && user_id_2.equals(MainActivity.User_id_2))
                            || (user_id_2.equals(MainActivity.User_id) && user_id_1.equals(MainActivity.User_id_2))) {
                        for (int i = 0; i < list_messages.size(); i++) {
                            if (list_messages.get(i).num == test_num) {
                                list_messages.remove(i);
                                break;
                            }
                        }
                        messageAdapter.notifyDataSetChanged();
                    }
                    else
                    {
                        Log.d("kkk", "GG");
                    }
                }
            });
        }
    };

    private Emitter.Listener OnEditMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.d("kkk", "принял - edit_message - " + data);
                    String nick = "", message = "", status = "", edited_time = "", time = "", user_id_1 = "", user_id_2 = "";
                    Boolean is_read = false, modifications_for_message = false;
                    int test_num = -1;
                    try {
                        if (data.has("is_read")) {
                            is_read = data.getBoolean("is_read");
                        }
                        else
                        {
                            is_read = false;
                        }
                        user_id_1 = data.getString("user_id");
                        user_id_2 = data.getString("user_id_2");
                        test_num = data.getInt("mes_num");
                        if (data.has("message"))
                        {
                            message = data.getString("message");
                            modifications_for_message = true;
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if ((user_id_1.equals(MainActivity.User_id) && user_id_2.equals(MainActivity.User_id_2))
                            || (user_id_2.equals(MainActivity.User_id) && user_id_1.equals(MainActivity.User_id_2))) {
                        for (int i = 0; i < list_messages.size(); i++) {
                            if (list_messages.get(i).num == test_num) {
                                if (modifications_for_message)
                                {
                                    list_messages.get(i).message = message;
                                }
                                list_messages.get(i).is_read = is_read;
                                break;
                            }
                        }
                        messageAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    };
}