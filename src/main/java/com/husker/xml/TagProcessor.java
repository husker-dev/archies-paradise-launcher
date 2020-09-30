package com.husker.xml;

import org.jdom2.Attribute;
import org.jdom2.Element;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import java.util.HashMap;

import static com.husker.xml.AttributeUtils.*;

public abstract class TagProcessor {

    private final HashMap<String, AttributeProcessor> attributeProcessors = new HashMap<String, AttributeProcessor>(){
        public AttributeProcessor put(String key, AttributeProcessor value){
            if(key.contains(",")){
                for(String s : key.split(","))
                    super.put(s.trim(), value);
            }else
                super.put(key, value);
            return value;
        }
    };

    public TagProcessor(){
        addComponentAttribute("font", (component, value, parameters) -> component.setFont(getFont(component, value, parameters)));
        addComponentAttribute("fontSize", (component, value, parameters) -> component.setFont(component.getFont().deriveFont(getFloat(value))));

        addComponentAttribute("background, bg", (component, value, parameters) -> component.setBackground(getColor(value)));
        addComponentAttribute("foreground, fg, textColor", (component, value, parameters) -> component.setForeground(getColor(value)));
        addComponentAttribute("enabled", (component, value, parameters) -> component.setEnabled(getBoolean(value)));
        addComponentAttribute("visible", (component, value, parameters) -> component.setVisible(getBoolean(value)));
        addComponentAttribute("name", (component, value, parameters) -> component.setName(value));
        addComponentAttribute("focusable", (component, value, parameters) -> component.setFocusable(getBoolean(value)));
        addComponentAttribute("prefSize, pref, preferredSize, size", (component, value, parameters) -> component.setPreferredSize(getDimension(value)));
        addComponentAttribute("minSize, min, minimumSize", (component, value, parameters) -> component.setMinimumSize(getDimension(value)));
        addComponentAttribute("maxSize, max, maximumSize", (component, value, parameters) -> component.setMaximumSize(getDimension(value)));
        addComponentAttribute("onClick, click, action", (component, value, parameters) -> {
            component.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent mouseEvent) {
                AttributeUtils.callEvent(value, parameters);
                }
            });
        });
        addComponentAttribute("onPress, press", (component, value, parameters) -> {
            component.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent mouseEvent) {
                    AttributeUtils.callEvent(value, parameters);
                }
            });
        });
        addComponentAttribute("onHover, hover", (component, value, parameters) -> {
            component.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent mouseEvent) {
                    AttributeUtils.callEvent(value, parameters);
                }
            });
        });

        addJComponentAttribute("alignmentX, alignX", (component, value, parameters) -> component.setAlignmentX(getFloat(value)));
        addJComponentAttribute("alignmentY, alignY", (component, value, parameters) -> component.setAlignmentY(getFloat(value)));

        addContainerAttribute("layout", (component, value, parameters) -> component.setLayout(getLayoutManager(value)));
    }

    public abstract Component createComponent();

    public void addComponent(Component component, Component child, Object arg){
        if(component instanceof Container)
            ((Container)component).add(child, arg);
    }

    public void addAttribute(String key, AttributeProcessor processor){
        attributeProcessors.put(key, processor);
    }

    public void addComponentAttribute(String key, ComponentAttributeProcessor processor){
        attributeProcessors.put(key, (component, value, parameters) -> {
            if(component instanceof Component)
                processor.accept((Component)component, value, parameters);
        });
    }

    public void addJComponentAttribute(String key, JComponentAttributeProcessor processor){
        attributeProcessors.put(key, (component, value, parameters) -> {
            if(component instanceof JComponent)
                processor.accept((JComponent)component, value, parameters);
        });
    }

    public void addContainerAttribute(String key, ContainerAttributeProcessor processor){
        attributeProcessors.put(key, (component, value, parameters) -> {
            if(component instanceof Container)
                processor.accept((Container)component, value, parameters);
        });
    }

    public void applyAttribute(Attribute attribute, Component component, AttributeProcessorParameters parameters){
        if(attributeProcessors.containsKey(attribute.getName()))
            attributeProcessors.get(attribute.getName()).accept(component, attribute.getValue(), parameters);
    }

    public HashMap<String, AttributeProcessor> getAttributeProcessors(){
        return attributeProcessors;
    }


    public interface AttributeProcessor{
        void accept(Object component, String value, AttributeProcessorParameters parameters);
    }

    public interface ComponentAttributeProcessor{
        void accept(Component component, String value, AttributeProcessorParameters parameters);
    }

    public interface JComponentAttributeProcessor{
        void accept(JComponent component, String value, AttributeProcessorParameters parameters);
    }

    public interface ContainerAttributeProcessor{
        void accept(Container component, String value, AttributeProcessorParameters parameters);
    }

    public static class AttributeProcessorParameters{
        private Object callerObject;
        private Element xmlElement;
        private Component parentComponent;
        private SwingXmlCreator creator;

        public Object getCallerObject() {
            return callerObject;
        }

        public void setCallerObject(Object callerObject) {
            this.callerObject = callerObject;
        }

        public Element getXmlElement() {
            return xmlElement;
        }

        public void setXmlElement(Element xmlElement) {
            this.xmlElement = xmlElement;
        }

        public Component getParentComponent() {
            return parentComponent;
        }

        public void setParentComponent(Component parentComponent) {
            this.parentComponent = parentComponent;
        }

        public SwingXmlCreator getCreator() {
            return creator;
        }

        public void setCreator(SwingXmlCreator creator) {
            this.creator = creator;
        }
    }

}
