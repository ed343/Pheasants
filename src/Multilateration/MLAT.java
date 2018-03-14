package Multilateration;

import Jama.Matrix;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import static java.lang.Math.abs;
import javafx.util.Pair;

public class MLAT {

    static int radioIndex = 0;
    static PrimerClass primer;

    public static void main(String args[]) {
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

        // 3. insert all radio 1 Meter RSSI
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
            ArrayList<Long> tData = log.getTimes();
            //Get IDs for this basestation.
            ArrayList<Long> idData = log.getIDs();
            //Get RSSI values for this basestation.
            ArrayList<Double> rssiData = log.getRSSIs();
            primer.setTRVals(tData, idData, rssiData);
        }

        reader.close();

        // 6. find out all distances using RSSI
        RssiEquation req = new RssiEquation();
        primer.idDistances = req.getTagDistance(primer.idRSSIs,
                primer.measuredPower);

        // 7. additional denoising steps like rssiEquationRefined
        // ********************************************************************
        //                 ***Multilateration part***
        // ********************************************************************
        // We organise everything by tag, and then by times.
        // For each tag we extract the times it was detected by each radio.
        // We match each time with detection times from the other radios.
        // Finally, we create the MLAT array and then compute the current tag's 
        // coordinates.
        // ********************************************************************
        // list will hoold all tags across all radios
        ArrayList<Long[]> tagsbyradios = new ArrayList<>();
        // tag_registry holds all tags across all radios and their coordinates
        HashMap<Long, HashMap<Long, ArrayList<Double[]>>> tag_registry = new HashMap<>();
        // used for initialising each element of tag_registry, i.e. a hashmap, 
        // with the times each one was picked up as keys
        HashMap<Long, ArrayList<Double[]>> init_inner_tag_reg;
        // the times each tag was picked up
        HashMap<Long, ArrayList<Long>> tag_detect_times = new HashMap<>();

        // for every radio
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

