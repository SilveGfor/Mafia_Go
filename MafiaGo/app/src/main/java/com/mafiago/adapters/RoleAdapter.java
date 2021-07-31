package com.mafiago.adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.example.mafiago.R;
import com.mafiago.fragments.CreateRoomFragment;
import com.mafiago.models.RoleModel;

import org.json.JSONException;

import java.util.ArrayList;

import static com.mafiago.fragments.CreateRoomFragment.mafia;
import static com.mafiago.fragments.CreateRoomFragment.peaceful;

public class RoleAdapter extends BaseAdapter {
    public ArrayList<RoleModel> list_roles;
    public Context context;
    public LayoutInflater layout;

    public RoleAdapter(ArrayList<RoleModel> list_roles, Context context)
    {
        this.list_roles = list_roles;
        this.context = context;
        layout=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return list_roles.size();
    }

    @Override
    public Object getItem(int position) {
        return list_roles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        view = layout.inflate(R.layout.item_role, null);

        ImageView IV_role = view.findViewById(R.id.ItemRole_IV_role);
        TextView TV_role = view.findViewById(R.id.itemRole_TV_role);

        String role = String.valueOf(list_roles.get(position).role).toLowerCase();
        TV_role.setText(role);

        IV_role.setOnClickListener(v -> {
            try {
                boolean addRole = true;
                for (int i = 0; i < peaceful.length(); i++)
                {
                    if (peaceful.get(i).equals(role))
                    {
                        addRole = false;
                    }
                }
                for (int i = 0; i < mafia.length(); i++)
                {
                    if (mafia.get(i).equals(role))
                    {
                        addRole = false;
                    }
                }
                if (addRole)
                {
                    if (list_roles.get(position).peaceful) {
                        peaceful.put(role);
                    }
                    else
                    {
                        mafia.put(role);
                    }
                    switch (list_roles.get(position).role)
                    {
                        case DOCTOR:
                            IV_role.setImageResource(R.drawable.doctor_alive);
                            break;
                        case LOVER:
                            IV_role.setImageResource(R.drawable.lover_alive);
                            break;
                        case MAFIA_DON:
                            IV_role.setImageResource(R.drawable.mafia_don_alive);
                            break;
                        case JOURNALIST:
                            IV_role.setImageResource(R.drawable.journalist_alive);
                            break;
                        case MANIAC:
                            IV_role.setImageResource(R.drawable.maniac_alive);
                            break;
                        case TERRORIST:
                            IV_role.setImageResource(R.drawable.terrorist_alive);
                            break;
                        case BODYGUARD:
                            IV_role.setImageResource(R.drawable.bodyguard_alive);
                            break;
                        case POISONER:
                            IV_role.setImageResource(R.drawable.poisoner_alive);
                            break;
                        case DOCTOR_OF_EASY_VIRTUE:
                            IV_role.setImageResource(R.drawable.doctor_of_easy_virtue_alive);
                            break;
                    }
                }
                else
                {
                    IV_role.setImageResource(R.drawable.grey_card);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        if (list_roles.get(position).peaceful) {
                            for (int i = 0; i < peaceful.length(); i++) {
                                try {
                                    if (role.equals(peaceful.get(i))) {
                                        peaceful.remove(i);
                                        break;
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            for (int i = 0; i < mafia.length(); i++) {
                                try {
                                    if (role.equals(mafia.get(i))) {
                                        mafia.remove(i);
                                        break;
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        });
        return view;
    }
}
