package com.example.mafiago.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mafiago.R;
import com.example.mafiago.models.FriendModel;
import com.example.mafiago.models.RoomModel;

import java.util.ArrayList;

public class FriendsFragment extends Fragment {

    ArrayList<FriendModel> list_friends = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friends, container, false);



        return view;
    }
}