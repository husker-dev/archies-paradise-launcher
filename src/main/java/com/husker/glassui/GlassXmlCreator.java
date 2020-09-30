package com.husker.glassui;

import com.alee.extended.WebComponent;
import com.alee.laf.label.WebLabel;
import com.husker.launcher.Resources;
import com.husker.launcher.ui.Screen;
import com.husker.xml.AttributeUtils;
import com.husker.xml.SwingXmlCreator;
import com.husker.xml.TagProcessor;

import java.awt.*;

// TODO: Implement this shit
public class GlassXmlCreator extends SwingXmlCreator {

    public GlassXmlCreator(Screen screen){
        extendTagProcessor("label", new WebTagProcessor() {
            public Component createComponent() {
                return new WebLabel(){{
                    setForeground(GlassUI.Colors.labelText);
                    setPreferredHeight(16);
                    setFont(Resources.Fonts.ChronicaPro_ExtraBold);
                }};
            }
        });
    }

    abstract class WebTagProcessor extends TagProcessor{
        public WebTagProcessor(){
            applyWebLaFParameters(this);
        }
    }

    private void applyWebLaFParameters(TagProcessor processor){
        processor.addAttribute("margin", (component, value, parameters) -> {
            Insets margin = AttributeUtils.getInsets(value);
            ((WebComponent)component).setMargin(margin.top, margin.left, margin.bottom, margin.right);
        });
    }
}
