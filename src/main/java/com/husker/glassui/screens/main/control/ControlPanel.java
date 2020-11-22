package com.husker.glassui.screens.main.control;

import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.laf.WebLookAndFeel;
import com.alee.laf.label.WebLabel;
import com.husker.glassui.GlassUI;
import com.husker.glassui.components.BlurButton;
import com.husker.glassui.components.BlurPagePanel;
import com.husker.glassui.components.BlurPanel;
import com.husker.glassui.components.TagPanel;
import com.husker.glassui.screens.main.keys.KeysPanel;
import com.husker.launcher.components.LabelButton;
import com.husker.launcher.components.TransparentPanel;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;

public class ControlPanel extends TransparentPanel {

    public ControlPanel(Screen screen){
        setLayout(new VerticalFlowLayout(0, 10));
        setPreferredWidth(510);
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

        add(new TagPanel(screen, "Лаунчер"){{
            addButtonAction(() -> {

            });
            setButtonIcons(screen.getLauncher().Resources.Icon_Reload, screen.getLauncher().Resources.Icon_Reload_Selected);
            getContent().setMargin(5, 20, 0, 0);
            addContent(new TransparentPanel(){{
                setLayout(new BorderLayout());
                setMargin(0, 20, 0, 0);

                // Left part
                add(new TransparentPanel(){{
                    setLayout(new BorderLayout(0, 5));
                    setPreferredWidth(160);

                    // List
                    add(new BlurPanel(screen, true){
                        {
                            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                            setPreferredHeight(160);

                            for(int i = 0; i < 5; i++){
                                add(new VersionPanel(screen));
                            }
                        }
                        public void onBlurApply(BlurParameter parameter, Component component) {
                            super.onBlurApply(parameter, component);
                            if(returnOnInvisible(parameter, component))
                                return;

                            GlassUI.applyBottomLayer(parameter);
                            parameter.setShadowSize(5);
                            parameter.setShadowType(BlurParameter.ShadowType.INNER);
                        }
                    });

                    // Pages
                    add(new BlurPagePanel(screen){{
                        setPages(3);
                    }}, BorderLayout.SOUTH);
                }}, BorderLayout.WEST);

                // Right part
                add(new TransparentPanel(){{

                }});
            }});

        }});
    }

    public void reloadVersionsList(){

    }

    static class VersionPanel extends TransparentPanel{
        public VersionPanel(Screen screen){

            setPreferredSize(100, 30);
            setLayout(new BorderLayout());

            WebLabel label = GlassUI.createSimpleLabel("v0.1");
            label.setIcon(new ImageIcon(screen.getLauncher().Resources.Icon_Checkbox_On.getScaledInstance(30, 30, Image.SCALE_SMOOTH)));

            add(label);
        }

    }
}
