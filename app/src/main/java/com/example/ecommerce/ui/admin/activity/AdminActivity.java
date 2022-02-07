package com.example.ecommerce.ui.admin.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
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
import com.example.ecommerce.ui.admin.fragment.AdminAddProductFragment;
import com.example.ecommerce.ui.admin.fragment.AdminOrderFragment;
import com.example.ecommerce.ui.admin.fragment.AdminProductsFragment;
import com.example.ecommerce.ui.admin.fragment.AdminSettingsFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class AdminActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private static final String TAG = "TAG:AdAct";

    private Toolbar mToolbar;
    private DrawerLayout mLayout;
    private ImageView mUserImageView;
    private TextView mUserNameText,mUserEmailText;
    private RecyclerView mRecycler;
    private ProgressDialog mLoadingBar;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference mProductRef = database.getReference("product_info");
    private StorageReference mStorage = FirebaseStorage.getInstance().getReference("user_image");
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
        try{
            Glide.with(context).load(url).centerCrop()
                    .placeholder(R.drawable.profile).into(mUserImageView);
        }
        catch(Exception e){

        }
        mUserNameText.setText(name);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        mToolbar = findViewById(R.id.adminDrawerToolbarId);
        setSupportActionBar(mToolbar);
        mLayout = findViewById(R.id.adminDrawerLayoutId);
        NavigationView navigationView = findViewById(R.id.adminUserNavigationId);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mLayout, mToolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        mLayout.addDrawerListener(toggle);
        toggle.syncState();
        if (savedInstanceState==null){
            getSupportFragmentManager().beginTransaction().replace(R.id.adminFrameLayoutId,new AdminProductsFragment()).commit();
            navigationView.setCheckedItem(R.id.admin_drawer_nav_products);
        }
        View holderView = navigationView.getHeaderView(0);
        mUserImageView = holderView.findViewById(R.id.adminUserImageViewId);
        mUserNameText = holderView.findViewById(R.id.adminUserNameId);
        mUserEmailText = holderView.findViewById(R.id.adminUserMailId);

        mLoadingBar = new ProgressDialog(this);
        setUserInfoListener();

        /*mUserImageView.setOnClickListener(v -> setProfilePicture());
        mUserNameText.setOnClickListener(v -> setUserName());*/
    }

    private void setUserInfoListener(){
        mUserRef.addChildEventListener(mUserListener);

    }
    private void setUserName(){
        Toast.makeText(this, "set user name", Toast.LENGTH_SHORT).show();
    }

    private void saveUserInfoImage(String url){
        Context context = this;
        SharedPreferences sharedPreferences = context.getSharedPreferences(User.Save.SAVED_STATUS_FILTER_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putString(User.Save.SAVED_IMAGE,url);
        myEdit.putString(User.Save.SAVED_TYPE,User.Key.DATABASE_SELLER);
        myEdit.apply();
        Log.d(TAG, "saveUserInfo: "+sharedPreferences.getString(User.Save.SAVED_TYPE,"not found"));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==R.id.admin_drawer_nav_products){
            getSupportFragmentManager().beginTransaction().replace(R.id.adminFrameLayoutId,new AdminProductsFragment()).commit();
            mToolbar.setTitle("Product");
        }
        else if (item.getItemId()==R.id.admin_drawer_nav_add_product){
            getSupportFragmentManager().beginTransaction().replace(R.id.adminFrameLayoutId,new AdminAddProductFragment()).commit();
            mToolbar.setTitle("Add Product");
        }
        else if (item.getItemId()==R.id.admin_drawer_nav_orders){
            getSupportFragmentManager().beginTransaction().replace(R.id.adminFrameLayoutId,new AdminOrderFragment()).commit();
            mToolbar.setTitle("Orders");
        }
        else if(item.getItemId()==R.id.admin_drawer_nav_settings){
            getSupportFragmentManager().beginTransaction().replace(R.id.adminFrameLayoutId,new AdminSettingsFragment()).commit();
            mToolbar.setTitle("Profile");
        }
        else if(item.getItemId()==R.id.admin_drawer_nav_logout){
            Toast.makeText(this, "logout", Toast.LENGTH_SHORT).show();
            logout();
        }
        mLayout.closeDrawer(GravityCompat.START);
        return true;
    }
    private void logout(){
        saveUserInfo(false,"","","","",User.Save.DEFAULT_IMAGE_LINK);
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
        Log.d(TAG, "saveUserInfo: "+name);
        Log.d(TAG, "saveUserInfo: "+email);
        Log.d(TAG, "saveUserInfo: "+password);
        Log.d(TAG, "saveUserInfo: "+type);
        Log.d(TAG, "saveUserInfo: "+image);
        myEdit.apply();
        App.resetSeller();
        startActivity(new Intent(AdminActivity.this, AccountActivity.class));
        finish();

    }
    private String getSavedName(){
        Context context = this;
        SharedPreferences sharedPreferences = context.getSharedPreferences(User.Save.SAVED_STATUS_FILTER_KEY, Context.MODE_PRIVATE);
        return sharedPreferences.getString(User.Save.SAVED_NAME,"not found");
    }
}