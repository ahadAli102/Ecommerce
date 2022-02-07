package com.example.ecommerce.ui.admin.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ecommerce.App;
import com.example.ecommerce.R;
import com.example.ecommerce.adapter.AdminOrderAdapter;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.utils.DateFormatter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class AdminOrderActivity extends AppCompatActivity implements AdminOrderAdapter.AdminOrderAdapterClickInterface {
    private RecyclerView mRecycler;
    private AdminOrderAdapter mAdapter;
    private List<Order> mOrders;

    private ImageView mImage;
    private TextView mNameText,mPriceText,mDescriptionText;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference mProductRef = database.getReference("product_info");
    private DatabaseReference mOrderRef = database.getReference("order_info");

    private Product mProduct;

    private static final String TAG = "TAG:AdOrAct";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order);
        mRecycler = findViewById(R.id.adminOrderActivityRecyclerViewId);
        mImage = findViewById(R.id.adminOrderActivityImageId);
        mNameText = findViewById(R.id.adminOrderActivityNameId);
        mPriceText = findViewById(R.id.adminOrderActivityPriceId);
        mDescriptionText = findViewById(R.id.adminOrderActivityDescriptionId);

        mProduct = (Product) getIntent().getSerializableExtra(Product.Key.ADMIN_DATA_PASS);

        mNameText.setText(mProduct.name);
        mPriceText.setText("Price : "+mProduct.price+"$");
        mDescriptionText.setText(mProduct.description);

        try{
            Glide.with(this).load(mProduct.imageUrl).into(mImage);
        }
        catch (Exception e){
            Log.d(TAG, "onCreate: "+e.getMessage());
        }

        mPriceText.setOnClickListener(v -> updatePrice(mProduct));

        setRecycler(mProduct);
    }
    private void setRecycler(Product product){
        Log.d(TAG, "setRecycler: called");
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRecycler.setHasFixedSize(true);
        mAdapter = new AdminOrderAdapter(this);
        mAdapter.setForActivity(this);
        mOrders = new ArrayList<>();
        mOrders.addAll(getFilterOrder(product));
        mAdapter.setOrders(mOrders);
        mRecycler.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

    }
    private List<Order> getFilterOrder(Product product){
        List<Order> orders = new ArrayList<>();
        Log.d(TAG, "getFilterOrder: product key : "+product.id);
        for(Order order : App.Seller.getAdminOrders()){
            if(order.productId.equals(product.id)){
                Log.d(TAG, "getFilterOrder: order product key : "+order.productId);
                orders.add(order);
            }
        }
        return orders;
    }
    private void updatePrice(Product product){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View dialogView = getLayoutInflater().inflate(R.layout.custom_product_price_input, null);
        builder.setView(dialogView);
        EditText priceEditText = dialogView.findViewById(R.id.customProductPriceInputPriceId);
        Button updateButton = dialogView.findViewById(R.id.customProductPriceInputButtonId);
        ImageView closeImage = dialogView.findViewById(R.id.customProductPriceInputCloseId);
        priceEditText.append(product.price);
        final AlertDialog dialog = builder.create();
        closeImage.setOnClickListener(v -> dialog.dismiss());
        updateButton.setOnClickListener(v -> {
            float price;
            try{
                price = Float.parseFloat(priceEditText.getText().toString().trim());
            }
            catch (Exception e){
                Toast.makeText(AdminOrderActivity.this, "Please enter valid price", Toast.LENGTH_SHORT).show();
                return;
            }
            if (price == 0.0){
                Toast.makeText(AdminOrderActivity.this, "Please enter al least 0.1$ ", Toast.LENGTH_SHORT).show();
                return;
            }

            mProductRef.child(product.id).child(Product.Key.DATABASE_PRODUCT_PRICE)
                    .setValue(""+price)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            dialog.dismiss();
                            mPriceText.setText("Price: "+price+"$");
                            App.Seller.updateProduct(product.id,""+price);
                            Toast.makeText(AdminOrderActivity.this, "Update successful", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(AdminOrderActivity.this, "Update unsuccessful: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(AdminOrderActivity.this, "Update failed: "+e.getMessage(), Toast.LENGTH_SHORT).show());
        });
        dialog.show();
    }

    public String selectDate(Order order,int position) {
        Toast.makeText(this, "select date on "+order.orderId, Toast.LENGTH_SHORT).show();
        String date = "";
        DatePickerDialog datePickerDialog;
        DatePicker dp;
        dp = new DatePicker(AdminOrderActivity.this);
        int year = dp.getYear();
        int month = dp.getMonth()+1;
        final int day = dp.getDayOfMonth();
        datePickerDialog = new DatePickerDialog(AdminOrderActivity.this, (datePicker, i, i1, i2) -> updateDate(order,position,i2+"-"+(i1+1)+"-"+i),year,month,day);
        datePickerDialog.show();

        return date;
    }
    private void updateDate(Order order, int position,String date){
        mOrderRef.child(order.orderId).child(Order.Key.ORDER_DELIVERY_DATE)
                .setValue(DateFormatter.format(date))
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        App.Seller.updateOrder(order.orderId, DateFormatter.format(date));
                        mOrders.clear();
                        setRecycler(mProduct);
                        Toast.makeText(AdminOrderActivity.this, "Update successful", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(AdminOrderActivity.this, "Update unsuccessful: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(AdminOrderActivity.this, "Update failed: "+e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onItemClick(int position) {

    }

    @Override
    public void onLongItemClick(int position) {

    }
}