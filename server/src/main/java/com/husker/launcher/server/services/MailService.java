package com.husker.launcher.server.services;

import com.husker.launcher.server.ServerMain;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class MailService implements Runnable {

    private Properties properties;

    public void run() {
        properties = System.getProperties();
        properties.put("mail.smtp.host", ServerMain.Settings.getEmailHost());
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");
    }

    public boolean send(String to_email, String title, String text){
        try {
            Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(ServerMain.Settings.getEmail(), ServerMain.Settings.getEmailPassword());
                }
            });
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(ServerMain.Settings.getEmail()));

            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to_email));

            message.setSubject(title);
            message.setText(text);
            Transport.send(message);
            return true;
        } catch (Exception mex) {
            mex.printStackTrace();
            return false;
        }
    }
}
