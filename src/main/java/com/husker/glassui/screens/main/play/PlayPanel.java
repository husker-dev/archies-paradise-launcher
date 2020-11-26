package com.husker.glassui.screens.main.play;

import com.husker.glassui.components.BlurButton;

import com.husker.glassui.components.BlurScalableImage;
import com.husker.launcher.Resources;
import com.husker.launcher.api.API;
import com.husker.launcher.managers.LaunchManager;
import com.husker.launcher.ui.components.ProgressBar;
import com.husker.launcher.ui.components.TransparentPanel;
import com.husker.launcher.api.ApiMethod;
import com.husker.launcher.ui.Screen;
import org.bridj.Pointer;
import org.bridj.cpp.com.COMRuntime;
import org.bridj.cpp.com.shell.ITaskbarList3;
import org.bridj.jawt.JAWTUtils;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.text.DecimalFormat;

public class PlayPanel extends TransparentPanel {


    private final Screen screen;
    private BlurScalableImage screenshotImage;

    private TransparentPanel playPanel;
    private BlurButton playBtn;

    private TransparentPanel loadingPanel;
    private ProgressBar progressBar;

    public PlayPanel(Screen screen){
        this.screen = screen;

        setLayout(new BorderLayout());
        setMargin(10, 10, 0, 10);

        add(new TransparentPanel(){{
            setLayout(new BorderLayout());
            add(screenshotImage = new BlurScalableImage(screen){{
                setLayout(new OverlayLayout(this));
                add(new TransparentPanel(){{
                    setLayout(new BorderLayout());
                    add(new JPanel(){{
                        setPreferredWidth(50);
                        setBackground(Color.BLUE);
                    }}, BorderLayout.WEST);
                    add(new JPanel(){{
                        setPreferredWidth(50);
                        setBackground(Color.BLUE);
                    }}, BorderLayout.EAST);
                }}, 0);
                add(new TransparentPanel(){{

                }}, 1);
            }});
        }});

        add(new TransparentPanel(){{
            setLayout(new OverlayLayout(this));
            setPreferredHeight(60);

            // Button
            add(playPanel = new TransparentPanel(){{
                setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
                setMargin(5, 0, 5, 0);

                add(playBtn = new BlurButton(screen, "Загрузка..."){{
                    setPreferredHeight(40);
                    setMargin(0, 90, 0, 90);
                    setEnabled(false);
                    addActionListener(e -> onPlayClick());
                }});
            }});

            // Progress
            add(loadingPanel = new TransparentPanel(){{
                setMargin(10, 5, 10, 5);

                add(progressBar = new ProgressBar());
                progressBar.setPreferredSize(new Dimension(500, 100));
            }});
            setMargin(5, 0, 0, 0);
        }}, BorderLayout.SOUTH);
        setProcessVisible(false);
    }

