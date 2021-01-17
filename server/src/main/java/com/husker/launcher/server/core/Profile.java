package com.husker.launcher.server.core;

import com.husker.launcher.server.services.http.ApiException;
import com.husker.launcher.server.services.http.ImageLink;
import com.husker.launcher.server.ServerMain;
import com.husker.launcher.server.settings.SettingsFile;
import com.husker.launcher.server.utils.IOUtils;
import com.husker.mio.MIO;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Profile {

    public static final long MAIL_CODE_VALID_MINUTES = 5;

    public static final String profilesFolder = "./players";
    public static final String dataFile = "data.yaml";
    public static final String keysFile = "keys.txt";
    public static final String mailCodesFile = "mail_codes.txt";
    public static final String passwordResetHashesFile = "password_reset_hashes.txt";
    public static final String skinFile = "skin.png";
    public static final String capeFile = "cape.png";
    public static final String elytraFile = "elytra.png";
    public static final String ipFile = "ip.txt";

    public static final String ACCESS_TOKEN = "access_token";
    public static final String LOGIN = "login";
    public static final String EMAIL = "email";
    public static final String UUID = "uuid";
    public static final String CREATION_TIME = "creation_time";
    public static final String EMAIL_CODE = "email_code";
    public static final String STATUS = "status";
    public static final String PASSWORD = "password";
    public static final String CURRENT_PASSWORD = "current_password";
    public static final String NEW_PASSWORD = "new_password";
    public static final String ID = "id";

    public static void create(String login, String password) throws IOException {
        if(!FormatUtils.isCorrectName(login))
            throw new ApiException("Wrong login format", 1);
        if(ProfileUtils.isNicknameExist(login))
            throw new ApiException("Login is already taken", 2);
        if(!FormatUtils.isCorrectPassword(password))
            throw new ApiException("Wrong password format", 3);

        int maxId = 0;
        for(Profile profile : Profile.getProfiles())
            maxId = Math.max(maxId, profile.getId());
        int id = maxId + 1;

        String path = profilesFolder + "/" + id;

        Files.createDirectories(Paths.get(path));

        SettingsFile data = new SettingsFile(path + "/" + dataFile);
        data.set(LOGIN, login);
        data.set(PASSWORD, DigestUtils.md2Hex(password));
        data.set(STATUS, "Гость");
        data.set(EMAIL, "null");
        data.set(UUID, java.util.UUID.randomUUID().toString());
        data.set(CREATION_TIME, System.currentTimeMillis());

        Files.createFile(Paths.get(path + "/" + ipFile));
        Files.createFile(Paths.get(path + "/" + keysFile));
        Files.createFile(Paths.get(path + "/" + mailCodesFile));
        Files.createFile(Paths.get(path + "/" + passwordResetHashesFile));
        Files.copy(Profile.class.getResourceAsStream("/steve.png"), Paths.get(path + "/" + skinFile));
        Files.copy(Profile.class.getResourceAsStream("/elytra.png"), Paths.get(path + "/" + elytraFile));
        Files.copy(Profile.class.getResourceAsStream("/cape.png"), Paths.get(path + "/" + capeFile));
    }

    public static int getProfilesCount(){
        try {
            Files.createDirectories(Paths.get(Profile.profilesFolder));
        }catch (Exception ignored){}
        return Objects.requireNonNull(new File(Profile.profilesFolder).list()).length;
    }

    public static int[] getIds(){
        String[] ids_text = new File(Profile.profilesFolder).list((dir, name) -> {
            File[] child = new File(dir.getAbsolutePath() + File.separator + name).listFiles();
            if(child != null && child.length > 0){
                for (File value : child)
                    if (value.getName().equals("data.yaml"))
                        return true;
            }
            return false;
        });
        if(ids_text == null)
            return new int[0];
        int[] ids = new int[ids_text.length];
        for(int i = 0; i < ids_text.length; i++) {
            try {
                ids[i] = Integer.parseInt(ids_text[i]);
            }catch (Exception ignored){}
        }
        Arrays.sort(ids);
        return ids;
    }

    public static Profile[] getProfiles(){
        ArrayList<Profile> profiles = new ArrayList<>();
        for(int id : getIds()){
            try{
                profiles.add(new Profile(id));
            }catch (Exception ignored){}
        }
        return profiles.toArray(new Profile[0]);
    }

    public static Profile getByName(String name){
        for(Profile profile : Profile.getProfiles())
            if(profile.data.get(LOGIN).equals(name))
                return profile;
        return null;
    }

    public static Profile getByEmail(String email){
        for(Profile profile : Profile.getProfiles())
            if(profile.data.get(EMAIL).equals(email))
                return profile;
        return null;
    }

    public static Profile get(String key){
        for(Profile profile : Profile.getProfiles())
            if(profile.Token.containsKey(key))
                return profile;
        return null;
    }

    public static Profile get(String login, String password){
        for(int id : Profile.getIds()){
            try {
                Profile profile = new Profile(id);
                if(profile.Data.getValue(LOGIN).equals(login) && ProfileUtils.arePasswordsEquals(profile.data.get(PASSWORD), password))
                    return profile;
            }catch (Exception ignored){}
        }
        return null;
    }

    public final SettingsFile data;
    private final int id;

    public Data Data = new Data();
    public Email Email = new Email();
    public Password Password = new Password();
    public IP IP = new IP();
    public Token Token = new Token();

    public Profile(int id){
        this.id = id;
        data = new SettingsFile(getFolder() + File.separator + dataFile);
    }

    public void remove(){
        try {
            MIO.delete(getFolder());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkStatus(String status){
        if(!Data.getValue(STATUS).equals(status))
            throw new ApiException("Account does not have permission to perform this method", 26);
    }

    public int getId(){
        return id;
    }

    public class Token{

        public String create(){
            String key = ProfileUtils.generateAccessToken();
            ProfileUtils.appendFile(getFolder() + "/" + keysFile, key);
            return key;
        }

        public void remove(String key){
            ProfileUtils.fileLineFilter(getFolder() + "/" + keysFile, line -> !key.equals(line));
        }

        public boolean containsKey(String key){
            return ProfileUtils.fileLinePredicate(getFolder() + "/" + keysFile, key::equals);
        }

    }

    public class Email {

        public void reset(){
            data.set(EMAIL, "null");
        }

        public boolean sendCode(String email){
            String code = ProfileUtils.generateMailCode();
            ProfileUtils.appendFile(getFolder() + "/" + mailCodesFile, System.currentTimeMillis() + "," + email + "," + code);
            return ServerMain.MailService.send(email, ServerMain.Settings.getEmailCodeTitle(), ServerMain.Settings.getEmailCodeText().replace("{code}", code).replace("{name}", Data.getValue(LOGIN)));
        }

        public boolean sendPasswordChangeCode(String email){
            String code = ProfileUtils.generateMailCode();
            ProfileUtils.appendFile(getFolder() + "/" + mailCodesFile, System.currentTimeMillis() + "," + email + "," + code);
            return ServerMain.MailService.send(email, ServerMain.Settings.getEmailPasswordChangeTitle(), ServerMain.Settings.getEmailPasswordChangeText().replace("{code}", code).replace("{name}", Data.getValue(LOGIN)));
        }

        public void removeInvalid(){
            ProfileUtils.fileLineFilter(getFolder() + "/" + mailCodesFile, line ->
                    System.currentTimeMillis() - Long.parseLong(line.split(",")[0]) < 1000 * 60 * MAIL_CODE_VALID_MINUTES);
        }

        public boolean containsCode(String code){
            removeInvalid();
            return ProfileUtils.fileLinePredicate(getFolder() + "/" + mailCodesFile, line ->
                    line.split(",")[2].equals(code));
        }

        public boolean containsCode(String email, String code){
            removeInvalid();
            return ProfileUtils.fileLinePredicate(getFolder() + "/" + mailCodesFile, line ->
                    line.split(",")[1].equals(email) && line.split(",")[2].equals(code));
        }

        public void removeCode(String code){
            ProfileUtils.fileLineFilter(getFolder() + "/" + mailCodesFile, line ->
                    !(line.split(",")[2].equals(code)));
        }

        public void removeCode(String email, String code){
            ProfileUtils.fileLineFilter(getFolder() + "/" + mailCodesFile, line ->
                    !(line.split(",")[1].equals(email) && line.split(",")[2].equals(code)));
        }

        public boolean isConfirmed(){
            return !Data.getValue(EMAIL).equals("null");
        }

        public boolean useCode(String code){
            if(containsCode(code)){
                removeCode(code);
                return true;
            }
            return false;
        }

        public boolean useCode(String email, String code){
            if(containsCode(email, code)){
                removeCode(email, code);
                return true;
            }
            return false;
        }
    }

    public class Password {

        public String createChangeHash(){
            String hash = ProfileUtils.generatePasswordChangeHash();
            ProfileUtils.appendFile(getFolder() + "/" + passwordResetHashesFile, hash);
            return hash;
        }

        public void removeChangeHash(String hash){
            ProfileUtils.fileLineFilter(getFolder() + "/" + passwordResetHashesFile, line -> !hash.equals(line));
        }

        public boolean containsChangeHash(String hash){
            return ProfileUtils.fileLinePredicate(getFolder() + "/" + passwordResetHashesFile, hash::equals);
        }

        public void change(String hash, String newPassword, String currentToken){
            if(containsChangeHash(hash)) {
                removeChangeHash(hash);
                set(newPassword, currentToken);
            }else
                throw new RuntimeException("Wrong change hash");
        }

        public void reset(){
            String newPassword = ProfileUtils.generatePassword();
            set(newPassword);
            ServerMain.MailService.send(Data.getValue(EMAIL), ServerMain.Settings.getEmailPasswordTitle(), ServerMain.Settings.getEmailPasswordText()
                    .replace("{password}", newPassword).replace("{name}", Data.getValue(LOGIN)));
        }

        public String get(){
            return data.get(PASSWORD);
        }

        public void set(String password){
            set(password, null);
        }

        public void set(String password, String currentToken){
            if(!FormatUtils.isCorrectPassword(password))
                throw new RuntimeException("Incorrect password format");
            data.set(PASSWORD, DigestUtils.md2Hex(password));
            ProfileUtils.fileLineFilter(getFolder() + "/" + keysFile, line -> line.equals(currentToken));
            ProfileUtils.fileLineFilter(getFolder() + "/" + mailCodesFile, line -> false);
        }
    }

    public class Data{

        public ImageLink getSkin() throws IOException {
            if(!Files.exists(Paths.get(getFolder() + "/" + skinFile)))
                Files.copy(Profile.class.getResourceAsStream("/steve.png"), Paths.get(getFolder() + "/" + skinFile));
            return new ImageLink(new File(getFolder() + "/" + skinFile));
        }

        public ImageLink getCape() throws IOException {
            if(!Files.exists(Paths.get(getFolder() + "/" + capeFile)))
                Files.copy(Profile.class.getResourceAsStream("/cape.png"), Paths.get(getFolder() + "/" + capeFile));
            return new ImageLink(new File(getFolder() + "/" + capeFile));
        }

        public ImageLink getElytra() throws IOException {
            if(!Files.exists(Paths.get(getFolder() + "/" + elytraFile)))
                Files.copy(Profile.class.getResourceAsStream("/elytra.png"), Paths.get(getFolder() + "/" + elytraFile));
            return new ImageLink(new File(getFolder() + "/" + elytraFile));
        }

        public String getValue(String parameter){
            return get(parameter).get(parameter);
        }

        public HashMap<String, String> get(String... parameters){
            HashMap<String, String> out_data = new HashMap<>();

            List<String> availableData = Arrays.asList(ID, LOGIN, EMAIL, STATUS, CREATION_TIME);

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

        public void set(JSONObject object, JSONObject confirms){
            HashMap<String, String> map = new HashMap<>();
            for(Map.Entry<String, Object> entry : object.toMap().entrySet())
                map.put(entry.getKey(), entry.getValue() + "");

            HashMap<String, String> confirms_map = new HashMap<>();
            for(Map.Entry<String, Object> entry : confirms.toMap().entrySet())
                confirms_map.put(entry.getKey(), entry.getValue() + "");
            set(map, confirms_map);
        }

        public void set(String... parameters){
            set(null, parameters);
        }

        public void set(HashMap<String, String> confirms, String... parameters){
            HashMap<String, String> parameterMap = new HashMap<>();
            for(int i = 0; i < parameters.length; i += 2)
                parameterMap.put(parameters[0], parameters[1]);
            set(parameterMap, confirms);
        }

        public void set(HashMap<String, String> fields, HashMap<String, String> confirms){
            HashMap<String, FieldSetter> setters = new HashMap<>();

            ApiException PASSWORD_REQUIRED = new ApiException("Current password required", 1);
            ApiException CODE_REQUIRED = new ApiException("Email code required", 2);
            ApiException INCORRECT_PASSWORD = new ApiException("Current password is incorrect", 3);
            ApiException INCORRECT_EMAIL_CODE = new ApiException("Current email code is incorrect", 4);
            ApiException INCORRECT_LOGIN_FORMAT = new ApiException("Incorrect login format", 5);
            ApiException LOGIN_EXIST = new ApiException("Required login is already exist", 6);
            ApiException INCORRECT_EMAIL_FORMAT = new ApiException("Incorrect email format", 7);
            ApiException EMAIL_IS_ALREADY_BOUND = new ApiException("Email is already bound", 8);

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
                addCondition(EMAIL_IS_ALREADY_BOUND, email -> !ProfileUtils.isEmailExist(email));
                setAction(value -> data.set(EMAIL, value));
            }});

            // Logic
            boolean passwordConfirmed = false;
            boolean emailConfirmed = false;

            if(confirms != null) {
                if (confirms.containsKey(EMAIL_CODE)) {
                    String email = fields.containsKey(EMAIL) ? fields.get(EMAIL) : getValue(EMAIL);

                    if (Email.useCode(email, confirms.get(EMAIL_CODE)))
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
        }

        class FieldSetter{

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
    }

    public class IP{

        public void set(String ip){
            try {
                Files.write(Paths.get(getFolder() + "/" + ipFile), Collections.singletonList(ip));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public String get(){
            try {
                return Files.readAllLines(Paths.get(getFolder() + "/" + ipFile)).get(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public String toString() {
        return "Profile{" +
                "id=" + id +
                '}';
    }

    public String getFolder(){
        return profilesFolder + File.separator + id;
    }

}
