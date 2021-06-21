package com.mafiago.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.mafiago.R;
import com.mafiago.classes.OnBackPressedListener;

import static com.mafiago.MainActivity.socket;

public class SettingsFragment extends Fragment implements OnBackPressedListener {

    Button btnExitSettings;
    Button btnExitAccount;

    public static final String APP_PREFERENCES = "user";
    public static final String APP_PREFERENCES_EMAIL = "email";
    public static final String APP_PREFERENCES_PASSWORD = "password";
    public static final String APP_PREFERENCES_LAST_ROLE = "role";

    private SharedPreferences mSettings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        btnExitSettings = view.findViewById(R.id.btnExitSettings);
        btnExitAccount = view.findViewById(R.id.fragmentSetings_btn_exit_account);

        mSettings = getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        btnExitSettings.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new MenuFragment()).commit();
        });

        btnExitAccount.setOnClickListener(v -> {
            socket.emit("leave_app", "");
            Log.d("kkk", "Socket_отправка - leave_app");
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new StartFragment()).commit();
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putString(APP_PREFERENCES_EMAIL, null);
            editor.putString(APP_PREFERENCES_PASSWORD, null);
            editor.apply();
        });


        return  view;
    }

    @Override
    public void onBackPressed() {
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new MenuFragment()).commit();
    }
}