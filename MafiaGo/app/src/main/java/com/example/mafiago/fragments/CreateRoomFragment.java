package com.example.mafiago.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.mafiago.MainActivity;
import com.example.mafiago.R;
import com.example.mafiago.adapters.GamesAdapter;
import com.example.mafiago.adapters.RoleAdapter;
import com.example.mafiago.enums.Role;
import com.example.mafiago.models.RoleModel;
import com.example.mafiago.models.RoomModel;

import org.json.JSONArray;
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

    public GridView GridView;

    ArrayList<RoleModel> list_roles = new ArrayList<>();

    static public JSONArray peaceful = new JSONArray();
    static public JSONArray mafia = new JSONArray();

    public static final String APP_PREFERENCES = "create_room";
    public static final String APP_PREFERENCES_ROOM_NAME = "room_name";
    public static final String APP_PREFERENCES_MAX_PEOPLE = "max_people";
    public static final String APP_PREFERENCES_MIN_PEOPLE = "min_people";
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

        btnCreateRoom = view.findViewById(R.id.fragmentCreateRoom_BTN_create_room);
        btnExitCreateRoom = view.findViewById(R.id.fragmentCreateRoom_BTN_exit);

        GridView = view.findViewById(R.id.fragmentCreateRoom_GV_roles);

        CreateRoomFragment.CreateRoomTask socketTask = new CreateRoomTask();
        socketTask.execute();



        SB_max_people.setOnSeekBarChangeListener(seekBarChangeListener);


        int max_people = mSettings.getInt(APP_PREFERENCES_MAX_PEOPLE, 8);
        ET_RoomName.setText(mSettings.getString(APP_PREFERENCES_ROOM_NAME, "GoodGame"));
        TV_max_people.setText(String.valueOf(max_people));
        SB_max_people.setProgress(max_people);
        SetRoles(max_people);



        btnCreateRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final JSONObject json = new JSONObject();
                final JSONObject json_roles = new JSONObject();
                try {
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
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putString(APP_PREFERENCES_ROOM_NAME, String.valueOf(ET_RoomName.getText()));
                editor.apply();
                //socket.emit("create_room", json);
                Log.d("kkk", "Socket_отправка - create_room - "+ json.toString());
            }
        });

        btnExitCreateRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putString(APP_PREFERENCES_ROOM_NAME, String.valueOf(ET_RoomName.getText()));
                editor.apply();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
            }
        });

        return view;
    }

    public class CreateRoomTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("kkk", "onPreExecute");
        }

        @Override
        protected Void doInBackground(Void... voids) {

            socket.on("create_room", onCreateRoom);
            socket.on("connect", onConnect);
            socket.on("disconnect", onDisconnect);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d("kkk", "onPostExecute");
        }
    }

    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            TV_max_people.setText(String.valueOf(progress));
            SetRoles(progress);
//TODO: доделать сохранение ролей при выходе из фрагмента
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putInt(APP_PREFERENCES_MAX_PEOPLE, progress);
            editor.apply();
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
                        MainActivity.RoomName = String.valueOf(ET_RoomName.getText());
                        Log.d("kkk", "Принял - create_room: " + MainActivity.Game_id);
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GameFragment()).commit();
                    } catch (JSONException e) {
                        return;
                    }
                }
            });
        }
    };

    public void SetRoles(int people) {
        RoleAdapter roleAdapter;
        list_roles = new ArrayList<>();
        peaceful = new JSONArray();
        mafia = new JSONArray();
        switch (people)
        {
            case 5:
                list_roles.add(new RoleModel(Role.SHERIFF, true));
                list_roles.add(new RoleModel(Role.DOCTOR, true));
                roleAdapter = new RoleAdapter(list_roles, getContext());
                break;
            case 6:
            case 7:
                list_roles.add(new RoleModel(Role.SHERIFF, true));
                list_roles.add(new RoleModel(Role.DOCTOR, true));
                list_roles.add(new RoleModel(Role.LOVER, true));
                roleAdapter = new RoleAdapter(list_roles, getContext());
                break;
            case 8:
                list_roles.add(new RoleModel(Role.SHERIFF, true));
                list_roles.add(new RoleModel(Role.DOCTOR, true));
                list_roles.add(new RoleModel(Role.LOVER, true));
                list_roles.add(new RoleModel(Role.MAFIA_DON, false));
                roleAdapter = new RoleAdapter(list_roles, getContext());
                break;
            default:
                list_roles.add(new RoleModel(Role.SHERIFF, true));
                list_roles.add(new RoleModel(Role.DOCTOR, true));
                list_roles.add(new RoleModel(Role.LOVER, true));
                list_roles.add(new RoleModel(Role.MAFIA_DON, false));
                roleAdapter = new RoleAdapter(list_roles, getContext());
                break;
        }
        GridView.setAdapter(roleAdapter);
    }

}