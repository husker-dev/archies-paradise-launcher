package com.husker.launcher.server;

import com.husker.launcher.server.utils.ProfileUtils;

import java.util.HashMap;

public class ClientGetters {

    public static HashMap<String, Client.TextGetter> textGetters = new HashMap<String, Client.TextGetter>(){{
        put("get_key", (in, out) -> {
            Profile profile = ProfileUtils.getProfile(in);

            String key = "-1";
            if(profile != null)
                key = profile.createKey();
            out.put("key", key);
        });

        put("remove_key", (in, out) -> {
            Profile profile = ProfileUtils.getProfile(in);

            if(profile == null || in.get("key") == null)
                out.put("return", "-1");
            else {
                profile.removeKey(in.get("key"));
                out.put("return", "0");
            }
        });

        put("get_encrypted_password", (in, out) -> out.put(Profile.PASSWORD, ProfileUtils.getProfile(in).getDataValue(Profile.PASSWORD)));
        put("get_profile_data", (in, out) -> out.putAll(ProfileUtils.getProfile(in).getData(in.get("get").split(","))));

        put("set_profile_data", (in, out) -> {
            Profile profile = ProfileUtils.getProfile(in);
            if(in.containsKey(Profile.KEY)){
                in.remove(Profile.KEY);
            }else{
                in.put(Profile.CURRENT_PASSWORD, in.get(Profile.PASSWORD));
                in.remove(Profile.LOGIN);
                in.remove(Profile.PASSWORD);
            }
            out.put("result", profile.modifyData(in) + "");
        });

        put("is_login_taken", (in, out) -> out.put("result", ProfileUtils.isNicknameExist(in.get(Profile.LOGIN)) ? "1" : "0"));

        put("register", (in, out) -> out.put("result", Profile.create(in.get(Profile.LOGIN), in.get(Profile.PASSWORD)) + ""));

        put("is_email_confirmed", (in, out) -> out.put("result", ProfileUtils.getProfile(in).isEmailConfirmed() ? "1" : "0"));

        put("send_email_code", (in, out) -> out.put("result", ProfileUtils.getProfile(in).sendEmailCode(in.get(Profile.EMAIL)) + ""));

        put("confirm_mail", (in, out) -> out.put("result", ProfileUtils.getProfile(in).modifyData(in) + ""));
    }};

    public static HashMap<String, Client.ImageGetter> imageGetters = new HashMap<String, Client.ImageGetter>(){{
        put("skin", in -> ProfileUtils.getProfile(in).getSkin());
    }};
}
