package com.example.mafiago.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.mafiago.MainActivity;
import com.example.mafiago.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import static  com.example.mafiago.MainActivity.socket;


public class CreateRoomFragment extends Fragment {

    EditText ET_RoomName;

    TextView TV_max_people;

    SeekBar SB_max_people;

    Button btnCreateRoom;
    Button btnExitCreateRoom;


    public static final String APP_PREFERENCES = "create_room";
    public static final String APP_PREFERENCES_ROOM_NAME = "room_name";
    public static final String APP_PREFERENCES_MAX_PEOPLE = "max_people";
    public static final String APP_PREFERENCES_ROLES = "roles";

    private SharedPreferences mSettings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_room, container, false);

        mSettings = getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        ET_RoomName = view.findViewById(R.id.fragmentCreateRoom_ET_room_name);

        TV_max_people = view.findViewById(R.id.fragmentCreateRoom_TV_max_people);

        SB_max_people = view.findViewById(R.id.fragmentCreateRoom_SB_max_players);

        btnCreateRoom = view.findViewById(R.id.btnCreate);
        btnExitCreateRoom = view.findViewById(R.id.btnExitCreateRoom);

        //socket.connect();

        socket.on("create_room", onCreateRoom);
        socket.on("connect", onConnect);
        socket.on("disconnect", onDisconnect);

        TV_max_people.setText("");

        SB_max_people.setOnSeekBarChangeListener(seekBarChangeListener);

        ET_RoomName.setText(mSettings.getString(APP_PREFERENCES_ROOM_NAME, null));
        SB_max_people.setProgress(mSettings.getInt(APP_PREFERENCES_MAX_PEOPLE, 5));

        btnCreateRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final JSONObject json = new JSONObject();
                final JSONObject json_roles = new JSONObject();
                try {
                    //Map<String,String> dict = new HashMap<String,String>();
                    //dict = {'mafia': ['mafia_don'], 'peaceful': ['doctor',  'lover']}
                    ArrayList<String> peaceful = new ArrayList<String>();
                    //peaceful.add("doctor");
                    ArrayList<String> mafia = new ArrayList<String>();
                    json_roles.put("peaceful", peaceful);
                    json_roles.put("mafia", mafia);
                    json.put("nick", MainActivity.NickName);
                    json.put("session_id", MainActivity.Session_id);
                    json.put("name", ET_RoomName.getText());
                    json.put("min_people_num", 5);
                    json.put("max_people_num", SB_max_people.getProgress());
                    json.put("roles", json_roles);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                socket.emit("create_room", json);
                Log.d("kkk", "Socket_отправка - create_room - "+ json.toString());
            }
        });

        btnExitCreateRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
            }
        });

        return view;
    }

    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            TV_max_people.setText(String.valueOf(SB_max_people.getProgress()));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                        final JSONObject json2 = new JSONObject();
                        try {
                            json2.put("nick", MainActivity.NickName);
                            json2.put("session_id", MainActivity.Session_id);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //socket.emit("connect_to_room", json2);
                        Log.d("kkk", "CONNECT");
                }
            });
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
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

    private Emitter.Listener onCreateRoom = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        MainActivity.Game_id = data.getInt("room_num");
                        Log.d("kkk", "Принял - create_room: " + MainActivity.Game_id);
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GameFragment()).commit();
                    } catch (JSONException e) {
                        return;
                    }
                }
            });
        }
    };

}