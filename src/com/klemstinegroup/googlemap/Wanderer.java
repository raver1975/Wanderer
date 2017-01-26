package com.klemstinegroup.googlemap;

//import com.klemstinegroup.googlemap.PairLite;

import com.sun.org.apache.xpath.internal.SourceTree;

import javax.imageio.ImageIO;
import javax.sound.midi.Soundbank;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Paul on 12/24/2016.
 */
public class Wanderer {

    private static GoogleMapGrabber gm = new GoogleMapGrabber();

    int tilesToDownLoad = GoogleMapGrabber.tilesSqRoot * GoogleMapGrabber.tilesSqRoot;

    ArrayList<Data> datalist = new ArrayList<Data>();

    public Wanderer() {
        long heapMaxSize = Runtime.getRuntime().maxMemory() / 1000000;
        System.out.println("java -Xmx" + heapMaxSize + "m -jar Wanderer.jar " + gm.name + " " + gm.lat + " " + gm.lon + " " + gm.zoom + " " + GoogleMapGrabber.tilesSqRoot + " " + GoogleMapGrabber.directory + " " +GoogleMapGrabber.style+" "+ gm.key);
        int x = 0, y = 0, dx = 0, dy = -1;
        int t = 0;
        while (tilesToDownLoad-- > 0) {

            datalist.add(new Data(x, y));
            if ((x == y) || ((x < 0) && (x == -y)) || ((x > 0) && (x == 1 - y))) {
                t = dx;
                dx = -dy;
                dy = t;
            }
            x += dx;
            y += dy;
        }

        int minx = Integer.MAX_VALUE, miny = Integer.MAX_VALUE, maxx = Integer.MIN_VALUE, maxy = Integer.MIN_VALUE;
        for (Data d : datalist) {
            minx = Math.min(minx, d.tileX);
            miny = Math.min(miny, d.tileY);
            maxx = Math.max(maxx, d.tileX);
            maxy = Math.max(maxy, d.tileY);
        }
        int w = maxx - minx;
        int h = maxy - miny;
        w *= GoogleMapGrabber.SIZE;
        h *= GoogleMapGrabber.SIZE;
        BufferedImage bigPNG = new BufferedImage((int) w, (int) h, BufferedImage.TYPE_INT_RGB);
        Graphics bigPNGGraphics = bigPNG.getGraphics();

        JFrame frameO = new JFrame("Orig");
        frameO.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frameO.setLayout(new BorderLayout());
        ImagePaneOrig imagePaneO = new ImagePaneOrig(bigPNG);
        frameO.add(imagePaneO);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frameO.setBounds(0, 0, screenSize.width, screenSize.height);
        frameO.setVisible(true);

        JFrame frame = new JFrame("Scale");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        ImagePane imagePane = new ImagePane(bigPNG);
        frame.add(imagePane);
        frame.setBounds(0, 0, screenSize.width, screenSize.height);
        frame.setVisible(true);

        minx = Integer.MAX_VALUE;
        miny = Integer.MAX_VALUE;
        maxx = Integer.MIN_VALUE;
        maxy = Integer.MIN_VALUE;

        for (Data d : datalist) {
            boolean flag;
            do {
                flag = false;
                minx = Math.min(minx, d.tileX);
                miny = Math.min(miny, d.tileY);
                maxx = Math.max(maxx, d.tileX);
                maxy = Math.max(maxy, d.tileY);
                System.out.println("------------------------------------");
                System.out.println("(" + d.tileX + "," + d.tileY + ")");
                BufferedImage satImage = null;
                BufferedImage dataImage = null;
                if ("sat".equals(GoogleMapGrabber.style)||"hybrid".equals(GoogleMapGrabber.style)) {
                    if (!(new File(GoogleMapGrabber.directory + d.filename).exists())) {
                        try {
                            System.out.println("loading sat");
                            satImage = ImageIO.read(new URL(d.sat));
                            BufferedImage satImageCrop = new BufferedImage(GoogleMapGrabber.SIZE, GoogleMapGrabber.SIZE, BufferedImage.TYPE_INT_RGB);
                            Graphics bg = satImageCrop.getGraphics();
                            bg.drawImage(satImage, 0, 0, null);
                            ImageIO.write(satImageCrop, "png", new File(GoogleMapGrabber.directory + d.filename));

                        } catch (IOException e) {
                            System.err.println("error - retrying!" + "\t" + e.getMessage());
                            flag = true;
                            continue;
                        }
                    } else {
                        try {
                            satImage = ImageIO.read(new File(GoogleMapGrabber.directory + d.filename));
                            //bigPNGGraphics.drawImage(image2, (w / 2 - gm.SIZE / 2) + d.tileX * gm.SIZE, (h / 2 - gm.SIZE / 2) - d.tileY * gm.SIZE, null);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }

                if ("data".equals(GoogleMapGrabber.style)||"hybrid".equals(GoogleMapGrabber.style)) {
                    if (!(new File(GoogleMapGrabber.directory + d.filenameRd).exists())) {
                        try {
                            System.out.println("loading data");
                            dataImage = ImageIO.read(new URL(d.road));
                            BufferedImage roadImageCrop = new BufferedImage(GoogleMapGrabber.SIZE, GoogleMapGrabber.SIZE, BufferedImage.TYPE_INT_RGB);
                            Graphics bg1 = roadImageCrop.getGraphics();
                            bg1.drawImage(dataImage, 0, 0, null);
                            ImageIO.write(roadImageCrop, "png", new File(GoogleMapGrabber.directory + d.filenameRd));

                        } catch (IOException e) {
                            System.err.println("error - retrying!" + "\t" + e.getMessage());
                            flag = true;
                            continue;
                        }
                    } else {
                        try {
                            dataImage = ImageIO.read(new File(GoogleMapGrabber.directory + d.filenameRd));
                            //bigPNGGraphics.drawImage(image2, (w / 2 - gm.SIZE / 2) + d.tileX * gm.SIZE, (h / 2 - gm.SIZE / 2) - d.tileY * gm.SIZE, null);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }

                if ("hybrid".equals(GoogleMapGrabber.style)&&dataImage != null && satImage != null) {
                    //combine
                    BufferedImage alphaImage = new BufferedImage(GoogleMapGrabber.SIZE, GoogleMapGrabber.SIZE, BufferedImage.TYPE_INT_ARGB);
                    int[] alphaPixels = alphaImage.getRGB(0, 0, GoogleMapGrabber.SIZE, GoogleMapGrabber.SIZE, null, 0, GoogleMapGrabber.SIZE);
                    int[] roadPixels = dataImage.getRGB(0, 0, GoogleMapGrabber.SIZE, GoogleMapGrabber.SIZE, null, 0, GoogleMapGrabber.SIZE);
                    int[] satPixels = satImage.getRGB(0, 0, GoogleMapGrabber.SIZE, GoogleMapGrabber.SIZE, null, 0, GoogleMapGrabber.SIZE);
                    for (int i = 0; i < satPixels.length; i++) {
                        int color = satPixels[i] & 0x00ffffff; // Mask preexisting alpha
                        int alpha = roadPixels[i] << 24; // Shift blue to alpha
                        //only show sat
                        alpha=0xff000000;
                        alphaPixels[i] = color | alpha;
                        int data=roadPixels[i]& 0x00ffffff;;
                        String s=new BigInteger(Integer.toString(data)).toString(16);
                        if (s.equals("111111")){
                            alphaPixels[i]=0xffffffff;
                        }
                    }
                    alphaImage.setRGB(0, 0, GoogleMapGrabber.SIZE, GoogleMapGrabber.SIZE, alphaPixels, 0, GoogleMapGrabber.SIZE);
                    bigPNGGraphics.drawImage(alphaImage, (w / 2 - gm.SIZE / 2) + d.tileX * gm.SIZE, (h / 2 - gm.SIZE / 2) - d.tileY * gm.SIZE, null);
                }

                if ("data".equals(GoogleMapGrabber.style)&&dataImage != null) {
                    bigPNGGraphics.drawImage(dataImage, (w / 2 - gm.SIZE / 2) + d.tileX * gm.SIZE, (h / 2 - gm.SIZE / 2) - d.tileY * gm.SIZE, null);
                }

                if ("sat".equals(GoogleMapGrabber.style) && satImage != null) {
                    bigPNGGraphics.drawImage(satImage, (w / 2 - gm.SIZE / 2) + d.tileX * gm.SIZE, (h / 2 - gm.SIZE / 2) - d.tileY * gm.SIZE, null);
                }


            }
            while (flag);
            imagePane.w = ((maxx - minx) + 1) * GoogleMapGrabber.SIZE;
            imagePane.h = ((maxy - miny) + 1) * GoogleMapGrabber.SIZE;

            imagePane.x = (bigPNG.getWidth() - imagePane.w) / 2;
            imagePane.y = (bigPNG.getHeight() - imagePane.h - GoogleMapGrabber.SIZE) / 2;

            if ((maxx - minx) % 2 == 1) imagePane.x += GoogleMapGrabber.SIZE / 2;
            if ((maxy - miny) % 2 == 0) imagePane.y += GoogleMapGrabber.SIZE / 2;

            imagePane.repaint();
            imagePaneO.repaint();
        }
        try {
            ImageIO.write(bigPNG, "png", new File(GoogleMapGrabber.directory + gm.name + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("done");

    }

    public static void main(String[] args) {
        if (args.length > 0) {
            if (args.length > 0) gm.name = args[0];
            if (args.length > 1) gm.lat = Double.parseDouble(args[1]);
            if (args.length > 2) gm.lon = Double.parseDouble(args[2]);
            if (args.length > 3) gm.zoom = Integer.parseInt(args[3]);
            if (args.length > 4) GoogleMapGrabber.tilesSqRoot = Integer.parseInt(args[4]);
            if (args.length > 5) GoogleMapGrabber.directory = args[5];
            if (args.length > 6) GoogleMapGrabber.style = args[6];
            if (args.length > 7) GoogleMapGrabber.key = args[7];

        }
        new Wanderer();
//        System.exit(0);
    }


    class ImagePane extends JLabel {

        private final BufferedImage bi;
        int x, y, w, h;

        public ImagePane(BufferedImage bi) {
            this.bi = bi;
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            //  g.drawImage(bi, 0, 0, getWidth(), getHeight(), this);
            g.drawImage(bi, 0, 0, getWidth(), getHeight(), x, y, x + w, y + h, this);
        }

    }

    class ImagePaneOrig extends JLabel {

        private final BufferedImage bi;
        int x, y, w, h;

        public ImagePaneOrig(BufferedImage bi) {
            this.bi = bi;
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(bi, 0, 0, getWidth(), getHeight(), this);
//            g.drawImage(bi, 0, 0, getWidth(), getHeight(), x, y, w, h, this);
        }

    }


    class Data {
        String sat;
        String road;
        String filename;
        int tileX;
        int tileY;
        public String filenameRd;


        public Data(int x, int y) {
            tileX = x;
            tileY = y;
            sat = gm.getSatelliteUrl(x, y);
            road = gm.getRoadMapUrl(x, y);
            filename = gm.getFileName(x, y);
            filenameRd = gm.getFileNameRd(x, y);
        }
    }
}
