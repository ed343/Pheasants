/*
 *Another try at making small Cartesian work for the MLAT
 */
package Multilateration;

import GUI.CoordinateTranslation;
import java.util.ArrayList;
import java.util.Random;

public class MapProcessing {
    
    int mapWidth = 480;
    int mapHeight = 280;
    int zoom = 16;
    double centerX;
    double centerY;
    
    Double[] corners;
    double realWidth;
    double realHeight;

    double xratio; // longitude
    double yratio;  // latitude
    
    // ArrayList to hold the coordinates of basestations that will be displayed
    ArrayList<Double[]> basestations = new ArrayList<>();
    ArrayList<Double[]> cartBasestations = new ArrayList<>();
    
    public MapProcessing(ArrayList<Double[]> bss) {
        basestations = bss;
        setup();
    }
    
    public void setup() {      
        
    Double[] frame = getRect(basestations);

    centerX = frame[3] + (frame[1] - frame[3]) / 2;
    centerY = frame[2] + (frame[0] - frame[2]) / 2;

    // {N, E, S, W}
   corners = getMapCorners(centerY, centerX, zoom);

    realWidth = corners[1] - corners[3];
    realHeight = corners[0] - corners[2];

    xratio = mapWidth / realWidth;     // longitude
    yratio = mapHeight / realHeight;   // latitude
    
    }
    
    public double[] getCenter() {
        double[] centerYX = {centerY, centerX};
        
        return centerYX;
    }
    
    public int getZoom() {
        return zoom;
    }
    
    public Double[] getCartesianLoc(Double[] loc) {
        double x = (getX(loc) - corners[3]) * xratio;
        // equation different from x coords, since geographical coordinates go from bottom to top
        // while image coordinates go top to bottom
        double y = (corners[0] - getY(loc)) * yratio;
        
        Random rand = new Random();                    

        double z = rand.nextFloat(); 

        Double[] coords = {x,y,z};
        
        // System.out.println("getCartesianLoc: " + coords[0] + ", " + coords[1] + ", " + coords[2]);

      return coords;
    }
    
    /**
     * Method will take in ArrayList of geographical basestation coordinates,
     * see whether we already have converted them to Cartesian coordinates,
     * and return new ArrayList of Cartesian coordinates that are generated
     * using the same method as in the visualisation.
     * @return 
     */
    public ArrayList<Double[]> getBasestations(ArrayList<Double[]> bss) {

        if (cartBasestations.isEmpty()) {

            for (Double[] bs : bss) {

                double x = (getX(bs) - corners[3]) * xratio;
                // equation different from x coords, since geographical coordinates go from bottom to top
                // while image coordinates go top to bottom
                double y = (corners[0] - getY(bs)) * yratio;
                
                Random rand = new Random();                    
                
                double z = rand.nextFloat(); // DON'T REALLY KNOW WHAT TO DO WITH Z COORDINATE

                Double[] c = {x,y,z};

                cartBasestations.add(c);
            }            
        }
        
        return cartBasestations;
    }
    
        /**
     * Method that calculates the corners of the downloaded Google Map, allowing
     * the right conversion between the geographical coordinates and
     * visualisation image. Uses CoordinateTranslation class.
     *
     * @param centerX
     * @param centerY
     * @return
     */
    public Double[] getMapCorners(double centerX, double centerY, int zoom) {
        CoordinateTranslation ct = new CoordinateTranslation();

        CoordinateTranslation.G_LatLng center = new CoordinateTranslation.G_LatLng(centerX, centerY);
        Double[] coords = ct.getCorners(center, zoom, mapWidth, mapHeight);

        return coords;
    }
    
    /**
     * getting coordinates of the rectangle that will cover all the basestations
     */
    public Double[] getRect(ArrayList<Double[]> basestations) {

        double maxY = -200;
        double maxX = -200;
        double minX = 200;
        double minY = 200;

        for (Double[] bs : basestations) {

            if (getX(bs) > maxX) {
                maxX = getX(bs);
            }
            if (getX(bs) < minX) {
                minX = getX(bs);
            }

            if (getY(bs) > maxY) {
                maxY = getY(bs);
            }

            if (getY(bs) < minY) {
                minY = getY(bs);
            }
        }

        Double[] frame = {maxY, maxX, minY, minX};

        return frame;
    }
    
        /**
     * get x (longitude) coordinate of the beacon
     */
    public double getY(Double[] coord) {
        return coord[0];
    }

    /**
     * get y (latitude) coordinate of the beacon
     */
    public double getX(Double[] coord) {
        return coord[1];
    }
}
