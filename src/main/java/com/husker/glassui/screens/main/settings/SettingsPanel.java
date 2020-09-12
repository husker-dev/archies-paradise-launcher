package com.husker.glassui.screens.main.settings;

import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.laf.label.WebLabel;
import com.alee.managers.style.StyleId;
import com.husker.glassui.components.*;
import com.husker.launcher.Resources;
import com.husker.launcher.components.TransparentPanel;
import com.husker.launcher.ui.Screen;
import com.husker.glassui.GlassUI;
import com.husker.launcher.utils.ShapeUtils;
import com.sun.management.OperatingSystemMXBean;

import javax.swing.*;
import java.awt.*;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;

import static com.husker.launcher.utils.ShapeUtils.*;
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
            add(new BlurCheckbox(screen, "Сохранить пароль", screen.getLauncher().getSettings().isAutoAuth()){{
                setOnAction(() -> {
                    screen.getLauncher().getSettings().setAutoAuth(isChecked());
                    if(isChecked()) {
                        screen.getLauncher().getUserConfig().setLogin(screen.getLauncher().NetManager.PlayerInfo.getNickname());
                        screen.getLauncher().getUserConfig().setPassword(screen.getLauncher().NetManager.PlayerInfo.getEncryptedPassword());
                    }else
                        screen.getLauncher().getUserConfig().reset();
                });
            }});

            add(new TransparentPanel(){{
                setLayout(new FlowLayout(LEFT, 0, 0));

                add(new BlurButton(screen, "Изменить фон"){{
                    setForeground(GlassUI.Colors.labelText);
                    setMargin(0, 15, 0, 15);
                    addActionListener(e -> screen.getLauncherUI().setScreen("backgroundSelection"));
                }});
            }});
        }});
        //add(Box.createRigidArea(new Dimension(0, 5)));
        add(createLabel("Игра"));
        add(new TransparentPanel(){{
            setMargin(0, 40, 0, 40);
            setLayout(new VerticalFlowLayout(0, 5));
            add(new BlurCheckbox(screen, "Запускать в окне", screen.getLauncher().getSettings().isWindowed()){{
                setOnAction(() -> screen.getLauncher().getSettings().setWindowed(isChecked()));
            }});

            add(Box.createRigidArea(new Dimension(0, 0)));
            add(new WebLabel("Выделяемая память"){{
                setForeground(GlassUI.Colors.labelText);
                setFont(Resources.Fonts.ChronicaPro_ExtraBold);
            }});

            ArrayList<Integer> memories = new ArrayList<>(Arrays.asList(256, 512, 1024, 2048, 4096, 8192));
            int value = screen.getLauncher().getSettings().getRAM();

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
                        screen.getLauncher().getSettings().setRAM(memories.get(index));
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
                                screen.getLauncher().getSettings().setRAM(ramCustomValue.getText().isEmpty() ? 1024 : Integer.parseInt(ramCustomValue.getText()));
                            else
                                screen.getLauncher().getSettings().setRAM(ramCustomValue.getText().isEmpty() ? 256 : (Integer.parseInt(ramCustomValue.getText()) * 1024));
                        }catch (Exception ex){
                            screen.getLauncher().getSettings().setRAM(256);
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
                                screen.getLauncher().getSettings().setRAM(ramCustomValue.getText().isEmpty() ? 1024 : Integer.parseInt(ramCustomValue.getText()));
                            else
                                screen.getLauncher().getSettings().setRAM(ramCustomValue.getText().isEmpty() ? 256 : (Integer.parseInt(ramCustomValue.getText()) * 1024));
                        }catch (Exception ex){
                            screen.getLauncher().getSettings().setRAM(256);
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
        return panel;
    }
}
