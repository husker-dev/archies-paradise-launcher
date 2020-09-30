package com.husker.xml;

import org.jdom2.Attribute;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Function;

public class AttributeUtils {

    public static void setTextApplicable(TagProcessor processor){
        processor.addComponentAttribute("text", (component, value, parameters) -> {
            try {
                Method method = component.getClass().getMethod("setText", String.class);
                method.setAccessible(true);
                method.invoke(component, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        processor.addComponentAttribute("alignmentX, alignX", (component, value, parameters) -> {
            try {
                Method method = component.getClass().getMethod("setHorizontalAlignment", Integer.class);
                method.setAccessible(true);
                method.invoke(component, getAlignment(value));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        processor.addComponentAttribute("alignmentY, alignY", (component, value, parameters) -> {
            try {
                Method method = component.getClass().getMethod("setVerticalAlignment", Integer.class);
                method.setAccessible(true);
                method.invoke(component, getAlignment(value));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        processor.addComponentAttribute("alignment, align", (component, value, parameters) -> {
            try {
                Method method = component.getClass().getMethod("setVerticalAlignment", Integer.class);
                method.setAccessible(true);
                method.invoke(component, getAlignment(value));

                method = component.getClass().getMethod("setHorizontalAlignment", Integer.class);
                method.setAccessible(true);
                method.invoke(component, getAlignment(value));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void setIconApplicable(TagProcessor processor) {
        processor.addComponentAttribute("icon", (component, value, parameters) -> setIcon(component, getIcon(parameters)));
        processor.addComponentAttribute("iconSize", (component, value, parameters) -> setIcon(component, getIcon(parameters)));
        processor.addComponentAttribute("iconWidth", (component, value, parameters) -> setIcon(component, getIcon(parameters)));
        processor.addComponentAttribute("iconHeight", (component, value, parameters) -> setIcon(component, getIcon(parameters)));
    }

    private static void setIcon(Component component, Icon icon){
        try {
            Method method = component.getClass().getMethod("setIcon", Icon.class);
            method.setAccessible(true);
            method.invoke(component, icon);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void callEvent(String methodName, TagProcessor.AttributeProcessorParameters parameters){
        try {
            if(parameters.getCallerObject() != null) {
                Method method = parameters.getCallerObject().getClass().getMethod(methodName);
                method.setAccessible(true);
                method.invoke(parameters.getCallerObject());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getAlignment(String value){
        if(value.equals("top") || value.equals("north"))
            return SwingConstants.TOP;
        if(value.equals("bottom") || value.equals("south"))
            return SwingConstants.BOTTOM;
        if(value.equals("left") || value.equals("west"))
            return SwingConstants.LEFT;
        if(value.equals("right") || value.equals("east"))
            return SwingConstants.RIGHT;
        if(value.equals("center"))
            return SwingConstants.CENTER;
        try {
            return Integer.parseInt(value);
        }catch (Exception ex){
            return 0;
        }
    }

    public static Object getPosition(Component parent, String value){
        if(parent == null || value == null)
            return -1;
        try {
            value = value.toLowerCase();
            if(parent instanceof Container){
                Container container = (Container) parent;
                if(container.getLayout() instanceof BorderLayout){
                    if(value.equals("top") || value.equals("north"))
                        return BorderLayout.NORTH;
                    if(value.equals("bottom") || value.equals("south"))
                        return BorderLayout.SOUTH;
                    if(value.equals("left") || value.equals("west"))
                        return BorderLayout.WEST;
                    if(value.equals("right") || value.equals("east"))
                        return BorderLayout.EAST;
                }
                return Integer.parseInt(value);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return -1;
    }

    public static LayoutManager getLayoutManager(String value){
        if(value.equals("border"))
            return new BorderLayout();
        if(value.startsWith("flow")){
            if(value.contains("(")){
                HashMap<String, Integer> types = new HashMap<String, Integer>(){{
                    put("center", FlowLayout.CENTER);
                    put("leading", FlowLayout.LEADING);
                    put("left", FlowLayout.LEFT);
                    put("right", FlowLayout.RIGHT);
                    put("trailing", FlowLayout.TRAILING);
                }};
                int vGap = 5;
                int hGap = 5;
                int type = 1;
                ArrayList<String> parameters = new ArrayList<>(Arrays.asList(value.split("\\(")[1].split("\\)")[0].split(",")));
                for(int i = 0; i < parameters.size(); i++) {
                    if (types.containsKey(parameters.get(i))) {
                        type = types.get(parameters.get(i));
                        parameters.remove(i);
                        break;
                    }
                }

                if(parameters.size() == 1){
                    vGap = Integer.parseInt(parameters.get(0));
                    hGap = Integer.parseInt(parameters.get(0));
                }
                if(parameters.size() == 2){
                    vGap = Integer.parseInt(parameters.get(0));
                    hGap = Integer.parseInt(parameters.get(1));
                }

                return new FlowLayout(type, vGap, hGap);
            }
        }
        return null;
    }

    public static Font getFont(Component component, String value, TagProcessor.AttributeProcessorParameters parameters){
        Font font = component.getFont();

        InputStream file = getFromResource(parameters, value);
        if(file != null) {
            try {
                font = Font.createFont(Font.TRUETYPE_FONT, file).deriveFont((float) font.getSize());
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }else
            font = new Font(value, Font.PLAIN, font.getSize());

        return font;
    }

    public static boolean getBoolean(String value){
        value = value.toLowerCase();
        return value.equals("true") || value.equals("1") || value.equals("yes");
    }

    public static Dimension getDimension(String value){
        try {
            if (value.contains(","))
                return new Dimension(Integer.parseInt(value.split(",")[0]), Integer.parseInt(value.split(",")[1]));
            return new Dimension(Integer.parseInt(value), Integer.parseInt(value));
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new Dimension(0, 0);
    }

    public static Rectangle getRectangle(String value){
        try {
            if (value.contains(","))
                return new Rectangle(Integer.parseInt(value.split(",")[0]), Integer.parseInt(value.split(",")[1]), Integer.parseInt(value.split(",")[2]), Integer.parseInt(value.split(",")[3]));
            return new Rectangle(Integer.parseInt(value), Integer.parseInt(value), Integer.parseInt(value), Integer.parseInt(value));
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new Rectangle(0, 0, 0, 0);
    }

    public static Insets getInsets(String value){
        try {
            if (value.contains(","))
                return new Insets(Integer.parseInt(value.split(",")[0]), Integer.parseInt(value.split(",")[1]), Integer.parseInt(value.split(",")[2]), Integer.parseInt(value.split(",")[3]));
            return new Insets(Integer.parseInt(value), Integer.parseInt(value), Integer.parseInt(value), Integer.parseInt(value));
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new Insets(0, 0, 0, 0);
    }

    public static int getInt(String value){
        try {
            return Integer.parseInt(value);
        }catch (Exception ex){
            ex.printStackTrace();
            return 0;
        }
    }

    public static float getFloat(String value){
        try {
            return Float.parseFloat(value);
        }catch (Exception ex){
            ex.printStackTrace();
            return 0;
        }
    }

    public static Color getColor(String color){
        if(color.contains("#")) {
            color = color.substring(1);
            int r = Integer.parseInt(color.substring(0, 2));
            int g = Integer.parseInt(color.substring(2, 4));
            int b = Integer.parseInt(color.substring(4, 6));
            int a = 255;
            if(color.length() == 8)
                a = Integer.parseInt(color.substring(6, 8));
            return new Color(r, g, b, a);
        }
        if(color.contains(",")){
            int[] rgba = new int[]{255, 255, 255, 255};

            for(int i = 0; i < color.split(",").length; i++)
                rgba[i] = Integer.parseInt(color.split(",")[i]);
            return new Color(rgba[0], rgba[1], rgba[2], rgba[3]);
        }
        switch (color.toLowerCase()) {
            case "black":
                return Color.BLACK;
            case "blue":
                return Color.BLUE;
            case "cyan":
                return Color.CYAN;
            case "darkgray":
                return Color.DARK_GRAY;
            case "gray":
                return Color.GRAY;
            case "green":
                return Color.GREEN;
            case "yellow":
                return Color.YELLOW;
            case "lightgray":
                return Color.LIGHT_GRAY;
            case "magneta":
                return Color.MAGENTA;
            case "orange":
                return Color.ORANGE;
            case "pink":
                return Color.PINK;
            case "red":
                return Color.RED;
            case "white":
                return Color.WHITE;
        }
        return new Color(0, 0, 0, 255);
    }

    public static Icon getIcon(TagProcessor.AttributeProcessorParameters parameters){
        int width = 0;
        int height = 0;
        InputStream image = null;

        for (Attribute attribute : parameters.getXmlElement().getAttributes()) {
            if (attribute.getName().equals("iconSize")) {
                width = getDimension(attribute.getValue()).width;
                height = getDimension(attribute.getValue()).height;
            }
            if (attribute.getName().equals("iconWidth"))
                width = Integer.parseInt(attribute.getValue());
            if (attribute.getName().equals("iconHeight"))
                height = Integer.parseInt(attribute.getValue());
            if (attribute.getName().equals("icon"))
                image = getFromResource(parameters, attribute.getValue());
        }

        try {
            if (image != null)
                return new ImageIcon(ImageIO.read(image).getScaledInstance(width, height, Image.SCALE_SMOOTH));
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    public static InputStream getFromResource(TagProcessor.AttributeProcessorParameters parameters, String path){
        InputStream image;
        for(Function<String, InputStream> function : parameters.getCreator().getResourceProcessors())
            if((image = function.apply(path)) != null)
                return image;
        return null;
    }
}
