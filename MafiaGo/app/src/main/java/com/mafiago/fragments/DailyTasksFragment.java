package com.mafiago.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.mafiago.R;
import com.mafiago.MainActivity;
import com.mafiago.adapters.DailyTasksAdapter;
import com.mafiago.adapters.PrivateMessagesAdapter;
import com.mafiago.classes.OnBackPressedListener;
import com.mafiago.enums.Role;
import com.mafiago.enums.Time;
import com.mafiago.models.DailyTaskModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import io.socket.emitter.Emitter;

import static com.mafiago.MainActivity.socket;

public class DailyTasksFragment extends Fragment implements OnBackPressedListener {

    ListView LV_tasks;
    ProgressBar PB_loading;
    ImageView Menu;
    RelativeLayout btn_back;

    DailyTasksAdapter dailyTasksAdapter;
    ArrayList<DailyTaskModel> list_tasks = new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_daily_tasks, container, false);

        LV_tasks = view.findViewById(R.id.fragmentDailyTasks_LV_tasks);
        PB_loading = view.findViewById(R.id.fragmentDailyTasks_PB);
        btn_back = view.findViewById(R.id.fragmentGamesList_RL_back);

        dailyTasksAdapter = new DailyTasksAdapter(list_tasks, getContext());
        LV_tasks.setAdapter(dailyTasksAdapter);
        Menu = view.findViewById(R.id.fragmentMenu_IV_menu);

        Menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
        });

        socket.on("get_daily_tasks", onGetDailyTasks);
        socket.on("change_daily_task", onChangeDailyTask);

        final JSONObject json = new JSONObject();
        try {
            json.put("nick", MainActivity.NickName);
            json.put("session_id", MainActivity.Session_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("get_daily_tasks", json);
        Log.d("kkk", "Socket_отправка - get_daily_tasks - "+ json.toString());

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new MenuFragment()).commit();
            }
        });

        return view;
    }
    @Override
    public void onBackPressed() {
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new MenuFragment()).commit();
    }

    private Emitter.Listener onGetDailyTasks = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONArray tasks = (JSONArray) args[0];
                    JSONObject data;
                    String title;
                    String description;
                    String prizeType;
                    int prize;
                    int progress;
                    int maxProgress;
                    boolean is_completed;
                    PB_loading.setVisibility(View.INVISIBLE);
                    Log.d("kkk", "Socket_принять - get_daily_tasks - " + args[0]);
                    try {
                        for (int i = 0; i < tasks.length(); i++)
                        {
                            data = tasks.getJSONObject(i);
                            title = data.getString("title");
                            description = data.getString("description");
                            prizeType = data.getString("prize_type");
                            prize = data.getInt("award");
                            progress = data.getInt("current_num");
                            maxProgress = data.getInt("max_num");
                            is_completed = data.getBoolean("is_completed");
                            list_tasks.add(new DailyTaskModel(title, description, prizeType, prize, progress, maxProgress, i, is_completed));
                        }
                        dailyTasksAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener onChangeDailyTask = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String title;
                    String description;
                    String prizeType;
                    int prize;
                    int progress;
                    int maxProgress;
                    boolean is_completed;
                    PB_loading.setVisibility(View.INVISIBLE);
                    Log.d("kkk", "Socket_принять - change_daily_task - " + args[0]);
                    try {
                        if (!data.has("error")) {
                            for (int i = 0; i < list_tasks.size(); i++) {
                                if (list_tasks.get(i).changed) {
                                    list_tasks.get(i).changed = false;
                                    title = data.getString("title");
                                    description = data.getString("description");
                                    prizeType = data.getString("prize_type");
                                    prize = data.getInt("award");
                                    progress = data.getInt("current_num");
                                    maxProgress = data.getInt("max_num");
                                    is_completed = data.getBoolean("is_completed");
                                    list_tasks.set(i, new DailyTaskModel(title, description, prizeType, prize, progress, maxProgress, i, is_completed));
                                }
                            }
                            dailyTasksAdapter.notifyDataSetChanged();
                        }
                        else
                        {
                            JSONObject time = data.getJSONObject("error");
                            int hours = time.getInt("hours");
                            int minutes = time.getInt("minutes");
                            int seconds = time.getInt("seconds");

                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            View viewError = getLayoutInflater().inflate(R.layout.dialog_error, null);
                            builder.setView(viewError);
                            TextView TV_error = viewError.findViewById(R.id.dialogError_TV_errorText);
                            TV_error.setText("Вы снова сможете сменить задание через " + hours + "ч " + minutes + "м " + seconds + "с");
                            AlertDialog alert;
                            alert = builder.create();
                            alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            alert.show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };
}