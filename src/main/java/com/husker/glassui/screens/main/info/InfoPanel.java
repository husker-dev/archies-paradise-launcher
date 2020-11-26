package com.husker.glassui.screens.main.info;

import com.alee.extended.label.WebStyledLabel;
import com.alee.laf.label.WebLabel;
import com.husker.glassui.components.BlurButtonLineChooser;
import com.husker.glassui.components.BlurPanel;
import com.husker.glassui.components.TagPanel;

import com.husker.launcher.Launcher;
import com.husker.launcher.Resources;
import com.husker.launcher.api.API;
import com.husker.launcher.managers.NetManager;
import com.husker.launcher.settings.LauncherConfig;
import com.husker.launcher.social.Social;
import com.husker.launcher.ui.components.TransparentPanel;
import com.husker.launcher.managers.UpdateManager;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.glassui.GlassUI;
import com.husker.launcher.ui.utils.ComponentUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.Map;

import static java.awt.FlowLayout.CENTER;

public class InfoPanel extends TransparentPanel {

    private final Screen screen;

    private boolean inited = false;

    private TransparentPanel launcherPanel, clientPanel;

    private ServerInfoPanel serverInfo;
    private WebLabel versionLabel;
    private WebLabel buildVersionLabel;
    private final ModPanel[] modPanels = new ModPanel[5];

