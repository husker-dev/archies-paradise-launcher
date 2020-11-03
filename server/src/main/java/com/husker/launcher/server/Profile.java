package com.husker.launcher.server;

import com.husker.launcher.server.utils.FormatUtils;
import com.husker.launcher.server.utils.ProfileUtils;
import com.husker.launcher.server.utils.settings.SettingsFile;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Profile {

    private final int id;

    public static final String profilesFolder = "./players";
    public static final String dataFile = "data.txt";
    public static final String keysFile = "keys.txt";
    public static final String mailCodesFile = "mailCodes.txt";
    public static final String skinFile = "skin.png";
    public static final String ipFile = "ip.txt";

    public static final String RESULT = "result";
    public static final String KEY = "access_token";
    public static final String LOGIN = "login";
    public static final String EMAIL = "email";
    public static final String EMAIL_CODE = "email_code";
    public static final String STATUS = "status";
    public static final String SKIN_URL = "skin_url";
    public static final String PASSWORD = "password";
    public static final String CURRENT_PASSWORD = "current_password";
    public static final String NEW_PASSWORD = "new_password";
    public static final String HAS_SKIN = "has_skin";
    public static final String ID = "id";

    public SettingsFile data;

    public static int create(String login, String password){
        try {
            if(!FormatUtils.isCorrectName(login))
                return 1;
            if(ProfileUtils.isNicknameExist(login))
                return 2;
            if(!FormatUtils.isCorrectPassword(password))
                return 3;

            int id = ProfileUtils.getUserCount() + 1;
            Files.createDirectories(Paths.get(profilesFolder + "/" + id));
            Files.write(Paths.get(profilesFolder + "/" + id + "/" + dataFile), new ArrayList<>(Arrays.asList(
                    LOGIN + ":" + login,
                    PASSWORD + ":" + DigestUtils.md2Hex(password),
                    SKIN_URL + ":null",
                    STATUS + ":Гость",
                    EMAIL + ":null")));
            Files.createFile(Paths.get(profilesFolder + "/" + id + "/" + ipFile));
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
                if(profile.getDataValue(LOGIN).equals(login) && ProfileUtils.arePasswordsEquals(profile.data.get(PASSWORD), password))
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

    public int getId(){
        return id;
    }

    public void setIP(String ip){
        try {
            Files.write(Paths.get(getFolder() + "/" + ipFile), Collections.singletonList(ip));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getIP(){
        try {
            return Files.readAllLines(Paths.get(getFolder() + "/" + ipFile)).get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getDataValue(String parameter){
        return getData(parameter).get(parameter);
    }

    public HashMap<String, String> getData(String... parameters){
        HashMap<String, String> out_data = new HashMap<>();

        List<String> availableData = Arrays.asList(ID, HAS_SKIN, SKIN_URL, LOGIN, EMAIL, STATUS);

        if(parameters.length == 0)
            parameters = availableData.toArray(new String[0]);

        for(String parameter : parameters) {
            if (availableData.contains(parameter)) {
                if (parameter.equals(ID))
                    out_data.put(parameter, id + "");
                else if (parameter.equals(HAS_SKIN))
                    out_data.put(parameter, Files.exists(Paths.get(getFolder() + "/" + skinFile)) ? "1" : "0");
                else if (parameter.equals(PASSWORD))
                    out_data.put(parameter, data.get(PASSWORD));
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

        return ServerMain.MailManager.send(email, ServerMain.Settings.getEmailTitle(), ServerMain.Settings.getEmailText().replace("[code]", code)) ? 0 : 1;
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

    public int setData(JSONObject object, JSONObject confirms){
        HashMap<String, String> map = new HashMap<>();
        for(Map.Entry<String, Object> entry : object.toMap().entrySet())
            map.put(entry.getKey(), entry.getValue() + "");

        HashMap<String, String> confirms_map = new HashMap<>();
        for(Map.Entry<String, Object> entry : confirms.toMap().entrySet())
            confirms_map.put(entry.getKey(), entry.getValue() + "");
        return setData(map, confirms_map);
    }

    public int setData(String... parameters){
        return setData(null, parameters);
    }

    public int setData(HashMap<String, String> confirms, String... parameters){
        HashMap<String, String> parameterMap = new HashMap<>();
        for(int i = 0; i < parameters.length; i += 2)
            parameterMap.put(parameters[0], parameters[1]);
        return setData(parameterMap, confirms);
    }

    public int setData(HashMap<String, String> fields, HashMap<String, String> confirms){
        try {
            HashMap<String, FieldSetter> setters = new HashMap<>();

            int PASSWORD_REQUIRED = 1;
            int CODE_REQUIRED = 2;
            int INCORRECT_PASSWORD = 3;
            int INCORRECT_EMAIL_CODE = 4;

            int INCORRECT_LOGIN_FORMAT = 5;
            int LOGIN_EXIST = 6;

            int INCORRECT_EMAIL_FORMAT = 7;
            int INCORRECT_PASSWORD_FORMAT = 8;

            // Setters
            setters.put(LOGIN, new FieldSetter(){{
                setPasswordRequired(true);
                addCondition(INCORRECT_LOGIN_FORMAT, FormatUtils::isCorrectName);
                addCondition(LOGIN_EXIST, value -> ProfileUtils.canChangeName(Profile.this, value));
                setAction(value -> data.set(LOGIN, value));
            }});
            setters.put(EMAIL, new FieldSetter(){{
                setPasswordRequired(true);
                setCodeRequired(true);
                addCondition(INCORRECT_EMAIL_FORMAT, FormatUtils::isCorrectEmail);
                setAction(value -> data.set(EMAIL, value));
            }});
            setters.put(PASSWORD, new FieldSetter(){{
                setPasswordRequired(true);
                setCodeRequired(true);
                addCondition(INCORRECT_PASSWORD_FORMAT, FormatUtils::isCorrectPassword);
                setAction(value -> data.set(PASSWORD, DigestUtils.md2Hex(value)));
            }});
            setters.put(SKIN_URL, new FieldSetter(){{
                setAction(value -> data.set(SKIN_URL, value));
            }});



            // Logic
            boolean passwordConfirmed = false;
            boolean emailConfirmed = false;

            if(confirms != null) {
                if (confirms.containsKey(EMAIL_CODE)) {
                    String email = fields.containsKey(EMAIL) ? fields.get(EMAIL) : getDataValue(EMAIL);

                    if (isValidEmailCode(email, confirms.get(EMAIL_CODE)))
                        emailConfirmed = true;
                    else
                        return INCORRECT_EMAIL_CODE;
                }
                if (confirms.containsKey(PASSWORD)) {
                    if (data.get(PASSWORD).equals(DigestUtils.md2Hex(confirms.get(PASSWORD))))
                        passwordConfirmed = true;
                    else
                        return INCORRECT_PASSWORD;
                }
            }

            // Checking confirms
            for(Map.Entry<String, String> entry : fields.entrySet()) {
                if (setters.containsKey(entry.getKey())) {
                    FieldSetter setter = setters.get(entry.getKey());

                    if(setter.isEmailRequired() && !emailConfirmed)
                        return CODE_REQUIRED;
                    if(setter.isPasswordRequired() && !passwordConfirmed)
                        return PASSWORD_REQUIRED;
                }
            }

            // Invoke
            for(Map.Entry<String, String> entry : fields.entrySet()){
                if(setters.containsKey(entry.getKey())) {
                    FieldSetter setter = setters.get(entry.getKey());
                    String value = entry.getValue();

                    int conditionResult = setter.checkConditions(value);
                    if(conditionResult == 0)
                        setter.run(value);
                    else
                        return conditionResult;
                }
            }

            return 0;
        }catch (Exception ex){
            ex.printStackTrace();
            return -1;
        }
    }

    static class FieldSetter{

        private boolean password = false;
        private boolean email = false;

        private Consumer<String> action;

        private HashMap<Predicate<String>, Integer> conditions = new HashMap<>();

        public void setPasswordRequired(boolean required){
            password = required;
        }

        public void setCodeRequired(boolean required){
            email = required;
        }

        public boolean isPasswordRequired(){
            return password;
        }

        public boolean isEmailRequired(){
            return email;
        }

        public void addCondition(int code, Predicate<String> condition){
            conditions.put(condition, code);
        }

        public int checkConditions(String value){
            for(Map.Entry<Predicate<String>, Integer> entry : conditions.entrySet())
                if(!entry.getKey().test(value))
                    return entry.getValue();
            return 0;
        }

        public void setAction(Consumer<String> action){
            this.action = action;
        }

        public void run(String value){
            action.accept(value);
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
