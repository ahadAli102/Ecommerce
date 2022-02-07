package com.example.ecommerce.ui.admin.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.ecommerce.App;
import com.example.ecommerce.R;
import com.example.ecommerce.adapter.AdminProductAdapter;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.User;
import com.example.ecommerce.ui.admin.activity.AdminOrderActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminProductsFragment extends Fragment implements AdminProductAdapter.AdminOrderAdapterClickInterface{
    private static final String TAG = "TAG:AdActOrdFr";
    
    private List<Order> mOrders;
    private List<Product> mProducts;

    private RecyclerView mRecycler;
    private AdminProductAdapter mAdapter;
    private ProgressDialog mLoadingBar;

    private View mView;

    private SearchView mSearchView;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference mProductRef = database.getReference("product_info");
    private DatabaseReference mOrderRef = database.getReference("order_info");

    private ValueEventListener mProductListener;
    private ChildEventListener mOrderListener;
    private Query mProductQuery;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: called");
        mView = inflater.inflate(R.layout.fragment_admin_products, container, false);
        initRecyclerView();
        mLoadingBar = new ProgressDialog(requireContext());
        //load();
        return mView;
    }
    private void load(){
        if(App.Seller.FIRST_ADMIN_PRODUCT_RUN){
            loadSpecificProducts();
            App.Seller.FIRST_ADMIN_PRODUCT_RUN = false;
        }
        else{
            initRecyclerView();
            mProducts.clear();
            mOrders.clear();
            mProducts.addAll(App.Seller.getAdminProducts());
            mOrders.addAll(App.Seller.getAdminOrders());
            mAdapter.setProducts(mProducts);
            mAdapter.notifyDataSetChanged();
            if(App.Seller.FIRST_ADMIN_PRODUCT_CHANGED){
                Toast.makeText(requireContext(), "Product changed", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onStart: product changed");
                App.Seller.FIRST_ADMIN_PRODUCT_CHANGED = false;
                removeDatabaseListeners();
                showLoadingDialog("Loading Data","Please wait while loading data!");
                loadSpecificProducts();
                App.Seller.FIRST_ADMIN_PRODUCT_RUN = false;
            }
        }
    }
    private void loadSpecificProducts(){
        mProductListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(mProducts == null){
                    mProducts = new ArrayList<>();
                }
                mProducts.clear();
                for (DataSnapshot childSnap : snapshot.getChildren()){
                    try{
                        if(childSnap.child(Product.Key.DATABASE_PRODUCT_VISIBILITY).getValue().toString()
                                .equals(Product.Key.DATABASE_PRODUCT_VISIBLE)){
                            Product product = new Product();
                            product.id = childSnap.getKey();
                            product.imageUrl = childSnap.child(Product.Key.DATABASE_PRODUCT_IMAGE_URL).getValue().toString();
                            product.category = childSnap.child(Product.Key.DATABASE_PRODUCT_CATEGORY).getValue().toString();
                            product.name = childSnap.child(Product.Key.DATABASE_PRODUCT_NAME).getValue().toString();
                            product.sellerName = childSnap.child(Product.Key.DATABASE_PRODUCT_SELLER_NAME).getValue().toString();
                            product.date = childSnap.child(Product.Key.DATABASE_PRODUCT_DATE).getValue().toString();
                            product.description = childSnap.child(Product.Key.DATABASE_PRODUCT_DESCRIPTION).getValue().toString();
                            product.price = childSnap.child(Product.Key.DATABASE_PRODUCT_PRICE).getValue().toString();
                            App.Seller.mOrderMap.put(childSnap.getKey(),true);
                            App.Seller.mProductMap.put(product.id,product.name);
                            mProducts.add(product);
                            Log.d(TAG, "onDataChange: key : "+childSnap.getKey()+" product name : "+product.name+" is visible");

                        }
                    }
                    catch (Exception e){
                        //Log.e(TAG, "onChildAdded: ", e);
                    }
                }
                closeLoadingDialog();
                Log.d(TAG, "onDataChange: product size is : "+mProducts.size());
                mAdapter.setProducts(mProducts);
                mAdapter.notifyDataSetChanged();
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
                    Log.d(TAG, "onChildAdded: " + orderProductId + " name : "+order.productName);
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
        mSearchView = mView.findViewById(R.id.adminProductFragmentSearchViewId);
        mRecycler = mView.findViewById(R.id.adminProductFragmentRecyclerViewId);
        mAdapter = new AdminProductAdapter(this,requireContext(),this);
        mProducts = new ArrayList<>();
        mOrders = new ArrayList<>();
        mRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        mRecycler.setAdapter(mAdapter);
        mAdapter.setProducts(mProducts);
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
        Log.d(TAG, "onStop: called");
        App.Seller.setAdminOrders(mOrders);
        App.Seller.setAdminProducts(mAdapter.getProducts());
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: called");
        App.Seller.setAdminOrders(mOrders);
        App.Seller.setAdminProducts(mAdapter.getProducts());
        removeDatabaseListeners();
        super.onDestroy();
    }

    @Override
    public void onItemClick(int position) {
        App.Seller.setAdminOrders(mOrders);
        App.Seller.setAdminProducts(mAdapter.getProducts());
        Intent intent = new Intent(requireActivity(),AdminOrderActivity.class);
        intent.putExtra(Product.Key.ADMIN_DATA_PASS,mAdapter.getProduct(position));
        startActivity(intent);
        //Toast.makeText(requireContext(), "Small click: "+position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLongItemClick(int position) {
        Product product = mProducts.get(position);
        AlertDialog.Builder dialog = new AlertDialog.Builder(requireContext());
        dialog.setTitle("Delete")
                .setMessage("Do you want to delete product "+product.name)
                .setPositiveButton("OK", (dialog12, which) -> deleteProduct(product,position))
                .setNegativeButton("CANCEL", (dialog1, which) -> dialog1.dismiss())
                .setCancelable(false)
                .create()
                .show();
        //Toast.makeText(requireContext(), "Long click: "+position, Toast.LENGTH_SHORT).show();
    }
    private void deleteProduct(final Product product,final int position){
        showLoadingDialog("Deleting "+product.name,"Please wait until "+product.name+" is deleted");
        mProductRef.child(product.id).child(Product.Key.DATABASE_PRODUCT_VISIBILITY)
                .setValue(Product.Key.DATABASE_PRODUCT_NOT_VISIBLE)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Log.d(TAG, "deleteProduct: delete successful");
                        Toast.makeText(requireContext(), "Delete successful", Toast.LENGTH_SHORT).show();
                        //removeProduct(position);
                    }
                    else {
                        Toast.makeText(requireContext(), "Delete Failed: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void removeProduct(int position){
        Log.d(TAG, "removeProduct: size: "+mProducts.size()+" position: "+position+" adapter: "+mAdapter.getItemCount());
        mAdapter.notifyItemRemoved(position);
        mProducts.remove(position);
        mAdapter.setProducts(mProducts);
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

    private void updatePrice(String price, String id) {
        Toast.makeText(requireContext(), ""+id+" will be changed to "+price, Toast.LENGTH_SHORT).show();
    }

    private void showLoadingDialog(String title,String message){
        mLoadingBar.setTitle(title);
        mLoadingBar.setMessage(message);
        mLoadingBar.setCanceledOnTouchOutside(false);
        mLoadingBar.show();
    }
    private void closeLoadingDialog(){
        mLoadingBar.dismiss();
    }
    
}