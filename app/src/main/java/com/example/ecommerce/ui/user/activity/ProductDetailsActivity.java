package com.example.ecommerce.ui.user.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.ecommerce.R;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

public class ProductDetailsActivity extends AppCompatActivity {
    private static final String TAG = "TAG:ProAct";
    private ImageView mImage;
    private TextView mNameText,mDescriptionText,mAmountText,mProductOrderText;
    private Button mAdd,mSub,mOk;
    private static int AMOUNT = 0;
    private EditText mOrderAddressText;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference mRef = database.getReference("order_info");


    public static final String PRODUCT_KEY="123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);
        mImage = findViewById(R.id.productDetailsImageId);
        mNameText = findViewById(R.id.productDetailsNameTextVewId);
        mDescriptionText = findViewById(R.id.productDetailsDescriptionId);
        mProductOrderText = findViewById(R.id.productDetailsOrderTextId);
        mAmountText = findViewById(R.id.productDetailsAmountId);
        mOrderAddressText = findViewById(R.id.productDetailsAddressId);
        mImage = findViewById(R.id.productDetailsImageId);
        mAdd = findViewById(R.id.productDetailsAddButton);
        mSub = findViewById(R.id.productDetailsSubButton);
        mOk = findViewById(R.id.productDetailsBuyButton);
        AMOUNT = 1;

        Product product = (Product) getIntent().getSerializableExtra(PRODUCT_KEY);
        mNameText.setText(product.name+" "+product.price);
        mDescriptionText.setText(product.description);

        mSub.setVisibility(View.INVISIBLE);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float pxHeight = displayMetrics.heightPixels;
        float pxWidth = displayMetrics.widthPixels;
        int targetSetWidth = Math.round(pxWidth);

        try{
            //Glide.with(getBaseContext()).load(product.imageUrl).centerCrop().into(mImage);
            Glide.with(getBaseContext())
                    .asBitmap()
                    .load(product.imageUrl)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            mImage.setImageBitmap(resource);
                            float w = resource.getWidth();
                            float h = resource.getHeight();
                            float targetHeight = pxWidth * (h / w);
                            int targetSetHeight = Math.round(targetHeight);
                            mImage.getLayoutParams().width = targetSetWidth;
                            mImage.getLayoutParams().height = targetSetHeight;
                            Toast.makeText(ProductDetailsActivity.this, "Image Loaded", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            Toast.makeText(ProductDetailsActivity.this, "Error on loading", Toast.LENGTH_SHORT).show();
                        }
                    });
            Log.d(TAG, "onCreate: "+product.imageUrl);
        }
        catch (Exception e){
            Toast.makeText(this, "Failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        mAdd.setOnClickListener(v -> {
            mAmountText.setText(""+(++AMOUNT));
            mSub.setVisibility(View.VISIBLE);
        });

        mSub.setOnClickListener(v -> {
            if(AMOUNT<2){
                mSub.setVisibility(View.INVISIBLE);
            }
            else{
                mAmountText.setText(""+(--AMOUNT));
                if(AMOUNT<2){
                    mSub.setVisibility(View.INVISIBLE);
                }
            }
        });
        mOk.setOnClickListener(v -> buyProduct(product));

    }
    private void buyProduct(Product product){
        String orderId = String.valueOf(System.currentTimeMillis());
        String orderAddress = mOrderAddressText.getText().toString().trim();
        if(orderAddress.equals("") || orderAddress.isEmpty()){
            Toast.makeText(this, "Please write delivery location", Toast.LENGTH_SHORT).show();
            return;
        }
        if(Integer.parseInt(mAmountText.getText().toString()) == 0){
            Toast.makeText(this, "Please add item amount", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this,product.name+" will be added on amount of "+AMOUNT, Toast.LENGTH_SHORT).show();
        mRef.child(orderId).setValue(getOrderInfo(product,orderAddress))
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Toast.makeText(ProductDetailsActivity.this, "Order successful", Toast.LENGTH_SHORT).show();
                        mProductOrderText.setText("Order successful");
                        mSub.setVisibility(View.INVISIBLE);
                        AMOUNT = 1;
                        mAmountText.setText(""+AMOUNT);
                    }
                    else{
                        Toast.makeText(ProductDetailsActivity.this, "Failed : "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        mProductOrderText.setText("Order Failed");
                    }
                })
                .addOnFailureListener(e -> {
                    mDescriptionText.setText("Order Failed");
                    Toast.makeText(ProductDetailsActivity.this, "Failed : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private Map<String,String> getOrderInfo(Product product,String orderAddress){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        String date = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        String time = currentTime.format(calendar.getTime());

        Map<String,String> orderMap = new LinkedHashMap<>();
        orderMap.put(Order.Key.ORDER_PRODUCT_ID,product.id);
        orderMap.put(Order.Key.ORDER_USER_NAME,getSavedUserName());
        orderMap.put(Order.Key.ORDER_AMOUNT,String.valueOf(AMOUNT));
        orderMap.put(Order.Key.ORDER_PAYMENT_STATUS,"paid");
        orderMap.put(Order.Key.ORDER_SUBMIT_DATE,time+" at "+date);
        orderMap.put(Order.Key.ORDER_DELIVERY_DATE,"not mentioned");
        orderMap.put(Order.Key.ORDER_DELIVERY_LOCATION,orderAddress);

        return orderMap;
    }

    private String getSavedUserName(){
        Context context = this;
        SharedPreferences sharedPreferences = context.getSharedPreferences(User.Save.SAVED_STATUS_FILTER_KEY, Context.MODE_PRIVATE);
        return sharedPreferences.getString(User.Save.SAVED_NAME,"not found");
    }
}