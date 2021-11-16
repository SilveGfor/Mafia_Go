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
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.mafiago.MainActivity.socket;

public class  ShopAdapter extends BaseAdapter {
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
        final String[] premium = {"usual"};
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
                view = layout.inflate(R.layout.item_convert_money, null);
                btnBuy = view.findViewById(R.id.itemConvertMoney_btn_buy);
                TV_title = view.findViewById(R.id.itemConvertMoney_TV_title);
                TV_cost = view.findViewById(R.id.itemConvertMoney_TV_price);
                Spinner spinner_convert = view.findViewById(R.id.itemConvertMoney_Spinner);
                TextView TV_desc_convert = view.findViewById(R.id.itemConvertMoney_TV_desc);
                IV = view.findViewById(R.id.itemConvertMoney_IV);
                ImageView IV_money = view.findViewById(R.id.itemConvertMoney_IV_money);

                ArrayAdapter<String> spinnerArrayAdapter_convert = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, list_shop.get(position).list_meaning);
                spinner_convert.setAdapter(spinnerArrayAdapter_convert);

                spinner_convert.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position2, long id) {
                        TV_cost.setText("Стоимость: " + list_shop.get(position).list_usual_prices.get(position2).price + " золота");
                        num_item[0] = position2;
                        IV_money.setImageResource(R.drawable.gold);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

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
                            json.put("store_type", "general");
                            json.put("dop_type", "conversion");
                            json.put("conversion_type", "money");
                            json.put("item", num_item[0]);
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
            case "convert_exp":
                view = layout.inflate(R.layout.item_convert_money, null);

                btnBuy = view.findViewById(R.id.itemConvertMoney_btn_buy);
                TV_title = view.findViewById(R.id.itemConvertMoney_TV_title);
                TV_cost = view.findViewById(R.id.itemConvertMoney_TV_price);
                spinner_convert = view.findViewById(R.id.itemConvertMoney_Spinner);
                TV_desc_convert = view.findViewById(R.id.itemConvertMoney_TV_desc);
                IV = view.findViewById(R.id.itemConvertMoney_IV);
                IV_money = view.findViewById(R.id.itemConvertMoney_IV_money);

                TV_desc_convert.setText("Вы можете конвертировать золото в опыт. Выберите количество опыта, которое вы хотите получить");
                TV_title.setText("Конвертация золота в опыт");
                IV.setImageResource(R.drawable.convert_xp_light);

                spinnerArrayAdapter_convert = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, list_shop.get(position).list_meaning);
                spinner_convert.setAdapter(spinnerArrayAdapter_convert);

                spinner_convert.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position2, long id) {
                        TV_cost.setText("Стоимость: " + list_shop.get(position).list_usual_prices.get(position2).price + " золота");
                        num_item[0] = position2;
                        IV_money.setImageResource(R.drawable.gold);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

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
                            json.put("store_type", "general");
                            json.put("dop_type", "conversion");
                            json.put("conversion_type", "exp");
                            json.put("item", num_item[0]);
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
                IV_money = view.findViewById(R.id.itemBuyStatus_IV_money);

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
                        TV_desc.setText("Статус за золото можно придумать самому");
                        ET_premiumStatus.setVisibility(View.VISIBLE);
                        spinner.setVisibility(View.INVISIBLE);
                        TV_status.setText("{" + ET_premiumStatus.getText() + "}");
                        TV_statusText.setTextColor(Color.parseColor("#F0BF41"));
                        IV_money.setImageResource(R.drawable.gold);
                    } else {
                        premium[0] = "usual";
                        TV_desc.setText("Статус за монеты можно выбрать из предложенного списка");
                        ET_premiumStatus.setVisibility(View.INVISIBLE);
                        spinner.setVisibility(View.VISIBLE);
                        String[] choose = context.getResources().getStringArray(R.array.statuses);
                        TV_status.setText(choose[spinner.getSelectedItemPosition()]);
                        TV_statusText.setTextColor(Color.parseColor("#848484"));
                        IV_money.setImageResource(R.drawable.money);
                    }
                    if (spinner2.getSelectedItemPosition() != 0)
                    {
                        spinner2.setSelection(0);
                    }
                    else {
                        spinner2.setSelection(1);
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
                        //1-15
                    }
                });

                btn_buy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if ((ET_premiumStatus.getText().length() >= 1 && ET_premiumStatus.getText().length() <= 15) || premium[0].equals("usual")) {
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
                                    } else {
                                        json.put("status", ET_premiumStatus.getText());
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                socket.emit("buy_item", json);
                                Log.d("kkk", "Socket_отправка - buy_item - " + json.toString());
                                alert.cancel();
                            });
                            btn_no.setOnClickListener(v12 -> {
                                alert.cancel();
                            });
                            alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            alert.show();
                        }
                        else
                        {
                            if (ET_premiumStatus.getText().length() == 0)
                            {
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                View viewDang = layout.inflate(R.layout.dialog_error, null);
                                builder.setView(viewDang);
                                TextView TV_title = viewDang.findViewById(R.id.dialogError_TV_errorTitle);
                                TextView TV_error = viewDang.findViewById(R.id.dialogError_TV_errorText);
                                TV_title.setText("Пустой статус!");
                                TV_error.setText("Вы можете придумать статус длиной от 1 символа до 15 включительно");
                                AlertDialog alert = builder.create();
                                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                alert.show();
                            }
                            else if (ET_premiumStatus.getText().length() >= 15)
                            {
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                View viewDang = layout.inflate(R.layout.dialog_error, null);
                                builder.setView(viewDang);
                                TextView TV_title = viewDang.findViewById(R.id.dialogError_TV_errorTitle);
                                TextView TV_error = viewDang.findViewById(R.id.dialogError_TV_errorText);
                                TV_title.setText("Длинный статус!");
                                TV_error.setText("Вы можете придумать статус длиной от 1 символа до 15 включительно");
                                AlertDialog alert = builder.create();
                                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                alert.show();
                            }
                        }
                    }
                });

                break;
            case "buy_color":
                view = layout.inflate(R.layout.item_buy_color, null);
                Spinner spinner_usual = view.findViewById(R.id.itemBuyColor_Spinner_usual);
                Spinner spinner_premium = view.findViewById(R.id.itemBuyColor_Spinner_premium);
                Spinner spinner_time = view.findViewById(R.id.itemBuyColor_Spinner_time);
                TV_desc = view.findViewById(R.id.itemBuyColor_TV_desc);
                TV_price = view.findViewById(R.id.itemBuyColor_TV_price);
                TV_nick = view.findViewById(R.id.itemBuyColor_nick);
                TV_statusText = view.findViewById(R.id.itemBuyColor_TV_statusText);
                switch_status = view.findViewById(R.id.itemBuyColor_Switch_status);
                btn_buy = view.findViewById(R.id.itemBuyColor_btn_buy);
                IV_money = view.findViewById(R.id.itemBuyColor_IV_money);

                num_item[0] = 0;
                num_price[0] = 0;

                TV_nick.setText(MainActivity.NickName);

                String[] list_meaning_usual = new String[list_shop.get(position).mas_usual_time.length];
                for (int i = 0; i < list_shop.get(position).mas_usual_time.length; i++)
                {
                    switch (list_shop.get(position).mas_usual_time[i])
                    {
                        case "#8DD3B6":
                            list_meaning_usual[i] = "мятный цвет";
                            break;
                        case "#AFCAFF":
                            list_meaning_usual[i] = "светло-синий цвет";
                            break;
                        case "#CBFFA1":
                            list_meaning_usual[i] = "салатовый цвет";
                            break;
                        default:
                            list_meaning_usual[i] = list_shop.get(position).mas_usual_time[i];
                            break;
                    }
                }

                String[] list_meaning_premium = new String[list_shop.get(position).mas_premium_time.length];
                for (int i = 0; i < list_shop.get(position).mas_premium_time.length; i++)
                {
                    switch (list_shop.get(position).mas_premium_time[i])
                    {
                        case "#FFE5A1":
                            list_meaning_premium[i] = "золотой цвет";
                            break;
                        case "#AFFFFF":
                            list_meaning_premium[i] = "голубой цвет";
                            break;
                        case "#FFAFCC":
                            list_meaning_premium[i] = "розовый цвет";
                            break;
                        default:
                            list_meaning_premium[i] = list_shop.get(position).mas_premium_time[i];
                            break;
                    }
                }

                time_mas = new String[list_shop.get(position).list_usual_prices.size()];
                for (int i = 0; i < list_shop.get(position).list_usual_prices.size(); i++)
                {
                    time_mas[i] = list_shop.get(position).list_usual_prices.get(i).amount;
                }

                ArrayAdapter<String> spinnerArrayAdapter3 = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, time_mas);
                //Вызываем адаптер
                spinner_time.setAdapter(spinnerArrayAdapter3);

                spinner_time.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

                spinnerArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, list_meaning_premium);
                // Вызываем адаптер
                spinner_premium.setAdapter(spinnerArrayAdapter);

                spinner_premium.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position2, long id) {
                        if (!premium[0].equals("usual")) {
                            TV_nick.setTextColor(Color.parseColor(list_shop.get(position).mas_premium_time[position2]));
                            num_item[0] = position2;
                        }
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                spinnerArrayAdapter2 = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, list_meaning_usual);
                // Вызываем адаптер
                spinner_usual.setAdapter(spinnerArrayAdapter2);
                spinner_usual.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position2, long id) {
                        TV_nick.setTextColor(Color.parseColor(list_shop.get(position).mas_usual_time[position2]));
                        num_item[0] = position2;
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                switch_status.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        premium[0] = "premium";
                        spinner_usual.setVisibility(View.INVISIBLE);
                        spinner_premium.setVisibility(View.VISIBLE);
                        TV_desc.setText("Цвет за золото можно выбрать из предложенного списка");
                        TV_statusText.setTextColor(Color.parseColor("#F0BF41"));
                        IV_money.setImageResource(R.drawable.gold);
                        if (spinner_premium.getSelectedItemPosition() != 0)
                        {
                            spinner_premium.setSelection(0);
                        }
                        else {
                            spinner_premium.setSelection(1);
                        }
                    } else {
                        premium[0] = "usual";
                        spinner_usual.setVisibility(View.VISIBLE);
                        spinner_premium.setVisibility(View.INVISIBLE);
                        TV_desc.setText("Цвет за монеты можно выбрать из предложенного списка");
                        TV_statusText.setTextColor(Color.parseColor("#848484"));
                        IV_money.setImageResource(R.drawable.money);
                        if (spinner_usual.getSelectedItemPosition() != 0)
                        {
                            spinner_usual.setSelection(0);
                        }
                        else {
                            spinner_usual.setSelection(1);
                        }
                    }
                    if (spinner_time.getSelectedItemPosition() != 0)
                    {
                        spinner_time.setSelection(0);
                    }
                    else {
                        spinner_time.setSelection(1);
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
                                json.put("dop_type", "personal_colors");
                                json.put("color_type", premium[0]);
                                json.put("store_type", "general");
                                json.put("item", num_price[0]);
                                if (premium[0].equals("usual")) {
                                    json.put("color", list_shop.get(position).mas_usual_time[num_item[0]]);
                                }
                                else
                                {
                                    json.put("color", list_shop.get(position).mas_premium_time[num_item[0]]);
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
            case "buy_chance":
                view = layout.inflate(R.layout.item_buy_chance, null);

                spinner_usual = view.findViewById(R.id.itemBuyChance_Spinner_usual);
                spinner_premium = view.findViewById(R.id.itemBuyChance_Spinner_premium);
                ShimmerTextView STV_percent = view.findViewById(R.id.itemBuyChance_ShimerText_chance);
                TV_price = view.findViewById(R.id.itemBuyChance_TV_price);
                TV_statusText = view.findViewById(R.id.itemBuyChance_TV_switchText);
                Switch switch_chance = view.findViewById(R.id.itemBuyChance_Switch);
                Button btn_buy_chance = view.findViewById(R.id.itemBuyChance_btn_buy);
                TextView TV_question = view.findViewById(R.id.ItemBuyChance_btn_question);
                IV_money = view.findViewById(R.id.itemBuyChance_IV_money);

                CircleImageView CIV_citizen = view.findViewById(R.id.itemBuyChance_CIV_citizen);
                CircleImageView CIV_sheriff = view.findViewById(R.id.itemBuyChance_CIV_sheriff);
                CircleImageView CIV_doctor = view.findViewById(R.id.itemBuyChance_CIV_doctor);
                CircleImageView CIV_lover = view.findViewById(R.id.itemBuyChance_CIV_lover);
                CircleImageView CIV_journalist = view.findViewById(R.id.itemBuyChance_CIV_journalist);
                CircleImageView CIV_bodyguard = view.findViewById(R.id.itemBuyChance_CIV_bodyguard);
                CircleImageView CIV_doctor_of_easy_virtue = view.findViewById(R.id.itemBuyChance_CIV_doctor_of_easy_virtue);
                CircleImageView CIV_maniac = view.findViewById(R.id.itemBuyChance_CIV_maniac);
                CircleImageView CIV_mafia = view.findViewById(R.id.itemBuyChance_CIV_mafia);
                CircleImageView CIV_mafia_don = view.findViewById(R.id.itemBuyChance_CIV_mafia_don);
                CircleImageView CIV_terrorist = view.findViewById(R.id.itemBuyChance_CIV_terrorist);
                CircleImageView CIV_poisoner = view.findViewById(R.id.itemBuyChance_CIV_poisoner);

                Shimmer shimmer = new Shimmer();

                final String[] premium_chance = {"usual"};
                final String[] role_chance = {""};

                spinnerArrayAdapter3 = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, list_shop.get(position).mas_premium_time);
                // Вызываем адаптер
                spinner_premium.setAdapter(spinnerArrayAdapter3);

                spinner_premium.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position2, long id) {
                        TV_price.setText("Стоимость: " + list_shop.get(position).list_premium_prices.get(position2).price + " золота");
                        num_price[0] = position2;
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                spinnerArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, list_shop.get(position).mas_usual_time);
                // Вызываем адаптер
                spinner_usual.setAdapter(spinnerArrayAdapter);
                spinner_usual.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position2, long id) {
                        TV_price.setText("Стоимость: " + list_shop.get(position).list_usual_prices.get(position2).price + " монет");
                        num_price[0] = position2;
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                switch_chance.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    Log.e("kkk", "2");
                    if (isChecked) {
                        premium_chance[0] = "premium";
                        spinner_usual.setVisibility(View.INVISIBLE);
                        spinner_premium.setVisibility(View.VISIBLE);
                        TV_statusText.setTextColor(Color.parseColor("#F0BF41"));
                        STV_percent.setText("+45%");
                        shimmer.start(STV_percent);
                        if (spinner_premium.getSelectedItemPosition() != 0)
                        {
                            spinner_premium.setSelection(0);
                        }
                        else {
                            spinner_premium.setSelection(1);
                        }
                        IV_money.setImageResource(R.drawable.gold);
                    } else {
                        premium_chance[0] = "usual";
                        spinner_usual.setVisibility(View.VISIBLE);
                        spinner_premium.setVisibility(View.INVISIBLE);
                        TV_statusText.setTextColor(Color.parseColor("#848484"));
                        STV_percent.setText("+25%");
                        shimmer.cancel();
                        if (spinner_usual.getSelectedItemPosition() != 0)
                        {
                            spinner_usual.setSelection(0);
                        }
                        else {
                            spinner_usual.setSelection(1);
                        }
                        IV_money.setImageResource(R.drawable.money);
                    }
                });

                TV_question.setOnClickListener(v -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    View viewDang = layout.inflate(R.layout.dialog_information, null);
                    builder.setView(viewDang);
                    TextView TV_title1 = viewDang.findViewById(R.id.dialogInformation_TV_title);
                    TextView TV_text = viewDang.findViewById(R.id.dialogInformation_TV_text);
                    TV_title1.setText("Увеличение шанса");
                    TV_text.setText("При действии бустера увеличения шанса роли, например, маньяка, роль маньяка будет выпадать с вероятностью 25 или 45 процентов в зависимости от типа бустера (обычный или премиум). Бустер не действует в кастомных комнатах, в комнатах с паролем или если в комнате нет роли, для которой куплен бустер.Если в комнате несколько человек с активным бустером увеличения шанса роли маньяка, то преимущество будет у первого вошедшего в комнату");
                    AlertDialog alert = builder.create();
                    alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alert.show();
                });

                CIV_citizen.setOnClickListener(v -> {
                    role_chance[0] = "citizen";
                    clear(CIV_citizen, CIV_sheriff, CIV_doctor, CIV_lover, CIV_journalist, CIV_bodyguard, CIV_doctor_of_easy_virtue, CIV_maniac, CIV_mafia, CIV_mafia_don, CIV_terrorist, CIV_poisoner);
                    CIV_citizen.setBorderWidth(6);
                });
                CIV_sheriff.setOnClickListener(v -> {
                    role_chance[0] = "sheriff";
                    clear(CIV_citizen, CIV_sheriff, CIV_doctor, CIV_lover, CIV_journalist, CIV_bodyguard, CIV_doctor_of_easy_virtue, CIV_maniac, CIV_mafia, CIV_mafia_don, CIV_terrorist, CIV_poisoner);
                    CIV_sheriff.setBorderWidth(6);
                });
                CIV_doctor.setOnClickListener(v -> {
                    role_chance[0] = "doctor";
                    clear(CIV_citizen, CIV_sheriff, CIV_doctor, CIV_lover, CIV_journalist, CIV_bodyguard, CIV_doctor_of_easy_virtue, CIV_maniac, CIV_mafia, CIV_mafia_don, CIV_terrorist, CIV_poisoner);
                    CIV_doctor.setBorderWidth(6);
                });
                CIV_lover.setOnClickListener(v -> {
                    role_chance[0] = "lover";
                    clear(CIV_citizen, CIV_sheriff, CIV_doctor, CIV_lover, CIV_journalist, CIV_bodyguard, CIV_doctor_of_easy_virtue, CIV_maniac, CIV_mafia, CIV_mafia_don, CIV_terrorist, CIV_poisoner);
                    CIV_lover.setBorderWidth(6);
                });
                CIV_journalist.setOnClickListener(v -> {
                    role_chance[0] = "journalist";
                    clear(CIV_citizen, CIV_sheriff, CIV_doctor, CIV_lover, CIV_journalist, CIV_bodyguard, CIV_doctor_of_easy_virtue, CIV_maniac, CIV_mafia, CIV_mafia_don, CIV_terrorist, CIV_poisoner);
                    CIV_journalist.setBorderWidth(6);
                });
                CIV_bodyguard.setOnClickListener(v -> {
                    role_chance[0] = "bodyguard";
                    clear(CIV_citizen, CIV_sheriff, CIV_doctor, CIV_lover, CIV_journalist, CIV_bodyguard, CIV_doctor_of_easy_virtue, CIV_maniac, CIV_mafia, CIV_mafia_don, CIV_terrorist, CIV_poisoner);
                    CIV_bodyguard.setBorderWidth(6);
                });
                CIV_doctor_of_easy_virtue.setOnClickListener(v -> {
                    role_chance[0] = "doctor_of_easy_virtue";
                    clear(CIV_citizen, CIV_sheriff, CIV_doctor, CIV_lover, CIV_journalist, CIV_bodyguard, CIV_doctor_of_easy_virtue, CIV_maniac, CIV_mafia, CIV_mafia_don, CIV_terrorist, CIV_poisoner);
                    CIV_doctor_of_easy_virtue.setBorderWidth(6);
                });
                CIV_maniac.setOnClickListener(v -> {
                    role_chance[0] = "maniac";
                    clear(CIV_citizen, CIV_sheriff, CIV_doctor, CIV_lover, CIV_journalist, CIV_bodyguard, CIV_doctor_of_easy_virtue, CIV_maniac, CIV_mafia, CIV_mafia_don, CIV_terrorist, CIV_poisoner);
                    CIV_maniac.setBorderWidth(6);
                });
                CIV_mafia.setOnClickListener(v -> {
                    role_chance[0] = "mafia";
                    clear(CIV_citizen, CIV_sheriff, CIV_doctor, CIV_lover, CIV_journalist, CIV_bodyguard, CIV_doctor_of_easy_virtue, CIV_maniac, CIV_mafia, CIV_mafia_don, CIV_terrorist, CIV_poisoner);
                    CIV_mafia.setBorderWidth(6);
                });
                CIV_mafia_don.setOnClickListener(v -> {
                    role_chance[0] = "mafia_don";
                    clear(CIV_citizen, CIV_sheriff, CIV_doctor, CIV_lover, CIV_journalist, CIV_bodyguard, CIV_doctor_of_easy_virtue, CIV_maniac, CIV_mafia, CIV_mafia_don, CIV_terrorist, CIV_poisoner);
                    CIV_mafia_don.setBorderWidth(6);
                });
                CIV_terrorist.setOnClickListener(v -> {
                    role_chance[0] = "terrorist";
                    clear(CIV_citizen, CIV_sheriff, CIV_doctor, CIV_lover, CIV_journalist, CIV_bodyguard, CIV_doctor_of_easy_virtue, CIV_maniac, CIV_mafia, CIV_mafia_don, CIV_terrorist, CIV_poisoner);
                    CIV_terrorist.setBorderWidth(6);
                });
                CIV_poisoner.setOnClickListener(v -> {
                    role_chance[0] = "poisoner";
                    clear(CIV_citizen, CIV_sheriff, CIV_doctor, CIV_lover, CIV_journalist, CIV_bodyguard, CIV_doctor_of_easy_virtue, CIV_maniac, CIV_mafia, CIV_mafia_don, CIV_terrorist, CIV_poisoner);
                    CIV_poisoner.setBorderWidth(6);
                });

                btn_buy_chance.setOnClickListener(v -> {
                    if (!role_chance[0].equals("")) {
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
                                json.put("dop_type", "chance_of_role");
                                json.put("chance_type", premium_chance[0]);
                                json.put("store_type", "general");
                                json.put("chance_role", role_chance[0]);
                                json.put("item", num_price[0]);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            socket.emit("buy_item", json);
                            Log.e("kkk", "Socket_отправка - buy_item - " + json.toString());
                            alert.cancel();
                        });
                        btn_no.setOnClickListener(v12 -> {
                            alert.cancel();
                        });
                        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        alert.show();
                    }
                    else
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        View viewDang = layout.inflate(R.layout.dialog_error, null);
                        builder.setView(viewDang);
                        TextView TV_errorName = viewDang.findViewById(R.id.dialogError_TV_errorTitle);
                        TextView TV_errorText = viewDang.findViewById(R.id.dialogError_TV_errorText);
                        TV_errorName.setText("Выберите роль!");
                        TV_errorText.setText("Вам надо выбрать роль, для которой вы хотите купить увеличение шанса получения");
                        AlertDialog alert = builder.create();
                        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        alert.show();
                    }
                });

                break;
        }
        return view;
    }

    private void clear(CircleImageView CIV1, CircleImageView CIV2, CircleImageView CIV3, CircleImageView CIV4, CircleImageView CIV5, CircleImageView CIV6,
                       CircleImageView CIV7, CircleImageView CIV8, CircleImageView CIV9, CircleImageView CIV10, CircleImageView CIV11, CircleImageView CIV12)
    {
        CIV1.setBorderWidth(0);
        CIV2.setBorderWidth(0);
        CIV3.setBorderWidth(0);
        CIV4.setBorderWidth(0);
        CIV5.setBorderWidth(0);
        CIV6.setBorderWidth(0);
        CIV7.setBorderWidth(0);
        CIV8.setBorderWidth(0);
        CIV9.setBorderWidth(0);
        CIV10.setBorderWidth(0);
        CIV11.setBorderWidth(0);
        CIV12.setBorderWidth(0);
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
