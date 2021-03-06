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
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mafiago.R;
import com.mafiago.MainActivity;
import com.mafiago.models.PremiumModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.mafiago.MainActivity.socket;

public class PremiumAdapter extends BaseAdapter {
    public ArrayList<PremiumModel> list_gold;
    public Context context;
    public LayoutInflater layout;

    // Конструктор
    public PremiumAdapter(ArrayList<PremiumModel> list_gold, Context context) {

        this.list_gold = list_gold;
        this.context = context;
        if (context != null) {
            layout=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;

        view = layout.inflate(R.layout.item_shop_premium, null);
        Button btnBuy = view.findViewById(R.id.itemShopPremium_btn_buy);
        TextView TV_title = view.findViewById(R.id.itemShopPremium_TV_premiumDescription);
        TextView TV_time = view.findViewById(R.id.itemShopPremium_TV_hours);
        TextView TV_cost = view.findViewById(R.id.itemShopPremium_TV_cost);
        ImageView IV_premium = view.findViewById(R.id.itemShopPremium_IV_premium);

        TV_time.setText(list_gold.get(position).amount);
        TV_cost.setText(list_gold.get(position).description);

        switch (list_gold.get(position).num)
        {
            case 0:
                TV_title.setText("Премиум-мини");
                IV_premium.setImageResource(R.drawable.crown_blue_circle);
                break;
            case 1:
                TV_title.setText("Премиум стандарт");
                IV_premium.setImageResource(R.drawable.crown_green_circle);
                break;
            case 2:
                TV_title.setText("Премиум");
                IV_premium.setImageResource(R.drawable.crown_grey_circle);
                break;
            case 3:
                TV_title.setText("Премиум+");
                IV_premium.setImageResource(R.drawable.crown_cian_circle);
                break;
            case 4:
                TV_title.setText("Премиум-супер");
                IV_premium.setImageResource(R.drawable.crown_gold_circle);
                break;
        }

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
                    json.put("store_type", "premium");
                    json.put("item", list_gold.get(position).num);
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
        return view;
    }

    @Override
    public int getCount() {
        return list_gold.size();
    }

    @Override
    public Object getItem(int position) {
        return list_gold.get(position);
    }


    @Override
    public long getItemId(int position) { return position; }
}
