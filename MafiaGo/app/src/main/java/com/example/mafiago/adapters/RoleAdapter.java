package com.example.mafiago.adapters;

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
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.example.mafiago.R;
import com.example.mafiago.fragments.CreateRoomFragment;
import com.example.mafiago.models.RoleModel;

import org.json.JSONException;

import java.util.ArrayList;

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
        CardView CV = view.findViewById(R.id.ItemRole_CV);
        CheckBox CB = view.findViewById(R.id.ItemRole_CB);

        String role = String.valueOf(list_roles.get(position).role).toLowerCase();
        CB.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                if (list_roles.get(position).peaceful) {
                    CreateRoomFragment.peaceful.put(role);
                }
                else
                {
                    CreateRoomFragment.mafia.put(role);
                }
            }
            else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    if (list_roles.get(position).peaceful) {
                        for (int i = 0; i < CreateRoomFragment.peaceful.length(); i++) {
                            try {
                                if (role.equals(CreateRoomFragment.peaceful.getString(i)))
                                {
                                    CreateRoomFragment.peaceful.remove(i);
                                    break;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    else
                    {
                        for (int i = 0; i < CreateRoomFragment.mafia.length(); i++) {
                            try {
                                if (role.equals(CreateRoomFragment.mafia.getString(i)))
                                {
                                    CreateRoomFragment.mafia.remove(i);
                                    break;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }
            }
        });

        switch (list_roles.get(position).role)
        {
            case DOCTOR:
                CV.setCardBackgroundColor(Color.parseColor("#009900"));
                IV_role.setImageResource(R.drawable.doctor_alive);
                break;
            case LOVER:
                CV.setCardBackgroundColor(Color.parseColor("#009900"));
                IV_role.setImageResource(R.drawable.lover_alive);
                break;
            case SHERIFF:
                CV.setCardBackgroundColor(Color.parseColor("#009900"));
                IV_role.setImageResource(R.drawable.sheriff_alive);
                break;
            case MAFIA_DON:
                CV.setCardBackgroundColor(Color.parseColor("#880000"));
                IV_role.setImageResource(R.drawable.mafia_alive);
                break;
        }
        return view;
    }
}
