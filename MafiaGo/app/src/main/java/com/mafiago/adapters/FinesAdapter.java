package com.mafiago.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Base64;
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
import com.mafiago.models.FineModel;
import com.mafiago.models.FriendModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.mafiago.MainActivity.socket;

public class FinesAdapter extends BaseAdapter {


    public ArrayList<FineModel> list_fines;
    public Context context;
    public LayoutInflater layout;

    public FinesAdapter(ArrayList<FineModel> list_fines, Context context)
    {
        this.list_fines = list_fines;
        this.context = context;
        layout=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = layout.inflate(R.layout.item_fine, null);

        ImageView IV_main = view.findViewById(R.id.itemFine_IV_main);
        ImageView IV_time = view.findViewById(R.id.itemFine_IV_time);
        ImageView IV_money = view.findViewById(R.id.itemFine_IV_money);
        ImageView IV_exp = view.findViewById(R.id.itemFine_IV_exp);
        TextView TV_title = view.findViewById(R.id.itemFine_IV_time);
        TextView TV_fineTime = view.findViewById(R.id.itemFine_TV_fineTime);
        TextView TV_reason = view.findViewById(R.id.itemFine_TV_reason);
        TextView TV_adminComment = view.findViewById(R.id.itemFine_TV_adminComment);
        TextView TV_time = view.findViewById(R.id.itemFine_TV_time);
        TextView TV_exp = view.findViewById(R.id.itemFine_TV_exp);
        TextView TV_money = view.findViewById(R.id.itemFine_TV_money);

        return view;
    }

    @Override
    public int getCount() {
        return list_fines.size();
    }

    @Override
    public Object getItem(int position) {
        return list_fines.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public Bitmap fromBase64(String image) {
        // Декодируем строку Base64 в массив байтов
        byte[] decodedString = Base64.decode(image, Base64.DEFAULT);

        // Декодируем массив байтов в изображение
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        // Помещаем изображение в ImageView
        return decodedByte;
    }
}
