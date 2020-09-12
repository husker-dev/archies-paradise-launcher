package com.husker.launcher.components.skin;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class SkinViewer extends GLJPanel {

    private static int ids = 0;
    private static final ArrayList<Integer> loadingQueue = new ArrayList<>();
    private final int id;

    private int lastMouseX = -1;
    private int lastMouseY = -1;

    private float rotateX = -23;
    private float rotateY = 23;

    private float camPointY = 16;
    private float camZoom = 50;

    private BufferedImage playerTexture;
    private boolean animated = false;
    private boolean rotationEnabled = true;

    private boolean autoResourceUpdate = true;

    private final SkinViewerRenderer renderer = new SkinViewerRenderer(this);

    public SkinViewer(){
        this(null);
    }

    public SkinViewer(BufferedImage texture){
        super(new GLCapabilities(GLProfile.getDefault()) {{
            setBackgroundOpaque(false);
            setSampleBuffers(true);
            setNumSamples(16);
        }});

        this.id = ids++;
        this.playerTexture = texture;

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
                if(rotationEnabled) {
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

    public SkinViewerRenderer getRenderer(){
        return renderer;
    }

    public void setPlayerTexture(BufferedImage texture){
        this.playerTexture = texture;

        askForLoadingQueue();
    }

    public BufferedImage getPlayerTexture(){
        return playerTexture;
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

    public boolean isAutoResourceUpdateEnabled() {
        return autoResourceUpdate;
    }

    public void setAutoResourceUpdate(boolean autoResourceUpdate) {
        this.autoResourceUpdate = autoResourceUpdate;
    }

    public void updateResource(){
        getRenderer().updateTexture();
    }

    public void waitForResourceLoad(){
        getRenderer().waitForResourceLoad();
    }

    void askForLoadingQueue(){
        if(!loadingQueue.contains(id))
            loadingQueue.add(id);
    }

    void leaveLoadingQueue(){
        if(loadingQueue.contains(id))
            loadingQueue.remove((Object)id);
    }

    boolean isInLoadingQueue(){
        return loadingQueue.size() > 0 && loadingQueue.get(0) == id;
    }
}
