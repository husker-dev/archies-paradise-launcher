package com.husker.launcher.ui.blur;

import com.husker.launcher.utils.ImageUtils;
import com.husker.launcher.LauncherWindow;
import com.husker.launcher.Resources;
import com.husker.launcher.blur.GaussianBlur;
import com.husker.launcher.utils.RenderUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;

import static com.husker.launcher.ui.blur.BlurParameter.ShadowType.*;

public class BlurPainter {

    private final BlurSegment segment;
    private final LauncherWindow launcher;
    private BlurParameter parameter;
    private BlurParameter lastParameters;

    // Cached:
    private BufferedImage blurred;
    private BufferedImage texture;
    private BufferedImage additionColor;
    private BufferedImage shadow;
    private boolean drawShadowOnTop = false;

    public BlurPainter(LauncherWindow launcher, BlurSegment segment){
        this.segment = segment;
        this.launcher = launcher;
    }

    public void paint(Graphics2D g2d){
        doCaching();

        if(parameter == null || parameter.getShape() == null || !parameter.isVisible())
            return;
        int x = parameter.getShape().getBounds().x;
        int y = parameter.getShape().getBounds().y;
        int shadowSize = parameter.getShadowSize();

        if(!drawShadowOnTop) {
            if (parameter.getShadowClip() != null)
                g2d.setClip(parameter.getShadowClip());
            g2d.drawImage(shadow, x - shadowSize, y - shadowSize, null);
            g2d.setClip(null);
        }
        g2d.drawImage(blurred, x, y, null);
        g2d.drawImage(texture, x, y, null);
        g2d.drawImage(additionColor, x, y, null);
        if(drawShadowOnTop) {
            if (parameter.getShadowClip() != null)
                g2d.setClip(parameter.getShadowClip());
            g2d.drawImage(shadow, x - shadowSize, y - shadowSize, null);
            g2d.setClip(null);
        }
    }

    public void doCaching(){
        parameter = new BlurParameter();
        segment.get(parameter);

        Shape shape = parameter.getShape();
        Shape translatedShape = shape;
        if(translatedShape != null && translatedShape.getBounds() != null)
            translatedShape = AffineTransform.getTranslateInstance(-translatedShape.getBounds().x, -translatedShape.getBounds().y).createTransformedShape(translatedShape);

        // Blur part
        if(lastParameters == null ||
            parameter.getShape() == null ||
            !parameter.getShape().equals(lastParameters.getShape()) ||
            parameter.getBlurFactor() != lastParameters.getBlurFactor()
        ){
            if(translatedShape != null && translatedShape.getBounds().width > 0 && translatedShape.getBounds().height > 0) {

                BufferedImage image = new BufferedImage(translatedShape.getBounds().width, translatedShape.getBounds().height + 50, BufferedImage.TYPE_INT_ARGB);

                // Draw background image
                Graphics2D image_g2d = image.createGraphics();
                image_g2d.translate(-shape.getBounds().x, -shape.getBounds().y);
                launcher.getBackgroundScalableImage().paint(image_g2d);

                // Increase image size to cut shape
                BufferedImage shapeSized = new BufferedImage(translatedShape.getBounds().width, translatedShape.getBounds().height, BufferedImage.TYPE_INT_ARGB);

                // Apply gaussian blur
                shapeSized.createGraphics().drawImage(GaussianBlur.fastBlur(image, parameter.getBlurFactor()), translatedShape.getBounds().x, translatedShape.getBounds().y, null);

                // Cropping by shape
                image = ImageUtils.getSubImage(shapeSized, translatedShape);

                // Render on 'blurred'
                if (image != null) {
                    blurred = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2d = blurred.createGraphics();
                    RenderUtils.enableAntialiasing(g2d);
                    g2d.setPaint(new TexturePaint(image, translatedShape.getBounds()));
                    g2d.fill(translatedShape);
                }
            }else
                blurred = null;
        }

        // Texture part
        if(lastParameters == null ||
                parameter.isUseTexture() != lastParameters.isUseTexture() ||
                !equalShapes(parameter.getShape(), lastParameters.getShape())
        ){
            if(translatedShape != null && translatedShape.getBounds().width > 0 && translatedShape.getBounds().height > 0) {
                Area toRender = new Area(new Rectangle(0, 0, launcher.getActualWidth() - 1, launcher.getActualHeight() - 1));
                toRender.intersect(new Area(parameter.getShape()));

                texture = new BufferedImage(translatedShape.getBounds().width, translatedShape.getBounds().height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = texture.createGraphics();
                RenderUtils.enableAntialiasing(g2d);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.015f));
                g2d.setPaint(new TexturePaint(Resources.blurTexture, toRender.getBounds2D()));
                g2d.fill(translatedShape);
            }else
                texture = null;
        }

