package com.mafiago.adapters;


import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mafiago.R;
import com.mafiago.MainActivity;
import com.mafiago.fragments.GameFragment;
import com.mafiago.models.RoomModel;

import java.util.ArrayList;

public class GamesAdapter extends BaseAdapter {
    public ArrayList<RoomModel> list_room;
    public Context context;
    public LayoutInflater layout;

    public GamesAdapter(ArrayList<RoomModel> list_room, Context context)
    {
        this.list_room = list_room;
        this.context = context;
         layout=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount()
    {
        return list_room.size();
    }
    @Override
    public Object getItem(int position)
    {
        return list_room.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        view = layout.inflate(R.layout.item_game, null);

        //ProgressBar PB_users = view.findViewById(R.id.Item_game_progressBar);

        RelativeLayout RL_info = view.findViewById(R.id.itemGame_RL_info);

        ArrayList<String> list_roles = list_room.get(position).list_roles;

        ImageView IV_maniac = view.findViewById(R.id.itemGame_maniac);
        ImageView IV_doctor = view.findViewById(R.id.itemGame_doctor);
        ImageView IV_lover = view.findViewById(R.id.itemGame_lover);
        ImageView IV_mafia_don = view.findViewById(R.id.itemGame_mafia_don);
        ImageView IV_poisoner = view.findViewById(R.id.itemGame_poisoner);
        ImageView IV_journalist = view.findViewById(R.id.itemGame_journalist);
        ImageView IV_terrorist = view.findViewById(R.id.itemGame_terrorist);
        ImageView IV_doctor_of_easy_virtue = view.findViewById(R.id.itemGame_doctor_of_easy_virtue);
        ImageView IV_bodyguard = view.findViewById(R.id.itemGame_bodyguard);
        ImageView IV_lock = view.findViewById(R.id.itemGame_lock);

        ImageView IV_citizen = view.findViewById(R.id.itemGame_citizen);
        ImageView IV_sheriff = view.findViewById(R.id.itemGame_sheriff);
        ImageView IV_mafia = view.findViewById(R.id.itemGame_mafia);

        //PB_users.setMax(list_room.get(position).max_people);
        //PB_users.setProgress(list_room.get(position).num_people);

        TextView txt_room_name = view.findViewById(R.id.itemGame_TV_roomName);
        TextView TV_roomState = view.findViewById(R.id.itemGame_TV_roomState);
        TextView txt_min_max_people = view.findViewById(R.id.itemGame_TV_minMaxPlayers);
        TextView txt_num_people = view.findViewById(R.id.itemGame_TV_playersInRoom);
        TextView TV_customRoom = view.findViewById(R.id.itemGame_TV_customRoom);

        RL_info.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View view_listOfUsers = layout.inflate(R.layout.dialog_list_of_users_in_room, null);
            builder.setView(view_listOfUsers);

            ListView LV_users = view_listOfUsers.findViewById(R.id.dialogFines_LV_listOfFines);

            UsersInRoomAdapter usersInRoomAdapter = new UsersInRoomAdapter(list_room.get(position).list_users, context);
            LV_users.setAdapter(usersInRoomAdapter);

            AlertDialog alert = builder.create();
            alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alert.show();
        });

        View finalView = view;

