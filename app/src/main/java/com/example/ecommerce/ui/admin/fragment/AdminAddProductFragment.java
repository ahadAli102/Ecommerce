package com.example.ecommerce.ui.admin.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ecommerce.App;
import com.example.ecommerce.R;
import com.example.ecommerce.adapter.seller.CategoryAdapter;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AdminAddProductFragment extends Fragment {
    private ImageView mProductImage;
    private Uri mImageUri;
    private TextView mNameText, mDescriptionText, mPriceText, mProductCategoryText;
    private Spinner mTypeSpinner;
    private Button mAddButton;
    private View mView;
    private ProgressDialog mLoadingBar;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference mRef = database.getReference("product_info");
    private StorageReference mStorage = FirebaseStorage.getInstance().getReference("product_image");


    private static final String TAG = "TAG:AdActAddProFr";

    private ActivityResultLauncher<Intent> mImageResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    Intent data = result.getData();
                    if (result.getResultCode() == Activity.RESULT_OK && data != null) {
                        mImageUri = data.getData();
                        Glide.with(requireContext()).load(mImageUri).centerCrop().into(mProductImage);
                    }
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for requireContext() fragment
        mView = inflater.inflate(R.layout.fragment_admin_add_product, container, false);
        init();
        return mView;
    }

    private void init() {
        mNameText = mView.findViewById(R.id.addProductFragmentNameTextId);
        mProductCategoryText = mView.findViewById(R.id.addProductFragmentCategoryTextId);
        mDescriptionText = mView.findViewById(R.id.addProductFragmentDescriptionTextId);
        mPriceText = mView.findViewById(R.id.addProductFragmentPriceTextId);
        mTypeSpinner = mView.findViewById(R.id.addProductFragmentTypeSpinnerId);
        mProductImage = mView.findViewById(R.id.addProductFragmentImageId);
        mAddButton = mView.findViewById(R.id.addProductFragmentAddButtonId);
        initSpinnerProperty();

        mAddButton.setOnClickListener(v -> validateProductData());
        mProductImage.setOnClickListener(v -> openGallery());
        mLoadingBar = new ProgressDialog(requireContext());
    }

    private void initSpinnerProperty() {
        CategoryAdapter adapter = new CategoryAdapter(requireContext());
        mTypeSpinner.setAdapter(adapter);
        mTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mProductCategoryText.setText(adapter.getName(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void openGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        mImageResultLauncher.launch(galleryIntent);
    }

    private void validateProductData() {
        String name = mNameText.getText().toString().trim();
        String description = mDescriptionText.getText().toString().trim();
        String price = mPriceText.getText().toString().trim();
        String category = mProductCategoryText.getText().toString().trim();


        if (mImageUri == null) {
            Toast.makeText(requireContext(), "Product image is mandatory...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(description)) {
            Toast.makeText(requireContext(), "Please write product description...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(price)) {
            Toast.makeText(requireContext(), "Please write product Price...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(name)) {
            Toast.makeText(requireContext(), "Please write product name...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(category)) {
            Toast.makeText(requireContext(), "Please select product category", Toast.LENGTH_SHORT).show();
        }
        else {
            storeProductInformation(name, description, price, category);
        }

    }

    private void storeProductInformation(String name, String description, String price, String category) {
        mLoadingBar.setTitle("Add New Product");
        mLoadingBar.setMessage("Dear Admin, please wait while we are adding the new product.");
        mLoadingBar.setCanceledOnTouchOutside(false);
        mLoadingBar.show();
        final String currentTimeMs = "" + System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        String currentDate = dateFormat.format(calendar.getTime());

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss a");
        String currentTime = timeFormat.format(calendar.getTime());
        final StorageReference ref = mStorage.child(currentTimeMs + ".jpg");
        ref.putFile(mImageUri)
                .addOnCompleteListener(task -> ref.getDownloadUrl()
                        .addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                try {
                                    final String downloadUrl = task1.getResult().toString();
                                    storeToRealTimeDatabase(currentTimeMs, downloadUrl, name, description, category,
                                            price, currentTime, currentDate);
                                } catch (NullPointerException e) {
                                    mLoadingBar.dismiss();
                                    Toast.makeText(requireContext(), "Failed: Not uploaded", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                mLoadingBar.dismiss();
                                Toast.makeText(requireContext(), "Failed: Task is not successful", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            mLoadingBar.dismiss();
                            Toast.makeText(requireContext(), "Failed: " + e.toString(), Toast.LENGTH_SHORT).show();
                        }))
                .addOnFailureListener(e -> {
                    mLoadingBar.dismiss();
                    Toast.makeText(requireContext(), "Failed: " + e.toString(), Toast.LENGTH_SHORT).show();
                });
    }

    private void storeToRealTimeDatabase(String currentTimeMs, String imageUrl,
                                         String mPName, String mDescription, String mCategoryName, String mPrice,
                                         String mSavedCurrentTime, String mSavedCurrentDate) {
        String sellerName = getSavedName();

        Log.d(TAG, "storeToRealTimeDatabase: name : "+mPName+" category : "+mCategoryName);

        Map<String, String> data = new HashMap<>();
        data.put(Product.Key.DATABASE_PRODUCT_NAME, mPName);
        data.put(Product.Key.DATABASE_PRODUCT_SELLER_NAME, sellerName);
        data.put(Product.Key.DATABASE_PRODUCT_IMAGE_URL, imageUrl);
        data.put(Product.Key.DATABASE_PRODUCT_DESCRIPTION, mDescription);
        data.put(Product.Key.DATABASE_PRODUCT_CATEGORY, mCategoryName);
        data.put(Product.Key.DATABASE_PRODUCT_PRICE, mPrice);
        data.put(Product.Key.DATABASE_PRODUCT_DATE, mSavedCurrentTime + " at " + mSavedCurrentDate);
        data.put(Product.Key.DATABASE_PRODUCT_VISIBILITY, Product.Key.DATABASE_PRODUCT_VISIBLE);

        mRef.child(currentTimeMs).setValue(data)
                .addOnCompleteListener(task -> {
                    mLoadingBar.dismiss();
                    if (task.isSuccessful()) {
                        App.Seller.FIRST_ADMIN_PRODUCT_CHANGED = true;
                        Toast.makeText(requireContext(), "Product is added successfully..", Toast.LENGTH_SHORT).show();
                    } else {
                        String message = task.getException().toString();
                        Toast.makeText(requireContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private String getSavedName() {
        String name = null;
        Context context = requireContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences(User.Save.SAVED_STATUS_FILTER_KEY, Context.MODE_PRIVATE);
        try {
            if (sharedPreferences.getBoolean(User.Save.SAVED_STATUS, false)) {
                name = sharedPreferences.getString(User.Save.SAVED_NAME, "no buyer");
            }
        } catch (Exception e) {
            Log.e(TAG, "onCreate: ", e);
        }
        return name;
    }
}