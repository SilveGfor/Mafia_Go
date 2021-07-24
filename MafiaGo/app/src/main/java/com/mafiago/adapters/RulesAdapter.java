package com.mafiago.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.example.mafiago.R;
import com.mafiago.models.RuleModel;

import java.util.List;

public class RulesAdapter extends PagerAdapter {

    private List<RuleModel> models;
    private LayoutInflater layoutInflater;
    private Context context;

    public RulesAdapter(List<RuleModel> models, Context context) {
        this.models = models;
        this.context = context;
    }

    @Override
    public int getCount() {
        return models.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.item_rules, container, false);

        ImageView IV_role;
        TextView TV_role, TV_description;

        IV_role = view.findViewById(R.id.itemRules_IV_role);
        TV_role = view.findViewById(R.id.itemRules_TV_role);
        TV_description = view.findViewById(R.id.itemRules_TV_description);

        IV_role.setImageResource(models.get(position).getImage());
        TV_role.setText(models.get(position).getTitle());
        TV_description.setText(models.get(position).getDesc());

        container.addView(view, 0);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }
}