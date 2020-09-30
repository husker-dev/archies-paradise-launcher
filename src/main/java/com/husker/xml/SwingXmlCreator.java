package com.husker.xml;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;

public class SwingXmlCreator {

    public static Component getComponent(Class<?> clazz, String path){
        return new SwingXmlCreator().createComponent(clazz.getResourceAsStream(path));
    }

    public static Component getComponent(Class<?> clazz, String path, Object eventHandler){
        return new SwingXmlCreator(eventHandler).createComponent(clazz.getResourceAsStream(path));
    }

    public static Component getComponent(InputStream inputStream){
        return new SwingXmlCreator().createComponent(inputStream);
    }

    public static Component getComponent(InputStream inputStream, Object eventHandler){
        return new SwingXmlCreator(eventHandler).createComponent(inputStream);
    }

    private final ArrayList<Function<String, InputStream>> resourceProcessors = new ArrayList<>();
    private final HashMap<String, TagProcessor> processors = new HashMap<>();
    private final HashMap<String, Object> parameters = new HashMap<>();

    public SwingXmlCreator(){
        this(null);
    }

    public SwingXmlCreator(Object eventHandler){
        setEventHandlerObject(eventHandler);

        addTagProcessor("panel", new SimpleTagProcessor(JPanel.class));
        addTagProcessor("label", new SimpleTagProcessor(JLabel.class){
            {
                AttributeUtils.setTextApplicable(this);
                AttributeUtils.setIconApplicable(this);
            }
        });
        addTagProcessor("button", new SimpleTagProcessor(JButton.class){
            {
                AttributeUtils.setTextApplicable(this);
                AttributeUtils.setIconApplicable(this);
            }
        });
        addTagProcessor("textfield", new SimpleTagProcessor(JTextField.class){
            {
                AttributeUtils.setTextApplicable(this);
                addComponentAttribute("columns", (component, value, parameters) -> ((JTextField)component).setColumns(AttributeUtils.getInt(value)));
                addComponentAttribute("onChanged, change", (component, value, parameters) -> {
                    ((JTextField)component).getDocument().addDocumentListener(new DocumentListener() {
                        public void changedUpdate(DocumentEvent e) {
                            warn();
                        }
                        public void removeUpdate(DocumentEvent e) {
                            warn();
                        }
                        public void insertUpdate(DocumentEvent e) {
                            warn();
                        }
                        public void warn() {
                            AttributeUtils.callEvent(value, parameters);
                        }
                    });
                });
            }
        });
        addTagProcessor("passwordfield", new SimpleTagProcessor(JPasswordField.class){
            {
                AttributeUtils.setTextApplicable(this);
                addComponentAttribute("columns", (component, value, parameters) -> ((JPasswordField)component).setColumns(AttributeUtils.getInt(value)));
                addComponentAttribute("char", (component, value, parameters) -> ((JPasswordField)component).setEchoChar(value.charAt(0)));
                addComponentAttribute("onChanged, change", (component, value, parameters) -> {
                    ((JPasswordField)component).getDocument().addDocumentListener(new DocumentListener() {
                        public void changedUpdate(DocumentEvent e) {
                            warn();
                        }
                        public void removeUpdate(DocumentEvent e) {
                            warn();
                        }
                        public void insertUpdate(DocumentEvent e) {
                            warn();
                        }
                        public void warn() {
                            AttributeUtils.callEvent(value, parameters);
                        }
                    });
                });
            }
        });
    }

    public void setEventHandlerObject(Object object){
        setParameter("eventHandler", object);
    }

    public Object getEventHandlerObject(){
        return getParameter("eventHandler");
    }

    public void setParameter(String key, Object value){
        parameters.put(key, value);
    }

    public Object getParameter(String key){
        return parameters.get(key);
    }

    public Component createComponent(Class<?> clazz, String path){
        return createComponent(clazz.getResourceAsStream(path));
    }

    public Component createComponent(InputStream inputStream){
        try {
            Document document = new SAXBuilder().build(new StringReader(readInputStream(inputStream)));
            Element root = document.getRootElement();

            if(root.getName().equals("swing") && root.getChildren().size() > 0){
                ComponentParameters parameters = processElement(null, root.getChildren().get(0));
                if(parameters != null)
                    return parameters.component;
                else
                    return null;
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    private ComponentParameters processElement(Component parent, Element element){
        try {
            if (processors.containsKey(element.getName())) {
                TagProcessor processor = processors.get(element.getName());

                Component component = processor.createComponent();
                for (Attribute attribute : element.getAttributes())
                    processor.applyAttribute(attribute, component, new TagProcessor.AttributeProcessorParameters(){{
                        setCallerObject(getEventHandlerObject());
                        setCreator(SwingXmlCreator.this);
                        setXmlElement(element);
                        setParentComponent(parent);
                    }});

                for (Element childElement : element.getChildren()) {
                    ComponentParameters parameters = processElement(component, childElement);
                    if(parameters != null)
                        processor.addComponent(component, parameters.component, parameters.arg);
                }

                return new ComponentParameters(component, AttributeUtils.getPosition(parent, element.getAttributeValue("position")));
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    private static String readInputStream(InputStream inputStream){
        Scanner scanner = new Scanner(inputStream);
        StringBuilder builder = new StringBuilder();
        while(scanner.hasNext())
            builder.append(scanner.nextLine()).append("\n");
        scanner.close();
        return builder.toString();
    }

    public void addTagProcessor(String tagName, TagProcessor processor){
        processors.put(tagName, processor);
    }

    public void extendTagProcessor(String tagName, TagProcessor processor){
        if(processors.containsKey(tagName)) {
            HashMap<String, TagProcessor.AttributeProcessor> oldProcessors = processors.get(tagName).getAttributeProcessors();
            HashMap<String, TagProcessor.AttributeProcessor> newProcessors = processor.getAttributeProcessors();

            processors.put(tagName, processor);
            for (Map.Entry<String, TagProcessor.AttributeProcessor> entry : oldProcessors.entrySet())
                processor.addAttribute(entry.getKey(), entry.getValue());
            for (Map.Entry<String, TagProcessor.AttributeProcessor> entry : newProcessors.entrySet())
                processor.addAttribute(entry.getKey(), entry.getValue());
        }else
            addTagProcessor(tagName, processor);
    }

    public void addResourceProcessor(Function<String, InputStream> processor){
        resourceProcessors.add(processor);
    }

    public Function<String, InputStream>[] getResourceProcessors(){
        return resourceProcessors.toArray(new Function[0]);
    }

    private static class ComponentParameters {
        final Component component;
        final Object arg;

        public ComponentParameters(Component component, Object arg){
            this.component = component;
            this.arg = arg;
        }
    }
}
