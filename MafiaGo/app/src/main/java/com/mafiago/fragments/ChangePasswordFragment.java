package com.mafiago.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.mafiago.R;
import com.mafiago.MainActivity;
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

import static com.mafiago.MainActivity.client;
import static com.mafiago.MainActivity.f;

public class ChangePasswordFragment extends Fragment implements OnBackPressedListener {

    private static final String url1 = MainActivity.url + "/forget-password-code";
    private static final String url2 = MainActivity.url + "/change-forgotten-password";

    String resp = "";

    Button btnChangePassword;
    Button btnSendCode;

    EditText ETemail;
    EditText ETpassword1;
    EditText ETpassword2;
    EditText ETcode;
    TextView text_reg;

    ProgressBar loading;

    TextView TV_sendCodeOneMoreTime;
    TextView TV_repeatChanging;

    public static final String APP_PREFERENCES = "user";
    public static final String APP_PREFERENCES_EMAIL = "email_change";
    public static final String APP_PREFERENCES_PASSWORD = "password_change";
    public static final String APP_PREFERENCES_WAIT_CODE = "wait_change";

    private SharedPreferences mSettings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);


        btnChangePassword = view.findViewById(R.id.fragmentChangePassword_btn_change);
        btnSendCode = view.findViewById(R.id.fragmentChangePassword_btn_sendCode);

        ETemail = view.findViewById(R.id.fragmentChangePassword_ET_email);
        ETcode = view.findViewById(R.id.fragmentChangePassword_ET_code);
        ETpassword1 = view.findViewById(R.id.fragmentChangePassword_ET_password1);
        ETpassword2 = view.findViewById(R.id.fragmentChangePassword_ET_password2);
        text_reg = view.findViewById(R.id.fragmentChangePassword_TV_infoChange);
        TV_repeatChanging = view.findViewById(R.id.fragmentChangePassword_TV_repeatChanging);
        TV_sendCodeOneMoreTime = view.findViewById(R.id.fragmentChangePassword_TV_sendOneMoreTime);
        loading = view.findViewById(R.id.fragmentChangePassword_PB);

        text_reg.setVisibility(View.GONE);
        btnSendCode.setVisibility(View.GONE);
        ETcode.setVisibility(View.GONE);
        TV_repeatChanging.setVisibility(View.GONE);
        TV_sendCodeOneMoreTime.setVisibility(View.GONE);

        loading.setVisibility(View.GONE);

        mSettings = getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);


        if (mSettings.contains(APP_PREFERENCES_WAIT_CODE)) {
            // Получаем значение из настроек
            boolean mWait = mSettings.getBoolean(APP_PREFERENCES_WAIT_CODE, false);
            // Выводим на экран данные из настроек
            if (mWait) {
                ETemail.setVisibility(View.GONE);
                ETpassword1.setVisibility(View.GONE);
                ETpassword2.setVisibility(View.GONE);
                btnChangePassword.setVisibility(View.GONE);
                text_reg.setVisibility(View.VISIBLE);
                btnSendCode.setVisibility(View.VISIBLE);
                ETcode.setVisibility(View.VISIBLE);
                TV_repeatChanging.setVisibility(View.VISIBLE);
                TV_sendCodeOneMoreTime.setVisibility(View.VISIBLE);
            } else {
                text_reg.setVisibility(View.GONE);
                btnSendCode.setVisibility(View.GONE);
                ETcode.setVisibility(View.GONE);
                TV_repeatChanging.setVisibility(View.GONE);
                TV_sendCodeOneMoreTime.setVisibility(View.GONE);
            }
        } else {
            Log.d("kkk", "SharedPref mWait - нет данных");
        }


        TV_repeatChanging.setOnClickListener(v -> {
            ETemail.setVisibility(View.VISIBLE);
            ETpassword1.setVisibility(View.VISIBLE);
            ETpassword2.setVisibility(View.VISIBLE);
            btnChangePassword.setVisibility(View.VISIBLE);
            text_reg.setVisibility(View.GONE);
            btnSendCode.setVisibility(View.GONE);
            ETcode.setVisibility(View.GONE);
            TV_repeatChanging.setVisibility(View.GONE);
            TV_sendCodeOneMoreTime.setVisibility(View.GONE);
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putBoolean(APP_PREFERENCES_WAIT_CODE, false);
            editor.apply();
        });

        TV_sendCodeOneMoreTime.setOnClickListener(v -> {
            final JSONObject json = new JSONObject();
            try {
                json.put("email", mSettings.getString(APP_PREFERENCES_EMAIL, ""));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("kkk", "Отправил: " + json);

            loading.setVisibility(View.VISIBLE);

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"), String.valueOf(json));
            Request request = new Request.Builder().url(url1).post(body).build();
            Call call = client.newCall(request);

            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("kkk", "Всё плохо");
                    ContextCompat.getMainExecutor(getContext()).execute(() -> {
                        loading.setVisibility(View.GONE);
                    });
                    Log.d("kkk", e.toString());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    resp = response.body().string().toString();
                    Log.d("kkk", "Принял - " + resp);
                    ContextCompat.getMainExecutor(getContext()).execute(() -> {
                        loading.setVisibility(View.GONE);
                    });
                    switch (resp) {
                        case "incorrect_email":
                            ContextCompat.getMainExecutor(getContext()).execute(() -> {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Нет пользователя с такой почтой")
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
                                ETpassword1.setVisibility(View.GONE);
                                ETpassword2.setVisibility(View.GONE);
                                btnChangePassword.setVisibility(View.GONE);
                                text_reg.setVisibility(View.VISIBLE);
                                btnSendCode.setVisibility(View.VISIBLE);
                                ETcode.setVisibility(View.VISIBLE);
                                TV_repeatChanging.setVisibility(View.VISIBLE);
                                TV_sendCodeOneMoreTime.setVisibility(View.VISIBLE);
                            });
                            SharedPreferences.Editor editor = mSettings.edit();
                            editor.putBoolean(APP_PREFERENCES_WAIT_CODE, true);
                            editor.putString(APP_PREFERENCES_EMAIL, mSettings.getString(APP_PREFERENCES_EMAIL, ""));
                            editor.putString(APP_PREFERENCES_PASSWORD, String.valueOf(ETpassword1.getText()));
                            editor.apply();
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
        });

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkOnline(getContext())) {
                    if (ETpassword1.getText().toString().equals(ETpassword2.getText().toString()) &&
                            !ETpassword1.getText().toString().trim().equals("") &&
                            ETpassword1.length() >= 7 &&
                            ETpassword1.length() <= 20) {
                        loading.setVisibility(View.VISIBLE);

                        String email = ETemail.getText().toString().toLowerCase().trim();

                        final JSONObject json = new JSONObject();
                        try {
                            json.put("email", email);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d("kkk", "Отправил: " + json);

                        RequestBody body = RequestBody.create(
                                MediaType.parse("application/json; charset=utf-8"), String.valueOf(json));
                        Request request = new Request.Builder().url(url1).post(body).build();
                        Call call = client.newCall(request);

                        call.enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Log.d("kkk", "Всё плохо");
                                ContextCompat.getMainExecutor(getContext()).execute(() -> {
                                    loading.setVisibility(View.GONE);
                                });
                                Log.d("kkk", e.toString());
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                resp = response.body().string().toString();
                                Log.d("kkk", "Принял - " + resp);
                                ContextCompat.getMainExecutor(getContext()).execute(() -> {
                                    loading.setVisibility(View.GONE);
                                });
                                switch (resp) {
                                    case "incorrect_email":
                                        ContextCompat.getMainExecutor(getContext()).execute(() -> {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                            builder.setTitle("Нет пользователя с такой почтой!")
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
                                            ETpassword1.setVisibility(View.GONE);
                                            ETpassword2.setVisibility(View.GONE);
                                            btnChangePassword.setVisibility(View.GONE);
                                            text_reg.setVisibility(View.VISIBLE);
                                            btnSendCode.setVisibility(View.VISIBLE);
                                            ETcode.setVisibility(View.VISIBLE);
                                            TV_repeatChanging.setVisibility(View.VISIBLE);
                                            TV_sendCodeOneMoreTime.setVisibility(View.VISIBLE);
                                        });

                                        SharedPreferences.Editor editor = mSettings.edit();
                                        editor.putBoolean(APP_PREFERENCES_WAIT_CODE, true);
                                        editor.putString(APP_PREFERENCES_EMAIL, email);
                                        editor.putString(APP_PREFERENCES_PASSWORD, String.valueOf(ETpassword1.getText()));
                                        editor.apply();
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
                    }
                    else
                    {
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
                        } else if (ETpassword1.length() < 7) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("Ваш пароль слишком короткий!")
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
                        } else if (ETpassword1.length() > 20) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("Ваш пароль слишком длинный!")
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

        btnSendCode.setOnClickListener(v -> {
            if (isNetworkOnline(getContext())) {
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putBoolean(APP_PREFERENCES_WAIT_CODE, false);
                editor.apply();
                loading.setVisibility(View.VISIBLE);
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
                        ContextCompat.getMainExecutor(getContext()).execute(() -> {
                            loading.setVisibility(View.GONE);
                        });
                        Log.d("kkk", e.toString());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        resp = response.body().string().toString();
                        Log.d("kkk", "Принял - " + resp);
                        ContextCompat.getMainExecutor(getContext()).execute(() -> {
                            loading.setVisibility(View.GONE);
                        });
                        switch (resp) {
                            case "password_updated":
                                ContextCompat.getMainExecutor(getContext()).execute(()  -> {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                    builder.setTitle("Пароль успешно обновлён!")
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