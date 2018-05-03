/*
 *Another try at making small Cartesian work for the MLAT
 */
package Multilateration;

import GUI.CoordinateTranslation;
import java.util.ArrayList;

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
    
    Double[] basestation1 = {50.738486, -3.531713};//Nat
    Double[] basestation2 = {50.738675, -3.531101};//James
    Double[] basestation3 = {50.738822, -3.531642};//Cat
    Double[] basestation4 = {50.738222, -3.5310};//Duplicate placed next to Cat.
    // initially 50.738829, -3.531627; 
    // now the fourth radio is created by adding to Cat
    // the difference between the corodinates of James and Nat


    
    public static void main(String[] args) {
        MapProcessing mp = new MapProcessing();
        mp.setup();
    }
    
    public MapProcessing() {
        basestations.add(basestation1);
        basestations.add(basestation2);
        basestations.add(basestation3);
        basestations.add(basestation4);
        setup();
    }
    
    public void setup() {      
        
    Double[] frame = getRect(basestations);

    centerX = frame[3] + (frame[1] - frame[3]) / 2;
    centerY = frame[2] + (frame[0] - frame[2]) / 2;
    System.out.println("centerY (lat):" + centerY);
    System.out.println("centerX (lon):" + centerX);

   corners = getMapCorners(centerY, centerX, zoom);

    realWidth = corners[1] - corners[3];
    realHeight = corners[0] - corners[2];

    xratio = mapWidth / realWidth;     // longitude
    yratio = mapHeight / realHeight;   // latitude
    
    System.out.println("N: "+ corners[0]);
    System.out.println("E: " + corners[1]);
    System.out.println("S: " + corners[2]);
    System.out.println("W: " + corners[3]);
    
    }
    
    public Double[] getCartesianLoc(Double[] loc) {
        double x = (getX(loc) - corners[3]) * xratio;
        // equation different from x coords, since geographical coordinates go from bottom to top
        // while image coordinates go top to bottom
        double y = (corners[0] - getY(loc)) * yratio;

        // value 0.00001395 was taken from this article: goo.gl/tq787k

        double z = (getX(loc) * 0.00001395 * mapWidth);

        Double[] coords = {x,y,z};

      return coords;
    }
    
    /**
     * Converting arraylist of double values to cartesian plane coordinate.
     * Could be used on basestations, as well as tags.
     */
    public void convert(ArrayList<Double[]> things) {
        for (Double[] thing: things) {
            
            double x = (getX(thing) - corners[3]) * xratio;
            // equation different from x coords, since geographical coordinates go from bottom to top
            // while image coordinates go top to bottom
            double y = (corners[0] - getY(thing)) * yratio;
            
            // value 0.00001395 was taken from this article: goo.gl/tq787k

            double z = (getX(thing) * 0.00001395 * mapWidth);
            
            Double[] coords = {x,y,z};
            
            cartBasestations.add(coords);
        }
    }
    
    public ArrayList<Double[]> getBasestations() {
        if (cartBasestations.isEmpty()) {
            convert(basestations);
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
