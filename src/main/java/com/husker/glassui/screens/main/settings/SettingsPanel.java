package com.husker.glassui.screens.main.settings;

import com.alee.extended.layout.VerticalFlowLayout;
import com.husker.glassui.components.*;
import com.husker.launcher.Resources;
import com.husker.launcher.settings.LauncherSettings;
import com.husker.launcher.ui.components.MLabel;
import com.husker.launcher.ui.components.TransparentPanel;
import com.husker.launcher.ui.Screen;
import com.husker.glassui.GlassUI;
import com.husker.launcher.ui.utils.ShapeUtils;
import com.sun.management.OperatingSystemMXBean;


import javax.swing.*;
import java.awt.*;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;

import static com.husker.launcher.ui.utils.ShapeUtils.*;
import static java.awt.FlowLayout.LEFT;

public class SettingsPanel extends TransparentPanel {

    private final Screen screen;
    private BlurButtonLineChooser ramChooser;
    private BlurTextField ramCustomValue;
    private BlurButtonLineChooser ramCustomValueType;

    public SettingsPanel(Screen screen){
        this.screen = screen;

        setLayout(new VerticalFlowLayout(0, 10));
        setMargin(25, 25, 10, 25);

        add(createLabel("Лаунчер"));
        add(new TransparentPanel(){{
            setMargin(0, 40, 0, 40);
            setLayout(new VerticalFlowLayout(0, 8));
            add(new TransparentPanel(){{
                setLayout(new FlowLayout(LEFT, 0, 0));

                add(new BlurCheckbox(screen, "Сохранять пароль", LauncherSettings.isAutoAuth()){{
                    setOnAction(() -> LauncherSettings.setAutoAuth(isChecked()));
                }});
                add(Box.createRigidArea(new Dimension(25, 0)));
                add(new BlurCheckbox(screen, "Закрывать при игре", !LauncherSettings.isPotatoSettings()){{
                    setOnAction(() -> LauncherSettings.setAutoClose(!isChecked()));
                }});
            }});

            add(new TransparentPanel(){{
                setLayout(new FlowLayout(LEFT, 0, 0));

                add(new BlurButton(screen, "Изменить фон"){{
                    setForeground(GlassUI.Colors.labelText);
                    setPadding(15, 15);
                    addActionListener(e -> screen.getLauncherUI().setScreen(BackgroundSelection.class));
                }});
            }});
        }});
        add(createLabel("Игра"));
        add(new TransparentPanel(){{
            setMargin(0, 40, 0, 40);
            setLayout(new VerticalFlowLayout(0, 5));
            add(new BlurCheckbox(screen, "Запускать в окне", LauncherSettings.isWindowed()){{
                setOnAction(() -> LauncherSettings.setWindowed(isChecked()));
            }});

            add(Box.createRigidArea(new Dimension(0, 0)));
            add(new MLabel("Выделяемая память"){{
                setForeground(GlassUI.Colors.labelText);
                setFont(Resources.Fonts.getChronicaProExtraBold());
            }});

            int gb = 1024;
            ArrayList<Integer> memories = new ArrayList<>(Arrays.asList(gb, 2 * gb, 4 * gb, 5 * gb, 7 * gb, 10 * gb));
            int value = LauncherSettings.getRAM();

            add(ramChooser = new BlurButtonLineChooser(screen){{
                OperatingSystemMXBean mxbean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
                long mem = mxbean.getTotalPhysicalMemorySize();
                long memMb = (int)(mem / 1024d / 1024d);

                for(int i = 0; i < memories.size(); i++){
                    if(memories.get(i) >= 1024)
                        addButton(getNum(memories.get(i) / 1024f) + " Гб");
                    else
                        addButton(memories.get(i) + " Мб");
                    if(memories.get(i) > memMb)
                        setEnabled(i, false);
                }
                addButton("...");
                addSelectedListener(index -> {
                    if(index == getComponentCount() - 1){
                        ramCustomValue.setVisible(true);
                        ramCustomValueType.setVisible(true);
                        if(ramCustomValue.getText().isEmpty())
                            ramCustomValue.setText("256");
                        ramCustomValueType.setSelected(0);
                    }else{
                        LauncherSettings.setRAM(memories.get(index));
                        ramCustomValue.setVisible(false);
                        ramCustomValueType.setVisible(false);
                    }
                });
            }});
            add(Box.createRigidArea(new Dimension(0, 5)));
            add(new TransparentPanel(){{
                setLayout(new BorderLayout(10, 0));
                add(ramCustomValue = new BlurTextField(screen){{
                    addTextListener(text -> {
                        try {
                            if (ramCustomValueType.getSelectedIndex() == 0)
                                LauncherSettings.setRAM(ramCustomValue.getText().isEmpty() ? 1024 : Integer.parseInt(ramCustomValue.getText()));
                            else
                                LauncherSettings.setRAM(ramCustomValue.getText().isEmpty() ? 256 : (Integer.parseInt(ramCustomValue.getText()) * 1024));
                        }catch (Exception ex){
                            LauncherSettings.setRAM(256);
                        }
                    });
                }});
                add(ramCustomValueType = new BlurButtonLineChooser(screen){{
                    setPreferredWidth(150);
                    addButton("Мб");
                    addButton("Гб");
                    addSelectedListener(index -> {
                        try {
                            if (ramCustomValueType.getSelectedIndex() == 0)
                                LauncherSettings.setRAM(ramCustomValue.getText().isEmpty() ? 1024 : Integer.parseInt(ramCustomValue.getText()));
                            else
                                LauncherSettings.setRAM(ramCustomValue.getText().isEmpty() ? 256 : (Integer.parseInt(ramCustomValue.getText()) * 1024));
                        }catch (Exception ex){
                            LauncherSettings.setRAM(256);
                        }
                    });
                }}, BorderLayout.EAST);
            }});

            if(memories.contains(value)) {
                ramChooser.setSelected(memories.indexOf(value));
                ramCustomValue.setVisible(false);
                ramCustomValueType.setVisible(false);
            }else{
                ramChooser.setSelected(ramChooser.getComponentCount() - 1);
                ramCustomValue.setVisible(true);
                ramCustomValueType.setVisible(true);

                if(value % 1024 == 0) {
                    ramCustomValue.setText(value / 1024 + "");
                    ramCustomValueType.setSelected(1);
                }else{
                    ramCustomValue.setText(value + "");
                    ramCustomValueType.setSelected(0);
                }
            }
        }});
    }

    private String getNum(float num){
        if(num == (int)num)
            return "" + (int)num;
        else
            return "" + num;
    }

    public Component createLabel(String text){
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

            screen.addBlurSegment("SettingsPanel.Label", parameter -> {
                if(BlurComponent.isReturnOnInvisible(parameter, this))
                    return;

                GlassUI.applyTopLayer(parameter);
                parameter.setAdditionColor(GlassUI.Colors.third);
                if (isDisplayable()) {
                    parameter.setShape(ShapeUtils.createRoundRectangle(this, 10, 10, ALL_CORNERS));
                    parameter.setShadowSize(4);
                }
            });
        }});
        return panel;
    }
}
