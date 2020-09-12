package com.husker.glassui.screens.main;

import com.alee.extended.label.WebStyledLabel;
import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.laf.label.WebLabel;
import com.alee.managers.style.StyleId;
import com.husker.glassui.components.BlurPanel;
import com.husker.launcher.Resources;
import com.husker.launcher.components.TransparentPanel;
import com.husker.launcher.managers.UpdateManager;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.glassui.GlassUI;
import com.husker.launcher.utils.ComponentUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.Map;

import static java.awt.FlowLayout.CENTER;

public class InfoPanel extends TransparentPanel {

    private final Screen screen;

    public InfoPanel(Screen screen){
        this.screen = screen;

        setLayout(new VerticalFlowLayout(0, 6));
        setMargin(10, -10, 0, -10);
        add(new WebStyledLabel(WebStyledLabel.CENTER){{
            setPreferredWidth(300);
            setPreferredHeight(50);
            setMaximumRows(30);
            setForeground(GlassUI.Colors.labelText);
            setFont(Resources.Fonts.ChronicaPro_ExtraBold.deriveFont(28f));
            setText(screen.getLauncher().getConfig().getTitle());
        }});

        add(new BlurPanel(screen) {
            {
                setPreferredWidth(200);
                setPreferredHeight(225);
                setLayout(new FlowLayout(CENTER));
                setMargin(20, 20, 0, 20);
                add(createInfoParameter("Версия", UpdateManager.VERSION));
                add(createInfoParameter("Разработчик", "Штенгауэр Никита", "https://vk.com/shtengauer_nikita"));
                add(createInfoParameter("Владелец", screen.getLauncher().getConfig().About.Owner.getName(), screen.getLauncher().getConfig().About.Owner.getURL()));
                add(createInfoParameter("GitHub", "husker-dev/ minecraft-launcher", "https://github.com/husker-dev/minecraft-launcher"));
                add(createInfoParameter("Поддержка", screen.getLauncher().getConfig().About.Support.getName(), screen.getLauncher().getConfig().About.Support.getURL()));
            }

            public void onBlurApply(BlurParameter parameter, Component component) {
                super.onBlurApply(parameter, component);
                parameter.setShadowType(BlurParameter.ShadowType.INNER);

                if(parameter.getShape() != null) {
                    parameter.setShape(parameter.getShape().getBounds());
                    Rectangle shape = parameter.getShape().getBounds();

                    shape.x += 10;
                    shape.width -= 20;
                    parameter.setClip(shape);
                }
            }
        });
        add(new TransparentPanel(){{
            setLayout(new FlowLayout(CENTER, 10, 0));
            setMargin(15, 10, 0, 10);

            add(createLightText("Java: " + System.getProperty("java.version")));
            add(createLightText("UI: " + screen.getLauncher().getCurrentUITitle()));
            add(createLightText("Arch: " + System.getProperty("os.arch")));
            add(createLightText("WebLaF: 1.2.12"));
        }});
    }

    public WebLabel createLightText(String text){
        return new WebLabel(text){{
            setForeground(new Color(170, 170, 170));
            setHorizontalAlignment(LEFT);
            setPreferredHeight(16);
            setFont(Resources.Fonts.ChronicaPro_ExtraBold.deriveFont(16f));
        }};
    }

    public WebLabel createTitleLabel(String text){
        return new WebLabel(text){{
            setForeground(new Color(50, 50, 50));
            setHorizontalAlignment(CENTER);
            setPreferredHeight(28);
            setFont(Resources.Fonts.ChronicaPro_ExtraBold.deriveFont(18f));
        }};
    }

    public TransparentPanel createInfoParameter(String title, String value){
        return createInfoParameter(title, value, null);
    }

    public TransparentPanel createInfoParameter(String title, String value, String link){
        TransparentPanel panel = new TransparentPanel();
        panel.setPreferredWidth(135);
        panel.setLayout(new BorderLayout());

        WebLabel titleLabel = createTitleLabel(title);
        WebStyledLabel valueLabel = new WebStyledLabel(){
            {
                setHorizontalAlignment(CENTER);
                setVerticalAlignment(TOP);
                setForeground(GlassUI.Colors.labelLightText);
                setPreferredHeight(45);
                setFont(Resources.Fonts.ChronicaPro_ExtraBold.deriveFont(14f));
                setMaximumRows(2);
                setText(value);
            }

            boolean hovered = false;

            public void paint(Graphics g) {
                super.paint(g);

                if(link != null){

                    Point location = ComponentUtils.getComponentLocationOnScreen(screen.getLauncher(), panel);
                    Point mouse = screen.getLauncher().getContentPane().getMousePosition();
                    Rectangle shape = new Rectangle(location.x, location.y, panel.getWidth(), panel.getHeight());

                    if(mouse != null && shape.contains(mouse)){
                        if(!hovered) {
                            Font font = getFont();
                            Map attributes = font.getAttributes();
                            attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
                            setFont(font.deriveFont(attributes));
                            hovered = true;
                        }
                    }else{
                        if(hovered) {
                            Font font = getFont();
                            Map attributes = font.getAttributes();
                            attributes.put(TextAttribute.UNDERLINE, -1);
                            setFont(font.deriveFont(attributes));
                            hovered = false;
                        }
                    }
                }
            }
        };
        if(link != null) {
            MouseAdapter hoverAdapter = new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    valueLabel.repaint();
                }

                public void mouseExited(MouseEvent e) {
                    valueLabel.repaint();
                }
            };
            valueLabel.addMouseListener(hoverAdapter);
            titleLabel.addMouseListener(hoverAdapter);
            panel.addMouseListener(hoverAdapter);
        }
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        MouseAdapter linkAdapter = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                screen.getLauncher().NetManager.openLink(link);
            }
        };

        if(link != null) {
            panel.addMouseListener(linkAdapter);
            titleLabel.addMouseListener(linkAdapter);
            valueLabel.addMouseListener(linkAdapter);
        }

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(valueLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)), BorderLayout.SOUTH);
        return panel;
    }

}