package com.husker.launcher.ui.utils;

import com.husker.launcher.settings.LauncherSettings;
import com.husker.launcher.ui.shadow.ShadowParameter;

import java.awt.*;
import java.awt.geom.Area;

public class RenderUtils {

    public static void enableInterpolation(Graphics gr){
        enableAntialiasing((Graphics2D)gr);
    }

    public static void enableInterpolation(Graphics2D g2d){
        if(!LauncherSettings.isPotatoSettings())
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    }

    public static void enableAntialiasing(Graphics gr){
        enableAntialiasing((Graphics2D)gr);
    }

    public static void enableAntialiasing(Graphics2D g2d){
        if(!LauncherSettings.isPotatoSettings())
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    public static void disableAntialiasing(Graphics gr){
        disableAntialiasing((Graphics2D)gr);
    }

    public static void disableAntialiasing(Graphics2D g2d){
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    public static void drawShade(Graphics2D g2d, Shape shape, Color color, int width) {
        if(width == 0)
            return;
        Composite oldComposite = g2d.getComposite();
        Stroke oldStroke = g2d.getStroke();
        Paint oldPaint = g2d.getPaint();

        float stepAlpha = (float)color.getAlpha() / (float)width;

        width = width * 2;
        g2d.setColor(color);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)stepAlpha));

        for(int i = width; i >= 2; i-=2) {
            g2d.setStroke(new BasicStroke(i, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.draw(shape);
        }

        g2d.setStroke(oldStroke);
        g2d.setComposite(oldComposite);
        g2d.setPaint(oldPaint);
    }

    public static void drawInnerShade(Graphics2D g2d, Shape shape, Color color, int width) {
        Shape oldClip = g2d.getClip();

        g2d.setClip(shape);
        drawShade(g2d, shape, color, width);

        g2d.setClip(oldClip);
    }

    public static void drawOuterShade(Graphics2D g2d, Shape shape, Color color, int width) {
        Shape oldClip = g2d.getClip();

        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        Area area = new Area(oldClip != null ? oldClip : new Rectangle(0, 0, gd.getDisplayMode().getWidth(), gd.getDisplayMode().getHeight()));
        area.subtract(new Area(shape));


        g2d.setClip(area);
        drawShade(g2d, shape, color, width);

        g2d.setClip(oldClip);
    }

    public static void paintShadow(ShadowParameter parameter, Graphics2D g2d){

        Paint shadowPaint = null;

        if(parameter.getDirection() == ShadowParameter.Direction.BOTTOM){
            shadowPaint = new GradientPaint(parameter.getPosition().x,
                    parameter.getPosition().y,
                    parameter.getDarkColor(),
                    parameter.getPosition().x,
                    parameter.getPosition().y + parameter.getSize().height,
                    parameter.getLightColor());
        }
        if(parameter.getDirection() == ShadowParameter.Direction.LEFT){
            shadowPaint = new GradientPaint(parameter.getPosition().x,
                    parameter.getPosition().y,
                    parameter.getLightColor(),
                    parameter.getPosition().x + parameter.getSize().width,
                    parameter.getPosition().y,
                    parameter.getDarkColor());
        }
        if(parameter.getDirection() == ShadowParameter.Direction.TOP){
            shadowPaint = new GradientPaint(parameter.getPosition().x,
                    parameter.getPosition().y,
                    parameter.getLightColor(),
                    parameter.getPosition().x,
                    parameter.getPosition().y + parameter.getSize().height,
                    parameter.getDarkColor());
        }
        if(parameter.getDirection() == ShadowParameter.Direction.RIGHT){
            shadowPaint = new GradientPaint(parameter.getPosition().x,
                    parameter.getPosition().y,
                    parameter.getDarkColor(),
                    parameter.getPosition().x + parameter.getSize().width,
                    parameter.getPosition().y,
                    parameter.getLightColor());
        }

        if(parameter.getDirection() == ShadowParameter.Direction.BOTTOM_LEFT){
            int radius = Math.max(parameter.getSize().height, parameter.getSize().width);
            shadowPaint = new RadialGradientPaint (
                    parameter.getPosition().x + radius,
                    parameter.getPosition().y,
                    radius,
                    new float[]{ 0f, 1f },
                    new Color[]{parameter.getDarkColor(), parameter.getLightColor()});
        }


        if(parameter.getDirection() == ShadowParameter.Direction.TOP_LEFT){
            int radius = Math.max(parameter.getSize().height, parameter.getSize().width);
            shadowPaint = new RadialGradientPaint (
                    parameter.getPosition().x + radius,
                    parameter.getPosition().y + radius,
                    radius,
                    new float[]{ 0f, 1f },
                    new Color[]{parameter.getDarkColor(), parameter.getLightColor()});
        }

        if(parameter.getDirection() == ShadowParameter.Direction.TOP_RIGHT){
            int radius = Math.max(parameter.getSize().height, parameter.getSize().width);
            shadowPaint = new RadialGradientPaint (
                    parameter.getPosition().x,
                    parameter.getPosition().y + radius,
                    radius,
                    new float[]{ 0f, 1f },
                    new Color[]{parameter.getDarkColor(), parameter.getLightColor()});
        }

        if(parameter.getDirection() == ShadowParameter.Direction.BOTTOM_RIGHT){
            int radius = Math.max(parameter.getSize().height, parameter.getSize().width);
            shadowPaint = new RadialGradientPaint (
                    parameter.getPosition().x,
                    parameter.getPosition().y,
                    radius,
                    new float[]{ 0f, 1f },
                    new Color[]{parameter.getDarkColor(), parameter.getLightColor()});
        }

        g2d.setPaint(shadowPaint);
        g2d.fill(new Rectangle(parameter.getPosition().x, parameter.getPosition().y, parameter.getSize().width, parameter.getSize().height));
    }

}
