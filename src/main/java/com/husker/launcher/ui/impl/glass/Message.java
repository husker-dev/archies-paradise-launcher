package com.husker.launcher.ui.impl.glass;

import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.style.StyleId;
import com.husker.launcher.ui.CenteredMenuScreen;
import com.husker.launcher.ui.LauncherUI;
import com.husker.launcher.utils.UIUtils;
import com.husker.launcher.ui.impl.glass.components.BlurButton;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Arrays;

import static com.husker.launcher.utils.UIUtils.ShadowSide.BOTTOM;

public class Message extends CenteredMenuScreen {

    private final int title_size = 50;

    private WebLabel title;
    private WebLabel message;
    private String prevScreen = "";
    private String[] prevParameters;

    public void onMenuInit() {

        addBlurSegment(parameter -> {
            GlassUI.applyBottomLayer(parameter);
            parameter.setShape(new RoundRectangle2D.Double(getMenuX(), getMenuY(), getMenuWidth(), getMenuHeight() - 15, 25, 25));
        });

        addBlurSegment(parameter -> {
            GlassUI.applyTopLayer(parameter);
            parameter.setShape(new RoundRectangle2D.Double(getMenuX(), getMenuY(), getMenuWidth(), title_size, 25, 25));
            parameter.setShadowClip(UIUtils.keepShadow(parameter, BOTTOM));
        });


        setLayout(new GridBagLayout());

        addToMenu(title = GlassUI.createTitleLabel("Заголовок"));
        addIndent(30);
        addToMenu(message = GlassUI.createSimpleLabel("Сообщение", true));
        addIndent(30);
        addToMenu(new WebPanel(StyleId.panelTransparent){{
            setLayout(new VerticalFlowLayout());
            setMargin(0, 50, 0, 50);
            add(new BlurButton(Message.this, "Окей"){{
                addActionListener(e -> {
                    getLauncherUI().setScreen(prevScreen, prevParameters);
                });
            }});
        }});
    }

    public static void showMessage(LauncherUI ui, String title, String text, String prevScreen, String... parameters ){
        ui.setScreen("message", prevScreen + "," + String.join(",", parameters), title, text);
    }

    public void onShow() {
        super.onShow();

        if(getParameters().length != 3){
            title.setText("Ошибка!");
            message.setText("Сделай всё правильно!!!");
            return;
        }
        if(getParameters()[0].contains(",")){
            ArrayList<String> parameters = new ArrayList<>(Arrays.asList(getParameters()[0].split(",")));

            prevScreen = parameters.remove(0);
            prevParameters = parameters.toArray(new String[0]);
        }else{
            prevScreen = getParameters()[0];
            prevParameters = new String[]{};
        }



        title.setText(getParameters()[1]);
        message.setText(getParameters()[2]);
    }

}
