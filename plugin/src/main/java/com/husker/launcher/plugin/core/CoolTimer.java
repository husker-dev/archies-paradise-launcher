package com.husker.launcher.plugin.core;

public class CoolTimer {

    private static long current_timer = 0;

    public CoolTimer(Runnable runnable, double time){
        new Thread(() -> {
            while(true){
                if(current_timer >= time) {
                    reset();
                    runnable.run();
                }
                current_timer += 10;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {}
            }
        }).start();
    }

    public void reset(){
        current_timer = 0;
    }
}
