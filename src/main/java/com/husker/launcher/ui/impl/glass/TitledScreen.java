package com.husker.launcher.ui.impl.glass;

import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.style.StyleId;
import com.husker.launcher.LauncherWindow;
import com.husker.launcher.NetManager;
import com.husker.launcher.Resources;
import com.husker.launcher.UpdateManager;
import com.husker.launcher.components.ScalableImage;
import com.husker.launcher.ui.CenteredMenuScreen;
import com.husker.launcher.utils.UIUtils;
import com.husker.launcher.ui.impl.glass.components.BlurButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.awt.geom.RoundRectangle2D;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.husker.launcher.utils.UIUtils.ShadowSide.BOTTOM;
import static com.husker.launcher.utils.UIUtils.ShadowSide.TOP;

public abstract class TitledScreen extends CenteredMenuScreen {

    private final int statusSize = 35;
    private final int logoSize = 180;

    private WebLabel statusLabel;
    private WebLabel titleLabel;

    private String title = "Заголовок";

    public void setTitle(String title){
        this.title = title;
        if(titleLabel != null)
            titleLabel.setText(title);
    }

    public void onMenuInit() {

        new Timer().schedule(new TimerTask() {
            public void run() {
                getLauncher().NetManager.updateStatusLabel(statusLabel);
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
        addToMenu(new WebPanel(StyleId.panelTransparent){{
            setLayout(new BorderLayout(0, 0));
            setMargin(2, 10, 0, 10);

            add(new WebPanel(StyleId.panelTransparent){{
                setPreferredHeight(statusSize);
                setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

                add(new WebLabel("Статус:  "){{
                    setForeground(new Color(70, 70, 70));
                    setPreferredHeight(15);
                    setFont(Resources.Fonts.ChronicaPro_ExtraBold);
                }});
                add(statusLabel = new WebLabel("Обновление..."){{
                    setForeground(new Color(120, 120, 120));
                    setPreferredHeight(15);
                    setFont(Resources.Fonts.ChronicaPro_ExtraBold);
                }});
            }}, BorderLayout.WEST);

            add(new WebPanel(StyleId.panelTransparent){{
                setPreferredHeight(statusSize);
                setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

                add(new WebLabel("v" + UpdateManager.VERSION){{
                    setForeground(new Color(140, 140, 140, 100));
                    setPreferredHeight(15);
                    setFont(Resources.Fonts.ChronicaPro);
                }});
            }}, BorderLayout.EAST);
        }});

        addToMenu(new ScalableImage(getLauncher().Resources.Logo){{
            setPreferredSize(new Dimension(getCenteredPanel().getWidth(), logoSize));
            setFitType(FitType.FIT_Y);
        }});

        addIndent(10);
        addToMenu(titleLabel = GlassUI.createTitleLabel(title));
        addIndent(5);

        addToMenu( new WebPanel(StyleId.panelTransparent){{
            setLayout(new VerticalFlowLayout(0, 6));
            setMargin(0, 40, 0, 40);

            createMenu(this);
        }});
        addIndent(20);

        addToMenu(new WebPanel(StyleId.panelTransparent){{
            setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
            setMargin(0, 40, 0, 40);

            createComponents(this);
        }});

        addIndent(0);

        addToMenu(new WebPanel(StyleId.panelTransparent){{
            setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
            setMargin(0, 40, 0, 40);
            setPreferredHeight(23);

            createSubComponents(this);

        }});
    }

    public abstract void createMenu(WebPanel panel);
    public abstract void createComponents(WebPanel panel);
    public abstract void createSubComponents(WebPanel panel);

    public WebLabel createLabel(String text){
        return new WebLabel(text){{
            setForeground(new Color(50, 50, 50));
            setHorizontalAlignment(LEFT);
            setPreferredHeight(15);
            setFont(Resources.Fonts.ChronicaPro_ExtraBold);
        }};
    }

    public BlurButton createButton(int col, String text, Runnable runnable){
        return new BlurButton(this, text){{
            setPreferredWidth((200 - 20 * (col - 1)) / col);
            addActionListener(e -> {
                if(runnable != null)
                    runnable.run();
            });
        }};
    }

    public WebLabel createSubLabel(String text, Runnable runnable){
        return new WebLabel(text){{
            setForeground(GlassUI.Colors.labelLightText);
            setFont(getFont().deriveFont(14f));
            setFont(Resources.Fonts.ChronicaPro_ExtraBold);
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
