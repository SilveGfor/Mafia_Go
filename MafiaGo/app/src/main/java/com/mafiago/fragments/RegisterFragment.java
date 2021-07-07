package com.mafiago.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.mafiago.MainActivity;
import com.example.mafiago.R;
import com.mafiago.classes.OnBackPressedListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.mafiago.MainActivity.client;
import static com.mafiago.MainActivity.f;

public class RegisterFragment extends Fragment implements OnBackPressedListener {

    private static final String url1 = MainActivity.url + "/reg-code";
    private static final String url2 = MainActivity.url + "/registration";

    String resp = "";

    Button btnReg;
    Button btnSendCode;
    Button btnExit;
    Button btnTry;

    EditText ETnick;
    EditText ETemail;
    EditText ETpassword1;
    EditText ETpassword2;
    EditText ETcode;
    TextView text_reg;
    Dialog dialog;

    Boolean incorrectEmail = false;
    Boolean sendCode = false;
    Boolean error = false;

    Boolean incorrect_email= false, incorrect_nick = false, incorrect_code = false, mat_nick = false, code_time_out = false, reg_in = false;

    public static final String APP_PREFERENCES = "user";
    public static final String APP_PREFERENCES_EMAIL = "email";
    public static final String APP_PREFERENCES_PASSWORD = "password";
    public static final String APP_PREFERENCES_NICKNAME = "nickname";
    public static final String APP_PREFERENCES_WAIT_CODE = "wait";

    private SharedPreferences mSettings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view;
        view = inflater.inflate(R.layout.fragment_register, container, false);
        btnReg = view.findViewById(R.id.btnReg);
        btnSendCode = view.findViewById(R.id.btnSendCode);
        btnExit = view.findViewById(R.id.btnExitRegister);
        btnTry = view.findViewById(R.id.btnTryReg);

        ETnick = view.findViewById(R.id.ETnickReg);
        ETemail = view.findViewById(R.id.ETemailReg);
        ETcode = view.findViewById(R.id.ETcodeReg);
        ETpassword1 = view.findViewById(R.id.ETpasswordReg1);
        ETpassword2 = view.findViewById(R.id.ETpasswordReg2);
        text_reg = view.findViewById(R.id.TVcode);

        text_reg.setVisibility(View.GONE);
        btnSendCode.setVisibility(View.GONE);
        ETcode.setVisibility(View.GONE);

