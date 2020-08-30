package com.husker.launcher.settings;

import com.husker.launcher.ui.impl.glass.GlassUI;
import com.husker.launcher.utils.settings.SettingsContainer;
import com.husker.launcher.utils.settings.SettingsFile;
import com.husker.launcher.utils.settings.SettingsSubElement;

public class LauncherConfig extends SettingsFile {

    public final Launcher Launcher = new Launcher(this);
    public final Net Net = new Net(this);
    public final Social Social = new Social(this);
    public final About About = new About(this);

    public String getTitle(){
        return get("title", "Empty title");
    }

    public static class Launcher extends SettingsSubElement {

        public Launcher(SettingsContainer parent){
            super(parent, "launcher");
        }

        public String getUI(){
            return get("ui", GlassUI.class.getCanonicalName());
        }

        public String getLogo(){
            return get("logo");
        }

        public String getIcon(){
            return get("icon");
        }
    }

    public static class Net extends SettingsSubElement{

        public final Minecraft Minecraft = new Minecraft(this);
        public final Auth Auth = new Auth(this);
        public final Internet Internet = new Internet(this);

        public Net(SettingsContainer parent) {
            super(parent, "net");
        }

        public static class Minecraft extends SettingsSubElement{

            public Minecraft(SettingsContainer parent) {
                super(parent, "minecraft");
            }

            public String getIp(){
                return get("ip", "127.0.0.1");
            }

            public int getPort(){
                try{
                    return Integer.parseInt(get("port"));
                }catch (Exception ex){
                    return 25565;
                }
            }

            public int getTimeout(){
                try{
                    return Integer.parseInt(get("timeout"));
                }catch (Exception ex){
                    return 3000;
                }
            }
        }

        public static class Auth extends SettingsSubElement{

            public Auth(SettingsContainer parent) {
                super(parent, "auth");
            }

            public String getIp(){
                return get("ip", "127.0.0.1");
            }

            public int getPort(){
                try{
                    return Integer.parseInt(get("port"));
                }catch (Exception ex){
                    return 15565;
                }
            }

            public int getTimeout(){
                try{
                    return Integer.parseInt(get("timeout"));
                }catch (Exception ex){
                    return 3000;
                }
            }

        }

        public static class Internet extends SettingsSubElement{

            public Internet(SettingsContainer parent) {
                super(parent, "internet");
            }

            public String getIp(){
                return get("ip", "google.com");
            }

            public int getTimeout(){
                try{
                    return Integer.parseInt(get("timeout"));
                }catch (Exception ex){
                    return 3000;
                }
            }

        }
    }

    public static class Social extends SettingsSubElement{

        public Social(SettingsContainer parent) {
            super(parent, "social");
        }

        public String getVkGroup(){
            return get("vkGroup");
        }

        public String getYouTube(){
            return get("youtube");
        }
    }

    public static class About extends SettingsSubElement{

        public Owner Owner = new Owner(this);
        public Support Support = new Support(this);

        public About(SettingsContainer parent) {
            super(parent, "about");
        }

        public static class Owner extends SettingsSubElement{

            public Owner(SettingsContainer parent) {
                super(parent, "owner");
            }

            public String getName(){
                return get("name");
            }

            public String getURL(){
                return get("url");
            }
        }

        public static class Support extends SettingsSubElement{

            public Support(SettingsContainer parent) {
                super(parent, "support");
            }

            public String getName(){
                return get("name");
            }

            public String getURL(){
                return get("url");
            }
        }
    }

    public LauncherConfig() {
        super("launcher_config.cfg");
    }
}
