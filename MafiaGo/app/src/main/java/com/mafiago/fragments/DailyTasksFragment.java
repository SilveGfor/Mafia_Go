package com.mafiago.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

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

    DailyTasksAdapter dailyTasksAdapter;
    ArrayList<DailyTaskModel> list_tasks = new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_daily_tasks, container, false);

        LV_tasks = view.findViewById(R.id.fragmentDailyTasks_LV_tasks);
        PB_loading = view.findViewById(R.id.fragmentDailyTasks_PB);

        dailyTasksAdapter = new DailyTasksAdapter(list_tasks, getContext());
        LV_tasks.setAdapter(dailyTasksAdapter);

        socket.on("get_daily_tasks", onGetDailyTasks);

        final JSONObject json = new JSONObject();
        try {
            json.put("nick", MainActivity.NickName);
            json.put("session_id", MainActivity.Session_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("get_daily_tasks", json);
        Log.d("kkk", "Socket_отправка - get_daily_tasks - "+ json.toString());

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
                            list_tasks.add(new DailyTaskModel(title, description, prizeType, prize, progress, maxProgress, i));
                        }
                        dailyTasksAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };
}