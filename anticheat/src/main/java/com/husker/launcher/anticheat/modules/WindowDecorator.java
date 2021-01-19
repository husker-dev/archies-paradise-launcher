package com.husker.launcher.anticheat.modules;

import com.husker.launcher.anticheat.AntiCheat;
import com.husker.launcher.anticheat.system.SystemUtils;
import com.husker.launcher.anticheat.system.Window;

public class WindowDecorator {

    private Window window;

    public WindowDecorator(AntiCheat antiCheat){
        while(antiCheat.getProcess().isAlive()){
            if(window != null && !window.isVisible())
                window = null;
            if(window == null)
                window = Window.getByProcess(antiCheat.getProcess());
            if(window != null)
                window.setTitle(antiCheat.getWindowTitle());
            SystemUtils.sleep(3000);
        }
    }
}
