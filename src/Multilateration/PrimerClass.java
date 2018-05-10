package Multilateration;

import java.math.BigInteger;
import javafx.util.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Class to set up data so that it can be correctly processed by the rest of
 * the system.
 * 
 */
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
    

    /**
     * Function to set the number of radios.
     * 
     * @param n: The number of radios to set.
     * 
     * @return : Boolean informing us whether radios have been set correctly.
     */
    boolean setNumberOfRadios(int n){
        // try catch if not getting a byte, or too big a number or an invalid
        // string
        no_of_radios=n;
        return true; // change for error handling
    }
    

    /**
     * Set the cartesian coordinates for a radio.
     * 
     * @param x: x-value of coordinate.
     * 
     * @param y: y-value of coordinate.
     * 
     * @param z: z-value of coordinate.
     * 
     * @return : Boolean informing us whether coordinates have been set
     *           correctly.
     */
    boolean setRadioCoordinates(double x, double y, double z){
        // try catch with error handling
        Double[] coordinates= new Double[]{x,y,z};
        radiosCoordinates.add(coordinates);
        return true; // change once implemented
    }

    /**
     * Function sets the measured power for a radio.
     * 
     * @param p: The measured power (must be in -dBm)
     * 
     * @return : Boolean informing us whether measured power has been set
     *           correctly.
     */
    boolean setRadioMeasuredPower(double p){
        // try catch with error handling
        measuredPower.add(p);
        return true; // change once implemented
    }
    

    /**
     * Method sets the idRSSIs in PrimerClass according to the parsed values it
     * receives, i.e time, tagIDs and rssi values.
     * 
     * @param time : Times array.
     * 
     * @param tagID: IDs array.
     * 
     * @param rssi : RSSIs array.
     * 
     * @return     : Boolean informing us whether idRSSIs has been set
     *               correctly.
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
    
    

    /**
     * Method returns the list of all radio coordinates.
     * @return : List of radio coordinates.
     */
    ArrayList<Double[]> getRadiosCoordinates(){
        return this.radiosCoordinates;
    }


    /**
     * Method returns the list of all measured powers for all radios.
     * 
     * @return Measured power.
     */
    ArrayList<Double> getMeasuredPower(){
        return this.measuredPower;
    }

    /**
     * Method returns the list of all parsed rssi values for the current radios.
     * 
     * @return idRSSI array.
     */
    ArrayList<HashMap<Long, ArrayList<Pair<BigInteger,Double>>>> getRssiValues(){
        return this.idRSSIs;
    }


    /**
     * Method returns all the tag distances computed for the current radio and the
     * tags it picked up.
     * 
     * @return Tag distances array.
     */
    ArrayList<HashMap<Long, HashMap<BigInteger,Double>>> getTagDistances(){
        return this.idDistances;
    }


}

