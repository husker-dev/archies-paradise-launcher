package com.husker.launcher.ui.impl.glass.components;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.style.StyleId;
import com.husker.launcher.Resources;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.ui.impl.glass.GlassUI;
import com.husker.launcher.utils.ComponentUtils;
import com.husker.launcher.utils.ShapeUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.husker.launcher.utils.ShapeUtils.Corner.*;

public class BlurTabPanel extends WebPanel implements BlurComponent {

    private final Screen screen;
    private final WebPanel topTabPanel;
    private final WebPanel bottomTabPanel;
    private WebPanel contentPanel = null;
    private String selectedTab = null;
    private final LinkedHashMap<String, WebLabel> tabLabels = new LinkedHashMap<>();
    private final LinkedHashMap<String, Component> tabContent = new LinkedHashMap<>();

    private final LinkedHashMap<WebLabel, String> tabText = new LinkedHashMap<>();

    public BlurTabPanel(Screen screen){
        super(StyleId.panelTransparent);
        this.screen = screen;
        setLayout(new BorderLayout());

        screen.addBlurSegment(parameter -> onBlurApply(parameter, contentPanel.getFirstComponent()));

        add(topTabPanel = new WebPanel(StyleId.panelTransparent){
            {
                setLayout(new GridBagLayout());
            }
            public void paint(Graphics g) {
                g.setColor(new Color(100, 100, 100, 100));

                int selected = -1;
                WebLabel selectedLabel = getSelectedTabLabel();
                for(int i = 0; i < topTabPanel.getComponentCount(); i++)
                    if(selectedLabel == topTabPanel.getComponent(i))
                        selected = i;

                int current_x = 0;
                int index = 0;
                for(Component component : topTabPanel.getComponents()){
                    current_x += component.getWidth();

                    if(index != selected && index != selected - 1 && index != topTabPanel.getComponentCount() - 1)
                        g.drawLine(current_x, 10, current_x, getHeight() - 10);

                    index++;
                }
                super.paint(g);
            }
        }, BorderLayout.NORTH);
        add(bottomTabPanel = new WebPanel(StyleId.panelTransparent){
            {
                setLayout(new GridBagLayout());
            }
            public void paint(Graphics g) {
                g.setColor(new Color(100, 100, 100, 100));

                int selected = -1;
                WebLabel selectedLabel = getSelectedTabLabel();
                for(int i = 0; i < bottomTabPanel.getComponentCount(); i++)
                    if(selectedLabel == bottomTabPanel.getComponent(i))
                        selected = i;

                int current_x = 0;
                int index = 0;
                for(Component component : bottomTabPanel.getComponents()){
                    current_x += component.getWidth();

                    if(index != selected && index != selected - 1 && index != bottomTabPanel.getComponentCount() - 1)
                        g.drawLine(current_x, 10, current_x, getHeight() - 10);
                    index++;
                }
                super.paint(g);
            }
        }, BorderLayout.SOUTH);
        add(contentPanel = new WebPanel(StyleId.panelTransparent){{
            setLayout(new BorderLayout());
        }});
    }

    public void addTab(String id, String title, Component component){
        addTab(id, title, null, component, true);
    }

    public void addTab(String id, Icon icon, Component component){
        addTab(id, "", icon, component, true);
    }

    public void addTab(String id, String title, Icon icon, Component component){
        addTab(id, title, icon, component, true);
    }

