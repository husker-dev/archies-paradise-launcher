package com.husker.launcher.ui.impl.glass.components;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.style.StyleId;
import com.husker.launcher.Resources;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.ui.impl.glass.GlassUI;
import com.husker.launcher.utils.ComponentUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import java.util.LinkedHashMap;
import java.util.Map;

public class BlurTabPanel extends WebPanel implements BlurComponent {

    private final Screen screen;
    private final WebPanel tabPanel;
    private final WebPanel contentPanel;
    private String selectedTab = null;
    private final LinkedHashMap<String, Component> tabComponents = new LinkedHashMap<>();
    private final LinkedHashMap<String, Component> contentComponents = new LinkedHashMap<>();

    public BlurTabPanel(Screen screen){
        super(StyleId.panelTransparent);
        this.screen = screen;
        setLayout(new BorderLayout());

        screen.addBlurSegment(this::onBlurApply);

        add(tabPanel = new WebPanel(StyleId.panelTransparent){{
            setLayout(new GridBagLayout());

        }}, BorderLayout.NORTH);
        add(contentPanel = new WebPanel(StyleId.panelTransparent){{
            setLayout(new BorderLayout());
        }});
    }

    public void addTab(String id, String title, Component component){
        addTab(id, title, null, component);
    }

    public void addTab(String id, Icon icon, Component component){
        addTab(id, "", icon, component);
    }

    public void addTab(String id, String title, Icon icon, Component component){
        contentComponents.put(id, component);

        WebLabel tabComponent = new WebLabel(){{
            setIcon(icon);
            setText(title);

            setForeground(GlassUI.Colors.labelText);
            setFont(Resources.Fonts.ChronicaPro_ExtraBold);
            setPreferredHeight(50);
            setHorizontalAlignment(CENTER);
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    setSelectedTab(id);
                }
            });
        }};

        tabComponents.put(id, tabComponent);
        tabPanel.add(tabComponent, new GridBagConstraints(){{
            this.weightx = 1;
            this.fill = 1;
        }});
        if(tabComponents.size() == 1)
            setSelectedTab(id);
    }

    public void setSelectedTab(String title){
        if(!contentComponents.containsKey(title))
            return;
        selectedTab = title;
        SwingUtilities.invokeLater(() -> {
            contentPanel.removeAll();
            contentPanel.add(contentComponents.get(title));
            updateUI();
        });
    }

    public void onBlurApply(BlurParameter parameter) {
        Area fullArea = new Area();
        int selectedIndex = 0;
        for(Map.Entry<String, Component> components : tabComponents.entrySet()){
            if(!selectedTab.equals(components.getKey())) {
                selectedIndex ++;
                continue;
            }

            Component component = components.getValue();
            Point location = ComponentUtils.getComponentLocationOnScreen(screen.getLauncher(), component);

            fullArea.add(new Area(new RoundRectangle2D.Double(location.x, location.y, component.getWidth(), component.getHeight() + 25, 25, 25)));
            break;
        }
        Point contentLocation = ComponentUtils.getComponentLocationOnScreen(screen.getLauncher(), contentPanel);
        fullArea.add(new Area(new RoundRectangle2D.Double(contentLocation.x, contentLocation.y, contentPanel.getWidth(), contentPanel.getHeight(), 25, 25)));

        GlassUI.applyTopLayer(parameter);
        parameter.setShape(fullArea);
        parameter.setVisible(isVisible() && isDisplayable());

        Area shadowArea = new Area();
        int x = fullArea.getBounds().x;
        int y = fullArea.getBounds().y;
        int width = fullArea.getBounds().width;
        shadowArea.add(new Area(new Rectangle(x, y, width, tabPanel.getHeight() + 25)));
        if(selectedIndex == 0)
            shadowArea.subtract(new Area(new Rectangle(x, y, 25, tabPanel.getHeight() + 25)));
        if(selectedIndex == tabComponents.size() - 1)
            shadowArea.subtract(new Area(new Rectangle(x + width - 25, y, 25, tabPanel.getHeight() + 25)));

        parameter.setShadowClip(shadowArea);
    }

    public Screen getScreen() {
        return screen;
    }
}
