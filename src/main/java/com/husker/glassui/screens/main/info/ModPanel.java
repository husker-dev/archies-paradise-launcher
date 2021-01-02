package com.husker.glassui.screens.main.info;

import com.husker.glassui.GlassUI;
import com.husker.glassui.components.BlurPanel;
import com.husker.glassui.components.BlurScalableImage;
import com.husker.launcher.ui.components.MLabel;
import com.husker.launcher.ui.components.TransparentPanel;
import com.husker.launcher.api.API;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;

import javax.swing.*;
import java.awt.*;

public class ModPanel extends BlurPanel {

    private final int index;
    private BlurScalableImage icon;
    private MLabel name;

    private final String clientId;

    public ModPanel(Screen screen, String clientId, int index){
        super(screen, false);
        this.clientId = clientId;
        this.index = index;

        setLayout(new OverlayLayout(this));

        add(new TransparentPanel(){{
            setLayout(new BorderLayout(0, 0));

            add(new TransparentPanel(){{
                setMargin(5);
                setLayout(new BorderLayout());
                add(icon = new BlurScalableImage(screen){
                    {
                        setFitType(FitType.FIT_XY);
                        setAnimated(true);
                    }
                    public void onBlurApply(BlurParameter parameter, Component component) {
                        super.onBlurApply(parameter, component);
                        if(returnOnInvisible(parameter, component))
                            return;
                        if(component == this)
                            parameter.setShadowSize(0);
                    }
                });
            }});

            add(name = GlassUI.createTagLabel(screen, ""), BorderLayout.SOUTH);
            name.setHorizontalAlignment(SwingConstants.CENTER);
            name.setFont(name.getFont().deriveFont(12f));
        }});

        setPreferredSize(40, 10);
    }

    public void onBlurApply(BlurParameter parameter, Component component) {
        super.onBlurApply(parameter, component);
        if(returnOnInvisible(parameter, component))
            return;
        if(component == this){
            parameter.setShadowType(BlurParameter.ShadowType.INNER);
            parameter.setShadowSize(5);
        }
    }

    public void updateInfo(int modCount){
        try {
            if(index > modCount - 1)
                throw new RuntimeException("Out of border");
            API.Client.ModInfo info = API.Client.getModInfo(clientId, index);
            if(info != null) {
                name.setText(info.getName());
                icon.setImage(info.hasIcon() ? API.Client.getModIcon(clientId, info.getIndex()) : null);
            }
        } catch (Exception e) {
            name.setText("");
            icon.setImage(null);
        }
    }
}
