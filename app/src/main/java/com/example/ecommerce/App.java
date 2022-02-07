package com.example.ecommerce;

import android.app.Application;
import android.util.Log;

import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App extends Application {
    private static final String TAG = "TAG:App";

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: ");
        User user = new User();
        Seller seller = new Seller();
        super.onCreate();
    }

    public static void resetUser(){
        User user = new User();
    }

    public static void resetSeller(){
        Seller seller = new Seller();
    }

    public static class User{
        public static boolean CART_FIRST_RUN;

        public static List<Product> mUserProducts;
        public static List<Order> mUserOrders;
        public static boolean USER_FIRST_RUN;

        public static Map<String, String> mSellerMap;
        public static Map<String, String> mImageMap;
        public static Map<String, String> mProductName;

        public User() {
            USER_FIRST_RUN = true;
            CART_FIRST_RUN = true;
            mUserProducts = new ArrayList<>();
            mUserProducts = new ArrayList<>();
            mUserOrders = new ArrayList<>();
            mSellerMap = new HashMap<>();
            mImageMap = new HashMap<>();
            mProductName = new HashMap<>();
        }

        public static List<Product> getProducts() {
            Log.d(TAG, "getProducts: size is : " + mUserProducts.size());
            return mUserProducts;
        }

        public static void clearProducts(){
            mUserProducts.clear();
            Log.d(TAG, "clearProducts: size is : "+mUserProducts.size());
        }

        public static void setProducts(List<Product> products) {
            Log.d(TAG, "setProducts: size is : " + products.size());
            mUserProducts.addAll(products);
        }

        public static List<Order> getOrders() {
            Log.d(TAG, "getOrders: size is : " + mUserOrders.size());
            return mUserOrders;
        }

        public static void setOrders(List<Order> orders) {
            Log.d(TAG, "setOrders: size is : " + orders.size());
            mUserOrders.clear();
            mUserOrders.addAll(orders);
        }
    }

    public static class Seller {

        public Seller() {
            FIRST_ADMIN_PRODUCT_RUN = true;
            FIRST_ADMIN_ORDER_RUN = true;
            mImageMap = new HashMap<>();
            mOrderMap = new HashMap<>();
            mProductMap = new HashMap<>();
            mAdminOrders = new ArrayList<>();
            mAdminProducts = new ArrayList<>();
        }

        public static boolean FIRST_ADMIN_PRODUCT_RUN;
        public static boolean FIRST_ADMIN_ORDER_RUN;
        public static boolean FIRST_ADMIN_PRODUCT_CHANGED;
        public static Map<String, String> mImageMap;
        public static Map<String, Boolean> mOrderMap;
        public static Map<String, String> mProductMap;

        public static List<Order> mAdminOrders;
        public static List<Product> mAdminProducts;

        public static List<Order> getAdminOrders() {
            Log.d(TAG, "getAdminOrders: size is : " + mAdminOrders.size());
            return mAdminOrders;
        }

        public static void setAdminOrders(List<Order> orders) {
            Log.d(TAG, "setAdminOrders: size is : " + orders.size());
            mAdminOrders.clear();
            mAdminOrders.addAll(orders);
        }

        public static void setAdminProducts(List<Product> products) {
            Log.d(TAG, "setAdminProducts: size is : " + products.size());
            mAdminProducts.clear();
            mAdminProducts.addAll(products);
        }

        public static List<Product> getAdminProducts() {
            Log.d(TAG, "getAdminProducts: size is : " + mAdminProducts.size());
            return mAdminProducts;
        }


        public static void updateProduct(String id, String price) {
            Log.d(TAG, "updateProduct: price is : " + price);
            int position = 0;
            boolean exist = false;
            for (Product products : mAdminProducts) {
                if (products.id.equals(id)) {
                    exist = true;
                    break;
                }
                ++position;
            }
            if (exist) {
                Log.d(TAG, "updateProduct: " + position + " price : " + mAdminProducts.get(position).price);
                mAdminProducts.get(position).price = price;
                Log.d(TAG, "updateProduct: " + position + " price : " + mAdminProducts.get(position).price);
            }
        }

        public static void updateOrder(String orderId, String date) {
            Log.d(TAG, "updateOrder: price is : " + date);
            int position = 0;
            boolean exist = false;
            for (Order products : mAdminOrders) {
                if (products.orderId.equals(orderId)) {
                    exist = true;
                    break;
                }
                ++position;
            }
            if (exist) {
                Log.d(TAG, "updateOrder: " + position + " price : " + mAdminOrders.get(position).receivedDate);
                mAdminOrders.get(position).receivedDate = date;
                Log.d(TAG, "updateOrder: " + position + " price : " + mAdminOrders.get(position).receivedDate);
            }
        }
    }


}
