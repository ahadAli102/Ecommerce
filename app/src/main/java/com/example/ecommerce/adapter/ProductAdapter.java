package com.example.ecommerce.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.ecommerce.R;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.ui.user.activity.HomeActivity;
import com.example.ecommerce.ui.user.fragment.HomeFragment;

import java.util.ArrayList;
import java.util.List;


public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder>{
    private static final String TAG = "TAG:ProAdpt";
    private List<Product> mMainProducts;
    private List<Product> mProducts;

    private Context mContext;
    private CustomOrderAdapterClickInterface mClickInterface;
    private HomeFragment mFragment;

    public ProductAdapter(CustomOrderAdapterClickInterface clickInterface, Context mContext, HomeFragment fragment) {
        this.mClickInterface = clickInterface;
        this.mContext = mContext;
        this.mFragment = fragment;
        this.mProducts = new ArrayList<>();
        mMainProducts = new ArrayList<>(mProducts);
    }
    public void setProducts(List<Product> products) {
        this.mProducts = products;
        mMainProducts = new ArrayList<>(mProducts);
        notifyDataSetChanged();
        Log.d(TAG, "setProducts: "+products.size());
    }

    public Product getProduct(int position){
        return mProducts.get(position);
    }
    public List<Product> getProducts(){
        return mMainProducts;
    }


    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_product_items_layout,parent,false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = mProducts.get(position);
        holder.mProductName.setText(product.name);
        holder.mProductDescription.setText(product.description);
        holder.mProductPrice.setText(product.price);
        try{
            Glide.with(mContext)
                    .asBitmap()
                    .load(product.imageUrl)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            holder.mProductImageView.setImageBitmap(resource);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });
        }
        catch (Exception e){
            Toast.makeText(mContext, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return mProducts.size();
    }
    public Filter getFilter(){
        return adminOrderFilter;
    }
    private Filter adminOrderFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence ch) {
            List<Product> filterProducts = new ArrayList<>();
            final String filterPattern = ch.toString().toLowerCase();
            if(ch==null || ch.length()==0){
                filterProducts.addAll(mMainProducts);
            }
            else {
                for(Product m : mMainProducts){
                    String name = String.valueOf(m.name).toLowerCase();
                    String description = String.valueOf(m.description).toLowerCase();
                    if(name.contains(filterPattern) || description.contains(filterPattern)){
                        filterProducts.add(m);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values=filterProducts;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mProducts.clear();
            mProducts.addAll((List<Product>)results.values);
            mFragment.setVisibility();
            notifyDataSetChanged();
        }
    };


    class ProductViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private TextView mProductName,mProductDescription,mProductPrice;
        private ImageView mProductImageView;
        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            mProductName = itemView.findViewById(R.id.singleProductNameId);
            mProductDescription = itemView.findViewById(R.id.singleAdminProductDescriptionId);
            mProductPrice = itemView.findViewById(R.id.singleProductPriceId);
            mProductImageView = itemView.findViewById(R.id.singleAdminProductImageId);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

        }

        @Override
        public void onClick(View v) {
            mClickInterface.onItemClick(getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            mClickInterface.onLongItemClick(getAdapterPosition());
            return false;
        }
    }

    public interface CustomOrderAdapterClickInterface {
        void onItemClick(int position);
        void onLongItemClick(int position);
    }
}
