package com.husker.glassui.screens.main.play;

import com.husker.glassui.screens.SimpleLoadingScreen;
import com.husker.launcher.minecraft.LaunchManager;
import com.husker.mio.processes.DeletingProcess;

public class RemovingProcess extends SimpleLoadingScreen {

    public void onContentInit() {
        setTitle("Клиент");
    }

    public void process() {
        setText("Удаление... 0%");
        new Thread(() -> {
            try {
                new DeletingProcess(LaunchManager.clientsFolderFile).addProgressListener(e -> {
                    setText((int)e.getPercent() + "%");
                }).startSync();
            } catch (Exception e) {
                e.printStackTrace();
            }
            getLauncherUI().setScreen(ClientSettings.class);
        }).start();
    }

}
