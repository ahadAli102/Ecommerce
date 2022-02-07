package com.example.ecommerce.ui.user.activity;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ecommerce.App;
import com.example.ecommerce.R;
import com.example.ecommerce.model.User;
import com.example.ecommerce.ui.AccountActivity;
import com.example.ecommerce.ui.user.fragment.CartFragment;
import com.example.ecommerce.ui.user.fragment.HomeFragment;
import com.example.ecommerce.ui.user.fragment.SettingsFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private Toolbar mToolbar;
    private DrawerLayout mLayout;
    private ImageView mUserImageView;
    private TextView mUserNameText, mUserEmailText;

    private static final String TAG = "TAG:HomeAct";

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference mUserRef =  database.getReference("users_info");

    private ChildEventListener mUserListener  = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            if(snapshot.getKey().equals(getSavedName())){
                Log.d(TAG, "onChildAdded: called");
                resetInformation(snapshot);
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            if(snapshot.getKey().equals(getSavedName())){
                Log.d(TAG, "onChildChanged: called");
                resetInformation(snapshot);
            }
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

    private void resetInformation(DataSnapshot snapshot) {
        Context context = this;
        String url = snapshot.child(User.Key.DATABASE_IMAGE).getValue().toString();
        String name = snapshot.child(User.Key.DATABASE_NAME).getValue().toString();
        String email = snapshot.child(User.Key.DATABASE_EMAIL).getValue().toString();
        Glide.with(context).load(url).centerCrop()
                .placeholder(R.drawable.profile).into(mUserImageView);
        mUserNameText.setText(name);
        mUserEmailText.setText(email);
        Toast.makeText(context, "Name is : "+name, Toast.LENGTH_SHORT).show();

        Log.d(TAG, "resetInformation: name : "+name);
        Log.d(TAG, "resetInformation: image : "+url);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mToolbar = findViewById(R.id.drawerToolbarId);
        setSupportActionBar(mToolbar);
        mLayout = findViewById(R.id.homeDrawerLayoutId);
        NavigationView navigationView = findViewById(R.id.homeUserNavigationId);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mLayout, mToolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        mLayout.addDrawerListener(toggle);
        toggle.syncState();
        if (savedInstanceState==null){
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayoutId,new HomeFragment()).commit();
            mToolbar.setTitle("Products");
            navigationView.setCheckedItem(R.id.home_drawer_nav_home);
        }
        View holderView = navigationView.getHeaderView(0);
        mUserImageView = holderView.findViewById(R.id.homeUserImageViewId);
        mUserNameText = holderView.findViewById(R.id.homeUserNameId);
        mUserEmailText = holderView.findViewById(R.id.homeUserPasswordId);
        setUserInfoListener();

    }

    private void setUserInfoListener(){
        Log.d(TAG, "setUserInfoListener: called");
        mUserRef.addChildEventListener(mUserListener);

    }


    @Override
    public void onBackPressed() {
        if(mLayout.isDrawerOpen(GravityCompat.START)){
            mLayout.closeDrawer(GravityCompat.START);
        }
        else{
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        try{
            mUserRef.removeEventListener(mUserListener);
        }
        catch (Exception e){

        }
        super.onDestroy();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==R.id.home_drawer_nav_home){
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayoutId,new HomeFragment()).commit();
            mToolbar.setTitle("Products");
        }
        if (item.getItemId()==R.id.home_drawer_nav_cart){
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayoutId,new CartFragment()).commit();
            mToolbar.setTitle("Orders");
        }
        else if(item.getItemId()==R.id.drawer_nav_settings){
            mToolbar.setTitle("Profile");
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayoutId,new SettingsFragment()).commit();

        }
        else if(item.getItemId()==R.id.drawer_nav_logout){
            Toast.makeText(this, "logout", Toast.LENGTH_SHORT).show();
            logout();
        }
        mLayout.closeDrawer(GravityCompat.START);
        return true;
    }
    private void logout(){
        saveUserInfo(false,"","","","",User.Save.DEFAULT_IMAGE_LINK);
    }



    private void saveUserInfoName(String name){
        Context context = this;
        SharedPreferences sharedPreferences = context.getSharedPreferences(User.Save.SAVED_STATUS_FILTER_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putString(User.Save.SAVED_NAME,name);
        myEdit.apply();
    }
    private void saveUserInfoImage(String url){
        Context context = this;
        SharedPreferences sharedPreferences = context.getSharedPreferences(User.Save.SAVED_STATUS_FILTER_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putString(User.Save.SAVED_IMAGE,url);
        myEdit.putString(User.Save.SAVED_TYPE,User.Key.DATABASE_BUYER);
        myEdit.apply();
        Log.d(TAG, "saveUserInfo: "+sharedPreferences.getString(User.Save.SAVED_TYPE,"not found"));
    }
    private Map<String,String> getSavedUserNameInfo(String name){
        Context context = this;
        SharedPreferences sharedPreferences = context.getSharedPreferences(User.Save.SAVED_STATUS_FILTER_KEY, Context.MODE_PRIVATE);
        Map<String,String> userdataMap = new HashMap<>();
        userdataMap.put(User.Key.DATABASE_EMAIL, sharedPreferences.getString(User.Save.SAVED_EMAIL,"ahad15-12484@diu.edu.bd"));
        userdataMap.put(User.Key.DATABASE_NAME,name);
        userdataMap.put(User.Key.DATABASE_PASSWORD, sharedPreferences.getString(User.Save.SAVED_PASSWORD,"123456"));
        userdataMap.put(User.Key.DATABASE_USER_TYPE, sharedPreferences.getString(User.Save.SAVED_TYPE,User.Key.DATABASE_BUYER));

        userdataMap.put(User.Key.DATABASE_IMAGE, sharedPreferences.getString(User.Save.SAVED_IMAGE,User.Save.DEFAULT_IMAGE_LINK));
        return userdataMap;
    }
    private void saveUserInfo(boolean status, String name, String email, String password, String type,String image){
        Context context = this;
        SharedPreferences sharedPreferences = context.getSharedPreferences(User.Save.SAVED_STATUS_FILTER_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putBoolean(User.Save.SAVED_STATUS,status);
        myEdit.putString(User.Save.SAVED_NAME,name);
        myEdit.putString(User.Save.SAVED_EMAIL,email);
        myEdit.putString(User.Save.SAVED_PASSWORD,password);
        myEdit.putString(User.Save.SAVED_TYPE,type);
        myEdit.putString(User.Save.SAVED_IMAGE,image);
        /*Log.d(TAG, "saveUserInfo: "+name);
        Log.d(TAG, "saveUserInfo: "+email);
        Log.d(TAG, "saveUserInfo: "+password);
        Log.d(TAG, "saveUserInfo: "+type);
        Log.d(TAG, "saveUserInfo: "+image);*/
        myEdit.apply();
        App.resetUser();
        startActivity(new Intent(HomeActivity.this, AccountActivity.class));
        finish();
    }

    private String getSavedName(){
        Context context = this;
        SharedPreferences sharedPreferences = context.getSharedPreferences(User.Save.SAVED_STATUS_FILTER_KEY, Context.MODE_PRIVATE);
        return sharedPreferences.getString(User.Save.SAVED_NAME,"not found");
    }
}