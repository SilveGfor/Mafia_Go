package com.mafiago.adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.example.mafiago.R;
import com.mafiago.MainActivity;
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
        View view = null;
        Button btnBuy;
        TextView TV_title;
        TextView TV_cost;
        ImageView IV;
        switch (list_shop.get(position).type)
        {
            case "no_ads":
                view = layout.inflate(R.layout.item_shopping, null);
                btnBuy = view.findViewById(R.id.itemShopping_btn_buy);
                TV_title = view.findViewById(R.id.itemShopping_TV_title);
                TV_cost = view.findViewById(R.id.itemBuyStatus_TV_price);
                IV = view.findViewById(R.id.itemShopping_IV);

                TV_cost.setText("Стоимость: " + list_shop.get(position).price + "₽");

                IV.setImageResource(R.drawable.no_ads_light);
                TV_title.setText("Выключить рекламу");

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
                break;
            case "convert_money":
                view = layout.inflate(R.layout.item_shopping, null);
                btnBuy = view.findViewById(R.id.itemShopping_btn_buy);
                TV_title = view.findViewById(R.id.itemShopping_TV_title);
                TV_cost = view.findViewById(R.id.itemBuyStatus_TV_price);
                IV = view.findViewById(R.id.itemShopping_IV);

                TV_cost.setText("Стоимость: " + list_shop.get(position).price + "₽");

                IV.setImageResource(R.drawable.convert_money_light);
                TV_title.setText("Конвертировать золото в монеты");

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
                break;
            case "convert_XP":
                view = layout.inflate(R.layout.item_shopping, null);
                btnBuy = view.findViewById(R.id.itemShopping_btn_buy);
                TV_title = view.findViewById(R.id.itemShopping_TV_title);
                TV_cost = view.findViewById(R.id.itemBuyStatus_TV_price);
                IV = view.findViewById(R.id.itemShopping_IV);

                TV_cost.setText("Стоимость: " + list_shop.get(position).price + "₽");

                IV.setImageResource(R.drawable.convert_money_light);
                TV_title.setText("Конвертировать золото в опыт");

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
                break;

            case "status":
                view = layout.inflate(R.layout.item_buy_status, null);

                Spinner spinner = view.findViewById(R.id.itemBuyStatus_Spinner);
                TextView TV_status = view.findViewById(R.id.itemBuyStatus_status);
                TextView TV_desc = view.findViewById(R.id.itemBuyStatus_TV_desc);
                TextView TV_price = view.findViewById(R.id.itemBuyStatus_TV_price);
                TextView TV_nick = view.findViewById(R.id.itemBuyStatus_nick);
                TextView TV_statusText = view.findViewById(R.id.itemBuyStatus_TV_statusText);
                EditText ET_premiumStatus = view.findViewById(R.id.itemBuyStatus_ET_premiumStatus);
                Switch switch_status = view.findViewById(R.id.itemBuyStatus_Switch_status);

                // Настраиваем адаптер
                ArrayAdapter<?> adapter =
                        ArrayAdapter.createFromResource(context, R.array.statuses, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // Вызываем адаптер
                spinner.setAdapter(adapter);

                TV_nick.setText(MainActivity.NickName);

                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String[] choose = context.getResources().getStringArray(R.array.statuses);
                        TV_status.setText(choose[position]);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                switch_status.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        TV_desc.setText("Статус за золото можно придумать самому");
                        TV_price.setText("Стоимость 100 золота\nВы можете купить премиум-аккаунт - тогда вы сможете бесплатно поставить себе премиум-статус");
                        ET_premiumStatus.setVisibility(View.VISIBLE);
                        spinner.setVisibility(View.INVISIBLE);
                        TV_status.setText("{" + ET_premiumStatus.getText() + "}");
                        TV_statusText.setTextColor(Color.parseColor("#F0BF41"));
                    } else {
                        TV_desc.setText("Статус за монеты можно выбрать из предложенного списка");
                        TV_price.setText("Стоимость 7000 монет\nВы можете купить премиум-аккаунт - тогда вы сможете бесплатно поставить себе премиум-статус");
                        ET_premiumStatus.setVisibility(View.INVISIBLE);
                        spinner.setVisibility(View.VISIBLE);
                        String[] choose = context.getResources().getStringArray(R.array.statuses);
                        TV_status.setText(choose[spinner.getSelectedItemPosition()]);
                        TV_statusText.setTextColor(Color.parseColor("#848484"));
                    }
                });
                ET_premiumStatus.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        TV_status.setText("{" + ET_premiumStatus.getText() + "}");
                    }
                });

                break;
        }



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
