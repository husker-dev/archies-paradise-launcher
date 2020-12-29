package com.husker.glassui.screens.main.settings;

import com.alee.laf.label.WebLabel;
import com.husker.glassui.GlassUI;
import com.husker.glassui.components.BlurButton;
import com.husker.glassui.components.BlurPagePanel;
import com.husker.glassui.components.BlurScalableImage;
import com.husker.glassui.screens.SimpleTitledScreen;
import com.husker.glassui.screens.main.MainScreen;
import com.husker.launcher.Resources;
import com.husker.launcher.settings.LauncherSettings;
import com.husker.launcher.ui.components.TransparentPanel;
import com.husker.launcher.ui.blur.BlurParameter;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static java.awt.FlowLayout.CENTER;

public class BackgroundSelection extends SimpleTitledScreen {

    private BlurPagePanel pages;
    private BlurScalableImage[] images = new BlurScalableImage[6];

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
            setFont(Resources.Fonts.getChronicaProExtraBold(18));
        }}, BorderLayout.NORTH);

        panel.add(new TransparentPanel(){{
            setLayout(new FlowLayout(CENTER, 15, 15));

            setPreferredWidth(550);
            setPreferredHeight(250);

            for(int i = 0; i < 6; i++){
                final int I = i;
                images[i] = new BlurScalableImage(BackgroundSelection.this){
                    {
                        setFitType(FitType.FILL_XY);
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
                images[i].addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        if(pages.getPage() * 6 + I < Resources.Background.length) {
                            getLauncher().setBackgroundImage(Resources.Background[pages.getPage() * 6 + I]);
                            LauncherSettings.setBackgroundIndex(pages.getPage() * 6 + I);
                            getLauncher().updateUI();
                        }
                    }
                });
                images[i].setPreferredSize(new Dimension(150, 100));
                add(images[i]);
            }
        }});

        panel.add(new TransparentPanel(){{
            setLayout(new FlowLayout(CENTER));
            add(pages = new BlurPagePanel(BackgroundSelection.this){{
                setPreferredWidth(400);
                int pages = Resources.Background.length / 6;
                if(Resources.Background.length % 6 != 0)
                    pages ++;

                setPages(pages);
                addPageListener(BackgroundSelection.this::setPage);
                setSelectedPage(0);
            }});
        }}, BorderLayout.SOUTH);

    }

    public void onShow() {
        super.onShow();
        pages.setSelectedPage(0);
        setPage(0);
    }

    public void setPage(int page){
        int from = 6 * page;

        for(int i = from; i < from + 6; i++){
            if(i < Resources.Background.length)
                images[i - from].setImage(Resources.Background[i]);
            else
                images[i - from].setImage(null);
        }
    }

    public void onButtonsInit(TransparentPanel panel) {
        panel.add(new BlurButton(this, "Назад"){{
            addActionListener(e -> getLauncherUI().setScreen(MainScreen.class));
            setPadding(25, 25);
        }});
    }
}
