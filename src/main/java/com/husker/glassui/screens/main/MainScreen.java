package com.husker.glassui.screens.main;

import com.husker.glassui.components.BlurTabPanel;
import com.husker.glassui.screens.main.info.InfoPanel;
import com.husker.glassui.screens.main.play.PlayPanel;
import com.husker.glassui.screens.main.profile.ProfilePanel;
import com.husker.glassui.screens.main.settings.SettingsPanel;
import com.husker.glassui.screens.main.social.VkGroupPanel;
import com.husker.glassui.screens.main.social.YoutubePanel;
import com.husker.launcher.components.TransparentPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;


public class MainScreen extends AbstractMainScreen {

    private NewsPanel newsPanel;
    private BlurTabPanel tabPanel;
    private VkGroupPanel vkPanel;
    private YoutubePanel youtubePanel;
    private ProfilePanel profilePanel;
    private PlayPanel playPanel;

    void onMenuInit(TransparentPanel menu) {
        menu.setLayout(new BorderLayout());

        menu.add(tabPanel = new BlurTabPanel(this){{
            setPreferredHeight(450);
            addTab("play", "Игра", getIcon(getLauncher().Resources.Icon_Play), playPanel = new PlayPanel(MainScreen.this));
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

        newsPanel.load();

        if(id.equals("news"))
            vkPanel.onShow();
        if(id.equals("videos"))
            youtubePanel.onShow();
        if(id.equals("profile"))
            profilePanel.onShow();
        if(id.equals("play"))
            playPanel.onShow();
    }

    // YouTube videos panel
    void onRightMenuInit(TransparentPanel menu) {
        menu.setLayout(new BorderLayout());
        menu.add(newsPanel = new NewsPanel(this));
    }

    public static ImageIcon getIcon(BufferedImage image){
        return new ImageIcon(image.getScaledInstance(25, 25, BufferedImage.SCALE_SMOOTH));
    }

}
