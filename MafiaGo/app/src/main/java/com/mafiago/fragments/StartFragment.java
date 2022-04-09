package com.mafiago.fragments;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import static com.mafiago.MainActivity.f;
import static com.mafiago.MainActivity.socket;
import static com.mafiago.MainActivity.PORT_APK;

public class StartFragment extends Fragment {

    private String url = MainActivity.url + "/login";

    //кнопки
    Button btnSignIn;
    Button btnReg;

    ProgressBar PB_loading;

    RelativeLayout RL_back;

    ImageView IV_gameName;

    ShimmerTextView STV_text;

    EditText ET_email;
    EditText ET_password;

    TextView TV_changePassword;

    String NickName = "";
    String Email = "";
    String Session_id = "";

    Boolean AutoRun = false;

    public static final String APP_PREFERENCES = "user";
    public static final String APP_PREFERENCES_EMAIL = "email";
    public static final String APP_PREFERENCES_PASSWORD = "password";
    public static final String APP_PREFERENCES_PORT = "port";

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
        ET_email = view.findViewById(R.id.fragmentRegister_ET_email);
        ET_password = view.findViewById(R.id.fragmentStart_ET_password);
        PB_loading = view.findViewById(R.id.fragmentStart_PB);
        TV_changePassword = view.findViewById(R.id.fragmentStart_TV_forgotPassword);
        RL_back = view.findViewById(R.id.fragmentGamesList_RL_back);
        STV_text = view.findViewById(R.id.fragmentStart_TV_text);
        IV_gameName = view.findViewById(R.id.fragmentStart_gameName);

        Shimmer shimmer = new Shimmer();
        shimmer.start(STV_text);

        manager = (NotificationManager) getActivity().getSystemService(NOTIFICATION_SERVICE);

        createNotificationChannel();

        mSettings = getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        if (PORT_APK) {
            IV_gameName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    View viewChangeNick = inflater.inflate(R.layout.dialog_change_nick, container, false);
                    builder.setView(viewChangeNick);
                    AlertDialog alert = builder.create();
                    alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                    EditText ET_nick = viewChangeNick.findViewById(R.id.dialogChangeNick_ET_newNick);
                    Button btn_changeNick = viewChangeNick.findViewById(R.id.dialogChangeNick_btn_changeNick);

                    ET_nick.setText(mSettings.getString(APP_PREFERENCES_PORT, "5000"));
                    btn_changeNick.setText("https://mafiagoserver.online:" + ET_nick.getText());
                    btn_changeNick.setAllCaps(false);
                    ET_nick.setHint("Введите новый порт");

                    ET_nick.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }
                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                        }
                        @Override
                        public void afterTextChanged(Editable s) {
                            btn_changeNick.setText("https://mafiagoserver.online:" + ET_nick.getText());
                        }
                    });

                    btn_changeNick.setOnClickListener(v13 -> {
                        SharedPreferences.Editor editor = mSettings.edit();
                        editor.putString(APP_PREFERENCES_PORT, String.valueOf(ET_nick.getText()));
                        editor.apply();
                        alert.cancel();
                        reset();
                    });

                    alert.show();
                }
            });
            Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.voting);
            IV_gameName.startAnimation(animation);
        }

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
                    MainActivity.nick = ET_email.getText().toString();
                    MainActivity.password = ET_password.getText().toString();
                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putString(APP_PREFERENCES_EMAIL, String.valueOf(ET_email.getText()));
                    editor.putString(APP_PREFERENCES_PASSWORD, String.valueOf(ET_password.getText()));
                    editor.apply();
                    if (!ET_password.getText().toString().equals("") && !ET_email.getText().toString().equals("")) {
                        AutoRun = false;
                        Login();
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

    public void Login() {
        try {
            final JSONObject json = new JSONObject();
            PB_loading.setVisibility(View.VISIBLE);
            final String[] resp = {""};

            json.put("email", MainActivity.nick);
            json.put("password", MainActivity.password);
            json.put("current_game_version", MainActivity.CURRENT_GAME_VERSION);

            Log.d("kkk", "Отправил: " + json + " на url: " + url);

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
                    ContextCompat.getMainExecutor(getContext()).execute(() -> {
                        PB_loading.setVisibility(View.INVISIBLE);

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        View viewDang = getLayoutInflater().inflate(R.layout.dialog_error, null);
                        builder.setView(viewDang);
                        TextView TV_title = viewDang.findViewById(R.id.dialogError_TV_errorTitle);
                        TextView TV_error = viewDang.findViewById(R.id.dialogError_TV_errorText);
                        TV_title.setText("Ошибка!");
                        TV_error.setText("Сообщите разработчику об ошибке: " + e.getMessage());
                        AlertDialog alert = builder.create();
                        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        alert.show();
                    });
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    resp[0] = response.body().string();
                    String Answer = resp[0];
                    Log.d("kkk", "Принял: " + Answer);
                    try {
                        switch (Answer) {
                            case "incorrect_nick":
                                ContextCompat.getMainExecutor(getContext()).execute(() -> {
                                    PB_loading.setVisibility(View.INVISIBLE);
                                    if (!AutoRun) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                        View viewDang = getLayoutInflater().inflate(R.layout.dialog_error, null);
                                        builder.setView(viewDang);
                                        TextView TV_title = viewDang.findViewById(R.id.dialogError_TV_errorTitle);
                                        TextView TV_error = viewDang.findViewById(R.id.dialogError_TV_errorText);
                                        TV_title.setText("Такого аккаунта не существует!");
                                        TV_error.setText("Возможно, вы указали неверный домен почты (например: @mail.ru вместо @gmail.com) или ошиблись в написании почты");
                                        AlertDialog alert = builder.create();
                                        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                        alert.show();
                                    }
                                });
                                break;
                            case "incorrect_password":
                                ContextCompat.getMainExecutor(getContext()).execute(() -> {
                                    PB_loading.setVisibility(View.INVISIBLE);
                                    if (!AutoRun) {
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
                                    }
                                });

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
                                    MainActivity.MyInviteCode = data.getString("my_invite_code");
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
                                else
                                {
                                    ContextCompat.getMainExecutor(getContext()).execute(() -> {
                                        PB_loading.setVisibility(View.INVISIBLE);

                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                        View viewDang = getLayoutInflater().inflate(R.layout.dialog_error, null);
                                        builder.setView(viewDang);
                                        TextView TV_title = viewDang.findViewById(R.id.dialogError_TV_errorTitle);
                                        TextView TV_error = viewDang.findViewById(R.id.dialogError_TV_errorText);
                                        TV_title.setText("Ошибка сервера!");
                                        TV_error.setText("Напишите разработчикам и подробно опишите проблему");
                                        AlertDialog alert = builder.create();
                                        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                        alert.show();

                                    });
                                }
                                break;
                        }
                    } catch (Exception e) {
                        Log.d("kkk", String.valueOf(e.getMessage()));
                    }
                }
            });
        } catch (Exception e) {
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

    private void reset() {
        Intent intent = new Intent(getContext(), MainActivity.class);
        startActivity(intent);
    }
}