package com.husker.launcher.ui.impl.glass.screens.main;

import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.style.StyleId;
import com.husker.launcher.Resources;
import com.husker.launcher.components.ScalableImage;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.impl.glass.GlassUI;
import com.husker.launcher.ui.impl.glass.components.BlurPanel;
import com.husker.launcher.utils.ShapeUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.TimerTask;

public abstract class AbstractMainScreen extends Screen {

    private final int logo_width = 300;
    private final int logoPlateWidth = 700;
    private final int logoPlateHeight = 175;

    private final int statusBarWidth = 300;

    private WebLabel statusLabel;

    abstract void onMenuInit(WebPanel menu);
    abstract void onRightMenuInit(WebPanel menu);

    public void onInit() {
        addBlurSegment(parameter -> {
            GlassUI.applyBottomLayer(parameter);
            parameter.setShape(ShapeUtils.createRoundRectangle(
                    (getWidth() - (logoPlateWidth / 2d + 100)) / 2d,
                    -25,
                    logoPlateWidth / 2d + 100,
                    logoPlateHeight,
                    200,
                    logoPlateHeight,
                    ShapeUtils.Corner.BOTTOM_RIGHT, ShapeUtils.Corner.BOTTOM_LEFT));
        });


        addBlurSegment(parameter -> {
            GlassUI.applyTopLayer(parameter);
            parameter.setShape(new RoundRectangle2D.Double((getWidth() - statusBarWidth) / 2d, logoPlateHeight - 40, statusBarWidth, 30, 25, 25));
        });

        // Swing part
        setLayout(new BorderLayout());

        // Title
        add(new WebPanel(StyleId.panelTransparent){{
            setLayout(new BorderLayout());

            // Left
            add(new WebPanel(StyleId.panelTransparent){{

            }}, BorderLayout.WEST);

            // Center
            add(new WebPanel(StyleId.panelTransparent){{
                setLayout(new VerticalFlowLayout(0, 0));
                add(new ScalableImage(getLauncher().Resources.Logo, ScalableImage.FitType.FIT_Y){{
                    setPreferredSize(new Dimension(logo_width, 135));
                }});
                add(Box.createRigidArea(new Dimension(0, 3)));
                add(new WebPanel(StyleId.panelTransparent){{
                    setPreferredHeight(30);
                    setLayout(new FlowLayout(FlowLayout.CENTER));

                    new java.util.Timer().schedule(new TimerTask() {
                        public void run() {
                            getLauncher().NetManager.updateStatusLabel(statusLabel);
                        }
                    }, 0, 500);

                    add(new WebLabel("Статус: "){{
                        setForeground(new Color(70, 70, 70));
                        setPreferredHeight(15);
                        setFont(Resources.Fonts.ChronicaPro_ExtraBold);
                    }});
                    add(statusLabel = new WebLabel("Обновление..."){{
                        setForeground(new Color(120, 120, 120));
                        setPreferredHeight(15);
                        setFont(Resources.Fonts.ChronicaPro_ExtraBold);
                    }});
                }}, BorderLayout.WEST);
            }}, BorderLayout.NORTH);

            // Right
            add(new WebPanel(StyleId.panelTransparent){{

            }}, BorderLayout.EAST);
        }}, BorderLayout.NORTH);

        add(new WebPanel(StyleId.panelTransparent){{
            setLayout(new GridBagLayout(){{
            }});

            // Left panel *reserved*
            add(new WebPanel(StyleId.panelTransparent){{
                setPreferredWidth(220);
                setPreferredHeight(450);
            }}, new GridBagConstraints(){{
                this.weightx = 1;
                this.anchor = GridBagConstraints.EAST;
                this.gridx = 1;
                this.insets = new Insets(0, 0, 0, 30);
            }});

            // Center panel
            add(new BlurPanel(AbstractMainScreen.this){{
                setPreferredWidth(500);

                setLayout(new VerticalFlowLayout(0, 0));
                setName("MainMenuPanel");
                onMenuInit(this);
            }}, new GridBagConstraints(){{
                this.weightx = 0;
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
                this.gridx = 3;
                this.insets = new Insets(0, 30, 0, 0);
            }});
        }}, BorderLayout.CENTER);
    }
}
