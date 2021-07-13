package com.mafiago.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.mafiago.R;
import com.mafiago.MainActivity;
import com.mafiago.adapters.FriendsAdapter;
import com.mafiago.adapters.PrivateChatsAdapter;
import com.mafiago.classes.OnBackPressedListener;
import com.mafiago.models.FriendModel;
import com.mafiago.models.PrivateChatModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.emitter.Emitter;

import static com.mafiago.MainActivity.socket;

public class FriendsFragment extends Fragment implements OnBackPressedListener {

    public ListView friendsView;

    public Button btnExit;

    public TextView TV_no_friends;

    public ProgressBar PB_loading;

    public FriendsAdapter friendsAdapter;

    ArrayList<FriendModel> list_friends = new ArrayList<>();

    public JSONObject json;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        friendsView = view.findViewById(R.id.fragmentFriends_list_friends);
        btnExit = view.findViewById(R.id.fragmentFriends_btn_exit);
        TV_no_friends = view.findViewById(R.id.fragmentFriends_TV_no_friends);
        PB_loading = view.findViewById(R.id.fragmentFriends_PB_loading);

        friendsAdapter = new FriendsAdapter(list_friends, getContext());
        friendsView.setAdapter(friendsAdapter);

        PB_loading.setVisibility(View.VISIBLE);
        TV_no_friends.setVisibility(View.GONE);

        json = new JSONObject();
        try {
            json.put("nick", MainActivity.NickName);
            json.put("session_id", MainActivity.Session_id);
            json.put("request_type", "other");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("get_list_of_friend_requests", json);
        Log.d("kkk", "Socket_отправка - get_list_of_friend_requests - "+ json.toString());

        json = new JSONObject();
        try {
            json.put("nick", MainActivity.NickName);
            json.put("session_id", MainActivity.Session_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("get_list_of_friends", json);
        Log.d("kkk", "Socket_отправка - get_list_of_friends - "+ json.toString());

        socket.off("get_friend");
        socket.off("get_friend_request");
        //socket.off("get_my_friend_request");

        socket.on("get_friend", OnGetFriend);
        socket.on("get_friend_request", OnGetFriendRequest);
        //socket.on("get_my_friend_request", OnGetMyFriendRequest);

        friendsView.setOnItemClickListener((parent, view1, position, id) -> {
            MainActivity.User_id_2 = list_friends.get(position).user_id_2;
            MainActivity.NickName_2 = list_friends.get(position).nick;
            MainActivity.bitmap_avatar_2 = fromBase64(list_friends.get(position).avatar);
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new PrivateMessagesFragment()).commit();
        });

        btnExit.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new MenuFragment()).commit();
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

    private final Emitter.Listener OnGetFriend = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PB_loading.setVisibility(View.GONE);
                if (args.length != 0) {
                    TV_no_friends.setVisibility(View.GONE);
                    JSONObject data = (JSONObject) args[0];
                    Log.d("kkk", "принял - get_friend - " + data);
                    String nick = "", user_id_1 = "", user_id_2 = "", message = "", avatar = "", playing_room_name = "";
                    int playing_room_num = 0;
                    boolean online = false;
                    try {
                        nick = data.getString("nick");
                        avatar = data.getString("avatar");
                        user_id_2 = data.getString("user_id");
                        online = data.getBoolean("is_online");
                        playing_room_num = data.getInt("playing_room_num");
                        if (playing_room_num != -1)
                        {
                            playing_room_name = data.getString("playing_room_name");
                            list_friends.add(new FriendModel(nick, user_id_2, online, avatar, playing_room_name, playing_room_num));
                        }
                        else
                        {
                            list_friends.add(new FriendModel(nick, user_id_2, online, avatar));
                        }
                        friendsAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    TV_no_friends.setVisibility(View.VISIBLE);
                }
            }
        });
    };

    private final Emitter.Listener OnGetFriendRequest = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PB_loading.setVisibility(View.GONE);
                if (args.length != 0) {
                    TV_no_friends.setVisibility(View.GONE);
                    JSONObject data = (JSONObject) args[0];
                    Log.d("kkk", "принял - get_friend_request - " + data);
                    String nick = "", user_id_1 = "", user_id_2 = "", message = "", avatar = "";
                    int playing_room_num = 0;
                    boolean online = false;
                    try {
                        nick = data.getString("nick");
                        avatar = data.getString("avatar");
                        user_id_2 = data.getString("user_id");
                        online = data.getBoolean("is_online");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    list_friends.add(new FriendModel(nick, user_id_2, online, avatar, true));
                    friendsAdapter.notifyDataSetChanged();
                }
                else
                {
                    TV_no_friends.setVisibility(View.VISIBLE);
                }
            }
        });
    };

    private final Emitter.Listener OnGetMyFriendRequest = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PB_loading.setVisibility(View.GONE);
                if (args.length != 0) {
                    TV_no_friends.setVisibility(View.GONE);
                    Log.d("kkk", "принял - get_my_friend_request - " + args[0]);
                    JSONObject data = (JSONObject) args[0];
                    String nick = "", user_id_1 = "", user_id_2 = "", message = "", avatar = "";
                    int playing_room_num = 0;
                    boolean online = false;
                    try {
                        nick = data.getString("nick");
                        avatar = data.getString("avatar");
                        user_id_2 = data.getString("user_id");
                        online = data.getBoolean("is_online");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    list_friends.add(new FriendModel(nick, user_id_2, online, avatar, true));
                    friendsAdapter.notifyDataSetChanged();
                }
                else
                {
                    TV_no_friends.setVisibility(View.VISIBLE);
                }
            }
        });
    };
}