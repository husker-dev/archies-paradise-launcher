package com.husker.launcher.ui.impl.glass.screens.main.profile;

import com.alee.extended.label.WebStyledLabel;
import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.style.StyleId;
import com.husker.launcher.Resources;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.ui.impl.glass.GlassUI;
import com.husker.launcher.ui.impl.glass.components.BlurButton;
import com.husker.launcher.ui.impl.glass.components.BlurPanel;
import com.husker.launcher.ui.impl.glass.components.SkinViewer;
import com.husker.launcher.ui.impl.glass.screens.SimpleTitledScreen;
import com.husker.launcher.utils.ShapeUtils;
import com.husker.launcher.utils.UIUtils;

import javax.swing.*;
import java.awt.*;

import static com.husker.launcher.utils.ShapeUtils.ALL_CORNERS;

public class SkinChooserScreen extends SimpleTitledScreen {

    public SkinChooserScreen() {
        super("Скины");
    }

    public void onMenuInit(WebPanel panel) {
        panel.setLayout(new VerticalFlowLayout(0, 0));
        panel.add(new WebPanel(StyleId.panelTransparent){{
            setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
            setPreferredSize(620, 400);
            for(int i = 0; i < 6; i++){
                add(new BlurPanel(SkinChooserScreen.this, true){
                    {
                        setPreferredSize(160, 160);
                        setLayout(new BorderLayout());

                        add(new SkinViewer(getLauncher().Resources.Skin_Steve){{
                            setPreferredSize(10, 160);
                            setCamZoom(30);
                            setRotationEnabled(false);
                            setCamY(22);
                        }});
                        add(new BlurPanel(SkinChooserScreen.this){
                            {
                                add(new WebLabel("Test"){{
                                    setMargin(4, 5, 2, 5);
                                    setPreferredHeight(35);
                                    setVerticalAlignment(CENTER);
                                    setHorizontalAlignment(CENTER);
                                    setForeground(GlassUI.Colors.labelLightText);
                                    setFont(Resources.Fonts.ChronicaPro_ExtraBold.deriveFont(15f));
                                }});
                            }

                            public void onBlurApply(BlurParameter parameter, Component component) {
                                super.onBlurApply(parameter, component);
                                if(returnOnInvisible(parameter, component))
                                    return;
                                if(component == this) {
                                    parameter.setShadowClip(UIUtils.keepShadow(parameter, 15, UIUtils.ShadowSide.TOP));
                                    parameter.setShape(ShapeUtils.createRoundRectangle(getLauncher(), component, 15, 15, ALL_CORNERS));
                                    parameter.setAdditionColor(GlassUI.Colors.buttonDefault);
                                }
                            }
                        }, BorderLayout.SOUTH);

                    }

                    public void onBlurApply(BlurParameter parameter, Component component) {
                        super.onBlurApply(parameter, component);
                        if(returnOnInvisible(parameter, component))
                            return;
                        if(component == this) {
                            parameter.setShape(ShapeUtils.createRoundRectangle(getLauncher(), component, 15, 15, ALL_CORNERS));
                            parameter.setShadowSize(5);
                        }
                    }
                });
            }
        }});
        panel.add(new WebPanel(StyleId.panelTransparent){{
            setMargin(0, 100, 0, 100);
            add(new BlurPanel(SkinChooserScreen.this, true){{
                setPreferredHeight(60);
            }});
        }});
    }

    public void onButtonsInit(WebPanel panel) {
        panel.add(new BlurButton(this, "Назад"){{
            addActionListener(e -> getLauncherUI().setScreen("main"));
            setMargin(0, 15, 0, 15);
        }});
    }
}
