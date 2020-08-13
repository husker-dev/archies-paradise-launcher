package com.husker.launcher.ui.impl.glass.screens;

import com.alee.extended.label.WebStyledLabel;
import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.style.StyleId;
import com.husker.launcher.LauncherWindow;
import com.husker.launcher.Resources;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.ui.impl.glass.GlassUI;
import com.husker.launcher.ui.impl.glass.components.BlurButton;
import com.husker.launcher.ui.impl.glass.components.BlurPanel;
import com.husker.launcher.ui.impl.glass.components.BlurTabPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static java.awt.FlowLayout.CENTER;

public class MainScreen extends AbstractMainScreen {

    void onMenuInit(WebPanel menu) {
        menu.setLayout(new BorderLayout());

        menu.add(new BlurTabPanel(this){{
            setPreferredHeight(400);
            addTab("play", "Игра", new WebPanel(StyleId.panelTransparent){{

            }});
            addTab("profile", "Профиль", new WebPanel(StyleId.panelTransparent){{

            }});
            addTab("info", new ImageIcon(getLauncher().Resources.Info.getScaledInstance(30, 30, BufferedImage.SCALE_SMOOTH)), new WebPanel(StyleId.panelTransparent){{
                setLayout(new VerticalFlowLayout(0, 6));
                setMargin(10, 10, 0, 10);
                add(new WebStyledLabel(WebStyledLabel.CENTER){{
                    setPreferredWidth(300);
                    setPreferredHeight(100);
                    setMaximumRows(30);
                    setForeground(GlassUI.Colors.labelText);
                    setFont(Resources.Fonts.ChronicaPro_ExtraBold.deriveFont(28f));
                    setText(getLauncher().getConfig().get("title"));
                }});

                add(new BlurPanel(MainScreen.this) {
                    {
                        setPreferredHeight(220);
                        setLayout(new FlowLayout(CENTER));
                        setMargin(10, 0, 0, 0);
                        add(createInfoParameter("Версия", LauncherWindow.VERSION));
                        add(createInfoParameter("Создатель", "Husker"));
                        add(createInfoParameter("Обладатель", "Archie"));
                        add(createInfoParameter("GitHub", "Тык"));
                    }

                    public void onBlurApply(BlurParameter parameter) {
                        super.onBlurApply(parameter);
                        parameter.setShadowType(BlurParameter.ShadowType.INNER);
                    }
                });

            }});
        }});
        //menu.setPreferredHeight(100);
    }

    public WebLabel createLabel(String text){
        return new WebLabel(text){{
            setForeground(new Color(50, 50, 50));
            setHorizontalAlignment(CENTER);
            setPreferredHeight(20);
            setFont(Resources.Fonts.ChronicaPro_ExtraBold.deriveFont(20f));
        }};
    }

    public WebPanel createInfoParameter(String title, String value){
        WebLabel label = GlassUI.createSimpleLabel(value);
        label.setForeground(GlassUI.Colors.labelLightText);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return createInfoParameter(title, label);
    }

    public WebPanel createInfoParameter(String title, Component component){
        WebPanel panel = new WebPanel(StyleId.panelTransparent);
        panel.setPreferredSize(130, 55);
        panel.setLayout(new BorderLayout());

        panel.add(createLabel(title), BorderLayout.NORTH);
        panel.add(component);
        panel.add(Box.createRigidArea(new Dimension(0, 15)), BorderLayout.SOUTH);
        return panel;
    }

}
