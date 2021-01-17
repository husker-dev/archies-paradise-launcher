package com.husker.glassui.screens.main;

import com.alee.extended.layout.VerticalFlowLayout;
import com.husker.glassui.components.BlurPanel;
import com.husker.glassui.components.BlurScalableImage;
import com.husker.launcher.Resources;
import com.husker.launcher.managers.NetManager;
import com.husker.launcher.ui.components.MLabel;
import com.husker.launcher.ui.components.TransparentPanel;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.glassui.GlassUI;
import com.husker.launcher.ui.utils.ShapeUtils;

import java.awt.*;
import java.util.TimerTask;

import static com.husker.launcher.ui.utils.ShapeUtils.ALL_CORNERS;

public abstract class AbstractMainScreen extends Screen {

    private MLabel statusLabel;

    abstract void onMenuInit(TransparentPanel menu);
    abstract void onRightMenuInit(TransparentPanel menu);
    abstract void onLeftMenuInit(TransparentPanel menu);

    public void onInit() {
        setLayout(new GridBagLayout());

        add(new BlurPanel(this, true){
            {
                setPreferredSize(400, 170);
                setLayout(new BorderLayout());
                add(new BlurScalableImage(AbstractMainScreen.this, Resources.Logo){
                    {
                        setFitType(FitType.FILL_Y);
                    }

                    public void onBlurApply(BlurParameter parameter, Component component) {
                        super.onBlurApply(parameter, component);
                        if(returnOnInvisible(parameter, component))
                            return;
                        if(component == this) {
                            parameter.setShadowSize(0);
                            parameter.setBlurFactor(0);
                        }
                    }
                });
                add(new TransparentPanel(){{
                    setLayout(new BorderLayout());
                    setMargin(0, 60, 0, 60);
                    add(new BlurPanel(AbstractMainScreen.this){
                        {
                            setPreferredHeight(30);
                            setMargin(3, 0, 0, 0);
                            setLayout(new FlowLayout(FlowLayout.CENTER));

                            new java.util.Timer().schedule(new TimerTask() {
                                public void run() {
                                    NetManager.updateStatusLabel(getLauncher(), statusLabel);
                                }
                            }, 0, 500);

                            add(new MLabel("Статус: "){{
                                setForeground(new Color(70, 70, 70));
                                setPreferredHeight(15);
                                setFont(Resources.Fonts.getChronicaProExtraBold());
                            }});
                            add(statusLabel = new MLabel("Обновление..."){{
                                setForeground(new Color(120, 120, 120));
                                setPreferredHeight(15);
                                setFont(Resources.Fonts.getChronicaProExtraBold());
                            }});
                        }
                        public void onBlurApply(BlurParameter parameter, Component component) {
                            super.onBlurApply(parameter, component);
                            if(returnOnInvisible(parameter, component))
                                return;
                            if(component == this)
                                parameter.setAdditionColor(GlassUI.Colors.buttonDefault);
                        }
                    });
                }}, BorderLayout.SOUTH);
            }

            public void onBlurApply(BlurParameter parameter, Component component) {
                super.onBlurApply(parameter, component);
                if(returnOnInvisible(parameter, component))
                    return;
                if(component == this){
                    Rectangle bounds = component.getBounds();
                    parameter.setShape(ShapeUtils.createRoundRectangle(bounds.x, bounds.y, bounds.width, bounds.height - 15, 30, 30, ALL_CORNERS));
                }
            }
        }, new GridBagConstraints(){{
            this.weightx = 0;
            this.gridy = 0;
            this.gridx = 2;
            this.insets = new Insets(0, 0, 30, 0);
        }});

        // Left panel *reserved*
        add(new TransparentPanel(){{
            setPreferredWidth(220);
            setPreferredHeight(450);
            onLeftMenuInit(this);
        }}, new GridBagConstraints(){{
            this.weightx = 1;
            this.anchor = GridBagConstraints.EAST;
            this.gridy = 1;
            this.gridx = 1;
            this.insets = new Insets(0, 0, 0, 30);
        }});

        // Center panel
        add(new BlurPanel(AbstractMainScreen.this){{
            setPreferredWidth(550);

            setLayout(new VerticalFlowLayout(0, 0));
            setName("MainMenuPanel");
            onMenuInit(this);
        }}, new GridBagConstraints(){{
            this.weightx = 0;
            this.gridy = 1;
            this.gridx = 2;
        }});

        // Right panel
        add(new BlurPanel(AbstractMainScreen.this){{
            setPreferredWidth(220);
            setPreferredHeight(450);
            setName("RightMenuPanel");
            onRightMenuInit(this);
        }}, new GridBagConstraints(){{
            this.weightx = 1;
            this.anchor = GridBagConstraints.WEST;
            this.gridy = 1;
            this.gridx = 3;
            this.insets = new Insets(0, 30, 0, 0);
        }});

    }
}
