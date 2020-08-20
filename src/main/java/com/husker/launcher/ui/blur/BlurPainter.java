package com.husker.launcher.ui.blur;

import com.husker.launcher.utils.ConsoleUtils;
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
    private Dimension lastLauncherSize;
    private BufferedImage lastBackground;

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

        if(!drawShadowOnTop && parameter.getShadowSize() > 0) {
            if (parameter.getShadowClip() != null) {
                if(parameter.getClip() != null){
                    Area clip = new Area(parameter.getShadowClip());
                    if(parameter.getClip() != null)
                        clip.intersect(new Area(parameter.getClip()));
                    g2d.setClip(clip);
                }else
                    g2d.setClip(parameter.getShadowClip());
            }
            g2d.drawImage(shadow, x - shadowSize, y - shadowSize, null);
            g2d.setClip(null);
        }
        if(parameter.getClip() != null)
            g2d.setClip(parameter.getClip());
        if(parameter.getBlurFactor() > 0)
            g2d.drawImage(blurred, x, y, null);
        if(parameter.isUseTexture())
            g2d.drawImage(texture, x, y, null);
        if(parameter.getAdditionColor() != null && parameter.getAdditionColor().getAlpha() > 0)
            g2d.drawImage(additionColor, x, y, null);
        if(drawShadowOnTop && parameter.getShadowSize() > 0) {
            if (parameter.getShadowClip() != null){
                if(parameter.getClip() != null){
                    Area clip = new Area(parameter.getShadowClip());
                    if(parameter.getClip() != null)
                        clip.intersect(new Area(parameter.getClip()));
                    g2d.setClip(clip);
                }else
                    g2d.setClip(parameter.getShadowClip());
            }
            g2d.drawImage(shadow, x - shadowSize, y - shadowSize, null);
            g2d.setClip(null);
        }
    }

    public void doCaching(){
        parameter = new BlurParameter(segment);
        segment.get(parameter);

        Shape shape = parameter.getShape();
        Shape translatedShape = shape;
        if(translatedShape != null && translatedShape.getBounds() != null)
            translatedShape = AffineTransform.getTranslateInstance(-translatedShape.getBounds().x, -translatedShape.getBounds().y).createTransformedShape(translatedShape);


        // Blur part
        if(lastParameters == null ||
                lastLauncherSize == null ||
                !areEquals(launcher.getBackgroundImage(), lastBackground) ||
                !areEquals(lastLauncherSize, launcher.getSize()) ||
                isOneNull(parameter.getShape(), lastParameters.getShape()) ||
                !equalShapes(parameter.getShape(), lastParameters.getShape()) ||
                !areEquals(parameter.getBlurFactor(), lastParameters.getBlurFactor())
        ){

            if(translatedShape != null && translatedShape.getBounds().width > 0 && translatedShape.getBounds().height > 0 && parameter.getBlurFactor() > 0) {
                lastLauncherSize = launcher.getSize();
                lastBackground = launcher.getBackgroundImage();

                ConsoleUtils.printDebug(getClass(), "Cache blur: " + parameter.getDebugName());

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
                isOneNull(parameter.getShape(), lastParameters.getShape()) ||
                !equalShapesSize(parameter.getShape(), lastParameters.getShape()) ||
                !areEquals(parameter.isUseTexture(), lastParameters.isUseTexture()) ||
                !areEquals(parameter.getTexture(), lastParameters.getTexture()) ||
                !areEquals(parameter.getTextureAlpha(), lastParameters.getTextureAlpha())
        ){
            if(translatedShape != null && translatedShape.getBounds().width > 0 && translatedShape.getBounds().height > 0) {
                ConsoleUtils.printDebug(getClass(), "Cache texture: " + parameter.getDebugName());
                Area toRender = new Area(new Rectangle(0, 0, launcher.getActualWidth() - 1, launcher.getActualHeight() - 1));
                toRender.intersect(new Area(translatedShape));

                texture = new BufferedImage(translatedShape.getBounds().width, translatedShape.getBounds().height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = texture.createGraphics();
                RenderUtils.enableAntialiasing(g2d);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, parameter.getTextureAlpha()));
                g2d.setPaint(new TexturePaint(parameter.getTexture(), toRender.getBounds()));
                g2d.fill(translatedShape);
            }else
                texture = null;
        }

        // Addition color part
        if(lastParameters == null ||
                isOneNull(parameter.getShape(), lastParameters.getShape()) ||
                !equalShapesSize(parameter.getShape(), lastParameters.getShape()) ||
                !areEquals(parameter.getAdditionColor(), lastParameters.getAdditionColor())
        ){
            if(translatedShape != null && translatedShape.getBounds().width > 0 && translatedShape.getBounds().height > 0) {
                ConsoleUtils.printDebug(getClass(), "Cache color: " + parameter.getDebugName());
                additionColor = new BufferedImage(translatedShape.getBounds().width, translatedShape.getBounds().height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = additionColor.createGraphics();
                RenderUtils.enableAntialiasing(g2d);
                g2d.setColor(parameter.getAdditionColor());
                g2d.fill(translatedShape);
            }else
                additionColor = null;
        }

        // Shadow part
        if(lastParameters == null ||
                isOneNull(parameter.getShape(), lastParameters.getShape()) ||
                !equalShapesSize(parameter.getShape(), lastParameters.getShape()) ||
                !areEquals(parameter.getShadowColor(), lastParameters.getShadowColor()) ||
                !areEquals(parameter.getShadowType(), lastParameters.getShadowType()) ||
                !areEquals(parameter.getShadowSize(), lastParameters.getShadowSize())
        ){
            if(translatedShape != null && translatedShape.getBounds().width > 0 && translatedShape.getBounds().height > 0) {
                ConsoleUtils.printDebug(getClass(), "Cache shadow: " + parameter.getDebugName());

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

    public boolean isOneNull(Object... objects){
        for(Object o : objects)
            if(o == null)
                return true;
        return false;
    }

    public boolean areEquals(Object object1, Object object2){
        if(object1 == object2)
            return true;
        if(object1 != null && object1.equals(object2))
            return true;
        return false;
    }

    private boolean equalShapes(Shape shape1, Shape shape2){
        if(shape1 == null || shape2 == null)
            return false;

        return new Area(shape1).equals(new Area(shape2));
    }

    private boolean equalShapesSize(Shape shape1, Shape shape2){
        if(shape1 == null || shape2 == null)
            return false;
        shape1 = AffineTransform.getTranslateInstance(-shape1.getBounds().x, -shape1.getBounds().y).createTransformedShape(shape1);
        shape2 = AffineTransform.getTranslateInstance(-shape2.getBounds().x, -shape2.getBounds().y).createTransformedShape(shape2);

        return new Area(shape1).equals(new Area(shape2));
    }

    public BlurSegment getBlurSegment(){
        return segment;
    }
}













