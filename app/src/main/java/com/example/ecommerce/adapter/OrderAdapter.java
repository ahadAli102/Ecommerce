package com.example.ecommerce.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.ecommerce.model.Order;

import java.util.ArrayList;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder>{
    private static final String TAG = "TAG:OrdAdpt";
    private List<Order> mMainOrders;
    private List<Order> mOrders;
    private Context mContext;

    private CustomOrderAdapterClickInterface mClickInterface;

    public OrderAdapter(CustomOrderAdapterClickInterface clickInterface, Context mContext){
        this.mClickInterface = clickInterface;
        this.mContext = mContext;
    }
    public void setOrders(List<Order> orders) {
        this.mOrders = orders;
        mMainOrders = new ArrayList<>(mOrders);
        notifyDataSetChanged();
        Log.d(TAG, "setProducts: "+orders.size());
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_order_layout,parent,false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = mOrders.get(position);
        holder.mProductName.setText(String.format("Product: %s", order.productName));
        holder.mAmountText.setText(String.format("Amount: %s", order.amount));
        holder.mDeliveryDate.setText(String.format("Receive Date%s", order.receivedDate));
        holder.mSellerText.setText(String.format("Delivery Date%s", order.sellerName));
        try{
            Glide.with(mContext)
                    .asBitmap()
                    .load(order.imageUrl)
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
        return mOrders.size();
    }


    class OrderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private TextView mProductName,mAmountText,mSellerText,mDeliveryDate;
        private ImageView mExpandImage,mProductImage;
        private ConstraintLayout mExpandLayout;
        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            mProductName = itemView.findViewById(R.id.singleOrderNameId);
            mAmountText = itemView.findViewById(R.id.singleOrderAmountId);
            mSellerText = itemView.findViewById(R.id.singleOrderSellerId);
            mDeliveryDate = itemView.findViewById(R.id.singleOrderReceivedDateId);
            mProductImage = itemView.findViewById(R.id.singleOrderImageViewId);
            mExpandImage = itemView.findViewById(R.id.singleOrderExpandImageViewId);
            mExpandLayout = itemView.findViewById(R.id.singleOrderExpandId);
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
