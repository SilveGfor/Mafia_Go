package com.example.mafiago.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.example.mafiago.MainActivity;
import com.example.mafiago.R;
import com.example.mafiago.adapters.FriendsAdapter;
import com.example.mafiago.adapters.GamesAdapter;
import com.example.mafiago.models.FriendModel;
import com.example.mafiago.models.RoomModel;

import java.util.ArrayList;

public class FriendsFragment extends Fragment {

    public ListView friendsView;

    public Button btnExit;

    ArrayList<FriendModel> list_friends = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        friendsView = view.findViewById(R.id.fragmentFriends_list_friends);
        btnExit = view.findViewById(R.id.fragmentFriends_btn_exit);

        //FriendModel model = new FriendModel("SilveGfor", true);
        list_friends.add(new FriendModel("SilveGfor", true));
        list_friends.add(new FriendModel("Van_Vour", false));
        list_friends.add(new FriendModel("Сис.Админ РОТ", false));
        FriendsAdapter customList = new FriendsAdapter(list_friends, getContext());
        friendsView.setAdapter(customList);

        friendsView.setOnItemClickListener((parent, view1, position, id) -> {
            //getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GameFragment()).commit();
        });

        btnExit.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new MenuFragment()).commit();
        });


        return view;
    }
}