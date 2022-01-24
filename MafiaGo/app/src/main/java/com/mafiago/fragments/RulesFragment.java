package com.mafiago.fragments;

import android.animation.ArgbEvaluator;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.mafiago.R;
import com.google.android.material.tabs.TabLayout;
import com.mafiago.adapters.RulesAdapter;
import com.mafiago.classes.NonSweepViewPager;
import com.mafiago.classes.OnBackPressedListener;
import com.mafiago.models.RuleModel;
import com.mafiago.pager_adapters.RulesPagerAdapter;
import com.mafiago.pager_adapters.SettingsPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class RulesFragment extends Fragment implements OnBackPressedListener {



    TabLayout tab;
    NonSweepViewPager viewPager;
    ImageView Menu;
    RelativeLayout btn_back;

    Button Back1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_rules, container, false);

        tab = view.findViewById(R.id.fragmentRules_TabLayout);
        viewPager = view.findViewById(R.id.fragmentRules_ViewPager);
        Menu = view.findViewById(R.id.fragmentMenu_IV_menu);
        btn_back = view.findViewById(R.id.fragmentGamesList_RL_back);

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
                new RulesPagerAdapter(getActivity().getSupportFragmentManager()));

        // Передаём ViewPager в TabLayout
        tab.setupWithViewPager(viewPager);

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
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new MenuFragment()).commit();
    }
}
