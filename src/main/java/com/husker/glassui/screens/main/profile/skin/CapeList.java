package com.husker.glassui.screens.main.profile.skin;

import com.alee.extended.layout.VerticalFlowLayout;
import com.husker.glassui.GlassUI;
import com.husker.glassui.components.BlurButton;
import com.husker.glassui.components.BlurPagePanel;
import com.husker.glassui.components.BlurPanel;
import com.husker.glassui.screens.SimpleTitledScreen;
import com.husker.glassui.screens.main.MainScreen;
import com.husker.launcher.Resources;
import com.husker.launcher.api.API;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.ui.components.LabelButton;
import com.husker.launcher.ui.components.TransparentPanel;
import com.husker.launcher.ui.components.skin.SkinViewer;
import com.husker.launcher.ui.utils.ComponentUtils;
import com.husker.launcher.ui.utils.ShapeUtils;
import com.husker.launcher.utils.filechooser.FileChooser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.io.File;

import static com.husker.launcher.ui.utils.ShapeUtils.ALL_CORNERS;

public class CapeList extends SimpleTitledScreen {

    private int page = 0;
    private final int pageElements = 6;
    private final CapePanel[] capePanel = new CapePanel[pageElements];
    private BlurPagePanel pagePanel;

    public CapeList() {
        super("Плащи", "Коллекция");
    }

    public void onMenuInit(TransparentPanel panel) {
        panel.setLayout(new VerticalFlowLayout(0, 0));

        panel.add(new TransparentPanel(){{
            setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
            setPreferredWidth(620);
            setPreferredHeight(360);

            for(int i = 0; i < capePanel.length; i++) {
                capePanel[i] = new CapePanel(CapeList.this);
                add(capePanel[i]);
            }
        }});
        panel.add(new TransparentPanel(){{
            setMargin(20, 100, 20, 100);
            add(pagePanel = new BlurPagePanel(CapeList.this){{
                addPageListener(CapeList.this::setPage);
            }});
        }});
    }


    public void onButtonsInit(TransparentPanel panel) {
        panel.setLayout(new GridBagLayout());
        panel.add(new BlurButton(this, "Назад"){{
            setImage(Resources.Icon_Back, 23, 23);
            addActionListener(e -> getLauncherUI().setScreen(SkinCategoriesLoading.class));
            setPadding(40, 40);
        }}, new GridBagConstraints(){{
            this.gridx = 0;
            this.weightx = 1;
        }});

        panel.add(new BlurButton(this, "Сбросить"){{
            addActionListener(e -> getLauncherUI().setScreen(CapeApply.class, new Parameters("reset", "true")));
            setPadding(40, 40);
        }}, new GridBagConstraints(){{
            this.gridx = 1;
            this.weightx = 1;
        }});

        panel.add(new BlurButton(this, "Добавить"){{
            setImage(Resources.Icon_Add, 23, 23);
            addActionListener(e -> {
                try{
                    FileChooser chooser = new FileChooser("Выбор плаща (64x32)");
                    chooser.addFileFilter("PNG Image", "png");

                    File file = chooser.open(getLauncher());
                    if(file != null)
                        getLauncherUI().setScreen(CapeApply.class, new Parameters("path", file.getAbsolutePath()));
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

    public void setPage(int page){
        this.page = page;
        pagePanel.setSelectedPage(page);

        String[] capes = getParameterValue("capes").split(",");

        int thisPageIndex = page * pageElements;
        int thisPageElements = Math.min(6, capes.length - (page * pageElements));

        for(int i = 0; i < capePanel.length; i++) {
            capePanel[i].apply(i < thisPageElements ? capes[thisPageIndex + i] : null);
            capePanel[i].loadCape(null);
        }

        final int PAGE = page;
        new Thread(() -> {
            for(int i = 0; i < thisPageElements; i++) {
                if(PAGE == this.page)
                    capePanel[i].loadCape(capes[thisPageIndex + i]);
            }
        }).start();
    }

    public void onShow() {
        try {
            super.onShow();

            float pages = getParameterValue("capes").split(",").length / (float) pageElements;
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

    public static class CapePanel extends BlurPanel {

        private final SkinViewer viewer;
        private LabelButton button;
        private String name;
        private int rotations = 0;

        public CapePanel(Screen screen) {
            super(screen, true);

            setPreferredSize(160, 160);
            setLayout(new OverlayLayout(this));

            add(new TransparentPanel(){{
                setLayout(new BorderLayout());
                add(new TransparentPanel(){{
                    setLayout(new BorderLayout());
                    add(button = new LabelButton(Resources.Icon_Reply, Resources.Icon_Reply_Selected){{
                        setImageSize(20);
                        setPreferredSize(30, 30);
                        addActionListener(() -> screen.getLauncherUI().setScreen(CapeApply.class, new Parameters("name", name)));
                    }}, BorderLayout.EAST);
                }}, BorderLayout.SOUTH);
            }}, 0);

           add(viewer = new SkinViewer(){{
                setPreferredSize(new Dimension(10, 160));
                setCamZoom(30);
                setCamY(16);
                setRotationX(-206);
                setRotationY(26);
                ComponentUtils.makeMouseEventTransparent(this, CapePanel.this);
            }}, 1);
        }

        public void onBlurApply(BlurParameter parameter, Component component) {
            super.onBlurApply(parameter, component);
            if(returnOnInvisible(parameter, component))
                return;
            if(component == this){
                GlassUI.applyBottomLayer(parameter);
                int size = 30;
                Area full = new Area(parameter.getShape());
                Area btn = new Area(ShapeUtils.createRoundRectangle(
                        parameter.getShape().getBounds().x + parameter.getShape().getBounds().width - size,
                        parameter.getShape().getBounds().y + parameter.getShape().getBounds().height - size,
                        size + 50, size + 50,
                        20, 20, ALL_CORNERS));

                full.subtract(btn);
                parameter.setShape(full);
                parameter.setShadowType(BlurParameter.ShadowType.INNER);
                parameter.setShadowSize(5);
            }
        }

        public void apply(String name){
            this.name = name;
            button.setVisible(name != null);

            if(name == null)
                viewer.setPlayerTexture(null);
        }

        public void loadCape(String name){
            try {
                BufferedImage image = API.Skins.getCapeByName(name);
                viewer.setPlayerTexture(null, image);
                viewer.setRotationX(-206 - 360 * ++rotations);
                viewer.setRotationY(26);
            }catch (Exception ex){
                viewer.setPlayerTexture(null, null);
            }
        }
    }
}

