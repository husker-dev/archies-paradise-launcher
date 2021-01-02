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
    private Screen screen;

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
        yt_id.setText(Social.YouTube.getId());
    }

    public void updateVkInfo(){
        vk_id.setText(Social.VK.getId());
    }

    public void updateInstagramInfo(){
        inst_id.setText(Social.Instagram.getId());
    }

    public void updateGitHubInfo(){
        github_id.setText(Social.GitHub.getRepository());
    }

    public void sendYouTubeInfo(){
        Social.YouTube.setId(yt_id.getText(), screen.getLauncher().User.getToken());
    }

    public void sendVkInfo(){
        Social.VK.setId(vk_id.getText(), screen.getLauncher().User.getToken());
    }

    public void sendInstagramInfo(){
        Social.Instagram.setId(inst_id.getText(), screen.getLauncher().User.getToken());
    }

    public void sendGitHubInfo(){
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
        updateYouTubeInfo();
        updateVkInfo();
        updateInstagramInfo();
        updateGitHubInfo();
    }
}
