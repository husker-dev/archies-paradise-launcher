package com.husker.glassui.components;

import com.husker.launcher.Resources;
import com.husker.launcher.ui.components.MLabel;
import com.husker.launcher.ui.components.TransparentPanel;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.glassui.GlassUI;
import com.husker.launcher.ui.utils.ComponentUtils;
import com.husker.launcher.ui.utils.ShapeUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.husker.launcher.ui.utils.ShapeUtils.ALL_CORNERS;
import static com.husker.launcher.ui.utils.ShapeUtils.Corner.*;

public class BlurTabPanel extends TransparentPanel implements BlurComponent {

    private final Screen screen;
    private final TransparentPanel topTabPanel;
    private final TransparentPanel bottomTabPanel;
    private TransparentPanel contentPanel = null;
    private String selectedTab = null;
    private final LinkedHashMap<String, MLabel> tabLabels = new LinkedHashMap<>();
    private final LinkedHashMap<String, Component> tabContent = new LinkedHashMap<>();

    private final LinkedHashMap<MLabel, String> tabText = new LinkedHashMap<>();

    private final ArrayList<Consumer<String>> listeners = new ArrayList<>();

    private boolean disposed = false;

    public BlurTabPanel(Screen screen){
        this.screen = screen;
        setLayout(new BorderLayout());

        screen.addBlurSegment("TabPanel.Content", parameter -> onBlurApply(parameter, contentPanel.getFirstComponent()));
        screen.addBlurSegment("TabPanel.Tab", parameter -> onBlurApply(parameter, getSelectedTabLabel()));

        add(topTabPanel = new TransparentPanel(){
            {
                setLayout(new GridBagLayout());
            }
            public void paint(Graphics g) {
                g.setColor(new Color(100, 100, 100, 100));

                int selected = -1;
                MLabel selectedLabel = getSelectedTabLabel();
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
        add(bottomTabPanel = new TransparentPanel(){
            {
                setLayout(new GridBagLayout());
            }
            public void paint(Graphics g) {
                g.setColor(new Color(100, 100, 100, 100));

                int selected = -1;
                MLabel selectedLabel = getSelectedTabLabel();
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
        add(contentPanel = new TransparentPanel(){{
            setLayout(new BorderLayout());
        }});
    }

    public void addTab(String id, String title, Component component){
        addTab(id, title, null, component, true);
    }

    public void addTab(String id, BufferedImage icon, Component component){
        addTab(id, "", icon, component, true);
    }

    public void addTab(String id, String title, BufferedImage icon, Component component){
        addTab(id, title, icon, component, true);
    }

    public void addTab(String id, String title, BufferedImage icon, Component component, boolean isTop){
        tabContent.put(id, component);

        MLabel tabLabel = createLabel(icon);
        tabLabel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                setSelectedTab(id);
            }
        });
        tabText.put(tabLabel, title);

        tabLabels.put(id, tabLabel);
        if(isTop) {
            topTabPanel.add(tabLabel, new GridBagConstraints() {{
                this.weightx = 1;
                this.fill = 1;
                this.insets = new Insets(0, 0, 0, 0);
            }});
        }else{
            bottomTabPanel.add(tabLabel, new GridBagConstraints() {{
                this.weightx = 1;
                this.fill = 1;
                this.insets = new Insets(0, 0, 0, 0);
            }});
        }

        if(tabLabels.size() == 1)
            setSelectedTab(id);
    }

    public void removeTab(String id){
        if(!tabContent.containsKey(id))
            return;

        tabContent.remove(id);
        MLabel label = tabLabels.remove(id);
        tabText.remove(label);

        if(topTabPanel.contains(label))
            topTabPanel.remove(label);
        if(bottomTabPanel.contains(label))
            bottomTabPanel.remove(label);
    }

    protected MLabel createLabel(BufferedImage icon){
        return new MLabel(){{
            setImage(icon);
            setImageSize(25);

            setForeground(GlassUI.Colors.labelText);
            setFont(Resources.Fonts.getChronicaProExtraBold());
            setPreferredHeight(50);
            setHorizontalAlignment(CENTER);
        }};
    }

    public void addBottomTab(String id, String title, Component component){
        addTab(id, title, null, component, false);
    }

    public void addBottomTab(String id, BufferedImage icon, Component component){
        addTab(id, "", icon, component, false);
    }

    public void addBottomTab(String id, String title, BufferedImage icon, Component component){
        addTab(id, title, icon, component, false);
    }

    public int getSelectedTabIndex(){
        return getIndex(selectedTab);
    }

    public String getSelectedTabId(){
        return selectedTab;
    }

    public MLabel getSelectedTabLabel(){
        return getTabLabel(selectedTab);
    }

    public Component getSelectedTabContent(){
        return getTabContent(selectedTab);
    }

    public String getId(int index){
        int selectedIndex = 0;
        for(Map.Entry<String, MLabel> components : tabLabels.entrySet()){
            if(selectedIndex != index)
                selectedIndex ++;
            else
                return components.getKey();
        }
        return null;
    }

    public int getIndex(String id){
        int selectedIndex = 0;
        for(Map.Entry<String, MLabel> components : tabLabels.entrySet()){
            if(!components.getKey().equals(id))
                selectedIndex ++;
            else
                return selectedIndex;
        }
        return -1;
    }

    public MLabel getTabLabel(int index){
        return tabLabels.get(getId(index));
    }

    public MLabel getTabLabel(String id){
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

        if(title.equals(selectedTab))
            return;

        SwingUtilities.invokeLater(() -> {
            if(selectedTab != null)
                tabLabels.get(selectedTab).setText("");
            selectedTab = title;
            tabLabels.get(selectedTab).setText(tabText.get(tabLabels.get(selectedTab)));
            contentPanel.removeAll();
            contentPanel.add(tabContent.get(title));
            screen.getLauncher().repaint();

            for(Consumer<String> listener : listeners)
                listener.accept(title);
        });
    }

    public void onBlurApply(BlurParameter parameter, Component component) {
        checkForDispose(parameter);
        if(returnOnInvisible(parameter, component))
            return;

        // Tab
        if(component instanceof MLabel && (topTabPanel.contains(component) || bottomTabPanel.contains(component)) && getSelectedTabLabel() == component && ((MLabel)component).getText().length() > 0){

            Point location = ComponentUtils.getComponentLocationOnScreen(screen.getLauncher(), component);
            Area shape;
            if(topTabPanel.contains(component))
                shape = new Area(ShapeUtils.createRoundRectangle(location.x, location.y, component.getWidth(), component.getHeight() + 25, 25, 25, TOP_LEFT, TOP_RIGHT));
            else
                shape = new Area(ShapeUtils.createRoundRectangle(location.x, location.y - 25, component.getWidth(), component.getHeight() + 25, 25, 25, BOTTOM_LEFT, BOTTOM_RIGHT));
            //shape.subtract(new Area(getContentShape()));

            GlassUI.applyTopLayer(parameter);
            parameter.setShape(shape);
            parameter.setShadowClip(getShadowClip());
            return;
        }

        // Content
        if(tabContent.containsValue(component)){
            GlassUI.applyTopLayer(parameter);
            parameter.setShape(getContentShape());
            parameter.setShadowClip(getShadowClip());
        }
    }

    protected Shape getContentShape(){
        return ShapeUtils.createRoundRectangle(contentPanel.getFirstComponent(), 25, 25, ALL_CORNERS);
    }

    protected Shape getShadowClip(){
        Area shadowArea = new Area();
        int x = ComponentUtils.getComponentLocationOnScreen(screen.getLauncher(), this).x;
        int y = ComponentUtils.getComponentLocationOnScreen(screen.getLauncher(), this).y;
        int width = getWidth();
        int height = getHeight();
        shadowArea.add(new Area(new Rectangle(x, y, width, topTabPanel.getHeight() + 25)));
        shadowArea.add(new Area(new Rectangle(x, y + height - (bottomTabPanel.getHeight() + 25), width, bottomTabPanel.getHeight() + 25)));

        MLabel selectedLabel = getSelectedTabLabel();

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

    public void dispose() {
        disposed = true;
    }

    public boolean isDisposed() {
        return disposed;
    }

    public void addTabChangedListener(Consumer<String> listener){
        listeners.add(listener);
    }
}
