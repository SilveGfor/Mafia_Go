package com.mafiago.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.mafiago.R;
import com.google.android.material.tabs.TabLayout;
import com.mafiago.MainActivity;
import com.mafiago.adapters.FriendsAdapter;
import com.mafiago.classes.OnBackPressedListener;
import com.mafiago.models.FriendModel;
import com.mafiago.pager_adapters.FriendsPagerAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.emitter.Emitter;

import static com.mafiago.MainActivity.socket;

public class FriendsFragment extends Fragment implements OnBackPressedListener {

    public TabLayout tabLayout;

    public ViewPager viewPager;

    public ListView friendsView;

    public ImageView Menu;
    public TextView TV_no_friends;
    public ProgressBar PB_loading;
    RelativeLayout btn_back;

    public FriendsAdapter friendsAdapter;

    ArrayList<FriendModel> list_friends = new ArrayList<>();

    public JSONObject json;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        tabLayout = view.findViewById(R.id.fragmentFriends_tabLayout);
        viewPager = view.findViewById(R.id.fragmentFriends_viewPager);
        Menu = view.findViewById(R.id.fragmentMenu_IV_menu);
        btn_back = view.findViewById(R.id.fragmentGamesList_RL_back);

        socket.off("get_friend");
        socket.off("get_friend_request");
        socket.off("accept_friend_request_to_me");
        socket.off("user_error");
        socket.off("get_my_friend_request");

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

        socket.on("get_profile", OnGetProfile);

        // Получаем ViewPager и устанавливаем в него адаптер
        viewPager.setAdapter(
                new FriendsPagerAdapter(getActivity().getSupportFragmentManager()));

