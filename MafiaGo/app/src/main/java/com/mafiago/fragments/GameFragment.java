package com.mafiago.fragments;

import android.app.Activity;
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
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.mafiago.MainActivity;
import com.example.mafiago.R;
import com.mafiago.adapters.MessageAdapter;
import com.mafiago.adapters.PlayersAdapter;
import com.mafiago.classes.OnBackPressedListener;
import com.mafiago.pager_adapters.GameChatPagerAdapter;
import com.mafiago.enums.Role;
import com.mafiago.enums.Time;
import com.mafiago.models.MessageModel;
import com.mafiago.models.Player;
import com.mafiago.models.UserModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import io.socket.emitter.Emitter;

import static android.app.Activity.RESULT_OK;
import static  com.mafiago.MainActivity.socket;
import static com.mafiago.fragments.MenuFragment.GALLERY_REQUEST;

public class GameFragment extends Fragment implements OnBackPressedListener {
    public GridView gridView_users;

    public TextView timer;
    public TextView dayTime;
    public TextView room_name;
    public TextView TV_mafia_count;
    public TextView TV_peaceful_count;

    public ImageView IV_influence_doctor;
    public ImageView IV_influence_lover;
    public ImageView IV_influence_sheriff;
    public ImageView IV_influence_bodyguard;
    public ImageView IV_influence_poisoner;

    public ConstraintLayout Constrain;

    public Player player;

    public Animation animation;

    ArrayList<MessageModel> list_chat = new ArrayList<>();
    ArrayList<UserModel> list_users = new ArrayList<>();
    int[] list_mafias = new int[] {0, 0, 0, 0, 0, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9};
    int[] list_peaceful = new int[] {0, 0, 0, 0, 0, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9, 10, 10, 11, 11};

    JSONObject users = new JSONObject();

    int answer_id = -1;
    public int StopTimer = 0;
    int messages_can_write = 10;
    public String journalist_check = null;
    public boolean has_paused = false;

    public int mafia_max = 0, mafia_now = 0, peaceful_max = 0, peaceful_now = 0;

    int num = -1;

    ImageView IV_screenshot;
    ViewPager viewPager;
    TabLayout tabLayout;
    GameChatPagerAdapter gameChatPagerAdapter;

    PlayersAdapter playersAdapter;

    View view_report;
    ImageView IV_screen;

    public JSONObject json;

    String base64_screenshot = "", report_nick = "", report_id = "";

    MessageAdapter messageAdapter;

    public int FirstVisibleItem = 0, VisibleItemsCount = 0,TotalItemsCount = 0;

    public static final String APP_PREFERENCES = "user";
    public static final String APP_PREFERENCES_LAST_ROLE = "role";

    private SharedPreferences mSettings;

    OnHeadlineSelectedListener mCallback;

    // Container Activity must implement this interface
    public interface OnHeadlineSelectedListener {
        public void onArticleSelected(int position);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnHeadlineSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_game, container, false);
        view_report = inflater.inflate(R.layout.dialog_report, container, false);
        IV_screen = view_report.findViewById(R.id.dialogReport_IV_screenshot);

        mSettings = getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        gridView_users = view.findViewById(R.id.fragmentGame_listUsers);
        room_name = view.findViewById(R.id.fragmentGame_TV_roomName);
        TV_mafia_count = view.findViewById(R.id.fragmentGame_TV_mafiaCount);
        TV_peaceful_count = view.findViewById(R.id.fragmentGame_TV_peacefulCount);

        timer = view.findViewById(R.id.fragmentGame_TV_timer);
        dayTime = view.findViewById(R.id.fragmentGame_TV_dayTime);

        Constrain = view.findViewById(R.id.fragmentGame_CL);

        IV_influence_doctor = view.findViewById(R.id.fragmentGame_ic_doctor);
        IV_influence_lover = view.findViewById(R.id.fragmentGame_ic_lover);
        IV_influence_sheriff = view.findViewById(R.id.fragmentGame_ic_sheriff);
        IV_influence_bodyguard = view.findViewById(R.id.fragmentGame_ic_bodyguard);
        IV_influence_poisoner = view.findViewById(R.id.fragmentGame_ic_poisoner);

        IV_screenshot = view_report.findViewById(R.id.dialogReport_IV_screenshot);

        // Получаем ViewPager и устанавливаем в него адаптер
        viewPager = view.findViewById(R.id.fragmentGame_VP_chat);
        gameChatPagerAdapter = new GameChatPagerAdapter(getActivity().getSupportFragmentManager(), getActivity());
        viewPager.setAdapter(gameChatPagerAdapter);

