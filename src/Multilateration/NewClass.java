/*
 Generating dummy log files.
 */
package Multilateration;

import java.util.ArrayList;

/**
 *
 * @author ed343
 */
public class NewClass {
    
    final double R = 6378137; // EARTH RADIUS (IN METERS)
       
    
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
        
        for(Double[] item : locations) {
            System.out.println(item[0]);
            System.out.println(item[1]);
            System.out.println(item[2]);
        }
        
        return locations;
    }
    
    public Double getDistance (Double[] base, Double[] tag) {
        Double d =0.0;
        Double tempx = Math.abs (base[0] - tag[0]);
        Double tempy = Math.abs (base[1] - tag[1]);
        Double tempz = Math.abs (base[2] - tag[2]);
   
        d = Math.sqrt(Math.pow(tempx, 2) + Math.pow(tempy, 2) + Math.pow(tempz, 2));
        
        System.out.println(d);
        return d; 
    }
    
    public Double getRSSI(Double d) {
        Double mp = -44.0;
        Double rssi = mp - 10 * 2 * Math.log10(d);
                
        return rssi;
    }
 
    
    public static void main(String args[]){
        //Double[] lst=new Double[]{-1.9272817558530993E9, 1.1893941753587626E8, -2.362406208433364E9};
        //Double[] lst=new Double[]{4028807.901663863, -248650.79535150624, 4938371.316965836};
        /**
        Double[] lst=new Double[]{-1.692361749263785E8, 1.0444170566407125E7, -2.074448074369939E8};
        NewClass nc= new NewClass();
        System.out.println(nc.cartesianToLatLon(lst)[0]);
        System.out.println(nc.cartesianToLatLon(lst)[1]);
        * */
        
        
        
        Double[] start = {2.0, 5.0, 0.12};        
        Double[][] locations = new Double[18][3];
        
        Double[] bs1 = {0.5, 10.0, 0.12};
        Double[] bs2 = {20.5, 10.0, 0.22};
        Double[] bs3 = {20.5, 1.0, 0.15};
        Double[] bs4 = {0.5, 1.0, 0.1};
        
        NewClass nc= new NewClass();        
        nc.getPheasantLocs(start, locations);        

        

        

        
        ArrayList<Double> log1= new ArrayList<>();
        
        for(Double[] loc: locations) {
            Double dist = nc.getDistance(bs1, loc);
            Double rssi = nc.getRSSI(dist);
            log1.add(rssi);
            System.out.println(log1);
            
        }
        

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
