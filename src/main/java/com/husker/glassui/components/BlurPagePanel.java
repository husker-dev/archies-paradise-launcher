package com.husker.glassui.components;

import com.alee.laf.label.WebLabel;
import com.husker.glassui.GlassUI;
import com.husker.glassui.screens.main.profile.skin.SkinFolders;
import com.husker.launcher.components.TransparentPanel;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.function.Consumer;

public class BlurPagePanel extends BlurPanel {

    private final TransparentPanel dotsPanel;
    private WebLabel[] dots = new WebLabel[0];
    private final WebLabel leftArrow, rightArrow;
    private int pages = 0;
    private int currentPage = 0;

    private final ArrayList<Consumer<Integer>> listeners = new ArrayList<>();

    private final Icon leftDefault = createIcon(21, getScreen().getLauncher().Resources.Icon_Arrow_Left);
    private final Icon leftSelected = createIcon(21, getScreen().getLauncher().Resources.Icon_Arrow_Left_Selected);

    private final Icon rightDefault = createIcon(21, getScreen().getLauncher().Resources.Icon_Arrow_Right);
    private final Icon rightSelected = createIcon(21, getScreen().getLauncher().Resources.Icon_Arrow_Right_Selected);

    private final Icon dotDefault = createIcon(13, getScreen().getLauncher().Resources.Icon_Dot);
    private final Icon dotSelected = createIcon(13, getScreen().getLauncher().Resources.Icon_Dot_Selected);

    public BlurPagePanel(Screen screen){
        super(screen, true);

        setPreferredHeight(35);
        setMargin(6, 0, 0, 0);
        setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));

        add(leftArrow = new WebLabel(){{
            setVerticalAlignment(CENTER);
            setHorizontalAlignment(CENTER);
            setPreferredWidth(40);

            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent mouseEvent) {
                    if(currentPage > 0) {
                        setSelectedPage(currentPage - 1);
                        for (Consumer<Integer> listener : listeners)
                            listener.accept(currentPage);
                    }
                }
            });
        }});
        add(dotsPanel = new TransparentPanel(){{
            setLayout(new FlowLayout(FlowLayout.CENTER));
        }});

        add(rightArrow = new WebLabel(){{
            setVerticalAlignment(CENTER);
            setHorizontalAlignment(CENTER);
            setPreferredWidth(40);

            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent mouseEvent) {
                    if(currentPage < pages - 1) {
                        setSelectedPage(currentPage + 1);
                        for(Consumer<Integer> listener : listeners)
                            listener.accept(currentPage);
                    }
                }
            });
        }});
    }

    public int getPage(){
        return currentPage;
    }

    public void addPageListener(Consumer<Integer> consumer){
        listeners.add(consumer);
    }

    public void setPages(int pages){
        this.pages = pages;

        dots = new WebLabel[pages];
        dotsPanel.removeAll();
        for(int i = 0; i < pages; i++){
            final int I = i;
            dots[i] = new WebLabel(){{
                setPreferredWidth(20);
                setVerticalAlignment(CENTER);
                setHorizontalAlignment(CENTER);
                setIcon(dotDefault);
                addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent mouseEvent) {
                        if(I != currentPage) {
                            setSelectedPage(I);
                            for(Consumer<Integer> listener : listeners)
                                listener.accept(currentPage);
                        }
                    }
                });
            }};
            dotsPanel.add(dots[i]);
        }

        setSelectedPage(0);
    }

    public void setSelectedPage(int page){
        this.currentPage = page;
        leftArrow.setIcon(currentPage > 0 ? leftSelected : leftDefault);
        rightArrow.setIcon(currentPage < pages - 1 ? rightSelected : rightDefault);

        for(int i = 0; i < pages; i++)
            dots[i].setIcon(i == currentPage ? dotSelected : dotDefault);
    }

    private ImageIcon createIcon(int size, BufferedImage image){
        return new ImageIcon(image.getScaledInstance(size, size, Image.SCALE_SMOOTH));
    }

    public void onBlurApply(BlurParameter parameter, Component component) {
        super.onBlurApply(parameter, component);
        if(returnOnInvisible(parameter, component))
            return;
        if(component == this){
            parameter.setAdditionColor(GlassUI.Colors.buttonDefault);
            parameter.setShadowSize(5);
        }
    }
}
