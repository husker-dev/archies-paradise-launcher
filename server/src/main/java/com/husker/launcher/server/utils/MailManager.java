package com.husker.launcher.server.utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class MailManager {

    private final String email;
    private final String password;
    private final Properties properties;

    public MailManager(String email, String password){
        this.email = email;
        this.password = password;

        properties = System.getProperties();

        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");
    }

    public boolean send(String to_email, String title, String text){
        try {

            Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(email, password);
                }
            });
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(email));

            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to_email));

            message.setSubject(title);
            message.setText(text);
            Transport.send(message);
        } catch (MessagingException mex) {
            mex.printStackTrace();
            return false;
        }
        return true;
    }
}
