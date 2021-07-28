package com.mafiago.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.RadioGroup;
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

public class MenuFragment extends Fragment{

    Button btnRules;
    Button btnGames;
    Button btnTools;

    TextView TV_money;
    TextView TV_exp;
    TextView TV_rang;
    TextView TV_gold;
    TextView TV_nick;

    CardView CV_info;

    static final int GALLERY_REQUEST = 1;

    ImageView IV_avatar;

    ImageView Chats;
    ImageView Friends;
    ImageView Shop;
    ImageView Competitions;
    ImageView VK;
    ImageView Telegram;

    String base64_screenshot = "", report_nick = "", report_id = "";

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

        btnRules = view.findViewById(R.id.fragmentSettingsProfile_btn_changeNick);
        btnGames = view.findViewById(R.id.fragmentSettingsProfile_btn_changeAvatar);
        Chats = view.findViewById(R.id.fragmentMenu_IV_chats);
        Friends = view.findViewById(R.id.fragmentMenu_IV_friends);
        Shop = view.findViewById(R.id.fragmentMenu_IV_shop);
        VK = view.findViewById(R.id.fragmentMenu_IV_vk);
        Telegram = view.findViewById(R.id.fragmentMenu_IV_telegram);
        Competitions = view.findViewById(R.id.fragmentMenu_IV_competitions);
        btnTools = view.findViewById(R.id.fragmentSettingsProfile_btn_changePassword);
        TV_money = view.findViewById(R.id.fragmentSettingsProfile_TV_money);
        TV_exp = view.findViewById(R.id.dialogYouHaveBeenBanned_TV_exp);
        TV_gold = view.findViewById(R.id.fragmentMenu_TV_gold);
        TV_rang = view.findViewById(R.id.fragmentSettingsProfile_TV_rang);
        TV_nick = view.findViewById(R.id.fragmentSettingsProfile_TV_nick);

        IV_avatar = view.findViewById(R.id.fragmentSettingsProfile_IV_avatar);

        mSettings = getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        //TODO: Сделать что-то про последнюю роль
        //SetBackgroundRole(mSettings.getString(APP_PREFERENCES_LAST_ROLE, "mafia"));

        socket.off("get_profile");

        socket.on("get_profile", OnGetProfile);

        CV_info = view.findViewById(R.id.fragmentMenu_CV);

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


        //final Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.bounce_center);

        // amplitude 0.2 and frequency 20
        //BounceInterpolator interpolator = new BounceInterpolator();
        //animation.setInterpolator(interpolator);
        //CV_info.startAnimation(animation);

        Telegram.setOnClickListener(v -> {
            Intent mIntent = new Intent();
            mIntent.setAction(Intent.ACTION_VIEW);
            mIntent.setData(Uri.parse("https://t.me/mafia_go_game"));
            startActivity(Intent.createChooser( mIntent, "Выберите браузер"));
        });

        VK.setOnClickListener(v -> {
            Intent mIntent = new Intent();
            mIntent.setAction(Intent.ACTION_VIEW);
            mIntent.setData(Uri.parse("https://vk.com/mafia_go_game"));
            startActivity(Intent.createChooser( mIntent, "Выберите браузер"));
        });

