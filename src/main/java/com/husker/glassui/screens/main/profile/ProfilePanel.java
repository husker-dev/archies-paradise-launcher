package com.husker.glassui.screens.main.profile;

import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.laf.label.WebLabel;
import com.husker.glassui.GlassUI;
import com.husker.glassui.components.BlurButton;
import com.husker.glassui.components.BlurButtonLineChooser;
import com.husker.glassui.components.BlurComponent;
import com.husker.glassui.components.BlurPanel;
import com.husker.glassui.screens.Message;
import com.husker.glassui.screens.main.MainScreen;
import com.husker.glassui.screens.main.profile.edit.InfoEdit;
import com.husker.glassui.screens.main.profile.skin.SkinFoldersLoading;
import com.husker.glassui.screens.main.profile.skin.SkinListLoading;
import com.husker.launcher.Resources;
import com.husker.launcher.components.TransparentPanel;
import com.husker.launcher.components.skin.SkinViewer;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.utils.ShapeUtils;
import com.husker.launcher.utils.SkinUtils;


import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static com.husker.launcher.utils.ShapeUtils.ALL_CORNERS;
import static java.awt.FlowLayout.LEFT;

public class ProfilePanel extends TransparentPanel {

    private final Screen screen;
    private SkinViewer skinViewer;

    private WebLabel name, email, status, id, skinType;

    public ProfilePanel(Screen screen){
        this.screen = screen;

        setLayout(new BorderLayout(10, 10));
        setMargin(20);


        add(new TransparentPanel(){{
            setLayout(new BorderLayout(0, 10));

            add(new BlurPanel(screen) {
                {
                    add(skinViewer = new SkinViewer());
                }
                public void onBlurApply(BlurParameter parameter, Component component) {
                    super.onBlurApply(parameter, component);
                    if(returnOnInvisible(parameter, component))
                        return;
                    if(component == this) {
                        parameter.setShadowType(BlurParameter.ShadowType.INNER);
                        parameter.setShape(ShapeUtils.createRoundRectangle(getScreen().getLauncher(), component, 15, 15, ALL_CORNERS));
                    }
                }
            });

            add(new BlurButtonLineChooser(screen){{
                addButton("Обычный");
                addButton("Ходьба");
                addSelectedListener(index -> {
                    if(index == 0)
                        skinViewer.setAnimated(false);
                    if(index == 1)
                        skinViewer.setAnimated(true);
                });
            }}, BorderLayout.SOUTH);
        }}, BorderLayout.WEST);

        add(new TransparentPanel(){{
            setLayout(new BorderLayout());

            add(new TransparentPanel(){{
                setLayout(new VerticalFlowLayout(0, 0));

                add(createTitleLabel("Информация", () -> screen.getLauncherUI().setScreen(InfoEdit.class)));
                add(GlassUI.createParameterLine("Имя", name = GlassUI.createParameterLineValueLabel(true)));
                add(GlassUI.createParameterLine("Email", email = GlassUI.createParameterLineValueLabel(false)));
                add(GlassUI.createParameterLine("Статус", status = GlassUI.createParameterLineValueLabel(false)));
                add(GlassUI.createParameterLine("Id", id = GlassUI.createParameterLineValueLabel(false)));
                add(Box.createRigidArea(new Dimension(0, 5)));
                add(createTitleLabel("Скин", () -> screen.getLauncherUI().setScreen(SkinFoldersLoading.class)));
                add(GlassUI.createParameterLine("Тип", skinType = GlassUI.createParameterLineValueLabel(false)));
            }});
            add(new TransparentPanel(){{
                setMargin(0, 10, 0, 0);
                setLayout(new BorderLayout(10, 0));
                add(new BlurButton(screen, "Сменить пароль"){{
                    setMargin(3, 20, 0, 20);
                    addActionListener(e -> {
                        Message.showMessage(screen.getLauncherUI(), "Проблемка", "Пока что изменить пароль нельзя(", MainScreen.class);
                    });
                }});
                add(new BlurButton(screen, "Выйти"){
                    {
                        setMargin(3, 20, 0, 20);
                        addActionListener(e -> ((GlassUI)screen.getLauncherUI()).logout());
                    }
                    public void onBlurApply(BlurParameter parameter, Component component) {
                        super.onBlurApply(parameter, component);
                        if(returnOnInvisible(parameter, component))
                            return;
                        if(component == this){
                            parameter.setAdditionColor(new Color(0, 0, 0, 0));
                            parameter.setBlurFactor(0);
                            parameter.setShadowSize(2);

                            if(isHovered())
                                parameter.setShadowColor(new Color(0, 0, 0, 90));
                        }
                    }
                }, BorderLayout.EAST);
            }}, BorderLayout.SOUTH);
        }});

    }

    public void onShow(){
        name.setText(screen.getLauncher().NetManager.PlayerInfo.getNickname());
        email.setText(screen.getLauncher().NetManager.PlayerInfo.getEmail());
        status.setText(screen.getLauncher().NetManager.PlayerInfo.getStatus());
        id.setText(screen.getLauncher().NetManager.PlayerInfo.getId() + "");
        skinType.setText(SkinUtils.isMale(screen.getLauncher().NetManager.PlayerInfo.getSkin()) ? "Обычный" : "Тонкий");
        if(skinViewer.getPlayerTexture() != screen.getLauncher().NetManager.PlayerInfo.getSkin())
            skinViewer.setPlayerTexture(screen.getLauncher().NetManager.PlayerInfo.getSkin());
    }

    public Component createTitleLabel(String text, Runnable action){
        TransparentPanel panel = new TransparentPanel(){{
            setLayout(new FlowLayout(LEFT));
        }};
        panel.add(new WebLabel(text){{
            setForeground(new Color(50, 50, 50));
            setHorizontalAlignment(LEFT);
            setVerticalAlignment(CENTER);
            setPreferredHeight(30);
            setFont(Resources.Fonts.ChronicaPro_ExtraBold.deriveFont(23f));
            setMargin(1, 10, 0, 10);

            screen.addBlurSegment("ProfilePanel.Label", parameter -> {
                if(BlurComponent.isReturnOnInvisible(parameter, this))
                    return;

                GlassUI.applyTopLayer(parameter);
                parameter.setAdditionColor(GlassUI.Colors.third);
                parameter.setShape(ShapeUtils.createRoundRectangle(screen.getLauncher(), this, 10, 10, ALL_CORNERS));
                parameter.setShadowSize(4);
            });
        }});

        panel.add(new WebLabel(){
            final ImageIcon defaultIcon = new ImageIcon(screen.getLauncher().Resources.Icon_Edit.getScaledInstance(20,  20, Image.SCALE_SMOOTH));
            final ImageIcon hoveredIcon = new ImageIcon(screen.getLauncher().Resources.Icon_Edit_Selected.getScaledInstance(20,  20, Image.SCALE_SMOOTH));

            {
                setMargin(0, 5, 0, 0);
                setPreferredHeight(30);
                setVerticalAlignment(CENTER);
                setIcon(new ImageIcon(screen.getLauncher().Resources.Icon_Edit.getScaledInstance(20,  20, Image.SCALE_SMOOTH)));

                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent mouseEvent) {
                        setIcon(hoveredIcon);
                    }
                    public void mouseExited(MouseEvent mouseEvent) {
                        setIcon(defaultIcon);
                    }

                    public void mousePressed(MouseEvent mouseEvent) {
                        if(action != null)
                            action.run();
                    }
                });
            }
        });
        return panel;
    }

}
