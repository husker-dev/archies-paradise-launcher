package com.husker.launcher;

import com.alee.laf.WebLookAndFeel;
import com.husker.launcher.ui.components.ScalableImage;

import com.husker.launcher.ui.components.TransparentPanel;
import com.husker.launcher.settings.*;
import com.husker.launcher.ui.blur.BlurPainter;
import com.husker.glassui.GlassUI;
import com.husker.launcher.ui.LauncherUI;
import com.husker.launcher.utils.SystemUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class Launcher extends JFrame {

    public static final String VERSION = "0.3.1";

    private static final Logger log = LogManager.getLogger(Launcher.class);

    private ScalableImage backgroundImage;
    private ScalableImage animationBackgroundImage;
    private LauncherUI currentUI;
    private String currentUIName;

    private float currentAlpha = 0;
    private boolean isAnimating = false;
    private TransparentPanel animationPanel;

    public final User User = new User();

    private final HashMap<String, Thread> initializations = new HashMap<>();
    public boolean launchError = false;
    public Exception launchException;

    public Launcher(){
        this(() -> {});
    }

    public Launcher(Runnable loadingListener){
        initThread("resources", () -> {
            log.info("Loading resources...");
            Resources.load();
            loadingListener.run();
        });

        setTitle(LauncherConfig.getTitle());
        setMinimumSize(new Dimension(1070, 690));
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                new Thread(() -> {
                    log.info("Closed");
                    System.exit(0);
                }).start();
            }
        });

        log.info("Installing the WebLaF library");
        WebLookAndFeel.install();
        loadingListener.run();

        waitThread("resources");

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

        setIconImage(Resources.Icon);
        setSize(currentUI.getDefaultSize());
        setLocationRelativeTo(null);

        loadingListener.run();
        currentUI.onInit();
        if(currentUI.isAnimated())
            currentAlpha = 255;
    }

    public void setBackgroundImage(BufferedImage image){
        backgroundImage.setImage(image);
        animationBackgroundImage.setImage(image);
    }

    public BufferedImage getBackgroundFromSettings(){
        if(LauncherSettings.getBackgroundIndex() >= Resources.Background.length)
            LauncherSettings.setBackgroundIndex(0);
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
                if (currentUI.getNextScreen() != null) {
                    currentAlpha += speed;
                    if (currentAlpha >= 255) {
                        currentAlpha = 255;
                        currentUI.applyNextScreen();
                        currentUI.setVisible(false);
                        currentUI.setVisible(true);
                        SystemUtils.sleep(250);
                    }
                } else {
                    currentAlpha -= speed;
                    if (currentAlpha <= 0) {
                        currentAlpha = 0;
                        isAnimating = false;
                        animationPanel.setVisible(false);
                        break;
                    }
                }
                if (animationPanel != null)
                    animationPanel.repaint();
                SystemUtils.sleep(10);
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

    public void updateUI(){
        getContentPane().revalidate();
        getContentPane().repaint();
    }

    private void initThread(String name, Runnable runnable){
        Thread thread = new Thread(() -> {
            try{
                runnable.run();
            }catch (Exception ex){
                launchError = true;
                launchException = ex;
                ex.printStackTrace();
            }
        });
        initializations.put(name, thread);
        thread.start();
    }

    private void waitThread(String name){
        try {
            initializations.get(name).join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



}
