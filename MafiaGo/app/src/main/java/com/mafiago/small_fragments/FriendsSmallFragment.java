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

public class FriendsSmallFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";

    private int mPage;

    static final int GALLERY_REQUEST = 1;

    public String base64_screenshot = "";

    View view_reportError;
    ImageView IV_screen;
    Button btn_addScreen;
    EditText ET_message;
    Button btm_sendError;

    ////////////////


    public static final String APP_PREFERENCES = "user";
    public static final String APP_PREFERENCES_EMAIL = "email";
    public static final String APP_PREFERENCES_PASSWORD = "password";
    public static final String APP_PREFERENCES_LAST_ROLE = "role";
    public static final String APP_PREFERENCES_SHOW_ROLE= "show_role";

    private SharedPreferences mSettings;

    public static FriendsSmallFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        FriendsSmallFragment fragment = new FriendsSmallFragment();
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
        View view = null;

        mSettings = getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        switch (mPage)
        {
            case 1:
                view = inflater.inflate(R.layout.fragment_settings_profile, container, false);
                break;
            case 2:
                view = inflater.inflate(R.layout.fragment_settings_profile, container, false);
                break;
            case 3:
                view = inflater.inflate(R.layout.fragment_settings_profile, container, false);
                break;
        }
        return view;
    }

    public Bitmap fromBase64(String image) {
        // Декодируем строку Base64 в массив байтов
        byte[] decodedString = Base64.decode(image, Base64.DEFAULT);

        // Декодируем массив байтов в изображение
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        // Помещаем изображение в ImageView
        return decodedByte;
    }

    /*
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
     */
}