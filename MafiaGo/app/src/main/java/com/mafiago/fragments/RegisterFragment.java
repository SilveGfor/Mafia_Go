package com.mafiago.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.mafiago.MainActivity;
import com.example.mafiago.R;
import com.mafiago.classes.OnBackPressedListener;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;

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

public class RegisterFragment extends Fragment implements OnBackPressedListener {

    private static final String url1 = MainActivity.url + "/reg-code";
    private static final String url2 = MainActivity.url + "/registration";

    String resp = "";

    Button btnReg;
    Button btnSendCode;

    RelativeLayout RL_back;

    ShimmerTextView STV_text;

    EditText ETnick;
    EditText ETemail;
    EditText ETpassword1;
    EditText ETpassword2;
    EditText ETcode;
    EditText ET_inviteCode;
    TextView text_reg;

    ProgressBar loading;

    TextView TV_sendCodeOneMoreTime;
    TextView TV_repeatRegistration;

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
        btnReg = view.findViewById(R.id.fragmentRegister_btn_reg);
        btnSendCode = view.findViewById(R.id.fragmentChangePassword_btn_sendCode);

        ETnick = view.findViewById(R.id.fragmentRegister_ET_nick);
        ETemail = view.findViewById(R.id.fragmentRegister_ET_email);
        ETcode = view.findViewById(R.id.fragmentChangePassword_ET_code);
        ET_inviteCode = view.findViewById(R.id.fragmentRegister_ET_inviteCode);
        ETpassword1 = view.findViewById(R.id.fragmentRegister_ET_password1);
        ETpassword2 = view.findViewById(R.id.fragmentRegister_ET_password2);
        text_reg = view.findViewById(R.id.fragmentChangePassword_TV_infoChange);
        TV_repeatRegistration = view.findViewById(R.id.fragmentChangePassword_TV_repeatChanging);
        TV_sendCodeOneMoreTime = view.findViewById(R.id.fragmentChangePassword_TV_sendOneMoreTime);
        loading = view.findViewById(R.id.fragmentChangePassword_PB);
        RL_back = view.findViewById(R.id.fragmentGamesList_RL_back);
        STV_text = view.findViewById(R.id.fragmentRegister_TV_text);

        Shimmer shimmer = new Shimmer();
        shimmer.start(STV_text);

        text_reg.setVisibility(View.GONE);
        btnSendCode.setVisibility(View.GONE);
        ETcode.setVisibility(View.GONE);
        TV_repeatRegistration.setVisibility(View.GONE);
        TV_sendCodeOneMoreTime.setVisibility(View.GONE);

        loading.setVisibility(View.GONE);

