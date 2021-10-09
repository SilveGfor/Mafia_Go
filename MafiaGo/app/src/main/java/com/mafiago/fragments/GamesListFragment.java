package com.mafiago.fragments;

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
import android.opengl.Visibility;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.mafiago.MainActivity;
import com.example.mafiago.R;
import com.mafiago.adapters.GamesAdapter;
import com.mafiago.classes.OnBackPressedListener;
import com.mafiago.models.RoomModel;
import com.mafiago.models.UserModel;

import org.florescu.android.rangeseekbar.RangeSeekBar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import io.socket.emitter.Emitter;

import static android.app.Activity.RESULT_OK;
import static com.mafiago.MainActivity.USERS;
import static com.mafiago.MainActivity.socket;
import static com.mafiago.fragments.MenuFragment.GALLERY_REQUEST;

public class GamesListFragment extends Fragment implements OnBackPressedListener {

    public ListView LV_games;

    TextView TV_no_games;
    TextView TV_playersCount;

    //public Button btnExit;
    public Button btnCreateRoom;

    public ProgressBar PB_loading;

    CheckBox CB_deletePlayingRoom;
    CheckBox CB_deleteNormalRoom;
    CheckBox CB_deletePasswordRoom;
    CheckBox CB_deleteCustomRoom;

    ImageView IV_screenshot;
    ImageView Menu;
    ImageView IV_filter;

    RelativeLayout RL_filter;
    RelativeLayout RL_search;
    RelativeLayout btn_back;

    EditText ET_search;

    ImageView IV_doctor;
    ImageView IV_lover;
    ImageView IV_journalist;
    ImageView IV_bodyguard;
    ImageView IV_doctor_of_easy_virtue;
    ImageView IV_maniac;
    ImageView IV_mafia_don;
    ImageView IV_terrorist;
    ImageView IV_poisoner;

    RangeSeekBar RSB_num_users;

    View view_report;
    ImageView IV_screen;
    GamesAdapter gamesAdapter;

    String base64_screenshot = "", report_nick = "", report_id = "", room_name = "";

    ArrayList<RoomModel> list_room = new ArrayList<>();
    ArrayList<RoomModel> list_room_copy = new ArrayList<>();

    Boolean deletePlayingRooms;
    Boolean deleteNormalRooms;
    Boolean deleteCustomRooms;
    Boolean deletePasswordRooms;

    int min_people_num;
    int max_people_num;

    public JSONObject json;

    public static final String APP_PREFERENCES = "filter";
    public static final String APP_PREFERENCES_PLAYING_ROOM = "playing_room";
    public static final String APP_PREFERENCES_NORMAL_ROOM = "normal_room";
    public static final String APP_PREFERENCES_CUSTOM_ROOM = "custom_room";
    public static final String APP_PREFERENCES_PASSWORD_ROOM = "password_room";
    public static final String APP_PREFERENCES_MIN_PEOPLE = "min_people";
    public static final String APP_PREFERENCES_MAX_PEOPLE = "max_people";

