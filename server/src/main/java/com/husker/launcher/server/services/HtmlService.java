package com.husker.launcher.server.services;

import com.husker.launcher.server.ApiRequests;
import com.husker.launcher.server.ServerMain;
import com.husker.launcher.server.Service;
import com.sun.net.httpserver.*;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.net.ssl.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class HtmlService extends Service {

    private String htmlPattern;

    public void onStart() {
        try {
            htmlPattern = readHtmlPattern();

            checkHtmlFile("index.html", "Launcher server", new String[]{
                    "You are on the launcher server page. Please don't touch anything or server will break.",
                    "(It's not a joke)",
            });
            checkHtmlFile("api.html", "Launcher API", new String[]{
                    "To see all api commands go to official GitHub repository:",
                    "",
                    "<a href=\"https://github.com/" + ServerMain.Settings.getGitHubId() + "\">" + ServerMain.Settings.getGitHubId() + "</a>",
            });
            checkHtmlFile("404.html", "Page not found - 404", new String[]{
                    "404",
                    "Page not found",
                    "",
                    "[<a href='/'>Go to main page</a>]",
            });

            HttpServer server = createHttpServer(ServerMain.Settings.getPort());
            if(server == null)
                return;
            server.setExecutor(Executors.newCachedThreadPool());

            server.createContext("/", exchange -> {
                try {
                    if (exchange.getRequestURI().getPath().equals("/"))
                        sendHtml(exchange, "index.html");
                    else
                        sendHtml(exchange, 404, "404.html");
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            });
            server.createContext("/favicon.ico", exchange -> sendImage(exchange, ImageIO.read(new URL("http://simpleicon.com/wp-content/uploads/wrench.png"))));
            server.createContext("/api", exchange -> sendHtml(exchange, "api.html"));

            server.createContext("/api/method", exchange -> {
                sendJSON(exchange, new JSONObject(){{
                    put("message", "Unknown method");
                }});
            });

            for(Map.Entry<String, ApiRequests.Getter> entry : ApiRequests.getters.entrySet()){
                server.createContext("/api/method/" + entry.getKey(), exchange -> {
                    try {
                        Object object = entry.getValue().apply(exchange);

                        if (object instanceof File)
                            sendFile(exchange, (File) object);
                        else if (object instanceof BufferedImage)
                            sendImage(exchange, (BufferedImage) object);
                        else if (object instanceof JSONObject)
                            sendJSON(exchange, (JSONObject) object);
                        else
                            sendText(exchange, object.toString());
                    }catch (ApiRequests.AttributeNotFoundException ax){
                        ax.printStackTrace();
                        try {
                            sendJSON(exchange, new ErrorMessage(ax.getMessage(), -2));
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }catch (Exception ex){
                        ex.printStackTrace();
                        try {
                            sendJSON(exchange, new ErrorMessage("An error occurred while executing the request. Please check your request", -1));
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            }
            server.start();

            started();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendText(HttpExchange exchange, String text) throws IOException{
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(200, text.getBytes(StandardCharsets.UTF_8).length);
        OutputStream os = exchange.getResponseBody();
        os.write(text.getBytes(StandardCharsets.UTF_8));
        os.close();
        exchange.close();
    }

    public void sendJSON(HttpExchange exchange, JSONObject jsonObject) throws IOException{
        String text = jsonObject.toString();
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(200, text.getBytes(StandardCharsets.UTF_8).length);
        OutputStream os = exchange.getResponseBody();
        os.write(text.getBytes(StandardCharsets.UTF_8));
        os.close();
        exchange.close();
    }

    public void sendImage(HttpExchange exchange, BufferedImage image) throws IOException{
        ByteArrayOutputStream tmp = new ByteArrayOutputStream();
        ImageIO.write(image, "png", tmp);
        tmp.close();

        exchange.getResponseHeaders().add("Content-Type", "image");
        exchange.getResponseHeaders().add("Content-Disposition", "filename=image.png");
        exchange.sendResponseHeaders(200, tmp.size());
        ImageIO.write(image, "png", exchange.getResponseBody());
        exchange.close();
    }

    public void sendFile(HttpExchange exchange, File file) throws IOException{
        exchange.getResponseHeaders().add("Content-Type", "application");
        exchange.getResponseHeaders().add("Content-Disposition", "attachment; filename=" + file.getName());

        exchange.sendResponseHeaders(200, file.length());
        OutputStream outputStream = exchange.getResponseBody();
        Files.copy(Paths.get(file.toURI()), outputStream);
        outputStream.flush();
        exchange.close();
    }

    public void sendHtml(HttpExchange exchange, String filePath) throws IOException {
        sendHtml(exchange, 200, filePath);
    }

    public void sendHtml(HttpExchange exchange, int code, String filePath) throws IOException {
        String text = String.join("\n", Files.readAllLines(Paths.get("./html/" + filePath)));

        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(code, text.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(text.getBytes(StandardCharsets.UTF_8));
        os.close();
        exchange.close();
    }

    public String[] generateConsoleText(String title, String[] text){
        String space = htmlPattern.split("\\[body]")[0];
        space = space.substring(space.lastIndexOf("\n") + 1);

        return htmlPattern.replace("[body]", String.join("<br>\n" + space, text) + "<br>").replace("[title]", title).split("\n");
    }

    public void checkHtmlFile(String name, String title, String[] content) throws IOException {
        if(!Files.exists(Paths.get("./html")))
            Files.createDirectory(Paths.get("./html"));
        if(!Files.exists(Paths.get("./html/" + name)))
            Files.write(Paths.get("./html/" + name), Arrays.asList(generateConsoleText(title, content)));
    }

    public static class ErrorMessage extends JSONObject{

        public ErrorMessage(String message, int code){
            put("error", message);
            put("error_code", code);
        }
    }

    private String readHtmlPattern(){
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/html_pattern.html"), StandardCharsets.UTF_8));
            StringBuilder out = new StringBuilder();

            String inputLine;
            while ((inputLine = br.readLine()) != null)
                out.append(inputLine).append("\n");
            br.close();
            return out.toString();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    public static HttpServer createHttpServer(int port) throws IOException {
        return HttpServer.create(new InetSocketAddress(port), 0);
    }

    public static HttpsServer createHttpsServer(int port){
        try {
            // Initialise the HTTPS server
            HttpsServer httpsServer = HttpsServer.create(new InetSocketAddress(port), 0);
            SSLContext sslContext = SSLContext.getInstance("TLS");

            // Initialise the keystore
            String passwordString = "myPassword";
            char[] password = passwordString.toCharArray();
            KeyStore ks = KeyStore.getInstance("JKS");
            FileInputStream fis = new FileInputStream("lig.keystore");
            ks.load(fis, password);

            // Set up the key manager factory
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, password);

            // Set up the trust manager factory
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);

            // Set up the HTTPS context and parameters
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                public void configure(HttpsParameters params) {
                    try {
                        // Initialise the SSL context
                        SSLContext c = SSLContext.getDefault();
                        SSLEngine engine = c.createSSLEngine();
                        params.setNeedClientAuth(false);
                        params.setCipherSuites(engine.getEnabledCipherSuites());
                        params.setProtocols(engine.getEnabledProtocols());

                        // Get the default parameters
                        SSLParameters defaultSSLParameters = c.getDefaultSSLParameters();
                        params.setSSLParameters(defaultSSLParameters);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            return httpsServer;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

}
