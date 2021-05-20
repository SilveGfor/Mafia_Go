package com.example.mafiago.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import com.example.mafiago.MainActivity;
import com.example.mafiago.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;


public class MenuFragment extends Fragment {
    Button btnRules;
    Button btnGames;
    Button btnFriends;
    Button btnTools;
    TextView txtNick;

    FloatingActionButton FAB_exit_menu;

    CardView CV_info;

    int pressedTimes = 0;

    // Идентификатор уведомления
    private static final int NOTIFY_ID = 101;

    // Идентификатор канала
    private static String CHANNEL_ID = "Notifications channel";

    public static final String APP_PREFERENCES = "user";
    public static final String APP_PREFERENCES_EMAIL = "email";
    public static final String APP_PREFERENCES_PASSWORD = "password";
    public static final String APP_PREFERENCES_NICKNAME = "nickname";

    private SharedPreferences mSettings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view;
        view = inflater.inflate(R.layout.fragment_menu, container, false);

        btnRules = view.findViewById(R.id.btnRules);
        btnGames = view.findViewById(R.id.btnGame);
        btnFriends = view.findViewById(R.id.btnFriends);
        btnTools = view.findViewById(R.id.btnTools);
        txtNick = view.findViewById(R.id.txtNick);
        txtNick.setText(MainActivity.NickName);

        mSettings = getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        FAB_exit_menu = view.findViewById(R.id.fragmentMenu_FAB_exit_menu);

        CV_info = view.findViewById(R.id.fragmentMenuMenu_CV_info);

        //настройки от Шлыкова
        //Nastroiki nastroiki = new Nastroiki();

        FAB_exit_menu.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new StartFragment()).commit();
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putString(APP_PREFERENCES_EMAIL, null);
            editor.putString(APP_PREFERENCES_PASSWORD, null);
            editor.apply();
        });

        CV_info.setOnClickListener(v -> {
            final Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.bounce_center);

            // amplitude 0.2 and frequency 20
            BounceInterpolator interpolator = new BounceInterpolator();
            animation.setInterpolator(interpolator);

            CV_info.startAnimation(animation);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            View view_profile = inflater.inflate(R.layout.item_profile, null);
            builder.setView(view_profile);
            FloatingActionButton FAB_add_friend = view_profile.findViewById(R.id.Item_profile_add_friend);
            TextView TV_nick = view_profile.findViewById(R.id.Item_profile_TV_nick);

            TV_nick.setText(MainActivity.NickName);
            FAB_add_friend.setOnClickListener(v1 -> {
                //добавление в друзья
            });
            AlertDialog alert = builder.create();
            alert.show();
        });

        btnTools.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new SettingsFragment()).commit();
        });

        btnRules.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new RulesFragment()).commit();
            }
        });

        btnGames.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
            }
        });

        btnFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new FriendsFragment()).commit();
            }
        });
        return view;
    }
}
