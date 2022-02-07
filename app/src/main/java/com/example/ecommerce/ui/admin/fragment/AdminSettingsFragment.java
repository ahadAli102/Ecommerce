package com.example.ecommerce.ui.admin.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ecommerce.R;
import com.example.ecommerce.model.User;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdminSettingsFragment extends Fragment {
    private static final String TAG = "TAG:HomeActSetFr";

    private CircleImageView mProfileImage;
    private ImageView mEditNameImage;
    private TextView mNameText,mEmailText,mChangePasswordText;
    private View mView;

    private ProgressDialog mLoadingBar;
    private String mUserPassword;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference mUserRef =  database.getReference("users_info");
    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference("user_image");

    private FirebaseStorage mStorage = FirebaseStorage.getInstance();

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
        Context context = requireContext();
        String url = snapshot.child(User.Key.DATABASE_IMAGE).getValue().toString();
        String name = snapshot.child(User.Key.DATABASE_NAME).getValue().toString();
        String password = snapshot.child(User.Key.DATABASE_PASSWORD).getValue().toString();
        String email = snapshot.child(User.Key.DATABASE_EMAIL).getValue().toString();
        Glide.with(context).load(url).centerCrop()
                .placeholder(R.drawable.profile).into(mProfileImage);
        mNameText.setText(name);
        mEmailText.setText(email);
        mUserPassword = password;
        saveUserInfoImage(url);

        Log.d(TAG, "resetInformation: name : "+name);
        Log.d(TAG, "resetInformation: password : "+password);
        Log.d(TAG, "resetInformation: image : "+url);
    }

    private ActivityResultLauncher<Intent> mImageResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {

                Intent data = result.getData();
                if (result.getResultCode() == Activity.RESULT_OK  &&  data!=null) {
                    Uri imageUri = data.getData();
                    try{
                        deleteOldImage(imageUri);
                    }
                    catch (Exception e){
                        Toast.makeText(requireContext(), "Failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_admin_settings, container, false);
        init();
        setUserInfoListener();
        return mView;
    }
    private void init(){
        mLoadingBar = new ProgressDialog(requireContext());
        mLoadingBar.setTitle("Uploading image");
        mLoadingBar.setMessage("Please wait while uploading image to server");
        mLoadingBar.setCanceledOnTouchOutside(false);
        mProfileImage = mView.findViewById(R.id.adminSettingsProfileImageViewId);
        mEditNameImage = mView.findViewById(R.id.adminSettingsFragmentNameEditId);
        mNameText = mView.findViewById(R.id.adminSettingsFragmentNameId);
        mEmailText = mView.findViewById(R.id.adminSettingsFragmentEmailId);
        mChangePasswordText = mView.findViewById(R.id.adminSettingsFragmentChangePasswordId);
        mProfileImage.setOnClickListener(v -> setProfilePicture());
        mChangePasswordText.setOnClickListener(v -> changePassword());
        mEditNameImage.setOnClickListener(v -> changeUserName());
        mNameText.setOnClickListener(v -> changeUserName());
    }
    @SuppressLint("SetTextI18n")
    private void changeUserName(){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        final View dialogView = getLayoutInflater().inflate(R.layout.custom_username_layout, null);
        builder.setView(dialogView);
        EditText nameEditText = dialogView.findViewById(R.id.customUsernameEditTextId);
        Button button = dialogView.findViewById(R.id.customUsernameButtonId);
        ImageView closeImage = dialogView.findViewById(R.id.customUsernameImageId);
        TextView hintText = dialogView.findViewById(R.id.customUsernameHintId);
        TextView titleText = dialogView.findViewById(R.id.customUsernameTextView);
        titleText.setText("Update Name");
        nameEditText.setHint("Enter name");
        hintText.setVisibility(View.INVISIBLE);
        nameEditText.append(mNameText.getText().toString());
        final AlertDialog dialog = builder.create();
        closeImage.setOnClickListener(v -> dialog.dismiss());
        button.setOnClickListener(v -> {
            String password = nameEditText.getText().toString().trim();
            hintText.setVisibility(View.VISIBLE);
            hintText.setText("checking wait!");
            DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
            mRootRef.child(User.Key.DATABASE_USER).child(getSavedName()).child(User.Key.DATABASE_NAME)
                    .setValue(password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            Log.d(TAG, "changeUserName: name changes");
                            Toast.makeText(requireContext(), "Name changed", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    });
        });
        dialog.show();
    }

    private void changePassword(){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        final View dialogView = getLayoutInflater().inflate(R.layout.custom_username_layout, null);
        builder.setView(dialogView);
        EditText passwordEditText = dialogView.findViewById(R.id.customUsernameEditTextId);
        Button button = dialogView.findViewById(R.id.customUsernameButtonId);
        ImageView closeImage = dialogView.findViewById(R.id.customUsernameImageId);
        TextView hintText = dialogView.findViewById(R.id.customUsernameHintId);
        TextView titleText = dialogView.findViewById(R.id.customUsernameTextView);
        titleText.setText("Update Password");
        passwordEditText.setHint("Enter password");
        hintText.setVisibility(View.INVISIBLE);
        passwordEditText.append(mUserPassword);
        final AlertDialog dialog = builder.create();
        closeImage.setOnClickListener(v -> dialog.dismiss());
        button.setOnClickListener(v -> {
            String password = passwordEditText.getText().toString().trim();
            hintText.setVisibility(View.VISIBLE);
            hintText.setText("checking wait!");
            DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
            mRootRef.child(User.Key.DATABASE_USER).child(getSavedName()).child(User.Key.DATABASE_PASSWORD)
                    .setValue(password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            Log.d(TAG, "changePassword: password changes");
                            Toast.makeText(requireContext(), "Password changed", Toast.LENGTH_SHORT).show();
                            saveUserInfoPassword(password);
                            dialog.dismiss();
                        }
                    });
        });
        dialog.show();
    }

    private Map<String,String> getSavedUserInfo(String name){
        Context context = requireContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences(User.Save.SAVED_STATUS_FILTER_KEY, Context.MODE_PRIVATE);
        Map<String,String> userdataMap = new HashMap<>();
        userdataMap.put(User.Key.DATABASE_EMAIL, sharedPreferences.getString(User.Save.SAVED_EMAIL,"ahad15-12484@diu.edu.bd"));
        userdataMap.put(User.Key.DATABASE_NAME,name);
        userdataMap.put(User.Key.DATABASE_PASSWORD, sharedPreferences.getString(User.Save.SAVED_PASSWORD,"123456"));
        userdataMap.put(User.Key.DATABASE_USER_TYPE, sharedPreferences.getString(User.Save.SAVED_TYPE,User.Key.DATABASE_BUYER));

        userdataMap.put(User.Key.DATABASE_IMAGE, sharedPreferences.getString(User.Save.SAVED_IMAGE,User.Save.DEFAULT_IMAGE_LINK));
        return userdataMap;
    }

    private void setProfilePicture(){
        Toast.makeText(requireContext(), "Select", Toast.LENGTH_SHORT).show();
        openGallery();
    }
    private void openGallery() {
        Log.d(TAG, "openGallery: called");
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        mImageResultLauncher.launch(galleryIntent);
    }

    private void saveToStorage(Uri image){
        Log.d(TAG, "saveToStorage: called");
        mStorageRef.child(getSavedName()).putFile(image)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        mStorageRef.child(getSavedName())
                                .getDownloadUrl()
                                .addOnCompleteListener(task1 -> {
                                    if(task1.isSuccessful()){
                                        mLoadingBar.dismiss();
                                        saveIntoDatabase(task1.getResult().toString());
                                    }
                                })
                                .addOnFailureListener(e ->{
                                    mLoadingBar.dismiss();
                                    Toast.makeText(requireContext(), "Failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e ->{
                    mLoadingBar.dismiss();
                    Toast.makeText(requireContext(), "Failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void deleteOldImage(Uri image){
        mLoadingBar.show();
        Log.d(TAG, "deleteOldImage: called");
        Context context = requireContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences(User.Save.SAVED_STATUS_FILTER_KEY, Context.MODE_PRIVATE);
        String oldUrl = sharedPreferences.getString(User.Save.SAVED_IMAGE,User.Save.DEFAULT_IMAGE_LINK);
        if(oldUrl.equals(User.Save.DEFAULT_IMAGE_LINK)){
            saveToStorage(image);
        }
        else{
            StorageReference deleteRef = mStorage.getReferenceFromUrl(oldUrl);
            deleteRef.delete().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    saveToStorage(image);
                }
                else {
                    mLoadingBar.dismiss();
                    Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }
    private void saveIntoDatabase(String url){
        Log.d(TAG, "saveIntoDatabase: Url:"+url);
        DatabaseReference userDb = database.getReference("users_info");
        userDb.child(getSavedName()).child(User.Key.DATABASE_IMAGE)
                .setValue(url)
                .addOnCompleteListener(task1 -> {
                    mLoadingBar.dismiss();
                    if(task1.isSuccessful()){
                        Toast.makeText(requireContext(), "Image Successful", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(requireContext(), "FAILED", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    mLoadingBar.dismiss();
                    Toast.makeText(requireContext(), "FAILED: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                });



    }

    private void setUserInfoListener(){
        Log.d(TAG, "setUserInfoListener: called");
        Context context = requireContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences(User.Save.SAVED_STATUS_FILTER_KEY, Context.MODE_PRIVATE);
        mNameText.setText(sharedPreferences.getString(User.Save.SAVED_NAME,"not found"));
        mUserRef.addChildEventListener(mUserListener);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try{
            mUserRef.removeEventListener(mUserListener);
        }
        catch (Exception e){
            Log.e(TAG, "onDestroy: ",e );
        }
    }

    private void saveUserInfoImage(String url){
        //Log.d(TAG, "saveUserInfoImage: url : "+url);
        Context context = requireContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences(User.Save.SAVED_STATUS_FILTER_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putString(User.Save.SAVED_IMAGE,url);
        myEdit.apply();
        //Log.d(TAG, "saveUserInfoImage: name: "+sharedPreferences.getString(User.Save.SAVED_NAME,"not found"));
        //Log.d(TAG, "saveUserInfoImage: image: "+sharedPreferences.getString(User.Save.SAVED_IMAGE,"not found"));
    }
    private void saveUserInfoPassword(String password){
        Context context = requireContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences(User.Save.SAVED_STATUS_FILTER_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putString(User.Save.SAVED_PASSWORD,password);
        myEdit.apply();
       /* Log.d(TAG, "saveUserInfoImage: name: "+sharedPreferences.getString(User.Save.SAVED_NAME,"not found"));
        Log.d(TAG, "saveUserInfoImage: type: "+sharedPreferences.getString(User.Save.SAVED_TYPE,"not found"));
        Log.d(TAG, "saveUserInfoImage: password: "+sharedPreferences.getString(User.Save.SAVED_PASSWORD,"not found"));
        Log.d(TAG, "saveUserInfoImage: image: "+sharedPreferences.getString(User.Save.SAVED_IMAGE,"not found"));*/
    }
    private String getSavedName(){
        Context context = requireContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences(User.Save.SAVED_STATUS_FILTER_KEY, Context.MODE_PRIVATE);
        return sharedPreferences.getString(User.Save.SAVED_NAME,"not found");
    }
}