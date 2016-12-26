package com.klemstinegroup.googlemap;

//import com.klemstinegroup.googlemap.PairLite;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
        long heapMaxSize = Runtime.getRuntime().maxMemory()/1000000;
        System.out.println("java -Xmx"+heapMaxSize+"m -jar Wanderer.jar "+gm.name+" "+gm.lat+" "+gm.lon+" "+gm.zoom+" "+ GoogleMapGrabber.tilesSqRoot+" "+ GoogleMapGrabber.directory);
        int x = 0, y = 0, dx = 0, dy = -1;
        int t = 0;
        while (tilesToDownLoad-- > 0) {
            String sat = gm.getRoadMapUrl(x, y);
            String filename = gm.getFileName(x, y);
            datalist.add(new Data(sat, filename, x, y));
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
            minx = Math.min(minx, d.tileX);
            miny = Math.min(miny, d.tileY);
            maxx = Math.max(maxx, d.tileX);
            maxy = Math.max(maxy, d.tileY);
            if (!new File(GoogleMapGrabber.directory + d.filename).exists()) {
                try {
                    BufferedImage image = ImageIO.read(new URL(d.sat));
                    BufferedImage image2 = new BufferedImage(GoogleMapGrabber.SIZE, GoogleMapGrabber.SIZE, BufferedImage.TYPE_INT_RGB);
                    Graphics bg = image2.getGraphics();
                    bg.drawImage(image, 0, 0, null);
                    ImageIO.write(image2, "png", new File(GoogleMapGrabber.directory + d.filename));
                    bigPNGGraphics.drawImage(image2, (w / 2 - gm.SIZE / 2) + d.tileX * gm.SIZE, (h / 2 - gm.SIZE / 2) - d.tileY * gm.SIZE, null);
                } catch (IOException e) {
                    System.err.println("error - retrying!");
                    continue;
                }
            } else {
                try {
                    BufferedImage image2 = ImageIO.read(new File(GoogleMapGrabber.directory + d.filename));
                    bigPNGGraphics.drawImage(image2, (w / 2 - gm.SIZE / 2) + d.tileX * gm.SIZE, (h / 2 - gm.SIZE / 2) - d.tileY * gm.SIZE, null);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
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
        //System.exit(0);
    }

    public static void main(String[] args) {
        if (args.length>0){
            gm.name=args[0];
            gm.lat=Double.parseDouble(args[1]);
            gm.lon=Double.parseDouble(args[2]);
            gm.zoom=Integer.parseInt(args[3]);
            GoogleMapGrabber.tilesSqRoot = Integer.parseInt(args[4]);
            GoogleMapGrabber.directory=args[5];
        }
        new Wanderer();
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
        String filename;
        int tileX;
        int tileY;

        public Data(String sat, String filename, int tileX, int tileY) {
            this.sat = sat;
            this.filename = filename;
            this.tileX = tileX;
            this.tileY = tileY;
        }
    }
}
