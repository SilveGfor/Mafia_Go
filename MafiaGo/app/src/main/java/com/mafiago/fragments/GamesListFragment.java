package com.mafiago.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mafiago.MainActivity;
import com.example.mafiago.R;
import com.mafiago.adapters.GamesAdapter;
import com.mafiago.models.RoomModel;
import com.mafiago.models.UserModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import io.socket.emitter.Emitter;

import static com.mafiago.MainActivity.socket;

public class GamesListFragment extends Fragment {

    public ListView listView;

    public Button btnExit;
    public Button btnCreateRoom;


    ArrayList<RoomModel> list_room = new ArrayList<>();

    public boolean First = true;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_games_list, container, false);

        listView = view.findViewById(R.id.GamesList);
        btnCreateRoom = view.findViewById(R.id.btnCreateRoom);
        btnExit = view.findViewById(R.id.btnExitGamesList);

        socket.on("connect", onConnect);
        socket.on("disconnect", onDisconnect);
        socket.on("add_room_to_list_of_rooms", onNewRoom);
        socket.on("delete_room_from_list_of_rooms", onDeleteRoom);
        socket.on("update_list_of_rooms", onUpdateRoom);
        socket.on("get_profile", OnGetProfile);

        final JSONObject json = new JSONObject();
        try {
            json.put("nick", MainActivity.NickName);
            json.put("session_id", MainActivity.Session_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("get_list_of_rooms", json);
        Log.d("kkk", "Socket_отправка - get_list_of_rooms - "+ json.toString());
        First = false;

        btnCreateRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new CreateRoomFragment()).addToBackStack("Fragments").commit();

            }
        });

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final JSONObject json2 = new JSONObject();
                try {
                    json2.put("nick", MainActivity.NickName);
                    json2.put("session_id",  MainActivity.Session_id);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("kkk", "Socket_отправка - disconnect_from_list_of_rooms "+ json2.toString());
                socket.emit("disconnect_from_list_of_rooms", json2);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new MenuFragment()).addToBackStack("Fragments").commit();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MainActivity.Game_id = list_room.get(position).id;
                MainActivity.RoomName = list_room.get(position).name;
                Log.d("kkk", "Переход в игру - " + MainActivity.Game_id);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GameFragment()).addToBackStack("Fragments").commit();
            }
        });



        return view;
    }

    private final Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("kkk", "Connect");
                    if (First)
                    {
                        final JSONObject json = new JSONObject();
                        try {
                            json.put("nick", MainActivity.NickName);
                            json.put("session_id", MainActivity.Session_id);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        socket.emit("get_list_of_rooms", json);
                        Log.d("kkk", "Socket_отправка - get_list_of_rooms - "+ json.toString());
                        First = false;
                    }
                    else {
                        final JSONObject json2 = new JSONObject();
                        try {
                            json2.put("nick", MainActivity.NickName);
                            json2.put("session_id", MainActivity.Session_id);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d("kkk", "Socket_отправка - connect_to_list_of_rooms - " + json2.toString());
                        socket.emit("connect_to_list_of_rooms", json2);
                    }

                }
            });
        }
    };

    private final Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("kkk", "Disconnect");
                }
            });


            Log.d("kkk", "Disconnect");
        }
    };

    private final Emitter.Listener onDeleteRoom = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    int num;
                    Log.d("kkk", "принял - Delete_Room - " + data);
                    try {
                        num = data.getInt("num");

                        Log.d("kkk", "Приём - Delete_room - " + num);

                        for(int i = 0; i< list_room.size(); i++) {
                            if (list_room.get(i).id == num)
                            {
                                list_room.remove(i);
                                GamesAdapter customList = new GamesAdapter(list_room, getContext());
                                listView.setAdapter(customList);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        }
    };

    private final Emitter.Listener onNewRoom = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String name = "", nick = "";
                    ArrayList<UserModel> list_users = new ArrayList<>();
                    Boolean alive = true;
                    int num = 0;
                    int min_people = 0;
                    int max_people = 0;
                    int num_people = 0;
                    JSONObject users = new JSONObject();
                    Log.d("kkk", "принял - add_room_to_list_of_rooms - " + data);
                    try {
                        name = data.getString("name");
                        num = data.getInt("num");
                        min_people = data.getInt("min_people_num");
                        max_people = data.getInt("max_people_num");
                        num_people = data.getInt("people_num");
                        users = data.getJSONObject("users");
                        for (Iterator iterator = users.keys(); iterator.hasNext();)
                        {
                            nick = (String) iterator.next();
                            String alive_string = users.getString(nick);
                            alive = alive_string.equals("alive");
                            list_users.add(new UserModel(nick, alive));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    RoomModel model = new RoomModel(name, min_people, max_people, num_people, num, list_users);
                    list_room.add(model);
                    GamesAdapter customList = new GamesAdapter(list_room, getContext());
                    listView.setAdapter(customList);
                }
            });
        }
    };

    private final Emitter.Listener onUpdateRoom = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String name;
                    String nick = "";
                    ArrayList<UserModel> list_users = new ArrayList<>();
                    Boolean alive = true;
                    JSONObject users = new JSONObject();
                    int num;
                    int min_people;
                    int max_people;
                    int num_people;
                    int id;
                    Log.d("kkk", "принял - UodateRoom - " + data);
                    try {
                        name = data.getString("name");
                        num = data.getInt("num");
                        min_people = data.getInt("min_people_num");
                        max_people = data.getInt("max_people_num");
                        num_people = data.getInt("people_num");
                        id = data.getInt("num");
                        users = data.getJSONObject("users");
                        for (Iterator iterator = users.keys(); iterator.hasNext();)
                        {
                            nick = (String) iterator.next();
                            String alive_string = users.getString(nick);
                            alive = alive_string.equals("alive");
                            list_users.add(new UserModel(nick, alive));
                        }

                        for(int i = 0; i< list_room.size(); i++) {
                            if (list_room.get(i).id == id)
                            {
                                RoomModel model = new RoomModel(name, min_people, max_people, num_people, id, list_users);
                                list_users.clear();
                                list_room.set(i, model);
                                GamesAdapter customList = new GamesAdapter(list_room, getContext());
                                listView.setAdapter(customList);
                            }
                            else
                            {
                                Log.d("kkk", "принял - UpdateRoom - " + "такой комнаты с таким айди нет!!!");
                            }
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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
                String nick = "", user_id_2 = "";
                int playing_room_num, money = 0, exp = 0;
                boolean online = false;
                try {
                    online = data.getBoolean("is_online");
                    nick = data.getString("nick");
                    user_id_2 = data.getString("user_id");
                    money = data.getInt("money");
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
                TextView TV_money = view_profile.findViewById(R.id.ItemProfile_TV_money);
                TextView TV_exp = view_profile.findViewById(R.id.ItemProfile_TV_exp);

                TV_money.setText(String.valueOf(money));
                TV_exp.setText(String.valueOf(exp));


                TextView TV_nick = view_profile.findViewById(R.id.Item_profile_TV_nick);
                ImageView IV_on_off = view_profile.findViewById(R.id.Item_profile_IV_on_off);

                if (online) IV_on_off.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_online));
                else IV_on_off.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_offline));

                TV_nick.setText(nick);
                FAB_kick.setVisibility(View.GONE);

                AlertDialog alert = builder.create();

                String finalUser_id_ = user_id_2;
                FAB_send_message.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alert.cancel();
                        MainActivity.User_id_2 = finalUser_id_;
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new PrivateChatFragment()).commit();
                    }
                });

                FAB_add_friend.setOnClickListener(v1 -> {
                    //TODO: добавление в друзья
                });
                alert.show();
                Log.d("kkk", "принял - get_profile - " + data);
            }
        });
    };
}