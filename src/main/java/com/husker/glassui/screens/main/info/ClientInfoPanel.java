package com.husker.glassui.screens.main.info;

import com.husker.glassui.GlassUI;
import com.husker.glassui.components.BlurTagPanel;
import com.husker.launcher.Resources;
import com.husker.launcher.api.API;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.components.MLabel;
import com.husker.launcher.ui.components.TransparentPanel;

import java.awt.*;

public class ClientInfoPanel extends TransparentPanel {

    private ServerInfoPanel serverInfo;
    private MLabel versionLabel;
    private MLabel buildVersionLabel;
    private final ModPanel[] modPanels = new ModPanel[5];

    private final String clientId;

    public ClientInfoPanel(Screen screen, String clientId){
        this.clientId = clientId;
        setLayout(new BorderLayout());
        setMargin(10, 10, 0, 10);

        add(new TransparentPanel(){{
            setLayout(new BorderLayout());

            // Info
            add(new BlurTagPanel(screen, "Информация"){{
                addButtonAction(ClientInfoPanel.this::updateData);
                setButtonIcons(Resources.Icon_Reload, Resources.Icon_Reload_Selected);
                addContent(GlassUI.createParameterLine("Версия", versionLabel = GlassUI.createParameterLineValueLabel(false)));
                addContent(GlassUI.createParameterLine("Номер сборки", buildVersionLabel = GlassUI.createParameterLineValueLabel(false)));
            }});

            // Card
            add(new TransparentPanel(){{
                setMargin(0, 20, 0, 0);
                add(serverInfo = new ServerInfoPanel(screen));
            }}, BorderLayout.EAST);
        }}, BorderLayout.NORTH);

        add(new BlurTagPanel(screen, "Моды"){{
            setMargin(10, 0, 0, 0);
            setContentLayout(new BorderLayout());
            addContent(new TransparentPanel(){{
                setLayout(new GridBagLayout());
                for(int i = 0; i < modPanels.length; i++)
                    add(modPanels[i] = new ModPanel(screen, clientId, i), new GridBagConstraints(){{
                        this.weightx = 1;
                        this.weighty = 1;
                        this.fill = 1;
                        this.insets = new Insets(5, 5, 5, 5);
                    }});
            }});
        }});
    }

    public void updateData(){
        new Thread(() -> {
            new Thread(serverInfo::updateInfo).start();

            try {
                versionLabel.setText(API.Client.getJarVersion(clientId));
                buildVersionLabel.setText(API.Client.getShortClientVersion(clientId) + "");
            } catch (API.InternalAPIException | API.UnknownClientException | API.ClientIsUpdatingException e) {
                e.printStackTrace();
            }

            int count = 0;
            try{
                count = API.Client.getModsCount(clientId);
            }catch (Exception ex){}

            for(ModPanel panel : modPanels) {
                try {
                    panel.updateInfo(count);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }).start();
    }
}
