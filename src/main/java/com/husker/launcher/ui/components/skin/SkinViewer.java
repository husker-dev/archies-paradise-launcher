package com.husker.launcher.ui.components.skin;

import com.husker.launcher.utils.SkinUtils;
import com.husker.launcher.utils.SystemUtils;
import com.jogamp.opengl.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;

public class SkinViewer extends HiDPIGLJPanel {

    private static final Logger log = LogManager.getLogger(SkinViewer.class);

    private int lastMouseX = -1;
    private int lastMouseY = -1;

    private float rotateX = -23;
    private float rotateY = 23;

    private float camPointY = 16;
    private float camZoom = 50;

    private int textureId = 0;
    private int bufferTextureId = 0;
    private HashMap<String, InputStream[]> textures = new HashMap<>();

    private BufferedImage playerTexture, capeTexture, elytraTexture;
    private boolean isMale = true;
    private boolean animated = false;
    private boolean rotationEnabled = true;

    private final SkinViewerRenderer renderer = new SkinViewerRenderer(this);

    public SkinViewer(){
        this(null);
    }

    public SkinViewer(BufferedImage texture) {
        super(new GLCapabilities(GLProfile.getDefault()) {{
            setBackgroundOpaque(false);
            setSampleBuffers(true);
            setNumSamples(16);
        }});

        setSurfaceScale(new float[]{(float)SystemUtils.getWindowScaleFactor(), (float)SystemUtils.getWindowScaleFactor()});

        Threading.disableSingleThreading();
        setPlayerTexture(texture);

        setOpaque(false);
        addGLEventListener(renderer);
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                lastMouseX = mouseEvent.getX();
                lastMouseY = mouseEvent.getY();
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent mouseEvent) {
                if (rotationEnabled) {
                    rotateX += 0.3f * (mouseEvent.getX() - lastMouseX);
                    rotateY += 0.3f * (mouseEvent.getY() - lastMouseY);

                    if (rotateY > 90)
                        rotateY = 90;
                    if (rotateY < -90)
                        rotateY = -90;

                    lastMouseX = mouseEvent.getX();
                    lastMouseY = mouseEvent.getY();
                }
            }
        });

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(200, 200));
    }

    public void removeNotify() {

    }

    public int getWidth() {
        return (int)((float)super.getWidth() * SystemUtils.getWindowScaleFactor());
    }

    public int getHeight() {
        return (int)((float)super.getHeight() * SystemUtils.getWindowScaleFactor());
    }

    public Dimension getDimension() {
        return new Dimension(super.getSurfaceWidth(), super.getSurfaceHeight());
    }

    public void addNotify() {
        try{

            /*float scale = 1 / (float)SystemUtils.getWindowScaleFactor();

            Field hasPixelScale = GLJPanel.class.getDeclaredField("hasPixelScale");
            hasPixelScale.setAccessible(true);
            Array.set(hasPixelScale.get(this), 0, scale);
            Array.set(hasPixelScale.get(this), 1, scale);

            Field minPixelScale = GLJPanel.class.getDeclaredField("minPixelScale");
            minPixelScale.setAccessible(true);
            Array.set(minPixelScale.get(this), 0, scale);
            Array.set(minPixelScale.get(this), 1, scale);

            Field maxPixelScale = GLJPanel.class.getDeclaredField("maxPixelScale");
            maxPixelScale.setAccessible(true);
            Array.set(maxPixelScale.get(this), 0, scale);
            Array.set(maxPixelScale.get(this), 1, scale);

            Field reqPixelScale = GLJPanel.class.getDeclaredField("reqPixelScale");
            reqPixelScale.setAccessible(true);
            Array.set(reqPixelScale.get(this), 0, scale);
            Array.set(reqPixelScale.get(this), 1, scale);

             */

            super.addNotify();
        }catch (Exception ignored){}
    }

    public SkinViewerRenderer getRenderer(){
        return renderer;
    }

    public void setPlayerTexture(BufferedImage skin, BufferedImage cape, BufferedImage elytra){
        this.playerTexture = skin;
        this.capeTexture = cape;
        this.elytraTexture = elytra;
        isMale = SkinUtils.isMale(skin);
        updateTextureInputStreams();
    }

    public void setPlayerTexture(BufferedImage skin, BufferedImage cape){
        this.playerTexture = skin;
        this.capeTexture = cape;
        isMale = SkinUtils.isMale(skin);
        updateTextureInputStreams();
    }

    public void setPlayerTexture(BufferedImage texture){
        this.playerTexture = texture;
        isMale = SkinUtils.isMale(texture);
        updateTextureInputStreams();
    }

    public void setCapeTexture(BufferedImage texture){
        this.capeTexture = texture;
        updateTextureInputStreams();
    }

    public void setElytraTexture(BufferedImage texture){
        this.elytraTexture = texture;
        updateTextureInputStreams();
    }

    public void updateTextureInputStreams(){
        if(playerTexture == null && capeTexture == null && elytraTexture == null) {
            textureId ++;
            textures = new HashMap<>();
        }else {
            bufferTextureId++;
            final int threadTextureId = bufferTextureId;
            new Thread(() -> {
                HashMap<String, InputStream[]> local = new HashMap<>();

                if (playerTexture != null) {
                    local.put("leg_left", getLeg(0, 16));
                    local.put("leg_right", getLeg(16, 48));

                    local.put("hand_left", getHand(40, 16));
                    local.put("hand_right", getHand(32, 48));

                    local.put("head", getHead(0, 0));
                    local.put("body", getBody(16, 16));

                    local.put("leg_left_layer", getLeg(0, 32));
                    local.put("leg_right_layer", getLeg(0, 48));

                    local.put("hand_left_layer", getHand(48, 48));
                    local.put("hand_right_layer", getHand(40, 32));

                    local.put("head_layer", getHead(32, 0));
                    local.put("body_layer", getBody(16, 32));
                }
                if(capeTexture != null)
                    local.put("cape", getCape());
                /*
                if(elytra != null)
                    local.put("elytra", getCape());
                 */

                if (bufferTextureId == threadTextureId) {
                    textures = local;
                    textureId++;
                }
            }).start();
        }
    }

    public BufferedImage getPlayerTexture(){
        return playerTexture;
    }

    public BufferedImage getCapeTexture(){
        return capeTexture;
    }

    public BufferedImage getElytraTexture(){
        return elytraTexture;
    }

    public boolean isAnimated() {
        return animated;
    }

    public void setAnimated(boolean animated) {
        this.animated = animated;
    }

    public float getCamY() {
        return camPointY;
    }

    public void setCamY(float camPointY) {
        this.camPointY = camPointY;
    }

    public float getCamZoom() {
        return camZoom;
    }

    public void setCamZoom(float camZoom) {
        this.camZoom = camZoom;
    }

    public boolean isRotationEnabled() {
        return rotationEnabled;
    }

    public void setRotationEnabled(boolean rotationEnabled) {
        this.rotationEnabled = rotationEnabled;
    }

    public float getRotationY() {
        return rotateY;
    }

    public void setRotationY(float rotateY) {
        this.rotateY = rotateY;
    }

    public float getRotationX() {
        return rotateX;
    }

    public void setRotationX(float rotateX) {
        this.rotateX = rotateX;
    }

    public boolean isMaleSkin(){
        return isMale;
    }

    boolean isInLoadingQueue(){
        return true;
    }

    private InputStream[] getBody(int translateX, int translateY)  {
        try {
            return new InputStream[]{
                getSubTexture(translateX + 4, translateY + 4, 8, 12),
                getSubTexture(translateX, translateY + 4, 4, 12),
                getSubTexture(translateX + 16, translateY + 4, 8, 12),
                getSubTexture(translateX + 12, translateY + 4, 4, 12),
                getSubTexture(translateX + 4, translateY, 8, 4),
                getSubTexture(translateX + 12, translateY, 8, 4)
            };
        }catch (Exception ex){
            return null;
        }
    }

    private InputStream[] getHead(int translateX, int translateY)  {
        try {
            return new InputStream[]{
                getSubTexture(translateX + 8, translateY + 8, 8, 8),
                getSubTexture(translateX + 16, translateY + 8, 8, 8),
                getSubTexture(translateX + 24, translateY + 8, 8, 8),
                getSubTexture(translateX, translateY + 8, 8, 8),
                getSubTexture(translateX + 8, translateY, 8, 8),
                getSubTexture(translateX + 16, translateY, 8, 8)
            };
        }catch (Exception ex){
            return null;
        }
    }

    private InputStream[] getLeg(int translateX, int translateY)  {
        try {
            return new InputStream[]{
                getSubTexture(translateX + 4, translateY + 4, 4, 12),
                getSubTexture(translateX + 8, translateY + 4, 4, 12),
                getSubTexture(translateX + 12, translateY + 4, 4, 12),
                getSubTexture(translateX, translateY + 4, 4, 12),
                getSubTexture(translateX + 4, translateY, 4, 4),
                getSubTexture(translateX + 8, translateY, 4, 4)
            };
        }catch (Exception ex){
            return null;
        }
    }

    // Face
    // Right
    // Back
    // Left
    // Bottom
    // Top

    private InputStream[] getCape()  {
        try {
            return new InputStream[]{
                    getSubTexture(capeTexture, 12, 1, 10, 16),
                    getSubTexture(capeTexture, 11, 1, 1, 16),
                    getSubTexture(capeTexture, 1, 1, 10, 16),
                    getSubTexture(capeTexture, 0, 1, 1, 16),
                    getSubTexture(capeTexture, 1, 0, 10, 1),
                    getSubTexture(capeTexture, 11, 0, 10, 1)
            };
        }catch (Exception ex){
            return null;
        }
    }

    private InputStream[] getHand(int translateX, int translateY)  {
        try {
            if (isMaleSkin()) {
                return new InputStream[]{
                    getSubTexture(translateX + 4, translateY + 4, 4, 12),
                    getSubTexture(translateX + 8, translateY + 4, 4, 12),
                    getSubTexture(translateX + 12, translateY + 4, 4, 12),
                    getSubTexture(translateX, translateY + 4, 4, 12),
                    getSubTexture(translateX + 4, translateY, 4, 4),
                    getSubTexture(translateX + 8, translateY, 4, 4)
                };
            } else {
                return new InputStream[]{
                    getSubTexture(translateX + 4, translateY + 4, 3, 12),
                    getSubTexture(translateX + 7, translateY + 4, 4, 12),
                    getSubTexture(translateX + 11, translateY + 4, 3, 12),
                    getSubTexture(translateX, translateY + 4, 4, 12),
                    getSubTexture(translateX + 4, translateY, 3, 4),
                    getSubTexture(translateX + 7, translateY, 3, 4)
                };
            }
        }catch (Exception ex){
            return null;
        }
    }

    public int getTexturesId(){
        return textureId;
    }

    public InputStream[] getTexturesInputStream(String name){
        if(playerTexture == null && capeTexture == null && elytraTexture == null)
            return null;
        return textures.get(name);
    }

    public InputStream getSubTexture(int x, int y, int width, int height) {
        return getSubTexture(playerTexture, x, y, width, height);
    }

    public InputStream getSubTexture(BufferedImage texture, int x, int y, int width, int height) {
        return getInputStream(texture.getSubimage(x, y, width, height));
    }

    public InputStream getInputStream(BufferedImage image){
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(image, "png", os);
            return new ByteArrayInputStream(os.toByteArray());
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
}
