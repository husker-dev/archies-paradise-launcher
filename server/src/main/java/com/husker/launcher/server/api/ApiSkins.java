package com.husker.launcher.server.api;

import com.husker.launcher.server.core.Profile;
import com.husker.launcher.server.services.http.ApiClass;
import com.husker.launcher.server.services.http.ApiException;
import com.husker.launcher.server.services.http.ImageLink;
import com.husker.launcher.server.services.http.SimpleJSON;
import org.json.JSONObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class ApiSkins extends ApiClass {

    public JSONObject getCapes(){
        ArrayList<String> list = new ArrayList<>();

        if(Files.exists(Paths.get("./capes")))
            for (File file : new File("./capes").listFiles())
                list.add(file.getName().split("\\.")[0]);
        return SimpleJSON.create("names", list.toArray(new String[0]));
    }

    public JSONObject getElytras(){
        ArrayList<String> list = new ArrayList<>();

        if(Files.exists(Paths.get("./elytras")))
            for (File file : new File("./elytras").listFiles())
                list.add(file.getName().split("\\.")[0]);
        return SimpleJSON.create("names", list.toArray(new String[0]));
    }

    public ImageLink getCapeByName(){
        String name = getAttribute("name");

        if(Files.exists(Paths.get("./capes/" + name + ".png")))
            return new ImageLink(new File("./capes/" + name + ".png"));
        else
            throw new ApiException("Cape " + name + " doesn't exist", 1);
    }

    public ImageLink getElytraByName(){
        String name = getAttribute("name");

        if(Files.exists(Paths.get("./elytras/" + name + ".png")))
            return new ImageLink(new File("./elytras/" + name + ".png"));
        else
            throw new ApiException("Elytra " + name + " doesn't exist", 1);
    }

    public JSONObject getCategories(){
        ArrayList<String> list = new ArrayList<>();

        if(Files.exists(Paths.get("./skins")))
            for (File file : new File("./skins").listFiles(File::isDirectory))
                list.add(file.getName());
        return SimpleJSON.create("categories", list.toArray(new String[0]));
    }

    public Object getCategoryPreview(){
        String category = getAttribute("category");
        if(Files.exists(Paths.get("./skins/" + category))){
            File[] skins = Objects.requireNonNull(new File("./skins/" + category).listFiles(file -> file.getName().endsWith(".png")));
            if(skins.length > 0)
                return new ImageLink(skins[new Random().nextInt(skins.length)]);
        }else
            throw new ApiException("Category '" + category + "' doesn't exist", 1);

        BufferedImage emptySkin = new BufferedImage(64, 64, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2d = (Graphics2D) emptySkin.getGraphics();
        for(int i = 0; i < 64; i++){
            g2d.setColor(Color.getHSBColor(1f / 64f * i, 1, 1));
            g2d.fillRect(0, i, 64, 1);
        }
        return emptySkin;
    }

    public JSONObject getCategorySkins(){
        String category = getAttribute("category");
        if(Files.exists(Paths.get("./skins/" + category))){
            File[] skins = new File("./skins/" + category).listFiles(file -> file.getName().endsWith(".png"));

            ArrayList<String> list = new ArrayList<>();
            for (File skin : skins)
                list.add(skin.getName().replace(".png", ""));

            return SimpleJSON.create("skins", list.toArray(new String[0]));
        }else
            throw new ApiException("Category '" + category + "' doesn't exist", 1);
    }

    public ImageLink getCategorySkin(){
        String category = getAttribute("category");
        String name = getAttribute("name");

        if(Files.exists(Paths.get("./skins/" + category + "/" + name + ".png")))
            return new ImageLink(new File("./skins/" + category + "/" + name + ".png"));
        else
            throw new ApiException("Skin " + category + "/" + name + " doesn't exist", 1);
    }

    public ImageLink getElytra() throws IOException {
        Profile profile;

        if(containsAttribute("name")){
            try{
                profile = new Profile(Integer.parseInt(getAttribute("name")));
            }catch (Exception e){
                profile = Profile.getByName(getAttribute("name"));
            }
        }else
            throw new ApiException("Attribute \"name\" or \"id\" not specified", 2);

        if(profile != null)
            return profile.Data.getElytra();
        else
            throw new ApiException("Can't find profile", 1);
    }

    public ImageLink getCape() throws IOException {
        Profile profile;

        if(containsAttribute("name")){
            try{
                profile = new Profile(Integer.parseInt(getAttribute("name")));
            }catch (Exception e){
                profile = Profile.getByName(getAttribute("name"));
            }
        }else
            throw new ApiException("Attribute \"name\" or \"id\" not specified", 2);

        if(profile != null)
            return profile.Data.getCape();
        else
            throw new ApiException("Can't find profile", 1);
    }

    public ImageLink getSkin() throws IOException {
        Profile profile;

        if(containsAttribute("name")){
            try{
                profile = new Profile(Integer.parseInt(getAttribute("name")));
            }catch (Exception e){
                profile = Profile.getByName(getAttribute("name"));
            }
        }else
            throw new ApiException("Attribute \"name\" or \"id\" not specified", 2);

        if(profile != null)
            return profile.Data.getSkin();
        else
            throw new ApiException("Can't find profile", 1);
    }

}
