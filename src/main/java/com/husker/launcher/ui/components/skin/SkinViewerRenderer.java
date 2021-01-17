package com.husker.launcher.ui.components.skin;

import com.alee.managers.animation.easing.Cubic;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.texture.TextureIO;

import java.io.InputStream;
import java.util.Random;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2ES1.*;

public class SkinViewerRenderer implements GLEventListener {

    private static final float layerIndent = 0.5f;

    private final GLU glu = new GLU();

    private boolean disposed = false;
    private int textureId = -1;

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

    private int[] cape = new int[6];
    private int[] elytra = new int[6];

    private static final float maxAngle = 40;
    private float angle = 0;
    private boolean animType = true;

    private float capeAnim = new Random().nextInt(1000);

    private float currentRotateX = 0;
    private float currentRotateY = 0;
    private float currentZoom = 0;
    private float currentCamY = 0;

    private long lastTime = -1;

    private float alpha = 0;

    private final SkinViewer viewer;

    public SkinViewerRenderer(SkinViewer viewer){
        this.viewer = viewer;
    }

    public void display(GLAutoDrawable drawable) {
        final GL2 gl = drawable.getGL().getGL2();

        float delta = (System.currentTimeMillis() - lastTime) / 100f;
        lastTime = System.currentTimeMillis();

        updateAnimation(delta);

        // Smooth value changing
        currentRotateX += delta * (viewer.getRotationX() - currentRotateX);
        currentRotateY += delta * (viewer.getRotationY() - currentRotateY);
        currentZoom += delta * (viewer.getCamZoom() - currentZoom);
        currentCamY += delta * (viewer.getCamY() - currentCamY);

        gl.glClearColor(0f, 0f, 0f, 0.003f);
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();

        checkForTextureChanges(gl);

        gl.glTranslatef(0f, 0, -currentZoom);
        gl.glRotatef(currentRotateY, 1, 0, 0);
        gl.glRotatef(currentRotateX, 0, 1, 0);
        gl.glTranslatef(0, -currentCamY, -0);

        if(!disposed && viewer.getTexturesId() == textureId){
            if(viewer.getPlayerTexture() != null || viewer.getCapeTexture() != null || viewer.getElytraTexture() != null){
                if (alpha < 1)
                    alpha += 0.25 * delta;
                if (alpha > 1)
                    alpha = 1;
            }
            gl.glColor4f(1, 1, 1, alpha);

            if(viewer.getPlayerTexture() != null) {
                drawLeg(gl, new Point3D(0, 0, -2), angle, rightLeg);
                drawLeg(gl, new Point3D(-4, 0, -2), -angle, leftLeg);
                drawCuboid(gl, new Point3D(-4, 12, -2), 8, 12, 4, body);
                drawCuboid(gl, new Point3D(-4, 24, -4), 8, 8, 8, head);

                drawFatLeg(gl, new Point3D(0, 0, -2), angle, rightLeg_layer);
                drawFatLeg(gl, new Point3D(-4, 0, -2), -angle, leftLeg_layer);
                drawCuboid(gl, new Point3D(-4 - layerIndent, 12 - layerIndent, -2 - layerIndent), 8 + layerIndent * 2f, 12 + layerIndent * 2f, 4 + layerIndent * 2f, body_layer);
                drawCuboid(gl, new Point3D(-4 - layerIndent, 24 - layerIndent, -4 - layerIndent), 8 + layerIndent * 2f, 8 + layerIndent * 2f, 8 + layerIndent * 2f, head_layer);

                drawHand(gl, new Point3D(viewer.isMaleSkin() ? -8 : -7, 12, -2), angle, true, leftHand);
                drawHand(gl, new Point3D(4, 12, -2), -angle, false, rightHand);
                drawFatHand(gl, new Point3D(viewer.isMaleSkin() ? -8 : -7, 12, -2), angle, true, leftHand_layer);
                drawFatHand(gl, new Point3D(4, 12, -2), -angle, false, rightHand_layer);
            }



            if(viewer.getCapeTexture() != null) {
                rotateXByPoint(gl, 2 * (1 + Math.sin(capeAnim)), 0, 24, -3);
                drawCuboid(gl, new Point3D(-5, 8, -3), 10, 16, 1, cape);
                resetMatrix(gl);
            }
            if(viewer.getElytraTexture() != null){
                gl.glPushMatrix();
                gl.glTranslated(0, 24, -3);
                gl.glRotated(8, 1, 0, 0);
                gl.glTranslated(0, -24, 3);
                drawCuboid(gl, new Point3D(-5, 8, -3), 10, 16, 1, elytra);
                gl.glPopMatrix();
            }
        }

        gl.glFlush();
    }

    public void drawFatLeg(GL2 gl, Point3D point, float rotation, int... textures){
        rotateXByPoint(gl, rotation, point.x + 2, point.y + 12, point.z + 2);


        point.x -= layerIndent;
        point.y -= layerIndent;
        point.z -= layerIndent;
        drawCuboid(gl, point, 4 + layerIndent * 2f, 12 + layerIndent * 2f, 4 + layerIndent * 2f, textures);

        resetMatrix(gl);
    }

