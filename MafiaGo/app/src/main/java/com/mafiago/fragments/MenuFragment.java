package com.mafiago.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.mafiago.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mafiago.MainActivity;
import com.mafiago.classes.OnBackPressedListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import io.socket.emitter.Emitter;

import static android.app.Activity.RESULT_OK;
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

    static final int GALLERY_REQUEST = 1;

    ImageView IV_background;
    ImageView IV_avatar;
    ImageView IV_screenshot;

    String base64_screenshot = "";

    View view_report;

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

        view_report = getLayoutInflater().inflate(R.layout.dialog_report, null);

        IV_screenshot = view_report.findViewById(R.id.dialogReport_IV_screenshot);

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
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new PrivateChatsFragment()).commit();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case GALLERY_REQUEST:
                if(resultCode == RESULT_OK){
                    Uri uri = imageReturnedIntent.getData();
                    IV_screenshot.setImageURI(null);
                    IV_screenshot.setImageURI(uri);

                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        byte[] bytes = stream.toByteArray();

                        base64_screenshot = Base64.encodeToString(bytes, Base64.DEFAULT);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        }
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
                int money = 0, exp = 0, gold = 0;
                JSONObject statistic = new JSONObject();
                int game_counter = 0, max_money_score = 0, max_exp_score = 0;
                String general_pers_of_wins = "", mafia_pers_of_wins = "", peaceful_pers_of_wins = "", user_id_2 = "";
                Log.d("kkk", "принял - get_profile - " + data);

                try {
                    statistic = data.getJSONObject("statistics");
                    game_counter = statistic.getInt("game_counter");
                    max_money_score = statistic.getInt("max_money_score");
                    max_exp_score = statistic.getInt("max_exp_score");
                    general_pers_of_wins = statistic.getString("general_pers_of_wins");
                    mafia_pers_of_wins = statistic.getString("mafia_pers_of_wins");
                    peaceful_pers_of_wins = statistic.getString("peaceful_pers_of_wins");
                    avatar = data.getString("avatar");
                    user_id_2 = data.getString("user_id");
                    online = data.getBoolean("is_online");
                    gold = data.getInt("gold");
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

                int finalMoney = money;
                int finalExp = exp;
                boolean finalOnline = online;
                String finalNick = nick;
                int finalGold = gold;
                int finalGame_counter = game_counter;
                int finalMax_money_score = max_money_score;
                int finalMax_exp_score = max_exp_score;
                String finalGeneral_pers_of_wins = general_pers_of_wins;
                String finalMafia_pers_of_wins = mafia_pers_of_wins;
                String finalPeaceful_pers_of_wins = peaceful_pers_of_wins;
                String finalAvatar = avatar;
                String finalUser_id_ = user_id_2;
                CV_info.setOnClickListener(v -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    View view_profile = getLayoutInflater().inflate(R.layout.item_profile, null);
                    builder.setView(view_profile);

                    FloatingActionButton FAB_add_friend = view_profile.findViewById(R.id.Item_profile_add_friend);
                    FloatingActionButton FAB_kick = view_profile.findViewById(R.id.Item_profile_kick);
                    FloatingActionButton FAB_send_message = view_profile.findViewById(R.id.Item_profile_send_message);
                    FloatingActionButton FAB_report = view_profile.findViewById(R.id.Item_profile_complain);
                    TextView TV_money = view_profile.findViewById(R.id.ItemProfile_TV_money);
                    TextView TV_exp = view_profile.findViewById(R.id.ItemProfile_TV_exp);
                    TextView TV_gold = view_profile.findViewById(R.id.ItemProfile_TV_gold);
                    ImageView IV_avatar = view_profile.findViewById(R.id.Item_profile_IV_avatar);

                    TextView TV_game_counter = view_profile.findViewById(R.id.ItemProfile_TV_game_counter);
                    TextView TV_max_money_score = view_profile.findViewById(R.id.ItemProfile_TV_max_money_score);
                    TextView TV_max_exp_score = view_profile.findViewById(R.id.ItemProfile_TV_max_exp_score);
                    TextView TV_general_pers_of_wins = view_profile.findViewById(R.id.ItemProfile_TV_general_pers_of_wins);
                    TextView TV_mafia_pers_of_wins = view_profile.findViewById(R.id.ItemProfile_TV_mafia_pers_of_wins);
                    TextView TV_peaceful_pers_of_wins = view_profile.findViewById(R.id.ItemProfile_TV_peaceful_pers_of_wins);

                    if (finalAvatar != null) {
                        IV_avatar.setImageBitmap(fromBase64(finalAvatar));
                    }

                    TV_game_counter.setText(String.valueOf(finalGame_counter));
                    TV_max_money_score.setText(String.valueOf(finalMax_money_score));
                    TV_max_exp_score.setText(String.valueOf(finalMax_exp_score));
                    TV_general_pers_of_wins.setText(String.valueOf(finalGeneral_pers_of_wins));
                    TV_mafia_pers_of_wins.setText(String.valueOf(finalMafia_pers_of_wins));
                    TV_peaceful_pers_of_wins.setText(String.valueOf(finalPeaceful_pers_of_wins));

                    if (finalNick.equals(MainActivity.NickName))
                    {
                        TV_gold.setText(String.valueOf(finalGold));
                        TV_money.setText(String.valueOf(finalMoney));
                    }
                    else
                    {
                        TV_gold.setVisibility(View.GONE);
                        TV_money.setVisibility(View.GONE);
                    }
                    TV_exp.setText(String.valueOf(finalExp));

                    TextView TV_nick = view_profile.findViewById(R.id.Item_profile_TV_nick);
                    ImageView IV_on_off = view_profile.findViewById(R.id.Item_profile_IV_on_off);

                    if (finalOnline) IV_on_off.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_online));
                    else IV_on_off.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_offline));

                    TV_nick.setText(finalNick);

                    FAB_report.setOnClickListener(v1 -> {
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
                        //View view_report = getLayoutInflater().inflate(R.layout.dialog_report, null);
                        builder2.setView(view_report);
                        AlertDialog alert2 = builder2.create();

                        Button btn_add_screenshot = view_report.findViewById(R.id.dialogReport_btn_add_screenshot);
                        Button btn_report = view_report.findViewById(R.id.dialogReport_btn_report);
                        ImageView IV_screenshot = view_report.findViewById(R.id.dialogReport_IV_screenshot);
                        EditText ET_report_message = view_report.findViewById(R.id.dialogReport_ET_report);

                        btn_add_screenshot.setOnClickListener(v2 -> {
                            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                            photoPickerIntent.setType("image/*");
                            startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
                        });

                        btn_report.setOnClickListener(v22 -> {
                            final JSONObject json2 = new JSONObject();
                            try {
                                json2.put("nick", MainActivity.NickName);
                                json2.put("session_id", MainActivity.Session_id);
                                json2.put("against_id", finalUser_id_);
                                json2.put("against_nick", finalNick);
                                json2.put("reason", ET_report_message.getText());
                                json2.put("image", base64_screenshot);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            socket.emit("send_complaint", json2);
                            Log.d("kkk", "Socket_отправка - send_complaint" + json2);
                            alert2.cancel();
                        });

                        alert2.show();
                    });

                    AlertDialog alert = builder.create();

                    FAB_add_friend.setVisibility(View.GONE);
                    FAB_send_message.setVisibility(View.GONE);
                    FAB_kick.setVisibility(View.GONE);
                    //FAB_report.setVisibility(View.GONE);
                    alert.show();
                });
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
