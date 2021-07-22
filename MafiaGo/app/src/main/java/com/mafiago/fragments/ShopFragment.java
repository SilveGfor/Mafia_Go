package com.mafiago.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mafiago.R;
import com.mafiago.classes.OnBackPressedListener;

import static com.mafiago.MainActivity.socket;

public class ShopFragment extends Fragment implements OnBackPressedListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shop, container, false);
    }
    @Override
    public void onBackPressed() {
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new MenuFragment()).commit();
    }
}