package com.husker.launcher.server.services;

import com.husker.launcher.server.ServerMain;
import com.husker.launcher.server.core.Profile;
import com.husker.launcher.server.services.http.*;
import com.sun.net.httpserver.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.Executors;

public class HttpService implements Runnable {

    private static final Logger log = LogManager.getLogger(HttpService.class);

    private String htmlPattern;

    public static class ErrorCodes{
        public static final int SUCCESS = 0;
        public static final int SIMPLE_EXCEPTION = -1;
        public static final int ATTRIBUTE_NOT_FOUND = -2;
    }

    public void run() {
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

            HttpServer server = HttpServer.create(new InetSocketAddress(ServerMain.Settings.getPort()), 0);
            if(server == null)
                return;
            server.setExecutor(Executors.newCachedThreadPool());

            server.createContext("/", exchange -> {
                try {
                    if (exchange.getRequestURI().getPath().equals("/"))
                        sendHtml(exchange, new File("./html/index.html"));
                    else
                        sendHtml(exchange, 404, "404.html");
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            });
            server.createContext("/favicon.ico", exchange -> redirect(exchange, "http://simpleicon.com/wp-content/uploads/wrench.png"));
            server.createContext("/api", exchange -> sendHtml(exchange, new File("./html/api.html")));
            server.createContext("/api/method", exchange -> sendJSON(exchange, SimpleJSON.create("message", "Unknown method")));

            server.createContext("/api/method/", exchange -> {
                try {
                    String packagePath = "com.husker.launcher.server.api";

                    String path = exchange.getRequestURI().getPath();
                    if(path.contains("?"))
                        path = path.split("\\?")[0];
                    path = path.substring(path.lastIndexOf("/") + 1);

                    if(!path.contains("."))
                        throw new NoSuchMethodException("Can't find method name");
                    String methodClass = path.split("\\.")[0];
                    String methodName = path.split("\\.")[1];
                    methodClass = methodClass.substring(0, 1).toUpperCase() + methodClass.substring(1).toLowerCase();

                    Class<?> clazz = Class.forName(packagePath + ".Api" + methodClass);
                    if(!ApiClass.class.isAssignableFrom(clazz))
                        throw new ClassNotFoundException("Not ApiClass instance");

                    ApiClass instance = (ApiClass) clazz.newInstance();
                    instance.setExchange(exchange);

                    Object result;
                    try {
                        // Empty
                        Method method = clazz.getDeclaredMethod(methodName);
                        result = method.invoke(instance);
                    }catch (NoSuchMethodException e){
                        // Profile
                        Method method = clazz.getDeclaredMethod(methodName, Profile.class);
                        Profile profile = Profile.get(instance.getAttribute(Profile.ACCESS_TOKEN));
                        if(profile == null)
                            throw new ApiException("Wrong access token", 25);
                        result = method.invoke(instance, profile);
                    }

                    if(result == null)
                        result = new JSONObject();
                    log.info("(" + exchange.getRemoteAddress().getHostName() + ") Invoked API method: " + methodClass + "." + methodName);


                    if (result instanceof File)
                        sendFile(exchange, (File) result);
                    else if(result instanceof ImageLink)
                        sendImage(exchange, (ImageLink) result);
                    else if (result instanceof BufferedImage)
                        sendImage(exchange, (BufferedImage) result);
                    else if (result instanceof JSONObject) {
                        JSONObject json = (JSONObject) result;
                        if(!json.has("code"))
                            json.put("code", ErrorCodes.SUCCESS);
                        sendJSON(exchange, (JSONObject) result);
                    }else
                        sendText(exchange, result.toString());
                }catch (Exception e){
                    if(e instanceof InvocationTargetException)
                        e = (Exception) e.getCause();
                    //e.printStackTrace();

                    int code = ErrorCodes.SIMPLE_EXCEPTION;
                    if(e instanceof ApiException)
                        code = ((ApiException)e).getCode();

                    sendJSON(exchange, SimpleJSON.create().put("code", code).put("message", e.getClass().getSimpleName() + ": " + e.getMessage()));
                }
            });

            server.start();
        }catch (BindException ex){
            log.info("Server is already opened!");
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendText(HttpExchange exchange, String text) throws IOException{
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(200, text.getBytes(StandardCharsets.UTF_8).length);
        OutputStream os = exchange.getResponseBody();
        os.write(text.getBytes(StandardCharsets.UTF_8));
        os.close();
        exchange.close();
    }

    public static void sendJSON(HttpExchange exchange, JSONObject jsonObject) throws IOException{
        String text = jsonObject.toString();
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(200, text.getBytes(StandardCharsets.UTF_8).length);
        OutputStream os = exchange.getResponseBody();
        os.write(text.getBytes(StandardCharsets.UTF_8));
        os.close();
        exchange.close();
    }

    public static void sendImage(HttpExchange exchange, BufferedImage image) throws IOException{
        ByteArrayOutputStream tmp = new ByteArrayOutputStream();
        ImageIO.write(image, "png", tmp);
        tmp.close();

        exchange.getResponseHeaders().add("Content-Type", "image");
        exchange.getResponseHeaders().add("Content-Disposition", "filename=image.png");
        exchange.sendResponseHeaders(200, tmp.size());
        ImageIO.write(image, "png", exchange.getResponseBody());
        exchange.close();
    }

    public static void sendImage(HttpExchange exchange, ImageLink image) throws IOException{
        exchange.getResponseHeaders().add("Content-Type", "image");
        exchange.getResponseHeaders().add("Content-Disposition", "filename=image.png");
        exchange.sendResponseHeaders(200, image.getFile().length());

        OutputStream outputStream = exchange.getResponseBody();
        Files.copy(Paths.get(image.getFile().toURI()), outputStream);
        outputStream.flush();

        exchange.close();
    }

    public static void sendFile(HttpExchange exchange, File file) throws IOException{
        exchange.getResponseHeaders().add("Content-Type", "application");
        exchange.getResponseHeaders().add("Content-Disposition", "attachment; filename=" + file.getName());

        exchange.sendResponseHeaders(200, file.length());
        OutputStream outputStream = exchange.getResponseBody();
        Files.copy(Paths.get(file.toURI()), outputStream);
        outputStream.flush();
        exchange.close();
    }

    public static void redirect(HttpExchange exchange, String url) throws IOException{
        exchange.getResponseHeaders().add("Location", url);
        exchange.sendResponseHeaders(302, url.getBytes().length);
        exchange.close();
    }

    public static void sendHtml(HttpExchange exchange, File file) throws IOException {
        sendHtml(exchange, 200, file);
    }

    public static void sendHtml(HttpExchange exchange, String text) throws IOException {
        sendHtml(exchange, 200, text);
    }

    public static void sendHtml(HttpExchange exchange, int code, String text) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(code, text.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(text.getBytes(StandardCharsets.UTF_8));
        os.close();
        exchange.close();
    }

    public static void sendHtml(HttpExchange exchange, int code, File file) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(code, file.length());
        Files.copy(Paths.get(file.toURI()), exchange.getResponseBody());
        exchange.close();
    }

    public String[] generateHtmlPage(String title, String[] text){
        String space = htmlPattern.split("\\[body]")[0];
        space = space.substring(space.lastIndexOf("\n") + 1);

        return htmlPattern.replace("[body]", String.join("<br>\n" + space, text) + "<br>").replace("[title]", title).split("\n");
    }

    public void checkHtmlFile(String name, String title, String[] content) throws IOException {
        if(!Files.exists(Paths.get("./html")))
            Files.createDirectory(Paths.get("./html"));
        if(!Files.exists(Paths.get("./html/" + name)))
            Files.write(Paths.get("./html/" + name), Arrays.asList(generateHtmlPage(title, content)));
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



}
