package com.husker.launcher.ui.impl.glass.screens.main.profile;

import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.style.StyleId;
import com.husker.launcher.Resources;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.ui.impl.glass.GlassUI;
import com.husker.launcher.ui.impl.glass.components.*;
import com.husker.launcher.utils.ShapeUtils;


import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static com.husker.launcher.utils.ShapeUtils.ALL_CORNERS;
import static java.awt.FlowLayout.LEFT;

public class ProfilePanel extends WebPanel {

    private final Screen screen;
    private SkinViewer skinViewer;

    private WebLabel name, email, status, id, skinName;

    public ProfilePanel(Screen screen){
        super(StyleId.panelTransparent);
        this.screen = screen;

        setLayout(new BorderLayout(10, 10));
        setMargin(20);


        add(new WebPanel(StyleId.panelTransparent){{
            setLayout(new BorderLayout(0, 10));


            add(new BlurPanel(screen) {
                {
                    add(skinViewer = new SkinViewer(Resources.getBufferedImage("husky.png")));
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

        add(new WebPanel(StyleId.panelTransparent){{
            setLayout(new BorderLayout());

            add(new WebPanel(StyleId.panelTransparent){{
                setLayout(new VerticalFlowLayout(0, 0));

                add(createTitleLabel("Информация", () -> screen.getLauncherUI().setScreen("info_edit")));
                add(createParameterLine("Имя", name = createParameterValueLabel(true)));
                add(createParameterLine("Email", email = createParameterValueLabel(false)));
                add(createParameterLine("Статус", status = createParameterValueLabel(false)));
                add(createParameterLine("Id", id = createParameterValueLabel(false)));
                add(Box.createRigidArea(new Dimension(0, 5)));
                add(createTitleLabel("Скин", () -> screen.getLauncherUI().setScreen("skin_chooser")));
                add(createParameterLine("Название", skinName = createParameterValueLabel(false)));
            }});
            add(new WebPanel(StyleId.panelTransparent){{
                setMargin(0, 10, 0, 0);
                setLayout(new BorderLayout(10, 0));
                add(new BlurButton(screen, "Сменить пароль"){{
                    setMargin(3, 20, 0, 20);
                    addActionListener(e -> {

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
        if(screen.getLauncher().NetManager.PlayerInfo.getSkinName() != null)
            skinName.setText(screen.getLauncher().NetManager.PlayerInfo.getSkinName());
        else
            skinName.setText("Без названия");
        skinViewer.setPlayerTexture(screen.getLauncher().NetManager.PlayerInfo.getSkin());
    }

    public Component createParameterLine(String name, Component component){
        return new WebLabel(StyleId.panelTransparent){
            {
                setMargin(10, 20, 0, 10);
                setLayout(new BorderLayout());
                add(new WebLabel(name + ":"){{
                    setForeground(GlassUI.Colors.labelText);
                    setFont(Resources.Fonts.ChronicaPro_ExtraBold.deriveFont(18f));
                    setMargin(0, 0, 0, 10);
                }}, BorderLayout.WEST);
                add(component);
            }

            public void paint(Graphics gr) {
                super.paint(gr);

                gr.setColor(new Color(200, 200, 200));
                gr.drawLine(10, getHeight() - 1, getWidth(), getHeight() - 1);
            }
        };
    }

    public Component createParameterLine(String name, String value){
        return createParameterLine(name, value, false);
    }

    public Component createParameterLine(String name, String value, boolean main){
        WebLabel valueLabel;

        Component out = createParameterLine(name, valueLabel = createParameterValueLabel(main));
        valueLabel.setText(value);
        return out;
    }

    public WebLabel createParameterValueLabel(boolean main){
        float minFontSize = 14;
        float maxFontSize = main ? 18 : 16;

        return new WebLabel(){
            {
                setForeground(GlassUI.Colors.labelLightText);
                setFont(Resources.Fonts.ChronicaPro_ExtraBold.deriveFont(maxFontSize));
                setHorizontalAlignment(RIGHT);
            }
            public void setText(String text) {
                super.setText(text);
                if(text == null)
                    return;

                for(int i = (int)maxFontSize; i >= minFontSize; i--){
                    setFont(Resources.Fonts.ChronicaPro_ExtraBold.deriveFont((float)i));
                    if(getFontMetrics(Resources.Fonts.ChronicaPro_ExtraBold.deriveFont((float)i)).stringWidth(text) < 160)
                        break;
                }
            }
        };
    }

    public Component createTitleLabel(String text, Runnable action){
        WebPanel panel = new WebPanel(StyleId.panelTransparent){{
            setLayout(new FlowLayout(LEFT));
        }};
        panel.add(new WebLabel(text){{
            setForeground(new Color(50, 50, 50));
            setHorizontalAlignment(LEFT);
            setVerticalAlignment(CENTER);
            setPreferredHeight(30);
            setFont(Resources.Fonts.ChronicaPro_ExtraBold.deriveFont(23f));
            setMargin(1, 10, 0, 10);

            screen.addBlurSegment("SettingsPanel.Label", parameter -> {
                if(BlurComponent.isReturnOnInvisible(parameter, this))
                    return;

                GlassUI.applyTopLayer(parameter);
                parameter.setAdditionColor(GlassUI.Colors.third);
                if (isDisplayable()) {
                    parameter.setShape(ShapeUtils.createRoundRectangle(screen.getLauncher(), this, 10, 10, ALL_CORNERS));
                    parameter.setShadowSize(4);
                }
            });
        }});

        panel.add(new WebLabel(){
            ImageIcon defaultIcon = new ImageIcon(screen.getLauncher().Resources.Icon_Edit.getScaledInstance(20,  20, Image.SCALE_SMOOTH));
            ImageIcon hoveredIcon = new ImageIcon(screen.getLauncher().Resources.Icon_Edit_Selected.getScaledInstance(20,  20, Image.SCALE_SMOOTH));

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
