/*
 Generating dummy log files.
 */
package Multilateration;

import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author ed343
 */
public class NewClass {
    
    final double R = 6378137; // EARTH RADIUS (IN METERS)
    static PrimerClass primer;
    static ArrayList bss;
       
    
    public Double[][] getPheasantLocs(Double[] start, Double[][] locations) {
        
        
        Double x = start[0];
        Double y = start[1];
        Double z = start[2]; 
        for (int i=0; i<18; i++) {
            locations[i][0]=x+1.0;
            locations[i][1]=y;
            locations[i][2]=z;
            x++;
        }
        
        return locations;
    }
    
    public Double getDistance (Double[] base, Double[] tag) {
        Double d =0.0;
        Double tempx = Math.abs (base[0] - tag[0]);
        Double tempy = Math.abs (base[1] - tag[1]);
        Double tempz = Math.abs (base[2] - tag[2]);
   
        d = Math.sqrt(Math.pow(tempx, 2) + Math.pow(tempy, 2) + Math.pow(tempz, 2));
       
        return d; 
    }
    
    public Double getRSSI(Double d) {
        Double mp = -44.0;
        Double rssi = mp - 10 * 2 * Math.log10(d);
                
        return rssi;
    }
    
    public int generateID() {
        Random rand = new Random(); 
        int value = rand.nextInt(50);
        
        return value;
    }
    
    public ArrayList<Double> generateTimes() {
        ArrayList al = new ArrayList();
        Double start = 10.0;
        for (int i=0;i<18; i++) {
            al.add(start+4.0);
            start+=4.0;
        }  
        
        return al;
    }
    
    public void runningIt() {
        primer = new PrimerClass();
        primer.setNumberOfRadios(4);
        for (Double[] bs : bss) {
            primer.setRadioCoordinates(bs);
        }
        
    }
 
    
    public static void main(String args[]){         
        
        Double[] start = {2.0, 5.0, 0.12};        
        Double[][] locations = new Double[18][3];
        bss = new ArrayList<Double[]>();
        
        Double[] bs1 = {0.5, 10.0, 0.12};
        Double[] bs2 = {20.5, 10.0, 0.22};
        Double[] bs3 = {20.5, 1.0, 0.15};
        Double[] bs4 = {0.5, 1.0, 0.1};
        
        bss.add((Double[])bs1);
        bss.add((Double[])bs2);
        bss.add((Double[])bs3);
        bss.add((Double[])bs4);
        
        NewClass nc= new NewClass(); 
        // populating location list with pheasant coordinates if it moves in a line
        nc.getPheasantLocs(start, locations);  
        
        int id = nc.generateID();
        System.out.println("Pheasant ID: " + id);
        
        ArrayList<Double> log1= new ArrayList<>();
        ArrayList<Double> log2= new ArrayList<>();
        ArrayList<Double> log3= new ArrayList<>();
        ArrayList<Double> log4= new ArrayList<>();
        
        
        for (Double[] loc: locations) {
            Double dist1 = nc.getDistance(bs1, loc);
            Double rssi1 = nc.getRSSI(dist1);
            log1.add(rssi1);
            
            Double dist2 = nc.getDistance(bs2, loc);
            Double rssi2 = nc.getRSSI(dist2);
            log1.add(rssi2);
            
            Double dist3 = nc.getDistance(bs3, loc);
            Double rssi3 = nc.getRSSI(dist3);
            log1.add(rssi3);
            
            Double dist4 = nc.getDistance(bs4, loc);
            Double rssi4 = nc.getRSSI(dist4);
            log1.add(rssi4);          
        }
        
        
        ArrayList times = nc.generateTimes();
        System.out.println(times);
        

    }
    

    
    
    
    public Double[] cartesianToLatLon(Double[] cartCoords) {
        
        double x = cartCoords[0];
        double y = cartCoords[1];
        double z = cartCoords[2];    

        double lat = Math.asin(z / R);
        
        double lon = Math.atan2(y, x);
        
        lat = radiansToDegrees(lat);
        lon = radiansToDegrees(lon);
        
        Double[] coords = {lat, lon};
           
        return coords;
    }
    
    public double radiansToDegrees (double rad) {
        return rad / (Math.PI /180);
    }
}
