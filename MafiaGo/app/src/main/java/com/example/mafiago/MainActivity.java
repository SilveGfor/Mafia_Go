  package com.example.mafiago;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.example.mafiago.adapters.PrivateMessagesAdapter;
import com.example.mafiago.fragments.CreateRoomFragment;
import com.example.mafiago.fragments.GameFragment;
import com.example.mafiago.fragments.GamesListFragment;
import com.example.mafiago.fragments.MenuFragment;
import com.example.mafiago.fragments.StartFragment;
import com.example.mafiago.models.PrivateMessageModel;

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
    public static String User_id = "";
    public static String User_id_2 = "";
    public static String Sid = "";
    public static int Game_id;

    public static String url = "http://82.148.17.116:5000";

    public static String password = "";
    public static String nick = "";

    // Идентификатор уведомления
    private static final int NOTIFY_ID = 101;

    // Идентификатор канала
    private static String CHANNEL_ID = "Notifications channel";

    NotificationCompat.Builder builder;
    NotificationManager manager;

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
        socket = IO.socket(URI.create(url), options); //главный namespace
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        socket.connect();

        client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).callTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();


        socket.on("connect", onConnect);
        socket.on("disconnect", onDisconnect);
        socket.on("ping", onPing);
        socket.on("chat_message", OnChatMessage);

        //TODO: Фоновый режим

        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new StartFragment()).commit();
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

    private Emitter.Listener OnChatMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("kkk", "принял - chat_message в MainActivity - " + data);
            String nick = "", message = "", status = "", edited_time = "", time = "";
            int link = -1;

            try {
                nick = data.getString("nick");
                message = data.getString("message");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            createNotificationChannel();
            createNotification("Новое сообщение!", nick + " написал вам новое сообщение!");
            showNotification();
        }
    };

    private Emitter.Listener onPing = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
                    //Log.d("kkk", "PING - " + args[0]);
        }
    };

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String CHANNEL_NAME = "MAFIAGOCHANNEL" ;
            String CHANNEL_DESCRIPTION = "MAFIAGOGAME";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel =
                    new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            channel.setDescription(CHANNEL_DESCRIPTION);
            manager.createNotificationChannel(channel);
        }
    }

    private void createNotification(String title, String message) {
        builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.mafiago)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.drawable.mafiago)) // большая картинка
                .setAutoCancel(true); // автоматически закрыть уведомление после нажатия
    }

    private  void showNotification() {
        manager.notify(NOTIFY_ID, builder.build());
    }

    private void hideNotification() {
        manager.cancel(NOTIFY_ID);
    }
}


