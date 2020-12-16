package com.husker.launcher.server.core;

import com.husker.launcher.server.services.http.ImageLink;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Screenshots {

    public static int getCount(){
        return getFiles().length;
    }

    public static ImageLink get(int index) throws IOException {
        return getSized(index, 600);
    }

    public static ImageLink getPreview(int index) throws IOException {
        return getSized(index, 150);
    }

    public static ImageLink getFull(int index) throws IOException {
        return getSized(index, -1);
    }

    public static ImageLink getSized(int index, int width) throws IOException {
        if(index > getCount())
            return null;
        ImageLink image = new ImageLink(getFiles()[index]);
        if(width == -1)
            return image;
        int height = (int)((float)width / (float)image.getWidth() * (float)image.getHeight());

        String cachedPath = "./screenshots/cached/" + width + "/" + image.getFile().getName();
        if(!Files.exists(Paths.get(cachedPath)))
            image.saveScaledInstance(width, height, cachedPath);
        return new ImageLink(cachedPath);
    }

    public static File[] getFiles(){
        return new File("./screenshots").listFiles(pathname -> pathname.getName().endsWith(".png"));
    }
}
