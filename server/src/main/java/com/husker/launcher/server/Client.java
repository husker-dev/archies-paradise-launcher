package com.husker.launcher.server;

import com.husker.launcher.server.utils.*;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Client {

    private final Socket socket;

    public Client(Socket client){
        this.socket = client;
        String ip = client.getInetAddress().getHostAddress();

        try {
            GetRequest received = GetRequest.create(socket);

            if(received.has("method") && received.getString("method").equals("client.get")){
                String name = received.getString("name");
                if(name.equals("other"))
                    sendFile(UpdateManager.clientFolder + "/other.zip");
                if(name.equals("mods"))
                    sendFile(UpdateManager.clientFolder + "/mods.zip");
                if(name.equals("versions"))
                    sendFile(UpdateManager.clientFolder + "/versions.zip");
                disconnect();
                return;
            }

            GetRequest outParameters = GetRequest.createWithTitle(received.getTitle());

            try {
                received.put("$request_ip", ip);
                textGetters.get(received.getTitle()).apply(received, outParameters);
            }catch (Exception ex){
                outParameters.put(Profile.RESULT, "-1");
                ex.printStackTrace();
            }
            if(!outParameters.containsKey(Profile.RESULT))
                outParameters.put(Profile.RESULT, "0");

            outParameters.send(socket);
        }catch (Exception ex){
            if(ex.getMessage() != null && !ex.getMessage().equals("null"))
                ConsoleUtils.printDebug(Client.class, ip + " [ERROR] " + ex.getMessage());
        }
        disconnect();
    }

    public void sendFile(String path){
        try {
            GetRequest.sendText(socket, new File(path).length() + "");

            OutputStream out = socket.getOutputStream();
            FileInputStream fis = new FileInputStream(new File(path));
            byte[] buffer = new byte[1024];

            int len;
            while ((len = fis.read(buffer)) > 0)
                out.write(buffer, 0, len);

            fis.close();
            out.close();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void disconnect(){
        try {
            socket.close();
        }catch (Exception ex){}
        try {
            socket.getInputStream().close();
        }catch (Exception ex){}
        try {
            socket.getOutputStream().close();
        }catch (Exception ex){}
    }

    public interface TextGetter{
        void apply(GetRequest in, GetRequest out);
    }

    public static HashMap<String, TextGetter> textGetters = new HashMap<String, Client.TextGetter>(){{
        put("auth.getAccessToken", (in, out) -> {
            Profile profile = ProfileUtils.getProfile(in, true);

            if(profile != null)
                out.put("access_token", profile.createKey());
            else
                out.put("result", "-1");
        });

        put("auth.create", (in, out) -> out.put("result", Profile.create(in.getString(Profile.LOGIN), in.getString(Profile.PASSWORD)) + ""));

        put("profile.getData", (in, out) -> out.put("data", ProfileUtils.getProfile(in).getData(in.getString("fields").split(","))));

        put("profile.setData", (in, out) -> {
            out.put("result", ProfileUtils.getProfile(in).setData(in.getJSONObject("fields"), in.getJSONObject("confirms")));
        });

        put("profile.isLoginTaken", (in, out) -> out.put("result", ProfileUtils.isNicknameExist(in.getString(Profile.LOGIN)) ? "1" : "0"));

        put("profile.isEmailConfirmed", (in, out) -> out.put("result", ProfileUtils.getProfile(in).isEmailConfirmed() ? "1" : "0"));

        put("profile.sendEmailCode", (in, out) -> out.put("result", ProfileUtils.getProfile(in).sendEmailCode(in.getString(Profile.EMAIL)) + ""));

        put("profile.confirmEmail", (in, out) -> {
            Profile profile = ProfileUtils.getProfile(in);
            if(!profile.data.get(Profile.EMAIL).equals("null")){
                out.put("result", 1);
                return;
            }

            if(profile.isValidEmailCode(in.getString(Profile.EMAIL), in.getString(Profile.EMAIL_CODE)))
                profile.data.set(Profile.EMAIL, in.getString(Profile.EMAIL));
        });

        put("profile.getSkin", (in, out) -> {
            out.put("skin", ProfileUtils.getProfile(in).getSkin());
        });

        put("profile.setSkin", (in, out) -> {
            Profile profile = ProfileUtils.getProfile(in);
            try {
                BufferedImage skin;

                if (in.containsKey("category") && in.containsKey("name"))
                    skin = ImageIO.read(new File("./skins/" + in.get("category") + "/" + in.get("name") + ".png"));
                else if(in.containsKey("skin"))
                    skin = in.getImage("skin");
                else {
                    out.put("result", "1");
                    return;
                }

                if(skin == null || skin.getWidth() != 64 || skin.getHeight() != 64){
                    out.put("result", "2");
                    return;
                }

                ImageIO.write(skin, "png", new File(profile.getFolder() + "/skin.png"));
                profile.setData(Profile.SKIN_URL, ProfileUtils.getImageUrl(skin));
            }catch (Exception ex){
                ex.printStackTrace();
                out.put("result", "-1");
            }
        });

        put("skins.getCategories", (in, out) -> {
            try {
                if(!Files.exists(Paths.get("./skins")))
                    Files.createDirectory(Paths.get("./skins"));
            }catch (Exception ex){}
            StringBuilder folders = new StringBuilder();
            for(File file : Objects.requireNonNull(new File("./skins").listFiles(File::isDirectory)))
                folders.append(file.getName()).append(",");
            out.put("categories", folders.substring(0, folders.lastIndexOf(",")));
        });

        put("skins.getCategoryPreview", (in, out) -> {
            if(Files.exists(Paths.get("./skins/" + in.get("category")))){
                File[] skins = Objects.requireNonNull(new File("./skins/" + in.get("category")).listFiles(file -> file.getName().endsWith(".png")));
                if(skins.length > 0){
                    try {
                        out.put("skin", ImageIO.read(skins[new Random().nextInt(skins.length)]));
                        return;
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            }
            out.put("result", "-1");
        });

        put("skins.getCategorySkins", (in, out) -> {
            if(Files.exists(Paths.get("./skins/" + in.get("category")))){
                File[] skins = Objects.requireNonNull(new File("./skins/" + in.get("category")).listFiles(file -> file.getName().endsWith(".png")));
                if(skins.length > 0){
                    try {
                        StringBuilder list = new StringBuilder();
                        for(int i = 0; i < skins.length; i++){
                            list.append(skins[i].getName().replace(".png", ""));
                            if(i != skins.length - 1)
                                list.append(",");
                        }
                        out.put("skins", list);
                        out.put("result", "0");
                        return;
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            }
            out.put("result", "-1");
        });

        put("skins.getSkin", (in, out) -> {
            if(Files.exists(Paths.get("./skins/" + in.get("category")))){
                File[] skins = Objects.requireNonNull(new File("./skins/" + in.get("category")).listFiles(file -> file.getName().endsWith(".png")));
                if(skins.length > 0){
                    try {
                        for(File file : skins) {
                            if (file.getName().replace(".png", "").equals(in.getString("name"))) {
                                out.put("name", in.getString("name"));
                                out.put("skin", ImageIO.read(file));
                                out.put("result", "0");
                                return;
                            }
                        }
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            }
            out.put("result", "-1");
        });


        put("vk.getInfo", (in, out) -> {
            out.put("id", ServerMain.Settings.getVkGroupId());
        });

        put("vk.setInfo", (in, out) -> {
            Profile profile = ProfileUtils.getProfile(in);
            if(profile.getDataValue(Profile.STATUS).equals("Администратор") && in.has("id"))
                ServerMain.Settings.setVkGroupId(in.getString("id"));
        });

        put("youtube.getInfo", (in, out) -> {
            out.put("id", ServerMain.Settings.getYoutubeId());
        });

        put("youtube.setInfo", (in, out) -> {
            Profile profile = ProfileUtils.getProfile(in);
            if(profile.getDataValue(Profile.STATUS).equals("Администратор") && in.has("id"))
                ServerMain.Settings.setYoutubeId(in.getString("id"));
        });

        put("client.getInfo", (in, out) -> {
            try {
                JSONObject object = new JSONObject(FileUtils.readFileToString(new File(UpdateManager.clientFolder + "/client_info.json"), StandardCharsets.UTF_8));
                out.put("build", object.get("build") + "");
                out.put("build_id", object.get("build_id") + "");
                out.put("version", object.get("version") + "");
            }catch (Exception ex){
                ex.printStackTrace();
            }
        });

        put("client.getModsInfo", (in, out) -> {
            try {
                JSONObject object = new JSONObject(FileUtils.readFileToString(new File(UpdateManager.clientFolder + "/client_info.json"), StandardCharsets.UTF_8));
                JSONArray mods = object.getJSONArray("mods");
                boolean has_icon = in.has("icon") && in.getBoolean("icon");

                ArrayList<JSONObject> objects = new ArrayList<>();
                if (in.has("count")) {
                    if (has_icon) {
                        for (int i = 0; objects.size() < in.getInt("count") && i < mods.length(); i++)
                            if (!mods.getJSONObject(i).getString("icon").equals("null"))
                                objects.add(mods.getJSONObject(i));
                    } else {
                        for (int i = 0; i < in.getInt("count"); i++)
                            objects.add(mods.getJSONObject(i));
                    }
                } else if (in.has("index")) {
                    if (has_icon) {
                        int remain = in.getInt("index");
                        for (int i = 0; i < mods.length(); i++) {
                            if (!mods.getJSONObject(i).getString("icon").equals("null")) {
                                if (remain == 0) {
                                    objects.add(mods.getJSONObject(i));
                                    break;
                                } else
                                    remain--;
                            }
                        }
                    } else {
                        objects.add(mods.getJSONObject(in.getInt("index")));
                    }
                }
                out.put("mods", objects);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        });

        put("client.getFilesInfo", (in, out) -> {
            try {
                JSONObject object = new JSONObject(IOUtils.readFileText(UpdateManager.clientFolder + "/files_info.json"));
                out.put("info", object);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        put("profile.bindIp", (in, out) -> {
            Profile profile = ProfileUtils.getProfile(in);
            profile.setIP(in.getString("$request_ip"));
        });

        put("profiles.isIpBound", (in, out) -> out.put("result", ProfileUtils.isValidIp(in.getString("name"), in.getString("ip"))));

        put("profiles.checkFormat", (in, out) -> {
            JSONObject fields = in.getJSONObject("fields");
            JSONObject out_fields = new JSONObject();

            if(fields.has(Profile.LOGIN))
                out_fields.put(Profile.LOGIN, FormatUtils.isCorrectName(fields.getString(Profile.LOGIN)));
            if(fields.has(Profile.PASSWORD))
                out_fields.put(Profile.PASSWORD, FormatUtils.isCorrectName(fields.getString(Profile.PASSWORD)));
            if(fields.has(Profile.EMAIL))
                out_fields.put(Profile.EMAIL, FormatUtils.isCorrectName(fields.getString(Profile.EMAIL)));
            out.put("fields", out_fields);
        });

        put("checksum", (in, out) -> {
            try {
                JSONObject object = new JSONObject(FileUtils.readFileToString(new File(UpdateManager.clientFolder + "/client_info.json"), StandardCharsets.UTF_8));
                out.put("equal_mods", true);
                out.put("equal_client", true);

                if(!in.has("mods") || !object.getString("mods_MD5").equals(in.getString("mods")))
                    out.put("equal_mods", false);
                if(!in.has("client") || !object.getString("client_MD5").equals(in.getString("client")))
                    out.put("equal_client", false);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        });
    }};
}
