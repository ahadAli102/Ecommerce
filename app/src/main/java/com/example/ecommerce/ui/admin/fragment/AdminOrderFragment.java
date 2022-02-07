package com.example.ecommerce.ui.admin.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.Toast;

import com.example.ecommerce.App;
import com.example.ecommerce.R;
import com.example.ecommerce.adapter.AdminOrderAdapter;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.User;
import com.example.ecommerce.utils.DateFormatter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminOrderFragment extends Fragment implements AdminOrderAdapter.AdminOrderAdapterClickInterface{

    private RecyclerView mRecycler;
    private AdminOrderAdapter mAdapter;
    private List<Order> mOrders;

    private View mView;

    private SearchView mSearchView;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference mProductRef = database.getReference("product_info");
    private DatabaseReference mOrderRef = database.getReference("order_info");

    private ValueEventListener mProductListener;
    private ChildEventListener mOrderListener;
    private Query mProductQuery;

    private static final String TAG = "TAG:AdActAdOrFr";
    

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: called");
        mView = inflater.inflate(R.layout.fragment_admin_order, container, false);
        init();
        return mView;
    }
    private void init(){
        mSearchView = mView.findViewById(R.id.adminOrderFragmentSearchViewId);
        mRecycler = mView.findViewById(R.id.adminOrderFragmentRecyclerViewId);
        mSearchView.setOnClickListener(v -> mSearchView.setIconified(false));
        initRecyclerView();
    }

    private void load(){
        mOrders = new ArrayList<>();
        if(App.Seller.FIRST_ADMIN_ORDER_RUN){
            loadSpecificProducts();
            App.Seller.FIRST_ADMIN_ORDER_RUN = false;
        }
        else{
            initRecyclerView();
            mOrders.clear();
            mOrders.addAll(App.Seller.getAdminOrders());
            mAdapter.setOrders(mOrders);
            mAdapter.notifyDataSetChanged();
            if(App.Seller.FIRST_ADMIN_PRODUCT_CHANGED){
                Toast.makeText(requireContext(), "Product changed", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onStart: product changed");
                App.Seller.FIRST_ADMIN_PRODUCT_CHANGED = false;
                removeDatabaseListeners();
                loadSpecificProducts();
                App.Seller.FIRST_ADMIN_PRODUCT_RUN = false;
            }
        }
    }
    private void loadSpecificProducts(){
        mProductListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnap : snapshot.getChildren()){
                    String sellerName = childSnap.child(Product.Key.DATABASE_PRODUCT_SELLER_NAME).getValue().toString();
                    String productName = childSnap.child(Product.Key.DATABASE_PRODUCT_NAME).getValue().toString();
                    App.Seller.mOrderMap.put(childSnap.getKey(),true);
                    App.Seller.mProductMap.put(childSnap.getKey(),productName);
                    Log.d(TAG, "onDataChange: key: "+childSnap.getKey()+" name: "+productName);
                }
                loadOrders();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        mProductQuery = mProductRef.orderByChild(Product.Key.DATABASE_PRODUCT_SELLER_NAME).equalTo(getSavedName());
        mProductQuery.addValueEventListener(mProductListener);
    }

    private void loadOrders(){
        if(mOrders == null){
            mOrders = new ArrayList<>();
        }
        mOrders.clear();
        mOrderListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String orderProductId = snapshot.child(Order.Key.ORDER_PRODUCT_ID).getValue().toString();
                if(App.Seller.mProductMap.containsKey(orderProductId)){
                    Order order = new Order();
                    order.orderId = snapshot.getKey();
                    order.productId = snapshot.child(Order.Key.ORDER_PRODUCT_ID).getValue().toString();
                    order.paymentStatus = snapshot.child(Order.Key.ORDER_PAYMENT_STATUS).getValue().toString();
                    order.orderedDate = snapshot.child(Order.Key.ORDER_SUBMIT_DATE).getValue().toString();
                    order.receivedDate = snapshot.child(Order.Key.ORDER_DELIVERY_DATE).getValue().toString();
                    order.oderedUserName = snapshot.child(Order.Key.ORDER_USER_NAME).getValue().toString();
                    order.amount = snapshot.child(Order.Key.ORDER_AMOUNT).getValue().toString();
                    order.productName = App.Seller.mProductMap.get(orderProductId);
                    mOrders.add(order);
                    mAdapter.setOrders(mOrders);
                    mAdapter.notifyDataSetChanged();
                    Log.d(TAG, "onChildAdded: " + orderProductId + " exist in database database\n");
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        mOrderRef.addChildEventListener(mOrderListener);
    }

    private void initRecyclerView(){
        mRecycler = mView.findViewById(R.id.adminOrderFragmentRecyclerViewId);
        mAdapter = new AdminOrderAdapter(this);
        mAdapter.setForFragment(this);
        mOrders = new ArrayList<>();
        mRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        mRecycler.setAdapter(mAdapter);
        mAdapter.setOrders(mOrders);
        mAdapter.notifyDataSetChanged();
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: called");
        load();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop: called ");
        App.Seller.setAdminOrders(mAdapter.getAllOrders());
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: called");
        App.Seller.setAdminOrders(mAdapter.getAllOrders());
        removeDatabaseListeners();
        super.onDestroy();
    }

    
    private void removeDatabaseListeners(){
        try{
            mProductQuery.removeEventListener(mProductListener);
        }
        catch (Exception e){

        }
        try{
            mOrderRef.removeEventListener(mOrderListener);
        }
        catch (Exception e){

        }
    }

    private String getSavedName(){
        Context context = requireContext();
        String name="";
        SharedPreferences sharedPreferences = context.getSharedPreferences(User.Save.SAVED_STATUS_FILTER_KEY, Context.MODE_PRIVATE);
        try{
            name = (sharedPreferences.getString(User.Save.SAVED_NAME,"not found"));
        }
        catch (Exception e){
            Log.e(TAG, "onCreate: ", e);
        }
        return name;
    }
    public String selectDate(Order order,int position) {
        Log.d(TAG, "selectDate: "+order);
        String date = "";
        DatePickerDialog datePickerDialog;
        DatePicker dp;
        dp = new DatePicker(requireContext());
        int year = dp.getYear();
        int month = dp.getMonth()+1;
        final int day = dp.getDayOfMonth();
        datePickerDialog = new DatePickerDialog(requireContext(), (datePicker, i, i1, i2) -> updateDate(order,position,i2+"-"+(i1+1)+"-"+i),year,month,day);
        datePickerDialog.show();

        return date;
    }
    private void updateDate(Order order, int position,String date){
        Log.d(TAG, "updateDate: "+order);
        mOrderRef.child(order.orderId).child(Order.Key.ORDER_DELIVERY_DATE)
                .setValue(DateFormatter.format(date))
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        App.Seller.updateOrder(order.orderId,DateFormatter.format(date));
                        mOrders.clear();
                        mOrders.addAll(App.Seller.getAdminOrders());
                        mAdapter.setOrders(mOrders);
                        mAdapter.notifyDataSetChanged();
                        Toast.makeText(requireContext(), "Update successful", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(requireContext(), "Update unsuccessful: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Update failed: "+e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    

    @Override
    public void onItemClick(int position) {
        Log.d(TAG, "onItemClick: "+mAdapter.getOrder(position));
    }

    @Override
    public void onLongItemClick(int position) {
        Log.d(TAG, "onItemClick: "+mAdapter.getOrder(position));
    }
}