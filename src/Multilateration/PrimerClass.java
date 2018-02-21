package Multilateration;

import java.util.ArrayList;
import java.util.HashMap;
import javafx.util.Pair;

public class PrimerClass {
    static int no_of_radios;
    // for all the following, the index refers to the radio instance
    // i.e. radiosCoordinates[0] gives the coordinates of the first radio
    //      rssiValues[0] gives all the hashmaps (keyed by tagID and valued by
    //                    a pair <time-rssi>) for all tags the first radio 
    //                    picked up
    //      tagDistances[0] gives all the tag distances from the first radio 
    //      etc
    ArrayList<Double[]> radiosCoordinates= new ArrayList<>();
    ArrayList<Double> measuredPower= new ArrayList<>();
    ArrayList<HashMap<Long, HashMap<Long, Double>>> rssiValues= new ArrayList<>();
    ArrayList<HashMap<Long, HashMap<Long, Double>>> tagDistances=new ArrayList<>();
    
    ////////////////////////////////////////////////////////////////////////////
    //////////////////////////////setters///////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    
    
    /* Method accepts int value to  denote number of radios
     */
    boolean setNumberOfRadios(int n){
        // try catch if not getting a byte, or too big a number or an invalid
        // string
        no_of_radios=n;
        return false; // change to true when implemented
    }
    
    /* Method accepts double values for x, y and z coordinates 
     * for each radio
     */
    boolean setRadioCoordinates(double x, double y, double z){
        // try catch with error handling
        Double[] coordinates= new Double[]{x,y,z};
        radiosCoordinates.add(coordinates);
        return false; // change once implemented
    }
    
    /* Method accepts double values for p, the RSSI measured at 1m from 
     * the radio. For example: -84 dB. 
     * Also known as the 1 Meter RSSI.
     */
    boolean setRadioMeasuredPower(double p){
        // try catch with error handling
        measuredPower.add(p);
        return false; // change once implemnted
    }
    
    /* Method accepts double values for the RSSI values extracted by the parser. 
     */
    boolean setTimeRssiValues(ArrayList<Long>time, ArrayList<Long> tagID, ArrayList<Double> rssi){
        // try catch with error handling
        
        // create the entire HashMap first
        // no way to do it fast, do it manually
        HashMap hm=new HashMap();
        for(int i=0; i<time.size(); i++){
            // we create a Pair for each time-rssi value pair and store it
            Pair pair= new Pair(tagID.get(i), rssi.get(i));
            hm.put(time.get(i), pair);
        }
        
        rssiValues.add(hm);
        return false; // change once implemented
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    //////////////////////////////getters///////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    
    
    /* Method returns the list of all radio coordinates.
     */
    ArrayList getRadiosCoordinates(){
        return this.radiosCoordinates;
    }

    /* Method returns the list of all measured powers for each radio.
     */
    ArrayList getMeasuredPower(){
        return this.measuredPower;
    }

    /* Method returns the list of all parsed rssi values for all radios.
     */
    ArrayList getRssiValues(){
        return this.rssiValues;
    }
    /* Method returns all the tag distances computed for all radios and the  
     * tags they picked up
     */
    ArrayList getTagDistances(){
        return this.tagDistances;
    }
}
