package com.example.ecommerce.model;

public class User {
    public String id;
    public String userName;
    public String email;
    public String password;
    public String type;

    public static class Key{
        public static final String DATABASE_USER= "users_info";
        public static final String DATABASE_NAME= "name";
        public static final String DATABASE_EMAIL= "email";
        public static final String DATABASE_USER_TYPE= "user_type";
        public static final String DATABASE_PASSWORD= "password";
        public static final String DATABASE_BUYER= "buyer";
        public static final String DATABASE_SELLER= "seller";
        public static final String DATABASE_IMAGE= "image_url";
    }
    
    public static class Save{
        public static final String SAVED_STATUS_FILTER_KEY = "save_1001";
        public static final String SAVED_STATUS = "save_status";
        public static final String SAVED_NAME = "save_name";
        public static final String SAVED_EMAIL = "save_email";
        public static final String SAVED_PASSWORD = "save_password";
        public static final String SAVED_TYPE = "save_type";
        public static final String SAVED_IMAGE = "save_image";
        public static final String DEFAULT_IMAGE_LINK = "https://www.freeiconspng.com/thumbs/profile-icon-png/profile-icon-9.png";
    }
}
