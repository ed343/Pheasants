package Multilateration;

import java.util.ArrayList;
import java.util.HashMap;
import javafx.util.Pair;

public class RssiEquation {

    /*
     * Method calculates distance in meters based on rssi values.
     * FORMULA:    RSSI = measuredPower - 10 * n * lg(d)
     *              d = 10 ^ ((measuredPower – RSSI) / (10 * n))
     *
     * --measuredPower is the RSSI measured at 1m from a known AP. 
     *   For example: -84 dB. Also known as the 1 Meter RSSI.
     *   ex: measuredPower = -59; 
     * --n is the propagation constant or path-loss exponent. 
     *   For example: 2.7 to 4.3 (Free space has n = 2 for reference).
     * --RSSI is the measured RSSI
     * --d is the distance in meters
     *
     * Both n and measuredPower are determined empirically.
     */
    public ArrayList<HashMap<Long, ArrayList<Pair<Long, Double>>>>
    getTagDistance(ArrayList<HashMap<Long, ArrayList<Pair<Long, Double>>>> rssi,
                   ArrayList<Double> measuredPower) {
        // ASSUMPTION ALL THE TAGS ARE SYNCHRONISED

        // For each tag, I want to get its time/rssi arrays from all radios
        // I search for detections at the times in the list, keep times and rssi 
        // vals in separate arrays and we compute the distance from the current 
        // radio to the current tag at the current time.
        ArrayList<HashMap<Long, ArrayList<Pair<Long, Double>>>> radios_dist
                                                            = new ArrayList<>();

        // loop over all radios
        for (int i = 0; i < rssi.size(); i++) {
            //get the tagIDs from the current radio
            Object[] idKeys = rssi.get(i).keySet().toArray();
            Long[] keys_long = new Long[rssi.get(i).size()];

            // create hashmap with tag IDs as keys and a list of distances at 
            // given times as values
            HashMap<Long, ArrayList<Pair<Long, Double>>> tags_dist_map = 
                                                         new HashMap<>();

            // for every tag the current radio has picked up
            for (int j = 0; j < keys_long.length; j++) {
                
                // convert tagID to long
                String stringToConvert = String.valueOf(idKeys[j]);
                Long convertedLong = Long.parseLong(stringToConvert);
                keys_long[j] = convertedLong;

                // get the arraylist of the times this tag was detected
                // this list correlates the times and the rssi values
                ArrayList<Pair<Long, Double>> inner = rssi.get(i).
                                                      get(keys_long[j]);
                
                // if this radio hasn't picked this tag up, we move on to 
                // the next tag
                if (inner == null) {
                    continue;
                }
                
                // create arrays that will hold the times and rssi values 
                // extracted separately
                Long[] times = new Long[inner.size()];
                Double[] rssis = new Double[inner.size()];
                
                // initialisation for the distance to-be computed 
                // and for an auxilliary list
                double distance;
                ArrayList<Pair<Long, Double>> time_dist_pair_list = 
                                              new ArrayList<>();
                
                // for all the Pairs in the arraylist we got for this tag
                for (int k = 0; k < inner.size(); k++) {
                    // extract times and rssis separately 
                    times[k] = inner.get(k).getKey();
                    rssis[k] = inner.get(k).getValue();

                    // compute RSSI distance according to equation
                    distance = Math.pow(10d, ((double) measuredPower.get(i)
                            - rssis[k]) / (10 * 2));
                    
                    // create new Pair with time of detection and the newly 
                    // computed distance at that time
                    Pair<Long, Double> p = new Pair<>(times[k], distance);
                    time_dist_pair_list.add(p);
                }
                tags_dist_map.put(keys_long[j], time_dist_pair_list);
            }
            radios_dist.add(tags_dist_map);
        }
        return radios_dist;
    }

    // *** DEPRECATED CODE ***
    /*
    HashMap getTagDistance(HashMap<Long, HashMap<Long, Double>> rssi,
                           int measuredPower) {

        // get all tagIDs from rssi
        Object[] idKeys = rssi.keySet().toArray();
        Long[] keys_long = new Long[rssi.size()];  //all the tag IDs
        HashMap<Long, HashMap<Long, Double>> fin = new HashMap();

        for (int i = 0; i < keys_long.length; i++) {
            // convert tagIDs to long
            String stringToConvert = String.valueOf(idKeys[i]);
            Long convertedLong = Long.parseLong(stringToConvert);
            keys_long[i] = convertedLong;

            // get the hashmap for each currently converted tag ID
            // this hashmap correlates the times and the rssi values
            HashMap<Long, Double> inner = rssi.get(keys_long[i]);
            // get all the times from rssi
            Object[] timeKeys = inner.keySet().toArray();
            Long[] time_long = new Long[rssi.size()];
            //initialisation for distance and for an auxilliary HashMap
            double distance = 0;
            HashMap<Long, Double> aux = new HashMap();
            for (int j = 0; j < inner.size(); j++) {
                // convert times to long
                String stringToConvert2 = String.valueOf(timeKeys[j]);
                Long convertedLong2 = Long.parseLong(stringToConvert2);
                time_long[j] = convertedLong2;

                // compute RSSI distance according to equation
                distance = Math.pow(10d, ((double) measuredPower
                        - inner.get(time_long[j])) / (10 * 2));
                aux.put(time_long[j], distance);
            }
            fin.put(keys_long[i], aux);
        }
        return fin;
    }
     */
}
