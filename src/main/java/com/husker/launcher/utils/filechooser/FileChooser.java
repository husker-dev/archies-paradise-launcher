package com.husker.launcher.utils.filechooser;

import com.husker.launcher.ui.utils.ComponentUtils;
import com.husker.launcher.utils.filechooser.api.JnaFileChooser;


import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class FileChooser {

    public enum Mode{
        FILES_ONLY,
        DIRECTORIES_ONLY,
        FILES_AND_DIRECTORIES
    }

    private String title = "Untitled";
    private final ArrayList<String[]> filters = new ArrayList<>();
    private Mode mode = Mode.FILES_ONLY;
    private boolean multiSelectionEnabled = false;

    public FileChooser(String title){
        this.title = title;
    }

    public FileChooser(Mode mode){
        this.mode = mode;
    }

    public FileChooser(String title, Mode mode){
        this.title = title;
        this.mode = mode;
    }

    public void addFileFilter(String name, String... extensions){
        filters.add(new ArrayList<String>(){{
            add(name);
            addAll(Arrays.asList(extensions));
        }}.toArray(new String[0]));
    }

    public void setMode(Mode mode){
        this.mode = mode;
    }

    public void setMultiSelectionEnabled(boolean enabled){
        multiSelectionEnabled = enabled;
    }

    public File open(Component parent){
        try {
            JnaFileChooser fc = new JnaFileChooser();
            fc.setMultiSelectionEnabled(multiSelectionEnabled);
            HashMap<Mode, JnaFileChooser.Mode> modes = new HashMap<>() {{
                put(Mode.FILES_ONLY, JnaFileChooser.Mode.Files);
                put(Mode.DIRECTORIES_ONLY, JnaFileChooser.Mode.Directories);
                put(Mode.FILES_AND_DIRECTORIES, JnaFileChooser.Mode.FilesAndDirectories);
            }};

            fc.setMode(modes.get(mode));

            for (String[] filter : filters)
                fc.addFilter(filter[0], Arrays.copyOfRange(filter, 1, filter.length));

            if(!(parent instanceof Window))
                parent = SwingUtilities.windowForComponent(parent);
            if (fc.showOpenDialog((Window)parent))
                return fc.getSelectedFile();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
}
