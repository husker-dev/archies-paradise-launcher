package com.husker.launcher.ui.impl.glass.screens;

import com.alee.laf.panel.WebPanel;
import com.alee.managers.style.StyleId;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.ui.impl.glass.GlassUI;
import com.husker.launcher.ui.impl.glass.TitledScreen;
import com.husker.launcher.ui.impl.glass.components.BlurButton;
import com.husker.launcher.ui.impl.glass.components.BlurPanel;
import com.husker.launcher.ui.impl.glass.components.BlurScalableImage;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import static java.awt.FlowLayout.CENTER;

public class BackgroundSelection extends Screen {

    public void onInit() {
        setLayout(new GridBagLayout());

        add(new BlurPanel(this, true){{
            setLayout(new BorderLayout());
            setPreferredWidth(550);
            setPreferredHeight(350);
            add(GlassUI.createTitleLabel("Фон"), BorderLayout.NORTH);

            add(new WebPanel(StyleId.panelTransparent){{
                setLayout(new FlowLayout(CENTER, 15, 15));

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
                            getLauncher().getSettings().set("background", I + "");
                            getLauncher().updateUI();
                        }
                    });
                    scalableImage.setPreferredSize(new Dimension(150, 100));
                    add(scalableImage);
                }
            }});

            add(new WebPanel(StyleId.panelTransparent){{
                setLayout(new FlowLayout(CENTER));
                add(new BlurButton(BackgroundSelection.this, "Назад"){{
                    addActionListener(e -> getLauncherUI().setScreen("main"));
                    setMargin(0, 15, 0, 15);
                }});
            }}, BorderLayout.SOUTH);
        }});
    }
}
