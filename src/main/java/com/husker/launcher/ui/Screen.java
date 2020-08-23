package com.husker.launcher.ui;

import com.husker.launcher.LauncherWindow;
import com.husker.launcher.ui.blur.BlurPainter;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.ui.blur.BlurSegment;
import com.husker.launcher.ui.shadow.ShadowSegment;
import com.husker.launcher.utils.ConsoleUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public abstract class Screen extends JPanel {

    private LauncherUI launcherUI;
    private final ArrayList<BlurPainter> blurPainters = new ArrayList<>();
    private final ArrayList<ShadowSegment> shadowSegments = new ArrayList<>();
    private String[] parameters;

    public Screen(){
        setOpaque(false);
        setBackground(new Color(0, 0, 0, 0));
    }

    public abstract void onInit();

    public void onShow(){
    }

    public void addBlurSegment(String name, BlurSegment segment){
        blurPainters.add(new BlurPainter(getLauncher(), segment, name));
    }

    public void removeBlurSegment(BlurSegment segment){
        BlurPainter toRemove = null;
        for(BlurPainter painter : blurPainters) {
            if (painter.getBlurSegment() == segment) {
                toRemove = painter;
                break;
            }
        }
        if(toRemove != null)
            blurPainters.remove(toRemove);

        ConsoleUtils.printDebug(getClass(), "Blur segment was removed");
    }

    public void removeBlurSegment(BlurParameter parameter){
        removeBlurSegment(parameter.getBlurSegment());
    }

    public void addShadowSegment(ShadowSegment segment){
        shadowSegments.add(segment);
    }

    public BlurPainter[] getBlurPainters(){
        return blurPainters.toArray(new BlurPainter[0]);
    }

    public ShadowSegment[] getShadowSegments(){
        return shadowSegments.toArray(new ShadowSegment[0]);
    }

    public LauncherUI getLauncherUI() {
        return launcherUI;
    }

    public void setLauncherUI(LauncherUI launcherUI) {
        this.launcherUI = launcherUI;
    }

    public LauncherWindow getLauncher(){
        return getLauncherUI().getLauncher();
    }

    public String[] getParameters() {
        return parameters;
    }

    public String[] getParameters(String... addition) {
        ArrayList<String> parameters = new ArrayList<>(Arrays.asList(getParameters()));
        parameters.addAll(Arrays.asList(addition));
        return parameters.toArray(new String[0]);
    }

    public void setParameters(String[] parameters) {
        this.parameters = parameters;
    }

    public void doCaching(){
        for(BlurPainter painter : blurPainters)
            painter.doCaching();
    }
}
