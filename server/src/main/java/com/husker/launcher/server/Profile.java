package com.husker.launcher.server;

import com.husker.launcher.server.services.HtmlService;
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
    public static final String ACCESS_TOKEN = "access_token";
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

    public static int create(String login, String password) throws IOException {
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
    }

    public static Profile getByName(String name){
        for(int id = 1; id <= ProfileUtils.getUserCount(); id++){
            try {
                Profile profile = new Profile(id);
                if(profile.getDataValue(LOGIN).equals(name))
                    return profile;
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        return null;
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

        List<String> availableData = Arrays.asList(ID, LOGIN, EMAIL, STATUS);

        for(String parameter : parameters) {
            if (availableData.contains(parameter)) {
                if (parameter.equals(ID))
                    out_data.put(parameter, id + "");
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

    public int sendEmailCode(String email) throws IOException {
        String code = ProfileUtils.generateMailCode();

        BufferedWriter output = new BufferedWriter(new FileWriter(getFolder() + "/" + mailCodesFile, true));
        output.write(System.currentTimeMillis() + "," + email + "," + code + "\n");
        output.flush();
        output.close();

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

    public HtmlService.ErrorMessage setData(JSONObject object, JSONObject confirms){
        HashMap<String, String> map = new HashMap<>();
        for(Map.Entry<String, Object> entry : object.toMap().entrySet())
            map.put(entry.getKey(), entry.getValue() + "");

        HashMap<String, String> confirms_map = new HashMap<>();
        for(Map.Entry<String, Object> entry : confirms.toMap().entrySet())
            confirms_map.put(entry.getKey(), entry.getValue() + "");
        return setData(map, confirms_map);
    }

    public HtmlService.ErrorMessage setData(String... parameters){
        return setData(null, parameters);
    }

    public HtmlService.ErrorMessage setData(HashMap<String, String> confirms, String... parameters){
        HashMap<String, String> parameterMap = new HashMap<>();
        for(int i = 0; i < parameters.length; i += 2)
            parameterMap.put(parameters[0], parameters[1]);
        return setData(parameterMap, confirms);
    }

    public HtmlService.ErrorMessage setData(HashMap<String, String> fields, HashMap<String, String> confirms){
        try {
            HashMap<String, FieldSetter> setters = new HashMap<>();

            HtmlService.ErrorMessage PASSWORD_REQUIRED = new HtmlService.ErrorMessage("Current password required", 1);
            HtmlService.ErrorMessage CODE_REQUIRED = new HtmlService.ErrorMessage("Email code required", 2);
            HtmlService.ErrorMessage INCORRECT_PASSWORD = new HtmlService.ErrorMessage("Current password is incorrect", 3);
            HtmlService.ErrorMessage INCORRECT_EMAIL_CODE = new HtmlService.ErrorMessage("Current email code is incorrect", 4);
            HtmlService.ErrorMessage INCORRECT_LOGIN_FORMAT = new HtmlService.ErrorMessage("Incorrect login format", 5);
            HtmlService.ErrorMessage LOGIN_EXIST = new HtmlService.ErrorMessage("Required login is already exist", 6);
            HtmlService.ErrorMessage INCORRECT_EMAIL_FORMAT = new HtmlService.ErrorMessage("Incorrect email format", 7);
            HtmlService.ErrorMessage INCORRECT_PASSWORD_FORMAT = new HtmlService.ErrorMessage("Incorrect password format", 8);

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

                    HtmlService.ErrorMessage conditionResult = setter.checkConditions(value);
                    if(conditionResult == null)
                        setter.run(value);
                    else
                        return conditionResult;
                }
            }

            return null;
        }catch (Exception ex){
            ex.printStackTrace();
            return new HtmlService.ErrorMessage("Unknown exception while reading profile data", -2);
        }
    }

    static class FieldSetter{

        private boolean password = false;
        private boolean email = false;

        private Consumer<String> action;

        private final HashMap<Predicate<String>, HtmlService.ErrorMessage> conditions = new HashMap<>();

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

        public void addCondition(HtmlService.ErrorMessage code, Predicate<String> condition){
            conditions.put(condition, code);
        }

        public HtmlService.ErrorMessage checkConditions(String value){
            for(Map.Entry<Predicate<String>, HtmlService.ErrorMessage> entry : conditions.entrySet())
                if(!entry.getKey().test(value))
                    return entry.getValue();
            return null;
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
