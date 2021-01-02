package com.husker.glassui.screens.main.play;

import com.husker.glassui.components.BlurButton;

import com.husker.launcher.Resources;
import com.husker.launcher.api.API;
import com.husker.launcher.discord.Discord;
import com.husker.launcher.managers.LaunchManager;
import com.husker.launcher.settings.LauncherSettings;
import com.husker.launcher.ui.components.ProgressBar;
import com.husker.launcher.ui.components.TransparentPanel;
import com.husker.launcher.api.ApiMethod;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.utils.ImageUtils;
import org.bridj.Pointer;
import org.bridj.cpp.com.COMRuntime;
import org.bridj.cpp.com.shell.ITaskbarList3;
import org.bridj.jawt.JAWTUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.Arrays;

public class PlayPanel extends TransparentPanel {


    private final Screen screen;
    private final ScreenshotsPanel screenshots;

    private TransparentPanel playPanel;
    private BlurButton playBtn;

    private TransparentPanel loadingPanel;
    private ProgressBar progressBar;

    private final static String[] clients = {
            "vr",
            "non_vr"
    };
    private boolean vr = false;
    private BlurButton clientBtn;
    int btnSize = 40;

    public PlayPanel(Screen screen){
        this.screen = screen;

        setLayout(new BorderLayout());
        setMargin(10, 10, 0, 10);

        add(screenshots = new ScreenshotsPanel(screen));

        add(new TransparentPanel(){{
            setLayout(new OverlayLayout(this));
            setPreferredHeight(60);

            // Button
            add(playPanel = new TransparentPanel(){{
                setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
                setMargin(5, 0, 5, 0);

                add(new TransparentPanel(){{
                    setPreferredSize(btnSize, btnSize);
                }});
                add(playBtn = new BlurButton(screen, "Загрузка..."){{
                    setPreferredHeight(btnSize);
                    setPreferredWidth(250);
                    setEnabled(false);
                    setImageSize(27);
                    addActionListener(e -> onPlayClick());
                }});
                add(clientBtn = new BlurButton(screen, ""){{
                    setImageSize((int)(btnSize * 0.8));
                    setPreferredHeight(btnSize);
                    addActionListener(e -> setClient(!vr));
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
        if(Arrays.asList(clients).contains(LauncherSettings.getClientType()))
            setClient(LauncherSettings.getClientType().equals("vr"));
        else
            setClient(false);
    }

    public void onPlayClick(){
        updateStatus();
        if(!playBtn.isEnabled())
            return;
        setProcessVisible(true);
        LaunchManager.playOrDownload(clients[vr ? 0:1], screen.getLauncher().User, args -> {
            int id = args.getProcessId();
            double percent = args.getPercent();

            if(id == -1 || id == -2) {
                Discord.setState(Discord.Texts.InMainMenu);
                screen.getLauncher().setVisible(true);
                setProcessVisible(false);
                updateStatus();

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
                progressBar.setValue(100);
                progressBar.setSpeedText("");
                progressBar.setValueText("");
            }
            if(id == 6){
                Discord.setState(Discord.Texts.InGame);
                progressBar.setText("Запущено");
                progressBar.setValue(100);
                progressBar.setSpeedText("");
                progressBar.setValueText("");
                screen.getLauncher().setVisible(false);
            }
        });
    }

    public void setClient(boolean vr){
        this.vr = vr;
        LauncherSettings.setClientType(clients[vr ? 0:1]);
        double scale = 0.8;
        if(vr)
            clientBtn.setImage(Resources.Icon_VR);
        else
            clientBtn.setImage(Resources.Icon_VR_Disabled);
        updateStatus();
    }

    public void setProcessVisible(boolean visible){
        playPanel.setVisible(!visible);
        playBtn.setVisible(!visible);
        clientBtn.setVisible(!visible);
        loadingPanel.setVisible(visible);
        progressBar.setVisible(visible);
    }

    public void onShow(){
        updateScreenshots();
        updateStatus();
    }

    public void updateScreenshots(){
        new Thread(() -> {
            try {
                int count = API.getJSON(ApiMethod.create("screenshots.getCount")).getInt("count");
                String[] urls = new String[count];
                String[] urls_preview = new String[count];
                String[] urls_full = new String[count];
                for(int i = 0; i < count; i++) {
                    urls[i] = API.getMethodUrl(ApiMethod.create("screenshots.get").set("index", i));
                    urls_preview[i] = API.getMethodUrl(ApiMethod.create("screenshots.get").set("index", i).set("size", "small"));
                    urls_full[i] = API.getMethodUrl(ApiMethod.create("screenshots.get").set("index", i).set("size", "large"));
                }
                screenshots.setUrls(urls, urls_preview, urls_full);
            } catch (API.InternalAPIException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void updateStatus(){
        new Thread(() -> {
            playBtn.setText("Загрузка...");
            playBtn.setEnabled(false);

            LaunchManager.ClientState lastState = LaunchManager.getClientState(clients[vr ? 0:1]);

            playBtn.setEnabled(lastState != LaunchManager.ClientState.ERROR && lastState != LaunchManager.ClientState.UPDATING);
            switch (lastState){
                case PLAY:
                    playBtn.setImage(Resources.Icon_Play);
                    playBtn.setText("Играть");
                    break;
                case UPDATING:
                    playBtn.setImage(Resources.Icon_Dot_Selected);
                    playBtn.setText("Обновляется");
                    break;
                case DOWNLOAD:
                    playBtn.setImage(Resources.Icon_Download);
                    playBtn.setText("Скачать");
                    break;
                case UPDATE:
                    playBtn.setImage(Resources.Icon_Download);
                    playBtn.setText("Обновить");
                    break;
                case ERROR:
                    playBtn.setImage(Resources.Icon_Dot_Selected);
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
