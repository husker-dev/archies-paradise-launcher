package com.husker.launcher.components.skin;

import com.alee.managers.animation.easing.Cubic;
import com.husker.launcher.utils.ConsoleUtils;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2ES1.GL_ALPHA_TEST;
import static com.jogamp.opengl.GL2GL3.GL_TEXTURE_LOD_BIAS;

public class SkinViewerRenderer implements GLEventListener {

    private static final float layerIndent = 0.5f;

    private final GLU glu = new GLU();

    private boolean disposed = false;
    private BufferedImage lastTexture;

    private int[] rightLeg = new int[6];
    private int[] leftLeg = new int[6];
    private int[] body = new int[6];
    private int[] leftHand = new int[6];
    private int[] rightHand = new int[6];
    private int[] head = new int[6];

    private int[] rightLeg_layer = new int[6];
    private int[] leftLeg_layer = new int[6];
    private int[] body_layer = new int[6];
    private int[] leftHand_layer = new int[6];
    private int[] rightHand_layer = new int[6];
    private int[] head_layer = new int[6];

    private static final float maxAngle = 40;
    private float angle = 0;
    private boolean animType = true;

    private float currentRotateX = 0;
    private float currentRotateY = 0;
    private float currentZoom = 0;
    private float currentCamY = 0;

    private long lastTime = -1;

    private float alpha = 0;
    private boolean textureLoaded = false;

    private final SkinViewer viewer;

    public SkinViewerRenderer(SkinViewer viewer){
        this.viewer = viewer;
    }

    public void display(GLAutoDrawable drawable) {
        final GL2 gl = drawable.getGL().getGL2();

        float delta = (System.currentTimeMillis() - lastTime) / 100f;
        lastTime = System.currentTimeMillis();

        updateResources(gl);
        updateAnimation(delta);

        // Smooth value changing
        currentRotateX += delta * (viewer.getRotationX() - currentRotateX);
        currentRotateY += delta * (viewer.getRotationY() - currentRotateY);
        currentZoom += delta * (viewer.getCamZoom() - currentZoom);
        currentCamY += delta * (viewer.getCamY() - currentCamY);

        gl.glClearColor(0f, 0f, 0f, 0.003f);
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();

        checkForTextureChanges();

        if(viewer.getPlayerTexture() != null && textureLoaded && lastTexture != null && !disposed && tasks.size() == 0) {
            gl.glTranslatef(0f, 0, -currentZoom);
            gl.glRotatef(currentRotateY, 1, 0, 0);
            gl.glRotatef(currentRotateX, 0, 1, 0);
            gl.glTranslatef(0, -currentCamY, -0);

            if (alpha < 1)
                alpha += 0.25 * delta;
            if (alpha > 1)
                alpha = 1;
            gl.glColor4f(1, 1, 1, alpha);

            drawLeg(gl, new Point3D(0, 0, -2), angle, rightLeg);
            drawLeg(gl, new Point3D(-4, 0, -2), -angle, leftLeg);
            drawHand(gl, new Point3D(-8, 12, -2), angle, leftHand);
            drawHand(gl, new Point3D(4, 12, -2), -angle, rightHand);
            drawCuboid(gl, new Point3D(-4, 12, -2), 8, 12, 4, body);
            drawCuboid(gl, new Point3D(-4, 24, -4), 8, 8, 8, head);

            drawFatLeg(gl, new Point3D(0, 0, -2), angle, rightLeg_layer);
            drawFatLeg(gl, new Point3D(-4, 0, -2), -angle, leftLeg_layer);
            drawFatHand(gl, new Point3D(-8, 12, -2), angle, leftHand_layer);
            drawFatHand(gl, new Point3D(4, 12, -2), -angle, rightHand_layer);
            drawCuboid(gl, new Point3D(-4 - layerIndent, 12 - layerIndent, -2 - layerIndent), 8 + layerIndent * 2f, 12 + layerIndent * 2f, 4 + layerIndent * 2f, body_layer);
            drawCuboid(gl, new Point3D(-4 - layerIndent, 24 - layerIndent, -4 - layerIndent), 8 + layerIndent * 2f, 8 + layerIndent * 2f, 8 + layerIndent * 2f, head_layer);
        }

        gl.glFlush();
    }

