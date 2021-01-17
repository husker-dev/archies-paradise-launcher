package com.husker.glassui.screens.main.play;

import com.husker.glassui.components.BlurButton;

import com.husker.launcher.Resources;
import com.husker.launcher.api.API;
import com.husker.launcher.discord.Discord;
import com.husker.launcher.minecraft.LaunchManager;
import com.husker.launcher.settings.LauncherSettings;
import com.husker.launcher.ui.components.MLabel;
import com.husker.launcher.ui.components.ProgressBar;
import com.husker.launcher.ui.components.TransparentPanel;
import com.husker.launcher.api.ApiMethod;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.utils.SystemUtils;
import com.husker.mio.ProgressArguments;
import com.husker.mio.processes.CopyingProcess;
import com.husker.mio.processes.DeletingProcess;
import com.husker.mio.processes.DownloadingProcess;
import com.husker.mio.processes.UnzippingProcess;
import org.bridj.Pointer;
import org.bridj.cpp.com.COMRuntime;
import org.bridj.cpp.com.shell.ITaskbarList3;
import org.bridj.jawt.JAWTUtils;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class PlayPanel extends TransparentPanel {

    public static class ButtonStates{
        public static final String Loading = "Загрузка...";
        public static final String Play = "Играть";
        public static final String Update = "Обновить";
        public static final String Download = "Скачать";
        public static final String Updating = "Обновляется...";
        public static final String Unavailable = "Недоступно";
    }

    private final Screen screen;
    private final ScreenshotsPanel screenshots;

    private TransparentPanel playPanel;
    private BlurButton playBtn;
    private MLabel vrMode;

    private TransparentPanel loadingPanel;
    private ProgressBar progressBar;

    private final int btnSize = 40;

    public PlayPanel(Screen screen){
        this.screen = screen;

        // Auto status updating
        new Timer().schedule(new TimerTask() {
            public void run() {
                if(!screen.getLauncher().isVisible())
                    return;

                if(playBtn.getText().equals(ButtonStates.Updating) || playBtn.getText().equals(ButtonStates.Unavailable))
                    updateStatus(true);
            }
        }, 0, 3000);

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

                add(vrMode = new MLabel(){{
                    setPreferredSize(btnSize, btnSize);
                    setImageSize((int)(btnSize * 0.8));
                }});
                add(playBtn = new BlurButton(screen, "Загрузка..."){{
                    setPreferredHeight(btnSize);
                    setPreferredWidth(250);
                    setEnabled(false);
                    setImageSize(27);
                    addActionListener(e -> onPlayClick());
                }});
                add(new BlurButton(screen, Resources.Icon_Subject){{
                    setImageSize((int)(btnSize * 0.8));
                    setPreferredHeight(btnSize);
                    addActionListener(e -> screen.getLauncherUI().setScreen(ClientSettings.class));
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
        updateStatus(false);
        if(!playBtn.isEnabled())
            return;
        setProcessVisible(true);
        LaunchManager.playOrDownload(LauncherSettings.getClientType(), screen.getLauncher().User, new LaunchManager.LaunchListener() {
            private void reset(){
                Discord.setState(Discord.Texts.InMainMenu);
                screen.getLauncher().setVisible(true);
                setProcessVisible(false);
                updateStatus(false);

                applyEmptyProgress("Загрузка...", 0);
            }

            public void onConfigSave() {
                applyEmptyProgress("Сохранение настроек...", 0);
            }

            public void onConfigApply() {
                applyEmptyProgress("Применение настроек...", 100);
            }

            public void onError() {
                reset();
            }

            public void onRemoveOld(ProgressArguments<DeletingProcess> event) {
                applyDeletingProgress("Удаление старых файлов...", event);
            }

            public void onVersionsDownloading(ProgressArguments<DownloadingProcess> event) {
                applyDownloadingProgress("Скачивание версий...", event);
            }

            public void onModsDownloading(ProgressArguments<DownloadingProcess> event) {
                applyDownloadingProgress("Скачивание модов...", event);
            }

            public void onOtherDownloading(ProgressArguments<DownloadingProcess> event) {
                applyDownloadingProgress("Скачивание клиента...", event);
            }

            public void onModsUnzipping(ProgressArguments<UnzippingProcess> event) {
                applyUnzippingProgress("Распаковка модов...", event);
            }

            public void onModsZipRemoving(ProgressArguments<DeletingProcess> event) {
                applyDeletingProgress("Удаление архива с модами...", event);
            }

            public void onVersionsUnzipping(ProgressArguments<UnzippingProcess> event) {
                applyUnzippingProgress("Распаковка версий...", event);
            }

            public void onVersionsZipRemoving(ProgressArguments<DeletingProcess> event) {
                applyDeletingProgress("Удаление архива с версиями...", event);
            }

            public void onOtherUnzipping(ProgressArguments<UnzippingProcess> event) {
                applyUnzippingProgress("Распаковка клиента...", event);
            }

            public void onOtherZipRemoving(ProgressArguments<DeletingProcess> event) {
                applyDeletingProgress("Удаление архива с клиентом...", event);
            }

            public void onOldCopying(ProgressArguments<CopyingProcess> event) {
                applyCopyingProgress("Перемещение клиента в новую папку...", event);
            }

            public void onClientUpdated() {
                reset();
            }

            public void onClientChecking() {
                applyEmptyProgress("Проверка клиента...", 50);
            }

            public void onClientStarting() {
                applyEmptyProgress("Запуск...", 100);
            }

            public void onClientStarted() {
                applyEmptyProgress("Запущен", 100);
                Discord.setState(Discord.Texts.InGame);
                screen.getLauncher().setVisible(false);
            }

            public void onClientClosed() {
                reset();
            }

            public void onModsRemoving(ProgressArguments<DeletingProcess> event) {
                applyDeletingProgress("Удаление модов...", event);
            }

            public void onVersionsRemoving(ProgressArguments<DeletingProcess> event) {
                applyDeletingProgress("Удаление версий...", event);
            }
        });
    }

    public void applyEmptyProgress(String text, int percent){
        progressBar.setText(text);
        progressBar.setValue(percent);
        progressBar.setSpeedText("");
        progressBar.setValueText("");
        TaskBar.setProgress(screen.getLauncher(), -1);
    }

    public void applyDeletingProgress(String text, ProgressArguments<DeletingProcess> arguments){
        progressBar.setText(text);
        progressBar.setValue(arguments.getPercent());
        progressBar.setSpeedText("");
        progressBar.setValueText((int) arguments.getPercent() + "%");
        TaskBar.setProgress(screen.getLauncher(), (int) arguments.getPercent());
    }

    public void applyDownloadingProgress(String text, ProgressArguments<DownloadingProcess> arguments){
        progressBar.setText(text);
        progressBar.setValue(arguments.getPercent());

        int current = (int) (arguments.getCurrentSize() / 1000000d);
        int full = (int) (arguments.getFullSize() / 1000000d);
        progressBar.setSpeedText(current + "/" + full + " Мб");
        progressBar.setValueText(SystemUtils.formatSpeed(arguments.getSpeed()));
        TaskBar.setProgress(screen.getLauncher(), (int) arguments.getPercent());
    }

    public void applyUnzippingProgress(String text, ProgressArguments<UnzippingProcess> arguments){
        progressBar.setText(text);
        progressBar.setValue(arguments.getPercent());
        progressBar.setSpeedText("");
        progressBar.setValueText((int) arguments.getPercent() + "%");
        TaskBar.setProgress(screen.getLauncher(), (int) arguments.getPercent());
    }

    public void applyCopyingProgress(String text, ProgressArguments<CopyingProcess> arguments){
        progressBar.setText(text);
        progressBar.setValue(arguments.getPercent());
        progressBar.setSpeedText("");
        progressBar.setValueText((int) arguments.getPercent() + "%");
        TaskBar.setProgress(screen.getLauncher(), (int) arguments.getPercent());
    }


    public void setProcessVisible(boolean visible){
        playPanel.setVisible(!visible);
        playBtn.setVisible(!visible);
        loadingPanel.setVisible(visible);
        progressBar.setVisible(visible);
    }

    public void onShow(){
        updateScreenshots();
        updateStatus(false);
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

    public void updateStatus(boolean silent){
        new Thread(() -> {
            if(!silent) {
                playBtn.setText(ButtonStates.Loading);
                playBtn.setEnabled(false);
            }
            LaunchManager.ClientState state = LaunchManager.getClientState(LauncherSettings.getClientType());

            vrMode.setImage(LauncherSettings.getClientType().equals("vr") ? Resources.Icon_VR : null);
            playBtn.setEnabled(state != LaunchManager.ClientState.ERROR && state != LaunchManager.ClientState.UPDATING);
            switch (state){
                case PLAY:
                    playBtn.setImage(Resources.Icon_Play);
                    playBtn.setText(ButtonStates.Play);
                    break;
                case UPDATING:
                    playBtn.setImage(null);
                    playBtn.setText(ButtonStates.Updating);
                    break;
                case DOWNLOAD:
                    playBtn.setImage(Resources.Icon_Download);
                    playBtn.setText(ButtonStates.Download);
                    break;
                case UPDATE:
                    playBtn.setImage(Resources.Icon_Download);
                    playBtn.setText(ButtonStates.Update);
                    break;
                case ERROR:
                    playBtn.setImage(Resources.Icon_Dot_Selected);
                    playBtn.setText(ButtonStates.Unavailable);
                    break;
            }
        }).start();
    }

    public static class TaskBar {
        static ITaskbarList3 list;
        static Pointer<?> hwnd;
        static int lastVal = -1;
        static {
            if(SystemUtils.isWindows()) {
                try {
                    list = COMRuntime.newInstance(ITaskbarList3.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public static void setProgress(JFrame frame, int progress){
            if(!SystemUtils.isWindows())
                return;
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
