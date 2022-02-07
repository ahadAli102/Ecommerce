package com.example.ecommerce.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ecommerce.App;
import com.example.ecommerce.R;
import com.example.ecommerce.model.User;
import com.example.ecommerce.ui.admin.activity.AdminActivity;
import com.example.ecommerce.ui.user.activity.HomeActivity;
import com.example.ecommerce.utils.EmailChecker;
import com.example.ecommerce.utils.SendMail;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class LoginFragment extends Fragment {
    private EditText mInputName, mInputEmailAddress, mInputPassword;
    private ProgressDialog loadingBar;
    private TextView mForgotText;
    private Button mLoginButton;

    private static final String TAG = "TAG:AccActLoginFrg";

    private final DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private ChildEventListener mForgotListener;
    private ValueEventListener mRootChildListener;
    private View mView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for requireContext() fragment
        mView = inflater.inflate(R.layout.fragment_login, container, false);
        mInputEmailAddress = mView.findViewById(R.id.loginEmailEditId);
        mInputPassword = mView.findViewById(R.id.loginPasswordEditId);
        mInputName = mView.findViewById(R.id.loginNameEditId);
        mLoginButton = mView.findViewById(R.id.loginLoginButtonId);
        mForgotText = mView.findViewById(R.id.loginForgotTextId);
        mLoginButton.setOnClickListener(v -> processLogin(v));
        mForgotText.setOnClickListener(v -> forgotPassword(mView));
        loadingBar = new ProgressDialog(requireContext());
        return mView;
    }
    public void processLogin(View view){
        String name = mInputName.getText().toString().trim();
        String email = mInputEmailAddress.getText().toString().trim();
        String password = mInputPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(requireContext(), "Please write your email...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password)) {
            Toast.makeText(requireContext(), "Please write your password...", Toast.LENGTH_SHORT).show();
        }
        else {
            hideKeyboard(view);
            loadingBar.setTitle("Login");
            loadingBar.setMessage("Please wait, while we are checking the credentials.");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            Toast.makeText(requireContext(), "All are ok", Toast.LENGTH_SHORT).show();

            checkLoginInformation(name, email, password);

            //mRootRef.push().getKey();
        }
    }

    private void checkLoginInformation(String name, String email, String password){
        try{
            Log.d(TAG, "checkLoginInformation: "+mRootRef.child(User.Key.DATABASE_USER));
        }
        catch (Exception e){
            Log.e(TAG, "onDataChange: ", e);
        }
        removeListeners();
        mRootChildListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(User.Key.DATABASE_USER).child(name).exists()){
                    try{
                        if(snapshot.child(User.Key.DATABASE_USER).child(name).child(User.Key.DATABASE_PASSWORD).getValue().equals(password)){
                            Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show();
                            goToActivity(name,email,password,
                                    snapshot.child(User.Key.DATABASE_USER).child(name).child(User.Key.DATABASE_USER_TYPE).getValue().toString(),
                                    snapshot.child(User.Key.DATABASE_USER).child(name).child(User.Key.DATABASE_IMAGE).getValue().toString());
                        }
                        else{
                            Toast.makeText(requireContext(), "Password in invalid", Toast.LENGTH_SHORT).show();
                        }
                        loadingBar.dismiss();
                    }
                    catch (NullPointerException e){
                        loadingBar.dismiss();
                        Log.e(TAG, "onDataChange: ", e);
                    }
                }
                else{
                    Toast.makeText(requireContext(), "Email does not Exist", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingBar.dismiss();
            }
        };
        removeListeners();
        mRootRef.addValueEventListener(mRootChildListener);
    }

    private void goToActivity(String name, String email, String password, String type, String image){
        CheckBox checkBox = mView.findViewById(R.id.loginCheckBoxId);
        saveUserInfo(checkBox.isChecked(),name,email,password,type,image);
        removeListeners();
        if(type.equalsIgnoreCase(User.Key.DATABASE_BUYER)){
            App.resetUser();
            startActivity(new Intent(requireActivity(), HomeActivity.class));
        }
        else{
            App.resetSeller();
            startActivity(new Intent(requireActivity(), AdminActivity.class));
        }
        requireActivity().finish();
    }
    private void saveUserInfo(boolean status, String name, String email, String password, String type,String image){
        Context context = requireContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences(User.Save.SAVED_STATUS_FILTER_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putBoolean(User.Save.SAVED_STATUS,status);
        myEdit.putString(User.Save.SAVED_NAME,name);
        myEdit.putString(User.Save.SAVED_EMAIL,email);
        myEdit.putString(User.Save.SAVED_PASSWORD,password);
        myEdit.putString(User.Save.SAVED_TYPE,type);
        myEdit.putString(User.Save.SAVED_IMAGE,image);
        Log.d(TAG, "saveUserInfo: "+status);
        Log.d(TAG, "saveUserInfo: "+name);
        Log.d(TAG, "saveUserInfo: "+email);
        Log.d(TAG, "saveUserInfo: "+password);
        Log.d(TAG, "saveUserInfo: "+type);
        Log.d(TAG, "saveUserInfo: "+image);
        myEdit.apply();
    }


    public void forgotPassword(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        final View dialogView = getLayoutInflater().inflate(R.layout.custom_username_layout, null);
        builder.setView(dialogView);
        EditText emailEditText = dialogView.findViewById(R.id.customUsernameEditTextId);
        Button button = dialogView.findViewById(R.id.customUsernameButtonId);
        ImageView closeImage = dialogView.findViewById(R.id.customUsernameImageId);
        TextView hintText = dialogView.findViewById(R.id.customUsernameHintId);
        TextView titleText = dialogView.findViewById(R.id.customUsernameTextView);
        titleText.setText("Forgot Password");
        emailEditText.setHint("Enter email");
        hintText.setVisibility(View.INVISIBLE);
        final AlertDialog dialog = builder.create();
        closeImage.setOnClickListener(v -> dialog.dismiss());
        final StringBuilder email = new StringBuilder();
        button.setOnClickListener(v -> {
            email.delete(0,email.length());
            email.append(emailEditText.getText().toString().trim());
            hintText.setVisibility(View.VISIBLE);
            if(!EmailChecker.validate(email.toString())){
                Toast.makeText(requireContext(), "Please enter valid email address", Toast.LENGTH_SHORT).show();
                return;
            }
            removeListeners();
            mRootRef.child(User.Key.DATABASE_USER).addChildEventListener(mForgotListener);

        });
        dialog.show();
        mForgotListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d(TAG, "onChildAdded: email: "+snapshot.child(User.Key.DATABASE_EMAIL).getValue().toString());
                Log.d(TAG, "onChildAdded: enter email: "+email.toString());
                if(snapshot.child(User.Key.DATABASE_EMAIL).getValue().toString().equals(email.toString())){
                    dialog.dismiss();
                    Log.d(TAG, "onChildAdded: enter");
                    String message = "Thanks for using our service\nYour account information are given below"+
                            "\nYour user name : "+snapshot.getKey()+"\nYour email: "+email.toString()+"\n"
                            +"Password: "+snapshot.child(User.Key.DATABASE_PASSWORD).getValue().toString();
                    new Thread(() -> {
                        SendMail sm = new SendMail(requireContext(), email.toString(), "E-Commerce password recovery", message);
                        sm.send();
                        requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Account information is sent to your email", Toast.LENGTH_SHORT).show());
                    }).start();
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

    }
    private void removeListeners(){
        try{
            mRootRef.child(User.Key.DATABASE_USER).removeEventListener(mForgotListener);
        }catch (Exception e){
            Log.e(TAG, "forgotPassword: ", e);
        }
        try{
            mRootRef.removeEventListener(mRootChildListener);
        }catch (Exception e){
            Log.e(TAG, "forgotPassword: ", e);
        }
    }
    public void hideKeyboard(View view) {
        try {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch(Exception ignored) {
        }
    }
}