        // Передаём ViewPager в TabLayout
        tabLayout = view.findViewById(R.id.fragmentGame_tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        playersAdapter = new PlayersAdapter(list_users, getContext());
        gridView_users.setAdapter(playersAdapter);

        player = new Player(MainActivity.NickName, MainActivity.Session_id, MainActivity.Game_id, MainActivity.Role);

        room_name.setText(MainActivity.RoomName);

        //socket.off("connect");
        //socket.off("disconnect");
        socket.off("get_in_room");
        socket.off("user_message");
        socket.off("leave_room");
        socket.off("timer");
        socket.off("time");
        socket.off("role");
        socket.off("restart");
        socket.off("role_action");
        socket.off("know_role");
        socket.off("system_message");
        socket.off("user_error");
        socket.off("mafias");
        socket.off("get_my_game_info");
        socket.off("success_get_in_room");
        socket.off("get_profile");
        socket.off("host_info");
        socket.off("ban_user_in_room");
        socket.off("ban_user_in_room_error");
        socket.off("user_message_delay");
        socket.off("send_complaint");
        socket.off("my_friend_request");

        socket.on("connect", onConnect);
        socket.on("disconnect", onDisconnect);
        socket.on("get_in_room", onGetInRoom);
        socket.on("user_message", onNewMessage);
        socket.on("leave_room", onLeaveUser);
        socket.on("timer", onTimer);
        socket.on("time", onTime);
        socket.on("role", onRole);
        socket.on("restart", onRestart);
        socket.on("role_action", onRoleAction);
        socket.on("know_role", onKnowRole);
        socket.on("system_message", onSystemMessage);
        socket.on("user_error", onUserError);
        socket.on("mafias", onMafias);
        socket.on("get_my_game_info", onGetMyGameInfo);
        socket.on("success_get_in_room", onSuccessGetInRoom);
        socket.on("get_profile", OnGetProfile);
        socket.on("host_info", OnHostInfo);
        socket.on("ban_user_in_room", OnBanUserInRoom);
        socket.on("ban_user_in_room_error", OnBanUserInRoomError);
        socket.on("user_message_delay", OnUserMessageDelay);
        socket.on("send_complaint", onSendComplain);
        socket.on("my_friend_request", onMySendRequest);
        socket.on("daily_task_completed", onDailyTaskCompleted);

        json = new JSONObject();
        try {
            json.put("nick", player.getNick());
            json.put("session_id", player.getSession_id());
            json.put("room", player.getRoom_num());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("get_in_room", json);
        Log.d("kkk", "Socket_отправка - get_in_room from main "+ json.toString());

        gridView_users.setOnItemClickListener((parent, view1, position, id) -> {
            mCallback.onArticleSelected(position);
            String nick = list_users.get(position).getNick();
            if (nick.equals(player.getNick()) && player.getTime() != Time.LOBBY && !player.Can_click())
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                View viewNewRole = getLayoutInflater().inflate(R.layout.dialog_new_role, null);
                builder.setView(viewNewRole);

                TextView TV_role = viewNewRole.findViewById(R.id.dialogNewRole_TV_mainText);
                TextView TV_roleDescription = viewNewRole.findViewById(R.id.dialogNewRole_TV_roleDescription);
                ImageView IV_role = viewNewRole.findViewById(R.id.dialogNewRole_IV_role);

                playersAdapter.notifyDataSetChanged();

                switch (player.getRole())
                {
                    case NONE:
                        TV_role.setText("У вас нет роли");
                        TV_roleDescription.setText("Извините, но что-то пошло не так");
                        IV_role.setImageResource(R.drawable.journalist_round);
                        IV_role.setImageResource(R.drawable.ic_error);
                        break;
                    case CITIZEN:
                        TV_role.setText("Ваша роль - Мирный житель");
                        TV_roleDescription.setText("Мирный житель - он живет в городе, где никого не знает. Его задача выяснить, кто из жителей стоит на стороне мафии, а кто - нет, и уничтожить всех мафиози, пока они не уничтожили весь город. Каждый раз по окончанию дня мирные жители могут голосовать за того, кого они считают мафией.");
                        IV_role.setImageResource(R.drawable.citizen_round);
                        break;
                    case MAFIA:
                        TV_role.setText("Ваша роль - мафия");
                        TV_roleDescription.setText("Мафия - неотъемлемый персонаж игры. Мафия образует опасную группировку из своих членов, и ее цель - уничтожить все мирное население города. Каждый член мафии знает всех остальных мафиози. Ночью мафия может обсуждать в отдельном чате свои мысли и выбирать свою новую жертву. Присутствует во всех играх.");
                        IV_role.setImageResource(R.drawable.mafia_round);
                        break;
                    case SHERIFF:
                        TV_role.setText("Ваша роль - Шериф");
                        TV_roleDescription.setText("Шериф - играет на стороне мирных жителей. Задача шерифа - упростить мирным жителям задачу поиска мафии. Ночью шериф может проверить любого игрока и узнать его роль. Присутствует во всех играх.");
                        IV_role.setImageResource(R.drawable.sheriff_round);
                        break;
                    case DOCTOR:
                        TV_role.setText("Ваша роль - Доктор");
                        TV_roleDescription.setText("Доктор - играет на стороне мирных. Доктор может защитить любого игрока от смерти ночью, тем самым увеличивая шансы мирных жителей на победу. Также доктор сможет защитить от смерти игрока, к которому пришел отравитель.");
                        IV_role.setImageResource(R.drawable.doctor_round);
                        break;
                    case LOVER:
                        TV_role.setText("Ваша роль - Любовница");
                        TV_roleDescription.setText("Любовница - на стороне мирных жителей. Никто не способен противостоять ее красоте, даже члены мафии, чем любовница и пользуется, чтобы помочь мирным. Ночью любовница самая первая может использовать свои чары против любого игрока, тем самым лишая его способностей роли и возможности голосовать днем. ");
                        IV_role.setImageResource(R.drawable.lover_round);
                        break;
                    case MAFIA_DON:
                        TV_role.setText("Ваша роль - Дон мафии");
                        TV_roleDescription.setText("Дон мафии - лидер мафиозной группировки. Дон обладает теми же способностями, что и обычная мафия, но его голос на ночном выборе жертвы считается за два.");
                        IV_role.setImageResource(R.drawable.mafia_don_round);
                        break;
                    case MANIAC:
                        TV_role.setText("Ваша роль - Маньяк");
                        TV_roleDescription.setText("Маньяк - играет за мирных. Маньяк примкнул к стороне мирных, потому что у него появился ночной конкурент - мафия. Маньяк не желает, чтобы его цели убивал кто-то еще, поэтому он старается каждую ночь убить кого-то из клана мафии.");
                        IV_role.setImageResource(R.drawable.maniac_round);
                        break;
                    case TERRORIST:
                        TV_role.setText("Ваша роль - Террорист");
                        TV_roleDescription.setText("Террорист - играет на стороне мафии. Он не знает, кто член мафиозной группировки, однако мафия знает, кто террорист, и не может его убить ночью. Задача террориста - во время дневного голосования выгодно подорвать свою жизнь вместе с жизнью мирного игрока, обладающего значительными способностями, например, шерифа или любовницу.");
                        IV_role.setImageResource(R.drawable.terrorist_round);
                        break;
                    case BODYGUARD:
                        TV_role.setText("Ваша роль - Телохранитель");
                        TV_roleDescription.setText("Телохранитель - играет на стороне мирных. Телохранитель может прийти к любому игроку днем и защищать его от смерти до конца следующей ночи. Если террорист попробует взорвать игрока, который под защитой телохранителя, то умрет только террорист, но игрок уже останется без защиты телохранителя. Защита телохранителя от смерти не действует для смерти по результатам дневного голосования. ");
                        IV_role.setImageResource(R.drawable.bodyguard_round);
                        break;
                    case DOCTOR_OF_EASY_VIRTUE:
                        TV_role.setText("Ваша роль - Доктор легкого поведения");
                        TV_roleDescription.setText("Доктор легкого поведения - на стороне мирных. Имеет опыт любовницы, однако его призвание - врачевание, поэтому ночью доктор легкого поведения может решить, воспользоваться ему способностями любовницы или доктора.\n");
                        IV_role.setImageResource(R.drawable.doctor_of_easy_virtue_round);
                        break;
                    case POISONER:
                        TV_role.setText("Ваша роль - Отравитель");
                        TV_roleDescription.setText("Отравитель - играет за мафию. Отравитель не знает, кто мафия, а мафия не знает, кто отравитель, поэтому при неосторожной игре отравитель может быть убит мафией ночью. Отравитель ночью может отравить любого игрока, который умрет на следующий день после голосования, если к нему не придет доктор или телохранитель. Отравленный человек настолько обессилел, что может написать только одно сообщение следующим днем. ");
                        IV_role.setImageResource(R.drawable.poisoner_round);
                        break;
                    case JOURNALIST:
                        TV_role.setText("Ваша роль - Агент СМИ");
                        TV_roleDescription.setText("Агент СМИ - проводит расследования на стороне мирных жителей. Ночью он может проверить любых двух игроков и выяснить, играют ли они в одной команде или нет.\n");
                        IV_role.setImageResource(R.drawable.journalist_round);
                        break;
                }
                AlertDialog alert = builder.create();
                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alert.show();
            }
            else {
                if (player.Can_click() && player.getStatus().equals("alive") && list_users.get(position).getAlive()) {
                    switch (player.getTime()) {
                        case LOBBY:
                            ShowProfile(nick);
                            break;
                        case NIGHT_LOVE:
                            switch (player.getRole()) {
                                case LOVER:
                                    RoleAction(nick);
                                    break;
                                case DOCTOR_OF_EASY_VIRTUE:
                                    player.setVoted_at_night(true);
                                    RoleAction(nick);
                                    break;
                            }
                            break;
                        case NIGHT_OTHER:
                            switch (player.getRole()) {
                                case SHERIFF:
                                    RoleAction(nick);
                                    break;
                                case DOCTOR:
                                    RoleAction(nick);
                                    break;
                                case DOCTOR_OF_EASY_VIRTUE:
                                    RoleAction(nick);
                                    break;
                                case MAFIA:
                                    RoleAction(nick);
                                    break;
                                case MAFIA_DON:
                                    RoleAction(nick);
                                    break;
                                case MANIAC:
                                    RoleAction(nick);
                                    break;
                                case POISONER:
                                    RoleAction(nick);
                                    break;
                                case JOURNALIST:
                                    if (journalist_check == null) {
                                        journalist_check = nick;
                                        for (int i = 0; i < list_users.size(); i++) {
                                            if (list_users.get(i).getNick().equals(nick)) {
                                                list_users.get(i).setAnimation_type(Role.NONE);
                                                break;
                                            }
                                        }
                                        playersAdapter.notifyDataSetChanged();
                                    } else {
                                        final JSONObject json = new JSONObject();
                                        try {
                                            json.put("nick", player.getNick());
                                            json.put("session_id", player.getSession_id());
                                            json.put("room", player.getRoom_num());
                                            json.put("influence_on_nick", journalist_check);
                                            json.put("influence_on_nick_2", nick);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        socket.emit("role_action", json);
                                        Log.d("kkk", "Socket_отправка - role_action" + json.toString());

                                        int journalist_checks_count = 0;
                                        for (int i = 0; i < list_users.size(); i++) {
                                            if (list_users.get(i).getNick().equals(journalist_check) || list_users.get(i).getNick().equals(nick)) {
                                                list_users.get(i).setChecked(true);
                                            }
                                            if (!list_users.get(i).getChecked()) {
                                                journalist_checks_count++;
                                            }
                                        }
                                        if (journalist_checks_count < 2) {
                                            for (int i = 0; i < list_users.size(); i++) {
                                                list_users.get(i).setChecked(false);
                                            }
                                        }
                                        playersAdapter.notifyDataSetChanged();
                                        journalist_check = null;
                                    }
                                    break;
                            }
                            break;
                        case DAY:
                            if (player.getRole() == Role.BODYGUARD) {
                                RoleAction(nick);
                            } else {
                                ShowProfile(nick);
                            }
                            break;
                        case VOTING:
                            if (player.getRole() == Role.TERRORIST) {
                                RoleAction(nick);
                            } else {
                                Voting(nick);
                            }
                            break;
                    }
                    if (journalist_check == null) {
                        StopAnimation();
                    }
                } else {
                    ShowProfile(nick);
                }
            }
        });
        dayTime.setOnClickListener(v -> {
            switch (player.getTime())
            {
                case LOBBY:
                    break;
                case NIGHT_LOVE:
                    break;
                case NIGHT_OTHER:
                    break;
                case DAY:
                    if (player.getStatus().equals("alive")) {
                        final JSONObject json2 = new JSONObject();
                        try {
                            json2.put("nick", MainActivity.NickName);
                            json2.put("session_id", MainActivity.Session_id);
                            json2.put("room", player.getRoom_num());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d("kkk", "Socket_отправка_skip_day - " + json2.toString());
                        socket.emit("skip_day", json2);
                        dayTime.setBackgroundResource(R.drawable.grey_button);
                    }
                    break;
                case VOTING:
                    break;
            }
        });
        return view;
    }

    public Bitmap fromBase64(String image) {
        // Декодируем строку Base64 в массив байтов
        byte[] decodedString = Base64.decode(image, Base64.DEFAULT);

        // Декодируем массив байтов в изображение
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        // Помещаем изображение в ImageView
        return decodedByte;
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

    @Override
    public void onResume() {
        if (has_paused && player.getTime() == Time.LOBBY)
        {
            for (int i = 0; i < list_users.size(); i++)
            {
                if (list_users.get(i).getNick().equals(player.getNick()))
                {
                    list_users.remove(i);
                }
            }
            json = new JSONObject();
            try {
                json.put("nick", player.getNick());
                json.put("session_id", player.getSession_id());
                json.put("room", player.getRoom_num());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            socket.emit("get_in_room", json);
            Log.d("kkk", "Socket_отправка - get_in_room onResume"+ json.toString());
            has_paused = false;
        }

        super.onResume();
    }

    @Override
    public void onPause() {
        has_paused = true;
        Log.d("kkk", "onPause in GameFragment");
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (player.getTime() == Time.LOBBY) {
            if (!timer.getText().equals("\u221e")) {
                if (Integer.parseInt(String.valueOf(timer.getText())) > 5) {
                    final JSONObject json2 = new JSONObject();
                    try {
                        json2.put("nick", MainActivity.NickName);
                        json2.put("session_id", MainActivity.Session_id);
                        json2.put("room", player.getRoom_num());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.d("kkk", "Socket_отправка_leave_user - " + json2.toString());
                    socket.emit("leave_room", json2);
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    View viewError = getLayoutInflater().inflate(R.layout.dialog_error, null);
                    builder.setView(viewError);
                    AlertDialog alert;
                    TextView TV_error = viewError.findViewById(R.id.dialogError_TV_errorText);
                    TV_error.setText("Нельзя выходить за несколько секунд до начала игры!");
                    alert = builder.create();
                    alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alert.show();
                }
            }
            else
            {
                final JSONObject json2 = new JSONObject();
                try {
                    json2.put("nick", MainActivity.NickName);
                    json2.put("session_id", MainActivity.Session_id);
                    json2.put("room", player.getRoom_num());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("kkk", "Socket_отправка_leave_user - " + json2.toString());
                socket.emit("leave_room", json2);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
            }
        }
        else
        {
            socket.off("get_in_room");
            socket.off("user_message");
            socket.off("leave_room");
            socket.off("system_message");
            socket.off("ban_user_in_room");
            socket.off("host_info");
            socket.off("role_action");
            final JSONObject json2 = new JSONObject();
            try {
                json2.put("nick", MainActivity.NickName);
                json2.put("session_id", MainActivity.Session_id);
                json2.put("room", player.getRoom_num());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("kkk", "Socket_отправка_leave_user - " + json2.toString());
            socket.emit("leave_room", json2);
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
        }
    }

    /*******************************
     *                             *
     *       SOCKETS start         *
     *                             *
     *******************************/

    private Emitter.Listener onGetInRoom = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Log.d("kkk", "Принял - get_in_room: " + args[0]);
                    JSONObject data = (JSONObject) args[0];
                    String nick;
                    String time;
                    String avatar;
                    int test_num;
                    try {
                        test_num = data.getInt("num");
                        time = data.getString("time");
                        time = getDate(time);
                        nick = data.getString("nick");
                        avatar = data.getString("avatar");
                        users.put(nick, avatar);
                        Log.d("kkk", "get_in_room - " + " Длина listchat = " + list_chat.size() + " /  testnum = " + test_num + " / num = " + num + "/ " + data);
                        boolean good = true;
                        for (int i = 0; i < list_users.size(); i++)
                        {
                            if (list_users.get(i).getNick().equals(nick))
                            {
                                good = false;
                                break;
                            }
                        }
                        if (test_num != num && good) {
                            if (test_num > num) {
                                num = test_num;
                            }
                            list_users.add(new UserModel(nick, Role.NONE, avatar));
                            playersAdapter.notifyDataSetChanged();
                        }
                    } catch (JSONException e) {
                        return;
                    }
                }
            });
        }
    };

    private Emitter.Listener onLeaveUser = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String nick = "";
                    String time = "";
                    int test_num = -1;
                    try {
                        test_num = data.getInt("num");
                        nick = data.getString("nick");
                        time = data.getString("time");
                        time = getDate(time);
                        users.remove(nick);
                        Log.d("kkk", "leave_user - " + " Длина listchat = " + list_chat.size() + " /  testnum = " + test_num + " / num = " + num + "/ " + data);
                    } catch (JSONException e) {
                        return;
                    }

                    if (test_num != num) {
                        if (test_num > num) {
                            num = test_num;
                        }
                        for (int i=list_users.size()-1; i>=0; i--)
                        {
                            if (list_users.get(i).getNick().equals(nick))
                            {
                                list_users.remove(i);
                            }
                        }
                        playersAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String nick;
                    String status;
                    try {
                        nick = data.getString("nick");
                        status = data.getString("status");

                        Log.e("kkk", status + data.getString("message") + nick);
                    } catch (JSONException e) {
                        Log.d("kkk", "JSONException");
                        return;
                    }
                }
            });
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
                    json = new JSONObject();
                    try {
                        json.put("nick", player.getNick());
                        json.put("session_id", player.getSession_id());
                        json.put("room", player.getRoom_num());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    socket.emit("get_in_room", json);
                    Log.d("kkk", "Socket_отправка - get_in_room"+ json.toString());
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

    //принимает таймер
    private Emitter.Listener onTimer = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String time;
                    try {
                        time = data.getString("timer");
                        if (!time.equals("stop"))
                        {
                            if (StopTimer == 1)
                            {
                                timer.setText("\u221e");
                                StopTimer = 0;
                            }
                            else {
                                timer.setText(time);
                            }
                        }
                        else {
                            StopTimer = 1;
                            timer.setText("--");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    //принимает время дня
    private Emitter.Listener onTime = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(() -> {
                JSONObject data = (JSONObject) args[0];
                String time;
                try {
                    time = data.getString("time");
                    switch (time)
                    {
                        case "lobby":
                            player.setTime(Time.LOBBY);
                            dayTime.setText("лобби");
                            dayTime.setBackgroundResource(R.drawable.green_button);
                            Constrain.setBackgroundResource(R.drawable.fon_day);
                            break;
                        case "night_love":
                            DeleteNumbersFromVoting();
                            player.setTime(Time.NIGHT_LOVE);
                            dayTime.setText("ночь");
                            dayTime.setBackgroundResource(R.drawable.died_button);
                            Constrain.setBackgroundResource(R.drawable.fon_night);
                            break;
                        case "night_other":
                            player.setTime(Time.NIGHT_OTHER);
                            dayTime.setText("ночь");
                            dayTime.setBackgroundResource(R.drawable.died_button);
                            Constrain.setBackgroundResource(R.drawable.fon_night);
                            break;
                        case "day":
                            DeleteNumbersFromVoting();
                            player.setTime(Time.DAY);
                            if (player.getStatus().equals("alive")) {
                                dayTime.setText("пропуск дня");
                                dayTime.setBackgroundResource(R.drawable.green_button);
                            }
                            else
                            {
                                dayTime.setText("день");
                                dayTime.setBackgroundResource(R.drawable.grey_button);
                            }
                            Constrain.setBackgroundResource(R.drawable.fon_day);
                            break;
                        case "voting":
                            player.setTime(Time.VOTING);
                            dayTime.setText("Голосование");
                            dayTime.setBackgroundResource(R.drawable.grey_button);
                            Constrain.setBackgroundResource(R.drawable.fon_day);
                            break;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (player.getStatus().equals("alive"))
                {
                    player.setCan_write(false);
                    StopAnimation();
                    switch (player.getTime())
                    {
                        case NIGHT_LOVE:
                            if (TV_mafia_count.getVisibility() != View.VISIBLE)
                            {
                                int players = list_users.size();
                                mafia_max = list_mafias[players];
                                peaceful_max = list_peaceful[players];
                                mafia_now = mafia_max;
                                peaceful_now = peaceful_max;
                                TV_mafia_count.setText("Мафия " + mafia_now + "/" + mafia_max);
                                TV_peaceful_count.setText("Мирные " + peaceful_now + "/" + peaceful_max);
                                TV_mafia_count.setVisibility(View.VISIBLE);
                                TV_peaceful_count.setVisibility(View.VISIBLE);
                            }
                            IV_influence_poisoner.setVisibility(View.GONE);
                            IV_influence_lover.setVisibility(View.GONE);
                            switch (player.getRole())
                            {
                                case LOVER:
                                    StartAnimation(Role.LOVER);
                                    break;
                                case DOCTOR_OF_EASY_VIRTUE:
                                    StartAnimation(Role.LOVER);
                                    break;
                                case MAFIA:
                                    player.setCan_write(true);
                                    break;
                                case MAFIA_DON:
                                    player.setCan_write(true);
                                    break;
                            }
                            break;
                        case NIGHT_OTHER:
                            if (IV_influence_lover.getVisibility() != View.VISIBLE) {
                                switch (player.getRole()) {
                                    case DOCTOR_OF_EASY_VIRTUE:
                                        if (!player.getVoted_at_night()) {
                                            StartAnimation(Role.DOCTOR);
                                        }
                                        break;
                                    case SHERIFF:
                                        StartAnimation(Role.SHERIFF);
                                        break;
                                    case DOCTOR:
                                        StartAnimation(Role.DOCTOR);
                                        break;
                                    case MAFIA:
                                        StartAnimation(Role.MAFIA);
                                        break;
                                    case MAFIA_DON:
                                        StartAnimation(Role.MAFIA_DON);
                                        break;
                                    case MANIAC:
                                        StartAnimation(Role.MANIAC);
                                        break;
                                    case POISONER:
                                        StartAnimation(Role.POISONER);
                                        break;
                                    case JOURNALIST:
                                        StartAnimation(Role.JOURNALIST);
                                        break;
                                }
                            }
                            break;
                        case DAY:
                            IV_influence_doctor.setVisibility(View.GONE);
                            player.setVoted_at_night(false);
                            player.setCan_write(true);
                            IV_influence_bodyguard.setVisibility(View.GONE);
                            if (player.getRole() == Role.BODYGUARD && IV_influence_lover.getVisibility() != View.VISIBLE) {
                                StartAnimation(Role.BODYGUARD);
                            }
                            break;
                        case VOTING:
                            messages_can_write = 10;
                            if (IV_influence_lover.getVisibility() != View.VISIBLE)
                            {
                                if (player.getRole() == Role.TERRORIST) {
                                    StartAnimation(Role.TERRORIST);
                                } else {
                                    StartAnimation(Role.VOTING);
                                }
                            }
                            break;
                    }
                }
            });
        }
    };

    //принимает роль
    private Emitter.Listener onRole = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String role;
                    try {
                        role = data.getString("role");
                        player.setRole(ConvertToRole(role));
                        SharedPreferences.Editor editor = mSettings.edit();
                        editor.putString(APP_PREFERENCES_LAST_ROLE, role);
                        editor.apply();
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        View viewNewRole = getLayoutInflater().inflate(R.layout.dialog_new_role, null);
                        builder.setView(viewNewRole);

                        TextView TV_role = viewNewRole.findViewById(R.id.dialogNewRole_TV_mainText);
                        TextView TV_roleDescription = viewNewRole.findViewById(R.id.dialogNewRole_TV_roleDescription);
                        ImageView IV_role = viewNewRole.findViewById(R.id.dialogNewRole_IV_role);

                        for (int i = 0; i < list_users.size(); i++)
                        {
                            if (list_users.get(i).getNick().equals(player.getNick()))
                            {
                                list_users.get(i).setRole(player.getRole());
                                break;
                            }
                        }
                        //playersAdapter.refresh(list_users);
                        playersAdapter.notifyDataSetChanged();

                        switch (player.getRole())
                        {
                            case NONE:
                                TV_role.setText("У вас нет роли");
                                TV_roleDescription.setText("Извините, но что-то пошло не так");
                                IV_role.setImageResource(R.drawable.journalist_round);
                                IV_role.setImageResource(R.drawable.ic_error);
                                break;
                            case CITIZEN:
                                TV_role.setText("Ваша роль - Мирный житель");
                                TV_roleDescription.setText("Мирный житель - он живет в городе, где никого не знает. Его задача выяснить, кто из жителей стоит на стороне мафии, а кто - нет, и уничтожить всех мафиози, пока они не уничтожили весь город. Каждый раз по окончанию дня мирные жители могут голосовать за того, кого они считают мафией.");
                                IV_role.setImageResource(R.drawable.citizen_round);
                                break;
                            case MAFIA:
                                TV_role.setText("Ваша роль - мафия");
                                TV_roleDescription.setText("Мафия - неотъемлемый персонаж игры. Мафия образует опасную группировку из своих членов, и ее цель - уничтожить все мирное население города. Каждый член мафии знает всех остальных мафиози. Ночью мафия может обсуждать в отдельном чате свои мысли и выбирать свою новую жертву. Присутствует во всех играх.");
                                IV_role.setImageResource(R.drawable.mafia_round);
                                break;
                            case SHERIFF:
                                TV_role.setText("Ваша роль - Шериф");
                                TV_roleDescription.setText("Шериф - играет на стороне мирных жителей. Задача шерифа - упростить мирным жителям задачу поиска мафии. Ночью шериф может проверить любого игрока и узнать его роль. Присутствует во всех играх.");
                                IV_role.setImageResource(R.drawable.sheriff_round);
                                break;
                            case DOCTOR:
                                TV_role.setText("Ваша роль - Доктор");
                                TV_roleDescription.setText("Доктор - играет на стороне мирных. Доктор может защитить любого игрока от смерти ночью, тем самым увеличивая шансы мирных жителей на победу. Также доктор сможет защитить от смерти игрока, к которому пришел отравитель.");
                                IV_role.setImageResource(R.drawable.doctor_round);
                                break;
                            case LOVER:
                                TV_role.setText("Ваша роль - Любовница");
                                TV_roleDescription.setText("Любовница - на стороне мирных жителей. Никто не способен противостоять ее красоте, даже члены мафии, чем любовница и пользуется, чтобы помочь мирным. Ночью любовница самая первая может использовать свои чары против любого игрока, тем самым лишая его способностей роли и возможности голосовать днем. ");
                                IV_role.setImageResource(R.drawable.lover_round);
                                break;
                            case MAFIA_DON:
                                TV_role.setText("Ваша роль - Дон мафии");
                                TV_roleDescription.setText("Дон мафии - лидер мафиозной группировки. Дон обладает теми же способностями, что и обычная мафия, но его голос на ночном выборе жертвы считается за два.");
                                IV_role.setImageResource(R.drawable.mafia_don_round);
                                break;
                            case MANIAC:
                                TV_role.setText("Ваша роль - Маньяк");
                                TV_roleDescription.setText("Маньяк - играет за мирных. Маньяк примкнул к стороне мирных, потому что у него появился ночной конкурент - мафия. Маньяк не желает, чтобы его цели убивал кто-то еще, поэтому он старается каждую ночь убить кого-то из клана мафии.");
                                IV_role.setImageResource(R.drawable.maniac_round);
                                break;
                            case TERRORIST:
                                TV_role.setText("Ваша роль - Террорист");
                                TV_roleDescription.setText("Террорист - играет на стороне мафии. Он не знает, кто член мафиозной группировки, однако мафия знает, кто террорист, и не может его убить ночью. Задача террориста - во время дневного голосования выгодно подорвать свою жизнь вместе с жизнью мирного игрока, обладающего значительными способностями, например, шерифа или любовницу.");
                                IV_role.setImageResource(R.drawable.terrorist_round);
                                break;
                            case BODYGUARD:
                                TV_role.setText("Ваша роль - Телохранитель");
                                TV_roleDescription.setText("Телохранитель - играет на стороне мирных. Телохранитель может прийти к любому игроку днем и защищать его от смерти до конца следующей ночи. Если террорист попробует взорвать игрока, который под защитой телохранителя, то умрет только террорист, но игрок уже останется без защиты телохранителя. Защита телохранителя от смерти не действует для смерти по результатам дневного голосования. ");
                                IV_role.setImageResource(R.drawable.bodyguard_round);
                                break;
                            case DOCTOR_OF_EASY_VIRTUE:
                                TV_role.setText("Ваша роль - Доктор легкого поведения");
                                TV_roleDescription.setText("Доктор легкого поведения - на стороне мирных. Имеет опыт любовницы, однако его призвание - врачевание, поэтому ночью доктор легкого поведения может решить, воспользоваться ему способностями любовницы или доктора.\n");
                                IV_role.setImageResource(R.drawable.doctor_of_easy_virtue_round);
                                break;
                            case POISONER:
                                TV_role.setText("Ваша роль - Отравитель");
                                TV_roleDescription.setText("Отравитель - играет за мафию. Отравитель не знает, кто мафия, а мафия не знает, кто отравитель, поэтому при неосторожной игре отравитель может быть убит мафией ночью. Отравитель ночью может отравить любого игрока, который умрет на следующий день после голосования, если к нему не придет доктор или телохранитель. Отравленный человек настолько обессилел, что может написать только одно сообщение следующим днем. ");
                                IV_role.setImageResource(R.drawable.poisoner_round);
                                break;
                            case JOURNALIST:
                                TV_role.setText("Ваша роль - Агент СМИ");
                                TV_roleDescription.setText("Агент СМИ - проводит расследования на стороне мирных жителей. Ночью он может проверить любых двух игроков и выяснить, играют ли они в одной команде или нет.\n");
                                IV_role.setImageResource(R.drawable.journalist_round);
                                break;
                        }
                        Log.d("kkk", "Socket_принять - role " + role);

                        AlertDialog alert = builder.create();
                        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        alert.show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    //запускается при падении сервера, чтобы продолжать игру
    private Emitter.Listener onRestart = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final JSONObject json2 = new JSONObject();
                    try {
                        json2.put("nick", player.getNick());
                        json2.put("room", player.getRoom_num());
                        json2.put("last_message_num", num);
                        json2.put("last_dead_message_num", -1);
                        json2.put("session_id", MainActivity.Session_id);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    socket.emit("connect_to_room", json2);
                    Log.d("kkk", "CONNECT");
                }
            });
        }
    };

