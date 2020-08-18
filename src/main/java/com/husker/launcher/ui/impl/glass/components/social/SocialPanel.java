package com.husker.launcher.ui.impl.glass.components.social;

import com.alee.extended.label.WebStyledLabel;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.style.StyleId;
import com.husker.launcher.Resources;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.ui.impl.glass.GlassUI;
import com.husker.launcher.ui.impl.glass.components.BlurPanel;
import com.husker.launcher.utils.ComponentUtils;
import com.husker.launcher.utils.ConsoleUtils;
import com.husker.launcher.utils.ShapeUtils;
import com.husker.launcher.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import static com.husker.launcher.utils.ShapeUtils.ALL_CORNERS;

public abstract class SocialPanel extends BlurPanel {

    private WebStyledLabel titleLabel;

    private final Color defaultTitleColor = GlassUI.Colors.labelLightText;
    private final Color hoveredTitleColor = GlassUI.Colors.labelText;

    public SocialPanel(Screen screen) {
        super(screen, true);

        setLayout(new OverlayLayout(this));
        setPreferredHeight(135);
        setPreferredWidth(200);

        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                titleLabel.setForeground(hoveredTitleColor);
            }
            public void mouseExited(MouseEvent e) {
                titleLabel.setForeground(defaultTitleColor);
            }
            public void mouseClicked(MouseEvent e) {
                onClick();
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            public void mouseMoved(MouseEvent e) {
                titleLabel.setForeground(hoveredTitleColor);
            }
            public void mouseDragged(MouseEvent e) {
                titleLabel.setForeground(hoveredTitleColor);
            }
        });

        add(new WebPanel(StyleId.panelTransparent){{
            ComponentUtils.makeMouseEventTransparent(this, SocialPanel.this);
            onContentInit(this);
        }}, 0);
        add(new WebPanel(StyleId.panelTransparent){{
            ComponentUtils.makeMouseEventTransparent(this, SocialPanel.this);
            setLayout(new BorderLayout());
            add(titleLabel = createTitleLabel(), BorderLayout.SOUTH);
        }}, 0);

    }

    public WebStyledLabel getTitleLabel(){
        return titleLabel;
    }

    public void setTitle(String text){
        titleLabel.setText(text);
    }

    public void onBlurApply(BlurParameter parameter, Component component) {
        super.onBlurApply(parameter, component);

        if(component == this) {
            parameter.setShadowSize(5);
            parameter.setBlurFactor(0);
            parameter.setDebugName("SocialPanel");
            if (parameter.getShape() != null) {
                Rectangle bounds = parameter.getShape().getBounds();
                parameter.setShape(ShapeUtils.createRoundRectangle(bounds.x, bounds.y, bounds.width, bounds.height, 15, 15, ALL_CORNERS));
            }
        }
        if(component == titleLabel){
            GlassUI.applyTopLayer(parameter);
            parameter.setShadowSize(5);
            parameter.setDebugName("SocialPanelLabel");

            if(getForeground() == defaultTitleColor)
                parameter.setAdditionColor(GlassUI.Colors.third);
            else
                parameter.setAdditionColor(GlassUI.Colors.buttonDefault);
            parameter.setShape(ShapeUtils.createRoundRectangle(getScreen().getLauncher(), titleLabel, 15, 15, ALL_CORNERS));

            parameter.setShadowClip(UIUtils.keepShadow(parameter, 15, UIUtils.ShadowSide.TOP));
        }
    }

    public void setIcon(BufferedImage image){
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

        titleLabel.setIcon(new ImageIcon(image.getScaledInstance((int)newWidth, (int)newHeight, Image.SCALE_SMOOTH)));
    }

    private WebStyledLabel createTitleLabel(){
        return new WebStyledLabel(){
            {
                ComponentUtils.makeMouseEventTransparent(this, SocialPanel.this);
                setOpaque(false);
                setBackground(new Color(0, 0, 0, 0));

                setMargin(4, 5, 2, 5);
                setPreferredHeight(35);
                setVerticalAlignment(CENTER);
                setMaximumRows(2);
                setForeground(defaultTitleColor);
                setFont(Resources.Fonts.ChronicaPro_Bold.deriveFont(10f));

                getScreen().addBlurSegment(parameter -> onBlurApply(parameter, this));
            }
            public void setText(String text) {
                super.setText(text);
                if(text == null)
                    return;

                float minFontSize = 10;
                float maxFontSize = 14;

                for(int i = (int)maxFontSize; i >= minFontSize; i--){
                    if(getFontMetrics(Resources.Fonts.ChronicaPro_Bold.deriveFont((float)i)).stringWidth(text) < 160) {
                        setFont(Resources.Fonts.ChronicaPro_Bold.deriveFont((float)i));
                        break;
                    }
                }
            }
        };
    }

    public abstract void onContentInit(WebPanel panel);

    public void onClick(){}
}
