package com.husker.glassui.screens.main.play;

import com.alee.laf.label.WebLabel;
import com.husker.glassui.GlassUI;
import com.husker.glassui.components.BlurPanel;
import com.husker.glassui.components.BlurScalableImage;
import com.husker.launcher.components.TransparentPanel;
import com.husker.launcher.managers.NetManager;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;

import javax.swing.*;
import java.awt.*;

public class ModPanel extends BlurPanel {

    private final Screen screen;
    private final int index;
    private BlurScalableImage icon;
    private WebLabel name;

    public ModPanel(Screen screen, int index){
        super(screen, false);
        this.screen = screen;
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
                    }
                    public void onBlurApply(BlurParameter parameter, Component component) {
                        super.onBlurApply(parameter, component);
                        if(returnOnInvisible(parameter, component))
                            return;
                        if(component == this){
                            parameter.setShadowSize(0);
                            parameter.setTextureAlpha(1f);
                        }
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

    public void updateInfo(){
        NetManager.Client.ModInfo info = screen.getLauncher().NetManager.Client.getModInfo(index, true);
        if(info != null) {
            name.setText(info.getName());

            if (info.getIcon() != null)
                icon.setImage(info.getIcon());
            else
                icon.setImage(screen.getLauncher().Resources.Texture_EmptyIcon);
        }else{
            name.setText("");
            icon.setImage(null);
        }
    }
}