        // Передаём ViewPager в TabLayout
        tabLayout.setupWithViewPager(viewPager);

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new MenuFragment()).commit();
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
    public void onBackPressed() {
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new MenuFragment()).commit();
    }

    private final Emitter.Listener OnGetFriendRequest = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PB_loading.setVisibility(View.GONE);
                if (args.length != 0) {
                    TV_no_friends.setVisibility(View.GONE);
                    JSONObject data = (JSONObject) args[0];
                    Log.d("kkk", "принял - get_friend_request - " + data);
                    String nick = "", user_id_1 = "", user_id_2 = "", message = "", avatar = "";
                    int playing_room_num = 0;
                    boolean online = false;
                    try {
                        nick = data.getString("nick");
                        avatar = data.getString("avatar");
                        user_id_2 = data.getString("user_id");
                        online = data.getBoolean("is_online");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    list_friends.add(new FriendModel(nick, user_id_2, online, avatar, true));
                    friendsAdapter.notifyDataSetChanged();
                }
                else
                {
                    TV_no_friends.setVisibility(View.VISIBLE);
                }
            }
        });
    };

    private final Emitter.Listener OnGetMyFriendRequest = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PB_loading.setVisibility(View.GONE);
                if (args.length != 0) {
                    TV_no_friends.setVisibility(View.GONE);
                    Log.d("kkk", "принял - get_my_friend_request - " + args[0]);
                    JSONObject data = (JSONObject) args[0];
                    String nick = "", user_id_1 = "", user_id_2 = "", message = "", avatar = "";
                    int playing_room_num = 0;
                    boolean online = false;
                    try {
                        nick = data.getString("nick");
                        avatar = data.getString("avatar");
                        user_id_2 = data.getString("user_id");
                        online = data.getBoolean("is_online");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    list_friends.add(new FriendModel(nick, user_id_2, online, avatar, true));
                    friendsAdapter.notifyDataSetChanged();
                }
                else
                {
                    TV_no_friends.setVisibility(View.VISIBLE);
                }
            }
        });
    };

    private final Emitter.Listener OnAcceptFriendRequestToMe = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("kkk", "принял - accept_friend_request_to_me - " + args[0]);
                JSONObject data = (JSONObject) args[0];
                String status = "";
                boolean accept = false;
                try {
                    status = data.getString("status");
                    accept = data.getBoolean("accept");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                AlertDialog alert;
                if (status.equals("OK"))
                {

                    if (accept)
                    {
                        builder.setTitle("Вы приняли заявку в друзья!")
                                .setMessage("")
                                .setIcon(R.drawable.ic_ok)
                                .setCancelable(false)
                                .setNegativeButton("Ок",
                                        (dialog, id) -> dialog.cancel());
                    }
                    else
                    {
                        builder.setTitle("Вы отклонили заявку в друзья!")
                                .setMessage("")
                                .setIcon(R.drawable.ic_error)
                                .setCancelable(false)
                                .setNegativeButton("Ок",
                                        (dialog, id) -> dialog.cancel());
                    }
                }
                else
                {
                    builder.setTitle("Извините, но что-то пошло не так")
                            .setMessage("")
                            .setIcon(R.drawable.ic_error)
                            .setCancelable(false)
                            .setNegativeButton("Ок",
                                    (dialog, id) -> dialog.cancel());
                }
                alert = builder.create();
                alert.show();
            }
        });
    };

    private final Emitter.Listener OnUserError = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("kkk", "принял - user_error - " + args[0]);
                JSONObject data = (JSONObject) args[0];
                String error = "";
                try {
                    error = data.getString("error");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                AlertDialog alert;
                switch (error)
                {
                    case "you_are_already_friends":
                        builder.setTitle("Вы уже друзья!")
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
                        builder.setTitle("Извините, но что-то пошло не так")
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
            }
        });
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
                String general_pers_of_wins = "", mafia_pers_of_wins = "", peaceful_pers_of_wins = "", main_status = "", main_personal_color = "";
                int was_citizen = 0, was_sheriff = 0, was_doctor = 0, was_lover = 0, was_journalist = 0, was_bodyguard = 0, was_doctor_of_easy_virtue = 0, was_maniac = 0, was_mafia = 0, was_mafia_don = 0, was_terrorist = 0, was_poisoner = 0;

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
                    general_pers_of_wins = statistic.getString("general_wins");
                    mafia_pers_of_wins = statistic.getString("mafia_wins");
                    peaceful_pers_of_wins = statistic.getString("peaceful_wins");
                    main_status = data.getString("main_status");
                    main_personal_color = data.getString("main_personal_color");

                    was_citizen = statistic.getInt("was_citizen");
                    was_sheriff = statistic.getInt("was_sheriff");
                    was_doctor = statistic.getInt("was_doctor");
                    was_lover = statistic.getInt("was_lover");
                    was_journalist = statistic.getInt("was_journalist");
                    was_bodyguard = statistic.getInt("was_bodyguard");
                    was_doctor_of_easy_virtue = statistic.getInt("was_doctor_of_easy_virtue");
                    was_maniac = statistic.getInt("was_maniac");
                    was_mafia = statistic.getInt("was_mafia");
                    was_mafia_don = statistic.getInt("was_mafia_don");
                    was_terrorist = statistic.getInt("was_terrorist");
                    was_poisoner = statistic.getInt("was_poisoner");

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
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                View view_profile = getLayoutInflater().inflate(R.layout.item_profile, null);
                builder.setView(view_profile);
                AlertDialog alert = builder.create();

                Button btn_add_friend = view_profile.findViewById(R.id.dialogOkNo_btn_no);
                Button btn_kick = view_profile.findViewById(R.id.itemProfile_btn_kickFromRoom);
                Button btn_send_message = view_profile.findViewById(R.id.itemGold_btn_buy);
                Button btn_report = view_profile.findViewById(R.id.dialogOkNo_btn_yes);
                ImageView IV_avatar = view_profile.findViewById(R.id.itemProfile_IV_avatar);
                ImageView IV_online = view_profile.findViewById(R.id.itemProfile_IV_online);
                TextView TV_nick = view_profile.findViewById(R.id.itemProfile_TV_nick);

                TextView TV_exp = view_profile.findViewById(R.id.itemDailyTask_TV_prize);
                TextView TV_rang = view_profile.findViewById(R.id.itemProfile_TV_rang);
                TextView TV_game_counter = view_profile.findViewById(R.id.itemProfile_TV_gamesCouner);
                TextView TV_max_money_score = view_profile.findViewById(R.id.itemProfile_TV_maxMoney);
                TextView TV_max_exp_score = view_profile.findViewById(R.id.itemProfile_TV_maxExp);
                TextView TV_general_pers_of_wins = view_profile.findViewById(R.id.itemProfile_TV_percentWins);
                TextView TV_mafia_pers_of_wins = view_profile.findViewById(R.id.itemProfile_TV_percentMafiaWins);
                TextView TV_peaceful_pers_of_wins = view_profile.findViewById(R.id.itemProfile_TV_percentPeacefulWins);
                TextView TV_status = view_profile.findViewById(R.id.itemProfile_TV_status);

                TextView TV_gamesCitizen = view_profile.findViewById(R.id.itemProfile_TV_gamesCitizen);
                TextView TV_gamesSheriff = view_profile.findViewById(R.id.itemProfile_TV_gamesSheriff);
                TextView TV_gamesDoctor = view_profile.findViewById(R.id.itemProfile_TV_gamesDoctor);
                TextView TV_gamesLover = view_profile.findViewById(R.id.itemProfile_TV_gamesLover);
                TextView TV_gamesJournalist = view_profile.findViewById(R.id.itemProfile_TV_gamesJournalist);
                TextView TV_gamesBodyguard = view_profile.findViewById(R.id.itemProfile_TV_gamesBodyguard);
                TextView TV_gamesManiac = view_profile.findViewById(R.id.itemProfile_TV_gamesManiac);
                TextView TV_gamesDoctorOfEasyVirtue = view_profile.findViewById(R.id.itemProfile_TV_gamesDoctorOfEasyVirtue);
                TextView TV_gamesMafia = view_profile.findViewById(R.id.itemProfile_TV_gamesMafia);
                TextView TV_gamesMafiaDon = view_profile.findViewById(R.id.itemProfile_TV_gamesMafiaDon);
                TextView TV_gamesTerrorist = view_profile.findViewById(R.id.itemProfile_TV_gamesTerrorist);
                TextView TV_gamesPoisoner = view_profile.findViewById(R.id.itemProfile_TV_gamesPoisoner);

                TV_gamesCitizen.setText("Мирный житель: " + was_citizen);
                TV_gamesSheriff.setText("Шериф: " + was_sheriff);
                TV_gamesDoctor.setText("Доктор: " + was_doctor);
                TV_gamesLover.setText("Любовница: " + was_lover);
                TV_gamesJournalist.setText("Агент СМИ: " + was_journalist);
                TV_gamesBodyguard.setText("Телохранитель: " + was_bodyguard);
                TV_gamesManiac.setText("Маньяк: " + was_maniac);
                TV_gamesDoctorOfEasyVirtue.setText("Доктор лёгкоо поведения: " + was_doctor_of_easy_virtue);
                TV_gamesMafia.setText("Мафия: " + was_mafia);
                TV_gamesMafiaDon.setText("Дон мафии: " + was_mafia_don);
                TV_gamesTerrorist.setText("Террорист: " + was_terrorist);
                TV_gamesPoisoner.setText("Отравитель: " + was_poisoner);

                TV_nick.setText(nick);
                if (!main_status.equals("")) {
                    TV_status.setText("{" + main_status + "}");
                }
                if (!main_personal_color.equals("")) {
                    TV_nick.setTextColor(Color.parseColor(main_personal_color));
                    TV_status.setTextColor(Color.parseColor(main_personal_color));
                }

                TV_exp.setText(exp + " XP");
                TV_rang.setText(rang + " ранг");
                TV_game_counter.setText("Сыграно игр " + game_counter);
                TV_max_money_score.setText("Макс. монет за игру " + max_money_score);
                TV_max_exp_score.setText("Макс. опыта за игру " + max_exp_score);
                TV_general_pers_of_wins.setText("Процент побед " + general_pers_of_wins);
                TV_mafia_pers_of_wins.setText("Побед за мафию " + mafia_pers_of_wins);
                TV_peaceful_pers_of_wins.setText("Побед за мирных " + peaceful_pers_of_wins);

                btn_kick.setVisibility(View.GONE);

                if (online) {
                    IV_online.setVisibility(View.VISIBLE);
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

                    if (finalAvatar != null) {
                        IV_dialog_avatar.setImageBitmap(fromBase64(finalAvatar));
                    }

                    AlertDialog alert2 = builder2.create();

                    btn_exit_avatar.setOnClickListener(v13 -> {
                        alert2.cancel();
                    });

                    alert2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alert2.show();
                });

                String finalNick = nick;
                String finalUser_id_ = user_id_2;
                btn_send_message.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alert.cancel();
                    }
                });

                btn_report.setVisibility(View.GONE);

                String finalUser_id_2 = user_id_2;
                btn_add_friend.setVisibility(View.GONE);

                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alert.show();
            }
        });
    };
}