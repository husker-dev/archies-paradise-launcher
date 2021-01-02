package com.husker.glassui.screens.main.control;

import com.alee.extended.layout.VerticalFlowLayout;
import com.husker.glassui.GlassUI;
import com.husker.glassui.components.*;
import com.husker.launcher.Resources;
import com.husker.launcher.api.API;
import com.husker.launcher.managers.ProfileApiMethod;
import com.husker.launcher.social.Social;
import com.husker.launcher.ui.components.MLabel;
import com.husker.launcher.ui.components.TransparentPanel;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.utils.filechooser.FileChooser;
import com.husker.net.Get;
import com.husker.net.HttpsUrlBuilder;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import static com.husker.launcher.utils.filechooser.FileChooser.Mode.DIRECTORIES_ONLY;
import static com.husker.launcher.utils.filechooser.FileChooser.Mode.FILES_AND_DIRECTORIES;

public class ControlPanel extends TransparentPanel {

    private String currentVersion;
    private final ArrayList<Release> releases = new ArrayList<>();
    private BlurPagePanel releasesPages;

    private Screen screen;

    private MLabel releaseTitle;
    private MLabel releaseDescription;
    private BlurButton applyBtn;

    private Release selectedRelease;

    private BlurList<Release> list;

    public ControlPanel(Screen screen){
        try {
            this.screen = screen;
            setLayout(new VerticalFlowLayout(0, 10));
            setPreferredWidth(510);
            setMargin(10, 10, 3, 10);

            add(new BlurTagPanel(screen, "Сборка") {{
                getContent().setMargin(10, 0, 0, 0);
                addContent(new TransparentPanel() {{
                    setMargin(0, 20, 0, 0);
                    setLayout(new FlowLayout(FlowLayout.LEFT, 15, 0));
                    add(new BlurButton(screen, "Выбрать сборку (VR)") {{
                        addActionListener(e -> {
                            try {
                                File file = chooseFile();
                                if (file != null)
                                    ClientLoading.load(getScreen(), file, "vr", "VR Client");
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        });
                    }});
                    add(new BlurButton(screen, "Выбрать сборку (Non-VR)") {{
                        addActionListener(e -> {
                            try {
                                File file = chooseFile();
                                if (file != null)
                                    ClientLoading.load(getScreen(), file, "non_vr", "Non-VR Client");
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        });
                    }});
                }});
            }});

            add(new BlurTagPanel(screen, "Лаунчер") {{
                addButtonAction(() -> reloadVersionsList());
                setButtonIcons(Resources.Icon_Reload, Resources.Icon_Reload_Selected);
                getContent().setMargin(10, 20, 0, 0);
                addContent(new TransparentPanel() {{
                    setLayout(new BorderLayout());
                    setMargin(0, 20, 0, 0);

                    // Left part
                    add(new TransparentPanel() {{
                        setLayout(new BorderLayout(0, 5));
                        setPreferredWidth(200);

                        // List
                        add(list = new BlurList<>(screen));
                        list.addSelectedListener(ControlPanel.this::setSelectedRelease);
                        for (int i = 0; i < 5; i++)
                            list.addContentPanel(new ReleasePanel(ControlPanel.this));

                        // Pages
                        add(releasesPages = new BlurPagePanel(screen) {{
                            setPages(3);
                            addPageListener(ControlPanel.this::setReleasePage);
                        }}, BorderLayout.SOUTH);
                    }}, BorderLayout.WEST);

                    // Right part
                    add(new TransparentPanel() {{
                        setMargin(0, 10, 0, 10);
                        setLayout(new BorderLayout());
                        add(new TransparentPanel() {{
                            setMargin(0, 0, 7, 0);
                            setLayout(new BorderLayout());
                            add(releaseTitle = GlassUI.createTitleLabel("Title"), BorderLayout.NORTH);
                            add(releaseDescription = GlassUI.createSimpleLabel("Description"));
                        }});
                        add(new TransparentPanel() {{
                            setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
                            add(applyBtn = new BlurButton(screen, "Сделать текущией"){{
                                addActionListener(e -> {
                                    new Thread(() -> {
                                        try {
                                            API.getJSON(ProfileApiMethod.create("launcher.setVersion", screen.getLauncher().User.getToken()).set("version", selectedRelease.id));
                                            reloadVersionsList();
                                        } catch (API.InternalAPIException internalAPIException) {
                                            internalAPIException.printStackTrace();
                                        }
                                    }).start();
                                });
                            }});
                        }}, BorderLayout.SOUTH);
                    }});
                }});
            }});
            setSelectedRelease(null);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void onShow(){
        setSelectedRelease(null);
        reloadVersionsList();
    }

    public void setSelectedRelease(Release release){
        this.selectedRelease = release;
        if(release != null) {
            releaseTitle.setText(release.title);

            HtmlRenderer renderer = HtmlRenderer.builder().escapeHtml(true).build();
            Parser parser = Parser.builder().build();
            releaseDescription.setText("<html>" + renderer.render(parser.parse(release.description)) + "</html>");

            applyBtn.setEnabled(!release.id.equals(currentVersion));
            applyBtn.setVisible(true);
        }else {
            applyBtn.setVisible(false);
            releaseTitle.setText("");
            releaseDescription.setText("");
        }
        repaint();
    }

    public void setReleasePage(int page){
        int from = page * 5;
        int to = Math.min(releases.size(), (page + 1) * 5);
        list.setContent(Arrays.copyOfRange(releases.toArray(new Release[0]), from, to));

        screen.getLauncher().updateUI();
    }

    public void reloadVersionsList(){
        new Thread(() -> {
            try {
                currentVersion = API.Launcher.getCurrentVersion();

                Get get = new Get(HttpsUrlBuilder.create("api.github.com/repos/" + Social.GitHub.getRepository() + "/releases"));

                releases.clear();
                JSONArray json = new JSONArray(get.getHtmlContent());
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
                for(int i = 0; i < json.length(); i++){
                    Release release = new Release();
                    JSONObject object = json.getJSONObject(i);
                    release.id = object.getString("tag_name");
                    release.date = dateFormat.parse(object.getString("created_at"));
                    release.title = object.getString("name");
                    if(object.has("body") && !object.getString("body").isEmpty())
                        release.description = object.getString("body");
                    else
                        release.description = "(Empty description)";

                    if(!object.getBoolean("prerelease")) {
                        releases.add(release);
                        if(releases.size() >= 5 * 4)
                            break;
                    }
                }
                if(releases.size() % 5 == 0)
                    releasesPages.setPages(releases.size() / 5);
                else
                    releasesPages.setPages(releases.size() / 5 + 1);
                setReleasePage(0);
            } catch (Exception e) {
                e.printStackTrace();
                setSelectedRelease(null);
                releasesPages.setPages(0);
                releases.clear();
            }
            screen.getLauncher().updateUI();
        }).start();
    }

    private File chooseFile() {
        return new FileChooser(DIRECTORIES_ONLY).open(screen.getLauncher());
    }

    static class Release {
        public String id, title, description;
        public Date date;
    }

    static class ReleasePanel extends BlurList.ContentPanel<Release> {

        private final MLabel date, version, icon;
        private final BufferedImage selectedIcon = Resources.Icon_Checkbox_On;
        private final ControlPanel panel;

        public ReleasePanel(ControlPanel panel){
            super(panel.screen);
            this.panel = panel;

            version = GlassUI.createSimpleLabel("");
            date = GlassUI.createSimpleLabel("");
            date.setForeground(GlassUI.Colors.labelLightText);
            icon = new MLabel();
            icon.setPreferredWidth(30);

            add(version);
            add(icon, BorderLayout.WEST);
            add(date, BorderLayout.EAST);
        }

        public void applyContent(Release release) {
            if(release != null) {
                version.setText((release.id.startsWith("v") ? "" : "v") + release.id);
                date.setText(new SimpleDateFormat("dd.MM.yyyy").format(release.date));
                icon.setImage(panel.currentVersion.equals(release.id) ? selectedIcon : null);
                icon.setImageSize(30);
            }else{
                version.setText("");
                date.setText("");
                icon.setImage(null);
            }
        }
    }

}
