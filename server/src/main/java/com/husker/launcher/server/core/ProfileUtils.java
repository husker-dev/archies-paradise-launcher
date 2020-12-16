package com.husker.launcher.server.core;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
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

    public static boolean isValidIp(String name, String ip){
        for(int i = 1; i <= getUserCount(); i++) {
            try {
                Profile profile = new Profile(i);
                if (profile.getDataValue(Profile.LOGIN).equals(name)) {
                    if (profile.getIP().equals(ip))
                        return true;
                    else
                        return false;
                }
            }catch (Exception ex){}
        }
        return false;
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

    public static boolean canChangeName(Profile profile, String newName){
        for(int i = 1; i <= getUserCount(); i++) {
            if(profile.getId() == i)
                continue;
            try {
                if (new Profile(i).getDataValue(Profile.LOGIN).equals(newName))
                    return false;
            }catch (Exception ignored){}
        }
        return true;
    }

    public static boolean isNicknameExist(String nickname){
        for(int i = 1; i <= getUserCount(); i++) {
            try {
                if (new Profile(i).getDataValue(Profile.LOGIN).equals(nickname))
                    return true;
            }catch (Exception ignored){}
        }
        return false;
    }

    public static boolean arePasswordsEquals(String hashed, String password){
        return hashed.equals(DigestUtils.md2Hex(password));
    }

}
