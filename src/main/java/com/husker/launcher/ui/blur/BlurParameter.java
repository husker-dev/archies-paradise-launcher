package com.husker.launcher.ui.blur;

import java.awt.*;

public class BlurParameter {

    public enum ShadowType{
        INNER,
        OUTER,
        INNER_OUTER
    }

    private Shape shape;
    private int blurFactor = 5;
    private boolean useTexture = true;
    private Color additionColor = new Color(0, 0, 0, 0);
    private Color shadowColor = new Color(0, 0, 0, 60);
    private ShadowType shadowType = ShadowType.OUTER;
    private int shadowSize = 0;
    private Shape shadowClip = null;

    private boolean visible = true;

    public Shape getShape() {
        return shape;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }

    public int getBlurFactor() {
        return blurFactor;
    }

    public void setBlurFactor(int blurFactor) {
        this.blurFactor = blurFactor;
    }

    public boolean isUseTexture() {
        return useTexture;
    }

    public void setUseTexture(boolean useTexture) {
        this.useTexture = useTexture;
    }

    public Color getAdditionColor() {
        return additionColor;
    }

    public void setAdditionColor(Color additionColor) {
        this.additionColor = additionColor;
    }

    public Color getShadowColor() {
        return shadowColor;
    }

    public void setShadowColor(Color shadowColor) {
        this.shadowColor = shadowColor;
    }

    public int getShadowSize() {
        return shadowSize;
    }

    public void setShadowSize(int shadowSize) {
        this.shadowSize = shadowSize;
    }

    public ShadowType getShadowType() {
        return shadowType;
    }

    public void setShadowType(ShadowType shadowType) {
        this.shadowType = shadowType;
    }

    public Shape getShadowClip() {
        return shadowClip;
    }

    public void setShadowClip(Shape clip) {
        this.shadowClip = clip;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
