package Multilateration;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import javafx.util.Pair;

public class RssiEquation {

    /**
     * Method calculates distance in meters based on RSSI values.
     * 
     * @param rssi         : The input array containing hash map of IDs, Times
     *                       and RSSIs.
     * 
     * @param measuredPower: The RSSI measured at 1m from a known basestation.
     * 
     * @return             : The array containing hash map of IDs, times and
     *                       distance estimations.
     */
    public ArrayList<HashMap<Long, HashMap<BigInteger, Double>>>
    getTagDistance(ArrayList<HashMap<Long, ArrayList<Pair<BigInteger, Double>>>> rssi,
                   ArrayList<Double> measuredPower) {
        // For each tag, I want to get its time/rssi arrays from all radios
        // I search for detections at the times in the list, keep times and rssi 
        // vals in separate arrays and we compute the distance from the current 
        // radio to the current tag at the current time.
        ArrayList<HashMap<Long, HashMap<BigInteger, Double>>> radios_dist
                                                            = new ArrayList<>();

        // loop over all radios
        for (int i = 0; i < rssi.size(); i++) {
            //get the tagIDs from the current radio
            Object[] idKeys = rssi.get(i).keySet().toArray();
            Long[] keys_long = new Long[rssi.get(i).size()];

            // create hashmap with tag IDs as keys and a list of distances at 
            // given times as values
            HashMap<Long, HashMap<BigInteger, Double>> tags_dist_map = 
                                                         new HashMap<>();

            // for every tag the current radio has picked up
            for (int j = 0; j < keys_long.length; j++) {
                
                // convert tagID to long
                String stringToConvert = String.valueOf(idKeys[j]);
                Long convertedLong = Long.parseLong(stringToConvert);
                keys_long[j] = convertedLong;

                // get the arraylist of the times this tag was detected
                // this list correlates the times and the rssi values
                ArrayList<Pair<BigInteger, Double>> inner = rssi.get(i).
                                                      get(keys_long[j]);
                
                // if this radio hasn't picked this tag up, we move on to 
                // the next tag
                if (inner == null) {
                    continue;
                }
                
                // create arrays that will hold the times and rssi values 
                // extracted separately
                BigInteger[] times = new BigInteger[inner.size()];
                Double[] rssis = new Double[inner.size()];
                
                // initialisation for the distance to-be computed 
                // and for an auxilliary list
                double distance;
                HashMap<BigInteger, Double> time_dist_hm = 
                                              new HashMap<>();
                
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
                    time_dist_hm.put(times[k], distance);
                }
                tags_dist_map.put(keys_long[j], time_dist_hm);
            }
            radios_dist.add(tags_dist_map);
        }
        return radios_dist;
    }

}
