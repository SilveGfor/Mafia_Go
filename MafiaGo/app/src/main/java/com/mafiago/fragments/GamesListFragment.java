package com.mafiago.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import io.socket.emitter.Emitter;

import static android.app.Activity.RESULT_OK;
import static com.mafiago.MainActivity.socket;
import static com.mafiago.fragments.MenuFragment.GALLERY_REQUEST;

public class GamesListFragment extends Fragment implements OnBackPressedListener {

    public ListView listView;

    public TextView TV_no_games;

    //public Button btnExit;
    public Button btnCreateRoom;

    public ProgressBar PB_loading;

    ImageView IV_screenshot;

    View view_report;

    String base64_screenshot = "", report_nick = "", report_id = "";

    ArrayList<RoomModel> list_room = new ArrayList<>();

    public JSONObject json;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_games_list, container, false);
        view_report = getLayoutInflater().inflate(R.layout.dialog_report, null);

        listView = view.findViewById(R.id.fragmentGamesList_LV_games);
        btnCreateRoom = view.findViewById(R.id.fragmentGamesList_btn_create_room);
        //btnExit = view.findViewById(R.id.btnExitGamesList);
        TV_no_games = view.findViewById(R.id.fragmentGamesList_TV_no_games);
        PB_loading = view.findViewById(R.id.fragmentGamesList_PB_loading);

        IV_screenshot = view_report.findViewById(R.id.dialogReport_IV_screenshot);

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case GALLERY_REQUEST:
                if(resultCode == RESULT_OK){
                    Uri uri = imageReturnedIntent.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        byte[] bytes = stream.toByteArray();

                        base64_screenshot = Base64.encodeToString(bytes, Base64.DEFAULT);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
                    View view_report = getLayoutInflater().inflate(R.layout.dialog_report, null);
                    builder2.setView(view_report);
                    AlertDialog alert2 = builder2.create();

                    Button btn_add_screenshot = view_report.findViewById(R.id.dialogReport_btn_add_screenshot);
                    Button btn_report = view_report.findViewById(R.id.dialogReport_btn_report);
                    ImageView IV_screenshot = view_report.findViewById(R.id.dialogReport_IV_screenshot);
                    EditText ET_report_message = view_report.findViewById(R.id.dialogReport_ET_report);

                    final String[] reason = {""};

                    RadioGroup radioGroup = view_report.findViewById(R.id.dialogReport_RG);

                    IV_screenshot.setImageURI(null);
                    IV_screenshot.setImageURI(uri);

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

                    btn_report.setOnClickListener(v22 -> {
                        final JSONObject json2 = new JSONObject();
                        try {
                            json2.put("nick", MainActivity.NickName);
                            json2.put("session_id", MainActivity.Session_id);
                            json2.put("against_id", report_id);
                            json2.put("against_nick", report_nick);
                            json2.put("reason", reason[0]);
                            json2.put("comment", ET_report_message.getText());
                            json2.put("image", base64_screenshot);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        socket.emit("send_complaint", json2);
                        Log.d("kkk", "Socket_отправка - send_complaint" + json2);
                        alert2.cancel();
                    });

                    btn_add_screenshot.setVisibility(View.GONE);

                    alert2.show();
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
                        ArrayList<String> list_roles = new ArrayList<>();
                        Boolean alive = true, is_on = false;
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
                            RoomModel model = new RoomModel(name, min_people, max_people, num_people, id, list_users, is_on, list_roles);
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
                    ArrayList<String> list_roles = new ArrayList<>();
                    Boolean alive = true, is_on = false;
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
                                RoomModel model = new RoomModel(name, min_people, max_people, num_people, id, list_users, is_on, list_roles);
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
                String nick = "", user_id_2 = "", avatar = "";
                int playing_room_num, money = 0, exp = 0, gold = 0;
                boolean online = false;
                JSONObject statistic = new JSONObject();
                int game_counter = 0, max_money_score = 0, max_exp_score = 0;
                String general_pers_of_wins = "", mafia_pers_of_wins = "", peaceful_pers_of_wins = "";

                try {
                    statistic = data.getJSONObject("statistics");
                    game_counter = statistic.getInt("game_counter");
                    if (statistic.has("max_money_score"))
                    {
                        max_money_score = statistic.getInt("max_money_score");
                        max_exp_score = statistic.getInt("max_exp_score");
                        gold = data.getInt("gold");
                        money = data.getInt("money");
                    }
                    general_pers_of_wins = statistic.getString("general_pers_of_wins");
                    mafia_pers_of_wins = statistic.getString("mafia_pers_of_wins");
                    peaceful_pers_of_wins = statistic.getString("peaceful_pers_of_wins");
                    online = data.getBoolean("is_online");
                    nick = data.getString("nick");
                    avatar = data.getString("avatar");
                    user_id_2 = data.getString("user_id");
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
                FloatingActionButton FAB_report = view_profile.findViewById(R.id.Item_profile_complain);
                TextView TV_money = view_profile.findViewById(R.id.ItemProfile_TV_money);
                TextView TV_exp = view_profile.findViewById(R.id.ItemProfile_TV_exp);
                TextView TV_gold = view_profile.findViewById(R.id.ItemProfile_TV_gold);
                ImageView IV_avatar = view_profile.findViewById(R.id.Item_profile_IV_avatar);

                TextView TV_game_counter = view_profile.findViewById(R.id.ItemProfile_TV_game_counter);
                TextView TV_max_money_score = view_profile.findViewById(R.id.ItemProfile_TV_max_money_score);
                TextView TV_max_exp_score = view_profile.findViewById(R.id.ItemProfile_TV_max_exp_score);
                TextView TV_general_pers_of_wins = view_profile.findViewById(R.id.ItemProfile_TV_general_pers_of_wins);
                TextView TV_mafia_pers_of_wins = view_profile.findViewById(R.id.ItemProfile_TV_mafia_pers_of_wins);
                TextView TV_peaceful_pers_of_wins = view_profile.findViewById(R.id.ItemProfile_TV_peaceful_pers_of_wins);

                TV_game_counter.setText(String.valueOf(game_counter));
                TV_max_money_score.setText(String.valueOf(max_money_score));
                TV_max_exp_score.setText(String.valueOf(max_exp_score));
                TV_general_pers_of_wins.setText(general_pers_of_wins);
                TV_mafia_pers_of_wins.setText(mafia_pers_of_wins);
                TV_peaceful_pers_of_wins.setText(peaceful_pers_of_wins);

                LinearLayout LL_gold = view_profile.findViewById(R.id.ItemProfile_LL_gold);
                LinearLayout LL_money = view_profile.findViewById(R.id.ItemProfile_LL_money);
                LinearLayout LL_max_money_score = view_profile.findViewById(R.id.ItemProfile_LL_max_money_score);
                LinearLayout LL_max_exp_score = view_profile.findViewById(R.id.ItemProfile_LL_max_exp_score);

                if (avatar != null) {
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

                    alert2.show();
                });

                if (nick.equals(MainActivity.NickName))
                {
                    TV_max_money_score.setText(String.valueOf(max_money_score));
                    TV_max_exp_score.setText(String.valueOf(max_exp_score));
                    TV_gold.setText(String.valueOf(gold));
                    TV_money.setText(String.valueOf(money));
                }
                else
                {
                    LL_gold.setVisibility(View.INVISIBLE);
                    LL_money.setVisibility(View.INVISIBLE);
                    LL_max_money_score.setVisibility(View.GONE);
                    LL_max_exp_score.setVisibility(View.GONE);
                }
                TV_exp.setText(String.valueOf(exp));

                TextView TV_nick = view_profile.findViewById(R.id.Item_profile_TV_nick);
                ImageView IV_on_off = view_profile.findViewById(R.id.Item_profile_IV_on_off);

                if (online) IV_on_off.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_online));
                else IV_on_off.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_offline));

                TV_nick.setText(nick);
                FAB_kick.setVisibility(View.GONE);

                AlertDialog alert = builder.create();

                String finalUser_id_ = user_id_2;
                report_nick = nick;
                report_id = user_id_2;

                if (!nick.equals(MainActivity.NickName)) {
                    String finalNick = nick;
                    FAB_send_message.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alert.cancel();
                            MainActivity.User_id_2 = finalUser_id_;
                            MainActivity.NickName_2 = finalNick;
                            MainActivity.bitmap_avatar_2 = fromBase64(finalAvatar);
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new PrivateMessagesFragment()).commit();
                        }
                    });

                    FAB_report.setOnClickListener(v1 -> {
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
                        View view_report = getLayoutInflater().inflate(R.layout.dialog_report, null);
                        builder2.setView(view_report);
                        AlertDialog alert2 = builder2.create();

                        Button btn_add_screenshot = view_report.findViewById(R.id.dialogReport_btn_add_screenshot);
                        Button btn_report = view_report.findViewById(R.id.dialogReport_btn_report);
                        EditText ET_report_message = view_report.findViewById(R.id.dialogReport_ET_report);

                        RadioGroup radioGroup = view_report.findViewById(R.id.dialogReport_RG);

                        btn_add_screenshot.setOnClickListener(v2 -> {
                            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                            photoPickerIntent.setType("image/*");
                            startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
                            alert2.cancel();
                        });

                        radioGroup.setVisibility(View.GONE);
                        btn_report.setVisibility(View.GONE);
                        ET_report_message.setVisibility(View.GONE);

                        alert2.show();
                    });

                    String finalUser_id_2 = user_id_2;
                    FAB_add_friend.setOnClickListener(v1 -> {
                        json = new JSONObject();
                        try {
                            json.put("nick", MainActivity.NickName);
                            json.put("session_id", MainActivity.Session_id);
                            json.put("user_id_2", finalUser_id_2);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        socket.emit("friend_request", json);
                        Log.d("kkk", "Socket_отправка - friend_request"+ json.toString());
                    });
                }
                else
                {
                    FAB_send_message.setVisibility(View.GONE);
                    FAB_report.setVisibility(View.GONE);
                    FAB_add_friend.setVisibility(View.GONE);
                }
                alert.show();
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
}