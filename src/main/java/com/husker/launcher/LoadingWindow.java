package com.husker.launcher;


import com.alee.utils.swing.extensions.SizeMethodsImpl;
import com.husker.launcher.components.ProgressBar;
import com.husker.launcher.components.ScalableImage;
import com.husker.launcher.managers.UpdateManager;
import com.husker.glassui.GlassUI;
import com.husker.launcher.utils.ComponentUtils;
import com.husker.launcher.utils.RenderUtils;
import com.husker.launcher.utils.ShapeUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

import static com.husker.launcher.utils.ShapeUtils.ALL_CORNERS;

public class LoadingWindow extends JFrame {

    private static final Color defaultColor = new Color(255, 255, 255, 200);
    private static final Color hoveredColor = new Color(255, 255, 255, 200);

    private static final Color defaultTextColor = new Color(160, 160, 160);
    private static final Color hoveredTextColor = new Color(90, 90, 90);

    private static final int shadow = 5;

    private final Launcher launcher;

    //private JLabel statusLabel;
    private JLabel closeLabel;
    private JLabel hideLabel;
    private JPanel statusPanel;

    private ProgressBar progressBar;

    private boolean error = false;
    private boolean starting = false;
    private boolean ready = false;

    public LoadingWindow(Launcher launcher){
        super("Launcher loading");
        setIconImage(launcher.Resources.Icon);
        this.launcher = launcher;

        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setSize(450,300);
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

                    add(statusPanel = new JPanel(){{
                        setBorder(BorderFactory.createEmptyBorder(5, 20, 10, 20));
                        setLayout(new BorderLayout());
                        add(progressBar = new ProgressBar(){{
                            setPreferredHeight(150);
                        }});
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
                        setFitType(FitType.FILL_XY);
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
                RenderUtils.drawOuterShade(g2d, ShapeUtils.createRoundRectangle(LoadingWindow.this, statusPanel, 0, 0), new Color(0, 0, 0, 60), 10);

                g2d.setClip(null);
                RenderUtils.drawOuterShade(g2d, getWindowShape(), new Color(0, 0, 0, 50), shadow);
                g2d.dispose();
            }
        });

        new Thread(() -> {
            setStatusText("Проверка обновлений...", 15);
            try {
                if(launcher.UpdateManager.hasUpdate()){
                    launcher.UpdateManager.downloadUpdate(args -> setStatusText("Скачивание...", (int)args.getSpeed() + " Мб/с", "(" + (int)(args.getSize() / 1000000d) + "/" + (int)(args.getCurrentSize() / 1000000d) + " Мб)", args.getPercent()));
                    launcher.UpdateManager.unzipUpdate(args -> setStatusText("Распаковка... ", args.getPercent()));
                    launcher.UpdateManager.unpackUpdateFolder(percent -> setStatusText("Завершение...", percent));

                    setStatusText("Перезапуск...", 50);
                    launcher.UpdateManager.rebootToApplyUpdate();
                    System.exit(0);
                }else {
                    starting = true;
                    if (error)
                        setErrorText();
                    else
                        setStatusText("Запуск...", 90);
                }

                ready = true;

            }catch (UpdateManager.UpdateException ex){
                switch (ex.getStage()) {
                    case VERSION_GET:
                        setStatusText("Ошибка проверки обновления!", "(" + ex.getCode() + ")", 0);
                        break;
                    case DOWNLOAD:
                        setStatusText("Ошибка скачивания!   (" + ex.getCode() + ")", 0);
                        break;
                    case UNZIP:
                        setStatusText("Ошибка распаковки!", "(" + ex.getCode() + ")", 0);
                        break;
                    case UNPACK:
                        setStatusText("Ошибка перемещения!", "(" + ex.getCode() + ")", 0);
                        break;
                    case REBOOT:
                        setStatusText("Ошибка перезапуска приложения!", "(" + ex.getCode() + ")", 0);
                        break;
                }
                logException(ex.getException());
            }catch (Exception e){
                e.printStackTrace();
                setStatusText("Неизвестная ошибка", "", 0);
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
        return ready;
    }

    public void setStatusText(String text, double progress){
        setStatusText(text, "", progress);
    }

    public void setStatusText(String text, String addition, double progress){
        setStatusText(text, addition, "", progress);
    }

    public void setStatusText(String text, String addition, String addition2, double progress){
        progressBar.setValueText(addition);
        progressBar.setValue(progress);
        progressBar.setText(text);
        progressBar.setSpeedText(addition2);
    }

    public void onError(){
        error = true;
        if(starting)
            setErrorText();
    }



    private void setErrorText(){
        if(!System.getProperty("java.version").startsWith("1.8"))
            setStatusText("Для работы рекомендуется Java 1.8", 0);
        else
            setStatusText("Ошибка при запуске!", 0);

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if(e.getKeyCode() == KeyEvent.VK_F5){
                ArrayList<String> text = new ArrayList<>();
                text.add("Java version: " + System.getProperty("java.version"));
                text.add("JAVA_HOME: " + System.getProperty("java.home"));

                JOptionPane.showConfirmDialog(this, String.join("\n", text.toArray(new String[0])), "Дополнительная информация", JOptionPane.OK_CANCEL_OPTION);
            }
            return false;
        });
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