    private SharedPreferences mSettings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_games_list, container, false);
        view_report = inflater.inflate(R.layout.dialog_report, container, false);
        IV_screen = view_report.findViewById(R.id.dialogReport_IV_screenshot);

        mSettings = getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        USERS = new JSONObject();

        CB_deletePlayingRoom = view.findViewById(R.id.fragmentGamesList_CB_playingRoom);
        CB_deleteNormalRoom = view.findViewById(R.id.fragmentGamesList_CB_normalRoom);
        CB_deletePasswordRoom = view.findViewById(R.id.fragmentGamesList_CB_passwordRoom);
        CB_deleteCustomRoom = view.findViewById(R.id.fragmentGamesList_CB_customRoom);
        RL_filter = view.findViewById(R.id.fragmentGamesList_RL_filter);
        RL_search = view.findViewById(R.id.fragmentGamesList_RL_search);
        RSB_num_users = view.findViewById(R.id.fragmentGamesList_RSB);
        IV_filter = view.findViewById(R.id.fragmentGamesList_IV_filter);
        btn_back = view.findViewById(R.id.fragmentGamesList_RL_back);
        ET_search = view.findViewById(R.id.fragmentGamesList_ET_search);

        IV_doctor = view.findViewById(R.id.fragmentGamesList_IV_doctor);
        IV_lover = view.findViewById(R.id.fragmentGamesList_IV_lover);
        IV_journalist = view.findViewById(R.id.fragmentGamesList_IV_journalist);
        IV_bodyguard = view.findViewById(R.id.fragmentGamesList_IV_bodyguard);
        IV_doctor_of_easy_virtue = view.findViewById(R.id.fragmentGamesList_IV_doctor_of_easy_virtue);
        IV_maniac = view.findViewById(R.id.fragmentGamesList_IV_maniac);
        IV_mafia_don = view.findViewById(R.id.fragmentGamesList_IV_mafia_don);
        IV_terrorist = view.findViewById(R.id.fragmentGamesList_IV_terrorist);
        IV_poisoner = view.findViewById(R.id.fragmentGamesList_IV_poisoner);

        LV_games = view.findViewById(R.id.fragmentGamesList_LV_games);
        btnCreateRoom = view.findViewById(R.id.fragmentGamesList_btn_create_room);
        //btnExit = view.findViewById(R.id.btnExitGamesList);
        TV_no_games = view.findViewById(R.id.fragmentGamesList_TV_no_games);
        PB_loading = view.findViewById(R.id.fragmentGamesList_PB_loading);
        TV_playersCount = view.findViewById(R.id.fragmentGamesList_playersCount);

        IV_screenshot = view_report.findViewById(R.id.dialogReport_IV_screenshot);
        Menu = view.findViewById(R.id.fragmentMenu_IV_menu);

        gamesAdapter = new GamesAdapter(list_room_copy, getContext());
        LV_games.setAdapter(gamesAdapter);

        CB_deletePlayingRoom.setOnCheckedChangeListener((buttonView, isChecked) -> {
            deletePlayingRooms = isChecked;
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putBoolean(APP_PREFERENCES_PLAYING_ROOM, deletePlayingRooms);
            editor.apply();
            setFilter(room_name, deletePlayingRooms, deleteNormalRooms, deleteCustomRooms, deletePasswordRooms, min_people_num, max_people_num);
        });
        CB_deleteNormalRoom.setOnCheckedChangeListener((buttonView, isChecked) -> {
            deleteNormalRooms = isChecked;
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putBoolean(APP_PREFERENCES_NORMAL_ROOM, deleteNormalRooms);
            editor.apply();
            setFilter(room_name, deletePlayingRooms, deleteNormalRooms, deleteCustomRooms, deletePasswordRooms, min_people_num, max_people_num);
        });
        CB_deleteCustomRoom.setOnCheckedChangeListener((buttonView, isChecked) -> {
            deleteCustomRooms = isChecked;
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putBoolean(APP_PREFERENCES_CUSTOM_ROOM, deleteCustomRooms);
            editor.apply();
            setFilter(room_name, deletePlayingRooms, deleteNormalRooms, deleteCustomRooms, deletePasswordRooms, min_people_num, max_people_num);
        });
        CB_deletePasswordRoom.setOnCheckedChangeListener((buttonView, isChecked) -> {
            deletePasswordRooms = isChecked;
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putBoolean(APP_PREFERENCES_PASSWORD_ROOM, deletePasswordRooms);
            editor.apply();
            setFilter(room_name, deletePlayingRooms, deleteNormalRooms, deleteCustomRooms, deletePasswordRooms, min_people_num, max_people_num);
        });

        deletePlayingRooms = mSettings.getBoolean(APP_PREFERENCES_PLAYING_ROOM, false);
        deleteNormalRooms = mSettings.getBoolean(APP_PREFERENCES_NORMAL_ROOM, false);
        deleteCustomRooms = mSettings.getBoolean(APP_PREFERENCES_CUSTOM_ROOM, false);
        deletePasswordRooms = mSettings.getBoolean(APP_PREFERENCES_PASSWORD_ROOM, false);
        max_people_num = mSettings.getInt(APP_PREFERENCES_MAX_PEOPLE, 20);
        min_people_num = mSettings.getInt(APP_PREFERENCES_MIN_PEOPLE, 5);
        IV_filter.setImageResource(R.drawable.ic_arrow_bottom);

        setFilter(room_name, deletePlayingRooms, deleteNormalRooms, deleteCustomRooms, deletePasswordRooms, min_people_num, max_people_num);

        RSB_num_users.setSelectedMaxValue(max_people_num);
        RSB_num_users.setSelectedMinValue(min_people_num);
        CB_deletePlayingRoom.setChecked(deletePlayingRooms);
        CB_deleteNormalRoom.setChecked(deleteNormalRooms);
        CB_deleteCustomRoom.setChecked(deleteCustomRooms);
        CB_deletePasswordRoom.setChecked(deletePasswordRooms);

        RL_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CB_deleteCustomRoom.getVisibility() != View.VISIBLE) {
                    IV_filter.setImageResource(R.drawable.ic_arrow_top);

                    TV_playersCount.setVisibility(View.VISIBLE);
                    CB_deletePlayingRoom.setVisibility(View.VISIBLE);
                    CB_deleteNormalRoom.setVisibility(View.VISIBLE);
                    CB_deletePasswordRoom.setVisibility(View.VISIBLE);
                    CB_deleteCustomRoom.setVisibility(View.VISIBLE);
                    RSB_num_users.setVisibility(View.VISIBLE);
                    /*
                    IV_doctor.setVisibility(View.VISIBLE);
                    IV_lover.setVisibility(View.VISIBLE);
                    IV_journalist.setVisibility(View.VISIBLE);
                    IV_bodyguard.setVisibility(View.VISIBLE);
                    IV_doctor_of_easy_virtue.setVisibility(View.VISIBLE);
                    IV_maniac.setVisibility(View.VISIBLE);
                    IV_mafia_don.setVisibility(View.VISIBLE);
                    IV_terrorist.setVisibility(View.VISIBLE);
                    IV_poisoner.setVisibility(View.VISIBLE);
                     */
                }
                else
                {
                    IV_filter.setImageResource(R.drawable.ic_arrow_bottom);

                    TV_playersCount.setVisibility(View.GONE);
                    CB_deletePlayingRoom.setVisibility(View.GONE);
                    CB_deleteNormalRoom.setVisibility(View.GONE);
                    CB_deletePasswordRoom.setVisibility(View.GONE);
                    CB_deleteCustomRoom.setVisibility(View.GONE);
                    RSB_num_users.setVisibility(View.GONE);
                    /*
                    IV_doctor.setVisibility(View.GONE);
                    IV_lover.setVisibility(View.GONE);
                    IV_journalist.setVisibility(View.GONE);
                    IV_bodyguard.setVisibility(View.GONE);
                    IV_doctor_of_easy_virtue.setVisibility(View.GONE);
                    IV_maniac.setVisibility(View.GONE);
                    IV_mafia_don.setVisibility(View.GONE);
                    IV_terrorist.setVisibility(View.GONE);
                    IV_poisoner.setVisibility(View.GONE);
                     */
                }
            }
        });

        RSB_num_users.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Object minValue, Object maxValue) {
                max_people_num = (int) maxValue;
                min_people_num = (int) minValue;

                SharedPreferences.Editor editor = mSettings.edit();
                editor.putInt(APP_PREFERENCES_MAX_PEOPLE, (int) maxValue);
                editor.putInt(APP_PREFERENCES_MIN_PEOPLE, (int) minValue);
                editor.apply();

                setFilter(room_name, deletePlayingRooms, deleteNormalRooms, deleteCustomRooms, deletePasswordRooms, min_people_num, max_people_num);
            }
        });

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
                                if ( getActivity() != null) {
                                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
                                }
                                return true;
                            case R.id.mainMenu_shop:
                                if ( getActivity() != null) {
                                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new ShopFragment()).commit();
                                }
                                return true;
                            case R.id.mainMenu_friends:
                                if ( getActivity() != null) {
                                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new FriendsFragment()).commit();
                                }
                                return true;
                            case R.id.mainMenu_chats:
                                if ( getActivity() != null) {
                                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new PrivateChatsFragment()).commit();
                                }
                                return true;
                            case R.id.mainMenu_settings:
                                if ( getActivity() != null) {
                                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new SettingsFragment()).commit();
                                    return true;
                                }
                        }
                        return true;
                    }
                });
                popup_menu.show();
            }
        });

        PB_loading.setVisibility(View.VISIBLE);
        TV_no_games.setVisibility(View.GONE);

        //socket.off("connect");
        //socket.off("disconnect");
        socket.off("add_room_to_list_of_rooms");
        socket.off("delete_room_from_list_of_rooms");
        socket.off("update_list_of_rooms");
        socket.off("get_profile");
        socket.off("send_complaint");

        socket.on("connect", onConnect);
        socket.on("disconnect", onDisconnect);
        socket.on("add_room_to_list_of_rooms", onNewRoom);
        socket.on("delete_room_from_list_of_rooms", onDeleteRoom);
        socket.on("update_list_of_rooms", onUpdateRoom);
        socket.on("get_profile", OnGetProfile);
        socket.on("send_complaint", onSendComplain);

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
/*
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new MenuFragment()).commit();
            }
        });
 */

        LV_games.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (MainActivity.Rang >= 2 || !list_room_copy.get(position).is_custom) {
                    MainActivity.Game_id = list_room_copy.get(position).id;
                    MainActivity.RoomName = list_room_copy.get(position).name;
                    Log.d("kkk", "Переход в игру - " + MainActivity.Game_id);
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GameFragment()).commit();
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    View viewDang = getLayoutInflater().inflate(R.layout.dialog_error, null);
                    builder.setView(viewDang);
                    TextView TV_title = viewDang.findViewById(R.id.dialogError_TV_errorTitle);
                    TextView TV_error = viewDang.findViewById(R.id.dialogError_TV_errorText);
                    TV_title.setText("Вход до 2 ранга запрещён!");
                    TV_error.setText("Создавать и играть в кастомных комнатах можно только после достижения 2 ранга");
                    AlertDialog alert = builder.create();
                    alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alert.show();
                }

            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CB_deleteCustomRoom.getVisibility() == View.VISIBLE) {
                    IV_filter.setImageResource(R.drawable.ic_arrow_bottom);

                    TV_playersCount.setVisibility(View.GONE);
                    CB_deletePlayingRoom.setVisibility(View.GONE);
                    CB_deleteNormalRoom.setVisibility(View.GONE);
                    CB_deletePasswordRoom.setVisibility(View.GONE);
                    CB_deleteCustomRoom.setVisibility(View.GONE);
                    RSB_num_users.setVisibility(View.GONE);
            /*
            IV_doctor.setVisibility(View.GONE);
            IV_lover.setVisibility(View.GONE);
            IV_journalist.setVisibility(View.GONE);
            IV_bodyguard.setVisibility(View.GONE);
            IV_doctor_of_easy_virtue.setVisibility(View.GONE);
            IV_maniac.setVisibility(View.GONE);
            IV_mafia_don.setVisibility(View.GONE);
            IV_terrorist.setVisibility(View.GONE);
            IV_poisoner.setVisibility(View.GONE);
             */
                }
                else {
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new MenuFragment()).commit();
                }
            }
        });

        ET_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                room_name = ET_search.getText().toString();
                setFilter(room_name, deletePlayingRooms, deleteNormalRooms, deleteCustomRooms, deletePasswordRooms, min_people_num, max_people_num);
            }
        });

        return view;
    }

    @Override
    public void onBackPressed() {
        if (CB_deleteCustomRoom.getVisibility() == View.VISIBLE) {
            IV_filter.setImageResource(R.drawable.ic_arrow_bottom);

            TV_playersCount.setVisibility(View.GONE);
            CB_deletePlayingRoom.setVisibility(View.GONE);
            CB_deleteNormalRoom.setVisibility(View.GONE);
            CB_deletePasswordRoom.setVisibility(View.GONE);
            CB_deleteCustomRoom.setVisibility(View.GONE);
            RSB_num_users.setVisibility(View.GONE);
            /*
            IV_doctor.setVisibility(View.GONE);
            IV_lover.setVisibility(View.GONE);
            IV_journalist.setVisibility(View.GONE);
            IV_bodyguard.setVisibility(View.GONE);
            IV_doctor_of_easy_virtue.setVisibility(View.GONE);
            IV_maniac.setVisibility(View.GONE);
            IV_mafia_don.setVisibility(View.GONE);
            IV_terrorist.setVisibility(View.GONE);
            IV_poisoner.setVisibility(View.GONE);
             */
        }
        else {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new MenuFragment()).commit();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case GALLERY_REQUEST:
                if(resultCode == RESULT_OK) {
                    Uri uri = imageReturnedIntent.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        byte[] bytes = stream.toByteArray();

                        base64_screenshot = Base64.encodeToString(bytes, Base64.DEFAULT);

                        IV_screen.setImageBitmap(fromBase64(base64_screenshot));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        }
    }

    public Bitmap fromBase64(String image) {
        // Декодируем строку Base64 в массив байтов
        byte[] decodedString = Base64.decode(image, Base64.DEFAULT);

        // Декодируем массив байтов в изображение
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        // Помещаем изображение в ImageView
        return decodedByte;
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
                                setFilter(room_name, deletePlayingRooms, deleteNormalRooms, deleteCustomRooms, deletePasswordRooms, min_people_num, max_people_num);
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
                        ArrayList<String> list_roles = new ArrayList<>();
                        Boolean alive = true, is_on = false, is_custom = false, has_password = false;
                        int id = 0;
                        int min_people = 0;
                        int max_people = 0;
                        int num_people = 0;
                        JSONObject users = new JSONObject();
                        JSONArray roles = new JSONArray();
                        Log.d("kkk", "принял - add_room_to_list_of_rooms - " + data);
                        TV_no_games.setVisibility(View.GONE);
                        try {
                            name = data.getString("name");
                            id = data.getInt("num");
                            is_on = data.getBoolean("is_on");
                            is_custom = data.getBoolean("is_custom");
                            has_password = data.getBoolean("has_password");
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
                            roles = data.getJSONArray("roles");
                            for (int i = 0; i < roles.length(); i++)
                            {
                                list_roles.add(roles.get(i).toString());
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
                            RoomModel model = new RoomModel(name, min_people, max_people, num_people, id, list_users, is_on, list_roles, is_custom, has_password);
                            list_room.add(model);
                            setFilter(room_name, deletePlayingRooms, deleteNormalRooms, deleteCustomRooms, deletePasswordRooms, min_people_num, max_people_num);
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
                    ArrayList<String> list_roles = new ArrayList<>();
                    Boolean alive = true, is_on = false, is_custom = false, has_password = false;
                    JSONObject users = new JSONObject();
                    JSONArray roles = new JSONArray();
                    int num;
                    int min_people;
                    int max_people;
                    int num_people;
                    int id;
                    Log.d("kkk", "принял - update_room - " + data);
                    try {
                        name = data.getString("name");
                        is_on = data.getBoolean("is_on");
                        is_custom = data.getBoolean("is_custom");
                        has_password = data.getBoolean("has_password");
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
                        roles = data.getJSONArray("roles");
                        for (int i = 0; i < roles.length(); i++)
                        {
                            list_roles.add(roles.get(i).toString());
                        }
                        for(int i = 0; i< list_room.size(); i++) {
                            if (list_room.get(i).id == id)
                            {
                                RoomModel model = new RoomModel(name, min_people, max_people, num_people, id, list_users, is_on, list_roles, is_custom, has_password);
                                list_room.set(i, model);
                                setFilter(room_name, deletePlayingRooms, deleteNormalRooms, deleteCustomRooms, deletePasswordRooms, min_people_num, max_people_num);
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
                String nick = "", user_id_2 = "", avatar = "";
                int playing_room_num, money = 0, exp = 0, gold = 0, rang = 0;
                boolean online = false;
                JSONObject statistic = new JSONObject();
                int game_counter = 0, max_money_score = 0, max_exp_score = 0;
                String general_pers_of_wins = "", mafia_pers_of_wins = "", peaceful_pers_of_wins = "";

                try {
                    statistic = data.getJSONObject("statistics");
                    game_counter = statistic.getInt("game_counter");
                    if (data.has("gold"))
                    {
                        gold = data.getInt("gold");
                        money = data.getInt("money");
                    }
                    max_money_score = statistic.getInt("max_money_score");
                    max_exp_score = statistic.getInt("max_exp_score");
                    general_pers_of_wins = statistic.getString("general_pers_of_wins");
                    mafia_pers_of_wins = statistic.getString("mafia_pers_of_wins");
                    peaceful_pers_of_wins = statistic.getString("peaceful_pers_of_wins");
                    online = data.getBoolean("is_online");
                    nick = data.getString("nick");
                    avatar = data.getString("avatar");
                    user_id_2 = data.getString("user_id");
                    exp = data.getInt("exp");
                    rang = data.getInt("rang");
                    if (data.has("playing_room_num")) playing_room_num = data.getInt("playing_room_num");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (!nick.equals(MainActivity.NickName)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    View view_profile = getLayoutInflater().inflate(R.layout.item_profile, null);
                    builder.setView(view_profile);
                    AlertDialog alert = builder.create();

                    Button btn_add_friend = view_profile.findViewById(R.id.dialogOkNo_btn_no);
                    Button btn_kick = view_profile.findViewById(R.id.itemProfile_btn_kickFromRoom);
                    btn_kick.setVisibility(View.GONE);
                    Button btn_send_message = view_profile.findViewById(R.id.itemGold_btn_buy);
                    Button btn_report = view_profile.findViewById(R.id.dialogOkNo_btn_yes);
                    ImageView IV_avatar = view_profile.findViewById(R.id.itemProfile_IV_avatar);
                    TextView TV_nick = view_profile.findViewById(R.id.itemProfile_TV_nick);

                    TextView TV_exp = view_profile.findViewById(R.id.itemDailyTask_TV_prize);
                    TextView TV_rang = view_profile.findViewById(R.id.itemProfile_TV_rang);
                    TextView TV_game_counter = view_profile.findViewById(R.id.itemProfile_TV_gamesCouner);
                    TextView TV_max_money_score = view_profile.findViewById(R.id.itemProfile_TV_maxMoney);
                    TextView TV_max_exp_score = view_profile.findViewById(R.id.itemProfile_TV_maxExp);
                    TextView TV_general_pers_of_wins = view_profile.findViewById(R.id.itemProfile_TV_percentWins);
                    TextView TV_mafia_pers_of_wins = view_profile.findViewById(R.id.itemProfile_TV_percentMafiaWins);
                    TextView TV_peaceful_pers_of_wins = view_profile.findViewById(R.id.itemProfile_TV_percentPeacefulWins);
                    TextView TV_onlineOffline = view_profile.findViewById(R.id.itemDailyTask_TV_description);

                    TV_nick.setText(nick);
                    TV_exp.setText(exp + " XP");
                    TV_rang.setText(rang + " ранг");
                    TV_game_counter.setText("Сыграно игр " + game_counter);
                    TV_max_money_score.setText("Макс. монет за игру " + max_money_score);
                    TV_max_exp_score.setText("Макс. опыта за игру " + max_exp_score);
                    TV_general_pers_of_wins.setText("Процент побед " + general_pers_of_wins);
                    TV_mafia_pers_of_wins.setText("Побед за мафию " + mafia_pers_of_wins);
                    TV_peaceful_pers_of_wins.setText("Побед за мирных " + peaceful_pers_of_wins);

                    if (!online) {
                        TV_onlineOffline.setText("не в сети");
                    }

                    final String[] reason = {""};

                    if (avatar != null && !avatar.equals("") && !avatar.equals("null")) {
                        IV_avatar.setImageBitmap(fromBase64(avatar));
                    }

                    String finalAvatar = avatar;
                    IV_avatar.setOnClickListener(v12 -> {
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
                        View view_avatar = getLayoutInflater().inflate(R.layout.dialog_avatar, null);
                        builder2.setView(view_avatar);

                        ImageView IV_dialog_avatar = view_avatar.findViewById(R.id.dialogAvatar_avatar);
                        Button btn_exit_avatar = view_avatar.findViewById(R.id.dialogAvatar_btn_exit);

                        IV_dialog_avatar.setImageBitmap(fromBase64(finalAvatar));

                        AlertDialog alert2 = builder2.create();

                        btn_exit_avatar.setOnClickListener(v13 -> {
                            alert2.cancel();
                        });

                        alert2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        alert2.show();
                    });

                    String finalNick = nick;
                    String finalUser_id_ = user_id_2;
                    btn_send_message.setOnClickListener(v -> {
                        alert.cancel();
                        MainActivity.User_id_2 = finalUser_id_;
                        MainActivity.NickName_2 = finalNick;
                        MainActivity.bitmap_avatar_2 = fromBase64(finalAvatar);
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new PrivateMessagesFragment()).commit();
                    });

                    btn_report.setOnClickListener(v1 -> {
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
                        if (view_report.getParent() != null) {
                            ((ViewGroup) view_report.getParent()).removeView(view_report);
                        }
                        builder2.setView(view_report);
                        AlertDialog alert2 = builder2.create();

                        Button btn_addScreenshot = view_report.findViewById(R.id.dialogReport_btn_add_screenshot);
                        Button btn_sendReport = view_report.findViewById(R.id.dialogReport_btn_report);
                        EditText ET_reportMessage = view_report.findViewById(R.id.dialogReport_ET_report);

                        RadioGroup radioGroup = view_report.findViewById(R.id.dialogReport_RG);

                        btn_addScreenshot.setOnClickListener(v2 -> {
                            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                            photoPickerIntent.setType("image/*");
                            startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
                        });

                        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(RadioGroup group, int checkedId) {
                                switch (checkedId) {
                                    case -1:
                                        break;
                                    case R.id.dialogReport_RB_1:
                                        reason[0] = "спам или флуд";
                                        break;
                                    case R.id.dialogReport_RB_2:
                                        reason[0] = "размещение материалов рекламного, эротического, порнографического или оскорбительного характера или иной информации, размещение которой запрещено или противоречит нормам действующего законодательства РФ";
                                        break;
                                    case R.id.dialogReport_RB_3:
                                        reason[0] = "распространение информации, которая направлена на пропаганду войны, разжигание национальной, расовой или религиозной ненависти и вражды или иной информации, за распространение которой предусмотрена уголовная или административная ответственность";
                                        break;
                                    case R.id.dialogReport_RB_4:
                                        reason[0] = "игра против/не в интересах своей команды";
                                        break;
                                    case R.id.dialogReport_RB_5:
                                        reason[0] = "фарм (т.е. ведение игры организованной группой лиц, цель которой направлена на быстрое извлечение прибыли вне зависимости от того, кто из участников группы победит)";
                                        break;
                                    case R.id.dialogReport_RB_6:
                                        reason[0] = "создание нескольких учётных записей в Приложении, фактически принадлежащих одному и тому же лицу";
                                        break;
                                    case R.id.dialogReport_RB_7:
                                        reason[0] = "совершение действий, направленный на введение других Пользователей в заблуждение (не касается игрового процесса)";
                                        break;
                                    case R.id.dialogReport_RB_8:
                                        reason[0] = "модератор/администратор злоупотребляет своими полномочиями или положением";
                                        break;
                                    case R.id.dialogReport_RB_9:
                                        reason[0] = "другое";
                                        break;

                                    default:
                                        break;
                                }
                            }
                        });

                        btn_sendReport.setOnClickListener(v22 -> {
                            if (!reason[0].equals("") && !ET_reportMessage.equals("") && !base64_screenshot.equals("")) {
                                report_nick = finalNick;
                                report_id = finalUser_id_;
                                final JSONObject json2 = new JSONObject();
                                try {
                                    json2.put("nick", MainActivity.NickName);
                                    json2.put("session_id", MainActivity.Session_id);
                                    json2.put("against_id", report_id);
                                    json2.put("against_nick", report_nick);
                                    json2.put("reason", reason[0]);
                                    json2.put("comment", ET_reportMessage.getText());
                                    json2.put("image", base64_screenshot);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                radioGroup.clearCheck();
                                IV_screen.setImageDrawable(null);
                                ET_reportMessage.setText("");
                                base64_screenshot = "";
                                socket.emit("send_complaint", json2);
                                Log.d("kkk", "Socket_отправка - send_complaint" + json2);
                                alert2.cancel();
                            }
                            else
                            {
                                AlertDialog.Builder builder3 = new AlertDialog.Builder(getActivity());
                                View viewError = getLayoutInflater().inflate(R.layout.dialog_error, null);
                                builder3.setView(viewError);
                                TextView TV_error = viewError.findViewById(R.id.dialogError_TV_errorText);
                                AlertDialog alert3;
                                TV_error.setText("Заполните все поля!");
                                alert3 = builder3.create();
                                alert3.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                alert3.show();
                            }
                        });

                        alert2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        alert2.show();
                    });

                    String finalUser_id_2 = user_id_2;
                    btn_add_friend.setOnClickListener(v1 -> {
                        json = new JSONObject();
                        try {
                            json.put("nick", MainActivity.NickName);
                            json.put("session_id", MainActivity.Session_id);
                            json.put("user_id_2", finalUser_id_2);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        socket.emit("friend_request", json);
                        Log.d("kkk", "Socket_отправка - friend_request" + json.toString());
                    });

                    alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alert.show();
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    View view_profile = getLayoutInflater().inflate(R.layout.dialog_my_profile, null);
                    builder.setView(view_profile);

                    TextView TV_money = view_profile.findViewById(R.id.dialogMyProfile_TV_money);
                    TextView TV_exp = view_profile.findViewById(R.id.dialogMyProfile_TV_exp);
                    TextView TV_gold = view_profile.findViewById(R.id.dialogMyProfile_TV_gold);
                    TextView TV_rang = view_profile.findViewById(R.id.dialogMyProfile_TV_rang);
                    ImageView IV_avatar = view_profile.findViewById(R.id.dialogMyProfile_IV_avatar);
                    TextView TV_nick = view_profile.findViewById(R.id.dialogMyProfile_TV_nick);

                    TextView TV_game_counter = view_profile.findViewById(R.id.dialogMyProfile_TV_gamesCouner);
                    TextView TV_max_money_score = view_profile.findViewById(R.id.dialogMyProfile_TV_maxMoney);
                    TextView TV_max_exp_score = view_profile.findViewById(R.id.dialogMyProfile_TV_maxExp);
                    TextView TV_general_pers_of_wins = view_profile.findViewById(R.id.dialogMyProfile_TV_percentWins);
                    TextView TV_mafia_pers_of_wins = view_profile.findViewById(R.id.dialogMyProfile_TV_percentMafiaWins);
                    TextView TV_peaceful_pers_of_wins = view_profile.findViewById(R.id.dialogMyProfile_TV_percentPeacefulWins);

                    if (avatar != null && !avatar.equals("") && !avatar.equals("null")) {
                        IV_avatar.setImageBitmap(fromBase64(avatar));
                    }

                    String finalAvatar1 = avatar;
                    IV_avatar.setOnClickListener(v12 -> {
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
                        View view_avatar = getLayoutInflater().inflate(R.layout.dialog_avatar, null);
                        builder2.setView(view_avatar);

                        ImageView IV_dialog_avatar = view_avatar.findViewById(R.id.dialogAvatar_avatar);
                        Button btn_exit_avatar = view_avatar.findViewById(R.id.dialogAvatar_btn_exit);

                        if (finalAvatar1 != null && !finalAvatar1.equals("") && !finalAvatar1.equals("null")) {
                            IV_dialog_avatar.setImageBitmap(fromBase64(finalAvatar1));
                        }

                        AlertDialog alert2 = builder2.create();

                        btn_exit_avatar.setOnClickListener(v13 -> {
                            alert2.cancel();
                        });

                        alert2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        alert2.show();
                    });

                    TV_game_counter.setText("Сыграно игр " + game_counter);
                    TV_max_money_score.setText("Макс. монет за игру " + max_money_score);
                    TV_max_exp_score.setText("Макс. опыта за игру " + max_exp_score);
                    TV_general_pers_of_wins.setText("Процент побед " + general_pers_of_wins);
                    TV_mafia_pers_of_wins.setText("Побед за мафию " + mafia_pers_of_wins);
                    TV_peaceful_pers_of_wins.setText("Побед за мирных " + peaceful_pers_of_wins);

                    TV_gold.setText(gold + " золота");
                    TV_money.setText(money + " $");
                    TV_exp.setText(exp + " XP");
                    TV_rang.setText(rang + " ранг");
                    TV_nick.setText(nick);

                    AlertDialog alert = builder.create();
                    alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alert.show();
                }
            }
        });
    };

    private final Emitter.Listener onSendComplain = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject data = (JSONObject) args[0];
                Log.d("kkk", "принял - send_complain - " + data);
                String status = "";
                try {
                    status = data.getString("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (status.equals("complaints_limit_exceeded"))
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Вы превысили лимит жалоб!")
                            .setMessage("")
                            .setIcon(R.drawable.ic_error)
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
    };

    public void setFilter(String name, boolean deletePlaying, boolean deleteNormal, boolean deleteCustom, boolean deletePassword, int min, int max)
    {
        list_room_copy.clear();
        list_room_copy.addAll(0, list_room);
        Log.d("kkk", "setFilter - " + deletePlaying + " " + deleteNormal + " " + deleteCustom + " " + deletePassword + " " + min + " " + max);
        Log.d("kkk", "0 - " + list_room_copy.size() + " " + list_room.size());

        for (int i = 0; i < list_room_copy.size(); i++)
        {
            if (deletePlaying && list_room_copy.get(i).is_on)
            {
                Log.d("kkk", "1 deletePlaying i = " + i + " size() = " + list_room_copy.size());
                list_room_copy.remove(i);
                i--;
                Log.d("kkk", "2 deletePlaying i = " + i + " size() = " + list_room_copy.size());
            }
            else if (deleteNormal && (!list_room_copy.get(i).is_custom && !list_room_copy.get(i).has_password))
            {
                Log.d("kkk", "1 deleteNormal i = " + i + " size() = " + list_room_copy.size());
                list_room_copy.remove(i);
                i--;
                Log.d("kkk", "2 deleteNormal i = " + i + " size() = " + list_room_copy.size());
            }
            else if (deleteCustom && list_room_copy.get(i).is_custom)
            {
                Log.d("kkk", "1 deleteCustom i = " + i + " size() = " + list_room_copy.size());
                list_room_copy.remove(i);
                i--;
                Log.d("kkk", "2 deleteCustom i = " + i + " size() = " + list_room_copy.size());
            }
            else if (deletePassword && list_room_copy.get(i).has_password)
            {
                Log.d("kkk", "1 deletePassword i = " + i + " size() = " + list_room_copy.size());
                list_room_copy.remove(i);
                i--;
                Log.d("kkk", "1 deletePassword i = " + i + " size() = " + list_room_copy.size());
            }
            else if (min > list_room_copy.get(i).min_people || max < list_room_copy.get(i).max_people)
            {
                Log.d("kkk", "1 MinMax i = " + i + " size() = " + list_room_copy.size() + " min = " + min + " " + max);
                list_room_copy.remove(i);
                i--;
                Log.d("kkk", "1 MinMax i = " + i + " size() = " + list_room_copy.size() + " min = " + min + " " + max);
            }
            else if (!name.equals(""))
            Log.d("kkk", "0 - " + list_room_copy.size() + " " + list_room.size());
        }
        Log.d("kkk", "finish - " + list_room_copy.size() + " " + list_room.size());
        if (list_room_copy.size() != 0) TV_no_games.setVisibility(View.GONE);
        else TV_no_games.setVisibility(View.VISIBLE);
        gamesAdapter.notifyDataSetChanged();
    }
}