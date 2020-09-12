package com.husker.launcher.server;

import com.husker.launcher.server.utils.ConsoleUtils;

public abstract class Service {

    private boolean started = false;

    public void start(){
        ConsoleUtils.printDebug(getClass(), "Starting...");

        new Thread(this::onStart).start();

        long startTime = System.currentTimeMillis();
        while(!started){
            // Timeout is 10 sec
            if(System.currentTimeMillis() - startTime > 10 * 1000) {
                ConsoleUtils.printDebug(getClass(), "Couldn't start!");
                return;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        ConsoleUtils.printDebug(getClass(), "Started!");
    }

    public abstract void onStart();

    public void started(){
        started = true;
    }
}
