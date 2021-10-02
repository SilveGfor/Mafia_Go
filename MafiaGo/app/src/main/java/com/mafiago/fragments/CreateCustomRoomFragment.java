package com.mafiago.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.mafiago.R;
import com.mafiago.MainActivity;
import com.mafiago.adapters.CustomRolesAdapter;
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

import static com.mafiago.MainActivity.f;
import static com.mafiago.MainActivity.socket;

public class CreateCustomRoomFragment extends Fragment implements OnBackPressedListener {

    EditText ET_RoomName;

    RangeSeekBar RSB_num_users;

    Button btnCreateRoom;
    TextView TV_question;
    SwitchCompat swith_password;
    EditText ET_password;
    ImageView Menu;
    RelativeLayout btn_back;

    public android.widget.GridView gridView;

    String name;
    int max_people;
    int min_people;
    String password = "";
    boolean has_password = false;

    ArrayList<RoleModel> list_roles = new ArrayList<>();

    CustomRolesAdapter roleAdapter;

    public static final String APP_PREFERENCES = "create_custom_room";
    public static final String APP_PREFERENCES_ROOM_NAME = "room_name";
    public static final String APP_PREFERENCES_HAS_PASSWORD = "has_password";
    public static final String APP_PREFERENCES_ROOM_PASSWORD = "password";
    public static final String APP_PREFERENCES_MAX_PEOPLE = "max_people";
    public static final String APP_PREFERENCES_MIN_PEOPLE = "min_people";
    public static final String APP_PREFERENCES_ROLES = "roles";

