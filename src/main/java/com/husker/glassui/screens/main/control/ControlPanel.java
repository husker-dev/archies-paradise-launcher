package com.husker.glassui.screens.main.control;

import com.alee.extended.label.WebStyledLabel;
import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.laf.WebLookAndFeel;
import com.alee.laf.label.WebLabel;
import com.husker.glassui.GlassUI;
import com.husker.glassui.components.BlurButton;
import com.husker.glassui.components.BlurPagePanel;
import com.husker.glassui.components.BlurPanel;
import com.husker.glassui.components.TagPanel;
import com.husker.launcher.Resources;
import com.husker.launcher.api.API;
import com.husker.launcher.api.ApiMethod;
import com.husker.launcher.social.Social;
import com.husker.launcher.ui.components.TransparentPanel;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.ui.utils.ShapeUtils;
import com.husker.launcher.ui.utils.UIUtils;
import com.husker.net.Get;
import com.husker.net.HttpsUrlBuilder;
import li.flor.nativejfilechooser.NativeJFileChooser;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ControlPanel extends TransparentPanel {

    private String currentVersion;
    private final ArrayList<Release> releases = new ArrayList<>();
    private final ArrayList<ReleasePanel> panels = new ArrayList<>();
    private BlurPagePanel releasesPages;

    private final Screen screen;

    private WebLabel releaseTitle;
    private WebLabel releaseDescription;
    private BlurButton applyBtn;

    private Release selectedRelease;

    public ControlPanel(Screen screen){
        this.screen = screen;
        setLayout(new VerticalFlowLayout(0, 10));
        setPreferredWidth(510);
        setMargin(10, 10, 3, 10);

        add(new TagPanel(screen, "Сборка"){{
            getContent().setMargin(10, 0, 0, 0);
            addContent(new TransparentPanel(){{
                setMargin(0, 20, 0, 0);
                setLayout(new FlowLayout(FlowLayout.LEFT, 15, 0));
                add(new BlurButton(screen, "Выбрать сборку (VR)"){{
                    addActionListener(e -> {
                        try {
                            File file = chooseFile();
                            if(file != null)
                                ClientLoading.load(getScreen(), file, "vr", "VR Client");
                        }catch (Exception ex){
                            ex.printStackTrace();
                        }
                    });
                }});
                add(new BlurButton(screen, "Выбрать сборку (Non-VR)"){{
                    addActionListener(e -> {
                        try {
                            File file = chooseFile();
                            if(file != null)
                                ClientLoading.load(getScreen(), file, "non_vr", "Non-VR Client");
                        }catch (Exception ex){
                            ex.printStackTrace();
                        }
                    });
                }});
            }});
        }});

        add(new TagPanel(screen, "Лаунчер"){{
            addButtonAction(() -> reloadVersionsList());
            setButtonIcons(Resources.Icon_Reload, Resources.Icon_Reload_Selected);
            getContent().setMargin(10, 20, 0, 0);
            addContent(new TransparentPanel(){{
                setLayout(new BorderLayout());
                setMargin(0, 20, 0, 0);

                // Left part
                add(new TransparentPanel(){{
                    setLayout(new BorderLayout(0, 5));
                    setPreferredWidth(200);

                    // List
                    add(new BlurPanel(screen, true){
                        {
                            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                            setPreferredHeight(160);
                            for(int i = 0; i < 5; i++){
                                ReleasePanel panel = new ReleasePanel(ControlPanel.this, i);
                                panels.add(panel);
                                add(panel);
                            }
                        }
                        public void onBlurApply(BlurParameter parameter, Component component) {
                            super.onBlurApply(parameter, component);
                            if(returnOnInvisible(parameter, component))
                                return;

                            GlassUI.applyBottomLayer(parameter);
                            parameter.setShadowSize(5);
                            parameter.setShadowType(BlurParameter.ShadowType.INNER);
                        }
                    });

                    // Pages
                    add(releasesPages = new BlurPagePanel(screen){{
                        setPages(3);
                        addPageListener(ControlPanel.this::setReleasePage);
                    }}, BorderLayout.SOUTH);
                }}, BorderLayout.WEST);

                // Right part
                add(new TransparentPanel(){{
                    setMargin(0, 10, 0, 10);
                    setLayout(new BorderLayout());
                    add(new TransparentPanel(){{
                        setMargin(0, 0, 7, 0);
                        setLayout(new BorderLayout());
                        add(releaseTitle = GlassUI.createTitleLabel("Title"), BorderLayout.NORTH);
                        add(releaseDescription = GlassUI.createSimpleLabel("Description"));
                    }});
                    add(new TransparentPanel(){{
                        setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
                        add(applyBtn = new BlurButton(screen, "Сделать текущией"));
                    }}, BorderLayout.SOUTH);
                }});
            }});
        }});
        setSelectedRelease(null);
    }

    public void onShow(){
        setSelectedRelease(null);
        reloadVersionsList();
    }

    public void setSelectedRelease(Release release){
        selectedRelease = release;
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
        for(ReleasePanel panel : panels)
            panel.setRelease(null);
        for(int i = page * 5; i < Math.min(releases.size(), (page + 1) * 5); i++)
           panels.get(i - (page * 5)).setRelease(releases.get(i));

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
        JFileChooser chooser = new NativeJFileChooser();
        chooser.setDialogTitle("Выбор сборки");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setFileFilter(new FileFilter() {
            public String getDescription() {
                return "Directory";
            }
            public boolean accept(File f) {
                return f.isDirectory();
            }
        });

        File file = null;

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
            file = chooser.getSelectedFile();
        return file;
    }

    static class Release {
        public String id, title, description;
        public Date date;
    }

    static class ReleasePanel extends BlurPanel{

        private final WebLabel date, version, icon;
        private final ControlPanel panel;
        private final Icon selectedIcon;
        private final int index;

        private Release release;

        public ReleasePanel(ControlPanel panel, int index){
            super(panel.screen);

            this.panel = panel;
            this.index = index;
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    panel.setSelectedRelease(release);
                }
            });
            setPreferredSize(130, 30);
            setLayout(new BorderLayout());

            setMargin(0, 0, 0, 10);

            version = GlassUI.createSimpleLabel("");
            date = GlassUI.createSimpleLabel("");
            date.setForeground(GlassUI.Colors.labelLightText);
            icon = new WebLabel();
            icon.setPreferredWidth(30);

            add(version);
            add(icon, BorderLayout.WEST);
            add(date, BorderLayout.EAST);

            selectedIcon = new ImageIcon(Resources.Icon_Checkbox_On.getScaledInstance(30, 30, Image.SCALE_SMOOTH));
        }

        public void paint(Graphics g) {
            if(index < 4) {
                g.setColor(new Color(160, 160, 160, 150));
                g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            }
            super.paint(g);
        }

        public void onBlurApply(BlurParameter parameter, Component component) {
            super.onBlurApply(parameter, component);
            if(returnOnInvisible(parameter, component))
                return;

            if(release != null && release == panel.selectedRelease){
                GlassUI.applyTopLayer(parameter);
                parameter.setShadowSize(5);
                parameter.setShape(ShapeUtils.createRoundRectangle(this, 0, 0));

                ArrayList<UIUtils.ShadowSide> shadows = new ArrayList<>();
                if(index > 0)
                    shadows.add(UIUtils.ShadowSide.TOP);
                if(index < 4)
                    shadows.add(UIUtils.ShadowSide.BOTTOM);
                parameter.setShadowClip(UIUtils.keepShadow(parameter, shadows.toArray(new UIUtils.ShadowSide[0])));
            }else{
                parameter.setVisible(false);
            }
        }

        public void setRelease(Release release){
            this.release = release;
            if(release != null) {
                version.setText((release.id.startsWith("v") ? "" : "v") + release.id);
                date.setText(new SimpleDateFormat("dd.MM.yyyy").format(release.date));
                icon.setIcon(panel.currentVersion.equals(release.id) ? selectedIcon : null);
            }else{
                version.setText("");
                date.setText("");
                icon.setIcon(null);
            }
        }
    }

}
