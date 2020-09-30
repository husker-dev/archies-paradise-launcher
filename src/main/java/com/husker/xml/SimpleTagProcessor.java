package com.husker.xml;

import java.awt.*;

public class SimpleTagProcessor extends TagProcessor{

    private final Class<? extends Component> componentClass;

    public SimpleTagProcessor(Class<? extends Component> componentClass){
        this.componentClass = componentClass;
    }

    public Component createComponent() {
        try {
            return componentClass.getConstructor().newInstance();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
}
