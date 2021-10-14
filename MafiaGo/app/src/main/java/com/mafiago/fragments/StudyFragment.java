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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.mafiago.R;
import com.google.android.material.tabs.TabLayout;
import com.mafiago.MainActivity;
import com.mafiago.adapters.MessageAdapter;
import com.mafiago.adapters.PlayersAdapter;
import com.mafiago.classes.OnBackPressedListener;
import com.mafiago.enums.Role;
import com.mafiago.enums.Time;
import com.mafiago.models.MessageModel;
import com.mafiago.models.Player;
import com.mafiago.models.RoleModel;
import com.mafiago.models.UserModel;
import com.mafiago.pager_adapters.GameChatPagerAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

import static com.mafiago.MainActivity.socket;

public class StudyFragment extends Fragment implements OnBackPressedListener {

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

    public RelativeLayout btn_back;

    public ImageView IV_influence_doctor;
    public ImageView IV_influence_lover;
    public ImageView IV_influence_sheriff;
    public ImageView IV_influence_bodyguard;
    public ImageView IV_influence_poisoner;
    public ImageView Menu;

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

        btn_back = view.findViewById(R.id.fragmentGamesList_RL_back);
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

        player = new Player(MainActivity.NickName, MainActivity.Session_id, MainActivity.Game_id, MainActivity.Role);

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
            dev = "VlAn_VOR";
        }

        list_users.add(new UserModel(MainActivity.NickName, Role.NONE, null));
        list_users.add(new UserModel(dev, Role.NONE, null));
        list_users.add(new UserModel("Игрок 1", Role.NONE, null));
        list_users.add(new UserModel("Игрок 2", Role.NONE, null));
        list_users.add(new UserModel("Игрок 3", Role.NONE, null));
        list_users.add(new UserModel("Игрок 4", Role.NONE, null));
        list_chat.add(new MessageModel(0, MainActivity.NickName + " вошёл(-а) в чат", "19-00", MainActivity.NickName, "ConnectMes", null));
        list_chat.add(new MessageModel(0, dev + " вошёл(-а) в чат", "19-00", MainActivity.NickName, "ConnectMes", null));
        list_chat.add(new MessageModel(0, "Игрок 1 вошёл(-а) в чат", "19-00", MainActivity.NickName, "ConnectMes", null));
        list_chat.add(new MessageModel(0, "Игрок 2 вошёл(-а) в чат", "19-00", MainActivity.NickName, "ConnectMes", null));
        list_chat.add(new MessageModel(0, "Игрок 3 вошёл(-а) в чат", "19-00", MainActivity.NickName, "ConnectMes", null));
        list_chat.add(new MessageModel(0, "Игрок 4 вошёл(-а) в чат", "19-00", MainActivity.NickName, "ConnectMes", null));

        playersAdapter.notifyDataSetChanged();
        messageAdapter.notifyDataSetChanged();

        switch (role)
        {
            case "mafia":
                list_chat.add(new MessageModel(0, "Приветствую тебя, ты попал на ускоренные курсы по подготовке юных мафиози! Я один из разработчиков Mafia Go и сейчас расскажу тебе основы. Нажми \"Далее\" для продолжения", "19-00", dev, "UsersMes", "alive", "user", null, ""));
                messageAdapter.notifyDataSetChanged();
                TV_continue.setOnClickListener(v -> {
                    switch (continue_num)
                    {
                        case 0:
                            list_chat.add(new MessageModel(0, "Каждому новичку необходимо пройти обучение по программе \"Мафия\", остальные роли можно освоить по желанию", "19-00", dev, "UsersMes", "alive", "user", null, ""));
                            messageAdapter.notifyDataSetChanged();
                            continue_num++;
                            break;
                        case 1:
                            list_chat.add(new MessageModel(0, "В игре есть 5 фаз: лобби, 1 фаза ночи, 2 фаза ночи, день и голосование. В фазе 'лобби' игроки собираются в комнате и ждут начала игры. Как только зайдёт минимальное количество человек для данной комнаты, пойдёт обратный отсчёт до начала игры", "19-00", dev, "UsersMes", "alive", "user", null, ""));
                            messageAdapter.notifyDataSetChanged();
                            continue_num++;
                            if (TotalItemsCount < FirstVisibleItem + VisibleItemsCount + 3) {
                                LV_chat.setSelection(messageAdapter.getCount() - 1);
                            }
                            break;
                        case 2:
                            list_chat.add(new MessageModel(0, "Наступила ночь! Пока город спит, путаны идут на охоту!", "19-00", "Server", "SystemMes"));
                            list_chat.add(new MessageModel(0, "Сразу после начала игры начинается 1 фаза ночи. Во время первой фазы ночи ходят любовница и доктор лёгкого поведения, мафия и дон мафии общаются в ночном чате, который видно только им, а остальные жители города мирно спят", "19-00", dev, "UsersMes", "alive", "user", null, ""));
                            list_users.get(0).setRole(Role.MAFIA);
                            list_users.get(1).setRole(Role.MAFIA_DON);

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
                            if (TotalItemsCount < FirstVisibleItem + VisibleItemsCount + 3) {
                                LV_chat.setSelection(messageAdapter.getCount() - 1);
                            }
                            break;
                        case 3:
                            list_chat.add(new MessageModel(0, "После 1 фазы ночи наступает 2 фаза. Во второй фазе большинство ролей могут применить свои способности. Ты - мафия, а значит ты можешь выбрать свою жертву (но писать во вторую фазу ночи уже нельзя). Почти в любой игре есть доктор, а это значит, что если все мафиози будут бить одного человека, а доктор случайно будет его лечить, то ночью никто не умрёт, поэтому мафия всегда должна делить свои голоса хотя бы на 2 человека. Давай я буду бить Игрока 4, а ты будешь бить игрока 3?", "19-00", dev, "UsersMes", "alive", "user", null, ""));

                            messageAdapter.notifyDataSetChanged();
                            continue_num++;
                            if (TotalItemsCount < FirstVisibleItem + VisibleItemsCount + 3) {
                                LV_chat.setSelection(messageAdapter.getCount() - 1);
                            }
                            break;
                        case 4:
                            list_chat.add(new MessageModel(0, "Нажми на мигающий пистолет", "19-00", dev, "UsersMes", "alive", "user", null, ""));
                            list_chat.add(new MessageModel(0, "Мафия выходит искать своих жертв, а часть мирного населения идет противостоять мафии!", "19-00", "Server", "SystemMes"));

                            player.setTime(Time.NIGHT_OTHER);
                            dayTime.setText("ночь (2 фаза)");
                            dayTime.setBackgroundResource(R.drawable.died_button);
                            Constrain.setBackgroundResource(R.drawable.fon_night);

                            list_chat.add(new MessageModel(0,dev + " голосует за Игрок 4", "19-00", dev, "VotingMes", null));
                            list_users.get(5).setVoting_number(2);

                            list_users.get(2).setAnimation_type(Role.MAFIA);
                            list_users.get(3).setAnimation_type(Role.MAFIA);
                            list_users.get(4).setAnimation_type(Role.MAFIA);
                            list_users.get(5).setAnimation_type(Role.MAFIA);

                            playersAdapter.notifyDataSetChanged();
                            messageAdapter.notifyDataSetChanged();
                            TV_continue.setVisibility(View.INVISIBLE);
                            continue_num++;
                            if (TotalItemsCount < FirstVisibleItem + VisibleItemsCount + 3) {
                                LV_chat.setSelection(messageAdapter.getCount() - 1);
                            }
                            break;
                        case 5:
                            list_chat.add(new MessageModel(0, "[Игрок 4] найден мертвым в собственной квартире!", "19-00", "Server", "KillMes"));
                            list_chat.add(new MessageModel(0, "Наступил день! Пора высказать свои мысли в чате!", "19-00", "Server", "SystemMes"));
                            for (int i = 0; i < list_users.size(); i++)
                            {
                                list_users.get(i).setVoting_number(0);
                            }
                            list_users.get(5).setAlive(false);
                            list_users.get(5).setRole(Role.DOCTOR);
                            list_chat.add(new MessageModel(0, "День. Город просыпается. Все живые игроки могут общаться в чате. Кстати, мы убили Игрока 4, а это значит, что доктор лечил кого-то другого. Днём можно обсудить погоду, можно кого-то обвинить или оправдать, а можно пропустить день. Если 75% игроков нажмут на кнопку \"Пропустить день\", то сразу начнётся голосование. Это нужно в тех случаях, когда уже очевидно, кого надо бить на голосовании", "19-00", dev, "UsersMes", "alive", "user", null, ""));

                            dayTime.setText("пропуск дня");
                            dayTime.setBackgroundResource(R.drawable.green_button);
                            Constrain.setBackgroundResource(R.drawable.fon_day);

                            messageAdapter.notifyDataSetChanged();
                            playersAdapter.notifyDataSetChanged();
                            continue_num++;
                            if (TotalItemsCount < FirstVisibleItem + VisibleItemsCount + 3) {
                                LV_chat.setSelection(messageAdapter.getCount() - 1);
                            }
                            break;
                        case 6:
                            list_chat.add(new MessageModel(0, "Мне кажется, что [Игрок 2] мафия", "19-00", "Игрок 4", "UsersMes", "last_message", "user", null, ""));
                            list_chat.add(new MessageModel(0, "После смерти каждый игрок имеет право отправить последнее сообщение. Такие сообщения отображаются бежевым цветом. Как только мёртвый игрок отправляет последнее сообщение, он может перелистнуть чат игры направо и попасть в чат мёртвых, где можно обсудить что-то с остальными мёртвыми игроками", "19-00", dev, "UsersMes", "alive", "user", null, ""));
                            messageAdapter.notifyDataSetChanged();
                            continue_num++;
                            if (TotalItemsCount < FirstVisibleItem + VisibleItemsCount + 3) {
                                LV_chat.setSelection(messageAdapter.getCount() - 1);
                            }
                            break;
                        case 7:
                            list_chat.add(new MessageModel(0, "Я шериф и проверил [Игрок 1] - мирный. Но мне тоже кажется, что [Игрок 2] мафит. Давайте убьём", "19-00", "Игрок 1", "UsersMes", "alive", "user", null, ""));
                            list_chat.add(new MessageModel(0, "Обычно шериф раскрывает свою личность в первый день и говорит, кого он проверил (но бывают и лжешерифы, так что будь осторожен). Если шериф проверил мирного жителя, то обычно он пытается рандомно угадать мафию. Сейчас шериф думает, что [Игрок 2] - мафия. Чтобы нас не раскрыли, старайся по возможности голосовать за того, за кого голосует шериф. Тогда он будет думать, что ты мирный. Постарайся на дневном голосовании не выделяться и голосовать вместе со всеми", "19-00", dev, "UsersMes", "alive", "user", null, ""));
                            messageAdapter.notifyDataSetChanged();
                            continue_num++;
                            if (TotalItemsCount < FirstVisibleItem + VisibleItemsCount + 3) {
                                LV_chat.setSelection(messageAdapter.getCount() - 1);
                            }
                            break;
                        case 8:
                            list_chat.add(new MessageModel(0, "Настало время проявить свою гражданскую позицию и проголосовать! Выбирайте кандидатов с умом, ведь потом победитель будет повешен!", "19-00", "Server", "SystemMes"));
                            list_users.get(1).setAnimation_type(Role.VOTING);
                            list_users.get(2).setAnimation_type(Role.VOTING);
                            list_users.get(3).setAnimation_type(Role.VOTING);
                            list_users.get(4).setAnimation_type(Role.VOTING);

                            list_chat.add(new MessageModel(0,"Игрок 1 голосует за Игрок 2", "19-00", "Игрок 1", "VotingMes", null));
                            list_users.get(3).setVoting_number(1);
                            list_chat.add(new MessageModel(0,"Игрок 2 голосует за Игрок 3", "19-00", "Игрок 2", "VotingMes", null));
                            list_users.get(4).setVoting_number(1);
                            list_chat.add(new MessageModel(0,"Игрок 3 голосует за Игрок 2", "19-00", "Игрок 3", "VotingMes", null));
                            list_users.get(3).setVoting_number(2);
                            list_chat.add(new MessageModel(0,dev + " голосует за Игрок 2", "19-00", dev, "VotingMes", null));
                            list_users.get(3).setVoting_number(3);

                            dayTime.setText("голосование");
                            dayTime.setBackgroundResource(R.drawable.grey_button);
                            Constrain.setBackgroundResource(R.drawable.fon_day);

                            playersAdapter.notifyDataSetChanged();
                            messageAdapter.notifyDataSetChanged();
                            TV_continue.setVisibility(View.INVISIBLE);
                            continue_num++;
                            if (TotalItemsCount < FirstVisibleItem + VisibleItemsCount + 3) {
                                LV_chat.setSelection(messageAdapter.getCount() - 1);
                            }
                            break;
                        case 9:
                            list_chat.add(new MessageModel(0, "Этим днем житель [Игрок 2] потерял доверие местного населения, и он был повешен!", "19-00", "Server", "KillMes"));
                            list_chat.add(new MessageModel(0, "Наступила ночь! Пока город спит, путаны идут на охоту!", "19-00", "Server", "SystemMes"));
                            list_users.get(3).setAlive(false);
                            list_users.get(3).setRole(Role.CITIZEN);
                            for (int i = 0; i < list_users.size(); i++)
                            {
                                list_users.get(i).setVoting_number(0);
                            }

                            player.setTime(Time.NIGHT_LOVE);
                            dayTime.setText("ночь (1 фаза)");
                            dayTime.setBackgroundResource(R.drawable.died_button);
                            Constrain.setBackgroundResource(R.drawable.fon_night);

                            messageAdapter.notifyDataSetChanged();
                            playersAdapter.notifyDataSetChanged();
                            continue_num++;
                            if (TotalItemsCount < FirstVisibleItem + VisibleItemsCount + 3) {
                                LV_chat.setSelection(messageAdapter.getCount() - 1);
                            }
                            break;
                        case 10:
                            list_chat.add(new MessageModel(0, "Шериф не угадал мафию, поэтому победа у нас в кармане! Если [Игрок 1] - шериф, то [Игрок 3] - любовница. Любовница сейчас для нас опаснее шерифа, поэтому надо бить её", "19-00", dev, "UsersMes", "alive", "user", null, ""));

                            messageAdapter.notifyDataSetChanged();
                            playersAdapter.notifyDataSetChanged();
                            continue_num++;
                            if (TotalItemsCount < FirstVisibleItem + VisibleItemsCount + 3) {
                                LV_chat.setSelection(messageAdapter.getCount() - 1);
                            }
                            break;
                        case 11:
                            list_chat.add(new MessageModel(0, "Если у тебя появился значок сердечка, то на тебя походила любовница. Из-за этого ты не можешь голосовать этой ночью и днём", "19-00", dev, "UsersMes", "alive", "user", null, ""));

                            animation = AnimationUtils.loadAnimation(getContext(), R.anim.bounce_center);
                            IV_influence_lover.setVisibility(View.VISIBLE);
                            IV_influence_lover.startAnimation(animation);

                            messageAdapter.notifyDataSetChanged();
                            playersAdapter.notifyDataSetChanged();
                            continue_num++;
                            if (TotalItemsCount < FirstVisibleItem + VisibleItemsCount + 3) {
                                LV_chat.setSelection(messageAdapter.getCount() - 1);
                            }
                            break;
                        case 12:
                            list_chat.add(new MessageModel(0, "Мафия выходит искать своих жертв, а часть мирного населения идет противостоять мафии!", "19-00", "Server", "SystemMes"));
                            list_chat.add(new MessageModel(0, "Значок звёздочки означает, что тебя проверил шериф и теперь он знает твою роль", "19-00", dev, "UsersMes", "alive", "user", null, ""));

                            player.setTime(Time.NIGHT_OTHER);
                            dayTime.setText("ночь (2 фаза)");
                            dayTime.setBackgroundResource(R.drawable.died_button);
                            Constrain.setBackgroundResource(R.drawable.fon_night);

                            animation = AnimationUtils.loadAnimation(getContext(), R.anim.bounce_center);
                            IV_influence_sheriff.setVisibility(View.VISIBLE);
                            IV_influence_sheriff.startAnimation(animation);

                            list_chat.add(new MessageModel(0,dev + " голосует за Игрок 3", "19-00", dev, "VotingMes", null));
                            list_users.get(4).setVoting_number(2);

                            playersAdapter.notifyDataSetChanged();
                            messageAdapter.notifyDataSetChanged();
                            continue_num++;
                            if (TotalItemsCount < FirstVisibleItem + VisibleItemsCount + 3) {
                                LV_chat.setSelection(messageAdapter.getCount() - 1);
                            }
                            break;
                        case 13:
                            list_chat.add(new MessageModel(0, "[Игрок 3] найден мертвым в собственной квартире!", "19-00", "Server", "KillMes"));
                            list_chat.add(new MessageModel(0, "Наступил день! Пора высказать свои мысли в чате!", "19-00", "Server", "SystemMes"));
                            for (int i = 0; i < list_users.size(); i++)
                            {
                                list_users.get(i).setVoting_number(0);
                            }
                            list_users.get(4).setAlive(false);
                            list_users.get(4).setRole(Role.LOVER);
                            list_chat.add(new MessageModel(0, "Сейчас голосовать могу только я и шериф, у нас будет дуэль. Надеюсь я выиграю. Если он меня победит, то отомсти за меня ночью! А теперь нажми на кнопку \"пропустить день\"", "19-00", dev, "UsersMes", "alive", "user", null, ""));

                            dayTime.setText("пропуск дня");
                            dayTime.setBackgroundResource(R.drawable.green_button);
                            Constrain.setBackgroundResource(R.drawable.fon_day);

                            playersAdapter.notifyDataSetChanged();
                            messageAdapter.notifyDataSetChanged();
                            TV_continue.setVisibility(View.INVISIBLE);
                            continue_num++;
                            if (TotalItemsCount < FirstVisibleItem + VisibleItemsCount + 3) {
                                LV_chat.setSelection(messageAdapter.getCount() - 1);
                            }
                            break;
                        case 14:
                            list_chat.add(new MessageModel(0,dev + " голосует за Игрок 1", "19-00", dev, "VotingMes", null));
                            list_users.get(2).setVoting_number(1);
                            list_chat.add(new MessageModel(0,"Игрок 1 голосует за " + dev, "19-00", "Игрок 1", "VotingMes", null));
                            list_users.get(1).setVoting_number(1);

                            playersAdapter.notifyDataSetChanged();
                            messageAdapter.notifyDataSetChanged();
                            continue_num++;
                            if (TotalItemsCount < FirstVisibleItem + VisibleItemsCount + 3) {
                                LV_chat.setSelection(messageAdapter.getCount() - 1);
                            }
                            break;
                        case 15:
                            list_chat.add(new MessageModel(0, "Этим днем житель [" + dev + "] потерял доверие местного населения, и он был повешен!", "19-00", "Server", "KillMes"));
                            list_chat.add(new MessageModel(0, "Наступила ночь! Пока город спит, путаны идут на охоту!", "19-00", "Server", "SystemMes"));
                            list_users.get(1).setAlive(false);
                            list_users.get(1).setRole(Role.MAFIA_DON);
                            IV_influence_lover.setVisibility(View.GONE);
                            for (int i = 0; i < list_users.size(); i++)
                            {
                                list_users.get(i).setVoting_number(0);
                            }

                            player.setTime(Time.NIGHT_LOVE);
                            dayTime.setText("ночь (1 фаза)");
                            dayTime.setBackgroundResource(R.drawable.died_button);
                            Constrain.setBackgroundResource(R.drawable.fon_night);

                            playersAdapter.notifyDataSetChanged();
                            messageAdapter.notifyDataSetChanged();
                            continue_num++;
                            if (TotalItemsCount < FirstVisibleItem + VisibleItemsCount + 3) {
                                LV_chat.setSelection(messageAdapter.getCount() - 1);
                            }
                            break;
                        case 16:
                            list_chat.add(new MessageModel(0, "Я научил тебя всему, что знал сам. Теперь иди и побеждай!", "19-00", dev, "UsersMes", "last_message", "user", null, ""));

                            playersAdapter.notifyDataSetChanged();
                            messageAdapter.notifyDataSetChanged();
                            continue_num++;
                            if (TotalItemsCount < FirstVisibleItem + VisibleItemsCount + 3) {
                                LV_chat.setSelection(messageAdapter.getCount() - 1);
                            }
                            break;
                        case 17:
                            list_chat.add(new MessageModel(0, "Мафия выходит искать своих жертв, а часть мирного населения идет противостоять мафии!", "19-00", "Server", "SystemMes"));

                            player.setTime(Time.NIGHT_OTHER);
                            dayTime.setText("ночь (2 фаза)");
                            dayTime.setBackgroundResource(R.drawable.died_button);
                            Constrain.setBackgroundResource(R.drawable.fon_night);

                            list_users.get(2).setAnimation_type(Role.MAFIA);

                            playersAdapter.notifyDataSetChanged();
                            messageAdapter.notifyDataSetChanged();
                            continue_num++;
                            TV_continue.setVisibility(View.INVISIBLE);
                            if (TotalItemsCount < FirstVisibleItem + VisibleItemsCount + 3) {
                                LV_chat.setSelection(messageAdapter.getCount() - 1);
                            }
                            break;
                        case 18:
                            list_chat.add(new MessageModel(0, "[Игрок 1] найден мертвым в собственной квартире!", "19-00", "Server", "KillMes"));
                            list_chat.add(new MessageModel(0, "В результате напряженной борьбы мафиози перебили все мирное население этого города!", "19-00", "Server", "SystemMes"));

                            list_users.get(2).setAlive(false);
                            list_users.get(2).setRole(Role.SHERIFF);
                            list_users.get(2).setVoting_number(0);

                            AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
                            View view_end_game = getLayoutInflater().inflate(R.layout.dialog_end_game, null);
                            builder2.setView(view_end_game);

                            TextView TV_message = view_end_game.findViewById(R.id.dialogEndGame_TV_title);
                            TextView TV_money = view_end_game.findViewById(R.id.dialogEndGame_TV_money);
                            TextView TV_exp = view_end_game.findViewById(R.id.dialogEndGame_TV_exp);

                            TV_message.setText("Поздравляем! Вы упешно прошли обучение, теперь вы готовы ко всему!");
                            TV_money.setText("0");
                            TV_exp.setText("0");

                            AlertDialog alert2 = builder2.create();
                            alert2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            alert2.show();

                            dayTime.setText("пропуск дня");
                            dayTime.setBackgroundResource(R.drawable.green_button);
                            Constrain.setBackgroundResource(R.drawable.fon_day);

                            playersAdapter.notifyDataSetChanged();
                            messageAdapter.notifyDataSetChanged();
                            continue_num++;
                            TV_continue.setText("В меню");
                            TV_continue.setVisibility(View.VISIBLE);
                            if (TotalItemsCount < FirstVisibleItem + VisibleItemsCount + 3) {
                                LV_chat.setSelection(messageAdapter.getCount() - 1);
                            }
                            break;
                        case 19:
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new MenuFragment()).commit();
                    }
                });

                dayTime.setOnClickListener(v -> {
                    if (continue_num == 14)
                    {
                        list_chat.add(new MessageModel(0, "Настало время проявить свою гражданскую позицию и проголосовать! Выбирайте кандидатов с умом, ведь потом победитель будет повешен!", "19-00", "Server", "SystemMes"));

                        dayTime.setText("голосование");
                        dayTime.setBackgroundResource(R.drawable.grey_button);
                        Constrain.setBackgroundResource(R.drawable.fon_day);

                        playersAdapter.notifyDataSetChanged();
                        messageAdapter.notifyDataSetChanged();
                        TV_continue.setVisibility(View.VISIBLE);
                        if (TotalItemsCount < FirstVisibleItem + VisibleItemsCount + 3) {
                            LV_chat.setSelection(messageAdapter.getCount() - 1);
                        }
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
                                list_users.get(5).setAnimation_type(Role.NONE);

                                list_chat.add(new MessageModel(0,MainActivity.NickName + " голосует за " + list_users.get(position).getNick(), "19-00", MainActivity.NickName, "VotingMes", null));
                                list_users.get(position).setVoting_number(list_users.get(position).getVoting_number() + 1);

                                list_chat.add(new MessageModel(0, "Как можно заметить, твой голос на ночном голосовании равен одному, а мой двум. Это происходит потому, что я дон мафии, а голос дона мафии считается ночью за 2. Если доктор не лечит Игрока 4, то он умрёт, т.к. за него большинство голосов (2 моих голоса), ну а если доктор будет лечить Игрока 4, то умрёт Игрок 3, за которого ты голосовал(-а)", "19-00", dev, "UsersMes", "alive", "user", null, ""));

                                messageAdapter.notifyDataSetChanged();
                                playersAdapter.notifyDataSetChanged();
                                if (TotalItemsCount < FirstVisibleItem + VisibleItemsCount + 3) {
                                    LV_chat.setSelection(messageAdapter.getCount() - 1);
                                }
                                TV_continue.setVisibility(View.VISIBLE);
                                break;
                            case 9:
                                list_users.get(1).setAnimation_type(Role.NONE);
                                list_users.get(2).setAnimation_type(Role.NONE);
                                list_users.get(3).setAnimation_type(Role.NONE);
                                list_users.get(4).setAnimation_type(Role.NONE);

                                list_chat.add(new MessageModel(0,MainActivity.NickName + " голосует за " + list_users.get(position).getNick(), "19-00", MainActivity.NickName, "VotingMes", null));
                                list_users.get(position).setVoting_number(list_users.get(position).getVoting_number() + 1);

                                messageAdapter.notifyDataSetChanged();
                                playersAdapter.notifyDataSetChanged();
                                TV_continue.setVisibility(View.INVISIBLE);
                                if (TotalItemsCount < FirstVisibleItem + VisibleItemsCount + 3) {
                                    LV_chat.setSelection(messageAdapter.getCount() - 1);
                                }
                                TV_continue.setVisibility(View.VISIBLE);
                                break;
                            case 18:
                                list_users.get(2).setAnimation_type(Role.NONE);

                                list_chat.add(new MessageModel(0,MainActivity.NickName + " голосует за " + list_users.get(position).getNick(), "19-00", MainActivity.NickName, "VotingMes", null));
                                list_users.get(position).setVoting_number(list_users.get(position).getVoting_number() + 1);

                                messageAdapter.notifyDataSetChanged();
                                playersAdapter.notifyDataSetChanged();
                                if (TotalItemsCount < FirstVisibleItem + VisibleItemsCount + 3) {
                                    LV_chat.setSelection(messageAdapter.getCount() - 1);
                                }
                                TV_continue.setVisibility(View.VISIBLE);
                                break;
                        }
                    }
                });

                break;
        }

        LV_chat.setOnScrollListener(new AbsListView.OnScrollListener() {
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                FirstVisibleItem = firstVisibleItem;
                VisibleItemsCount = visibleItemCount;
                TotalItemsCount = totalItemCount;
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new SettingsFragment()).commit();
            }
        });

        return view;
    }

    @Override
    public void onBackPressed() {
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new SettingsFragment()).commit();
    }
}