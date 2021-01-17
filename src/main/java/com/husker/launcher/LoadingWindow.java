package com.husker.launcher;


import com.husker.launcher.discord.Discord;
import com.husker.launcher.ui.components.ProgressBar;
import com.husker.launcher.ui.components.ScalableImage;
import com.husker.launcher.managers.UpdateManager;
import com.husker.launcher.ui.utils.ComponentUtils;
import com.husker.launcher.ui.utils.RenderUtils;
import com.husker.launcher.ui.utils.ShapeUtils;
import com.husker.launcher.utils.SystemUtils;
import com.husker.mio.ProgressArguments;
import com.husker.mio.processes.DeletingProcess;
import com.husker.mio.processes.DownloadingProcess;
import com.husker.mio.processes.UnzippingProcess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.*;

import static com.husker.launcher.ui.utils.ShapeUtils.ALL_CORNERS;

public class LoadingWindow extends JFrame {

    private static final Color defaultColor = new Color(255, 255, 255, 200);
    private static final Color hoveredColor = new Color(255, 255, 255, 200);

    private static final Color defaultTextColor = new Color(160, 160, 160);
    private static final Color hoveredTextColor = new Color(90, 90, 90);

    private static final int shadow = 5;

    private Launcher launcher;
    private boolean launcherError = false;
    private boolean launcherStarted = false;
    private boolean updated = false;

    private JLabel closeLabel;
    private JLabel hideLabel;
    private JPanel statusPanel;

    private ProgressBar progressBar;

    private int launcherStatus = 0;
    private final Thread launcherLoadingThread;

    public LoadingWindow(){
        super("Launcher Loading");

        Discord.init();
        Discord.setState(Discord.Texts.Loading);
        Resources.loadBase();
        setIconImage(Resources.Icon);

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
                setOpaque(false);
                setBackground(new Color(0, 0, 0, 0));
                setBorder(BorderFactory.createEmptyBorder(shadow, shadow, shadow, shadow));
                setLayout(new OverlayLayout(this));

                add(new JPanel(){{
                    setOpaque(false);
                    setBackground(new Color(0, 0, 0, 0));

                    setLayout(new BorderLayout());
                    add(new ScalableImage(Resources.Logo));

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
                                    setFont(Resources.Fonts.getChronicaProExtraBold(14));
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

                                    g2d.setColor(hovered ? hoveredColor : defaultColor);
                                    g2d.fill(ShapeUtils.createRoundRectangle(0, 0, getWidth(), getHeight(), 10, 10, ShapeUtils.Corner.BOTTOM_LEFT));
                                    super.paint(graphics);
                                }
                            });
                            add(closeLabel = new JLabel("X"){
                                boolean hovered = false;
                                {
                                    setHorizontalAlignment(CENTER);
                                    setVerticalAlignment(CENTER);
                                    setFont(Resources.Fonts.getChronicaProExtraBold(14));
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

                                    g2d.setColor(hovered ? hoveredColor : defaultColor);
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
                    add(new ScalableImage(Resources.Background_Loading){
                        {
                            setFitType(FitType.FILL_XY);
                        }
                        public void paint(Graphics g) {
                            Shape s = getWindowShape();
                            g.setClip(AffineTransform.getTranslateInstance(-s.getBounds().x, -s.getBounds().y).createTransformedShape(s));
                            super.paint(g);
                        }
                    });
                }});
            }

