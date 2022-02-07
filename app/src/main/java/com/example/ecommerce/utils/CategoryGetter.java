package com.example.ecommerce.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.ecommerce.R;
import com.example.ecommerce.model.CategoryFilter;
import com.example.ecommerce.model.seller.Category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CategoryGetter {
    private static String SAVED_FILTER="filter 100";
    private static String SAVED_FILTER_KEY="101";
    private static final String TAG = "TAG:CategoryGetter";

    public static List<Category> getProductsCategory(){
        List<Category> categories = new ArrayList<>();
        categories.add(new Category("Cap", R.drawable.hats));
        categories.add(new Category("Glass",R.drawable.glasses));
        categories.add(new Category("HeadPhone",R.drawable.headphoness));
        categories.add(new Category("T-Shirt",R.drawable.tshirts));
        categories.add(new Category("Laptop",R.drawable.laptops));
        categories.add(new Category("Mobile",R.drawable.mobiles));
        categories.add(new Category("Shoe",R.drawable.shoess));
        categories.add(new Category("Sweater",R.drawable.sweather));
        categories.add(new Category("Watch",R.drawable.watches));
        return categories;
    }

    private static List<String> getSavedFilter(Context context) throws Exception{
        SharedPreferences preferences = context.getSharedPreferences(SAVED_FILTER,Context.MODE_PRIVATE);
        String categories = preferences.getString(SAVED_FILTER_KEY,"");
        Log.d(TAG, "getSavedFilter: categories : "+categories);
        if (categories.isEmpty() || categories.equals("")){
            throw new Exception("NoSavedValueException");
        }
        else {
            return new ArrayList<>(Arrays.asList(categories.split(",")));
        }
    }

    private static List<CategoryFilter> getAllCategories(){
        List<CategoryFilter> categoryFilters = new ArrayList<>();
        categoryFilters.add(new CategoryFilter("Cap", false));
        categoryFilters.add(new CategoryFilter("Female Dress",false));
        categoryFilters.add(new CategoryFilter("Glass",false));
        categoryFilters.add(new CategoryFilter("HeadPhone",false));
        categoryFilters.add(new CategoryFilter("T-Shirt",false));
        categoryFilters.add(new CategoryFilter("Mobile",false));
        categoryFilters.add(new CategoryFilter("Shoe",false));
        categoryFilters.add(new CategoryFilter("Sweater",false));
        categoryFilters.add(new CategoryFilter("Watch",false));
        return categoryFilters;
    }

    public static List<CategoryFilter> getFilterCategories(Context context) throws Exception {
        List<CategoryFilter> categoryFilters = getAllCategories();
        for (String category : getSavedFilter(context)  ) {
            for (int i = 0; i < categoryFilters.size(); i++) {
                if (categoryFilters.get(i).name.equals(category)){
                    categoryFilters.get(i).selected = true;
                }
            }
        }
        return categoryFilters;
    }


    private static List<CategoryFilter> getSelectedFilters(List<CategoryFilter> allFilters){
        List<CategoryFilter> savedFilters = new ArrayList<>();
        for (CategoryFilter filter : allFilters) {
            if (filter.selected){
                savedFilters.add(filter);
            }
        }
        return savedFilters;
    }
    private static void saveFilterCategories(Context context,String value){
        Log.d(TAG, "saveFilterCategories: value : "+value);
        SharedPreferences preferences = context.getSharedPreferences(SAVED_FILTER,Context.MODE_PRIVATE);
        SharedPreferences.Editor myEdit = preferences.edit();
        myEdit.putString(SAVED_FILTER_KEY,value);
        myEdit.apply();
    }
    public static void setFilterCategories(Context context,List<CategoryFilter> filters){
        Log.d(TAG, "setFilterCategories: called");
        StringBuilder sb = new StringBuilder();
        List<CategoryFilter> savedFilters = getSelectedFilters(filters);
        for (int i = 0; i < savedFilters.size(); i++) {
            if(savedFilters.get(i).selected){
                sb.append(savedFilters.get(i).name);
            }
            if(i==savedFilters.size()-1){
                sb.append(",");
            }
        }
        saveFilterCategories(context,sb.toString());
    }
}
