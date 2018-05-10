/*
Generating dummy log files.
 */
package Multilateration;

import GUI.MapProcessing;
import Jama.Matrix;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.util.Pair;
import org.apache.commons.math3.distribution.PoissonDistribution;

public class Simulation {

    static PrimerClass primer;
    static ArrayList<Long> tags;
    static ArrayList<Double[]> basestations;
    static int no_of_tags;

    static HashMap<Long, HashMap<BigInteger, Double[]>> tag_registry;
    // these are the final ordered data
    static ArrayList<Long> all_tags = new ArrayList<>();
    static ArrayList<ArrayList<BigInteger>> all_times = new ArrayList<>();
    static ArrayList<ArrayList<Double[]>> all_coords = new ArrayList<>();
    
    static MapProcessing mp;
    
    static int detections = 16;

    /* 
     */
    /**
     * Method computes the locations of the pheasant trajectory given the
     * starting position. Currently, this draws the trajectory as a straight
     * line.
     *
     * @param start - start location coordinates
     * @return locs - array of all points o the trajectory
     */
    public Double[][] getPheasantLocs(Double[] start) {
        // create an array that will hold the coordinates part of the trajectory
        Double[][] locs = new Double[detections][2];
        Double x = start[1]; // longitude
        Double y = start[0]; // latitude

        // function creates a line
        for (int i = 0; i < detections; i++) {
            locs[i][1] = x + 0.0001;
            locs[i][0] = y;
            x = x + 0.0001;
        }
        return locs;
    }
    
    /**
     * Method computes locations for a semi-circle.
     * @param centre = the centre of the circle.
     * @param radius = the radius of the circle.
     * @param startAngle = the start angle, the function will create locations
     *                     from startAngle up to startAngle + 180 degrees.
     * 
     * @return Array of locations.
     */
    public Double[][] drawSemiCircle(Double[]centre, Double radius, Double startAngle) {
        Double[][] locs = new Double[18][2];
        //xcoord for centre
        Double cx = centre[1]; // longitude
        //ycoord for centre
        Double cy = centre[0]; // latitude
        Double deg = startAngle;
        for(int i=0;i<18;i++) {
            //Convert to radians
            Double rad = (deg*Math.PI)/180;
            // x location
            locs[i][1] = cx + (radius*Math.cos(rad));
            // y location
            locs[i][0] = cy + (radius*Math.sin(rad));
            deg+=10;
        }
        return locs;
    }
    
    /**
     * Method computes distance between a basestation and a tag.
     *
     * @param base - basestation coordinates
     * @param tag - tag coordinates
     * @return
     */
    public Double getDistance(Double[] base, Double[] tag) {
        Double d = 0.0;
        Double tempx = Math.abs(base[0] - tag[0]);
        Double tempy = Math.abs(base[1] - tag[1]);
        Double tempz = Math.abs(base[2] - tag[2]);

        d = Math.sqrt(Math.pow(tempx, 2) + Math.pow(tempy, 2) + Math.pow(tempz, 2));

        return d;
    }

    /**
     * Method gets RSSI value from distances.
     *
     * @param d - distance
     * @param radio_index - index of the radio to get measuredPower
     * @return RSSI value
     */
    public Double getRSSI(Double d, int radio_index) {
        Double mp = primer.measuredPower.get(radio_index);
        Double rssi = mp - 10 * 2 * Math.log10(d);

        return rssi;
    }

    // not using this
    public int generateID() {
        Random rand = new Random();
        int value = rand.nextInt(50);

        return value;
    }