                    //iterate over the times when the current radio detected 
                    // the current tag
                    for (int k = 0; k < primer.idRSSIs.get(i).
                            get(convertedLong).size(); k++) {

                        // get the times from when the current radio detected 
                        // the current tag
                        ArrayList<Pair<Long, Double>> times
                                = primer.idRSSIs.get(i).get(convertedLong);

                        // list will hold only the times
                        ArrayList<Long> tms = new ArrayList<>();

                        // for all detections
                        for (Pair p : times) {

                            // get the time
                            String aux_str = String.valueOf(p.getKey());
                            Long aux_long = Long.parseLong(aux_str);

                            // if multiple detections at the same time,
                            // keep just one
                            if (!tms.contains(aux_long)) {
                                tms.add(aux_long);
                            }

                            // use empty arraylist of coordinates to init
                            ArrayList<Double[]> a = new ArrayList<>();
                            init_inner_tag_reg.put(aux_long, a);
                        }
                        // create a new entry for this tag and all its detection
                        // times
                        tag_detect_times.put(convertedLong, tms);
                    }
                    tag_registry.put(convertedLong, init_inner_tag_reg);
                } // if the tag is already there
                // see if we get new times from this radio
                else {
                    // get all the times we already have for this radio
                    Object[] times = tag_registry.get(convertedLong).
                                     keySet().toArray();
                    ArrayList<Long> time_long = new ArrayList<>();
                    
                    // all the times we have already recorded it was picked up
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
                            ArrayList<Double[]> a = new ArrayList<>();
                            tag_registry.get(convertedLong).put(l, a);
                        }

                    }
                }
                // add the list of tags the current radio picked up
                tagsbyradios.add(keys_long);
            }
        }
        
        // at this point I have all my tags stored in tag_registry
        // there is one hashmap per tag, consisiting of a time key and
        // their corresponding values are initialised to null, i.e.
        // we don't currently have coordinates for the tags

        // each radio becomes dominant in turn
        // create hashmap to hold the hashmap we'll get from 
        // getTagCoordinatesbyLeadRadio
        HashMap<Long, HashMap<Long, Double[]>> hm_returned = new HashMap<>();
        for (int radio = 0; radio < primer.no_of_radios; radio++) {
            System.out.println("LEAD RADIO " + radio);
            // call function
            hm_returned = getTagCoordinatesbyLeadRadio(radio, tag_detect_times);
            // for all tags returned
            for (Long key : hm_returned.keySet()) {
                // get the current tag's inner hashmap that has times as keys 
                // and coordinates as values
                HashMap<Long, Double[]> inner = hm_returned.get(key);                
                // for all times as keys in inner
                for (Long time : inner.keySet()) {
                    // get the inner hashmap we already have for this tag
                    init_inner_tag_reg = tag_registry.get(key);
                    // get the arraylist from this hashmap
                    ArrayList<Double[]> aux_arr = init_inner_tag_reg.get(time);
                    // add the 3 coordinates we got from the method
                    Double[] aux_coord = new Double[]{inner.get(time)[0], 
                                                   inner.get(time)[1], 
                                                   inner.get(time)[2]};
                    aux_arr.add(aux_coord);
                    // put it back
                    init_inner_tag_reg.put(time, aux_arr);
                    tag_registry.put(key, init_inner_tag_reg);
                }
            }
        }
    }

    public static HashMap<Long, HashMap<Long, Double[]>> 
                  getTagCoordinatesbyLeadRadio(int radio_index, 
                                  HashMap<Long, ArrayList<Long>> times_by_tag) {

        // get all tags from current radio
        Object[] idKeys = primer.idDistances.get(radio_index).keySet().toArray();
        Long[] keys_long = new Long[primer.idDistances.get(radio_index).size()];

        // convert tagIDs to long
        for (int j = 0; j < keys_long.length; j++) {
            String stringToConvert = String.valueOf(idKeys[j]);
            Long convertedLong = Long.parseLong(stringToConvert);
            keys_long[j] = convertedLong;
            System.out.println("Tag parsed: " + convertedLong);
        }
        
        // initialise the hashmap we're going to return
        HashMap<Long, HashMap<Long, Double[]>> hm = new HashMap<>();
        
        // iterate by tagID
        for (Long ID : keys_long) {
            // get all times the current tag was detected by current radio
            ArrayList<Long> times = times_by_tag.get(ID);

            // iterate over every time instance the tag was picked up
            for (int j = 0; j < times.size(); j++) {
                Long first_radio_detection_time = times.get(j);
                // 'first_radio_detection_time' is in turn all the times the 
                // current radio picked up the current tag. We guide our 
                // detections by this first_radio time and we check if the other
                // radios picked this tag up at roughly the same time instance.

                // create an array that will hold the distances between each  
                // radio to the current tag
                ArrayList<Double> distances = new ArrayList<>();

                // we now inspect all radios
                // and get the distances they detected for tag: ID at 
                // roughly the time: first_radio_detection_time
                for (int i = 0; i < primer.no_of_radios; i++) {

                    // aux_time is the time the current radio picked up the 
                    // current tag
                    try {
                        Long aux_time
                                = primer.idDistances.get(i).get(ID).get(j).getKey();
                        // DISCLAIMER: THIS CODE ASSUMES THAT THE DETECTIONS WILL BE
                        // SIMILAR IN VALUE AND NUMBER FOR ALL RADIOS. This is why
                        // we use .get(j) above, because we assume the times are 
                        // ordered.
                        // TODO: Change this to be robust to deal with unordered stuff
                        // this gives me many -999.999
                        // if i fix this, i'll get rid of that
                        if (abs(aux_time - first_radio_detection_time) < 3) {
                            // if the time difference between detections is 
                            // small consider them concurrent
                            distances.add(primer.idDistances.get(i).
                                    get(ID).get(j).getValue());
                        } else {
                            // if this fails, i.e. radio did not pick up tag at 
                            // this time we need to drop this radio
                            // we add a distance of -999.999
                            // that will help us check for valid distances in 
                            // MLATEquation
                            distances.add(-999.999);
                        }
                    } catch (NullPointerException e) {
                        // if we get a null pointer exception
                        // this radio didn't pick up the tag
                        distances.add(-999.999);
                        System.out.println("error finding tag " + ID + 
                                           " at time " + times.get(j) + 
                                           " at radio" + i);
                        System.out.println(distances.size());
                    }
                }

                // we have obtained the distances to this tag from all radios 
                // at the current time, so now we can
                // call MLAT here since have all the data we need
                
                MLATEquation eq = new MLATEquation(distances.size(),
                        primer.getRadiosCoordinates(),
                        distances);

                System.out.println("Tag: " + ID + " at time " + times.get(j) + 
                                    ":");
                System.out.println("No of radios which have picked it up is "
                                   + distances.size());
                // check that we have enough valid distances
                boolean x = eq.fix();
                if (x) {
                    // we have enough
                    Matrix A = eq.getA();
                    Matrix B = eq.getB();
                    Matrix sol = A.solve(B); //these are our coordinates 
                    // the matrix is 4x1, and entries 1,2,3
                    // give us the x, y, z coord
                    Double[] aux_d = new Double[]{sol.get(1, 0), sol.get(2, 0), 
                                                  sol.get(3, 0)};
                    HashMap aux_map = new HashMap();
                    aux_map.put(times.get(j), aux_d);
                    hm.put(ID, aux_map);
                    sol.print(10, 5);
                } else {
                    // we don't have enough valid distances, i.e.
                    // no other radios picked up this tag at this time
                    continue;
                }
            }
        }
        // return the coordinates for the current leading radio
        return hm;
    }
}
