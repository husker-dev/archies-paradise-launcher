package com.husker.glassui.screens.main.profile.skin;

import com.alee.extended.layout.VerticalFlowLayout;
import com.husker.glassui.GlassUI;
import com.husker.glassui.components.BlurButton;
import com.husker.glassui.components.BlurPagePanel;
import com.husker.glassui.components.BlurPanel;
import com.husker.glassui.screens.SimpleTitledScreen;
import com.husker.launcher.Resources;
import com.husker.launcher.api.API;
import com.husker.launcher.ui.components.TransparentPanel;
import com.husker.launcher.ui.components.skin.SkinViewer;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.ui.utils.ComponentUtils;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class SkinList extends SimpleTitledScreen {

    private int page = 0;
    private final int pageElements = 6;
    private final SkinPanel[] skinPanel = new SkinPanel[pageElements];
    private BlurPagePanel pagePanel;

    public SkinList() {
        super("Скины", "[Категория]");
    }

    public void onMenuInit(TransparentPanel panel) {
        panel.setLayout(new VerticalFlowLayout(0, 0));

        panel.add(new TransparentPanel(){{
            setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
            setPreferredWidth(620);
            setPreferredHeight(360);

            for(int i = 0; i < skinPanel.length; i++) {
                skinPanel[i] = new SkinPanel(SkinList.this);
                add(skinPanel[i]);
            }
        }});
        panel.add(new TransparentPanel(){{
            setMargin(20, 100, 20, 100);
            add(pagePanel = new BlurPagePanel(SkinList.this){{
                addPageListener(SkinList.this::setPage);
            }});
        }});
    }


    public void onButtonsInit(TransparentPanel panel) {
        panel.add(new BlurButton(this, "Назад"){{
            setImage(Resources.Icon_Back, 23, 23);
            addActionListener(e -> getLauncherUI().setScreen(SkinCategoriesLoading.class, new Parameters("notReset", "1")));
            setPadding(40, 40);
        }});
    }

    public void setPage(int page){
        this.page = page;
        pagePanel.setSelectedPage(page);

        String[] skins = getParameterValue("skins").split(",");

        int thisPageIndex = page * pageElements;
        int thisPageElements = Math.min(6, skins.length - (page * pageElements));

        for(int i = 0; i < skinPanel.length; i++) {
            skinPanel[i].apply(getParameterValue("folder"), i < thisPageElements ? skins[thisPageIndex + i] : null);
            skinPanel[i].loadSkin(null, null);
        }

        final int PAGE = page;
        new Thread(() -> {
            for(int i = 0; i < thisPageElements; i++) {
                if(PAGE == this.page)
                    skinPanel[i].loadSkin(getParameterValue("folder"), skins[thisPageIndex + i]);
            }
        }).start();
    }

    public void onShow() {
        try {
            super.onShow();

            setSubTitle(getParameterValue("folder"));

            float pages = getParameterValue("skins").split(",").length / (float) pageElements;
            if (pages == (int) pages)
                pagePanel.setPages((int) pages);
            else
                pagePanel.setPages((int) pages + 1);

            if(getParameterValue("notReset", "0").equals("0"))
                setPage(0);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public static class SkinPanel extends BlurPanel {

        private final SkinViewer viewer;

        private String folder, name;

        public SkinPanel(Screen screen) {
            super(screen, true);

            setPreferredSize(160, 160);
            setLayout(new BorderLayout());

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent mouseEvent) {
                    viewer.setCamY(26);
                    viewer.setCamZoom(20);
                }
                public void mouseExited(MouseEvent mouseEvent) {
                    viewer.setCamY(22);
                    viewer.setCamZoom(30);
                    viewer.setRotationX(-23);
                    viewer.setRotationY(23);
                }

                public void mousePressed(MouseEvent mouseEvent) {
                    screen.getLauncherUI().setScreen(SkinApply.class, new Parameters("folder", folder, "name", name));
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
                ComponentUtils.makeMouseEventTransparent(this, SkinPanel.this);
            }});
        }

        public void onBlurApply(BlurParameter parameter, Component component) {
            super.onBlurApply(parameter, component);
            if(returnOnInvisible(parameter, component))
                return;
            if(component == this){
                GlassUI.applyBottomLayer(parameter);
                parameter.setShadowType(BlurParameter.ShadowType.INNER);
                parameter.setShadowSize(5);
            }
        }

        public void apply(String folder, String name){
            this.folder = folder;
            this.name = name;

            if(name == null)
                viewer.setPlayerTexture(null);
        }

        public void loadSkin(String folder, String name){
            if(folder == null || name == null){
                viewer.setPlayerTexture(null);
                return;
            }
            try {
                BufferedImage image = API.Skins.getCategorySkin(folder, name);
                viewer.setPlayerTexture(image);
            }catch (Exception ex){
                viewer.setPlayerTexture(Resources.Skin_Steve);
            }
        }
    }
}
