package com.example.mafiago.fragments;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.mafiago.MainActivity;
import com.example.mafiago.R;
import com.example.mafiago.adapters.FriendsAdapter;
import com.example.mafiago.adapters.GamesAdapter;
import com.example.mafiago.models.FriendModel;
import com.example.mafiago.models.RoomModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.romainpiel.shimmer.Shimmer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.emitter.Emitter;

import static com.example.mafiago.MainActivity.socket;

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

        final JSONObject json = new JSONObject();
        try {
            json.put("nick", MainActivity.NickName);
            json.put("session_id", MainActivity.Session_id);
            json.put("user_id", MainActivity.User_id);
            json.put("chat_type", "active");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("get_list_of_chats", json);
        Log.d("kkk", "Socket_отправка - get_list_of_chats - "+ json.toString());

        socket.on("get_list_of_chats", OnGetListOfChats);

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

    private final Emitter.Listener OnGetListOfChats = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject data = (JSONObject) args[0];
                String nick = "";
                boolean online = false;

                try {
                    online = data.getBoolean("is_online");
                    nick = data.getString("nick");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("kkk", "принял - get_profile - " + data);

            }
        });
    };
}