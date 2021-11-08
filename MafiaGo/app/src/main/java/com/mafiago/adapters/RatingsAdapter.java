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
import com.mafiago.models.PrivateChatModel;
import com.mafiago.models.RatingModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.mafiago.MainActivity.socket;

public class RatingsAdapter extends BaseAdapter {
    public ArrayList<RatingModel> list_ratings;
    public Context context;
    public LayoutInflater layout;

    public RatingsAdapter(ArrayList<RatingModel> list_ratings, Context context)
    {
        this.list_ratings = list_ratings;
        this.context = context;
        layout=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = layout.inflate(R.layout.item_rating_user, null);

        TextView TV_nick = view.findViewById(R.id.itemRatingUser_TV_nick);
        TextView TV_score = view.findViewById(R.id.itemRatingUser_TV_score);
        TextView TV_place = view.findViewById(R.id.itemRatingUser_TV_place);
        ImageView IV_avatar = view.findViewById(R.id.itemRatingUser_IV_avatar);
        ImageView IV_online = view.findViewById(R.id.itemRatingUser_IV_online);

        TV_score.setText(list_ratings.get(position).score);
        TV_place.setText(String.valueOf(list_ratings.get(position).place));

        if (list_ratings.get(position).avatar != null) {
            IV_avatar.setImageBitmap(list_ratings.get(position).avatar);
        }

        IV_avatar.setOnClickListener(v -> {
            final JSONObject json = new JSONObject();
            try {
                json.put("nick", MainActivity.NickName);
                json.put("session_id", MainActivity.Session_id);
                json.put("info_user_id", list_ratings.get(position).user_id);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            socket.emit("get_profile", json);
            Log.d("kkk", "Socket_отправка - get_profile - "+ json.toString());
        });

        if (list_ratings.get(position).status.equals(""))
        {
            TV_nick.setText(list_ratings.get(position).nick);
        }
        else
        {
            TV_nick.setText(list_ratings.get(position).nick + " {" + list_ratings.get(position).status + "}");
        }

        if (!list_ratings.get(position).color.equals(""))
        {
            TV_nick.setTextColor(Color.parseColor(list_ratings.get(position).color));
        }


        if (list_ratings.get(position).online) {
            IV_online.setVisibility(View.VISIBLE);
        }
        return view;
    }

    @Override
    public int getCount() {
        return list_ratings.size();
    }

    @Override
    public Object getItem(int position) {
        return list_ratings.get(position);
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
