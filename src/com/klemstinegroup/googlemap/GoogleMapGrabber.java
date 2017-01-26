package com.klemstinegroup.googlemap;

import java.io.File;

public class GoogleMapGrabber {

    //7000 seems max
    //86 with 12G heap
    static int tilesSqRoot = 20;
    public static String key="AIzaSyAsj-GadPbVpK0-G-HxHxbcPKuSLUme5xE";
    public static String style="hybrid";
    public double lat = 13.176055;//,-65.0199923;//31.778 ;
    public double lon =  -59.568534;//35.2354;
    public int zoom = 16;
    public String name = "Barbados";//"Jerusalem";
    static String directory = "c:/temp/barbados";

    static{
        if (!directory.endsWith(File.separator))directory+=File.separator;
    }

    public static int SIZE = 512;
    static final double GOOGLEOFFSET = 268435456;
    static final double GOOGLEOFFSET_RADIUS = GOOGLEOFFSET / Math.PI;
    static final double MATHPI_180 = Math.PI / 180;

    static private final double preLonToX1 = GOOGLEOFFSET_RADIUS * MATHPI_180;

    public final static double LonToX(double lon) {
        return Math.round(GOOGLEOFFSET + preLonToX1 * lon);
    }

    public final static double LatToY(double lat) {
        return Math.round(GOOGLEOFFSET
                - GOOGLEOFFSET_RADIUS
                * Math.log((1 + Math.sin(lat * MATHPI_180))
                / (1 - Math.sin(lat * MATHPI_180))) / 2);
    }

    public final static double XToLon(double x) {
        return ((Math.round(x) - GOOGLEOFFSET) / GOOGLEOFFSET_RADIUS) * 180
                / Math.PI;
    }

    public final static double YToLat(double y) {
        return (Math.PI / 2 - 2 * Math
                .atan(Math.exp((Math.round(y) - GOOGLEOFFSET)
                        / GOOGLEOFFSET_RADIUS)))
                * 180 / Math.PI;
    }

    public final static double adjustLonByPixels(double lon, int delta, int zoom) {
        return XToLon(LonToX(lon) + (delta << (21 - zoom)));
    }

    public final static double adjustLatByPixels(double lat, int delta, int zoom) {
        return YToLat(LatToY(lat) + (delta << (21 - zoom)));
    }

    public String getRoadMapUrl(int x, int y) {
        int x1=x*SIZE;
        int y1=y*SIZE;
        double templon = adjustLonByPixels(lon, x1, zoom);
        double templat = adjustLatByPixels(lat, -y1, zoom);
        return "http://maps.googleapis.com/maps/api/staticmap?" + "center="
                + templat
                + ","
                + templon
                + "&"
                + "zoom="
                + zoom
                + "&"
                + "format=png32&"
                + "sensor=false&"
                + "size="
                + SIZE
                + "x"
                + (SIZE + 25)
                + "&"
                + "maptype=roadmap&"
                + "style=feature:road|element:geometry|color:0xffffff&"
                + "style=feature:transit|element:geometry|color:0xeeeeee&"
                + "style=feature:poi|element:geometry|color:0xaaaaaa&"
                + "style=feature:landscape.man_made|element:geometry|color:0x888888&"
                + "style=feature:landscape.natural|element:geometry|color:0x444444&"
                + "style=feature:water|element:geometry|color:0x111111&"

                + "style=feature:administrative|element:geometry|lightness:-100&"
                + "style=feature:all|element:labels|visibility:off&"
                + "key="+key;
    }

    public String getSatelliteUrl(int x, int y) {
        int x1 = x * SIZE;
        int y1 = y * SIZE;
        double templon = adjustLonByPixels(lon, x1, zoom);
        double templat = adjustLatByPixels(lat, -y1, zoom);
        return "http://maps.googleapis.com/maps/api/staticmap?" + "center="
                + templat + "," + templon + "&" + "zoom=" + zoom + "&"
                + "format=png32&" + "sensor=false&" + "size=" + SIZE + "x"
                + (SIZE + 25) + "&" + "maptype=satellite&key="+key;
    }


    public String getFileName(int x, int y) {
        int x1 = x * SIZE;
        int y1 = y * SIZE;
        double templon = adjustLonByPixels(lon, x1, zoom);
        double templat = adjustLatByPixels(lat, -y1, zoom);
        return "tile_" + name + "_" + zoom + "_" + templat + "_" + templon + "_" + x + "_" + y + ".png";
    }

    public String getFileNameRd(int x, int y) {
        int x1 = x * SIZE;
        int y1 = y * SIZE;
        double templon = adjustLonByPixels(lon, x1, zoom);
        double templat = adjustLatByPixels(lat, -y1, zoom);
        return "tile_" + name + "RD_" + zoom + "_" + templat + "_" + templon + "_" + x + "_" + y + ".png";
    }
}
