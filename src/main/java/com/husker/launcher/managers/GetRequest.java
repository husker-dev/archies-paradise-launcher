package com.husker.launcher.managers;


import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class GetRequest extends JSONObject{

    public static final String REQUEST_TAG = "request";

    private GetRequest(String text){
        super(text);
    }

    private GetRequest(){
    }

    public boolean containsKey(String key){
        return opt(key) != null;
    }

    public BufferedImage getImage(String key){
        try {
            return fromBase64(getString(key));
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    public void put(String key, BufferedImage image){
        try {
            put(key, toBase64(image));
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public String getTitle(){
        return getString(REQUEST_TAG);
    }

    public void putAll(HashMap<String, ?> map){
        for(Map.Entry<String, ?> entry : map.entrySet())
            put(entry.getKey(), entry.getValue());
    }

    public HashMap<String, String> toStringMap(){
        HashMap<String, String> map = new HashMap<>();
        for(Map.Entry<String, Object> entry : toMap().entrySet())
            map.put(entry.getKey(), entry.getValue().toString());
        return map;
    }

    public static GetRequest create(String text){
        return new GetRequest(text);
    }

    public static GetRequest createWithTitle(String title, Object... parameters){
        return new GetRequest(){{
            put(REQUEST_TAG, title);
            for(int i = 0; i < parameters.length; i+= 2)
                put(parameters[i].toString(), parameters[i + 1]);
        }};
    }

    public void send(Socket socket) throws IOException {
        sendText(socket, toString());
    }

    public static GetRequest create(Socket socket) throws IOException {
        return create(receiveText(socket));
    }

    private static void sendText(Socket socket, String text) throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

        out.write(text + "\n");
        out.flush();
    }

    private static String receiveText(Socket socket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        return in.readLine();
    }

    public static String toBase64(BufferedImage image) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", stream);
            stream.flush();
            byte[] imageBytes = stream.toByteArray();
            stream.close();

            return Base64.getEncoder().encodeToString(imageBytes);
        }catch (Exception ex){
            return null;
        }
    }

    public static BufferedImage fromBase64(String text) {
        try {
            ByteArrayInputStream stream = new ByteArrayInputStream(Base64.getDecoder().decode(text));
            BufferedImage image = ImageIO.read(stream);
            stream.close();

            return image;
        }catch (Exception ex){
            return null;
        }
    }
}