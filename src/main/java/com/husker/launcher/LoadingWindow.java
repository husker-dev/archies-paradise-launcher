package com.husker.launcher;


import com.husker.launcher.components.ScalableImage;
import com.husker.launcher.managers.UpdateManager;
import com.husker.glassui.GlassUI;
import com.husker.launcher.utils.ComponentUtils;
import com.husker.launcher.utils.RenderUtils;
import com.husker.launcher.utils.ShapeUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;

import static com.husker.launcher.utils.ShapeUtils.ALL_CORNERS;

public class LoadingWindow extends JFrame {

    private static final Color defaultColor = new Color(255, 255, 255, 200);
    private static final Color hoveredColor = new Color(255, 255, 255, 200);

    private static final Color defaultTextColor = new Color(160, 160, 160);
    private static final Color hoveredTextColor = new Color(90, 90, 90);

    private static final int shadow = 5;

    private final Launcher launcher;

    private JLabel statusLabel;
    private JLabel closeLabel;
    private JLabel hideLabel;

    private String html;
    private int fileSize;
    private int currentSize = 0;

    private boolean error = false;
    private boolean starting = false;

    public LoadingWindow(Launcher launcher){
        super("Launcher loading");
        setIconImage(launcher.Resources.Icon);
        this.launcher = launcher;

        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setSize(310,230);
        setUndecorated(true);
        setLocationRelativeTo(null);
        setBackground(Color.GRAY);

        FrameDragListener frameDragListener = new FrameDragListener(this);
        addMouseListener(frameDragListener);
        addMouseMotionListener(frameDragListener);

        setBackground(new Color(0, 0, 0, 0));

        getRootPane().setLayout(new BorderLayout());
        getRootPane().add(new JPanel(){
            {
                setBorder(BorderFactory.createEmptyBorder(shadow, shadow, shadow, shadow));
                setLayout(new OverlayLayout(this));

                add(new JPanel(){{
                    setOpaque(false);
                    setBackground(new Color(0, 0, 0, 0));

                    setLayout(new BorderLayout());
                    add(new ScalableImage(launcher.Resources.Logo));

                    add(statusLabel = new JLabel(){{
                        setBorder(BorderFactory.createEmptyBorder(-10, 0, 0, 0));
                        setOpaque(true);
                        setPreferredSize(new Dimension(0, 60));
                        setBackground(defaultColor);
                        setFont(Resources.Fonts.ChronicaPro_ExtraBold.deriveFont(15f));
                        setForeground(GlassUI.Colors.labelText);
                        setVerticalAlignment(CENTER);
                        setHorizontalAlignment(CENTER);
                    }}, BorderLayout.SOUTH);

                    add(new JPanel(){
                        {
                            setBackground(new Color(0, 0, 0, 0));
                            setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
                            int width = 40;
                            int height = 25;

                            add(hideLabel = new JLabel("-"){
                                boolean hovered = false;
                                {
                                    setHorizontalAlignment(CENTER);
                                    setVerticalAlignment(CENTER);
                                    setFont(Resources.Fonts.ChronicaPro_ExtraBold.deriveFont(14f));
                                    setPreferredSize(new Dimension(width, height));
                                    setForeground(defaultTextColor);
                                    addMouseListener(new MouseAdapter() {
                                        public void mouseClicked(MouseEvent mouseEvent) {
                                            LoadingWindow.this.setState(Frame.ICONIFIED);
                                        }
                                        public void mouseEntered(MouseEvent mouseEvent) {
                                            setForeground(hoveredTextColor);
                                            hovered = true;
                                            LoadingWindow.this.repaint();
                                        }
                                        public void mouseExited(MouseEvent mouseEvent) {
                                            setForeground(defaultTextColor);
                                            hovered = false;
                                            LoadingWindow.this.repaint();
                                        }
                                    });
                                }
                                public void paint(Graphics graphics) {
                                    Graphics2D g2d = (Graphics2D) graphics;
                                    if(hovered)
                                        g2d.setColor(hoveredColor);
                                    else
                                        g2d.setColor(defaultColor);
                                    g2d.fill(ShapeUtils.createRoundRectangle(0, 0, getWidth(), getHeight(), 10, 10, ShapeUtils.Corner.BOTTOM_LEFT));
                                    super.paint(graphics);
                                }
                            });
                            add(closeLabel = new JLabel("X"){
                                boolean hovered = false;
                                {
                                    setHorizontalAlignment(CENTER);
                                    setVerticalAlignment(CENTER);
                                    setFont(Resources.Fonts.ChronicaPro_ExtraBold.deriveFont(14f));
                                    setPreferredSize(new Dimension(width, height));
                                    setForeground(defaultTextColor);
                                    addMouseListener(new MouseAdapter() {
                                        public void mouseClicked(MouseEvent mouseEvent) {
                                            System.exit(0);
                                        }
                                        public void mouseEntered(MouseEvent mouseEvent) {
                                            super.mouseEntered(mouseEvent);
                                            setForeground(hoveredTextColor);
                                            hovered = true;
                                            LoadingWindow.this.repaint();
                                        }
                                        public void mouseExited(MouseEvent mouseEvent) {
                                            super.mouseExited(mouseEvent);
                                            setForeground(defaultTextColor);
                                            hovered = false;
                                            LoadingWindow.this.repaint();
                                        }
                                    });
                                }
                                public void paint(Graphics graphics) {
                                    Graphics2D g2d = (Graphics2D) graphics;
                                    if(hovered)
                                        g2d.setColor(hoveredColor);
                                    else
                                        g2d.setColor(defaultColor);
                                    g2d.fill(ShapeUtils.createRoundRectangle(0, 0, getWidth(), getHeight(), 10, 10));
                                    super.paint(graphics);
                                }
                            });

                        }
                        public void paint(Graphics graphics) {
                            super.paint(graphics);
                            graphics.setColor(new Color(0, 0, 0, 50));
                            int cur_x = getComponent(0).getX();
                            for(int i = 0; i < getComponentCount() - 1; i++){
                                cur_x += getComponent(i).getWidth();
                                graphics.drawLine(cur_x, 8, cur_x, getHeight() - 8);
                            }

                        }
                    }, BorderLayout.NORTH);
                }});

                add(new JPanel(){{
                    setBackground(new Color(255, 255, 255, 10));
                }});

                add(new JPanel(){{
                    setLayout(new BorderLayout());
                    add(new ScalableImage(launcher.Resources.Loading_Background){{
                        setFitType(FitType.FIT_XY);
                    }});
                }});
            }

            public void paint(Graphics graphics) {
                Graphics2D g2d = (Graphics2D) graphics;

                BufferedImage tmp = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                super.paint(tmp.createGraphics());

                g2d.setPaint(new TexturePaint(tmp, new Rectangle(0, -1, tmp.getWidth(), tmp.getHeight())));
                RenderUtils.enableAntialiasing(g2d);
                g2d.fill(getWindowShape());

                Point hideLocation = ComponentUtils.getComponentLocationOnScreen(LoadingWindow.this, hideLabel);

                g2d.setClip(getWindowShape());
                RenderUtils.drawOuterShade(g2d, ShapeUtils.createRoundRectangle(hideLocation.x, hideLocation.y, hideLabel.getWidth() + closeLabel.getWidth(), hideLabel.getHeight() - 1, 10, 10, ShapeUtils.Corner.BOTTOM_LEFT), new Color(0, 0, 0, 60), 10);
                RenderUtils.drawOuterShade(g2d, ShapeUtils.createRoundRectangle(LoadingWindow.this, statusLabel, 0, 0), new Color(0, 0, 0, 60), 10);

                g2d.setClip(null);
                RenderUtils.drawOuterShade(g2d, getWindowShape(), new Color(0, 0, 0, 50), shadow);
                g2d.dispose();
            }
        });

        new Thread(() -> {
            setStatusText("Проверка обновлений...");
            try {
                if(launcher.UpdateManager.hasUpdate()){
                    setStatusText("Обновление...");
                    launcher.UpdateManager.downloadUpdate(percent -> setStatusText("Скачивание обновления...   " + percent + "%"));

                    setStatusText("Распаковка...");
                    launcher.UpdateManager.unzipUpdate(percent -> setStatusText("Распаковка...   " + percent + "%"));

                    setStatusText("Завершение...");
                    launcher.UpdateManager.unpackUpdateFolder(percent -> setStatusText("Завершение...   " + percent + "%"));

                    setStatusText("Перезапуск...");
                    launcher.UpdateManager.rebootToApplyUpdate();
                }else {
                    starting = true;
                    if (error)
                        setErrorText();
                    else
                        setStartingText();
                }

            }catch (UpdateManager.UpdateException ex){
                switch (ex.getStage()) {
                    case VERSION_GET:
                        setStatusText("Возникла ошибка при", "проверке обновления!   (" + ex.getCode() + ")");
                        break;
                    case DOWNLOAD:
                        setStatusText("Возникла ошибка при", "скачивании обновления!   (" + ex.getCode() + ")");
                        break;
                    case UNZIP:
                        setStatusText("Возникла ошибка при", "распаковке обновления!   (" + ex.getCode() + ")");
                        break;
                    case UNPACK:
                        setStatusText("Возникла ошибка при", "распаковке папки обновления!   (" + ex.getCode() + ")");
                        break;
                    case REBOOT:
                        setStatusText("Возникла ошибка при", "перезапуске приложения!   (" + ex.getCode() + ")");
                        break;
                }
                logException(ex.getException());
            }
        }).start();

        setVisible(true);
    }

