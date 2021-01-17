package com.husker.glassui.screens.main.play;

import com.husker.glassui.components.BlurScalableImage;
import com.husker.launcher.Resources;
import com.husker.launcher.managers.NetManager;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.ui.components.MLabel;
import com.husker.launcher.ui.components.TransparentPanel;
import com.husker.launcher.ui.utils.ImageUtils;
import com.husker.launcher.utils.SystemUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.TimerTask;

public class ScreenshotsPanel extends TransparentPanel {

    private final BlurScalableImage viewer;
    private TransparentPanel pages;

    private String[] urls;
    private BufferedImage[] urls_preview;
    private String[] urls_fullscreen;

    private TransparentPanel nextBtn, prevBtn;

    private int selectedIndex = 0;

    private final BufferedImage dotDefault = ImageUtils.applyDefaultShadow(Resources.Icon_Dot);
    private final BufferedImage dotSelected = ImageUtils.applyDefaultShadow(Resources.Icon_Dot_Selected);

    private final long timer = 8 * 1000;
    private long currentDelay = 0;

    public ScreenshotsPanel(Screen screen){
        new Thread(() -> {
            while(true){
                currentDelay += 100;
                if(currentDelay >= timer){
                    currentDelay = 0;
                    nextPage(false);
                }
                SystemUtils.sleep(100);
            }
        }).start();
        setLayout(new OverlayLayout(this));
        add(viewer = new BlurScalableImage(screen){
            {
                setAnimated(true);
                setFitType(FitType.FILL_XY);
            }
            public void onBlurApply(BlurParameter parameter, Component component) {
                super.onBlurApply(parameter, component);
                if (returnOnInvisible(parameter, component))
                    return;
                parameter.setShadowType(BlurParameter.ShadowType.INNER);
            }
        });

        // Buttons
        add(new TransparentPanel(){{
            setLayout(new BorderLayout());
            add(prevBtn = new ArrowButton(ImageUtils.applyDefaultShadow(Resources.Icon_Arrow_Left), true, ScreenshotsPanel.this::prevPage), BorderLayout.WEST);
            add(nextBtn = new ArrowButton(ImageUtils.applyDefaultShadow(Resources.Icon_Arrow_Right), false, ScreenshotsPanel.this::nextPage), BorderLayout.EAST);
            add(new TransparentPanel(){{
                setLayout(new FlowLayout(FlowLayout.RIGHT));
                add(new MLabel(){
                    boolean hovered = false;
                    {
                        setImageSize(25);
                        setImage(Resources.Icon_Fullscreen);
                        addMouseListener(new MouseAdapter() {
                            public void mouseEntered(MouseEvent e) {
                                hovered = true;
                            }
                            public void mouseExited(MouseEvent e) {
                                hovered = false;
                            }
                            public void mousePressed(MouseEvent e) {
                                NetManager.openLink(urls_fullscreen[selectedIndex]);
                            }
                        });
                    }
                    public void paint(Graphics g) {
                        Graphics2D g2d = (Graphics2D) g;
                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, hovered ? 0.9f : 0.5f));
                        super.paint(g);
                    }
                });
            }}, BorderLayout.NORTH);
        }}, 0);

        // Pages
        add(new TransparentPanel(){{
            setLayout(new BorderLayout());
            add(pages = new TransparentPanel(){{
                setLayout(new FlowLayout());
            }}, BorderLayout.SOUTH);
        }}, 1);
    }

    private void updateDots(int count, int selected){
        SwingUtilities.invokeLater(() -> {
            if(count != pages.getComponentCount()) {
                pages.removeAll();
                for (int i = 0; i < count; i++)
                    pages.add(new MLabel(selected == i ? dotSelected : dotDefault, 13));
            }else{
                for(int i = 0; i < pages.getComponentCount(); i++){
                    MLabel dot = (MLabel)pages.getComponent(i);
                    if(dot.getImage() == dotSelected){
                        dot.setImage(dotDefault);
                        break;
                    }
                }
                ((MLabel)pages.getComponent(selected)).setImage(dotSelected);
            }
        });
    }

    public void setPage(int index){
        setPage(index, true);
    }

    public void setPage(int index, boolean usePreview){
        selectedIndex = index;
        updateDots(urls.length, index);
        new Thread(() -> {
            try {
                //boolean first = viewer.getImage() == null;

                viewer.setImage(urls_preview[index]);
                /*
                BufferedImage loaded = ImageIO.read(new URL(urls[index]));
                if(index == selectedIndex) {
                    if(usePreview) {
                        viewer.setAnimated(false);
                        viewer.setImage(loaded);
                        viewer.setAnimated(true);
                    }else
                        viewer.setImage(loaded);
                }

                 */
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void nextPage(){
        nextPage(true);
    }

    public void nextPage(boolean usePreview){
        currentDelay = 0;

        if(urls == null || urls.length < 2)
            return;
        if (selectedIndex == urls.length - 1)
            setPage(0, usePreview);
        else
            setPage(selectedIndex + 1, usePreview);
    }

    public void prevPage(){
        prevPage(true);
    }

    public void prevPage(boolean usePreview){
        currentDelay = 0;

        if(urls == null || urls.length < 2)
            return;
        if(selectedIndex == 0)
            setPage(urls.length - 1, usePreview);
        else
            setPage(selectedIndex - 1, usePreview);
    }

    public void setUrls(String[] def, String[] preview, String[] full){
        if(Arrays.equals(def, urls))
            return;

        this.urls = def;
        this.urls_fullscreen = full;

        prevBtn.setVisible(preview.length > 1);
        nextBtn.setVisible(preview.length > 1);

        new Thread(() -> {
            this.urls_preview = new BufferedImage[urls.length];
            try {
                this.urls_preview[0] = ImageIO.read(new URL(full[0]));
                setPage(0);
                //updateDots(urls.length, 0);

                for(int i = 1; i < urls.length; i++)
                    this.urls_preview[i] = ImageIO.read(new URL(full[i]));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static class ArrowButton extends TransparentPanel {

        boolean hovered = false;

        public ArrowButton(BufferedImage icon, boolean way, Runnable action){
            setPreferredWidth(100);
            add(new MLabel(){{
                setImageSize(50);
                setImage(icon);
            }}, way ? BorderLayout.WEST : BorderLayout.EAST);
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                }
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                }
                public void mousePressed(MouseEvent e) {
                    action.run();
                }
            });
        }

        public void paint(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, hovered ? 0.8f : 0.3f));
            super.paint(g);
        }
    }
}
