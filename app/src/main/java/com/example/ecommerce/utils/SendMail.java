package com.example.ecommerce.utils;

import android.content.Context;
import android.util.Log;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendMail {

    private static final String EMAIL = "thegentleman660@gmail.com";
    private static final String PASSWORD = "191-15-12484";

    private static final String TAG = "TAG:SendMail";
    private Context context;
    private Session session;
    private String email;
    private String subject;
    private String message;
    public SendMail(Context context, String email, String subject, String message) {
        this.context = context;
        this.email = email;
        this.subject = subject;
        this.message = message;
    }

    public void send(){
        Log.d(TAG, "send: "+email);
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(EMAIL, PASSWORD);
                    }
                });

        try {
            MimeMessage email = new MimeMessage(session);
            email.setFrom(new InternetAddress(EMAIL));
            email.addRecipient(Message.RecipientType.TO, new InternetAddress(this.email));
            email.setSubject(subject);
            email.setText(message);
            Transport.send(email);

        } catch (MessagingException e) {
            Log.e(TAG, "send: ", e);
            e.printStackTrace();
        }
    }
}
