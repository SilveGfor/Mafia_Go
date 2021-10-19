package com.mafiago.fragments;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mafiago.MainActivity;
import com.example.mafiago.R;
import com.mafiago.adapters.PrivateChatsAdapter;
import com.mafiago.classes.OnBackPressedListener;
import com.mafiago.models.PrivateChatModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.emitter.Emitter;

import static com.mafiago.MainActivity.bitmap_avatar_2;
import static com.mafiago.MainActivity.socket;

public class PrivateChatsFragment extends Fragment implements OnBackPressedListener {

    public ListView friendsView;

    public TextView TV_no_chats;

    public ProgressBar PB_loading;
    public ImageView Menu;
    RelativeLayout btn_back;

    public PrivateChatsAdapter privateChatsAdapter;

    ArrayList<PrivateChatModel> list_friends = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_private_chats, container, false);
        friendsView = view.findViewById(R.id.fragmentPrivateChat_list_friends);
        TV_no_chats = view.findViewById(R.id.fragmentPrivateChat_TV_no_chats);
        PB_loading = view.findViewById(R.id.fragmentPrivateChat_PB_loading);
        btn_back = view.findViewById(R.id.fragmentGamesList_RL_back);
        Menu = view.findViewById(R.id.fragmentMenu_IV_menu);

        Menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup_menu = new PopupMenu(getActivity(), Menu);
                popup_menu.inflate(R.menu.main_menu);
                popup_menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.mainMenu_play:
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
                                return true;
                            case R.id.mainMenu_shop:
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new ShopFragment()).commit();
                                return true;
                            case R.id.mainMenu_friends:
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new FriendsFragment()).commit();
                                return true;
                            case R.id.mainMenu_chats:
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new PrivateChatsFragment()).commit();
                                return true;
                            case R.id.mainMenu_settings:
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new SettingsFragment()).commit();
                                return true;
                        }
                        return true;
                    }
                });
                popup_menu.show();
            }
        });

        privateChatsAdapter = new PrivateChatsAdapter(list_friends, getContext());
        friendsView.setAdapter(privateChatsAdapter);

        PB_loading.setVisibility(View.VISIBLE);
        TV_no_chats.setVisibility(View.GONE);

        socket.off("add_chat_to_list_of_chats");
        socket.off("add_chat_to_list_of_blocked_chats");

        socket.on("add_chat_to_list_of_chats", OnAddChatToListOfChats);
        socket.on("add_chat_to_list_of_blocked_chats", OnAddChatToListOfBlockedChats);

        JSONObject json = new JSONObject();
        try {
            json.put("nick", MainActivity.NickName);
            json.put("session_id", MainActivity.Session_id);
            json.put("user_id", MainActivity.User_id);
            json.put("chat_type", "active");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("get_list_of_chats", json);
        Log.d("kkk", "Socket_отправка - get_list_of_chats - "+ json.toString());

        JSONObject json2 = new JSONObject();
        try {
            json2.put("chat_type", "blocked");
            json2.put("nick", MainActivity.NickName);
            json2.put("session_id", MainActivity.Session_id);
            json2.put("user_id", MainActivity.User_id);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("get_list_of_chats", json2);
        Log.d("kkk", "Socket_отправка - get_list_of_chats - "+ json2.toString());

        friendsView.setOnItemClickListener((parent, view1, position, id) -> {
            MainActivity.User_id_2 = list_friends.get(position).getUser_id_2();
            MainActivity.NickName_2 = list_friends.get(position).getNick();
            MainActivity.bitmap_avatar_2 = fromBase64(list_friends.get(position).getAvatar());
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new PrivateMessagesFragment()).commit();
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new MenuFragment()).commit();
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
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new MenuFragment()).commit();
    }

    private final Emitter.Listener OnAddChatToListOfChats = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PB_loading.setVisibility(View.GONE);
                if (args.length != 0) {
                    TV_no_chats.setVisibility(View.GONE);
                    JSONObject data = (JSONObject) args[0];
                    Log.d("kkk", "принял - get_list_of_chats - " + data);
                    String nick = "", user_id_1 = "", user_id_2 = "", message = "", avatar = "";
                    boolean online = false, my_message = false, is_read = true;
                    try {
                        JSONArray user_ids = data.getJSONArray("user_ids");
                        user_id_1 = user_ids.getString(0);
                        user_id_2 = user_ids.getString(1);

                        JSONObject user_nicks = data.getJSONObject("user_nicks");
                        if (!user_id_1.equals(MainActivity.User_id)) {
                            String test_id = user_id_1;
                            user_id_1 = user_id_2;
                            user_id_2 = test_id;
                        }
                        online = data.getBoolean("is_online");
                        nick = user_nicks.getString(user_id_2);
                        avatar = data.getString("avatar");

                        JSONObject last_message = data.getJSONObject("last_message");
                        message = last_message.getString("message");
                        if (last_message.has("is_read")) {
                            is_read = last_message.getBoolean("is_read");
                        }

                        my_message = last_message.getString("nick").equals(MainActivity.NickName);

                        JSONObject blocked = data.getJSONObject("is_blocked");

                        list_friends.add(new PrivateChatModel(nick, message, user_id_2, online, false, avatar, false, my_message, is_read));
                        privateChatsAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    TV_no_chats.setVisibility(View.VISIBLE);
                }
            }
        });
    };

    private final Emitter.Listener OnAddChatToListOfBlockedChats = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PB_loading.setVisibility(View.GONE);
                if (args.length != 0) {
                    TV_no_chats.setVisibility(View.GONE);
                    JSONObject data = (JSONObject) args[0];
                    Log.d("kkk", "принял - get_list_of_chats - " + data);
                    String nick = "", user_id_1 = "", user_id_2 = "", message = "", avatar = "";
                    boolean online = false, i_blocked = false, my_message = false, is_read = false;
                    try {
                        JSONArray user_ids = data.getJSONArray("user_ids");
                        user_id_1 = user_ids.getString(0);
                        user_id_2 = user_ids.getString(1);

                        JSONObject user_nicks = data.getJSONObject("user_nicks");
                        if (!user_id_1.equals(MainActivity.User_id)) {
                            String test_id = user_id_1;
                            user_id_1 = user_id_2;
                            user_id_2 = test_id;
                        }
                        nick = user_nicks.getString(user_id_2);
                        avatar = data.getString("avatar");
                        online = data.getBoolean("is_online");

                        JSONObject last_message = data.getJSONObject("last_message");
                        message = last_message.getString("message");
                        my_message = last_message.getBoolean("message");
                        is_read = last_message.getBoolean("is_read");

                        JSONObject blocked = data.getJSONObject("is_blocked");
                        if (blocked.getBoolean(user_id_1))
                        {
                            i_blocked = true;
                        }

                        list_friends.add(new PrivateChatModel(nick, message, user_id_2, online, false, avatar, false, my_message, is_read));
                        privateChatsAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    };
}