package com.husker.glassui.components.social;


import com.alee.utils.swing.extensions.SizeMethods;
import com.husker.glassui.GlassUI;
import com.husker.glassui.components.BlurPanel;
import com.husker.launcher.Resources;
import com.husker.launcher.ui.components.MStyledLabel;
import com.husker.launcher.ui.components.TransparentPanel;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.ui.utils.ComponentUtils;
import com.husker.launcher.ui.utils.ShapeUtils;
import com.husker.launcher.ui.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import static com.husker.launcher.ui.utils.ShapeUtils.ALL_CORNERS;

public abstract class SocialPanel extends BlurPanel {

    private MStyledLabel titleLabel;

    private final Color defaultTitleColor = GlassUI.Colors.labelLightText;
    private final Color hoveredTitleColor = GlassUI.Colors.labelText;

    private boolean isSelectable = true;

    private boolean useTransparentTitle = false;

    private boolean inner = false;

    public SocialPanel(Screen screen) {
        super(screen, true);

        setLayout(new OverlayLayout(this));
        setPreferredHeight(135);
        setPreferredWidth(200);

        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if(isSelectable) {
                    titleLabel.setMaximumRows(6);
                    titleLabel.setMaximumHeight(80);
                    titleLabel.setMinimumHeight(80);
                    titleLabel.setPreferredHeight(SizeMethods.UNDEFINED);
                    titleLabel.updateUI();
                    screen.getLauncher().updateUI();

                    titleLabel.setForeground(hoveredTitleColor);
                }
            }
            public void mouseExited(MouseEvent e) {
                if(isSelectable) {
                    titleLabel.setMaximumRows(2);
                    titleLabel.setMaximumHeight(35);
                    titleLabel.setMinimumHeight(35);
                    titleLabel.setPreferredHeight(35);
                    titleLabel.updateUI();
                    screen.getLauncher().updateUI();

                    titleLabel.setForeground(defaultTitleColor);
                }
            }
            public void mouseClicked(MouseEvent e) {
                onClick();
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            public void mouseMoved(MouseEvent e) {
                if(isSelectable)
                    titleLabel.setForeground(hoveredTitleColor);
            }
            public void mouseDragged(MouseEvent e) {
                if(isSelectable)
                    titleLabel.setForeground(hoveredTitleColor);
            }
        });

        add(new TransparentPanel(){{
            ComponentUtils.makeMouseEventTransparent(this, SocialPanel.this);
            onContentInit(this);
        }}, 0);
        add(new TransparentPanel(){{
            ComponentUtils.makeMouseEventTransparent(this, SocialPanel.this);
            setLayout(new BorderLayout());
            add(titleLabel = createTitleLabel(), BorderLayout.SOUTH);
        }}, 0);
    }

    public boolean isHovered(){
        return titleLabel.getForeground() == hoveredTitleColor;
    }

    public MStyledLabel getTitleLabel(){
        return titleLabel;
    }

    public void setTitle(String text){
        titleLabel.setText(text);
    }

    public void setInner(boolean inner){
        this.inner = inner;
    }

    public boolean isInner(){
        return inner;
    }

    public void onBlurApply(BlurParameter parameter, Component component) {
        super.onBlurApply(parameter, component);
        if(returnOnInvisible(parameter, component))
            return;

        if(component == this) {
            parameter.setShadowSize(5);
            parameter.setBlurFactor(0);

            parameter.setShadowType(inner ? BlurParameter.ShadowType.INNER : BlurParameter.ShadowType.OUTER);

            Rectangle bounds = parameter.getShape().getBounds();
            parameter.setShape(ShapeUtils.createRoundRectangle(bounds.x, bounds.y, bounds.width, bounds.height, 15, 15, ALL_CORNERS));
        }
        if(component == titleLabel){
            GlassUI.applyTopLayer(parameter);
            parameter.setShadowSize(5);
            parameter.setBlurFactor(useTransparentTitle ? 0 : 25);

            if(titleLabel.getForeground() == defaultTitleColor)
                parameter.setAdditionColor(GlassUI.Colors.third);
            else
                parameter.setAdditionColor(GlassUI.Colors.buttonDefault);

            parameter.setShape(ShapeUtils.createRoundRectangle(titleLabel, 15, 15, ALL_CORNERS));

            if(!isInner())
                parameter.setShadowClip(UIUtils.keepShadow(parameter, 15, UIUtils.ShadowSide.TOP));
        }
    }

    public void setIcon(BufferedImage image){
        if(image == null){
            titleLabel.setImage(null);
            return;
        }

        float width = image.getWidth();
        float height = image.getHeight();

        float size = 22;

        float newWidth, newHeight;

        if(width > height){
            newWidth = size;
            newHeight = size / width * height;
        }else{
            newHeight = size;
            newWidth = size / height * width;
        }
        titleLabel.setImageSize((int)newWidth, (int)newHeight);
        titleLabel.setImage(image);
    }

    private MStyledLabel createTitleLabel(){
        return new MStyledLabel(){
            {
                ComponentUtils.makeMouseEventTransparent(this, SocialPanel.this);
                setOpaque(false);
                setBackground(new Color(0, 0, 0, 0));

                setMargin(4, 5, 2, 5);
                setPreferredHeight(35);
                setVerticalAlignment(CENTER);
                setMaximumRows(2);
                setForeground(defaultTitleColor);
                setFont(Resources.Fonts.getChronicaProExtraBold(10));

                setMaximumTextWidth(180);

                getScreen().addBlurSegment("SocialPanel.Title", parameter -> onBlurApply(parameter, this));
            }
            public void setText(String text) {
                super.setText(text);
                if(text == null)
                    return;

                float minFontSize = 10;
                float maxFontSize = 14;

                for(int i = (int)maxFontSize; i >= minFontSize; i--){
                    if(getFontMetrics(Resources.Fonts.getChronicaProBold(i)).stringWidth(text) < 160) {
                        setFont(Resources.Fonts.getChronicaProBold(i));
                        break;
                    }
                }
            }
        };
    }

    public abstract void onContentInit(TransparentPanel panel);
    public abstract void update();

    public void onClick(){}

    public boolean isSelectable() {
        return isSelectable;
    }

    public void setSelectable(boolean selectable) {
        isSelectable = selectable;
    }

    public boolean isUseTransparentTitle() {
        return useTransparentTitle;
    }

    public void setUseTransparentTitle(boolean useTransparentTitle) {
        this.useTransparentTitle = useTransparentTitle;
    }
}
