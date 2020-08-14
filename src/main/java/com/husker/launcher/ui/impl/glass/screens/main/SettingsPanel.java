package com.husker.launcher.ui.impl.glass.screens.main;

import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.style.StyleId;
import com.husker.launcher.Resources;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.impl.glass.GlassUI;
import com.husker.launcher.ui.impl.glass.components.BlurTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SettingsPanel extends WebPanel {

    private final Screen screen;

    public SettingsPanel(Screen screen){
        super(StyleId.panelTransparent);
        this.screen = screen;

        setLayout(new VerticalFlowLayout(0, 10));
        setMargin(25, 25, 10, 10);

        add(createLabel("Лаунчер"));
        add(new WebPanel(StyleId.panelTransparent){{
            setMargin(0, 20, 0, 0);
            setLayout(new VerticalFlowLayout(0, 5));
            add(new Checkbox("Сохранить пароль"));
            add(new Checkbox("Автоматический запуск игры"));
        }});

    }



    public WebLabel createLabel(String text){
        return new WebLabel(text){{
            setForeground(new Color(50, 50, 50));
            setHorizontalAlignment(LEFT);
            setPreferredHeight(23);
            setFont(Resources.Fonts.ChronicaPro_ExtraBold.deriveFont(23f));
        }};
    }

    public class Checkbox extends WebLabel{

        private boolean checked = false;

        public Checkbox(String text){
            super(text);
            setChecked(checked);
            setForeground(GlassUI.Colors.labelLightText);
            //setPreferredHeight(16);
            setFont(Resources.Fonts.ChronicaPro_ExtraBold);

            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    setChecked(!isChecked());
                }
            });
        }

        public boolean isChecked() {
            return checked;
        }

        public void setChecked(boolean checked) {
            this.checked = checked;

            if(checked)
                setIcon(new ImageIcon(screen.getLauncher().Resources.Icon_Checkbox_On.getScaledInstance(25, 25, Image.SCALE_SMOOTH)));
            else
                setIcon(new ImageIcon(screen.getLauncher().Resources.Icon_Checkbox_Off.getScaledInstance(25, 25, Image.SCALE_SMOOTH)));
        }
    }
}