        mSettings = getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        RL_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new StartFragment()).commit();
            }
        });

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
                TV_repeatRegistration.setVisibility(View.VISIBLE);
                TV_sendCodeOneMoreTime.setVisibility(View.VISIBLE);
            } else {
                text_reg.setVisibility(View.GONE);
                btnSendCode.setVisibility(View.GONE);
                ETcode.setVisibility(View.GONE);
                TV_repeatRegistration.setVisibility(View.GONE);
                TV_sendCodeOneMoreTime.setVisibility(View.GONE);
            }
        } else {
            Log.d("kkk", "SharedPref mWait - нет данных");
        }


        TV_repeatRegistration.setOnClickListener(v -> {
            ETemail.setVisibility(View.VISIBLE);
            ETnick.setVisibility(View.VISIBLE);
            ETpassword1.setVisibility(View.VISIBLE);
            ETpassword2.setVisibility(View.VISIBLE);
            btnReg.setVisibility(View.VISIBLE);
            text_reg.setVisibility(View.GONE);
            btnSendCode.setVisibility(View.GONE);
            ETcode.setVisibility(View.GONE);
            TV_repeatRegistration.setVisibility(View.GONE);
            TV_sendCodeOneMoreTime.setVisibility(View.GONE);
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putBoolean(APP_PREFERENCES_WAIT_CODE, false);
            editor.apply();
        });

        TV_sendCodeOneMoreTime.setOnClickListener(v -> {
            final JSONObject json = new JSONObject();
            try {
                json.put("email", mSettings.getString(APP_PREFERENCES_EMAIL, ""));
                json.put("nick", mSettings.getString(APP_PREFERENCES_NICKNAME, ""));
                json.put("invite_code", ET_inviteCode.getText());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("kkk", "Отправил: " + json);

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"), String.valueOf(json));
            Request request = new Request.Builder().url(url1).post(body).build();
            Call call = client.newCall(request);

            String finalNick = mSettings.getString(APP_PREFERENCES_NICKNAME, "");
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
                                TV_repeatRegistration.setVisibility(View.VISIBLE);
                                TV_sendCodeOneMoreTime.setVisibility(View.VISIBLE);
                            });

                            SharedPreferences.Editor editor = mSettings.edit();
                            editor.putBoolean(APP_PREFERENCES_WAIT_CODE, true);
                            editor.putString(APP_PREFERENCES_EMAIL, mSettings.getString(APP_PREFERENCES_EMAIL, ""));
                            editor.putString(APP_PREFERENCES_NICKNAME, finalNick);
                            editor.putString(APP_PREFERENCES_PASSWORD, String.valueOf(ETpassword1.getText()));
                            editor.apply();


                            break;
                        case "incorrect_invite_code":
                            ContextCompat.getMainExecutor(getContext()).execute(()  -> {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Некорректный код друга!")
                                        .setMessage("Проверьте правильность написания")
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
        });


        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkOnline(getContext())) {
                    if (ETpassword1.getText().toString().equals(ETpassword2.getText().toString()) &&
                            !ETpassword1.getText().toString().trim().equals("") &&
                            !ETnick.getText().toString().trim().equals("") &&
                            !ETnick.getText().toString().contains(".") &&
                            ETpassword1.length() >= 7 &&
                            ETpassword1.length() <= 20) {

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
                        if (nick.length() >= 3) {
                            if (nick.length() <= 15) {

                                loading.setVisibility(View.VISIBLE);

                                String email = ETemail.getText().toString().toLowerCase().trim();

                                final JSONObject json = new JSONObject();
                                try {
                                    json.put("email", email);
                                    json.put("nick", nick);
                                    json.put("invite_code", ET_inviteCode.getText());
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
                                                    TV_repeatRegistration.setVisibility(View.VISIBLE);
                                                    TV_sendCodeOneMoreTime.setVisibility(View.VISIBLE);
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
                                            case "incorrect_invite_code":
                                                ContextCompat.getMainExecutor(getContext()).execute(()  -> {
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                    builder.setTitle("Некорректный код друга!")
                                                            .setMessage("Проверьте правильность написания")
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
                            }
                            else
                            {
                                AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
                                View viewDang = getLayoutInflater().inflate(R.layout.dialog_error, null);
                                builder2.setView(viewDang);
                                TextView TV_title = viewDang.findViewById(R.id.dialogError_TV_errorTitle);
                                TextView TV_error = viewDang.findViewById(R.id.dialogError_TV_errorText);
                                TV_title.setText("Длинный ник!");
                                TV_error.setText("Ваш ник должен быть меньше 16 символов");
                                AlertDialog alert2 = builder2.create();
                                alert2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                alert2.show();
                            }
                        }
                        else
                        {
                            AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
                            View viewDang = getLayoutInflater().inflate(R.layout.dialog_error, null);
                            builder2.setView(viewDang);
                            TextView TV_title = viewDang.findViewById(R.id.dialogError_TV_errorTitle);
                            TextView TV_error = viewDang.findViewById(R.id.dialogError_TV_errorText);
                            TV_title.setText("Короткий ник!");
                            TV_error.setText("Ваш ник должен быть больше 2 символов");
                            AlertDialog alert2 = builder2.create();
                            alert2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            alert2.show();
                        }
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
                        } else if (ETnick.getText().toString().contains(".")) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("Ник не должен содержать точку!")
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
                    json.put("nick", mSettings.getString(APP_PREFERENCES_NICKNAME, ""));
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