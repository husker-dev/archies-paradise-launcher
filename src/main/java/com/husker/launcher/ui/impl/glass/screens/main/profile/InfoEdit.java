package com.husker.launcher.ui.impl.glass.screens.main.profile;

import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.style.StyleId;
import com.husker.launcher.utils.FormatUtils;
import com.husker.launcher.Resources;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.ui.impl.glass.GlassUI;
import com.husker.launcher.ui.impl.glass.SimpleCenteredScreen;
import com.husker.launcher.ui.impl.glass.components.*;
import com.husker.launcher.utils.ShapeUtils;

import javax.swing.*;
import java.awt.*;

import static com.husker.launcher.utils.ShapeUtils.ALL_CORNERS;
import static java.awt.FlowLayout.CENTER;


public class InfoEdit extends SimpleCenteredScreen {

    private BlurTextField nickname, email;
    private BlurPasswordField password;
    private BlurButton apply;
    private SkinViewer viewer;

    public void onInit() {
        add(new BlurPanel(this, false){
            {
                setPreferredWidth(340);
                setLayout(new BorderLayout(0, 0));


                add(new BlurPanel(InfoEdit.this, true){{
                    setLayout(new BorderLayout(0, 0));
                    add(GlassUI.createTitleLabel("Редактирование"));
                }}, BorderLayout.NORTH);

                add(new WebPanel(StyleId.panelTransparent){{
                    setLayout(new VerticalFlowLayout(0, 0));

                    setMargin(10, 0, 0, 0);
                    add(viewer = new SkinViewer(Resources.getBufferedImage("husky.png")){{
                        setRotationEnabled(false);
                        setCamY(28);
                        setCamZoom(18);
                        setPreferredHeight(180);
                        setRotationY(-8);

                    }});

                    add(new BlurPanel(InfoEdit.this, true){
                        {
                            setLayout(new BorderLayout(0, 0));

                            // Content
                            add(new WebPanel(StyleId.panelTransparent){{
                                setLayout(new VerticalFlowLayout(0, 0));
                                setMargin(10, 0, 0, 0);

                                add(new WebLabel("Информация"){{
                                    setHorizontalAlignment(CENTER);
                                    setForeground(GlassUI.Colors.labelText);
                                    setFont(Resources.Fonts.ChronicaPro_ExtraBold.deriveFont(25f));
                                }});

                                add(new WebPanel(StyleId.panelTransparent){{
                                    setLayout(new VerticalFlowLayout(0, 0));
                                    setMargin(0, 30, 0, 30);

                                    add(createTitleLabel("Имя"));
                                    add(nickname = createTextField());
                                    nickname.addTextListener(text -> updateApplyButton());
                                    add(Box.createRigidArea(new Dimension(0, 5)));
                                    add(createTitleLabel("Почта"));
                                    add(email = createTextField());
                                    email.addTextListener(text -> updateApplyButton());
                                }});

                                add(Box.createRigidArea(new Dimension(0, 10)));

                                add(createSeparator());

                                add(new WebPanel(StyleId.panelTransparent){{
                                    setLayout(new VerticalFlowLayout(0, 0));
                                    setMargin(0, 30, 0, 30);

                                    add(createTitleLabel("Текущий пароль"));
                                    add(password = createPasswordField());
                                    password.addTextListener(text -> updateApplyButton());
                                }});
                            }});
                            add(new WebPanel(StyleId.panelTransparent){{
                                setLayout(new FlowLayout(CENTER, 10, 0));
                                setMargin(20, 0, 0, 0);

                                add(new BlurButton(InfoEdit.this, "Назад"){{
                                    setPreferredWidth(120);
                                    addActionListener(e -> getLauncherUI().setScreen("main"));
                                }});
                                add(apply = new BlurButton(InfoEdit.this, "Применить"){{
                                    setEnabled(false);
                                    setPreferredWidth(120);
                                    addActionListener(e -> {
                                        getLauncherUI().setScreen("info_edit_apply", password.getText(), nickname.getText(), email.getText());
                                    });
                                }});
                            }}, BorderLayout.SOUTH);
                        }

                        public void onBlurApply(BlurParameter parameter, Component component) {
                            super.onBlurApply(parameter, component);
                            if(returnOnInvisible(parameter, component))
                                return;
                            if(component == this)
                                parameter.setShape(cutRectangle(parameter.getShape()));
                        }
                    });
                }});
            }
            public void onBlurApply(BlurParameter parameter, Component component) {
                super.onBlurApply(parameter, component);
                if(returnOnInvisible(parameter, component))
                    return;
                if(component == this)
                    parameter.setShape(cutRectangle(parameter.getShape()));
            }
        });
    }

    public void updateApplyButton(){
        new Thread(() -> {
            apply.setEnabled(FormatUtils.isCorrectEmail(email.getText()) && FormatUtils.isCorrectName(nickname.getText()) && FormatUtils.isCorrectPassword(password.getText()));
        }).start();
    }

    public void onShow() {
        viewer.setPlayerTexture(getLauncher().NetManager.PlayerInfo.getSkin());
        if(getParameters().length == 2){
            nickname.setText(getParameters()[0]);
            email.setText(getParameters()[1]);
        }else {
            nickname.setText(getLauncher().NetManager.PlayerInfo.getNickname());
            email.setText(getLauncher().NetManager.PlayerInfo.getEmail());
        }

        password.clear();
    }

    private Shape cutRectangle(Shape shape){
        Rectangle bounds = shape.getBounds();
        bounds.height -= 15;
        return ShapeUtils.createRoundRectangle(bounds.x, bounds.y, bounds.width, bounds.height, 25, 25, ALL_CORNERS);
    }

    private Component createTitleLabel(String text){
        return new WebPanel(StyleId.panelTransparent){{
            setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

            add(new WebLabel(text){{
                setForeground(GlassUI.Colors.labelText);
                setFont(Resources.Fonts.ChronicaPro_ExtraBold);
            }});
        }};
    }

    private BlurTextField createTextField(){
        return new BlurTextField(InfoEdit.this){{
            setPreferredWidth(250);
        }};
    }

    private BlurPasswordField createPasswordField(){
        return new BlurPasswordField(InfoEdit.this){{
            setPreferredWidth(250);
        }};
    }

    private Component createSeparator(){
        return new WebPanel(StyleId.panelTransparent){
            {
                setPreferredHeight(20);
            }
            public void paint(Graphics gr) {
                gr.setColor(new Color(180, 180, 180));
                gr.drawLine(20, getHeight() / 2, getWidth() - 20, getHeight() / 2);
            }
        };
    }

}
