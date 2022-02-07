package com.example.ecommerce.model;

import java.io.Serializable;

public class Product implements Serializable {
    public String id;
    public String name;
    public String sellerName;
    public String date;
    public String imageUrl;
    public String description;
    public String category;
    public String price;

    @Override
    public String toString() {
        return new StringBuilder()
                .append("id"+ id)
                .append("\nname"+ name)
                .append("\nsellerName"+ sellerName)
                .append("\ndate"+ date)
                .append("\ncategory"+ category)
                .append("\nprice"+ price)
                .toString();
    }

    public static class Key{
        public static final String ADMIN_DATA_PASS = "admin_10";

        public static final String DATABASE_PRODUCT_NAME = "name";
        public static final String DATABASE_PRODUCT_SELLER_NAME = "sellerName";
        public static final String DATABASE_PRODUCT_IMAGE_URL = "imageUrl";
        public static final String DATABASE_PRODUCT_DESCRIPTION = "description";
        public static final String DATABASE_PRODUCT_CATEGORY = "category";
        public static final String DATABASE_PRODUCT_PRICE = "price";
        public static final String DATABASE_PRODUCT_DATE = "date";
        public static final String DATABASE_PRODUCT_VISIBILITY = "visibility";
        public static final String DATABASE_PRODUCT_VISIBLE = "visible";
        public static final String DATABASE_PRODUCT_NOT_VISIBLE = "not visible";
    }

}
