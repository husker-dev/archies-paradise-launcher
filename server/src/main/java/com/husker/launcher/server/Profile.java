package com.husker.launcher.server;

import com.husker.launcher.server.utils.FormatUtils;
import com.husker.launcher.server.utils.ProfileUtils;
import com.husker.launcher.server.utils.settings.SettingsFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Profile {

    private final int id;

    public static final String profilesFolder = "./players";
    public static final String dataFile = "data.txt";
    public static final String keysFile = "keys.txt";
    public static final String mailCodesFile = "mailCodes.txt";
    public static final String skinFile = "skin.png";

    public static final String RESULT = "result";
    public static final String KEY = "key";
    public static final String LOGIN = "login";
    public static final String EMAIL = "email";
    public static final String EMAIL_CODE = "email_code";
    public static final String ENCRYPTED = "encrypted";
    public static final String STATUS = "status";
    public static final String SKIN_NAME = "skinName";
    public static final String PASSWORD = "password";
    public static final String CURRENT_PASSWORD = "current_password";
    public static final String NEW_PASSWORD = "new_password";
    public static final String HAS_SKIN = "hasSkin";
    public static final String ID = "id";

    public SettingsFile data;

    public static int create(String login, String password){
        try {
            if(ProfileUtils.isNicknameExist(login))
                return 1;
            if(!FormatUtils.isCorrectPassword(password))
                return 2;

            int id = ProfileUtils.getUserCount() + 1;
            Files.createDirectories(Paths.get(profilesFolder + "/" + id));
            Files.write(Paths.get(profilesFolder + "/" + id + "/" + dataFile), new ArrayList<>(Arrays.asList(LOGIN + ":" + login, PASSWORD + ":" + password, SKIN_NAME + ":null", STATUS + ":Гость", EMAIL + ":null")));
            Files.createFile(Paths.get(profilesFolder + "/" + id + "/" + keysFile));
            Files.createFile(Paths.get(profilesFolder + "/" + id + "/" + mailCodesFile));
            return 0;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return -1;
    }

    public static Profile get(String key){
        for(int id = 1; id <= ProfileUtils.getUserCount(); id++){
            try {
                for (String line : Files.readAllLines(Paths.get(profilesFolder + "/" + id + "/" + keysFile))) {
                    if (line.split(",")[1].equals(key) && ProfileUtils.isValidKey(Long.parseLong(line.split(",")[0]))) {
                        ProfileUtils.removeInvalidKeys(id);
                        return new Profile(id);
                    }
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        return null;
    }

    public static Profile get(String login, String password){
        for(int id = 1; id <= ProfileUtils.getUserCount(); id++){
            try {
                Profile profile = new Profile(id);
                if(profile.data.get(PASSWORD).equals(password) && profile.getDataValue(LOGIN).equals(login))
                    return profile;
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        return null;
    }

    public Profile(int id){
        this.id = id;
        data = new SettingsFile(new File(getFolder() + "/" + dataFile));
    }

    public String getDataValue(String parameter){
        return getData(parameter).get(parameter);
    }

    public HashMap<String, String> getData(String... parameters){
        HashMap<String, String> out_data = new HashMap<>();

        List<String> availableData = Arrays.asList(ID, HAS_SKIN, SKIN_NAME, LOGIN, EMAIL, STATUS, PASSWORD);

        if(parameters.length == 0)
            parameters = availableData.toArray(new String[0]);

        for(String parameter : parameters) {
            if (availableData.contains(parameter)) {
                if (parameter.equals(ID))
                    out_data.put(parameter, id + "");
                else if (parameter.equals(HAS_SKIN))
                    out_data.put(parameter, Files.exists(Paths.get(getFolder() + "/" + skinFile)) ? "1" : "0");
                else if (parameter.equals(PASSWORD))
                    out_data.put(parameter, ProfileUtils.encrypt(data.get(PASSWORD)));
                else
                    out_data.put(parameter, data.get(parameter));
            }
        }

        return out_data;
    }

    public String createKey(){
        try {
            String key = ProfileUtils.generateKey();
            BufferedWriter output = new BufferedWriter(new FileWriter(getFolder() + "/" + keysFile, true));
            output.write(System.currentTimeMillis() + "," + key + "\n");
            output.flush();
            output.close();
            return key;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    public int sendEmailCode(String email){
        String code = ProfileUtils.generateMailCode();

        try {
            BufferedWriter output = new BufferedWriter(new FileWriter(getFolder() + "/" + mailCodesFile, true));
            output.write(System.currentTimeMillis() + "," + email + "," + code + "\n");
            output.flush();
            output.close();
        }catch (Exception ex){
            ex.printStackTrace();
            return -1;
        }

        return ServerMain.MailManager.send(email, ServerMain.Settings.getEmailTitle(), "Код подтверждения: " + code) ? 0 : -1;
    }

    public boolean isValidEmailCode(String email, String code){
        try {
            ProfileUtils.removeInvalidEmails(id);

            for (String line : Files.readAllLines(Paths.get(getFolder() + "/" + mailCodesFile)))
                if (line.split(",")[1].equals(email) && line.split(",")[2].equals(code))
                    return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    public void removeKey(String key){
        try {
            ArrayList<String> newKeys = new ArrayList<>();
            List<String> currentKeys = Files.readAllLines(Paths.get(getFolder() + "/" + keysFile));
            for (String line : currentKeys)
                if(!line.equals(key))
                    newKeys.add(line);

            if (newKeys.size() != currentKeys.size())
                Files.write(Paths.get(getFolder() + "/" + keysFile), newKeys);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public int modifyData(HashMap<String, String> modify){
        try {
            int INCORRECT_CURRENT_PASSWORD = 1;
            int INCORRECT_LOGIN_FORMAT = 2;
            int LOGIN_EXIST = 3;
            int INCORRECT_EMAIL_FORMAT = 4;
            int INCORRECT_EMAIL_CODE = 5;
            int INCORRECT_PASSWORD_FORMAT = 6;

            if(modify.containsKey(LOGIN)){
                if(!FormatUtils.isCorrectName(modify.get(LOGIN)))
                    return INCORRECT_LOGIN_FORMAT;
                if(!getDataValue(LOGIN).equals(modify.get(LOGIN)) && ProfileUtils.isNicknameExist(modify.get(LOGIN)))
                    return LOGIN_EXIST;
                if(!modify.containsKey(CURRENT_PASSWORD) || !modify.get(CURRENT_PASSWORD).equals(data.get(PASSWORD)))
                    return INCORRECT_CURRENT_PASSWORD;

                data.set(LOGIN, modify.get(LOGIN));
            }

            if(modify.containsKey(EMAIL)){
                if(!FormatUtils.isCorrectName(modify.get(LOGIN)))
                    return INCORRECT_EMAIL_FORMAT;
                if(!modify.containsKey(EMAIL_CODE) || !isValidEmailCode(modify.get(EMAIL), modify.get(EMAIL_CODE)))
                    return INCORRECT_EMAIL_CODE;

                data.set(EMAIL, modify.get(EMAIL));
            }

            if(modify.containsKey(NEW_PASSWORD)){
                String email = modify.containsKey(EMAIL) ? modify.get(EMAIL) : getDataValue(EMAIL);
                if(!FormatUtils.isCorrectPassword(modify.get(NEW_PASSWORD)))
                    return INCORRECT_PASSWORD_FORMAT;
                if(!modify.containsKey(EMAIL_CODE) || !isValidEmailCode(email, modify.get(EMAIL_CODE)))
                    return INCORRECT_EMAIL_CODE;

                data.set(PASSWORD, modify.get(NEW_PASSWORD));
            }

            if(modify.containsKey(SKIN_NAME))
                data.set(SKIN_NAME, modify.get(SKIN_NAME));

            return 0;
        }catch (Exception ex){
            ex.printStackTrace();
            return -1;
        }
    }

    public boolean isEmailConfirmed(){
        return !getData(EMAIL).get(EMAIL).equals("null");
    }

    public String getFolder(){
        return profilesFolder + "/" + id;
    }

    public BufferedImage getSkin(){
        try {
            return ImageIO.read(getResource(skinFile));
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    public InputStream getResource(String name){
        try {
            return new FileInputStream(new File(getFolder() + "/" + name));
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
}
