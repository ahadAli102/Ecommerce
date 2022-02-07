package com.example.ecommerce.ui.user.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.ecommerce.App;
import com.example.ecommerce.R;
import com.example.ecommerce.adapter.ProductAdapter;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.ui.user.activity.ProductDetailsActivity;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {

    private static final String TAG = "TAG:HomeActHomeFr";
    private ProgressDialog mLoadingBar;

    private List<Product> mCapProducts,mGlassProduct,mHeadphoneProduct;
    private List<Product> mTShirtProduct,mLaptopProduct,mMobileProduct,mShoeProduct,mSweaterProduct,mWatchProduct;
    
    private ProductAdapter mCapAdapter,mGlassAdapter,mHeadphoneAdapter;
    private ProductAdapter mTShirtAdapter,mLaptopAdapter,mMobileAdapter,mShoeAdapter,mSweaterAdapter,mWatchAdapter;
    
    private RecyclerView mCapRecycler,mGlassRecycler,mHeadphoneRecycler;
    private RecyclerView mTShirtRecycler,mLaptopRecycler,mMobileRecycler;
    private RecyclerView mShoeRecycler,mSweaterRecycler,mWatchRecycler;

    private SnapHelper mCapSnap,mGlassSnap,mHeadphoneSnap;
    private SnapHelper mTShirtSnap,mLaptopSnap,mMobileSnap,mShoeSnap,mSweaterSnap,mWatchSnap;

    private ConstraintLayout mCapLayout, mGlassLayout, mHeadphoneLayout;
    private ConstraintLayout mTShirtLayout, mLaptopLayout, mMobileLayout;
    private ConstraintLayout mShoeLayout, mSweaterLayout, mWatchLayout;
    
    private ImageView mCapImage,mGlassImage,mHeadphoneImage;
    private ImageView mTShirtImage,mLaptopImage,mMobileImage;
    private ImageView mShoeImage,mSweaterImage,mWatchImage;

    private SearchView mSearchView;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference mProductRef = database.getReference("product_info");

    private Query mProductQuery;
    private ChildEventListener mProductChildEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            addChild(snapshot);
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            addChild(snapshot);
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

    private View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d(TAG, "onCreateView: called");
        mView = inflater.inflate(R.layout.fragment_home, container, false);
        mLoadingBar = new ProgressDialog(requireContext());
        //setRecyclerView();
        initTextImage();
        setProducts();
        return mView;
    }

    private void addChild(DataSnapshot snapshot){
        try{
            if(snapshot.child(Product.Key.DATABASE_PRODUCT_VISIBILITY).getValue().toString()
                    .equals(Product.Key.DATABASE_PRODUCT_VISIBLE)){
                Product product = new Product();
                product.id = snapshot.getKey();
                Log.d(TAG, "onChildAdded: "+snapshot.getKey());
                product.name = snapshot.child("name").getValue().toString();
                product.sellerName = snapshot.child("sellerName").getValue().toString();
                product.imageUrl = snapshot.child("imageUrl").getValue().toString();
                product.description = snapshot.child("description").getValue().toString();
                product.category = snapshot.child("category").getValue().toString();
                product.price = snapshot.child("price").getValue().toString();
                product.date = snapshot.child("date").getValue().toString();
                addProduct(product);
                App.User.mSellerMap.put(product.id,product.sellerName);
                App.User.mImageMap.put(product.id,product.imageUrl);
                App.User.mProductName.put(product.id,product.name);
                mLoadingBar.dismiss();
            }
        }
        catch (Exception e){
            Log.e(TAG, "onChildAdded: ", e);
        }
    }

    private void setProducts(){
        initAllProduct();
        if(App.User.USER_FIRST_RUN){
            initRecyclerView();
            loadProducts();
            //setListInAdapter();
            App.User.USER_FIRST_RUN = false;
        }
        else{
            initRecyclerView();
            addAllProduct();
        }
    }
    private void loadProducts(){
        Log.d(TAG, "loadProducts: called");
        mLoadingBar.setTitle("Getting data");
        mLoadingBar.setMessage("Please wait while getting data from server");
        mLoadingBar.setCanceledOnTouchOutside(false);
        mLoadingBar.show();
        /*mProductQuery = mProductRef.orderByChild(Product.Key.DATABASE_PRODUCT_VISIBLE).equalTo(Product.Key.DATABASE_PRODUCT_VISIBLE,Product.Key.DATABASE_PRODUCT_VISIBLE);
        Log.d(TAG, "loadProducts: query : "+mProductQuery.toString());
        mProductQuery.addChildEventListener(mProductChildEventListener);*/
        mProductRef.addChildEventListener(mProductChildEventListener);
    }
    private void initRecyclerView(){
        Log.d(TAG, "setRecyclerView: called");
        mSearchView = mView.findViewById(R.id.userHomeSearchViewId);
        mCapRecycler = mView.findViewById(R.id.userHomeCapRecyclerViewId);
        mGlassRecycler = mView.findViewById(R.id.userHomeGlassRecyclerViewId);
        mHeadphoneRecycler = mView.findViewById(R.id.userHomeHeadPhoneRecyclerViewId);
        mLaptopRecycler = mView.findViewById(R.id.userHomeLaptopRecyclerViewId);
        mMobileRecycler = mView.findViewById(R.id.userHomeMobileRecyclerViewId);
        mShoeRecycler = mView.findViewById(R.id.userHomeShoeRecyclerViewId);
        mSweaterRecycler = mView.findViewById(R.id.userHomeSweaterRecyclerViewId);
        mTShirtRecycler = mView.findViewById(R.id.userHomeTShirtRecyclerViewId);
        mWatchRecycler = mView.findViewById(R.id.userHomeWatchRecyclerViewId);
        
        mCapAdapter = new ProductAdapter(mCapAdapterClick,requireContext(),this);
        mGlassAdapter = new ProductAdapter(mGlassAdapterClick,requireContext(),this);
        mHeadphoneAdapter = new ProductAdapter(mHeadphoneAdapterClick,requireContext(),this);
        mLaptopAdapter = new ProductAdapter(mLaptopAdapterClick,requireContext(),this);
        mMobileAdapter = new ProductAdapter(mMobileAdapterClick,requireContext(),this);
        mShoeAdapter = new ProductAdapter(mShoeAdapterClick,requireContext(),this);
        mSweaterAdapter = new ProductAdapter(mSweaterAdapterClick,requireContext(),this);
        mTShirtAdapter = new ProductAdapter(mTShirtAdapterClick,requireContext(),this);
        mWatchAdapter = new ProductAdapter(mWatchAdapterClick,requireContext(),this);

        mCapSnap = new PagerSnapHelper();
        mGlassSnap = new PagerSnapHelper();
        mHeadphoneSnap = new PagerSnapHelper();
        mLaptopSnap = new PagerSnapHelper();
        mMobileSnap = new PagerSnapHelper();
        mShoeSnap = new PagerSnapHelper();
        mSweaterSnap = new PagerSnapHelper();
        mTShirtSnap = new PagerSnapHelper();
        mWatchSnap = new PagerSnapHelper();

        mCapRecycler.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        mCapRecycler.setAdapter(mCapAdapter);
        mCapSnap.attachToRecyclerView(mCapRecycler);

        mGlassRecycler.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        mGlassRecycler.setAdapter(mGlassAdapter);
        mGlassSnap.attachToRecyclerView(mGlassRecycler);

        mHeadphoneRecycler.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        mHeadphoneRecycler.setAdapter(mHeadphoneAdapter);
        mHeadphoneSnap.attachToRecyclerView(mHeadphoneRecycler);

        mLaptopRecycler.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        mLaptopRecycler.setAdapter(mLaptopAdapter);
        mLaptopSnap.attachToRecyclerView(mLaptopRecycler);

        mMobileRecycler.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        mMobileRecycler.setAdapter(mMobileAdapter);
        mMobileSnap.attachToRecyclerView(mMobileRecycler);

        mShoeRecycler.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        mShoeRecycler.setAdapter(mShoeAdapter);
        mShoeSnap.attachToRecyclerView(mShoeRecycler);

        mSweaterRecycler.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        mSweaterRecycler.setAdapter(mSweaterAdapter);
        mSweaterSnap.attachToRecyclerView(mSweaterRecycler);

        mTShirtRecycler.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        mTShirtRecycler.setAdapter(mTShirtAdapter);
        mTShirtSnap.attachToRecyclerView(mTShirtRecycler);

        mWatchRecycler.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        mWatchRecycler.setAdapter(mWatchAdapter);
        mWatchSnap.attachToRecyclerView(mWatchRecycler);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mCapAdapter.getFilter().filter(newText);
                mGlassAdapter.getFilter().filter(newText);
                mHeadphoneAdapter.getFilter().filter(newText);
                mLaptopAdapter.getFilter().filter(newText);
                mMobileAdapter.getFilter().filter(newText);
                mShoeAdapter.getFilter().filter(newText);
                mSweaterAdapter.getFilter().filter(newText);
                mTShirtAdapter.getFilter().filter(newText);
                mWatchAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    private void initAllProduct(){
        Log.d(TAG, "initAllProduct: called");
        mCapProducts = new ArrayList<>();
        mGlassProduct = new ArrayList<>();
        mHeadphoneProduct = new ArrayList<>();
        mLaptopProduct = new ArrayList<>();
        mMobileProduct = new ArrayList<>();
        mShoeProduct = new ArrayList<>();
        mSweaterProduct = new ArrayList<>();
        mTShirtProduct = new ArrayList<>();
        mWatchProduct = new ArrayList<>();
    }
    private void addProduct(Product product){
        Log.d(TAG, "addProduct: called");
        if(product.category.equals("Cap")){
            mCapProducts.add(product);
        }
        else if(product.category.equals("Glass")){
            mGlassProduct.add(product);
        }else if(product.category.equals("HeadPhone")){
            mHeadphoneProduct.add(product);
        }else if(product.category.equals("T-Shirt")){
            mTShirtProduct.add(product);
        }else if(product.category.equals("Laptop")){
            mLaptopProduct.add(product);
        }else if(product.category.equals("Mobile")){
            mMobileProduct.add(product);
        }else if(product.category.equals("Shoe")){
            mShoeProduct.add(product);
        }else if(product.category.equals("Sweater")){
            mSweaterProduct.add(product);
        }else if(product.category.equals("Watch")){
            mWatchProduct.add(product);
        }
        setListInAdapter();
    }
    private void addAllProduct(){
        Log.d(TAG, "addAllProduct: called");
        for (Product product : App.User.getProducts()) {
            if(product.category.equals("Cap")){
                mCapProducts.add(product);
            }
            else if(product.category.equals("Glass")){
                mGlassProduct.add(product);
            }else if(product.category.equals("HeadPhone")){
                mHeadphoneProduct.add(product);
            }else if(product.category.equals("T-Shirt")){
                mTShirtProduct.add(product);
            }else if(product.category.equals("Laptop")){
                mLaptopProduct.add(product);
            }else if(product.category.equals("Mobile")){
                mMobileProduct.add(product);
            }else if(product.category.equals("Shoe")){
                mShoeProduct.add(product);
            }else if(product.category.equals("Sweater")){
                mSweaterProduct.add(product);
            }else if(product.category.equals("Watch")){
                mWatchProduct.add(product);
            }
        }
        setListInAdapter();
    }
    private void setListInAdapter(){
        Log.d(TAG, "setListInAdapter: called");
        mCapAdapter.setProducts(mCapProducts);
        mGlassAdapter.setProducts(mGlassProduct);
        mHeadphoneAdapter.setProducts(mHeadphoneProduct);
        mTShirtAdapter.setProducts(mTShirtProduct);
        mLaptopAdapter.setProducts(mLaptopProduct);
        mMobileAdapter.setProducts(mMobileProduct);
        mShoeAdapter.setProducts(mShoeProduct);
        mSweaterAdapter.setProducts(mSweaterProduct);
        mWatchAdapter.setProducts(mWatchProduct);
        
        mCapAdapter.notifyDataSetChanged();
        mGlassAdapter.notifyDataSetChanged();
        mHeadphoneAdapter.notifyDataSetChanged();
        mTShirtAdapter.notifyDataSetChanged();
        mLaptopAdapter.notifyDataSetChanged();
        mMobileAdapter.notifyDataSetChanged();
        mShoeAdapter.notifyDataSetChanged();
        mSweaterAdapter.notifyDataSetChanged();
        mWatchAdapter.notifyDataSetChanged();

        setVisibility();

    }
    public void setVisibility(){
        if(mCapAdapter.getItemCount()==0){
            mCapLayout.setVisibility(View.GONE);
        }
        else{
            mCapLayout.setVisibility(View.VISIBLE);
        }
        if(mGlassAdapter.getItemCount()==0){
            mGlassLayout.setVisibility(View.GONE);
        }
        else{
            mGlassLayout.setVisibility(View.VISIBLE);
        }
        if(mHeadphoneAdapter.getItemCount()==0){
            mHeadphoneLayout.setVisibility(View.GONE);
        }
        else{
            mHeadphoneLayout.setVisibility(View.VISIBLE);
        }
        if(mTShirtAdapter.getItemCount()==0){
            mTShirtLayout.setVisibility(View.GONE);
        }
        else{
            mTShirtLayout.setVisibility(View.VISIBLE);
        }
        if(mLaptopAdapter.getItemCount()==0){
            mLaptopLayout.setVisibility(View.GONE);
        }
        else{
            mLaptopLayout.setVisibility(View.VISIBLE);
        }
        if(mMobileAdapter.getItemCount()==0){
            mMobileLayout.setVisibility(View.GONE);
        }
        else{
            mMobileLayout.setVisibility(View.VISIBLE);
        }
        if(mShoeAdapter.getItemCount()==0){
            mShoeLayout.setVisibility(View.GONE);
        }
        else{
            mShoeLayout.setVisibility(View.VISIBLE);
        }
        if(mSweaterAdapter.getItemCount()==0){
            mSweaterLayout.setVisibility(View.GONE);
        }
        else{
            mSweaterLayout.setVisibility(View.VISIBLE);
        }
        if(mWatchAdapter.getItemCount()==0){
            mWatchLayout.setVisibility(View.GONE);
        }
        else{
            mWatchLayout.setVisibility(View.VISIBLE);
        }
    }
    
    private void initTextImage(){
        Log.d(TAG, "initTextImage: called");
        mCapLayout = mView.findViewById(R.id.userHomeCapLayoutId);
        mGlassLayout = mView.findViewById(R.id.userHomeGlassLayoutId);
        mHeadphoneLayout = mView.findViewById(R.id.userHomeHeadPhoneLayoutId);
        mTShirtLayout = mView.findViewById(R.id.userHomeTShirtLayoutId);
        mLaptopLayout = mView.findViewById(R.id.userHomeLaptopLayoutId);
        mMobileLayout = mView.findViewById(R.id.userHomeMobileLayoutId);
        mShoeLayout = mView.findViewById(R.id.userHomeShoeLayoutId);
        mSweaterLayout = mView.findViewById(R.id.userHomeSweaterLayoutId);
        mWatchLayout = mView.findViewById(R.id.userHomeWatchLayoutId);
        
        mCapImage = mView.findViewById(R.id.userHomeCapExpandImageId);
        mGlassImage = mView.findViewById(R.id.userHomeGlassExpandImageId);
        mHeadphoneImage = mView.findViewById(R.id.userHomeHeadPhoneExpandImageId);
        mTShirtImage = mView.findViewById(R.id.userHomeTShirtExpandImageId);
        mLaptopImage = mView.findViewById(R.id.userHomeLaptopExpandImageId);
        mMobileImage = mView.findViewById(R.id.userHomeMobileExpandImageId);
        mShoeImage = mView.findViewById(R.id.userHomeShoeExpandImageId);
        mSweaterImage = mView.findViewById(R.id.userHomeSweaterExpandImageId);
        mWatchImage = mView.findViewById(R.id.userHomeWatchExpandImageId);

        View.OnClickListener mCapListener = v -> {
            if(mCapRecycler.getVisibility() == View.VISIBLE){
                mCapRecycler.setVisibility(View.GONE);
                mCapImage.setImageResource(R.drawable.ic_arrow_down);
            }
            else{
                mCapRecycler.setVisibility(View.VISIBLE);
                mCapImage.setImageResource(R.drawable.ic_arrow_up);
            }
        };
        View.OnClickListener mGlassListener = v -> {
            if(mGlassRecycler.getVisibility() == View.VISIBLE){
                mGlassRecycler.setVisibility(View.GONE);
                mGlassImage.setImageResource(R.drawable.ic_arrow_down);
            }
            else{
                mGlassRecycler.setVisibility(View.VISIBLE);
                mGlassImage.setImageResource(R.drawable.ic_arrow_up);
            }
        };
        View.OnClickListener mHeadPhoneListener = v -> {
            if(mHeadphoneRecycler.getVisibility() == View.VISIBLE){
                mHeadphoneRecycler.setVisibility(View.GONE);
                mHeadphoneImage.setImageResource(R.drawable.ic_arrow_down);
            }
            else{
                mHeadphoneRecycler.setVisibility(View.VISIBLE);
                mHeadphoneImage.setImageResource(R.drawable.ic_arrow_up);
            }
        };
        View.OnClickListener mTShirtListener = v -> {
            if(mTShirtRecycler.getVisibility() == View.VISIBLE){
                mTShirtRecycler.setVisibility(View.GONE);
                mTShirtImage.setImageResource(R.drawable.ic_arrow_down);
            }
            else{
                mTShirtRecycler.setVisibility(View.VISIBLE);
                mTShirtImage.setImageResource(R.drawable.ic_arrow_up);
            }
        };
        View.OnClickListener mLaptopListener = v -> {
            if(mLaptopRecycler.getVisibility() == View.VISIBLE){
                mLaptopRecycler.setVisibility(View.GONE);
                mLaptopImage.setImageResource(R.drawable.ic_arrow_down);
            }
            else{
                mLaptopRecycler.setVisibility(View.VISIBLE);
                mLaptopImage.setImageResource(R.drawable.ic_arrow_up);
            }
        };
        View.OnClickListener mMobileListener = v -> {
            if(mMobileRecycler.getVisibility() == View.VISIBLE){
                mMobileRecycler.setVisibility(View.GONE);
                mMobileImage.setImageResource(R.drawable.ic_arrow_down);
            }
            else{
                mMobileRecycler.setVisibility(View.VISIBLE);
                mMobileImage.setImageResource(R.drawable.ic_arrow_up);
            }
        };
        View.OnClickListener mShoeListener = v -> {
            if(mShoeRecycler.getVisibility() == View.VISIBLE){
                mShoeRecycler.setVisibility(View.GONE);
                mShoeImage.setImageResource(R.drawable.ic_arrow_down);
            }
            else{
                mShoeRecycler.setVisibility(View.VISIBLE);
                mShoeImage.setImageResource(R.drawable.ic_arrow_up);
            }
        }; 
        View.OnClickListener mSweaterListener = v -> {
            if(mSweaterRecycler.getVisibility() == View.VISIBLE){
                mSweaterRecycler.setVisibility(View.GONE);
                mSweaterImage.setImageResource(R.drawable.ic_arrow_down);
            }
            else{
                mSweaterRecycler.setVisibility(View.VISIBLE);
                mSweaterImage.setImageResource(R.drawable.ic_arrow_up);
            }
        }; 
        View.OnClickListener mWatchListener = v -> {
            if(mWatchRecycler.getVisibility() == View.VISIBLE){
                mWatchRecycler.setVisibility(View.GONE);
                mWatchImage.setImageResource(R.drawable.ic_arrow_down);
            }
            else{
                mWatchRecycler.setVisibility(View.VISIBLE);
                mWatchImage.setImageResource(R.drawable.ic_arrow_up);
            }
        };
        mCapImage.setOnClickListener(mCapListener);
        mGlassImage.setOnClickListener(mGlassListener);
        mHeadphoneImage.setOnClickListener(mHeadPhoneListener);
        mTShirtImage.setOnClickListener(mTShirtListener);
        mLaptopImage.setOnClickListener(mLaptopListener);
        mMobileImage.setOnClickListener(mMobileListener);
        mShoeImage.setOnClickListener(mShoeListener);
        mSweaterImage.setOnClickListener(mSweaterListener);
        mWatchImage.setOnClickListener(mWatchListener);

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: called");
        App.User.clearProducts();
        App.User.setProducts(mCapAdapter.getProducts());
        App.User.setProducts(mGlassAdapter.getProducts());
        App.User.setProducts(mHeadphoneAdapter.getProducts());
        App.User.setProducts(mTShirtAdapter.getProducts());
        App.User.setProducts(mLaptopAdapter.getProducts());
        App.User.setProducts(mMobileAdapter.getProducts());
        App.User.setProducts(mShoeAdapter.getProducts());
        App.User.setProducts(mSweaterAdapter.getProducts());
        App.User.setProducts(mWatchAdapter.getProducts());
        super.onDestroy();
    }

    private ProductAdapter.CustomOrderAdapterClickInterface mCapAdapterClick = new ProductAdapter.CustomOrderAdapterClickInterface() {
        @Override
        public void onItemClick(int position) {
            productDetailsActivity(mCapAdapter.getProduct(position));
        }

        @Override
        public void onLongItemClick(int position) {

        }
    };
    private ProductAdapter.CustomOrderAdapterClickInterface mGlassAdapterClick = new ProductAdapter.CustomOrderAdapterClickInterface() {
        @Override
        public void onItemClick(int position) {
            productDetailsActivity(mGlassAdapter.getProduct(position));
        }

        @Override
        public void onLongItemClick(int position) {

        }
    };
    private ProductAdapter.CustomOrderAdapterClickInterface mHeadphoneAdapterClick = new ProductAdapter.CustomOrderAdapterClickInterface() {
        @Override
        public void onItemClick(int position) {
            productDetailsActivity(mHeadphoneAdapter.getProduct(position));
        }

        @Override
        public void onLongItemClick(int position) {

        }
    };
    private ProductAdapter.CustomOrderAdapterClickInterface mTShirtAdapterClick = new ProductAdapter.CustomOrderAdapterClickInterface() {
        @Override
        public void onItemClick(int position) {
            productDetailsActivity(mTShirtAdapter.getProduct(position));
        }

        @Override
        public void onLongItemClick(int position) {

        }
    };
    private ProductAdapter.CustomOrderAdapterClickInterface mLaptopAdapterClick = new ProductAdapter.CustomOrderAdapterClickInterface() {
        @Override
        public void onItemClick(int position) {
            productDetailsActivity(mLaptopAdapter.getProduct(position));
        }

        @Override
        public void onLongItemClick(int position) {

        }
    };
    private ProductAdapter.CustomOrderAdapterClickInterface mMobileAdapterClick = new ProductAdapter.CustomOrderAdapterClickInterface() {
        @Override
        public void onItemClick(int position) {
            productDetailsActivity(mMobileAdapter.getProduct(position));
        }

        @Override
        public void onLongItemClick(int position) {

        }
    };
    private ProductAdapter.CustomOrderAdapterClickInterface mShoeAdapterClick = new ProductAdapter.CustomOrderAdapterClickInterface() {
        @Override
        public void onItemClick(int position) {
            productDetailsActivity(mShoeAdapter.getProduct(position));
        }

        @Override
        public void onLongItemClick(int position) {

        }
    };
    private ProductAdapter.CustomOrderAdapterClickInterface mSweaterAdapterClick = new ProductAdapter.CustomOrderAdapterClickInterface() {
        @Override
        public void onItemClick(int position) {
            productDetailsActivity(mSweaterAdapter.getProduct(position));
        }

        @Override
        public void onLongItemClick(int position) {

        }
    };
    private ProductAdapter.CustomOrderAdapterClickInterface mWatchAdapterClick = new ProductAdapter.CustomOrderAdapterClickInterface() {
        @Override
        public void onItemClick(int position) {
            productDetailsActivity(mWatchAdapter.getProduct(position));
        }

        @Override
        public void onLongItemClick(int position) {

        }
    };
    private void productDetailsActivity(Product product){
        Log.d(TAG, "productDetailsActivity: "+product);
        Intent intent = new Intent(requireActivity(), ProductDetailsActivity.class);
        intent.putExtra(ProductDetailsActivity.PRODUCT_KEY,product);
        startActivity(intent);
    }
}