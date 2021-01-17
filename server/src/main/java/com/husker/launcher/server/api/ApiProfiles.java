package com.husker.launcher.server.api;

import com.husker.launcher.server.core.Profile;
import com.husker.launcher.server.core.ProfileUtils;
import com.husker.launcher.server.services.http.ApiClass;
import com.husker.launcher.server.services.http.ApiException;
import com.husker.launcher.server.services.http.SimpleJSON;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Objects;

public class ApiProfiles extends ApiClass {



    public JSONObject isIPBound(){
        int id = Integer.parseInt(getAttribute("id"));
        JSONObject object = new JSONObject();
        boolean bound = new Profile(id).IP.get().equals(getAttribute("ip"));
        object.put("bound", bound);
        if(bound)
            object.put("name", new Profile(id).data.get(Profile.LOGIN));
        return object;
    }

    public JSONObject getStatuses(){
        JSONObject out = new JSONObject();

        String[] playersIds = getAttribute("ids").split(",");
        for(String id : playersIds) {
            try {
                if (ProfileUtils.isExist(Integer.parseInt(id)))
                    out.put(id, new Profile(Integer.parseInt(id)).Data.getValue(Profile.STATUS));
            }catch (Exception ignored){}
        }

        return new SimpleJSON("statuses", out);
    }

    public JSONObject getId(){
        return new SimpleJSON("id", Objects.requireNonNull(Profile.getByName(getAttribute("name"))).getId());
    }

    public JSONObject getUUID(){
        return new SimpleJSON("uuid", Objects.requireNonNull(Profile.getByName(getAttribute("name"))).data.get("uuid"));
    }

    public JSONObject isExist(){
        Profile profile;
        if(containsAttribute(Profile.LOGIN))
            profile = Profile.getByName(getAttribute(Profile.LOGIN));
        else if(containsAttribute(Profile.EMAIL))
            profile = Profile.getByEmail(getAttribute(Profile.EMAIL));
        else
            throw new ApiException("Attributes \"name\" or \"email\" not specified", 1);
        JSONObject object = new SimpleJSON("exist", profile != null);
        if(profile != null){
            object.put("id", profile.getId());
            object.put("name", profile.Data.getValue(Profile.LOGIN));
            object.put("email_bound", !profile.Data.getValue(Profile.EMAIL).equals("null"));
        }
        return object;
    }

    public JSONObject searchProfiles(Profile profile){
        profile.checkStatus("Администратор");
        String search = "";
        if(containsAttribute("search"))
            search = getAttribute("search");

        JSONArray info = new JSONArray();
        if(!search.isEmpty()){
            for(Profile p : Profile.getProfiles()){
                String name = p.Data.getValue(Profile.LOGIN);
                int id = p.getId();

                if(name.toLowerCase().contains(search.toLowerCase()) || (id + "").contains(search))
                    info.put(new JSONObject().put("id", p.getId()).put("name", p.Data.getValue(Profile.LOGIN)));
            }
        }else {
            for(Profile p : Profile.getProfiles())
                info.put(new JSONObject().put("id", p.getId()).put("name", p.Data.getValue(Profile.LOGIN)));
        }
        return new SimpleJSON("info", info);
    }

    public JSONObject getProfileInfo(Profile profile){
        profile.checkStatus("Администратор");
        int id = Integer.parseInt(getAttribute("id"));
        String[] fields = getAttribute("fields").split(",");
        if(!ProfileUtils.isExist(id))
            throw new ApiException("Profile doesn't exist", 1);

        Profile found = new Profile(id);
        JSONObject info = new JSONObject();
        for(String field : fields)
            if(found.data.containsVar(field))
                info.put(field, found.data.get(field));
        return new SimpleJSON("info", info);
    }

    public void setStatus(Profile profile){
        profile.checkStatus("Администратор");
        int id = Integer.parseInt(getAttribute("id"));
        String status = getAttribute("status");

        if(!ProfileUtils.isExist(id))
            throw new ApiException("Can't find profile with id " + id, 1);
        Profile p = new Profile(id);
        p.data.set(Profile.STATUS, status);
    }

    public void resetEmail(Profile profile){
        profile.checkStatus("Администратор");
        int id = Integer.parseInt(getAttribute("id"));
        if(!ProfileUtils.isExist(id))
            throw new ApiException("Can't find profile with id " + id, 1);
        new Profile(id).Email.reset();
    }

    public void resetPassword(Profile profile){
        profile.checkStatus("Администратор");
        int id = Integer.parseInt(getAttribute("id"));
        if(!ProfileUtils.isExist(id))
            throw new ApiException("Can't find profile with id " + id, 1);
        new Profile(id).Password.reset();
    }

    public void removeProfile(Profile profile){
        profile.checkStatus("Администратор");
        int id = Integer.parseInt(getAttribute("id"));
        if(!ProfileUtils.isExist(id))
            throw new ApiException("Can't find profile with id " + id, 1);
        new Profile(id).remove();
    }

    public void sendPasswordChangeCode(){
        int id = Integer.parseInt(getAttribute("id"));
        Profile profile = new Profile(id);
        profile.Email.sendPasswordChangeCode(profile.data.get(Profile.EMAIL));
    }

    public JSONObject generateChangeHash(){
        int id = Integer.parseInt(getAttribute("id"));
        String code = getAttribute("code");

        Profile profile = new Profile(id);
        if(profile.Email.useCode(code))
            return new SimpleJSON("hash", profile.Password.createChangeHash());
        else
            throw new ApiException("Wrong email code", 1);
    }

    public void changePassword(){
        int id = Integer.parseInt(getAttribute("id"));
        String hash = getAttribute("hash");
        String password = getAttribute("password");
        String token = null;
        if(containsAttribute(Profile.ACCESS_TOKEN))
            token = getAttribute(Profile.ACCESS_TOKEN);

        Profile profile = new Profile(id);
        profile.Password.change(hash, password, token);
    }

    public void setGuest(){
        int id = Integer.parseInt(getAttribute("id"));

        Profile profile = new Profile(id);
        profile.data.set(Profile.STATUS, "Гость");
    }
}