    /**
     * Method generates a list of times for detections.
     *
     * @return al - arraylist of times
     */
    public ArrayList<BigInteger> generateTimes() {

        ArrayList<BigInteger> al = new ArrayList<>();
        for (int j = 0; j < no_of_tags; j++) {
            BigInteger start = new BigInteger("10");
            BigInteger four = new BigInteger("4");
            for (int i = 0; i < detections; i++) {
                al.add(start.add(four));
                start = start.add(four);
            }
        }
        return al;
    }
    /**
     * TO-DO
     * @param iters
     * @param startTime
     * @return 
     */
    static ArrayList<BigInteger> gTPoisson(int iters, BigInteger startTime) {
        // Create times array
        ArrayList<BigInteger> times = new ArrayList<>();
        // Add the start time to the array
        times.add(startTime);
        BigInteger currentTime = startTime;
        // Create a list of integers, ranging from start(INPUT 4) to end(INPUT 5) inclusive.
        List<Integer> range = IntStream.rangeClosed(0, 2).boxed().collect(Collectors.toList());
        // Array to hold probabilities for all integers in range.
        ArrayList<Double> rProbs = new ArrayList<>();
        // Create a poisson distribution with mean of mean(INPUT 3)
        PoissonDistribution pdist = new PoissonDistribution(1);
        double cp = 0;
        for (Integer range1 : range) {
            // Calculate probabilities for all integers in range and add to array.
            double prob = pdist.cumulativeProbability(range1);
            rProbs.add(prob);
            // Calculate the cumulative probabilty for members of range.
            cp += prob;
        }

        for (int i = 0; i < iters; i++) {
            // Generate a random double between 0 and cp.
            double p = Math.random() * cp;
            double cumulativeProbability = 0.0;
            BigInteger t2add = new BigInteger("0");
            // Psuedorandomly determine the next inter-detection time.
            for (int j = 0; j < rProbs.size(); j++) {
                cumulativeProbability += rProbs.get(j);
                if (p <= cumulativeProbability) {
                    t2add = new BigInteger(String.valueOf(range.get(j)));
                    break;
                }
            }
            if (t2add.compareTo(BigInteger.valueOf(2)) == 0) {
                currentTime = updateTimes(currentTime);
                times.add(currentTime);
                currentTime = updateTimes(currentTime);
                times.add(currentTime);
            } else if (t2add.compareTo(BigInteger.valueOf(1)) == 0) {
                currentTime = updateTimes(currentTime);
                currentTime = updateTimes(currentTime);
                times.add(currentTime);
            } else {
                currentTime = updateTimes(currentTime);
                currentTime = updateTimes(currentTime);
            }

        }
        if(times.size()<iters) {
            int num = iters-times.size();
            for(int x=0;x<num;x++) {
                currentTime = updateTimes(currentTime);
                times.add(currentTime);
            }
        }
            
        return times;
    }
    /**
     * Function takes times, IDs and RSSIs and writes them to a 'fake' log file
     * in csv format.
     * @param times
     * @param IDs
     * @param rssis
     * @param fp
     * @throws IOException 
     */
    public void writeLog(ArrayList<BigInteger> times, ArrayList<Long> IDs, 
                         ArrayList<Double> rssis, String fp) throws IOException{
        File file = new File(fp);
        file.createNewFile();
        FileWriter writer = new FileWriter(fp);
        for (int i = 0; i < 15; i++) {
            writer.write("Time=" + String.valueOf(times.get(i)) + ";ID="
                    + String.valueOf(IDs.get(i)) + ";RSSI=" 
                    + String.valueOf(rssis.get(i)) + ";\n");
        }
        writer.close();
    }

