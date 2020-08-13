package com.husker.launcher.ui;

import com.husker.launcher.LauncherWindow;
import com.husker.launcher.ui.blur.BlurPainter;
import com.husker.launcher.ui.blur.BlurSegment;
import com.husker.launcher.ui.shadow.ShadowSegment;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public abstract class Screen extends JPanel {

    private LauncherUI launcherUI;
    private final ArrayList<BlurPainter> blurSegments = new ArrayList<>();
    private final ArrayList<ShadowSegment> shadowSegments = new ArrayList<>();
    private String[] parameters;

    public Screen(){
        setOpaque(false);
        setBackground(new Color(0, 0, 0, 0));
    }

    public abstract void onInit();

    public void onShow(){
    }

    public void addBlurSegment(BlurSegment segment){
        blurSegments.add(new BlurPainter(getLauncher(), segment));
    }

    public void addShadowSegment(ShadowSegment segment){
        shadowSegments.add(segment);
    }

    public BlurPainter[] getBlurPainters(){
        return blurSegments.toArray(new BlurPainter[0]);
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
        for(BlurPainter painter : blurSegments)
            painter.doCaching();
    }
}
