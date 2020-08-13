package com.husker.launcher.ui.impl.glass.screens.login;

import com.husker.launcher.NetManager;
import com.husker.launcher.ui.impl.glass.SimpleLoadingScreen;
import com.husker.launcher.ui.impl.glass.Message;


public class AuthProcess extends SimpleLoadingScreen {

    public void onContentInit() {
        setTitle("Вход");
        setText("Проверка соединения...");
    }

    public void onShow() {
        if(getParameters().length != 2)
            setParameters(new String[]{"test1", "test2"});

        new Thread(() -> {
            setText("Проверка соединения...");

            java.util.List<NetManager.ServerStatus> server_status = NetManager.getServerOnlineStatus(getLauncher());
            if(server_status.contains(NetManager.ServerStatus.AUTH_ONLINE))
                setText("Авторизация...");
            else {
                Message.showMessage(getLauncherUI(), "Ошибка", "Сервер авторизации недоступен!", "login");
                return;
            }
        }).start();
    }

}