            public void paint(Graphics graphics) {
                Graphics2D g2d = (Graphics2D) graphics;

                g2d.setClip(getWindowShape());
                super.paint(g2d);

                Point hideLocation = ComponentUtils.getComponentLocationOnScreen(LoadingWindow.this, hideLabel);

                g2d.setClip(getWindowShape());
                RenderUtils.drawOuterShade(g2d, ShapeUtils.createRoundRectangle(hideLocation.x, hideLocation.y, hideLabel.getWidth() + closeLabel.getWidth(), hideLabel.getHeight() - 1, 10, 10, ShapeUtils.Corner.BOTTOM_LEFT), new Color(0, 0, 0, 60), 10);
                RenderUtils.drawOuterShade(g2d, ShapeUtils.createRoundRectangle(statusPanel, 0, 0), new Color(0, 0, 0, 60), 10);

                g2d.setClip(null);
                RenderUtils.drawOuterShade(g2d, getWindowShape(), new Color(0, 0, 0, 50), shadow);
                g2d.dispose();
            }
        });

        new java.util.Timer().schedule(new TimerTask() {
            public void run() {
                if(launcherStarted && updated){
                    setVisible(false);
                    launcher.setVisible(true);
                    cancel();
                }
                if(launcherError && updated){
                    setErrorText();
                    cancel();
                }
            }
        }, 0, 30);

        launcherLoadingThread = new Thread(() -> {
            try {
                launcher = new Launcher(() -> {
                    launcherStatus++;
                    if(updated)
                        setStatusText("Запуск...", 100d / 3 * launcherStatus);
                });
                if(launcher.launchError)
                    throw launcher.launchException;
                launcherStarted = true;
            }catch (Exception ex){
                ex.printStackTrace();
                launcherError = true;
            }
        });
        launcherLoadingThread.start();

        new Thread(() -> {
            setStatusText("Проверка обновлений...", 15);
            try {
                if(UpdateManager.hasUpdate()){
                    launcherLoadingThread.stop();
                    UpdateManager.processUpdating(new UpdateManager.UpdateProcessor() {
                        public void onRemoveOld(ProgressArguments<DeletingProcess> arguments) {
                            setStatusText("Удаление временных файлов...", arguments.getPercent());
                        }
                        public void onConnecting() {
                            setStatusText("Соединение...", 0);
                        }
                        public void onDownloading(ProgressArguments<DownloadingProcess> arguments) {
                            String data = (int)(arguments.getCurrentSize() / 1000000d) + "/" + (int)(arguments.getFullSize() / 1000000d) + " Мб";
                            setStatusText("Скачивание...", SystemUtils.formatSpeed(arguments.getSpeed()), data, arguments.getPercent());
                        }
                        public void onUnzipping(ProgressArguments<UnzippingProcess> arguments) {
                            setStatusText("Распаковка обновления...", arguments.getPercent());
                        }
                        public void onZipRemoving(ProgressArguments<DeletingProcess> arguments) {
                            setStatusText("Удаление временных файлов...", arguments.getPercent());
                        }
                        public void onUnpack(double percent) {
                            setStatusText("Перемещение...", percent);
                        }
                        public void onReboot() {
                            setStatusText("Перезапуск...", 90);
                        }
                    });
                }else {
                    setStatusText("Запуск...", 25);
                    updated = true;
                }
            }catch (UpdateManager.UpdateException ex){
                launcherLoadingThread.stop();
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
                ex.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
                setStatusText("Неизвестная ошибка", "", 0);
            }
        }).start();

        setVisible(true);
    }

    public Shape getWindowShape(){
        return ShapeUtils.createRoundRectangle(shadow, shadow, getWidth() - 1 - shadow * 2, getHeight() - 1 - shadow * 2, 20, 20, ALL_CORNERS);
    }

    public void setStatusText(String text, double progress){
        setStatusText(text, "", progress);
    }

    public void setStatusText(String text, String addition, double progress){
        setStatusText(text, addition, "", progress);
    }

    private String correctText(String text){
        if(Resources.Fonts.needTransform()) {
            String spaceBuffer = new String(new char[(int)((double)text.length() * 0.3)]).replace("\0", " ");
            text += spaceBuffer;
        }
        return text;
    }

    public void setStatusText(String text, String addition, String addition2, double progress){
        progressBar.setValueText(correctText(addition));
        progressBar.setValue(progress);
        progressBar.setText(correctText(text));
        progressBar.setSpeedText(correctText(addition2));
    }

    private void setErrorText(){
        setStatusText("Ошибка при запуске лаунчера", 0);

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
