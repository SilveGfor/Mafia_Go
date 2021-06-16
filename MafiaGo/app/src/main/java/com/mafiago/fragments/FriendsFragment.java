package com.mafiago.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.mafiago.MainActivity;
import com.example.mafiago.R;
import com.mafiago.adapters.FriendsAdapter;
import com.mafiago.classes.OnBackPressedListener;
import com.mafiago.models.FriendModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.emitter.Emitter;

import static com.mafiago.MainActivity.socket;

public class FriendsFragment extends Fragment implements OnBackPressedListener {

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

        JSONObject json = new JSONObject();
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

        socket.off("add_chat_to_list_of_chats");

        socket.on("add_chat_to_list_of_chats", OnAddChatToListOfChats);

        friendsView.setOnItemClickListener((parent, view1, position, id) -> {
            MainActivity.User_id_2 = list_friends.get(position).getUser_id_2();
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new PrivateChatFragment()).commit();
        });

        btnExit.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new MenuFragment()).commit();
        });


        return view;
    }

    @Override
    public void onBackPressed() {
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new MenuFragment()).commit();
    }

    private final Emitter.Listener OnAddChatToListOfChats = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject data = (JSONObject) args[0];
                Log.d("kkk", "принял - get_list_of_chats - " + data);
                String nick = "", user_id_1 = "", user_id_2 = "", message = "";
                boolean online = false;
                try {
                    JSONArray user_ids = data.getJSONArray("user_ids");
                    user_id_1 = user_ids.getString(0);
                    user_id_2 = user_ids.getString(1);

                    JSONObject user_nicks = data.getJSONObject("user_nicks");
                    if (!user_id_1.equals(MainActivity.User_id))
                    {
                        String test_id = user_id_1;
                        user_id_1 = user_id_2;
                        user_id_2 = test_id;
                    }
                    nick = user_nicks.getString(user_id_2);

                    JSONObject last_message = data.getJSONObject("last_message");
                    message = last_message.getString("message");

                    JSONObject blocked = data.getJSONObject("is_blocked");

                    list_friends.add(new FriendModel(nick, message, user_id_2, true));
                    FriendsAdapter customList = new FriendsAdapter(list_friends, getContext());
                    friendsView.setAdapter(customList);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });
    };
}