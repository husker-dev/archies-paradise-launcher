package com.husker.launcher;

import com.alee.laf.WebLookAndFeel;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.components.ScalableImage;

import com.husker.launcher.ui.components.TransparentPanel;
import com.husker.launcher.settings.*;
import com.husker.launcher.ui.blur.BlurPainter;
import com.husker.glassui.GlassUI;
import com.husker.launcher.ui.LauncherUI;
import com.husker.launcher.utils.ConsoleUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;

public class Launcher extends JFrame {

    private static final Logger log = LogManager.getLogger(Launcher.class);

    private ScalableImage backgroundImage;
    private ScalableImage animationBackgroundImage;
    private LauncherUI currentUI;
    private String currentUIName;

    private float currentAlpha = 0;
    private boolean isAnimating = false;
    private TransparentPanel animationPanel;

    public final User User = new User();

    public Launcher(){
        log.info("Loading resources...");
        Resources.load();

        log.info("Installing the WebLaF library");
        WebLookAndFeel.install();
        Toolkit.getDefaultToolkit().setDynamicLayout(true);
        System.setProperty("sun.awt.noerasebackground", "true");


        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                new Thread(() -> {
                    log.info("Closed");
                    System.exit(0);
                }).start();
            }
        });

        currentUIName = LauncherConfig.INSTANCE.get("ui");
        if(currentUIName == null)
            currentUIName = GlassUI.class.getCanonicalName();
        try {
            Class<? extends LauncherUI> c = (Class<? extends LauncherUI>) Class.forName(currentUIName);
            currentUI = c.getConstructor(Launcher.class).newInstance(this);
        }catch (Exception ex){
            ex.printStackTrace();
            currentUI = new GlassUI(this);
        }

        setContentPane(new TransparentPanel(){{
            setLayout(new OverlayLayout(this));

            // For animation
            add(animationPanel = new TransparentPanel(){
                {
                    setLayout(new BorderLayout());
                    add(animationBackgroundImage = new ScalableImage(getBackgroundFromSettings(), ScalableImage.FitType.FILL_XY));
                    setVisible(false);
                }

                public void paint(Graphics g) {
                    Graphics2D g2d = (Graphics2D)g;
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, currentAlpha / 255f));

                    super.paint(g2d);
                }
            });

            // UI
            add(new TransparentPanel(){{
                setLayout(new BorderLayout());
                add(currentUI);
            }});

            // Blur, Shadow, etc...
            add(new JPanel(){
                {
                    setOpaque(false);
                    setBackground(new Color(0, 0, 0, 0));
                    setLayout(new BorderLayout());
                }
                public void paint(Graphics g) {
                    if(currentUI != null && currentUI.getScreen() != null){
                        Graphics2D g2d = (Graphics2D)g;
                        for(BlurPainter painter : currentUI.getScreen().getBlurPainters()) {
                            if(painter != null)
                                painter.paint(g2d);
                        }
                    }

                    super.paint(g);
                }
            });

            // Background
            add(new JPanel(){{
                setLayout(new BorderLayout());
                add(backgroundImage = new ScalableImage(getBackgroundFromSettings(), ScalableImage.FitType.FILL_XY));
            }});
        }});

        setTitle(LauncherConfig.getTitle());
        setIconImage(Resources.Icon);

        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(currentUI.getDefaultSize());
        setLocationRelativeTo(null);

        currentUI.onInit();
        if(currentUI.isAnimated())
            currentAlpha = 255;
    }

    public void setBackgroundImage(BufferedImage image){
        backgroundImage.setImage(image);
        animationBackgroundImage.setImage(image);
    }

    public BufferedImage getBackgroundFromSettings(){
        if(LauncherSettings.getBackgroundIndex() == 0 && Resources.Background[0] == null)
            LauncherSettings.setBackgroundIndex(1);

        return Resources.Background[LauncherSettings.getBackgroundIndex()];
    }

    public BufferedImage getBackgroundImage(){
        return backgroundImage.getImage();
    }

    public ScalableImage getBackgroundScalableImage(){
        return backgroundImage;
    }

    public int getActualWidth(){
        return getRootPane().getWidth();
    }

    public int getActualHeight(){
        return getRootPane().getHeight();
    }

    public void beginAnimation(){
        if(currentUI == null || currentUI.getNextScreen() == null || isAnimating)
            return;
        isAnimating = true;
        animationPanel.setVisible(true);

        new Thread(() -> {
            final float speed = 15;

            while(true){
                try{
                    if(currentUI.getNextScreen() != null){
                        currentAlpha += speed;
                        if(currentAlpha >= 255){
                            currentAlpha = 255;
                            currentUI.applyNextScreen();
                            currentUI.setVisible(false);
                            currentUI.setVisible(true);
                            Thread.sleep(250);
                        }
                    }else{
                        currentAlpha -= speed;
                        if(currentAlpha <= 0){
                            currentAlpha = 0;
                            isAnimating = false;
                            animationPanel.setVisible(false);
                            break;
                        }
                    }
                    if(animationPanel != null)
                        animationPanel.repaint();
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
            }
        }).start();
    }

    public String getCurrentUIName(){
        return currentUIName;
    }

    public String getCurrentUITitle(){
        if(currentUIName.contains("."))
            return currentUIName.split("\\.")[currentUIName.split("\\.").length - 1];
        return currentUIName;
    }

    public static String getSettingsFolder(){
        try {
            Process p =  Runtime.getRuntime().exec("reg query \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders\" /v personal");
            p.waitFor();

            InputStream in = p.getInputStream();
            byte[] b = new byte[in.available()];
            in.read(b);
            in.close();

            return new String(b).split("\\s\\s+")[4] + "/" + LauncherConfig.getTitle();
        } catch(Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    public void updateUI(){
        getContentPane().revalidate();
        getContentPane().repaint();
    }

}
