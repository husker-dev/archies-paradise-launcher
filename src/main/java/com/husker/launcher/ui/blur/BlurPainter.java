package com.husker.launcher.ui.blur;

import com.husker.launcher.utils.ConsoleUtils;
import com.husker.launcher.utils.ImageUtils;
import com.husker.launcher.LauncherWindow;
import com.husker.launcher.Resources;
import com.husker.launcher.blur.GaussianBlur;
import com.husker.launcher.utils.RenderUtils;
import com.husker.launcher.utils.ShapeUtils;

import javax.sound.sampled.Clip;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.util.ArrayList;

import static com.husker.launcher.ui.blur.BlurParameter.ShadowType.*;

public class BlurPainter {

    private final BlurSegment segment;
    private final LauncherWindow launcher;
    private final String name;
    private BlurParameter parameter;

    private BlurParameter lastParameters;
    private Dimension lastLauncherSize;
    private BufferedImage lastBackground;
    private GraphicsConfiguration lastConfiguration;

    // Cached:
    private VolatileImage blurred;
    private VolatileImage texture;
    private VolatileImage additionColor;
    private VolatileImage shadow;

    private VolatileImage full;
    private boolean drawShadowOnTop = false;

    public BlurPainter(LauncherWindow launcher, BlurSegment segment, String name){
        this.segment = segment;
        this.launcher = launcher;
        this.name = name;
    }

    public void paint(Graphics2D g2d){
        doCaching();

        if(parameter == null || parameter.getShape() == null || !parameter.isVisible())
            return;

        int x = parameter.getShape().getBounds().x;
        int y = parameter.getShape().getBounds().y;
        int shadowSize = parameter.getShadowSize();

        g2d.drawImage(full, x - shadowSize, y - shadowSize, null);
    }

