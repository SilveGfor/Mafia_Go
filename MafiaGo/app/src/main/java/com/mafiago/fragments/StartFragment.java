package com.mafiago.fragments;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.mafiago.MainActivity;
import com.example.mafiago.R;
import com.mafiago.adapters.PlayersAdapter;
import com.mafiago.enums.Role;
import com.mafiago.models.FineModel;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.mafiago.MainActivity.client;
import static com.mafiago.MainActivity.socket;

public class StartFragment extends Fragment {

    private static final String url= MainActivity.url + "/login";

    //кнопки
    Button btnSignIn;
    Button btnReg;

    ProgressBar PB_loading;

    RelativeLayout RL_back;

    ShimmerTextView STV_text;

    EditText ETemail;
    EditText ETpassword;

    TextView TV_changePassword;

    String NickName = "";
    String Email = "";
    String Session_id = "";

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
        btnSignIn = view.findViewById(R.id.fragmentStart_btn_enter);
        btnReg = view.findViewById(R.id.fragmentStart_btn_register);
        ETemail = view.findViewById(R.id.fragmentRegister_ET_email);
        ETpassword = view.findViewById(R.id.fragmentStart_ET_password);
        PB_loading = view.findViewById(R.id.fragmentStart_PB);
        TV_changePassword = view.findViewById(R.id.fragmentStart_TV_forgotPassword);
        RL_back = view.findViewById(R.id.fragmentGamesList_RL_back);
        STV_text = view.findViewById(R.id.fragmentStart_TV_text);

        Shimmer shimmer = new Shimmer();
        shimmer.start(STV_text);

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

