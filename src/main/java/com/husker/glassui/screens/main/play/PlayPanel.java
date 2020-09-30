package com.husker.glassui.screens.main.play;

import com.alee.laf.label.WebLabel;
import com.husker.glassui.GlassUI;
import com.husker.glassui.components.BlurButton;
import com.husker.launcher.components.ProgressBar;
import com.husker.glassui.components.TagPanel;
import com.husker.launcher.components.TransparentPanel;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.utils.MinecraftStarter;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.text.DecimalFormat;

public class PlayPanel extends TransparentPanel {

    private WebLabel versionLabel;
    private WebLabel buildVersionLabel;
    private ServerInfoPanel serverInfo;
    private final Screen screen;
    private final ModPanel[] modPanels = new ModPanel[5];

    private TransparentPanel playPanel;
    private BlurButton playBtn;

    private TransparentPanel loadingPanel;
    private ProgressBar progressBar;

    private int lastState = -1;

    public PlayPanel(Screen screen){
        this.screen = screen;

        setLayout(new BorderLayout());
        setMargin(10, 10, 3, 10);

        add(new TransparentPanel(){{
            setLayout(new BorderLayout());

            // Info
            add(new TagPanel(screen, "Информация"){{
                addContent(GlassUI.createParameterLine("Версия", versionLabel = GlassUI.createParameterLineValueLabel(false)));
                addContent(GlassUI.createParameterLine("Номер сборки", buildVersionLabel = GlassUI.createParameterLineValueLabel(false)));
            }});

            // Card
            add(new TransparentPanel(){{
                setMargin(0, 20, 0, 0);
                add(serverInfo = new ServerInfoPanel(screen));
            }}, BorderLayout.EAST);
        }}, BorderLayout.NORTH);

        add(new TagPanel(screen, "Моды"){{
            setMargin(10, 0, 0, 0);
            setContentLayout(new BorderLayout());
            addContent(new TransparentPanel(){{
                setLayout(new GridBagLayout());
                for(int i = 0; i < modPanels.length; i++)
                    add(modPanels[i] = new ModPanel(screen, i), new GridBagConstraints(){{
                        this.weightx = 1;
                        this.weighty = 1;
                        this.fill = 1;
                        this.insets = new Insets(5, 5, 5, 5);
                    }});
            }});
        }});

        add(new TransparentPanel(){{
            setLayout(new OverlayLayout(this));
            setPreferredHeight(40);

            // Button
            add(playPanel = new TransparentPanel(){{
                setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
                add(playBtn = new BlurButton(screen, "Загрузка..."){{
                    setMargin(0, 50, 0, 50);
                    setEnabled(false);
                    addActionListener(e -> onPlayClick());
                }});
            }});

            // Progress
            add(loadingPanel = new TransparentPanel(){{
                setVisible(false);
                setVisible(false);
                setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
                add(progressBar = new ProgressBar(screen));
                progressBar.setPreferredWidth(500);
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
        screen.getLauncher().NetManager.Client.playOrDownload(args -> {
            int id = args.getProcessId();
            double percent = args.getPercent();

            if(id == -1) {
                screen.getLauncher().setVisible(true);
                setProcessVisible(false);
                updateData();
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
            }
            if(id == 2){
                progressBar.setText("Распаковка клиента...");
                progressBar.setValue(percent);
                progressBar.setSpeedText("");
                progressBar.setValueText((int)percent + "%");
            }
            if(id == 3){
                progressBar.setText("Перемещение...");
                progressBar.setValue(percent);
                progressBar.setSpeedText("");
                progressBar.setValueText((int)percent + "%");
            }
            if(id == 4){
                progressBar.setText("Удаление временных файлов...");
                progressBar.setValue(percent);
                progressBar.setSpeedText("");
                progressBar.setValueText((int)percent + "%");
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
            playBtn.setText("Загрузка...");
            playBtn.setEnabled(false);

            new Thread(serverInfo::updateInfo).start();

            lastState = screen.getLauncher().NetManager.Client.hasUpdate();
            if(lastState == screen.getLauncher().NetManager.Client.PLAY) {
                playBtn.setEnabled(true);
                playBtn.setIcon(new ImageIcon(screen.getLauncher().Resources.Icon_Play.getScaledInstance(27, 27, Image.SCALE_SMOOTH)));
                playBtn.setText("Играть");
            }
            if(lastState == screen.getLauncher().NetManager.Client.DOWNLOAD) {
                playBtn.setEnabled(true);
                playBtn.setIcon(new ImageIcon(screen.getLauncher().Resources.Icon_Download.getScaledInstance(27, 27, Image.SCALE_SMOOTH)));
                playBtn.setText("Скачать");
            }
            if(lastState == screen.getLauncher().NetManager.Client.UPDATE) {
                playBtn.setEnabled(true);
                playBtn.setIcon(new ImageIcon(screen.getLauncher().Resources.Icon_Download.getScaledInstance(27, 27, Image.SCALE_SMOOTH)));
                playBtn.setText("Обновить");
            }
            if(lastState == screen.getLauncher().NetManager.Client.ERROR){
                playBtn.setEnabled(false);
                playBtn.setIcon(new ImageIcon(screen.getLauncher().Resources.Icon_Dot_Selected.getScaledInstance(27, 27, Image.SCALE_SMOOTH)));
                playBtn.setText("Недоступно");
            }

            versionLabel.setText(screen.getLauncher().NetManager.Client.getJarVersion());
            buildVersionLabel.setText(screen.getLauncher().NetManager.Client.getShortClientVersion());

            for(ModPanel panel : modPanels) {
                try {
                    panel.updateInfo();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }).start();


    }
}
