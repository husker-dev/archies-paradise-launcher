package com.husker.launcher.social;

import com.husker.launcher.Main;
import com.husker.launcher.api.API;
import com.husker.launcher.api.ApiMethod;
import com.husker.launcher.managers.ProfileApiMethod;
import com.husker.net.Get;
import com.husker.net.HttpsUrlBuilder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.ConnectException;
import java.net.URL;
import java.util.ArrayList;

public class Social {

    private static final Logger log = LogManager.getLogger(Social.class);

    static {
        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
    }

    public static class About {

        private static String ownerName, ownerUrl, supportName, supportUrl;

        public static String getOwnerName(){
            if(ownerName == null)
               updateOwnerInfo();
            return ownerName;
        }

        public static String getOwnerUrl(){
            if(ownerUrl == null)
                updateOwnerInfo();
            return ownerUrl;
        }

        public static String getSupportName(){
            if(supportName == null)
                updateSupportInfo();
            return supportName;
        }

        public static String getSupportUrl(){
            if(supportUrl == null)
                updateSupportInfo();
            return supportUrl;
        }

        private static void updateOwnerInfo(){
            try {
                JSONObject jsonObject = API.getJSON(ProfileApiMethod.create("about.getOwnerInfo"));
                ownerName = jsonObject.getString("name");
                ownerUrl = jsonObject.getString("url");
            } catch (API.InternalAPIException e) {
                e.printStackTrace();
            }
        }

        private static void updateSupportInfo(){
            try {
                JSONObject jsonObject = API.getJSON(ProfileApiMethod.create("about.getSupportInfo"));
                supportName = jsonObject.getString("name");
                supportUrl = jsonObject.getString("url");
            } catch (API.InternalAPIException e) {
                e.printStackTrace();
            }
        }
    }

    public static class GitHub {
        private static String id;

        public static int setRepository(String id, String token){
            try {
                int result = API.getJSON(ProfileApiMethod.create("github.setInfo", token).set("repo", id)).getBoolean("changed") ? 1 : 0;
                if(result == 1)
                    GitHub.id = id;
                return result;
            } catch (Exception ignored) {}
            return -1;
        }

