package com.mafiago.small_fragments;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.service.autofill.TextValueSanitizer;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.mafiago.R;
import com.mafiago.MainActivity;
import com.mafiago.adapters.FriendRequestsAdapter;
import com.mafiago.adapters.FriendsAdapter;
import com.mafiago.adapters.RatingsAdapter;
import com.mafiago.enums.Role;
import com.mafiago.models.RatingModel;
import com.mafiago.models.ShopModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.socket.emitter.Emitter;

import static com.mafiago.MainActivity.socket;

public class SmallRatingsFragment extends Fragment {

    public static final String ARG_PAGE = "ARG_PAGE";
    private int mPage;
    RatingsAdapter ratingsAdapter;
    boolean pushed_1 = true;
    String main_name = "";

    ArrayList<RatingModel> list_ratings = new ArrayList<>();

    JSONObject json;

    Button btn_1;
    Button btn_2;
    ListView LV_rating;
    ProgressBar PB_loading;
    Spinner spinner;
    TextView TV_noRatings;
    ConstraintLayout CL_places;
    TextView TV_questionRatings;
    TextView TV_1;
    TextView TV_2;
    TextView TV_3;
    TextView TV_4_10;
    TextView TV_11_50;
    TextView TV_51_100;
    ImageView IV_1;
    ImageView IV_2;
    ImageView IV_3;
    ImageView IV_4_10;
    ImageView IV_11_50;
    ImageView IV_51_100;

