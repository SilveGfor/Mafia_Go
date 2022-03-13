package com.mafiago.classes;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.mafiago.R;
import com.mafiago.MainActivity;
import com.mafiago.models.NotificationModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.emitter.Emitter;

import static com.mafiago.MainActivity.NickName;
import static com.mafiago.MainActivity.Session_id;
import static com.mafiago.MainActivity.User_id_2;
import static com.mafiago.MainActivity.socket;

public class BackgroundTask extends Service {
    ArrayList<NotificationModel> notifications = new ArrayList<>();

    // Идентификатор уведомления
    //private static final int NOTIFY_ID = 101;

    public int друг_айди = 1024;

    public  int i = 0;
    // Идентификатор канала
    private static String CHANNEL_ID = "Notifications channel";

    NotificationCompat.Builder builder;
    NotificationManager manager;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flag, int startId) {
        //socket.on("connect", onConnect);
        //socket.on("disconnect", onDisconnect);
        socket.on("chat_message", OnChatMessage);
        socket.on("friend_request", OnFriendRequest);
        socket.on("accept_friend_request", OnAcceptFriendRequest);
        //socket.on("ping", onPing);
        socket.on("new_fine", onNewFine);

        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        createNotificationChannel();
        return START_STICKY;
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
            Log.d("kkk", "принял - chat_message в BackgroundTask - " + data);
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

    private Emitter.Listener OnFriendRequest = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("kkk", "принял - friend_request в BackgroundTask - " + data);
            String nick = "";
            try {
                nick = data.getString("nick");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            createNotificationChannel();
            createNotification("Заявка в друзья", "message");
            builder.setStyle(new NotificationCompat.InboxStyle()
                    .addLine(nick + " хочет добавить вас в друзья!"));
            showNotification(друг_айди);
            друг_айди++;
        }
    };

    private Emitter.Listener onNewFine = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("kkk", "принял - new_fine в BackgroundTask - " + data);
            String nick = "";
            String admin_comment = "";
            try {
                admin_comment = data.getString("admin_comment");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            createNotificationChannel();
            createNotification("Новый штраф!", "message");
            builder.setStyle(new NotificationCompat.InboxStyle()
                    .addLine(admin_comment));
            showNotification(друг_айди);
            друг_айди++;
        }
    };

    private Emitter.Listener OnAcceptFriendRequest = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("kkk", "принял - accept_friend_request в BackgroundTask - " + data);
            String nick = "";
            boolean accept = false;
            try {
                nick = data.getString("nick");
                accept = data.getBoolean("accept");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            createNotificationChannel();
            createNotification("Заявка в друзья", "message");
            if (accept)
            {
                builder.setStyle(new NotificationCompat.InboxStyle()
                        .addLine(nick + " принял(-а) вашу заявку в друзья!"));
            }
            else
            {
                builder.setStyle(new NotificationCompat.InboxStyle()
                        .addLine(nick + " отклонил(-а) вашу заявку в друзья!"));
            }
            showNotification(друг_айди);
            друг_айди++;
        }
    };

    private Emitter.Listener onPing = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.d("kkk", "PING2 - " + args[0]);
            /*
            createNotificationChannel();
            createNotification("PING", "ping");
            друг_айди++;
            builder.setStyle(new NotificationCompat.InboxStyle()
                    .addLine("pinGGG + " + друг_айди));
            showNotification(друг_айди);
            hideNotification(друг_айди - 1);
             */
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
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.mipmap.ic_launcher_circle)) // большая картинка
                .setAutoCancel(true); // автоматически закрыть уведомление после нажатия
    }

    private  void showNotification(int NOTIFY_ID) {
        manager.notify(NOTIFY_ID, builder.build());
    }

    private void hideNotification(int NOTIFY_ID) {
        manager.cancel(NOTIFY_ID);
    }
}