    public void doCaching(){
        parameter = new BlurParameter(segment);
        segment.get(parameter);

        boolean changed = false;

        boolean blur_changed = false;
        boolean texture_changed = false;
        boolean color_changed = false;
        boolean shadow_changed = false;

        if(lastParameters != null && lastParameters.isVisible() != parameter.isVisible())
            changed = true;

        if(!changed && lastParameters != null && lastParameters.getShadowClip() != null && parameter.getShadowClip() != null && !equalShapes(lastParameters.getShadowClip(), parameter.getShadowClip()))
            changed = true;

        long start = System.currentTimeMillis();

        if(parameter.isVisible()) {

            Shape shape = parameter.getShape();
            Shape translatedShape = shape;
            if(translatedShape != null && translatedShape.getBounds() != null)
                translatedShape = AffineTransform.getTranslateInstance(-translatedShape.getBounds().x, -translatedShape.getBounds().y).createTransformedShape(translatedShape);

            boolean graphicsConfigurationChanged = lastConfiguration == null || !lastConfiguration.equals(launcher.getGraphicsConfiguration());
            lastConfiguration = launcher.getGraphicsConfiguration();

            // Blur part
            if (lastParameters == null ||
                    lastLauncherSize == null ||
                    !areEquals(launcher.getBackgroundImage(), lastBackground) ||
                    !areEquals(lastLauncherSize, launcher.getSize()) ||
                    isOneNull(parameter.getShape(), lastParameters.getShape()) ||
                    !equalShapes(parameter.getShape(), lastParameters.getShape()) ||
                    !areEquals(parameter.getBlurFactor(), lastParameters.getBlurFactor()) ||
                    (blurred != null && blurred.contentsLost()) ||
                    graphicsConfigurationChanged
            ) {
                blur_changed = true;
                changed = true;

                if(!areEquals(lastLauncherSize, launcher.getSize()))
                    lastLauncherSize = launcher.getSize();
                if(!areEquals(launcher.getBackgroundImage(), lastBackground))
                    lastBackground = launcher.getBackgroundImage();

                if (translatedShape != null && translatedShape.getBounds().width > 0 && translatedShape.getBounds().height > 0 && parameter.getBlurFactor() > 0) {

                    BufferedImage image = new BufferedImage(translatedShape.getBounds().width, translatedShape.getBounds().height + (parameter.getBlurFactor() * 2), BufferedImage.TYPE_INT_ARGB);

                    // Draw background image
                    Graphics2D image_g2d = image.createGraphics();
                    image_g2d.translate(-shape.getBounds().x, -shape.getBounds().y);
                    launcher.getBackgroundScalableImage().paint(image_g2d);

                    // Increase image size to cut shape
                    BufferedImage shapeSized = new BufferedImage(translatedShape.getBounds().width, translatedShape.getBounds().height, BufferedImage.TYPE_INT_ARGB);

                    // Apply gaussian blur
                    shapeSized.createGraphics().drawImage(GaussianBlur.fastBlur(image, parameter.getBlurFactor()), translatedShape.getBounds().x, translatedShape.getBounds().y, null);

                    // Cropping by shape
                    image = ImageUtils.getSubImage(shapeSized, translatedShape.getBounds());

                    // Render on 'blurred'
                    if (image != null) {
                        blurred = ImageUtils.createVolatileImage(launcher, image.getWidth(), image.getHeight());
                        Graphics2D g2d = blurred.createGraphics();
                        RenderUtils.enableAntialiasing(g2d);

                        g2d.setPaint(new TexturePaint(image, translatedShape.getBounds()));
                        g2d.fill(translatedShape);
                        g2d.dispose();
                    }

                    image_g2d.dispose();
                } else
                    blurred = null;
            }

            // Texture part
            if (lastParameters == null ||
                    isOneNull(parameter.getShape(), lastParameters.getShape()) ||
                    !equalShapesSize(parameter.getShape(), lastParameters.getShape()) ||
                    !areEquals(parameter.isUseTexture(), lastParameters.isUseTexture()) ||
                    !areEquals(parameter.getTexture(), lastParameters.getTexture()) ||
                    !areEquals(parameter.getTextureAlpha(), lastParameters.getTextureAlpha()) ||
                    (texture != null && texture.contentsLost()) ||
                    graphicsConfigurationChanged
            ) {
                changed = true;
                texture_changed = true;

                if (translatedShape != null && translatedShape.getBounds().width > 0 && translatedShape.getBounds().height > 0) {
                    Area toRender = new Area(new Rectangle(0, 0, launcher.getActualWidth() - 1, launcher.getActualHeight() - 1));
                    toRender.intersect(new Area(translatedShape));

                    texture = ImageUtils.createVolatileImage(launcher, translatedShape.getBounds().width, translatedShape.getBounds().height);
                    Graphics2D g2d = texture.createGraphics();
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, parameter.getTextureAlpha()));
                    g2d.setPaint(new TexturePaint(parameter.getTexture(), toRender.getBounds()));
                    RenderUtils.enableAntialiasing(g2d);

                    g2d.fill(translatedShape);
                    g2d.dispose();
                } else
                    texture = null;
            }

            // Addition color part
            if (lastParameters == null ||
                    isOneNull(parameter.getShape(), lastParameters.getShape()) ||
                    !equalShapesSize(parameter.getShape(), lastParameters.getShape()) ||
                    !areEquals(parameter.getAdditionColor(), lastParameters.getAdditionColor()) ||
                    (additionColor != null && additionColor.contentsLost()) ||
                    graphicsConfigurationChanged
            ) {
                changed = true;
                color_changed = true;

                if (translatedShape != null && translatedShape.getBounds().width > 0 && translatedShape.getBounds().height > 0) {
                    additionColor = ImageUtils.createVolatileImage(launcher, translatedShape.getBounds().width, translatedShape.getBounds().height);
                    Graphics2D g2d = additionColor.createGraphics();
                    RenderUtils.enableAntialiasing(g2d);
                    g2d.setColor(parameter.getAdditionColor());
                    g2d.fill(translatedShape);
                    g2d.dispose();
                } else
                    additionColor = null;
            }

