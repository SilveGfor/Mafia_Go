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
                        alert.cancel();
                        MainActivity.User_id_2 = finalUser_id_;
                        MainActivity.NickName_2 = finalNick;
                        MainActivity.bitmap_avatar_2 = fromBase64(finalAvatar);
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new PrivateMessagesFragment()).commit();
                    });

                    btn_add_friend.setVisibility(View.INVISIBLE);

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
}