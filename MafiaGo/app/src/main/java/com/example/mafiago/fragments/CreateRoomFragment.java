package com.example.mafiago.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.example.mafiago.MainActivity;
import com.example.mafiago.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class CreateRoomFragment extends Fragment {

    EditText ET_RoomName;
    EditText ET_MinPeople;
    EditText ET_MaxPeople;

    Button btnCreateRoom;



    private Socket socket;
    {
        try{
            socket = IO.socket("https://" + MainActivity.url);
        }catch (URISyntaxException e){
            throw new RuntimeException();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_room, container, false);

        ET_RoomName = view.findViewById(R.id.ETRoomName);
        ET_MinPeople = view.findViewById(R.id.ETMinPeople);
        ET_MaxPeople = view.findViewById(R.id.ETMaxPeople);

        btnCreateRoom = view.findViewById(R.id.btnCreate);

        socket.connect();

        socket.on("create_room", onCreateRoom);

        btnCreateRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final JSONObject json = new JSONObject();
                try {
                    json.put("nick", MainActivity.NickName);
                    json.put("session_id", MainActivity.Session_id);
                    json.put("name", ET_RoomName.getText());
                    json.put("min_people_num", ET_MinPeople.getText());
                    json.put("max_people_num", ET_MaxPeople.getText());
                    json.put("roles", "{peaceful: [], mafia: []}");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                socket.emit("create_room", json);
                Log.d("kkk", "Socket_отправка - create_room - "+ json.toString());
            }
        });




        return view;
    }

    private Emitter.Listener onCreateRoom = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        MainActivity.Game_id = data.getInt("room_num");
                        Log.d("kkk", "Принял - create_room: " + MainActivity.Game_id);
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GameFragment()).commit();
                    } catch (JSONException e) {
                        return;
                    }
                }
            });
        }
    };

}