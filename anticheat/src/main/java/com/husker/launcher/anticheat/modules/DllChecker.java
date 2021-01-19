package com.husker.launcher.anticheat.modules;

import com.husker.launcher.anticheat.AntiCheat;
import com.husker.launcher.anticheat.info.MinecraftClientInfo;
import com.husker.launcher.anticheat.system.DllInstance;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DllChecker {

    /*
        Check dlls in process (Only Windows)
    */

    private final AntiCheat antiCheat;

    public DllChecker(AntiCheat antiCheat){
        this.antiCheat = antiCheat;

        System.out.println("{java}: " + format("{java}"));
        System.out.println("{versionDirectory}: " + format("{versionDirectory}"));
        System.out.println("{appdata}: " + format("{appdata}"));

        new Thread(() -> {
            while(antiCheat.getProcess().isAlive()){
                ArrayList<DllInstance> currentDlls = new ArrayList<>(DllInstance.getProcessDlls(antiCheat.getProcess()));

                ArrayList<DllInstance> unavailable = getUnavailableDlls(currentDlls);
                System.out.println("Unavailable dlls: ");
                unavailable.forEach(System.out::println);

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public ArrayList<DllInstance> getUnavailableDlls(List<DllInstance> dlls){
        ArrayList<DllInstance> out = new ArrayList<>();
        for(DllInstance dll : dlls) {
            if (!(containsInAvailablePath(dll.path) || containsInStartsPath(dll.path)))
                out.add(dll);
        }

        HashMap<String, Integer> count = new HashMap<>();
        for(DllInstance dll : out){
            if(!count.containsKey(dll.name))
                count.put(dll.name, 1);
            else
                count.put(dll.name, count.get(dll.name) + 1);
        }
        removeIfOne(out, count, "SAPIWrapper_x64.dll");
        removeIfOne(out, count, "SAPIWrapper_x86.dll");
        removeIfOne(out, count, "lwjgl64.dll");
        removeIfOne(out, count, "lwjgl32.dll");
        removeIfOne(out, count, "OpenAL64.dll");
        removeIfOne(out, count, "OpenAL32.dll");

        return out;
    }

    private void removeIfOne(ArrayList<DllInstance> list, HashMap<String, Integer> map, String dllName){
        if(map.containsKey(dllName) && map.get(dllName) == 1) {
            for(int i = 0; i < list.size(); i++)
                if(list.get(i).name.equals(dllName))
                    list.remove(i);
        }
    }

    public boolean containsInAvailablePath(String path){
        for(String s : availableDlls){
            if(format(s).equals(path))
                return true;
        }
        return false;
    }

    public boolean containsInStartsPath(String path){
        for(String s : startWith){
            if(path.startsWith(format(s)))
                return true;
        }
        return false;
    }

    public String format(String s){
        return s.replace("{java}", antiCheat.getJavaPath().replace("/", "\\"))
                .replace("{versionDirectory}", MinecraftClientInfo.getVersionFolder(antiCheat.getClientFolder()).getAbsolutePath().replace("/", "\\"))
                .replace("{appdata}", new File(System.getenv("APPDATA")).getParentFile().getAbsolutePath().replace("/", "\\"));
    }

    public static final List<String> startWith = Arrays.asList(
            "{appdata}\\Local\\Temp\\jna-",
            "C:\\Windows\\WinSxS\\amd64_microsoft.windows.common-controls_",
            "{appdata}\\Local\\Temp\\jnigen\\",
            "C:\\Windows\\System32\\DriverStore\\FileRepository\\nv_dispi.inf_",
            "C:\\Windows\\WinSxS\\amd64_microsoft.windows.gdiplus_"
    );

    public static final List<String> availableDlls = Arrays.asList(
            "{java}\\bin\\management.dll",
            "{java}\\bin\\sunmscapi.dll",
            "{java}\\bin\\verify.dll",
            "{java}\\bin\\nio.dll",
            "{java}\\bin\\vcruntime140.dll",
            "{java}\\bin\\zip.dll",
            "{java}\\bin\\unpack.dll",
            "{java}\\bin\\unpack.dll",
            "{java}\\bin\\net.dll",
            "{java}\\bin\\sunec.dll",
            "{java}\\bin\\java.dll",
            "{java}\\bin\\jpeg.dll",
            "{java}\\bin\\msvcp140.dll",
            "{java}\\bin\\awt.dll",
            "{java}\\bin\\jvm.dll",
            "{java}\\bin\\server\\jvm.dll",
            "{java}\\bin\\t2k.dll",
            "{java}\\bin\\fontmanager.dll",

            "{versionDirectory}\\natives\\SAPIWrapper_x64.dll",
            "{versionDirectory}\\natives\\SAPIWrapper_x86.dll",
            "{versionDirectory}\\natives\\lwjgl64.dll",
            "{versionDirectory}\\natives\\lwjgl.dll",
            "{versionDirectory}\\natives\\OpenAL64.dll",
            "{versionDirectory}\\natives\\OpenAL32.dll",

            "C:\\Windows\\System32\\drprov.dll",
            "C:\\Windows\\System32\\netutils.dll",
            "C:\\Windows\\SYSTEM32\\LINKINFO.dll",
            "C:\\Windows\\System32\\DAVHLPR.dll",
            "C:\\Windows\\SYSTEM32\\cscapi.dll",
            "C:\\Windows\\system32\\NetworkExplorer.dll",
            "C:\\Windows\\System32\\wkscli.dll",
            "C:\\Windows\\System32\\ntlanman.dll",
            "C:\\Windows\\System32\\imagehlp.dll",
            "C:\\Windows\\SYSTEM32\\MPR.dll",
            "C:\\Windows\\System32\\davclnt.dll",
            "C:\\Windows\\System32\\DevDispItemProvider.dll",
            "C:\\Windows\\SYSTEM32\\gpapi.dll",
            "C:\\Windows\\System32\\EhStorAPI.dll",
            "C:\\Windows\\System32\\EhStorShell.dll",
            "C:\\Windows\\System32\\dlnashext.dll",
            "C:\\Windows\\System32\\PlayToDevice.dll",
            "C:\\Windows\\system32\\wpdshext.dll",
            "C:\\Windows\\System32\\PortableDeviceApi.dll",
            "C:\\Windows\\system32\\d3d9.dll",
            "C:\\Windows\\System32\\PSAPI.DLL",
            "C:\\Windows\\System32\\NSI.dll",
            "C:\\Windows\\SYSTEM32\\WSOCK32.dll",
            "C:\\Windows\\SYSTEM32\\VERSION.dll",
            "C:\\Windows\\System32\\rasadhlp.dll",
            "C:\\Windows\\SYSTEM32\\CRYPTBASE.dll",
            "C:\\Windows\\SYSTEM32\\HID.DLL",
            "C:\\Windows\\SYSTEM32\\ColorAdapterClient.dll",
            "C:\\Windows\\SYSTEM32\\XInput1_4.dll",
            "C:\\Windows\\SYSTEM32\\kernel.appcore.dll",
            "C:\\Windows\\System32\\winrnr.dll",
            "C:\\Windows\\SYSTEM32\\msasn1.dll",
            "C:\\Windows\\SYSTEM32\\UMPDC.dll",
            "C:\\Windows\\SYSTEM32\\WTSAPI32.dll",
            "C:\\Windows\\SYSTEM32\\resourcepolicyclient.dll",
            "C:\\Windows\\system32\\wshbth.dll",
            "C:\\Windows\\system32\\napinsp.dll",
            "C:\\Windows\\SYSTEM32\\dhcpcsvc6.DLL",
            "C:\\Windows\\SYSTEM32\\CRYPTSP.dll",
            "C:\\Windows\\system32\\pnrpnsp.dll",
            "C:\\Windows\\system32\\NLAapi.dll",
            "C:\\Windows\\SYSTEM32\\dhcpcsvc.DLL",
            "C:\\Windows\\System32\\win32u.dll",
            "C:\\Windows\\SYSTEM32\\profapi.dll",
            "C:\\Windows\\SYSTEM32\\winmmbase.dll",
            "C:\\Windows\\SYSTEM32\\WINMM.dll",
            "C:\\Windows\\System32\\bcrypt.dll",
            "C:\\Windows\\SYSTEM32\\ncrypt.dll",
            "C:\\Windows\\System32\\GDI32.dll",
            "C:\\Windows\\SYSTEM32\\Wldp.dll",
            "C:\\Windows\\SYSTEM32\\GLU32.dll",
            "C:\\Windows\\System32\\DEVOBJ.dll",
            "C:\\Windows\\SYSTEM32\\USERENV.dll",
            "C:\\Windows\\SYSTEM32\\dwmapi.dll",
            "C:\\Windows\\System32\\IMM32.DLL",
            "C:\\Windows\\SYSTEM32\\cryptnet.dll",
            "C:\\Windows\\SYSTEM32\\ntmarta.dll",
            "C:\\Windows\\system32\\rsaenh.dll",
            "C:\\Windows\\SYSTEM32\\IPHLPAPI.DLL",
            "C:\\Windows\\SYSTEM32\\dxcore.dll",
            "C:\\Windows\\SYSTEM32\\NTASN1.dll",
            "C:\\Windows\\SYSTEM32\\icm32.dll",
            "C:\\Windows\\System32\\dinput8.dll",
            "C:\\Windows\\SYSTEM32\\powrprof.dll",
            "C:\\Windows\\System32\\cfgmgr32.dll",
            "C:\\Windows\\System32\\shlwapi.dll",
            "C:\\Windows\\SYSTEM32\\WINSTA.dll",
            "C:\\Windows\\System32\\WINTRUST.dll",
            "C:\\Windows\\system32\\Oleacc.dll",
            "C:\\Windows\\system32\\mswsock.dll",
            "C:\\Windows\\System32\\WS2_32.dll",
            "C:\\Windows\\System32\\fwpuclnt.dll",
            "C:\\Windows\\System32\\bcryptprimitives.dll",
            "C:\\Windows\\System32\\MMDevApi.dll",
            "C:\\Windows\\SYSTEM32\\apphelp.dll",
            "C:\\Windows\\System32\\sechost.dll",
            "C:\\Windows\\SYSTEM32\\dsound.dll",
            "C:\\Windows\\System32\\msvcp_win.dll",
            "C:\\Windows\\System32\\msvcrt.dll",
            "C:\\Windows\\system32\\uxtheme.dll",
            "C:\\Windows\\System32\\clbcatq.dll",
            "C:\\Windows\\System32\\ADVAPI32.dll",
            "C:\\Windows\\System32\\SHCORE.dll",
            "C:\\Windows\\SYSTEM32\\mscms.dll",
            "C:\\Windows\\System32\\KERNEL32.DLL",
            "C:\\Windows\\SYSTEM32\\DNSAPI.dll",
            "C:\\Windows\\System32\\OLEAUT32.dll",
            "C:\\Windows\\System32\\CoreMessaging.dll",
            "C:\\Windows\\SYSTEM32\\PROPSYS.dll",
            "C:\\Windows\\SYSTEM32\\textinputframework.dll",
            "C:\\Windows\\System32\\ucrtbase.dll",
            "C:\\Windows\\System32\\gdi32full.dll",
            "C:\\Windows\\System32\\MSCTF.dll",
            "C:\\Windows\\SYSTEM32\\OPENGL32.dll",
            "C:\\Windows\\System32\\ole32.dll",
            "C:\\Windows\\System32\\RPCRT4.dll",
            "C:\\Windows\\SYSTEM32\\inputhost.dll",
            "C:\\Windows\\SYSTEM32\\wintypes.dll",
            "C:\\Windows\\System32\\CRYPT32.dll",
            "C:\\Windows\\SYSTEM32\\AUDIOSES.DLL",
            "C:\\Windows\\System32\\USER32.dll",
            "C:\\Windows\\SYSTEM32\\ntdll.dll",
            "C:\\Windows\\system32\\nvspcap64.dll",
            "C:\\Windows\\System32\\KERNELBASE.dll",
            "C:\\Windows\\System32\\combase.dll",
            "C:\\Windows\\System32\\CoreUIComponents.dll",
            "C:\\Windows\\System32\\SETUPAPI.dll",
            "C:\\Windows\\System32\\SHELL32.dll",
            "C:\\Windows\\SYSTEM32\\windows.storage.dll",
            "C:\\Windows\\System32\\ws2_32.DLL"
    );
}
