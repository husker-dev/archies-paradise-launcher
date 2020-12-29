package com.husker.glassui.screens.main.info;

import com.husker.glassui.components.BlurButtonLineChooser;

import com.husker.launcher.ui.components.TransparentPanel;
import com.husker.launcher.ui.Screen;

import java.awt.*;

public class InfoPanel extends TransparentPanel {

    private final Screen screen;
    private BlurButtonLineChooser chooser;

    private final LauncherInfo launcherPanel;
    private final ClientInfoPanel clientPanel1, clientPanel2;

    public InfoPanel(Screen screen){
        this.screen = screen;

        setLayout(new BorderLayout());

        launcherPanel = new LauncherInfo(screen);
        clientPanel1 = new ClientInfoPanel(screen, "vr");
        clientPanel2 = new ClientInfoPanel(screen, "non_vr");

        add(launcherPanel);

        add(new TransparentPanel(){{
            setLayout(new GridBagLayout());
            setPreferredHeight(50);

            add(chooser = new BlurButtonLineChooser(screen){{
                addButton(" Лаунчер ");
                addButton(" VR ");
                addButton(" Non-VR ");
                addSelectedListener(index -> {
                    BorderLayout layout = (BorderLayout)InfoPanel.this.getLayout();
                    InfoPanel.this.remove(layout.getLayoutComponent(BorderLayout.CENTER));
                    if(index == 0)
                        InfoPanel.this.add(launcherPanel);
                    if(index == 1)
                        InfoPanel.this.add(clientPanel1);
                    if(index == 2)
                        InfoPanel.this.add(clientPanel2);
                    updateCurrentData();
                    screen.getLauncher().updateUI();
                });
            }}, new GridBagConstraints(){{
                this.weightx = 1;
                this.weighty = 1;
            }});
        }}, BorderLayout.SOUTH);
    }

    public void onShow(){
        updateCurrentData();
    }


    public void updateCurrentData(){
        new Thread(() -> {
            int index = chooser.getSelectedIndex();
            if(index == 0)
                launcherPanel.updateData();
            if(index == 1)
                clientPanel1.updateData();
            if(index == 2)
                clientPanel2.updateData();
        }).start();
    }

}
