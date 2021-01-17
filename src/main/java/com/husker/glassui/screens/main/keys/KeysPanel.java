package com.husker.glassui.screens.main.keys;

import com.alee.extended.layout.VerticalFlowLayout;
import com.husker.glassui.GlassUI;
import com.husker.glassui.components.BlurTextField;
import com.husker.glassui.components.BlurTagPanel;
import com.husker.launcher.Resources;
import com.husker.launcher.social.Social;
import com.husker.launcher.ui.components.MLabel;
import com.husker.launcher.ui.components.TransparentPanel;
import com.husker.launcher.ui.Screen;

import javax.swing.*;
import java.awt.*;

public class KeysPanel extends TransparentPanel {

    private BlurTextField vk_id, yt_id, inst_id, github_id;
    private final Screen screen;
    private boolean eventsEnabled = true;

    public KeysPanel(Screen screen){
        this.screen = screen;
        setLayout(new BorderLayout());
        setMargin(10, 10, 10, 10);

        add(new TransparentPanel(){{
            setLayout(new VerticalFlowLayout(0, 10));
            setPreferredWidth(510);

            add(new BlurTagPanel(screen, "YouTube"){{
                addButtonAction(KeysPanel.this::updateYouTubeInfo);
                setButtonIcons(Resources.Icon_Reload, Resources.Icon_Reload_Selected);
                getContent().setMargin(10, 0, 0, 0);
                addContent(new TransparentPanel(){{
                    setMargin(0, 20, 0, 0);
                    setLayout(new VerticalFlowLayout(0, 10));

                    add(createParameter("ID:", yt_id = new BlurTextField(screen){{
                        addTextListener(text -> sendYouTubeInfo());
                    }}));
                }});
            }});

            add(new BlurTagPanel(screen, "VK"){{
                addButtonAction(KeysPanel.this::updateVkInfo);
                setButtonIcons(Resources.Icon_Reload, Resources.Icon_Reload_Selected);
                getContent().setMargin(10, 0, 0, 0);
                addContent(new TransparentPanel(){{
                    setMargin(0, 20, 0, 0);
                    setLayout(new VerticalFlowLayout(0, 10));

                    add(createParameter("ID:", vk_id = new BlurTextField(screen){{
                        addTextListener(text -> sendVkInfo());
                    }}));
                }});
            }});

            add(new BlurTagPanel(screen, "Instagram"){{
                addButtonAction(KeysPanel.this::updateInstagramInfo);
                setButtonIcons(Resources.Icon_Reload, Resources.Icon_Reload_Selected);
                getContent().setMargin(10, 0, 0, 0);
                addContent(new TransparentPanel(){{
                    setMargin(0, 20, 0, 0);
                    setLayout(new VerticalFlowLayout(0, 10));

                    add(createParameter("Имя:", inst_id = new BlurTextField(screen){{
                        addTextListener(text -> sendInstagramInfo());
                    }}));
                }});
            }});

            add(new BlurTagPanel(screen, "GitHub"){{
                addButtonAction(KeysPanel.this::updateGitHubInfo);
                setButtonIcons(Resources.Icon_Reload, Resources.Icon_Reload_Selected);
                getContent().setMargin(10, 0, 0, 0);
                addContent(new TransparentPanel(){{
                    setMargin(0, 20, 0, 0);
                    setLayout(new VerticalFlowLayout(0, 10));

                    add(createParameter("Repo:", github_id = new BlurTextField(screen){{
                        addTextListener(text -> sendGitHubInfo());
                    }}));
                }});
            }});

        }}, BorderLayout.WEST);

    }

    public void updateYouTubeInfo(){
        eventsEnabled = false;
        yt_id.setEnabled(false);
        yt_id.setText("...");
        String text = Social.YouTube.getId();
        SwingUtilities.invokeLater(() -> yt_id.setText(text));
        yt_id.setEnabled(true);
        eventsEnabled = true;
    }

    public void updateVkInfo(){
        eventsEnabled = false;
        vk_id.setEnabled(false);
        vk_id.setText("...");
        String text = Social.VK.getId();
        SwingUtilities.invokeLater(() -> vk_id.setText(text));
        vk_id.setEnabled(true);
        eventsEnabled = true;
    }

    public void updateInstagramInfo(){
        eventsEnabled = false;
        inst_id.setEnabled(false);
        inst_id.setText("...");
        String text = Social.Instagram.getId();
        SwingUtilities.invokeLater(() -> inst_id.setText(text));
        inst_id.setEnabled(true);
        eventsEnabled = true;
    }

    public void updateGitHubInfo(){
        eventsEnabled = false;
        github_id.setEnabled(false);
        github_id.setText("...");
        String text = Social.GitHub.getRepository();
        SwingUtilities.invokeLater(() -> github_id.setText(text));
        github_id.setEnabled(true);
        eventsEnabled = true;
    }

    public void sendYouTubeInfo(){
        if(eventsEnabled)
            Social.YouTube.setId(yt_id.getText(), screen.getLauncher().User.getToken());
    }

    public void sendVkInfo(){
        if(eventsEnabled)
            Social.VK.setId(vk_id.getText(), screen.getLauncher().User.getToken());
    }

    public void sendInstagramInfo(){
        if(eventsEnabled)
            Social.Instagram.setId(inst_id.getText(), screen.getLauncher().User.getToken());
    }

    public void sendGitHubInfo(){
        if(eventsEnabled)
            Social.GitHub.setRepository(github_id.getText(), screen.getLauncher().User.getToken());
    }

    private TransparentPanel createParameter(String text, JComponent component){
        return new TransparentPanel(){{
            setLayout(new BorderLayout());

            MLabel label = GlassUI.createSimpleLabel(text);
            label.setPreferredWidth(80);
            add(label, BorderLayout.WEST);
            add(component);
        }};
    }

    public void onShow(){
        new Thread(() -> {
            updateYouTubeInfo();
            updateVkInfo();
            updateInstagramInfo();
            updateGitHubInfo();
        }).start();
    }
}
