package com.example.ecommerce.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.util.Log;

import com.example.ecommerce.R;
import com.example.ecommerce.adapter.AuthenticationPagerAdapter;

public class AccountActivity extends AppCompatActivity {
    private static final String TAG = "TAG:AccountAct";
    ViewPager2 myViewPager2;
    AuthenticationPagerAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        Log.d(TAG, "onCreate: account");
        myViewPager2 = findViewById(R.id.accountViewPager);

        myAdapter = new AuthenticationPagerAdapter(getSupportFragmentManager(), getLifecycle());
        myViewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

        myViewPager2.setAdapter(myAdapter);

        myViewPager2.setPageTransformer(new MarginPageTransformer(1500));
    }
}