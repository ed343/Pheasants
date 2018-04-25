/*
Generating dummy log files.
*/
package Multilateration;

import Jama.Matrix;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.util.Pair;
import org.apache.commons.math3.distribution.PoissonDistribution;

public class NewClass {
    
    final double R = 6378137; // EARTH RADIUS (IN METERS)
    static PrimerClass primer;
    static ArrayList<Double[]> bss;
    static ArrayList<Long> tags;
    static HashMap<Long, HashMap<BigInteger, Double[]>> tag_registry;
    
    // these are the final ordered data
    // use them as shown in the end of method order_times_coords()
    static ArrayList<Long> all_tags= new ArrayList<>();
    static ArrayList<ArrayList<BigInteger>> all_times= new ArrayList<>();
    static ArrayList<ArrayList<Double[]>> all_coords= new ArrayList<>();
    
    
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
    
    // not using this
    public int generateID() {
        Random rand = new Random();
        int value = rand.nextInt(50);
        
        return value;
    }
    
    public ArrayList<BigInteger> generateTimes() {
        ArrayList<BigInteger> al = new ArrayList<>();
        BigInteger start = new BigInteger("10");
        BigInteger four = new BigInteger("4");
        for (int i=0;i<18; i++) {
            al.add(start.add(four));
            start = start.add(four);
        }
        return al;
    }
    
    static ArrayList<BigInteger> gTPoisson(int iters, BigInteger startTime) {
        // Create times array
        ArrayList<BigInteger> times = new ArrayList<>();
        // Add the start time to the array
        times.add(startTime);
        BigInteger currentTime = startTime;
        // Create a list of integers, ranging from start(INPUT 4) to end(INPUT 5) inclusive.
        List<Integer> range = IntStream.rangeClosed(0, 2).boxed().collect(Collectors.toList());
        // Array to hold probabilities for all integers in range.
        ArrayList<Double> rProbs = new ArrayList<>();
        // Create a poisson distribution with mean of mean(INPUT 3)
        PoissonDistribution pdist = new PoissonDistribution(1);
        double cp =0;
        for (Integer range1 : range) {
            // Calculate probabilities for all integers in range and add to array.
            double prob = pdist.cumulativeProbability(range1);
            rProbs.add(prob);
            // Calculate the cumulative probabilty for members of range.
            cp += prob;
        }
        
        for(int i=0;i<iters;i++) {
            // Generate a random double between 0 and cp.
            double p = Math.random() * cp;
            double cumulativeProbability = 0.0;
            BigInteger t2add = new BigInteger("0");
            // Psuedorandomly determine the next inter-detection time.
            for (int j=0;j<rProbs.size();j++) {
                cumulativeProbability += rProbs.get(j);
                if (p <= cumulativeProbability) {
                    t2add = new BigInteger(String.valueOf(range.get(j)));
                    break;
                }
            }
            if(t2add.compareTo(BigInteger.valueOf(2))==0) {
                currentTime = updateTimes(currentTime);
                times.add(currentTime);
                currentTime = updateTimes(currentTime);
                times.add(currentTime);
            }
            else if(t2add.compareTo(BigInteger.valueOf(1))==0) {
                currentTime = updateTimes(currentTime);
                currentTime = updateTimes(currentTime);
                times.add(currentTime);
            }
            else {
                currentTime = updateTimes(currentTime);
                currentTime = updateTimes(currentTime);
            }
            
            
        }
        
        
        
        return times;
    }
    
