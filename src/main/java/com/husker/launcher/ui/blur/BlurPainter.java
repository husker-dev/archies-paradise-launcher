package com.husker.launcher.ui.blur;

import com.husker.launcher.ui.utils.ImageUtils;
import com.husker.launcher.Launcher;
import com.husker.launcher.ui.blur.impl.GaussianBlur;
import com.husker.launcher.ui.utils.RenderUtils;
import com.husker.launcher.ui.utils.ShapeUtils;
import com.husker.launcher.utils.SystemUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.VolatileImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static com.husker.launcher.ui.blur.BlurParameter.ShadowType.*;

public class BlurPainter {

    private static BufferedImage bg;
    private static BufferedImage blurredBg;

    private final BlurSegment segment;
    private final Launcher launcher;
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

    public BlurPainter(Launcher launcher, BlurSegment segment, String name){
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

        if(bg != launcher.getBackgroundImage()){
            bg = launcher.getBackgroundImage();

            float newWidth = 800;
            float newHeight = newWidth / bg.getWidth() * bg.getHeight();

            blurredBg = ImageUtils.getScaledInstance(bg, (int)newWidth, (int)newHeight, BufferedImage.SCALE_FAST);
            blurredBg = getBlurFilter(25, 0).filter(blurredBg, null);
            blurredBg = getBlurFilter(0, 25).filter(blurredBg, null);
        }

        boolean changed = false;

        boolean blur_changed = false;
        boolean texture_changed = false;
        boolean color_changed = false;
        boolean shadow_changed = false;

        if(lastParameters != null && lastParameters.isVisible() != parameter.isVisible())
            changed = true;

        if(!changed && lastParameters != null && lastParameters.getShadowClip() != null && parameter.getShadowClip() != null && !equalShapes(lastParameters.getShadowClip(), parameter.getShadowClip()))
            changed = true;

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
                    BufferedImage image = new BufferedImage(translatedShape.getBounds().width, translatedShape.getBounds().height, BufferedImage.TYPE_INT_ARGB);

                    int radius = parameter.getBlurFactor();
                    Graphics2D image_g2d = image.createGraphics();
                    image_g2d.translate(-shape.getBounds().x, -shape.getBounds().y);

                    if (radius == 25) {
                        launcher.getBackgroundScalableImage().setImage(blurredBg);
                        launcher.getBackgroundScalableImage().paint(image_g2d);
                        launcher.getBackgroundScalableImage().setImage(bg);
                    }else {
                        double scale = 0.2;

                        // Draw background image
                        image_g2d.translate(-shape.getBounds().x, -shape.getBounds().y);
                        launcher.getBackgroundScalableImage().paint(image_g2d);

                        image = ImageUtils.toBufferedImage(image.getScaledInstance((int) (image.getWidth() * scale), (int) (image.getHeight() * scale), Image.SCALE_FAST));

                        // Apply gaussian blur
                        image = getBlurFilter(radius, 0).filter(image, null);
                        image = getBlurFilter(0, radius).filter(image, null);

                        image = ImageUtils.toBufferedImage(image.getScaledInstance(translatedShape.getBounds().width, translatedShape.getBounds().height, Image.SCALE_FAST));
                    }

                    // Cropping by shape
                    image = ImageUtils.getSubImage(image, translatedShape.getBounds());

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
                    Area toRender = new Area(new Rectangle(0, 0, launcher.getActualWidth(), launcher.getActualHeight()));
                    toRender.intersect(new Area(translatedShape));

                    int scaledW = (int)(translatedShape.getBounds().width * SystemUtils.getWindowScaleFactor());
                    int scaledH = (int)(translatedShape.getBounds().height * SystemUtils.getWindowScaleFactor());

                    texture = ImageUtils.createVolatileImage(launcher, scaledW, scaledH);
                    Graphics2D g2d = texture.createGraphics();
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)parameter.getTextureAlpha()));

                    g2d.setPaint(new TexturePaint(parameter.getTexture(), toRender.getBounds()));
                    RenderUtils.enableAntialiasing(g2d);
                    if(SystemUtils.getWindowScaleFactor(lastConfiguration) == 1)
                        RenderUtils.enableInterpolation(g2d);
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
                    g2d.setColor(parameter.getAdditionColor());

                    //RenderUtils.disableAntialiasing(g2d);
                    //g2d.fill(translatedShape);

                    RenderUtils.enableAntialiasing(g2d);
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

                    //ConsoleUtils.printDebug(getClass(), "Redraw: " + name + " in " + (System.currentTimeMillis() - start) / 1000d + " sec " + (changedText.isEmpty() ? "" : "(" + changedText + ")"));
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }else
                full = null;
            lastParameters = parameter;
        }


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


    public static ConvolveFilter getBlurFilter(int horizontalRadius, int verticalRadius) {
        int width = horizontalRadius * 2 + 1;
        int height = verticalRadius * 2 + 1;

        float weight = 1.0f / (width * height);
        float[] data = new float[width * height];

        Arrays.fill(data, weight);

        Kernel kernel = new Kernel(width, height, data);
        return new ConvolveFilter(kernel);
    }
}


