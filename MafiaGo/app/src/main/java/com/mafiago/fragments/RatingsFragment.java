package com.mafiago.fragments;

import android.app.AlertDialog;
import android.content.Intent;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.mafiago.R;
import com.google.android.material.tabs.TabLayout;
import com.mafiago.MainActivity;
import com.mafiago.adapters.FriendsAdapter;
import com.mafiago.classes.OnBackPressedListener;
import com.mafiago.enums.Time;
import com.mafiago.models.FriendModel;
import com.mafiago.pager_adapters.FriendsPagerAdapter;
import com.mafiago.pager_adapters.RatingsPagerAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.emitter.Emitter;

import static com.mafiago.MainActivity.socket;
import static com.mafiago.fragments.MenuFragment.GALLERY_REQUEST;

public class RatingsFragment extends Fragment implements OnBackPressedListener {

    public TabLayout tabLayout;

    public ViewPager viewPager;

    public ImageView Menu;
    RelativeLayout btn_back;

    public JSONObject json;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ratings, container, false);
        tabLayout = view.findViewById(R.id.fragmentRatings_tabLayout);
        viewPager = view.findViewById(R.id.fragmentRatings_viewPager);
        Menu = view.findViewById(R.id.fragmentMenu_IV_menu);
        btn_back = view.findViewById(R.id.fragmentRatings_RL_back);

        socket.off("get_rating");
        socket.off("get_profile");

        socket.on("get_profile", OnGetProfile);

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

        // Получаем ViewPager и устанавливаем в него адаптер
        viewPager.setAdapter(
                new RatingsPagerAdapter(getActivity().getSupportFragmentManager()));

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
                if (!nick.equals(MainActivity.NickName)) {
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
                    TV_gamesDoctorOfEasyVirtue.setText("Доктор лёгкого поведения: " + was_doctor_of_easy_virtue);
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
                    btn_send_message.setOnClickListener(v -> {
                        alert.cancel();
                        MainActivity.User_id_2 = finalUser_id_;
                        MainActivity.NickName_2 = finalNick;
                        MainActivity.bitmap_avatar_2 = fromBase64(finalAvatar);
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new PrivateMessagesFragment()).commit();
                    });

                    btn_report.setVisibility(View.GONE);

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
                    ImageView IV_online = view_profile.findViewById(R.id.dialogMyProfile_IV_online);
                    TextView TV_nick = view_profile.findViewById(R.id.dialogMyProfile_TV_nick);
                    TextView TV_status = view_profile.findViewById(R.id.dialogMyProfile_TV_status);

                    TextView TV_game_counter = view_profile.findViewById(R.id.dialogMyProfile_TV_gamesCouner);
                    TextView TV_max_money_score = view_profile.findViewById(R.id.dialogMyProfile_TV_maxMoney);
                    TextView TV_max_exp_score = view_profile.findViewById(R.id.dialogMyProfile_TV_maxExp);
                    TextView TV_general_pers_of_wins = view_profile.findViewById(R.id.dialogMyProfile_TV_percentWins);
                    TextView TV_mafia_pers_of_wins = view_profile.findViewById(R.id.dialogMyProfile_TV_percentMafiaWins);
                    TextView TV_peaceful_pers_of_wins = view_profile.findViewById(R.id.dialogMyProfile_TV_percentPeacefulWins);

                    TextView TV_gamesCitizen = view_profile.findViewById(R.id.dialogMyProfile_TV_gamesCitizen);
                    TextView TV_gamesSheriff = view_profile.findViewById(R.id.dialogMyProfile_TV_gamesSheriff);
                    TextView TV_gamesDoctor = view_profile.findViewById(R.id.dialogMyProfile_TV_gamesDoctor);
                    TextView TV_gamesLover = view_profile.findViewById(R.id.dialogMyProfile_TV_gamesLover);
                    TextView TV_gamesJournalist = view_profile.findViewById(R.id.dialogMyProfile_TV_gamesJournalist);
                    TextView TV_gamesBodyguard = view_profile.findViewById(R.id.dialogMyProfile_TV_gamesBodyguard);
                    TextView TV_gamesManiac = view_profile.findViewById(R.id.dialogMyProfile_TV_gamesManiac);
                    TextView TV_gamesDoctorOfEasyVirtue = view_profile.findViewById(R.id.dialogMyProfile_TV_gamesDoctorOfEasyVirtue);
                    TextView TV_gamesMafia = view_profile.findViewById(R.id.dialogMyProfile_TV_gamesMafia);
                    TextView TV_gamesMafiaDon = view_profile.findViewById(R.id.dialogMyProfile_TV_gamesMafiaDon);
                    TextView TV_gamesTerrorist = view_profile.findViewById(R.id.dialogMyProfile_TV_gamesTerrorist);
                    TextView TV_gamesPoisoner = view_profile.findViewById(R.id.dialogMyProfile_TV_gamesPoisoner);

                    TV_gamesCitizen.setText("Мирный житель: " + was_citizen);
                    TV_gamesSheriff.setText("Шериф: " + was_sheriff);
                    TV_gamesDoctor.setText("Доктор: " + was_doctor);
                    TV_gamesLover.setText("Любовница: " + was_lover);
                    TV_gamesJournalist.setText("Агент СМИ: " + was_journalist);
                    TV_gamesBodyguard.setText("Телохранитель: " + was_bodyguard);
                    TV_gamesManiac.setText("Маньяк: " + was_maniac);
                    TV_gamesDoctorOfEasyVirtue.setText("Доктор лёгкого поведения: " + was_doctor_of_easy_virtue);
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
                    if (online)
                    {
                        IV_online.setVisibility(View.VISIBLE);
                    }

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

                    TV_game_counter.setText("Сыграно игр: " + game_counter);
                    TV_max_money_score.setText("Макс. монет за игру: " + max_money_score);
                    TV_max_exp_score.setText("Макс. опыта за игру: " + max_exp_score);
                    TV_general_pers_of_wins.setText("Процент побед: " + general_pers_of_wins);
                    TV_mafia_pers_of_wins.setText("Побед за мафию: " + mafia_pers_of_wins);
                    TV_peaceful_pers_of_wins.setText("Побед за мирных: " + peaceful_pers_of_wins);

                    TV_gold.setText(gold + " золота");
                    TV_money.setText(money + " $");
                    TV_exp.setText(exp + " XP");
                    TV_rang.setText(rang + " ранг");

                    AlertDialog alert = builder.create();
                    alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alert.show();
                }
            }
        });
    };

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
}