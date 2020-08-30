package com.husker.launcher.ui.impl.glass.screens;

import com.alee.laf.panel.WebPanel;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.ui.impl.glass.components.BlurButton;
import com.husker.launcher.ui.impl.glass.components.BlurScalableImage;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static java.awt.FlowLayout.CENTER;

public class BackgroundSelection extends SimpleTitledScreen {

    public BackgroundSelection() {
        super("Фон");
    }

    public void onMenuInit(WebPanel panel) {
        panel.setLayout(new FlowLayout(CENTER, 15, 15));

        panel.setPreferredWidth(550);
        panel.setPreferredHeight(350);

        for(int i = 1; i < getLauncher().Resources.Background.length; i++){
            final int I = i;
            BlurScalableImage scalableImage = new BlurScalableImage(BackgroundSelection.this, getLauncher().Resources.Background[i]){
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
            panel.add(scalableImage);
        }
    }

    public void onButtonsInit(WebPanel panel) {
        panel.add(new BlurButton(this, "Назад"){{
            addActionListener(e -> getLauncherUI().setScreen("main"));
            setMargin(0, 15, 0, 15);
        }});
    }
}
