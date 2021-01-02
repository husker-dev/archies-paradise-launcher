package com.husker.glassui.components;

import com.husker.glassui.GlassUI;
import com.husker.launcher.Resources;
import com.husker.launcher.ui.components.MLabel;
import com.husker.launcher.ui.components.TransparentPanel;
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
    private MLabel[] dots = new MLabel[0];
    private final MLabel leftArrow, rightArrow;
    private int pages = 0;
    private int currentPage = 0;

    private final ArrayList<Consumer<Integer>> listeners = new ArrayList<>();

    private final BufferedImage leftDefault = Resources.Icon_Arrow_Left;
    private final BufferedImage leftSelected = Resources.Icon_Arrow_Left_Selected;

    private final BufferedImage rightDefault = Resources.Icon_Arrow_Right;
    private final BufferedImage rightSelected = Resources.Icon_Arrow_Right_Selected;

    private final BufferedImage dotDefault = Resources.Icon_Dot;
    private final BufferedImage dotSelected = Resources.Icon_Dot_Selected;

    public BlurPagePanel(Screen screen){
        super(screen, true);

        setPreferredHeight(35);
        setMargin(6, 0, 0, 0);
        setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));

        add(leftArrow = new MLabel(){{
            setVerticalAlignment(CENTER);
            setHorizontalAlignment(CENTER);
            setPreferredWidth(40);
            setImageSize(21);

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

        add(rightArrow = new MLabel(){{
            setVerticalAlignment(CENTER);
            setHorizontalAlignment(CENTER);
            setPreferredWidth(40);
            setImageSize(21);

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

        dots = new MLabel[pages];
        dotsPanel.removeAll();
        for(int i = 0; i < pages; i++){
            final int I = i;
            dots[i] = new MLabel(){{
                setPreferredWidth(20);
                setVerticalAlignment(CENTER);
                setHorizontalAlignment(CENTER);
                setImageSize(13);
                setImage(dotDefault);
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
        leftArrow.setImage(currentPage > 0 ? leftSelected : leftDefault);
        rightArrow.setImage(currentPage < pages - 1 ? rightSelected : rightDefault);

        for(int i = 0; i < pages; i++)
            dots[i].setImage(i == currentPage ? dotSelected : dotDefault);
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
