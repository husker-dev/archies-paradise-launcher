package com.husker.launcher.ui.impl.glass.screens.main;

import com.alee.laf.panel.WebPanel;
import com.alee.managers.style.StyleId;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.impl.glass.components.social.BlankSocialPanel;
import com.husker.launcher.ui.impl.glass.components.social.SocialPanel;
import com.husker.launcher.ui.impl.glass.components.social.vk.VkPostPanel;
import com.husker.launcher.ui.impl.glass.components.social.youtube.YoutubeVideoPanel;
import com.husker.launcher.utils.ConsoleUtils;

import javax.swing.*;

import java.awt.*;

import static java.awt.FlowLayout.*;

public class NewsPanel extends WebPanel {

    private static final int MAX_COUNT = 3;

    private final Screen screen;
    private final WebPanel blankContent;
    private final WebPanel content;

    public NewsPanel(Screen screen){
        super(StyleId.panelTransparent);
        this.screen = screen;
        setLayout(new OverlayLayout(this));

        add(blankContent = new WebPanel(StyleId.panelTransparent){{
            setLayout(new FlowLayout(CENTER, 0, 12));
            for(int i = 0; i < 3; i++)
                add(new BlankSocialPanel(screen));
        }});
        add(content = new WebPanel(StyleId.panelTransparent){{
            setLayout(new FlowLayout(CENTER, 0, 12));
            screen.getLauncher().BrowserManager.getYoutubeVideoParametersAsync(parameters -> {
                for(int i = 0; i < Math.min(parameters.length - 1, 1); i++)
                    addSocialPanel(new YoutubeVideoPanel(screen, parameters[i]));
                screen.getLauncher().updateUI();
            });

            screen.getLauncher().BrowserManager.getVkPostParametersAsync(parameters -> {
                int start = 0;
                for(int i = start; i < Math.min(parameters.length, start + 2); i++)
                    addSocialPanel(VkPostPanel.create(screen, parameters[i]));

                screen.getLauncher().updateUI();
            });
        }});
    }

    private void addSocialPanel(SocialPanel panel){
        panel.doLayout();
        content.add(panel);

        ((BlankSocialPanel)blankContent.getComponent(content.getComponentCount() - 1)).hidePanel();
        if(content.getComponentCount() == MAX_COUNT) {
            blankContent.removeAll();
            ConsoleUtils.printDebug(getClass(), "All news have been initialized");
        }
    }

}
