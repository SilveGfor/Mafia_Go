package com.example.mafiago.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.mafiago.MainActivity;
import com.example.mafiago.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.mafiago.MainActivity.client;


public class RegisterFragment extends Fragment {

    private static final String url1 = "https://" + MainActivity.url + "/reg-code";
    private static final String url2 = "https://" + MainActivity.url + "/registration";

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

        // TODO: Починить разметку

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

                if (ETpassword1.getText().toString().equals(ETpassword2.getText().toString())) {

                    Log.d("kkk", "Запуск Asycs");
                    RegisterFragment.CodeTask loginTask = new RegisterFragment.CodeTask();
                    loginTask.execute();
                } else {
                    Log.d("kkk", "Неправильный пароль");
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Ошибка!")
                            .setMessage("")
                            .setIcon(R.drawable.ic_error)
                            .setCancelable(false)
                            .setNegativeButton("Пароли не совпадают!",
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
                RegisterFragment.RegisterTask regTask = new RegisterFragment.RegisterTask();
                regTask.execute();

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

    class CodeTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("kkk", "onPreExecute");
        }

        @Override
        protected Void doInBackground(Void... urs) {

            final JSONObject json = new JSONObject();
            try {
                json.put("email", ETemail.getText());
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
                    Log.d("kkk", e.toString());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    resp = response.body().string().toString();
                    Log.d("kkk", "Принял - " + resp);
                    switch (resp)
                    {
                        case "incorrect_email":
                            incorrectEmail = true;
                            break;
                        case "send_code":
                            sendCode = true;
                            SharedPreferences.Editor editor = mSettings.edit();
                            editor.putBoolean(APP_PREFERENCES_WAIT_CODE, true);
                            editor.putString(APP_PREFERENCES_EMAIL, String.valueOf(ETemail.getText()));
                            editor.putString(APP_PREFERENCES_NICKNAME, String.valueOf(ETnick.getText()));
                            editor.putString(APP_PREFERENCES_PASSWORD, String.valueOf(ETpassword1.getText()));
                            editor.apply();
                            break;
                        default:
                            error = true;
                            break;
                    }
                }
            });
            return null;
        }
        @Override
        protected void onPostExecute (Void aVoid){
            super.onPostExecute(aVoid);
            try {
                Thread.sleep(2000); //Приостанавливает поток на 1 секунду

                Log.d("kkk", String.valueOf(sendCode));

                if (incorrectEmail) {
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
                    incorrectEmail = false;
                } else if (sendCode) {
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
                    sendCode = false;
                    ETemail.setVisibility(View.GONE);
                    ETnick.setVisibility(View.GONE);
                    ETpassword1.setVisibility(View.GONE);
                    ETpassword2.setVisibility(View.GONE);
                    btnReg.setVisibility(View.GONE);
                    text_reg.setVisibility(View.VISIBLE);
                    btnSendCode.setVisibility(View.VISIBLE);
                    ETcode.setVisibility(View.VISIBLE);
                } else if (error) {
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
                    error = false;
                }
                Log.d("kkk", "onPostExecute" + incorrectEmail + sendCode + error);
            } catch (Exception e) {

            }
        }
    }

    class RegisterTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("kkk", "onPreExecute");
        }

        @Override
        protected Void doInBackground(Void... urs) {

            SharedPreferences.Editor editor = mSettings.edit();
            editor.putBoolean(APP_PREFERENCES_WAIT_CODE, false);
            editor.apply();
            if (ETpassword1.getText().toString().equals(ETpassword2.getText().toString())) {
                final JSONObject json = new JSONObject();
                try {
                    json.put("code", ETcode.getText());
                    json.put("nick", mSettings.getString(APP_PREFERENCES_NICKNAME, ""));
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
                        switch (resp)
                        {
                            //incorrect_email incorrect_nick incorrect_code mat_nick code_time_out reg_in
                            case "incorrect_email":
                                incorrect_email = true;
                                break;
                            case "incorrect_nick":
                                incorrect_nick = true;
                                break;
                            case "incorrect_code":
                                incorrect_code = true;
                                break;
                            case "mat_nick":
                                mat_nick = true;
                                break;
                            case "code_time_out":
                                code_time_out = true;
                                break;
                            case "reg_in":
                                reg_in = true;
                                break;
                            default:
                                error = true;
                                break;
                        }
                    }
                });


            } else {
                Log.d("kkk", "пароли не сопадают");
            }
            return null;
        }
        @Override
        protected void onPostExecute (Void aVoid){
            super.onPostExecute(aVoid);
            try {
                Thread.sleep(2000); //Приостанавливает поток на 1 секунду

                Log.d("kkk", String.valueOf(sendCode));
                //incorrect_email incorrect_nick incorrect_code mat_nick code_time_out reg_in
                if (incorrect_email) {
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
                    incorrect_email = false;
                }
                else if (incorrect_nick) {
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
                    incorrect_nick = false;
                }
                else if (incorrect_code) {
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
                    incorrect_code = false;
                }
                else if (mat_nick) {
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
                    mat_nick = false;
                }
                else if (code_time_out) {
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
                    code_time_out = false;
                }
                else if (reg_in) {
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
                    reg_in = false;
                }
                Log.d("kkk", "onPostExecute" + incorrectEmail + sendCode + error);
            } catch (Exception e) {

            }
        }
    }


}