    public void drawFatLeg(GL2 gl, Point3D point, float rotation, int... textures){
        gl.glPushMatrix();
        gl.glTranslated(point.x + 2, point.y + 12, point.z + 2);
        gl.glRotated(rotation,1,0,0);
        gl.glTranslated(-(point.x + 2), -(point.y + 12), -(point.z + 2));

        point.x -= layerIndent;
        point.y -= layerIndent;
        point.z -= layerIndent;
        drawCuboid(gl, point, 4 + layerIndent * 2f, 12 + layerIndent * 2f, 4 + layerIndent * 2f, textures);

        gl.glPopMatrix();
    }

    public void drawFatHand(GL2 gl, Point3D point, float rotation, int... textures){
        gl.glPushMatrix();
        gl.glTranslated(point.x + 2, point.y + 10, point.z + 2);
        gl.glRotated(rotation,1,0,0);
        gl.glTranslated(-(point.x + 2), -(point.y + 10), -(point.z + 2));

        point.x -= layerIndent;
        point.y -= layerIndent;
        point.z -= layerIndent;
        drawCuboid(gl, point, 4 + layerIndent * 2f, 12 + layerIndent * 2f, 4 + layerIndent * 2f, textures);

        gl.glPopMatrix();
    }

    public void drawHand(GL2 gl, Point3D point, float rotation, int... textures){
        gl.glPushMatrix();
        gl.glTranslated(point.x + 2, point.y + 10, point.z + 2);
        gl.glRotated(rotation,1,0,0);
        gl.glTranslated(-(point.x + 2), -(point.y + 10), -(point.z + 2));

        drawCuboid(gl, point, 4, 12, 4, textures);

        gl.glPopMatrix();
    }

    public void drawLeg(GL2 gl, Point3D point, float rotation, int... textures){
        gl.glPushMatrix();
        gl.glTranslated(point.x + 2, point.y + 12, point.z + 2);
        gl.glRotated(rotation,1,0,0);
        gl.glTranslated(-(point.x + 2), -(point.y + 12), -(point.z + 2));

        drawCuboid(gl, point, 4, 12, 4, textures);

        gl.glPopMatrix();
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        final GL2 gl = drawable.getGL().getGL2();
        if(height == 0)
            height = 1;

        final float h = (float) width / (float) height;
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);

        gl.glLoadIdentity();

        glu.gluPerspective(45.0f, h, 1, 100);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    public void init(GLAutoDrawable drawable) {
        final GL2 gl = drawable.getGL().getGL2();

        currentRotateX = viewer.getRotationX();
        currentRotateY = viewer.getRotationY();
        currentZoom = viewer.getCamZoom();
        currentCamY = viewer.getCamY();

        lastTime = System.currentTimeMillis();

        gl.glShadeModel(GL2.GL_SMOOTH);
        gl.glClearColor(0f, 0f, 0f, 0.01f);
        gl.glClearDepth(1.0f);
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glDepthFunc(GL2.GL_LEQUAL);
        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);

        gl.glEnable(GL_MULTISAMPLE);
        gl.glEnable(GL_BLEND);
        gl.glEnable(GL_ALPHA_TEST);
        gl.glAlphaFunc(GL2.GL_GREATER, 0.0f);

