/*
 Generating dummy log files.
 */
package Multilateration;

import Jama.Matrix;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import javafx.util.Pair;

/**
 *
 * @author ed343
 */
public class NewClass {
    
    final double R = 6378137; // EARTH RADIUS (IN METERS)
    static PrimerClass primer;
    static ArrayList<Double[]> bss;
    static ArrayList<Long> tags;
       
    
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
    
    public ArrayList<Long> generateTimes() {
        ArrayList al = new ArrayList();
        Double start = 10.0;
        for (int i=0;i<18; i++) {
            al.add(start+4.0);
            start+=4.0;
        }  
        
        return al;
    }
    
    public void runningIt() {
        
    }
 
    
    public static void main(String args[]){         
        
        Double[] start = {2.0, 5.0, 0.12};        
        Double[][] locations = new Double[18][3];
        bss = new ArrayList<>();
        
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
        
        //get times
        ArrayList<Long> timez = nc.generateTimes();
        
        // get tags
        tags=new ArrayList<>();
        tags.add(44001004238L);
        
        //running it

        primer = new PrimerClass();
        primer.setNumberOfRadios(4);
        for (Double[] bs : bss) {
            primer.setRadioCoordinates(bs);
        }
        for (int i=0; i<=3; i++){
            primer.setRadioMeasuredPower(-44);
        }
        
        primer.setTRVals(timez, tags, log1);
        
        RssiEquation req = new RssiEquation();
        primer.idDistances = req.getTagDistance(primer.idRSSIs, primer.measuredPower);
        
        // tag_registry holds all tags across all radios and their coordinates
        // at each time they were detected
        HashMap<Long, HashMap<Long, Double[]>> tag_registry = new HashMap<>();
        // used for initialising each element of tag_registry, i.e. a hashmap,
        // with the times each one was picked up as keys
        HashMap<Long, Double[]> init_inner_tag_reg;
        // the times each tag was picked up
        HashMap<Long, ArrayList<Long>> tag_detect_times = new HashMap<>();
        
        // for each radio
        for (int i = 0; i < primer.no_of_radios; i++) {
            //get the tagIDs this radio picked up
            Object[] idKeys = primer.idRSSIs.get(i).keySet().toArray();
            Long[] keys_long = new Long[primer.idRSSIs.get(i).size()];
            
            // go through each tag
            for (int j = 0; j < keys_long.length; j++) {
                // initialise the hahsmap that will hold all detection times as
                // keys
                init_inner_tag_reg = new HashMap<>();
                String stringToConvert = String.valueOf(idKeys[j]);
                Long convertedLong = Long.parseLong(stringToConvert);
                keys_long[j] = convertedLong;
                
                // if the tag hasn't been added to the registry before
                if (!tag_registry.containsKey(convertedLong)) {
                    
                    // iterate over the times when the current radio detected
                    // the current tag
                    for (int k = 0; k < primer.idRSSIs.get(i).
                            get(convertedLong).size(); k++) {
                        
                        // get these times and their corresponding rssis
                        ArrayList<Pair<Long, Double>> times
                                = primer.idRSSIs.get(i).get(convertedLong);
                        
                        // list will hold only the times
                        ArrayList<Long> tms = new ArrayList<>();
                        
                        // for all detection pairs
                        for (Pair p : times) {
                            
                            // get the time
                            String aux_str = String.valueOf(p.getKey());
                            Long detection_time = Long.parseLong(aux_str);
                            
                            // if multiple detections at the same time,
                            // keep just one
                            if (!tms.contains(detection_time)) {
                                tms.add(detection_time);
                            }
                            
                            // use empty array of coordinates for initiliasation
                            Double[] init_coords = new Double[3];
                            init_inner_tag_reg.put(detection_time, init_coords);
                        }
                        // create a new entry for this tag and all its detection
                        // times
                        tag_detect_times.put(convertedLong, tms);
                    }
                    tag_registry.put(convertedLong, init_inner_tag_reg);
                } 
                // if the tag is already stored in tag_registry
                // see if we get new times from this radio
                else {
                    // get all the times we already have for this radio
                    Object[] times = tag_registry.get(convertedLong).
                            keySet().toArray();
                    ArrayList<Long> time_long = new ArrayList<>();
                    
                    // iterate over all the times we have already recorded it 
                    // has been picked up
                    for (int k = 0; k < times.length; k++) {
                        String stringToConv = String.valueOf(times[k]);
                        Long convertedtime = Long.parseLong(stringToConv);
                        time_long.add(convertedtime);
                    }
                    
                    // get the detections from the current radio
                    ArrayList<Pair<Long, Double>> tim
                            = primer.idRSSIs.get(i).get(convertedLong);
                    
                    // list will hold only the times
                    ArrayList<Long> tms = new ArrayList<>();
                    
                    // for all detections
                    for (Pair p : tim) {
                        // get&convert the time
                        String aux_str = String.valueOf(p.getKey());
                        Long aux_long = Long.parseLong(aux_str);
                        
                        // if multiple detections at the same time,
                        // keep just one
                        if (!tms.contains(aux_long)) {
                            tms.add(aux_long);
                        }
                    }
                    // check if this radio detected the tag at times it was not
                    // detected by other radios
                    for (Long l : tms) {
                        // didn't have this time previously
                        // need to put it in tag_registry
                        if (!time_long.contains(l)) {
                            // we are still at the tag: convertedLong
                            // we add a new pair of detection for the current
                            // time and an empty arraylist for its to-be
                            // computed coordinates
                            Double[] a = new Double[3];
                            tag_registry.get(convertedLong).put(l, a);
                            time_long.add(l);
                        }
                        
                    }
                    // replace the entry we had for this tag with the updated 
                    // arraylist of times it was picked up
                tag_detect_times.put(convertedLong, time_long);    
                }
            }
        }
        
        // PART 2 - ACTUAL MULTILATERATION
        // will call applyMLAT()
        System.out.println("No of tags is " + tag_registry.size());
        // at this point I have all my tags stored in tag_registry
        // there is one hashmap per tag, consisiting of a time key and
        // their corresponding values are initialised to null, i.e.
        // we don't currently have coordinates for the tags
        
        // create hashmap to hold the hashmap we'll get from
        // getTagCoordinatesbyLeadRadio
        HashMap<Long, HashMap<Long, Double[]>> hm_returned;
        // call function
        hm_returned = applyMLAT(tag_detect_times);
        // for all tags returned
        for (Long key : hm_returned.keySet()) {
            // get the current tag's inner hashmap that has times as keys
            // and coordinates as values
            HashMap<Long, Double[]> inner = hm_returned.get(key);
            // for all times as keys in inner
            for (Long time : inner.keySet()) {
                // get the inner hashmap we already have for this tag
                init_inner_tag_reg = tag_registry.get(key);
                // get the array of coordinates from this hashmap
                Double[] aux_arr;
                // add the 3 coordinates we got from the method
                Double[] aux_coord = new Double[]{inner.get(time)[0],
                    inner.get(time)[1]};
                aux_arr=aux_coord;
                // put it back
                init_inner_tag_reg.put(time, aux_arr);
                tag_registry.put(key, init_inner_tag_reg);
            }
        }
    }
    /* This function can loops through all tags which we have in
    /   tag_detect_times.
    /  Gets all the times from tag_detect_times and loops through all radios,
    /   looking for detections around that time.
    /  If detection exists, ok, put forward for mlat
    /   If not, ok, make sure mlat doesn't consider the current radio by adding 
    /  a distance of -999,999
    */
    
