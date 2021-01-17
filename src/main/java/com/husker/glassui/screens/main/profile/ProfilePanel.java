package com.husker.glassui.screens.main.profile;

import com.alee.extended.layout.VerticalFlowLayout;
import com.husker.glassui.GlassUI;
import com.husker.glassui.components.BlurButton;
import com.husker.glassui.components.BlurButtonLineChooser;
import com.husker.glassui.components.BlurComponent;
import com.husker.glassui.components.BlurPanel;
import com.husker.glassui.screens.main.MainScreen;
import com.husker.glassui.screens.main.profile.edit.InfoEdit;
import com.husker.glassui.screens.main.profile.skin.SkinCategoriesLoading;
import com.husker.glassui.screens.reset.R_1_SendingCode;
import com.husker.launcher.Resources;
import com.husker.launcher.User;
import com.husker.launcher.ui.components.MLabel;
import com.husker.launcher.ui.components.TransparentPanel;
import com.husker.launcher.ui.components.skin.SkinViewer;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.ui.utils.ShapeUtils;
import com.husker.launcher.utils.SkinUtils;


import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import static com.husker.launcher.ui.utils.ShapeUtils.ALL_CORNERS;
import static java.awt.FlowLayout.LEFT;

public class ProfilePanel extends TransparentPanel {

    private final Screen screen;
    private SkinViewer skinViewer;

    private MLabel name, email, status, id, skinType;

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
                        parameter.setShape(ShapeUtils.createRoundRectangle(component, 15, 15, ALL_CORNERS));
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
                add(createTitleLabel("Скин", () -> screen.getLauncherUI().setScreen(SkinCategoriesLoading.class)));
                add(GlassUI.createParameterLine("Тип", skinType = GlassUI.createParameterLineValueLabel(false)));
            }});
            add(new TransparentPanel(){{
                setMargin(0, 10, 0, 0);
                setLayout(new BorderLayout(10, 0));
                add(new BlurButton(screen, "Сменить пароль"){{
                    setPadding(20, 20);
                    addActionListener(e -> {
                        User user = screen.getLauncher().User;
                        R_1_SendingCode.show(screen.getLauncherUI(), user.getId(), user.getNickname(), MainScreen.class);
                    });
                }});
                add(new BlurButton(screen, "Выйти"){
                    {
                        setPadding(20, 20);
                        addActionListener(e -> screen.getLauncher().User.logout());
                    }
                    public void onBlurApply(BlurParameter parameter, Component component) {
                        super.onBlurApply(parameter, component);
                        if(returnOnInvisible(parameter, component))
                            return;
                        if(component == this)
                            GlassUI.applyTransparentButton(parameter, isHovered());
                    }
                }, BorderLayout.EAST);
            }}, BorderLayout.SOUTH);
        }});

    }

    public void onShow(){
        new Thread(() -> {
            name.setText(screen.getLauncher().User.getNickname());
            email.setText(screen.getLauncher().User.getEmail());
            status.setText(screen.getLauncher().User.getStatus());
            id.setText(screen.getLauncher().User.getId() + "");
            skinType.setText(SkinUtils.isMale(screen.getLauncher().User.getSkin()) ? "Обычный" : "Тонкий");

            if(skinViewer.getPlayerTexture() != screen.getLauncher().User.getSkin())
                skinViewer.setPlayerTexture(screen.getLauncher().User.getSkin());
            if(skinViewer.getCapeTexture() != screen.getLauncher().User.getCape())
                skinViewer.setCapeTexture(screen.getLauncher().User.getCape());
            if(skinViewer.getElytraTexture() != screen.getLauncher().User.getElytra())
                skinViewer.setElytraTexture(screen.getLauncher().User.getElytra());
        }).start();
    }

    public Component createTitleLabel(String text, Runnable action){
        TransparentPanel panel = new TransparentPanel(){{
            setLayout(new FlowLayout(LEFT));
        }};
        panel.add(new MLabel(text){{
            setForeground(new Color(50, 50, 50));
            setHorizontalAlignment(LEFT);
            setVerticalAlignment(CENTER);
            setPreferredHeight(30);
            setFont(Resources.Fonts.getChronicaProExtraBold(23));
            setMargin(1, 10, 0, 10);

            screen.addBlurSegment("ProfilePanel.Label", parameter -> {
                if(BlurComponent.isReturnOnInvisible(parameter, this))
                    return;

                GlassUI.applyTopLayer(parameter);
                parameter.setAdditionColor(GlassUI.Colors.third);
                parameter.setShape(ShapeUtils.createRoundRectangle(this, 10, 10, ALL_CORNERS));
                parameter.setShadowSize(4);
            });
        }});

        panel.add(new MLabel(){
            final BufferedImage defaultIcon = Resources.Icon_Edit;
            final BufferedImage hoveredIcon = Resources.Icon_Edit_Selected;

            {
                setMargin(0, 5, 0, 0);
                setPreferredHeight(30);
                setVerticalAlignment(CENTER);
                setImageSize(20);
                setImage(defaultIcon);

                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent mouseEvent) {
                        setImage(hoveredIcon);
                    }
                    public void mouseExited(MouseEvent mouseEvent) {
                        setImage(defaultIcon);
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
