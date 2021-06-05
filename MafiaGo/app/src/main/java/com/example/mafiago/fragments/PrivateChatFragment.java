package com.example.mafiago.fragments;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.mafiago.MainActivity;
import com.example.mafiago.R;
import com.example.mafiago.adapters.FriendsAdapter;
import com.example.mafiago.adapters.MessageAdapter;
import com.example.mafiago.adapters.PrivateMessagesAdapter;
import com.example.mafiago.models.FriendModel;
import com.example.mafiago.models.MessageModel;
import com.example.mafiago.models.PrivateMessageModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.emitter.Emitter;

import static com.example.mafiago.MainActivity.socket;

public class PrivateChatFragment extends Fragment {

    public ListView MessageView;

    public Button btnExit, btnDeleteAnswer;

    public ImageView btnSend;
    public ImageView btnEditMessage;

    public EditText ET_input;

    public RelativeLayout RL_answer;

    public TextView TV_answer_nick, TV_answer_mes;

    public int num = -1;

    ArrayList<PrivateMessageModel> list_messages = new ArrayList<>();
    int answer_id = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_private_chat, container, false);
        MessageView = view.findViewById(R.id.fragmentPrivateChat_list_messages);
        btnExit = view.findViewById(R.id.fragmentPrivateChat_btn_exit);
        btnSend = view.findViewById(R.id.fragmentPrivateChat_btn_send_mes);
        btnEditMessage = view.findViewById(R.id.fragmentPrivateChat_btn_edit);
        btnDeleteAnswer = view.findViewById(R.id.fragmentPrivateChat_btn_delete_answer);
        ET_input = view.findViewById(R.id.fragmentPrivateChat_ET_input);

        RL_answer = view.findViewById(R.id.fragmentPrivateChat_RL_answer);
        TV_answer_nick = view.findViewById(R.id.fragmentPrivateChat_answer_nick);
        TV_answer_mes = view.findViewById(R.id.fragmentPrivateChat_answer_text);

        RL_answer.setVisibility(View.GONE);

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

        socket.on("connect", OnConnect);
        socket.on("disconnect", OnDisconnect);
        socket.on("get_chat_info", OnGetChatInfo);
        socket.on("chat_message", OnChatMessage);
        socket.on("delete_message", OnDeleteMessage);
        socket.on("edit_message", OnEditMessage);

        MessageView.setOnItemClickListener((parent, view12, position, id) -> {
            if (list_messages.get(position).nickName.equals(MainActivity.NickName))
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                View view_menu = getLayoutInflater().inflate(R.layout.item_chat_menu, null);
                builder.setView(view_menu);

                AlertDialog alert = builder.create();

                Button btnAnswer = view_menu.findViewById(R.id.item_chat_menu_btn_answer);
                Button btnEdit = view_menu.findViewById(R.id.item_chat_menu_btn_edit);
                Button btnDelete = view_menu.findViewById(R.id.item_chat_menu_btn_delete);

                btnAnswer.setOnClickListener(v -> {
                    alert.cancel();
                    Log.d("kkk", "----");
                    Log.d("kkk", "position - " + String.valueOf(position));
                    Log.d("kkk", "----");
                    answer_id = position;
                    TV_answer_nick.setText(list_messages.get(position).nickName);
                    TV_answer_mes.setText(list_messages.get(position).message);
                    RL_answer.setVisibility(View.VISIBLE);
                });

                btnEdit.setOnClickListener(v -> {
                    alert.cancel();

                    btnSend.setVisibility(View.INVISIBLE);
                    btnEditMessage.setVisibility(View.VISIBLE);

                    ET_input.setText(list_messages.get(position).message);

                    btnEditMessage.setOnClickListener(v1 -> {
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
                        btnEditMessage.setVisibility(View.GONE);
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



                alert.show();
            }
            else {
                Log.d("kkk", "----");
                Log.d("kkk", "position - " + String.valueOf(position));
                Log.d("kkk", "----");
                answer_id = position;
                TV_answer_nick.setText(list_messages.get(position).nickName);
                TV_answer_mes.setText(list_messages.get(position).message);
                RL_answer.setVisibility(View.VISIBLE);
            }
        });

        btnSend.setOnClickListener(v -> {
            if (!ET_input.getText().toString().equals(""))
            {
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
                    json2.put("message", ET_input.getText());
                    json2.put("link", answer_id);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("kkk", "Socket_отправка chat_message - " + json2.toString());
                socket.emit("chat_message", json2);
                answer_id = -1;
                RL_answer.setVisibility(View.GONE);
                ET_input.setText("");
            }
            else
            {
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

        btnExit.setOnClickListener(v -> {
            MainActivity.User_id_2 = "";
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new FriendsFragment()).commit();
        });

        btnDeleteAnswer.setOnClickListener(v -> {
            RL_answer.setVisibility(View.GONE);
        });

        return view;
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
                    String nick = "", message = "", status = "", edited_time = "", time = "";
                    int link = -1;

                    try {
                        nick = data.getString("nick");
                        num = data.getInt("num");
                        time = data.getString("time");
                        message = data.getString("message");
                        status = data.getString("status");
                        link = data.getInt("link");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if(link == -1)
                    {
                        Log.d("kkk", "UserMes + " + nick + " - " + message);
                        list_messages.add(new PrivateMessageModel(num, message, time.substring(11,16), nick, "UserMes", status));
                        PrivateMessagesAdapter messageAdapter = new PrivateMessagesAdapter(list_messages, getContext());
                        MessageView.setAdapter(messageAdapter);
                        MessageView.setSelection(messageAdapter.getCount() - 1);
                    }
                    else
                    {
                        Log.d("kkk", "AnswerMes ; " + " ; link = " + link);
                        PrivateMessageModel messageModel = new PrivateMessageModel(num, message, time.substring(11,16), nick, "AnswerMes", status, list_messages.get(link).answerNick, list_messages.get(link).message, list_messages.get(link).answerTime, link);
                        list_messages.add(messageModel);
                        PrivateMessagesAdapter messageAdapter = new PrivateMessagesAdapter(list_messages, getContext());
                        MessageView.setAdapter(messageAdapter);
                        MessageView.setSelection(messageAdapter.getCount() - 1);
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
                    String nick = "", message = "", status = "", edited_time = "", time = "";
                    int test_num = -1;

                    //TODO: edited_time довыводить
                    try {
                        test_num = data.getInt("mes_num");
                        time = data.getString("time");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    for (int i = 0; i < list_messages.size(); i++)
                    {
                        if (list_messages.get(i).num == test_num)
                        {
                            list_messages.remove(i);
                            break;
                        }
                    }
                    PrivateMessagesAdapter messageAdapter = new PrivateMessagesAdapter(list_messages, getContext());
                    MessageView.setAdapter(messageAdapter);
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
                    String nick = "", message = "", status = "", edited_time = "", time = "";
                    int test_num = -1;

                    try {
                        test_num = data.getInt("mes_num");
                        message = data.getString("message");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    for (int i = 0; i < list_messages.size(); i++)
                    {
                        if (list_messages.get(i).num == test_num)
                        {
                            list_messages.get(i).message = message;
                            break;
                        }
                    }
                    PrivateMessagesAdapter messageAdapter = new PrivateMessagesAdapter(list_messages, getContext());
                    MessageView.setAdapter(messageAdapter);
                }
            });
        }
    };
}