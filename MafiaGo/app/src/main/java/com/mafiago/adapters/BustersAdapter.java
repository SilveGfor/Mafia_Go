package com.mafiago.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.mafiago.R;
import com.mafiago.MainActivity;
import com.mafiago.enums.Role;
import com.mafiago.models.BusterModel;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;

import java.util.ArrayList;

public class BustersAdapter extends BaseAdapter {
    public ArrayList<BusterModel> list_busters;
    public Context context;
    public LayoutInflater layout;

    // Конструктор
    public BustersAdapter(ArrayList<BusterModel> list_busters, Context context) {

        this.list_busters = list_busters;
        this.context = context;
        if (context != null) {
            layout=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        switch (list_busters.get(position).type)
        {
            case "status":
                view = layout.inflate(R.layout.item_buster_status_color, null);
                ShimmerTextView TV_type = view.findViewById(R.id.itemBusterStatusColor_TV_type);
                TextView TV_object = view.findViewById(R.id.itemBusterStatusColor_TV_object);
                TextView TV_time = view.findViewById(R.id.itemBusterStatusColor_TV_time);

                TV_type.setText("Статус:");

                TV_object.setText("{" + list_busters.get(position).object + "}");
                TV_time.setText(list_busters.get(position).time);
                break;
            case "color":
                view = layout.inflate(R.layout.item_buster_status_color, null);
                TV_type = view.findViewById(R.id.itemBusterStatusColor_TV_type);
                TV_object = view.findViewById(R.id.itemBusterStatusColor_TV_object);
                TV_time = view.findViewById(R.id.itemBusterStatusColor_TV_time);

                TV_type.setText("Цвет:");

                TV_object.setText(MainActivity.NickName);
                TV_object.setTextColor(Color.parseColor(list_busters.get(position).object));
                TV_time.setText(list_busters.get(position).time);
                break;
            case "chance_of_role":
                view = layout.inflate(R.layout.item_buster_status_color, null);
                ShimmerTextView STV_type = view.findViewById(R.id.itemBusterStatusColor_TV_type);
                TV_object = view.findViewById(R.id.itemBusterStatusColor_TV_object);
                TV_time = view.findViewById(R.id.itemBusterStatusColor_TV_time);

                if (list_busters.get(position).is_premium) {
                    STV_type.setText("Шанс 45%");
                    Shimmer shimmer = new Shimmer();
                    shimmer.start(STV_type);
                }
                else
                {
                    STV_type.setText("Шанс 25%");
                }

                switch (list_busters.get(position).object)
                {
                    case "citizen":
                        TV_object.setText("Мирный житель");
                        break;
                    case "mafia":
                        TV_object.setText("Мафия");
                        break;
                    case "sheriff":
                        TV_object.setText("Шериф");
                        break;
                    case "doctor":
                        TV_object.setText("Доктор");
                        break;
                    case "lover":
                        TV_object.setText("Любовница");
                        break;
                    case "mafia_don":
                        TV_object.setText("Дон мафии");
                        break;
                    case "maniac":
                        TV_object.setText("Маньяк");
                        break;
                    case "terrorist":
                        TV_object.setText("Террорист");
                        break;
                    case "bodyguard":
                        TV_object.setText("Телохранитель");
                        break;
                    case "poisoner":
                        TV_object.setText("Отравитель");
                        break;
                    case "journalist":
                        TV_object.setText("Журналист");
                        break;
                    case "doctor_of_easy_virtue":
                        TV_object.setText("ДЛП");
                        break;
                }
                TV_time.setText(list_busters.get(position).time);
                break;
        }
        return view;
    }

    @Override
    public int getCount() {
        return list_busters.size();
    }

    @Override
    public Object getItem(int position) {
        return list_busters.get(position);
    }


    @Override
    public long getItemId(int position) { return position; }
}
