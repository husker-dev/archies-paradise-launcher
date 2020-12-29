package com.husker.glassui.screens.main.profile.edit;

import com.husker.glassui.screens.Message;
import com.husker.glassui.screens.SimpleLoadingScreen;
import com.husker.glassui.screens.main.MainScreen;
import com.husker.launcher.api.API;

import java.util.HashMap;

import static com.husker.launcher.api.API.*;

public class InfoApplying extends SimpleLoadingScreen {

    public void onContentInit() {
        setTitle("Редактирование");
        setText("Применение...");
    }

    public void process() {
        String currentPassword = getParameterValue(API.PASSWORD);
        String emailCode = null;

        HashMap<String, String> changedParameters = new HashMap<>();
        if(getParameters().containsKey(API.LOGIN))
            changedParameters.put(API.LOGIN, getParameterValue(API.LOGIN));

        if(getParameters().containsKey(API.EMAIL)) {
            changedParameters.put(API.EMAIL, getParameterValue(API.EMAIL));
            emailCode = getParameterValue(API.EMAIL_CODE);
        }

        try {
            getLauncher().User.setData(currentPassword, emailCode, changedParameters);
            getLauncherUI().setScreen(MainScreen.class);
        } catch (IncorrectCurrentPasswordException e) {
            showErrorMessage("Неверный текущий пароль!");
        } catch (LoginAlreadyExistException e) {
            showErrorMessage("Данный логин уже существует!");
        } catch (IncorrectEmailCodeException e) {
            showErrorMessage("Неверный код подтверждения!");
        } catch (IncorrectEmailFormatException e) {
            showErrorMessage("Недопустимый формат почты!");
        } catch (CurrentPasswordRequiredException e) {
            showErrorMessage("Требуется ввести текущий пароль!");
        } catch (EmailCodeRequiredException e) {
            showErrorMessage("Требуется ввести код подтверждения!");
        } catch (EmailAlreadyExistException e) {
            showErrorMessage("Данный email уже привязан к аккаунту!");
        } catch (IncorrectLoginFormatException e) {
            showErrorMessage("Недопустимый формат логина!");
        }
    }

    private void showErrorMessage(String text){
        Message.showMessage(getLauncherUI(), "Проблемка", text, InfoEdit.class, getParameters());
    }
}