    private Emitter.Listener onRoleAction = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String role;
                    try {
                        role = data.getString("role");
                        Log.d("kkk", "Socket_принять - role_action " + args[0]);
                        switch (role)
                        {
                            case "doctor":
                                IV_influence_doctor.setVisibility(View.VISIBLE);
                                break;
                            case "lover":
                                StopAnimation();
                                IV_influence_lover.setVisibility(View.VISIBLE);
                                break;
                            case "sheriff":
                                IV_influence_sheriff.setVisibility(View.VISIBLE);
                                break;
                            case "bodyguard":
                                IV_influence_bodyguard.setVisibility(View.VISIBLE);
                                break;
                            case "poisoner":
                                IV_influence_poisoner.setVisibility(View.VISIBLE);
                                break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener onKnowRole = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String nick;
                    try {
                        Log.d("kkk", "Socket_принять - know_role " + args[0]);
                        Role role = ConvertToRole(data.getString("role"));
                        nick = data.getString("nick");
                        for (int i = 0; i < list_users.size(); i++)
                        {
                            if (list_users.get(i).getNick().equals(nick))
                            {
                                list_users.get(i).setRole(role);
                                break;
                            }
                        }
                        playersAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener onSystemMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String message, time, status, mafia_nick, user_nick, voter, nick;
                    int test_num, money, exp;
                    JSONObject data2;
                    try {
                        status = data.getString("status");
                        test_num = data.getInt("num");
                        time = data.getString("time");
                        time = getDate(time);
                        message = data.getString("message");
                        Log.d("kkk", "system message - " + " Длина listchat = " + list_chat.size() + " /  testnum = " + test_num + " / num = " + num + " , status - " + status + "/" +  data);
                        if (test_num != num) {
                            if (test_num > num) {
                                num = test_num;
                            }
                            switch (status)
                            {
                                case "game_over":
                                    money = data.getInt("money");
                                    exp = data.getInt("exp");

                                    AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
                                    View view_end_game = getLayoutInflater().inflate(R.layout.dialog_end_game, null);
                                    builder2.setView(view_end_game);

                                    TextView TV_message = view_end_game.findViewById(R.id.dialogEndGame_TV_title);
                                    TextView TV_money = view_end_game.findViewById(R.id.dialogEndGame_TV_money);
                                    TextView TV_exp = view_end_game.findViewById(R.id.dialogEndGame_TV_exp);

                                    TV_message.setText(message);
                                    TV_money.setText(String.valueOf(money));
                                    TV_exp.setText(String.valueOf(exp));

                                    AlertDialog alert2 = builder2.create();
                                    alert2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                    alert2.show();

                                    StopAnimation();

                                    socket.off("connect");
                                    socket.off("disconnect");
                                    socket.off("get_in_room");
                                    socket.off("user_message");
                                    socket.off("leave_room");
                                    socket.off("timer");
                                    socket.off("time");
                                    socket.off("role");
                                    socket.off("restart");
                                    socket.off("role_action");
                                    socket.off("know_role");
                                    socket.off("system_message");
                                    socket.off("user_error");
                                    socket.off("mafias");
                                    socket.off("get_my_game_info");
                                    socket.off("success_get_in_room");
                                    socket.off("get_profile");
                                    socket.off("host_info");
                                    socket.off("ban_user_in_room");
                                    socket.off("ban_user_in_room_error");
                                    socket.off("user_message_delay");
                                    socket.off("send_complaint");
                                    socket.off("my_friend_request");
                                    break;
                                case "dead_user":
                                    data2 = data.getJSONObject("message");
                                    message = data2.getString("message");
                                    nick = data2.getString("nick");
                                    Role role = ConvertToRole(data2.getString("role"));
                                    switch (role) {
                                        case CITIZEN:
                                        case SHERIFF:
                                        case DOCTOR:
                                        case LOVER:
                                        case MANIAC:
                                        case BODYGUARD:
                                        case DOCTOR_OF_EASY_VIRTUE:
                                        case JOURNALIST:
                                            peaceful_now--;
                                            break;
                                        case MAFIA_DON:
                                        case POISONER:
                                        case TERRORIST:
                                        case MAFIA:
                                            mafia_now--;
                                            break;
                                    }
                                    TV_mafia_count.setText("Мафия " + mafia_now + "/" + mafia_max);
                                    TV_peaceful_count.setText("Мирные " + peaceful_now + "/" + peaceful_max);
                                    for (int i = 0; i < list_users.size(); i++)
                                    {
                                        if (list_users.get(i).getNick().equals(nick)) {
                                            list_users.get(i).setRole(role);
                                            list_users.get(i).setAlive(false);
                                            if (nick.equals(player.getNick())) {
                                                StopAnimation();
                                                IV_influence_doctor.setVisibility(View.GONE);
                                                IV_influence_lover.setVisibility(View.GONE);
                                                IV_influence_sheriff.setVisibility(View.GONE);
                                                IV_influence_bodyguard.setVisibility(View.GONE);
                                                IV_influence_poisoner.setVisibility(View.GONE);
                                                player.setStatus("dead");
                                                player.setCan_write(true);
                                                if (player.getTime() == Time.DAY)
                                                {
                                                    dayTime.setText("день");
                                                    dayTime.setBackgroundResource(R.drawable.grey_button);
                                                }
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                                View viewNewRole = getLayoutInflater().inflate(R.layout.dialog_died_user, null);
                                                builder.setView(viewNewRole);
                                                AlertDialog alert = builder.create();
                                                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                                alert.show();
                                                break;
                                            }
                                        }
                                    }
                                    if (data2.has("nick_2")) {
                                        String nick2 = data2.getString("nick_2");
                                        Role role2 = ConvertToRole(data2.getString("role_2"));
                                        switch (role2) {
                                            case CITIZEN:
                                            case SHERIFF:
                                            case DOCTOR:
                                            case LOVER:
                                            case MANIAC:
                                            case BODYGUARD:
                                            case DOCTOR_OF_EASY_VIRTUE:
                                            case JOURNALIST:
                                                peaceful_now--;
                                                break;
                                            case MAFIA_DON:
                                            case POISONER:
                                            case TERRORIST:
                                            case MAFIA:
                                                mafia_now--;
                                                break;
                                        }
                                        TV_mafia_count.setText("Мафия " + mafia_now + "/" + mafia_max);
                                        TV_peaceful_count.setText("Мирные " + peaceful_now + "/" + peaceful_max);
                                        for (int i = 0; i < list_users.size(); i++)
                                        {
                                            if (list_users.get(i).getNick().equals(nick2)) {
                                                list_users.get(i).setRole(role2);
                                                list_users.get(i).setAlive(false);
                                                if (nick.equals(player.getNick())) {
                                                    StopAnimation();
                                                    IV_influence_doctor.setVisibility(View.GONE);
                                                    IV_influence_lover.setVisibility(View.GONE);
                                                    IV_influence_sheriff.setVisibility(View.GONE);
                                                    IV_influence_bodyguard.setVisibility(View.GONE);
                                                    IV_influence_poisoner.setVisibility(View.GONE);
                                                    player.setStatus("dead");
                                                    player.setCan_write(true);
                                                    if (player.getTime() == Time.DAY)
                                                    {
                                                        dayTime.setText("день");
                                                        dayTime.setBackgroundResource(R.drawable.grey_button);
                                                    }

                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                                    View viewNewRole = getLayoutInflater().inflate(R.layout.dialog_died_user, null);
                                                    builder.setView(viewNewRole);
                                                    AlertDialog alert = builder.create();
                                                    alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                                    alert.show();
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    playersAdapter.notifyDataSetChanged();
                                    break;
                                case "role_action_mafia":
                                    data2 = data.getJSONObject("message");
                                    mafia_nick = data2.getString("mafia_nick");
                                    user_nick = data2.getString("user_nick");
                                    boolean is_don = data.getBoolean("is_don");
                                    for (int i = 0; i < list_users.size(); i++) {
                                        if (list_users.get(i).getNick().equals(user_nick)) {
                                            if (!is_don) {
                                                list_users.get(i).setVoting_number(list_users.get(i).getVoting_number() + 1);
                                            }
                                            else
                                            {
                                                list_users.get(i).setVoting_number(list_users.get(i).getVoting_number() + 2);
                                            }
                                            break;
                                        }
                                    }
                                    playersAdapter.notifyDataSetChanged();
                                    break;
                                case "voting":
                                    data2 = data.getJSONObject("message");
                                    voter = data2.getString("voter");
                                    user_nick = data2.getString("user_nick");
                                    String nick_from_iterator;
                                    String avatar = "";
                                    for (int i = 0; i < list_users.size(); i++) {
                                        if (list_users.get(i).getNick().equals(user_nick)) {
                                            list_users.get(i).setVoting_number(list_users.get(i).getVoting_number() + 1);
                                        }
                                    }
                                    playersAdapter.notifyDataSetChanged();
                                    break;
                                case "time_info":
                                    break;
                                case "journalist":
                                    data2 = data.getJSONObject("message");
                                    message = data2.getString("message");
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

    private final Emitter.Listener onUserError = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if (getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("kkk", "Socket_принять - user_error " + args[0]);
                    JSONObject data = (JSONObject) args[0];
                    String error;
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    View viewError = getLayoutInflater().inflate(R.layout.dialog_error, null);
                    builder.setView(viewError);
                    TextView TV_error = viewError.findViewById(R.id.dialogError_TV_errorText);
                    try {
                        error = data.getString("error");
                        AlertDialog alert;
                        switch (error) {
                            case "game_has_been_started":
                                TV_error.setText("Игра уже началась");
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
                                break;
                            case "max_people_in_room":
                                TV_error.setText("В комнате нет мест");
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
                                break;
                            case "you_are_playing_in_another_room":
                                TV_error.setText("Вы играете в другой игре");
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
                                break;
                            case "game_is_over":
                                TV_error.setText("Игра закончена");
                                break;
                            case "you_are_banned_in_this_room":
                                TV_error.setText("Вы забанены в этой комнате");
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
                                break;
                            case "you_are_already_friends":
                                TV_error.setText("Вы уже друзья!");
                                break;
                            case "you_are_already_sent_request":
                                TV_error.setText("Вы уже отправили запрос этому человеку!");
                                break;
                            case "you_are_already_got_request":
                                TV_error.setText("Вы уже получили запрос в друзья от этого человека!");
                                break;
                            default:
                                TV_error.setText("Что-то пошло не так");
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
                                break;
                        }
                        alert = builder.create();
                        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        alert.show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener onMafias = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    JSONObject data2;
                    String nick;
                    Log.d("kkk", "Socket_принять - mafias - " + args[0]);
                    try {
                        data2 = data.getJSONObject("mafias");
                        for (Iterator iterator = data2.keys(); iterator.hasNext();)
                        {
                            nick = (String) iterator.next();
                            Role role = ConvertToRole(data2.getString(nick));
                            for (int i = 0; i < list_users.size(); i++)
                            {
                                if (list_users.get(i).getNick().equals(nick))
                                {
                                    if (role == Role.MAFIA_DON)
                                    {

                                    }
                                    list_users.get(i).setRole(role);
                                }
                            }
                            playersAdapter.notifyDataSetChanged();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };  

    private Emitter.Listener onGetMyGameInfo = new Emitter.Listener()  {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String role = "", status = "", time = "", nick;
                    boolean can_act = false, can_vote = false, last_message = false, can_skip_day = true;
                    boolean sheriff = false, doctor = false, lover = false, bodyguard = false, poisoner = false;
                    int voted_number;
                    Log.d("kkk", "Socket_принять - get_my_game_info - " + args[0]);
                    JSONObject data2;
                    JSONObject influences;
                    try {
                        if (data.has("sheriff_checks"))
                        {
                            data2 = data.getJSONObject("sheriff_checks");
                            for (Iterator iterator = data2.keys(); iterator.hasNext();)
                            {
                                nick = (String) iterator.next();
                                Role role2 = ConvertToRole(data2.getString(nick));
                                for (int i = 0; i < list_users.size(); i++)
                                {
                                    if (list_users.get(i).getNick().equals(nick))
                                    {
                                        list_users.get(i).setRole(role2);
                                        break;
                                    }
                                }
                            }
                            playersAdapter.notifyDataSetChanged();
                        }
                        if (data.has("journalist_checks"))
                        {
                            JSONArray journalist_checks;
                            journalist_checks = data.getJSONArray("journalist_checks");
                            for (int i = 0; i < journalist_checks.length(); i++)
                            {
                                for (int j = 0; j < list_users.size(); j++)
                                {
                                    if (list_users.get(j).getNick().equals(journalist_checks.getString(i)))
                                    {
                                        list_users.get(j).setChecked(true);
                                    }
                                }
                            }
                            playersAdapter.notifyDataSetChanged();
                        }
                        if (!data.isNull("voting"))
                        {
                            DeleteNumbersFromVoting();
                            JSONObject voting = data.getJSONObject("voting");
                            for (Iterator iterator = voting.keys(); iterator.hasNext();)
                            {
                                nick = (String) iterator.next();
                                voted_number = voting.getInt(nick);
                                for (int i = 0; i < list_users.size(); i++)
                                {
                                    if (list_users.get(i).getNick().equals(nick)) {
                                        list_users.get(i).setVoting_number(voted_number);
                                    }
                                }
                            }
                            playersAdapter.notifyDataSetChanged();
                        }
                        else {
                            DeleteNumbersFromVoting();
                        }
                        can_skip_day = data.getBoolean("can_skip_day");
                        messages_can_write = data.getInt("messages_counter");
                        role = data.getString("role");
                        status = data.getString("status");
                        time = data.getString("time");
                        can_vote = data.getBoolean("can_vote");
                        can_act = data.getBoolean("can_act");
                        influences = data.getJSONObject("influences");
                        sheriff = influences.getBoolean("sheriff");
                        doctor = influences.getBoolean("doctor");
                        lover = influences.getBoolean("lover");
                        bodyguard = influences.getBoolean("bodyguard");
                        poisoner = influences.getBoolean("poisoner");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (sheriff) IV_influence_sheriff.setVisibility(View.VISIBLE);
                    if (doctor) IV_influence_doctor.setVisibility(View.VISIBLE);
                    if (lover) IV_influence_lover.setVisibility(View.VISIBLE);
                    if (bodyguard) IV_influence_bodyguard.setVisibility(View.VISIBLE);
                    if (poisoner) IV_influence_poisoner.setVisibility(View.VISIBLE);


                    player.setRole(ConvertToRole(role));
                    player.setStatus(status);
                    for (int i = 0; i < list_users.size(); i++)
                    {
                        if (list_users.get(i).getNick().equals(player.getNick()))
                        {
                            list_users.get(i).setRole(player.getRole());
                        }
                    }
                    switch (time) {
                        case "lobby":
                            player.setTime(Time.LOBBY);
                            dayTime.setText("лобби");
                            dayTime.setBackgroundResource(R.drawable.green_button);
                            Constrain.setBackgroundResource(R.drawable.fon_day);
                            break;
                        case "night_love":
                            DeleteNumbersFromVoting();
                            player.setTime(Time.NIGHT_LOVE);
                            dayTime.setText("ночь");
                            dayTime.setBackgroundResource(R.drawable.died_button);
                            Constrain.setBackgroundResource(R.drawable.fon_night);
                            break;
                        case "night_other":
                            player.setTime(Time.NIGHT_OTHER);
                            dayTime.setText("ночь");
                            dayTime.setBackgroundResource(R.drawable.died_button);
                            Constrain.setBackgroundResource(R.drawable.fon_night);
                            break;
                        case "day":
                            DeleteNumbersFromVoting();
                            player.setTime(Time.DAY);
                            if (player.getStatus().equals("alive")) {
                                dayTime.setText("пропуск дня");
                                if (can_skip_day) {
                                    dayTime.setBackgroundResource(R.drawable.green_button);
                                }
                                else
                                {
                                    dayTime.setBackgroundResource(R.drawable.grey_button);
                                }
                            }
                            else
                            {
                                dayTime.setText("день");
                                dayTime.setBackgroundResource(R.drawable.grey_button);
                            }
                            Constrain.setBackgroundResource(R.drawable.fon_day);
                            break;
                        case "voting":
                            player.setTime(Time.VOTING);
                            dayTime.setText("Голосование");
                            dayTime.setBackgroundResource(R.drawable.grey_button);
                            Constrain.setBackgroundResource(R.drawable.fon_day);
                            break;
                    }
                    if (TV_mafia_count.getVisibility() != View.VISIBLE)
                    {
                        int players = list_users.size();
                        mafia_max = list_mafias[players];
                        peaceful_max = list_peaceful[players];
                        mafia_now = mafia_now + mafia_max;
                        peaceful_now = peaceful_now + peaceful_max;
                        TV_mafia_count.setText("Мафия " + mafia_now + "/" + mafia_max);
                        TV_peaceful_count.setText("Мирные " + peaceful_now + "/" + peaceful_max);
                        TV_mafia_count.setVisibility(View.VISIBLE);
                        TV_peaceful_count.setVisibility(View.VISIBLE);
                    }
                    if (player.getStatus().equals("alive")) {
                        player.setCan_write(false);
                        switch (player.getTime()) {
                            case NIGHT_LOVE:
                                IV_influence_poisoner.setVisibility(View.GONE);
                                IV_influence_lover.setVisibility(View.GONE);
                                switch (player.getRole()) {
                                    case MAFIA:
                                        player.setCan_write(true);
                                        break;
                                    case MAFIA_DON:
                                        player.setCan_write(true);
                                        break;
                                    case LOVER:
                                        if (can_act) StartAnimation(Role.LOVER);
                                }
                                break;
                            case NIGHT_OTHER:
                                switch (player.getRole()) {
                                    case MAFIA:
                                    case MAFIA_DON:
                                        if (can_act) StartAnimation(Role.MAFIA);
                                        break;
                                    case DOCTOR:
                                        if (can_act) StartAnimation(Role.DOCTOR);
                                        break;
                                    case SHERIFF:
                                        if (can_act) StartAnimation(Role.SHERIFF);
                                        break;
                                    case MANIAC:
                                        if (can_act) StartAnimation(Role.MANIAC);
                                        break;
                                    case DOCTOR_OF_EASY_VIRTUE:
                                        if (can_act) StartAnimation(Role.DOCTOR_OF_EASY_VIRTUE);
                                        break;
                                    case POISONER:
                                        if (can_act) StartAnimation(Role.POISONER);
                                        break;
                                    case JOURNALIST:
                                        if (can_act) StartAnimation(Role.JOURNALIST);
                                        break;
                                    default:
                                        Log.d("kkk", "В " + player.getTime() + " - нельзя активировать роль " + player.getRole());
                                }
                                break;
                            case DAY:
                                IV_influence_doctor.setVisibility(View.GONE);
                                player.setVoted_at_night(false);
                                player.setCan_write(true);
                                IV_influence_bodyguard.setVisibility(View.GONE);
                                if (player.getRole() == Role.BODYGUARD && IV_influence_lover.getVisibility() != View.VISIBLE) {
                                    StartAnimation(Role.BODYGUARD);
                                }
                                break;
                            case VOTING:
                                messages_can_write = 10;
                                if (IV_influence_lover.getVisibility() != View.VISIBLE)
                                {
                                    if (player.getRole() == Role.TERRORIST) {
                                        StartAnimation(Role.TERRORIST);
                                    } else {
                                        StartAnimation(Role.VOTING);
                                    }
                                }
                        }
                    }
                    else {
                        player.setCan_write(true);
                    }
                }
            });
        }
    };

    private Emitter.Listener onSuccessGetInRoom = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final JSONObject json = new JSONObject();
                    try {
                        json.put("nick", player.getNick());
                        json.put("room", player.getRoom_num());
                        json.put("last_message_num", num);
                        json.put("last_dead_message_num", -1);
                        json.put("session_id", player.getSession_id());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    socket.emit("connect_to_room", json);
                    Log.e("kkk", "connect_to_room - " + player.getRoom_num() + " " + json);
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

                    Button btn_add_friend = view_profile.findViewById(R.id.itemProfile_btn_addFriend);
                    Button btn_kick = view_profile.findViewById(R.id.itemProfile_btn_kickFromRoom);
                    Button btn_send_message = view_profile.findViewById(R.id.itemProfile_btn_sendMessage);
                    Button btn_report = view_profile.findViewById(R.id.itemProfile_btn_report);
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

                    if (player.isHost())
                    {
                        String finalNick1 = nick;
                        btn_kick.setOnClickListener(v -> {
                            if (player.getTime() == Time.LOBBY) {
                                if (!timer.getText().equals("\u221e")) {
                                    if (Integer.parseInt(String.valueOf(timer.getText())) > 5) {
                                        final JSONObject json2 = new JSONObject();
                                        try {
                                            json2.put("nick", MainActivity.NickName);
                                            json2.put("session_id", MainActivity.Session_id);
                                            json2.put("room", player.getRoom_num());
                                            json2.put("ban_nick", finalNick1);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        Log.d("kkk", "Socket_отправка_ban_user_in_room - " + json2.toString());
                                        socket.emit("ban_user_in_room", json2);
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
                                    } else {
                                        AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
                                        View viewError = getLayoutInflater().inflate(R.layout.dialog_error, null);
                                        builder2.setView(viewError);
                                        AlertDialog alert2;
                                        TextView TV_error = viewError.findViewById(R.id.dialogError_TV_errorText);
                                        TV_error.setText("Нельзя никого выгонять за несколько секунд до начала игры!");
                                        alert2 = builder2.create();
                                        alert2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                        alert2.show();
                                    }
                                }
                                else
                                {
                                    final JSONObject json2 = new JSONObject();
                                    try {
                                        json2.put("nick", MainActivity.NickName);
                                        json2.put("session_id", MainActivity.Session_id);
                                        json2.put("room", player.getRoom_num());
                                        json2.put("ban_nick", finalNick1);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    Log.d("kkk", "Socket_отправка_ban_user_in_room - " + json2.toString());
                                    socket.emit("ban_user_in_room", json2);
                                }
                            }
                        });
                    }
                    else
                    {
                        btn_kick.setVisibility(View.GONE);
                    }

                    if (!online) {
                        TV_onlineOffline.setText("не в сети");
                    }

                    final String[] reason = {""};

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

                        alert2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        alert2.show();
                    });

                    String finalNick = nick;
                    String finalUser_id_ = user_id_2;
                    btn_send_message.setOnClickListener(v -> {
                        final JSONObject json2 = new JSONObject();
                        try {
                            json2.put("nick", MainActivity.NickName);
                            json2.put("session_id", MainActivity.Session_id);
                            json2.put("room", player.getRoom_num());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d("kkk", "Socket_отправка_leave_user - " + json2.toString());
                        socket.emit("leave_room", json2);

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

                    if (avatar != null) {
                        IV_avatar.setImageBitmap(fromBase64(avatar));
                    }

                    String finalAvatar1 = avatar;
                    IV_avatar.setOnClickListener(v12 -> {
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
                        View view_avatar = getLayoutInflater().inflate(R.layout.dialog_avatar, null);
                        builder2.setView(view_avatar);

                        ImageView IV_dialog_avatar = view_avatar.findViewById(R.id.dialogAvatar_avatar);
                        Button btn_exit_avatar = view_avatar.findViewById(R.id.dialogAvatar_btn_exit);

                        IV_dialog_avatar.setImageBitmap(fromBase64(finalAvatar1));

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

    private final Emitter.Listener OnHostInfo = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject data = (JSONObject) args[0];
                Log.d("kkk", "принял - host_info - " + data);
                String host_nick = "";
                int ban_limit = 5;

                try {
                    host_nick = data.getString("host_nick");
                    ban_limit = data.getInt("ban_limit");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                player.setHost_nick(host_nick);
                player.setBan_limit(ban_limit);
            }
        });
    };

    private final Emitter.Listener OnBanUserInRoom = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject data = (JSONObject) args[0];
                Log.d("kkk", "принял - ban_user_in_room - " + data);
                String nick = "";
                String time = "";
                int test_num = -1;
                try {
                    test_num = data.getInt("num");
                    nick = data.getString("nick");
                    time = data.getString("time");
                    time = getDate(time);
                } catch (JSONException e) {
                    return;
                }
                if (test_num > num)
                {
                    num = test_num;
                }
                if (nick.equals(MainActivity.NickName))
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    View viewError = getLayoutInflater().inflate(R.layout.dialog_error, null);
                    builder.setView(viewError);
                    TextView TV_title = viewError.findViewById(R.id.dialogError_TV_errorTitle);
                    TextView TV_error = viewError.findViewById(R.id.dialogError_TV_errorText);
                    TV_title.setText("Вас кикнули из комнаты!");
                    TV_error.setText("Попробуйте зайти в другие комнаты");

                    AlertDialog alert = builder.create();
                    alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alert.show();
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
                }
                for (int i=list_users.size()-1; i>=0; i--)
                {
                    if (list_users.get(i).getNick().equals(nick))
                    {
                        Log.d("kkk", "remove " + list_users.get(i).getNick());
                        list_users.remove(i);
                    }
                }
                playersAdapter.notifyDataSetChanged();
            }
        });
    };

    private final Emitter.Listener OnBanUserInRoomError = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("kkk", "принял - ban_user_in_room_error");
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                View viewError = getLayoutInflater().inflate(R.layout.dialog_error, null);
                builder.setView(viewError);
                TextView TV_error = viewError.findViewById(R.id.dialogError_TV_errorText);
                TV_error.setText("Вы не можете кикать админов из комнаты!");

                AlertDialog alert = builder.create();
                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alert.show();
            }
        });
    };

    private final Emitter.Listener OnUserMessageDelay = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //JSONObject data = (JSONObject) args[0];
                Log.d("kkk", "принял - user_message_delay - " + args[0]);
                String time_to_unmute = String.valueOf(args[0]);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                View viewError = getLayoutInflater().inflate(R.layout.dialog_error, null);
                builder.setView(viewError);
                TextView TV_title = viewError.findViewById(R.id.dialogError_TV_errorTitle);
                TextView TV_error = viewError.findViewById(R.id.dialogError_TV_errorText);
                TV_title.setText("Вы слишком часто отправляете сообщения!");
                TV_error.setText("Вы сможете написать следующее сообщение через " + time_to_unmute + " секунд");

                AlertDialog alert = builder.create();
                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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

    private final Emitter.Listener onMySendRequest = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject data = (JSONObject) args[0];
                Log.d("kkk", "принял - my_send_request - " + data);
                String status = "";
                try {
                    status = data.getString("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (status.equals("OK"))
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Запрос отправлен!")
                            .setMessage("")
                            .setIcon(R.drawable.ic_ok)
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

    private Emitter.Listener onDailyTaskCompleted = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String title;
                    String description;
                    String prizeType;
                    int prize;
                    int progress;
                    int maxProgress;
                    boolean is_completed;
                    Log.d("kkk", "Socket_принять - daily_tasl_completed - " + args[0]);
                    try {
                        title = data.getString("title");
                        description = data.getString("description");
                        prizeType = data.getString("prize_type");
                        prize = data.getInt("award");
                        progress = data.getInt("current_num");
                        maxProgress = data.getInt("max_num");
                        is_completed = data.getBoolean("is_completed");
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        View viewCompletedDialog = getLayoutInflater().inflate(R.layout.dialog_completed_daily_task, null);
                        builder.setView(viewCompletedDialog);

                        TextView TV_title = viewCompletedDialog.findViewById(R.id.dialogEndGame_TV_title);
                        TextView TV_description = viewCompletedDialog.findViewById(R.id.dialogEndGame_TV_youGet);
                        ImageView IV_prize = viewCompletedDialog.findViewById(R.id.dialogCompleteDailyTask_IV_prize);
                        TextView TV_prize = viewCompletedDialog.findViewById(R.id.dialogCompleteDailyTask_TV_prize);
                        ProgressBar PB = viewCompletedDialog.findViewById(R.id.dialogCompleteDailyTask_PB_horizontal);
                        TextView TV_progress = viewCompletedDialog.findViewById(R.id.dialogCompleteDailyTask_TV_progress);

                        TV_title.setText(title);
                        TV_description.setText(description);
                        if (prizeType.equals("exp"))
                        {
                            IV_prize.setImageResource(R.drawable.experience);
                            TV_prize.setText(prize + " XP");
                        }
                        else
                        {
                            IV_prize.setImageResource(R.drawable.money);
                            TV_prize.setText(prize + " $");
                        }
                        TV_progress.setText(progress + "/" + maxProgress);
                        PB.setMax(maxProgress);
                        PB.setProgress(progress);

                        AlertDialog alert;
                        alert = builder.create();
                        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        alert.show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    /*******************************
     *                             *
     *       SOCKETS end           *
     *                             *
     *******************************/

    //запуск анимации
    public void StartAnimation(Role type) {
        player.setCan_click(true);
        for (int i = 0; i < list_users.size(); i++)
        {
            if (list_users.get(i).getAlive())
            {
                switch (type)
                {
                    case JOURNALIST:
                        list_users.get(i).setAnimation_type(type);
                        break;
                    default:
                        if (!list_users.get(i).getNick().equals(player.getNick()))
                        {
                            list_users.get(i).setAnimation_type(type);
                            break;
                        }
                }

            }
            playersAdapter.notifyDataSetChanged();
        }
    }
    //конец анимации
    public void StopAnimation() {
        player.setCan_click(false);
        for (int i = 0; i < list_users.size(); i++)
        {
            list_users.get(i).setAnimation_type(Role.NONE);
        }
        playersAdapter.notifyDataSetChanged();
    }
    //действие роли
    public void RoleAction(String nick) {
        final JSONObject json3 = new JSONObject();
        try {
            json3.put("nick", player.getNick());
            json3.put("session_id", player.getSession_id());
            json3.put("room", player.getRoom_num());
            json3.put("influence_on_nick", nick);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("role_action", json3);
        Log.d("kkk", "Socket_отправка - role_action"+ json3.toString());
    }
    //дневное голосование
    public void Voting(String nick) {
        final JSONObject json3 = new JSONObject();
        try {
            json3.put("nick", player.getNick());
            json3.put("session_id", player.getSession_id());
            json3.put("room", player.getRoom_num());
            json3.put("influence_on_nick", nick);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("voting", json3);
        Log.d("kkk", "Socket_отправка - voting"+ json3.toString());
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
    //Вывод профиля
    public void ShowProfile(String nick) {
        final JSONObject json = new JSONObject();
        try {
            json.put("nick", player.getNick());
            json.put("session_id", player.getSession_id());
            json.put("info_nick", nick);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("get_profile", json);
        Log.d("kkk", "Socket_отправка - get_profile - "+ json.toString());
    }
    //убрать цифры из голосования
    public void DeleteNumbersFromVoting() {
        for (int i = 0; i < list_users.size(); i++)
        {
            list_users.get(i).setVoting_number(0);
        }
        playersAdapter.notifyDataSetChanged();
    }
    //Привести дату к местному времени
    public String getDate(String ourDate) {
        try
        {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date value = formatter.parse(ourDate);

            SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm"); //this format changeable
            dateFormatter.setTimeZone(TimeZone.getDefault());
            ourDate = dateFormatter.format(value);
        }
        catch (Exception e)
        {
            ourDate = "00:00";
        }
        return ourDate;
    }
}