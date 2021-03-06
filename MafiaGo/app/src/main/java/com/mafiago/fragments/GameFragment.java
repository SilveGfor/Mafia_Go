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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.material.tabs.TabLayout;
import com.mafiago.MainActivity;
import com.example.mafiago.R;
import com.mafiago.adapters.MessageAdapter;
import com.mafiago.adapters.PlayersAdapter;
import com.mafiago.adapters.RoleInRoomAdapter;
import com.mafiago.classes.OnBackPressedListener;
import com.mafiago.models.PremiumModel;
import com.mafiago.models.RoleModel;
import com.mafiago.pager_adapters.GameChatPagerAdapter;
import com.mafiago.enums.Role;
import com.mafiago.enums.Time;
import com.mafiago.models.MessageModel;
import com.mafiago.models.Player;
import com.mafiago.models.UserModel;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;

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
import java.util.Timer;
import java.util.TimerTask;

import io.socket.emitter.Emitter;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;
import static com.mafiago.MainActivity.f;
import static  com.mafiago.MainActivity.socket;
import static com.mafiago.fragments.MenuFragment.GALLERY_REQUEST;

public class GameFragment extends Fragment implements OnBackPressedListener{
    private RewardedAd mRewardedAd;

    public GridView gridView_users;

    public TextView timer;
    public TextView dayTime;
    public TextView room_name;
    public TextView TV_mafia_count;
    public TextView TV_peaceful_count;
    public TextView TV_playersCount;
    public TextView TV_playersMinMaxInfo;

    public ImageView IV_influence_doctor;
    public ImageView IV_influence_lover;
    public ImageView IV_influence_sheriff;
    public ImageView IV_influence_bodyguard;
    public ImageView IV_influence_poisoner;
    public ImageView Menu;
    public RelativeLayout btn_back;

    public ConstraintLayout Constrain;

    public Player player;

    public Animation animation;

    boolean poisoner = false;

    private Timer mTimer;
    private MyTimerTask mMyTimerTask;

    public ArrayList<RoleModel> list_roles = new ArrayList<>();
    public ArrayList<MessageModel> list_chat = new ArrayList<>();
    public ArrayList<UserModel> list_users = new ArrayList<>();
    int[] list_mafias = new int[] {0, 0, 0, 0, 0, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9};
    int[] list_peaceful = new int[] {0, 0, 0, 0, 0, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9, 10, 10, 11, 11};

    public int StopTimer = 0;
    int messages_can_write = 10;
    public String journalist_check = null;
    public boolean has_paused = false;

    public int mafia_max = 0, mafia_now = 0, peaceful_max = 0, peaceful_now = 0;

    int num = -1;
    int answer_id = -1;

    ImageView IV_screenshot;

    ListView LV_chat;
    EditText ET_message;
    Button btnSend;
    RelativeLayout RL_send;
    TextView TV_answerMes;

    PlayersAdapter playersAdapter;

    View view_report;
    ImageView IV_screen;

    ////////////
    CardView CV_x2;
    CardView CV_x3;
    TextView TV_moneyGameOver;
    TextView TV_expGameOver;
    ////////////

    public JSONObject json;

    String base64_screenshot = "", report_nick = "", report_id = "";

    public MessageAdapter messageAdapter;

    public int FirstVisibleItem = 0, VisibleItemsCount = 0,TotalItemsCount = 0;

    public static final String APP_PREFERENCES = "user";
    public static final String APP_PREFERENCES_LAST_ROLE = "role";

    private SharedPreferences mSettings;

    OnUserSelectedListener mCallback;

