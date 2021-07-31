package com.mafiago.small_fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mafiago.R;
import com.mafiago.MainActivity;
import com.mafiago.fragments.StartFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import io.socket.emitter.Emitter;

import static android.app.Activity.RESULT_OK;
import static com.mafiago.MainActivity.socket;

public class SettingsMainFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";

    private int mPage;

    static final int GALLERY_REQUEST = 1;

    Button btnReportError;
    Button btnExitAccount;
    Button btnShowRole;
    Button btnSelectTheme;

    TextView TV_role;
    TextView TV_message;
    TextView TV_usersAgreement;
    TextView TV_privacyPolicy;

    public String base64_screenshot = "";

    View view_reportError;
    ImageView IV_screen;
    Button btn_addScreen;
    EditText ET_message;
    Button btm_sendError;

    ////////////////

    TextView TV_nick;
    TextView TV_rang;
    TextView TV_money;
    TextView TV_exp;
    TextView TV_gold;
    ImageView IV_avatar;

    TextView TV_game_counter;
    TextView TV_max_money_score;
    TextView TV_max_exp_score;
    TextView TV_general_pers_of_wins;
    TextView TV_mafia_pers_of_wins;
    TextView TV_peaceful_pers_of_wins;

    Button btnChangeAvatar;
    Button btnChangeNick;
    Button btnChangePassword;

    public static final String APP_PREFERENCES = "user";
    public static final String APP_PREFERENCES_EMAIL = "email";
    public static final String APP_PREFERENCES_PASSWORD = "password";
    public static final String APP_PREFERENCES_LAST_ROLE = "role";
    public static final String APP_PREFERENCES_SHOW_ROLE= "show_role";

    private SharedPreferences mSettings;

    public static SettingsMainFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        SettingsMainFragment fragment = new SettingsMainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPage = getArguments().getInt(ARG_PAGE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view;

        mSettings = getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        if (mPage == 1)
        {
            view = inflater.inflate(R.layout.fragment_settings_main, container, false);
            view_reportError = inflater.inflate(R.layout.dialog_report_error, container, false);
            IV_screen = view_reportError.findViewById(R.id.dialogReportError_IV_screen);
            btn_addScreen = view_reportError.findViewById(R.id.dialogReportError_btn_addScreen);
            ET_message = view_reportError.findViewById(R.id.dialogChangePassword_ET_newPassword2);
            btm_sendError = view_reportError.findViewById(R.id.dialogChangeNick_btn_changeNick);

            btnReportError = view.findViewById(R.id.fragmentSettingsProfile_btn_changeAvatar);
            btnExitAccount = view.findViewById(R.id.fragmentSettingsProfile_btn_changeNick);
            btnShowRole = view.findViewById(R.id.fragmentSettingsMain_btn_showRole);
            btnSelectTheme = view.findViewById(R.id.fragmentSettingsProfile_btn_changePassword);
            TV_role = view.findViewById(R.id.fragmentSettingsMain_TV_role);
            TV_message = view.findViewById(R.id.fragmentSettingsMain_TV_message);
            TV_usersAgreement = view.findViewById(R.id.fragmentSettingsMain_TV_usersAgreement);
            TV_privacyPolicy = view.findViewById(R.id.fragmentSettingsMain_TV_privacyPolicy);

            socket.off("send_problem");

            socket.on("send_problem", onSendProblem);

            if (!MainActivity.Role.equals("user")) {
                boolean showRole = mSettings.getBoolean(APP_PREFERENCES_SHOW_ROLE, true);
                btnShowRole.setVisibility(View.VISIBLE);
                TV_message.setVisibility(View.VISIBLE);
                TV_message.setText(MainActivity.NickName + ": Привет!");
                if (showRole)
                {
                    TV_role.setVisibility(View.VISIBLE);
                    btnShowRole.setText("Скрыть роль");
                    switch (MainActivity.Role)
                    {
                        case "moderator":
                            TV_role.setText("модератор");
                            TV_role.setTextColor(Color.parseColor("#C71585"));
                            break;
                        case "admin":
                            TV_role.setText("админ");
                            TV_role.setTextColor(Color.parseColor("#FF0000"));
                            break;
                        case "head_admin":
                            TV_role.setText("глав. админ");
                            TV_role.setTextColor(Color.parseColor("#008B8B"));
                            break;
                        case "designer":
                            TV_role.setText("дизайнер");
                            TV_role.setTextColor(Color.parseColor("#8A2BE2"));
                            break;
                        case "developer":
                            TV_role.setText("разработчик");
                            TV_role.setTextColor(Color.parseColor("#8B0000"));
                            break;
                    }
                }
            }

            btnReportError.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                if (view_reportError.getParent() != null) {
                    ((ViewGroup) view_reportError.getParent()).removeView(view_reportError);
                }
                ET_message.setText("");
                builder.setView(view_reportError);
                AlertDialog alert = builder.create();
                btn_addScreen.setOnClickListener(v1 -> {
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
                });
                btm_sendError.setOnClickListener(v12 -> {
                    if (!base64_screenshot.equals("")) {
                        final JSONObject json = new JSONObject();
                        try {
                            json.put("nick", MainActivity.NickName);
                            json.put("session_id", MainActivity.Session_id);
                            json.put("comment", ET_message.getText());
                            json.put("image", base64_screenshot);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        socket.emit("send_problem", json);
                        Log.d("kkk", "Socket_отправка - send_problem" + json);
                        alert.cancel();
                    }
                    else
                    {
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(getContext());
                        builder2.setTitle("Вставьте скриншот!")
                                .setMessage("")
                                .setIcon(R.drawable.ic_error)
                                .setCancelable(false)
                                .setNegativeButton("Ок",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });
                        AlertDialog alert2 = builder2.create();
                        alert2.show();
                    }
                });
                alert.show();
            });

            btnExitAccount.setOnClickListener(v -> {
                socket.emit("leave_app", "");
                Log.d("kkk", "Socket_отправка - leave_app");
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new StartFragment()).commit();
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putString(APP_PREFERENCES_EMAIL, null);
                editor.putString(APP_PREFERENCES_PASSWORD, null);
                editor.apply();
            });

            btnShowRole.setOnClickListener(v -> {
                boolean showRole = mSettings.getBoolean(APP_PREFERENCES_SHOW_ROLE, true);
                if (showRole)
                {
                    btnShowRole.setText("Показать роль");
                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putBoolean(APP_PREFERENCES_SHOW_ROLE, false);
                    editor.apply();
                    TV_role.setVisibility(View.GONE);
                }
                else {
                    btnShowRole.setText("Скрыть роль");
                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putBoolean(APP_PREFERENCES_SHOW_ROLE, true);
                    editor.apply();
                    TV_role.setVisibility(View.VISIBLE);
                    switch (MainActivity.Role) {
                        case "moderator":
                            TV_role.setText("модератор");
                            TV_role.setTextColor(Color.parseColor("#C71585"));
                            break;
                        case "admin":
                            TV_role.setText("админ");
                            TV_role.setTextColor(Color.parseColor("#FF0000"));
                            break;
                        case "head_admin":
                            TV_role.setText("глав. админ");
                            TV_role.setTextColor(Color.parseColor("#008B8B"));
                            break;
                        case "designer":
                            TV_role.setText("дизайнер");
                            TV_role.setTextColor(Color.parseColor("#8A2BE2"));
                            break;
                        case "developer":
                            TV_role.setText("разработчик");
                            TV_role.setTextColor(Color.parseColor("#8B0000"));
                            break;
                    }
                }
                showRole = mSettings.getBoolean(APP_PREFERENCES_SHOW_ROLE, true);
                Log.d("kkk", String.valueOf(showRole));
            });

            btnSelectTheme.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("В разработке...")
                        .setMessage("")
                        .setIcon(R.drawable.ic_razrabotka)
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

            TV_usersAgreement.setOnClickListener(v -> {
                Intent mIntent = new Intent();
                mIntent.setAction(Intent.ACTION_VIEW);
                mIntent.setData(Uri.parse("https://multi-games-dev.wixsite.com/mafia-go/%D0%BF%D0%BE%D0%BB%D1%8C%D0%B7%D0%BE%D0%B2%D0%B0%D1%82%D0%B5%D0%BB%D1%8C%D1%81%D0%BA%D0%BE%D0%B5-%D1%81%D0%BE%D0%B3%D0%BB%D0%B0%D1%88%D0%B5%D0%BD%D0%B8%D0%B5"));
                startActivity(Intent.createChooser( mIntent, "Выберите браузер"));
            });

            TV_privacyPolicy.setOnClickListener(v -> {
                Intent mIntent = new Intent();
                mIntent.setAction(Intent.ACTION_VIEW);
                mIntent.setData(Uri.parse("https://multi-games-dev.wixsite.com/mafia-go/%D0%BF%D0%BE%D0%BB%D0%B8%D1%82%D0%B8%D0%BA%D0%B0-%D0%BA%D0%BE%D0%BD%D1%84%D0%B8%D0%B4%D0%B5%D0%BD%D1%86%D0%B8%D0%B0%D0%BB%D1%8C%D0%BD%D0%BE%D1%81%D1%82%D0%B8"));
                startActivity(Intent.createChooser( mIntent, "Выберите браузер"));
            });
        }
        else
        {
            view = inflater.inflate(R.layout.fragment_settings_profile, container, false);

            TV_nick = view.findViewById(R.id.fragmentSettingsProfile_TV_nick);
            TV_rang = view.findViewById(R.id.fragmentSettingsProfile_TV_rang);
            TV_money = view.findViewById(R.id.fragmentSettingsProfile_TV_money);
            TV_exp = view.findViewById(R.id.dialogYouHaveBeenBanned_TV_exp);
            TV_gold = view.findViewById(R.id.fragmentSettingsProfile_TV_gold);
            IV_avatar = view.findViewById(R.id.fragmentSettingsProfile_IV_avatar);

            TV_game_counter = view.findViewById(R.id.fragmentSettingsProfile_TV_gamesCount);
            TV_max_money_score = view.findViewById(R.id.fragmentSettingsProfile_TV_maxMoney);
            TV_max_exp_score = view.findViewById(R.id.fragmentSettingsProfile_TV_maxExp);
            TV_general_pers_of_wins = view.findViewById(R.id.fragmentSettingsProfile_TV_percentWins);
            TV_mafia_pers_of_wins = view.findViewById(R.id.fragmentSettingsProfile_TV_percentMafiaWins);
            TV_peaceful_pers_of_wins = view.findViewById(R.id.fragmentSettingsProfile_TV_percentPeacefulWins);

            btnChangeAvatar = view.findViewById(R.id.fragmentSettingsProfile_btn_changeAvatar);
            btnChangeNick = view.findViewById(R.id.fragmentSettingsProfile_btn_changeNick);
            btnChangePassword = view.findViewById(R.id.fragmentSettingsProfile_btn_changePassword);

            socket.off("get_profile");

            socket.on("get_profile", OnGetProfile);

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

            socket.on("edit_profile", onEditProfile);

            btnChangeAvatar.setOnClickListener(v -> {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
            });

            btnChangeNick.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                View viewChangeNick = inflater.inflate(R.layout.dialog_change_nick, container, false);
                builder.setView(viewChangeNick);
                AlertDialog alert = builder.create();
                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alert.show();
            });

            btnChangePassword.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                View viewChangeNick = inflater.inflate(R.layout.dialog_change_password, container, false);
                builder.setView(viewChangeNick);
                AlertDialog alert = builder.create();
                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alert.show();
            });
        }

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
                    if (mPage == 1)
                    {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                            byte[] bytes = stream.toByteArray();

                            base64_screenshot = Base64.encodeToString(bytes, Base64.DEFAULT);

                            IV_screen.setImageBitmap(fromBase64(base64_screenshot));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else {

                        try {
                        /*
                        Bitmap bitmap2 = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap2.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        Log.d("kkk", String.valueOf(bitmap2.getByteCount() / 1048576));
                        byte[] bytes = stream.toByteArray();
                        String base642 = Base64.encodeToString(bytes, Base64.DEFAULT);
                        //Log.d("kkk", String.valueOf(base642.length()));
                         */

                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                            int scaleDivider = 5;
                            int max = 0;
                            if (bitmap.getWidth() > bitmap.getHeight()) {
                                max = bitmap.getWidth();
                            } else {
                                max = bitmap.getHeight();
                            }
                            if (max <= 300) {
                                scaleDivider = 1;
                            } else if (max <= 600) {
                                scaleDivider = 2;
                            } else if (max <= 900) {
                                scaleDivider = 3;
                            } else if (max <= 1200) {
                                scaleDivider = 4;
                            } else if (max <= 1500) {
                                scaleDivider = 5;
                            } else if (max <= 1800) {
                                scaleDivider = 6;
                            } else if (max <= 2100) {
                                scaleDivider = 7;
                            } else if (max <= 2400) {
                                scaleDivider = 8;
                            } else if (max <= 2700) {
                                scaleDivider = 9;
                            } else if (max <= 3000) {
                                scaleDivider = 10;
                            } else if (max <= 3300) {
                                scaleDivider = 11;
                            } else if (max <= 3600) {
                                scaleDivider = 12;
                            } else if (max <= 3900) {
                                scaleDivider = 13;
                            } else if (max <= 4200) {
                                scaleDivider = 14;
                            }

                            int scaleWidth = bitmap.getWidth() / scaleDivider;
                            int scaleHeight = bitmap.getHeight() / scaleDivider;
                            Log.d("kkk", String.valueOf(scaleWidth));
                            Log.d("kkk", String.valueOf(scaleHeight));
                            byte[] downsizedImageBytes =
                                    getDownsizedImageBytes(bitmap, scaleWidth, scaleHeight);
                            String base64 = Base64.encodeToString(downsizedImageBytes, Base64.DEFAULT);
                            Log.d("kkk", String.valueOf("Длина = " + base64.length()));
                            if (base64.length() <= 524288) {
                                IV_avatar.setImageBitmap(fromBase64(base64));
                                final JSONObject json2 = new JSONObject();
                                try {
                                    json2.put("nick", MainActivity.NickName);
                                    json2.put("session_id", MainActivity.Session_id);
                                    json2.put("avatar", base64);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Log.d("kkk", "Socket_отправка - edit_profile - " + json2.toString());
                                socket.emit("edit_profile", json2);
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Слишком большое изображение!")
                                        .setMessage("")
                                        .setIcon(R.drawable.ic_error)
                                        .setCancelable(false)
                                        .setNegativeButton("Ок",
                                                (dialog, id) -> dialog.cancel());
                                AlertDialog alert = builder.create();
                                alert.show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
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

                TV_game_counter.setText("Сыграно игр " + String.valueOf(game_counter));
                TV_max_money_score.setText("Макс число монет " + String.valueOf(max_money_score));
                TV_max_exp_score.setText("Макс число опыта " + String.valueOf(max_exp_score));
                TV_general_pers_of_wins.setText("Процент побед " + general_pers_of_wins);
                TV_mafia_pers_of_wins.setText("Побед мафии " + mafia_pers_of_wins);
                TV_peaceful_pers_of_wins.setText("Побед мирных " + peaceful_pers_of_wins);
            }
        });
    };

    private final Emitter.Listener onEditProfile = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(() -> {
            JSONObject data = (JSONObject) args[0];
            String status = "";
            try {
                status = data.getString("status");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            AlertDialog.Builder builder;
            AlertDialog alert;
            switch (status)
            {
                case "OK":
                    builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Профиль успешно изменён!")
                            .setMessage("")
                            .setIcon(R.drawable.ic_ok)
                            .setCancelable(false)
                            .setNegativeButton("Ок",
                                    (dialog, id) -> dialog.cancel());
                    alert = builder.create();
                    break;
                default:
                    builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Что-то пошло не так!")
                            .setMessage("")
                            .setIcon(R.drawable.ic_error)
                            .setCancelable(false)
                            .setNegativeButton("Ок",
                                    (dialog, id) -> dialog.cancel());
                    alert = builder.create();
                    break;
            }
            alert.show();
            Log.d("kkk", "принял - edit_profile - " + data);
        });
    };

    private final Emitter.Listener onSendProblem = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(() -> {
            JSONObject data = (JSONObject) args[0];
            String status = "";
            try {
                status = data.getString("status");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            AlertDialog.Builder builder;
            AlertDialog alert;
            switch (status)
            {
                case "OK":
                    builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Вы успешно сообщили о проблеме!")
                            .setMessage("")
                            .setIcon(R.drawable.ic_ok)
                            .setCancelable(false)
                            .setNegativeButton("Ок",
                                    (dialog, id) -> dialog.cancel());
                    alert = builder.create();
                    break;
                case "problems_limit_exceeded":
                    builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Истрачен лимит ошибок!")
                            .setMessage("")
                            .setIcon(R.drawable.ic_ok)
                            .setCancelable(false)
                            .setNegativeButton("Ок",
                                    (dialog, id) -> dialog.cancel());
                    alert = builder.create();
                    break;
                default:
                    builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Что-то пошло не так!")
                            .setMessage("")
                            .setIcon(R.drawable.ic_error)
                            .setCancelable(false)
                            .setNegativeButton("Ок",
                                    (dialog, id) -> dialog.cancel());
                    alert = builder.create();
                    break;
            }
            alert.show();
            Log.d("kkk", "принял - edit_profile - " + data);
        });
    };
}