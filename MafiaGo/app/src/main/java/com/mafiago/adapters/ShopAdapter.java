package com.mafiago.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mafiago.R;
import com.mafiago.MainActivity;
import com.mafiago.models.GoldModel;
import com.mafiago.models.ShopModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.mafiago.MainActivity.socket;

public class ShopAdapter extends BaseAdapter {
    public ArrayList<ShopModel> list_shop;
    public Context context;
    public LayoutInflater layout;

    // Конструктор
    public ShopAdapter(ArrayList<ShopModel> list_shop, Context context) {

        this.list_shop = list_shop;
        this.context = context;
        if (context != null) {
            layout=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;

        view = layout.inflate(R.layout.item_shopping, null);
        Button btnBuy = view.findViewById(R.id.itemShopping_btn_buy);
        TextView TV_title = view.findViewById(R.id.itemShopping_TV_title);
        TextView TV_cost = view.findViewById(R.id.itemShopping_TV_description);
        ImageView IV = view.findViewById(R.id.itemShopping_IV);

        TV_cost.setText("Стоимость: " + list_shop.get(position).price + "₽");

        switch (list_shop.get(position).type)
        {
            case "no_ads":
                IV.setImageResource(R.drawable.no_ads_light);
                TV_title.setText("Выключить рекламу");
                break;
            case "convert_money":
                IV.setImageResource(R.drawable.convert_money_light);
                TV_title.setText("Конвертировать золото в монеты");
                break;
            case "convert_XP":
                IV.setImageResource(R.drawable.convert_money_light);
                TV_title.setText("Конвертировать золото в опыт");
                break;
        }


        btnBuy.setOnClickListener(v -> {
            final JSONObject json = new JSONObject();
            try {
                json.put("nick", MainActivity.NickName);
                json.put("session_id", MainActivity.Session_id);
                json.put("store_type", "gold");
                json.put("item", list_shop.get(position).num);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            socket.emit("buy_item", json);
            Log.d("kkk", "Socket_отправка - buy_item - "+ json.toString());
        });
        return view;
    }

    @Override
    public int getCount() {
        return list_shop.size();
    }

    @Override
    public Object getItem(int position) {
        return list_shop.get(position);
    }


    @Override
    public long getItemId(int position) { return position; }
}
