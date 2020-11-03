package com.husker.glassui.screens.main.social;

import com.alee.laf.label.WebLabel;
import com.husker.glassui.GlassUI;
import com.husker.glassui.components.BlurScalableImage;
import com.husker.glassui.components.social.SocialLoadGrid;
import com.husker.glassui.components.social.SocialPanel;
import com.husker.launcher.Resources;
import com.husker.launcher.components.LabelButton;
import com.husker.launcher.components.TransparentPanel;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.utils.ShapeUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static com.husker.launcher.utils.ShapeUtils.ALL_CORNERS;

public abstract class SocialTabPanel extends TransparentPanel {

    private final Screen screen;
    private final SocialLoadGrid socialGrid;
    private boolean loaded = false;

    private WebLabel title, description;
    private BlurScalableImage logo;
    private String link;

    public SocialTabPanel(Screen screen) {
        this.screen = screen;

        setLayout(new BorderLayout());

        add(socialGrid = new SocialLoadGrid(screen, 2, 2){{
            setMargin(0, 35, 15, 35);
            setIndent(15);
        }});

        add(new TransparentPanel(){{
            setLayout(new BorderLayout());
            setMargin(15, 15, 15, 15);

            add(new LabelButton(screen.getLauncher().Resources.Icon_Reply, screen.getLauncher().Resources.Icon_Reply_Selected, 30, 30){{
                setPreferredSize(50, 50);
                addActionListener(() -> screen.getLauncher().NetManager.openLink(link));
            }}, BorderLayout.EAST);

            add(new TransparentPanel(){{
                setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

                add(logo = new BlurScalableImage(screen){
                    {
                        setPreferredSize(new Dimension(50, 50));
                        setFitType(FitType.FILL);
                    }
                    public void onBlurApply(BlurParameter parameter, Component component) {
                        super.onBlurApply(parameter, component);
                        if(returnOnInvisible(parameter, component))
                            return;
                        if(component == this){
                            parameter.setShape(ShapeUtils.createRoundRectangle(screen.getLauncher(), component, getWidth(), getHeight(), ALL_CORNERS));
                            parameter.setShadowSize(0);
                        }
                    }
                });
                add(Box.createRigidArea(new Dimension(10, 10)));
                add(new TransparentPanel(){{
                    setLayout(new BorderLayout());
                    add(title = new WebLabel(){{
                        setForeground(GlassUI.Colors.labelText);
                        setFont(Resources.Fonts.ChronicaPro_ExtraBold.deriveFont(20f));
                    }}, BorderLayout.NORTH);

                    add(description = new WebLabel(){{
                        setForeground(GlassUI.Colors.labelLightText);
                        setFont(Resources.Fonts.ChronicaPro_ExtraBold.deriveFont(13f));
                    }}, BorderLayout.SOUTH);
                }});
            }});
        }}, BorderLayout.NORTH);
    }

    public abstract void onLoad();

    public void onShow(){
        if(loaded)
            return;
        else
            loaded = true;

        new Thread(() -> {
            onLoad();
            socialGrid.updatePanels();
        }).start();

    }


    public void addSocialPanel(SocialPanel panel){
        if(panel != null)
            socialGrid.addSocialPanel(panel);
    }

    public void setLogo(BufferedImage image){
        logo.setImage(image);
    }

    public void setTitle(String text){
        title.setText(text);
    }

    public void setDescription(String text){
        description.setText(text);
    }

    public void setLink(String link){
        this.link = link;
    }

    public Screen getScreen(){
        return screen;
    }

}
