package com.mafiago.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.mafiago.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mafiago.MainActivity;
import com.mafiago.classes.OnBackPressedListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import io.socket.emitter.Emitter;

import static com.mafiago.MainActivity.socket;


public class MenuFragment extends Fragment implements OnBackPressedListener {

    Button btnRules;
    Button btnGames;
    Button btnFriends;
    Button btnTools;

    TextView TV_money;
    TextView TV_exp;
    TextView TV_nick;

    CardView CV_info;

    ImageView IV_background;
    ImageView IV_avatar;

    // Идентификатор уведомления
    private static final int NOTIFY_ID = 101;

    // Идентификатор канала
    private static String CHANNEL_ID = "Notifications channel";

    public static final String APP_PREFERENCES = "user";
    public static final String APP_PREFERENCES_EMAIL = "email";
    public static final String APP_PREFERENCES_PASSWORD = "password";
    public static final String APP_PREFERENCES_LAST_ROLE = "role";

    private SharedPreferences mSettings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view;
        view = inflater.inflate(R.layout.fragment_menu, container, false);

        btnRules = view.findViewById(R.id.btnRules);
        btnGames = view.findViewById(R.id.btnGame);
        btnFriends = view.findViewById(R.id.btnFriends);
        btnTools = view.findViewById(R.id.btnTools);
        TV_money = view.findViewById(R.id.fragmentMenu_TV_money);
        TV_exp = view.findViewById(R.id.fragmentMenu_TV_exp);
        TV_nick = view.findViewById(R.id.fragmentMenu_TV_nick);

        IV_background = view.findViewById(R.id.fragmentMenu_IV_background);
        IV_avatar = view.findViewById(R.id.fragmentMenu_IV_avatar);

        mSettings = getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        SetBackgroundRole(mSettings.getString(APP_PREFERENCES_LAST_ROLE, "mafia"));

        socket.off("get_profile");

        socket.on("get_profile", OnGetProfile);

        CV_info = view.findViewById(R.id.fragmentMenuMenu_CV_info);

        //настройки от Шлыкова
        //Nastroiki nastroiki = new Nastroiki();

        final JSONObject json = new JSONObject();
        try {
            json.put("nick", MainActivity.NickName);
            json.put("session_id", MainActivity.Session_id);
            json.put("info_nick", MainActivity.NickName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("get_profile", json);
        Log.d("kkk", "Socket_отправка - get_profile - "+ json.toString());
        final Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.bounce_center);

        // amplitude 0.2 and frequency 20
        BounceInterpolator interpolator = new BounceInterpolator();
        animation.setInterpolator(interpolator);

        CV_info.startAnimation(animation);

        btnTools.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new SettingsFragment()).commit();
        });

        btnRules.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new RulesFragment()).commit();
            }
        });

        btnGames.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkOnline(getContext()))
                {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
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

        btnFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkOnline(getContext())) {
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new FriendsFragment()).commit();
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

        fromBase64(toBase64());

        return view;
    }

    @Override
    public void onBackPressed() {
        socket.emit("leave_app", "");
        Log.d("kkk", "Socket_отправка - leave_app");
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new StartFragment()).commit();
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(APP_PREFERENCES_EMAIL, null);
        editor.putString(APP_PREFERENCES_PASSWORD, null);
        editor.apply();
    }

    //TODO: реализовать функцию
    public String toBase64() {
        // Получаем изображение из ImageView
        BitmapDrawable drawable = (BitmapDrawable) IV_background.getDrawable();
        Bitmap bitmap = drawable.getBitmap();

        // Записываем изображение в поток байтов.
        // При этом изображение можно сжать и / или перекодировать в другой формат.
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

        // Получаем изображение из потока в виде байтов
        byte[] bytes = byteArrayOutputStream.toByteArray();

        // Кодируем байты в строку Base64 и возвращаем
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public Bitmap fromBase64(String image) {
        // Декодируем строку Base64 в массив байтов
        byte[] decodedString = Base64.decode(image, Base64.DEFAULT);

        // Декодируем массив байтов в изображение
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        // Помещаем изображение в ImageView
        return decodedByte;
    }

    private final Emitter.Listener OnGetProfile = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject data = (JSONObject) args[0];
                String nick = "", avatar = "";
                boolean online = false;
                int money = 0, exp = 0;

                try {
                    avatar = data.getString("avatar");
                    online = data.getBoolean("is_online");
                    nick = data.getString("nick");
                    money = data.getInt("money");
                    exp = data.getInt("exp");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (avatar != null) {
                    IV_avatar.setImageBitmap(fromBase64(avatar));
                }

                TV_money.setText(String.valueOf(money));
                TV_exp.setText(String.valueOf(exp));
                TV_nick.setText(nick);

                Log.d("kkk", "принял - get_profile - " + data);
            }
        });
    };

    public void SetBackgroundRole(String role) {
        switch (role)
        {
            case "citizen":
                IV_background.setImageResource(R.drawable.citizen_alive);
                break;
            case "mafia":
                IV_background.setImageResource(R.drawable.mafia_alive);
                break;
            case "sheriff":
                IV_background.setImageResource(R.drawable.sheriff_alive);
                break;
            case "doctor":
                IV_background.setImageResource(R.drawable.doctor_alive);
                break;
            case "lover":
                IV_background.setImageResource(R.drawable.lover_alive);
                break;
            default:
                IV_background.setImageResource(R.drawable.mafia_alive);
                break;
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
}
