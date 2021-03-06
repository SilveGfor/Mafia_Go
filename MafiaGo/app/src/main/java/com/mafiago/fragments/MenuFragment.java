package com.mafiago.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.mafiago.R;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mafiago.MainActivity;
import com.mafiago.adapters.BustersAdapter;
import com.mafiago.classes.OnBackPressedListener;
import com.mafiago.models.BusterModel;
import com.mafiago.models.ShopModel;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import de.hdodenhof.circleimageview.CircleImageView;
import io.socket.emitter.Emitter;

import static android.app.Activity.RESULT_OK;
import static com.mafiago.MainActivity.socket;
import static com.mafiago.MainActivity.url;

public class MenuFragment extends Fragment implements OnBackPressedListener {
    private static final String ARG_STUDY = "study";

    Button btnRating;
    Button btnGames;
    Button btnTools;
    Button btnDailyTasks;
    RelativeLayout btn_back;
    RelativeLayout RL_boosters;

    TextView TV_money;
    TextView TV_exp;
    TextView TV_rang;
    TextView TV_gold;
    TextView TV_nick;
    TextView TV_status;

    CardView CV_info;

    static final int GALLERY_REQUEST = 1;

    CircleImageView IV_avatar;

    ImageView Chats;
    ImageView Friends;
    ImageView Shop;
    ImageView Competitions;
    ImageView VK;
    ImageView Telegram;
    ImageView Menu;
    ImageView IV_mafiaGo;

    ProgressBar PB_loading;

    Boolean was_study;
    String study_type = "";

    String base64_screenshot = "", report_nick = "", report_id = "";

    ArrayList<BusterModel> list_busters = new ArrayList<>();

    // ?????????????????????????? ??????????????????????
    private static final int NOTIFY_ID = 101;

    // ?????????????????????????? ????????????
    private static String CHANNEL_ID = "Notifications channel";

    public static final String APP_PREFERENCES = "user";
    public static final String APP_PREFERENCES_WAS_STUDY = "study";
    public static final String APP_PREFERENCES_EMAIL = "email";
    public static final String APP_PREFERENCES_PASSWORD = "password";
    public static final String APP_PREFERENCES_LAST_ROLE = "role";
    public static final String APP_PREFERENCES_FULLSCREEN = "fullscreen";

    private SharedPreferences mSettings;

    public static RewardedAd mRewardedAd;