    public void drawFatHand(GL2 gl, Point3D point, float rotation, boolean isLeft, int... textures){
        int size = viewer.isMaleSkin() ? 4 : 3;

        rotateXByPoint(gl, rotation, point.x + (size / 2d), point.y + 10, point.z + 2);
        rotateXByPoint(gl, (isLeft ? -1 : 1) * 3 * Math.sin(capeAnim), point.x + (size / 2d), point.y + 10, point.z + 2);
        rotateZByPoint(gl, (isLeft ? -1 : 1) * (1 + Math.sin(capeAnim)), point.x + (size / 2d), point.y + 10, point.z + 2);

        point.x -= layerIndent;
        point.y -= layerIndent;
        point.z -= layerIndent;
        drawCuboid(gl, point, size + layerIndent * 2f, 12 + layerIndent * 2f, 4 + layerIndent * 2f, textures);

        resetMatrix(gl);
    }

    public void drawHand(GL2 gl, Point3D point, float rotation, boolean isLeft, int... textures){
        int size = viewer.isMaleSkin() ? 4 : 3;
        rotateXByPoint(gl, rotation, point.x + (size / 2d), point.y + 10, point.z + 2);
        rotateXByPoint(gl, (isLeft ? -1 : 1) * 3 * Math.sin(capeAnim), point.x + (size / 2d), point.y + 10, point.z + 2);
        rotateZByPoint(gl, (isLeft ? -1 : 1) * (1 + Math.sin(capeAnim)), point.x + (size / 2d), point.y + 10, point.z + 2);
        drawCuboid(gl, point, size, 12, 4, textures);
        resetMatrix(gl);
    }

    public void drawLeg(GL2 gl, Point3D point, float rotation, int... textures){
        rotateXByPoint(gl, rotation, point.x + 2, point.y + 12, point.z + 2);
        drawCuboid(gl, point, 4, 12, 4, textures);
        resetMatrix(gl);
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

    public void resetMatrix(GL2 gl){
        gl.glPopMatrix();
    }

    public void rotateXByPoint(GL2 gl, double angle, double x, double y, double z){
        gl.glPushMatrix();
        gl.glTranslated(x, y, z);
        gl.glRotated(angle, 1, 0, 0);
        gl.glTranslated(-x, -y, -z);
    }

    public void rotateYByPoint(GL2 gl, double angle, double x, double y, double z){
        gl.glPushMatrix();
        gl.glTranslated(x, y, z);
        gl.glRotated(angle, 0, 1, 0);
        gl.glTranslated(-x, -y, -z);
    }

    public void rotateZByPoint(GL2 gl, double angle, double x, double y, double z){
        gl.glPushMatrix();
        gl.glTranslated(x, y, z);
        gl.glRotated(angle, 0, 0, 1);
        gl.glTranslated(-x, -y, -z);
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
    }

    public void updateAnimation(float delta){
        capeAnim += delta * 0.15;

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

    public void checkForTextureChanges(GL2 gl){
        if((viewer.getPlayerTexture() != null || viewer.getCapeTexture() != null || viewer.getElytraTexture() != null) && (disposed || viewer.getTexturesId() != textureId))
            updateTexture(gl);
    }

    public void updateTexture(GL2 gl){
        alpha = 0;

        if(viewer.getPlayerTexture() != null) {
            rightLeg = fromTextures(gl, viewer.getTexturesInputStream("leg_right"));
            leftLeg = fromTextures(gl, viewer.getTexturesInputStream("leg_left"));

            rightLeg_layer = fromTextures(gl, viewer.getTexturesInputStream("leg_right_layer"));
            leftLeg_layer = fromTextures(gl, viewer.getTexturesInputStream("leg_left_layer"));

            rightHand = fromTextures(gl, viewer.getTexturesInputStream("hand_right"));
            leftHand = fromTextures(gl, viewer.getTexturesInputStream("hand_left"));

            rightHand_layer = fromTextures(gl, viewer.getTexturesInputStream("hand_right_layer"));
            leftHand_layer = fromTextures(gl, viewer.getTexturesInputStream("hand_left_layer"));

            body = fromTextures(gl, viewer.getTexturesInputStream("body"));
            body_layer = fromTextures(gl, viewer.getTexturesInputStream("body_layer"));

            head = fromTextures(gl, viewer.getTexturesInputStream("head"));
            head_layer = fromTextures(gl, viewer.getTexturesInputStream("head_layer"));
        }
        if(viewer.getCapeTexture() != null)
            cape = fromTextures(gl, viewer.getTexturesInputStream("cape"));
        if(viewer.getElytraTexture() != null)
            elytra = fromTextures(gl, viewer.getTexturesInputStream("elytra"));

        textureId = viewer.getTexturesId();
        disposed = false;
    }

    private int[] fromTextures(GL2 gl, InputStream[] textures){
        if(textures == null)
            return null;

        int[] values = new int[textures.length];
        for(int i = 0; i < textures.length; i++) {
            try {
                values[i] = TextureIO.newTexture(textures[i], false, "png").getTextureObject(gl);
                gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            }catch (Exception ex){
                values[i] = -1;
            }
        }
        return values;
    }


    public void drawCuboid(GL2 gl, Point3D location, float sizeX, float sizeY, float sizeZ, int... textures){
        if(textures == null)
            return;
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