package com.example.ecommerce.model;

public class Order {
    public String orderId;
    public String productId;
    public String oderedUserName;
    public String paymentStatus;
    public String amount;
    public String orderedDate;
    public String receivedDate;
    public String imageUrl;
    public String productName;
    public String sellerName;

    public Order() {
        this.orderId = "";
        this.productId = "";
        this.oderedUserName = "";
        this.paymentStatus = "";
        this.amount = "";
        this.orderedDate = "";
        this.receivedDate = "";
        this.imageUrl = "";
        this.productName = "";
        this.sellerName = "";
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("orderId: "+ orderId)
                .append("\nproductId : "+ productId)
                .append("\noderedUserName: "+ oderedUserName)
                .append("\npaymentStatus: "+ paymentStatus)
                .append("\namount: "+ amount)
                .append("\norderedDate: "+ orderedDate)
                .append("\nreceivedDate: "+ receivedDate)
                .append("\nimageUrl: "+ imageUrl)
                .append("\nproductName: "+ productName)
                .append("\nsellerName: "+ sellerName)
                .toString();
    }

    public static class Key{
        public static final String ORDER_ID = "id";
        public static final String ORDER_PRODUCT_ID = "product_id";
        public static final String ORDER_USER_NAME = "buyer";
        public static final String ORDER_PAYMENT_STATUS = "payment_status";
        public static final String ORDER_AMOUNT = "amount";
        public static final String ORDER_SUBMIT_DATE = "order_date";
        public static final String ORDER_DELIVERY_DATE = "delivery_date";
        public static final String ORDER_DELIVERY_LOCATION = "delivery_location";
    }
}