    public static MenuFragment newInstance(String study_type) {
        MenuFragment fragment = new MenuFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STUDY, study_type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            study_type = getArguments().getString(ARG_STUDY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view;
        view = inflater.inflate(R.layout.fragment_menu, container, false);

        btnRating = view.findViewById(R.id.fragmentMenu_btn_rating);
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
        TV_status = view.findViewById(R.id.fragmentMenu_TV_status);
        PB_loading = view.findViewById(R.id.fragmentMenu_PB);
        btnDailyTasks = view.findViewById(R.id.fragmentMenu_btn_dailyTasks);
        Menu = view.findViewById(R.id.fragmentMenu_IV_menu);
        btn_back = view.findViewById(R.id.fragmentGamesList_RL_back);
        RL_boosters = view.findViewById(R.id.fragmentMenu_RL_boosters);
        IV_mafiaGo = view.findViewById(R.id.fragmentMenu_IV_mafiaGo);

        IV_avatar = view.findViewById(R.id.fragmentSettingsProfile_IV_avatar);

        mSettings = getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);


        //TODO: ?????????????? ??????-???? ?????? ?????????????????? ????????
        //SetBackgroundRole(mSettings.getString(APP_PREFERENCES_LAST_ROLE, "mafia"));

        socket.off("get_profile");

        socket.on("get_profile", OnGetProfile);

        CV_info = view.findViewById(R.id.fragmentMenu_CV);

        //?????????????????? ???? ??????????????
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
        BlockView();
        Log.d("kkk", "Socket_???????????????? - get_profile - "+ json.toString());


        was_study = mSettings.getBoolean(APP_PREFERENCES_WAS_STUDY, false);
        if (!was_study)
        {
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putBoolean(APP_PREFERENCES_WAS_STUDY, true);
            editor.apply();

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            View viewDang = getLayoutInflater().inflate(R.layout.dialog_answer_about_possibilities, null);
            builder.setView(viewDang);
            AlertDialog alert = builder.create();

            Button btn_new = viewDang.findViewById(R.id.dialogAnswerAboutPossibilities_btn_new);
            Button btn_old = viewDang.findViewById(R.id.dialogAnswerAboutPossibilities_btn_old);

            btn_new.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alert.cancel();
                    new TapTargetSequence(getActivity())
                            .targets(
                                    TapTarget.forView(IV_mafiaGo,"????????????, ???? ?????????? ?? Mafia Go - ???????????? ???????????? ?????????????????? ??????????. ?????? ?????????????? ???????? ?????????? ????????, ?????? ???????? ?????????? ???????? ??????????????????????, ???????????? ?? ?????????????? ???????? ?????????????????? ??????????????????","")
                                            .outerCircleColor(R.color.orange)
                                            .outerCircleAlpha(0.96f)
                                            .targetCircleColor(R.color.white)
                                            .titleTextSize(20)
                                            .titleTextColor(R.color.white)
                                            .descriptionTextSize(10)
                                            .descriptionTextColor(R.color.black)
                                            .textColor(R.color.white)
                                            .textTypeface(Typeface.SANS_SERIF)
                                            .dimColor(R.color.black)
                                            .drawShadow(true)
                                            .cancelable(false)
                                            .tintTarget(true)
                                            .transparentTarget(true)
                                            .targetRadius(120),
                                    TapTarget.forView(btnGames,"???????????? ???????????????? ?? ???????????? ???????????????? - ???? ???????????? ?????????????? ????????????","")
                                            .outerCircleColor(R.color.notActiveText)
                                            .outerCircleAlpha(0.96f)
                                            .targetCircleColor(R.color.white)
                                            .titleTextSize(20)
                                            .titleTextColor(R.color.white)
                                            .descriptionTextSize(10)
                                            .descriptionTextColor(R.color.black)
                                            .textColor(R.color.white)
                                            .textTypeface(Typeface.SANS_SERIF)
                                            .dimColor(R.color.black)
                                            .drawShadow(true)
                                            .cancelable(false)
                                            .tintTarget(true)
                                            .transparentTarget(true)
                                            .targetRadius(60)).listener(new TapTargetSequence.Listener() {
                        @Override
                        public void onSequenceFinish() {
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new StudyGamesListFragment()).commit();
                        }

                        @Override
                        public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                        }

                        @Override
                        public void onSequenceCanceled(TapTarget lastTarget) {
                        }
                    }).start();
                }
            });
            btn_old.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Boolean fullscreen = mSettings.getBoolean(APP_PREFERENCES_FULLSCREEN, false);
                    if (!fullscreen)
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        View viewQuestion = inflater.inflate(R.layout.dialog_ok_no, null);
                        builder.setView(viewQuestion);
                        AlertDialog alert = builder.create();
                        TextView TV_text = viewQuestion.findViewById(R.id.dialogOkNo_text);
                        Button btn_yes = viewQuestion.findViewById(R.id.dialogOkNo_btn_yes);
                        Button btn_no = viewQuestion.findViewById(R.id.dialogOkNo_btn_no);
                        TV_text.setText("???????????? ???????????????? ?????????????????????????? ??????????? ???? ???????????? ???????????? ???????????????? ???????? ???????????????? ?? ????????????????????");
                        btn_yes.setOnClickListener(v1 -> {
                            SharedPreferences.Editor editor = mSettings.edit();
                            editor.putBoolean(APP_PREFERENCES_FULLSCREEN, true);
                            editor.apply();
                            reset();
                            alert.cancel();
                        });
                        btn_no.setOnClickListener(v12 -> {
                            alert.cancel();
                        });
                        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        alert.show();
                    }
                    alert.cancel();
                }
            });

            alert.setCancelable(false);
            alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alert.show();
        }

        switch (study_type)
        {
            case "end":
                new TapTargetSequence(getActivity())
                        .targets(
                                TapTarget.forView(CV_info,"?? ?????????????? ???????? ???????? ?????????? ???????????????????? ???????? ??????????????","")
                                        .id(1)
                                        .outerCircleColor(R.color.orange)
                                        .outerCircleAlpha(0.96f)
                                        .targetCircleColor(R.color.white)
                                        .titleTextSize(20)
                                        .titleTextColor(R.color.white)
                                        .descriptionTextSize(10)
                                        .descriptionTextColor(R.color.black)
                                        .textColor(R.color.white)
                                        .textTypeface(Typeface.SANS_SERIF)
                                        .dimColor(R.color.black)
                                        .drawShadow(true)
                                        .cancelable(false)
                                        .tintTarget(true)
                                        .transparentTarget(true)
                                        .targetRadius(120),
                                TapTarget.forView(RL_boosters,"?????? ?????????? ???????????????????? ???????????? ???????????????? ????????????????, ?????????????? ?????????? ???????????? ?? ????????????????","")
                                        .id(2)
                                        .outerCircleColor(R.color.orange)
                                        .outerCircleAlpha(0.96f)
                                        .targetCircleColor(R.color.white)
                                        .titleTextSize(20)
                                        .titleTextColor(R.color.white)
                                        .descriptionTextSize(10)
                                        .descriptionTextColor(R.color.black)
                                        .textColor(R.color.white)
                                        .textTypeface(Typeface.SANS_SERIF)
                                        .dimColor(R.color.black)
                                        .drawShadow(true)
                                        .cancelable(false)
                                        .tintTarget(true)
                                        .transparentTarget(true)
                                        .targetRadius(60),
                                TapTarget.forView(VK,"???????? ???????????? ?? ????","")
                                        .outerCircleColor(R.color.orange)
                                        .outerCircleAlpha(0.96f)
                                        .targetCircleColor(R.color.white)
                                        .titleTextSize(20)
                                        .titleTextColor(R.color.white)
                                        .descriptionTextSize(10)
                                        .descriptionTextColor(R.color.black)
                                        .textColor(R.color.white)
                                        .textTypeface(Typeface.SANS_SERIF)
                                        .dimColor(R.color.black)
                                        .drawShadow(true)
                                        .cancelable(false)
                                        .tintTarget(true)
                                        .transparentTarget(true)
                                        .targetRadius(60),
                                TapTarget.forView(Telegram,"?????? ?????????? ?? ????????????????, ?????? ???? ?????????????????? ?????????????????? ?????????????? ?? ???????????? ???????????? ?? ???????????????????? ????????!","")
                                        .outerCircleColor(R.color.orange)
                                        .outerCircleAlpha(0.96f)
                                        .targetCircleColor(R.color.white)
                                        .titleTextSize(20)
                                        .titleTextColor(R.color.white)
                                        .descriptionTextSize(10)
                                        .descriptionTextColor(R.color.black)
                                        .textColor(R.color.white)
                                        .textTypeface(Typeface.SANS_SERIF)
                                        .dimColor(R.color.black)
                                        .drawShadow(true)
                                        .cancelable(false)
                                        .tintTarget(true)
                                        .transparentTarget(true)
                                        .targetRadius(60),
                                TapTarget.forView(Shop,"?????? ???????????????????? ??????????????, ?????? ?????????? ???????????? ????????????, ??????????????-??????????????, ?????????????? ??????, ???????????? ?? ???????????? ???????????????? ????????","")
                                        .outerCircleColor(R.color.orange)
                                        .outerCircleAlpha(0.96f)
                                        .targetCircleColor(R.color.white)
                                        .titleTextSize(20)
                                        .titleTextColor(R.color.white)
                                        .descriptionTextSize(10)
                                        .descriptionTextColor(R.color.black)
                                        .textColor(R.color.white)
                                        .textTypeface(Typeface.SANS_SERIF)
                                        .dimColor(R.color.black)
                                        .drawShadow(true)
                                        .cancelable(false)
                                        .tintTarget(true)
                                        .transparentTarget(true)
                                        .targetRadius(60),
                                TapTarget.forView(Competitions,"?????????????????????? ?????????????? ???? ?????????? ????????","")
                                        .outerCircleColor(R.color.orange)
                                        .outerCircleAlpha(0.96f)
                                        .targetCircleColor(R.color.white)
                                        .titleTextSize(20)
                                        .titleTextColor(R.color.white)
                                        .descriptionTextSize(10)
                                        .descriptionTextColor(R.color.black)
                                        .textColor(R.color.white)
                                        .textTypeface(Typeface.SANS_SERIF)
                                        .dimColor(R.color.black)
                                        .drawShadow(true)
                                        .cancelable(false)
                                        .tintTarget(true)
                                        .transparentTarget(true)
                                        .targetRadius(60),
                                TapTarget.forView(Chats,"?????? ???????????? ?????????????????? ?????????????????? ??????. ???????????????? ???????????? ?????????????????? ?????????? ???????????? ????????????, ???? ?????????????????????? ?????????????????? ?????? ?? ????????????","")
                                        .outerCircleColor(R.color.orange)
                                        .outerCircleAlpha(0.96f)
                                        .targetCircleColor(R.color.white)
                                        .titleTextSize(20)
                                        .titleTextColor(R.color.white)
                                        .descriptionTextSize(10)
                                        .descriptionTextColor(R.color.black)
                                        .textColor(R.color.white)
                                        .textTypeface(Typeface.SANS_SERIF)
                                        .dimColor(R.color.black)
                                        .drawShadow(true)
                                        .cancelable(false)
                                        .tintTarget(true)
                                        .transparentTarget(true)
                                        .targetRadius(60),
                                TapTarget.forView(Friends,"???????????? ????????????, ?????? ?????????? ??????????????, ?? ?????????? ???????????????? ???????????? ???????? ???????????? ?? ???????? ???????????????????????????? ?? ??????","")
                                        .outerCircleColor(R.color.orange)
                                        .outerCircleAlpha(0.96f)
                                        .targetCircleColor(R.color.white)
                                        .titleTextSize(20)
                                        .titleTextColor(R.color.white)
                                        .descriptionTextSize(10)
                                        .descriptionTextColor(R.color.black)
                                        .textColor(R.color.white)
                                        .textTypeface(Typeface.SANS_SERIF)
                                        .dimColor(R.color.black)
                                        .drawShadow(true)
                                        .cancelable(false)
                                        .tintTarget(true)
                                        .transparentTarget(true)
                                        .targetRadius(60)).listener(new TapTargetSequence.Listener() {
                    @Override
                    public void onSequenceFinish() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        View viewQuestion = inflater.inflate(R.layout.dialog_ok_no, null);
                        builder.setView(viewQuestion);
                        AlertDialog alert = builder.create();
                        TextView TV_text = viewQuestion.findViewById(R.id.dialogOkNo_text);
                        Button btn_yes = viewQuestion.findViewById(R.id.dialogOkNo_btn_yes);
                        Button btn_no = viewQuestion.findViewById(R.id.dialogOkNo_btn_no);
                        TV_text.setText("???? ???????? ???????? ???????????????? ?????????????? ?? ??????????!\n???????????? ???????????????? ?????????????????????????? ??????????? ???? ???????????? ???????????? ???????????????? ???????? ???????????????? ?? ????????????????????");
                        btn_yes.setOnClickListener(v1 -> {
                            SharedPreferences.Editor editor = mSettings.edit();
                            editor.putBoolean(APP_PREFERENCES_FULLSCREEN, true);
                            editor.apply();
                            reset();
                            alert.cancel();
                        });
                        btn_no.setOnClickListener(v12 -> {
                            alert.cancel();
                        });
                        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        alert.show();
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                        if (lastTarget.id() == 1)
                        {
                            CV_info.callOnClick();
                        }
                        else if (lastTarget.id() == 2)
                        {
                            RL_boosters.callOnClick();
                        }
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                    }
                }).start();
                break;
        }


        //final Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.bounce_center);

        // amplitude 0.2 and frequency 20
        //BounceInterpolator interpolator = new BounceInterpolator();
        //animation.setInterpolator(interpolator);
        //CV_info.startAnimation(animation);

        Menu.setOnClickListener(v -> {
            PopupMenu popup_menu = new PopupMenu(getActivity(), Menu);
            popup_menu.inflate(R.menu.main_menu);
            popup_menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.mainMenu_play:
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
                            return true;
                        case R.id.mainMenu_shop:
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new ShopFragment()).commit();
                            return true;
                        case R.id.mainMenu_friends:
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new FriendsFragment()).commit();
                            return true;
                        case R.id.mainMenu_chats:
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new PrivateChatsFragment()).commit();
                            return true;
                        case R.id.mainMenu_settings:
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new SettingsFragment()).commit();
                            return true;
                    }
                    return true;
                }
            });
            popup_menu.show();
        });

        Telegram.setOnClickListener(v -> {
            Intent mIntent = new Intent();
            mIntent.setAction(Intent.ACTION_VIEW);
            mIntent.setData(Uri.parse("https://t.me/mafia_go_game"));
            startActivity(Intent.createChooser( mIntent, "???????????????? ??????????????"));
        });

        btnDailyTasks.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new DailyTasksFragment()).commit();
        });

        VK.setOnClickListener(v -> {
            Intent mIntent = new Intent();
            mIntent.setAction(Intent.ACTION_VIEW);
            mIntent.setData(Uri.parse("https://vk.com/mafia_go_game"));
            startActivity(Intent.createChooser( mIntent, "???????????????? ??????????????"));
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
                /*
                AlertDialog.Builder builder2 = new AlertDialog.Builder(getContext());
                builder2.setTitle("?? ????????????????????...")
                        .setMessage("")
                        .setIcon(R.drawable.ic_razrabotka)
                        .setCancelable(false)
                        .setNegativeButton("????",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert2 = builder2.create();
                alert2.show();
                 */
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new FriendsFragment()).commit();
            }
        });

        btnRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new RatingsFragment()).commit();
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
                    builder.setTitle("?? ?????? ?????? ?????????????????????? ?? ??????????????????!")
                            .setMessage("")
                            .setIcon(R.drawable.ic_ban)
                            .setCancelable(false)
                            .setNegativeButton("????",
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
                    builder.setTitle("?? ?????? ?????? ?????????????????????? ?? ??????????????????!")
                            .setMessage("")
                            .setIcon(R.drawable.ic_ban)
                            .setCancelable(false)
                            .setNegativeButton("????",
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

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finishAffinity();
            }
        });

        return view;
    }

    private void reset() {
        Intent intent = new Intent(getContext(), MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        getActivity().finishAffinity();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if (imageReturnedIntent == null
                || imageReturnedIntent.getData() == null) {
            return;
        }

        // ?????????????? ???? ~500???? ????????????????. ?????????? ?????? ?????????????? ???????????????? ?????????? ~2????


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
                                    reason[0] = "???????? ?????? ????????";
                                    break;
                                case R.id.dialogReport_RB_2:
                                    reason[0] = "???????????????????? ???????????????????? ????????????????????, ????????????????????????, ?????????????????????????????????? ?????? ?????????????????????????????? ?????????????????? ?????? ???????? ????????????????????, ???????????????????? ?????????????? ?????????????????? ?????? ???????????????????????? ???????????? ???????????????????????? ???????????????????????????????? ????";
                                    break;
                                case R.id.dialogReport_RB_3:
                                    reason[0] = "?????????????????????????????? ????????????????????, ?????????????? ???????????????????? ???? ???????????????????? ??????????, ???????????????????? ????????????????????????, ?????????????? ?????? ?????????????????????? ?????????????????? ?? ???????????? ?????? ???????? ????????????????????, ???? ?????????????????????????????? ?????????????? ?????????????????????????? ?????????????????? ?????? ???????????????????????????????? ??????????????????????????????";
                                    break;
                                case R.id.dialogReport_RB_4:
                                    reason[0] = "???????? ????????????/???? ?? ?????????????????? ?????????? ??????????????";
                                    break;
                                case R.id.dialogReport_RB_5:
                                    reason[0] = "???????? (??.??. ?????????????? ???????? ???????????????????????????? ?????????????? ??????, ???????? ?????????????? ???????????????????? ???? ?????????????? ???????????????????? ?????????????? ?????? ?????????????????????? ???? ????????, ?????? ???? ???????????????????? ???????????? ??????????????)";
                                    break;
                                case R.id.dialogReport_RB_6:
                                    reason[0] = "???????????????? ???????????????????? ?????????????? ?????????????? ?? ????????????????????, ???????????????????? ?????????????????????????? ???????????? ?? ???????? ???? ????????";
                                    break;
                                case R.id.dialogReport_RB_7:
                                    reason[0] = "???????????????????? ????????????????, ???????????????????????? ???? ???????????????? ???????????? ?????????????????????????? ?? ?????????????????????? (???? ???????????????? ???????????????? ????????????????)";
                                    break;
                                case R.id.dialogReport_RB_8:
                                    reason[0] = "??????????????????/?????????????????????????? ???????????????????????????? ???????????? ???????????????????????? ?????? ????????????????????";
                                    break;
                                case R.id.dialogReport_RB_9:
                                    reason[0] = "????????????";
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
                        Log.d("kkk", "Socket_???????????????? - send_complaint" + json2);
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
        // ???????????????????? ???????????? Base64 ?? ???????????? ????????????
        byte[] decodedString = Base64.decode(image, Base64.DEFAULT);

        // ???????????????????? ???????????? ???????????? ?? ??????????????????????
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        // ???????????????? ?????????????????????? ?? ImageView
        return decodedByte;
    }

    private final Emitter.Listener OnGetProfile = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject data = (JSONObject) args[0];
                UnblockView();
                String nick = "", avatar = "";
                boolean online = false;
                int money = 0, exp = 0, gold = 0, rang = 0;
                JSONObject statistic = new JSONObject();
                JSONObject JO_statuses = new JSONObject();
                JSONObject JO_colors = new JSONObject();
                JSONObject JO_chance_of_role = new JSONObject();
                int game_counter = 0, max_money_score = 0, max_exp_score = 0;
                String general_pers_of_wins = "", mafia_pers_of_wins = "", peaceful_pers_of_wins = "", user_id_2 = "", main_status = "", main_personal_color = "", premium_time = "";
                int was_citizen = 0, was_sheriff = 0, was_doctor = 0, was_lover = 0, was_journalist = 0, was_bodyguard = 0, was_doctor_of_easy_virtue = 0, was_maniac = 0, was_mafia = 0, was_mafia_don = 0, was_terrorist = 0, was_poisoner = 0;
                //data.remove("avatar");
                //data.remove("statistics");
                //data.remove("chance_of_role");
                Log.d("kkk", "???????????? - get_profile - " + data);
                try {
                    statistic = data.getJSONObject("statistics");
                    JO_statuses = data.getJSONObject("statuses");
                    JO_colors = data.getJSONObject("personal_colors");
                    JO_chance_of_role = data.getJSONObject("chance_of_role");
                    game_counter = statistic.getInt("game_counter");
                    max_money_score = statistic.getInt("max_money_score");
                    max_exp_score = statistic.getInt("max_exp_score");
                    general_pers_of_wins = statistic.getString("general_wins");
                    mafia_pers_of_wins = statistic.getString("mafia_wins");
                    peaceful_pers_of_wins = statistic.getString("peaceful_wins");
                    avatar = data.getString("avatar");
                    //"main_status":"??????????-????????????","main_personal_color" personal_colors premium_time
                    main_status = data.getString("main_status");
                    main_personal_color = data.getString("main_personal_color");

                    was_citizen = statistic.getInt("was_citizen");
                    was_sheriff = statistic.getInt("was_sheriff");
                    was_doctor = statistic.getInt("was_doctor");
                    was_lover = statistic.getInt("was_lover");
                    was_journalist = statistic.getInt("was_journalist");
                    was_bodyguard = statistic.getInt("was_bodyguard");
                    was_doctor_of_easy_virtue = statistic.getInt("was_doctor_of_easy_virtue");
                    was_maniac = statistic.getInt("was_maniac");
                    was_mafia = statistic.getInt("was_mafia");
                    was_mafia_don = statistic.getInt("was_mafia_don");
                    was_terrorist = statistic.getInt("was_terrorist");
                    was_poisoner = statistic.getInt("was_poisoner");

                    user_id_2 = data.getString("user_id");
                    online = data.getBoolean("is_online");
                    gold = data.getInt("gold");
                    nick = data.getString("nick");
                    money = data.getInt("money");
                    exp = data.getInt("exp");
                    rang = data.getInt("rang");

                    if (!data.isNull("premium_time")) {
                        IV_avatar.setBorderWidth(4);
                        JSONObject JO_premium_time = data.getJSONObject("premium_time");
                        if (JO_premium_time.getInt("months") == 0) {
                            if (JO_premium_time.getInt("days") == 0) {
                                if (JO_premium_time.getInt("hours") == 0) {
                                    if (JO_premium_time.getInt("minutes") == 0) {
                                        premium_time = JO_premium_time.getInt("seconds") + " ??????.";
                                    } else {
                                        premium_time = JO_premium_time.getInt("minutes") + " ??????. " + JO_premium_time.getInt("seconds") + " ??????.";
                                    }
                                } else {
                                    premium_time = JO_premium_time.getInt("hours") + " ??. " + JO_premium_time.getInt("minutes") + " ??????.";
                                }
                            } else {
                                premium_time = JO_premium_time.getInt("days") + " ??. " + JO_premium_time.getInt("hours") + " ??.";
                            }
                        } else {
                            premium_time = JO_premium_time.getInt("months") + " ??????. " + JO_premium_time.getInt("days") + " ??.";
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Iterator iterator;
                String status;

                for (iterator = JO_statuses.keys(); iterator.hasNext();)
                {
                    status = (String) iterator.next();
                    try {
                        if (!JO_statuses.getString(status).equals("forever"))
                        {
                            JSONObject JO_status = JO_statuses.getJSONObject(status);
                            String time = "";
                            if (JO_status.getInt("months") == 0) {
                                if (JO_status.getInt("days") == 0) {
                                    if (JO_status.getInt("hours") == 0) {
                                        if (JO_status.getInt("minutes") == 0) {
                                            time = JO_status.getInt("seconds") + " ??????.";
                                        } else {
                                            time = JO_status.getInt("minutes") + " ??????.";
                                        }
                                    } else {
                                        time = JO_status.getInt("hours") + " ??.";
                                    }
                                } else {
                                    time = JO_status.getInt("days") + " ??.";
                                }
                            } else {
                                time = JO_status.getInt("months") + " ??????.";
                            }
                            list_busters.add(new BusterModel("status", time, status));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                String color = "";

                for (iterator = JO_colors.keys(); iterator.hasNext();)
                {
                    color = (String) iterator.next();
                    try {
                        if (!JO_colors.getString(color).equals("forever"))
                        {
                            JSONObject JO_color = JO_colors.getJSONObject(color);
                            String time = "";
                            if (JO_color.getInt("months") == 0) {
                                if (JO_color.getInt("days") == 0) {
                                    if (JO_color.getInt("hours") == 0) {
                                        if (JO_color.getInt("minutes") == 0) {
                                            time = JO_color.getInt("seconds") + " ??????.";
                                        } else {
                                            time = JO_color.getInt("minutes") + " ??????.";
                                        }
                                    } else {
                                        time = JO_color.getInt("hours") + " ??.";
                                    }
                                } else {
                                    time = JO_color.getInt("days") + " ??.";
                                }
                            } else {
                                time = JO_color.getInt("months") + " ??????.";
                            }
                            list_busters.add(new BusterModel("color", time, color));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                String role_change = "";
                boolean is_premium;

                for (iterator = JO_chance_of_role.keys(); iterator.hasNext();)
                {
                    role_change = (String) iterator.next();
                    try {
                        JSONObject JO_role = JO_chance_of_role.getJSONObject(role_change);
                        if (!JO_role.isNull("hours")) {
                            JSONObject JO_role_time = JO_role.getJSONObject("hours");
                            is_premium = JO_role.getString("type").equals("premium");
                            String time = "";
                            if (JO_role_time.getInt("months") == 0) {
                                if (JO_role_time.getInt("days") == 0) {
                                    if (JO_role_time.getInt("hours") == 0) {
                                        if (JO_role_time.getInt("minutes") == 0) {
                                            time = JO_role_time.getInt("seconds") + " ??????.";
                                        } else {
                                            time = JO_role_time.getInt("minutes") + " ??????.";
                                        }
                                    } else {
                                        time = JO_role_time.getInt("hours") + " ??.";
                                    }
                                } else {
                                    time = JO_role_time.getInt("days") + " ??.";
                                }
                            } else {
                                time = JO_role_time.getInt("months") + " ??????.";
                            }
                            list_busters.add(new BusterModel("chance_of_role", time, role_change, is_premium));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                if (avatar != null && !avatar.equals("") && !avatar.equals("null")) {
                    IV_avatar.setImageBitmap(fromBase64(avatar));
                }

                TV_money.setText(String.valueOf(money));
                TV_exp.setText(String.valueOf(exp));
                TV_gold.setText(String.valueOf(gold));
                TV_rang.setText(String.valueOf(rang));

                TV_nick.setText(MainActivity.NickName);
                if (!main_status.equals("")) {
                    TV_status.setText("{" + main_status + "}");
                }
                if (!main_personal_color.equals("")) {
                    TV_nick.setTextColor(Color.parseColor(main_personal_color));
                    TV_status.setTextColor(Color.parseColor(main_personal_color));
                }

                PB_loading.setVisibility(View.GONE);

                TV_money.setVisibility(View.VISIBLE);
                TV_exp.setVisibility(View.VISIBLE);
                TV_gold.setVisibility(View.VISIBLE);
                TV_rang.setVisibility(View.VISIBLE);
                TV_nick.setVisibility(View.VISIBLE);
                TV_status.setVisibility(View.VISIBLE);

                report_nick = nick;
                report_id = user_id_2;
                String finalNick = nick;
                String finalMain_status = main_status;
                String finalMain_personal_color = main_personal_color;
                boolean finalOnline = online;
                String finalAvatar = avatar;
                int finalGame_counter = game_counter;
                int finalMax_money_score = max_money_score;
                int finalMax_exp_score = max_exp_score;
                String finalMafia_pers_of_wins = mafia_pers_of_wins;
                String finalPeaceful_pers_of_wins = peaceful_pers_of_wins;
                String finalGeneral_pers_of_wins = general_pers_of_wins;
                int finalGold = gold;
                int finalMoney = money;
                int finalExp = exp;
                int finalRang = rang;
                int finalWas_citizen = was_citizen;
                int finalWas_sheriff = was_sheriff;
                int finalWas_doctor = was_doctor;
                int finalWas_lover = was_lover;
                int finalWas_journalist = was_journalist;
                int finalWas_bodyguard = was_bodyguard;
                int finalWas_maniac = was_maniac;
                int finalWas_doctor_of_easy_virtue = was_doctor_of_easy_virtue;
                int finalWas_mafia = was_mafia;
                int finalWas_mafia_don = was_mafia_don;
                int finalWas_poisoner = was_poisoner;
                int finalWas_terrorist = was_terrorist;
                CV_info.setOnClickListener(v -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    View view_profile = getLayoutInflater().inflate(R.layout.dialog_my_profile, null);
                    builder.setView(view_profile);

                    TextView TV_money = view_profile.findViewById(R.id.dialogMyProfile_TV_money);
                    TextView TV_exp = view_profile.findViewById(R.id.dialogMyProfile_TV_exp);
                    TextView TV_gold = view_profile.findViewById(R.id.dialogMyProfile_TV_gold);
                    TextView TV_rang = view_profile.findViewById(R.id.dialogMyProfile_TV_rang);
                    ImageView IV_avatar = view_profile.findViewById(R.id.dialogMyProfile_IV_avatar);
                    ImageView IV_online = view_profile.findViewById(R.id.dialogMyProfile_IV_online);
                    TextView TV_nick = view_profile.findViewById(R.id.dialogMyProfile_TV_nick);
                    TextView TV_status = view_profile.findViewById(R.id.dialogMyProfile_TV_status);

                    TextView TV_game_counter = view_profile.findViewById(R.id.dialogMyProfile_TV_gamesCouner);
                    TextView TV_max_money_score = view_profile.findViewById(R.id.dialogMyProfile_TV_maxMoney);
                    TextView TV_max_exp_score = view_profile.findViewById(R.id.dialogMyProfile_TV_maxExp);
                    TextView TV_general_pers_of_wins = view_profile.findViewById(R.id.dialogMyProfile_TV_percentWins);
                    TextView TV_mafia_pers_of_wins = view_profile.findViewById(R.id.dialogMyProfile_TV_percentMafiaWins);
                    TextView TV_peaceful_pers_of_wins = view_profile.findViewById(R.id.dialogMyProfile_TV_percentPeacefulWins);

                    TextView TV_gamesCitizen = view_profile.findViewById(R.id.dialogMyProfile_TV_gamesCitizen);
                    TextView TV_gamesSheriff = view_profile.findViewById(R.id.dialogMyProfile_TV_gamesSheriff);
                    TextView TV_gamesDoctor = view_profile.findViewById(R.id.dialogMyProfile_TV_gamesDoctor);
                    TextView TV_gamesLover = view_profile.findViewById(R.id.dialogMyProfile_TV_gamesLover);
                    TextView TV_gamesJournalist = view_profile.findViewById(R.id.dialogMyProfile_TV_gamesJournalist);
                    TextView TV_gamesBodyguard = view_profile.findViewById(R.id.dialogMyProfile_TV_gamesBodyguard);
                    TextView TV_gamesManiac = view_profile.findViewById(R.id.dialogMyProfile_TV_gamesManiac);
                    TextView TV_gamesDoctorOfEasyVirtue = view_profile.findViewById(R.id.dialogMyProfile_TV_gamesDoctorOfEasyVirtue);
                    TextView TV_gamesMafia = view_profile.findViewById(R.id.dialogMyProfile_TV_gamesMafia);
                    TextView TV_gamesMafiaDon = view_profile.findViewById(R.id.dialogMyProfile_TV_gamesMafiaDon);
                    TextView TV_gamesTerrorist = view_profile.findViewById(R.id.dialogMyProfile_TV_gamesTerrorist);
                    TextView TV_gamesPoisoner = view_profile.findViewById(R.id.dialogMyProfile_TV_gamesPoisoner);

                    TV_gamesCitizen.setText("???????????? ????????????: " + finalWas_citizen);
                    TV_gamesSheriff.setText("??????????: " + finalWas_sheriff);
                    TV_gamesDoctor.setText("????????????: " + finalWas_doctor);
                    TV_gamesLover.setText("??????????????????: " + finalWas_lover);
                    TV_gamesJournalist.setText("?????????? ??????: " + finalWas_journalist);
                    TV_gamesBodyguard.setText("??????????????????????????: " + finalWas_bodyguard);
                    TV_gamesManiac.setText("????????????: " + finalWas_maniac);
                    TV_gamesDoctorOfEasyVirtue.setText("???????????? ?????????????? ??????????????????: " + finalWas_doctor_of_easy_virtue);
                    TV_gamesMafia.setText("??????????: " + finalWas_mafia);
                    TV_gamesMafiaDon.setText("?????? ??????????: " + finalWas_mafia_don);
                    TV_gamesTerrorist.setText("??????????????????: " + finalWas_terrorist);
                    TV_gamesPoisoner.setText("????????????????????: " + finalWas_poisoner);

                    TV_nick.setText(finalNick);
                    if (!finalMain_status.equals("")) {
                        TV_status.setText("{" + finalMain_status + "}");
                    }
                    if (!finalMain_personal_color.equals("")) {
                        TV_nick.setTextColor(Color.parseColor(finalMain_personal_color));
                        TV_status.setTextColor(Color.parseColor(finalMain_personal_color));
                    }
                    if (finalOnline)
                    {
                        IV_online.setVisibility(View.VISIBLE);
                    }

                    if (finalAvatar != null && !finalAvatar.equals("") && !finalAvatar.equals("null")) {
                        IV_avatar.setImageBitmap(fromBase64(finalAvatar));
                    }

                    String finalAvatar1 = finalAvatar;
                    IV_avatar.setOnClickListener(v12 -> {
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
                        View view_avatar = getLayoutInflater().inflate(R.layout.dialog_avatar, null);
                        builder2.setView(view_avatar);

                        ImageView IV_dialog_avatar = view_avatar.findViewById(R.id.dialogAvatar_avatar);
                        Button btn_exit_avatar = view_avatar.findViewById(R.id.dialogAvatar_btn_exit);

                        if (finalAvatar1 != null && !finalAvatar1.equals("") && !finalAvatar1.equals("null")) {
                            IV_dialog_avatar.setImageBitmap(fromBase64(finalAvatar1));
                        }


                        AlertDialog alert2 = builder2.create();

                        btn_exit_avatar.setOnClickListener(v13 -> {
                            alert2.cancel();
                        });

                        alert2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        alert2.show();
                    });

                    TV_game_counter.setText("?????????????? ??????: " + finalGame_counter);
                    TV_max_money_score.setText("????????. ?????????? ???? ????????: " + finalMax_money_score);
                    TV_max_exp_score.setText("????????. ?????????? ???? ????????: " + finalMax_exp_score);
                    TV_general_pers_of_wins.setText("?????????????? ??????????: " + finalGeneral_pers_of_wins);
                    TV_mafia_pers_of_wins.setText("?????????? ???? ??????????: " + finalMafia_pers_of_wins);
                    TV_peaceful_pers_of_wins.setText("?????????? ???? ????????????: " + finalPeaceful_pers_of_wins);

                    TV_gold.setText(finalGold + " ????????????");
                    TV_money.setText(finalMoney + " $");
                    TV_exp.setText(finalExp + " XP");
                    TV_rang.setText(finalRang + " ????????");

                    AlertDialog alert = builder.create();
                    alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alert.show();
                });

                String finalPremium_time = premium_time;

                RL_boosters.setOnClickListener(v -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    View viewBusters = getLayoutInflater().inflate(R.layout.dialog_active_busters, null);
                    builder.setView(viewBusters);
                    AlertDialog alert = builder.create();

                    CircleImageView CIV_avatar = viewBusters.findViewById(R.id.dialogActiveBusters_IV_avatar);
                    TextView TV_premiumAccount = viewBusters.findViewById(R.id.dialogActiveBusters_TV_premiumAccount);
                    TextView TV_noBusters = viewBusters.findViewById(R.id.dialogActiveBusters_TV_noBusters);
                    TextView TV_premiumTime = viewBusters.findViewById(R.id.dialogActiveBusters_TV_premiumTime);
                    ShimmerTextView STV_premiumMoney = viewBusters.findViewById(R.id.dialogActiveBusters_STV_premiumMoney);
                    ShimmerTextView STV_premiumExp = viewBusters.findViewById(R.id.dialogActiveBusters_STV_premiumExp);
                    ImageView IV_premiumMoney = viewBusters.findViewById(R.id.dialogActiveBusters_IV_money);
                    ImageView IV_premiumExp = viewBusters.findViewById(R.id.dialogActiveBusters_IV_exp);
                    ListView LV_activeBusters = viewBusters.findViewById(R.id.dialogActiveBusters_LV_activeBusters);

                    if (list_busters.size() != 0)
                    {
                        TV_noBusters.setVisibility(View.INVISIBLE);
                    }
                    BustersAdapter bustersAdapter = new BustersAdapter(list_busters, getContext());
                    LV_activeBusters.setAdapter(bustersAdapter);

                    if (!finalPremium_time.equals(""))
                    {
                        if (finalAvatar != null && !finalAvatar.equals("") && !finalAvatar.equals("null")) {
                            CIV_avatar.setImageBitmap(fromBase64(finalAvatar));
                            CIV_avatar.setBorderWidth(4);
                        }
                        TV_premiumAccount.setText("??????????????-??????????????");
                        TV_premiumTime.setText("????????????????: " + finalPremium_time);

                        STV_premiumMoney.setVisibility(View.VISIBLE);
                        STV_premiumExp.setVisibility(View.VISIBLE);
                        IV_premiumMoney.setVisibility(View.VISIBLE);
                        IV_premiumExp.setVisibility(View.VISIBLE);
                        Shimmer shimmer = new Shimmer();
                        shimmer.start(STV_premiumMoney);
                        shimmer.start(STV_premiumExp);
                    }

                    alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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

    public void BlockView()
    {
        btnRating.setClickable(false);
        btnGames.setClickable(false);
        btnTools.setClickable(false);
        btnDailyTasks.setClickable(false);
        btn_back.setClickable(false);
        RL_boosters.setClickable(false);
        CV_info.setClickable(false);
        Chats.setClickable(false);
        Friends.setClickable(false);
        Shop.setClickable(false);
        Competitions.setClickable(false);
        VK.setClickable(false);
        Telegram.setClickable(false);
        Menu.setClickable(false);
    }
    public void UnblockView()
    {
        btnRating.setClickable(true);
        btnGames.setClickable(true);
        btnTools.setClickable(true);
        btnDailyTasks.setClickable(true);
        btn_back.setClickable(true);
        RL_boosters.setClickable(true);
        CV_info.setClickable(true);
        Chats.setClickable(true);
        Friends.setClickable(true);
        Shop.setClickable(true);
        Competitions.setClickable(true);
        VK.setClickable(true);
        Telegram.setClickable(true);
        Menu.setClickable(true);
    }
}