        // Addition color part
        if(lastParameters == null ||
                !parameter.getAdditionColor().equals(lastParameters.getAdditionColor()) ||
                !equalShapes(parameter.getShape(), lastParameters.getShape())
        ){
            if(translatedShape != null && translatedShape.getBounds().width > 0 && translatedShape.getBounds().height > 0) {
                additionColor = new BufferedImage(translatedShape.getBounds().width, translatedShape.getBounds().height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = additionColor.createGraphics();
                RenderUtils.enableAntialiasing(g2d);
                g2d.setColor(parameter.getAdditionColor());
                g2d.fill(translatedShape);
            }else
                additionColor = null;
        }

        // Shadow part
        //System.out.println("---");
        if(lastParameters == null ||
                !equalShapes(parameter.getShape(), lastParameters.getShape()) ||
                !areEquals(parameter.getShadowColor(), lastParameters.getShadowColor()) ||
                !areEquals(parameter.getShadowType(), lastParameters.getShadowType()) ||
                !areEquals(parameter.getShadowSize(), lastParameters.getShadowSize())
        ){
            if(translatedShape != null && translatedShape.getBounds().width > 0 && translatedShape.getBounds().height > 0) {

                drawShadowOnTop = parameter.getShadowType() != OUTER;

                shadow = new BufferedImage(translatedShape.getBounds().width + parameter.getShadowSize() * 2, translatedShape.getBounds().height + parameter.getShadowSize() * 2, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = shadow.createGraphics();

                Shape shadowTranslated = AffineTransform.getTranslateInstance(parameter.getShadowSize(), parameter.getShadowSize()).createTransformedShape(translatedShape);

                if (parameter.getShadowType() == INNER)
                    RenderUtils.drawInnerShade(g2d, shadowTranslated, parameter.getShadowColor(), parameter.getShadowSize());
                if (parameter.getShadowType() == INNER_OUTER)
                    RenderUtils.drawShade(g2d, shadowTranslated, parameter.getShadowColor(), parameter.getShadowSize());
                if (parameter.getShadowType() == OUTER)
                    RenderUtils.drawOuterShade(g2d, shadowTranslated, parameter.getShadowColor(), parameter.getShadowSize());
            }else
                shadow = null;
        }

        lastParameters = parameter;
    }

    public boolean areEquals(Object object1, Object object2){
        if(object1 == object2)
            return true;
        if(object1 != null && object1.equals(object2))
            return true;
        return false;
    }

    private boolean equalShapes(Shape shape1, Shape shape2){
        if(shape1 == shape2)
            return true;
        if(shape1 == null || shape2 == null)
            return false;
        shape1 = AffineTransform.getTranslateInstance(-shape1.getBounds().x, -shape1.getBounds().y).createTransformedShape(shape1);
        shape2 = AffineTransform.getTranslateInstance(-shape2.getBounds().x, -shape2.getBounds().y).createTransformedShape(shape2);

        return new Area(shape1).equals(new Area(shape2));

    }
}













