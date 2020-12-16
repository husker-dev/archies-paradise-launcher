package com.husker.launcher.server.services.http;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

public class ImageLink {

    private final File file;
    private Dimension size;

    public ImageLink(String path){
        this(new File(path));
    }

    public ImageLink(File file){
        if(!Files.exists(Paths.get(file.getAbsolutePath())))
            throw new NullPointerException("Image '" + file.getAbsolutePath() + "' doesn't exist");
        this.file = file;
    }

    public ImageLink saveScaledInstance(int width, int height, String path) throws IOException {
        System.out.println("1: " + path);
        String directory = path.substring(0, path.lastIndexOf("/"));
        Files.createDirectories(Paths.get(directory));
        System.out.println("2: " + directory);

        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        scaled.createGraphics().drawImage(ImageIO.read(file).getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH), 0, 0, null);
        ImageIO.write(scaled, "png", new File(path));
        System.gc();
        return new ImageLink(path);
    }

    public InputStream getInputStream() throws FileNotFoundException {
        return new FileInputStream(file);
    }

    public File getFile(){
        return file;
    }

    public int getWidth() throws IOException {
        return getSize().width;
    }

    public int getHeight() throws IOException {
        return getSize().height;
    }

    public Dimension getSize() throws IOException {
        if(size != null)
            return size;

        int pos = file.getName().lastIndexOf(".");
        if (pos == -1)
            throw new IOException("No extension for file: " + file.getAbsolutePath());
        String suffix = file.getName().substring(pos + 1);
        Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix(suffix);
        while(iter.hasNext()) {
            ImageReader reader = iter.next();
            try {
                ImageInputStream stream = new FileImageInputStream(file);
                reader.setInput(stream);
                int width = reader.getWidth(reader.getMinIndex());
                int height = reader.getHeight(reader.getMinIndex());
                size = new Dimension(width, height);
                return size;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                reader.dispose();
            }
        }

        throw new IOException("Not a known image file: " + file.getAbsolutePath());
    }
}
