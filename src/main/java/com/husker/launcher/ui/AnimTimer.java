package com.husker.launcher.ui;

import com.husker.launcher.utils.SystemUtils;

import java.awt.*;
import java.util.function.Consumer;

public class AnimTimer {

    private final Consumer<Double> event;
    private int fps;
    private long lastTime;

    public AnimTimer(Component context, Consumer<Double> event){
        this(SystemUtils.getRefreshRate(context), event);
    }

    public AnimTimer(Consumer<Double> runnable){
        this(SystemUtils.getRefreshRate(), runnable);
    }

    public AnimTimer(int fps, Consumer<Double> runnable){
        this.event = runnable;
        this.fps = fps;
        this.lastTime = System.currentTimeMillis();

        new Thread(() -> {
            while(true) {
                long currentTime = System.currentTimeMillis();
                long delta = currentTime - lastTime;
                lastTime = currentTime;

                try {
                    event.accept((double) delta / 1000.0);
                }catch (Exception ex){
                    ex.printStackTrace();
                }

                try {
                    Thread.sleep(1000 / fps);
                } catch (InterruptedException ignored) {
                }
            }
        }).start();
    }

    public int getFps(){
        return fps;
    }

    public void setFps(int fps){
        this.fps = fps;
    }
}
