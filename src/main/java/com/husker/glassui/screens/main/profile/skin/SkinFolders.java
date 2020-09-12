package com.husker.glassui.screens.main.profile.skin;

import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.laf.WebLookAndFeel;
import com.alee.laf.label.WebLabel;
import com.husker.glassui.components.BlurButton;
import com.husker.glassui.components.BlurPageList;
import com.husker.glassui.components.BlurPanel;
import com.husker.glassui.screens.SimpleTitledScreen;
import com.husker.launcher.Resources;
import com.husker.launcher.components.TransparentPanel;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.glassui.GlassUI;
import com.husker.launcher.components.skin.SkinViewer;
import com.husker.launcher.utils.ComponentUtils;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;


public class SkinFolders extends SimpleTitledScreen {

    private int page = 0;
    private final int pageElements = 6;
    private final SkinFolderPanel[] skinPanel = new SkinFolderPanel[pageElements];
    private BlurPageList pagePanel;

    public SkinFolders() {
        super("Скины", "Категории");
    }

    public void onMenuInit(TransparentPanel panel) {
        panel.setLayout(new VerticalFlowLayout(0, 0));

        panel.add(new TransparentPanel(){{
            setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
            setPreferredWidth(620);
            setPreferredHeight(360);

            for(int i = 0; i < skinPanel.length; i++) {
                skinPanel[i] = new SkinFolderPanel(SkinFolders.this);
                add(skinPanel[i]);
            }
        }});
        panel.add(new TransparentPanel(){{
            setMargin(20, 100, 20, 100);
            add(pagePanel = new BlurPageList(SkinFolders.this){{
                addPageListener(SkinFolders.this::setPage);
            }});
        }});
    }

    public void onButtonsInit(TransparentPanel panel) {
        panel.setLayout(new GridBagLayout());
        panel.add(new BlurButton(this, "Назад"){{
            addActionListener(e -> getLauncherUI().setScreen("main"));
            setMargin(3, 40, 0, 40);
        }}, new GridBagConstraints(){{
            this.gridx = 0;
            this.weightx = 1;
        }});

        panel.add(new BlurButton(this, "Открыть..."){{
            addActionListener(e -> {
                try{
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                    JFileChooser chooser = new JFileChooser(){{
                        setDialogTitle("Выбор скина");
                        setFileFilter(new FileFilter() {
                            public String getDescription() {
                                return "PNG Images (*.png)";
                            }

                            public boolean accept(File f) {
                                return f.isDirectory() || f.getName().toLowerCase().endsWith(".png");
                            }
                        });
                    }};

                    if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
                        getLauncherUI().setScreen("skin_apply", new Parameters("path", chooser.getSelectedFile().getAbsolutePath()));

                    WebLookAndFeel.install();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            });
            setMargin(3, 30, 0, 30);
        }}, new GridBagConstraints(){{
            this.gridx = 1;
            this.weightx = 1;
        }});
    }

    public void onShow(){
        super.onShow();

        float pages = getParameterValue("folders").split(",").length / (float)pageElements;
        if(pages == (int)pages)
            pagePanel.setPages((int)pages);
        else
            pagePanel.setPages((int)pages + 1);

        if(getParameterValue("notReset", "0").equals("0"))
            setPage(0);
    }

    public void setPage(int page){
        this.page = page;
        pagePanel.setSelectedPage(page);

        String[] folders = getParameterValue("folders").split(",");

        int thisPageIndex = page * pageElements;
        int thisPageElements = Math.min(6, folders.length - (page * pageElements));

        for(int i = 0; i < skinPanel.length; i++) {
            skinPanel[i].setTitle(i < thisPageElements ? folders[thisPageIndex + i] : null);
            skinPanel[i].loadSkin(null);
        }

        final int PAGE = page;
        new Thread(() -> {
            for(int i = 0; i < thisPageElements; i++) {
                if(PAGE == this.page)
                    skinPanel[i].loadSkin(folders[thisPageIndex + i]);
            }
        }).start();
    }


    public static class SkinFolderPanel extends BlurPanel{

        private final SkinViewer viewer;
        private WebLabel nameLabel;
        private final BlurPanel namePanel;
        private boolean hovered = false;

        public SkinFolderPanel(Screen screen) {
            super(screen, true);

            setPreferredSize(160, 160);
            setLayout(new BorderLayout());

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent mouseEvent) {
                    hovered = true;

                    nameLabel.setForeground(GlassUI.Colors.labelText);
                    viewer.setCamY(26);
                    viewer.setCamZoom(20);
                }
                public void mouseExited(MouseEvent mouseEvent) {
                    hovered = false;

                    nameLabel.setForeground(GlassUI.Colors.labelLightText);
                    viewer.setCamY(22);
                    viewer.setCamZoom(30);
                    viewer.setRotationX(-23);
                    viewer.setRotationY(23);
                }

                public void mousePressed(MouseEvent mouseEvent) {
                    screen.getLauncherUI().setScreen("skin_list_loading", new Parameters("folder", nameLabel.getText()));
                }
            });

