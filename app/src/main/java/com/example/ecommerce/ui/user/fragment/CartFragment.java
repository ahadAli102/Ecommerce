package com.example.ecommerce.ui.user.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ecommerce.App;
import com.example.ecommerce.R;
import com.example.ecommerce.adapter.OrderAdapter;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.User;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CartFragment extends Fragment implements OrderAdapter.CustomOrderAdapterClickInterface{

    private List<Order> mOrders;
    private OrderAdapter mAdapter;

    private RecyclerView mRecycler;
    private ProgressDialog mLoadingBar;

    private static final String TAG = "TAG:HomeActCartFr";

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference mOrderRef = database.getReference("order_info");
    private DatabaseReference mProduct = database.getReference("product_info");
    private DatabaseReference mUserRef = database.getReference("users_info");
    private StorageReference mStorage = FirebaseStorage.getInstance().getReference("user_image");

    private Query mOrderQuery,mProductQuery;

    private View mView;

    private ChildEventListener mOrderChildListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d(TAG, "onCreateView: called");
        mView = inflater.inflate(R.layout.fragment_cart, container, false);
        mRecycler = mView.findViewById(R.id.userCartFragmentRecyclerViewId);
        mLoadingBar = new ProgressDialog(requireContext());
        //setRecyclerView();
        mLoadingBar = new ProgressDialog(requireContext());
        setProducts();
        return mView;
    }

    private void setProducts(){
        mOrders = new ArrayList<>();
        if(App.User.CART_FIRST_RUN){
            loadOrders();
            setListener();
            setRecyclerView();
            App.User.CART_FIRST_RUN = false;
        }
        else{
            mOrders.addAll(App.User.getOrders());
            setRecyclerView();
            loadOrders();
        }
    }


    private void loadOrders(){
        Log.d(TAG, "loadOrders: called");
        mOrderChildListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                /*
                public String orderId;
                public String productId;
                public String oderedUserName;
                public String paymentStatus;
                public String amount;
                public String orderedDate;
                public String receivedDate;
                public String imageUrl;
                public String productName;
                public String sellerName;
                 */
                Order order = new Order();
                order.orderId = snapshot.getKey();
                order.productId = snapshot.child(Order.Key.ORDER_PRODUCT_ID).getValue().toString();
                order.oderedUserName = snapshot.child(Order.Key.ORDER_USER_NAME).getValue().toString();
                order.paymentStatus = snapshot.child(Order.Key.ORDER_PAYMENT_STATUS).getValue().toString();
                order.amount = snapshot.child(Order.Key.ORDER_AMOUNT).getValue().toString();
                order.orderedDate = snapshot.child(Order.Key.ORDER_SUBMIT_DATE).getValue().toString();
                order.receivedDate = snapshot.child(Order.Key.ORDER_DELIVERY_DATE).getValue().toString();
                order.productName = App.User.mProductName.get(order.productId);
                order.imageUrl = App.User.mImageMap.get(order.productId);
                order.sellerName = App.User.mSellerMap.get(order.productId);
                Log.d(TAG, "onChildAdded: "+order);
                mOrders.add(order);
                mAdapter.setOrders(mOrders);
                mAdapter.notifyDataSetChanged();
                //addAdditionalInformation(order.productId,order);
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
    }

    /*
    private void addAdditionalInformation(String productKey, Order order) {

        Log.d(TAG, "addAdditionalInformation: "+mProduct.child(productKey).toString());
        ChildEventListener listener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                order.imageUrl = snapshot.child("imageUrl").getValue().toString();
                order.sellerName = snapshot.child("sellerName").getValue().toString();
                Log.d(TAG, "onChildAdded: "+snapshot.getChildrenCount());
                mOrders.add(order);
                Log.d(TAG, "onChildAdded: "+order);
                mAdapter.setOrders(mOrders);
                mAdapter.notifyDataSetChanged();
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

        Query query = mProduct.orderByChild(productKey).equalTo(productKey);
        query.addChildEventListener(listener);
        mProductListeners.add(listener);
    } */

    private void setListener(){
        mOrderQuery = mOrderRef.orderByChild(Order.Key.ORDER_USER_NAME).equalTo(getSavedName());
        mOrderQuery.addChildEventListener(mOrderChildListener);
    }
    private void removeListener(){
        try{
            mOrderQuery.removeEventListener(mOrderChildListener);
        }
        catch (Exception e){

        }
    }

    private void setRecyclerView(){
        Log.d(TAG, "setRecyclerView: called");
        mLoadingBar.dismiss();
        mAdapter= new OrderAdapter(this,requireContext());
        mRecycler.setHasFixedSize(true);
        mRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter.setOrders(mOrders);
        mRecycler.setAdapter(mAdapter);
        Log.d(TAG, "setRecyclerView: Size is : "+ mOrders.size());
        mAdapter.notifyDataSetChanged();

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


    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: called");
        App.User.setOrders(mOrders);
        removeListener();
        //removeListeners();
        super.onDestroy();
    }

    @Override
    public void onItemClick(int position) {

    }

    @Override
    public void onLongItemClick(int position) {

    }

}