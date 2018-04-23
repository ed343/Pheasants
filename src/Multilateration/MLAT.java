package Multilateration;

import Jama.Matrix;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import javafx.util.Pair;

public class MLAT {
    
    static int radioIndex = 0;
    static PrimerClass primer;
    
    public static HashMap<Long, HashMap<BigInteger, Double[]>> main(String args[]) {
        // ArrayList to store the Data extracted from all Log files.
        // each entry in the ArrayList keeps the data associated with one radio
        ArrayList<LogData> dataArr = new ArrayList<>();
        
        // 0. create a program instance:
        primer = new PrimerClass();
        
        // 1. set no. of radios in field
        // Read from System.in
        Scanner reader = new Scanner(System.in);
        System.out.println("Enter number of radios: ");
        int n = 0;
        if (reader.hasNext()) {
            n = reader.nextInt();
        }
        primer.setNumberOfRadios(n);
        
        // 2. insert all radio coordinates
        for (int i = 0; i < primer.no_of_radios; i++) {
            double x = 0, y = 0, z = 0;
            System.out.println("Enter x coordinate: ");
            if (reader.hasNext()) {
                x = reader.nextDouble(); // Scans the next token of the input as a double.
            }
            System.out.println("Enter y coordinate: ");
            if (reader.hasNext()) {
                y = reader.nextDouble();
            }
            System.out.println("Enter z coordinate: ");
            if (reader.hasNext()) {
                z = reader.nextDouble();
            }
            primer.setRadioCoordinates(x, y, z);
        }
        
        // 3. insert all radio 1-Meter RSSI
        for (int i = 0; i < primer.no_of_radios; i++) {
            System.out.println("Enter measured power: ");
            double m = reader.nextDouble();
            primer.setRadioMeasuredPower(m);
        }
        
        // 4. Extract data from all relevant log files.
        for (int i = 0; i < primer.no_of_radios; i++) {
            //Ask user to input the full path of a log file.
            System.out.println("Enter filepath to log: ");
            String fPath = new String();
            if (reader.hasNext()) {
                fPath = reader.next();
            }
            //Create a new instance of LogData with path.
            LogData log = new LogData(fPath);
            //Add LogData object to ArrayList.
            dataArr.add(log);
        }
        
        // 5. get all rssi values
        for (int i = 0; i < primer.no_of_radios; i++) {
            // add an entry in idRSSIs ArrayList for each parsed file
            // Retrieve LogData object
            LogData log = dataArr.get(i);
            //Get times for this base station.
            ArrayList<BigInteger> tData = log.getTimes();
            //Get IDs for this basestation.
            ArrayList<Long> idData = log.getIDs();
            //Get normalised RSSI values for this basestation.
            ArrayList<Double> rssiData = log.getNormRSSIs();
            primer.setTRVals(tData, idData, rssiData);
        }
        
        reader.close();
        
        // 6. find out all distances using RSSI
        RssiEquation req = new RssiEquation();
        primer.idDistances = req.getTagDistance(primer.idRSSIs,
                primer.measuredPower);
        
        // 7. additional denoising steps like rssiEquationRefined
        // ...
        
        // ********************************************************************
        //                 ***Multilateration part***
        // ********************************************************************
        // We organise everything by tag, and then by times.
        // For each tag we extract the times it was detected by each radio.
        // We match each time with detection times from the other radios.
        // Finally, we create the MLAT array and then compute the current tag's
        // coordinates.
        // ********************************************************************
        
        
        // PART 1
        // Extract information from the fields of the primer object, such as 
        // idRSSIs and create variables that reflect the state of the system 
        // on the whole.
        // For instance: tag_registry holds all the tags that were detected 
        // across all radios and their coordinates at all detection times
        //               tag_detect_times holds all tags that were detected
        // across all radios and each of the times they were picked up.
        
        
        
        // tag_registry holds all tags across all radios and their coordinates
        // at each time they were detected
        HashMap<Long, HashMap<BigInteger, Double[]>> tag_registry = new HashMap<>();
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
        return hm_returned;
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
                System.out.println(primer.idDistances);
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
// the matrix is 4x1, and entries 1,2,3
                    // give us the x, y, z coord
                    GUI.CoordinateTranslation ct = new GUI.CoordinateTranslation();
                    Double[] coords = new Double[]{sol.get(1,0), sol.get(2, 0),
                        sol.get(3, 0)};
                    HashMap time_coords_map = new HashMap();
                    time_coords_map.put(time, coords);
                    hm.put(key, time_coords_map);
                    System.out.println(" ");
                    System.out.println("The location of this tag is: "+ Arrays.deepToString(coords));
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
}
