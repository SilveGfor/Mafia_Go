package com.example.mafiago.fragments;

import android.animation.ArgbEvaluator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.mafiago.R;
import com.example.mafiago.adapters.RulesAdapter;
import com.example.mafiago.models.RuleModel;

import java.util.ArrayList;
import java.util.List;

public class RulesFragment extends Fragment {

    ViewPager viewPager;
    RulesAdapter rules_adapter;
    List<RuleModel> models;
    Integer[] colors = null;
    ArgbEvaluator argbEvaluator = new ArgbEvaluator();

    Button Back1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_rules, container, false);

        Back1 = view.findViewById(R.id.btnBack1);

        Back1.setVisibility(View.VISIBLE);

        models = new ArrayList<>();
        models.add(new RuleModel(R.drawable.ic_snow, "Мафия", "Главный персонаж, именно мафия решает, кого она убьёт ночью. Чтобы выиграть, команде мирных нужно истребить всю мафию!"));
        models.add(new RuleModel(R.drawable.icon_cher, "Шериф", "Один из главных и решающий персонажей игры. Перед рассветом может проверить любого игрока на принадлежность к мафии"));
        models.add(new RuleModel(R.drawable.icon_doctor, "Доктор", "Наш спаситель. Перед рассветом может исцелить любого игрока. Даже если кого-то ночью хотели убить, доктор может его спасти"));
        models.add(new RuleModel(R.drawable.citizen, "Городской житель", "Обычный игрок, у него нет каких-либо специальных возможностей. Днём пытается вместе со всеми найти мафию"));

        rules_adapter = new RulesAdapter(models, getActivity());

        viewPager = view.findViewById(R.id.viewPager);
        viewPager.setAdapter(rules_adapter);
        viewPager.setPadding(130,0,130,0);

        Integer[] colors_temp = {
                getResources().getColor(R.color.color1),
                getResources().getColor(R.color.color2),
                getResources().getColor(R.color.color3),
                getResources().getColor(R.color.color4)
        };

        colors = colors_temp;

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position < (rules_adapter.getCount() - 1) && position < (colors.length - 1))
                {

                    viewPager.setBackgroundColor(
                            (Integer) argbEvaluator.evaluate(
                                    positionOffset,
                                    colors[position],
                                    colors[position + 1]
                            )
                    );

                }
                else
                {
                    viewPager.setBackgroundColor(colors[colors.length - 1]);
                }
            }

            @Override
            public void onPageSelected(int i) {

            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        Back1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Back1.setVisibility(View.GONE);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new MenuFragment()).commit();
            }
        });

        return view;
    }
}
