package com.husker.glassui.screens.main.play;

import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.utils.NetUtils;
import com.husker.glassui.GlassUI;
import com.husker.glassui.components.BlurButton;
import com.husker.glassui.components.BlurCheckbox;
import com.husker.glassui.components.BlurTagPanel;
import com.husker.glassui.screens.SimpleTitledScreen;
import com.husker.glassui.screens.main.MainScreen;
import com.husker.launcher.managers.NetManager;
import com.husker.launcher.minecraft.LaunchManager;
import com.husker.launcher.settings.LauncherSettings;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.ui.components.TransparentPanel;
import com.husker.mio.FSUtils;
import com.husker.mio.MIO;

import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ClientSettings extends SimpleTitledScreen {

    private BlurCheckbox vrClient, gamepad;
    private BlurButton openDirectory, reset, remove;

    public ClientSettings() {
        super("Minecraft");
    }

    public void onMenuInit(TransparentPanel panel) {
        panel.setPreferredWidth(350);
        panel.setPreferredHeight(320);

        panel.setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 15));
        panel.setMargin(25, 15, 15, 25);

        panel.add(new BlurTagPanel(this, "Модификации"){{
            addContent(new TransparentPanel(){{
                setMargin(10, 15, 0, 0);
                setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 7));
                add(vrClient = new BlurCheckbox(ClientSettings.this, "Поддержка VR"){{
                    setOnAction(() -> LauncherSettings.setClientType(isChecked() ? "vr" : "non_vr"));
                }});
                add(gamepad = new BlurCheckbox(ClientSettings.this, "Поддержка геймпада"){{
                    setOnAction(() -> LauncherSettings.setControllerSupport(isChecked()));
                }});
            }});
        }});

        panel.add(new BlurTagPanel(this, "Управление"){{
            addContent(new TransparentPanel(){{
                setMargin(15, 15, 0, 0);
                setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 10));

                add(openDirectory = new BlurButton(ClientSettings.this, "Открыть папку"){{
                    addActionListener(e -> {
                        new Thread(() -> {
                            try {
                                NetManager.openLink(LaunchManager.clientsFolderFile.toURI().toURL().toString());
                            }catch (Exception ex){
                                ex.printStackTrace();
                            }
                        }).start();
                    });
                }});
                add(reset = new BlurButton(ClientSettings.this, "Сбросить настройки"){
                    {
                        addActionListener(e -> {
                            for(File file : FSUtils.getChildren(LaunchManager.clientsFolderFile)){
                                removeIfExist(new File(file, "optionsshaders.txt"));
                                removeIfExist(new File(file, "optionsof.txt"));
                                removeIfExist(new File(file, "options.txt"));
                            }
                            updateButtons();
                        });
                    }
                    void removeIfExist(File file){
                        try {
                            if (Files.exists(Paths.get(file.getAbsolutePath())))
                                Files.delete(Paths.get(file.getAbsolutePath()));
                        }catch (Exception ignored){}
                    }
                });
                add(remove = new BlurButton(ClientSettings.this, "Удалить клиент"){
                    {
                        addActionListener(e -> getLauncherUI().setScreen(RemovingProcess.class));
                    }
                    public void onBlurApply(BlurParameter parameter, Component component) {
                        super.onBlurApply(parameter, component);
                        if(returnOnInvisible(parameter, component))
                            return;
                        if(component == this)
                            GlassUI.applyTransparentButton(parameter, isHovered());
                    }
                });
            }});
        }});
    }

    public void onButtonsInit(TransparentPanel panel) {
        panel.add(new BlurButton(this, "Назад"){{
            addActionListener(e -> getLauncherUI().setScreen(MainScreen.class));
            setPadding(40, 40);
        }}, new GridBagConstraints(){{
            this.gridx = 0;
            this.weightx = 1;
        }});
    }

    public void onShow() {
        vrClient.setChecked(LauncherSettings.getClientType().equals("vr"));
        gamepad.setChecked(LauncherSettings.getControllerSupport());
        updateButtons();
    }

    public void updateButtons(){
        boolean hasClients = false;
        for(File file : FSUtils.getChildren(LaunchManager.clientsFolderFile))
            if(!file.getName().endsWith("_to_save") && !file.getName().endsWith("_to_save" + File.separator))
                hasClients = true;

        openDirectory.setEnabled(hasClients);
        remove.setEnabled(hasClients);
        reset.setEnabled(hasClients);
    }
}
