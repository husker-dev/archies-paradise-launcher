package com.husker.launcher.updater;

import com.husker.mio.FSUtils;
import com.husker.mio.ProgressArguments;
import com.husker.mio.processes.CopyingProcess;
import com.husker.mio.processes.DeletingProcess;
import com.vnetpublishing.java.suapp.SU;
import com.vnetpublishing.java.suapp.SuperUserApplication;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

public class Main extends SuperUserApplication {

    public static boolean safeMode = false;
    public static String updateFolder;
    public static String launcherFolder;

    private static final JLabel[] texts = new JLabel[5];
    private static JProgressBar progress;
    private static boolean isWorking = true;

    private static boolean isAdmin = true;

    public static void main(String[] args){
        Main application = new Main();

        SU.setDaemon(true);
        int result = -1;
        try {
            result = SU.run(application, args);
        }catch (Exception ignored){}

        if(result != 0) {
            isAdmin = false;
            application.run(args);
        }
    }

    public int run(String[] args) {
        try {
            updateFolder = getArgumentValue(args, "folder", new File("update_launcher").getAbsolutePath());
            safeMode = getArgumentValue(args, "safe", "false").equals("true");
            launcherFolder = getArgumentValue(args, "launcher", new File("./").getAbsolutePath());

            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            JFrame frame = new JFrame();
            frame.setTitle("Обновление (" + (isAdmin ? "Admin" : "No admin") + ")");
            frame.setSize(380, 160);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setResizable(false);

            frame.setVisible(true);

            frame.getContentPane().setLayout(new BorderLayout());
            frame.getContentPane().add(new JPanel() {{
                setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                setLayout(new BorderLayout());

                add(new JPanel() {{
                    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                    setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
                    for (int i = 0; i < texts.length; i++) {
                        final int I = i;
                        add(texts[i] = new JLabel(" ") {{
                            setFont(getFont().deriveFont(13f));
                            setForeground(new Color(0, 0, 0, (int) (255d / (double) texts.length * (double) (I + 1))));
                        }});
                    }
                }});

                add(progress = new JProgressBar() {{
                    setMaximum(100);
                    setValue(0);
                }}, BorderLayout.SOUTH);
            }});

            new Thread(() -> {
                doEvilThings();
                isWorking = false;
                System.exit(0);
            }).start();

            while (isWorking) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }catch (Exception ex){
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);

            JOptionPane.showMessageDialog(null, sw, "Ошибка", JOptionPane.INFORMATION_MESSAGE);
            ex.printStackTrace();
            isWorking = false;
            System.exit(0);
        }
        return 0;
    }

    public static void log(String text){
        for(int i = 0; i < texts.length - 1; i++)
            texts[i].setText(texts[i + 1].getText());
        texts[texts.length - 1].setText(text);
    }

    public static String getArgumentValue(String[] args, String variable, String defaultValue){
        for(String arg : args)
            if (arg.startsWith("--" + variable + "="))
                return arg.replace("--" + variable + "=", "");
        return defaultValue;
    }

    public static void doEvilThings(){
        try {
            File[] toDelete = Objects.requireNonNull(new File(launcherFolder).listFiles(file ->
                    !file.getName().equals("updater.jar") &&
                            !file.getName().equals(new File(updateFolder).getName()) &&
                            !file.getName().equals("clients")
            ));
            File[] toMove = FSUtils.getChildren(new File(updateFolder)).toArray(new File[0]);

            for (int i = 0; i < toDelete.length; i++) {
                final int I = i;
                delete(toDelete[i], event -> {
                    double part = 300d / toDelete.length;
                    progress.setValue((int) (part * I + (event.getPercent() / 100 * part)));
                });
            }

            for (int i = 0; i < toMove.length; i++) {
                final int I = i;
                if(toMove[i].getName().equals("updater.jar"))
                    continue;
                move(toMove[i], event -> {
                    double part = 300d / toMove.length;
                    progress.setValue((int) ((100d / 3d) + part * I + (event.getPercent() / 100d * part)));
                });
            }

            delete(new File(updateFolder), event -> progress.setValue((int) ((100d / 3d * 2) + event.getPercent() / 3)));

            log("Запуск...");

            try {
                Runtime.getRuntime().exec(new File("launcher.exe").getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }catch (Exception ex){
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);

            JOptionPane.showMessageDialog(null, sw, "Ошибка", JOptionPane.INFORMATION_MESSAGE);
            ex.printStackTrace();
            isWorking = false;
            System.exit(0);
        }
    }

    public static String getExecutorName(){
        return new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName();
    }

    public static void delete(File file, Consumer<ProgressArguments<DeletingProcess>> progress){
        log("Удаление: " + file.getName() + "...");
        if(safeMode) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {}
            return;
        }
        try {
            new DeletingProcess(file).addProgressListener(progress).startSync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void move(File file, Consumer<ProgressArguments<CopyingProcess>> progress){
        log("Перемещение: " + file.getParentFile().getName() + File.separator + file.getName() + "...");
        if(safeMode) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {}
            return;
        }
        try {
            new CopyingProcess(file, new File(launcherFolder)).addProgressListener(progress).startSync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
