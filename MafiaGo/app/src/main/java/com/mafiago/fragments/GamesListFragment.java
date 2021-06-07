package com.mafiago.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

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

    ArrayList<UserModel> list_users = new ArrayList<>();

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
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new CreateRoomFragment()).commit();

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
                Log.d("kkk", "Socket_отправка - "+ json2.toString());
                socket.emit("disconnect_from_list_of_rooms", json2);
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
                    list_users.clear();
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
}