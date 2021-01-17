package com.husker.glassui.screens;

import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.utils.swing.extensions.SizeMethodsImpl;
import com.husker.glassui.components.BlurButton;
import com.husker.launcher.Launcher;
import com.husker.launcher.Resources;
import com.husker.launcher.managers.NetManager;
import com.husker.launcher.ui.components.MLabel;
import com.husker.launcher.ui.components.TransparentPanel;
import com.husker.launcher.managers.UpdateManager;
import com.husker.launcher.ui.components.ScalableImage;
import com.husker.launcher.ui.CenteredMenuScreen;
import com.husker.glassui.GlassUI;
import com.husker.launcher.ui.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.awt.geom.RoundRectangle2D;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.husker.launcher.ui.utils.UIUtils.ShadowSide.BOTTOM;
import static com.husker.launcher.ui.utils.UIUtils.ShadowSide.TOP;

public abstract class TitledLogoScreen extends CenteredMenuScreen {

    private final int statusSize = 35;
    private final int logoSize = 180;

    private MLabel statusLabel;
    private MLabel titleLabel;

    private String title = "Заголовок";

    public void setTitle(String title){
        this.title = title;
        if(titleLabel != null)
            titleLabel.setText(title);
    }

    public void onMenuInit() {

        new Timer().schedule(new TimerTask() {
            public void run() {
                NetManager.updateStatusLabel(getLauncher(), statusLabel);
            }
        }, 0, 500);

        // Under logo
        addBlurSegment("TitledScreen.BottomPlate", parameter -> {
            GlassUI.applyBottomLayer(parameter);
            parameter.setShape(new RoundRectangle2D.Double(getCenteredPanel().getX(), getCenteredPanel().getY(), getCenteredPanel().getWidth(), getCenteredPanel().getHeight(), 25, 25));
        });

        // Status bar
        addBlurSegment("TitledScreen.StatusBar", parameter -> {
            GlassUI.applyTopLayer(parameter);
            parameter.setShape(new RoundRectangle2D.Double(getCenteredPanel().getX(), getCenteredPanel().getY(), getCenteredPanel().getWidth(), statusSize, 25, 25));
            parameter.setShadowClip(UIUtils.keepShadow(parameter, BOTTOM));
        });

        // Login main borders
        addBlurSegment("TitledScreen.TopPlate", parameter -> {
            GlassUI.applyTopLayer(parameter);
            parameter.setShape(new RoundRectangle2D.Double(getCenteredPanel().getX(), getCenteredPanel().getY() + logoSize + statusSize, getCenteredPanel().getWidth(), getCenteredPanel().getHeight() - logoSize - statusSize - 39, 25, 25));
            parameter.setShadowClip(UIUtils.keepShadow(parameter, TOP, BOTTOM));
        });

        // Шапка - статус и версия
        addToMenu(new TransparentPanel(){{
            setLayout(new BorderLayout(0, 0));
            setMargin(2, 10, 0, 10);

            add(new TransparentPanel(){{
                setPreferredHeight(statusSize);
                setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

                add(new MLabel("Статус:  "){{
                    setForeground(new Color(70, 70, 70));
                    setPreferredHeight(15);
                    setFont(Resources.Fonts.getChronicaProExtraBold());
                }});
                add(statusLabel = new MLabel("Обновление..."){{
                    setForeground(new Color(120, 120, 120));
                    setPreferredHeight(15);
                    setFont(Resources.Fonts.getChronicaProExtraBold());
                }});
            }}, BorderLayout.WEST);

            add(new TransparentPanel(){{
                setPreferredHeight(statusSize);
                setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

                add(new MLabel("v" + Launcher.VERSION){{
                    setForeground(new Color(140, 140, 140, 100));
                    setPreferredHeight(15);
                    setFont(Resources.Fonts.getChronicaPro());
                }});
            }}, BorderLayout.EAST);
        }});

        addToMenu(new ScalableImage(Resources.Logo){{
            setPreferredSize(new Dimension(getCenteredPanel().getWidth(), logoSize));
            setFitType(FitType.FILL_Y);
        }});

        addIndent(10);
        addToMenu(titleLabel = GlassUI.createTitleLabel(title));
        addIndent(5);

        addToMenu(new TransparentPanel(){{
            setLayout(new VerticalFlowLayout(0, 6));
            setMargin(0, 40, 0, 40);

            createMenu(this);
        }});
        addIndent(20);

        TransparentPanel componentsPanel = new TransparentPanel(){
            {
                setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
                setMargin(0, 0, 0, 0);
            }
            public Component add(Component component) {
                if (getComponentCount() == 1) {
                    SizeMethodsImpl.setPreferredWidth((JComponent) getComponent(0), 120);
                    SizeMethodsImpl.setPreferredWidth((JComponent)component, 120);
                } else
                    SizeMethodsImpl.setPreferredWidth((JComponent)component, 170);

                return super.add(component);
            }
        };
        addToMenu(componentsPanel);
        createComponents(componentsPanel);

        addIndent(0);

        TransparentPanel sub = new TransparentPanel(){{
            setLayout(new VerticalFlowLayout(FlowLayout.CENTER, 0, 0));
            setMargin(5, 40, 0, 40);
            createSubComponents(this);
        }};
        if(sub.getComponentCount() == 0)
            sub.add(createSubLabel(" ", () -> {}));
        addToMenu(sub);
    }

    public abstract void createMenu(TransparentPanel panel);
    public abstract void createComponents(TransparentPanel panel);
    public abstract void createSubComponents(TransparentPanel panel);

    public MLabel createLabel(String text){
        return new MLabel(text){{
            setForeground(new Color(50, 50, 50));
            setHorizontalAlignment(LEFT);
            setPreferredHeight(15);
            setFont(Resources.Fonts.getChronicaProExtraBold());
        }};
    }

    public BlurButton createButton(String text, Runnable runnable){
        return new BlurButton(this, text){{
            setPreferredWidth(170);
            addActionListener(e -> {
                if(runnable != null)
                    runnable.run();
            });
        }};
    }

    public MLabel createSubLabel(String text, Runnable runnable){
        return new MLabel(text){{
            setForeground(GlassUI.Colors.labelLightText);
            setFont(getFont().deriveFont(14f));
            setFont(Resources.Fonts.getChronicaProExtraBold());
            setHorizontalAlignment(CENTER);

            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    runnable.run();
                }

                public void mouseEntered(MouseEvent e) {
                    Font font = getFont();
                    Map attributes = font.getAttributes();
                    attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
                    setFont(font.deriveFont(attributes));
                }
                public void mouseExited(MouseEvent e) {
                    Font font = getFont();
                    Map attributes = font.getAttributes();
                    attributes.put(TextAttribute.UNDERLINE, -1);
                    setFont(font.deriveFont(attributes));
                }
            });
        }};
    }
}
