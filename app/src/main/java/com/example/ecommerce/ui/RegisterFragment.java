package com.example.ecommerce.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ecommerce.R;
import com.example.ecommerce.model.User;
import com.example.ecommerce.utils.EmailChecker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class RegisterFragment extends Fragment {
    private Button mCreateAccountButton;
    private EditText mInputName, mInputEmailAddress, mInputPassword,mReInputPassword;
    private ProgressDialog loadingBar;

    public static final String DEFAULT_IMAGE_LINK = "https://www.freeiconspng.com/thumbs/profile-icon-png/profile-icon-9.png";

    private static final String TAG = "TAG:AccActRegFrg";

    private final DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    
    private View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_register, container, false);
        mCreateAccountButton = mView.findViewById(R.id.registerButtonId);
        mInputName = mView.findViewById(R.id.registerUsernameInput);
        mInputPassword = mView.findViewById(R.id.registerPasswordInputId);
        mReInputPassword = mView.findViewById(R.id.registerRePasswordInputId);
        mInputEmailAddress = mView.findViewById(R.id.registerEmailInputId);
        loadingBar = new ProgressDialog(requireContext());
        mCreateAccountButton.setOnClickListener(view -> createAccount(view));
        
        return mView;
    }
    private void createAccount(View view) {
        String name = mInputName.getText().toString().trim();
        String email = mInputEmailAddress.getText().toString().trim();
        String password = mInputPassword.getText().toString().trim();
        String rePassword = mReInputPassword.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(requireContext(), "Please write your name...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(email)) {
            Toast.makeText(requireContext(), "Please write your email...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password)) {
            Toast.makeText(requireContext(), "Please write your password...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password)) {
            Toast.makeText(requireContext(), "Please re write your password...", Toast.LENGTH_SHORT).show();
        }
        else if(!EmailChecker.validate(email)){
            Toast.makeText(requireContext(), "Please enter valid email...", Toast.LENGTH_SHORT).show();
        }
        else if(!password.equals(rePassword)){
            Toast.makeText(requireContext(), "Passwords don't match...", Toast.LENGTH_SHORT).show();
        }
        else {
            hideKeyboard(view);
            loadingBar.setTitle("Create Account");
            loadingBar.setMessage("Please wait, while we are checking the credentials.");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            Toast.makeText(requireContext(), "All are ok", Toast.LENGTH_SHORT).show();

            registerIntoDatabase(name, email, password);

        }
    }



    private void registerIntoDatabase(final String name, final String email, final String password) {
        try{
            Log.d(TAG, "registerIntoDatabase: "+mRootRef.child(User.Key.DATABASE_USER).child(name));
        }
        catch (Exception e){
            Log.e(TAG, "onDataChange: ", e);
        }
        mRootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (!(dataSnapshot.child(User.Key.DATABASE_USER).child(name).exists()))
                {
                    HashMap<String, Object> userdataMap = new HashMap<>();
                    userdataMap.put(User.Key.DATABASE_NAME, name);
                    userdataMap.put(User.Key.DATABASE_USER_TYPE, User.Key.DATABASE_BUYER);
                    userdataMap.put(User.Key.DATABASE_EMAIL, email);
                    userdataMap.put(User.Key.DATABASE_PASSWORD, password);
                    userdataMap.put(User.Key.DATABASE_IMAGE, DEFAULT_IMAGE_LINK);
                    mRootRef.child(User.Key.DATABASE_USER).child(name).updateChildren(userdataMap)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(requireContext(), "Congratulations, your account has been created.", Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }
                                else {
                                    loadingBar.dismiss();
                                    Toast.makeText(requireContext(), "Network Error: Please try again after some time...", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                else {
                    Toast.makeText(requireContext(), "requireContext() " + name + " already exists.", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                    Toast.makeText(requireContext(), "Please try again with another name.", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                loadingBar.dismiss();
            }
        });
    }
    public void hideKeyboard(View view) {
        try {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch(Exception ignored) {
        }
    }
}