package com.husker.launcher.utils.filechooser;

import com.husker.launcher.ui.utils.ComponentUtils;
import com.husker.launcher.utils.SystemUtils;
import com.husker.launcher.utils.filechooser.api.JnaFileChooser;


import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
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
        if(SystemUtils.isWindows()) {
            try {
                JnaFileChooser fc = new JnaFileChooser();
                fc.setMultiSelectionEnabled(multiSelectionEnabled);
                HashMap<Mode, JnaFileChooser.Mode> modes = new HashMap<Mode, JnaFileChooser.Mode>() {{
                    put(Mode.FILES_ONLY, JnaFileChooser.Mode.Files);
                    put(Mode.DIRECTORIES_ONLY, JnaFileChooser.Mode.Directories);
                    put(Mode.FILES_AND_DIRECTORIES, JnaFileChooser.Mode.FilesAndDirectories);
                }};

                fc.setMode(modes.get(mode));

                for (String[] filter : filters)
                    fc.addFilter(filter[0], Arrays.copyOfRange(filter, 1, filter.length));

                if (!(parent instanceof Window))
                    parent = SwingUtilities.windowForComponent(parent);
                if (fc.showOpenDialog((Window) parent))
                    return fc.getSelectedFile();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }else{
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                JFileChooser chooser = new JFileChooser(title);
                chooser.setMultiSelectionEnabled(multiSelectionEnabled);

                HashMap<Mode, Integer> modes = new HashMap<Mode, Integer>() {{
                    put(Mode.FILES_ONLY, JFileChooser.FILES_ONLY);
                    put(Mode.DIRECTORIES_ONLY, JFileChooser.DIRECTORIES_ONLY);
                    put(Mode.FILES_AND_DIRECTORIES, JFileChooser.FILES_AND_DIRECTORIES);
                }};

                chooser.setFileSelectionMode(modes.get(mode));

                chooser.setAcceptAllFileFilterUsed(false);
                for (String[] filter : filters)
                    chooser.addChoosableFileFilter(new FileNameExtensionFilter(filter[0], Arrays.copyOfRange(filter, 1, filter.length)));

                if (!(parent instanceof Window))
                    parent = SwingUtilities.windowForComponent(parent);
                if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION)
                    return chooser.getSelectedFile();

            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        return null;
    }
}
