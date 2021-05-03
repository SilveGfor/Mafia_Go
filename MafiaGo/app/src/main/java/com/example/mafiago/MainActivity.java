package com.example.mafiago;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.example.mafiago.fragments.CreateRoomFragment;
import com.example.mafiago.fragments.GameFragment;
import com.example.mafiago.fragments.GamesListFragment;
import com.example.mafiago.fragments.MenuFragment;
import com.example.mafiago.fragments.StartFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class MainActivity extends AppCompatActivity {


    public static String NickName = "";
    public static String Session_id = "";
    public static int Game_id;
    public static String url = "1d19e08842f0d6.localhost.run";

    public static String password = "";
    public static String nick = "";
/*
    public static Socket socket;
    {
        try{
            IO.Options options = IO.Options.builder()
                    .setReconnection(false)
                    .build();
            socket = IO.socket(URI.create("https://" + MainActivity.url), options); // the main namespace
            //socket = IO.socket("https://" + MainActivity.url);

        }catch (URISyntaxException e){
            throw new RuntimeException();
        }
    }
 */

public static Socket socket;
    {
        IO.Options options = IO.Options.builder()
                .setReconnectionDelay(0)
                .build();
        socket = IO.socket(URI.create("https://" + MainActivity.url), options); // the main namespace
        //socket = IO.socket("https://" + MainActivity.url);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        socket.connect();

        MainActivity.SocketTask socketTask = new SocketTask();
        socketTask.execute();

        getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new StartFragment()).commit();
    }

    class SocketTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("kkk", "onPreExecute");
        }

        @Override
        protected Void doInBackground(Void... voids) {

            //socket.on("connect", onConnect);
            //socket.on("disconnect", onDisconnect);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d("kkk", "onPostExecute");
        }
    }
/*
    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(this == null)
                return;
            Activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                        final JSONObject json2 = new JSONObject();
                        try {
                            json2.put("nick", NickName);
                            json2.put("session_id", Session_id);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        socket.emit("connect_to_room", json2);
                        Log.d("kkk", "CONNECT");
                }
            });
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(this == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("kkk", "DISCONNECT");
                }
            });
        }
    };
 */
}


