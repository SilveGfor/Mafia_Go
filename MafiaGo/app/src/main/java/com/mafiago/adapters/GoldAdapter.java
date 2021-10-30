package com.mafiago.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.BillingFlowParams;
import com.example.mafiago.R;
import com.mafiago.models.GoldModel;
import com.mafiago.models.PremiumModel;

import java.util.ArrayList;

public class GoldAdapter extends BaseAdapter {
    public ArrayList<GoldModel> list_gold;
    public Context context;
    public LayoutInflater layout;

    // Конструктор
    public GoldAdapter(ArrayList<GoldModel> list_gold, Context context) {

        this.list_gold = list_gold;
        this.context = context;
        if (context != null) {
            layout=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;

        view = layout.inflate(R.layout.item_gold, null);
        Button btnBuy = view.findViewById(R.id.itemGold_btn_buy);
        TextView TV_goldCount = view.findViewById(R.id.itemGold_TV_goldCount);
        TextView TV_cost = view.findViewById(R.id.itemGold_TV_cost);
        ImageView IV_gold = view.findViewById(R.id.itemGold_IV_gold);

        TV_goldCount.setText(list_gold.get(position).skuDetails.getTitle().replace("(Mafia Go)", ""));
        TV_cost.setText("Вы можете купить " + list_gold.get(position).skuDetails.getTitle().replace("(Mafia Go)", "") + " за " + list_gold.get(position).skuDetails.getPrice());

        switch (list_gold.get(position).num)
        {
            case 0:
                IV_gold.setImageResource(R.drawable.gold_1);
                break;
            case 1:
                IV_gold.setImageResource(R.drawable.gold_2);
                break;
            case 2:
                IV_gold.setImageResource(R.drawable.gold_3);
                break;
            case 3:
                IV_gold.setImageResource(R.drawable.gold_4);
                break;
            case 4:
            default:
                IV_gold.setImageResource(R.drawable.gold_5);
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
                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(list_gold.get(position).skuDetails)
                        .build();
                list_gold.get(position).billingClient.launchBillingFlow(activity, billingFlowParams);
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