    static BigInteger updateTimes(BigInteger currentTime) {
        currentTime = currentTime.add(BigInteger.valueOf(4));
        // Ensure times are correct in relation to minutes.
        int check1 = currentTime.mod(BigInteger.valueOf(100)).compareTo(BigInteger.valueOf(60));
        if(check1==1 ||check1 ==0)  {
            currentTime = currentTime.add(BigInteger.valueOf(40));
        }
        // Ensure times are correct in relation to hours.
        int check2 = currentTime.mod(BigInteger.valueOf(10000)).mod(BigInteger.valueOf(100)).compareTo(BigInteger.valueOf(6000));
        if(check2==1 || check2==0) {
            currentTime = currentTime.add(BigInteger.valueOf(4000));
        }
        return currentTime;
        
    }
    
    
    public static void main(String args[]){
        
        Double[] start = {4028782.80, -248650.79, 4938371.316};
        Double[][] locations = new Double[18][3];
        bss = new ArrayList<>();
        
        Double[] bs1 = {4028807.901663863, -248650.79535150624, 4938371.316965836};
        Double[] bs2 = {4028794.2982123313, -248606.7586559792, 4938384.631943058};
        Double[] bs3 = {4028779.3045908133, -248644.0189771986, 4938394.987999302};
        Double[] bs4 = {4028778.767492502, -248642.9270798688, 4938395.481144028};
        
        bss.add((Double[])bs1);
        bss.add((Double[])bs2);
        bss.add((Double[])bs3);
        bss.add((Double[])bs4);
        
        NewClass nc= new NewClass();
        // populating location list with pheasant coordinates if it moves in a line
        nc.getPheasantLocs(start, locations);
        
        //Not using this
        //int id = nc.generateID();
        //System.out.println("Pheasant ID: " + id);
        
        ArrayList<Double> log1= new ArrayList<>();
        ArrayList<Double> log2= new ArrayList<>();
        ArrayList<Double> log3= new ArrayList<>();
        ArrayList<Double> log4= new ArrayList<>();
        
        // need to create logs for each of the radios, fixed only adding to
        // log 1 to addding to each log
        for (Double[] loc: locations) {
            Double dist1 = nc.getDistance(bs1, loc);
            Double rssi1 = nc.getRSSI(dist1);
            log1.add(rssi1);
            
            Double dist2 = nc.getDistance(bs2, loc);
            Double rssi2 = nc.getRSSI(dist2);
            log2.add(rssi2);
            
            Double dist3 = nc.getDistance(bs3, loc);
            Double rssi3 = nc.getRSSI(dist3);
            log3.add(rssi3);
            
            Double dist4 = nc.getDistance(bs4, loc);
            Double rssi4 = nc.getRSSI(dist4);
            log4.add(rssi4);
        }
        
        //get times
        ArrayList<BigInteger> timez = nc.generateTimes();
        // get tags
        // simply replicate this radio
        tags=new ArrayList<>();
        for(int i=0; i<18; i++){
            tags.add(44001004238L);
        }
        
        //run the thingamabob
        
        primer = new PrimerClass();
        primer.setNumberOfRadios(4);
        for (Double[] bs : bss) {
            primer.setRadioCoordinates(bs[0], bs[1],bs[2]);
        }
        for (int i=0; i<=3; i++){
            primer.setRadioMeasuredPower(-44);
        }
        
        // set lists in primer for each radio
        
        primer.setTRVals(timez, tags, log1);
        primer.setTRVals(timez, tags, log2);
        primer.setTRVals(timez, tags, log3);
        primer.setTRVals(timez, tags, log4);
        
        
        RssiEquation req = new RssiEquation();
        primer.idDistances = req.getTagDistance(primer.idRSSIs, primer.measuredPower);
        
        //MLAT magic below
        
        
        // tag_registry holds all tags across all radios and their coordinates
        // at each time they were detected
        tag_registry = new HashMap<>();
        // used for initialising each element of tag_registry, i.e. a hashmap,
        // with the times each one was picked up as keys
        HashMap<BigInteger, Double[]> init_inner_tag_reg;
        // the times each tag was picked up
        HashMap<Long, ArrayList<BigInteger>> tag_detect_times = new HashMap<>();
        
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
                        ArrayList<Pair<BigInteger, Double>> times
                                = primer.idRSSIs.get(i).get(convertedLong);
                        
                        // list will hold only the times
                        ArrayList<BigInteger> tms = new ArrayList<>();
                        
                        // for all detection pairs
                        for (Pair p : times) {
                            
                            // get the time
                            String aux_str = String.valueOf(p.getKey());
                            BigInteger detection_time = new BigInteger(aux_str);
                            
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
                    ArrayList<BigInteger> time_long = new ArrayList<>();
                    
                    // iterate over all the times we have already recorded it
                    // has been picked up
                    for (int k = 0; k < times.length; k++) {
                        String stringToConv = String.valueOf(times[k]);
                        BigInteger convertedtime = new BigInteger(stringToConv);
                        time_long.add(convertedtime);
                    }
                    
                    // get the detections from the current radio
                    ArrayList<Pair<BigInteger, Double>> tim
                            = primer.idRSSIs.get(i).get(convertedLong);
                    
                    // list will hold only the times
                    ArrayList<BigInteger> tms = new ArrayList<>();
                    
                    // for all detections
                    for (Pair p : tim) {
                        // get&convert the time
                        String aux_str = String.valueOf(p.getKey());
                        BigInteger aux_long = new BigInteger(aux_str);
                        
                        // if multiple detections at the same time,
                        // keep just one
                        if (!tms.contains(aux_long)) {
                            tms.add(aux_long);
                        }
                    }
                    // check if this radio detected the tag at times it was not
                    // detected by other radios
                    for (BigInteger l : tms) {
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
        HashMap<Long, HashMap<BigInteger, Double[]>> hm_returned;
        // call function
        hm_returned = applyMLAT(tag_detect_times);
        // for all tags returned
        for (Long key : hm_returned.keySet()) {
            // get the current tag's inner hashmap that has times as keys
            // and coordinates as values
            HashMap<BigInteger, Double[]> inner = hm_returned.get(key);
            // for all times as keys in inner
            for (BigInteger time : inner.keySet()) {
                
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
        order_times_coords();
    }
    /* This function can loops through all tags which we have in
    /   tag_detect_times.
    /  Gets all the times from tag_detect_times and loops through all radios,
    /   looking for detections around that time.
    /  If detection exists, ok, put forward for mlat
    /   If not, ok, make sure mlat doesn't consider the current radio by adding
    /  a distance of -999,999
    */
    
    public static HashMap<Long, HashMap<BigInteger, Double[]>> applyMLAT(
            HashMap<Long, ArrayList<BigInteger>> tag_detect_times){
        // initialise the hashmap we're going to return
        HashMap time_coords_map = new HashMap();
        HashMap<Long, HashMap<BigInteger, Double[]>> hm = new HashMap<>();
        for (Long key : tag_detect_times.keySet()) {
            ArrayList<BigInteger> times = tag_detect_times.get(key);
            for (BigInteger time: times){
                ArrayList<Double> distances= new ArrayList<>();
                BigInteger three = new BigInteger("3");
                BigInteger four = new BigInteger("4");
                BigInteger one = new BigInteger("1");
                for(int i = 0; i < primer.no_of_radios; i++) {
                    // get the distance between the ith radio and the key tag at
                    // the time time
                    for(BigInteger time_margin=time.subtract(three); time_margin.compareTo(time.add(four))==-1;
                            time_margin.add(one)){
                        try {
                            distances.add(primer.idDistances.get(i).get(key).
                                    get(time));
                            break;
                        }
                        catch(NullPointerException e){
                            if (time_margin.compareTo(time.add(three))==0)
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
                    System.out.println(A.get(1, 0));
                    Matrix B = eq.getB();
                    System.out.println(B.get(1,0));
                    Matrix sol = A.solve(B);
                    System.out.println(sol.get(1,0));//these are our coordinates
                    System.out.println(sol.get(2,0));
                    // the matrix is 4x1,
                    // and entries 1,2,3
                    // give us the x, y, z coord
                    GUI.CoordinateTranslation ct = new GUI.CoordinateTranslation();
                    Double[] coords = new Double[]{sol.get(1, 0), -1*sol.get(2, 0),-1*sol.get(3, 0)};
                    System.out.println("small coord system coords");
                    System.out.println(coords[0]);
                    System.out.println(coords[1]);
                    System.out.println(coords[2]);
                    Double[] geoCoords = ct.cartesianToLatLon(coords);
                    
                    time_coords_map.put(time, geoCoords);
                    hm.put(key, time_coords_map);
                    System.out.println(" ");
                    System.out.println("The geographical location of this tag is: "+ Arrays.deepToString(geoCoords));
                    System.out.println(" ");
                    sol.print(10, 5);
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
    
    public static HashMap<Long, HashMap<BigInteger, Double[]>> getLocations(){
        return tag_registry;
    }
    
    public static void order_times_coords(){
        HashMap<Long, HashMap<BigInteger, Double[]>> tgr=getLocations();
        for (Map.Entry<Long, HashMap<BigInteger, Double[]>> entry : tgr.entrySet()) {
            Long key = entry.getKey();
            HashMap<BigInteger, Double[]> value = entry.getValue();
            all_tags.add(key);
            ArrayList<BigInteger> times= new ArrayList<>();
            ArrayList<Double[]> coords=new ArrayList<>();
            for (Map.Entry<BigInteger, Double[]> entry2 : value.entrySet()) {
                BigInteger key2 = entry2.getKey();
                times.add(key2);
                Double[] value2= entry2.getValue();
                coords.add(value2);
            }
            // do sorting here on times and coords
            ArrayList<BigInteger> times_orig=times;
            times_orig=makeDeepCopyBigInteger(times);
            
            ArrayList<Double[]> sorted_coords= new ArrayList<>();
            Collections.sort(times);
            for(int i=0; i<times.size(); i++){
                for(int j=0; j<times.size(); j++){
                    if (times.get(i)==times_orig.get(j)){
                        sorted_coords.add(i,coords.get(j));
                        break;
                    }
                }
            }
            all_times.add(times);
            all_coords.add(sorted_coords);
        }
        // can use the attributes like this:
        System.out.println("Tag:");
        System.out.println(all_tags.get(0));
        for(int i=0; i<all_times.get(0).size(); i++){
            System.out.println("Time:");
            System.out.println(all_times.get(0).get(i));
            System.out.println("Coords:");
            System.out.println(all_coords.get(0).get(i)[0]);
            System.out.println(all_coords.get(0).get(i)[1]);
            
        }
    }
    private static ArrayList<BigInteger> makeDeepCopyBigInteger(ArrayList<BigInteger> times){
        ArrayList<BigInteger> copy = new ArrayList<>(times.size());
        for(BigInteger i : times){
            BigInteger a = i.add(BigInteger.ZERO);
            copy.add(a);
        }
        return copy;
    }
}
