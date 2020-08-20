package com.husker.launcher;

import com.alee.laf.WebLookAndFeel;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.style.StyleId;
import com.husker.launcher.components.ScalableImage;

import com.husker.launcher.ui.blur.BlurPainter;
import com.husker.launcher.ui.impl.glass.GlassUI;
import com.husker.launcher.ui.LauncherUI;
import com.husker.launcher.utils.ConsoleUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;

public class LauncherWindow extends JFrame {

    public static final String VERSION = "1.0";

    private ScalableImage backgroundImage;
    private ScalableImage animationBackgroundImage;
    private LauncherUI currentUI;
    private final String currentUIName;

    private float currentAlpha = 0;
    private boolean isAnimating = false;
    private WebPanel animationPanel;

    private final SettingsFile config = new SettingsFile("launcher_config.cfg");
    private final SettingsFile settings = new SettingsFile(new File("launcher.cfg")){{
        setDefault("auto_auth", "true");
        setDefault("background", "1");
        setDefault("windowed", "false");
        setDefault("ram", "256");
    }};

    public final Resources Resources = new Resources(this);
    public final NetManager NetManager = new NetManager(this);
    public final BrowserManager BrowserManager = new BrowserManager(this);

    public LauncherWindow(){
        ConsoleUtils.printDebug(getClass(), "Installing the WebLaF library");

        WebLookAndFeel.install();

        Toolkit.getDefaultToolkit().setDynamicLayout(true);
        System.setProperty("sun.awt.noerasebackground", "true");

        ConsoleUtils.printResult("OK");

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                new Thread(() -> {
                    ConsoleUtils.printDebug(LauncherWindow.class, "Closing");
                    BrowserManager.close();
                    ConsoleUtils.printDebug(LauncherWindow.class, "Closed");
                    System.exit(0);
                }).start();
            }
        });

        currentUIName = config.get("ui", GlassUI.class.getCanonicalName());
        try {
            Class<? extends LauncherUI> c = (Class<? extends LauncherUI>) Class.forName(currentUIName);
            currentUI = c.getConstructor(LauncherWindow.class).newInstance(this);
        }catch (Exception ex){
            ex.printStackTrace();
            currentUI = new GlassUI(this);
        }

        setContentPane(new WebPanel(StyleId.panelTransparent){{
            setLayout(new OverlayLayout(this));

            // For animation
            add(animationPanel = new WebPanel(){
                {
                    setLayout(new BorderLayout());
                    add(animationBackgroundImage = new ScalableImage(getBackgroundFromSettings(), ScalableImage.FitType.FIT_XY));
                    setOpaque(false);
                    setBackground(new Color(0, 0, 0, 0));
                    setVisible(false);
                }

                public void paint(Graphics g) {
                    Graphics2D g2d = (Graphics2D)g;
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, currentAlpha / 255f));

                    super.paint(g2d);
                }
            });

            // UI
            add(new WebPanel(StyleId.panelTransparent){{
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
                        for(BlurPainter painter : currentUI.getScreen().getBlurPainters())
                            painter.paint(g2d);
                    }

                    super.paint(g);
                }
            });

            // Background
            add(new JPanel(){{
                setLayout(new BorderLayout());
                add(backgroundImage = new ScalableImage(getBackgroundFromSettings(), ScalableImage.FitType.FIT_XY));
            }});


        }});

        setTitle(config.get("title"));
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
        if(getSettings().get("background").equals("0") && Resources.Background[0] == null)
            getSettings().set("background", "1");

        return Resources.Background[Integer.parseInt(getSettings().get("background"))];
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

    public SettingsFile getConfig() {
        return config;
    }

    public SettingsFile getSettings() {
        return settings;
    }

    public void beginAnimation(){
        if(currentUI == null || currentUI.getNextScreen() == null || isAnimating)
            return;
        isAnimating = true;
        animationPanel.setVisible(true);

        new Thread(() -> {
            final float speed = 15;

            while(true){
                if(currentUI.getNextScreen() != null){
                    currentAlpha += speed;

                    if(currentAlpha >= 255){
                        currentAlpha = 255;

                        currentUI.applyNextScreen();
                        currentUI.setVisible(false);
                        currentUI.setVisible(true);

                        try {
                            Thread.sleep(250);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

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

                try {
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

    public void updateUI(){
        ((WebPanel)getContentPane()).updateUI();
        ((WebPanel)getContentPane()).repaint();
    }

}
