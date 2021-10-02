package com.mafiago.small_fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.mafiago.R;
import com.mafiago.MainActivity;
import com.mafiago.adapters.FriendRequestsAdapter;
import com.mafiago.adapters.FriendsAdapter;
import com.mafiago.adapters.GoldAdapter;
import com.mafiago.adapters.PremiumAdapter;
import com.mafiago.adapters.ShopAdapter;
import com.mafiago.fragments.GamesListFragment;
import com.mafiago.models.FriendModel;
import com.mafiago.models.GoldModel;
import com.mafiago.models.ShopModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.emitter.Emitter;

import static com.mafiago.MainActivity.socket;

public class SmallShopFragment extends Fragment {

    public static final String ARG_PAGE = "ARG_PAGE";

    private int mPage;

    JSONObject json;

    ListView LV_gold;
    ArrayList<GoldModel> list_gold = new ArrayList();
    GoldAdapter goldAdapter;

    /////////////////

    ListView LV_shop;
    ArrayList<ShopModel> list_shop = new ArrayList();
    ShopAdapter shopAdapter;
    Button btn_busters;

    /////////////////

    ListView LV_premium;
    ArrayList<GoldModel> list_premium = new ArrayList();
    PremiumAdapter premiumAdapter;

    public static SmallShopFragment newInstance(int page) {
        SmallShopFragment fragment = new SmallShopFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPage = getArguments().getInt(ARG_PAGE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = null;

        json = new JSONObject();
        try {
            json.put("nick", MainActivity.NickName);
            json.put("session_id", MainActivity.Session_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("get_store", json);
        Log.d("kkk", "Socket_отправка - get_store - "+ json.toString());

        switch (mPage)
        {

            case 1:
                view = inflater.inflate(R.layout.small_fragment_shop, container, false);

                LV_gold = view.findViewById(R.id.smallFragmentShop_LV_shop);
                goldAdapter = new GoldAdapter(list_gold, getContext());
                LV_gold.setAdapter(goldAdapter);

                socket.on("get_store", OnGetGoldStore);
                socket.on("buy_item", OnBuyItem);

                goldAdapter.notifyDataSetChanged();
                break;
            case 2:
                view = inflater.inflate(R.layout.small_fragment_shop, container, false);

                btn_busters = view.findViewById(R.id.smallFragmentShop_btn_busters);
                LV_shop = view.findViewById(R.id.smallFragmentShop_LV_shop);
                shopAdapter = new ShopAdapter(list_shop, getContext());
                LV_shop.setAdapter(shopAdapter);

                //btn_busters.setVisibility(View.VISIBLE);

                socket.on("get_store", OnGetMainStore);
                socket.on("buy_item", OnBuyItem);

                shopAdapter.notifyDataSetChanged();
                break;
            case 3:
                view = inflater.inflate(R.layout.small_fragment_shop, container, false);

                btn_busters = view.findViewById(R.id.smallFragmentShop_btn_busters);
                LV_premium = view.findViewById(R.id.smallFragmentShop_LV_shop);
                premiumAdapter = new PremiumAdapter(list_premium, getContext());
                LV_premium.setAdapter(premiumAdapter);

                //socket.on("get_store", OnGetPremiumStore);
                //socket.on("buy_item", OnBuyPremiumItem);
                //socket.on("user_error", onUserError);

                premiumAdapter.notifyDataSetChanged();
                break;
        }
        return view;
    }

    private final Emitter.Listener OnGetGoldStore = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (args.length != 0 && list_gold.size() == 0) {
                    JSONObject data = (JSONObject) args[0];
                    Log.d("kkk", "принял - get_store - " + data);
                    JSONArray gold_array;
                    JSONObject gold_data;
                    String description = "", transaction_description = "", sale_amount = "", amount = "";
                    int price = 0;
                    boolean is_sale = false;
                    try {
                        gold_array = data.getJSONArray("gold");
                        for (int i = 0; i < gold_array.length(); i++)
                        {
                            gold_data = gold_array.getJSONObject(i);
                            description = gold_data.getString("description");
                            transaction_description = gold_data.getString("transaction_description");
                            amount = gold_data.getString("amount");
                            price = gold_data.getInt("price");
                            is_sale = gold_data.getBoolean("is_sale");
                            sale_amount = gold_data.getString("sale_amount");
                            list_gold.add(new GoldModel(description, amount, price, is_sale, transaction_description, sale_amount, list_gold.size(), "gold"));
                        }

                        goldAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    };

    private final Emitter.Listener OnGetPremiumStore = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (args.length != 0 && list_premium.size() == 0) {
                    JSONObject data = (JSONObject) args[0];
                    Log.d("kkk", "принял - get_store - " + data);
                    JSONArray gold_array;
                    JSONObject gold_data;
                    String description = "", transaction_description = "", sale_amount = "", amount = "";
                    int price = 0, hours = 0;
                    boolean is_sale = false;
                    try {
                        gold_array = data.getJSONArray("premium");
                        for (int i = 0; i < gold_array.length(); i++)
                        {
                            gold_data = gold_array.getJSONObject(i);
                            description = gold_data.getString("description");
                            amount = gold_data.getString("amount");
                            price = gold_data.getInt("price");
                            hours = gold_data.getInt("hours");
                            is_sale = gold_data.getBoolean("is_sale");
                            sale_amount = gold_data.getString("sale_amount");
                            list_premium.add(new GoldModel(description, amount, price, is_sale, transaction_description, sale_amount, list_premium.size(), "premium"));
                        }
                        premiumAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    };

    private final Emitter.Listener OnGetMainStore = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (args.length != 0 && list_shop.size() == 0) {
                    JSONObject data = (JSONObject) args[0];
                    Log.d("kkk", "принял - get_store - " + data);
                    JSONObject JO_price;

                    JSONObject JO_general_data;
                    JSONObject JO_statuses_common_data;
                    JSONObject JO_statuses_usual_data;
                    JSONArray JA_usual_statuses;
                    JSONArray JA_usual_statuses_prices;
                    JSONArray JA_premium_statuses_prices;
                    String[] list_statuces;
                    ArrayList<ShopModel> list_usual_prices = new ArrayList();
                    ArrayList<ShopModel> list_premium_prices = new ArrayList();

                    //

                    JSONObject JO_colors_common_data;
                    JSONObject JO_colors_usual_data;
                    JSONArray JA_usual_colors;
                    JSONArray JA_usual_colors_prices;
                    String[] list_colors;
                    ArrayList<ShopModel> list_prices_colors = new ArrayList();

                    //

                    JSONObject JO_conversion_common_data;

                    JSONArray JA_conversion_money_data;
                    String[] list_money_conversion;
                    ArrayList<ShopModel> list_prices_money = new ArrayList();

                    //

                    JSONArray JA_conversion_exp_data;
                    String[] list_exp_conversion;
                    ArrayList<ShopModel> list_prices_exp = new ArrayList();

                    String description = "", transaction_description = "", sale_amount = "", amount = "";
                    int price = 0;
                    boolean is_sale = false;
                    try {
                        JO_general_data = data.getJSONObject("general");
                        JO_statuses_common_data = JO_general_data.getJSONObject("statuses");
                        JO_statuses_usual_data = JO_statuses_common_data.getJSONObject("usual");
                        JA_usual_statuses = JO_statuses_usual_data.getJSONArray("statuses");

                        list_statuces = new String[JA_usual_statuses.length()];
                        for (int i = 0; i < JA_usual_statuses.length(); i++)
                        {
                            list_statuces[i] = JA_usual_statuses.getString(i);
                        }

                        JA_usual_statuses_prices = JO_statuses_usual_data.getJSONArray("prices");
                        for (int i = 0; i < JA_usual_statuses_prices.length(); i++)
                        {
                            JO_price = JA_usual_statuses_prices.getJSONObject(i);
                            description = JO_price.getString("description");
                            amount = JO_price.getString("amount");
                            price = JO_price.getInt("price");
                            is_sale = JO_price.getBoolean("is_sale");
                            sale_amount = JO_price.getString("sale_amount");
                            list_usual_prices.add(new ShopModel(description, amount, price, is_sale, transaction_description, sale_amount, list_usual_prices.size()));
                        }

                        JA_premium_statuses_prices = JO_statuses_common_data.getJSONArray("premium");
                        for (int i = 0; i < JA_premium_statuses_prices.length(); i++)
                        {
                            JO_price = JA_premium_statuses_prices.getJSONObject(i);
                            description = JO_price.getString("description");
                            amount = JO_price.getString("amount");
                            price = JO_price.getInt("price");
                            is_sale = JO_price.getBoolean("is_sale");
                            sale_amount = JO_price.getString("sale_amount");
                            list_premium_prices.add(new ShopModel(description, amount, price, is_sale, transaction_description, sale_amount, list_premium_prices.size()));
                        }

                        list_shop.add(new ShopModel("buy_status", list_usual_prices, list_statuces, list_premium_prices));

                        //////////////////////

                        JO_colors_common_data = JO_general_data.getJSONObject("personal_colors");
                        JO_colors_usual_data = JO_colors_common_data.getJSONObject("usual");
                        JA_usual_colors = JO_colors_usual_data.getJSONArray("colors");

                        list_colors = new String[JA_usual_colors.length()];
                        for (int i = 0; i < JA_usual_colors.length(); i++)
                        {
                            list_colors[i] = JA_usual_colors.getString(i);
                        }

                        JA_usual_colors_prices = JO_colors_usual_data.getJSONArray("prices");
                        for (int i = 0; i < JA_usual_colors_prices.length(); i++)
                        {
                            JO_price = JA_usual_colors_prices.getJSONObject(i);
                            description = JO_price.getString("description");
                            amount = JO_price.getString("amount");
                            price = JO_price.getInt("price");
                            is_sale = JO_price.getBoolean("is_sale");
                            sale_amount = JO_price.getString("sale_amount");
                            list_prices_colors.add(new ShopModel(description, amount, price, is_sale, transaction_description, sale_amount, list_prices_colors.size()));
                        }

                        list_shop.add(new ShopModel("buy_color", list_prices_colors, list_colors));

                        /////////////////

                        JO_conversion_common_data = JO_general_data.getJSONObject("conversion");
                        JA_conversion_money_data = JO_conversion_common_data.getJSONArray("money");

                        list_money_conversion = new String[JA_conversion_money_data.length()];
                        for (int i = 0; i < JA_conversion_money_data.length(); i++)
                        {
                            JO_price = JA_conversion_money_data.getJSONObject(i);
                            description = JO_price.getString("description");
                            price = JO_price.getInt("price");
                            is_sale = JO_price.getBoolean("is_sale");
                            sale_amount = JO_price.getString("sale_amount");
                            list_money_conversion[i] = JO_price.getString("money");
                            list_prices_money.add(new ShopModel(description, amount, price, is_sale, transaction_description, sale_amount, list_prices_money.size()));
                        }

                        list_shop.add(new ShopModel("convert_money", list_prices_money, list_money_conversion));

                        /////////////////

                        JA_conversion_exp_data = JO_conversion_common_data.getJSONArray("exp");

                        list_exp_conversion = new String[JA_conversion_exp_data.length()];
                        for (int i = 0; i < JA_conversion_exp_data.length(); i++)
                        {
                            JO_price = JA_conversion_exp_data.getJSONObject(i);
                            description = JO_price.getString("description");
                            price = JO_price.getInt("price");
                            is_sale = JO_price.getBoolean("is_sale");
                            sale_amount = JO_price.getString("sale_amount");
                            list_exp_conversion[i] = JO_price.getString("exp");
                            list_prices_exp.add(new ShopModel(description, amount, price, is_sale, transaction_description, sale_amount, list_prices_exp.size()));
                        }

                        list_shop.add(new ShopModel("convert_exp", list_prices_exp, list_exp_conversion));

                        shopAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    };

    private final Emitter.Listener OnBuyItem = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String url = (String) args[0];
                if (args.length != 0 && url.contains("http")) {
                    Log.e("kkk", "1 " + args[0]);
                    Intent mIntent = new Intent();
                    mIntent.setAction(Intent.ACTION_VIEW);
                    mIntent.setData(Uri.parse(url));
                    startActivity(Intent.createChooser( mIntent, "Выберите браузер"));
                }
            }
        });
    };

    private final Emitter.Listener OnBuyPremiumItem = args -> {
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (args.length != 0) {
                    String status = (String) args[0];
                    Log.d("kkk", "buy_item " + status);
                    if (status.equals("OK"))
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        View viewError = getLayoutInflater().inflate(R.layout.dialog_error, null);
                        builder.setView(viewError);
                        AlertDialog alert;
                        alert = builder.create();

                        TextView TV = viewError.findViewById(R.id.dialogError_TV_errorText);
                        TextView TV_title = viewError.findViewById(R.id.dialogError_TV_errorTitle);
                        ImageView IV = viewError.findViewById(R.id.dialogError_IV);

                        IV.setImageResource(R.drawable.crown_gold_dark);
                        TV.setText("Вы успешно завершили покупку!");
                        TV_title.setText("Успешно!");
                        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        alert.show();
                    }
                }
            }
        });
    };

    private final Emitter.Listener onUserError = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if (getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("kkk", "Socket_принять - user_error " + args[0]);
                    JSONObject data = (JSONObject) args[0];
                    String error;
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    View viewError = getLayoutInflater().inflate(R.layout.dialog_error, null);
                    builder.setView(viewError);
                    TextView TV_error = viewError.findViewById(R.id.dialogError_TV_errorText);
                    try {
                        error = data.getString("error");
                        AlertDialog alert;
                        alert = builder.create();
                        switch (error) {
                            case "you_dont_have_enough_gold":
                                TV_error.setText("У вас не хватает золота для покупки!");
                                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                alert.show();
                                break;
                            default:
                                TV_error.setText("Что-то пошло не так");
                                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                alert.show();
                                break;
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };
}