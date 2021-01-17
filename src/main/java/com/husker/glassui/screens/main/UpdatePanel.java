package com.husker.glassui.screens.main;

import com.husker.glassui.GlassUI;
import com.husker.glassui.components.BlurPanel;
import com.husker.launcher.Launcher;
import com.husker.launcher.Resources;
import com.husker.launcher.api.API;
import com.husker.launcher.ui.components.MLabel;
import com.husker.launcher.ui.components.TransparentPanel;
import com.husker.launcher.ui.Screen;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class UpdatePanel extends BlurPanel {

    private static final Logger log = LogManager.getLogger(UpdatePanel.class);

    private final MLabel[] labels = new MLabel[2];

    public UpdatePanel(Screen screen){
        super(screen, true);
        setPreferredHeight(60);

        setLayout(new BorderLayout());

        add(new MLabel(){{
            setImageSize(40);
            setImage(Resources.Icon_Download);
            setMargin(0, 10, 0, 10);
        }}, BorderLayout.WEST);

        add(new TransparentPanel(){{
            setMargin(10, 0, 0, 0);
            setLayout(new BorderLayout());
            add(labels[0] = GlassUI.createSimpleLabel("Доступно новое"), BorderLayout.NORTH);
            add(labels[1] = GlassUI.createSimpleLabel("обновление!"));

            for(MLabel label : labels)
                label.setForeground(GlassUI.Colors.labelLightText);
        }});

        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent mouseEvent) {
                for(MLabel label : labels){
                    Font font = label.getFont();
                    Map attributes = font.getAttributes();
                    attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
                    label.setFont(font.deriveFont(attributes));
                }
            }

            public void mouseExited(MouseEvent mouseEvent) {
                for(MLabel label : labels){
                    Font font = label.getFont();
                    Map attributes = font.getAttributes();
                    attributes.put(TextAttribute.UNDERLINE, -1);
                    label.setFont(font.deriveFont(attributes));
                }
            }
            public void mouseClicked(MouseEvent mouseEvent) {
                try {
                    String path = new File("launcher.exe").getAbsolutePath();
                    if(!Files.exists(Paths.get(path)))
                        throw new NullPointerException("File doesn't exist");
                    Runtime.getRuntime().exec(path);
                    System.exit(0);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Ошибка запуска приложения! Пожалуйста, перезапустите лаунчер", "Ошибка!", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        new Timer().schedule(new TimerTask() {
            public void run() {
                if(!screen.getLauncher().isVisible()) {
                    setVisible(false);
                    return;
                }
                try {
                    setVisible(!API.Launcher.getCurrentVersion().equals(Launcher.VERSION));
                } catch (API.InternalAPIException e) {
                    e.printStackTrace();
                    setVisible(false);
                }
            }
        }, 0, 10000);
    }
}
