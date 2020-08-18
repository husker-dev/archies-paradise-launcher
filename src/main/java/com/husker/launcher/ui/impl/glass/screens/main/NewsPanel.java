package com.husker.launcher.ui.impl.glass.screens.main;

import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.style.StyleId;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.impl.glass.components.social.vk.VkPostPanel;
import com.husker.launcher.ui.impl.glass.components.social.youtube.YoutubeVideoPanel;
import com.husker.launcher.utils.ConsoleUtils;

import static java.awt.FlowLayout.*;

public class NewsPanel extends WebPanel {

    private final Screen screen;

    public NewsPanel(Screen screen){
        super(StyleId.panelTransparent);
        this.screen = screen;
        setLayout(new VerticalFlowLayout(CENTER, 0, 10));
        setMargin(10);

        screen.getLauncher().BrowserManager.getYoutubeVideoParametersAsync(parameters -> {
            for(int i = 0; i < Math.min(parameters.length - 1, 1); i++)
                add(new YoutubeVideoPanel(screen, parameters[i]));

            screen.getLauncher().updateUI();
        });

        screen.getLauncher().BrowserManager.getVkPostParametersAsync(parameters -> {
            int start = 0;
            for(int i = start; i < Math.min(parameters.length, start + 2); i++)
                add(VkPostPanel.create(screen, parameters[i]));

            screen.getLauncher().updateUI();
        });
    }

}