            addMouseMotionListener(new MouseAdapter() {
                public void mouseMoved(MouseEvent mouseEvent) {
                    int fromX = -33;
                    int toX = -13;
                    int fromY = 13;
                    int toY = 33;

                    Point mouse = getMousePosition();
                    if(mouse != null) {
                        int mouseX = mouse.x;
                        int mouseY = mouse.y;

                        viewer.setRotationX(fromX + (toX - fromX) * (mouseX / (float) getWidth()));
                        viewer.setRotationY(fromY + (toY - fromY) * (mouseY / (float) getHeight()));
                    }
                }
            });

            add(viewer = new SkinViewer(){{
                setPreferredSize(new Dimension(10, 160));
                setCamZoom(30);
                setCamY(22);
                setRotationEnabled(false);
                ComponentUtils.makeMouseEventTransparent(this, SkinFolderPanel.this);
            }});
            add(namePanel = new BlurPanel(screen){
                {
                    ComponentUtils.makeMouseEventTransparent(this, SkinFolderPanel.this);
                    add(nameLabel = new WebLabel("[Name]"){{
                        setMargin(4, 5, 2, 5);
                        setPreferredHeight(35);
                        setVerticalAlignment(CENTER);
                        setHorizontalAlignment(CENTER);
                        setForeground(GlassUI.Colors.labelLightText);
                        setFont(Resources.Fonts.ChronicaPro_ExtraBold.deriveFont(15f));
                        ComponentUtils.makeMouseEventTransparent(this, SkinFolderPanel.this);
                    }});
                }

                public void onBlurApply(BlurParameter parameter, Component component) {
                    super.onBlurApply(parameter, component);
                    if(returnOnInvisible(parameter, component))
                        return;
                    if(component == this) {
                        GlassUI.applyTag(parameter);
                        parameter.setAdditionColor(GlassUI.Colors.buttonDefault);
                    }
                }
            }, BorderLayout.SOUTH);
        }

        public void onBlurApply(BlurParameter parameter, Component component) {
            super.onBlurApply(parameter, component);
            if(returnOnInvisible(parameter, component))
                return;
            if(component == this){
                parameter.setVisible(namePanel.isVisible());
                if(!namePanel.isVisible())
                    return;

                GlassUI.applyBottomLayer(parameter);
                parameter.setShadowType(BlurParameter.ShadowType.INNER);
                parameter.setShadowSize(5);
            }
        }

        public void setTitle(String title){
            namePanel.setVisible(title != null);
            if(title == null){
                viewer.setPlayerTexture(null);
                nameLabel.setText("");
            }else
                nameLabel.setText(title);
        }

        public void loadSkin(String name){
            if(name == null){
                viewer.setPlayerTexture(null);
                return;
            }

            try {
                BufferedImage image = getScreen().getLauncher().NetManager.Skins.getFolderPreview(name);
                viewer.setPlayerTexture(image);
            }catch (Exception ex){
                viewer.setPlayerTexture(getScreen().getLauncher().Resources.Skin_Steve);
            }
        }
    }
}