    private SharedPreferences mSettings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_custom_room, container, false);

        mSettings = getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        ET_RoomName = view.findViewById(R.id.fragmentCreateCustomRoom_ET_roomName);
        TV_question = view.findViewById(R.id.fragmentCreateCustomRoom_TV_question);
        swith_password = view.findViewById(R.id.fragmentCreateCustomRoom_Swith_password);
        ET_password = view.findViewById(R.id.fragmentCreateCustomRoom_ET_password);
        btn_back = view.findViewById(R.id.fragmentGamesList_RL_back);

        RSB_num_users = view.findViewById(R.id.fragmentCreateCustomRoom_PSB_playerNum);

        btnCreateRoom = view.findViewById(R.id.fragmentCreateCustomRoom_btn_createRoom);

        gridView = view.findViewById(R.id.fragmentCreateCustomRoom_GV_roles);
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



        roleAdapter = new CustomRolesAdapter(list_roles, getContext());
        gridView.setAdapter(roleAdapter);

        name = "";
        max_people = mSettings.getInt(APP_PREFERENCES_MAX_PEOPLE, 8);
        min_people = mSettings.getInt(APP_PREFERENCES_MIN_PEOPLE, 5);
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
        SetRoles(min_people, max_people);

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

        for(int i = 0; i < list_roles.size(); i++) {
            String role = mSettings.getString("role_" + i ,"");
            switch (role)
            {
                case "citizen":
                    list_roles.set(i, new RoleModel(Role.CITIZEN, true));
                    break;
                case "mafia":
                    list_roles.set(i, new RoleModel(Role.MAFIA, false));
                    break;
                case "sheriff":
                    list_roles.set(i, new RoleModel(Role.SHERIFF, true));
                    break;
                case "doctor":
                    list_roles.set(i, new RoleModel(Role.DOCTOR, true));
                    break;
                case "lover":
                    list_roles.set(i, new RoleModel(Role.LOVER, true));
                    break;
                case "mafia_don":
                    list_roles.set(i, new RoleModel(Role.MAFIA_DON, false));
                    break;
                case "maniac":
                    list_roles.set(i, new RoleModel(Role.MANIAC, true));
                    break;
                case "terrorist":
                    list_roles.set(i, new RoleModel(Role.TERRORIST, false));
                    break;
                case "bodyguard":
                    list_roles.set(i, new RoleModel(Role.BODYGUARD, true));
                    break;
                case "poisoner":
                    list_roles.set(i, new RoleModel(Role.POISONER, false));
                    break;
                case "journalist":
                    list_roles.set(i, new RoleModel(Role.JOURNALIST, true));
                    break;
                case "doctor_of_easy_virtue":
                    list_roles.set(i, new RoleModel(Role.DOCTOR_OF_EASY_VIRTUE, true));
                    break;
            }
        }
        roleAdapter.notifyDataSetChanged();

        TV_question.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            View viewDang = getLayoutInflater().inflate(R.layout.dialog_information, null);
            builder.setView(viewDang);
            TextView TV_title = viewDang.findViewById(R.id.dialogInformation_TV_title);
            TextView TV_text = viewDang.findViewById(R.id.dialogInformation_TV_text);
            TV_title.setText("Кастомные игры");
            TV_text.setText("Это игры, где вы сами выбираете количество ролей в комнате. Каждая карточка - это условно игрок. Зелёные карточки - минимальное количество игроков. Если вы поставите игру от 5 человек, то зелёных карточек будет 5 и вы должны выбрать роли, которые будут в игре при минимальном кол-ве игроков. Белые карточки - это роли, которые будут включены в игру, если людей будет больше минимума. \n Пример: Вы выбрали игру от 5 до 8, но зашло всего 6 человек. Это значит, что люди получат все зелёные роли и самую первую белую роль. Если игроков будет всего 5, то они получат только зелёные роли, ну а если игроков 8 - они получат и зелёные, и белые роли. В списке комнат роли шериф, мирный и мафия отображаться не будут");
            AlertDialog alert = builder.create();
            alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alert.show();
        });

        RSB_num_users.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Object minValue, Object maxValue) {
                min_people = (int) minValue;
                max_people = (int) maxValue;
                SetRoles((int) minValue, (int) maxValue);
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
                    boolean all_roles = true, have_mafia = false, have_citizen = false, have_mafia_in_start = false, have_citizen_in_start = false;
                    for (int i = 0; i < list_roles.size(); i++)
                    {
                        if (list_roles.get(i).role == Role.NONE) {
                            all_roles = false;
                            break;
                        }
                        if (list_roles.get(i).peaceful) have_citizen = true;
                        else have_mafia = true;
                        if (i < min_people && list_roles.get(i).peaceful) have_citizen_in_start = true;
                        else if (i < min_people && !list_roles.get(i).peaceful) have_mafia_in_start = true;
                    }
                    name = ET_RoomName.getText().toString();
                    password = ET_password.getText().toString();

                    if (all_roles) {
                        if (have_citizen) {
                            if (have_mafia) {
                                if (have_citizen_in_start) {
                                    if (have_mafia_in_start) {
                                        if (name.length() >= 1) {
                                            if (name.length() <= 25) {
                                                if (password.length() >= 1 || !has_password) {
                                                    int flag = 0;
                                                    for (int i = 0; i < name.length(); i++) {
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

                                                    JSONArray peaceful = new JSONArray();
                                                    JSONArray mafia = new JSONArray();
                                                    Set<String> custom_roles = new HashSet<String>();
                                                    JSONArray roles = new JSONArray();
                                                    try {
                                                        for (int i = 0; i < list_roles.size(); i++) {
                                                            custom_roles.add(list_roles.get(i).role.toString().toLowerCase());
                                                            roles.put(list_roles.get(i).role.toString().toLowerCase());
                                                            if (list_roles.get(i).role != Role.SHERIFF && list_roles.get(i).role != Role.CITIZEN && list_roles.get(i).role != Role.MAFIA) {
                                                                boolean flak = true;
                                                                if (list_roles.get(i).peaceful) {
                                                                    for (int j = 0; j < peaceful.length(); j++) {
                                                                        if (peaceful.get(j).equals(list_roles.get(i).role.toString().toLowerCase())) {
                                                                            flak = false;
                                                                        }
                                                                    }
                                                                    if (flak) {
                                                                        peaceful.put(list_roles.get(i).role.toString().toLowerCase());
                                                                    }
                                                                } else {
                                                                    for (int j = 0; j < mafia.length(); j++) {
                                                                        if (mafia.get(j).equals(list_roles.get(i).role.toString().toLowerCase())) {
                                                                            flak = false;
                                                                        }
                                                                    }
                                                                    if (flak) {
                                                                        mafia.put(list_roles.get(i).role.toString().toLowerCase());
                                                                    }

                                                                }
                                                            }
                                                        }
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
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
                                                        json.put("custom_roles_list", roles);
                                                        json.put("is_custom", true);
                                                        json.put("has_password", has_password);
                                                        json.put("password", password);

                                                        SharedPreferences.Editor editor = mSettings.edit();
                                                        editor.putString(APP_PREFERENCES_ROOM_NAME, name);
                                                        for (int i = 0; i < roles.length(); i++) {
                                                            editor.putString("role_" + i, roles.getString(i));
                                                        }
                                                        editor.putString(APP_PREFERENCES_ROLES, roles.toString());
                                                        editor.putBoolean(APP_PREFERENCES_HAS_PASSWORD, has_password);
                                                        if (!password.equals("")) {
                                                            editor.putString(APP_PREFERENCES_ROOM_PASSWORD, password);
                                                        }
                                                        editor.apply();
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }

                                                    socket.emit("create_room", json);
                                                    Log.d("kkk", "Socket_отправка - create_room - " + json.toString());
                                                }
                                                else
                                                {
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                                    View viewDang = getLayoutInflater().inflate(R.layout.dialog_error, null);
                                                    builder.setView(viewDang);
                                                    TextView TV_title = viewDang.findViewById(R.id.dialogError_TV_errorTitle);
                                                    TextView TV_error = viewDang.findViewById(R.id.dialogError_TV_errorText);
                                                    TV_title.setText("Короткий пароль!");
                                                    TV_error.setText("Пароль должен содержать как минимум 1 символ");
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
                                                TV_title.setText("Слишком длинное название!");
                                                TV_error.setText("Название комнаты должно быть меньше 26 символов!");
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
                                            TV_title.setText("Слишком короткое название!");
                                            TV_error.setText("Название комнаты должно содержать хотя бы один символ!");
                                            AlertDialog alert = builder.create();
                                            alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                            alert.show();
                                        }
                                    } else {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                        View viewDang = getLayoutInflater().inflate(R.layout.dialog_error, null);
                                        builder.setView(viewDang);
                                        TextView TV_title = viewDang.findViewById(R.id.dialogError_TV_errorTitle);
                                        TextView TV_error = viewDang.findViewById(R.id.dialogError_TV_errorText);
                                        TV_title.setText("Ого!");
                                        TV_error.setText("Вы не выбрали ни одну роль мафии на минимальное количество людей!");

                                        AlertDialog alert = builder.create();
                                        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                        alert.show();
                                    }
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    View viewDang = getLayoutInflater().inflate(R.layout.dialog_error, null);
                                    builder.setView(viewDang);
                                    TextView TV_title = viewDang.findViewById(R.id.dialogError_TV_errorTitle);
                                    TextView TV_error = viewDang.findViewById(R.id.dialogError_TV_errorText);
                                    TV_title.setText("Ого!");
                                    TV_error.setText("Вы не выбрали ни одну роль мирных на минимальное количество людей!");

                                    AlertDialog alert = builder.create();
                                    alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                    alert.show();
                                }
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                View viewDang = getLayoutInflater().inflate(R.layout.dialog_error, null);
                                builder.setView(viewDang);
                                TextView TV_title = viewDang.findViewById(R.id.dialogError_TV_errorTitle);
                                TextView TV_error = viewDang.findViewById(R.id.dialogError_TV_errorText);
                                TV_title.setText("Ого!");
                                TV_error.setText("Вы не выбрали ни одну роль мафии!");

                                AlertDialog alert = builder.create();
                                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                alert.show();
                            }
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            View viewDang = getLayoutInflater().inflate(R.layout.dialog_error, null);
                            builder.setView(viewDang);
                            TextView TV_title = viewDang.findViewById(R.id.dialogError_TV_errorTitle);
                            TextView TV_error = viewDang.findViewById(R.id.dialogError_TV_errorText);
                            TV_title.setText("Ого!");
                            TV_error.setText("Вы не выбрали ни одну мирную роль!");

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
                        TV_title.setText("Ошибка!");
                        TV_error.setText("Вы должны заполнить все поля!");

                        AlertDialog alert = builder.create();
                        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        alert.show();
                    }
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

        gridView.setOnItemClickListener((parent, view1, position, id) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            View viewDang = getLayoutInflater().inflate(R.layout.dialog_custom_roles, null);
            builder.setView(viewDang);
            ImageView citizen = viewDang.findViewById(R.id.dialogCustomRoles_citizen);
            ImageView sheriff = viewDang.findViewById(R.id.dialogCustomRoles_sheriff);
            ImageView doctor = viewDang.findViewById(R.id.dialogCustomRoles_doctor);
            ImageView lover = viewDang.findViewById(R.id.dialogCustomRoles_lover);
            ImageView journalist = viewDang.findViewById(R.id.dialogCustomRoles_journalist);
            ImageView bodyguard = viewDang.findViewById(R.id.dialogCustomRoles_bodyguard);
            ImageView maniac = viewDang.findViewById(R.id.dialogCustomRoles_maniac);
            ImageView doctor_of_easy_virtue = viewDang.findViewById(R.id.dialogCustomRoles_doctor_of_easy_virtue);
            ImageView IV_mafia = viewDang.findViewById(R.id.dialogCustomRoles_mafia);
            ImageView mafia_don = viewDang.findViewById(R.id.dialogCustomRoles_mafia_don);
            ImageView terrorist = viewDang.findViewById(R.id.dialogCustomRoles_terrorist);
            ImageView poisoner = viewDang.findViewById(R.id.dialogCustomRoles_poisoner);

            AlertDialog alert = builder.create();

            citizen.setOnClickListener(v -> {
                list_roles.set(position, new RoleModel(Role.CITIZEN, true));
                roleAdapter.notifyDataSetChanged();
                alert.cancel();
            });

            sheriff.setOnClickListener(v -> {
                list_roles.set(position, new RoleModel(Role.SHERIFF, true));
                roleAdapter.notifyDataSetChanged();
                alert.cancel();
            });

            doctor.setOnClickListener(v -> {
                list_roles.set(position, new RoleModel(Role.DOCTOR, true));
                roleAdapter.notifyDataSetChanged();
                alert.cancel();
            });

            lover.setOnClickListener(v -> {
                list_roles.set(position, new RoleModel(Role.LOVER, true));
                roleAdapter.notifyDataSetChanged();
                alert.cancel();
            });

            journalist.setOnClickListener(v -> {
                list_roles.set(position, new RoleModel(Role.JOURNALIST, true));
                roleAdapter.notifyDataSetChanged();
                alert.cancel();
            });

            bodyguard.setOnClickListener(v -> {
                list_roles.set(position, new RoleModel(Role.BODYGUARD, true));
                roleAdapter.notifyDataSetChanged();
                alert.cancel();
            });

            maniac.setOnClickListener(v -> {
                list_roles.set(position, new RoleModel(Role.MANIAC, true));
                roleAdapter.notifyDataSetChanged();
                alert.cancel();
            });

            doctor_of_easy_virtue.setOnClickListener(v -> {
                list_roles.set(position, new RoleModel(Role.DOCTOR_OF_EASY_VIRTUE, true));
                roleAdapter.notifyDataSetChanged();
                alert.cancel();
            });

            IV_mafia.setOnClickListener(v -> {
                list_roles.set(position, new RoleModel(Role.MAFIA, false));
                roleAdapter.notifyDataSetChanged();
                alert.cancel();
            });

            mafia_don.setOnClickListener(v -> {
                list_roles.set(position, new RoleModel(Role.MAFIA_DON, false));
                roleAdapter.notifyDataSetChanged();
                alert.cancel();
            });

            terrorist.setOnClickListener(v -> {
                list_roles.set(position, new RoleModel(Role.TERRORIST, false));
                roleAdapter.notifyDataSetChanged();
                alert.cancel();
            });

            poisoner.setOnClickListener(v -> {
                list_roles.set(position, new RoleModel(Role.POISONER, false));
                roleAdapter.notifyDataSetChanged();
                alert.cancel();
            });

            alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alert.show();
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putString(APP_PREFERENCES_ROOM_NAME, String.valueOf(ET_RoomName.getText()));
                editor.apply();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new CreateRoomFragment()).commit();
            }
        });

        return view;
    }

    @Override
    public void onBackPressed() {
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(APP_PREFERENCES_ROOM_NAME, String.valueOf(ET_RoomName.getText()));
        editor.apply();
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new CreateRoomFragment()).commit();
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
                        MainActivity.PlayersMinMaxInfo = "от " + min_people + " до " + max_people;
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

    public void SetRoles(int min_people, int max_people) {
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
        roleAdapter.min_people = min_people;
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

    //конвертировать String в Role
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
}