    public static SmallRatingsFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        SmallRatingsFragment fragment = new SmallRatingsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPage = getArguments().getInt(ARG_PAGE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.small_fragment_ratings, container, false);
        switch (mPage)
        {
            case 1:
                view = inflater.inflate(R.layout.small_fragment_ratings, container, false);
                btn_1 = view.findViewById(R.id.smallFragmentRatings_btn_1);
                btn_2 = view.findViewById(R.id.smallFragmentRatings_btn_2);
                LV_rating = view.findViewById(R.id.smallFragmentRatings_LV);
                PB_loading = view.findViewById(R.id.smallFragmentRatings_PB);
                spinner = view.findViewById(R.id.smallFragmentRatings_Spinner);
                TV_noRatings = view.findViewById(R.id.smallFragmentRatings_TV_noRatings);
                TV_1 = view.findViewById(R.id.smallFragmentRatings_TV_1);
                TV_2 = view.findViewById(R.id.smallFragmentRatings_TV_2);
                TV_3 = view.findViewById(R.id.smallFragmentRatings_TV_3);
                TV_4_10 = view.findViewById(R.id.smallFragmentRatings_TV_4_10);
                TV_11_50 = view.findViewById(R.id.smallFragmentRatings_TV_11_50);
                TV_51_100 = view.findViewById(R.id.smallFragmentRatings_TV_51_100);
                IV_1 = view.findViewById(R.id.smallFragmentRatings_IV_1);
                IV_2 = view.findViewById(R.id.smallFragmentRatings_IV_2);
                IV_3 = view.findViewById(R.id.smallFragmentRatings_IV_3);
                IV_4_10 = view.findViewById(R.id.smallFragmentRatings_IV_4_10);
                IV_11_50 = view.findViewById(R.id.smallFragmentRatings_IV_11_50);
                IV_51_100 = view.findViewById(R.id.smallFragmentRatings_IV_51_100);
                CL_places = view.findViewById(R.id.smallFragmentRatings_CL_places);
                TV_questionRatings = view.findViewById(R.id.smallFragmentRatings_TV_questionRatings);

                /*
                TV_questionRatings.setOnClickListener(v -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    View viewDang = getLayoutInflater().inflate(R.layout.dialog_information, null);
                    builder.setView(viewDang);
                    TextView TV_title = viewDang.findViewById(R.id.dialogInformation_TV_title);
                    TextView TV_text = viewDang.findViewById(R.id.dialogInformation_TV_text);
                    TV_title.setText("Награды за места в рейтинге!");
                    TV_text.setText("текст");
                    AlertDialog alert = builder.create();
                    alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alert.show();
                });
                 */

                ratingsAdapter = new RatingsAdapter(list_ratings, getContext());
                LV_rating.setAdapter(ratingsAdapter);

                PB_loading.setVisibility(View.VISIBLE);

                socket.on("get_rating", onGetRating);
                main_name = "game_counter";

                btn_1.setOnClickListener(v -> {
                    if (!pushed_1)
                    {
                        pushed_1 = true;
                        btn_1.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.orange_button));
                        btn_2.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.grey_button));
                        if (spinner.getSelectedItemPosition() != 0)
                        {
                            spinner.setSelection(0);
                        }
                        else
                        {
                            spinner.setSelection(1);
                        }
                    }
                });

                btn_2.setOnClickListener(v -> {
                    if (pushed_1)
                    {
                        pushed_1 = false;
                        btn_1.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.grey_button));
                        btn_2.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.orange_button));
                        if (spinner.getSelectedItemPosition() != 0)
                        {
                            spinner.setSelection(0);
                        }
                        else
                        {
                            spinner.setSelection(1);
                        }
                    }
                });

                String[] mas_time = new String[]{"за день", "за неделю", "за месяц", "за всё время"};

                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, mas_time);
                spinner.setAdapter(spinnerArrayAdapter);

                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position2, long id) {
                        TV_noRatings.setVisibility(View.INVISIBLE);
                        PB_loading.setVisibility(View.VISIBLE);
                        switch (position2)
                        {
                            case 0:
                                if (pushed_1) {
                                    CallSocket("game_counter_by_day");
                                    list_ratings.clear();
                                    ratingsAdapter.notifyDataSetChanged();
                                }
                                else
                                {
                                    CallSocket("custom_room_game_counter_by_day");
                                    list_ratings.clear();
                                    ratingsAdapter.notifyDataSetChanged();
                                }
                                TV_1.setText("1  -  500");
                                TV_2.setText("2  -  250");
                                TV_3.setText("3  -  100");
                                TV_4_10.setText("4-10  -  500");
                                TV_11_50.setText("11-50  -  250");
                                TV_51_100.setText("51-100  -  100");
                                IV_1.setImageResource(R.drawable.experience);
                                IV_2.setImageResource(R.drawable.experience);
                                IV_3.setImageResource(R.drawable.experience);
                                IV_4_10.setImageResource(R.drawable.money);
                                IV_11_50.setImageResource(R.drawable.money);
                                IV_51_100.setImageResource(R.drawable.money);
                                CL_places.setVisibility(View.VISIBLE);
                                break;
                            case 1:
                                if (pushed_1) {
                                    CallSocket("game_counter_by_week");
                                    list_ratings.clear();
                                    ratingsAdapter.notifyDataSetChanged();
                                }
                                else {
                                    CallSocket("custom_room_game_counter_by_week");
                                    list_ratings.clear();
                                    ratingsAdapter.notifyDataSetChanged();
                                }
                                TV_1.setText("1  -  500");
                                TV_2.setText("2  -  250");
                                TV_3.setText("3  -  100");
                                TV_4_10.setText("4-10  -  500");
                                TV_11_50.setText("11-50  -  250");
                                TV_51_100.setText("51-100  -  100");
                                IV_1.setImageResource(R.drawable.gold);
                                IV_2.setImageResource(R.drawable.gold);
                                IV_3.setImageResource(R.drawable.gold);
                                IV_4_10.setImageResource(R.drawable.experience);
                                IV_11_50.setImageResource(R.drawable.experience);
                                IV_51_100.setImageResource(R.drawable.experience);
                                CL_places.setVisibility(View.VISIBLE);
                                break;
                            case 2:
                                if (pushed_1) {
                                    CallSocket("game_counter_by_month");
                                    list_ratings.clear();
                                    ratingsAdapter.notifyDataSetChanged();
                                }
                                else {
                                    CallSocket("custom_room_game_counter_by_month");
                                    list_ratings.clear();
                                    ratingsAdapter.notifyDataSetChanged();
                                }
                                TV_1.setText("1  -  500");
                                TV_2.setText("2  -  300");
                                TV_3.setText("3  -  200");
                                TV_4_10.setText("4-10  -  100");
                                TV_11_50.setText("11-50  -  1000");
                                TV_51_100.setText("51-100  -  3000");
                                IV_1.setImageResource(R.drawable.gold);
                                IV_2.setImageResource(R.drawable.gold);
                                IV_3.setImageResource(R.drawable.gold);
                                IV_4_10.setImageResource(R.drawable.gold);
                                IV_11_50.setImageResource(R.drawable.experience);
                                IV_51_100.setImageResource(R.drawable.money);
                                CL_places.setVisibility(View.VISIBLE);
                                break;
                            case 3:
                                if (pushed_1) {
                                    CallSocket("game_counter");
                                    list_ratings.clear();
                                    ratingsAdapter.notifyDataSetChanged();
                                }
                                else {
                                    CallSocket("custom_room_game_counter");
                                    list_ratings.clear();
                                    ratingsAdapter.notifyDataSetChanged();
                                }
                                CL_places.setVisibility(View.GONE);
                                break;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                break;
            case 2:
                view = inflater.inflate(R.layout.small_fragment_ratings, container, false);
                btn_1 = view.findViewById(R.id.smallFragmentRatings_btn_1);
                btn_2 = view.findViewById(R.id.smallFragmentRatings_btn_2);
                LV_rating = view.findViewById(R.id.smallFragmentRatings_LV);
                PB_loading = view.findViewById(R.id.smallFragmentRatings_PB);
                spinner = view.findViewById(R.id.smallFragmentRatings_Spinner);
                TV_noRatings = view.findViewById(R.id.smallFragmentRatings_TV_noRatings);
                TV_1 = view.findViewById(R.id.smallFragmentRatings_TV_1);
                TV_2 = view.findViewById(R.id.smallFragmentRatings_TV_2);
                TV_3 = view.findViewById(R.id.smallFragmentRatings_TV_3);
                TV_4_10 = view.findViewById(R.id.smallFragmentRatings_TV_4_10);
                TV_11_50 = view.findViewById(R.id.smallFragmentRatings_TV_11_50);
                TV_51_100 = view.findViewById(R.id.smallFragmentRatings_TV_51_100);
                IV_1 = view.findViewById(R.id.smallFragmentRatings_IV_1);
                IV_2 = view.findViewById(R.id.smallFragmentRatings_IV_2);
                IV_3 = view.findViewById(R.id.smallFragmentRatings_IV_3);
                IV_4_10 = view.findViewById(R.id.smallFragmentRatings_IV_4_10);
                IV_11_50 = view.findViewById(R.id.smallFragmentRatings_IV_11_50);
                IV_51_100 = view.findViewById(R.id.smallFragmentRatings_IV_51_100);
                CL_places = view.findViewById(R.id.smallFragmentRatings_CL_places);

                btn_1.setVisibility(View.GONE);
                btn_2.setVisibility(View.GONE);
                PB_loading.setVisibility(View.VISIBLE);

                ratingsAdapter = new RatingsAdapter(list_ratings, getContext());
                LV_rating.setAdapter(ratingsAdapter);

                socket.on("get_rating", onGetRating);
                main_name = "wins";

                mas_time = new String[]{"за день", "за неделю", "за месяц", "за всё время"};

                spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, mas_time);
                spinner.setAdapter(spinnerArrayAdapter);

                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position2, long id) {
                        TV_noRatings.setVisibility(View.INVISIBLE);
                        PB_loading.setVisibility(View.VISIBLE);
                        switch (position2)
                        {
                            case 0:
                                CallSocket("general_wins_by_day");
                                list_ratings.clear();
                                ratingsAdapter.notifyDataSetChanged();
                                TV_1.setText("1  -  500");
                                TV_2.setText("2  -  250");
                                TV_3.setText("3  -  100");
                                TV_4_10.setText("4-10  -  500");
                                TV_11_50.setText("11-50  -  250");
                                TV_51_100.setText("51-100  -  100");
                                IV_1.setImageResource(R.drawable.experience);
                                IV_2.setImageResource(R.drawable.experience);
                                IV_3.setImageResource(R.drawable.experience);
                                IV_4_10.setImageResource(R.drawable.money);
                                IV_11_50.setImageResource(R.drawable.money);
                                IV_51_100.setImageResource(R.drawable.money);
                                CL_places.setVisibility(View.VISIBLE);
                                break;
                            case 1:
                                CallSocket("general_wins_by_week");
                                list_ratings.clear();
                                ratingsAdapter.notifyDataSetChanged();
                                TV_1.setText("1  -  500");
                                TV_2.setText("2  -  250");
                                TV_3.setText("3  -  100");
                                TV_4_10.setText("4-10  -  500");
                                TV_11_50.setText("11-50  -  250");
                                TV_51_100.setText("51-100  -  100");
                                IV_1.setImageResource(R.drawable.gold);
                                IV_2.setImageResource(R.drawable.gold);
                                IV_3.setImageResource(R.drawable.gold);
                                IV_4_10.setImageResource(R.drawable.experience);
                                IV_11_50.setImageResource(R.drawable.experience);
                                IV_51_100.setImageResource(R.drawable.experience);
                                CL_places.setVisibility(View.VISIBLE);
                                break;
                            case 2:
                                CallSocket("general_wins_by_month");
                                list_ratings.clear();
                                ratingsAdapter.notifyDataSetChanged();
                                TV_1.setText("1  -  500");
                                TV_2.setText("2  -  300");
                                TV_3.setText("3  -  200");
                                TV_4_10.setText("4-10  -  100");
                                TV_11_50.setText("11-50  -  1000");
                                TV_51_100.setText("51-100  -  3000");
                                IV_1.setImageResource(R.drawable.gold);
                                IV_2.setImageResource(R.drawable.gold);
                                IV_3.setImageResource(R.drawable.gold);
                                IV_4_10.setImageResource(R.drawable.gold);
                                IV_11_50.setImageResource(R.drawable.experience);
                                IV_51_100.setImageResource(R.drawable.money);
                                CL_places.setVisibility(View.VISIBLE);
                                break;
                            case 3:
                                CallSocket("general_wins");
                                list_ratings.clear();
                                ratingsAdapter.notifyDataSetChanged();
                                CL_places.setVisibility(View.GONE);
                                break;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                break;
            case 3:
                view = inflater.inflate(R.layout.small_fragment_ratings, container, false);
                btn_1 = view.findViewById(R.id.smallFragmentRatings_btn_1);
                btn_2 = view.findViewById(R.id.smallFragmentRatings_btn_2);
                LV_rating = view.findViewById(R.id.smallFragmentRatings_LV);
                PB_loading = view.findViewById(R.id.smallFragmentRatings_PB);
                spinner = view.findViewById(R.id.smallFragmentRatings_Spinner);
                TV_noRatings = view.findViewById(R.id.smallFragmentRatings_TV_noRatings);
                TV_1 = view.findViewById(R.id.smallFragmentRatings_TV_1);
                TV_2 = view.findViewById(R.id.smallFragmentRatings_TV_2);
                TV_3 = view.findViewById(R.id.smallFragmentRatings_TV_3);
                TV_4_10 = view.findViewById(R.id.smallFragmentRatings_TV_4_10);
                TV_11_50 = view.findViewById(R.id.smallFragmentRatings_TV_11_50);
                TV_51_100 = view.findViewById(R.id.smallFragmentRatings_TV_51_100);
                IV_1 = view.findViewById(R.id.smallFragmentRatings_IV_1);
                IV_2 = view.findViewById(R.id.smallFragmentRatings_IV_2);
                IV_3 = view.findViewById(R.id.smallFragmentRatings_IV_3);
                IV_4_10 = view.findViewById(R.id.smallFragmentRatings_IV_4_10);
                IV_11_50 = view.findViewById(R.id.smallFragmentRatings_IV_11_50);
                IV_51_100 = view.findViewById(R.id.smallFragmentRatings_IV_51_100);
                CL_places = view.findViewById(R.id.smallFragmentRatings_CL_places);

                btn_1.setText("Заработанный");
                btn_2.setText("Общий");
                PB_loading.setVisibility(View.VISIBLE);

                ratingsAdapter = new RatingsAdapter(list_ratings, getContext());
                LV_rating.setAdapter(ratingsAdapter);

                socket.on("get_rating", onGetRating);
                main_name = "exp";

                btn_1.setOnClickListener(v -> {
                    if (!pushed_1)
                    {
                        pushed_1 = true;
                        btn_1.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.orange_button));
                        btn_2.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.grey_button));
                        spinner.setVisibility(View.VISIBLE);
                        if (spinner.getSelectedItemPosition() != 0)
                        {
                            spinner.setSelection(0);
                        }
                        else
                        {
                            spinner.setSelection(1);
                        }
                    }
                });

                btn_2.setOnClickListener(v -> {
                    if (pushed_1)
                    {
                        pushed_1 = false;
                        btn_1.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.grey_button));
                        btn_2.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.orange_button));
                        spinner.setVisibility(View.GONE);
                        CL_places.setVisibility(View.GONE);
                        TV_noRatings.setVisibility(View.INVISIBLE);
                        PB_loading.setVisibility(View.VISIBLE);
                        CallSocket("total_exp");
                        list_ratings.clear();
                                    ratingsAdapter.notifyDataSetChanged();
                    }
                });

                mas_time = new String[]{"за день", "за неделю", "за месяц", "за всё время"};

                spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, mas_time);
                spinner.setAdapter(spinnerArrayAdapter);

                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position2, long id) {
                        TV_noRatings.setVisibility(View.INVISIBLE);
                        PB_loading.setVisibility(View.VISIBLE);
                        switch (position2)
                        {
                            case 0:
                                if (pushed_1) {
                                    CallSocket("exp_by_day");
                                    list_ratings.clear();
                                    ratingsAdapter.notifyDataSetChanged();
                                    TV_1.setText("1  -  500");
                                    TV_2.setText("2  -  250");
                                    TV_3.setText("3  -  100");
                                    TV_4_10.setText("4-10  -  500");
                                    TV_11_50.setText("11-50  -  250");
                                    TV_51_100.setText("51-100  -  100");
                                    IV_1.setImageResource(R.drawable.experience);
                                    IV_2.setImageResource(R.drawable.experience);
                                    IV_3.setImageResource(R.drawable.experience);
                                    IV_4_10.setImageResource(R.drawable.money);
                                    IV_11_50.setImageResource(R.drawable.money);
                                    IV_51_100.setImageResource(R.drawable.money);
                                    CL_places.setVisibility(View.VISIBLE);
                                }
                                break;
                            case 1:
                                if (pushed_1) {
                                    CallSocket("exp_by_week");
                                    list_ratings.clear();
                                    ratingsAdapter.notifyDataSetChanged();
                                    TV_1.setText("1  -  500");
                                    TV_2.setText("2  -  250");
                                    TV_3.setText("3  -  100");
                                    TV_4_10.setText("4-10  -  500");
                                    TV_11_50.setText("11-50  -  250");
                                    TV_51_100.setText("51-100  -  100");
                                    IV_1.setImageResource(R.drawable.gold);
                                    IV_2.setImageResource(R.drawable.gold);
                                    IV_3.setImageResource(R.drawable.gold);
                                    IV_4_10.setImageResource(R.drawable.experience);
                                    IV_11_50.setImageResource(R.drawable.experience);
                                    IV_51_100.setImageResource(R.drawable.experience);
                                    CL_places.setVisibility(View.VISIBLE);
                                }
                                break;
                            case 2:
                                if (pushed_1) {
                                    CallSocket("exp_by_month");
                                    list_ratings.clear();
                                    ratingsAdapter.notifyDataSetChanged();
                                    TV_1.setText("1  -  500");
                                    TV_2.setText("2  -  300");
                                    TV_3.setText("3  -  200");
                                    TV_4_10.setText("4-10  -  100");
                                    TV_11_50.setText("11-50  -  1000");
                                    TV_51_100.setText("51-100  -  3000");
                                    IV_1.setImageResource(R.drawable.gold);
                                    IV_2.setImageResource(R.drawable.gold);
                                    IV_3.setImageResource(R.drawable.gold);
                                    IV_4_10.setImageResource(R.drawable.gold);
                                    IV_11_50.setImageResource(R.drawable.experience);
                                    IV_51_100.setImageResource(R.drawable.money);
                                    CL_places.setVisibility(View.VISIBLE);
                                }
                                break;
                            case 3:
                                if (pushed_1) {
                                    CallSocket("real_exp");
                                    list_ratings.clear();
                                    ratingsAdapter.notifyDataSetChanged();
                                    CL_places.setVisibility(View.GONE);
                                }
                                break;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                break;
        }


        return view;
    }

    private final Emitter.Listener onGetRating = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    JSONArray JA_users;
                    JSONObject JO_user;
                    String name = "";
                    String nick, status, color, user_id, avatar, score;
                    boolean online;
                    Log.d("kkk", "принял - get_rating1 - " + data);

                    try {
                        name = data.getString("name");
                        if (name.contains(main_name))
                        {
                            JA_users = data.getJSONArray("data");
                            for (int i = 0; i < JA_users.length(); i++) {
                                JO_user = JA_users.getJSONObject(i);
                                nick = JO_user.getString("nick");
                                status = JO_user.getString("status");
                                color = JO_user.getString("color");
                                user_id = JO_user.getString("user_id");
                                avatar = JO_user.getString("avatar");
                                score = JO_user.getString("score");
                                JO_user.remove("avatar");
                                Log.e("kkk", String.valueOf(JO_user));
                                list_ratings.add(new RatingModel(nick, false, fromBase64(avatar), status, color, user_id, list_ratings.size() + 1, score));
                            }
                            PB_loading.setVisibility(View.INVISIBLE);
                            ratingsAdapter.notifyDataSetChanged();
                            if (list_ratings.size() == 0)
                            {
                                TV_noRatings.setVisibility(View.VISIBLE);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    public Bitmap fromBase64(String image) {
        // Декодируем строку Base64 в массив байтов
        byte[] decodedString = Base64.decode(image, Base64.DEFAULT);

        // Декодируем массив байтов в изображение
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        // Помещаем изображение в ImageView
        return decodedByte;
    }

    public void CallSocket(String name)
    {
        json = new JSONObject();
        try {
            json.put("nick", MainActivity.NickName);
            json.put("session_id", MainActivity.Session_id);
            json.put("name", name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("get_rating", json);
        Log.d("kkk", "Socket_отправка - get_rating - "+ json.toString());
    }
}