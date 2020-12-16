package com.husker.launcher.server.api;

import com.husker.launcher.server.core.Profile;
import com.husker.launcher.server.services.HttpService;
import com.husker.launcher.server.services.http.*;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

public class ApiProfile extends ApiClass {

    public JSONObject getData(Profile profile){
        return SimpleJSON.create("data", profile.getData(getAttribute("fields").split(",")));
    }

    public void setData(Profile profile){
        HashMap<String, String> fields = getAttributeMap();
        HashMap<String, String> confirms = new HashMap<String, String>(){{
            if(fields.containsKey(Profile.EMAIL_CODE)) {
                put(Profile.EMAIL_CODE, getAttribute(Profile.EMAIL_CODE));
                fields.remove(Profile.EMAIL_CODE);
            }
            if(fields.containsKey(Profile.CURRENT_PASSWORD)) {
                put(Profile.CURRENT_PASSWORD, getAttribute(Profile.CURRENT_PASSWORD));
                fields.remove(Profile.CURRENT_PASSWORD);
            }
        }};
        profile.setData(fields, confirms);
    }

    public JSONObject isEmailConfirmed(Profile profile){
        return SimpleJSON.create("confirmed", profile.isEmailConfirmed());
    }

    public void sendEmailCode(Profile profile) throws IOException {
        String email = containsAttribute(Profile.EMAIL) ? getAttribute(Profile.EMAIL) : profile.data.get(Profile.EMAIL);
        if(!profile.sendEmailCode(email))
            throw new ApiException("Can't send email code", 1);
    }

    public void confirmEmail(Profile profile){
        if(!profile.data.get(Profile.EMAIL).equals("null"))
            throw new ApiException("Email is empty", 1);

        if(profile.isValidEmailCode(getAttribute(Profile.EMAIL), getAttribute(Profile.EMAIL_CODE)))
            profile.data.set(Profile.EMAIL, getAttribute(Profile.EMAIL));
        else
            throw new ApiException("Email code is not valid", 2);
    }

    public void bindIP(Profile profile){
        profile.setIP(getExchange().getRemoteAddress().getHostName());
    }

    public ImageLink getSkin(Profile profile){
        return profile.getSkin();
    }

    public void setSkin(Profile profile) throws IOException {
        BufferedImage skin;

        if (containsAttribute("category") && containsAttribute("name")) {
            String category = getAttribute("category");
            String name = getAttribute("name");

            if(!Files.exists(Paths.get("./skins/" + category)))
                throw new ApiException("Category '" + category + "' doesn't exist", 1);
            if(!Files.exists(Paths.get("./skins/" + category + "/" + name + ".png")))
                throw new ApiException("Skin '" + name + "' doesn't exist in category '" + category + "'", 2);

            skin = ImageIO.read(new File("./skins/" + category + "/" + name + ".png"));
        }else if(containsAttribute("skin"))
            skin = HttpService.fromBase64(getAttribute("skin"));
        else
            throw new AttributeNotFoundException("Can't find attributes (category, name) or (skin)");

        if(skin == null || skin.getWidth() != 64 || skin.getHeight() != 64)
            throw new ApiException("Image is too large. Preferred skin size: 64x64", 3);

        ImageIO.write(skin, "png", new File(profile.getFolder() + "/skin.png"));
    }
}