    public static HashMap<Long, HashMap<Long, Double[]>> applyMLAT(
            HashMap<Long, ArrayList<Long>> tag_detect_times){
        // initialise the hashmap we're going to return
        HashMap<Long, HashMap<Long, Double[]>> hm = new HashMap<>();
        for (Long key : tag_detect_times.keySet()) {
            ArrayList<Long> times = tag_detect_times.get(key);
            for (Long time: times){
                ArrayList<Double> distances= new ArrayList<>();
                for(int i = 0; i < primer.no_of_radios; i++) {
                    // get the distance between the ith radio and the key tag at
                    // the time time
                    for(Long time_margin=time-3; time_margin<time+4;
                            time_margin++){
                        try {
                            distances.add(primer.idDistances.get(i).get(key).
                                    get(time));
                            break;
                        }
                        catch(NullPointerException e){
                            if (time_margin==time+3)
                            {
                                //eliminate this radio
                                distances.add(-999.999);
                            }
                        }
                    }
                }
                MLATEquation eq = new MLATEquation(distances.size(),
                        primer.getRadiosCoordinates(),
                        distances);
                
                System.out.println("Tag: " + key + " at time " + time +
                        ":");
                // check that we have enough valid distances
                boolean x = eq.fix();
                if (x) {
                    // we have enough
                    Matrix A = eq.getA();
                    Matrix B = eq.getB();
                    Matrix sol = A.solve(B); //these are our coordinates
                    // the matrix is 4x1, and entries 1,2,3
                    // give us the x, y, z coord
                    GUI.CoordinateTranslation ct = new GUI.CoordinateTranslation();
                    Double[] coords = new Double[]{sol.get(1, 0), sol.get(2, 0),
                        sol.get(3, 0)};
                    System.out.println("small coord system coords");
                    System.out.println(coords[0]);
                    System.out.println(coords[1]);
                    System.out.println(coords[2]);
                    Double[] geoCoords = ct.cartesianToLatLon(coords);
                    HashMap time_coords_map = new HashMap();
                    time_coords_map.put(time, geoCoords);
                    hm.put(key, time_coords_map);
                    System.out.println(" ");
                    System.out.println("The geographical location of this tag is: "+ Arrays.deepToString(geoCoords));
                    System.out.println(" ");
                    //sol.print(10, 5);
                } else {
                    // we don't have enough valid distances, i.e.
                    // no other radios picked up this tag at this time
                    continue;
                }
            }
        }
        return hm;
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
