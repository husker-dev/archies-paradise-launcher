package com.husker.launcher.settings;

import com.husker.launcher.utils.SystemUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Base64;

public class UserInfoFile extends SettingsFile {

    public UserInfoFile() {
        super(SystemUtils.getSettingsFolder() + "/user.yaml");
    }

    public String getLogin(){
        if(!containsVar("login"))
            return null;
        return get("login");
    }

    public void setLogin(String login){
        set("login", login);
    }

    public String getPassword(){
        if(!containsVar("password"))
            return null;
        return decrypt(get("password"));
    }

    public void setPassword(String password){
        set("password", encrypt(password));
    }

    public void reset(){
        setLogin("null");
        setPassword("null");
    }

    public boolean hasAccount(){
        String login = getLogin();
        String password = getPassword();

        return login != null && password != null && !login.equals("null") && !password.equals("null");
    }

    private String encrypt(String text) {
        return encrypt(text, getKey());
    }

    private String decrypt(String text){
        return decrypt(text, getKey());
    }

    public static String decrypt(String strToDecrypt, String secret) {
        try {
            byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(secret.toCharArray(), "some salt...".getBytes(), 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String encrypt(String strToEncrypt, String secret) {
        try {
            byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(secret.toCharArray(), "some salt...".getBytes(), 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getKey(){
        String key = "$$$$$$$$$$$$$$$$";
        String userName = System.getProperty("user.name");

        if(userName.length() > key.length() / 2)
            userName = userName.substring(0, key.length() / 2);

        key = userName + key.substring(userName.length());
        key = key.substring(0, key.length() - userName.length()) + userName;

        return key;
    }
}
