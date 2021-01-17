package com.husker.launcher.ui;

import com.husker.launcher.utils.SystemUtils;

import java.awt.*;
import java.util.function.Consumer;

public class AnimationTimer {

    private final Consumer<Double> event;
    private int fps;
    private long lastTime;

    public AnimationTimer(Component context, Consumer<Double> event){
        this(SystemUtils.getRefreshRate(context), event);
    }

    public AnimationTimer(Consumer<Double> runnable){
        this(SystemUtils.getRefreshRate(), runnable);
    }

    public AnimationTimer(int fps, Consumer<Double> runnable){
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

                SystemUtils.sleep(1000 / fps);
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
