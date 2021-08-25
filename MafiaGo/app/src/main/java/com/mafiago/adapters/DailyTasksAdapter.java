package com.mafiago.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.mafiago.R;
import com.mafiago.MainActivity;
import com.mafiago.models.DailyTaskModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.mafiago.MainActivity.socket;

public class DailyTasksAdapter extends BaseAdapter {
    public ArrayList<DailyTaskModel> list_tasks;
    public Context context;
    public LayoutInflater layout;

    // Конструктор
    public DailyTasksAdapter(ArrayList<DailyTaskModel> list_tasks, Context context) {

        this.list_tasks = list_tasks;
        this.context = context;
        if (context != null) {
            layout=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;

        view = layout.inflate(R.layout.item_daily_task, null);
        TextView TV_title = view.findViewById(R.id.itemDailyTask_TV_title);
        TextView TV_description = view.findViewById(R.id.itemDailyTask_TV_description);
        ImageView IV_prize = view.findViewById(R.id.itemDailyTask_IV_prize);
        ImageView IV_taskImage = view.findViewById(R.id.itemDailyTask_IV_taskImage);
        TextView TV_prize = view.findViewById(R.id.itemDailyTask_TV_prize);
        ProgressBar PB = view.findViewById(R.id.itemDailyTask_PB_horizontal);
        TextView TV_progress = view.findViewById(R.id.dialogCompleteDailyTask_TV_progress);
        Button btn_change = view.findViewById(R.id.itemDailyTask_btn_changeTask);

        if (!MainActivity.Theme.equals("dark"))
        {
            IV_taskImage.setImageResource(R.drawable.game_goal_round_light);
        }

        if (!list_tasks.get(position).completed)
        {
            btn_change.setOnClickListener(v -> {
                final JSONObject json = new JSONObject();
                try {
                    json.put("nick", MainActivity.NickName);
                    json.put("session_id", MainActivity.Session_id);
                    json.put("change_task_num", list_tasks.get(position).num);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                list_tasks.get(position).changed = true;
                socket.emit("change_daily_task", json);
                Log.d("kkk", "Socket_отправка - change_daily_task - "+ json.toString());
            });
        }
        else
        {
            btn_change.setTextColor(Color.parseColor("#848484"));
        }


        TV_title.setText(list_tasks.get(position).title);
        TV_description.setText(list_tasks.get(position).description);
        if (list_tasks.get(position).prizeType.equals("exp"))
        {
            IV_prize.setImageResource(R.drawable.experience);
            TV_prize.setText(list_tasks.get(position).prize + " XP");
        }
        else
        {
            IV_prize.setImageResource(R.drawable.money);
            TV_prize.setText(list_tasks.get(position).prize + " $");
        }
        TV_progress.setText(list_tasks.get(position).progress + "/" + list_tasks.get(position).maxProgress);
        PB.setMax(list_tasks.get(position).maxProgress);
        PB.setProgress(list_tasks.get(position).progress);
        return view;
    }

    @Override
    public int getCount() {
        return list_tasks.size();
    }

    @Override
    public Object getItem(int position) {
        return list_tasks.get(position);
    }


    @Override
    public long getItemId(int position) { return position; }
}
