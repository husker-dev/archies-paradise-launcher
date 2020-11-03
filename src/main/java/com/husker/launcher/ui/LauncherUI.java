package com.husker.launcher.ui;

import com.husker.launcher.utils.ConsoleUtils;
import com.husker.launcher.Launcher;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public abstract class LauncherUI extends JPanel {

    private final Launcher launcher;
    private final HashMap<String, Screen> screens = new HashMap<>();
    private String currentScreen;

    private String nextScreen;
    private Screen.Parameters nextScreenParameters;
    private boolean animated = false;

    public LauncherUI(Launcher launcher){
        this.launcher = launcher;

        setOpaque(false);
        setBackground(new Color(0, 0, 0, 0));
        setLayout(new BorderLayout());
    }

    public abstract void onInit();
    public abstract Dimension getDefaultSize();

    public Launcher getLauncher(){
        return launcher;
    }

    @SafeVarargs
    public final void addScreen(Class<? extends Screen>... screens){
        for(Class<? extends Screen> screen : screens) {
            try {
                addScreen(screen.getConstructor().newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addScreen(Screen... screens){
        for(Screen screen : screens)
            addScreen(screen);
    }

    public void addScreen(Screen screen){
        addScreen(screen.getClass().getSimpleName(), screen);
    }

    public void addScreen(String name, Screen screen){
        ConsoleUtils.printDebug(getClass(), "Initializing screen: " + name);

        try {
            screen.setName(name);
            screen.setLauncherUI(this);
            screen.onInit();
            screen.doLayout();
            screens.put(name, screen);

            ConsoleUtils.printResult("OK");
        }catch (Exception ex){
            ConsoleUtils.printResult("ERROR");
            ex.printStackTrace();
        }
    }

    public String getScreen(Class<? extends Screen> screenClass){
        for(Map.Entry<String, Screen> entry : screens.entrySet())
            if (entry.getValue().getClass() == screenClass)
                return entry.getKey();
        return null;
    }

    public void setScreen(Class<? extends Screen> screenClass){
        setScreen(getScreen(screenClass));
    }

    public void setScreen(Class<? extends Screen> screenClass, Screen.Parameters parameters){
        setScreen(getScreen(screenClass), parameters);
    }

    public void setScreen(String name){
        setScreen(name, new Screen.Parameters());
    }

    public void setScreen(String name, String parameters){
        setScreen(name, new Screen.Parameters(parameters));
    }

    public void setScreen(String name, Screen.Parameters parameters){
        if(!screens.containsKey(name))
            throw new NullPointerException("Can't find screen: " + name);

        nextScreen = name;
        nextScreenParameters = parameters;
        ConsoleUtils.printDebug(getClass(), "Changing screen to: " + name + "  " + parameters.toString());

        if(animated)
            getLauncher().beginAnimation();
        else {
            applyNextScreen();
            getLauncher().repaint();
        }
    }

    public String getScreenName(){
        return currentScreen;
    }

    public Screen getScreen(){
        return screens.get(currentScreen);
    }

    public void setBackgroundImage(BufferedImage image){
        launcher.setBackgroundImage(image);
    }

    public boolean isAnimated() {
        return animated;
    }

    public void setAnimated(boolean animation) {
        this.animated = animation;
    }

    public String getNextScreen() {
        return nextScreen;
    }

    public void applyNextScreen() {
        this.removeAll();
        currentScreen = nextScreen;
        if(!screens.containsKey(nextScreen))
            return;

        add(screens.get(nextScreen));
        screens.get(nextScreen).doLayout();
        screens.get(nextScreen).setParameters(nextScreenParameters);
        try {
            screens.get(nextScreen).onShow();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        this.nextScreen = null;
    }

}