    /**
     * Function to generate log files. 
     * @param size - number of detections to generate
     * @param fp - file path to the folder where log files will be stored.
     */
    public void genLog(int size, String fp) {

        Simulation nc = new Simulation();

        Double[] start = {50.73848598629042, -3.531734873115414}; 
        // {4028782.80, -248650.79, 4938371.316};

        Double[][] locations = new Double[size][2];

        basestations = nc.getGeoBasestations();

        MapProcessing mp = new MapProcessing(basestations);

        // get ArrayList of cartesian coordinates of basestations
        ArrayList<Double[]> stations = mp.getBasestations(basestations);

        // populate location list with pheasant coords if it moves in a line
        locations = nc.getPheasantLocs(start);

        ArrayList<Double> rs1 = new ArrayList<>();
        ArrayList<Double> rs2 = new ArrayList<>();
        ArrayList<Double> rs3 = new ArrayList<>();
        ArrayList<Double> rs4 = new ArrayList<>();

        // need to create logs for each of the radios, fixed only adding to
        // log 1 to addding to each log
        for (Double[] loc : locations) {

            Double[] locCart = mp.getCartesianLoc(loc);

            Double dist1 = getDistance(stations.get(0), locCart);
            Double rssi1 = getRSSI(dist1, 0);
            rs1.add(rssi1);

            Double dist2 = getDistance(stations.get(1), locCart);
            Double rssi2 = getRSSI(dist2, 1);
            rs2.add(rssi2);

            Double dist3 = getDistance(stations.get(2), locCart);
            Double rssi3 = getRSSI(dist3, 2);
            rs3.add(rssi3);

            Double dist4 = getDistance(stations.get(3), locCart);
            Double rssi4 = getRSSI(dist4, 3);
            rs4.add(rssi4);
        }

        //get times
        //start time
        BigInteger sTime = new BigInteger("20171116090000");
        ArrayList<BigInteger> timez = gTPoisson(size, sTime);
        // get tags
        // simply replicate this radio
        tags = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            tags.add(44001004238L);
        }
        String fp1 = fp + "/1.csv";
        String fp2 = fp + "/2.csv";
        String fp3 = fp + "/3.csv";
        String fp4 = fp + "/4.csv";
        try {
            writeLog(timez, tags, rs1, fp1);
            writeLog(timez, tags, rs2, fp2);
            writeLog(timez, tags, rs3, fp3);
            writeLog(timez, tags, rs4, fp4);
        } catch (IOException ex) {
            System.out.println("didn't work");
        }
    }

    /**
     * TO-DO
     * @param currentTime
     * @return 
     */
    static BigInteger updateTimes(BigInteger currentTime) {
        currentTime = currentTime.add(BigInteger.valueOf(4));
        // Ensure times are correct in relation to minutes.
        int check1 = currentTime.mod(BigInteger.valueOf(100)).
                                     compareTo(BigInteger.valueOf(60));
        if (check1 == 1 || check1 == 0) {
            currentTime = currentTime.add(BigInteger.valueOf(40));
        }
        // Ensure times are correct in relation to hours.
        int check2 = currentTime.mod(BigInteger.valueOf(10000)).
                                 mod(BigInteger.valueOf(100)).
                                 compareTo(BigInteger.valueOf(6000));
        if (check2 == 1 || check2 == 0) {
            currentTime = currentTime.add(BigInteger.valueOf(4000));
        }
        return currentTime;

    }
    /**
     * Method sets the coordinates of the basestations.
     * @return 
     */
    public ArrayList<Double[]> getGeoBasestations() {

        ArrayList<Double[]> temp = new ArrayList<>();
        // read basestation coordinates from log 
        // upload selected basestations
        // and get their coordinates from the database --- ????
        Double[] bs1 = {50.738486, -3.531713};
        Double[] bs2 = {50.738675, -3.531101};
        Double[] bs3 = {50.738822, -3.531642};
        Double[] bs4 = {50.738829, -3.531627};

        temp.add(bs1);
        temp.add(bs2);
        temp.add(bs3);
        temp.add(bs4);

        return temp;
    }
    
    /**
     * Function returns the simulated locations for as many tags, radios and 
     * time steps as desired.
     * @return registry - the registry of tags, which contain all the tags 
     *                    with each one's detection time and corresponding 
     *                    coordinate
     */
    public HashMap<Long, HashMap<BigInteger, Double[]>> simulateLocations() {

        no_of_tags = 4;
        basestations = getGeoBasestations();
        // start position should be rand. generated between basestations
        Double[] start = {50.73848598629042, -3.531734873115414};
        // for each tag, arraylist.get(tag_index) holds all its locations
        ArrayList<Double[][]> locations = new ArrayList<>();
        
        mp = new MapProcessing(basestations);
        // get ArrayList of cartesian coordinates of basestations
        ArrayList<Double[]> stations = mp.getBasestations(basestations);
        
        // set basestation variables in primer class
        primer = new PrimerClass();
        primer.setNumberOfRadios(4);
        for (Double[] bs : stations) {
            primer.setRadioCoordinates(bs[0], bs[1], bs[2]);
        }

        for (int i = 0; i < 4; i++) {
            primer.setRadioMeasuredPower(-44);
        }

        // get coordinates for each of the tags
        for (int i = 0; i < no_of_tags; i++) {
            locations.add(getPheasantLocs(start));
            start[0] = start[0] + 0.0001;
            start[1] = start[1] + 0.0001;
        }

        ArrayList<Double> rs1 = new ArrayList<>();
        ArrayList<Double> rs2 = new ArrayList<>();
        ArrayList<Double> rs3 = new ArrayList<>();
        ArrayList<Double> rs4 = new ArrayList<>();

        // need to create logs for each of the radios, fixed only adding to
        // log 1 to addding to each log
        for (int i = 0; i < no_of_tags; i++) {
            for (Double[] loc : locations.get(i)) {

                Double[] locCart = mp.getCartesianLoc(loc);

                Double dist1 = getDistance(stations.get(0), locCart);
                Double rssi1 = getRSSI(dist1, 0);
                rs1.add(rssi1);

                Double dist2 = getDistance(stations.get(1), locCart);
                Double rssi2 = getRSSI(dist2, 1);
                rs2.add(rssi2);

                Double dist3 = getDistance(stations.get(2), locCart);
                Double rssi3 = getRSSI(dist3, 2);
                rs3.add(rssi3);

                Double dist4 = getDistance(stations.get(3), locCart);
                Double rssi4 = getRSSI(dist4, 3);
                rs4.add(rssi4);
            }
        }

        //get times
        ArrayList<BigInteger> times = generateTimes();

        // get tags
        tags = new ArrayList<>();
        Long first_tag = 44001004238L;
        for (int j = 0; j < no_of_tags; j++) {
            for (int i = 0; i < detections; i++) {
                tags.add(first_tag);
            }
            first_tag += 1;
        }
        
        // set tag variables in primer class
        primer.setTRVals(times, tags, rs1);
        primer.setTRVals(times, tags, rs2);
        primer.setTRVals(times, tags, rs3);
        primer.setTRVals(times, tags, rs4);

        // get tag distances with primer and rssiequation
        RssiEquation req = new RssiEquation();
        primer.idDistances = req.getTagDistance(primer.idRSSIs, primer.measuredPower);
        
        // get the tag registry
        HashMap<Long, HashMap<BigInteger, Double[]>> registry = MLAT();
        order_times_coords();
        return registry;
    }
    
    /**
     * Method creates a registry of all tags and their appropriate distances to 
     * all radios at each ones' detection times and then proceeds to 
     * multilaterate these values.
     * @return tag_registry - the registry of all tags, times and multilaterated
     *                        coordinates
     */
    public static HashMap<Long, HashMap<BigInteger, Double[]>> MLAT() {
        // PART 1 : distance registry creation
        
        // the times each tag was picked up, passed to applyMLAT()
        HashMap<Long, ArrayList<BigInteger>> tag_detect_times = new HashMap<>();
        
        // holds all tags across all radios and their (time, coord) values
        tag_registry = new HashMap<>();
        
        // used for initialising each element of tag_registry, i.e. a hashmap,
        // with the times each one was picked up as keys
        HashMap<BigInteger, Double[]> init_inner_tag_reg;

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
                    inner.get(time)[1], inner.get(time)[2]};
                aux_arr = aux_coord;
                // put it back
                init_inner_tag_reg.put(time, aux_arr);
                tag_registry.put(key, init_inner_tag_reg);
            }
        }
        //order_times_coords();
        return tag_registry;
    }

    /**
     * This function can loop through all tags which we have in tag_detect_times.
     * Gets all the times from tag_detect_times and loops through all radios, 
     * looking for detections around that time. If detection exists, ok, put 
     * forward for mlat. If not, ok, make sure mlat doesn't consider the current
     * radio by adding a distance of -999,999 to the list.
     * @param tag_detect_times - the times of detection for each tag
     * @return hm - holds (tag, time, coords) instances
     */
    public static HashMap<Long, HashMap<BigInteger, Double[]>> applyMLAT(
            HashMap<Long, ArrayList<BigInteger>> tag_detect_times) {
        // initialise the hashmap we're going to return
        HashMap<Long, HashMap<BigInteger, Double[]>> hm = new HashMap<>();

        for (Long key : tag_detect_times.keySet()) {
            int count = 0;
            ArrayList<BigInteger> times = tag_detect_times.get(key);
            HashMap time_coords_map = new HashMap();
            for (BigInteger time : times) {
                ArrayList<Double> distances = new ArrayList<>();
                BigInteger three = new BigInteger("3");
                BigInteger four = new BigInteger("4");
                BigInteger one = new BigInteger("1");
                for (int i = 0; i < primer.no_of_radios; i++) {
                    // get the distance between the ith radio and the key tag at
                    // the time time
                    for (BigInteger time_margin = time.subtract(three); 
                                    time_margin.compareTo(time.add(four)) == -1;
                                    time_margin.add(one)) {
                        try {
                            distances.add(primer.idDistances.get(i).get(key).
                                    get(time));
                            break;
                        } catch (NullPointerException e) {
                            if (time_margin.compareTo(time.add(three)) == 0) {
                                //eliminate this radio
                                distances.add(-999.999);
                            }
                        }
                    }
                }
                MLATEquation eq = new MLATEquation(distances.size(),
                        primer.getRadiosCoordinates(),
                        distances);
                // check that we have enough valid distances
                boolean x = eq.fix();
                if (x) {
                    // we have enough
                    Matrix A = eq.getA();
                    Matrix B = eq.getB();
                    Matrix sol = A.solve(B);
                    Double[] coords = new Double[]{sol.get(1, 0), 
                                                   sol.get(2, 0) * (-1),
                                                   sol.get(3, 0) * (-1)};
                    time_coords_map.put(time, coords);
                    hm.put(key, time_coords_map);
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
    /**
     * Getter for tag_registry
     * @return tag_registry - the full list of tags, detection times and their 
     * corresponding coordinates
     */
    
    public static HashMap<Long, HashMap<BigInteger, Double[]>> getLocations() {
        return tag_registry;
    }

    /**
     * Method orders the elements in tag_registry by chronological measures.
     * @return all_coords : sorted coordinates corresponding to sorted times in 
     *                      all_times
     */
    public static ArrayList<ArrayList<Double[]>> order_times_coords() {
        HashMap<Long, HashMap<BigInteger, Double[]>> tgr = getLocations();
        for (Map.Entry<Long, HashMap<BigInteger, Double[]>> entry : tgr.entrySet()) {
            Long key = entry.getKey();
            HashMap<BigInteger, Double[]> value = entry.getValue();
            all_tags.add(key);
            ArrayList<BigInteger> times = new ArrayList<>();
            ArrayList<Double[]> coords = new ArrayList<>();
            for (Map.Entry<BigInteger, Double[]> entry2 : value.entrySet()) {
                BigInteger key2 = entry2.getKey();
                times.add(key2);
                Double[] value2 = entry2.getValue();
                coords.add(value2);
            }
            // do sorting here on times and coords
            ArrayList<BigInteger> times_orig = times;
            times_orig = makeDeepCopyBigInteger(times);

            ArrayList<Double[]> sorted_coords = new ArrayList<>();
            Collections.sort(times);
            for (int i = 0; i < times.size(); i++) {
                for (int j = 0; j < times.size(); j++) {
                    if (times.get(i) == times_orig.get(j)) {
                        sorted_coords.add(i, coords.get(j));
                        break;
                    }
                }
            }
            all_times.add(times);
            all_coords.add(sorted_coords);
        }
        return all_coords;

    }
    /**
     * Method makes deep copy for BigInteger type.
     * @param times - array to be copied
     * @return copy - copied array
     */
    public static ArrayList<BigInteger> makeDeepCopyBigInteger(ArrayList<BigInteger> times) {
        ArrayList<BigInteger> copy = new ArrayList<>(times.size());
        for (BigInteger i : times) {
            BigInteger a = i.add(BigInteger.ZERO);
            copy.add(a);
        }
        return copy;
    }
}
