package com.example.ecommerce.adapter.seller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.ecommerce.R;
import com.example.ecommerce.model.seller.Category;
import com.example.ecommerce.utils.CategoryGetter;

import java.util.List;

public class CategoryAdapter extends BaseAdapter {
    Context mContext;
    List<Category> mProducts;
    LayoutInflater mInflater;
    public CategoryAdapter(Context context) {
        this.mProducts = CategoryGetter.getProductsCategory();
        this.mContext = context;
    }

    public String getName(int position){
        return mProducts.get(position).name;
    }

    @Override
    public int getCount() {
        return mProducts.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        if(view==null){
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = mInflater.inflate(R.layout.single_custom_spinner_layout,viewGroup,false);
        }
        TextView textView = view.findViewById(R.id.singleCustomSpinnerNameId);
        ImageView imageView = view.findViewById(R.id.singleCustomSpinnerImage);
        textView.setText(mProducts.get(position).name);
        Glide.with(mContext).load(mProducts.get(position).image).into(imageView);
        return view;
    }
}