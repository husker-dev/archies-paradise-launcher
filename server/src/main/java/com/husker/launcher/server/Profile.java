package com.husker.launcher.server;

import com.husker.launcher.server.utils.FormatUtils;
import com.husker.launcher.server.utils.ProfileUtils;

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

    public static final String KEY = "key";
    public static final String LOGIN = "login";
    public static final String EMAIL = "email";
    public static final String STATUS = "status";
    public static final String SKIN_NAME = "skinName";
    public static final String PASSWORD = "password";
    public static final String CURRENT_PASSWORD = "current_password";
    public static final String HAS_SKIN = "hasSkin";
    public static final String ID = "id";

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
                boolean correctLogin = false;
                boolean correctPassword = false;
                for (String line : Files.readAllLines(Paths.get(profilesFolder + "/" + id + "/" + dataFile))) {
                    ArrayList<String> parts = new ArrayList<>(Arrays.asList(line.split(":")));
                    String parameter = parts.remove(0);
                    String value = String.join(":", parts.toArray(new String[0])).trim();

                    if(parameter.equals(LOGIN) && value.equals(login))
                        correctLogin = true;
                    if(parameter.equals(PASSWORD) && value.equals(password))
                        correctPassword = true;
                }
                if(correctLogin && correctPassword)
                    return new Profile(id);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        return null;
    }

    public Profile(int id){
        this.id = id;
    }

    public HashMap<String, String> getData(String... parameters){
        HashMap<String, String> out_values = new HashMap<>();
        ArrayList<String> parameterList = new ArrayList<>(Arrays.asList(parameters));
        parameterList.remove(PASSWORD);

        for(String parameter : parameters)
            out_values.put(parameter, "null");

        try {
            for (String line : Files.readAllLines(Paths.get(getFolder() + "/" + dataFile))) {
                ArrayList<String> parts = new ArrayList<>(Arrays.asList(line.split(":")));
                if(parameterList.size() == 0 || parameterList.contains(parts.get(0)))
                    out_values.put(parts.remove(0), String.join(":", parts.toArray(new String[0])).trim());
            }
        }catch (Exception ignored){}

        if(parameterList.contains(ID))
            out_values.put(ID, id + "");
        if(parameterList.contains(HAS_SKIN))
            out_values.put(HAS_SKIN, Files.exists(Paths.get(getFolder() + "/" + skinFile)) ? "1" : "0");

        return out_values;
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

        return ServerMain.MailManager.send(email, ServerMain.Settings.get("email_title"), "Код подтверждения: " + code) ? 0 : -1;
    }

    public int confirmMail(String email, String code){
        try {
            ProfileUtils.removeInvalidEmails(id);

            for (String line : Files.readAllLines(Paths.get(profilesFolder + "/" + id + "/" + mailCodesFile))) {
                if (line.split(",")[1].equals(email) && line.split(",")[2].equals(code)) {

                    ArrayList<String> lines = new ArrayList<>();
                    for (Map.Entry<String, String> entry : getData().entrySet()) {
                        if(entry.getKey().equals(EMAIL))
                            entry.setValue(email);
                        lines.add(entry.getKey() + ":" + entry.getValue());
                    }
                    Files.write(Paths.get(getFolder() + "/" + dataFile), lines);

                    return 0;
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return -1;
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

    public int modifyData(String currentPassword, HashMap<String, String> data){
        if(currentPassword.equals(getData(PASSWORD).get(PASSWORD))){
            try {
                HashMap<String, String> oldData = getData();
                if(data.containsKey(LOGIN) && !FormatUtils.isCorrectName(data.get(LOGIN)))
                    return 2;
                if(data.containsKey(LOGIN) && !getData(LOGIN).get(LOGIN).equals(data.get(LOGIN)) && ProfileUtils.isNicknameExist(data.get(LOGIN)))
                    return 3;
                if(data.containsKey(EMAIL) && !FormatUtils.isCorrectEmail(data.get(EMAIL)))
                    return 4;
                if(data.containsKey(EMAIL) && data.containsKey("email_code"))
                    return 4;

                if (data.containsKey(LOGIN))
                    oldData.put(LOGIN, data.get(LOGIN));
                if (data.containsKey(EMAIL))
                    confirmMail(data.get(EMAIL), data.get("email_code"));

                ArrayList<String> lines = new ArrayList<>();
                for (Map.Entry<String, String> entry : oldData.entrySet())
                    lines.add(entry.getKey() + ":" + entry.getValue());
                Files.write(Paths.get(getFolder() + "/" + dataFile), lines);
                return 0;
            }catch (Exception ex){
                ex.printStackTrace();
                return -1;
            }
        }
        return 1;
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
