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
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mafiago.MainActivity;
import com.example.mafiago.R;
import com.mafiago.adapters.GamesAdapter;
import com.mafiago.classes.OnBackPressedListener;
import com.mafiago.models.RoomModel;
import com.mafiago.models.UserModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import io.socket.emitter.Emitter;

import static com.mafiago.MainActivity.socket;

public class GamesListFragment extends Fragment implements OnBackPressedListener {

    public ListView listView;

    public TextView TV_no_games;

    public Button btnExit;
    public Button btnCreateRoom;

    public ProgressBar PB_loading;

    ArrayList<RoomModel> list_room = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_games_list, container, false);

        listView = view.findViewById(R.id.GamesList);
        btnCreateRoom = view.findViewById(R.id.btnCreateRoom);
        btnExit = view.findViewById(R.id.btnExitGamesList);
        TV_no_games = view.findViewById(R.id.fragmentGamesList_TV_no_games);
        PB_loading = view.findViewById(R.id.fragmentGamesList_PB_loading);

        PB_loading.setVisibility(View.VISIBLE);
        TV_no_games.setVisibility(View.GONE);

        socket.off("connect");
        socket.off("disconnect");
        socket.off("add_room_to_list_of_rooms");
        socket.off("delete_room_from_list_of_rooms");
        socket.off("update_list_of_rooms");
        socket.off("get_profile");

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

        btnCreateRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new CreateRoomFragment()).commit();

            }
        });

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new MenuFragment()).commit();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MainActivity.Game_id = list_room.get(position).id;
                MainActivity.RoomName = list_room.get(position).name;
                Log.d("kkk", "Переход в игру - " + MainActivity.Game_id);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GameFragment()).commit();
            }
        });

        return view;
    }

    @Override
    public void onBackPressed() {
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new MenuFragment()).commit();
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
                        final JSONObject json = new JSONObject();
                        try {
                            json.put("nick", MainActivity.NickName);
                            json.put("session_id", MainActivity.Session_id);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        socket.emit("get_list_of_rooms", json);
                        Log.d("kkk", "Socket_отправка - get_list_of_rooms - "+ json.toString());
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
                                if (list_room.size() == 0)
                                {
                                    TV_no_games.setVisibility(View.VISIBLE);
                                }
                                break;
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
                    PB_loading.setVisibility(View.GONE);
                    if (args.length != 0)
                    {
                        JSONObject data = (JSONObject) args[0];
                        String name = "", nick = "";
                        ArrayList<UserModel> list_users = new ArrayList<>();
                        Boolean alive = true, is_on = false;
                        int id = 0;
                        int min_people = 0;
                        int max_people = 0;
                        int num_people = 0;
                        JSONObject users = new JSONObject();
                        Log.d("kkk", "принял - add_room_to_list_of_rooms - " + data);
                        TV_no_games.setVisibility(View.GONE);
                        try {
                            name = data.getString("name");
                            id = data.getInt("num");
                            is_on = data.getBoolean("is_on");
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
                        Boolean not_doable = true;
                        for (int i = 0; i < list_room.size(); i++)
                        {
                            if (list_room.get(i).id == id)
                            {
                                not_doable = false;
                                break;
                            }
                        }
                        if (not_doable) {
                            RoomModel model = new RoomModel(name, min_people, max_people, num_people, id, list_users, is_on);
                            list_room.add(model);
                            GamesAdapter customList = new GamesAdapter(list_room, getContext());
                            listView.setAdapter(customList);
                        }
                    }
                    else
                    {
                        TV_no_games.setVisibility(View.VISIBLE);
                    }
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
                    Boolean alive = true, is_on = false;
                    JSONObject users = new JSONObject();
                    int num;
                    int min_people;
                    int max_people;
                    int num_people;
                    int id;
                    Log.d("kkk", "принял - update_room - " + data);
                    try {
                        name = data.getString("name");
                        is_on = data.getBoolean("is_on");
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
                                RoomModel model = new RoomModel(name, min_people, max_people, num_people, id, list_users, is_on);
                                list_room.set(i, model);
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

    private final Emitter.Listener OnGetProfile = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject data = (JSONObject) args[0];
                Log.d("kkk", "принял - get_profile - " + data);
                String nick = "", user_id_2 = "";
                int playing_room_num, money = 0, exp = 0, gold = 0;
                boolean online = false;
                JSONObject statistic = new JSONObject();
                int game_counter = 0, max_money_score = 0, max_exp_score = 0;
                String general_pers_of_wins = "", mafia_pers_of_wins = "", peaceful_pers_of_wins = "";

                try {
                    statistic = data.getJSONObject("statistics");
                    game_counter = statistic.getInt("game_counter");
                    max_money_score = statistic.getInt("max_money_score");
                    max_exp_score = statistic.getInt("max_exp_score");
                    general_pers_of_wins = statistic.getString("general_pers_of_wins");
                    mafia_pers_of_wins = statistic.getString("mafia_pers_of_wins");
                    peaceful_pers_of_wins = statistic.getString("peaceful_pers_of_wins");
                    online = data.getBoolean("is_online");
                    nick = data.getString("nick");
                    user_id_2 = data.getString("user_id");
                    money = data.getInt("money");
                    exp = data.getInt("exp");
                    gold = data.getInt("gold");
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
                FloatingActionButton FAB_complain = view_profile.findViewById(R.id.Item_profile_complain);
                TextView TV_money = view_profile.findViewById(R.id.ItemProfile_TV_money);
                TextView TV_exp = view_profile.findViewById(R.id.ItemProfile_TV_exp);
                TextView TV_gold = view_profile.findViewById(R.id.ItemProfile_TV_gold);

                TextView TV_game_counter = view_profile.findViewById(R.id.ItemProfile_TV_game_counter);
                TextView TV_max_money_score = view_profile.findViewById(R.id.ItemProfile_TV_max_money_score);
                TextView TV_max_exp_score = view_profile.findViewById(R.id.ItemProfile_TV_max_exp_score);
                TextView TV_general_pers_of_wins = view_profile.findViewById(R.id.ItemProfile_TV_general_pers_of_wins);
                TextView TV_mafia_pers_of_wins = view_profile.findViewById(R.id.ItemProfile_TV_mafia_pers_of_wins);
                TextView TV_peaceful_pers_of_wins = view_profile.findViewById(R.id.ItemProfile_TV_peaceful_pers_of_wins);

                TV_game_counter.setText(game_counter);
                TV_max_money_score.setText(max_money_score);
                TV_max_exp_score.setText(max_exp_score);
                TV_general_pers_of_wins.setText(general_pers_of_wins);
                TV_mafia_pers_of_wins.setText(mafia_pers_of_wins);
                TV_peaceful_pers_of_wins.setText(peaceful_pers_of_wins);

                TV_gold.setText(String.valueOf(gold));
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

                if (!nick.equals(MainActivity.NickName)) {
                    FAB_send_message.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alert.cancel();
                            MainActivity.User_id_2 = finalUser_id_;
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new PrivateMessagesFragment()).commit();
                        }
                    });

                    FAB_complain.setOnClickListener(v -> {

                    });

                    FAB_add_friend.setOnClickListener(v1 -> {
                        //TODO: добавление в друзья
                    });
                }
                else
                {
                    FAB_send_message.setVisibility(View.GONE);
                    FAB_complain.setVisibility(View.GONE);
                    FAB_add_friend.setVisibility(View.GONE);
                }
                alert.show();
            }
        });
    };
}