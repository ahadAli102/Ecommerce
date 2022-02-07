package com.example.ecommerce.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommerce.R;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.ui.admin.activity.AdminOrderActivity;
import com.example.ecommerce.ui.admin.fragment.AdminOrderFragment;

import java.util.ArrayList;
import java.util.List;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.OrderViewHolder>{
    private static final String TAG = "TAG:AdmOrdAdpt";
    private List<Order> mMainOrders;
    private List<Order> mOrders;

    private AdminOrderAdapterClickInterface mClickInterface;

    private AdminOrderActivity mActivity;
    private AdminOrderFragment mFragment;

    private boolean isActivity,isFragment;

    public AdminOrderAdapter(AdminOrderAdapterClickInterface clickInterface){
        this.mClickInterface = clickInterface;

    }
    public void setOrders(List<Order> orders) {
        this.mOrders = orders;
        mMainOrders = new ArrayList<>(mOrders);
        Log.d(TAG, "setProducts: "+orders.size());
    }
    public List<Order> getAllOrders(){
        return mMainOrders;
    }

    public Order getOrder(int position){
        return mOrders.get(position);
    }

    public void setForActivity(AdminOrderActivity activity){
        this.mActivity = activity;
        isActivity = true;
        isFragment = false;
    }
    public void setForFragment(AdminOrderFragment fragment){
        this.mFragment = fragment;
        isFragment = true;
        isActivity = false;
    }


    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_admin_order_layout,parent,false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = mOrders.get(position);
        holder.mProductName.setText(String.format("Product: %s", order.productName));
        holder.mBuyerName.setText(String.format("Customer name: %s", order.oderedUserName));
        holder.mAmountText.setText(String.format("Amount: %s", order.amount));
        holder.mDeliveryDate.setText(String.format("Delivery: %s", order.receivedDate));
        holder.mPaymentStatus.setText(String.format("Payment status: %s", order.paymentStatus));
        holder.mDeliveryDate.setOnClickListener(v -> {
            if(isActivity && !isFragment){
                mActivity.selectDate(mOrders.get(position),position);
            }
            if (isFragment && !isActivity){
                mFragment.selectDate(mOrders.get(position),position);
            }
        });
        if (isActivity && !isFragment){
            holder.mProductName.setVisibility(View.GONE);
        }
    }
    @Override
    public int getItemCount() {
        return mOrders.size();
    }
    public Filter getFilter(){
        return adminOrderFilter;
    }
    private Filter adminOrderFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence ch) {
            List<Order> filterOrder = new ArrayList<>();
            final String filterPattern = ch.toString().toLowerCase();
            if(ch==null || ch.length()==0){
                filterOrder.addAll(mMainOrders);
            }
            else {
                for(Order m : mMainOrders){
                    String name = String.valueOf(m.productName).toLowerCase();
                    String amount = String.valueOf(m.amount).toLowerCase();
                    String deliveryDate = String.valueOf(m.receivedDate).toLowerCase();
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
            mOrders.clear();
            mOrders.addAll((List<Order>)results.values);
            notifyDataSetChanged();
        }
    };

    class OrderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private TextView mProductName,mBuyerName,mAmountText,mDeliveryDate,mPaymentStatus;
        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            mProductName = itemView.findViewById(R.id.singleAdminOrderProductId);
            mBuyerName = itemView.findViewById(R.id.singleAdminOrderBuyerId);
            mAmountText = itemView.findViewById(R.id.singleAdminOrderAmountId);
            mDeliveryDate = itemView.findViewById(R.id.singleAdminOrderReceivedDateId);
            mPaymentStatus = itemView.findViewById(R.id.singleAdminOrderPaymentId);
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
