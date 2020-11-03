package com.husker.glassui.screens.main.keys;

import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.laf.label.WebLabel;
import com.husker.glassui.GlassUI;
import com.husker.glassui.components.BlurButton;
import com.husker.glassui.components.BlurTextField;
import com.husker.glassui.components.TagPanel;
import com.husker.launcher.components.TransparentPanel;
import com.husker.launcher.managers.GetRequest;
import com.husker.launcher.managers.NetManager;
import com.husker.launcher.ui.Screen;

import javax.swing.*;
import java.awt.*;

public class KeysPanel extends TransparentPanel {

    private BlurTextField vk_id, yt_id;
    private Screen screen;

    public KeysPanel(Screen screen){
        this.screen = screen;
        setLayout(new BorderLayout());
        setMargin(10, 10, 10, 10);

        add(new TransparentPanel(){{
            setLayout(new VerticalFlowLayout(0, 10));
            setPreferredWidth(510);

            add(new TagPanel(screen, "YouTube"){{
                addButtonAction(KeysPanel.this::updateYouTubeInfo);
                setButtonIcons(screen.getLauncher().Resources.Icon_Reload, screen.getLauncher().Resources.Icon_Reload_Selected);
                getContent().setMargin(10, 0, 0, 0);
                addContent(new TransparentPanel(){{
                    setMargin(0, 20, 0, 0);
                    setLayout(new VerticalFlowLayout(0, 10));

                    add(createParameter("ID:", yt_id = new BlurTextField(screen){{
                        addTextListener(text -> sendYouTubeInfo());
                    }}));
                }});
            }});

            add(new TagPanel(screen, "VK"){{
                addButtonAction(KeysPanel.this::updateVkInfo);
                setButtonIcons(screen.getLauncher().Resources.Icon_Reload, screen.getLauncher().Resources.Icon_Reload_Selected);
                getContent().setMargin(10, 0, 0, 0);
                addContent(new TransparentPanel(){{
                    setMargin(0, 20, 0, 0);
                    setLayout(new VerticalFlowLayout(0, 10));

                    add(createParameter("ID:", vk_id = new BlurTextField(screen){{
                        addTextListener(text -> sendVkInfo());
                    }}));
                }});
            }});

        }}, BorderLayout.WEST);

    }

    public void updateYouTubeInfo(){
        try {
            GetRequest request = screen.getLauncher().NetManager.get(GetRequest.createWithTitle("youtube.getInfo", NetManager.ACCESS_TOKEN, screen.getLauncher().NetManager.PlayerInfo.getKey()));
            yt_id.setText(request.getString("id"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateVkInfo(){
        try {
            GetRequest request = screen.getLauncher().NetManager.get(GetRequest.createWithTitle("vk.getInfo", NetManager.ACCESS_TOKEN, screen.getLauncher().NetManager.PlayerInfo.getKey()));
            vk_id.setText(request.getString("id"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendYouTubeInfo(){
        try {
            screen.getLauncher().NetManager.get(GetRequest.createWithTitle("youtube.setInfo", "id", yt_id.getText(), NetManager.ACCESS_TOKEN, screen.getLauncher().NetManager.PlayerInfo.getKey()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendVkInfo(){
        try {
            screen.getLauncher().NetManager.get(GetRequest.createWithTitle("vk.setInfo", "id", vk_id.getText(), NetManager.ACCESS_TOKEN, screen.getLauncher().NetManager.PlayerInfo.getKey()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private TransparentPanel createParameter(String text, JComponent component){
        return new TransparentPanel(){{
            setLayout(new BorderLayout());

            WebLabel label = GlassUI.createSimpleLabel(text);
            label.setPreferredWidth(80);
            add(label, BorderLayout.WEST);
            add(component);
        }};
    }

    public void onShow(){
        updateYouTubeInfo();
        updateVkInfo();
    }
}