    public void onPlayClick(){
        updateData();
        if(!playBtn.isEnabled())
            return;
        setProcessVisible(true);
        LaunchManager.playOrDownload(screen.getLauncher().User, args -> {
            int id = args.getProcessId();
            double percent = args.getPercent();

            if(id == -1 || id == -2) {
                screen.getLauncher().setVisible(true);
                setProcessVisible(false);
                updateData();

                TaskBar.setProgress(screen.getLauncher(), -1);
            }
            if(id == 0){
                progressBar.setText("Удаление...");
                progressBar.setValue(percent);
                progressBar.setSpeedText("");
                progressBar.setValueText("");
            }
            if(id == 1){
                progressBar.setText("Скачивание...");
                progressBar.setValue(percent);

                int current = (int)(args.getCurrentSize() / 1000000d);
                int full = (int)(args.getFullSize() / 1000000d);
                progressBar.setSpeedText(current + "/" + full + " Мб");
                progressBar.setValueText(new DecimalFormat("#0").format(args.getSpeed()) + " Мб/сек");

                TaskBar.setProgress(screen.getLauncher(), (int)percent);
            }
            if(id == 2){
                progressBar.setText("Распаковка клиента...");
                progressBar.setValue(percent);
                progressBar.setSpeedText("");
                progressBar.setValueText((int)percent + "%");

                TaskBar.setProgress(screen.getLauncher(), (int)percent);
            }
            if(id == 3){
                progressBar.setText("Перемещение...");
                progressBar.setValue(percent);
                progressBar.setSpeedText("");
                progressBar.setValueText((int)percent + "%");

                TaskBar.setProgress(screen.getLauncher(), (int)percent);
            }
            if(id == 4){
                progressBar.setText("Удаление временных файлов...");
                progressBar.setValue(percent);
                progressBar.setSpeedText("");
                progressBar.setValueText((int)percent + "%");

                TaskBar.setProgress(screen.getLauncher(), (int)percent);
            }
            if(id == 5){
                progressBar.setText("Запуск...");
                progressBar.setValue(0);
                progressBar.setSpeedText("");
                progressBar.setValueText("");
            }
            if(id == 6){
                progressBar.setText("Запущено");
                progressBar.setValue(100);
                progressBar.setSpeedText("");
                progressBar.setValueText("");
                screen.getLauncher().setVisible(false);
            }
        });
    }

    public void setProcessVisible(boolean visible){
        playPanel.setVisible(!visible);
        playBtn.setVisible(!visible);
        loadingPanel.setVisible(visible);
        progressBar.setVisible(visible);
    }

    public void onShow(){
        updateData();
    }

    public void updateData(){
        new Thread(() -> {
            try {
                screenshotImage.setImage(API.getImage(ApiMethod.create("screenshots.get").set("index", 0)));
            } catch (API.APIException e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            playBtn.setText("Загрузка...");
            playBtn.setEnabled(false);


            LaunchManager.ClientState lastState = LaunchManager.getClientState();
            playBtn.setEnabled(lastState != LaunchManager.ClientState.ERROR);

            switch (lastState){
                case PLAY:
                    playBtn.setIcon(new ImageIcon(Resources.Icon_Play.getScaledInstance(27, 27, Image.SCALE_SMOOTH)));
                    playBtn.setText("Играть");
                    break;
                case DOWNLOAD:
                    playBtn.setIcon(new ImageIcon(Resources.Icon_Download.getScaledInstance(27, 27, Image.SCALE_SMOOTH)));
                    playBtn.setText("Скачать");
                    break;
                case UPDATE:
                    playBtn.setIcon(new ImageIcon(Resources.Icon_Download.getScaledInstance(27, 27, Image.SCALE_SMOOTH)));
                    playBtn.setText("Обновить");
                    break;
                case ERROR:
                    playBtn.setIcon(new ImageIcon(Resources.Icon_Dot_Selected.getScaledInstance(27, 27, Image.SCALE_SMOOTH)));
                    playBtn.setText("Недоступно");
                    break;
            }
        }).start();
    }

    public static class TaskBar {
        static ITaskbarList3 list;
        static Pointer<?> hwnd;
        static int lastVal = -1;
        static {
            try {
                list = COMRuntime.newInstance(ITaskbarList3.class);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        public static void setProgress(JFrame frame, int progress){
            if(progress == lastVal)
                return;
            lastVal = progress;

            new Thread(() -> {
                if(hwnd == null)
                    hwnd = Pointer.pointerToAddress(JAWTUtils.getNativePeerHandle(frame));
                list.SetProgressValue((Pointer)hwnd, progress, 100);
                if(progress == -1)
                    list.SetProgressState((Pointer)hwnd, ITaskbarList3.TbpFlag.TBPF_NOPROGRESS);
                else
                    list.SetProgressState((Pointer)hwnd, ITaskbarList3.TbpFlag.TBPF_NORMAL);
            }).start();
        }
    }
}
