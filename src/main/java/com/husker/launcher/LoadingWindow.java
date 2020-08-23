package com.husker.launcher;


import com.alee.utils.FileUtils;
import com.husker.launcher.components.ScalableImage;
import com.husker.launcher.ui.impl.glass.GlassUI;
import com.husker.launcher.utils.ComponentUtils;
import com.husker.launcher.utils.ConsoleUtils;
import com.husker.launcher.utils.RenderUtils;
import com.husker.launcher.utils.ShapeUtils;
import net.lingala.zip4j.ZipFile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import static com.husker.launcher.utils.ShapeUtils.ALL_CORNERS;

public class LoadingWindow extends JFrame {

    public static String latestVersion = null;
    private final LauncherWindow launcher;

    private JLabel statusLabel;
    private JLabel closeLabel;
    private JLabel hideLabel;

    private String html;
    private int fileSize;
    private int currentSize = 0;

    private boolean error = false;
    private boolean starting = false;

    private static final SettingsFile config = new SettingsFile("update_config.cfg");

    private static final boolean checkUpdates = false;

    public LoadingWindow(LauncherWindow launcher){
        super("Launcher loading");
        setIconImage(launcher.Resources.Icon);
        this.launcher = launcher;

        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setSize(300,220);
        setUndecorated(true);
        setLocationRelativeTo(null);
        setBackground(Color.GRAY);

        setBackground(new Color(0, 0, 0, 0));

        getRootPane().setLayout(new BorderLayout());
        getRootPane().add(new JPanel(){
            {
                setLayout(new OverlayLayout(this));

                add(new JPanel(){{
                    setOpaque(false);
                    setBackground(new Color(0, 0, 0, 0));

                    setLayout(new BorderLayout());
                    add(new ScalableImage(launcher.Resources.Logo));

                    add(statusLabel = new JLabel("Проверка обновлений..."){{
                        setOpaque(true);
                        setPreferredSize(new Dimension(0, 50));
                        setBackground(new Color(255, 255, 255, 150));
                        setFont(Resources.Fonts.ChronicaPro_ExtraBold.deriveFont(15f));
                        setVerticalAlignment(CENTER);
                        setHorizontalAlignment(CENTER);
                    }}, BorderLayout.SOUTH);

                    add(new JPanel(){{
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
                                setForeground(new Color(130, 130, 130));
                                addMouseListener(new MouseAdapter() {
                                    public void mouseClicked(MouseEvent mouseEvent) {
                                        LoadingWindow.this.setState(Frame.ICONIFIED);
                                    }
                                    public void mouseEntered(MouseEvent mouseEvent) {
                                        super.mouseEntered(mouseEvent);
                                        setForeground(new Color(122, 122, 122));
                                        hovered = true;
                                        LoadingWindow.this.repaint();
                                    }
                                    public void mouseExited(MouseEvent mouseEvent) {
                                        super.mouseExited(mouseEvent);
                                        setForeground(new Color(130, 130, 130));
                                        hovered = false;
                                        LoadingWindow.this.repaint();
                                    }
                                });
                            }
                            public void paint(Graphics graphics) {
                                Graphics2D g2d = (Graphics2D) graphics;
                                if(hovered)
                                    g2d.setColor(new Color(220, 220, 220, 200));
                                else
                                    g2d.setColor(new Color(230, 230, 230, 120));
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
                                setForeground(new Color(140, 0, 0));
                                addMouseListener(new MouseAdapter() {
                                    public void mouseClicked(MouseEvent mouseEvent) {
                                        System.exit(0);
                                    }
                                    public void mouseEntered(MouseEvent mouseEvent) {
                                        super.mouseEntered(mouseEvent);
                                        setForeground(new Color(110, 10, 10));
                                        hovered = true;
                                        LoadingWindow.this.repaint();
                                    }
                                    public void mouseExited(MouseEvent mouseEvent) {
                                        super.mouseExited(mouseEvent);
                                        setForeground(new Color(140, 0, 0));
                                        hovered = false;
                                        LoadingWindow.this.repaint();
                                    }
                                });
                            }
                            public void paint(Graphics graphics) {
                                Graphics2D g2d = (Graphics2D) graphics;
                                if(hovered)
                                    g2d.setColor(new Color(160, 60, 60, 200));
                                else
                                    g2d.setColor(new Color(180, 80, 80, 140));
                                g2d.fill(ShapeUtils.createRoundRectangle(0, 0, getWidth(), getHeight(), 10, 10));
                                super.paint(graphics);
                            }
                        });

                    }}, BorderLayout.NORTH);
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
                RenderUtils.disableAntialiasing(g2d);
                g2d.fill(getWindowShape());

                Point hideLocation = ComponentUtils.getComponentLocationOnScreen(LoadingWindow.this, hideLabel);

                RenderUtils.drawOuterShade(g2d, ShapeUtils.createRoundRectangle(hideLocation.x, hideLocation.y, hideLabel.getWidth() + closeLabel.getWidth(), hideLabel.getHeight() - 1, 10, 10, ShapeUtils.Corner.BOTTOM_LEFT), new Color(0, 0, 0, 60), 10);

                RenderUtils.drawOuterShade(g2d, ShapeUtils.createRoundRectangle(LoadingWindow.this, statusLabel, 0, 0), new Color(0, 0, 0, 60), 10);

                g2d.dispose();
            }
        });

        new Thread(() -> {
            if(checkUpdates) {
                html = NetManager.getURLContent(config.get("github") + "/releases/latest");

                if (html == null) {
                    latestVersion = "unknown";
                    return;
                }

                latestVersion = html.split("<li class=\"d-block mb-1\">")[1].split("class=\"css-truncate-target\"")[1].split("\">")[1].split("</span>")[0];
            }

            if(checkUpdates && !latestVersion.equals(LauncherWindow.VERSION)){
                ConsoleUtils.printDebug(getClass(), "Current version: " + LauncherWindow.VERSION + ", Latest version: " + latestVersion);

                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Обновление...");
                    LoadingWindow.this.repaint();
                });

                String url = "https://github.com/" + html.split("d-flex flex-justify-between flex-items-center py-1 py-md-2 Box-body px-2")[1].split("href=\"")[1].split("\"")[0];

                BufferedInputStream in = null;
                try {
                    URLConnection connection = new URL(url).openConnection();
                    connection.connect();

                    fileSize = connection.getContentLength();

                    in = new BufferedInputStream(new URL(url).openStream());

                    Files.createDirectories(Paths.get("./update"));
                    for(File fileToDelete : Objects.requireNonNull(new File("./update").listFiles()))
                        FileUtils.deleteFile(fileToDelete);

                    FileOutputStream fileOutputStream = new FileOutputStream("./update/archive.zip");
                    byte[] dataBuffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                        fileOutputStream.write(dataBuffer, 0, bytesRead);

                        currentSize += bytesRead;
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("Обновление...    " + (int)((float)currentSize / (float)fileSize * 100f) + "%");
                            LoadingWindow.this.repaint();
                        });
                    }
                    fileOutputStream.close();

                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Распаковка...");
                        LoadingWindow.this.repaint();
                    });

                    ZipFile file = new ZipFile("./update/archive.zip");
                    file.extractAll("./update");

                    while(Files.exists(Paths.get("./update/archive.zip")))
                        Files.deleteIfExists(Paths.get("./update/archive.zip"));

                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Завершение...");
                        LoadingWindow.this.repaint();
                    });

                    try {
                        String folder = Objects.requireNonNull(new File("./update").list())[0];

                        for(String fileToMove : Objects.requireNonNull(new File("./update/" + folder).list()))
                            Files.move(Paths.get("./update/" + folder + "/" + fileToMove), Paths.get( "./update/" + fileToMove));
                        FileUtils.deleteFile(new File("./update/" + folder));
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }

                    Runtime.getRuntime().exec("java -jar starter.jar launcher.cfg");
                    System.exit(0);

                } catch (Exception e) {
                    try {
                        if (in != null)
                            in.close();
                    }catch (Exception ig){}
                    e.printStackTrace();
                }
            }else {
                starting = true;
                if(error)
                    setErrorText();
                else
                    setStartingText();

            }
        }).start();

        setVisible(true);
    }

    public Shape getWindowShape(){
        return ShapeUtils.createRoundRectangle(0, 0, getWidth() - 1, getHeight() - 1, 20, 20, ALL_CORNERS);
    }

    public boolean isOK(){
        if(!checkUpdates)
            return true;
        while(true){
            if(latestVersion != null)
                break;
            try {
                Thread.sleep(10);
            }catch (Exception ex){}
        }
        return latestVersion.equals(LauncherWindow.VERSION);
    }

    public void onError(){
        error = true;
        if(starting)
            setErrorText();
    }

    private void setStartingText(){
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Запуск...");
            LoadingWindow.this.repaint();
        });
    }

    private void setErrorText(){
        if(!System.getProperty("java.version").startsWith("1.8"))
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("<html><center>Ошибка при запуске!<br>Попробуйте установить Java 1.8</html>");
                LoadingWindow.this.repaint();
            });
        else
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("Ошибка при запуске!");
                LoadingWindow.this.repaint();
            });
    }

}
