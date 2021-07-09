package com.mafiago.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.mafiago.MainActivity;
import com.example.mafiago.R;

import com.mafiago.adapters.RoleAdapter;
import com.mafiago.classes.OnBackPressedListener;
import com.mafiago.enums.Role;
import com.mafiago.models.RoleModel;

import org.florescu.android.rangeseekbar.RangeSeekBar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.emitter.Emitter;

import static com.mafiago.MainActivity.f;
import static  com.mafiago.MainActivity.socket;

public class CreateRoomFragment extends Fragment implements OnBackPressedListener {

    EditText ET_RoomName;

    TextView TV_max_people;

    RangeSeekBar RSB_num_users;

    Button btnCreateRoom;
    Button btnExitCreateRoom;

    public GridView GridView;

    String name;

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

        RSB_num_users = view.findViewById(R.id.fragmentCreateRoom_RSB_num_users);

        btnCreateRoom = view.findViewById(R.id.fragmentCreateRoom_BTN_create_room);
        btnExitCreateRoom = view.findViewById(R.id.fragmentCreateRoom_BTN_exit);

        GridView = view.findViewById(R.id.fragmentCreateRoom_GV_roles);

        socket.off("create_room");
        socket.off("connect");
        socket.off("disconnect");
        socket.off("user_error");

        socket.on("create_room", onCreateRoom);
        socket.on("connect", onConnect);
        socket.on("disconnect", onDisconnect);
        socket.on("user_error", onUserError);


        name = "";
        int max_people = mSettings.getInt(APP_PREFERENCES_MAX_PEOPLE, 8);
        int min_people = mSettings.getInt(APP_PREFERENCES_MIN_PEOPLE, 5);
        ET_RoomName.setText(mSettings.getString(APP_PREFERENCES_ROOM_NAME, "London Bridge"));
        TV_max_people.setText(String.valueOf(max_people));
        RSB_num_users.setSelectedMaxValue(max_people);
        RSB_num_users.setSelectedMinValue(min_people);
        SetRoles(max_people);

