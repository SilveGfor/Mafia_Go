package com.mafiago.pager_adapters;

import android.content.Context;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.mafiago.small_fragments.GameChatFragment;

import java.util.ArrayList;

public class GameChatPagerAdapter extends FragmentStatePagerAdapter {
    final int PAGE_COUNT = 2;
    private String tabTitles[] = new String[] { "Tab1", "Tab2"};
    private Context context;

    public GameChatPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override public int getCount() {
        return PAGE_COUNT;
    }

    @Override public Fragment getItem(int position) {
        Log.d("kkk", "getItem in GameChatPagerAdapter");
        return GameChatFragment.newInstance(position + 1);
    }

    @Override public CharSequence getPageTitle(int position) {
        // генерируем заголовок в зависимости от позиции
        return tabTitles[position];
    }
}