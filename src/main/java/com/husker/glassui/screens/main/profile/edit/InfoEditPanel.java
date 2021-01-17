package com.husker.glassui.screens.main.profile.edit;

import com.alee.extended.layout.VerticalFlowLayout;
import com.husker.glassui.components.BlurPanel;
import com.husker.glassui.components.BlurPasswordField;
import com.husker.glassui.components.BlurTextField;
import com.husker.launcher.Resources;
import com.husker.launcher.ui.components.MLabel;
import com.husker.launcher.ui.components.TransparentPanel;
import com.husker.launcher.ui.components.skin.SkinViewer;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.glassui.GlassUI;
import com.husker.glassui.screens.SimpleCenteredScreen;
import com.husker.launcher.ui.utils.ComponentUtils;
import com.husker.launcher.ui.utils.ShapeUtils;
import com.husker.launcher.ui.utils.UIUtils;

import java.awt.*;

import static com.husker.launcher.ui.utils.ShapeUtils.ALL_CORNERS;
import static java.awt.FlowLayout.CENTER;

public abstract class InfoEditPanel extends SimpleCenteredScreen {

    private SkinViewer viewer;

    private MLabel titleLabel;
    private MLabel subTitleLabel;

    public void onInit() {
        add(new BlurPanel(this, false){
            {
                setPreferredWidth(340);
                setLayout(new BorderLayout(0, 0));

                add(new BlurPanel(InfoEditPanel.this, true){
                    {
                        setLayout(new BorderLayout(0, 0));
                        add(titleLabel = GlassUI.createTitleLabel("Редактирование"));
                    }
                    public void onBlurApply(BlurParameter parameter, Component component) {
                        super.onBlurApply(parameter, component);
                        if(returnOnInvisible(parameter, component))
                            return;
                        if(component == this)
                            parameter.setShadowClip(UIUtils.keepShadow(parameter, 25, UIUtils.ShadowSide.BOTTOM));
                    }
                }, BorderLayout.NORTH);

                add(new TransparentPanel(){{
                    setLayout(new BorderLayout(0, 0));

                    setMargin(10, 0, 0, 0);

                    add(viewer = new SkinViewer(Resources.getBufferedImage("steve.png")){{
                        setRotationEnabled(false);
                        setCamY(28);
                        setCamZoom(18);
                        setPreferredSize(new Dimension(60, 180));
                        setRotationY(-8);
                        ComponentUtils.makeMouseEventTransparent(this);
                    }}, BorderLayout.NORTH);

                    add(new BlurPanel(InfoEditPanel.this, true){
                        {
                            setLayout(new BorderLayout(0, 0));

                            // Content
                            add(new TransparentPanel(){{
                                setLayout(new VerticalFlowLayout(0, 0));
                                setMargin(10, 0, 0, 0);

                                add(subTitleLabel = new MLabel("Информация"){{
                                    setHorizontalAlignment(CENTER);
                                    setForeground(GlassUI.Colors.labelText);
                                    setFont(Resources.Fonts.getChronicaProExtraBold(25));
                                }});

                                onContentInit(this);
                            }});
                            add(new TransparentPanel(){{
                                setLayout(new FlowLayout(CENTER, 10, 0));
                                setMargin(20, 0, 0, 0);

                                onButtonsInit(this);
                            }}, BorderLayout.SOUTH);
                        }

                        public void onBlurApply(BlurParameter parameter, Component component) {
                            super.onBlurApply(parameter, component);
                            if(returnOnInvisible(parameter, component))
                                return;
                            if(component == this) {
                                parameter.setShape(cutRectangle(parameter.getShape()));
                                parameter.setShadowClip(UIUtils.keepShadow(parameter, 25, UIUtils.ShadowSide.TOP));
                            }
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

    public abstract void onContentInit(TransparentPanel panel);
    public abstract void onButtonsInit(TransparentPanel panel);

    public void setTitle(String title){
        titleLabel.setText(title);
    }

    public void setSubTitle(String subTitle){
        subTitleLabel.setText(subTitle);
    }

    protected Component createSeparator(){
        return new TransparentPanel(){
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
        return new TransparentPanel(){{
            setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

            add(new MLabel(text){{
                setForeground(GlassUI.Colors.labelText);
                setFont(Resources.Fonts.getChronicaProExtraBold());
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
        viewer.setPlayerTexture(getLauncher().User.getSkin());
    }
}