        RSB_num_users.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Object minValue, Object maxValue) {
                SetRoles((int) maxValue);
                //TODO: доделать сохранение ролей при выходе из фрагмента
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putInt(APP_PREFERENCES_MAX_PEOPLE, (int) maxValue);
                editor.putInt(APP_PREFERENCES_MIN_PEOPLE, (int) minValue);
                editor.apply();
            }
        });

        btnCreateRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkOnline(getContext())) {
                    name = ET_RoomName.getText().toString();
                    int flag = 0;
                    for (int i = 0; i < name.length(); i ++)
                    {
                        if (Character.isLetter(name.charAt(i))) {
                            for (int j = 0; j < f.length; j++) {
                                if (name.charAt(i) == f[j]) {
                                    flag = 1;
                                }
                            }

                            if (flag != 1) {
                                name = name.replace(String.valueOf(name.charAt(i)), "");
                            }
                            flag = 0;
                        }
                    }

                    final JSONObject json = new JSONObject();
                    final JSONObject json_roles = new JSONObject();
                    try {
                        json_roles.put("peaceful", peaceful);
                        json_roles.put("mafia", mafia);
                        json.put("nick", MainActivity.NickName);
                        json.put("session_id", MainActivity.Session_id);
                        json.put("name", name);
                        json.put("min_people_num", RSB_num_users.getSelectedMinValue());
                        json.put("max_people_num", RSB_num_users.getSelectedMaxValue());
                        json.put("roles", json_roles);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putString(APP_PREFERENCES_ROOM_NAME, name);
                    editor.apply();
                    socket.emit("create_room", json);
                    Log.d("kkk", "Socket_отправка - create_room - " + json.toString());
                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("У вас нет подключения к интернету!")
                            .setMessage("")
                            .setIcon(R.drawable.ic_ban)
                            .setCancelable(false)
                            .setNegativeButton("Ок",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
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

    @Override
    public void onBackPressed() {
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
    }

    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

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
                        MainActivity.RoomName = name;
                        Log.d("kkk", "Принял - create_room: " + MainActivity.Game_id);
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GameFragment()).commit();
                    } catch (JSONException e) {
                        return;
                    }
                }
            });
        }
    };

    private final Emitter.Listener onUserError = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if (getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String error;
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    try {
                        error = data.getString("error");
                        AlertDialog alert;
                        switch (error) {
                            case "you_are_playing_in_another_room":
                                builder.setTitle("Извините, но вы играете в другой игре")
                                        .setMessage("")
                                        .setIcon(R.drawable.ic_error)
                                        .setCancelable(false)
                                        .setNegativeButton("Ок",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.cancel();
                                                    }
                                                });
                                break;
                            case "mat_name":
                                builder.setTitle("Извините, но это неприличное название комнаты!")
                                        .setMessage("")
                                        .setIcon(R.drawable.ic_error)
                                        .setCancelable(false)
                                        .setNegativeButton("Ок",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.cancel();
                                                    }
                                                });
                                break;
                            default:
                                builder.setTitle("Что-то пошло не так")
                                        .setMessage("")
                                        .setIcon(R.drawable.ic_error)
                                        .setCancelable(false)
                                        .setNegativeButton("Ок",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.cancel();
                                                    }
                                                });
                                break;
                        }
                        alert = builder.create();
                        alert.show();
                        Log.d("kkk", "Socket_принять - user_error " + args[0]);
                    } catch (JSONException e) {
                        e.printStackTrace();
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
                list_roles.add(new RoleModel(Role.DOCTOR, true));
                roleAdapter = new RoleAdapter(list_roles, getContext());
                break;
            case 6:
            case 7:
                list_roles.add(new RoleModel(Role.DOCTOR, true));
                list_roles.add(new RoleModel(Role.LOVER, true));
                list_roles.add(new RoleModel(Role.MAFIA_DON, false));
                roleAdapter = new RoleAdapter(list_roles, getContext());
                break;
            case 8:
            case 9:
            case 10:
            case 11:
                list_roles.add(new RoleModel(Role.DOCTOR, true));
                list_roles.add(new RoleModel(Role.LOVER, true));
                list_roles.add(new RoleModel(Role.MAFIA_DON, false));
                list_roles.add(new RoleModel(Role.JOURNALIST, true));
                roleAdapter = new RoleAdapter(list_roles, getContext());
                break;
            case 12:
            case 13:
                list_roles.add(new RoleModel(Role.DOCTOR, true));
                list_roles.add(new RoleModel(Role.LOVER, true));
                list_roles.add(new RoleModel(Role.MAFIA_DON, false));
                list_roles.add(new RoleModel(Role.JOURNALIST, true));
                list_roles.add(new RoleModel(Role.BODYGUARD, true));
                list_roles.add(new RoleModel(Role.TERRORIST, false));
                list_roles.add(new RoleModel(Role.DOCTOR_OF_EASY_VIRTUE, true));
                roleAdapter = new RoleAdapter(list_roles, getContext());
                break;
            case 14:
                list_roles.add(new RoleModel(Role.DOCTOR, true));
                list_roles.add(new RoleModel(Role.LOVER, true));
                list_roles.add(new RoleModel(Role.MAFIA_DON, false));
                list_roles.add(new RoleModel(Role.JOURNALIST, true));
                list_roles.add(new RoleModel(Role.BODYGUARD, true));
                list_roles.add(new RoleModel(Role.TERRORIST, false));
                list_roles.add(new RoleModel(Role.DOCTOR_OF_EASY_VIRTUE, true));
                list_roles.add(new RoleModel(Role.MANIAC, true));
                list_roles.add(new RoleModel(Role.POISONER, false));
                roleAdapter = new RoleAdapter(list_roles, getContext());
                break;
            default:
                list_roles.add(new RoleModel(Role.DOCTOR, true));
                list_roles.add(new RoleModel(Role.LOVER, true));
                list_roles.add(new RoleModel(Role.MAFIA_DON, false));
                list_roles.add(new RoleModel(Role.JOURNALIST, true));
                list_roles.add(new RoleModel(Role.BODYGUARD, true));
                list_roles.add(new RoleModel(Role.TERRORIST, false));
                list_roles.add(new RoleModel(Role.DOCTOR_OF_EASY_VIRTUE, true));
                list_roles.add(new RoleModel(Role.MANIAC, true));
                list_roles.add(new RoleModel(Role.POISONER, false));
                roleAdapter = new RoleAdapter(list_roles, getContext());
                break;
        }
        GridView.setAdapter(roleAdapter);
    }

    public boolean isNetworkOnline(Context context) {
        boolean status = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getNetworkInfo(0);
            if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
                status = true;
            } else {
                netInfo = cm.getNetworkInfo(1);
                if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED)
                    status = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return status;
    }

}