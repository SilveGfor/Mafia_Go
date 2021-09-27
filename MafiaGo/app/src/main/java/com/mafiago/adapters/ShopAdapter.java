package com.mafiago.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
        final int[] num_item = {0};
        final int[] num_price = {0};
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    View viewQuestion = layout.inflate(R.layout.dialog_ok_no, null);
                    builder.setView(viewQuestion);
                    AlertDialog alert = builder.create();
                    TextView TV_text = viewQuestion.findViewById(R.id.dialogOkNo_text);
                    Button btn_yes = viewQuestion.findViewById(R.id.dialogOkNo_btn_yes);
                    Button btn_no = viewQuestion.findViewById(R.id.dialogOkNo_btn_no);
                    TV_text.setText("Вы уверены, что хотите совершить покупку?");
                    btn_yes.setOnClickListener(v1 -> {
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
                        alert.cancel();
                    });
                    btn_no.setOnClickListener(v12 -> {
                        alert.cancel();
                    });
                    alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alert.show();
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

            case "buy_status":
                view = layout.inflate(R.layout.item_buy_status, null);

                Spinner spinner = view.findViewById(R.id.itemBuyStatus_Spinner);
                Spinner spinner2 = view.findViewById(R.id.itemBuyStatus_Spinner2);
                TextView TV_status = view.findViewById(R.id.itemBuyStatus_status);
                TextView TV_desc = view.findViewById(R.id.itemBuyStatus_TV_desc);
                TextView TV_price = view.findViewById(R.id.itemBuyStatus_TV_price);
                TextView TV_nick = view.findViewById(R.id.itemBuyStatus_nick);
                TextView TV_statusText = view.findViewById(R.id.itemBuyStatus_TV_statusText);
                EditText ET_premiumStatus = view.findViewById(R.id.itemBuyStatus_ET_premiumStatus);
                Switch switch_status = view.findViewById(R.id.itemBuyStatus_Switch_status);
                Button btn_buy = view.findViewById(R.id.itemBuyStatus_btn_buy);

                final String[] premium = {"usual"};

                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, list_shop.get(position).list_meaning);
                // Вызываем адаптер
                spinner.setAdapter(spinnerArrayAdapter);

                String[] time_mas;
                time_mas = new String[list_shop.get(position).list_usual_prices.size()];
                for (int i = 0; i < list_shop.get(position).list_usual_prices.size(); i++)
                {
                    time_mas[i] = list_shop.get(position).list_usual_prices.get(i).amount;
                }

                ArrayAdapter<String> spinnerArrayAdapter2 = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, time_mas);
                //Вызываем адаптер
                spinner2.setAdapter(spinnerArrayAdapter2);

                TV_nick.setText(MainActivity.NickName);

                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position2, long id) {
                        TV_status.setText("{" + list_shop.get(position).list_meaning[position2] + "}");
                        num_item[0] = position2;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                switch_status.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        premium[0] = "premium";
                        spinner2.setSelection(1);
                        TV_desc.setText("Статус за золото можно придумать самому");
                        ET_premiumStatus.setVisibility(View.VISIBLE);
                        spinner.setVisibility(View.INVISIBLE);
                        TV_status.setText("{" + ET_premiumStatus.getText() + "}");
                        TV_statusText.setTextColor(Color.parseColor("#F0BF41"));
                    } else {
                        premium[0] = "usual";
                        spinner2.setSelection(0);
                        TV_desc.setText("Статус за монеты можно выбрать из предложенного списка");
                        ET_premiumStatus.setVisibility(View.INVISIBLE);
                        spinner.setVisibility(View.VISIBLE);
                        String[] choose = context.getResources().getStringArray(R.array.statuses);
                        TV_status.setText(choose[spinner.getSelectedItemPosition()]);
                        TV_statusText.setTextColor(Color.parseColor("#848484"));
                    }
                });

                spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position2, long id) {
                        if (!premium[0].equals("premium")) {
                            TV_price.setText("Стоимость: " + list_shop.get(position).list_usual_prices.get(position2).price + " монет");
                        }
                        else {
                            TV_price.setText("Стоимость: " + list_shop.get(position).list_premium_prices.get(position2).price + " золота");
                        }
                        num_price[0] = position2;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

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

                btn_buy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        View viewQuestion = layout.inflate(R.layout.dialog_ok_no, null);
                        builder.setView(viewQuestion);
                        AlertDialog alert = builder.create();
                        TextView TV_text = viewQuestion.findViewById(R.id.dialogOkNo_text);
                        Button btn_yes = viewQuestion.findViewById(R.id.dialogOkNo_btn_yes);
                        Button btn_no = viewQuestion.findViewById(R.id.dialogOkNo_btn_no);
                        TV_text.setText("Вы уверены, что хотите совершить покупку?");
                        btn_yes.setOnClickListener(v1 -> {
                            final JSONObject json = new JSONObject();
                            try {
                                json.put("nick", MainActivity.NickName);
                                json.put("session_id", MainActivity.Session_id);
                                json.put("dop_type", "statuses");
                                json.put("status_type", premium[0]);
                                json.put("store_type", "general");
                                json.put("item", num_price[0]);
                                if (premium[0].equals("usual")) {
                                    json.put("status", list_shop.get(position).list_meaning[num_item[0]]);
                                }
                                else
                                {
                                    json.put("status", ET_premiumStatus.getText());
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            socket.emit("buy_item", json);
                            Log.d("kkk", "Socket_отправка - buy_item - "+ json.toString());
                            alert.cancel();
                        });
                        btn_no.setOnClickListener(v12 -> {
                            alert.cancel();
                        });
                        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        alert.show();
                    }
                });

                break;
            case "buy_color":
                view = layout.inflate(R.layout.item_buy_color, null);

                spinner = view.findViewById(R.id.itemBuyColor_Spinner_color);
                spinner2 = view.findViewById(R.id.itemBuyColor_Spinner_time);
                TV_status = view.findViewById(R.id.itemBuyColor_status);
                TV_desc = view.findViewById(R.id.itemBuyColor_TV_desc);
                TV_price = view.findViewById(R.id.itemBuyColor_TV_price);
                TV_nick = view.findViewById(R.id.itemBuyColor_nick);
                TV_statusText = view.findViewById(R.id.itemBuyColor_TV_statusText);
                switch_status = view.findViewById(R.id.itemBuyColor_Switch_status);
                btn_buy = view.findViewById(R.id.itemBuyColor_btn_buy);

                num_item[0] = 0;
                num_price[0] = 0;

                //final String[] premium = {"usual"};
                String[] list_meaning = new String[list_shop.get(position).list_meaning.length];
                for (int i = 0; i < list_shop.get(position).list_meaning.length; i++)
                {
                    switch (list_shop.get(position).list_meaning[i])
                    {
                        case "#8DD3B6":
                            list_meaning[i] = "мятный цвет";
                            break;
                        case "#AFCAFF":
                            list_meaning[i] = "светло-синий цвет";
                            break;
                        case "#CBFFA1":
                            list_meaning[i] = "салатовый цвет";
                            break;
                        default:
                            list_meaning[i] = list_shop.get(position).list_meaning[i];
                            break;
                    }
                }

                spinnerArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, list_meaning);
                //Вызываем адаптер
                spinner.setAdapter(spinnerArrayAdapter);

                time_mas = new String[list_shop.get(position).list_usual_prices.size()];
                for (int i = 0; i < list_shop.get(position).list_usual_prices.size(); i++)
                {
                    time_mas[i] = list_shop.get(position).list_usual_prices.get(i).amount;
                }

                spinnerArrayAdapter2 = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, time_mas);
                //Вызываем адаптер
                spinner2.setAdapter(spinnerArrayAdapter2);

                TV_nick.setText(MainActivity.NickName);

                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position2, long id) {
                        TV_status.setTextColor(Color.parseColor(list_shop.get(position).list_meaning[position2]));
                        TV_nick.setTextColor(Color.parseColor(list_shop.get(position).list_meaning[position2]));
                        num_item[0] = position2;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position2, long id) {
                        TV_price.setText("Стоимость: " + list_shop.get(position).list_usual_prices.get(position2).price + " монет");
                        num_price[0] = position2;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                /*
                switch_status.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        //premium[0] = "premium";
                        TV_desc.setText("Статус за золото можно придумать самому");
                        spinner.setVisibility(View.INVISIBLE);
                        TV_status.setText("{" + ET_premiumStatus.getText() + "}");
                        TV_statusText.setTextColor(Color.parseColor("#F0BF41"));
                    } else {
                        //premium[0] = "usual";
                        TV_desc.setText("Статус за монеты можно выбрать из предложенного списка");
                        spinner.setVisibility(View.VISIBLE);
                        String[] choose = context.getResources().getStringArray(R.array.statuses);
                        TV_status.setText(choose[spinner.getSelectedItemPosition()]);
                        TV_statusText.setTextColor(Color.parseColor("#848484"));
                    }
                });
                 */

                btn_buy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        View viewQuestion = layout.inflate(R.layout.dialog_ok_no, null);
                        builder.setView(viewQuestion);
                        AlertDialog alert = builder.create();
                        TextView TV_text = viewQuestion.findViewById(R.id.dialogOkNo_text);
                        Button btn_yes = viewQuestion.findViewById(R.id.dialogOkNo_btn_yes);
                        Button btn_no = viewQuestion.findViewById(R.id.dialogOkNo_btn_no);
                        TV_text.setText("Вы уверены, что хотите совершить покупку?");
                        btn_yes.setOnClickListener(v1 -> {
                            final JSONObject json = new JSONObject();
                            try {
                                json.put("nick", MainActivity.NickName);
                                json.put("session_id", MainActivity.Session_id);
                                json.put("dop_type", "personal_colors");
                                json.put("color_type", "usual");
                                json.put("store_type", "general");
                                json.put("item", num_price[0]);
                                json.put("color", list_shop.get(position).list_meaning[num_item[0]]);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            socket.emit("buy_item", json);
                            Log.d("kkk", "Socket_отправка - buy_item - "+ json.toString());
                            alert.cancel();
                        });
                        btn_no.setOnClickListener(v12 -> {
                            alert.cancel();
                        });
                        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        alert.show();
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
