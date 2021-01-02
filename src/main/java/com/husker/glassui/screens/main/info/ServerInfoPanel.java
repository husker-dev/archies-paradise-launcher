package com.husker.glassui.screens.main.info;

import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.laf.label.WebLabel;
import com.husker.glassui.GlassUI;
import com.husker.glassui.components.BlurPanel;
import com.husker.launcher.Launcher;
import com.husker.launcher.Resources;
import com.husker.launcher.api.API;
import com.husker.launcher.settings.LauncherConfig;
import com.husker.launcher.ui.components.MLabel;
import com.husker.launcher.ui.components.TransparentPanel;
import com.husker.launcher.managers.NetManager;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.ui.utils.UIUtils;
import org.json.JSONObject;

import java.awt.*;
import java.io.IOException;

public class ServerInfoPanel extends BlurPanel {

    private final MLabel title;
    private MLabel status;
    private MLabel maxPlayers, players;
    private MLabel ping;

    public ServerInfoPanel(Screen screen) {
        super(screen, true);
        setLayout(new BorderLayout( 0, 0));

        add(title = GlassUI.createSimpleLabel("Сервер", true), BorderLayout.NORTH);
        title.setFont(title.getFont().deriveFont(18f));
        title.setPreferredHeight(25);
        add(new BlurPanel(screen){
            {
                setMargin(10);
                setLayout(new VerticalFlowLayout(0, 3));

                add(createParameterLine("Статус", status = createParameterLabel()));
                add(createParameterLine("Игроки", new TransparentPanel(){{
                    setLayout(new FlowLayout(FlowLayout.CENTER, 3, 0));
                    add(players = createParameterLabel("0"));
                    players.setForeground(GlassUI.Colors.labelText);
                    add(createParameterLabel("/"));
                    add(maxPlayers = createParameterLabel("0"));
                }}));
                add(createParameterLine("Пинг", ping = createParameterLabel()));
            }
            public void onBlurApply(BlurParameter parameter, Component component) {
                super.onBlurApply(parameter, component);
                if(returnOnInvisible(parameter, component))
                    return;
                if(component == this){
                    GlassUI.applyTopLayer(parameter);
                    parameter.setAdditionColor(GlassUI.Colors.buttonDefault);
                    parameter.setShadowSize(5);
                    parameter.setShadowClip(UIUtils.keepShadow(parameter, 10, UIUtils.ShadowSide.TOP));
                }
            }
        });

        setPreferredWidth(220);
    }

    public void updateInfo(){
        Color red = new Color(160, 0, 0);
        Color yellow = new Color(160, 160, 0);
        Color green = new Color(0, 160, 0);

        try {
            status.setText("Обновление");
            status.setForeground(yellow);
            ping.setText("...");

            API.Minecraft.ServerInfo info = API.Minecraft.getServerInfo();
            JSONObject object = NetManager.MinecraftServer.info(info.getIP(), info.getPort());

            status.setText("Онлайн");
            status.setForeground(green);
            players.setText(object.getJSONObject("players").getInt("online") + "");
            maxPlayers.setText(object.getJSONObject("players").getInt("max") + "");
            ping.setText(object.getLong("ping")  + " мс");
        } catch (IOException | API.InternalAPIException e) {
            status.setForeground(red);
            status.setText("Оффлайн");
            players.setText("");
            maxPlayers.setText("");
            ping.setText("0");
        }
    }

    public Component createParameterLine(String name, Component parameter){
        return new TransparentPanel(){
            {
                setLayout(new BorderLayout());

                MLabel nameLabel = GlassUI.createSimpleLabel(name + ":");

                add(nameLabel, BorderLayout.WEST);
                add(parameter, BorderLayout.EAST);
            }

            public void paint(Graphics gr) {
                super.paint(gr);

                gr.setColor(new Color(240, 240, 240));
                gr.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            }
        };
    }

    public MLabel createParameterLabel(){
        return createParameterLabel(" ");
    }

    public MLabel createParameterLabel(String text){
        return new MLabel(text){{
            setForeground(GlassUI.Colors.labelLightText);
            setFont(Resources.Fonts.getChronicaProExtraBold());
            setHorizontalAlignment(RIGHT);
        }};
    }

    public void onBlurApply(BlurParameter parameter, Component component) {
        super.onBlurApply(parameter, component);
        if(returnOnInvisible(parameter, component))
            return;
        if(component == this){
            GlassUI.applyTopLayer(parameter);
            parameter.setShadowSize(5);
            parameter.setAdditionColor(GlassUI.Colors.third);
        }
    }
}
