package com.mafiago.pager_adapters;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.mafiago.small_fragments.FriendsSmallFragment;
import com.mafiago.small_fragments.SettingsMainFragment;

public class FriendsPagerAdapter extends FragmentStatePagerAdapter {
    final int PAGE_COUNT = 3;
    private String tabTitles[] = new String[] { "Друзья", "Входящие", "Исходящие"};

    public FriendsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override public int getCount() {
        return PAGE_COUNT;
    }

    @Override public Fragment getItem(int position) {
        return FriendsSmallFragment.newInstance(position + 1);
    }

    @Override public CharSequence getPageTitle(int position) {
        // генерируем заголовок в зависимости от позиции
        return tabTitles[position];
    }
}
