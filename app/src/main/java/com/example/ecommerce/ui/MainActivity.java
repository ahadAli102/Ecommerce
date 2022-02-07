package com.example.ecommerce.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.ecommerce.R;
import com.example.ecommerce.model.User;
import com.example.ecommerce.ui.admin.activity.AdminActivity;
import com.example.ecommerce.ui.user.activity.HomeActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "TAG:MainAct";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Context context = this;
        SharedPreferences sharedPreferences = context.getSharedPreferences(User.Save.SAVED_STATUS_FILTER_KEY, Context.MODE_PRIVATE);
        Log.d(TAG, "onCreate: "+sharedPreferences.getString(User.Save.SAVED_TYPE,null));
        boolean save = sharedPreferences.getBoolean(User.Save.SAVED_STATUS,false);
        String type  = sharedPreferences.getString(User.Save.SAVED_TYPE,null);
        if(save){
            if(type.equalsIgnoreCase(User.Key.DATABASE_BUYER)){
                startActivity(new Intent(MainActivity.this, HomeActivity.class));
            }
            else if(type.equalsIgnoreCase("seller")){
                startActivity(new Intent(MainActivity.this, AdminActivity.class));
            }
            else{
                startActivity(new Intent(MainActivity.this, AccountActivity.class));
            }
        }
        else{
            startActivity(new Intent(MainActivity.this, AccountActivity.class));
        }
        finish();
    }


    public void register(View view) {
        startActivity(new Intent(MainActivity.this, AccountActivity.class));
    }

    public void login(View view) {
        startActivity(new Intent(MainActivity.this, AccountActivity.class));
    }
}