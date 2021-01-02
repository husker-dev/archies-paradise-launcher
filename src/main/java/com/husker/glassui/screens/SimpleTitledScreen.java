package com.husker.glassui.screens;

import com.husker.launcher.ui.components.MLabel;
import com.husker.launcher.ui.components.TransparentPanel;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.glassui.GlassUI;
import com.husker.glassui.components.BlurPanel;
import com.husker.launcher.ui.utils.ShapeUtils;
import com.husker.launcher.ui.utils.UIUtils;

import javax.swing.*;
import java.awt.*;

import static com.husker.launcher.ui.utils.ShapeUtils.ALL_CORNERS;
import static java.awt.FlowLayout.CENTER;

public abstract class SimpleTitledScreen extends SimpleCenteredScreen {

    private final String title;
    private final String subTitle;

    private MLabel subTitleLabel, titleLabel;

    public SimpleTitledScreen(String title){
        this(title, null);
    }

    public SimpleTitledScreen(String title, String subTitle){
        this.title = title;
        this.subTitle = subTitle;
    }

    public void onInit() {

        add(new BlurPanel(this, false){
            {
                setLayout(new BorderLayout(0, 10));
                setMargin(10, 0, 0, 0);

                // Title
                add(new TransparentPanel(){{
                    setLayout(new FlowLayout(CENTER, 0, 0));
                    add(createTitlePanel());
                }}, BorderLayout.NORTH);

                add(new BlurPanel(SimpleTitledScreen.this, true){
                    {
                        onMenuInit(this);
                    }

                    public void onBlurApply(BlurParameter parameter, Component component) {
                        super.onBlurApply(parameter, component);
                        if(returnOnInvisible(parameter, component))
                            return;
                        parameter.setShadowClip(UIUtils.keepShadow(parameter, 10, UIUtils.ShadowSide.TOP, UIUtils.ShadowSide.BOTTOM));
                    }
                });

                add(new TransparentPanel(){{
                    setLayout(new FlowLayout(CENTER, 0, 0));
                    onButtonsInit(this);
                }}, BorderLayout.SOUTH);
            }

            public void onBlurApply(BlurParameter parameter, Component component) {
                super.onBlurApply(parameter, component);
                if(returnOnInvisible(parameter, component))
                    return;
                if(component == this){
                    Rectangle bounds = parameter.getShape().getBounds();
                    bounds.height -= 15;

                    parameter.setShape(ShapeUtils.createRoundRectangle(bounds.x, bounds.y, bounds.width, bounds.height, 25, 25, ALL_CORNERS));
                }
            }
        });
    }

    private Component createTitlePanel(){
        return new BlurPanel(SimpleTitledScreen.this){
            {
                setMargin(2, 10, 0, 10);
                setLayout(new FlowLayout(CENTER, 5, 0));
                if(subTitle != null){
                    titleLabel = GlassUI.createTitleLabel(title);
                    titleLabel.setHorizontalAlignment(SwingConstants.RIGHT);
                    titleLabel.setForeground(GlassUI.Colors.labelLightText);

                    JPanel subTitleSeparator = new JPanel(){
                        {
                            setOpaque(false);
                        }
                        public void paint(Graphics graphics) {
                            super.paint(graphics);

                            int indent = 5;

                            graphics.setColor(GlassUI.Colors.separator);
                            graphics.drawLine(getWidth() / 2, indent, getWidth() / 2, getHeight() - indent);
                        }
                    };
                    subTitleSeparator.setPreferredSize(new Dimension(10, 30));
                    subTitleSeparator.setForeground(GlassUI.Colors.labelText);

                    subTitleLabel = GlassUI.createTitleLabel(subTitle);
                    subTitleLabel.setHorizontalAlignment(SwingConstants.LEFT);
                    subTitleLabel.setForeground(GlassUI.Colors.labelText);

                    add(titleLabel, BorderLayout.WEST);
                    add(subTitleSeparator);
                    add(subTitleLabel, BorderLayout.EAST);
                }else {
                    add(titleLabel = GlassUI.createTitleLabel(title));
                }
            }
            public void onBlurApply(BlurParameter parameter, Component component) {
                super.onBlurApply(parameter, component);
                if(returnOnInvisible(parameter, component))
                    return;

                GlassUI.applyTag(parameter);
            }
        };
    }

    public void setTitle(String title){
        titleLabel.setText(title);
    }

    public void setSubTitle(String subTitle){
        subTitleLabel.setText(subTitle);
    }

    public abstract void onMenuInit(TransparentPanel panel);
    public abstract void onButtonsInit(TransparentPanel panel);
}
