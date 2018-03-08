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


HashMap getTagDistance2(HashMap<Long, ArrayList<Pair<Long,Double>>>rssi, 
                           int measuredPower) {
        
        // get all tagIDs from rssi
        Object[] idKeys = rssi.keySet().toArray();
        Long[] keys_long = new Long[rssi.size()];  //all the tag IDs
        HashMap<Long, ArrayList<Pair<Long,Double>>> fin = new HashMap();

        for (int i = 0; i < keys_long.length; i++) {
            // convert tagIDs to long
            String stringToConvert = String.valueOf(idKeys[i]);
            Long convertedLong = Long.parseLong(stringToConvert);
            keys_long[i] = convertedLong;

            // get the hashmap for each currently converted tag ID
            // this hashmap correlates the times and the rssi values
            ArrayList<Pair<Long, Double>> inner = rssi.get(keys_long[i]);
            // get all the times from rssi
            Long[] times = new Long[inner.size()];
            Double[] rssis = new Double[inner.size()];
            //initialisation for distance and for an auxilliary HashMap
            double distance = 0;
            ArrayList<Pair<Long, Double>> aux = new ArrayList<>();
            for (int j = 0; j < inner.size(); j++) {
                times[j] = inner.get(j).getKey();
                rssis[j] = inner.get(j).getValue();

                
                // compute RSSI distance according to equation
                distance = Math.pow(10d, ((double) measuredPower
                        - rssis[j]) / (10 * 2));
                Pair<Long,Double> p = new Pair<>(times[j], distance);
                aux.add(p);
            }
            fin.put(keys_long[i], aux);
        }
        return fin;
    }
}
