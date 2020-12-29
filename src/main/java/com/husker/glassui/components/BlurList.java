package com.husker.glassui.components;

import com.husker.glassui.GlassUI;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.ui.utils.ShapeUtils;
import com.husker.launcher.ui.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;


public class BlurList<T> extends BlurPanel{

    private final List<Consumer<T>> selectedListener = new ArrayList<>();

    private final List<ContentPanel<T>> contentPanels = new ArrayList<>();
    private final List<T> content = new ArrayList<>();
    private T selected;

    public BlurList(Screen screen) {
        super(screen);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredHeight(160);
    }

    public void onBlurApply(BlurParameter parameter, Component component) {
        super.onBlurApply(parameter, component);
        if(returnOnInvisible(parameter, component))
            return;

        GlassUI.applyBottomLayer(parameter);
        parameter.setShadowSize(5);
        parameter.setShadowType(BlurParameter.ShadowType.INNER);
    }

    public void addSelectedListener(Consumer<T> listener){
        selectedListener.add(listener);
    }

    public int getContentPanelsCount(){
        return contentPanels.size();
    }

    public List<ContentPanel<T>> getContentPanels(){
        return contentPanels;
    }

    public void addContentPanel(ContentPanel<T> element){
        element.list = this;
        element.index = getContentPanelsCount();
        element.content = null;
        element.applyContent(null);
        contentPanels.add(element);
        add(element);
        updateContentPanels();
    }

    public T getContentAt(int index){
        return content.get(index);
    }

    public List<T> getContent(){
        return content;
    }

    public void setSelected(T element){
        selectedListener.forEach(listener -> listener.accept(element));
        selected = element;
        updateContentPanels();
    }

    public void setContent(T[] content){
        this.content.clear();
        this.content.addAll(Arrays.asList(content));
        updateContentPanels();
    }

    private void updateContentPanels(){
        for(int i = 0; i < contentPanels.size(); i++) {
            T content = null;
            if(i < this.content.size())
                content = this.content.get(i);

            contentPanels.get(i).content = content;
            contentPanels.get(i).applyContent(content);
        }
    }

    public abstract static class ContentPanel<T> extends BlurPanel {

        private int index = -1;
        private BlurList<T> list;
        private T content;

        public ContentPanel(Screen screen) {
            super(screen);
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    setSelected();
                }
            });
            setPreferredSize(130, 30);
            setLayout(new BorderLayout());
            setMargin(0, 10, 0, 10);
        }

        public void paint(Graphics g) {
            if(!isLast() && !isSelected() && (getList().selected == null || getList().selected != getList().getContentPanels().get(index + 1).content)) {
                g.setColor(new Color(160, 160, 160, 150));
                g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            }
            super.paint(g);
        }

        public T getContent(){
            return content;
        }

        public abstract void applyContent(T content);

        public void onBlurApply(BlurParameter parameter, Component component) {
            super.onBlurApply(parameter, component);
            if(returnOnInvisible(parameter, component))
                return;

            if(isSelected()){
                GlassUI.applyTopLayer(parameter);
                parameter.setShadowSize(5);
                parameter.setShape(ShapeUtils.createRoundRectangle(this, 0, 0));

                ArrayList<UIUtils.ShadowSide> shadows = new ArrayList<>();
                if(!isFirst())
                    shadows.add(UIUtils.ShadowSide.TOP);
                if(!isLast())
                    shadows.add(UIUtils.ShadowSide.BOTTOM);

                parameter.setShadowClip(UIUtils.keepShadow(parameter, shadows.toArray(new UIUtils.ShadowSide[0])));
            }else
                parameter.setVisible(false);
        }

        public boolean isFirst(){
            return index == 0;
        }

        public boolean isLast(){
            return index == getList().getContentPanelsCount() - 1;
        }

        public BlurList<T> getList() {
            return list;
        }

        public int getIndex(){
            return index;
        }

        public boolean isSelected() {
            return getList().selected != null && getList().selected.equals(content);
        }

        public void setSelected() {
            getList().setSelected(content);
        }
    }
}
