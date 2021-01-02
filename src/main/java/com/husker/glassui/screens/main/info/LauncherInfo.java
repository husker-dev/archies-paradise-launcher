package com.husker.glassui.screens.main.info;

import com.alee.extended.label.WebStyledLabel;
import com.husker.glassui.GlassUI;
import com.husker.glassui.components.BlurPanel;
import com.husker.launcher.Launcher;
import com.husker.launcher.Resources;
import com.husker.launcher.managers.NetManager;
import com.husker.launcher.settings.LauncherConfig;
import com.husker.launcher.social.Social;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.ui.components.MLabel;
import com.husker.launcher.ui.components.TransparentPanel;
import com.husker.launcher.ui.utils.ComponentUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.Map;

import static java.awt.FlowLayout.CENTER;

public class LauncherInfo extends TransparentPanel {

    private Screen screen;
    private InfoParameter versionInfo, developerInfo, ownerInfo, gitHubInfo, supportInfo;

    public LauncherInfo(Screen screen){
        this.screen = screen;
        setLayout(new BorderLayout());
        setMargin(0, -10, 0, -10);
        add(new WebStyledLabel(WebStyledLabel.CENTER){{
            setPreferredWidth(300);
            setPreferredHeight(70);
            setMaximumRows(30);
            setForeground(GlassUI.Colors.labelText);
            setFont(Resources.Fonts.getChronicaProExtraBold(28));
            setText(LauncherConfig.getTitle());
        }}, BorderLayout.NORTH);
        add(new BlurPanel(screen) {
            {
                String repo = Social.GitHub.getRepository();

                setLayout(new FlowLayout(CENTER));
                setMargin(20, 20, 0, 20);

                add(versionInfo = new InfoParameter("Версия", Launcher.VERSION));
                add(developerInfo = new InfoParameter("Разработчик", "Штенгауэр Никита", "https://vk.com/shtengauer_nikita"));
                add(ownerInfo = new InfoParameter("Владелец"));
                add(gitHubInfo = new InfoParameter("GitHub"));
                add(supportInfo = new InfoParameter("Поддержка"));

                updateData();
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
    }

    public void updateData(){
        new Thread(() -> {
            String repo = Social.GitHub.getRepository();

            ownerInfo.setValue(Social.About.getOwnerName(), Social.About.getOwnerUrl());
            gitHubInfo.setValue(repo == null ? "" : repo.replace("/", "/ "), repo == null ? "" : "https://github.com/" + repo);
            supportInfo.setValue(Social.About.getSupportName(), Social.About.getSupportUrl());
        }).start();
    }

    public MLabel createLightText(String text){
        return new MLabel(text){{
            setForeground(new Color(170, 170, 170));
            setHorizontalAlignment(LEFT);
            setPreferredHeight(16);
            setFont(Resources.Fonts.getChronicaProExtraBold());
        }};
    }

    public MLabel createTitleLabel(String text){
        return new MLabel(text){{
            setForeground(new Color(50, 50, 50));
            setHorizontalAlignment(CENTER);
            setPreferredHeight(28);
            setFont(Resources.Fonts.getChronicaProExtraBold(18));
        }};
    }

    class InfoParameter extends TransparentPanel{

        private String link = "";
        private WebStyledLabel valueLabel;

        public InfoParameter(String title){
            this(title, "", "");
        }

        public InfoParameter(String title, String value){
            this(title, value, "");
        }

        public InfoParameter(String title, String value, String link){
            this.link = link;
            setPreferredWidth(135);
            setLayout(new BorderLayout());

            MLabel titleLabel = createTitleLabel(title);
            valueLabel = new WebStyledLabel(){
                {
                    setHorizontalAlignment(CENTER);
                    setVerticalAlignment(TOP);
                    setForeground(GlassUI.Colors.labelLightText);
                    setPreferredHeight(45);
                    setFont(Resources.Fonts.getChronicaProExtraBold(14));
                    setMaximumRows(2);
                    setText(value);
                }

                boolean hovered = false;

                public void paint(Graphics g) {
                    super.paint(g);

                    if(link != null){

                        Point location = ComponentUtils.getComponentLocationOnScreen(screen.getLauncher(), InfoParameter.this);
                        Point mouse = screen.getLauncher().getContentPane().getMousePosition();
                        Rectangle shape = new Rectangle(location.x, location.y, InfoParameter.this.getWidth(), InfoParameter.this.getHeight());

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
                addMouseListener(hoverAdapter);
            }
            valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

            MouseAdapter linkAdapter = new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    NetManager.openLink(InfoParameter.this.link);
                }
            };

            if(link != null) {
                addMouseListener(linkAdapter);
                titleLabel.addMouseListener(linkAdapter);
                valueLabel.addMouseListener(linkAdapter);
            }

            add(titleLabel, BorderLayout.NORTH);
            add(valueLabel);
            add(Box.createRigidArea(new Dimension(0, 20)), BorderLayout.SOUTH);
        }

        public void setValue(String value, String link){
            this.link = link;
            valueLabel.setText(value);
        }

    }
}
