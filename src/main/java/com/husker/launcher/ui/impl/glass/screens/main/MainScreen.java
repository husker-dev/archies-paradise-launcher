package com.husker.launcher.ui.impl.glass.screens.main;

import com.alee.laf.panel.WebPanel;
import com.alee.managers.style.StyleId;
import com.husker.launcher.ui.impl.glass.components.BlurTabPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;


public class MainScreen extends AbstractMainScreen {
    BlurTabPanel panel;

    void onMenuInit(WebPanel menu) {
        menu.setLayout(new BorderLayout());

        menu.add(panel = new BlurTabPanel(this){{
            setPreferredHeight(450);
            addTab("play", "Игра", getIcon(getLauncher().Resources.Icon_Play), new WebPanel(StyleId.panelTransparent){{

            }});
            addTab("profile", "Профиль", getIcon(getLauncher().Resources.Icon_Profile), new WebPanel(StyleId.panelTransparent){{

            }});
            addTab("news", "Новости", getIcon(getLauncher().Resources.Icon_Book), new WebPanel(StyleId.panelTransparent){{

            }});
            addTab("videos", "Видео", getIcon(getLauncher().Resources.Icon_Videos), new WebPanel(StyleId.panelTransparent){{

            }});
            addBottomTab("settings", "Настройки", getIcon(getLauncher().Resources.Icon_Settings), new SettingsPanel(MainScreen.this));
            addBottomTab("info", "Информация", getIcon(getLauncher().Resources.Icon_Info), new InfoPanel(MainScreen.this));
        }});
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