            // Shadow part
            if (lastParameters == null ||
                    isOneNull(parameter.getShape(), lastParameters.getShape()) ||
                    !equalShapesSize(parameter.getShape(), lastParameters.getShape()) ||
                    !areEquals(parameter.getShadowColor(), lastParameters.getShadowColor()) ||
                    !areEquals(parameter.getShadowType(), lastParameters.getShadowType()) ||
                    !areEquals(parameter.getShadowSize(), lastParameters.getShadowSize()) ||
                    (shadow != null && shadow.contentsLost()) ||
                    graphicsConfigurationChanged
            ) {
                changed = true;
                shadow_changed = true;

                if (translatedShape != null && translatedShape.getBounds().width > 0 && translatedShape.getBounds().height > 0) {
                    drawShadowOnTop = parameter.getShadowType() != OUTER;

                    shadow = ImageUtils.createVolatileImage(launcher, translatedShape.getBounds().width + parameter.getShadowSize() * 2, translatedShape.getBounds().height + parameter.getShadowSize() * 2);
                    Graphics2D g2d = shadow.createGraphics();

                    Shape shadowTranslated = AffineTransform.getTranslateInstance(parameter.getShadowSize(), parameter.getShadowSize()).createTransformedShape(translatedShape);

                    if (parameter.getShadowType() == INNER)
                        RenderUtils.drawInnerShade(g2d, shadowTranslated, parameter.getShadowColor(), parameter.getShadowSize());
                    if (parameter.getShadowType() == INNER_OUTER)
                        RenderUtils.drawShade(g2d, shadowTranslated, parameter.getShadowColor(), parameter.getShadowSize());
                    if (parameter.getShadowType() == OUTER)
                        RenderUtils.drawOuterShade(g2d, shadowTranslated, parameter.getShadowColor(), parameter.getShadowSize());
                } else
                    shadow = null;
            }
        }

        if(changed){
            if(parameter != null && parameter.getShape() != null && parameter.isVisible() && parameter.getShape().getBounds().width > 0 && parameter.getShape().getBounds().height > 0) {
                try {
                    int shadowSize = parameter.getShadowSize();

                    int x = shadowSize;
                    int y = shadowSize;

                    Shape shadowClip = ShapeUtils.translateShape(parameter.getShadowClip(), -parameter.getShape().getBounds().x + x, -parameter.getShape().getBounds().y + y);
                    Shape fullClip = ShapeUtils.translateShape(parameter.getClip(), -parameter.getShape().getBounds().x + x, -parameter.getShape().getBounds().y + y);

                    full = ImageUtils.createVolatileImage(launcher, parameter.getShape().getBounds().width + shadowSize * 2, parameter.getShape().getBounds().height + shadowSize * 2);
                    Graphics2D g2d = full.createGraphics();

                    do{
                        if (!drawShadowOnTop && shadowSize > 0) {
                            if (shadowClip != null) {
                                if (fullClip != null) {
                                    Area clip = new Area(shadowClip);
                                    clip.intersect(new Area(fullClip));
                                    g2d.setClip(clip);
                                } else
                                    g2d.setClip(shadowClip);
                            }
                            g2d.drawImage(shadow, 0, 0, null);
                            g2d.setClip(null);
                        }
                        if (parameter.getClip() != null)
                            g2d.setClip(fullClip);
                        if (parameter.getBlurFactor() > 0)
                            g2d.drawImage(blurred, x, y, null);
                        if (parameter.isUseTexture())
                            g2d.drawImage(texture, x, y, null);
                        if (parameter.getAdditionColor() != null && parameter.getAdditionColor().getAlpha() > 0)
                            g2d.drawImage(additionColor, x, y, null);
                        if (drawShadowOnTop && shadowSize > 0) {
                            if (shadowClip != null) {
                                if (fullClip != null) {
                                    Area clip = new Area(shadowClip);
                                    clip.intersect(new Area(fullClip));
                                    g2d.setClip(clip);
                                } else
                                    g2d.setClip(shadowClip);
                            }
                            g2d.drawImage(shadow, 0, 0, null);
                            g2d.setClip(null);
                        }
                        g2d.dispose();
                    }while (full.contentsLost());


                    ArrayList<String> changedElements = new ArrayList<>();
                    if (blur_changed)
                        changedElements.add("blur");
                    if (texture_changed)
                        changedElements.add("texture");
                    if (color_changed)
                        changedElements.add("color");
                    if (shadow_changed)
                        changedElements.add("shadow");
                    String changedText = String.join(",", changedElements.toArray(new String[0]));

                    ConsoleUtils.printDebug(getClass(), "Redraw: " + name + " in " + (System.currentTimeMillis() - start) / 1000d + " sec " + (changedText.isEmpty() ? "" : "(" + changedText + ")"));
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }else
                full = null;
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













