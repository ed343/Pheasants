package Multilateration;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import java.util.ArrayList;
import java.util.HashMap;

/* This class describes a way to refine and denoise the computed distances.
 * That is, assuming that a pheasant does not move very much in the span of 
 *   delta t (e.g. 10 s), we check each radio's RSSI values associated with
 *   the tag whose distance we are trying to refine.
 * If the transmission is noisy, each of the RSSI values will indicate a 
 *   different distance to the tag, even if the timespan is small. We calculate 
 *   the mean and standard deviation of these samples and derive a new distance.
 */
public class RssiEquationRefined {

    /*
     * Method calculates a denoised distance, taking into consideration that
     *   RSSI values are normally distributed.
     * Method takes as parameters the already computed distances for a tag,
     *   and a delta t.
     * Supporting maths:
     *   By sampling distance values, we can find their mean (miu) and standard 
     *     deviation (std).
     *   We can calculate a denoised distance according to this formula:
     *     r=miu^4/(miu^2+std^2)
     * @returns: a new HashMap meant to replace tagDistances
     */
    HashMap getTagDistance(HashMap<Long, HashMap<Long, Double>> tagDistances, 
                           int delta_t) {
        // get all tagIDs from rssi
        Object[] tagIds = tagDistances.keySet().toArray();
        Long[] tagIds_long = new Long[tagDistances.size()];  //all the tag IDs
        HashMap<Long, HashMap<Long, Double>> fin = new HashMap();
        for (int i = 0; i < tagIds_long.length; i++) {
            
            // convert tagIDs to long
            String stringToConvert = String.valueOf(tagIds[i]);
            Long convertedLong = Long.parseLong(stringToConvert);
            tagIds_long[i] = convertedLong;

            // get the hashmap for each currently converted tag ID
            // this hashmap correlates the times and the rssi values
            HashMap<Long, Double> inner = tagDistances.get(tagIds_long[i]);
            // get all the times from rssi
            Object[] timeKeys = inner.keySet().toArray();
            Long[] time_long = new Long[tagDistances.size()];
            //initialisation for an auxilliary HashMap
            HashMap<Long, Double> aux = new HashMap();

            for (int j = 0; j < inner.size(); j++) {
                // convert times to long
                String stringToConvert2 = String.valueOf(timeKeys[j]);
                Long convertedLong2 = Long.parseLong(stringToConvert2);
                time_long[j] = convertedLong2;
                
                // compute denoised distance from samples
                // consider detections witin a certain timespan delta_t
                ArrayList<Double> radii = new ArrayList();
                radii.add(inner.get(time_long[j]));
                for (int k = j + 1; k < inner.size(); k++) {
                    if (time_long[j] - time_long[k] < delta_t) {
                        radii.add(inner.get(time_long[k]));
                    }
                }
                // compute mean and standard deviation from samples
                double mean = 0;
                double std = 0;
                for (int l = 0; l < radii.size(); l++) {
                    mean += (double) radii.get(l);
                }
                for (int l = 0; l < radii.size(); l++) {
                    std += pow((radii.get(l) - mean), 2);
                }
                std = sqrt(std / radii.size());
                //!!!
                // here potentially create a threshold for a value of std
                // if it doesn't change a lot, we leave it as it is
                // calculate denoised distance
                double denoised_distance = sqrt(pow(mean, 4) / 
                                            (pow(mean, 2) + pow(std, 2)));
                // save it
                aux.put(time_long[j], denoised_distance);
            }
            fin.put(tagIds_long[i], aux);
        }
        return fin;
    }
}