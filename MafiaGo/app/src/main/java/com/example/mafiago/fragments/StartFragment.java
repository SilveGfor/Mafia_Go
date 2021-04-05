package com.example.mafiago.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

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


public class StartFragment extends Fragment {

    private static final String url= "http://" + MainActivity.url + "/login";

    //OkHttp
    private OkHttpClient client = new OkHttpClient();

    //кнопки
    Button btnSignIn;
    Button btnReg;
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

            LoginTask loginTask = new LoginTask();
            loginTask.execute();
        }
        else
        {
            Log.d("kkk", "SharedPref mEmail, mPassword - нет данных");
        }
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //final String[] userss = {ETemail.getText().toString(), ETpassword.getText().toString()};
                MainActivity.nick = ETemail.getText().toString();
                MainActivity.password = ETpassword.getText().toString();
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putString(APP_PREFERENCES_EMAIL, String.valueOf(ETemail.getText()));
                editor.putString(APP_PREFERENCES_PASSWORD, String.valueOf(ETpassword.getText()));
                editor.apply();
                if (!ETpassword.getText().toString().equals("") && !ETemail.getText().toString().equals("")) {
                    LoginTask loginTask = new LoginTask();
                    loginTask.execute();
                }
                else
                {
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
        });

        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new RegisterFragment()).commit();
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
            OkHttpClient client = new OkHttpClient();

            final String[] resp = {""};
            SharedPreferences mSettings;
            mSettings = getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

            final JSONObject json = new JSONObject();
            try {
                json.put("email", MainActivity.nick);
                json.put("password", MainActivity.password);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("kkk", "Отправил: " + json);
            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"), String.valueOf(json));
            Request request = new Request.Builder().url(url).post(body).build();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    resp[0] = response.body().string();
                    String Answer = resp[0];

                    Log.d("kkk", "Принял: " + Answer);
                    try {
                        if (Answer.equals("incorrect_email"))
                        {
                            BadLogin = true;
                            Log.d("kkk", "Такого аккаунта не существует");
                        }
                        else  if (Answer.equals("incorrect_password"))
                        {
                            BadPassword = true;
                            Log.d("kkk", "Неправильный пароль");
                        }
                        else
                        {
                            JSONObject obj = new JSONObject(resp[0]);

                            NickName = obj.get("nick").toString();
                            Email = obj.get("email").toString();
                            Session_id = obj.get("session_id").toString();

                            MainActivity.NickName = NickName;
                            MainActivity.Session_id = Session_id;



                            Log.d("kkk", "НИК: " + NickName);
                            Log.d("kkk", "Переход в MenuFragment");
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new MenuFragment()).commit();
                            //ETpassword.setText("");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("kkk", "Ошибка!!!");
                    }
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (BadLogin && !AutoRun)
            {
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
                BadLogin = false;
            }
            else if (BadPassword && !AutoRun)
            {
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
                BadPassword = false;
                ETpassword.setText("");
            }
            AutoRun = false;
            Log.d("kkk", "onPostExecute");
        }
    }
}