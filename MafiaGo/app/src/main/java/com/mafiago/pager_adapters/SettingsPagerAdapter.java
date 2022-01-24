package com.mafiago.pager_adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.mafiago.R;
import com.google.android.material.tabs.TabLayout;
import com.mafiago.small_fragments.GameChatFragment;
import com.mafiago.small_fragments.SettingsMainFragment;

import java.util.concurrent.TimeUnit;

import static com.mafiago.MainActivity.socket;

public class SettingsPagerAdapter extends FragmentStatePagerAdapter {

    final int PAGE_COUNT = 2;
    private String tabTitles[] = new String[] { "Настройки профиля", "Основные"};
    private SettingsMainFragment fragment1;
    private SettingsMainFragment fragment2;
    private Context context;

    public SettingsPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override public int getCount() {
        return PAGE_COUNT;
    }

    @Override public Fragment getItem(int position) {
        if (position == 0)
        {
            Log.d("kkk", "getItem " + position);
            fragment1 = SettingsMainFragment.newInstance(position + 1);
            return fragment1;
        }
        else
        {
            Log.d("kkk", "getItem " + position);
            fragment2 = SettingsMainFragment.newInstance(position + 1);
            return fragment2;
        }
    }

    @Override public CharSequence getPageTitle(int position) {
        // генерируем заголовок в зависимости от позиции
        return tabTitles[position];
    }

    public void startProfileStudy()
    {
        if (fragment1 != null) {
            fragment1.startProfileStudy();
        }
    }

    public void startMainStudy()
    {
        Log.d("kkk", "startProfileStudy1");
        Log.d("kkk", "startProfileStudy2" + fragment1);
        if (fragment2 != null) {
            fragment2.startMainStudy();
        }
    }
}