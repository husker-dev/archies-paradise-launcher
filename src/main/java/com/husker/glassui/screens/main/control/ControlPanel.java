package com.husker.glassui.screens.main.control;

import com.alee.laf.WebLookAndFeel;
import com.husker.glassui.components.BlurButton;
import com.husker.glassui.components.TagPanel;
import com.husker.launcher.components.TransparentPanel;
import com.husker.launcher.ui.Screen;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;

public class ControlPanel extends TransparentPanel {

    public ControlPanel(Screen screen){
        setLayout(new BorderLayout());
        setMargin(10, 10, 3, 10);

        add(new TagPanel(screen, "Сборка"){{
            getContent().setMargin(5, 0, 0, 0);
            addContent(new TransparentPanel(){{
                setMargin(0, 20, 0, 0);
                setLayout(new FlowLayout(FlowLayout.LEFT));
                add(new BlurButton(screen, "Выбрать файл"){{
                    addActionListener(e -> {
                        try {
                            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                            JFileChooser chooser = new JFileChooser() {{
                                setDialogTitle("Выбор скина");
                                setFileSelectionMode(JFileChooser.FILES_ONLY);
                                setFileFilter(new FileFilter() {
                                    public String getDescription() {
                                        return "Zip Archive (.zip)";
                                    }

                                    public boolean accept(File f) {
                                        return f.isDirectory();
                                    }
                                });
                            }};

                            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                                File file = chooser.getSelectedFile();


                            }

                            WebLookAndFeel.install();
                        }catch (Exception ex){
                            ex.printStackTrace();
                        }
                    });
                }});
            }});
        }});
    }
}
