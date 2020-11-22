package com.husker.glassui.components.social.impl;

import com.alee.extended.label.WebStyledLabel;
import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.laf.label.WebLabel;
import com.alee.utils.swing.extensions.SizeMethods;
import com.husker.glassui.GlassUI;
import com.husker.launcher.Resources;
import com.husker.launcher.components.ScalableImage;
import com.husker.launcher.components.TransparentPanel;
import com.husker.launcher.managers.social.VkPostInfo;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.glassui.components.social.ImageSocialPanel;
import com.husker.launcher.utils.ShapeUtils;

import javax.swing.*;
import java.awt.*;

import static com.husker.launcher.utils.ShapeUtils.ALL_CORNERS;


public class VkPostPanel extends ImageSocialPanel {

    private VkPostInfo postInfo;
    private final int index;
    private WebStyledLabel tag;

    public VkPostPanel(Screen screen, int index) {
        super(screen);
        this.index = index;

        setIcon(getScreen().getLauncher().Resources.Logo_VK);
    }

    public void update() {
        postInfo = getScreen().getLauncher().API.Social.getPost(index);

        setTitle(postInfo.getText());

        if(postInfo instanceof VkPostInfo.Snippet) {
            VkPostInfo.Snippet snippet = (VkPostInfo.Snippet) postInfo;

            add(new TransparentPanel(){{
                setLayout(new VerticalFlowLayout());
                setMargin(35, 0, 20, 0);

                add(new WebStyledLabel(snippet.getSnippetTitle()){{
                    setVerticalAlignment(CENTER);
                    setHorizontalAlignment(CENTER);
                    setForeground(new Color(255, 255, 255));
                    setPreferredHeight(20);
                    setFont(Resources.Fonts.ChronicaPro_ExtraBold.deriveFont(15f));
                }});

                add(new WebStyledLabel(snippet.getSnippetAuthor()){{
                    setVerticalAlignment(CENTER);
                    setHorizontalAlignment(CENTER);
                    setForeground(new Color(255, 255, 255));
                    setPreferredHeight(20);
                    setFont(Resources.Fonts.ChronicaPro_Bold.deriveFont(12f));
                }});
            }}, 1);
        }
        if(postInfo instanceof VkPostInfo.Video) {
            add(new TransparentPanel(){{
                setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
                setMargin(5, 5, 20, 5);

                add(tag = new WebStyledLabel("Видео"){{
                    setMargin(0, 10, 0, 10);
                    setVerticalAlignment(CENTER);
                    setHorizontalAlignment(CENTER);
                    setForeground(GlassUI.Colors.labelText);
                    setPreferredHeight(20);
                    setFont(Resources.Fonts.ChronicaPro_Bold.deriveFont(11f));
                    getScreen().addBlurSegment("VkPostPanel.Video.Tag", parameter1 -> onBlurApply(parameter1, tag));
                    setIcon(new ImageIcon(getScreen().getLauncher().Resources.Icon_Play.getScaledInstance(14, 14, Image.SCALE_SMOOTH)));
                }});
            }}, 1);
        }

        new Thread(() -> setImage(postInfo.getPreview())).start();
    }

    public void onBlurApply(BlurParameter parameter, Component component) {
        super.onBlurApply(parameter, component);
        if(returnOnInvisible(parameter, component))
            return;

        if(component == tag){
            parameter.setBlurFactor(0);
            parameter.setShadowSize(5);
            parameter.setAdditionColor(GlassUI.Colors.first);
            parameter.setShape(ShapeUtils.createRoundRectangle(getScreen().getLauncher(), tag, 15, 15, ALL_CORNERS));
        }
    }

    public void onClick() {
        if(postInfo != null)
            getScreen().getLauncher().NetManager.openLink(postInfo.getUrl());
    }

    public static VkPostPanel create(Screen screen, int index){
        return new VkPostPanel(screen, index);
    }



}