    private void logException(Exception exception){
        try {
            PrintWriter pw = new PrintWriter(new File("./log.txt"));
            exception.printStackTrace(pw);
            pw.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Shape getWindowShape(){
        return ShapeUtils.createRoundRectangle(shadow, shadow, getWidth() - 1 - shadow * 2, getHeight() - 1 - shadow * 2, 20, 20, ALL_CORNERS);
    }

    public boolean isOK(){
        try {
            return !launcher.UpdateManager.hasUpdate();
        }catch (Exception ex){
            return false;
        }
    }

    public void setStatusText(String... text){
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("<html><center>" + String.join("<br><center>", text) + "</html>");
            LoadingWindow.this.repaint();
        });
    }

    public void onError(){
        error = true;
        if(starting)
            setErrorText();
    }

    private void setStartingText(){
        setStatusText("Запуск...");
    }

    private void setErrorText(){
        if(!System.getProperty("java.version").startsWith("1.8"))
            setStatusText("Ошибка при запуске!", "Попробуйте установить Java 1.8");
        else
            setStatusText("Ошибка при запуске!");
    }

    public static class FrameDragListener extends MouseAdapter {

        private final JFrame frame;
        private Point mouseDownCompCoords = null;

        public FrameDragListener(JFrame frame) {
            this.frame = frame;
        }

        public void mouseReleased(MouseEvent e) {
            mouseDownCompCoords = null;
        }

        public void mousePressed(MouseEvent e) {
            mouseDownCompCoords = e.getPoint();
        }

        public void mouseDragged(MouseEvent e) {
            Point currCoords = e.getLocationOnScreen();
            frame.setLocation(currCoords.x - mouseDownCompCoords.x, currCoords.y - mouseDownCompCoords.y);
        }
    }

}
