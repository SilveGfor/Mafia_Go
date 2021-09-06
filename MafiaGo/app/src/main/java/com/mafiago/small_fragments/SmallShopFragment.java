package com.mafiago.small_fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mafiago.R;
import com.mafiago.MainActivity;
import com.mafiago.adapters.FriendRequestsAdapter;
import com.mafiago.adapters.FriendsAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import static com.mafiago.MainActivity.socket;

public class SmallShopFragment extends Fragment {

    public static final String ARG_PAGE = "ARG_PAGE";

    private int mPage;

    public static SmallShopFragment newInstance(int page) {
        SmallShopFragment fragment = new SmallShopFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPage = getArguments().getInt(ARG_PAGE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = null;

        switch (mPage)
        {
            case 1:
                view = inflater.inflate(R.layout.small_fragment_friends_list, container, false);
                break;
            case 2:
                view = inflater.inflate(R.layout.small_fragment_friends_requests, container, false);
                LV_requests = view.findViewById(R.id.smallFragmentFriendsRequests_LV);
                TV_no_requests = view.findViewById(R.id.smallFragmentFriendsRequests_TV_noRequests);
                PB_loading_requests = view.findViewById(R.id.smallFragmentFriendsRequests_PB);

                requestsAdapter = new FriendRequestsAdapter(list_requests, getContext());
                LV_requests.setAdapter(requestsAdapter);

                PB_loading_requests.setVisibility(View.VISIBLE);
                TV_no_requests.setVisibility(View.GONE);

                socket.on("get_friend_request", OnGetFriendRequest);

                json = new JSONObject();
                try {
                    json.put("nick", MainActivity.NickName);
                    json.put("session_id", MainActivity.Session_id);
                    json.put("request_type", "other");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                socket.emit("get_list_of_friend_requests", json);
                Log.d("kkk", "Socket_отправка - get_list_of_friend_requests - "+ json.toString());

                break;
            case 3:
                view = inflater.inflate(R.layout.small_fragment_friends_requests, container, false);
                LV_requests = view.findViewById(R.id.smallFragmentFriendsRequests_LV);
                TV_no_requests = view.findViewById(R.id.smallFragmentFriendsRequests_TV_noRequests);
                PB_loading_requests = view.findViewById(R.id.smallFragmentFriendsRequests_PB);

                requestsAdapter = new FriendRequestsAdapter(list_requests, getContext());
                LV_requests.setAdapter(requestsAdapter);

                PB_loading_requests.setVisibility(View.VISIBLE);
                TV_no_requests.setVisibility(View.GONE);

                socket.on("get_my_friend_request", OnGetMyFriendRequest);

                json = new JSONObject();
                try {
                    json.put("nick", MainActivity.NickName);
                    json.put("session_id", MainActivity.Session_id);
                    json.put("request_type", "my");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                socket.emit("get_list_of_friend_requests", json);
                Log.d("kkk", "Socket_отправка - get_list_of_friend_requests - "+ json.toString());

                break;
        }
        return view;
    }
}