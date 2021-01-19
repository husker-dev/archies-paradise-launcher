package com.husker.launcher.anticheat.system;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.IntByReference;

import java.util.concurrent.atomic.AtomicReference;

public class Window {

    private static final MyUser32 user32 = MyUser32.INSTANCE;

    public static Window getByName(String name){
        WinDef.HWND hwnd = user32.FindWindow(null, name);
        if(hwnd != null)
            return new Window(hwnd);
        else
            return null;
    }

    public static Window getByProcess(Process p){
        return getByProcessID(SystemUtils.getProcessID(p));
    }

    public static Window getByProcessID(long id){
        AtomicReference<Window> window = new AtomicReference<>();

        user32.EnumWindows((User32.WNDENUMPROC) (hwnd, data) -> {
            IntByReference procId = new IntByReference();
            user32.GetWindowThreadProcessId(hwnd, procId);

            if(id == procId.getValue()){
                window.set(new Window(hwnd));
                return false;
            }
            return true;
        }, null);
        return window.get();
    }

    private final WinDef.HWND hwnd;

    public Window(WinDef.HWND hwnd){
        this.hwnd = hwnd;
    }

    public void setTitle(String title){
        user32.SetWindowTextW(hwnd, title);
    }

    public boolean isVisible(){
        return user32.IsWindowVisible(hwnd);
    }
}
