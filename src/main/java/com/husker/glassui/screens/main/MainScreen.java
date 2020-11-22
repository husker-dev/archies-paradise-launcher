package com.husker.glassui.screens.main;

import com.husker.glassui.components.BlurTabPanel;
import com.husker.glassui.screens.main.control.ControlPanel;
import com.husker.glassui.screens.main.keys.KeysPanel;
import com.husker.glassui.screens.main.info.InfoPanel;
import com.husker.glassui.screens.main.people.PeoplePanel;
import com.husker.glassui.screens.main.play.PlayPanel;
import com.husker.glassui.screens.main.profile.ProfilePanel;
import com.husker.glassui.screens.main.settings.SettingsPanel;
import com.husker.glassui.screens.main.social.VkGroupPanel;
import com.husker.glassui.screens.main.social.YoutubePanel;
import com.husker.launcher.components.TransparentPanel;
import javafx.scene.layout.BorderPane;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;


public class MainScreen extends AbstractMainScreen {

    private NewsPanel newsPanel;
    private UpdatePanel updatePanel;

    private BlurTabPanel tabPanel;
    private VkGroupPanel vkPanel;
    private YoutubePanel youtubePanel;
    private ProfilePanel profilePanel;
    private PlayPanel playPanel;
    private InfoPanel infoPanel;

    private KeysPanel keysPanel;
    private PeoplePanel peoplePanel;
    private ControlPanel controlPanel;

    public void onInit(){
        try{
            super.onInit();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    void onMenuInit(TransparentPanel menu) {
        try {
            menu.setLayout(new BorderLayout());

            menu.add(tabPanel = new BlurTabPanel(this) {{
                setPreferredHeight(450);
                addTab("play", "Игра", getIcon(getLauncher().Resources.Icon_Play), playPanel = new PlayPanel(MainScreen.this));
                addTab("profile", "Профиль", getIcon(getLauncher().Resources.Icon_Profile), profilePanel = new ProfilePanel(MainScreen.this));
                addTab("news", "Новости", getIcon(getLauncher().Resources.Icon_Book), vkPanel = new VkGroupPanel(MainScreen.this));
                addTab("videos", "Видео", getIcon(getLauncher().Resources.Icon_Videos), youtubePanel = new YoutubePanel(MainScreen.this));
                addBottomTab("settings", "Настройки", getIcon(getLauncher().Resources.Icon_Settings), new SettingsPanel(MainScreen.this));
                addBottomTab("info", "Информация", getIcon(getLauncher().Resources.Icon_Info), infoPanel = new InfoPanel(MainScreen.this));

                addTabChangedListener(id -> onShowPanelEvent());
            }});

            controlPanel = new ControlPanel(MainScreen.this);
            keysPanel = new KeysPanel(MainScreen.this);
            peoplePanel = new PeoplePanel(MainScreen.this);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void onShow(){
        try {
            onShowPanelEvent();
            newsPanel.load();

            tabPanel.removeTab("control");
            tabPanel.removeTab("keys");
            tabPanel.removeTab("people");
            if(getLauncher().API.PlayerInfo.getStatus().equals("Администратор")){
                tabPanel.addTab("control", "Управление", getIcon(getLauncher().Resources.Icon_Folder), controlPanel);
                tabPanel.addBottomTab("keys", "Ссылки", getIcon(getLauncher().Resources.Icon_Code), keysPanel);
                tabPanel.addBottomTab("people", "Пользователи", getIcon(getLauncher().Resources.Icon_People), peoplePanel);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void onShowPanelEvent(){
        String id = tabPanel.getSelectedTabId();
        if (id.equals("news"))
            vkPanel.onShow();
        if (id.equals("videos"))
            youtubePanel.onShow();
        if (id.equals("profile"))
            profilePanel.onShow();
        if (id.equals("play"))
            playPanel.onShow();
        if (id.equals("keys"))
            keysPanel.onShow();
        if(id.equals("info"))
            infoPanel.onShow();
    }

    void onRightMenuInit(TransparentPanel menu) {
        menu.setLayout(new BorderLayout());
        menu.add(newsPanel = new NewsPanel(this));
    }

    void onLeftMenuInit(TransparentPanel menu) {
        menu.setLayout(new BorderLayout());
        menu.add(updatePanel = new UpdatePanel(this), BorderLayout.NORTH);
    }

    public static ImageIcon getIcon(BufferedImage image){
        return new ImageIcon(image.getScaledInstance(25, 25, BufferedImage.SCALE_SMOOTH));
    }

}
