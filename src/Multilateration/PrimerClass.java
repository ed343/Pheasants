package Multilateration;

import java.math.BigInteger;
import javafx.util.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class PrimerClass {
    public static int no_of_radios;
    // for the following two entries, the index refers to the radio instance
    // i.e. radiosCoordinates[0] gives the coordinates of the first radio
    //
    // THIS INFO WILL BE TAKEN FROM BASESTATION REGISTRATION
    public static ArrayList<Double[]> radiosCoordinates= new ArrayList<>();
    ArrayList<Double> measuredPower= new ArrayList<>();

    //following from James' new code, we should probably only need the following
    ArrayList<HashMap<Long, ArrayList<Pair<BigInteger,Double>>>> idRSSIs = 
                                                              new ArrayList<>();
    // one entry for each radio
    // the entry is a hashmap keyed by tag IDs
    // and values are a hashmap keyed by times
    // and values are the distances between the current radio and the tag at the
    // current time
    ArrayList<HashMap<Long, HashMap<BigInteger,Double>>> idDistances = 
                                                              new ArrayList<>();


    ////////////////////////////////////////////////////////////////////////////
    //////////////////////////////setters///////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    
    
    /* Method accepts int value to  denote number of radios
     */
    boolean setNumberOfRadios(int n){
        // try catch if not getting a byte, or too big a number or an invalid
        // string
        no_of_radios=n;
        return true; // change for error handling
    }
    
    /* Method accepts double values for x, y and z coordinates 
     * for each radio
     */
    boolean setRadioCoordinates(double x, double y, double z){
        // try catch with error handling
        Double[] coordinates= new Double[]{x,y,z};
        radiosCoordinates.add(coordinates);
        return true; // change once implemented
    }
    
    /* Method accepts double values for p, the RSSI measured at 1m from 
     * the radio. For example: -84 dB. 
     * Also known as the 1 Meter RSSI.
     */
    boolean setRadioMeasuredPower(double p){
        // try catch with error handling
        measuredPower.add(p);
        return true; // change once implemented
    }
    
    /* Method sets the idRSSIs in PrimerClass according to the parsed values it
     * receives, i.e. time, tagIDs and rssi values
     * For each radio, we create a HashMap that will hold its tags'
     * detections. That means that the first element of the hm HashMap will
     * have as key the first tag detected by the first radio and as value it
     * will have an ArrayList of the Pairs(times, rssi value).
     */
    boolean setTRVals(ArrayList<BigInteger>time, ArrayList<Long> tagID, 
                                    ArrayList<Double> rssi) {
        // get all the tags that were passed as a parameter in tagID
        HashMap<Long, ArrayList<Pair<BigInteger,Double>>> hm= new HashMap();
        Set<Long> uniqueIDs = new HashSet<>(tagID);
        Object[] uIArr = uniqueIDs.toArray();
        
        // for each tag
        for(int i=0;i<uniqueIDs.size();i++) {
            // parse it as a Long
            long val = Long.parseLong(uIArr[i].toString());
            // create an empty list of detections
            ArrayList<Pair<BigInteger,Double>> detList = new ArrayList<>();
            // whenever the current tag shows up in the tag list,  
            // keep its corresponding detection times
            for(int j=0; j<tagID.size();j++) {
                if(tagID.get(j)==val) {
                    // add all the current tag's detections to its list
                    Pair<BigInteger,Double> timeRSSI = new Pair<>(
                                                 time.get(j),rssi.get(j));
                    detList.add(timeRSSI);
                }
            }
            // add an entry in the hashmap with the tag id as key and the list 
            // of its detections as the value
            hm.put(val, detList);
        }
        idRSSIs.add(hm);
        return true;
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    //////////////////////////////getters///////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    
    
    /* Method returns the list of all radio coordinates.
     */
    ArrayList<Double[]> getRadiosCoordinates(){
        return this.radiosCoordinates;
    }

    /* Method returns the list of all measured powers for all radios.
     */
    ArrayList<Double> getMeasuredPower(){
        return this.measuredPower;
    }

    /* Method returns the list of all parsed rssi values for the current radios.
    */
    ArrayList<HashMap<Long, ArrayList<Pair<BigInteger,Double>>>> getRssiValues(){
        return this.idRSSIs;
    }
    /* Method returns all the tag distances computed for the current radio and the
     * tags it picked up.
     */

    ArrayList<HashMap<Long, HashMap<BigInteger,Double>>> getTagDistances(){
        return this.idDistances;
    }


    // *** DEPRECATED CODE ***

    //keep initial logic as back-up
    //rssiValues[0]     gives all the hashmaps (keyed by tagID and valued by
    //                  a pair <time-rssi>) for all tags the first radio
    //                  picked up
    //tagDistances[0]   gives all the tag distances from the first radio
    //etc
    //ArrayList<HashMap<Long, HashMap<Long, Double>>> rssiValues= new ArrayList<>();
    //ArrayList<HashMap<Long, HashMap<Long, Double>>> tagDistances=new ArrayList<>();

    /* Method accepts double values for the RSSI values extracted by the parser.
     * Deprecated due to new code logic in which each beacon represents an object with its own fields.
     *
    boolean setTimeAndRssiValues(ArrayList<Long>time, ArrayList<Long> tagID,
                                    ArrayList<Double> rssi){
        // try catch with error handling

        // create the entire HashMap first
        // no way to do it fast, do it manually
        HashMap<Long, HashMap<Long, Double>> hm=new HashMap();
        for(int i=0; i<time.size(); i++){
            // we create a Pair for each time-rssi value pair and store it
            HashMap<Long, Double> pair= new HashMap();
            pair.put(time.get(i), rssi.get(i));
            hm.put(tagID.get(i), pair);
        }

        rssiValues.add(hm);
        return false; // change once implemented
    }
      */

    /* Method returns the list of all parsed rssi values for all radios.
     * Deprecated due to new code logic in which each beacon represents an 
     *  object with its own fields.
    ArrayList getRssiValues(){
        return this.rssiValues;
    }
     */
    /* Method returns all the tag distances computed for all radios and the  
     * tags they picked up
     * Deprecated due to new code logic in which each beacon represents an 
     * object with its own fields.

    ArrayList getTagDistances(){
        return this.tagDistances;
    }
    */
}

