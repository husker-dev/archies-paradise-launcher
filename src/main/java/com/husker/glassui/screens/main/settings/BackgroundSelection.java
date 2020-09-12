package com.husker.glassui.screens.main.settings;

import com.alee.laf.label.WebLabel;
import com.husker.glassui.GlassUI;
import com.husker.glassui.components.BlurButton;
import com.husker.glassui.components.BlurScalableImage;
import com.husker.glassui.screens.SimpleTitledScreen;
import com.husker.launcher.Resources;
import com.husker.launcher.components.TransparentPanel;
import com.husker.launcher.ui.blur.BlurParameter;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static java.awt.FlowLayout.CENTER;

public class BackgroundSelection extends SimpleTitledScreen {

    public BackgroundSelection() {
        super("Настройки", "Фон");
    }

    public void onMenuInit(TransparentPanel panel) {
        panel.setLayout(new BorderLayout());
        panel.add(new WebLabel("Выберите фон"){{
            setMargin(15, 0, 0, 0);
            setForeground(GlassUI.Colors.labelLightText);
            setVerticalAlignment(CENTER);
            setHorizontalAlignment(CENTER);
            setFont(Resources.Fonts.ChronicaPro_ExtraBold.deriveFont(18f));
        }}, BorderLayout.NORTH);

        panel.add(new TransparentPanel(){{
            setLayout(new FlowLayout(CENTER, 15, 15));

            setPreferredWidth(550);
            setPreferredHeight(250);

            for(int i = 1; i < getLauncher().Resources.Background.length; i++){
                final int I = i;
                BlurScalableImage scalableImage = new BlurScalableImage(BackgroundSelection.this, getLauncher().Resources.Background[i]){
                    {
                        setFitType(FitType.FIT_XY);
                    }
                    public void onBlurApply(BlurParameter parameter, Component component) {
                        super.onBlurApply(parameter, component);

                        if(getImage() == getLauncher().getBackgroundImage()) {
                            parameter.setTextureAlpha(0.4f);
                            parameter.setShadowType(BlurParameter.ShadowType.INNER);
                            parameter.setShadowSize(5);
                        }
                    }
                };
                scalableImage.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        getLauncher().setBackgroundImage(getLauncher().Resources.Background[I]);
                        getLauncher().getSettings().setBackgroundIndex(I);
                        getLauncher().updateUI();
                    }
                });
                scalableImage.setPreferredSize(new Dimension(150, 100));
                add(scalableImage);
            }
        }});
    }

    public void onButtonsInit(TransparentPanel panel) {
        panel.add(new BlurButton(this, "Назад"){{
            addActionListener(e -> getLauncherUI().setScreen("main"));
            setMargin(0, 25, 0, 25);
        }});
    }
}
