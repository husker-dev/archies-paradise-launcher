package com.husker.launcher.ui.shadow;

import java.awt.*;

public class ShadowParameter {

    public enum Direction{
        TOP,
        BOTTOM,
        LEFT,
        RIGHT,

        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }

    public enum RenderQueue{
        BEFORE_BLUR,
        AFTER_BLUR
    }

    private Color dark = new Color(0, 0, 0, 40);
    private Color light = new Color(0, 0, 0, 0);

    private Point position = new Point(0, 0);
    private Dimension size = new Dimension(100, 100);

    private RenderQueue renderQueue = RenderQueue.BEFORE_BLUR;

    private Direction direction = Direction.BOTTOM;

    public Color getDarkColor() {
        return dark;
    }

    public void setDarkColor(Color dark) {
        this.dark = dark;
    }

    public Color getLightColor() {
        return light;
    }

    public void setLightColor(Color light) {
        this.light = light;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public Dimension getSize() {
        return size;
    }

    public void setSize(Dimension size) {
        this.size = size;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public RenderQueue getRenderQueue() {
        return renderQueue;
    }

    public void setRenderQueue(RenderQueue renderQueue) {
        this.renderQueue = renderQueue;
    }
}
