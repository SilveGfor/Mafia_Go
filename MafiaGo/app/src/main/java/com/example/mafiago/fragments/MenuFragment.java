package com.example.mafiago.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.mafiago.MainActivity;
import com.example.mafiago.R;
import com.example.mafiago.adapters.GamesAdapter;
import com.example.mafiago.models.RoomModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.emitter.Emitter;

import static  com.example.mafiago.MainActivity.socket;


public class MenuFragment extends Fragment {
    Button btnRules;
    Button btnGames;
    Button btnFriends;
    Button btnTools;
    ShimmerTextView txtNick;

    FloatingActionButton FAB_exit_menu;

    CardView CV_info;

    ImageView IV_background;

    int pressedTimes = 0;

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
        txtNick = view.findViewById(R.id.txtNick);
        txtNick.setText(MainActivity.NickName);

        IV_background = view.findViewById(R.id.fragmentMenu_IV_background);

        MenuFragment.ProfileTask socketTask = new ProfileTask();
        socketTask.execute();

        mSettings = getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        SetBackgroundRole(mSettings.getString(APP_PREFERENCES_LAST_ROLE, "mafia"));

        FAB_exit_menu = view.findViewById(R.id.fragmentMenu_FAB_exit_menu);



        CV_info = view.findViewById(R.id.fragmentMenuMenu_CV_info);

        //настройки от Шлыкова
        //Nastroiki nastroiki = new Nastroiki();

        FAB_exit_menu.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new StartFragment()).commit();
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putString(APP_PREFERENCES_EMAIL, null);
            editor.putString(APP_PREFERENCES_PASSWORD, null);
            editor.apply();
        });

        CV_info.setOnClickListener(v -> {

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




        });

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
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
            }
        });

        btnFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new FriendsFragment()).commit();
            }
        });
        return view;
    }

    public class ProfileTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("kkk", "onPreExecute");
        }

        @Override
        protected Void doInBackground(Void... voids) {

            socket.on("get_profile", OnGetProfile);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d("kkk", "onPostExecute");
        }
    }

    private final Emitter.Listener OnGetProfile = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject data = (JSONObject) args[0];
                String nick = "";
                boolean online = false;

                try {
                    online = data.getBoolean("is_online");
                    nick = data.getString("nick");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                final Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.bounce_center);

                // amplitude 0.2 and frequency 20
                BounceInterpolator interpolator = new BounceInterpolator();
                animation.setInterpolator(interpolator);

                CV_info.startAnimation(animation);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                View view_profile = getLayoutInflater().inflate(R.layout.item_profile, null);
                builder.setView(view_profile);

                FloatingActionButton FAB_add_friend = view_profile.findViewById(R.id.Item_profile_add_friend);
                FloatingActionButton FAB_kick = view_profile.findViewById(R.id.Item_profile_kick);
                TextView TV_nick = view_profile.findViewById(R.id.Item_profile_TV_nick);
                ImageView IV_on_off = view_profile.findViewById(R.id.Item_profile_IV_on_off);

                if (online) IV_on_off.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_online));
                else IV_on_off.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_offline));

                FAB_kick.setVisibility(View.GONE);
                TV_nick.setText(nick);
                FAB_add_friend.setOnClickListener(v1 -> {
                    //TODO: добавление в друзья
                });
                AlertDialog alert = builder.create();
                alert.show();

                Shimmer shimmer = new Shimmer();
                shimmer.start(txtNick);
                Log.d("kkk", "принял - get_profile - " + data);

            }
        });
    };


    public void SetBackgroundRole(String role)
    {
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

}
