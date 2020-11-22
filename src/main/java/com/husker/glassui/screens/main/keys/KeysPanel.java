package com.husker.glassui.screens.main.keys;

import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.laf.label.WebLabel;
import com.husker.glassui.GlassUI;
import com.husker.glassui.components.BlurTextField;
import com.husker.glassui.components.TagPanel;
import com.husker.launcher.components.TransparentPanel;
import com.husker.launcher.managers.ApiMethod;
import com.husker.launcher.managers.NetManager;
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

            add(new TagPanel(screen, "Instagram"){{
                addButtonAction(KeysPanel.this::updateInstagramInfo);
                setButtonIcons(screen.getLauncher().Resources.Icon_Reload, screen.getLauncher().Resources.Icon_Reload_Selected);
                getContent().setMargin(10, 0, 0, 0);
                addContent(new TransparentPanel(){{
                    setMargin(0, 20, 0, 0);
                    setLayout(new VerticalFlowLayout(0, 10));

                    add(createParameter("Имя:", inst_id = new BlurTextField(screen){{
                        addTextListener(text -> sendInstagramInfo());
                    }}));
                }});
            }});

            add(new TagPanel(screen, "GitHub"){{
                addButtonAction(KeysPanel.this::updateGitHubInfo);
                setButtonIcons(screen.getLauncher().Resources.Icon_Reload, screen.getLauncher().Resources.Icon_Reload_Selected);
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
        yt_id.setText(screen.getLauncher().API.Social.getYouTubeId());
    }

    public void updateVkInfo(){
        vk_id.setText(screen.getLauncher().API.Social.getVkId());
    }

    public void updateInstagramInfo(){
        inst_id.setText(screen.getLauncher().API.Social.getInstagramId());
    }

    public void updateGitHubInfo(){
        github_id.setText(screen.getLauncher().API.Social.getGitHubRepo());
    }

    public void sendYouTubeInfo(){
        screen.getLauncher().API.Social.setYouTubeId(yt_id.getText());
    }

    public void sendVkInfo(){
        screen.getLauncher().API.Social.setVkId(vk_id.getText());
    }

    public void sendInstagramInfo(){
        screen.getLauncher().API.Social.setInstagramId(inst_id.getText());
    }

    public void sendGitHubInfo(){
        screen.getLauncher().API.Social.setGitHubRepo(github_id.getText());
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
        updateInstagramInfo();
        updateGitHubInfo();
    }
}
