  package com.example.mafiago;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
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
import java.util.concurrent.TimeUnit;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity {

    public static OkHttpClient client;
    public static String NickName = "";
    public static String Session_id = "";
    public static String RoomName = "";
    public static int Game_id;

    public static String url = "http://82.148.17.116:5000";

    public static String password = "";
    public static String nick = "";

    // Идентификатор уведомления
    private static final int NOTIFY_ID = 101;

    // Идентификатор канала
    private static String CHANNEL_ID = "Notifications channel";

public static Socket socket;
    {
        IO.Options options = IO.Options.builder()
                .setReconnection(true)
                .setReconnectionAttempts(Integer.MAX_VALUE)
                .setReconnectionDelay(1_000)
                .setReconnectionDelayMax(3_000)
                .setRandomizationFactor(0.5)
                .setTimeout(20_000)
                .build();
        socket = IO.socket(URI.create(url), options); // the main namespace
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        socket.connect();

        client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).callTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        MainActivity.SocketTask socketTask = new SocketTask();
        socketTask.execute();


        //TODO: Сделать уведомления
        /*
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(MainActivity.this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.doctor_alive)
                        .setContentTitle("Напоминание")
                        .setContentText("Пора покормить кота")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(MainActivity.this);
        notificationManager.notify(NOTIFY_ID, builder.build());
        */

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

            socket.on("connect", onConnect);
            socket.on("disconnect", onDisconnect);
            socket.on("ping", onPing);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d("kkk", "onPostExecute");
        }
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
                        final JSONObject json2 = new JSONObject();
                        try {
                            json2.put("nick", NickName);
                            json2.put("session_id", Session_id);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        socket.emit("connection", json2);
                        Log.d("kkk", "CONNECTION");
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
                    Log.d("kkk", "DISCONNECTION");
        }
    };

    private Emitter.Listener onPing = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
                    //Log.d("kkk", "PING - " + args[0]);
        }
    };

}