    // Container Activity must implement this interface
    public interface OnUserSelectedListener {
        void onUserSelected(String nick);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnUserSelectedListener) activity;
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

        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(getActivity(), "ca-app-pub-9325171650796125/6129708436",
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        Log.d("kkk", "Error " + loadAdError.getMessage());
                        mRewardedAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        mRewardedAd = rewardedAd;
                        Log.d("kkk", "Ad was loaded.");
                        mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when ad is shown.
                                Log.d("kkk", "Ad was shown.");
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when ad fails to show.
                                Log.d("kkk", "Ad failed to show.");
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when ad is dismissed.
                                // Set the ad reference to null so you don't show the ad a second time.
                                Log.d("kkk", "Ad was dismissed.");
                                //mRewardedAd = null;
                            }
                        });
                    }
                });

        mSettings = getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        gridView_users = view.findViewById(R.id.fragmentGame_listUsers);
        room_name = view.findViewById(R.id.fragmentGame_TV_roomName);
        TV_mafia_count = view.findViewById(R.id.fragmentGame_TV_mafiaCount);
        TV_peaceful_count = view.findViewById(R.id.fragmentGame_TV_peacefulCount);
        TV_playersCount = view.findViewById(R.id.fragmentGame_TV_playersCount);
        TV_playersMinMaxInfo = view.findViewById(R.id.fragmentGame_TV_playersMinMaxInfo);
        btn_back = view.findViewById(R.id.fragmentGamesList_RL_back);

        timer = view.findViewById(R.id.fragmentGame_TV_timer);
        dayTime = view.findViewById(R.id.fragmentGame_TV_dayTime);

        Constrain = view.findViewById(R.id.fragmentGame_CL);

        IV_influence_doctor = view.findViewById(R.id.fragmentGame_ic_doctor);
        IV_influence_lover = view.findViewById(R.id.fragmentGame_ic_lover);
        IV_influence_sheriff = view.findViewById(R.id.fragmentGame_ic_sheriff);
        IV_influence_bodyguard = view.findViewById(R.id.fragmentGame_ic_bodyguard);
        IV_influence_poisoner = view.findViewById(R.id.fragmentGame_ic_poisoner);

        IV_screenshot = view_report.findViewById(R.id.dialogReport_IV_screenshot);
        Menu = view.findViewById(R.id.fragmentMenu_IV_menu);

        Menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup_menu = new PopupMenu(getActivity(), Menu);
                popup_menu.inflate(R.menu.main_menu);
                popup_menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
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
                                    Log.d("kkk", "Socket_???????????????? leave_room - " + json2.toString());
                                    socket.emit("leave_room", json2);
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
                                }
                                else
                                {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    View viewError = getLayoutInflater().inflate(R.layout.dialog_error, null);
                                    builder.setView(viewError);
                                    AlertDialog alert;
                                    TextView TV_error = viewError.findViewById(R.id.dialogError_TV_errorText);
                                    TV_error.setText("???????????? ???????????????? ???? ?????????????????? ???????????? ???? ???????????? ????????!");
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
                                Log.d("kkk", "Socket_???????????????? leave_room - " + json2.toString());
                                socket.emit("leave_room", json2);
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
                            if (!player.is_observer) {
                                final JSONObject json2 = new JSONObject();
                                try {
                                    json2.put("nick", MainActivity.NickName);
                                    json2.put("session_id", MainActivity.Session_id);
                                    json2.put("room", player.getRoom_num());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Log.d("kkk", "Socket_???????????????? leave_room - " + json2.toString());
                                socket.emit("leave_room", json2);
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
                                Log.d("kkk", "Socket_???????????????? leave_room_observer - " + json2.toString());
                                socket.emit("leave_room_observer", json2);
                            }
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
                        }
                        return true;
                    }
                });
                popup_menu.show();
            }
        });

        playersAdapter = new PlayersAdapter(list_users, getContext());
        gridView_users.setAdapter(playersAdapter);

        player = new Player(MainActivity.NickName, MainActivity.Session_id, MainActivity.Game_id, MainActivity.Role);

        LV_chat = view.findViewById(R.id.fragmentGame_LV_chat);
        ET_message = view.findViewById(R.id.fragmentGame_ET);
        RL_send = view.findViewById(R.id.fragmentGame_RL_send);
        btnSend = view.findViewById(R.id.fragmentGame_btn_send);
        TV_answerMes = view.findViewById(R.id.fragmentGame_TV_answerMes);

        room_name.setText(MainActivity.RoomName);
        TV_playersMinMaxInfo.setText(MainActivity.PlayersMinMaxInfo);

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
        socket.off("roles_counter");
        socket.off("success_get_in_room_observer");
        socket.off("get_increased_game_award");
        socket.off("re_voting");

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
        socket.on("roles_counter", onRolesCounter);
        socket.on("success_get_in_room_observer", onSuccessGetInRoomObserver);
        socket.on("get_increased_game_award", onGetIncreasedGameAward);

        json = new JSONObject();
        try {
            json.put("nick", player.getNick());
            json.put("session_id", player.getSession_id());
            json.put("room", player.getRoom_num());
            json.put("password", MainActivity.Password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("get_in_room", json);
        Log.d("kkk", "Socket_???????????????? - get_in_room from main "+ json.toString());

        room_name.setOnClickListener(v -> {
            if (player.getTime() != Time.LOBBY)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                View viewDang = getLayoutInflater().inflate(R.layout.dialog_roles_in_room, null);
                builder.setView(viewDang);
                ListView LV_role = viewDang.findViewById(R.id.dialogRolesInRoom_LV);
                RoleInRoomAdapter rolesAdapter = new RoleInRoomAdapter(list_roles, getContext());
                LV_role.setAdapter(rolesAdapter);
                rolesAdapter.notifyDataSetChanged();

                AlertDialog alert = builder.create();
                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alert.show();
            }
        });

        gridView_users.setOnItemClickListener((parent, view1, position, id) -> {
            String nick = list_users.get(position).getNick();
            if (nick.equals(player.getNick()) && player.getTime() != Time.LOBBY && !player.Can_click())
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                View viewNewRole = getLayoutInflater().inflate(R.layout.dialog_new_role, null);
                builder.setView(viewNewRole);

                TextView TV_role = viewNewRole.findViewById(R.id.dialogNewRole_TV_mainText);
                TextView TV_roleDescription = viewNewRole.findViewById(R.id.dialogNewRole_TV_roleDescription);
                ImageView IV_role = viewNewRole.findViewById(R.id.dialogNewRole_IV_role);

                switch (player.getRole())
                {
                    case NONE:
                        TV_role.setText("?? ?????? ?????? ????????");
                        TV_roleDescription.setText("????????????????, ???? ??????-???? ?????????? ???? ??????");
                        IV_role.setImageResource(R.drawable.journalist_round);
                        IV_role.setImageResource(R.drawable.ic_error);
                        break;
                    case CITIZEN:
                        TV_role.setText("???????? ???????? - ???????????? ????????????");
                        TV_roleDescription.setText("???????????? ???????????? - ???? ?????????? ?? ????????????, ?????? ???????????? ???? ??????????. ?????? ???????????? ????????????????, ?????? ???? ?????????????? ?????????? ???? ?????????????? ??????????, ?? ?????? - ??????, ?? ???????????????????? ???????? ??????????????, ???????? ?????? ???? ???????????????????? ???????? ??????????. ???????????? ?????? ???? ?????????????????? ?????? ???????????? ???????????? ?????????? ???????????????????? ???? ????????, ???????? ?????? ?????????????? ????????????.");
                        IV_role.setImageResource(R.drawable.citizen_round);
                        break;
                    case MAFIA:
                        TV_role.setText("???????? ???????? - ??????????");
                        TV_roleDescription.setText("?????????? - ???????????????????????? ???????????????? ????????. ?????????? ???????????????? ?????????????? ?????????????????????? ???? ?????????? ????????????, ?? ???? ???????? - ???????????????????? ?????? ???????????? ?????????????????? ????????????. ???????????? ???????? ?????????? ?????????? ???????? ?????????????????? ??????????????. ?????????? ?????????? ?????????? ?????????????????? ?? ?????????????????? ???????? ???????? ?????????? ?? ???????????????? ???????? ?????????? ????????????. ???????????????????????? ???? ???????? ??????????.");
                        IV_role.setImageResource(R.drawable.mafia_round);
                        break;
                    case SHERIFF:
                        TV_role.setText("???????? ???????? - ??????????");
                        TV_roleDescription.setText("?????????? - ???????????? ???? ?????????????? ???????????? ??????????????. ???????????? ???????????? - ?????????????????? ???????????? ?????????????? ???????????? ???????????? ??????????. ?????????? ?????????? ?????????? ?????????????????? ???????????? ???????????? ?? ???????????? ?????? ????????. ???????????????????????? ???? ???????? ??????????.");
                        IV_role.setImageResource(R.drawable.sheriff_round);
                        break;
                    case DOCTOR:
                        TV_role.setText("???????? ???????? - ????????????");
                        TV_roleDescription.setText("???????????? - ???????????? ???? ?????????????? ????????????. ???????????? ?????????? ???????????????? ???????????? ???????????? ???? ???????????? ??????????, ?????? ?????????? ???????????????????? ?????????? ???????????? ?????????????? ???? ????????????. ?????????? ???????????? ???????????? ???????????????? ???? ???????????? ????????????, ?? ???????????????? ???????????? ????????????????????.");
                        IV_role.setImageResource(R.drawable.doctor_round);
                        break;
                    case LOVER:
                        TV_role.setText("???????? ???????? - ??????????????????");
                        TV_roleDescription.setText("?????????????????? - ???? ?????????????? ???????????? ??????????????. ?????????? ???? ???????????????? ?????????????????????????? ???? ??????????????, ???????? ?????????? ??????????, ?????? ?????????????????? ?? ????????????????????, ?????????? ???????????? ????????????. ?????????? ?????????????????? ?????????? ???????????? ?????????? ???????????????????????? ???????? ???????? ???????????? ???????????? ????????????, ?????? ?????????? ?????????? ?????? ???????????????????????? ???????? ?? ?????????????????????? ???????????????????? ????????. ");
                        IV_role.setImageResource(R.drawable.lover_round);
                        break;
                    case MAFIA_DON:
                        TV_role.setText("???????? ???????? - ?????? ??????????");
                        TV_roleDescription.setText("?????? ?????????? - ?????????? ?????????????????? ??????????????????????. ?????? ???????????????? ???????? ???? ??????????????????????????, ?????? ?? ?????????????? ??????????, ???? ?????? ?????????? ???? ???????????? ???????????? ???????????? ?????????????????? ???? ??????.");
                        IV_role.setImageResource(R.drawable.mafia_don_round);
                        break;
                    case MANIAC:
                        TV_role.setText("???????? ???????? - ????????????");
                        TV_roleDescription.setText("???????????? - ???????????? ???? ????????????. ???????????? ???????????????? ?? ?????????????? ????????????, ???????????? ?????? ?? ???????? ???????????????? ???????????? ?????????????????? - ??????????. ???????????? ???? ????????????, ?????????? ?????? ???????? ???????????? ??????-???? ??????, ?????????????? ???? ?????????????????? ???????????? ???????? ?????????? ????????-???? ???? ?????????? ??????????.");
                        IV_role.setImageResource(R.drawable.maniac_round);
                        break;
                    case TERRORIST:
                        TV_role.setText("???????? ???????? - ??????????????????");
                        TV_roleDescription.setText("?????????????????? - ???????????? ???? ?????????????? ??????????. ???? ???? ??????????, ?????? ???????? ?????????????????? ??????????????????????, ???????????? ?????????? ??????????, ?????? ??????????????????, ?? ???? ?????????? ?????? ?????????? ??????????. ???????????? ???????????????????? - ???? ?????????? ???????????????? ?????????????????????? ?????????????? ?????????????????? ???????? ?????????? ???????????? ?? ???????????? ?????????????? ????????????, ?????????????????????? ?????????????????????????? ??????????????????????????, ????????????????, ???????????? ?????? ??????????????????.");
                        IV_role.setImageResource(R.drawable.terrorist_round);
                        break;
                    case BODYGUARD:
                        TV_role.setText("???????? ???????? - ??????????????????????????");
                        TV_roleDescription.setText("?????????????????????????? - ???????????? ???? ?????????????? ????????????. ?????????????????????????? ?????????? ???????????? ?? ???????????? ???????????? ???????? ?? ???????????????? ?????? ???? ???????????? ???? ?????????? ?????????????????? ????????. ???????? ?????????????????? ?????????????????? ???????????????? ????????????, ?????????????? ?????? ?????????????? ??????????????????????????, ???? ?????????? ???????????? ??????????????????, ???? ?????????? ?????? ?????????????????? ?????? ???????????? ??????????????????????????. ???????????? ?????????????????????????? ???? ???????????? ???? ?????????????????? ?????? ???????????? ???? ?????????????????????? ???????????????? ??????????????????????. ");
                        IV_role.setImageResource(R.drawable.bodyguard_round);
                        break;
                    case DOCTOR_OF_EASY_VIRTUE:
                        TV_role.setText("???????? ???????? - ???????????? ?????????????? ??????????????????");
                        TV_roleDescription.setText("???????????? ?????????????? ?????????????????? - ???? ?????????????? ????????????. ?????????? ???????? ??????????????????, ???????????? ?????? ?????????????????? - ????????????????????, ?????????????? ?????????? ???????????? ?????????????? ?????????????????? ?????????? ????????????, ?????????????????????????????? ?????? ?????????????????????????? ?????????????????? ?????? ??????????????.\n");
                        IV_role.setImageResource(R.drawable.doctor_of_easy_virtue_round);
                        break;
                    case POISONER:
                        TV_role.setText("???????? ???????? - ????????????????????");
                        TV_roleDescription.setText("???????????????????? - ???????????? ???? ??????????. ???????????????????? ???? ??????????, ?????? ??????????, ?? ?????????? ???? ??????????, ?????? ????????????????????, ?????????????? ?????? ???????????????????????? ???????? ???????????????????? ?????????? ???????? ???????? ???????????? ??????????. ???????????????????? ?????????? ?????????? ???????????????? ???????????? ????????????, ?????????????? ?????????? ???? ?????????????????? ???????? ?????????? ??????????????????????, ???????? ?? ???????? ???? ???????????? ???????????? ?????? ??????????????????????????. ?????????????????????? ?????????????? ?????????????????? ??????????????????, ?????? ?????????? ???????????????? ???????????? ???????? ?????????????????? ?????????????????? ????????. ");
                        IV_role.setImageResource(R.drawable.poisoner_round);
                        break;
                    case JOURNALIST:
                        TV_role.setText("???????? ???????? - ?????????? ??????");
                        TV_roleDescription.setText("?????????? ?????? - ???????????????? ?????????????????????????? ???? ?????????????? ???????????? ??????????????. ?????????? ???? ?????????? ?????????????????? ?????????? ???????? ?????????????? ?? ????????????????, ???????????? ???? ?????? ?? ?????????? ?????????????? ?????? ??????.\n");
                        IV_role.setImageResource(R.drawable.journalist_round);
                        break;
                }
                AlertDialog alert = builder.create();
                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alert.show();
            }
            else {
                if (player.Can_click() && player.getStatus().equals("alive") && list_users.get(position).getAlive() &&
                        list_users.get(position).getAnimation_type() != Role.NONE) {
                    Log.e("kkk", String.valueOf(list_users.get(position).getAnimation_type()));
                    switch (player.getTime()) {
                        case LOBBY:
                            mCallback.onUserSelected(nick);
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
                                    if (nick.equals(player.getNick()))
                                    {
                                        player.healed_yourself = true;
                                    }
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
                                        Log.d("kkk", "Socket_???????????????? - role_action" + json.toString());

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
                                mCallback.onUserSelected(nick);
                            }
                            break;
                        case VOTING:
                        case REVOTING:
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
                    if (!player.is_observer) {
                        mCallback.onUserSelected(nick);
                    }
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
                        Log.d("kkk", "Socket_????????????????_skip_day - " + json2.toString());
                        socket.emit("skip_day", json2);
                        dayTime.setBackgroundResource(R.drawable.grey_button);
                    }
                    break;
                case VOTING:
                    break;
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.getTime() == Time.LOBBY) {
                    if (!timer.getText().equals("\u221e")) {
                        if (Integer.parseInt(String.valueOf(timer.getText())) > 5) {
                            askToLeave();
                        }
                        else
                        {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            View viewError = getLayoutInflater().inflate(R.layout.dialog_error, null);
                            builder.setView(viewError);
                            AlertDialog alert;
                            TextView TV_error = viewError.findViewById(R.id.dialogError_TV_errorText);
                            TV_error.setText("???????????? ???????????????? ???? ?????????????????? ???????????? ???? ???????????? ????????!");
                            alert = builder.create();
                            alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            alert.show();
                        }
                    }
                    else
                    {
                        askToLeave();
                    }
                }
                else
                {
                    if (!player.is_observer) {
                        askToLeave();
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
                        Log.d("kkk", "Socket_???????????????? leave_room_observer - " + json2.toString());
                        socket.emit("leave_room_observer", json2);
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
                    }
                }
            }
        });

        messageAdapter = new MessageAdapter(list_chat, getContext());
        LV_chat.setAdapter(messageAdapter);

        RL_send.setOnClickListener(v -> {
            String input = String.valueOf(ET_message.getText());
            if (input.length() > 300) {
                input = input.substring(0, 301);
            }
            /*
            int flag = 0;
            for (int i = 0; i < input.length(); i ++)
            {
                if (Character.isLetter(input.charAt(i))) {
                    for (int j = 0; j < f.length; j++) {
                        if (input.charAt(i) == f[j]) {
                            flag = 1;
                        }
                    }

                    if (flag != 1) {
                        input = input.replace(String.valueOf(input.charAt(i)), "");
                    }
                    flag = 0;
                }
            }
             */
            if (player.getStatus().equals("alive") || player.getTime() == Time.LOBBY) {
                if (player.Can_write()) {
                    if (!input.equals("") && !input.equals("/n")) {
                        if (player.getTime() == Time.DAY) {
                            if (!poisoner) {
                                if (messages_can_write > 0) {
                                    messages_can_write--;

                                    final JSONObject json2 = new JSONObject();
                                    try {
                                        json2.put("nick", player.getNick());
                                        json2.put("session_id", player.getSession_id());
                                        json2.put("room", player.getRoom_num());
                                        json2.put("message", input);
                                        json2.put("link", answer_id);
                                        answer_id = -1;
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    Log.d("kkk", "Socket_???????????????? user_message - " + json2.toString());
                                    socket.emit("user_message", json2);
                                    answer_id = -1;
                                    TV_answerMes.setVisibility(View.GONE);
                                    ET_message.setText("");

                                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                                    //Find the currently focused view, so we can grab the correct window token from it.
                                    View view2 = getActivity().getCurrentFocus();
                                    //If no view currently has focus, create a new one, just so we can grab a window token from it
                                    if (view2 == null) {
                                        view2 = new View(getActivity());
                                    }
                                    imm.hideSoftInputFromWindow(view2.getWindowToken(), 0);
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                    builder.setTitle("???? ???? ???????????? ??????????!")
                                            .setMessage("???????????? ???????????????????? ???????????? 10 ?????????????????? ?? ????????!")
                                            .setIcon(R.drawable.ic_error)
                                            .setCancelable(false)
                                            .setNegativeButton("????",
                                                    (dialog, id) -> dialog.cancel());
                                    AlertDialog alert = builder.create();
                                    alert.show();
                                }

                            } else {
                                if (messages_can_write == 10) {
                                    messages_can_write--;

                                    final JSONObject json2 = new JSONObject();
                                    try {
                                        json2.put("nick", player.getNick());
                                        json2.put("session_id", player.getSession_id());
                                        json2.put("room", player.getRoom_num());
                                        json2.put("message", input);
                                        json2.put("link", answer_id);
                                        answer_id = -1;
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    Log.d("kkk", "Socket_???????????????? user_message - " + json2.toString());
                                    socket.emit("user_message", json2);
                                    answer_id = -1;
                                    TV_answerMes.setVisibility(View.GONE);
                                    ET_message.setText("");

                                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                                    //Find the currently focused view, so we can grab the correct window token from it.
                                    View view2 = getActivity().getCurrentFocus();
                                    //If no view currently has focus, create a new one, just so we can grab a window token from it
                                    if (view2 == null) {
                                        view2 = new View(getActivity());
                                    }
                                    imm.hideSoftInputFromWindow(view2.getWindowToken(), 0);
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                    builder.setTitle("?????? ????????????????!")
                                            .setMessage("???? ???? ???????????? ???????????? ???????????? 1 ?????????????????? ?? ????????!")
                                            .setIcon(R.drawable.ic_error)
                                            .setCancelable(false)
                                            .setNegativeButton("????",
                                                    (dialog, id) -> dialog.cancel());
                                    AlertDialog alert = builder.create();
                                    alert.show();
                                }
                            }

                        } else {
                            final JSONObject json2 = new JSONObject();
                            try {
                                json2.put("nick", player.getNick());
                                json2.put("session_id", player.getSession_id());
                                json2.put("room", player.getRoom_num());
                                json2.put("message", input);
                                json2.put("link", answer_id);
                                answer_id = -1;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Log.d("kkk", "Socket_???????????????? user_message - " + json2.toString());
                            socket.emit("user_message", json2);
                            answer_id = -1;
                            TV_answerMes.setVisibility(View.GONE);
                            ET_message.setText("");

                            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                            //Find the currently focused view, so we can grab the correct window token from it.
                            View view2 = getActivity().getCurrentFocus();
                            //If no view currently has focus, create a new one, just so we can grab a window token from it
                            if (view2 == null) {
                                view2 = new View(getActivity());
                            }
                            imm.hideSoftInputFromWindow(view2.getWindowToken(), 0);
                        }
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("????????????!")
                                .setMessage("???????????? ???????????????????? ???????????? ??????????????????!")
                                .setIcon(R.drawable.ic_error)
                                .setCancelable(false)
                                .setNegativeButton("????",
                                        (dialog, id) -> dialog.cancel());
                        AlertDialog alert = builder.create();
                        alert.show();

                    }
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("???? ???? ???????????? ???????????? ???????????????????? ??????????????????!")
                            .setMessage("")
                            .setIcon(R.drawable.ic_error)
                            .setCancelable(false)
                            .setNegativeButton("????",
                                    (dialog, id) -> dialog.cancel());
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            } else {
                final JSONObject json2 = new JSONObject();
                try {
                    json2.put("nick", player.getNick());
                    json2.put("session_id", player.getSession_id());
                    json2.put("room", player.getRoom_num());
                    json2.put("message", input);
                    json2.put("link", answer_id);
                    answer_id = -1;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("kkk", "Socket_???????????????? user_message - " + json2.toString());
                socket.emit("user_message", json2);
                answer_id = -1;
                TV_answerMes.setVisibility(View.GONE);
                ET_message.setText("");
                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            }
        });

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

        LV_chat.setOnItemClickListener((parent, view12, position, id) -> {
            if(list_chat.get(position).mesType.equals("UsersMes") || list_chat.get(position).mesType.equals("AnswerMes"))
            {
                answer_id = list_chat.get(position).num;
                TV_answerMes.setText(list_chat.get(position).nickName + ": " + list_chat.get(position).message);
                TV_answerMes.setVisibility(View.VISIBLE);
            }
        });

        TV_answerMes.setOnClickListener(v -> {
            answer_id = -1;
            TV_answerMes.setVisibility(View.GONE);
        });


        return view;
    }

    public Bitmap fromBase64(String image) {
        // ???????????????????? ???????????? Base64 ?? ???????????? ????????????
        byte[] decodedString = Base64.decode(image, Base64.DEFAULT);

        // ???????????????????? ???????????? ???????????? ?? ??????????????????????
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        // ???????????????? ?????????????????????? ?? ImageView
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
        if (has_paused)
        {
            if (!player.is_observer) {
                if (player.getTime() == Time.LOBBY) {
                    for (int i = 0; i < list_users.size(); i++) {
                        if (list_users.get(i).getNick().equals(player.getNick())) {
                            list_users.remove(i);
                        }
                    }
                    json = new JSONObject();
                    try {
                        json.put("nick", player.getNick());
                        json.put("session_id", player.getSession_id());
                        json.put("room", player.getRoom_num());
                        json.put("password", MainActivity.Password);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    socket.emit("get_in_room", json);
                    Log.d("kkk", "Socket_???????????????? - get_in_room onResume" + json.toString());
                    has_paused = false;
                } else {
                    json = new JSONObject();
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
                    Log.d("kkk", "connect_to_room - " + json);
                }
            }
            else
            {
                JSONObject json = new JSONObject();
                try {
                    json.put("nick", player.getNick());
                    json.put("session_id", player.getSession_id());
                    json.put("room", player.getRoom_num());
                    json.put("password", MainActivity.Password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                socket.emit("get_in_room_observer", json);
                Log.d("kkk", "Socket_???????????????? - get_in_room_observer - "+ json.toString());
            }
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
                    askToLeave();
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    View viewError = getLayoutInflater().inflate(R.layout.dialog_error, null);
                    builder.setView(viewError);
                    AlertDialog alert;
                    TextView TV_error = viewError.findViewById(R.id.dialogError_TV_errorText);
                    TV_error.setText("???????????? ???????????????? ???? ?????????????????? ???????????? ???? ???????????? ????????!");
                    alert = builder.create();
                    alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alert.show();
                }
            }
            else
            {
                askToLeave();
            }
        }
        else
        {
            if (!player.is_observer) {
                askToLeave();
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
                Log.d("kkk", "Socket_???????????????? leave_room_observer - " + json2.toString());
                socket.emit("leave_room_observer", json2);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
            }
        }
    }

    /*******************************
     *                             *
     *       SOCKETS start         *
     *                             *
     *******************************/

    private final Emitter.Listener onGetInRoom = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Log.d("kkk", "???????????? - get_in_room: " + args[0]);
                    JSONObject data = (JSONObject) args[0];
                    String nick;
                    String time;
                    String avatar;
                    String status;
                    String user_color;
                    int test_num;
                    int room_id;
                    try {
                        room_id = data.getInt("room");
                        if (room_id == player.getRoom_num()) {
                            test_num = data.getInt("num");
                            status = data.getString("user_status");
                            time = data.getString("time");
                            time = getDate(time);
                            nick = data.getString("nick");
                            avatar = data.getString("avatar");
                            user_color = data.getString("user_color");
                            MessageModel messageModel = new MessageModel(test_num, nick + " ??????????(-??) ?? ??????", time, nick, "ConnectMes", fromBase64(avatar));
                            Log.d("kkk", "GameChatFragment get_in_room, num = " + num + ", data = " + data);
                            if (test_num != num) {
                                for (int i = 0; i < list_chat.size(); i++) {
                                    if (list_chat.get(i).message.equals(nick + " ??????????(-??) ???? ????????")) {
                                        list_chat.remove(i);
                                        break;
                                    }
                                }
                                if (test_num > num) {
                                    num = test_num;
                                    list_chat.add(messageModel);
                                } else {
                                    for (int i = 0; i < list_chat.size(); i++) {
                                        if (test_num < list_chat.get(i).num) {
                                            list_chat.add(i, messageModel);
                                            break;
                                        }
                                    }
                                }
                                for (int i = 0; i < list_chat.size(); i++) {
                                    if (list_chat.get(i).nickName.equals(nick)) {
                                        list_chat.get(i).avatar = fromBase64(avatar);
                                        list_chat.get(i).status_text = status;
                                        list_chat.get(i).user_color = user_color;
                                    }
                                }
                                list_users.add(new UserModel(nick, Role.NONE, avatar, status, user_color));

                                messageAdapter.notifyDataSetChanged();
                                playersAdapter.notifyDataSetChanged();
                                TV_playersCount.setText("????????????: " + list_users.size());
                                if (TotalItemsCount < FirstVisibleItem + VisibleItemsCount + 3) {
                                    LV_chat.setSelection(messageAdapter.getCount() - 1);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private final Emitter.Listener onLeaveUser = new Emitter.Listener() {
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
                    int room_id = 0;
                    try {
                        room_id = data.getInt("room");
                        test_num = data.getInt("num");
                        nick = data.getString("nick");
                        time = data.getString("time");
                        time = getDate(time);
                        Log.d("kkk", "leave_user - " + " ?????????? listchat = " + list_chat.size() + " /  testnum = " + test_num + " / num = " + num + "/ " + data);
                    } catch (JSONException e) {
                        return;
                    }

                    MessageModel messageModel = new MessageModel(test_num, nick + " ??????????(-??) ???? ????????", time, nick, "DisconnectMes", fromBase64(""));

                    if (test_num != num && room_id == player.getRoom_num()) {
                        for (int i = 0; i < list_chat.size(); i++) {
                            if (list_chat.get(i).message.equals(nick + " ??????????(-??) ?? ??????")) {
                                list_chat.remove(i);
                                break;
                            }
                        }
                        if (test_num > num) {
                            num = test_num;
                            list_chat.add(messageModel);
                        } else {
                            for (int i = 0; i < list_chat.size(); i++) {
                                if (test_num < list_chat.get(i).num) {
                                    list_chat.add(i, messageModel);
                                    break;
                                }
                            }
                        }
                        for (int i = 0; i < list_users.size(); i++)
                        {
                            if (list_users.get(i).getNick().equals(nick))
                            {
                                list_users.remove(i);
                                break;
                            }
                        }

                        messageAdapter.notifyDataSetChanged();
                        playersAdapter.notifyDataSetChanged();
                        TV_playersCount.setText("????????????: " + list_users.size());

                        if (TotalItemsCount < FirstVisibleItem + VisibleItemsCount + 3) {
                            LV_chat.setSelection(messageAdapter.getCount() - 1);
                        }
                    }
                }
            });
        }
    };

    private final Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String nick;
                    String message;
                    String time;
                    String status;
                    String status_text = "";
                    String user_color = "";
                    String nick_from_iterator;
                    int test_num;
                    int room_id;
                    int link;
                    try {
                        //status_text = data.getString("role");
                        nick = data.getString("nick");
                        status = data.getString("status");
                        room_id = data.getInt("room");
                        if (room_id == player.getRoom_num()) {
                            String avatar = "";
                            for (int i = 0; i < list_users.size(); i++) {
                                if (nick.equals(list_users.get(i).getNick())) {
                                    avatar = list_users.get(i).getAvatar();
                                    status_text = list_users.get(i).status;
                                    user_color = list_users.get(i).user_color;
                                    break;
                                }
                            }
                            test_num = data.getInt("num");
                            Log.d("kkk", "GameFragment new_message, num = " +  num + ", " + data);
                            boolean good = true;
                            for (int i = 0; i < list_chat.size(); i++) {
                                if (list_chat.get(i).num == test_num) {
                                    good = false;
                                    break;
                                }
                            }
                            if (test_num != num && good) {
                                if (test_num > num) {
                                    num = test_num;
                                    time = data.getString("time");
                                    time = getDate(time);
                                    message = data.getString("message");
                                    link = data.getInt("link");
                                    MessageModel messageModel;
                                    if (link == -1) {
                                        messageModel = new MessageModel(test_num, message, time, nick, "UsersMes", status, status_text, fromBase64(avatar), user_color);
                                    } else {
                                        messageModel = new MessageModel(test_num, message, time, nick, "AnswerMes", status, link, status_text, fromBase64(avatar), user_color);
                                    }
                                    list_chat.add(messageModel);
                                } else {
                                    time = data.getString("time");
                                    time = getDate(time);
                                    message = data.getString("message");
                                    status = data.getString("status");
                                    link = data.getInt("link");
                                    MessageModel messageModel;
                                    if (link == -1) {
                                        messageModel = new MessageModel(test_num, message, time, nick, "UsersMes", status, status_text, fromBase64(avatar), user_color);
                                    } else {
                                        messageModel = new MessageModel(test_num, message, time, nick, "AnswerMes", status, link, status_text, fromBase64(avatar), user_color);
                                    }
                                    for (int i = 0; i < list_chat.size(); i++) {
                                        if (test_num < list_chat.get(i).num) {
                                            list_chat.add(i, messageModel);
                                            break;
                                        }
                                    }
                                }
                                messageAdapter.notifyDataSetChanged();
                                if (TotalItemsCount < FirstVisibleItem + VisibleItemsCount + 3) {
                                    LV_chat.setSelection(messageAdapter.getCount() - 1);
                                }
                            } else {
                                Log.d("kkk", "?????????????????? ????????????????!");
                            }
                        }
                    } catch (JSONException e) {
                        Log.d("kkk", "JSONException");
                        return;
                    }
                }
            });
        }
    };

    private final Emitter.Listener onConnect = new Emitter.Listener() {
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
                        json.put("password", MainActivity.Password);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    socket.emit("get_in_room", json);
                    Log.d("kkk", "Socket_???????????????? - get_in_room"+ json.toString());
                    Log.d("kkk", "CONNECT");
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
                    Log.d("kkk", "DISCONNECT");
                }
            });
        }
    };

    //?????????????????? ????????????
    private final Emitter.Listener onTimer = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String time;
                    int room_id;
                    try {
                        room_id = data.getInt("room");
                        if (room_id == player.getRoom_num()) {
                            time = data.getString("timer");
                            if (!time.equals("stop")) {
                                if (StopTimer == 1) {
                                    timer.setText("\u221e");
                                    StopTimer = 0;
                                } else {
                                    timer.setText(time);
                                }
                            } else {
                                StopTimer = 1;
                                timer.setText("\u221e");
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    //?????????????????? ?????????? ??????
    private final Emitter.Listener onTime = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(() -> {
                JSONObject data = (JSONObject) args[0];
                String time;
                int room_id;
                try {
                    room_id = data.getInt("room");
                    if (room_id == player.getRoom_num()) {
                        time = data.getString("time");
                        switch (time) {
                            case "lobby":
                                player.setTime(Time.LOBBY);
                                dayTime.setText("??????????");
                                dayTime.setBackgroundResource(R.drawable.green_button);
                                Constrain.setBackgroundResource(R.drawable.fon_day);
                                break;
                            case "night_love":
                                DeleteNumbersFromVoting();
                                player.setTime(Time.NIGHT_LOVE);
                                dayTime.setText("????????");
                                dayTime.setBackgroundResource(R.drawable.died_button);
                                Constrain.setBackgroundResource(R.drawable.fon_night);
                                break;
                            case "night_other":
                                player.setTime(Time.NIGHT_OTHER);
                                dayTime.setText("????????");
                                dayTime.setBackgroundResource(R.drawable.died_button);
                                Constrain.setBackgroundResource(R.drawable.fon_night);
                                break;
                            case "day":
                                messages_can_write = 10;
                                DeleteNumbersFromVoting();
                                player.setTime(Time.DAY);
                                if (player.getStatus().equals("alive") && !player.is_observer) {
                                    dayTime.setText("?????????????? ??????");
                                    dayTime.setBackgroundResource(R.drawable.green_button);
                                } else {
                                    dayTime.setText("????????");
                                    dayTime.setBackgroundResource(R.drawable.grey_button);
                                }
                                Constrain.setBackgroundResource(R.drawable.fon_day);
                                break;
                            case "voting":
                                player.setTime(Time.VOTING);
                                dayTime.setText("??????????????????????");
                                dayTime.setBackgroundResource(R.drawable.grey_button);
                                Constrain.setBackgroundResource(R.drawable.fon_day);
                                break;
                            case "re_voting":
                                DeleteNumbersFromVoting();
                                player.setTime(Time.REVOTING);
                                dayTime.setText("??????????????????????????????");
                                dayTime.setBackgroundResource(R.drawable.grey_button);
                                Constrain.setBackgroundResource(R.drawable.fon_day);
                                break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                StopAnimation();
                if (player.getStatus().equals("alive"))
                {
                    player.setCan_write(false);
                    switch (player.getTime())
                    {
                        case NIGHT_LOVE:
                            player.setVoted_at_night(false);
                            poisoner = false;
                            IV_influence_poisoner.clearAnimation();
                            IV_influence_poisoner.setVisibility(View.GONE);
                            IV_influence_lover.clearAnimation();
                            IV_influence_lover.setVisibility(View.GONE);
                            switch (player.getRole())
                            {
                                case LOVER:
                                case DOCTOR_OF_EASY_VIRTUE:
                                    StartAnimation(Role.LOVER);
                                    break;
                                case MAFIA:
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
                            IV_influence_doctor.clearAnimation();
                            IV_influence_doctor.setVisibility(View.GONE);
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

    //?????????????????? ????????
    private final Emitter.Listener onRole = new Emitter.Listener() {
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
                                TV_role.setText("?? ?????? ?????? ????????");
                                TV_roleDescription.setText("????????????????, ???? ??????-???? ?????????? ???? ??????");
                                IV_role.setImageResource(R.drawable.journalist_round);
                                IV_role.setImageResource(R.drawable.ic_error);
                                break;
                            case CITIZEN:
                                TV_role.setText("???????? ???????? - ???????????? ????????????");
                                TV_roleDescription.setText("???????????? ???????????? - ???? ?????????? ?? ????????????, ?????? ???????????? ???? ??????????. ?????? ???????????? ????????????????, ?????? ???? ?????????????? ?????????? ???? ?????????????? ??????????, ?? ?????? - ??????, ?? ???????????????????? ???????? ??????????????, ???????? ?????? ???? ???????????????????? ???????? ??????????. ???????????? ?????? ???? ?????????????????? ?????? ???????????? ???????????? ?????????? ???????????????????? ???? ????????, ???????? ?????? ?????????????? ????????????.");
                                IV_role.setImageResource(R.drawable.citizen_round);
                                break;
                            case MAFIA:
                                TV_role.setText("???????? ???????? - ??????????");
                                TV_roleDescription.setText("?????????? - ???????????????????????? ???????????????? ????????. ?????????? ???????????????? ?????????????? ?????????????????????? ???? ?????????? ????????????, ?? ???? ???????? - ???????????????????? ?????? ???????????? ?????????????????? ????????????. ???????????? ???????? ?????????? ?????????? ???????? ?????????????????? ??????????????. ?????????? ?????????? ?????????? ?????????????????? ?? ?????????????????? ???????? ???????? ?????????? ?? ???????????????? ???????? ?????????? ????????????. ???????????????????????? ???? ???????? ??????????.");
                                IV_role.setImageResource(R.drawable.mafia_round);
                                break;
                            case SHERIFF:
                                TV_role.setText("???????? ???????? - ??????????");
                                TV_roleDescription.setText("?????????? - ???????????? ???? ?????????????? ???????????? ??????????????. ???????????? ???????????? - ?????????????????? ???????????? ?????????????? ???????????? ???????????? ??????????. ?????????? ?????????? ?????????? ?????????????????? ???????????? ???????????? ?? ???????????? ?????? ????????. ???????????????????????? ???? ???????? ??????????.");
                                IV_role.setImageResource(R.drawable.sheriff_round);
                                break;
                            case DOCTOR:
                                TV_role.setText("???????? ???????? - ????????????");
                                TV_roleDescription.setText("???????????? - ???????????? ???? ?????????????? ????????????. ???????????? ?????????? ???????????????? ???????????? ???????????? ???? ???????????? ??????????, ?????? ?????????? ???????????????????? ?????????? ???????????? ?????????????? ???? ????????????. ?????????? ???????????? ???????????? ???????????????? ???? ???????????? ????????????, ?? ???????????????? ???????????? ????????????????????.");
                                IV_role.setImageResource(R.drawable.doctor_round);
                                break;
                            case LOVER:
                                TV_role.setText("???????? ???????? - ??????????????????");
                                TV_roleDescription.setText("?????????????????? - ???? ?????????????? ???????????? ??????????????. ?????????? ???? ???????????????? ?????????????????????????? ???? ??????????????, ???????? ?????????? ??????????, ?????? ?????????????????? ?? ????????????????????, ?????????? ???????????? ????????????. ?????????? ?????????????????? ?????????? ???????????? ?????????? ???????????????????????? ???????? ???????? ???????????? ???????????? ????????????, ?????? ?????????? ?????????? ?????? ???????????????????????? ???????? ?? ?????????????????????? ???????????????????? ????????. ");
                                IV_role.setImageResource(R.drawable.lover_round);
                                break;
                            case MAFIA_DON:
                                TV_role.setText("???????? ???????? - ?????? ??????????");
                                TV_roleDescription.setText("?????? ?????????? - ?????????? ?????????????????? ??????????????????????. ?????? ???????????????? ???????? ???? ??????????????????????????, ?????? ?? ?????????????? ??????????, ???? ?????? ?????????? ???? ???????????? ???????????? ???????????? ?????????????????? ???? ??????.");
                                IV_role.setImageResource(R.drawable.mafia_don_round);
                                break;
                            case MANIAC:
                                TV_role.setText("???????? ???????? - ????????????");
                                TV_roleDescription.setText("???????????? - ???????????? ???? ????????????. ???????????? ???????????????? ?? ?????????????? ????????????, ???????????? ?????? ?? ???????? ???????????????? ???????????? ?????????????????? - ??????????. ???????????? ???? ????????????, ?????????? ?????? ???????? ???????????? ??????-???? ??????, ?????????????? ???? ?????????????????? ???????????? ???????? ?????????? ????????-???? ???? ?????????? ??????????.");
                                IV_role.setImageResource(R.drawable.maniac_round);
                                break;
                            case TERRORIST:
                                TV_role.setText("???????? ???????? - ??????????????????");
                                TV_roleDescription.setText("?????????????????? - ???????????? ???? ?????????????? ??????????. ???? ???? ??????????, ?????? ???????? ?????????????????? ??????????????????????, ???????????? ?????????? ??????????, ?????? ??????????????????, ?? ???? ?????????? ?????? ?????????? ??????????. ???????????? ???????????????????? - ???? ?????????? ???????????????? ?????????????????????? ?????????????? ?????????????????? ???????? ?????????? ???????????? ?? ???????????? ?????????????? ????????????, ?????????????????????? ?????????????????????????? ??????????????????????????, ????????????????, ???????????? ?????? ??????????????????.");
                                IV_role.setImageResource(R.drawable.terrorist_round);
                                break;
                            case BODYGUARD:
                                TV_role.setText("???????? ???????? - ??????????????????????????");
                                TV_roleDescription.setText("?????????????????????????? - ???????????? ???? ?????????????? ????????????. ?????????????????????????? ?????????? ???????????? ?? ???????????? ???????????? ???????? ?? ???????????????? ?????? ???? ???????????? ???? ?????????? ?????????????????? ????????. ???????? ?????????????????? ?????????????????? ???????????????? ????????????, ?????????????? ?????? ?????????????? ??????????????????????????, ???? ?????????? ???????????? ??????????????????, ???? ?????????? ?????? ?????????????????? ?????? ???????????? ??????????????????????????. ???????????? ?????????????????????????? ???? ???????????? ???? ?????????????????? ?????? ???????????? ???? ?????????????????????? ???????????????? ??????????????????????. ");
                                IV_role.setImageResource(R.drawable.bodyguard_round);
                                break;
                            case DOCTOR_OF_EASY_VIRTUE:
                                TV_role.setText("???????? ???????? - ???????????? ?????????????? ??????????????????");
                                TV_roleDescription.setText("???????????? ?????????????? ?????????????????? - ???? ?????????????? ????????????. ?????????? ???????? ??????????????????, ???????????? ?????? ?????????????????? - ????????????????????, ?????????????? ?????????? ???????????? ?????????????? ?????????????????? ?????????? ????????????, ?????????????????????????????? ?????? ?????????????????????????? ?????????????????? ?????? ??????????????.\n");
                                IV_role.setImageResource(R.drawable.doctor_of_easy_virtue_round);
                                break;
                            case POISONER:
                                TV_role.setText("???????? ???????? - ????????????????????");
                                TV_roleDescription.setText("???????????????????? - ???????????? ???? ??????????. ???????????????????? ???? ??????????, ?????? ??????????, ?? ?????????? ???? ??????????, ?????? ????????????????????, ?????????????? ?????? ???????????????????????? ???????? ???????????????????? ?????????? ???????? ???????? ???????????? ??????????. ???????????????????? ?????????? ?????????? ???????????????? ???????????? ????????????, ?????????????? ?????????? ???? ?????????????????? ???????? ?????????? ??????????????????????, ???????? ?? ???????? ???? ???????????? ???????????? ?????? ??????????????????????????. ?????????????????????? ?????????????? ?????????????????? ??????????????????, ?????? ?????????? ???????????????? ???????????? ???????? ?????????????????? ?????????????????? ????????. ");
                                IV_role.setImageResource(R.drawable.poisoner_round);
                                break;
                            case JOURNALIST:
                                TV_role.setText("???????? ???????? - ?????????? ??????");
                                TV_roleDescription.setText("?????????? ?????? - ???????????????? ?????????????????????????? ???? ?????????????? ???????????? ??????????????. ?????????? ???? ?????????? ?????????????????? ?????????? ???????? ?????????????? ?? ????????????????, ???????????? ???? ?????? ?? ?????????? ?????????????? ?????? ??????.\n");
                                IV_role.setImageResource(R.drawable.journalist_round);
                                break;
                        }
                        Log.d("kkk", "Socket_?????????????? - role " + role);

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

    //?????????????????????? ?????? ?????????????? ??????????????, ?????????? ???????????????????? ????????
    private final Emitter.Listener onRestart = new Emitter.Listener() {
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

    //???????????????? ??????????-???? ????????
    private final Emitter.Listener onRoleAction = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (player.game_on) {
                        JSONObject data = (JSONObject) args[0];
                        String role;
                        try {
                            role = data.getString("role");
                            Log.d("kkk", "Socket_?????????????? - role_action " + args[0]);
                            animation = AnimationUtils.loadAnimation(getContext(), R.anim.bounce_center);
                            switch (role) {
                                case "doctor":
                                    IV_influence_doctor.setVisibility(View.VISIBLE);
                                    IV_influence_doctor.startAnimation(animation);
                                    break;
                                case "lover":
                                    StopAnimation();
                                    IV_influence_lover.setVisibility(View.VISIBLE);
                                    IV_influence_lover.startAnimation(animation);
                                    break;
                                case "sheriff":
                                    IV_influence_sheriff.setVisibility(View.VISIBLE);
                                    IV_influence_sheriff.startAnimation(animation);
                                    break;
                                case "bodyguard":
                                    IV_influence_bodyguard.setVisibility(View.VISIBLE);
                                    IV_influence_bodyguard.startAnimation(animation);
                                    break;
                                case "poisoner":
                                    poisoner = true;
                                    IV_influence_poisoner.setVisibility(View.VISIBLE);
                                    IV_influence_poisoner.startAnimation(animation);
                                    break;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    };

    private final Emitter.Listener onKnowRole = new Emitter.Listener() {
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
                        Log.d("kkk", "Socket_?????????????? - know_role " + args[0]);
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

    private final Emitter.Listener onSystemMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String message, time, status, mafia_nick, user_nick, voter, nick;
                    int test_num, money, exp, room_id;
                    boolean is_premium;
                    JSONObject data2;
                    try {
                        status = data.getString("status");
                        test_num = data.getInt("num");
                        time = data.getString("time");
                        time = getDate(time);
                        message = data.getString("message");
                        room_id = data.getInt("room");
                        MessageModel messageModel = new MessageModel(test_num, "???????????? ???????????? ??????????????????", time, "Server", "SystemMes");
                        Log.d("kkk", "system message - " + " ?????????? listchat = " + list_chat.size() + " /  testnum = " + test_num + " / num = " + num + " , status - " + status + "/" +  data);
                        if (test_num != num && room_id == player.getRoom_num()) {
                            switch (status)
                            {
                                case "game_over":
                                    if (data.has("money")) {
                                        money = data.getInt("money");
                                        exp = data.getInt("exp");
                                        is_premium = data.getBoolean("is_premium");
                                        player.game_on = false;
                                        DeleteNumbersFromVoting();

                                        AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
                                        View view_end_game = getLayoutInflater().inflate(R.layout.dialog_end_game, null);
                                        builder2.setView(view_end_game);

                                        AlertDialog alert2 = builder2.create();
                                        alert2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                                        TextView TV_message = view_end_game.findViewById(R.id.dialogEndGame_TV_title);
                                        TV_moneyGameOver = view_end_game.findViewById(R.id.dialogEndGame_TV_money);
                                        TV_expGameOver = view_end_game.findViewById(R.id.dialogEndGame_TV_exp);
                                        ShimmerTextView STV_premiumExp = view_end_game.findViewById(R.id.dialogEndGame_STV_premiumExp);
                                        ShimmerTextView STV_premiumMoney = view_end_game.findViewById(R.id.dialogEndGame_STV_premiumMoney);
                                        CV_x2 = view_end_game.findViewById(R.id.dialogEndGame_CV_x2);
                                        CV_x3 = view_end_game.findViewById(R.id.dialogEndGame_CV_x3);

                                        if (is_premium) {
                                            STV_premiumExp.setVisibility(View.VISIBLE);
                                            STV_premiumMoney.setVisibility(View.VISIBLE);
                                            Shimmer shimmer = new Shimmer();
                                            shimmer.start(STV_premiumExp);
                                            shimmer.start(STV_premiumMoney);
                                        }

                                        mTimer = new Timer();
                                        mMyTimerTask = new MyTimerTask();

                                        // singleshot delay 40000 ms
                                        mTimer.schedule(mMyTimerTask, 200000);

                                        CV_x2.setOnClickListener(v -> {
                                            if (mRewardedAd != null) {
                                                mRewardedAd.show(getActivity(), new OnUserEarnedRewardListener() {
                                                    @Override
                                                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                                        // Handle the reward.
                                                        Log.d("kkk", "The user earned the reward.");

                                                        socket.off("get_increased_game_award");

                                                        JSONObject json = new JSONObject();
                                                        try {
                                                            json.put("nick", MainActivity.NickName);
                                                            json.put("session_id", MainActivity.Session_id);
                                                            json.put("award_type", "advert");
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                        socket.emit("get_increased_game_award", json);
                                                        Log.d("kkk", "Socket_???????????????? - get_increased_game_award - "+ json.toString());


                                                        TV_moneyGameOver.setText(TV_moneyGameOver.getText() + " x2");
                                                        TV_expGameOver.setText(TV_expGameOver.getText() + " x2");

                                                        CV_x2.setVisibility(View.INVISIBLE);
                                                        CV_x3.setVisibility(View.INVISIBLE);
                                                    }
                                                });
                                            } else {
                                                Log.d("kkk", "The rewarded ad wasn't ready yet.");
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                                View viewDang = getLayoutInflater().inflate(R.layout.dialog_error, null);
                                                builder.setView(viewDang);
                                                TextView TV_title = viewDang.findViewById(R.id.dialogError_TV_errorTitle);
                                                TextView TV_error = viewDang.findViewById(R.id.dialogError_TV_errorText);
                                                TV_title.setText("?????????????? ?????? ???? ??????????????????????!");
                                                TV_error.setText("???????????????????? ?????? ?????? ?????????? ?????????????????? ????????????");
                                                AlertDialog alert = builder.create();
                                                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                                alert.show();
                                            }
                                        });

                                        CV_x3.setOnClickListener(v -> {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                            View viewQuestion = getLayoutInflater().inflate(R.layout.dialog_ok_no, null);
                                            builder.setView(viewQuestion);
                                            AlertDialog alert = builder.create();
                                            TextView TV_text = viewQuestion.findViewById(R.id.dialogOkNo_text);
                                            Button btn_yes = viewQuestion.findViewById(R.id.dialogOkNo_btn_yes);
                                            Button btn_no = viewQuestion.findViewById(R.id.dialogOkNo_btn_no);
                                            TV_text.setText("???? ??????????????, ?????? ???????????? ?????????????????? ?????????????? ?? 3 ???????? ???? 50 ?????????????");
                                            btn_yes.setOnClickListener(v1 -> {
                                                JSONObject json = new JSONObject();
                                                try {
                                                    json.put("nick", MainActivity.NickName);
                                                    json.put("session_id", MainActivity.Session_id);
                                                    json.put("award_type", "gold");
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                socket.emit("get_increased_game_award", json);
                                                Log.d("kkk", "Socket_???????????????? - get_increased_game_award - "+ json.toString());

                                                CV_x2.setVisibility(View.INVISIBLE);
                                                CV_x3.setVisibility(View.INVISIBLE);
                                                alert.cancel();
                                            });
                                            btn_no.setOnClickListener(v12 -> {
                                                alert.cancel();
                                            });
                                            alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                            alert.show();
                                        });

                                        TV_message.setText(message);
                                        TV_moneyGameOver.setText(String.valueOf(money));
                                        TV_expGameOver.setText(String.valueOf(exp));

                                        alert2.show();

                                        StopAnimation();

                                        messageModel = new MessageModel(test_num, message, time, "Server", "SystemMes");
                                    }
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
                                    TV_mafia_count.setText("?????????? " + mafia_now + "/" + mafia_max);
                                    TV_peaceful_count.setText("???????????? " + peaceful_now + "/" + peaceful_max);
                                    for (int i = 0; i < list_users.size(); i++)
                                    {
                                        if (list_users.get(i).getNick().equals(nick)) {
                                            list_users.get(i).setRole(role);
                                            list_users.get(i).setAlive(false);
                                            list_users.get(i).setAnimation_type(Role.NONE);
                                            if (nick.equals(player.getNick())) {
                                                StopAnimation();
                                                IV_influence_doctor.setVisibility(View.GONE);
                                                IV_influence_lover.setVisibility(View.GONE);
                                                IV_influence_bodyguard.setVisibility(View.GONE);
                                                IV_influence_poisoner.setVisibility(View.GONE);
                                                player.setStatus("dead");
                                                player.setCan_write(true);
                                                if (player.getTime() == Time.DAY)
                                                {
                                                    dayTime.setText("????????");
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
                                        TV_mafia_count.setText("?????????? " + mafia_now + "/" + mafia_max);
                                        TV_peaceful_count.setText("???????????? " + peaceful_now + "/" + peaceful_max);
                                        for (int i = 0; i < list_users.size(); i++)
                                        {
                                            if (list_users.get(i).getNick().equals(nick2)) {
                                                list_users.get(i).setRole(role2);
                                                list_users.get(i).setAlive(false);
                                                list_users.get(i).setAnimation_type(Role.NONE);
                                                if (nick2.equals(player.getNick())) {
                                                    StopAnimation();
                                                    IV_influence_doctor.setVisibility(View.GONE);
                                                    IV_influence_lover.setVisibility(View.GONE);
                                                    IV_influence_bodyguard.setVisibility(View.GONE);
                                                    IV_influence_poisoner.setVisibility(View.GONE);
                                                    player.setStatus("dead");
                                                    player.setCan_write(true);
                                                    if (player.getTime() == Time.DAY)
                                                    {
                                                        dayTime.setText("????????");
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
                                    messageModel = new MessageModel(test_num, message, time, "Server", "KillMes");

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
                                    messageModel = new MessageModel(test_num, "???????????????? ???? " + user_nick, time, mafia_nick, "VotingMes", fromBase64(""));
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
                                    messageModel = new MessageModel(test_num, "???????????????? ???? " + user_nick, time, voter, "VotingMes", fromBase64(avatar));
                                    break;
                                case "time_info":
                                    messageModel = new MessageModel(test_num, message, time, "Server", "SystemMes");
                                    break;
                                case "journalist":
                                    data2 = data.getJSONObject("message");
                                    message = data2.getString("message");
                                    messageModel = new MessageModel(test_num, message, time, "Server", "JournalistMes");
                                    break;
                                case "re_voting":
                                    if (IV_influence_lover.getVisibility() != View.VISIBLE && player.getStatus().equals("alive"))
                                    {
                                        if (player.getRole() == Role.TERRORIST) {
                                            StartAnimation(Role.TERRORIST);
                                        } else {
                                            JSONArray nicks_array = data.getJSONArray("re_voting_nicks");
                                            for (int i = 0; i < nicks_array.length(); i++)
                                            {
                                                String array_nick = nicks_array.getString(i);
                                                for (int j = 0; j < list_users.size(); j++) {
                                                    if (list_users.get(j).getNick().equals(array_nick) && !array_nick.equals(player.getNick())) {
                                                        list_users.get(j).setAnimation_type(Role.VOTING);
                                                    }

                                                }
                                            }
                                            player.setCan_click(true);
                                        }
                                    }
                                    messageModel = new MessageModel(test_num, message, time, "Server", "SystemMes");
                                    break;
                            }
                            Log.d("kkK", String.valueOf(messageModel.message));
                            Log.d("kkK", String.valueOf(list_chat.size()));
                            Log.d("kkK", String.valueOf("test_num " + test_num));
                            Log.d("kkK", String.valueOf("num " + num));
                            if (test_num > num) {
                                num = test_num;
                                list_chat.add(messageModel);
                                Log.d("kkk", "1");
                            } else {
                                Log.d("kkk", "2.1");
                                for (int i = 0; i < list_chat.size(); i++) {
                                    Log.d("kkk", "if " + test_num + " < " + list_chat.get(i).num);
                                    if (test_num < list_chat.get(i).num) {
                                        list_chat.add(i, messageModel);
                                        Log.d("kkk", "2");
                                        break;
                                    }
                                }
                            }
                            Log.d("kkK", String.valueOf(list_chat.size()));
                            messageAdapter.notifyDataSetChanged();
                            playersAdapter.notifyDataSetChanged();
                            if (TotalItemsCount < FirstVisibleItem + VisibleItemsCount + 3) {
                                LV_chat.setSelection(messageAdapter.getCount() - 1);
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
                    Log.d("kkk", "Socket_?????????????? - user_error " + args[0]);
                    JSONObject data = (JSONObject) args[0];
                    String error;
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    View viewError = getLayoutInflater().inflate(R.layout.dialog_error, null);
                    builder.setView(viewError);
                    TextView TV_error = viewError.findViewById(R.id.dialogError_TV_errorText);
                    try {
                        error = data.getString("error");
                        AlertDialog alert;
                        alert = builder.create();
                        switch (error) {
                            case "game_has_been_started":
                                AlertDialog.Builder builder2 = new AlertDialog.Builder(getContext());
                                View viewQuestion = getLayoutInflater().inflate(R.layout.dialog_ok_no, null);
                                builder2.setView(viewQuestion);
                                AlertDialog alert2 = builder2.create();
                                TextView TV_text = viewQuestion.findViewById(R.id.dialogOkNo_text);
                                Button btn_yes = viewQuestion.findViewById(R.id.dialogOkNo_btn_yes);
                                Button btn_no = viewQuestion.findViewById(R.id.dialogOkNo_btn_no);
                                TV_text.setText("???????? ?????? ????????. ???? ???????????? ?????????? ????????????????????????? ???? ???? ?????????????? ???????????? ?? ??????, ???????????????????? ?? ??????-???????? ???????????? ???? ?????????????? ??????????????, ???? ???? ?????????????? ??????????????????");
                                btn_yes.setOnClickListener(v1 -> {
                                    JSONObject json = new JSONObject();
                                    try {
                                        json.put("nick", player.getNick());
                                        json.put("session_id", player.getSession_id());
                                        json.put("room", player.getRoom_num());
                                        json.put("password", MainActivity.Password);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    socket.emit("get_in_room_observer", json);
                                    Log.d("kkk", "Socket_???????????????? - get_in_room_observer - "+ json.toString());
                                    alert2.cancel();
                                });
                                btn_no.setOnClickListener(v12 -> {
                                    alert2.cancel();
                                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
                                });
                                alert2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                alert2.show();
                                break;
                            case "max_people_in_room":
                                TV_error.setText("?? ?????????????? ?????? ????????!");
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
                                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                alert.show();
                                break;
                            case "no_room":
                                TV_error.setText("???????? ?????????????? ???? ????????????????????!");
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
                                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                alert.show();
                                break;
                            case "incorrect_password":
                                TV_error.setText("???? ?????????? ???????????????????????? ????????????!");
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
                                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                alert.show();
                                break;
                            case "you_are_playing_in_another_room":
                                TV_error.setText("???? ?????????????? ?? ???????????? ????????!");
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
                                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                alert.show();
                                break;
                            case "game_is_over":
                                TV_error.setText("???????? ??????????????????!");
                                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                alert.show();
                                break;
                            case "you_are_banned_in_this_room":
                                TV_error.setText("???? ???????????????? ?? ???????? ??????????????");
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
                                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                alert.show();
                                break;
                            case "you_are_already_friends":
                                TV_error.setText("???? ?????? ????????????!");
                                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                alert.show();
                                break;
                            case "you_are_already_sent_request":
                                TV_error.setText("???? ?????? ?????????????????? ???????????? ?????????? ????????????????!");
                                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                alert.show();
                                break;
                            case "you_are_already_got_request":
                                TV_error.setText("???? ?????? ???????????????? ???????????? ?? ???????????? ???? ?????????? ????????????????!");
                                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                alert.show();
                                break;
                            case "you_are_already_observer":
                                TV_error.setText("???? ?????? ???????????????????? ???? ???????????? ??????????!");
                                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                alert.show();
                                break;
                            case "you_are_observer":
                                TV_error.setText("???? ?????? ???????????????????? ???? ???????? ??????????!");
                                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                alert.show();
                                break;
                            case "you_dont_have_enough_gold":
                                TV_error.setText("?? ?????? ???????????????????????? ????????????!");
                                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                alert.show();
                                break;
                            default:
                                TV_error.setText("??????-???? ?????????? ???? ??????...");
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
                                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                alert.show();
                                break;
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private final Emitter.Listener onMafias = new Emitter.Listener() {
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
                    Log.d("kkk", "Socket_?????????????? - mafias - " + args[0]);
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

    private final Emitter.Listener onGetMyGameInfo = new Emitter.Listener()  {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!player.is_observer) {
                        JSONObject data = (JSONObject) args[0];
                        String role = "", status = "", time = "", nick;
                        boolean can_act = false, can_vote = false, last_message = false, can_skip_day = true;
                        boolean sheriff = false, doctor = false, lover = false, bodyguard = false, poisoner = false;
                        int voted_number;
                        Log.d("kkk", "Socket_?????????????? - get_my_game_info - " + args[0]);
                        JSONObject data2;
                        JSONObject influences;
                        try {
                            DeleteNumbersFromVoting();
                            if (data.has("sheriff_checks")) {
                                data2 = data.getJSONObject("sheriff_checks");
                                for (Iterator iterator = data2.keys(); iterator.hasNext(); ) {
                                    nick = (String) iterator.next();
                                    Role role2 = ConvertToRole(data2.getString(nick));
                                    for (int i = 0; i < list_users.size(); i++) {
                                        if (list_users.get(i).getNick().equals(nick)) {
                                            list_users.get(i).setRole(role2);
                                            break;
                                        }
                                    }
                                }
                                playersAdapter.notifyDataSetChanged();
                            }
                            if (data.has("journalist_checks")) {
                                JSONArray journalist_checks;
                                journalist_checks = data.getJSONArray("journalist_checks");
                                for (int i = 0; i < journalist_checks.length(); i++) {
                                    for (int j = 0; j < list_users.size(); j++) {
                                        if (list_users.get(j).getNick().equals(journalist_checks.getString(i))) {
                                            list_users.get(j).setChecked(true);
                                        }
                                    }
                                }
                                playersAdapter.notifyDataSetChanged();
                            }
                            if (!data.isNull("voting")) {
                                JSONObject voting = data.getJSONObject("voting");
                                Log.e("kkk", "4 " + voting.toString());
                                for (Iterator iterator = voting.keys(); iterator.hasNext(); ) {
                                    nick = (String) iterator.next();
                                    voted_number = voting.getInt(nick);
                                    Log.e("kkk", "5 " + nick + voted_number);
                                    for (int i = 0; i < list_users.size(); i++) {
                                        if (list_users.get(i).getNick().equals(nick)) {
                                            list_users.get(i).setVoting_number(voted_number);
                                            Log.e("kkk", "6 " + i + nick + voted_number);
                                        }
                                    }
                                }
                                playersAdapter.notifyDataSetChanged();
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
                            player.healed_yourself = data.getBoolean("heal_yourself");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        player.setRole(ConvertToRole(role));
                        player.setStatus(status);
                        for (int i = 0; i < list_users.size(); i++) {
                            if (list_users.get(i).getNick().equals(player.getNick())) {
                                list_users.get(i).setRole(player.getRole());
                            }
                        }
                        switch (time) {
                            case "lobby":
                                player.setTime(Time.LOBBY);
                                dayTime.setText("??????????");
                                dayTime.setBackgroundResource(R.drawable.green_button);
                                Constrain.setBackgroundResource(R.drawable.fon_day);
                                break;
                            case "night_love":
                                DeleteNumbersFromVoting();
                                player.setTime(Time.NIGHT_LOVE);
                                dayTime.setText("????????");
                                dayTime.setBackgroundResource(R.drawable.died_button);
                                Constrain.setBackgroundResource(R.drawable.fon_night);
                                break;
                            case "night_other":
                                player.setTime(Time.NIGHT_OTHER);
                                dayTime.setText("????????");
                                dayTime.setBackgroundResource(R.drawable.died_button);
                                Constrain.setBackgroundResource(R.drawable.fon_night);
                                break;
                            case "day":
                                DeleteNumbersFromVoting();
                                player.setTime(Time.DAY);
                                if (player.getStatus().equals("alive")) {
                                    dayTime.setText("?????????????? ??????");
                                    if (can_skip_day) {
                                        dayTime.setBackgroundResource(R.drawable.green_button);
                                    } else {
                                        dayTime.setBackgroundResource(R.drawable.grey_button);
                                    }
                                } else {
                                    dayTime.setText("????????");
                                    dayTime.setBackgroundResource(R.drawable.grey_button);
                                }
                                Constrain.setBackgroundResource(R.drawable.fon_day);
                                break;
                            case "voting":
                                player.setTime(Time.VOTING);
                                dayTime.setText("??????????????????????");
                                dayTime.setBackgroundResource(R.drawable.grey_button);
                                Constrain.setBackgroundResource(R.drawable.fon_day);
                                break;
                            case "re_voting":
                                player.setTime(Time.REVOTING);
                                dayTime.setText("??????????????????????????????");
                                dayTime.setBackgroundResource(R.drawable.grey_button);
                                Constrain.setBackgroundResource(R.drawable.fon_day);
                                break;
                        }
                    /*
                    if (TV_mafia_count.getVisibility() != View.VISIBLE)
                    {
                        int players = list_users.size();
                        mafia_max = list_mafias[players];
                        peaceful_max = list_peaceful[players];
                        mafia_now = mafia_now + mafia_max;
                        peaceful_now = peaceful_now + peaceful_max;
                        TV_mafia_count.setText("?????????? " + mafia_now + "/" + mafia_max);
                        TV_peaceful_count.setText("???????????? " + peaceful_now + "/" + peaceful_max);
                        TV_mafia_count.setVisibility(View.VISIBLE);
                        TV_peaceful_count.setVisibility(View.VISIBLE);
                    }
                     */

                        if (player.getStatus().equals("alive")) {
                            player.setCan_write(false);
                            switch (player.getTime()) {
                                case NIGHT_LOVE:
                                    IV_influence_poisoner.setVisibility(View.GONE);
                                    IV_influence_lover.setVisibility(View.GONE);
                                    switch (player.getRole()) {
                                        case MAFIA:
                                        case MAFIA_DON:
                                            player.setCan_write(true);
                                            break;
                                        case LOVER:
                                        case DOCTOR_OF_EASY_VIRTUE:
                                            if (can_act) StartAnimation(Role.LOVER);
                                            break;
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
                                            if (can_act) StartAnimation(Role.DOCTOR);
                                            break;
                                        case POISONER:
                                            if (can_act) StartAnimation(Role.POISONER);
                                            break;
                                        case JOURNALIST:
                                            if (can_act) StartAnimation(Role.JOURNALIST);
                                            break;
                                        default:
                                            Log.d("kkk", "?? " + player.getTime() + " - ???????????? ???????????????????????? ???????? " + player.getRole());
                                    }
                                    break;
                                case DAY:
                                    IV_influence_doctor.setVisibility(View.GONE);
                                    player.setVoted_at_night(false);
                                    player.setCan_write(true);
                                    IV_influence_bodyguard.setVisibility(View.GONE);
                                    if (player.getRole() == Role.BODYGUARD && IV_influence_lover.getVisibility() != View.VISIBLE) {
                                        if (can_act) StartAnimation(Role.BODYGUARD);
                                    }
                                    break;
                                case VOTING:
                                    messages_can_write = 10;
                                    if (IV_influence_lover.getVisibility() != View.VISIBLE) {
                                        if (player.getRole() == Role.TERRORIST) {
                                            if (can_act) StartAnimation(Role.TERRORIST);
                                        } else {
                                            if (can_vote) {
                                                StartAnimation(Role.VOTING);
                                            }
                                        }
                                    }
                                    break;
                                case REVOTING:
                                    if (!data.isNull("re_voting_nicks"))
                                    {
                                        Log.e("kkk", "1");
                                        if (IV_influence_lover.getVisibility() != View.VISIBLE && player.getStatus().equals("alive")) {
                                            if (player.getRole() == Role.TERRORIST) {
                                                if (can_act) StartAnimation(Role.TERRORIST);
                                            } else {
                                                if (can_vote) {
                                                    try {
                                                        JSONArray nicks_array = data.getJSONArray("re_voting_nicks");
                                                        Log.e("kkk", "2 " + nicks_array);
                                                        for (int i = 0; i < nicks_array.length(); i++)
                                                        {
                                                            String array_nick = nicks_array.getString(i);
                                                            for (int j = 0; j < list_users.size(); j++) {
                                                                if (list_users.get(j).getNick().equals(array_nick) && !array_nick.equals(player.getNick())) {
                                                                    list_users.get(j).setAnimation_type(Role.VOTING);
                                                                    Log.e("kkk", "2 " + list_users.get(j).getNick());
                                                                }
                                                            }
                                                        }
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                    player.setCan_click(true);
                                                    playersAdapter.notifyDataSetChanged();
                                                }
                                            }
                                        }
                                    }
                                    break;
                            }
                        } else {
                            player.setCan_write(true);
                        }

                        if (sheriff) IV_influence_sheriff.setVisibility(View.VISIBLE);
                        if (doctor) IV_influence_doctor.setVisibility(View.VISIBLE);
                        if (lover) IV_influence_lover.setVisibility(View.VISIBLE);
                        if (bodyguard) IV_influence_bodyguard.setVisibility(View.VISIBLE);
                        if (poisoner) IV_influence_poisoner.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        JSONObject data = (JSONObject) args[0];
                        String time = "";
                        int voted_number;
                        String nick = "";
                        try {
                            time = data.getString("time");
                            if (!data.isNull("voting")) {
                                DeleteNumbersFromVoting();
                                JSONObject voting = data.getJSONObject("voting");
                                for (Iterator iterator = voting.keys(); iterator.hasNext(); ) {
                                    nick = (String) iterator.next();
                                    voted_number = voting.getInt(nick);
                                    for (int i = 0; i < list_users.size(); i++) {
                                        if (list_users.get(i).getNick().equals(nick)) {
                                            list_users.get(i).setVoting_number(voted_number);
                                        }
                                    }
                                }
                                playersAdapter.notifyDataSetChanged();
                            } else {
                                DeleteNumbersFromVoting();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        switch (time) {
                            case "lobby":
                                player.setTime(Time.LOBBY);
                                dayTime.setText("??????????");
                                dayTime.setBackgroundResource(R.drawable.green_button);
                                Constrain.setBackgroundResource(R.drawable.fon_day);
                                break;
                            case "night_love":
                                DeleteNumbersFromVoting();
                                player.setTime(Time.NIGHT_LOVE);
                                dayTime.setText("????????");
                                dayTime.setBackgroundResource(R.drawable.died_button);
                                Constrain.setBackgroundResource(R.drawable.fon_night);
                                break;
                            case "night_other":
                                player.setTime(Time.NIGHT_OTHER);
                                dayTime.setText("????????");
                                dayTime.setBackgroundResource(R.drawable.died_button);
                                Constrain.setBackgroundResource(R.drawable.fon_night);
                                break;
                            case "day":
                                DeleteNumbersFromVoting();
                                player.setTime(Time.DAY);
                                dayTime.setText("????????");
                                dayTime.setBackgroundResource(R.drawable.grey_button);
                                Constrain.setBackgroundResource(R.drawable.fon_day);
                                break;
                            case "voting":
                                player.setTime(Time.VOTING);
                                dayTime.setText("??????????????????????");
                                dayTime.setBackgroundResource(R.drawable.grey_button);
                                Constrain.setBackgroundResource(R.drawable.fon_day);
                                break;
                        }
                    }
                }
            });
        }
    };

    private final Emitter.Listener onSuccessGetInRoom = new Emitter.Listener() {
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
                        json.put("room", player.getRoom_num());
                        json.put("last_message_num", num);
                        json.put("last_dead_message_num", -1);
                        json.put("session_id", player.getSession_id());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    socket.emit("connect_to_room", json);
                    Log.d("kkk", "connect_to_room - " + json);
                }
            });
        }
    };

    private final Emitter.Listener onSuccessGetInRoomObserver = new Emitter.Listener() {
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
                        json.put("session_id", player.getSession_id());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    socket.emit("connect_to_room_observer", json);
                    Log.e("kkk", "connect_to_room_observer - " + json);
                    player.is_observer = true;
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
                Log.d("kkk", "???????????? - get_profile - " + data);
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

                    TV_gamesCitizen.setText("???????????? ????????????: " + was_citizen);
                    TV_gamesSheriff.setText("??????????: " + was_sheriff);
                    TV_gamesDoctor.setText("????????????: " + was_doctor);
                    TV_gamesLover.setText("??????????????????: " + was_lover);
                    TV_gamesJournalist.setText("?????????? ??????: " + was_journalist);
                    TV_gamesBodyguard.setText("??????????????????????????: " + was_bodyguard);
                    TV_gamesManiac.setText("????????????: " + was_maniac);
                    TV_gamesDoctorOfEasyVirtue.setText("???????????? ?????????????? ??????????????????: " + was_doctor_of_easy_virtue);
                    TV_gamesMafia.setText("??????????: " + was_mafia);
                    TV_gamesMafiaDon.setText("?????? ??????????: " + was_mafia_don);
                    TV_gamesTerrorist.setText("??????????????????: " + was_terrorist);
                    TV_gamesPoisoner.setText("????????????????????: " + was_poisoner);

                    TV_nick.setText(nick);
                    if (!main_status.equals("")) {
                        TV_status.setText("{" + main_status + "}");
                    }
                    if (!main_personal_color.equals("")) {
                        TV_nick.setTextColor(Color.parseColor(main_personal_color));
                        TV_status.setTextColor(Color.parseColor(main_personal_color));
                    }

                    TV_exp.setText(exp + " XP");
                    TV_rang.setText(rang + " ????????");
                    TV_game_counter.setText("?????????????? ?????? " + game_counter);
                    TV_max_money_score.setText("????????. ?????????? ???? ???????? " + max_money_score);
                    TV_max_exp_score.setText("????????. ?????????? ???? ???????? " + max_exp_score);
                    TV_general_pers_of_wins.setText("?????????????? ?????????? " + general_pers_of_wins);
                    TV_mafia_pers_of_wins.setText("?????????? ???? ?????????? " + mafia_pers_of_wins);
                    TV_peaceful_pers_of_wins.setText("?????????? ???? ???????????? " + peaceful_pers_of_wins);

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
                                        Log.d("kkk", "Socket_????????????????_ban_user_in_room - " + json2.toString());
                                        socket.emit("ban_user_in_room", json2);
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
                                    } else {
                                        AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
                                        View viewError = getLayoutInflater().inflate(R.layout.dialog_error, null);
                                        builder2.setView(viewError);
                                        AlertDialog alert2;
                                        TextView TV_error = viewError.findViewById(R.id.dialogError_TV_errorText);
                                        TV_error.setText("???????????? ???????????? ???????????????? ???? ?????????????????? ???????????? ???? ???????????? ????????!");
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
                                    Log.d("kkk", "Socket_????????????????_ban_user_in_room - " + json2.toString());
                                    socket.emit("ban_user_in_room", json2);
                                }
                            }
                        });
                    }
                    else
                    {
                        btn_kick.setVisibility(View.GONE);
                    }

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
                        final JSONObject json2 = new JSONObject();
                        try {
                            json2.put("nick", MainActivity.NickName);
                            json2.put("session_id", MainActivity.Session_id);
                            json2.put("room", player.getRoom_num());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d("kkk", "Socket_????????????????_leave_user - " + json2.toString());
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
                                        reason[0] = "???????? ?????? ????????";
                                        break;
                                    case R.id.dialogReport_RB_2:
                                        reason[0] = "???????????????????? ???????????????????? ????????????????????, ????????????????????????, ?????????????????????????????????? ?????? ?????????????????????????????? ?????????????????? ?????? ???????? ????????????????????, ???????????????????? ?????????????? ?????????????????? ?????? ???????????????????????? ???????????? ???????????????????????? ???????????????????????????????? ????";
                                        break;
                                    case R.id.dialogReport_RB_3:
                                        reason[0] = "?????????????????????????????? ????????????????????, ?????????????? ???????????????????? ???? ???????????????????? ??????????, ???????????????????? ????????????????????????, ?????????????? ?????? ?????????????????????? ?????????????????? ?? ???????????? ?????? ???????? ????????????????????, ???? ?????????????????????????????? ?????????????? ?????????????????????????? ?????????????????? ?????? ???????????????????????????????? ??????????????????????????????";
                                        break;
                                    case R.id.dialogReport_RB_4:
                                        reason[0] = "???????? ????????????/???? ?? ?????????????????? ?????????? ??????????????";
                                        break;
                                    case R.id.dialogReport_RB_5:
                                        reason[0] = "???????? (??.??. ?????????????? ???????? ???????????????????????????? ?????????????? ??????, ???????? ?????????????? ???????????????????? ???? ?????????????? ???????????????????? ?????????????? ?????? ?????????????????????? ???? ????????, ?????? ???? ???????????????????? ???????????? ??????????????)";
                                        break;
                                    case R.id.dialogReport_RB_6:
                                        reason[0] = "???????????????? ???????????????????? ?????????????? ?????????????? ?? ????????????????????, ???????????????????? ?????????????????????????? ???????????? ?? ???????? ???? ????????";
                                        break;
                                    case R.id.dialogReport_RB_7:
                                        reason[0] = "???????????????????? ????????????????, ???????????????????????? ???? ???????????????? ???????????? ?????????????????????????? ?? ?????????????????????? (???? ???????????????? ???????????????? ????????????????)";
                                        break;
                                    case R.id.dialogReport_RB_8:
                                        reason[0] = "??????????????????/?????????????????????????? ???????????????????????????? ???????????? ???????????????????????? ?????? ????????????????????";
                                        break;
                                    case R.id.dialogReport_RB_9:
                                        reason[0] = "????????????";
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
                                Log.d("kkk", "Socket_???????????????? - send_complaint" + json2);
                                alert2.cancel();
                            }
                            else
                            {
                                AlertDialog.Builder builder3 = new AlertDialog.Builder(getActivity());
                                View viewError = getLayoutInflater().inflate(R.layout.dialog_error, null);
                                builder3.setView(viewError);
                                TextView TV_error = viewError.findViewById(R.id.dialogError_TV_errorText);
                                AlertDialog alert3;
                                TV_error.setText("?????????????????? ?????? ????????!");
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
                        Log.d("kkk", "Socket_???????????????? - friend_request" + json.toString());
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

                    TV_gamesCitizen.setText("???????????? ????????????: " + was_citizen);
                    TV_gamesSheriff.setText("??????????: " + was_sheriff);
                    TV_gamesDoctor.setText("????????????: " + was_doctor);
                    TV_gamesLover.setText("??????????????????: " + was_lover);
                    TV_gamesJournalist.setText("?????????? ??????: " + was_journalist);
                    TV_gamesBodyguard.setText("??????????????????????????: " + was_bodyguard);
                    TV_gamesManiac.setText("????????????: " + was_maniac);
                    TV_gamesDoctorOfEasyVirtue.setText("???????????? ?????????????? ??????????????????: " + was_doctor_of_easy_virtue);
                    TV_gamesMafia.setText("??????????: " + was_mafia);
                    TV_gamesMafiaDon.setText("?????? ??????????: " + was_mafia_don);
                    TV_gamesTerrorist.setText("??????????????????: " + was_terrorist);
                    TV_gamesPoisoner.setText("????????????????????: " + was_poisoner);

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

                    TV_game_counter.setText("?????????????? ??????: " + game_counter);
                    TV_max_money_score.setText("????????. ?????????? ???? ????????: " + max_money_score);
                    TV_max_exp_score.setText("????????. ?????????? ???? ????????: " + max_exp_score);
                    TV_general_pers_of_wins.setText("?????????????? ??????????: " + general_pers_of_wins);
                    TV_mafia_pers_of_wins.setText("?????????? ???? ??????????: " + mafia_pers_of_wins);
                    TV_peaceful_pers_of_wins.setText("?????????? ???? ????????????: " + peaceful_pers_of_wins);

                    TV_gold.setText(gold + " ????????????");
                    TV_money.setText(money + " $");
                    TV_exp.setText(exp + " XP");
                    TV_rang.setText(rang + " ????????");

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
                Log.d("kkk", "???????????? - host_info - " + data);
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
                Log.d("kkk", "???????????? - ban_user_in_room - " + data);
                String nick = "";
                String time = "";
                String host_nick = "";
                int test_num = -1, room_id;
                try {
                    test_num = data.getInt("num");
                    nick = data.getString("nick");
                    room_id = data.getInt("room");
                    host_nick = data.getString("host_nick");
                    time = data.getString("time");
                    time = getDate(time);
                } catch (JSONException e) {
                    return;
                }
                MessageModel messageModel = new MessageModel(test_num, host_nick + " ????????????(-??) " + nick, time, nick, "KickMes");
                if (test_num != num && room_id == player.getRoom_num()) {
                    if (test_num > num) {
                        num = test_num;
                        list_chat.add(messageModel);
                    }
                    else {
                        for (int i = 0; i < list_chat.size(); i++) {
                            if (test_num > list_chat.get(i).num) {
                                list_chat.add(i, messageModel);
                                break;
                            }
                        }
                    }
                    if (nick.equals(MainActivity.NickName)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        View viewError = getLayoutInflater().inflate(R.layout.dialog_error, null);
                        builder.setView(viewError);
                        TextView TV_title = viewError.findViewById(R.id.dialogError_TV_errorTitle);
                        TextView TV_error = viewError.findViewById(R.id.dialogError_TV_errorText);
                        TV_title.setText("?????? ?????????????? ???? ??????????????!");
                        TV_error.setText("???????????????????? ?????????? ?? ???????????? ??????????????");

                        AlertDialog alert = builder.create();
                        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        alert.show();
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
                    }
                    for (int i = list_users.size() - 1; i >= 0; i--) {
                        if (list_users.get(i).getNick().equals(nick)) {
                            Log.d("kkk", "remove " + list_users.get(i).getNick());
                            list_users.remove(i);
                        }
                    }

                    for (int i = 0; i < list_chat.size(); i++) {
                        if (list_chat.get(i).message.equals(nick + " ??????????(-??) ?? ??????")) {
                            list_chat.remove(i);
                        }
                    }
                    messageAdapter.notifyDataSetChanged();
                    playersAdapter.notifyDataSetChanged();
                    TV_playersCount.setText("????????????: " + list_users.size());
                    if (TotalItemsCount < FirstVisibleItem + VisibleItemsCount + 3) {
                        LV_chat.setSelection(messageAdapter.getCount() - 1);
                    }
                }
            }
        });
    };

    private final Emitter.Listener OnBanUserInRoomError = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("kkk", "???????????? - ban_user_in_room_error");
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                View viewError = getLayoutInflater().inflate(R.layout.dialog_error, null);
                builder.setView(viewError);
                TextView TV_error = viewError.findViewById(R.id.dialogError_TV_errorText);
                TV_error.setText("???? ???? ???????????? ???????????? ?????????????? ???? ??????????????!");

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
                Log.d("kkk", "???????????? - user_message_delay - " + args[0]);
                String time_to_unmute = String.valueOf(args[0]);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                View viewError = getLayoutInflater().inflate(R.layout.dialog_error, null);
                builder.setView(viewError);
                TextView TV_title = viewError.findViewById(R.id.dialogError_TV_errorTitle);
                TextView TV_error = viewError.findViewById(R.id.dialogError_TV_errorText);
                TV_title.setText("???? ?????????????? ?????????? ?????????????????????? ??????????????????!");
                TV_error.setText("???? ?????????????? ???????????????? ?????????????????? ?????????????????? ?????????? " + time_to_unmute + " ????????????");

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
                Log.d("kkk", "???????????? - send_complain - " + data);
                String status = "";
                try {
                    status = data.getString("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (status.equals("complaints_limit_exceeded"))
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("???? ?????????????????? ?????????? ??????????!")
                            .setMessage("")
                            .setIcon(R.drawable.ic_error)
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
    };

    private final Emitter.Listener onMySendRequest = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject data = (JSONObject) args[0];
                Log.d("kkk", "???????????? - my_send_request - " + data);
                String status = "";
                try {
                    status = data.getString("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (status.equals("OK"))
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("???????????? ??????????????????!")
                            .setMessage("")
                            .setIcon(R.drawable.ic_ok)
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
    };

    private final Emitter.Listener onGetIncreasedGameAward = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String status = String.valueOf(args[0]);
                Log.d("kkk", "???????????? - get_increased_game_award - " + status);
                if (status.equals("OK"))
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    View viewError = getLayoutInflater().inflate(R.layout.dialog_error, null);
                    builder.setView(viewError);
                    AlertDialog alert;
                    alert = builder.create();

                    TextView TV = viewError.findViewById(R.id.dialogError_TV_errorText);
                    TextView TV_title = viewError.findViewById(R.id.dialogError_TV_errorTitle);
                    ImageView IV = viewError.findViewById(R.id.dialogError_IV);

                    TV_moneyGameOver.setText(TV_moneyGameOver.getText() + " x3");
                    TV_expGameOver.setText(TV_expGameOver.getText() + " x3");

                    IV.setImageResource(R.drawable.crown_gold_dark);
                    TV.setText("???? ?????????????????? ???????? ?????????????? ?? 3 ????????!!");
                    TV_title.setText("??????????????!");
                    alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alert.show();
                }
            }
        });
    };

    private final Emitter.Listener onDailyTaskCompleted = new Emitter.Listener() {
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
                    Log.d("kkk", "Socket_?????????????? - daily_tasl_completed - " + args[0]);
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

    private final Emitter.Listener onRolesCounter = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject data = (JSONObject) args[0];
                Log.d("kkk", "???????????? - roles_counter - " + data);
                String role = "";
                int role_count = 0;
                mafia_max = 0;
                peaceful_max = 0;
                list_roles.clear();
                try {
                    for (Iterator iterator = data.keys(); iterator.hasNext();)
                    {
                        role = (String) iterator.next();
                        role_count = data.getInt(role);

                        switch (role)
                        {
                            case "citizen":
                                list_roles.add(new RoleModel(Role.CITIZEN, role_count));
                                peaceful_max += role_count;
                                break;
                            case "mafia":
                                list_roles.add(new RoleModel(Role.MAFIA, role_count));
                                mafia_max += role_count;
                                break;
                            case "sheriff":
                                list_roles.add(new RoleModel(Role.SHERIFF, role_count));
                                peaceful_max += role_count;
                                break;
                            case "doctor":
                                list_roles.add(new RoleModel(Role.DOCTOR, role_count));
                                peaceful_max += role_count;
                                break;
                            case "lover":
                                list_roles.add(new RoleModel(Role.LOVER, role_count));
                                peaceful_max += role_count;
                                break;
                            case "mafia_don":
                                list_roles.add(new RoleModel(Role.MAFIA_DON, role_count));
                                mafia_max += role_count;
                                break;
                            case "maniac":
                                list_roles.add(new RoleModel(Role.MANIAC, role_count));
                                peaceful_max += role_count;
                                break;
                            case "terrorist":
                                list_roles.add(new RoleModel(Role.TERRORIST, role_count));
                                mafia_max += role_count;
                                break;
                            case "bodyguard":
                                list_roles.add(new RoleModel(Role.BODYGUARD, role_count));
                                peaceful_max += role_count;
                                break;
                            case "poisoner":
                                list_roles.add(new RoleModel(Role.POISONER, role_count));
                                mafia_max += role_count;
                                break;
                            case "journalist":
                                list_roles.add(new RoleModel(Role.JOURNALIST, role_count));
                                peaceful_max += role_count;
                                break;
                            case "doctor_of_easy_virtue":
                                list_roles.add(new RoleModel(Role.DOCTOR_OF_EASY_VIRTUE, role_count));
                                peaceful_max += role_count;
                                break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (mafia_now <= 0) {
                    mafia_now = mafia_now + mafia_max;
                }
                if (peaceful_now <= 0) {
                    peaceful_now = peaceful_now + peaceful_max;
                }
                TV_mafia_count.setText("?????????? " + mafia_now + "/" + mafia_max);
                TV_peaceful_count.setText("???????????? " + peaceful_now + "/" + peaceful_max);
                TV_mafia_count.setVisibility(View.VISIBLE);
                TV_peaceful_count.setVisibility(View.VISIBLE);
            }
        });
    };

    /*******************************
     *                             *
     *       SOCKETS end           *
     *                             *
     *******************************/

    //???????????? ????????????????
    public void StartAnimation(Role type) {
        if (!player.is_observer) {
            player.setCan_click(true);
            switch (type) {
                case JOURNALIST:
                    for (int i = 0; i < list_users.size(); i++) {
                        if (list_users.get(i).getAlive()) {
                            list_users.get(i).setAnimation_type(type);
                        }
                    }
                case DOCTOR:
                    for (int i = 0; i < list_users.size(); i++) {
                        if (list_users.get(i).getAlive()) {
                            if (!list_users.get(i).getNick().equals(player.getNick())) {
                                list_users.get(i).setAnimation_type(type);
                            } else if (!player.healed_yourself) {
                                list_users.get(i).setAnimation_type(type);
                            }
                        }
                    }
                    break;
                case MAFIA:
                case MAFIA_DON:
                    for (int i = 0; i < list_users.size(); i++) {
                        if (list_users.get(i).getAlive() && !list_users.get(i).getNick().equals(player.getNick()) && list_users.get(i).getRole() == Role.NONE) {
                            list_users.get(i).setAnimation_type(type);
                        }

                    }
                    break;
                default:
                    for (int i = 0; i < list_users.size(); i++) {
                        if (list_users.get(i).getAlive() && !list_users.get(i).getNick().equals(player.getNick())) {
                            list_users.get(i).setAnimation_type(type);
                        }

                    }
            }
            playersAdapter.notifyDataSetChanged();
        }

    }
    //?????????? ????????????????
    public void StopAnimation() {
        player.setCan_click(false);
        for (int i = 0; i < list_users.size(); i++)
        {
            list_users.get(i).setAnimation_type(Role.NONE);
        }
        playersAdapter.notifyDataSetChanged();
    }
    //???????????????? ????????
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
        Log.d("kkk", "Socket_???????????????? - role_action"+ json3.toString());
    }
    //?????????????? ??????????????????????
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
        Log.d("kkk", "Socket_???????????????? - voting"+ json3.toString());
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
    //?????????? ??????????????
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
        Log.d("kkk", "Socket_???????????????? - get_profile - "+ json.toString());
    }
    //???????????? ?????????? ???? ??????????????????????
    public void DeleteNumbersFromVoting() {
        for (int i = 0; i < list_users.size(); i++)
        {
            list_users.get(i).setVoting_number(0);
        }
        playersAdapter.notifyDataSetChanged();
    }
    //???????????????? ???????? ?? ???????????????? ??????????????
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

    public void askToLeave() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View viewQuestion = getLayoutInflater().inflate(R.layout.dialog_ok_no, null);
        builder.setView(viewQuestion);
        AlertDialog alert = builder.create();
        TextView TV_text = viewQuestion.findViewById(R.id.dialogOkNo_text);
        Button btn_yes = viewQuestion.findViewById(R.id.dialogOkNo_btn_yes);
        Button btn_no = viewQuestion.findViewById(R.id.dialogOkNo_btn_no);
        TV_text.setText("???? ??????????????, ?????? ???????????? ?????????? ???? ???????????????");
        btn_yes.setOnClickListener(v1 -> {
            final JSONObject json2 = new JSONObject();
            try {
                json2.put("nick", MainActivity.NickName);
                json2.put("session_id", MainActivity.Session_id);
                json2.put("room", player.getRoom_num());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("kkk", "Socket_???????????????? leave_room - " + json2.toString());
            socket.emit("leave_room", json2);
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
            alert.cancel();
        });
        btn_no.setOnClickListener(v12 -> {
            alert.cancel();
        });
        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alert.show();
    }

    class MyTimerTask extends TimerTask {
        @Override
        public void run() {

            if (getActivity() != null) {
                ContextCompat.getMainExecutor(getActivity()).execute(() -> {
                    CV_x2.setVisibility(View.INVISIBLE);
                    CV_x3.setVisibility(View.INVISIBLE);
                });
            }
        }
    }
}