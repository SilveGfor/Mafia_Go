package com.example.mafiago.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.mafiago.MainActivity;
import com.example.mafiago.R;


public class MenuFragment extends Fragment {
    Button btnRules;
    Button btnGames;
    Button btnProfile;
    Button btnTools;
    TextView txtNick;

    int pressedTimes = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view;
        view = inflater.inflate(R.layout.fragment_menu, container, false);

        btnRules = view.findViewById(R.id.btnRules);
        btnGames = view.findViewById(R.id.btnGame);
        btnProfile = view.findViewById(R.id.btnProfile);
        btnTools = view.findViewById(R.id.btnTools);
        txtNick = view.findViewById(R.id.txtNick);
        txtNick.setText(MainActivity.NickName);

        Button bubbleButton = (Button) view.findViewById(R.id.bubble_button);
        TextView tvNotification = (TextView)view.findViewById(R.id.tvNotification);


        bubbleButton.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                pressedTimes += 1;
                tvNotification.setText(String.valueOf(pressedTimes));

            }
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

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Профиль")
                        .setMessage("")
                        .setIcon(R.drawable.ic_info)
                        .setCancelable(false)
                        .setNegativeButton("Ок",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
        return view;
    }
}