    public void addTab(String id, String title, Icon icon, Component component, boolean isTop){
        tabContent.put(id, component);

        WebLabel tabLabel = createLabel(icon);
        tabLabel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                setSelectedTab(id);
            }
        });
        tabText.put(tabLabel, title);

        tabLabels.put(id, tabLabel);
        screen.addBlurSegment(parameter -> onBlurApply(parameter, tabLabel));
        if(isTop) {
            topTabPanel.add(tabLabel, new GridBagConstraints() {{
                this.weightx = 1;
                this.fill = 1;
            }});
        }else{
            bottomTabPanel.add(tabLabel, new GridBagConstraints() {{
                this.weightx = 1;
                this.fill = 1;
            }});
        }

        if(tabLabels.size() == 1)
            setSelectedTab(id);
    }

    protected WebLabel createLabel(Icon icon){
        return new WebLabel(){{
            setIcon(icon);

            setForeground(GlassUI.Colors.labelText);
            setFont(Resources.Fonts.ChronicaPro_ExtraBold);
            setPreferredHeight(50);
            setHorizontalAlignment(CENTER);
        }};
    }

    public void addBottomTab(String id, String title, Component component){
        addTab(id, title, null, component, false);
    }

    public void addBottomTab(String id, Icon icon, Component component){
        addTab(id, "", icon, component, false);
    }

    public void addBottomTab(String id, String title, Icon icon, Component component){
        addTab(id, title, icon, component, false);
    }

    public int getSelectedTabIndex(){
        return getIndex(selectedTab);
    }

    public String getSelectedTabId(){
        return selectedTab;
    }

    public WebLabel getSelectedTabLabel(){
        return getTabLabel(selectedTab);
    }

    public Component getSelectedTabContent(){
        return getTabContent(selectedTab);
    }

    public String getId(int index){
        int selectedIndex = 0;
        for(Map.Entry<String, WebLabel> components : tabLabels.entrySet()){
            if(selectedIndex != index)
                selectedIndex ++;
            else
                return components.getKey();
        }
        return null;
    }

    public int getIndex(String id){
        int selectedIndex = 0;
        for(Map.Entry<String, WebLabel> components : tabLabels.entrySet()){
            if(!components.getKey().equals(id))
                selectedIndex ++;
            else
                return selectedIndex;
        }
        return -1;
    }

    public WebLabel getTabLabel(int index){
        return tabLabels.get(getId(index));
    }

    public WebLabel getTabLabel(String id){
        return tabLabels.get(id);
    }

    public Component getTabContent(String id){
        return tabContent.get(id);
    }

    public Component getTabContent(int index){
        return getTabContent(getId(index));
    }

    public void setSelectedTab(String title){
        if(!tabContent.containsKey(title))
            return;

        SwingUtilities.invokeLater(() -> {
            if(selectedTab != null)
                tabLabels.get(selectedTab).setText("");
            selectedTab = title;
            tabLabels.get(selectedTab).setText(tabText.get(tabLabels.get(selectedTab)));
            contentPanel.removeAll();
            contentPanel.add(tabContent.get(title));
            updateUI();
        });
    }

    public void onBlurApply(BlurParameter parameter, Component component) {

        // Tab
        if(component instanceof WebLabel && (topTabPanel.contains(component) || bottomTabPanel.contains(component)) && getSelectedTabLabel() == component){
            Point location = ComponentUtils.getComponentLocationOnScreen(screen.getLauncher(), component);

            Area shape;
            if(topTabPanel.contains(component))
                shape = new Area(ShapeUtils.createRoundRectangle(location.x, location.y, component.getWidth(), component.getHeight() + 25, 25, 25, TOP_LEFT, TOP_RIGHT));
            else
                shape = new Area(ShapeUtils.createRoundRectangle(location.x, location.y - 25, component.getWidth(), component.getHeight() + 25, 25, 25, BOTTOM_LEFT, BOTTOM_RIGHT));
            shape.subtract(new Area(getContentShape()));

            GlassUI.applyTopLayer(parameter);
            parameter.setVisible(isVisible() && isDisplayable());
            parameter.setShape(shape);
            parameter.setShadowClip(getShadowClip());
            return;
        }

        // Content
        if(tabContent.containsValue(component)){
            GlassUI.applyTopLayer(parameter);
            parameter.setVisible(isVisible() && isDisplayable());
            parameter.setShape(getContentShape());
            parameter.setShadowClip(getShadowClip());
            return;
        }
    }

    protected RoundRectangle2D.Double getContentShape(){
        Component component = contentPanel.getFirstComponent();
        Point location = ComponentUtils.getComponentLocationOnScreen(screen.getLauncher(), component);
        return new RoundRectangle2D.Double(location.x, location.y, component.getWidth(), component.getHeight(), 25, 25);
    }

    protected Shape getShadowClip(){
        int selectedIndex = getSelectedTabIndex();

        Area shadowArea = new Area();
        int x = ComponentUtils.getComponentLocationOnScreen(screen.getLauncher(), this).x;
        int y = ComponentUtils.getComponentLocationOnScreen(screen.getLauncher(), this).y;
        int width = getWidth();
        int height = getHeight();
        shadowArea.add(new Area(new Rectangle(x, y, width, topTabPanel.getHeight() + 25)));
        shadowArea.add(new Area(new Rectangle(x, y + height - (bottomTabPanel.getHeight() + 25), width, bottomTabPanel.getHeight() + 25)));

        WebLabel selectedLabel = getSelectedTabLabel();

        if(topTabPanel.contains(selectedLabel) && topTabPanel.getFirstComponent() == selectedLabel)
            shadowArea.subtract(new Area(new Rectangle(x, y, 25, topTabPanel.getHeight() + 25)));
        if(topTabPanel.contains(selectedLabel) && topTabPanel.getLastComponent() == selectedLabel)
            shadowArea.subtract(new Area(new Rectangle(x + width - 25, y, 25, topTabPanel.getHeight() + 25)));

        if(bottomTabPanel.contains(selectedLabel) && bottomTabPanel.getFirstComponent() == selectedLabel)
            shadowArea.subtract(new Area(new Rectangle(x, y + height - (bottomTabPanel.getHeight() + 25), 25, bottomTabPanel.getHeight() + 25)));
        if(bottomTabPanel.contains(selectedLabel) && bottomTabPanel.getLastComponent() == selectedLabel)
            shadowArea.subtract(new Area(new Rectangle(x + width - 25, y + height - (bottomTabPanel.getHeight() + 25), 25, bottomTabPanel.getHeight() + 25)));

        shadowArea.subtract(new Area(new Rectangle(x + 25, y + topTabPanel.getHeight(), width - 50, 25)));
        shadowArea.subtract(new Area(new Rectangle(x + 25, y + height - bottomTabPanel.getHeight() - 25, width - 50, 25)));
        return shadowArea;
    }

    public Screen getScreen() {
        return screen;
    }
}
