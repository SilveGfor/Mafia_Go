package com.mafiago.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.mafiago.R;
import com.google.android.material.tabs.TabLayout;
import com.mafiago.MainActivity;
import com.mafiago.adapters.MessageAdapter;
import com.mafiago.adapters.PlayersAdapter;
import com.mafiago.enums.Role;
import com.mafiago.enums.Time;
import com.mafiago.models.MessageModel;
import com.mafiago.models.Player;
import com.mafiago.models.RoleModel;
import com.mafiago.models.UserModel;
import com.mafiago.pager_adapters.GameChatPagerAdapter;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

public class StudyFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_ROLE = "param1";

    private String role;

    public GridView GV_users;

    public TextView timer;
    public TextView dayTime;
    public TextView room_name;
    public TextView TV_mafia_count;
    public TextView TV_peaceful_count;
    public TextView TV_playersCount;
    public TextView TV_playersMinMaxInfo;
    public TextView TV_continue;

    public ImageView IV_influence_doctor;
    public ImageView IV_influence_lover;
    public ImageView IV_influence_sheriff;
    public ImageView IV_influence_bodyguard;
    public ImageView IV_influence_poisoner;

    public ListView LV_chat;

    public ConstraintLayout Constrain;

    public Player player;

    public Animation animation;

    ArrayList<RoleModel> list_roles = new ArrayList<>();
    ArrayList<MessageModel> list_chat = new ArrayList<>();
    ArrayList<UserModel> list_users = new ArrayList<>();

    JSONObject users = new JSONObject();

    int answer_id = -1;
    public int StopTimer = 0;
    public String dev = "";
    int num = -1;
    int continue_num = 0;

    PlayersAdapter playersAdapter;

    MessageAdapter messageAdapter;

    public int FirstVisibleItem = 0, VisibleItemsCount = 0,TotalItemsCount = 0;

    public static final String APP_PREFERENCES = "user";
    public static final String APP_PREFERENCES_LAST_ROLE = "role";

    private SharedPreferences mSettings;

    public static StudyFragment newInstance(String role) {
        StudyFragment fragment = new StudyFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROLE, role);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            role = getArguments().getString(ARG_ROLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_study, container, false);

        LV_chat = view.findViewById(R.id.fragmentStudy_LV_chat);
        mSettings = getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        GV_users = view.findViewById(R.id.fragmentStudy_listUsers);
        room_name = view.findViewById(R.id.fragmentStudy_TV_roomName);
        TV_mafia_count = view.findViewById(R.id.fragmentStudy_TV_mafiaCount);
        TV_peaceful_count = view.findViewById(R.id.fragmentStudy_TV_peacefulCount);
        TV_playersCount = view.findViewById(R.id.fragmentStudy_TV_playersCount);
        TV_playersMinMaxInfo = view.findViewById(R.id.fragmentStudy_TV_playersMinMaxInfo);
        TV_continue = view.findViewById(R.id.fragmentStudy_TV_continue);

        timer = view.findViewById(R.id.fragmentStudy_TV_timer);
        dayTime = view.findViewById(R.id.fragmentStudy_TV_dayTime);

        Constrain = view.findViewById(R.id.fragmentStudy_CL);

        IV_influence_doctor = view.findViewById(R.id.fragmentStudy_ic_doctor);
        IV_influence_lover = view.findViewById(R.id.fragmentStudy_ic_lover);
        IV_influence_sheriff = view.findViewById(R.id.fragmentStudy_ic_sheriff);
        IV_influence_bodyguard = view.findViewById(R.id.fragmentStudy_ic_bodyguard);
        IV_influence_poisoner = view.findViewById(R.id.fragmentStudy_ic_poisoner);

        messageAdapter = new MessageAdapter(list_chat, getContext());
        LV_chat.setAdapter(messageAdapter);
        playersAdapter = new PlayersAdapter(list_users, getContext());
        GV_users.setAdapter(playersAdapter);

        Random rd = new Random();
        if (rd.nextInt(2) == 1)
        {
            dev = "SilveGfor";
        }
        else
        {
            dev = "Vlan_Vor";
        }

        list_users.add(new UserModel(MainActivity.NickName, Role.NONE, null));
        list_users.add(new UserModel(dev, Role.NONE, null));
        list_users.add(new UserModel("Игрок 2", Role.NONE, null));
        list_users.add(new UserModel("Игрок 3", Role.NONE, null));
        list_users.add(new UserModel("Игрок 4", Role.NONE, null));
        list_users.add(new UserModel("Игрок 5", Role.NONE, null));
        list_chat.add(new MessageModel(0, MainActivity.NickName + " вошёл(-а) в чат", "19-00", MainActivity.NickName, "ConnectMes", null));
        list_chat.add(new MessageModel(0, dev + " вошёл(-а) в чат", "19-00", MainActivity.NickName, "ConnectMes", null));
        list_chat.add(new MessageModel(0, "Игрок 2 вошёл(-а) в чат", "19-00", MainActivity.NickName, "ConnectMes", null));
        list_chat.add(new MessageModel(0, "Игрок 3 вошёл(-а) в чат", "19-00", MainActivity.NickName, "ConnectMes", null));
        list_chat.add(new MessageModel(0, "Игрок 5 вошёл(-а) в чат", "19-00", MainActivity.NickName, "ConnectMes", null));

        playersAdapter.notifyDataSetChanged();
        messageAdapter.notifyDataSetChanged();

        switch (role)
        {
            case "mafia":
                list_chat.add(new MessageModel(0, "Приветствую тебя, ты попал на ускоренные курсы по подготовке юных мафиози! Я один из разработчиков Mafia Go и сейчас расскажу тебе основы. Нажми 'Далее' для продолжения", "19-00", dev, "UsersMes", "alive", "user", null));
                messageAdapter.notifyDataSetChanged();
                TV_continue.setOnClickListener(v -> {
                    switch (continue_num)
                    {
                        case 0:
                            list_chat.add(new MessageModel(0, "Каждому новичку необходимо пройти обучение по программе 'Мафия', остальные роли можно освоить по желанию", "19-00", dev, "UsersMes", "alive", "user", null));
                            messageAdapter.notifyDataSetChanged();
                            continue_num++;
                            break;
                        case 1:
                            list_chat.add(new MessageModel(0, "В игре есть 5 фаз: лобби, 1 фаза ночи, 2 фаза ночи, день и голосование. В фазе 'лобби' игроки собираются в комнате и ждут начала игры. Как только зайдёт минимальное кол-во человек для данной комнаты, пойдёт обратный отсчёт до начала игры", "19-00", dev, "UsersMes", "alive", "user", null));
                            messageAdapter.notifyDataSetChanged();
                            continue_num++;
                            break;
                        case 2:
                            list_chat.add(new MessageModel(0, "Сразу после начала игры начинается 1 фаза ночи. Во время первой фазы ночи ходит любовница и доктор лёгкого поведения, мафия и дон мафии общаются в ночном чате, который видно только им, а остальные жители города мирно спят", "19-00", dev, "UsersMes", "alive", "user", null));
                            list_users.get(0).setRole(Role.MAFIA);
                            list_users.get(4).setRole(Role.MAFIA_DON);

                            player.setTime(Time.NIGHT_LOVE);
                            dayTime.setText("ночь (1 фаза)");
                            dayTime.setBackgroundResource(R.drawable.died_button);
                            Constrain.setBackgroundResource(R.drawable.fon_night);

                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            View viewNewRole = getLayoutInflater().inflate(R.layout.dialog_new_role, null);
                            builder.setView(viewNewRole);
                            TextView TV_role = viewNewRole.findViewById(R.id.dialogNewRole_TV_mainText);
                            TextView TV_roleDescription = viewNewRole.findViewById(R.id.dialogNewRole_TV_roleDescription);
                            ImageView IV_role = viewNewRole.findViewById(R.id.dialogNewRole_IV_role);
                            TV_role.setText("Ваша роль - мафия");
                            TV_roleDescription.setText("Мафия - неотъемлемый персонаж игры. Мафия образует опасную группировку из своих членов, и ее цель - уничтожить все мирное население города. Каждый член мафии знает всех остальных мафиози. Ночью мафия может обсуждать в отдельном чате свои мысли и выбирать свою новую жертву. Присутствует во всех играх.");
                            IV_role.setImageResource(R.drawable.mafia_round);
                            AlertDialog alert = builder.create();
                            alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            alert.show();

                            playersAdapter.notifyDataSetChanged();
                            messageAdapter.notifyDataSetChanged();
                            continue_num++;
                            break;
                        case 3:
                            list_chat.add(new MessageModel(0, "После 1 фазы ночи наступает 2 фаза. Во второй фазе большинство ролей могут применить свои способности. Ты - мафия, а значит ты можешь выбрать свою жертву (но писать во вторую фазу ночи уже нельзя). Почти в любой игре есть доктор, а это значит, что если все мафиози будут бить одного человека, а доктор случайно будет его лечить, то ночью никто не умрёт, поэтому мафия всегда должна делить свои голоса хотя бы на 2 человека. Давай я буду бить Игрока 4, а ты будешь бить игрока 3?", "19-00", "Игрок 4", "UsersMes", "alive", "user", null));

                            messageAdapter.notifyDataSetChanged();
                            continue_num++;
                            break;
                        case 4:
                            list_chat.add(new MessageModel(0, "Нажми на мигающий пистолет", "19-00", "Игрок 4", "UsersMes", "alive", "user", null));

                            player.setTime(Time.NIGHT_OTHER);
                            dayTime.setText("ночь (2 фаза)");
                            dayTime.setBackgroundResource(R.drawable.died_button);
                            Constrain.setBackgroundResource(R.drawable.fon_night);

                            list_chat.add(new MessageModel(0,dev + " голосует за Игрок 4", "19-00", dev, "VotingMes", null));
                            list_users.get(4).setVoting_number(2);

                            list_users.get(2).setAnimation_type(Role.MAFIA);
                            list_users.get(3).setAnimation_type(Role.MAFIA);
                            list_users.get(4).setAnimation_type(Role.MAFIA);

                            playersAdapter.notifyDataSetChanged();
                            messageAdapter.notifyDataSetChanged();
                            continue_num++;
                            break;
                        case 5:
                            list_users.get(4).setAlive(false);
                            list_users.get(4).setRole(Role.SHERIFF);
                            list_chat.add(new MessageModel(0, "День. Город просыпается. Все живые игроки могут общаться в чате. Кстати мы убили Игрока 4, значит доктор лечил кого-то другого. Днём можно обсудить погоду, можно кого-то обвинить или оправдать, ну и конечно день можно пропустить. Если 75% игроков нажмут на кнопку 'Пропустить день', то сразу начнётся голосование. Это нужно в тех случаях, когда уже очевидно, кого надо бить на голосовании", "19-00", dev, "UsersMes", "alive", "user", null));

                            dayTime.setText("пропуск дня");
                            dayTime.setBackgroundResource(R.drawable.green_button);
                            Constrain.setBackgroundResource(R.drawable.fon_day);

                            messageAdapter.notifyDataSetChanged();
                            continue_num++;
                            break;
                        case 6:
                            list_chat.add(new MessageModel(0, "Я шериф и я проверил [" + MainActivity.NickName + "]. Он мафия, бейте его на голосовании", "19-00", "Игрок 4", "UsersMes", "last_message", "user", null));
                            list_chat.add(new MessageModel(0, "После смерти каждый игрок имеет право отправить последнее сообщение. Такие сообщения отображаются оранжевым цветом. Как только мёртвый игрок отправляет последнее сообщение, он может перелистнуть чат игры направо и попасть в чат мёртвых, где можно обсудить что-то с остальными мёртвыми игроками", "19-00", dev, "UsersMes", "alive", "user", null));
                            messageAdapter.notifyDataSetChanged();
                            continue_num++;
                            break;
                    }
                });

                GV_users.setOnItemClickListener((parent, view1, position, id) -> {
                    if (list_users.get(position).getAnimation_type() != Role.NONE)
                    {
                        switch (continue_num)
                        {
                            case 5:
                                list_users.get(2).setAnimation_type(Role.NONE);
                                list_users.get(3).setAnimation_type(Role.NONE);
                                list_users.get(4).setAnimation_type(Role.NONE);
                                list_users.get(position).setVoting_number(list_users.get(position).getVoting_number() + 1);

                                animation = AnimationUtils.loadAnimation(getContext(), R.anim.bounce_center);
                                IV_influence_sheriff.setVisibility(View.VISIBLE);
                                IV_influence_sheriff.startAnimation(animation);

                                list_chat.add(new MessageModel(0, "Как можно заметить, твой голос на ночном голосовании равен одному, а мой двум. Это происходит потому, что я дон мафии, а голос дона мафии считается ночью за 2. Если доктор не лечит Игрока 4, то он умрёт, т.к. за него большинство голосов (2 моих голоса), ну а если доктор будет лечить Игрока 4, то умрёт Игрок 3, за которого ты голосовал(-а). Кстати значок со звёздочкой, который появился у тебя означает, что тебя проверил шериф и узнал твою роль. Скорее всего днём он расскажет всем, что ты мафия", "19-00", dev, "UsersMes", "alive", "user", null));

                                messageAdapter.notifyDataSetChanged();
                                playersAdapter.notifyDataSetChanged();
                                break;
                        }
                    }
                });


                break;
        }

        return view;
    }
}