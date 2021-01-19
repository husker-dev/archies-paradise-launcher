package com.husker.launcher.anticheat.system;

import com.sun.jna.Native;
import com.sun.jna.Pointer;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public interface MyUser32 extends User32 {
    public static MyUser32 INSTANCE = Native.load("user32", MyUser32.class, W32APIOptions.UNICODE_OPTIONS);

    long SetWindowLongPtr(WinDef.HWND hWnd, int nIndex, long ln);
    int SetWindowTextW(HWND hWnd, String title);
    boolean ShowWindow(WinDef.HWND hWnd, int  nCmdShow);
    WinDef.HWND FindWindow(String className, String windowName);
    WinDef.BOOL SetWindowTextA(
            WinDef.HWND hWnd,
            String lpString
    );
    boolean SetWindowText(WinDef.HWND hwnd, String newText);

    interface WNDENUMPROC extends StdCallLibrary.StdCallCallback {
        boolean callback(Pointer hWnd, Pointer arg);
    }

    boolean EnumWindows(WNDENUMPROC lpEnumFunc, Pointer arg);
}
