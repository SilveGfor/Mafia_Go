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


public class RegisterFragment extends Fragment {

    private static final String url1 = "http://" + MainActivity.url + "/reg-code/";
    private static final String url2 = "http://" + MainActivity.url + "/registration/";

    //OkHttp
    private OkHttpClient client = new OkHttpClient();
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
                    RegisterFragment.LoginTask loginTask = new RegisterFragment.LoginTask();
                    loginTask.execute();
                } else {
                    Log.d("kkk", "Неправильный пароль");
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Пользователь с такой почтой уже есть")
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
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new StartFragment()).commit();
                        }
                    });


                } else {
                    Log.d("kkk", "пароли не сопадают");
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

    class LoginTask extends AsyncTask<Void, Void, Void> {

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
                    if (resp.equals("incorrect_email")) {
                        incorrectEmail = true;
                    } else if (resp.equals("send_code")) {
                        sendCode = true;
                        SharedPreferences.Editor editor = mSettings.edit();
                        editor.putBoolean(APP_PREFERENCES_WAIT_CODE, true);
                        editor.putString(APP_PREFERENCES_EMAIL, String.valueOf(ETemail.getText()));
                        editor.putString(APP_PREFERENCES_NICKNAME, String.valueOf(ETnick.getText()));
                        editor.putString(APP_PREFERENCES_PASSWORD, String.valueOf(ETpassword1.getText()));
                        editor.apply();
                    } else {
                        error = true;
                    }


                }
            });
            return null;
        }
        @Override
        protected void onPostExecute (Void aVoid){
            super.onPostExecute(aVoid);
            try {
                Thread.sleep(1000); //Приостанавливает поток на 1 секунду

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


}
