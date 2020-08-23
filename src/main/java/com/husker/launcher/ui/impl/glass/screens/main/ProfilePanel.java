package com.husker.launcher.ui.impl.glass.screens.main;

import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.style.StyleId;
import com.husker.launcher.Resources;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.impl.glass.GlassUI;
import com.husker.launcher.ui.impl.glass.components.BlurButton;
import com.husker.launcher.ui.impl.glass.components.BlurButtonLineChooser;
import com.husker.launcher.ui.impl.glass.components.BlurComponent;
import com.husker.launcher.ui.impl.glass.components.SkinViewer;
import com.husker.launcher.utils.ShapeUtils;


import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static com.husker.launcher.utils.ShapeUtils.ALL_CORNERS;
import static java.awt.FlowLayout.LEFT;

public class ProfilePanel extends WebPanel {

    private Screen screen;

    public ProfilePanel(Screen screen){
        super(StyleId.panelTransparent);
        this.screen = screen;

        setLayout(new BorderLayout(10, 10));
        setMargin(20);


        add(new WebPanel(StyleId.panelTransparent){{
            setLayout(new BorderLayout(0, 10));

            add(new SkinViewer(screen){{
                setPlayerTexture(Resources.getBufferedImage("husker.png"));
            }});
            add(new BlurButtonLineChooser(screen){{
                addButton("Обычный");
                addButton("Ходьба");
            }}, BorderLayout.SOUTH);
        }}, BorderLayout.WEST);

        add(new WebPanel(StyleId.panelTransparent){{
            setLayout(new VerticalFlowLayout(0, 0));

            add(createTitleLabel("Информация", () -> {}));
            add(createParameterLabel("Имя", "Husker", true));
            add(createParameterLabel("Email", "redfancoestar@gmail.com"));
            add(createParameterLabel("Id", "1"));
            add(Box.createRigidArea(new Dimension(0, 5)));
            add(createTitleLabel("Скин", () -> {}));
            add(createParameterLabel("Название", "Хаски"));

            add(Box.createRigidArea(new Dimension(0, 10)));
            add(new WebPanel(StyleId.panelTransparent){{
                setLayout(new FlowLayout(FlowLayout.RIGHT));
                add(new BlurButton(screen, "Сменить пароль"){{
                    setMargin(3, 30, 0, 30);
                    addActionListener(e -> {

                    });
                }});
            }});
            add(new WebPanel(StyleId.panelTransparent){{
                setLayout(new FlowLayout(FlowLayout.RIGHT));
                add(new BlurButton(screen, "Выйти"){{
                    setMargin(3, 20, 0, 20);
                    addActionListener(e -> {

                    });
                }});
            }});
        }});
    }

    public Component createParameterLabel(String name, Component component){
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

    public Component createParameterLabel(String name, String value){
        return createParameterLabel(name, value, false);
    }

    public Component createParameterLabel(String name, String value, boolean main){
        WebLabel valueLabel;

        float minFontSize = 14;
        float maxFontSize = main ? 18 : 16;

        Component out = createParameterLabel(name, valueLabel = new WebLabel(){
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
        });
        valueLabel.setText(value);
        return out;
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

        panel.add(new WebLabel(){{
            setMargin(0, 5, 0, 0);
            setPreferredHeight(30);
            setVerticalAlignment(CENTER);
            setIcon(new ImageIcon(screen.getLauncher().Resources.Icon_Edit.getScaledInstance(20,  20, Image.SCALE_SMOOTH)));

            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent mouseEvent) {
                    if(action != null)
                        action.run();
                }
            });
        }});
        return panel;
    }

}
