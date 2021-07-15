package com.mafiago.fragments;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.mafiago.MainActivity;
import com.example.mafiago.R;
import com.mafiago.classes.OnBackPressedListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.NOTIFICATION_SERVICE;
import static android.content.Context.SYSTEM_HEALTH_SERVICE;
import static com.mafiago.MainActivity.client;

public class StartFragment extends Fragment {

    private static final String url= MainActivity.url + "/login";

    //кнопки
    Button btnSignIn;
    Button btnReg;

    ProgressBar PB_loading;

    EditText ETemail;
    EditText ETpassword;

    String NickName = "";
    String Email = "";
    String Session_id = "";

    Boolean BadLogin = false;
    Boolean BadPassword = false;
    Boolean AutoRun = false;

    public static final String APP_PREFERENCES = "user";
    public static final String APP_PREFERENCES_EMAIL = "email";
    public static final String APP_PREFERENCES_PASSWORD = "password";
    public static final String APP_PREFERENCES_NICKNAME = "nickname";

    private SharedPreferences mSettings;

    public int друг_айди = 13;

    // Идентификатор канала
    private static String CHANNEL_ID = "Notifications channel";

    NotificationCompat.Builder builder;
    NotificationManager manager;

    public int h;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view;
        view = inflater.inflate(R.layout.fragment_start, container, false);
        //поиск айди кнопок
        btnSignIn = view.findViewById(R.id.btnSignIn);
        btnReg = view.findViewById(R.id.btnReg);
        ETemail = view.findViewById(R.id.ETemail);
        ETpassword = view.findViewById(R.id.ETpassword);
        PB_loading = view.findViewById(R.id.fragmentStart_PB_loading);

        PB_loading.setVisibility(View.INVISIBLE);

        manager = (NotificationManager) getActivity().getSystemService(NOTIFICATION_SERVICE);

        createNotificationChannel();

        //MainActivity.mPlayer= MediaPlayer.create(getContext(), R.raw.fon_music);

        mSettings = getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        //if (mSettings.contains(APP_PREFERENCES_EMAIL) && mSettings.contains(APP_PREFERENCES_PASSWORD)) {
        if (!mSettings.getString(APP_PREFERENCES_EMAIL, "").equals("")) {
            // Получаем значение из настроек
            String mEmail = mSettings.getString(APP_PREFERENCES_EMAIL, "");
            String mPassword = mSettings.getString(APP_PREFERENCES_PASSWORD, "");
            // Выводим на экран данные из настроек
            Log.d("kkk", "SharedPref mEmail - " + mEmail);
            Log.d("kkk", "SharedPref mPassword - " + mPassword);

            AutoRun = true;

            MainActivity.nick = mEmail;
            MainActivity.password = mPassword;

            Login();
        }
        else
        {
            Log.d("kkk", "SharedPref mEmail, mPassword - нет данных");
        }

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkOnline(getContext())) {
                    //final String[] userss = {ETemail.getText().toString(), ETpassword.getText().toString()};
                    MainActivity.nick = ETemail.getText().toString();
                    MainActivity.password = ETpassword.getText().toString();
                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putString(APP_PREFERENCES_EMAIL, String.valueOf(ETemail.getText()));
                    editor.putString(APP_PREFERENCES_PASSWORD, String.valueOf(ETpassword.getText()));
                    editor.apply();
                    if (!ETpassword.getText().toString().equals("") && !ETemail.getText().toString().equals("")) {
                        AutoRun = false;
                        Login();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Заполните все поля!")
                                .setMessage("")
                                .setIcon(R.drawable.ic_info)
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
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("У вас нет подключения к интернету!")
                            .setMessage("")
                            .setIcon(R.drawable.ic_ban)
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
            }
        });

        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new RegisterFragment()).commit();
            }
        });
        return view;
    }

    public void Login() {
        try {
            final JSONObject json = new JSONObject();
            PB_loading.setVisibility(View.VISIBLE);
            final String[] resp = {""};
            SharedPreferences mSettings;
            mSettings = getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

            json.put("email", MainActivity.nick);
            json.put("password", MainActivity.password);

            Log.d("kkk", "Отправил: " + json);

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"), String.valueOf(json));

            Request request = new Request.Builder()
                    .url(url).post(body)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                    Log.d("kkk", "Failure: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    resp[0] = response.body().string();
                    String Answer = resp[0];
                    Log.d("kkk", "Принял: " + Answer);
                    try {
                        if (Answer.equals("incorrect_email")) {
                            if (!AutoRun) {
                                PB_loading.setVisibility(View.INVISIBLE);
                                ContextCompat.getMainExecutor(getContext()).execute(()  -> {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                    builder.setTitle("Такого аккаунта не существует!")
                                            .setMessage("")
                                            .setIcon(R.drawable.ic_info)
                                            .setCancelable(false)
                                            .setNegativeButton("Ок",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                        }
                                                    });
                                    AlertDialog alert = builder.create();
                                    alert.show();
                                });
                            }
                        } else if (Answer.equals("incorrect_password")) {
                            if (!AutoRun) {
                                PB_loading.setVisibility(View.INVISIBLE);
                                ContextCompat.getMainExecutor(getContext()).execute(()  -> {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                    builder.setTitle("Неправильный пароль!")
                                            .setMessage("")
                                            .setIcon(R.drawable.ic_info)
                                            .setCancelable(false)
                                            .setNegativeButton("Ок",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                        }
                                                    });
                                    AlertDialog alert = builder.create();
                                    alert.show();
                                });
                            }
                        } else {
                            JSONObject obj = new JSONObject(resp[0]);

                            NickName = obj.get("nick").toString();
                            Email = obj.get("email").toString();
                            Session_id = obj.get("session_id").toString();
                            MainActivity.User_id = obj.get("user_id").toString();
                            MainActivity.Sid = obj.get("sid").toString();
                            MainActivity.Role = obj.get("role").toString();

                            MainActivity.NickName = NickName;
                            MainActivity.Session_id = Session_id;

                            Log.d("kkk", "НИК: " + NickName);
                            Log.d("kkk", "Переход в MenuFragment");
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new MenuFragment()).commit();
                        }
                    } catch (Exception e) {
                        createNotificationChannel();
                        createNotification("Ошибка", "message");
                        builder.setStyle(new NotificationCompat.InboxStyle()
                                .addLine(String.valueOf(e)));
                        showNotification(друг_айди);
                        друг_айди++;
                    }
                }
            });
        } catch (Exception e) {
            createNotificationChannel();
            createNotification("Ошибка", "message");
            builder.setStyle(new NotificationCompat.InboxStyle()
                    .addLine(String.valueOf(e)));
            showNotification(друг_айди);
            друг_айди++;
        }
    }


    public boolean isNetworkOnline(Context context) {
        boolean status = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getNetworkInfo(0);
            if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
                status = true;
            } else {
                netInfo = cm.getNetworkInfo(1);
                if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED)
                    status = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return status;
    }
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
        builder = new NotificationCompat.Builder(getActivity(), CHANNEL_ID)
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
}