    public InfoPanel(Screen screen){
        this.screen = screen;

        setLayout(new BorderLayout());

        launcherPanel = new TransparentPanel(){{
            setLayout(new BorderLayout());
            setMargin(0, -10, 0, -10);
            add(new WebStyledLabel(WebStyledLabel.CENTER){{
                //setMargin(10, 0, 0, 0);
                setPreferredWidth(300);
                setPreferredHeight(70);
                setMaximumRows(30);
                setForeground(GlassUI.Colors.labelText);
                setFont(Resources.Fonts.ChronicaPro_ExtraBold.deriveFont(28f));
                setText(LauncherConfig.getTitle());
            }}, BorderLayout.NORTH);
            add(new BlurPanel(screen) {
                {
                    String repo = Social.GitHub.getRepository();

                    setLayout(new FlowLayout(CENTER));
                    setMargin(20, 20, 0, 20);
                    add(createInfoParameter("Версия", UpdateManager.VERSION));
                    add(createInfoParameter("Разработчик", "Штенгауэр Никита", "https://vk.com/shtengauer_nikita"));
                    add(createInfoParameter("Владелец", Social.About.getOwnerName(), Social.About.getOwnerUrl()));
                    add(createInfoParameter("GitHub",  repo.replace("/", "/ "), "https://github.com/" + repo));
                    add(createInfoParameter("Поддержка", Social.About.getSupportName(), Social.About.getSupportUrl()));
                }

                public void onBlurApply(BlurParameter parameter, Component component) {
                    super.onBlurApply(parameter, component);
                    parameter.setShadowType(BlurParameter.ShadowType.INNER);

                    if(parameter.getShape() != null) {
                        parameter.setShape(parameter.getShape().getBounds());
                        Rectangle shape = parameter.getShape().getBounds();

                        shape.x += 10;
                        shape.width -= 20;
                        parameter.setClip(shape);
                    }
                }
            });
        }};

        clientPanel = new TransparentPanel(){{
            setLayout(new BorderLayout());
            setMargin(10, 10, 0, 10);

            add(new TransparentPanel(){{
                setLayout(new BorderLayout());

                // Info
                add(new TagPanel(screen, "Информация"){{
                    addButtonAction(InfoPanel.this::updateData);
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
        }};

        add(launcherPanel);

        add(new TransparentPanel(){{
            setLayout(new GridBagLayout());
            setPreferredHeight(50);

            add(new BlurButtonLineChooser(screen){{
                addButton(" Лаунчер ");
                addButton(" Клиент ");
                addSelectedListener(index -> {
                    if(index == 0){
                        InfoPanel.this.add(launcherPanel);
                        InfoPanel.this.remove(clientPanel);
                    }
                    if(index == 1){
                        InfoPanel.this.remove(launcherPanel);
                        InfoPanel.this.add(clientPanel);
                        updateData();
                    }
                });
            }}, new GridBagConstraints(){{
                this.weightx = 1;
                this.weighty = 1;
            }});
        }}, BorderLayout.SOUTH);
    }

    public void onShow(){
        if(!inited)
            updateData();
        inited = true;
    }

    public void updateData(){
        new Thread(() -> {
            new Thread(serverInfo::updateInfo).start();

            try {
                versionLabel.setText(API.Client.getJarVersion());
                buildVersionLabel.setText(API.Client.getShortClientVersion());
            } catch (API.APIException e) {
                e.printStackTrace();
            }

            for(ModPanel panel : modPanels) {
                try {
                    panel.updateInfo();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    public WebLabel createLightText(String text){
        return new WebLabel(text){{
            setForeground(new Color(170, 170, 170));
            setHorizontalAlignment(LEFT);
            setPreferredHeight(16);
            setFont(Resources.Fonts.ChronicaPro_ExtraBold.deriveFont(16f));
        }};
    }

    public WebLabel createTitleLabel(String text){
        return new WebLabel(text){{
            setForeground(new Color(50, 50, 50));
            setHorizontalAlignment(CENTER);
            setPreferredHeight(28);
            setFont(Resources.Fonts.ChronicaPro_ExtraBold.deriveFont(18f));
        }};
    }

    public TransparentPanel createInfoParameter(String title, String value){
        return createInfoParameter(title, value, null);
    }

    public TransparentPanel createInfoParameter(String title, String value, String link){
        TransparentPanel panel = new TransparentPanel();
        panel.setPreferredWidth(135);
        panel.setLayout(new BorderLayout());

        WebLabel titleLabel = createTitleLabel(title);
        WebStyledLabel valueLabel = new WebStyledLabel(){
            {
                setHorizontalAlignment(CENTER);
                setVerticalAlignment(TOP);
                setForeground(GlassUI.Colors.labelLightText);
                setPreferredHeight(45);
                setFont(Resources.Fonts.ChronicaPro_ExtraBold.deriveFont(14f));
                setMaximumRows(2);
                setText(value);
            }

            boolean hovered = false;

            public void paint(Graphics g) {
                super.paint(g);

                if(link != null){

                    Point location = ComponentUtils.getComponentLocationOnScreen(screen.getLauncher(), panel);
                    Point mouse = screen.getLauncher().getContentPane().getMousePosition();
                    Rectangle shape = new Rectangle(location.x, location.y, panel.getWidth(), panel.getHeight());

                    if(mouse != null && shape.contains(mouse)){
                        if(!hovered) {
                            Font font = getFont();
                            Map attributes = font.getAttributes();
                            attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
                            setFont(font.deriveFont(attributes));
                            hovered = true;
                        }
                    }else{
                        if(hovered) {
                            Font font = getFont();
                            Map attributes = font.getAttributes();
                            attributes.put(TextAttribute.UNDERLINE, -1);
                            setFont(font.deriveFont(attributes));
                            hovered = false;
                        }
                    }
                }
            }
        };
        if(link != null) {
            MouseAdapter hoverAdapter = new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    valueLabel.repaint();
                }

                public void mouseExited(MouseEvent e) {
                    valueLabel.repaint();
                }
            };
            valueLabel.addMouseListener(hoverAdapter);
            titleLabel.addMouseListener(hoverAdapter);
            panel.addMouseListener(hoverAdapter);
        }
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        MouseAdapter linkAdapter = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                NetManager.openLink(link);
            }
        };

        if(link != null) {
            panel.addMouseListener(linkAdapter);
            titleLabel.addMouseListener(linkAdapter);
            valueLabel.addMouseListener(linkAdapter);
        }

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(valueLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)), BorderLayout.SOUTH);
        return panel;
    }

}
