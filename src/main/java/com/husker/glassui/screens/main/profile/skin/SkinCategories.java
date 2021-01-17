package com.husker.glassui.screens.main.profile.skin;

import com.alee.extended.layout.VerticalFlowLayout;
import com.husker.glassui.components.BlurButton;
import com.husker.glassui.components.BlurPagePanel;
import com.husker.glassui.components.BlurPanel;
import com.husker.glassui.screens.SimpleTitledScreen;
import com.husker.glassui.screens.main.MainScreen;
import com.husker.launcher.Resources;
import com.husker.launcher.api.API;
import com.husker.launcher.discord.Discord;
import com.husker.launcher.ui.components.MLabel;
import com.husker.launcher.ui.components.TransparentPanel;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.glassui.GlassUI;
import com.husker.launcher.ui.components.skin.SkinViewer;
import com.husker.launcher.ui.utils.ComponentUtils;
import com.husker.launcher.utils.filechooser.FileChooser;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;


public class SkinCategories extends SimpleTitledScreen {

    private int page = 0;
    private final int pageElements = 6;
    private final SkinFolderPanel[] skinPanel = new SkinFolderPanel[pageElements];
    private BlurPagePanel pagePanel;

    public SkinCategories() {
        super("Скины", "Категории");
    }

    public void onMenuInit(TransparentPanel panel) {
        panel.setLayout(new VerticalFlowLayout(0, 0));

        panel.add(new TransparentPanel(){{
            setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
            setPreferredWidth(620);
            setPreferredHeight(360);

            for(int i = 0; i < skinPanel.length; i++) {
                skinPanel[i] = new SkinFolderPanel(SkinCategories.this);
                add(skinPanel[i]);
            }
        }});
        panel.add(new TransparentPanel(){{
            setMargin(20, 100, 20, 100);
            add(pagePanel = new BlurPagePanel(SkinCategories.this){{
                addPageListener(SkinCategories.this::setPage);
            }});
        }});
    }

    public void onButtonsInit(TransparentPanel panel) {
        panel.setLayout(new GridBagLayout());
        panel.add(new BlurButton(this, "Назад"){{
            setImage(Resources.Icon_Back, 23, 23);
            addActionListener(e -> getLauncherUI().setScreen(MainScreen.class));
            setPadding(40, 40);
        }}, new GridBagConstraints(){{
            this.gridx = 0;
            this.weightx = 1;
        }});

        panel.add(new BlurButton(this, "Плащи"){{
            addActionListener(e -> getLauncherUI().setScreen(CapeListLoading.class));
            setPadding(40, 40);
        }}, new GridBagConstraints(){{
            this.gridx = 1;
            this.weightx = 1;
        }});

        panel.add(new BlurButton(this, "Добавить"){{
            setImage(Resources.Icon_Add, 23, 23);
            addActionListener(e -> {
                try{
                    FileChooser chooser = new FileChooser("Выбор скина");
                    chooser.addFileFilter("PNG Image", "png");

                    File file = chooser.open(getLauncher());
                    if(file != null)
                        getLauncherUI().setScreen(SkinApply.class, new Parameters("path", file.getAbsolutePath()));
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            });
            setPadding(30, 30);
        }}, new GridBagConstraints(){{
            this.gridx = 2;
            this.weightx = 1;
        }});
    }

    public void onShow(){
        super.onShow();
        Discord.setState(Discord.Texts.InSkins);

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
        private MLabel nameLabel;
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
                    screen.getLauncherUI().setScreen(SkinListLoading.class, new Parameters("folder", nameLabel.getText()));
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
                    add(nameLabel = new MLabel("[Name]"){{
                        setMargin(4, 5, 2, 5);
                        setPreferredHeight(35);
                        setVerticalAlignment(CENTER);
                        setHorizontalAlignment(CENTER);
                        setForeground(GlassUI.Colors.labelLightText);
                        setFont(Resources.Fonts.getChronicaProExtraBold(15));
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
                viewer.setPlayerTexture(API.Skins.getCategoryPreview(name));
            }catch (Exception ex){
                viewer.setPlayerTexture(Resources.Skin_Steve);
            }
        }
    }
}
