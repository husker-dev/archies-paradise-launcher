package com.husker.launcher.server.core;

import com.husker.launcher.server.services.http.ApiException;
import com.husker.launcher.server.services.http.ImageLink;
import com.husker.launcher.server.ServerMain;
import com.husker.launcher.server.settings.SettingsFile;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Profile {

    private final int id;

    public static final String profilesFolder = "./players";
    public static final String dataFile = "data.yaml";
    public static final String keysFile = "keys.txt";
    public static final String mailCodesFile = "mail_codes.txt";
    public static final String skinFile = "skin.png";
    public static final String ipFile = "ip.txt";

    public static final String RESULT = "result";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String LOGIN = "login";
    public static final String EMAIL = "email";
    public static final String EMAIL_CODE = "email_code";
    public static final String STATUS = "status";
    public static final String PASSWORD = "password";
    public static final String CURRENT_PASSWORD = "current_password";
    public static final String NEW_PASSWORD = "new_password";
    public static final String ID = "id";

    public SettingsFile data;

    public static void create(String login, String password) throws IOException {
        if(!FormatUtils.isCorrectName(login))
            throw new ApiException("Wrong login format", 1);
        if(ProfileUtils.isNicknameExist(login))
            throw new ApiException("Login is already taken", 2);
        if(!FormatUtils.isCorrectPassword(password))
            throw new ApiException("Wrong password format", 3);

        int id = ProfileUtils.getUserCount() + 1;
        String path = profilesFolder + "/" + id;

        Files.createDirectories(Paths.get(path));

        SettingsFile data = new SettingsFile(path + "/" + dataFile);
        data.set(LOGIN, login);
        data.set(PASSWORD, DigestUtils.md2Hex(password));
        data.set(STATUS, "Гость");
        data.set(EMAIL, "null");

        Files.createFile(Paths.get(path + "/" + ipFile));
        Files.createFile(Paths.get(path + "/" + keysFile));
        Files.createFile(Paths.get(path + "/" + mailCodesFile));
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
        data = new SettingsFile(getFolder() + "/" + dataFile);
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

    public boolean sendEmailCode(String email) throws IOException {
        String code = ProfileUtils.generateMailCode();

        BufferedWriter output = new BufferedWriter(new FileWriter(getFolder() + "/" + mailCodesFile, true));
        output.write(System.currentTimeMillis() + "," + email + "," + code + "\n");
        output.flush();
        output.close();

        return ServerMain.MailService.send(email, ServerMain.Settings.getEmailTitle(), ServerMain.Settings.getEmailText().replace("{code}", code).replace("{name}", getDataValue(LOGIN)));
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

    public void setData(JSONObject object, JSONObject confirms){
        HashMap<String, String> map = new HashMap<>();
        for(Map.Entry<String, Object> entry : object.toMap().entrySet())
            map.put(entry.getKey(), entry.getValue() + "");

        HashMap<String, String> confirms_map = new HashMap<>();
        for(Map.Entry<String, Object> entry : confirms.toMap().entrySet())
            confirms_map.put(entry.getKey(), entry.getValue() + "");
        setData(map, confirms_map);
    }

    public void setData(String... parameters){
        setData(null, parameters);
    }

    public void setData(HashMap<String, String> confirms, String... parameters){
        HashMap<String, String> parameterMap = new HashMap<>();
        for(int i = 0; i < parameters.length; i += 2)
            parameterMap.put(parameters[0], parameters[1]);
        setData(parameterMap, confirms);
    }

    public void setData(HashMap<String, String> fields, HashMap<String, String> confirms){
        try {
            HashMap<String, FieldSetter> setters = new HashMap<>();

            ApiException PASSWORD_REQUIRED = new ApiException("Current password required", 1);
            ApiException CODE_REQUIRED = new ApiException("Email code required", 2);
            ApiException INCORRECT_PASSWORD = new ApiException("Current password is incorrect", 3);
            ApiException INCORRECT_EMAIL_CODE = new ApiException("Current email code is incorrect", 4);
            ApiException INCORRECT_LOGIN_FORMAT = new ApiException("Incorrect login format", 5);
            ApiException LOGIN_EXIST = new ApiException("Required login is already exist", 6);
            ApiException INCORRECT_EMAIL_FORMAT = new ApiException("Incorrect email format", 7);
            ApiException INCORRECT_PASSWORD_FORMAT = new ApiException("Incorrect password format", 8);

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

            // Logic
            boolean passwordConfirmed = false;
            boolean emailConfirmed = false;

            if(confirms != null) {
                if (confirms.containsKey(EMAIL_CODE)) {
                    String email = fields.containsKey(EMAIL) ? fields.get(EMAIL) : getDataValue(EMAIL);

                    if (isValidEmailCode(email, confirms.get(EMAIL_CODE)))
                        emailConfirmed = true;
                    else
                        throw INCORRECT_EMAIL_CODE;
                }
                if (confirms.containsKey(CURRENT_PASSWORD)) {
                    if (data.get(PASSWORD).equals(DigestUtils.md2Hex(confirms.get(CURRENT_PASSWORD))))
                        passwordConfirmed = true;
                    else
                        throw INCORRECT_PASSWORD;
                }
            }

            // Checking confirms
            for(Map.Entry<String, String> entry : fields.entrySet()) {
                if (setters.containsKey(entry.getKey())) {
                    FieldSetter setter = setters.get(entry.getKey());

                    if(setter.isEmailRequired() && !emailConfirmed)
                        throw CODE_REQUIRED;
                    if(setter.isPasswordRequired() && !passwordConfirmed)
                        throw PASSWORD_REQUIRED;
                }
            }

            // Invoke
            for(Map.Entry<String, String> entry : fields.entrySet()){
                if(setters.containsKey(entry.getKey())) {
                    FieldSetter setter = setters.get(entry.getKey());
                    String value = entry.getValue();

                    ApiException conditionResult = setter.checkConditions(value);
                    if(conditionResult == null)
                        setter.run(value);
                    else
                        throw conditionResult;
                }
            }

        }catch (Exception ex){
            ex.printStackTrace();
            throw new ApiException("Unknown exception while reading profile data", -2);
        }
    }

    static class FieldSetter{

        private boolean password = false;
        private boolean email = false;

        private Consumer<String> action;

        private final HashMap<Predicate<String>, ApiException> conditions = new HashMap<>();

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

        public void addCondition(ApiException error, Predicate<String> condition){
            conditions.put(condition, error);
        }

        public ApiException checkConditions(String value){
            for(Map.Entry<Predicate<String>, ApiException> entry : conditions.entrySet())
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

    public ImageLink getSkin(){
        try {
            return new ImageLink(new File(getFolder() + "/skin.png"));
        }catch (Exception ex){
            ex.printStackTrace();
        }
        try {
            if(!Files.exists(Paths.get("./steve.png")))
                Files.copy(getClass().getResourceAsStream("/steve.png"), Paths.get("./steve.png"));
            return new ImageLink("./steve.png");
        }catch (Exception ex){
            return null;
        }
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
