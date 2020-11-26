package com.husker.glassui.screens.main;

import com.alee.laf.label.WebLabel;
import com.husker.glassui.GlassUI;
import com.husker.glassui.components.BlurPanel;
import com.husker.launcher.Resources;
import com.husker.launcher.ui.components.TransparentPanel;
import com.husker.launcher.ui.Screen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class UpdatePanel extends BlurPanel {

    private WebLabel[] labels = new WebLabel[2];

    public UpdatePanel(Screen screen){
        super(screen, true);
        setPreferredHeight(60);

        setLayout(new BorderLayout());

        add(new WebLabel(){{
            setIcon(new ImageIcon(Resources.Icon_Download.getScaledInstance(40, 40, Image.SCALE_SMOOTH)));
            setMargin(0, 10, 0, 10);
        }}, BorderLayout.WEST);

        add(new TransparentPanel(){{
            setMargin(10, 0, 0, 0);
            setLayout(new BorderLayout());
            add(labels[0] = GlassUI.createSimpleLabel("Доступно новое"), BorderLayout.NORTH);
            add(labels[1] = GlassUI.createSimpleLabel("обновление!"));

            for(WebLabel label : labels)
                label.setForeground(GlassUI.Colors.labelLightText);
        }});

        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent mouseEvent) {
                for(WebLabel label : labels){
                    Font font = label.getFont();
                    Map attributes = font.getAttributes();
                    attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
                    label.setFont(font.deriveFont(attributes));
                }
            }

            public void mouseExited(MouseEvent mouseEvent) {
                for(WebLabel label : labels){
                    Font font = label.getFont();
                    Map attributes = font.getAttributes();
                    attributes.put(TextAttribute.UNDERLINE, -1);
                    label.setFont(font.deriveFont(attributes));
                }
            }
            public void mouseClicked(MouseEvent mouseEvent) {
                try {
                    Process proc = Runtime.getRuntime().exec("java -jar " + (new File(".").getAbsolutePath() + "launcher.jar"));
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Ошибка!", "Ошибка запуска приложения!", JOptionPane.ERROR_MESSAGE);
                }

            }
        });
    }
}