            Login(container);
        }
        else
        {
            Log.d("kkk", "SharedPref mEmail, mPassword - нет данных");
        }

        RL_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finishAffinity();
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkOnline(getContext())) {
                    MainActivity.nick = ETemail.getText().toString();
                    MainActivity.password = ETpassword.getText().toString();
                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putString(APP_PREFERENCES_EMAIL, String.valueOf(ETemail.getText()));
                    editor.putString(APP_PREFERENCES_PASSWORD, String.valueOf(ETpassword.getText()));
                    editor.apply();
                    if (!ETpassword.getText().toString().equals("") && !ETemail.getText().toString().equals("")) {
                        AutoRun = false;
                        Login(container);
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        View viewDang = getLayoutInflater().inflate(R.layout.dialog_error, null);
                        builder.setView(viewDang);
                        TextView TV_title = viewDang.findViewById(R.id.dialogError_TV_errorTitle);
                        TextView TV_error = viewDang.findViewById(R.id.dialogError_TV_errorText);
                        TV_title.setText("Не все поля заполнены!");
                        TV_error.setText("Вы должны заполнить все поля");
                        AlertDialog alert = builder.create();
                        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        alert.show();
                    }
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    View viewDang = getLayoutInflater().inflate(R.layout.dialog_error, null);
                    builder.setView(viewDang);
                    TextView TV_title = viewDang.findViewById(R.id.dialogError_TV_errorTitle);
                    TextView TV_error = viewDang.findViewById(R.id.dialogError_TV_errorText);
                    TV_title.setText("Нет подключения к интернету!");
                    TV_error.setText("Проверьте соединение сети");
                    AlertDialog alert = builder.create();
                    alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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

        TV_changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new ChangePasswordFragment()).commit();
            }
        });
        return view;
    }

    public void Login(ViewGroup container) {
        try {
            final JSONObject json = new JSONObject();
            PB_loading.setVisibility(View.VISIBLE);
            final String[] resp = {""};
            SharedPreferences mSettings;
            mSettings = getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

            json.put("email", MainActivity.nick);
            json.put("password", MainActivity.password);
            json.put("current_game_version", MainActivity.CURRENT_GAME_VERSION);

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
                        switch (Answer) {
                            case "incorrect_email":
                                if (!AutoRun) {
                                    ContextCompat.getMainExecutor(getContext()).execute(() -> {
                                        PB_loading.setVisibility(View.INVISIBLE);
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                        View viewDang = getLayoutInflater().inflate(R.layout.dialog_error, null);
                                        builder.setView(viewDang);
                                        TextView TV_title = viewDang.findViewById(R.id.dialogError_TV_errorTitle);
                                        TextView TV_error = viewDang.findViewById(R.id.dialogError_TV_errorText);
                                        TV_title.setText("Такого аккаунта не существует!");
                                        TV_error.setText("Возможно, вы указали неверный домен почты (например: @mail.ru вместо @gmail.com) или ошиблись в написании почти");
                                        AlertDialog alert = builder.create();
                                        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                        alert.show();
                                    });
                                }
                                break;
                            case "incorrect_password":
                                if (!AutoRun) {
                                    ContextCompat.getMainExecutor(getContext()).execute(() -> {
                                        PB_loading.setVisibility(View.INVISIBLE);
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                        View viewDang = getLayoutInflater().inflate(R.layout.dialog_error, null);
                                        builder.setView(viewDang);
                                        TextView TV_title = viewDang.findViewById(R.id.dialogError_TV_errorTitle);
                                        TextView TV_error = viewDang.findViewById(R.id.dialogError_TV_errorText);
                                        TV_title.setText("Неправильный пароль!");
                                        TV_error.setText("Если вы забыли пароль, то его всегда можно восстановить по вашей почте");
                                        AlertDialog alert = builder.create();
                                        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                        alert.show();
                                    });
                                }
                                break;
                            case "incorrect_game_version":
                                    ContextCompat.getMainExecutor(getContext()).execute(() -> {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                        View viewDang = getLayoutInflater().inflate(R.layout.dialog_information, null);
                                        builder.setView(viewDang);
                                        TextView TV_title = viewDang.findViewById(R.id.dialogInformation_TV_title);
                                        TextView TV_text = viewDang.findViewById(R.id.dialogInformation_TV_text);
                                        TV_title.setText("Обновите игру!");
                                        TV_text.setText("Уже доступно новое обновление");
                                        AlertDialog alert = builder.create();
                                        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                        alert.show();
                                    });
                                    break;
                            default:
                                JSONObject data = new JSONObject(resp[0]);
                                if (data.has("session_id"))
                                {
                                    NickName = data.get("nick").toString();
                                    Email = data.get("email").toString();
                                    Session_id = data.get("session_id").toString();
                                    MainActivity.User_id = data.get("user_id").toString();
                                    MainActivity.Sid = data.get("sid").toString();
                                    MainActivity.Role = data.get("role").toString();
                                    MainActivity.Rang = data.getInt("rang");
                                    MainActivity.MyInviteCode = data.getInt("my_invite_code");
                                    if (data.getString("avatar") == null || data.getString("avatar").equals("") || data.getString("avatar").equals("null")) {
                                        ContextCompat.getMainExecutor(getContext()).execute(() -> {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                            View viewDang = getLayoutInflater().inflate(R.layout.dialog_error, null);
                                            builder.setView(viewDang);
                                            TextView TV_title = viewDang.findViewById(R.id.dialogError_TV_errorTitle);
                                            TextView TV_error = viewDang.findViewById(R.id.dialogError_TV_errorText);
                                            TV_title.setText("Ух ты!");
                                            TV_error.setText("Вы не поставили аватарку. Её можно установить в настройках");
                                            AlertDialog alert = builder.create();
                                            alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                            alert.show();
                                        });
                                    }

                                    MainActivity.NickName = NickName;
                                    MainActivity.Session_id = Session_id;
                                    MainActivity.onResume = true;
                                    final JSONObject json2 = new JSONObject();
                                    try {
                                        json2.put("nick", NickName);
                                        json2.put("session_id", Session_id);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    socket.emit("connection", json2);
                                    Log.d("kkk", "CONNECTION after Login");
                                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new MenuFragment()).commit();
                                }
                                else {
                                    if (!data.getString("ban_time").equals("forever")) {
                                        JSONObject fine = data.getJSONObject("fine");
                                        JSONObject dataBanTime = data.getJSONObject("ban_time");

                                        String admin_comment;
                                        String creation_time;
                                        int exp = 0;
                                        int hour;
                                        int money = 0;
                                        String reason;

                                        int days, hours, minutes, seconds;

                                        admin_comment = fine.getString("admin_comment");
                                        creation_time = fine.getString("creation_time");
                                        if (fine.has("exp")) exp = fine.getInt("exp");
                                        if (fine.has("money")) money = fine.getInt("money");
                                        hour = fine.getInt("hour");
                                        reason = fine.getString("reason");

                                        days = dataBanTime.getInt("days");
                                        hours = dataBanTime.getInt("hours");
                                        minutes = dataBanTime.getInt("minutes");
                                        seconds = dataBanTime.getInt("seconds");


                                        int finalMoney = money;
                                        int finalExp = exp;
                                        ContextCompat.getMainExecutor(getContext()).execute(() -> {
                                            PB_loading.setVisibility(View.INVISIBLE);
                                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                            View viewBan = getLayoutInflater().inflate(R.layout.dialog_you_have_been_banned, container, false);
                                            builder.setView(viewBan);
                                            AlertDialog alert = builder.create();
                                            alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                                            TextView TV_reason = viewBan.findViewById(R.id.dialogYouHaveBeenBanned_TV_reason);
                                            TextView TV_comment = viewBan.findViewById(R.id.dialogYouHaveBeenBanned_TV_comment);
                                            TextView TV_banTime = viewBan.findViewById(R.id.dialogYouHaveBeenBanned_TV_banTime);
                                            TextView TV_timeYouMustWait = viewBan.findViewById(R.id.dialogYouHaveBeenBanned_TV_timeYouMustWait);
                                            TextView IV_money = viewBan.findViewById(R.id.dialogYouHaveBeenBanned_TV_money);
                                            TextView IV_exp = viewBan.findViewById(R.id.dialogYouHaveBeenBanned_TV_exp);

                                            TV_reason.setText("Причина: " + reason);
                                            TV_comment.setText("Комментарий: " + admin_comment);
                                            TV_banTime.setText("Время бана: " + hour + "ч");
                                            IV_money.setText(String.valueOf(finalMoney));
                                            IV_exp.setText(String.valueOf(finalExp));

                                            if (days == 0) {
                                                if (hours == 0) {
                                                    if (minutes == 0) {
                                                        TV_timeYouMustWait.setText("Вы сможете зайти через " + seconds + " с");
                                                    } else {
                                                        TV_timeYouMustWait.setText("Вы сможете зайти через " + minutes + " м " + seconds + " с");
                                                    }
                                                } else {
                                                    TV_timeYouMustWait.setText("Вы сможете зайти через " + hours + " ч " + minutes + " м");
                                                }

                                            } else {
                                                TV_timeYouMustWait.setText("Вы сможете зайти через " + days + " д " + hours + " ч");
                                            }


                                            alert.show();
                                        });
                                    }
                                    else
                                    {
                                        ContextCompat.getMainExecutor(getContext()).execute(() -> {
                                            PB_loading.setVisibility(View.INVISIBLE);
                                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                            View viewBan = getLayoutInflater().inflate(R.layout.dialog_you_have_been_banned, container, false);
                                            builder.setView(viewBan);
                                            AlertDialog alert = builder.create();
                                            alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                                            TextView TV_reason = viewBan.findViewById(R.id.dialogYouHaveBeenBanned_TV_reason);
                                            TextView TV_comment = viewBan.findViewById(R.id.dialogYouHaveBeenBanned_TV_comment);
                                            TextView TV_banTime = viewBan.findViewById(R.id.dialogYouHaveBeenBanned_TV_banTime);
                                            TextView TV_timeYouMustWait = viewBan.findViewById(R.id.dialogYouHaveBeenBanned_TV_timeYouMustWait);
                                            TextView IV_money = viewBan.findViewById(R.id.dialogYouHaveBeenBanned_TV_money);
                                            TextView IV_exp = viewBan.findViewById(R.id.dialogYouHaveBeenBanned_TV_exp);

                                            try {
                                                if (data.getJSONObject("fine").has("reason"))
                                                {
                                                    JSONObject fine = data.getJSONObject("fine");

                                                    String admin_comment;
                                                    String creation_time;
                                                    int exp = 0;
                                                    int hour;
                                                    int money = 0;
                                                    String reason;

                                                    int days, hours, minutes, seconds;

                                                    admin_comment = fine.getString("admin_comment");
                                                    creation_time = fine.getString("creation_time");
                                                    if (fine.has("exp")) exp = fine.getInt("exp");
                                                    if (fine.has("money")) money = fine.getInt("money");
                                                    if (!fine.getString("hour").equals("forever"))
                                                    {
                                                        hour = fine.getInt("hour");
                                                        TV_banTime.setText("Время бана: " + hour + "ч");
                                                    }
                                                    else
                                                    {
                                                        TV_banTime.setText("Время бана: ВЕЧНОСТЬ");
                                                    }

                                                    reason = fine.getString("reason");

                                                    TV_reason.setText("Причина: " + reason);
                                                    TV_comment.setText("Комментарий: " + admin_comment);

                                                    IV_money.setText(String.valueOf(money));
                                                    IV_exp.setText(String.valueOf(exp));
                                                }
                                                else
                                                {
                                                    TV_reason.setText("Причина: Разработчик игры решил вас забанить!");
                                                    TV_comment.setText("Комментарий: Если вас забанил разработчик, то вы сделали что-то очень плохое, так что подумайте над своим поведением!");
                                                    TV_banTime.setText("Время бана: ВЕЧНОСТЬ");
                                                    IV_money.setText(String.valueOf(0));
                                                    IV_exp.setText(String.valueOf(0));
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }

                                            TV_timeYouMustWait.setText("Вы забанены навсегда!");



                                            alert.show();
                                        });
                                    }
                                }
                                break;
                        }
                    } catch (Exception e) {
                        /*
                        createNotificationChannel();
                        createNotification(String.valueOf(e.getStackTrace()[0]).substring(20), "message");
                        builder.setStyle(new NotificationCompat.InboxStyle()
                                .addLine(String.valueOf(e)));
                        showNotification(друг_айди);
                        друг_айди++;
                         */
                        Log.d("kkk", String.valueOf(e.getMessage()));
                        /*
                        createNotificationChannel();
                        createNotification(String.valueOf(e.getStackTrace()[0]).substring(41), "message");
                        builder.setStyle(new NotificationCompat.InboxStyle()
                                .addLine(String.valueOf(e.getMessage())));
                        showNotification(друг_айди);
                        друг_айди++;
                         */
                    }
                }
            });
        } catch (Exception e) {
            /*
            createNotificationChannel();
            createNotification(String.valueOf(e.getStackTrace()[0]).substring(20), "message");
            builder.setStyle(new NotificationCompat.InboxStyle()
                    .addLine(String.valueOf(e)));
            showNotification(друг_айди);
            друг_айди++;
             */

            /*
            createNotificationChannel();
            createNotification(String.valueOf(e.getStackTrace()[0]).substring(41), "message");
            builder.setStyle(new NotificationCompat.InboxStyle()
                    .addLine(String.valueOf(e.getMessage())));
            showNotification(друг_айди);
            друг_айди++;
             */
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