        view.setOnClickListener(v -> {
            AppCompatActivity activity = (AppCompatActivity) finalView.getContext();

            if (MainActivity.Rang >= 2 || !list_room.get(position).is_custom) {
                if (!list_room.get(position).has_password) {
                    MainActivity.Game_id = list_room.get(position).id;
                    MainActivity.RoomName = list_room.get(position).name;
                    MainActivity.PlayersMinMaxInfo = "???? " + list_room.get(position).min_people + " ???? " + list_room.get(position).max_people;
                    Log.d("kkk", "?????????????? ?? ???????? - " + MainActivity.Game_id);
                    activity.getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GameFragment()).commit();
                } else {
                    int game_id = list_room.get(position).id;
                    String room_name = list_room.get(position).name;
                    String playersMinMaxInfo = "???? " + list_room.get(position).min_people + " ???? " + list_room.get(position).max_people;
                    ;
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    View viewDang = layout.inflate(R.layout.dialog_room_password, null);
                    builder.setView(viewDang);
                    EditText ET_password = viewDang.findViewById(R.id.dialogRoomPassword_ET_password);
                    Button btn_join = viewDang.findViewById(R.id.dialogRoomPassword_btn_join);
                    AlertDialog alert = builder.create();
                    btn_join.setOnClickListener(v1 -> {
                        MainActivity.Game_id = game_id;
                        MainActivity.RoomName = room_name;
                        MainActivity.PlayersMinMaxInfo = playersMinMaxInfo;
                        MainActivity.Password = ET_password.getText().toString();
                        Log.d("kkk", "?????????????? ?? ???????? - " + MainActivity.Game_id);
                        alert.cancel();
                        activity.getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GameFragment()).commit();
                    });
                    alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alert.show();

                }
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                View viewDang = layout.inflate(R.layout.dialog_error, null);
                builder.setView(viewDang);
                TextView TV_title = viewDang.findViewById(R.id.dialogError_TV_errorTitle);
                TextView TV_error = viewDang.findViewById(R.id.dialogError_TV_errorText);
                TV_title.setText("???????? ???? 2 ?????????? ????????????????!");
                TV_error.setText("?????????????????? ?? ???????????? ?? ?????????????????? ???????????????? ?????????? ???????????? ?????????? ???????????????????? 2 ??????????");
                AlertDialog alert = builder.create();
                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alert.show();
            }
        });

        for (int i = 0; i < list_roles.size(); i++) {
            switch (list_roles.get(i)) {
                case "doctor":
                    IV_doctor.setVisibility(View.VISIBLE);
                    break;
                case "lover":
                    IV_lover.setVisibility(View.VISIBLE);
                    break;
                case "mafia_don":
                    IV_mafia_don.setVisibility(View.VISIBLE);
                    break;
                case "maniac":
                    IV_maniac.setVisibility(View.VISIBLE);
                    break;
                case "terrorist":
                    IV_terrorist.setVisibility(View.VISIBLE);
                    break;
                case "bodyguard":
                    IV_bodyguard.setVisibility(View.VISIBLE);
                    break;
                case "poisoner":
                    IV_poisoner.setVisibility(View.VISIBLE);
                    break;
                case "journalist":
                    IV_journalist.setVisibility(View.VISIBLE);
                    break;
                case "doctor_of_easy_virtue":
                    IV_doctor_of_easy_virtue.setVisibility(View.VISIBLE);
                    break;
                case "mafia":
                    IV_mafia.setVisibility(View.VISIBLE);
                    break;
                case "citizen":
                    IV_citizen.setVisibility(View.VISIBLE);
                    break;
                case "sheriff":
                    IV_sheriff.setVisibility(View.VISIBLE);
                    break;
            }
        }

        if (list_room.get(position).has_password) {
            IV_lock.setVisibility(View.VISIBLE);
        }

        if (list_room.get(position).is_custom) {
            TV_customRoom.setVisibility(View.VISIBLE);
        }

        if (list_room.get(position).is_on) {
            TV_roomState.setText("???????? ????????");
            TV_roomState.setTextColor(Color.parseColor("#F44336"));
        } else {
            TV_roomState.setText("??????????");
            //TV_roomState.setTextColor(Color.parseColor("#4CAF50"));
        }

        txt_room_name.setText(list_room.get(position).name);
        txt_min_max_people.setText("???? " + list_room.get(position).min_people + " ???? " + list_room.get(position).max_people);
        txt_num_people.setText("??????????????: " + list_room.get(position).num_people);
        return view;
    }
}