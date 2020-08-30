package com.husker.launcher.ui.impl.glass.screens.main.profile;

import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.style.StyleId;
import com.husker.launcher.Resources;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.ui.impl.glass.GlassUI;
import com.husker.launcher.ui.impl.glass.screens.SimpleCenteredScreen;
import com.husker.launcher.ui.impl.glass.components.*;
import com.husker.launcher.utils.ShapeUtils;

import java.awt.*;

import static com.husker.launcher.utils.ShapeUtils.ALL_CORNERS;
import static java.awt.FlowLayout.CENTER;

public abstract class InfoEditPanel extends SimpleCenteredScreen {

    private SkinViewer viewer;

    private WebLabel titleLabel;
    private WebLabel subTitleLabel;

    public void onInit() {
        add(new BlurPanel(this, false){
            {
                setPreferredWidth(340);
                setLayout(new BorderLayout(0, 0));

                add(new BlurPanel(InfoEditPanel.this, true){{
                    setLayout(new BorderLayout(0, 0));
                    add(titleLabel = GlassUI.createTitleLabel("Редактирование"));
                }}, BorderLayout.NORTH);

                add(new WebPanel(StyleId.panelTransparent){{
                    setLayout(new VerticalFlowLayout(0, 0));

                    setMargin(10, 0, 0, 0);
                    add(viewer = new SkinViewer(Resources.getBufferedImage("steve.png")){{
                        setRotationEnabled(false);
                        setCamY(28);
                        setCamZoom(18);
                        setPreferredHeight(180);
                        setRotationY(-8);
                    }});

                    add(new BlurPanel(InfoEditPanel.this, true){
                        {
                            setLayout(new BorderLayout(0, 0));

                            // Content
                            add(new WebPanel(StyleId.panelTransparent){{
                                setLayout(new VerticalFlowLayout(0, 0));
                                setMargin(10, 0, 0, 0);

                                add(subTitleLabel = new WebLabel("Информация"){{
                                    setHorizontalAlignment(CENTER);
                                    setForeground(GlassUI.Colors.labelText);
                                    setFont(Resources.Fonts.ChronicaPro_ExtraBold.deriveFont(25f));
                                }});

                                onContentInit(this);
                            }});
                            add(new WebPanel(StyleId.panelTransparent){{
                                setLayout(new FlowLayout(CENTER, 10, 0));
                                setMargin(20, 0, 0, 0);

                                onButtonsInit(this);
                            }}, BorderLayout.SOUTH);
                        }

                        public void onBlurApply(BlurParameter parameter, Component component) {
                            super.onBlurApply(parameter, component);
                            if(returnOnInvisible(parameter, component))
                                return;
                            if(component == this)
                                parameter.setShape(cutRectangle(parameter.getShape()));
                        }
                    });
                }});
            }
            public void onBlurApply(BlurParameter parameter, Component component) {
                super.onBlurApply(parameter, component);
                if(returnOnInvisible(parameter, component))
                    return;
                if(component == this)
                    parameter.setShape(cutRectangle(parameter.getShape()));
            }
        });
    }

    public abstract void onContentInit(WebPanel panel);
    public abstract void onButtonsInit(WebPanel panel);

    public void setTitle(String title){
        titleLabel.setText(title);
    }

    public void setSubTitle(String subTitle){
        subTitleLabel.setText(subTitle);
    }

    protected Component createSeparator(){
        return new WebPanel(StyleId.panelTransparent){
            {
                setPreferredHeight(20);
            }
            public void paint(Graphics gr) {
                gr.setColor(new Color(180, 180, 180));
                gr.drawLine(20, getHeight() / 2, getWidth() - 20, getHeight() / 2);
            }
        };
    }

    protected BlurTextField createTextField(){
        return new BlurTextField(this){{
            setPreferredWidth(250);
        }};
    }

    protected BlurPasswordField createPasswordField(){
        return new BlurPasswordField(this){{
            setPreferredWidth(250);
        }};
    }

    protected Component createTitleLabel(String text){
        return new WebPanel(StyleId.panelTransparent){{
            setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

            add(new WebLabel(text){{
                setForeground(GlassUI.Colors.labelText);
                setFont(Resources.Fonts.ChronicaPro_ExtraBold);
            }});
        }};
    }

    public SkinViewer getSkinViewer(){
        return viewer;
    }

    private Shape cutRectangle(Shape shape){
        Rectangle bounds = shape.getBounds();
        bounds.height -= 15;
        return ShapeUtils.createRoundRectangle(bounds.x, bounds.y, bounds.width, bounds.height, 25, 25, ALL_CORNERS);
    }

    public void onShow() {
        viewer.setPlayerTexture(getLauncher().NetManager.PlayerInfo.getSkin());
    }
}


