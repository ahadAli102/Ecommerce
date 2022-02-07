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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.ecommerce.R;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.ui.admin.fragment.AdminProductsFragment;

import java.util.ArrayList;
import java.util.List;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.OrderViewHolder> {
    private static final String TAG = "TAG:AdOrdAdpt";
    private List<Product> mMainProducts;
    private List<Product> mProducts;

    private Context mContext;
    private AdminOrderAdapterClickInterface mClickInterface;

    private AdminProductsFragment mFragment;

    public AdminProductAdapter(AdminOrderAdapterClickInterface clickInterface, Context mContext, AdminProductsFragment fragment){
        this.mClickInterface = clickInterface;
        this.mContext = mContext;
        this.mFragment = fragment;
    }
    public void setProducts(List<Product> products) {
        this.mProducts = products;
        mMainProducts = new ArrayList<>(mProducts);
        notifyDataSetChanged();
        Log.d(TAG, "setProducts: size "+products.size());
        Log.d(TAG, "setProducts: item count "+products.size());
    }
    public Product getProduct(int position){
        return mProducts.get(position);
    }
    public List<Product> getProducts(){
        return mMainProducts;
    }

    public Filter getFilter(){
        return adminProductFilter;
    }
    private Filter adminProductFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence ch) {
            List<Product> filterOrder = new ArrayList<>();
            final String filterPattern = ch.toString().toLowerCase();
            if(ch==null || ch.length()==0){
                filterOrder.addAll(mMainProducts);
            }
            else {
                for(Product m : mMainProducts){
                    String name = String.valueOf(m.name).toLowerCase();
                    String amount = String.valueOf(m.price).toLowerCase();
                    String deliveryDate = String.valueOf(m.category).toLowerCase();
                    if(name.contains(filterPattern) ||
                            amount.contains(filterPattern) || deliveryDate.contains(filterPattern)){
                        filterOrder.add(m);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values=filterOrder;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mProducts.clear();
            mProducts.addAll((List<Product>)results.values);
            notifyDataSetChanged();
        }
    };

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_admin_product_layout,parent,false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Product product = mProducts.get(position);
        holder.mName.setText(product.name);
        holder.mDescription.setText(product.description);
        holder.mPrice.setText("Price: "+product.price+"$");
        try{
            Glide.with(mContext)
                    .asBitmap()
                    .load(product.imageUrl)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            holder.mProductImage.setImageBitmap(resource);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });
        }
        catch (Exception e){
            Toast.makeText(mContext, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        holder.mExpandImage.setOnClickListener(v -> {
            if(holder.mExpandLayout.getVisibility() == View.VISIBLE){
                holder.mExpandLayout.setVisibility(View.GONE);
                holder.mExpandImage.setImageResource(R.drawable.ic_arrow_down);
            }
            else{
                holder.mExpandLayout.setVisibility(View.VISIBLE);
                holder.mExpandImage.setImageResource(R.drawable.ic_arrow_up);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mProducts.size();
    }


    class OrderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private TextView mName,mPrice,mDescription;
        private ImageView mExpandImage,mProductImage;
        private ConstraintLayout mExpandLayout;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            mName = itemView.findViewById(R.id.singleAdminProductNameId);
            mProductImage = itemView.findViewById(R.id.singleAdminProductImageId);
            mDescription = itemView.findViewById(R.id.singleAdminProductDescriptionId);
            mExpandImage = itemView.findViewById(R.id.singleAdminExpandImageId);

            mExpandLayout = itemView.findViewById(R.id.singleAdminPriceExpandId);
            mPrice = itemView.findViewById(R.id.singleAdminProductPriceNameId);

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

    public interface AdminOrderAdapterClickInterface {
        void onItemClick(int position);
        void onLongItemClick(int position);
    }
}
