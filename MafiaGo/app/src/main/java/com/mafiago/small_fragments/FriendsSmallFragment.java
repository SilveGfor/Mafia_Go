package com.mafiago.small_fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.mafiago.R;
import com.mafiago.MainActivity;
import com.mafiago.adapters.FriendRequestsAdapter;
import com.mafiago.adapters.FriendsAdapter;
import com.mafiago.fragments.StartFragment;
import com.mafiago.models.FriendModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import io.socket.emitter.Emitter;

import static android.app.Activity.RESULT_OK;
import static com.mafiago.MainActivity.socket;

public class FriendsSmallFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";

    private int mPage;

    static final int GALLERY_REQUEST = 1;

    public String base64_screenshot = "";

    public JSONObject json;

    View view_reportError;
    ImageView IV_screen;
    Button btn_addScreen;
    EditText ET_message;
    Button btm_sendError;

    ////////////////1
    RelativeLayout RL_findFriend;
    Button btn_findFriend;
    EditText ET_searchFriend;
    ListView LV_friends;
    TextView TV_no_friends;
    ProgressBar PB_loading;
    FriendsAdapter friendsAdapter;
    ArrayList<FriendModel> list_friends = new ArrayList<>();

    ////////////////2
    ListView LV_requests;
    TextView TV_no_requests;
    ProgressBar PB_loading_requests;
    FriendRequestsAdapter requestsAdapter;
    ArrayList<FriendModel> list_requests = new ArrayList<>();

    ////////////////2


    public static final String APP_PREFERENCES = "user";
    public static final String APP_PREFERENCES_EMAIL = "email";
    public static final String APP_PREFERENCES_PASSWORD = "password";
    public static final String APP_PREFERENCES_LAST_ROLE = "role";
    public static final String APP_PREFERENCES_SHOW_ROLE= "show_role";

    private SharedPreferences mSettings;

    public static FriendsSmallFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        FriendsSmallFragment fragment = new FriendsSmallFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPage = getArguments().getInt(ARG_PAGE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = null;

        mSettings = getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        switch (mPage)
        {
            case 1:
                view = inflater.inflate(R.layout.small_fragment_friends_list, container, false);
                ET_searchFriend = view.findViewById(R.id.smallFragmentFriendsList_ET_search);
                LV_friends = view.findViewById(R.id.smallFragmentFriendsList_LV_friends);
                TV_no_friends = view.findViewById(R.id.smallFragmentFriendsList_TV_noFriends);
                PB_loading = view.findViewById(R.id.smallFragmentFriendsList_PB);
                RL_findFriend = view.findViewById(R.id.smallFragmentFriendsList_RL_findFriend);
                btn_findFriend = view.findViewById(R.id.smallFragmentFriendsList_btn_search);

                friendsAdapter = new FriendsAdapter(list_friends, getContext());
                LV_friends.setAdapter(friendsAdapter);

                PB_loading.setVisibility(View.VISIBLE);
                TV_no_friends.setVisibility(View.GONE);

                socket.on("get_friend", OnGetFriend);

                btn_findFriend.setOnClickListener(v -> {
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(getContext());
                    builder2.setTitle("В разработке...")
                            .setMessage("")
                            .setIcon(R.drawable.ic_razrabotka)
                            .setCancelable(false)
                            .setNegativeButton("Ок",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert2 = builder2.create();
                    alert2.show();
                });

                json = new JSONObject();
                try {
                    json.put("nick", MainActivity.NickName);
                    json.put("session_id", MainActivity.Session_id);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                socket.emit("get_list_of_friends", json);
                Log.d("kkk", "Socket_отправка - get_list_of_friends - "+ json.toString());
                break;
            case 2:
                view = inflater.inflate(R.layout.small_fragment_friends_requests, container, false);
                LV_requests = view.findViewById(R.id.smallFragmentFriendsRequests_LV);
                TV_no_requests = view.findViewById(R.id.smallFragmentFriendsRequests_TV_noRequests);
                PB_loading_requests = view.findViewById(R.id.smallFragmentFriendsRequests_PB);

                requestsAdapter = new FriendRequestsAdapter(list_requests, getContext());
                LV_requests.setAdapter(requestsAdapter);

                PB_loading_requests.setVisibility(View.VISIBLE);
                TV_no_requests.setVisibility(View.GONE);

                socket.on("get_friend_request", OnGetFriendRequest);

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

                break;
            case 3:
                view = inflater.inflate(R.layout.small_fragment_friends_requests, container, false);
                LV_requests = view.findViewById(R.id.smallFragmentFriendsRequests_LV);
                TV_no_requests = view.findViewById(R.id.smallFragmentFriendsRequests_TV_noRequests);
                PB_loading_requests = view.findViewById(R.id.smallFragmentFriendsRequests_PB);

                requestsAdapter = new FriendRequestsAdapter(list_requests, getContext());
                LV_requests.setAdapter(requestsAdapter);

                PB_loading_requests.setVisibility(View.VISIBLE);
                TV_no_requests.setVisibility(View.GONE);

                socket.on("get_my_friend_request", OnGetMyFriendRequest);

                json = new JSONObject();
                try {
                    json.put("nick", MainActivity.NickName);
                    json.put("session_id", MainActivity.Session_id);
                    json.put("request_type", "my");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                socket.emit("get_list_of_friend_requests", json);
                Log.d("kkk", "Socket_отправка - get_list_of_friend_requests - "+ json.toString());

                break;
        }
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
                    int playing_room_num = 0, min_people = 0, max_people = 0;
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
                            min_people = data.getInt("min_people_num");
                            max_people = data.getInt("max_people_num");
                            list_friends.add(new FriendModel(nick, user_id_2, online, avatar, playing_room_name, playing_room_num, min_people, max_people));
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
                PB_loading_requests.setVisibility(View.GONE);
                if (args.length != 0) {
                    TV_no_requests.setVisibility(View.GONE);
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

                    list_requests.add(new FriendModel(nick, user_id_2, online, avatar, false));
                    requestsAdapter.notifyDataSetChanged();
                }
                else
                {
                    TV_no_requests.setVisibility(View.VISIBLE);
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
                PB_loading_requests.setVisibility(View.GONE);
                if (args.length != 0) {
                    TV_no_requests.setVisibility(View.GONE);
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

                    list_requests.add(new FriendModel(nick, user_id_2, online, avatar, true));
                    requestsAdapter.notifyDataSetChanged();
                }
                else
                {
                    TV_no_requests.setVisibility(View.VISIBLE);
                }
            }
        });
    };
}