package com.husker.launcher.server.services.browser;

import com.husker.launcher.server.Client;
import com.husker.launcher.server.GetRequest;
import com.husker.launcher.server.ServerMain;
import com.husker.launcher.server.Service;
import com.husker.launcher.server.services.ConsoleService;
import com.husker.launcher.server.utils.ConsoleUtils;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

public class BrowserService extends Service {

    private ChromeDriver driver;
    private final Timer timer = new Timer();

    private final ArrayList<VkPostParameter> vkPostParameters = new ArrayList<>();
    private final ArrayList<YoutubeVideoParameters> youtubeParameters = new ArrayList<>();

    private BufferedImage vkPreviewLogo;
    private String vkTitle;
    private String vkDescription;

    private BufferedImage ytPreviewLogo;
    private String ytTitle;
    private String ytSub;

    public void onStart() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("window-size=1200x600");
        options.addArguments("--lang=ru");

        ChromeDriverService service = new ChromeDriverService.Builder()
                .usingDriverExecutable(new File("./chromedriver_win.exe"))
                .build();

        // Mute chrome service output
        System.setProperty(ChromeDriverService.CHROME_DRIVER_SILENT_OUTPUT_PROPERTY, "true");
        java.util.logging.Logger.getLogger("org.openqa.selenium").setLevel(Level.OFF);
        service.sendOutputTo(new OutputStream(){public void write(int b){}});

        driver = new ChromeDriver(service, options);

