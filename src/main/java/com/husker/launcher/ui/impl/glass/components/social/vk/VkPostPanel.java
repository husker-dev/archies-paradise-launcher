package com.husker.launcher.ui.impl.glass.components.social.vk;

import com.alee.extended.label.WebStyledLabel;
import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.style.StyleId;
import com.alee.utils.swing.extensions.SizeMethods;
import com.husker.launcher.Resources;
import com.husker.launcher.components.ScalableImage;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.ui.impl.glass.GlassUI;
import com.husker.launcher.ui.impl.glass.components.social.ImageSocialPanel;
import com.husker.launcher.utils.ShapeUtils;

import javax.swing.*;
import java.awt.*;

import static com.husker.launcher.utils.ShapeUtils.ALL_CORNERS;


public class VkPostPanel extends ImageSocialPanel {

    private final VkPostParameter parameter;

    public VkPostPanel(Screen screen, VkPostParameter parameter) {
        super(screen);
        this.parameter = parameter;

        setUseTransparentTitle(true);
        setIcon(getScreen().getLauncher().Resources.Logo_VK);
        setTitle(parameter.getText());

        getTitleLabel().setMaximumRows(6);
        getTitleLabel().setMaximumHeight(80);
        getTitleLabel().setMinimumHeight(35);
        getTitleLabel().setPreferredHeight(SizeMethods.UNDEFINED);
    }

    public void onContentInit(WebPanel panel) {
        super.onContentInit(panel);
        panel.setMargin(0);
    }

    public void onBlurApply(BlurParameter parameter, Component component) {
        super.onBlurApply(parameter, component);
        if(returnOnInvisible(parameter, component))
            return;

        if(component == getBlurScalableImage()){
            Rectangle bounds = parameter.getShape().getBounds();
            parameter.setShape(ShapeUtils.createRoundRectangle(bounds.x, bounds.y, bounds.width, bounds.height, 15, 15, ALL_CORNERS));
        }
    }

    public void onClick() {
        getScreen().getLauncher().NetManager.openLink(parameter.getUrl());
    }

    public static VkPostPanel create(Screen screen, VkPostParameter parameter){
        if(parameter instanceof VkPostParameter.Snippet)
            return new Snippet(screen, (VkPostParameter.Snippet) parameter);
        if(parameter instanceof VkPostParameter.Youtube)
            return new Youtube(screen, (VkPostParameter.Youtube) parameter);
        if(parameter instanceof VkPostParameter.Video)
            return new Video(screen, (VkPostParameter.Video) parameter);
        if(parameter instanceof VkPostParameter.Picture)
            return new Picture(screen, (VkPostParameter.Picture) parameter);

        return new VkPostPanel(screen, parameter);
    }

    public static class Video extends VkPostPanel{

        WebStyledLabel tagText;

        public Video(Screen screen, VkPostParameter.Video parameter) {
            super(screen, parameter);

            setImage(parameter.getImage());

            add(new WebPanel(StyleId.panelTransparent){{
                setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
                setMargin(5, 5, 20, 5);

                add(tagText = new WebStyledLabel("Видео"){{
                    setMargin(0, 10, 0, 10);
                    setVerticalAlignment(CENTER);
                    setHorizontalAlignment(CENTER);
                    setForeground(GlassUI.Colors.labelText);
                    setPreferredHeight(20);
                    setFont(Resources.Fonts.ChronicaPro_Bold.deriveFont(11f));
                    screen.addBlurSegment("VkPostPanel.Video.Tag", parameter1 -> onBlurApply(parameter1, tagText));
                    setIcon(new ImageIcon(getScreen().getLauncher().Resources.Icon_Play.getScaledInstance(14, 14, Image.SCALE_SMOOTH)));
                }});

            }}, 1);
        }

        public void onBlurApply(BlurParameter parameter, Component component) {
            super.onBlurApply(parameter, component);
            if(returnOnInvisible(parameter, component))
                return;

            if(component == getBlurScalableImage())
                parameter.setAdditionColor(new Color(0, 0, 0, 0));
            if(component == tagText){

                if(returnOnInvisible(parameter, component))
                    return;

                parameter.setVisible(component.isDisplayable());
                parameter.setBlurFactor(0);
                parameter.setShadowSize(5);
                parameter.setAdditionColor(GlassUI.Colors.first);
                parameter.setShape(ShapeUtils.createRoundRectangle(getScreen().getLauncher(), tagText, 15, 15, ALL_CORNERS));
            }
        }
    }

    public static class Youtube extends Video{

        public Youtube(Screen screen, VkPostParameter.Youtube parameter) {
            super(screen, parameter);
            tagText.setText("YouTube");
        }
    }

    public static class Picture extends VkPostPanel{

        public Picture(Screen screen, VkPostParameter.Picture parameter) {
            super(screen, parameter);
            getBlurScalableImage().setFitType(ScalableImage.FitType.FIT_XY);
            setImage(parameter.getImage());
        }
    }

    public static class Snippet extends VkPostPanel{

        public Snippet(Screen screen, VkPostParameter.Snippet parameter) {
            super(screen, parameter);

            setImage(parameter.getImage());
            getTitleLabel().setMaximumRows(2);

            add(new WebPanel(StyleId.panelTransparent){{
                setLayout(new VerticalFlowLayout());
                setMargin(35, 0, 20, 0);

                add(new WebStyledLabel(parameter.getSnippetTitle()){{
                    setVerticalAlignment(CENTER);
                    setHorizontalAlignment(CENTER);
                    setForeground(new Color(255, 255, 255));
                    setPreferredHeight(20);
                    setFont(Resources.Fonts.ChronicaPro_ExtraBold.deriveFont(15f));
                }});

                add(new WebStyledLabel(parameter.getSnippetAuthor()){{
                    setVerticalAlignment(CENTER);
                    setHorizontalAlignment(CENTER);
                    setForeground(new Color(255, 255, 255));
                    setPreferredHeight(20);
                    setFont(Resources.Fonts.ChronicaPro_Bold.deriveFont(12f));
                }});

            }}, 1);
        }

        public void onBlurApply(BlurParameter parameter, Component component) {
            super.onBlurApply(parameter, component);

            if(component == getBlurScalableImage())
                parameter.setAdditionColor(new Color(0, 0, 0, 100));
        }
    }

}
