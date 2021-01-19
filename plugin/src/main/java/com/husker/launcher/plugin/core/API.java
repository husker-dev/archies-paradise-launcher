package com.husker.launcher.plugin.core;

import com.husker.launcher.plugin.AAuthPlugin;
import com.husker.launcher.plugin.impl.AAuthConfig;
import com.husker.net.Get;
import com.husker.net.HttpUrlBuilder;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ConnectException;

public class API {

    private static JSONObject getJson(CharSequence url) throws IOException {
        try {
            Get get = new Get(url);
            return new JSONObject(get.getHtmlContent());
        }catch (ConnectException ex){
            AAuthPlugin.instance.getLogger().info("Can't connect to launcher server!");
            throw ex;
        }
    }

    private static String getMethodUrl(String methodName){
        return "http://" + AAuthConfig.getIP() + ":" + AAuthConfig.getPort() + "/api/method/" + methodName;
    }

    public static String authUser(String id, String ip) throws BadAuthException{
        try{
            JSONObject json = getJson(new HttpUrlBuilder(getMethodUrl("profiles.isIPBound")).set("id", id).set("ip", ip));
            if(json.has("bound") && json.getBoolean("bound"))
                return json.getString("name");
        } catch (ConnectException ignored){

        } catch (Exception ex){
            ex.printStackTrace();
        }
        throw new BadAuthException();
    }

    public static String banIP(int id){
        try{
            getJson(new HttpUrlBuilder(getMethodUrl("profiles.setGuest")).set("id", id));

        } catch (ConnectException ignored){

        } catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    public static String getIdByName(String name){
        try{
            JSONObject jsonObject = getJson(new HttpUrlBuilder(getMethodUrl("profiles.getId")).set("name", name));
            if(jsonObject.has("id"))
                return jsonObject.getString("id");
        } catch (ConnectException ignored){

        } catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    public static JSONObject getStatuses(String[] ids) throws IOException {
        return getJson(new HttpUrlBuilder(getMethodUrl("profiles.getStatuses")).set("ids", String.join(",", ids))).getJSONObject("statuses");
    }

    public static class BadAuthException extends Exception { }
}
