package com.husker.launcher.ui.impl.glass.components.social.vk;

import com.alee.extended.label.WebStyledLabel;
import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.style.StyleId;
import com.alee.utils.swing.extensions.SizeMethods;
import com.husker.launcher.Resources;
import com.husker.launcher.components.ScalableImage;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.ui.impl.glass.components.social.ImageSocialPanel;
import com.husker.launcher.utils.ConsoleUtils;

import javax.swing.*;
import java.awt.*;


public class VkPostPanel extends ImageSocialPanel {

    private VkPostParameter parameter;

    public VkPostPanel(Screen screen, VkPostParameter parameter) {
        super(screen);
        this.parameter = parameter;

        setIcon(getScreen().getLauncher().Resources.Logo_VK);
        getTitleLabel().setMaximumRows(6);
        getTitleLabel().setMaximumHeight(80);
        getTitleLabel().setMinimumHeight(35);
        getTitleLabel().setPreferredHeight(SizeMethods.UNDEFINED);

        setTitle(parameter.getText());
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

        WebStyledLabel videoText;

        public Video(Screen screen, VkPostParameter.Video parameter) {
            super(screen, parameter);

            setImage(parameter.getImage());
            getTitleLabel().setPreferredHeight(100);

            add(new WebPanel(StyleId.panelTransparent){{
                setLayout(new VerticalFlowLayout());
                setMargin(10, 0, 20, 0);

                add(videoText = new WebStyledLabel("Видео"){{
                    setVerticalAlignment(CENTER);
                    setHorizontalAlignment(CENTER);
                    setForeground(new Color(255, 255, 255));
                    setPreferredHeight(20);
                    setFont(Resources.Fonts.ChronicaPro_ExtraBold.deriveFont(11f));
                }});

            }}, 1);
        }

        public void onBlurApply(BlurParameter parameter, Component component) {
            super.onBlurApply(parameter, component);

            if(component == getBlurScalableImage())
                parameter.setAdditionColor(new Color(0, 0, 0, 100));
        }
    }

    public static class Youtube extends Video{

        public Youtube(Screen screen, VkPostParameter.Youtube parameter) {
            super(screen, parameter);
            videoText.setText("YouTube");
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

            add(new WebPanel(StyleId.panelTransparent){{
                setLayout(new VerticalFlowLayout());
                setMargin(40, 0, 20, 0);

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
