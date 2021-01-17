package com.husker.launcher.server.api;

import com.husker.launcher.server.core.Client;
import com.husker.launcher.server.core.Profile;
import com.husker.launcher.server.services.HttpService;
import com.husker.launcher.server.services.http.ApiClass;
import com.husker.launcher.server.services.http.ApiException;
import com.husker.launcher.server.services.http.ImageLink;
import com.husker.launcher.server.services.http.SimpleJSON;
import com.husker.launcher.server.utils.IOUtils;
import com.husker.mio.FSUtils;
import com.husker.mio.MIO;
import com.husker.mio.ProgressArguments;
import com.husker.mio.processes.DeletingProcess;
import com.husker.mio.processes.UnzippingProcess;
import org.apache.commons.fileupload.MultipartStream;
import org.json.JSONObject;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.function.Consumer;

public class ApiClients extends ApiClass {

    public void update(Profile profile) throws Exception {
        profile.checkStatus("Администратор");

        String name = getAttribute("name");
        String id = getAttribute("id");

        String zipPath = "./received_clients_tmp/" + id + ".zip";
        String unzippedPath = "./received_clients_tmp/" + id + "_unzipped";
        String clientPath = "./received_clients_tmp/" + id;

        MIO.delete(clientPath);
        MIO.delete(unzippedPath);
        MIO.delete(zipPath);

        Files.createDirectories(Paths.get(clientPath));

        String boundary = getExchange().getRequestHeaders().getFirst("Content-type").split("boundary=")[1].split(" ")[0];
        MultipartStream multipartStream = new MultipartStream(getExchange().getRequestBody(), boundary.getBytes(), 1024, null);

        boolean nextPart = multipartStream.skipPreamble();
        while (nextPart) {
            String header = multipartStream.readHeaders();
            String contentType = header.split("Content-Type: ")[1].split("\n")[0].trim();

            if(contentType.equals("application/zip")){
                FileOutputStream os = new FileOutputStream(zipPath);
                multipartStream.readBodyData(os);
                os.close();
            }else
                multipartStream.readBodyData(null);

            nextPart = multipartStream.readBoundary();
        }

        Client.updatingClients.add(id);

        new UnzippingProcess(zipPath, clientPath).addProgressListener(new Consumer<ProgressArguments<UnzippingProcess>>() {
            int old_percent;
            public void accept(ProgressArguments<UnzippingProcess> e) {
                if((int)e.getPercent() != old_percent) {
                    System.out.println("Unzipping received client archive: " + (int)e.getPercent() + "%");
                    old_percent = (int)e.getPercent();
                }
            }
        }).startSync();
        new DeletingProcess(zipPath).addProgressListener(e -> System.out.println("Deleting received client archive: " + e.getPercent())).startSync();

        Client.archive(name, id);

        Client.updatingClients.remove(id);
        if(FSUtils.getChildren(new File("./received_clients_tmp/")).size() == 0)
            MIO.delete("./received_clients_tmp/");
    }

    public JSONObject getList(){
        return new SimpleJSON("clients", Client.getList());
    }

    public File get(){
        Client client;
        try {
            client = new Client(getAttribute("id"));
        }catch (NullPointerException ex){
            throw new ApiException("Can't find client with id: " + getAttribute("id"), 1);
        } catch (IllegalAccessException e) {
            throw new ApiException("Client is updating", 2);
        }
        String name = getAttribute("name");

        switch (name){
            case "other": return client.getOtherFile();
            case "mods": return client.getModsFile();
            case "versions": return client.getVersionsFile();
        }
        throw new ApiException("Client part not specified or not found", 2);
    }

    public JSONObject getSizeInfo(){
        try {
            return new Client(getAttribute("id")).getSizeInfo();
        } catch (IllegalAccessException e) {
            throw new ApiException("Client is updating", 2);
        }catch (Exception ex) {
            throw new ApiException("Client part not specified or not found", 1);
        }
    }

    public JSONObject getFilesInfo(){
        try {
            JSONObject out = new JSONObject();
            JSONObject info = new Client(getAttribute("id")).getClientInfo();

            out.put("build", info.get("build"));
            out.put("build_id", info.get("build_id"));
            out.put("version", info.get("version"));
            return out;
        } catch (IllegalAccessException e) {
            throw new ApiException("Client is updating", 2);
        }catch (Exception ex){
            throw new ApiException("Client part not specified or not found", 1);
        }
    }

    public JSONObject checksum(){
        try {
            JSONObject out = new JSONObject();
            JSONObject info = new Client(getAttribute("id")).getClientInfo();
            out.put("equal_mods", true);
            out.put("equal_versions", true);

            String mods = getAttribute("mods");
            String client = getAttribute("versions");

            if(mods == null || !info.getString("mods_MD5").equals(mods))
                out.put("equal_mods", false);
            if(client == null || !info.getString("versions_MD5").equals(client))
                out.put("equal_versions", false);
            return out;
        } catch (IllegalAccessException e) {
            throw new ApiException("Client is updating", 2);
        }catch (Exception ax){
            throw new ApiException("Client part not specified or not found", 1);
        }
    }

    public JSONObject getModsCount(){
        try {
            Client client = new Client(getAttribute("id"));
            return new SimpleJSON("count", client.getClientInfo().getJSONArray("mods").length());
        }catch (NullPointerException e){
            throw new ApiException("Client part not specified or not found", 1);
        } catch (IllegalAccessException e) {
            throw new ApiException("Client is updating", 2);
        }catch (Exception e) {
            return new SimpleJSON("count", 0);
        }
    }

    public JSONObject getModInfo() throws IOException {
        try {
            Client client = new Client(getAttribute("id"));
            return client.getModsInfo(containsAttribute("index") ? Integer.parseInt(getAttribute("index")) : -1);
        }catch (IllegalAccessException e) {
            throw new ApiException("Client is updating", 2);
        }catch (Exception e){
            throw new ApiException("Client part not specified or not found", 1);
        }
    }

    public BufferedImage getModIcon() throws IOException {
        try {
            Client client = new Client(getAttribute("id"));
            return HttpService.fromBase64(client.getModIcon(Integer.parseInt(getAttribute("index"))));
        }catch (IllegalAccessException e) {
            throw new ApiException("Client is updating", 2);
        }catch (Exception e){
            throw new ApiException("Client part not specified or not found", 1);
        }
    }
}
