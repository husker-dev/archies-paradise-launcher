package com.husker.launcher.server.utils;

import com.husker.launcher.server.GetRequest;
import com.husker.launcher.server.Profile;
import com.husker.launcher.server.ServerMain;
import org.apache.commons.codec.binary.Base64;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ProfileUtils {

    public static final long KEY_VALID_HOURS = 24;
    public static final long MAIL_CODE_VALID_MINUTES = 5;

    public static void removeInvalidKeys(int id){
        try {
            ArrayList<String> newKeys = new ArrayList<>();
            List<String> currentKeys = Files.readAllLines(Paths.get(Profile.profilesFolder + "/" + id + "/" + Profile.keysFile));
            for (String line : currentKeys){
                long time = Long.parseLong(line.split(",")[0]);
                if(isValidKey(time))
                    newKeys.add(line);
            }

            if(newKeys.size() != currentKeys.size())
                Files.write(Paths.get(Profile.profilesFolder + "/" + id + "/" + Profile.keysFile), newKeys);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public static void removeInvalidEmails(int id){
        try {
            ArrayList<String> newKeys = new ArrayList<>();
            List<String> currentKeys = Files.readAllLines(Paths.get(Profile.profilesFolder + "/" + id + "/" + Profile.mailCodesFile));
            for (String line : currentKeys){
                long time = Long.parseLong(line.split(",")[0]);
                if(isValidMailCode(time))
                    newKeys.add(line);
            }

            if(newKeys.size() != currentKeys.size())
                Files.write(Paths.get(Profile.profilesFolder + "/" + id + "/" + Profile.mailCodesFile), newKeys);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public static boolean isValidIp(String uuid, String ip){
        Profile profile = Profile.getByUUID(uuid);
        try {
            if (profile.getIP().equals(ip))
                return true;
            else
                return false;
        }catch (Exception ex){
            return false;
        }
    }

    public static boolean isValidKey(long time){
        return System.currentTimeMillis() - time < KEY_VALID_HOURS * 60 * 60 * 1000;
    }

    public static boolean isValidMailCode(long time){
        return System.currentTimeMillis() - time < MAIL_CODE_VALID_MINUTES * 60 * 1000;
    }

    public static String generateKey(){
        StringBuilder text = new StringBuilder();

        int len = 20;
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = upper.toLowerCase();
        String digits = "0123456789";
        String alphanum = upper + lower + digits;

        do {
            for (int i = 0; i < len; i++)
                text.append(alphanum.charAt(new Random().nextInt(alphanum.length() - 1)));
        }while (Profile.get(text.toString()) != null);
        return text.toString();
    }

    public static String generateMailCode(){
        StringBuilder text = new StringBuilder();

        int len = 6;
        String digits = "0123456789";

        do {
            for (int i = 0; i < len; i++)
                text.append(digits.charAt(new Random().nextInt(digits.length() - 1)));
        }while (Profile.get(text.toString()) != null);
        return text.toString();
    }

    public static int getUserCount(){
        try {
            Files.createDirectories(Paths.get(Profile.profilesFolder));
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return Objects.requireNonNull(new File(Profile.profilesFolder).list()).length;
    }

    public static Profile getProfile(GetRequest parameters){
        if(parameters.containsKey(Profile.KEY))
            return Profile.get(parameters.getString(Profile.KEY));
        else {
            if(parameters.containsKey(Profile.ENCRYPTED) && parameters.get(Profile.ENCRYPTED).equals("1"))
                return Profile.get(parameters.getString(Profile.LOGIN), decrypt(parameters.getString(Profile.PASSWORD)));
            else
                return Profile.get(parameters.getString(Profile.LOGIN), parameters.getString(Profile.PASSWORD));
        }
    }

    public static boolean isNicknameExist(String nickname){
        for(int i = 1; i <= getUserCount(); i++)
            if(new Profile(i).getDataValue(Profile.LOGIN).equals(nickname))
                return true;
        return false;
    }

    public static boolean isUUIDExist(String uuid){
        for(int i = 1; i <= getUserCount(); i++)
            if(new Profile(i).getDataValue(Profile.UUID).equals(uuid))
                return true;
        return false;
    }

    public static String createUuid(){
        String uuid;
        do{
            uuid = UUID.randomUUID().toString();
        }while (isUUIDExist(uuid));
        return uuid;
    }

    public static void sendText(Socket socket, Object text) throws IOException {
        BufferedWriter bf = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        bf.write(text + "\n");
        bf.flush();
    }

    public static String encrypt(final String text) {
        return Base64.encodeBase64String(xor(text.getBytes()));
    }

    public static String decrypt(final String hash) {
        return new String(xor(Base64.decodeBase64(hash.getBytes())), StandardCharsets.UTF_8);
    }

    private static byte[] xor(final byte[] input) {
        final byte[] output = new byte[input.length];
        final byte[] secret = ServerMain.Settings.getEncryptionKey().getBytes();
        int spos = 0;
        for (int pos = 0; pos < input.length; ++pos) {
            output[pos] = (byte) (input[pos] ^ secret[spos]);
            spos += 1;
            if (spos >= secret.length)
                spos = 0;
        }
        return output;
    }

}
