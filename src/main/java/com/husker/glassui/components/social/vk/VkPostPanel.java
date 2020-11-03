package com.husker.glassui.components.social.vk;

import com.alee.extended.label.WebStyledLabel;
import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.utils.swing.extensions.SizeMethods;
import com.husker.glassui.GlassUI;
import com.husker.launcher.Resources;
import com.husker.launcher.components.ScalableImage;
import com.husker.launcher.components.TransparentPanel;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.glassui.components.social.ImageSocialPanel;
import com.husker.launcher.utils.ShapeUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static com.husker.launcher.utils.ShapeUtils.ALL_CORNERS;


public class VkPostPanel extends ImageSocialPanel {

    private final VkPostInfo postInfo;

    public VkPostPanel(Screen screen, VkPostInfo postInfo) {
        super(screen);
        this.postInfo = postInfo;

        setUseTransparentTitle(true);
        setIcon(getScreen().getLauncher().Resources.Logo_VK);
        setTitle(this.postInfo.getText());

        getTitleLabel().setMaximumRows(6);
        getTitleLabel().setMaximumHeight(80);
        getTitleLabel().setMinimumHeight(35);
        getTitleLabel().setPreferredHeight(SizeMethods.UNDEFINED);
    }

    public void onContentInit(TransparentPanel panel) {
        super.onContentInit(panel);
        panel.setMargin(0);
    }

    public void update() {
        loadImage();
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

    public void loadImage(){
        new Thread(() -> {
            setImage(postInfo.getPreview());
        }).start();
    }

    public void onClick() {
        getScreen().getLauncher().NetManager.openLink(postInfo.getUrl());
    }

    public static VkPostPanel create(Screen screen, int index){
        VkPostInfo post = screen.getLauncher().NetManager.Social.getPost(index);
        if(post == null)
            return null;

        if(post instanceof VkPostInfo.Snippet)
            return new Snippet(screen, (VkPostInfo.Snippet) post);
        if(post instanceof VkPostInfo.Video)
            return new Video(screen, (VkPostInfo.Video) post);
        if(post instanceof VkPostInfo.Picture)
            return new Picture(screen, (VkPostInfo.Picture) post);

        return new VkPostPanel(screen, post);
    }

    public static class Video extends VkPostPanel{

        private WebStyledLabel tagText;

        public Video(Screen screen, VkPostInfo.Video parameter) {
            super(screen, parameter);

            add(new TransparentPanel(){{
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

        public void setVideoTagText(String text){
            tagText.setText(text);
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

                parameter.setBlurFactor(0);
                parameter.setShadowSize(5);
                parameter.setAdditionColor(GlassUI.Colors.first);
                parameter.setShape(ShapeUtils.createRoundRectangle(getScreen().getLauncher(), tagText, 15, 15, ALL_CORNERS));
            }
        }
    }


    public static class Picture extends VkPostPanel{

        public Picture(Screen screen, VkPostInfo.Picture parameter) {
            super(screen, parameter);
            getBlurScalableImage().setFitType(ScalableImage.FitType.FILL_XY);
        }
    }

    public static class Snippet extends VkPostPanel{

        public Snippet(Screen screen, VkPostInfo.Snippet parameter) {
            super(screen, parameter);
            getTitleLabel().setMaximumRows(2);

            add(new TransparentPanel(){{
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