        ConsoleUtils.printDebug(getClass(), "First caching in process...");
        timer.schedule(new TimerTask() {
            public void run() {
                ConsoleUtils.printDebug(BrowserService.class, "Caching new social data...");

                youtubeParameters.clear();
                vkPostParameters.clear();

                ConsoleUtils.printDebug(BrowserService.class, "Caching VK...");
                driver.get(getVKUrl());
                processVkGroupParsing(driver.getPageSource());

                ConsoleUtils.printDebug(BrowserService.class, "Caching YouTube...");
                driver.get(getYouTubeUrl() + "/videos");
                processYoutubeParsing(driver.getPageSource());

                ConsoleUtils.printDebug(BrowserService.class, "Cached!");
            }
        }, 1000, ServerMain.Settings.getSocialDelay() * 1000 * 60);
        started();
    }

    public String getVKUrl(){
        return "https://vk.com/" + ServerMain.Settings.getVkGroup();
    }

    public String getYouTubeUrl(){
        return "https://www.youtube.com/channel/" + ServerMain.Settings.getYoutubeId();
    }

    public String getVkTitle(){
        return vkTitle;
    }

    public String getVkDescription(){
        return vkDescription;
    }

    public BufferedImage getVkPreviewLogo() {
        return vkPreviewLogo;
    }

    public String getYoutubeTitle(){
        return ytTitle;
    }

    public String getYoutubeSubscribers(){
        return ytSub;
    }

    public BufferedImage getYoutubePreviewLogo() {
        return ytPreviewLogo;
    }

    public VkPostParameter[] getVkPosts(){
        return vkPostParameters.toArray(new VkPostParameter[0]);
    }

    public YoutubeVideoParameters[] getYoutubeVideos(){
        return youtubeParameters.toArray(new YoutubeVideoParameters[0]);
    }

    private void processYoutubeParsing(String text){
        String titleBar = text.split("<div id=\"channel-header\" class")[1].split("<app-toolbar sticky")[0];

        try{
            ytPreviewLogo = ImageIO.read(new URL(titleBar.split("<img id=\"img\"")[1].split("src=\"")[1].split("\">")[0]));
        }catch (Exception ex){
            ex.printStackTrace();
            ytPreviewLogo = null;
        }
        ytTitle = titleBar.split("<yt-formatted-string id=\"text\"")[1].split("ytd-channel-name\">")[1].split("</yt")[0];
        ytSub = titleBar.split("subscriber-count")[1].split("renderer\">")[1].split("</yt")[0];

        String author = text.split("<div id=\"container\" class=\"style-scope ytd-channel-name\">")[1].split("class=\"style-scope ytd-channel-name\"")[2].split("</yt-formatted-string>")[0].substring(1);

        text = text.split("<div id=\"items\" class=\"style-scope ytd-grid-renderer\"")[1];

        for(String videoText : text.split("<ytd-grid-video-renderer")){
            try{
                if(!videoText.contains("class=\"style-scope yt-img-shadow") || !videoText.contains("src=\""))
                    continue;

                String image = videoText.split("class=\"style-scope yt-img-shadow")[1].split("src=\"")[1].split("\">")[0];
                if(!image.startsWith("https://i.ytimg.com"))
                    break;

                String title = videoText.split("id=\"video-title\"")[1].split("title=\"")[1].split("\" href")[0];

                String date = videoText.split("id=\"video-title\"")[1].split("aria-label=\"")[1].split("\" title")[0];

                date = date.replaceAll(title, "");
                date = date.replaceAll(author, "");

                date = date.replace("назад ", "");
                date = date.replace("Автор: ", "");

                date = date.replace("лет", "years").replace("года", "years").replace("год", "years");
                date = date.replace("месяцев", "months").replace("месяца", "months").replace("месяц", "months");
                date = date.replace("недель", "weeks").replace("недели", "weeks").replace("неделю", "weeks");
                date = date.replace("дней", "days").replace("дня", "days").replace("день", "days");
                date = date.replace("часов", "hours").replace("часа", "hours").replace("час", "hours");
                date = date.replace("минуты", "minutes").replace("минута", "minutes").replace("минут", "minutes");
                date = date.replace("секунды", "seconds").replace("секунда", "seconds").replace("секунд", "seconds");
                date = date.trim();

                long timeAgo = 0;
                if(date.contains("seconds"))
                    timeAgo += 1000 * Integer.parseInt(date.split("seconds")[0].split(" ")[date.split("seconds")[0].split(" ").length -1]);
                if(date.contains("minutes"))
                    timeAgo += 60 * 1000 * Integer.parseInt(date.split("minutes")[0].split(" ")[date.split("minutes")[0].split(" ").length -1]);
                if(date.contains("hours"))
                    timeAgo += 60 * 60 * 1000 * Integer.parseInt(date.split("hours")[0].split(" ")[date.split("hours")[0].split(" ").length -1]);
                if(date.contains("days"))
                    timeAgo += 24 * 60 * 60 * 1000 * Integer.parseInt(date.split("days")[0].split(" ")[date.split("days")[0].split(" ").length -1]);
                if(date.contains("weeks"))
                    timeAgo += 7 * 24 * 60 * 60 * 1000 * Integer.parseInt(date.split("weeks")[0].split(" ")[date.split("weeks")[0].split(" ").length -1]);
                if(date.contains("months"))
                    timeAgo += 30L * 7L * 24L * 60L * 60L * 1000L * Long.parseLong(date.split("months")[0].split(" ")[date.split("months")[0].split(" ").length -1]);
                if(date.contains("years"))
                    timeAgo += 365L * 30L * 7L * 24L * 60L * 60L * 1000L * Long.parseLong(date.split("years")[0].split(" ")[date.split("years")[0].split(" ").length -1]);

                long dateTime = System.currentTimeMillis() - timeAgo;

                BufferedImage preview = ImageIO.read(new URL(image));
                preview = preview.getSubimage(0, (int)(preview.getHeight() * 0.125f), preview.getWidth(), (int)(preview.getHeight() * 0.75f));

                String videoUrl = "https://www.youtube.com/watch?v=" + image.split("vi/")[1].split("/")[0];

                youtubeParameters.add(new YoutubeVideoParameters(preview, title, videoUrl, dateTime));
            }catch (Exception ex){
                //ex.printStackTrace();
            }
        }
    }

    public void processVkGroupParsing(String text){
        String titleBar = text.split("<div class=\"page_cover_info clear_fix\">")[1].split("<div class=\"wide_column_left\">")[0];
        try {
            this.vkPreviewLogo = ImageIO.read(new URL(titleBar.split("<img src=\"")[1].split("\" class=\"post_img\"")[0].replace("&amp;", "&")));
        }catch (Exception ex){
            ex.printStackTrace();
        }
        this.vkTitle = titleBar.split("<h1 class=\"page_name\">")[1].split("</h1>")[0];
        this.vkDescription = titleBar.split("<span class=\"current_text\">")[1].split("</span></div>")[0];

        text = text.split("id=\"page_wall_posts\"")[1];

        String[] posts = text.split("_post post page_block all own post");
        for(String post : posts){
            try {
                if (!post.contains("post_content"))
                    continue;

                String content = post.split("wall_post_cont _wall_post_cont\">")[1].split("<div class=\"like_wrap")[0];
                content = content.replace("&amp;", "&");

                // Title
                String title = post.split("<div class=\"wall_post_text\">")[1].split("</div>")[0];
                while(title.contains("<") && title.contains(">"))
                    title = title.replace(title.substring(title.indexOf("<"), title.indexOf(">") + 1), "");

                // Url
                String url = "https://vk.com/" + post.split("class=\"post_link\"")[1].split("href=\"")[1].split("\"")[0];

                // Snippet
                if(content.contains("article_snippet__image_wrap")){
                    String img = content.split("article_snippet__image")[2].split("url\\(")[1].split("\\)\">")[0];
                    String snippet_title = content.split("article_snippet__title\">")[1].split("</div>")[0];
                    String snippet_author = content.split("article_snippet__author\">")[1].split("<span")[0];

                    vkPostParameters.add(new VkPostParameter.Snippet(title, url, ImageIO.read(new URL(img)), snippet_title, snippet_author));
                    continue;
                }

                // Pictures
                if(content.contains("page_post_sized_thumbs") && content.contains("фотография")){
                    String img = content.split("page_post_sized_thumbs")[1].split("url\\(")[1].split("\\);\"")[0];
                    vkPostParameters.add(new VkPostParameter.Picture(title, url, ImageIO.read(new URL(img))));
                    continue;
                }

                // Youtube video
                if(content.contains("page_post_sized_thumbs") && content.contains("/video-") && content.contains("video_thumb_label_item\">YouTube")){
                    String img = content.split("page_post_sized_thumbs")[1].split("url\\(")[1].split("\\);\"")[0];
                    vkPostParameters.add(new VkPostParameter.Youtube(title, url, ImageIO.read(new URL(img))));
                    continue;
                }

                // Video
                if(content.contains("page_post_sized_thumbs") && content.contains("/video")){
                    String img = content.split("page_post_sized_thumbs")[1].split("url\\(")[1].split("\\);\"")[0];
                    vkPostParameters.add(new VkPostParameter.Video(title, url, ImageIO.read(new URL(img))));
                    continue;
                }

                vkPostParameters.add(new VkPostParameter(title, url));
            }catch (IOException ignored){
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }
}