        Shop.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new ShopFragment()).commit();
        });

        Competitions.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new CompetitionsFragment()).commit();
        });

        btnTools.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new SettingsFragment()).commit();
        });

        Friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new FriendsFragment()).commit();
            }
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

        Chats.setOnClickListener(new View.OnClickListener() {
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
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if (imageReturnedIntent == null
                || imageReturnedIntent.getData() == null) {
            return;
        }

        // сжимает до ~500КБ максимум. Тогда как обычная картинка весит ~2МБ


        switch(requestCode) {
            case GALLERY_REQUEST:
                if(resultCode == RESULT_OK){
                    Uri uri = imageReturnedIntent.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        //bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        byte[] bytes = stream.toByteArray();

                        int scaleDivider = 5;
                        if (bitmap.getWidth() > bitmap.getHeight()) {
                            scaleDivider = bitmap.getWidth() / 256;
                        }
                        else
                        {
                            scaleDivider = bitmap.getHeight() / 256;
                        }

                        int scaleWidth = bitmap.getWidth() / scaleDivider;
                        int scaleHeight = bitmap.getHeight() / scaleDivider;
                        Log.d("kkk", String.valueOf(scaleWidth));
                        Log.d("kkk", String.valueOf(scaleHeight));
                        byte[] downsizedImageBytes =
                                    getDownsizedImageBytes(bitmap, scaleWidth, scaleHeight);
                        base64_screenshot = Base64.encodeToString(downsizedImageBytes, Base64.DEFAULT);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
                    View view_report = getLayoutInflater().inflate(R.layout.dialog_report, null);
                    builder2.setView(view_report);
                    AlertDialog alert2 = builder2.create();

                    Button btn_add_screenshot = view_report.findViewById(R.id.dialogReport_btn_add_screenshot);
                    Button btn_report = view_report.findViewById(R.id.dialogReport_btn_report);
                    ImageView IV_screenshot = view_report.findViewById(R.id.dialogReport_IV_screenshot);
                    EditText ET_report_message = view_report.findViewById(R.id.dialogReport_ET_report);

                    final String[] reason = {""};

                    RadioGroup radioGroup = view_report.findViewById(R.id.dialogReport_RG);

                    IV_screenshot.setImageURI(null);
                    IV_screenshot.setImageURI(uri);

                    radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(RadioGroup group, int checkedId) {
                            switch (checkedId) {
                                case -1:
                                    break;
                                case R.id.dialogReport_RB_1:
                                    reason[0] = "спам или флуд";
                                    break;
                                case R.id.dialogReport_RB_2:
                                    reason[0] = "размещение материалов рекламного, эротического, порнографического или оскорбительного характера или иной информации, размещение которой запрещено или противоречит нормам действующего законодательства РФ";
                                    break;
                                case R.id.dialogReport_RB_3:
                                    reason[0] = "распространение информации, которая направлена на пропаганду войны, разжигание национальной, расовой или религиозной ненависти и вражды или иной информации, за распространение которой предусмотрена уголовная или административная ответственность";
                                    break;
                                case R.id.dialogReport_RB_4:
                                    reason[0] = "игра против/не в интересах своей команды";
                                    break;
                                case R.id.dialogReport_RB_5:
                                    reason[0] = "фарм (т.е. ведение игры организованной группой лиц, цель которой направлена на быстрое извлечение прибыли вне зависимости от того, кто из участников группы победит)";
                                    break;
                                case R.id.dialogReport_RB_6:
                                    reason[0] = "создание нескольких учётных записей в Приложении, фактически принадлежащих одному и тому же лицу";
                                    break;
                                case R.id.dialogReport_RB_7:
                                    reason[0] = "совершение действий, направленный на введение других Пользователей в заблуждение (не касается игрового процесса)";
                                    break;
                                case R.id.dialogReport_RB_8:
                                    reason[0] = "модератор/администратор злоупотребляет своими полномочиями или положением";
                                    break;
                                case R.id.dialogReport_RB_9:
                                    reason[0] = "другое";
                                    break;

                                default:
                                    break;
                            }
                        }
                    });

                    btn_report.setOnClickListener(v22 -> {
                        final JSONObject json2 = new JSONObject();
                        try {
                            json2.put("nick", MainActivity.NickName);
                            json2.put("session_id", MainActivity.Session_id);
                            json2.put("against_id", report_id);
                            json2.put("against_nick", report_nick);
                            json2.put("reason", reason[0]);
                            json2.put("comment", ET_report_message.getText());
                            json2.put("image", base64_screenshot);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        socket.emit("send_complaint", json2);
                        Log.d("kkk", "Socket_отправка - send_complaint" + json2);
                        alert2.cancel();
                    });

                    btn_add_screenshot.setVisibility(View.GONE);

                    alert2.show();
                }
        }
    }

    public byte[] getDownsizedImageBytes(Bitmap fullBitmap, int scaleWidth, int scaleHeight) throws IOException {

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(fullBitmap, scaleWidth, scaleHeight, true);

        // 2. Instantiate the downsized image content as a byte[]
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] downsizedImageBytes = baos.toByteArray();

        return downsizedImageBytes;
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
                int money = 0, exp = 0, gold = 0, rang = 0;
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
                    rang = data.getInt("rang");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (avatar != null) {
                    IV_avatar.setImageBitmap(fromBase64(avatar));
                }

                TV_money.setText(String.valueOf(money));
                TV_exp.setText(String.valueOf(exp));
                TV_gold.setText(String.valueOf(gold));
                TV_rang.setText(String.valueOf(rang));
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
                report_nick = nick;
                report_id = user_id_2;
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

                    IV_avatar.setOnClickListener(v12 -> {
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
                        View view_avatar = getLayoutInflater().inflate(R.layout.dialog_avatar, null);
                        builder2.setView(view_avatar);

                        ImageView IV_dialog_avatar = view_avatar.findViewById(R.id.dialogAvatar_avatar);
                        Button btn_exit_avatar = view_avatar.findViewById(R.id.dialogAvatar_btn_exit);

                        IV_dialog_avatar.setImageBitmap(fromBase64(finalAvatar));

                        AlertDialog alert2 = builder2.create();

                        btn_exit_avatar.setOnClickListener(v13 -> {
                            alert2.cancel();
                        });

                        alert2.show();
                    });

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
                        View view_report = getLayoutInflater().inflate(R.layout.dialog_report, null);
                        builder2.setView(view_report);
                        AlertDialog alert2 = builder2.create();

                        Button btn_add_screenshot = view_report.findViewById(R.id.dialogReport_btn_add_screenshot);
                        Button btn_report = view_report.findViewById(R.id.dialogReport_btn_report);
                        ImageView IV_screenshot = view_report.findViewById(R.id.dialogReport_IV_screenshot);
                        EditText ET_report_message = view_report.findViewById(R.id.dialogReport_ET_report);

                        final String[] reason = {""};

                        RadioGroup radioGroup = view_report.findViewById(R.id.dialogReport_RG);

                        btn_add_screenshot.setOnClickListener(v2 -> {
                            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                            photoPickerIntent.setType("image/*");
                            startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
                            alert2.cancel();
                        });

                        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(RadioGroup group, int checkedId) {
                                switch (checkedId) {
                                    case -1:
                                        break;
                                    case R.id.dialogReport_RB_1:
                                        reason[0] = "спам или флуд";
                                        break;
                                    case R.id.dialogReport_RB_2:
                                        reason[0] = "размещение материалов рекламного, эротического, порнографического или оскорбительного характера или иной информации, размещение которой запрещено или противоречит нормам действующего законодательства РФ";
                                        break;
                                    case R.id.dialogReport_RB_3:
                                        reason[0] = "распространение информации, которая направлена на пропаганду войны, разжигание национальной, расовой или религиозной ненависти и вражды или иной информации, за распространение которой предусмотрена уголовная или административная ответственность";
                                        break;
                                    case R.id.dialogReport_RB_4:
                                        reason[0] = "игра против/не в интересах своей команды";
                                        break;
                                    case R.id.dialogReport_RB_5:
                                        reason[0] = "фарм (т.е. ведение игры организованной группой лиц, цель которой направлена на быстрое извлечение прибыли вне зависимости от того, кто из участников группы победит)";
                                        break;
                                    case R.id.dialogReport_RB_6:
                                        reason[0] = "создание нескольких учётных записей в Приложении, фактически принадлежащих одному и тому же лицу";
                                        break;
                                    case R.id.dialogReport_RB_7:
                                        reason[0] = "совершение действий, направленный на введение других Пользователей в заблуждение (не касается игрового процесса)";
                                        break;
                                    case R.id.dialogReport_RB_8:
                                        reason[0] = "модератор/администратор злоупотребляет своими полномочиями или положением";
                                        break;
                                    case R.id.dialogReport_RB_9:
                                        reason[0] = "другое";
                                        break;

                                    default:
                                        break;
                                }
                            }
                        });

                        btn_report.setOnClickListener(v22 -> {
                            final JSONObject json2 = new JSONObject();
                            try {
                                json2.put("nick", MainActivity.NickName);
                                json2.put("session_id", MainActivity.Session_id);
                                json2.put("against_id", finalUser_id_);
                                json2.put("against_nick", finalNick);
                                json2.put("reason", reason[0]);
                                json2.put("comment", ET_report_message.getText());
                                json2.put("image", base64_screenshot);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            socket.emit("send_complaint", json2);
                            Log.d("kkk", "Socket_отправка - send_complaint" + json2);
                            alert2.cancel();
                        });

                        radioGroup.setVisibility(View.GONE);
                        btn_report.setVisibility(View.GONE);
                        ET_report_message.setVisibility(View.GONE);

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