        mSettings = getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);


        if (mSettings.contains(APP_PREFERENCES_WAIT_CODE)) {
            // Получаем значение из настроек
            boolean mWait = mSettings.getBoolean(APP_PREFERENCES_WAIT_CODE, false);
            // Выводим на экран данные из настроек
            if (mWait) {

                ETemail.setVisibility(View.GONE);
                ETnick.setVisibility(View.GONE);
                ETpassword1.setVisibility(View.GONE);
                ETpassword2.setVisibility(View.GONE);
                btnReg.setVisibility(View.GONE);
                text_reg.setVisibility(View.VISIBLE);
                btnSendCode.setVisibility(View.VISIBLE);
                ETcode.setVisibility(View.VISIBLE);
            } else {
                text_reg.setVisibility(View.GONE);
                btnSendCode.setVisibility(View.GONE);
                ETcode.setVisibility(View.GONE);
            }
        } else {
            Log.d("kkk", "SharedPref mWait - нет данных");
        }

        btnTry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ETemail.setVisibility(View.VISIBLE);
                ETnick.setVisibility(View.VISIBLE);
                ETpassword1.setVisibility(View.VISIBLE);
                ETpassword2.setVisibility(View.VISIBLE);
                btnReg.setVisibility(View.VISIBLE);
                text_reg.setVisibility(View.GONE);
                btnSendCode.setVisibility(View.GONE);
                ETcode.setVisibility(View.GONE);
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putBoolean(APP_PREFERENCES_WAIT_CODE, false);
                editor.apply();
            }
        });

        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkOnline(getContext())) {
                    if (ETpassword1.getText().toString().equals(ETpassword2.getText().toString()) && !ETpassword1.getText().toString().trim().equals("") && !ETnick.getText().toString().trim().equals("")) {

                        String nick = ETnick.getText().toString();
                        int flag = 0;
                        for (int i = 0; i < nick.length(); i ++)
                        {
                            if (Character.isLetter(nick.charAt(i))) {
                                for (int j = 0; j < f.length; j++) {
                                    if (nick.charAt(i) == f[j]) {
                                        flag = 1;
                                    }
                                }

                                if (flag != 1) {
                                    nick = nick.replace(String.valueOf(nick.charAt(i)), "");
                                }
                                flag = 0;
                            }
                        }

                        String email = ETemail.getText().toString().toLowerCase().trim();

                        final JSONObject json = new JSONObject();
                        try {
                            json.put("email", email);
                            json.put("nick", nick);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d("kkk", "Отправил: " + json);

                        RequestBody body = RequestBody.create(
                                MediaType.parse("application/json; charset=utf-8"), String.valueOf(json));
                        Request request = new Request.Builder().url(url1).post(body).build();
                        Call call = client.newCall(request);

                        String finalNick = nick;
                        call.enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Log.d("kkk", "Всё плохо");
                                Log.d("kkk", e.toString());
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                resp = response.body().string().toString();
                                Log.d("kkk", "Принял - " + resp);
                                switch (resp) {
                                    case "incorrect_email":
                                        ContextCompat.getMainExecutor(getContext()).execute(() -> {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                            builder.setTitle("Пользователь с такой почтой уже есть")
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
                                        });
                                        break;
                                    case "send_code":
                                        ContextCompat.getMainExecutor(getContext()).execute(() -> {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                            builder.setTitle("Код успешно отправлен на почту!")
                                                    .setMessage("")
                                                    .setIcon(R.drawable.mafiago)
                                                    .setCancelable(false)
                                                    .setNegativeButton("Ок",
                                                            new DialogInterface.OnClickListener() {
                                                                public void onClick(DialogInterface dialog, int id) {
                                                                    dialog.cancel();
                                                                }
                                                            });
                                            AlertDialog alert = builder.create();
                                            alert.show();
                                            ETemail.setVisibility(View.GONE);
                                            ETnick.setVisibility(View.GONE);
                                            ETpassword1.setVisibility(View.GONE);
                                            ETpassword2.setVisibility(View.GONE);
                                            btnReg.setVisibility(View.GONE);
                                            text_reg.setVisibility(View.VISIBLE);
                                            btnSendCode.setVisibility(View.VISIBLE);
                                            ETcode.setVisibility(View.VISIBLE);
                                        });

                                        SharedPreferences.Editor editor = mSettings.edit();
                                        editor.putBoolean(APP_PREFERENCES_WAIT_CODE, true);
                                        editor.putString(APP_PREFERENCES_EMAIL, email);
                                        editor.putString(APP_PREFERENCES_NICKNAME, finalNick);
                                        editor.putString(APP_PREFERENCES_PASSWORD, String.valueOf(ETpassword1.getText()));
                                        editor.apply();


                                        break;
                                    case "bad_email":
                                        ContextCompat.getMainExecutor(getContext()).execute(() -> {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                            builder.setTitle("Вы ввели некорректную почту!")
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
                                        });
                                        break;
                                    case "incorrect_nick":
                                        ContextCompat.getMainExecutor(getContext()).execute(() -> {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                            builder.setTitle("Пользователь с таким ником уже есть!")
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
                                        });
                                        break;
                                    case "mat_nick":
                                        ContextCompat.getMainExecutor(getContext()).execute(() -> {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                            builder.setTitle("Ваш ник не проходит цензуру!")
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
                                        });
                                        break;
                                    default:
                                        ContextCompat.getMainExecutor(getContext()).execute(() -> {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                            builder.setTitle("Что-то пошло не так!")
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
                                        });
                                        break;
                                }
                            }
                        });
                    } else {
                        if (!ETpassword1.getText().toString().equals(ETpassword2.getText().toString())) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("Ваши пароли не совпадают!")
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
                        } else if (ETpassword1.getText().toString().trim().equals("")) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("Ваш пароль не может быть пустым!")
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
                        } else if (ETnick.getText().toString().trim().equals("")) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("Ваш ник не может быть пустым!")
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
                    }
                }
                else {
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

        btnSendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkOnline(getContext())) {
                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putBoolean(APP_PREFERENCES_WAIT_CODE, false);
                    editor.apply();
                    final JSONObject json = new JSONObject();
                    try {
                        json.put("code", ETcode.getText());
                        json.put("email", mSettings.getString(APP_PREFERENCES_EMAIL, ""));
                        json.put("password", mSettings.getString(APP_PREFERENCES_PASSWORD, ""));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.d("kkk", "Отправил: " + json);

                    RequestBody body = RequestBody.create(
                            MediaType.parse("application/json; charset=utf-8"), String.valueOf(json));
                    Request request = new Request.Builder().url(url2).post(body).build();
                    Call call = client.newCall(request);

                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.d("kkk", "Всё плохо");
                            Log.d("kkk", e.toString());
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            resp = response.body().string().toString();
                            Log.d("kkk", "Принял - " + resp);
                            switch (resp) {
                                case "incorrect_email":
                                    ContextCompat.getMainExecutor(getContext()).execute(()  -> {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                        builder.setTitle("Пользователь с такой почтой уже есть!")
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
                                    });
                                    break;
                                case "bad_email":
                                    ContextCompat.getMainExecutor(getContext()).execute(()  -> {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                        builder.setTitle("Некорректная почта!")
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
                                    });
                                    break;
                                case "incorrect_code":
                                    ContextCompat.getMainExecutor(getContext()).execute(()  -> {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                        builder.setTitle("Неверный код!")
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
                                    });
                                    break;
                                case "code_time_out":
                                    ContextCompat.getMainExecutor(getContext()).execute(()  -> {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                        builder.setTitle("Время действия кода истекло!")
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
                                    });
                                    break;
                                case "reg_in":
                                    ContextCompat.getMainExecutor(getContext()).execute(()  -> {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                        builder.setTitle("Регистрация прошла успешно!")
                                                .setMessage("")
                                                .setIcon(R.drawable.ic_ok)
                                                .setCancelable(false)
                                                .setNegativeButton("Ок",
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                dialog.cancel();
                                                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new StartFragment()).commit();
                                                            }
                                                        });
                                        AlertDialog alert = builder.create();
                                        alert.show();
                                    });
                                    break;
                                default:
                                    ContextCompat.getMainExecutor(getContext()).execute(()  -> {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                        builder.setTitle("Что-то пошло не так!")
                                                .setMessage("")
                                                .setIcon(R.drawable.ic_error)
                                                .setCancelable(false)
                                                .setNegativeButton("Ок",
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                dialog.cancel();
                                                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new StartFragment()).commit();
                                                            }
                                                        });
                                        AlertDialog alert = builder.create();
                                        alert.show();
                                    });
                                    break;
                            }
                        }
                    });
                }
                else {
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

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new StartFragment()).commit();
            }
        });
        return view;
    }

    @Override
    public void onBackPressed() {
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new StartFragment()).commit();
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
}