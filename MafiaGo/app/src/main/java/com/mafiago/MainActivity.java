  package com.mafiago;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.example.mafiago.R;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.security.ProviderInstaller;
import com.mafiago.classes.BackgroundTask;
import com.mafiago.classes.OnBackPressedListener;
import com.mafiago.fragments.StartFragment;
import com.mafiago.models.NotificationModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;

  public class MainActivity extends AppCompatActivity {

    static String str = "йцукенгшщзхъфывапролджэячсмитьбюёЙЦУКЕНГШЩЗХЪФЫВАПРОЛДЖЭЯЧСМИТЬБЮЁ\n" +
            "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM\n" +
            "ґєіїЇІЄҐ\n" +
            "ўЎ\n" +
            "ćčłńśšŭźžĆČŁŃŚŠŬŹŽ\n" +
            "äğñöşūüáǵúóýıÄĞÑÖŞŪÜÁǴÓÚÝIİ";
    public static char[] f = str.toCharArray();
    public static OkHttpClient client;
    public static String NickName = "";
    public static String NickName_2 = "";
    public static String Session_id = "";
    public static String RoomName = "";
    public static String User_id = "";
    public static String User_id_2 = "";
    public static String Sid = "";
    public static String Role = "";
    public static int Game_id;
    public static Bitmap bitmap_avatar_2;
    public static String CURRENT_GAME_VERSION = "0.0.1";
    public static JSONObject USERS = new JSONObject();

    public static String url = "https://mafiagoserver.online:5000";

    public static String password = "";
    public static String nick = "";

    ArrayList<NotificationModel> notifications = new ArrayList<>();

    // Идентификатор уведомления
    //private static final int NOTIFY_ID = 101;

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

        /*
        try {
            ProviderInstaller.installIfNeeded(this);
        } catch (GooglePlayServicesRepairableException e) {
            // Thrown when Google Play Services is not installed, up-to-date, or enabled
            // Show dialog to allow users to install, update, or otherwise enable Google Play services.
            GooglePlayServicesUtil.getErrorDialog(e.getConnectionStatusCode(), this, 0);
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.e("SecurityException", "Google Play Services not available.");
            }
         */

        ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
                .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_1, TlsVersion.TLS_1_0)
                .cipherSuites(
                        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256,
                        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
                        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA)
                .build();

        client = new OkHttpClient.Builder().//connectionSpecs(Collections.singletonList(spec)).
                connectTimeout(30, TimeUnit.SECONDS).callTimeout(30, TimeUnit.SECONDS).
                readTimeout(30, TimeUnit.SECONDS).build();

        this.startService(new Intent(this, BackgroundTask.class));

        //socket.on("connect", onConnect);
        //socket.on("disconnect", onDisconnect);
        //socket.on("ping", onPing);
        //socket.on("chat_message", OnChatMessage);

        //TODO: Фоновый режим

        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        createNotificationChannel();

        getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new StartFragment()).commit();
    }

    @Override
    protected void onDestroy() {
        socket.emit("leave_app", "");
        Log.d("kkk", "Socket_отправка - leave_app");
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        socket.emit("leave_app", "");
        Log.d("kkk", "Socket_отправка - leave_app");
        super.onPause();
    }

    @Override
    protected void onResume() {
        final JSONObject json2 = new JSONObject();
        try {
            json2.put("nick", NickName);
            json2.put("session_id", Session_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("connection", json2);
        Log.d("kkk", "CONNECTION");
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        OnBackPressedListener backPressedListener = null;
        for (Fragment fragment: fm.getFragments()) {
            if (fragment instanceof  OnBackPressedListener) {
                backPressedListener = (OnBackPressedListener) fragment;
                break;
            }
        }

        if (backPressedListener != null) {
            backPressedListener.onBackPressed();
        } else {
            super.onBackPressed();
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

    private Emitter.Listener OnChatMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("kkk", "принял - chat_message в MainActivity - " + data);
            String nick = "", message = "", status = "", edited_time = "", time = "", user_id_2 = "";
            int link = -1;
            int id = 101;

            try {
                nick = data.getString("nick");
                message = data.getString("message");
                user_id_2 = data.getString("user_id");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (!nick.equals(NickName) && !user_id_2.equals(User_id_2)) {
                createNotificationChannel();
                createNotification(nick, message);

                ArrayList<String> messages = null;

                for (int i = 0; i < notifications.size(); i++) {
                    if (notifications.get(i).nick.equals(nick)) {
                        messages = notifications.get(i).messages;
                        id = i;
                        notifications.get(i).messages.add(message);
                        if (messages.size() == 1)
                            builder.setStyle(new NotificationCompat.InboxStyle()
                                    .addLine(messages.get(0)));
                        else if (messages.size() == 2)
                            builder.setStyle(new NotificationCompat.InboxStyle()
                                    .addLine(messages.get(0)).addLine(messages.get(1)));
                        else if (messages.size() == 3)
                            builder.setStyle(new NotificationCompat.InboxStyle()
                                    .addLine(messages.get(0)).addLine(messages.get(1))
                                    .addLine(messages.get(2)));
                        else if (messages.size() == 4)
                            builder.setStyle(new NotificationCompat.InboxStyle()
                                    .addLine(messages.get(0)).addLine(messages.get(1))
                                    .addLine(messages.get(2)).addLine(messages.get(3)));
                        else if (messages.size() > 4)
                            builder.setStyle(new NotificationCompat.InboxStyle()
                                    .addLine(messages.get(0)).addLine(messages.get(1))
                                    .addLine(messages.get(2)).addLine(messages.get(3))
                                    .setSummaryText("+" + (messages.size() - 4) + " more"));
                        break;
                    }
                }
                if (messages == null) {
                    messages = new ArrayList<>();
                    messages.add(message);
                    id = notifications.size();
                    notifications.add(new NotificationModel(nick, messages));
                    builder.setStyle(new NotificationCompat.InboxStyle()
                            .addLine(messages.get(0)));
                }
                showNotification(id);
            }
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

    private  void showNotification(int NOTIFY_ID) {
        manager.notify(NOTIFY_ID, builder.build());
    }

    private void hideNotification(int NOTIFY_ID) {
        manager.cancel(NOTIFY_ID);
    }

    public boolean isOnline() {
        String cs = Context.CONNECTIVITY_SERVICE;
        ConnectivityManager cm = (ConnectivityManager) getSystemService(cs);
        if (cm.getActiveNetworkInfo() == null) {
            return false;
        } else {
            return true;
        }
    }
/*
    char[] nick = ETnick.getText().toString().toCharArray();
    int flag = 0;
                        for (int i = 0; i < nick.length; i ++)
    {
        if (Character.isLetter(nick[i])) {
            for (int j = 0; j < f.length; j++) {
                if (nick[i] == f[j]) {
                    flag = 1;
                }
            }
            if (flag != 1) {
                Log.d("kkk", "Не тот символ!!!  " + nick[i]);
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Недопустимый символ - " + nick[i])
                        .setMessage("")
                        .setIcon(R.drawable.ic_error)
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
            flag = 0;
        }
    }

 */
}


