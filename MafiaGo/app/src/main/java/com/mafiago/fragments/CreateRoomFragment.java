package com.mafiago.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.opengl.Visibility;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.VisibleForTesting;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
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
import java.util.HashSet;
import java.util.Set;

import io.socket.emitter.Emitter;

import static android.content.Context.APP_OPS_SERVICE;
import static com.mafiago.MainActivity.f;
import static  com.mafiago.MainActivity.socket;

public class CreateRoomFragment extends Fragment implements OnBackPressedListener {
    private static final String ARG_STUDY = "study";
    String study_type = "";

    boolean blockView = false;

    EditText ET_RoomName;

    RangeSeekBar RSB_num_users;

    Button btnCreateRoom;
    Button btnCustomRoom;

    Spinner spinnerDayTime;

    RelativeLayout btn_back;
    GridView gridView;
    SwitchCompat swith_password;
    EditText ET_password;
    ImageView Menu;
    ProgressBar PB_loading;

    String name;
    String password = "";
    boolean has_password = false;
    int minPlayers = 0;
    int maxPlayers = 0;
    int dayTime;

    ArrayList<RoleModel> list_roles = new ArrayList<>();

    RoleAdapter roleAdapter;

    public JSONArray peaceful = new JSONArray();
    public JSONArray mafia = new JSONArray();

    public static final String APP_PREFERENCES = "create_room";
    public static final String APP_PREFERENCES_ROOM_NAME = "room_name";
    public static final String APP_PREFERENCES_HAS_PASSWORD = "has_password";
    public static final String APP_PREFERENCES_ROOM_PASSWORD = "password";
    public static final String APP_PREFERENCES_MAX_PEOPLE = "max_people";
    public static final String APP_PREFERENCES_MIN_PEOPLE = "min_people";
    public static final String APP_PREFERENCES_ROLES = "roles";
    public static final String APP_PREFERENCES_DAY_TIME_POSITION = "day_time_position";

    private SharedPreferences mSettings;

