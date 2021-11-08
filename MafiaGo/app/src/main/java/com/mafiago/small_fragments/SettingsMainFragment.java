package com.mafiago.small_fragments;

import android.annotation.TargetApi;
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
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.mafiago.R;
import com.mafiago.MainActivity;
import com.mafiago.enums.Role;
import com.mafiago.fragments.GameFragment;
import com.mafiago.fragments.RulesFragment;
import com.mafiago.fragments.StartFragment;
import com.mafiago.fragments.StudyFragment;
import com.mafiago.models.RoleModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

import io.socket.emitter.Emitter;

import static android.app.Activity.RESULT_OK;
import static com.mafiago.MainActivity.f;
import static com.mafiago.MainActivity.nick;
import static com.mafiago.MainActivity.socket;

public class SettingsMainFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";

    private int mPage;

    static final int GALLERY_REQUEST = 1;

    Button btnReportError;
    Button btnExitAccount;
    Button btnSelectTheme;
    Button btnRules;

    TextView TV_usersAgreement;
    TextView TV_privacyPolicy;
    TextView TV_inviteCode;

    public String base64_screenshot = "";

    View view_reportError;
    ImageView IV_screen;
    Button btn_addScreen;
    EditText ET_message;
    Button btm_sendError;
    RelativeLayout RL_copy;

    ////////////////

    TextView TV_nick;
    TextView TV_rang;
    TextView TV_money;
    TextView TV_exp;
    TextView TV_gold;
    ImageView IV_avatar;

    Spinner spinner;
    Spinner spinner2;

    TextView TV_game_counter;
    TextView TV_max_money_score;
    TextView TV_max_exp_score;
    TextView TV_general_pers_of_wins;
    TextView TV_mafia_pers_of_wins;
    TextView TV_peaceful_pers_of_wins;

    Button btnChangeAvatar;
    Button btnChangeNick;
    Button btnChangePassword;
    Button btnStudy;

    public static final String APP_PREFERENCES = "user";
    public static final String APP_PREFERENCES_EMAIL = "email";
    public static final String APP_PREFERENCES_PASSWORD = "password";
    public static final String APP_PREFERENCES_LAST_ROLE = "role";
    public static final String APP_PREFERENCES_SHOW_ROLE= "show_role";
    public static final String APP_PREFERENCES_THEME= "theme";

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
            view = inflater.inflate(R.layout.fragment_settings_profile, container, false);

            TV_nick = view.findViewById(R.id.fragmentSettingsProfile_TV_nick);
            TV_rang = view.findViewById(R.id.fragmentSettingsProfile_TV_rang);
            TV_money = view.findViewById(R.id.fragmentSettingsProfile_TV_money);
            TV_exp = view.findViewById(R.id.dialogYouHaveBeenBanned_TV_exp);
            TV_gold = view.findViewById(R.id.fragmentSettingsProfile_TV_gold);
            IV_avatar = view.findViewById(R.id.fragmentSettingsProfile_IV_avatar);
            spinner = view.findViewById(R.id.fragmentSettingsProfile_Spinner_statuce);
            spinner2 = view.findViewById(R.id.fragmentSettingsProfile_Spinner_color);

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
            socket.off("edit_profile");

            socket.on("get_profile", OnGetProfile);
            socket.on("edit_profile", onEditProfile);

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

            btnChangeAvatar.setOnClickListener(v -> {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
            });

            btnChangeNick.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                View viewChangeNick = inflater.inflate(R.layout.dialog_change_nick, container, false);
                builder.setView(viewChangeNick);

                EditText ET_nick = viewChangeNick.findViewById(R.id.dialogChangeNick_ET_newNick);
                Button btn_changeNick = viewChangeNick.findViewById(R.id.dialogChangeNick_btn_changeNick);

                btn_changeNick.setOnClickListener(v13 -> {
                    String nick = ET_nick.getText().toString();

                    Log.e("kkk", nick);
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
                    Log.e("kkk", nick);
                    Log.e("kkk", String.valueOf(nick.length()));
                    if (nick.length() >= 3)
                    {
                        if (nick.length() <= 15) {
                            if (!nick.contains(".")) {
                                final JSONObject json2 = new JSONObject();
                                try {
                                    json2.put("nick", MainActivity.NickName);
                                    json2.put("session_id", MainActivity.Session_id);
                                    json2.put("new_nick", nick);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Log.d("kkk", "Socket_отправка - edit_profile - " + json2.toString());
                                socket.emit("edit_profile", json2);
                            } else {
                                AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
                                View viewDang = getLayoutInflater().inflate(R.layout.dialog_error, null);
                                builder2.setView(viewDang);
                                TextView TV_title = viewDang.findViewById(R.id.dialogError_TV_errorTitle);
                                TextView TV_error = viewDang.findViewById(R.id.dialogError_TV_errorText);
                                TV_title.setText("Некорректный символ!");
                                TV_error.setText("Нельзя использовать точку в нике");
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
                });

                AlertDialog alert = builder.create();
                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alert.show();
            });

            btnChangePassword.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                View viewChangePas = inflater.inflate(R.layout.dialog_change_password, container, false);
                builder.setView(viewChangePas);

                Button btn_changePasswordInAlert = viewChangePas.findViewById(R.id.dialogChangePassword_btn_changePassword);
                EditText ET_oldPassword = viewChangePas.findViewById(R.id.dialogChangePassword_ET_oldPassword);
                EditText ET_newPassword1 = viewChangePas.findViewById(R.id.dialogChangePassword_ET_newPassword1);
                EditText ET_newPassword2 = viewChangePas.findViewById(R.id.dialogChangePassword_ET_newPassword2);

                btn_changePasswordInAlert.setOnClickListener(v14 -> {
                    if (ET_newPassword1.getText().toString().equals(ET_newPassword2.getText().toString()) &&
                            !ET_newPassword1.getText().toString().trim().equals("") &&
                            ET_newPassword1.length() >= 7 &&
                            ET_newPassword1.length() <= 20) {
                        final JSONObject json2 = new JSONObject();
                        try {
                            json2.put("nick", MainActivity.NickName);
                            json2.put("session_id", MainActivity.Session_id);
                            json2.put("new_password", ET_newPassword1.getText());
                            json2.put("password", ET_oldPassword.getText());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d("kkk", "Socket_отправка - edit_profile - " + json2.toString());
                        socket.emit("edit_profile", json2);
                    }
                    else {
                        if (!ET_newPassword1.getText().toString().equals(ET_newPassword2.getText().toString())) {
                            AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
                            View viewDang = getLayoutInflater().inflate(R.layout.dialog_error, null);
                            builder2.setView(viewDang);
                            TextView TV_title = viewDang.findViewById(R.id.dialogError_TV_errorTitle);
                            TextView TV_error = viewDang.findViewById(R.id.dialogError_TV_errorText);
                            TV_title.setText("Ошибка!");
                            TV_error.setText("Ваши новые пароли не совпадают!");
                            AlertDialog alert2 = builder2.create();
                            alert2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            alert2.show();
                        } else if (ET_newPassword1.getText().toString().trim().equals("")) {
                            AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
                            View viewDang = getLayoutInflater().inflate(R.layout.dialog_error, null);
                            builder2.setView(viewDang);
                            TextView TV_title = viewDang.findViewById(R.id.dialogError_TV_errorTitle);
                            TextView TV_error = viewDang.findViewById(R.id.dialogError_TV_errorText);
                            TV_title.setText("Ошибка!");
                            TV_error.setText("Ваш пароль не может быть пустым!");
                            AlertDialog alert2 = builder2.create();
                            alert2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            alert2.show();
                        } else if (ET_newPassword1.length() < 7) {
                            AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
                            View viewDang = getLayoutInflater().inflate(R.layout.dialog_error, null);
                            builder2.setView(viewDang);
                            TextView TV_title = viewDang.findViewById(R.id.dialogError_TV_errorTitle);
                            TextView TV_error = viewDang.findViewById(R.id.dialogError_TV_errorText);
                            TV_title.setText("Ошибка!");
                            TV_error.setText("Ваш пароль слишком короткий!");
                            AlertDialog alert2 = builder2.create();
                            alert2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            alert2.show();
                        } else if (ET_newPassword1.length() > 20) {
                            AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
                            View viewDang = getLayoutInflater().inflate(R.layout.dialog_error, null);
                            builder2.setView(viewDang);
                            TextView TV_title = viewDang.findViewById(R.id.dialogError_TV_errorTitle);
                            TextView TV_error = viewDang.findViewById(R.id.dialogError_TV_errorText);
                            TV_title.setText("Ошибка!");
                            TV_error.setText("Ваш пароль слишком длинный!");
                            AlertDialog alert2 = builder2.create();
                            alert2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            alert2.show();
                        }
                    }
                });

                AlertDialog alert = builder.create();
                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alert.show();


            });
        }
        else
        {
            view = inflater.inflate(R.layout.fragment_settings_main, container, false);
            view_reportError = inflater.inflate(R.layout.dialog_report_error, container, false);
            IV_screen = view_reportError.findViewById(R.id.dialogReportError_IV_screen);
            btn_addScreen = view_reportError.findViewById(R.id.dialogReportError_btn_addScreen);
            ET_message = view_reportError.findViewById(R.id.dialogChangePassword_ET_newPassword2);
            btm_sendError = view_reportError.findViewById(R.id.dialogChangeNick_btn_changeNick);

            btnReportError = view.findViewById(R.id.fragmentSettingsProfile_btn_changeAvatar);
            btnExitAccount = view.findViewById(R.id.fragmentSettingsProfile_btn_changeNick);
            btnSelectTheme = view.findViewById(R.id.fragmentSettingsMain_chooseTheme);
            btnStudy = view.findViewById(R.id.fragmentSettingsProfile_btn_study);
            btnRules = view.findViewById(R.id.fragmentSettingsProfile_btn_rules);
            TV_usersAgreement = view.findViewById(R.id.fragmentSettingsMain_TV_usersAgreement);
            TV_privacyPolicy = view.findViewById(R.id.fragmentSettingsMain_TV_privacyPolicy);
            TV_inviteCode = view.findViewById(R.id.fragmentSettingsMain_TV_inviteCode);
            RL_copy = view.findViewById(R.id.fragmentSettingsMain_RL_copy);

            socket.off("send_problem");

            socket.on("send_problem", onSendProblem);

            TV_inviteCode.setText("Пригласительный код для друзей: " + MainActivity.MyInviteCode + "\nВаш друг может указать его при регистрации и сыграть 50 игр - тогда вы оба получите по 100 золота");

            btnStudy.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                View viewDang = getLayoutInflater().inflate(R.layout.dialog_custom_roles, null);
                builder.setView(viewDang);
                ImageView citizen = viewDang.findViewById(R.id.dialogCustomRoles_citizen);
                ImageView sheriff = viewDang.findViewById(R.id.dialogCustomRoles_sheriff);
                ImageView doctor = viewDang.findViewById(R.id.dialogCustomRoles_doctor);
                ImageView lover = viewDang.findViewById(R.id.dialogCustomRoles_lover);
                ImageView journalist = viewDang.findViewById(R.id.dialogCustomRoles_journalist);
                ImageView bodyguard = viewDang.findViewById(R.id.dialogCustomRoles_bodyguard);
                ImageView maniac = viewDang.findViewById(R.id.dialogCustomRoles_maniac);
                ImageView doctor_of_easy_virtue = viewDang.findViewById(R.id.dialogCustomRoles_doctor_of_easy_virtue);
                ImageView IV_mafia = viewDang.findViewById(R.id.dialogCustomRoles_mafia);
                ImageView mafia_don = viewDang.findViewById(R.id.dialogCustomRoles_mafia_don);
                ImageView terrorist = viewDang.findViewById(R.id.dialogCustomRoles_terrorist);
                ImageView poisoner = viewDang.findViewById(R.id.dialogCustomRoles_poisoner);
                TextView TV_choose = viewDang.findViewById(R.id.dialogCustomRoles_TV_choose);

                citizen.setImageResource(R.drawable.citizen_dead);
                sheriff.setImageResource(R.drawable.sheriff_dead);
                doctor.setImageResource(R.drawable.doctor_dead);
                lover.setImageResource(R.drawable.lover_dead);
                journalist.setImageResource(R.drawable.journalist_dead);
                bodyguard.setImageResource(R.drawable.bodyguard_dead);
                maniac.setImageResource(R.drawable.maniac_dead);
                doctor_of_easy_virtue.setImageResource(R.drawable.doctor_of_easy_virtue_dead);mafia_don.setImageResource(R.drawable.mafia_don_dead);
                terrorist.setImageResource(R.drawable.terrorist_dead);
                poisoner.setImageResource(R.drawable.poisoner_dead);

                TV_choose.setText("Выберите роль для обучения");

                AlertDialog alert = builder.create();

                citizen.setOnClickListener(v1 -> {
                    /*
                    StudyFragment studyFragment = StudyFragment.newInstance("citizen");
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, studyFragment).commit();
                    alert.cancel();
                     */
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(getContext());
                    builder2.setTitle("В разработке...")
                            .setMessage("")
                            .setIcon(R.drawable.ic_razrabotka)
                            .setCancelable(false)
                            .setNegativeButton("Ок",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert2 = builder2.create();
                    alert2.show();
                });

                sheriff.setOnClickListener(v1 -> {
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(getContext());
                    builder2.setTitle("В разработке...")
                            .setMessage("")
                            .setIcon(R.drawable.ic_razrabotka)
                            .setCancelable(false)
                            .setNegativeButton("Ок",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert2 = builder2.create();
                    alert2.show();
                });

                doctor.setOnClickListener(v1 -> {
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(getContext());
                    builder2.setTitle("В разработке...")
                            .setMessage("")
                            .setIcon(R.drawable.ic_razrabotka)
                            .setCancelable(false)
                            .setNegativeButton("Ок",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert2 = builder2.create();
                    alert2.show();
                });

                lover.setOnClickListener(v1 -> {
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(getContext());
                    builder2.setTitle("В разработке...")
                            .setMessage("")
                            .setIcon(R.drawable.ic_razrabotka)
                            .setCancelable(false)
                            .setNegativeButton("Ок",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert2 = builder2.create();
                    alert2.show();;
                });

                journalist.setOnClickListener(v1 -> {
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(getContext());
                    builder2.setTitle("В разработке...")
                            .setMessage("")
                            .setIcon(R.drawable.ic_razrabotka)
                            .setCancelable(false)
                            .setNegativeButton("Ок",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert2 = builder2.create();
                    alert2.show();
                });

                bodyguard.setOnClickListener(v1 -> {
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(getContext());
                    builder2.setTitle("В разработке...")
                            .setMessage("")
                            .setIcon(R.drawable.ic_razrabotka)
                            .setCancelable(false)
                            .setNegativeButton("Ок",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert2 = builder2.create();
                    alert2.show();
                });

                maniac.setOnClickListener(v1 -> {
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(getContext());
                    builder2.setTitle("В разработке...")
                            .setMessage("")
                            .setIcon(R.drawable.ic_razrabotka)
                            .setCancelable(false)
                            .setNegativeButton("Ок",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert2 = builder2.create();
                    alert2.show();
                });

                doctor_of_easy_virtue.setOnClickListener(v1 -> {
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(getContext());
                    builder2.setTitle("В разработке...")
                            .setMessage("")
                            .setIcon(R.drawable.ic_razrabotka)
                            .setCancelable(false)
                            .setNegativeButton("Ок",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert2 = builder2.create();
                    alert2.show();
                });

                IV_mafia.setOnClickListener(v1 -> {
                    StudyFragment studyFragment = StudyFragment.newInstance("mafia");
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, studyFragment).commit();
                    alert.cancel();
                });

                mafia_don.setOnClickListener(v1 -> {
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(getContext());
                    builder2.setTitle("В разработке...")
                            .setMessage("")
                            .setIcon(R.drawable.ic_razrabotka)
                            .setCancelable(false)
                            .setNegativeButton("Ок",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert2 = builder2.create();
                    alert2.show();
                });

                terrorist.setOnClickListener(v1 -> {
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(getContext());
                    builder2.setTitle("В разработке...")
                            .setMessage("")
                            .setIcon(R.drawable.ic_razrabotka)
                            .setCancelable(false)
                            .setNegativeButton("Ок",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert2 = builder2.create();
                    alert2.show();
                });

                poisoner.setOnClickListener(v1 -> {
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(getContext());
                    builder2.setTitle("В разработке...")
                            .setMessage("")
                            .setIcon(R.drawable.ic_razrabotka)
                            .setCancelable(false)
                            .setNegativeButton("Ок",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert2 = builder2.create();
                    alert2.show();
                });

                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alert.show();
            });

            btnRules.setOnClickListener(v -> {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new RulesFragment()).commit();
            });

            RL_copy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    copyText(String.valueOf(MainActivity.MyInviteCode));
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    View viewDang = getLayoutInflater().inflate(R.layout.dialog_information, null);
                    builder.setView(viewDang);
                    TextView TV_title = viewDang.findViewById(R.id.dialogInformation_TV_title);
                    TextView TV_text = viewDang.findViewById(R.id.dialogInformation_TV_text);
                    TV_title.setText("Код скопирован!");
                    TV_text.setText("Пригласительный код успешно скопирован в буфер обмена");
                    AlertDialog alert = builder.create();
                    alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alert.show();
                }
            });

            String theme = mSettings.getString(APP_PREFERENCES_THEME, "dark");
            Log.e("kkk", theme);
            if (theme.equals("dark"))
            {
                btnSelectTheme.setText("Выбрать светлую тему");
            }
            else
            {
                btnSelectTheme.setText("Выбрать тёмную тему");
            }

            btnSelectTheme.setOnClickListener(v -> {
                SharedPreferences.Editor editor = mSettings.edit();
                if (theme.equals("dark")) {
                    editor.putString(APP_PREFERENCES_THEME, "light");
                }
                else {
                    editor.putString(APP_PREFERENCES_THEME, "dark");
                }
                editor.apply();
                reset();
            });

            /*
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
             */

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
                    if (!base64_screenshot.equals("") && ET_message.length() != 0) {
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
                        builder2.setTitle("Заполните все поля!")
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
                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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


        return view;
    }

    private void reset()
    {
        Intent intent = new Intent(getContext(), MainActivity.class);
        startActivity(intent);
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
                    else
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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void copyText(String copiedText) {
        int sdk = android.os.Build.VERSION.SDK_INT;
        if(sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(copiedText);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("TAG",copiedText);
            clipboard.setPrimaryClip(clip);
        }
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
                JSONObject statuses = new JSONObject();
                JSONObject JO_colors = new JSONObject();
                String[] list_statuses;
                String[] list_normal_statuses;
                String[] list_colors;
                int game_counter = 0, max_money_score = 0, max_exp_score = 0;
                String general_pers_of_wins = "", mafia_pers_of_wins = "", peaceful_pers_of_wins = "", user_id_2 = "", main_status = "", main_personal_color = "";
                Log.d("kkk", "принял - get_profile - " + data);

                try {
                    statistic = data.getJSONObject("statistics");
                    statuses = data.getJSONObject("statuses");
                    JO_colors = data.getJSONObject("personal_colors");
                    game_counter = statistic.getInt("game_counter");
                    max_money_score = statistic.getInt("max_money_score");
                    max_exp_score = statistic.getInt("max_exp_score");
                    general_pers_of_wins = statistic.getString("general_wins");
                    mafia_pers_of_wins = statistic.getString("mafia_wins");
                    peaceful_pers_of_wins = statistic.getString("peaceful_wins");
                    avatar = data.getString("avatar");
                    user_id_2 = data.getString("user_id");
                    online = data.getBoolean("is_online");
                    gold = data.getInt("gold");
                    nick = data.getString("nick");
                    money = data.getInt("money");
                    exp = data.getInt("exp");
                    rang = data.getInt("rang");
                    main_status = data.getString("main_status");
                    main_personal_color = data.getString("main_personal_color");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (avatar != null && !avatar.equals("") && !avatar.equals("null")) {
                    IV_avatar.setImageBitmap(fromBase64(avatar));
                }

                TV_money.setText(String.valueOf(money));
                TV_exp.setText(String.valueOf(exp));
                TV_gold.setText(String.valueOf(gold));
                TV_rang.setText(String.valueOf(rang));
                TV_nick.setText(nick);

                TV_game_counter.setText("Сыграно игр: " + String.valueOf(game_counter));
                TV_max_money_score.setText("Макс число монет: " + String.valueOf(max_money_score));
                TV_max_exp_score.setText("Макс число опыта: " + String.valueOf(max_exp_score));
                TV_general_pers_of_wins.setText("Процент побед: " + general_pers_of_wins);
                TV_mafia_pers_of_wins.setText("Побед мафии: " + mafia_pers_of_wins);
                TV_peaceful_pers_of_wins.setText("Побед мирных: " + peaceful_pers_of_wins);

                list_statuses = new String[statuses.length() + 1];
                list_normal_statuses = new String[statuses.length() + 1];
                list_statuses[0] = "нет";
                list_normal_statuses[0] = "нет";
                int i;
                Iterator iterator;
                String status;

                for (iterator = statuses.keys(), i = 1; iterator.hasNext(); i++)
                {
                    status = (String) iterator.next();
                    list_normal_statuses[i] = status;
                    try {
                        if (!statuses.getString(status).equals("forever"))
                        {
                            JSONObject JO_status = statuses.getJSONObject(status);
                            String time = "";
                            if (JO_status.getInt("months") == 0) {
                                if (JO_status.getInt("days") == 0) {
                                    if (JO_status.getInt("hours") == 0) {
                                        if (JO_status.getInt("minutes") == 0) {
                                            time = " (" + JO_status.getInt("seconds") + " сек.)";
                                        } else {
                                            time = " (" + JO_status.getInt("minutes") + " мин.)";
                                        }
                                    } else {
                                        time = " (" + JO_status.getInt("hours") + " ч.)";
                                    }
                                } else {
                                    time = " (" + JO_status.getInt("days") + " д.)";
                                }
                            } else {
                                time = " (" + JO_status.getInt("months") + " мес.)";
                            }
                            status = status + time;
                        }
                        list_statuses[i] = status;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                if (list_statuses.length != 0) {
                    final boolean[] need_to_send = {false};
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, list_statuses);
                    // Вызываем адаптер
                    spinner.setAdapter(spinnerArrayAdapter);

                    for (int k = 0; k < list_normal_statuses.length; k++) {
                        if (list_normal_statuses[k].equals(main_status)) {
                            spinner.setSelection(k);
                        }
                    }

                    TV_nick.setText(MainActivity.NickName);

                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (need_to_send[0]) {
                                final JSONObject json2 = new JSONObject();
                                try {
                                    json2.put("nick", MainActivity.NickName);
                                    json2.put("session_id", MainActivity.Session_id);
                                    if (position == 0) {
                                        json2.put("main_status", "");
                                    }
                                    else
                                    {
                                        json2.put("main_status", list_normal_statuses[position]);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Log.d("kkk", "Socket_отправка - edit_profile - " + json2.toString());
                                socket.emit("edit_profile", json2);
                            }
                            else
                            {
                                need_to_send[0] = true;
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }
                else
                {
                    spinner.setVisibility(View.INVISIBLE);
                }

                String[] list_colors_words = new String[JO_colors.length() + 1];
                list_colors = new String[JO_colors.length() + 1];
                list_colors[0] = "нет";
                list_colors_words[0] = "нет";
                int j;
                String color;

                for (iterator = JO_colors.keys(), j = 1; iterator.hasNext(); j++)
                {
                    String time = "";
                    color = (String) iterator.next();
                    list_colors[j] = color;
                    try {
                        if (!JO_colors.getString(color).equals("forever"))
                        {
                            JSONObject JO_color_time = JO_colors.getJSONObject(color);
                            if (JO_color_time.getInt("months") == 0) {
                                if (JO_color_time.getInt("days") == 0) {
                                    if (JO_color_time.getInt("hours") == 0) {
                                        if (JO_color_time.getInt("minutes") == 0) {
                                            time = " (" + JO_color_time.getInt("seconds") + " сек.)";
                                        } else {
                                            time = " (" + JO_color_time.getInt("minutes") + " мин.)";
                                        }
                                    } else {
                                        time = " (" + JO_color_time.getInt("hours") + " ч.)";
                                    }
                                } else {
                                    time = " (" + JO_color_time.getInt("days") + " д.)";
                                }
                            } else {
                                time = " (" + JO_color_time.getInt("months") + " мес.)";
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    switch (color)
                    {
                        case "#8DD3B6":
                            list_colors_words[j] = "мятный цвет" + time;
                            break;
                        case "#AFCAFF":
                            list_colors_words[j] = "светло-синий цвет" + time;
                            break;
                        case "#CBFFA1":
                            list_colors_words[j] = "салатовый цвет" + time;
                            break;
                        case "#FFE5A1":
                            list_colors_words[j] = "золотой цвет" + time;
                            break;
                        case "#AFFFFF":
                            list_colors_words[j] = "голубой цвет" + time;
                            break;
                        case "#FFAFCC":
                            list_colors_words[j] = "розовый цвет" + time;
                            break;
                        default:
                            list_colors_words[j] = color + time;
                            break;
                    }
                }

                if (list_colors.length != 0) {
                    final boolean[] need_to_send = {false};
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, list_colors_words);
                    // Вызываем адаптер
                    spinner2.setAdapter(spinnerArrayAdapter);

                    for (int k = 1; k < list_colors.length; k++) {
                        if (list_colors[k].equals(main_personal_color)) {
                            spinner2.setSelection(k);
                            break;
                        }
                    }

                    TV_nick.setText(MainActivity.NickName);

                    spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (need_to_send[0]) {
                                final JSONObject json2 = new JSONObject();
                                try {
                                    json2.put("nick", MainActivity.NickName);
                                    json2.put("session_id", MainActivity.Session_id);
                                    if (position == 0) {
                                        json2.put("main_personal_color", "");
                                    }
                                    else
                                    {
                                        json2.put("main_personal_color", list_colors[position]);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Log.d("kkk", "Socket_отправка - edit_profile - " + json2.toString());
                                socket.emit("edit_profile", json2);

                            }
                            else
                            {
                                need_to_send[0] = true;
                            }
                            if (!list_colors[position].equals("") && !list_colors[position].equals("нет"))
                            {
                                TV_nick.setTextColor(Color.parseColor(list_colors[position]));
                            }
                            else
                            {
                                TV_nick.setTextColor(Color.parseColor("#FFFFFF"));
                            }

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }
                else
                {
                    spinner2.setVisibility(View.GONE);
                }

            }
        });
    };

    private final Emitter.Listener onEditProfile = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(() -> {
            JSONObject data = (JSONObject) args[0];
            String status = "";
            Log.d("kkk", "принял - edit_profile - " + data);
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
                            .setMessage("Перезайдите в приложение для отображения изменений")
                            .setIcon(R.drawable.ic_ok)
                            .setCancelable(false)
                            .setNegativeButton("Ок",
                                    (dialog, id) -> dialog.cancel());
                    alert = builder.create();
                    break;
                case "incorrect_password":
                    builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Старый пароль введён неверно!")
                            .setMessage("")
                            .setIcon(R.drawable.ic_error)
                            .setCancelable(false)
                            .setNegativeButton("Ок",
                                    (dialog, id) -> dialog.cancel());
                    alert = builder.create();
                    break;
                case "new_nick_is_the_same_with_old_nick":
                    builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Новый ник такой же, как и старый!")
                            .setMessage("")
                            .setIcon(R.drawable.ic_error)
                            .setCancelable(false)
                            .setNegativeButton("Ок",
                                    (dialog, id) -> dialog.cancel());
                    alert = builder.create();
                    break;
                case "incorrect_new_nick ":
                    builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Такой ник уже занят!")
                            .setMessage("")
                            .setIcon(R.drawable.ic_error)
                            .setCancelable(false)
                            .setNegativeButton("Ок",
                                    (dialog, id) -> dialog.cancel());
                    alert = builder.create();
                    break;
                case "last_nick_update_was_less_than_a_month_ago":
                    builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Вы уже меняли ник в этом месяце!")
                            .setMessage("")
                            .setIcon(R.drawable.ic_error)
                            .setCancelable(false)
                            .setNegativeButton("Ок",
                                    (dialog, id) -> dialog.cancel());
                    alert = builder.create();
                    break;
                case "mat_in_new_nick":
                    builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Ваш ник не проходит цензуру!")
                            .setMessage("")
                            .setIcon(R.drawable.ic_error)
                            .setCancelable(false)
                            .setNegativeButton("Ок",
                                    (dialog, id) -> dialog.cancel());
                    alert = builder.create();
                    break;
                case "cant_change_nick_because_you_are_playing_in_room":
                    builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Нельзя менять ник, если вы играете в комнате!")
                            .setMessage("")
                            .setIcon(R.drawable.ic_error)
                            .setCancelable(false)
                            .setNegativeButton("Ок",
                                    (dialog, id) -> dialog.cancel());
                    alert = builder.create();
                    break;
                case "cant_change_nick_because_you_are_observer":
                    builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Нельзя менять ник, если вы наблюдаете за другой игрой!")
                            .setMessage("")
                            .setIcon(R.drawable.ic_error)
                            .setCancelable(false)
                            .setNegativeButton("Ок",
                                    (dialog, id) -> dialog.cancel());
                    alert = builder.create();
                    break;
                case "invalid_personal_color":
                    builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Время действия этого цвета истекло!")
                            .setMessage("")
                            .setIcon(R.drawable.ic_error)
                            .setCancelable(false)
                            .setNegativeButton("Ок",
                                    (dialog, id) -> dialog.cancel());
                    alert = builder.create();
                    break;
                case "invalid_status":
                    builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Время действия этого статуса истекло!")
                            .setMessage("")
                            .setIcon(R.drawable.ic_error)
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