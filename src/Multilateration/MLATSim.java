package Multilateration;

import Jama.Matrix;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import javafx.util.Pair;

public class MLATSim {
    
    static int radioIndex = 0;
    static PrimerClass primer;
    ArrayList<Double[]> basestations = new ArrayList<>();
    
    HashMap<Long, HashMap<Long, Double[]>> runMLAT() {
        // ArrayList to store the Data extracted from all Log files.
        // each entry in the ArrayList keeps the data associated with one radio
        ArrayList<LogData> dataArr = new ArrayList<>();
        
        // 0. create a program instance:
        primer = new PrimerClass();
        
        // 1. set no. of radios in field
        // Read from System.in
        /*
        Scanner reader = new Scanner(System.in);
        System.out.println("Enter number of radios: ");
        int n = 0;
        if (reader.hasNext()) {
            n = reader.nextInt();
        }
        */
        primer.setNumberOfRadios(4);
        System.out.println("Number of base stations(radios) is 4");
        /*
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
        */
        Double[] basestation1 = {50.728146, -3.527182};
        Double[] basestation2 = {50.729000, -3.523541};
        Double[] basestation3 = {50.727346, -3.523541};
        Double[] basestation4 = {50.729438, -3.526964};

        basestations.add(basestation1);
        basestations.add(basestation2);
        basestations.add(basestation3);
        basestations.add(basestation4);
        GUI.VisualisationController vis = new GUI.VisualisationController();
        Double[] frame = vis.getRect(basestations);

        double centerX = frame[3] + (frame[1] - frame[3]) / 2;
        double centerY = frame[2] + (frame[0] - frame[2]) / 2;
        System.out.println("centerY (lat):" + centerY);
        System.out.println("centerX (lon):" + centerX);
        
        Double[] corners = vis.getMapCorners(centerY, centerX, 16);
        double south = corners[2];
        double west = corners[3];
        GUI.CoordinateTranslation ct = new GUI.CoordinateTranslation();
        Double[] lbCorner = ct.lonLatToCartesian(south, west);
        double ox = lbCorner[0];
        double oy = lbCorner[1];
        double oz = lbCorner[2];
        for(int i=0; i<basestations.size();i++) {
            Double[] bs = basestations.get(i);
            Double[] cart = ct.lonLatToCartesian(bs[0], bs[1]);
            double bx = cart[0];
            double by = cart[1];
            double bz = cart[2];
            primer.setRadioCoordinates((bx-ox), (by-oy), (bz-oz));
        }
        /*
        primer.setRadioCoordinates(5, 4, 2);
        System.out.println("The coordinates of the first radio are (5,4,2)");
        primer.setRadioCoordinates(451, 1, 1.5);
        System.out.println("The coordinates of the first radio are (451,1,1.5)");
        primer.setRadioCoordinates(450, 13, 1);
        System.out.println("The coordinates of the first radio are (450,13,1)");
        primer.setRadioCoordinates(451, 541, 2);
        System.out.println("The coordinates of the first radio are (451,541,2)");
        */
        //}
        
        // 3. insert all radio 1-Meter RSSI
        /*
        for (int i = 0; i < primer.no_of_radios; i++) {
            System.out.println("Enter measured power: ");
            double m = reader.nextDouble();
        */
        /*
        Enter x coordinate: 
5
Enter y coordinate: 
4
Enter z coordinate: 
2
Enter x coordinate: 
451
Enter y coordinate: 
1
Enter z coordinate: 
1.5
Enter x coordinate: 
450
Enter y coordinate: 
13
Enter z coordinate: 
1
Enter x coordinate: 
451
Enter y coordinate: 
541
Enter z coordinate: 
2
Enter measured power: 
-54
Enter measured power: 
-74
Enter measured power: 
-56
Enter measured power: 
-41
        */
        primer.setRadioMeasuredPower(-54);
        System.out.println("The measured power of the first radio is -54");
        primer.setRadioMeasuredPower(-74);
        System.out.println("The measured power of the second radio is -74");
        primer.setRadioMeasuredPower(-56);
        System.out.println("The measured power of the third radio is -56");
        primer.setRadioMeasuredPower(-41);
        System.out.println("The measured power of the fourth radio is -41");
        
        //}
        
        // 4. Extract data from all relevant log files.
        /*
        for (int i = 0; i < primer.no_of_radios; i++) {
            //Ask user to input the full path of a log file.
            System.out.println("Enter filepath to log: ");
            String fPath = new String();
            if (reader.hasNext()) {
                fPath = reader.next();
            }
            //Create a new instance of LogData with path.
        */
        
        // CHANGE TO PATH OF LOG FILES ON YOUR MACHINE.
            LogData log1 = new LogData("/Users/Natalia/Documents/university/4th year/ECMM427 group project/ATLAS_log_files/atlas--1.log");
            dataArr.add(log1);
            LogData log2 = new LogData("/Users/Natalia/Documents/university/4th year/ECMM427 group project/ATLAS_log_files/atlas--2.log");
            dataArr.add(log2);
            LogData log3 = new LogData("/Users/Natalia/Documents/university/4th year/ECMM427 group project/ATLAS_log_files/atlas--3.log");
            dataArr.add(log3);
            LogData log4 = new LogData("/Users/Natalia/Documents/university/4th year/ECMM427 group project/ATLAS_log_files/atlas--1.log");
            dataArr.add(log4);
        //}
        
        // 5. get all rssi values
        for (int i = 0; i < primer.no_of_radios; i++) {
            // add an entry in idRSSIs ArrayList for each parsed file
            // Retrieve LogData object
            LogData log = dataArr.get(i);
            //Get times for this base station.
            ArrayList<Long> tData = log.getTimes();
            //Get IDs for this basestation.
            ArrayList<Long> idData = log.getIDs();
            //Get RSSI values for this basestation.
            ArrayList<Double> rssiData = log.getRSSIs();
            primer.setTRVals(tData, idData, rssiData);
        }
        
        //reader.close();
        
        // 6. find out all distances using RSSI
        RssiEquation req = new RssiEquation();
        primer.idDistances = req.getTagDistance(primer.idRSSIs, primer.measuredPower);
        //primer.idDistances = req.getTagDistance(primer.idRSSIs,
        //        primer.measuredPower);
        
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
    
    public static void main(String[] args) {
        MLATSim m = new MLATSim();
        m.runMLAT();
    }
}