    public static CreateRoomFragment newInstance(String study_type) {
        CreateRoomFragment fragment = new CreateRoomFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STUDY, study_type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            study_type = getArguments().getString(ARG_STUDY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_room, container, false);

        mSettings = getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        ET_RoomName = view.findViewById(R.id.fragmentRegister_ET_email);

        RSB_num_users = view.findViewById(R.id.fragmentCreateRoom_PSB_playerNum);
        swith_password = view.findViewById(R.id.fragmentCreateRoom_Swith_password);
        ET_password = view.findViewById(R.id.fragmentCreateRoom_ET_password);
        btn_back = view.findViewById(R.id.fragmentGamesList_RL_back);
        PB_loading = view.findViewById(R.id.fragmentCreateRoom_PB);
        spinnerDayTime = view.findViewById(R.id.fragmentCreateRoom_Spinner_dayTime);

        btnCreateRoom = view.findViewById(R.id.fragmentCreateRoom_btn_createRoom);
        btnCustomRoom = view.findViewById(R.id.fragmentCreateRoom_btn_customRoom);

        gridView = view.findViewById(R.id.fragmentCreateRoom_GV_roles);
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

        //socket.off("connect");
        //socket.off("disconnect");
        socket.off("create_room");
        socket.off("user_error");

        socket.on("create_room", onCreateRoom);
        socket.on("connect", onConnect);
        socket.on("disconnect", onDisconnect);
        socket.on("user_error", onUserError);

        roleAdapter = new RoleAdapter(list_roles, getContext());
        gridView.setAdapter(roleAdapter);

        name = "";
        int max_people = mSettings.getInt(APP_PREFERENCES_MAX_PEOPLE, 8);
        int min_people = mSettings.getInt(APP_PREFERENCES_MIN_PEOPLE, 5);
        has_password = mSettings.getBoolean(APP_PREFERENCES_HAS_PASSWORD, false);
        if (has_password)
        {
            swith_password.setChecked(true);
            ET_password.setVisibility(View.VISIBLE);
            password = mSettings.getString(APP_PREFERENCES_ROOM_PASSWORD, "");
            ET_password.setText(password);
        }
        ET_RoomName.setText(mSettings.getString(APP_PREFERENCES_ROOM_NAME, "King's road"));
        //TV_max_people.setText(String.valueOf(max_people));
        RSB_num_users.setSelectedMaxValue(max_people);
        RSB_num_users.setSelectedMinValue(min_people);
        maxPlayers = max_people;
        minPlayers = min_people;
        SetRoles(max_people);

        String[] timeList = { "45 ????????????", "70 ????????????", "1,5 ????????????", "2,5 ????????????", "5 ??????????" };
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, timeList);
        // ???????????????? ??????????????
        spinnerDayTime.setAdapter(spinnerArrayAdapter);
        spinnerDayTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position)
                {
                    case 0:
                        dayTime = 45;
                        break;
                    case 1:
                        dayTime = 70;
                        break;
                    case 2:
                        dayTime = 90;
                        break;
                    case 3:
                        dayTime = 150;
                        break;
                    case 4:
                        dayTime = 300;
                        break;
                }
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putInt(APP_PREFERENCES_DAY_TIME_POSITION, position);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinnerDayTime.setSelection(mSettings.getInt(APP_PREFERENCES_DAY_TIME_POSITION, 1));

        swith_password.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                has_password = true;
                password = mSettings.getString(APP_PREFERENCES_ROOM_PASSWORD, "");
                ET_password.setText(password);
                ET_password.setVisibility(View.VISIBLE);
            } else {
                has_password = false;
                ET_password.setVisibility(View.GONE);
            }
        });

        Set<String> set = mSettings.getStringSet(APP_PREFERENCES_ROLES, new HashSet<String>());
        for(String r : set) {
            for (int i = 0; i < list_roles.size(); i++)
            {
                if (list_roles.get(i).role == ConvertToRole(r))
                {
                    list_roles.get(i).visible = true;
                    if (list_roles.get(i).peaceful)
                    {
                        peaceful.put(list_roles.get(i).role.toString().toLowerCase());
                    }
                    else
                    {
                        mafia.put(list_roles.get(i).role.toString().toLowerCase());
                    }
                }
            }
        }

        switch (study_type)
        {
            case "game":
                new TapTargetSequence(getActivity())
                        .targets(
                                TapTarget.forView(ET_RoomName,"?? ???????????? ?????????????? ?????????? ???????? ????????????????, ???????????????????? ???????? ?????? ???????????????????????????? ??????????????????","")
                                        .outerCircleColor(R.color.orange)
                                        .outerCircleAlpha(0.96f)
                                        .targetCircleColor(R.color.white)
                                        .titleTextSize(20)
                                        .titleTextColor(R.color.white)
                                        .descriptionTextSize(10)
                                        .descriptionTextColor(R.color.black)
                                        .textColor(R.color.white)
                                        .textTypeface(Typeface.SANS_SERIF)
                                        .dimColor(R.color.black)
                                        .drawShadow(true)
                                        .cancelable(false)
                                        .tintTarget(true)
                                        .transparentTarget(true)
                                        .targetRadius(120),
                                TapTarget.forView(swith_password,"?????? ?????????? ???????????????????? ???????????? ??????????????, ?????????? ???????? ?????????? ?????????? ???????????? ????, ?????? ?????????? ????????????","")
                                        .outerCircleColor(R.color.orange)
                                        .outerCircleAlpha(0.96f)
                                        .targetCircleColor(R.color.white)
                                        .titleTextSize(20)
                                        .titleTextColor(R.color.white)
                                        .descriptionTextSize(10)
                                        .descriptionTextColor(R.color.black)
                                        .textColor(R.color.white)
                                        .textTypeface(Typeface.SANS_SERIF)
                                        .dimColor(R.color.black)
                                        .drawShadow(true)
                                        .cancelable(false)
                                        .tintTarget(true)
                                        .transparentTarget(true)
                                        .targetRadius(60),
                                TapTarget.forView(spinnerDayTime,"???????? ????????????, ???? ???????????? ?????????????? ?????????????????????????????????? ??????. ???????????? ???????? ???????????? 70 ????????????","")
                                        .outerCircleColor(R.color.orange)
                                        .outerCircleAlpha(0.96f)
                                        .targetCircleColor(R.color.white)
                                        .titleTextSize(20)
                                        .titleTextColor(R.color.white)
                                        .descriptionTextSize(10)
                                        .descriptionTextColor(R.color.black)
                                        .textColor(R.color.white)
                                        .textTypeface(Typeface.SANS_SERIF)
                                        .dimColor(R.color.black)
                                        .drawShadow(true)
                                        .cancelable(false)
                                        .tintTarget(true)
                                        .transparentTarget(true)
                                        .targetRadius(100),
                                TapTarget.forView(RSB_num_users,"???? ???????????? ?????????????? ??????. ?? ????????. ???????????????????? ?????????????? ?? ?????????? ??????????????. ???????? ???? ????????????????, ???????? ???? ?????????????????? ?????????????????????? ???????????????????? ??????????. ???????? ???? ???????????? ?????????????? ????????, ?????????????? ?????????? ?? ??????????????","")
                                        .outerCircleColor(R.color.orange)
                                        .outerCircleAlpha(0.96f)
                                        .targetCircleColor(R.color.white)
                                        .titleTextSize(20)
                                        .titleTextColor(R.color.white)
                                        .descriptionTextSize(10)
                                        .descriptionTextColor(R.color.black)
                                        .textColor(R.color.white)
                                        .textTypeface(Typeface.SANS_SERIF)
                                        .dimColor(R.color.black)
                                        .drawShadow(true)
                                        .cancelable(false)
                                        .tintTarget(true)
                                        .transparentTarget(true)
                                        .targetRadius(200),
                                TapTarget.forView(btnCustomRoom,"?????????????????? ?????????????? ???????????????? ???? 2 ??????????","")
                                        .outerCircleColor(R.color.orange)
                                        .outerCircleAlpha(0.96f)
                                        .targetCircleColor(R.color.white)
                                        .titleTextSize(20)
                                        .titleTextColor(R.color.white)
                                        .descriptionTextSize(10)
                                        .descriptionTextColor(R.color.black)
                                        .textColor(R.color.white)
                                        .textTypeface(Typeface.SANS_SERIF)
                                        .dimColor(R.color.black)
                                        .drawShadow(true)
                                        .cancelable(false)
                                        .tintTarget(true)
                                        .transparentTarget(true)
                                        .targetRadius(60),
                                TapTarget.forView(Menu,"???? ?????????? ???????????? ???????? ???????? ?????? ????????, ?? ?????????????? ???????? ?????????? ???????????? ???????????????????? ???? ???????????? ????????????. ?????????? ???????????????? ?? ??????????????????","")
                                        .outerCircleColor(R.color.notActiveText)
                                        .outerCircleAlpha(0.96f)
                                        .targetCircleColor(R.color.white)
                                        .titleTextSize(20)
                                        .titleTextColor(R.color.white)
                                        .descriptionTextSize(10)
                                        .descriptionTextColor(R.color.black)
                                        .textColor(R.color.white)
                                        .textTypeface(Typeface.SANS_SERIF)
                                        .dimColor(R.color.black)
                                        .drawShadow(true)
                                        .cancelable(false)
                                        .tintTarget(true)
                                        .transparentTarget(true)
                                        .targetRadius(60)).listener(new TapTargetSequence.Listener() {
                    @Override
                    public void onSequenceFinish() {
                        PopupMenu popup_menu = new PopupMenu(getActivity(), Menu);
                        popup_menu.inflate(R.menu.main_menu);
                        popup_menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.mainMenu_play:
                                    case R.id.mainMenu_shop:
                                    case R.id.mainMenu_friends:
                                    case R.id.mainMenu_chats:
                                    case R.id.mainMenu_settings:
                                        SettingsFragment settingsFragment = SettingsFragment.newInstance("profile");
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, settingsFragment).commit();
                                        return true;
                                }
                                return true;
                            }
                        });
                        popup_menu.show();
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                    }
                }).start();
                break;
        }

        gridView.setOnItemClickListener((parent, view1, position, id) -> {
            if (!list_roles.get(position).visible) {
                list_roles.get(position).visible = true;
                if (list_roles.get(position).peaceful)
                {
                    peaceful.put(list_roles.get(position).role.toString().toLowerCase());
                }
                else
                {
                    mafia.put(list_roles.get(position).role.toString().toLowerCase());
                }
            }
            else
            {
                list_roles.get(position).visible = false;
                if (list_roles.get(position).peaceful)
                {
                    try {
                        for (int i = 0; i < peaceful.length(); i++) {
                            if (peaceful.get(i).equals(list_roles.get(position).role.toString().toLowerCase())) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                    peaceful.remove(i);
                                }
                            }

                        }
                    }
                     catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    try {
                        for (int i = 0; i < mafia.length(); i++) {
                            if (mafia.get(i).equals(list_roles.get(position).role.toString().toLowerCase())) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                    mafia.remove(i);
                                }
                            }

                        }
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            roleAdapter.notifyDataSetChanged();
        });

        btnCustomRoom.setOnClickListener(v -> {
            if (MainActivity.Rang >= 2) {
                if (PB_loading.getVisibility() != View.VISIBLE) {
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new CreateCustomRoomFragment()).commit();
                }
            }
            else
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                View viewDang = getLayoutInflater().inflate(R.layout.dialog_error, null);
                builder.setView(viewDang);
                TextView TV_title = viewDang.findViewById(R.id.dialogError_TV_errorTitle);
                TextView TV_error = viewDang.findViewById(R.id.dialogError_TV_errorText);
                TV_title.setText("???????? ???? 2 ?????????? ????????????????!");
                TV_error.setText("?????????????????? ?? ???????????? ?? ?????????????????? ???????????????? ?????????? ???????????? ?????????? ???????????????????? 2 ??????????");
                AlertDialog alert = builder.create();
                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alert.show();
            }
        });

        RSB_num_users.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Object minValue, Object maxValue) {
                SetRoles((int) maxValue);
                //TODO: ???????????????? ???????????????????? ?????????? ?????? ???????????? ???? ??????????????????
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putInt(APP_PREFERENCES_MAX_PEOPLE, (int) maxValue);
                editor.putInt(APP_PREFERENCES_MIN_PEOPLE, (int) minValue);
                minPlayers = (int) minValue;
                maxPlayers = (int) maxValue;
                editor.apply();
            }
        });

        btnCreateRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkOnline(getContext())) {
                    name = ET_RoomName.getText().toString();
                    password = ET_password.getText().toString();
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
                    if (name.length() >= 1) {
                        if (name.length() <= 25) {
                            if (password.length() >= 1 || !has_password) {
                                BlockView();
                                Set<String> roles = new HashSet<String>();
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
                                    json.put("is_custom", false);
                                    json.put("has_password", has_password);
                                    json.put("password", password);
                                    json.put("day_time", dayTime);

                                    for (int i = 0; i < peaceful.length(); i++) {
                                        roles.add(peaceful.getString(i));
                                    }
                                    for (int i = 0; i < mafia.length(); i++) {
                                        roles.add(mafia.getString(i));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                PB_loading.setVisibility(View.VISIBLE);
                                SharedPreferences.Editor editor = mSettings.edit();
                                editor.putStringSet(APP_PREFERENCES_ROLES, roles);
                                editor.putString(APP_PREFERENCES_ROOM_NAME, name);
                                editor.putBoolean(APP_PREFERENCES_HAS_PASSWORD, has_password);
                                if (!password.equals("")) {
                                    editor.putString(APP_PREFERENCES_ROOM_PASSWORD, password);
                                }
                                editor.apply();
                                socket.emit("create_room", json);
                                Log.d("kkk", "Socket_???????????????? - create_room - " + json.toString());

                            }
                            else
                            {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                View viewDang = getLayoutInflater().inflate(R.layout.dialog_error, null);
                                builder.setView(viewDang);
                                TextView TV_title = viewDang.findViewById(R.id.dialogError_TV_errorTitle);
                                TextView TV_error = viewDang.findViewById(R.id.dialogError_TV_errorText);
                                TV_title.setText("?????????????? ???????????????? ????????????!");
                                TV_error.setText("???????????? ???????????? ?????????????????? ?????? ?????????????? 1 ????????????");
                                AlertDialog alert = builder.create();
                                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                alert.show();
                            }
                        }
                        else
                        {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            View viewDang = getLayoutInflater().inflate(R.layout.dialog_error, null);
                            builder.setView(viewDang);
                            TextView TV_title = viewDang.findViewById(R.id.dialogError_TV_errorTitle);
                            TextView TV_error = viewDang.findViewById(R.id.dialogError_TV_errorText);
                            TV_title.setText("?????????????? ?????????????? ????????????????!");
                            TV_error.setText("???????????????? ?????????????? ???????????? ???????? ???????????? 26 ????????????????!");
                            AlertDialog alert = builder.create();
                            alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            alert.show();
                        }
                    }
                    else
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        View viewDang = getLayoutInflater().inflate(R.layout.dialog_error, null);
                        builder.setView(viewDang);
                        TextView TV_title = viewDang.findViewById(R.id.dialogError_TV_errorTitle);
                        TextView TV_error = viewDang.findViewById(R.id.dialogError_TV_errorText);
                        TV_title.setText("?????????????? ???????????????? ????????????????!");
                        TV_error.setText("???????????????? ?????????????? ???????????? ?????????????????? ???????? ???? ???????? ????????????!");
                        AlertDialog alert = builder.create();
                        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        alert.show();
                    }
                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("?? ?????? ?????? ?????????????????????? ?? ??????????????????!")
                            .setMessage("")
                            .setIcon(R.drawable.ic_ban)
                            .setCancelable(false)
                            .setNegativeButton("????",
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

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PB_loading.getVisibility() != View.VISIBLE) {
                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putString(APP_PREFERENCES_ROOM_NAME, String.valueOf(ET_RoomName.getText()));
                    editor.apply();
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
                }
            }
        });

        return view;
    }

    @Override
    public void onBackPressed() {
        if (PB_loading.getVisibility() != View.VISIBLE && !blockView) {
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putString(APP_PREFERENCES_ROOM_NAME, String.valueOf(ET_RoomName.getText()));
            editor.apply();
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
        }
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
                        PB_loading.setVisibility(View.INVISIBLE);
                        MainActivity.Game_id = data.getInt("room_num");
                        MainActivity.RoomName = name;
                        MainActivity.PlayersMinMaxInfo = "???? " + minPlayers + " ???? " + maxPlayers;
                        Log.d("kkk", "???????????? - create_room: " + MainActivity.Game_id);
                        UnblockView();
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
                    UnblockView();
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    try {
                        error = data.getString("error");
                        AlertDialog alert;
                        PB_loading.setVisibility(View.INVISIBLE);
                        switch (error) {
                            case "you_are_playing_in_another_room":
                                builder.setTitle("????????????????, ???? ???? ?????????????? ?? ???????????? ????????")
                                        .setMessage("")
                                        .setIcon(R.drawable.ic_error)
                                        .setCancelable(false)
                                        .setNegativeButton("????",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.cancel();
                                                    }
                                                });
                                break;
                            case "mat_name":
                                builder.setTitle("????????????????, ???? ?????? ?????????????????????? ???????????????? ??????????????!")
                                        .setMessage("")
                                        .setIcon(R.drawable.ic_error)
                                        .setCancelable(false)
                                        .setNegativeButton("????",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.cancel();
                                                    }
                                                });
                                break;
                            default:
                                builder.setTitle("??????-???? ?????????? ???? ??????")
                                        .setMessage("")
                                        .setIcon(R.drawable.ic_error)
                                        .setCancelable(false)
                                        .setNegativeButton("????",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.cancel();
                                                    }
                                                });
                                break;
                        }
                        alert = builder.create();
                        alert.show();
                        Log.d("kkk", "Socket_?????????????? - user_error " + args[0]);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    /*
    public void SetRoles(int max_people) {
        if (list_roles.size() < max_people)
        {
            while (list_roles.size() < max_people)
            {
                list_roles.add(new RoleModel(Role.NONE, true));
            }
        }
        else
        {
            while (list_roles.size() > max_people)
            {
                list_roles.remove(list_roles.size() - 1);
            }
        }
        roleAdapter.notifyDataSetChanged();
    }
     */

    public void SetRoles(int people) {

        peaceful = new JSONArray();
        mafia = new JSONArray();
        list_roles.clear();
        switch (people)
        {
            case 5:
                list_roles.add(new RoleModel(Role.DOCTOR, true));
                break;
            case 6:
            case 7:
                list_roles.add(new RoleModel(Role.DOCTOR, true));
                list_roles.add(new RoleModel(Role.LOVER, true));
                list_roles.add(new RoleModel(Role.MAFIA_DON, false));
                break;
            case 8:
            case 9:
            case 10:
            case 11:
                list_roles.add(new RoleModel(Role.DOCTOR, true));
                list_roles.add(new RoleModel(Role.LOVER, true));
                list_roles.add(new RoleModel(Role.MAFIA_DON, false));
                list_roles.add(new RoleModel(Role.JOURNALIST, true));
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
                break;
        }
        roleAdapter.notifyDataSetChanged();

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

    //???????????????????????????? String ?? Role
    public Role ConvertToRole(String role) {
        switch (role)
        {
            case "none":
                return Role.NONE;
            case "citizen":
                return Role.CITIZEN;
            case "mafia":
                return Role.MAFIA;
            case "sheriff":
                return Role.SHERIFF;
            case "doctor":
                return Role.DOCTOR;
            case "lover":
                return Role.LOVER;
            case "mafia_don":
                return Role.MAFIA_DON;
            case "maniac":
                return Role.MANIAC;
            case "terrorist":
                return Role.TERRORIST;
            case "bodyguard":
                return Role.BODYGUARD;
            case "poisoner":
                return Role.POISONER;
            case "journalist":
                return Role.JOURNALIST;
            case "doctor_of_easy_virtue":
                return Role.DOCTOR_OF_EASY_VIRTUE;
            default:
                return Role.NONE;
        }
    }

    public void BlockView()
    {
        blockView = true;
        btnCreateRoom.setClickable(false);
        btnCustomRoom.setClickable(false);
        Menu.setClickable(false);
        btn_back.setClickable(false);
    }
    public void UnblockView()
    {
        blockView = false;
        btnCreateRoom.setClickable(true);
        btnCustomRoom.setClickable(true);
        Menu.setClickable(true);
        btn_back.setClickable(true);
    }
}