        gl.glEnable(GL_TEXTURE_2D);
    }

    public void dispose(GLAutoDrawable drawable) {
        ConsoleUtils.printDebug(SkinViewer.class, "Dispose");
        disposed = true;

        viewer.leaveLoadingQueue();
    }

    public void updateAnimation(float delta){
        if(viewer.isAnimated()) {
            double speed = 1 - new Cubic.In().calculate(0, 1, Math.abs(angle), maxAngle);
            double value = 0.5 + delta * speed * 10;

            if (animType) {
                angle += value;
                if (angle > maxAngle) {
                    animType = false;
                    angle = maxAngle;
                }
            } else {
                angle -= value;
                if (angle < -maxAngle) {
                    animType = true;
                    angle = -maxAngle;
                }
            }
        }else{
            if(angle != 0){
                float speed = 1f - (Math.abs(angle) / maxAngle);
                double value = 1.0 + delta * speed * 20;

                if(angle > 0) {
                    animType = false;
                    angle -= value;
                    if(angle < 0)
                        angle = 0;
                }
                if(angle < 0){
                    animType = true;
                    angle += value;
                    if(angle > 0)
                        angle = 0;
                }
            }
        }
    }

    public void checkForTextureChanges(){
        if(viewer.getPlayerTexture() != null && viewer.isAutoResourceUpdateEnabled() && (lastTexture != viewer.getPlayerTexture() || disposed))
            updateTexture();
    }

    public boolean isTextureLoaded(){
        return textureLoaded;
    }

    public void waitForResourceLoad(){
        while(tasks.size() > 0 || lastTexture != viewer.getPlayerTexture()){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateTexture(){
        alpha = 0;
        textureLoaded = false;
        lastTexture = viewer.getPlayerTexture();
        disposed = false;

        viewer.askForLoadingQueue();

        tasks.put("1", gl -> {
            rightLeg = getHandOrLeg(gl, 16, 48);
            leftLeg = getHandOrLeg(gl, 0, 16);
        });

        tasks.put("2", gl -> {
            leftHand = getHandOrLeg(gl, 40, 16);
            rightHand = getHandOrLeg(gl, 32, 48);
        });

        tasks.put("3", gl -> {
            leftHand = getHandOrLeg(gl, 40, 16);
            rightHand = getHandOrLeg(gl, 32, 48);
        });

        tasks.put("4", gl -> {
            body = getBody(gl, 16, 16);
            head = getHead(gl, 0, 0);
        });

        tasks.put("5", gl -> {
            rightLeg_layer = getHandOrLeg(gl, 0, 48);
            leftLeg_layer = getHandOrLeg(gl, 0, 32);
        });

        tasks.put("6", gl -> {
            leftHand_layer = getHandOrLeg(gl, 48, 48);
            rightHand_layer = getHandOrLeg(gl, 40, 32);
        });
        tasks.put("7", gl -> {
            body_layer = getBody(gl, 16, 32);
            head_layer = getHead(gl, 32, 0);

            textureLoaded = true;
        });
    }

    public int[] getHandOrLeg(GL2 gl, int translateX, int translateY){
        int[] out = new int[6];
        out[0] = getSubTexture(gl, translateX + 4, translateY + 4, 4, 12);
        out[1] = getSubTexture(gl, translateX + 8, translateY + 4, 4, 12);
        out[2] = getSubTexture(gl, translateX + 12, translateY + 4, 4, 12);
        out[3] = getSubTexture(gl, translateX, translateY + 4, 4, 12);
        out[4] = getSubTexture(gl, translateX + 4, translateY, 4, 4);
        out[5] = getSubTexture(gl, translateX + 8, translateY, 4, 4);
        return out;
    }

    public int[] getBody(GL2 gl, int translateX, int translateY){
        int[] out = new int[6];
        out[0] = getSubTexture(gl, translateX + 4, translateY + 4, 8, 12);
        out[1] = getSubTexture(gl, translateX, translateY + 4, 4, 12);
        out[2] = getSubTexture(gl, translateX + 16, translateY + 4, 8, 12);
        out[3] = getSubTexture(gl, translateX + 12, translateY + 4, 4, 12);
        out[4] = getSubTexture(gl, translateX + 4, translateY, 8, 4);
        out[5] = getSubTexture(gl, translateX + 12, translateY, 8, 4);
        return out;
    }

    public int[] getHead(GL2 gl, int translateX, int translateY){
        int[] out = new int[6];
        out[0] = getSubTexture(gl, translateX + 8, translateY + 8, 8, 8);
        out[1] = getSubTexture(gl, translateX + 16, translateY + 8, 8, 8);
        out[2] = getSubTexture(gl, translateX + 24, translateY + 8, 8, 8);
        out[3] = getSubTexture(gl, translateX + 0, translateY + 8, 8, 8);
        out[4] = getSubTexture(gl, translateX + 8, translateY + 0, 8, 8);
        out[5] = getSubTexture(gl, translateX + 16, translateY + 0, 8, 8);
        return out;
    }

    public int getSubTexture(GL2 gl, int x, int y, int width, int height){
        Texture texture = null;
        try {
            if(viewer.getPlayerTexture() != null) {
                texture = TextureIO.newTexture(getInputStream(viewer.getPlayerTexture().getSubimage(x, y, width, height)), false, "png");
                gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
                gl.glGenerateMipmap(GL_TEXTURE_2D);
                gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST);
                gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_LOD_BIAS, -1);

                FloatBuffer buffer = FloatBuffer.allocate(1);
                gl.glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, buffer);
                gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, buffer.get());
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        if(texture != null)
            return texture.getTextureObject(gl);
        else
            return -1;
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

    public void drawCuboid(GL2 gl, Point3D location, float sizeX, float sizeY, float sizeZ, int... textures){
        Point3D p1 = location;  // Face left bottom
        Point3D p2 = new Point3D(location.getX() + sizeX, location.getY(), location.getZ());  // Face right bottom
        Point3D p3 = new Point3D(location.getX() + sizeX, location.getY() + sizeY, location.getZ());  // Face right top
        Point3D p4 = new Point3D(location.getX(), location.getY() + sizeY, location.getZ());  // Face left top

        Point3D p5 = new Point3D(location.getX(), location.getY(), location.getZ() + sizeZ);  // Not-Face left bottom
        Point3D p6 = new Point3D(location.getX() + sizeX, location.getY(), location.getZ() + sizeZ);  // Not-Face right bottom
        Point3D p7 = new Point3D(location.getX() + sizeX, location.getY() + sizeY, location.getZ() + sizeZ);  // Not-Face right top
        Point3D p8 = new Point3D(location.getX(), location.getY() + sizeY, location.getZ() + sizeZ);  // Not-Face left top

        drawTexture(gl, textures[2], p1, p2, p3, p4);
        drawTexture(gl, textures[1], p2, p6, p7, p3);
        drawTexture(gl, textures[0], p6, p5, p8, p7);
        drawTexture(gl, textures[3], p5, p1, p4, p8);
        drawTexture(gl, textures[4], p7, p8, p4, p3);
        drawTexture(gl, textures[5], p6, p5, p1, p2);
    }

    public void drawTexture(GL2 gl, int texture, Point3D p1, Point3D p2, Point3D p3, Point3D p4){
        gl.glBindTexture(GL_TEXTURE_2D, texture);
        gl.glBegin(GL2.GL_QUADS);

        gl.glTexCoord2f(1.0f, 0.0f);

        gl.glVertex3f(p1.getX(), p1.getY(), p1.getZ());
        gl.glTexCoord2f(0.0f, 0.0f);

        gl.glVertex3f(p2.getX(), p2.getY(), p2.getZ());
        gl.glTexCoord2f(0.0f, 1.0f);

        gl.glVertex3f(p3.getX(), p3.getY(), p3.getZ());

        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3f(p4.getX(), p4.getY(), p4.getZ());

        gl.glEnd();
    }

    private final LinkedHashMap<String, Consumer<GL2>> tasks = new LinkedHashMap<>();
    public void updateResources(GL2 gl){
        if(tasks.size() > 0 && viewer.isInLoadingQueue()) {
            Map.Entry<String, Consumer<GL2>> entry = tasks.entrySet().iterator().next();
            entry.getValue().accept(gl);
            tasks.remove(entry.getKey());

            if(tasks.size() == 0)
                viewer.leaveLoadingQueue();
        }
    }

    public static class Point3D{

        private float x;
        private float y;
        private float z;

        public Point3D(float x, float y, float z){
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }

        public float getZ() {
            return z;
        }

        public void setZ(float z) {
            this.z = z;
        }
    }
}