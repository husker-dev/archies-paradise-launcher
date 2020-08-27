package com.husker.launcher.ui.impl.glass.screens.main;

import com.alee.laf.panel.WebPanel;
import com.alee.managers.style.StyleId;
import com.husker.launcher.ui.impl.glass.components.BlurTabPanel;
import com.husker.launcher.ui.impl.glass.screens.main.profile.ProfilePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;


public class MainScreen extends AbstractMainScreen {

    private BlurTabPanel tabPanel;
    private VkGroupPanel vkPanel;
    private YoutubePanel youtubePanel;
    private ProfilePanel profilePanel;

    void onMenuInit(WebPanel menu) {
        menu.setLayout(new BorderLayout());

        menu.add(tabPanel = new BlurTabPanel(this){{
            setPreferredHeight(450);
            addTab("play", "Игра", getIcon(getLauncher().Resources.Icon_Play), new WebPanel(StyleId.panelTransparent){{

            }});
            addTab("profile", "Профиль", getIcon(getLauncher().Resources.Icon_Profile), profilePanel = new ProfilePanel(MainScreen.this));
            addTab("news", "Новости", getIcon(getLauncher().Resources.Icon_Book), vkPanel = new VkGroupPanel(MainScreen.this));
            addTab("videos", "Видео", getIcon(getLauncher().Resources.Icon_Videos), youtubePanel = new YoutubePanel(MainScreen.this));
            addBottomTab("settings", "Настройки", getIcon(getLauncher().Resources.Icon_Settings), new SettingsPanel(MainScreen.this));
            addBottomTab("info", "Информация", getIcon(getLauncher().Resources.Icon_Info), new InfoPanel(MainScreen.this));

            addTabChangedListener(id -> {
                if(id.equals("news"))
                    vkPanel.onShow();
                if(id.equals("videos"))
                    youtubePanel.onShow();
                if(id.equals("profile"))
                    profilePanel.onShow();
            });
        }});
    }

    public void onShow(){
        String id = tabPanel.getSelectedTabId();

        if(id.equals("news"))
            vkPanel.onShow();
        if(id.equals("videos"))
            youtubePanel.onShow();
        if(id.equals("profile"))
            profilePanel.onShow();
    }

    // YouTube videos panel
    void onRightMenuInit(WebPanel menu) {
        menu.setLayout(new BorderLayout());
        menu.add(new NewsPanel(this));
    }

    public static ImageIcon getIcon(BufferedImage image){
        return new ImageIcon(image.getScaledInstance(25, 25, BufferedImage.SCALE_SMOOTH));
    }

}
