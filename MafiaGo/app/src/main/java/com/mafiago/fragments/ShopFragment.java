package com.mafiago.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.example.mafiago.R;
import com.google.android.material.tabs.TabLayout;
import com.mafiago.classes.OnBackPressedListener;
import com.mafiago.pager_adapters.SettingsPagerAdapter;
import com.mafiago.pager_adapters.ShopPagerAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.socket.emitter.Emitter;

import static com.mafiago.MainActivity.socket;

public class ShopFragment extends Fragment implements OnBackPressedListener {

    ImageView Menu;
    TabLayout tab;
    ViewPager viewPager;
    RelativeLayout btn_back;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_shop, container, false);

        tab = view.findViewById(R.id.fragmentShop_TabLayout);
        viewPager = view.findViewById(R.id.fragmentShop_ViewPager);
        Menu = view.findViewById(R.id.fragmentMenu_IV_menu);
        btn_back = view.findViewById(R.id.fragmentGamesList_RL_back);

        // Получаем ViewPager и устанавливаем в него адаптер
        viewPager.setAdapter(
                new ShopPagerAdapter(getActivity().getSupportFragmentManager(), getActivity()));

        // Передаём ViewPager в TabLayout
        tab.setupWithViewPager(viewPager);

        viewPager.setCurrentItem(1);

        socket.off("user_error");
        socket.off("get_store");
        socket.off("buy_item");

        socket.on("buy_item", OnBuyItem);
        socket.on("user_error", onUserError);

        Menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup_menu = new PopupMenu(getActivity(), Menu);
                popup_menu.inflate(R.menu.main_menu);
                popup_menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.mainMenu_play:
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
                                return true;
                            case R.id.mainMenu_shop:
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new ShopFragment()).commit();
                                return true;
                            case R.id.mainMenu_friends:
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new FriendsFragment()).commit();
                                return true;
                            case R.id.mainMenu_chats:
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new PrivateChatsFragment()).commit();
                                return true;
                            case R.id.mainMenu_settings:
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new SettingsFragment()).commit();
                                return true;
                        }
                        return true;
                    }
                });
                popup_menu.show();
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new MenuFragment()).commit();
            }
        });

        return view;
    }

    @Override
    public void onBackPressed() {
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new MenuFragment()).commit();
    }

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
                            case "you_dont_have_enough_money":
                                TV_error.setText("У вас не хватает монет для покупки!");
                                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                alert.show();
                                break;
                            case "mat_in_status":
                                TV_error.setText("Нецензурный статус!");
                                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                alert.show();
                                break;
                            case "forbidden_status":
                                TV_error.setText("Недопустимый статус!");
                                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                alert.show();
                                break;
                            case "you_already_have_this_eternal_status":
                                TV_error.setText("У вас уже куплен этот статус!");
                                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                alert.show();
                                break;
                            case "you_already_have_this_eternal_color":
                                TV_error.setText("У вас уже куплен этот цвет!");
                                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                alert.show();
                                break;
                            case "you_already_have_role_chance_booster":
                                TV_error.setText("У вас уже куплено увеличение шанса выпадения роли! Вы сможете купить новое после окончания срока действия старого");
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

    private final Emitter.Listener OnBuyItem = args -> {
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
}