        public static String getRepository(){
            try {
                return API.getJSON(ApiMethod.create("github.getInfo")).getString("repo");
            } catch (API.InternalAPIException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static class Instagram {
        private static final ArrayList<InstagramPhotoInfo> photos = new ArrayList<>();
        private static String id;

        public static InstagramPhotoInfo getPhoto(int index){
            loadInstagram();
            return photos.get(index);
        }

        public static int setId(String id, String token){
            try {
                int result = API.getJSON(ProfileApiMethod.create("instagram.setInfo", token).set("id", id)).getBoolean("changed") ? 1 : 0;
                if(result == 1)
                    Instagram.id = id;
                return result;
            } catch (Exception ignored) {}
            return -1;
        }

        public static String getId(){
            if(id == null) {
                try {
                    id = API.getJSON(ApiMethod.create("instagram.getInfo")).getString("id");
                } catch (Exception ignored) {}
            }
            return id;
        }

        private static void loadInstagram(){
            for(int t = 0; t < 10; t++) {
                try {
                    if (photos.size() > 0)
                        return;

                    String id = getId();

                    Get inst = new Get(new HttpsUrlBuilder("instagram.com/" + id));
                    inst.execute();
                    String content = inst.getHtmlContent();
                    JSONObject object = new JSONObject(content.split("window._sharedData =")[1].split("</script>")[0]);

                    JSONArray images = object.getJSONObject("entry_data").getJSONArray("ProfilePage")
                            .getJSONObject(0).getJSONObject("graphql").getJSONObject("user").getJSONObject("edge_owner_to_timeline_media").getJSONArray("edges");

                    for (int i = 0; i < 4; i++) {
                        JSONObject image = images.getJSONObject(i).getJSONObject("node");

                        String url = "https://instagram.com/p/" + image.getString("shortcode");
                        String text = image.getJSONObject("edge_media_to_caption").getJSONArray("edges").getJSONObject(0).getJSONObject("node").getString("text");
                        String previewUrl = image.getString("display_url");

                        photos.add(new InstagramPhotoInfo(i, text, previewUrl, url));
                    }
                    break;
                }catch (ConnectException | JSONException ex) {
                    log.info("Instagram loading error: " + ex.getMessage());
                }catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static class YouTube{

        private static final ArrayList<YouTubeVideoInfo> videos = new ArrayList<>();

        private static String title;
        private static String subscribers;
        private static BufferedImage logo;
        private static String id;

        public static int setId(String id, String token){
            try {
                int result = API.getJSON(ProfileApiMethod.create("youtube.setInfo", token).set("id", id)).getBoolean("changed") ? 1 : 0;
                if(result == 1)
                    YouTube.id = id;
                return result;
            } catch (Exception ignored) {}
            return -1;
        }

        public static String getId(){
            if(id == null) {
                try {
                    id = API.getJSON(ApiMethod.create("youtube.getInfo")).getString("id");
                } catch (Exception ignored) {}
            }
            return id;
        }

        public static String getTitle(){
            load();
            return title;
        }

        public static String getSubscribers(){
            load();
            return subscribers;
        }

        public static BufferedImage getLogo(){
            load();
            return logo;
        }

        public static String getUrl(){
            return "https://youtube.com/channel/" + getId();
        }

        public static YouTubeVideoInfo getVideo(int index){
            load();
            return videos.get(index);
        }

        private static void load(){
            while(true) {
                try {
                    if (videos.size() > 0)
                        return;

                    String id = getId();

                    Get get = new Get(new HttpsUrlBuilder("youtube.com/channel/" + id));
                    String html = get.getHtmlContent();
                    if(html.contains("var ytInitialData = "))
                        html = html.split("var ytInitialData = ")[1];
                    else if (html.contains("window\\[\"ytInitialData\"] ="))
                        html = html.split("window\\[\"ytInitialData\"] =")[1];
                    JSONObject content = new JSONObject(html.split("</script>")[0]);

                    title = content.getJSONObject("metadata").getJSONObject("channelMetadataRenderer").getString("title");
                    logo = ImageIO.read(new URL(content.getJSONObject("metadata").getJSONObject("channelMetadataRenderer").getJSONObject("avatar").getJSONArray("thumbnails").getJSONObject(0).getString("url")));
                    subscribers = content.getJSONObject("header").getJSONObject("c4TabbedHeaderRenderer").getJSONObject("subscriberCountText").getString("simpleText");
                    subscribers = subscribers.replace("\u00a0", " ");

                    JSONArray json_videos = content.getJSONObject("contents").getJSONObject("twoColumnBrowseResultsRenderer")
                            .getJSONArray("tabs").getJSONObject(0).getJSONObject("tabRenderer")
                            .getJSONObject("content").getJSONObject("sectionListRenderer").getJSONArray("contents").getJSONObject(1)
                            .getJSONObject("itemSectionRenderer").getJSONArray("contents").getJSONObject(0)
                            .getJSONObject("shelfRenderer").getJSONObject("content").getJSONObject("horizontalListRenderer").getJSONArray("items");

                    for (int i = 0; i < Math.min(4, json_videos.length()); i++) {
                        String previewUrl = json_videos.getJSONObject(i).getJSONObject("gridVideoRenderer").getJSONObject("thumbnail").getJSONArray("thumbnails").getJSONObject(3).getString("url");
                        previewUrl = previewUrl.replace("hqdefault", "maxresdefault");
                        String title = json_videos.getJSONObject(i).getJSONObject("gridVideoRenderer").getJSONObject("title").getString("simpleText");
                        if(title.substring(0, 1).getBytes()[0] == 63)
                            title = "●" + title.substring(2);

                        String url = "https://www.youtube.com/watch?v=" + json_videos.getJSONObject(i).getJSONObject("gridVideoRenderer").getString("videoId");
                        videos.add(new YouTubeVideoInfo(i, title, previewUrl, url));
                    }
                    break;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static class VK {

        private static final ArrayList<VKPostInfo> posts = new ArrayList<>();

        private static String id = null;
        private static String title;
        private static String description;
        private static BufferedImage logo;

        public static String getId(){
            if(id == null) {
                try {
                    id = API.getJSON(ApiMethod.create("vk.getInfo")).getString("id");
                } catch (Exception ignored) {}
            }
            return id;
        }

        public static int setId(String id, String token){
            try {
                int result = API.getJSON(ProfileApiMethod.create("vk.setInfo", token).set("id", id)).getBoolean("changed") ? 1 : 0;
                if(result == 1)
                    VK.id = id;
                return result;
            } catch (Exception ignored) {}
            return -1;
        }

        public static VKPostInfo getPost(int index){
            load();
            return posts.get(index);
        }

        public static String getTitle(){
            load();
            return title;
        }

        public static String getDescription(){
            load();
            return description;
        }

        public static BufferedImage getLogo(){
            load();
            return logo;
        }

        public static String getUrl(){
            return "https://vk.com/" + getId();
        }

        private static void load(){
            try {
                if(posts.size() > 0)
                    return;

                String id = getId();

                Get get = new Get(new HttpsUrlBuilder("vk.com/" + id));
                String content = get.getHtmlContent();

                logo = ImageIO.read(new URL(content.split("class=\"basisGroup__mainInfoRow\"")[1].split("src=\"")[1].split("\"")[0]));
                title = content.split("class=\"basisGroup__groupTitle op_header\">")[1].split("</h2>")[0].trim();
                if(content.contains("class=\"pp_status\">"))
                    description = content.split("class=\"pp_status\">")[1].split("</div>")[0];
                else
                    description = "";

                String[] content_items = content.split("container=\"group_wall\">")[1].split("<div class=\"wall_item\"");
                for(String item : content_items){

                    // Если закреплённая
                    if(item.contains("wi_explain") || !item.contains("wi_info"))
                        continue;

                    String url = "https://vk.com/" + id + "?w=" + item.split("wi_info")[1].split("href=\"")[1].split("\"")[0].substring(1);

                    String text = item.contains("pi_text") ? item.split("pi_text\">")[1].split("</div>")[0] : null;
                    String photo = item.contains("thumb_map thumb_map_wide thumb_map_l al_photo") ? item.split("background-image: url\\(")[1].split("\\)")[0] : null;
                    String videoPreview = item.contains("thumb_map thumb_map_wide thumb_map_l") ? item.split("background-image: url\\(")[1].split("\\)")[0] : null;
                    String snippetPreview = item.contains("articleSnippet") ? item.split("background-image: url\\(")[1].split("\\)")[0] : null;
                    String snippetTitle = item.contains("articleSnippet") ? item.split("articleSnippet_title\">")[1].split("</div>")[0] : null;
                    String snippetAuthor = item.contains("articleSnippet") ? item.split("articleSnippet_author\">")[1].split("<span>")[0] : null;

                    if(text != null){
                        text = text.replace("<br/><br/>", "\n");
                        text = text.replace("<br/>", "\n");
                        text = text.replace("</a>", "");
                        text = text.replace("<span>", "").replace("</span>", "");
                        text = text.replace("&#33;", "");
                        text = text.replace("&quot;", "\"");
                        text = text.trim();

                        while(text.contains("<a"))
                            text = text.replace("<a" + text.split("<a")[1].split(">")[0] + ">", "");
                        while(text.contains("<img"))
                            text = text.replace("<img" + text.split("<img")[1].split(">")[0] + ">", "");
                    }

                    // Snippet
                    if(snippetPreview != null){
                        posts.add(new VKPostInfo.Snippet(posts.size(), text, snippetPreview, url, snippetTitle, snippetAuthor));
                        continue;
                    }
                    // Picture
                    if(photo != null){
                        posts.add(new VKPostInfo.Picture(posts.size(), text, photo, url));
                        continue;
                    }
                    // Video
                    if(videoPreview != null){
                        posts.add(new VKPostInfo.Video(posts.size(), text, videoPreview, url));
                        continue;
                    }
                    // Empty
                    posts.add(new VKPostInfo(posts.size(), text, null, url));
                }

            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }
}
