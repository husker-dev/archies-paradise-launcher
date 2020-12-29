package com.husker.launcher.ui.components;

import com.alee.laf.label.WebLabel;
import com.husker.launcher.Resources;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


import java.awt.*;

public class CLabel extends WebLabel {

    private static final Logger log = LogManager.getLogger(CLabel.class);


    public CLabel(){
        super("");
    }

    public CLabel(String text){
        super(text);
    }

    public void paint(Graphics g){
        super.paint(g);


    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

}
