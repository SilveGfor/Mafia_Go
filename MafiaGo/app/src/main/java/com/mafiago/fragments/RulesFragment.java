package com.mafiago.fragments;

import android.animation.ArgbEvaluator;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

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

    Button Back1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_rules, container, false);

        tab = view.findViewById(R.id.fragmentRules_TabLayout);
        viewPager = view.findViewById(R.id.fragmentRules_ViewPager);

        // Получаем ViewPager и устанавливаем в него адаптер
        viewPager.setAdapter(
                new RulesPagerAdapter(getActivity().getSupportFragmentManager()));

        // Передаём ViewPager в TabLayout
        tab.setupWithViewPager(viewPager);


        return view;
    }
    @Override
    public void onBackPressed() {
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new MenuFragment()).commit();
    }
}
