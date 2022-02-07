package com.example.ecommerce.utils;

import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


@SuppressLint("SimpleDateFormat")
public final class DateFormatter {
    public static String format(String oldDate){
        DateFormat originalFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        DateFormat targetFormat = new SimpleDateFormat("MMMM dd, yyyy");
        Date date = null;
        try {
            date = originalFormat.parse(oldDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return targetFormat.format(date);
    }
}
