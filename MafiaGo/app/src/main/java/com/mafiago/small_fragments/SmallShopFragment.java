package com.mafiago.small_fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.example.mafiago.R;
import com.mafiago.MainActivity;
import com.mafiago.adapters.FriendRequestsAdapter;
import com.mafiago.adapters.FriendsAdapter;
import com.mafiago.adapters.GoldAdapter;
import com.mafiago.adapters.PremiumAdapter;
import com.mafiago.adapters.ShopAdapter;
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

        switch (mPage)
        {
            case 1:
                view = inflater.inflate(R.layout.small_fragment_shop, container, false);

                LV_gold = view.findViewById(R.id.smallFragmentShop_LV_shop);
                goldAdapter = new GoldAdapter(list_gold, getContext());
                LV_gold.setAdapter(goldAdapter);

                socket.on("get_store", OnGetGoldStore);
                socket.on("buy_item", OnBuyItem);

                json = new JSONObject();
                try {
                    json.put("nick", MainActivity.NickName);
                    json.put("session_id", MainActivity.Session_id);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                socket.emit("get_store", json);
                Log.d("kkk", "Socket_отправка - get_store - "+ json.toString());

                goldAdapter.notifyDataSetChanged();
                break;
            case 2:
                view = inflater.inflate(R.layout.small_fragment_shop, container, false);

                btn_busters = view.findViewById(R.id.smallFragmentShop_btn_busters);
                LV_shop = view.findViewById(R.id.smallFragmentShop_LV_shop);
                shopAdapter = new ShopAdapter(list_shop, getContext());
                LV_shop.setAdapter(shopAdapter);

                btn_busters.setVisibility(View.VISIBLE);

                //socket.on("get_store", OnGetMainStore);
                //socket.on("buy_item", OnBuyItem);

                json = new JSONObject();
                try {
                    json.put("nick", MainActivity.NickName);
                    json.put("session_id", MainActivity.Session_id);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //socket.emit("get_store", json);
                //Log.d("kkk", "Socket_отправка - get_store - "+ json.toString());

                list_shop.add(new ShopModel("status", 2, 3, false, "", "", 0));
                shopAdapter.notifyDataSetChanged();
                break;
            case 3:
                view = inflater.inflate(R.layout.small_fragment_shop, container, false);

                btn_busters = view.findViewById(R.id.smallFragmentShop_btn_busters);
                LV_premium = view.findViewById(R.id.smallFragmentShop_LV_shop);
                premiumAdapter = new PremiumAdapter(list_premium, getContext());
                LV_premium.setAdapter(premiumAdapter);

                socket.on("get_store", OnGetPremiumStore);
                socket.on("buy_item", OnBuyItem);

                json = new JSONObject();
                try {
                    json.put("nick", MainActivity.NickName);
                    json.put("session_id", MainActivity.Session_id);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                socket.emit("get_store", json);
                Log.d("kkk", "Socket_отправка - get_store - "+ json.toString());

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
                if (args.length != 0) {
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
                if (args.length != 0) {
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
                if (args.length != 0) {
                    JSONObject data = (JSONObject) args[0];
                    Log.d("kkk", "принял - get_store - " + data);
                    JSONArray shop_array;
                    JSONObject shop_data;
                    String description = "", transaction_description = "", sale_amount = "";
                    int amount = 0, price = 0;
                    boolean is_sale = false;
                    try {
                        shop_array = data.getJSONArray("gold");
                        for (int i = 0; i < shop_array.length(); i++)
                        {
                            shop_data = shop_array.getJSONObject(i);
                            description = shop_data.getString("description");
                            transaction_description = shop_data.getString("transaction_description");
                            amount = shop_data.getInt("amount");
                            price = shop_data.getInt("price");
                            is_sale = shop_data.getBoolean("is_sale");
                            sale_amount = shop_data.getString("sale_amount");
                            list_shop.add(new ShopModel(description, amount, price, is_sale, transaction_description, sale_amount, list_gold.size()));
                        }
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
                if (args.length != 0) {
                    String url = (String) args[0];
                    Intent mIntent = new Intent();
                    mIntent.setAction(Intent.ACTION_VIEW);
                    mIntent.setData(Uri.parse(url));
                    startActivity(Intent.createChooser( mIntent, "Выберите браузер"));
                }
            }
        });
    };
}