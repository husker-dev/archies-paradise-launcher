package com.husker.launcher;

import com.husker.launcher.api.API;
import com.husker.launcher.settings.UserInfoFile;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import static com.husker.launcher.api.API.*;

public class User {

    private String token = "yes_my_name_is_token";

    private String name = "Имя";
    private String email = "Почта";
    private String status = "Статус";
    private long id = -1;
    private BufferedImage skin = Resources.Skin_Steve;
    private BufferedImage cape, elytra;
    private boolean emailConfirmed = false;

    public final UserInfoFile File = new UserInfoFile();

    private final ArrayList<Runnable> logoutListeners = new ArrayList<>();

    public User(){
    }

    private void errorLogoutEvent(){
        logoutListeners.forEach(Runnable::run);
    }

    public void addLogoutListener(Runnable listener){
        logoutListeners.add(listener);
    }

    public boolean containSaved(){
        return File.hasAccount();
    }

    public void auth(String login, String password) throws WrongAuthDataException, InternalAPIException {
        this.token = API.Auth.getAccessToken(login, password);
        File.setLogin(login);
        File.setPassword(password);
        updateData();
    }

    public void updateData() {
        try {
            HashMap<String, String> data = API.Profile.getData(token, LOGIN, EMAIL, ID, STATUS);

            name = data.get(LOGIN);
            email = data.get(EMAIL);
            id = Long.parseLong(data.get(ID));
            status = data.get(STATUS);

            File.setLogin(name);

            emailConfirmed = API.Profile.isEmailConfirmed(token);
            skin = API.Profile.getSkin(token);
            cape = API.Profile.getCape(token);
            elytra = API.Profile.getElytra(token);
        } catch (InternalAPIException | WrongAccessTokenException e) {
            errorLogoutEvent();
        }
    }

    public void sendConfirmCode(String email) throws EmailCodeSendingException, EmailAlreadyExistException {
        try {
            API.Profile.sendEmailCode(token, email);
        }catch (InternalAPIException | WrongAccessTokenException e){
            errorLogoutEvent();
        }
    }

    public boolean confirmMail(String email, String code) throws EmailIsNotSpecifiedException, IncorrectEmailCodeException {
        try {
            API.Profile.confirmEmail(token, email, code);
            return emailConfirmed = API.Profile.isEmailConfirmed(token);
        }catch (InternalAPIException | WrongAccessTokenException  e){
            errorLogoutEvent();
            return false;
        }
    }

    public String getNickname(){
        return name;
    }

    public String getEmail(){
        return email;
    }

    public String getToken(){
        return token;
    }

    public long getId(){
        return id;
    }

    public BufferedImage getSkin(){
        return skin;
    }

    public BufferedImage getCape(){
        return cape;
    }

    public BufferedImage getElytra(){
        return cape;
    }

    public String getStatus() {
        return status;
    }

    public boolean isEmailConfirmed() {
        return emailConfirmed;
    }

    public void setData(String currentPassword, String emailCode, HashMap<String, String> data) throws IncorrectCurrentPasswordException, LoginAlreadyExistException, IncorrectEmailCodeException, IncorrectEmailFormatException, CurrentPasswordRequiredException, EmailCodeRequiredException, IncorrectLoginFormatException, EmailAlreadyExistException {
        try {
            Profile.setData(token, data, currentPassword, emailCode);
            updateData();
            if(data.containsKey(PASSWORD))
                File.setPassword(data.get(PASSWORD));
        } catch (InternalAPIException | WrongAccessTokenException e) {
            errorLogoutEvent();
        }
    }

    public void bindIP() {
        try {
            Profile.bindIP(token);
        } catch (InternalAPIException | WrongAccessTokenException e) {
            errorLogoutEvent();
        }
    }

    public void logout(){
        token = "";
        name = "";
        email = "";
        id = -1;
        logoutListeners.forEach(Runnable::run);
        File.setPassword("null");
    }

    public void setCape(BufferedImage image){
        try {
            Profile.setCape(token, image);
            updateData();
        } catch (InternalAPIException | WrongAccessTokenException | SkinTooLargeException e) {
            errorLogoutEvent();
        }
    }

    public void setCape(String name){
        try {
            Profile.setCape(token, name);
            updateData();
        } catch (InternalAPIException | WrongAccessTokenException | SkinNameNotFoundException e) {
            errorLogoutEvent();
        }
    }

    public void setSkin(BufferedImage image) throws SkinTooLargeException {
        try {
            Profile.setSkin(token, image);
            updateData();
        } catch (InternalAPIException | WrongAccessTokenException e) {
            errorLogoutEvent();
        }
    }

    public void setSkin(String category, String name) throws CategoryNotFoundException, SkinNameNotFoundException {
        try {
            Profile.setSkin(token, category, name);
            updateData();
        } catch (InternalAPIException | WrongAccessTokenException e) {
            errorLogoutEvent();
        }
    }
}
