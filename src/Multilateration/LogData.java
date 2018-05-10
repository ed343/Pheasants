package Multilateration;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import javafx.util.Pair;
import org.apache.commons.math3.filter.DefaultMeasurementModel;
import org.apache.commons.math3.filter.DefaultProcessModel;
import org.apache.commons.math3.filter.KalmanFilter;
import org.apache.commons.math3.filter.MeasurementModel;
import org.apache.commons.math3.filter.ProcessModel;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;

/**
 *
 * @author James
 * 
 * Class to extract data from log files, to be used in
 * location estimation.
 */
public class LogData {
    String FilePath;
    ArrayList<BigInteger> Times= new ArrayList<>();
    ArrayList<Long> IDs= new ArrayList<>();
    ArrayList<Double> Frequencies= new ArrayList<>();
    ArrayList<Double> TBuffers= new ArrayList<>();
    ArrayList<Double> TDets= new ArrayList<>();
    ArrayList<Double> SDets= new ArrayList<>();
    ArrayList<Double> RSSIs= new ArrayList<>();
    ArrayList<Double> normRSSIs= new ArrayList<>();
    ArrayList<Double> SNRs= new ArrayList<>();
    ArrayList<Double> Euclid_SNRs= new ArrayList<>();
    ArrayList<Double> Headrooms= new ArrayList<>();
    ArrayList<Double> Gains= new ArrayList<>();
    ArrayList<Double> filtRSSIs= new ArrayList<>();
    

    /**
     * Main constructor: use this constructor for real log files.
     * @param fp    : The full file path to the log file from which data will 
     *                be extracted.
     * 
     * @param filter: Determines whether or not filtering is applied. 
     * 
     * @param gran  : Determines whether or not granularity is applied.
     *              
     * @param gC    : Granularity constant - number of seconds to average
     *                over.
     */
    public LogData(String fp, boolean filter, boolean gran, int gC) {
        //Set file path.
        this.FilePath = fp;
        //Automatically extract data from log file when object is created.
        this.extractData(fp);
        
        if(this.RSSIs.get(0)>=0)
            //Normalise RSSIs if they are +ve.
            normaliseRSSIs(-30,-70);
        if(gran) {
            if(filter) {
                //Filter and granularise.
                filterRSSIs();
                granularise(this.Times,this.RSSIs,this.IDs,gC);
            } else
                //Granularise only.
                granularise(this.Times,this.RSSIs,this.IDs,gC);
        } else if(filter) {
            //Filter only.
            filterRSSIs(); 
        }
        
    }
    
    /**
     * Function to convert date/time string into number.
     * 
     * @param dt: The input string.
     * 
     * @return  : The date/time number in the form of a BigInteger object.
     */
    public BigInteger getDT(String dt) {
        //Get constituent parts
        String year = dt.substring(0,4);
        String month = dt.substring(5,7);
        String day = dt.substring(8,10);
        String hour = dt.substring(11,13);
        String minute = dt.substring(14,16);
        String second = dt.substring(17,19);
        //Formulate full number string.
        String fullString = year+month+day+hour+minute+second;
        //Create new BigInteger with number string.
        BigInteger flatDT = new BigInteger(fullString);
        
        return flatDT;
    }
    
    /**
     * Function used to apply granularity to RSSI data.
     * 
     * @param times: Array of times.
     * 
     * @param rssis: Array of RSSIs.
     * 
     * @param IDs  : Array of IDs.
     * 
     * @param gran : Granularity constant - number of seconds to average
     *               over.
     */
    public void granularise(ArrayList<BigInteger> times, ArrayList<Double> rssis, ArrayList<Long> IDs, int gran) {
        //New primer
        PrimerClass primer = new PrimerClass();
        //Set up primer
        primer.setTRVals(times, IDs, rssis);
        ArrayList<HashMap<Long, ArrayList<Pair<BigInteger,Double>>>> hmArr = primer.idRSSIs;
        HashMap<Long, ArrayList<Pair<BigInteger,Double>>> hmRet = new HashMap<>();
        ArrayList<Pair<BigInteger,Double>> pairs = new ArrayList<>();
        // Get hash map using PrimerClass object.
        HashMap<Long, ArrayList<Pair<BigInteger,Double>>> hm = hmArr.get(0);
        // Get array of all tag IDs
        List idArray =new ArrayList(hm.keySet());
        Collections.sort(idArray);
        Set<Long> inpIDs = hm.keySet();     
        // Iterate over tags
        for(Object id : idArray) {
            ArrayList<Pair<BigInteger,Double>> tRs = hm.get(id);
            ArrayList<BigInteger> ts = new ArrayList<>();
            ArrayList<Double> rs = new ArrayList<>();
            // Get times and rssis for this tag.
            for(int i=0;i<tRs.size();i++) {
                ts.add(tRs.get(i).getKey());
                rs.add(tRs.get(i).getValue());
            }
            // Set current time to first detection time.
            BigInteger currentTime = ts.get(0);
            // Set next time to be gran seconds in the future.
            BigInteger nextTime = currentTime.add(BigInteger.valueOf(gran));
            // Ensure times match with seconds, minutes etc.
            nextTime = correctT(nextTime);
            // Array to hold blocks of rssi values.
            ArrayList<ArrayList<Double>> blocks = new ArrayList<>();
            int blocknum = 1;
            ArrayList<Double> rsvals = new ArrayList<>();
            // Add first rssi val to array.
            rsvals.add(rs.get(0));
            if(rs.size()==1) {
                blocks.add(rsvals);
            }
            // Separate rssis into blocks, each corresponding to gran seconds of detections.
            for(int i=1;i<rs.size();i++) {
                if(ts.get(i).compareTo(nextTime)==1) {
                    blocks.add(rsvals);
                    rsvals = new ArrayList<>();
                    currentTime = ts.get(i);
                    nextTime = currentTime.add(BigInteger.valueOf(gran));
                    nextTime = correctT(nextTime);
                }
                if(ts.get(i).compareTo(nextTime)==-1||ts.get(i).compareTo(nextTime)==0) {
                    rsvals.add(rs.get(i));
                    if(i==rs.size()-1)
                        blocks.add(rsvals);
                } else {
                    blocks.add(rsvals);
                    rsvals = new ArrayList<>();
                    blocknum += 1;
                    currentTime = nextTime;
                    nextTime = currentTime.add(BigInteger.valueOf(gran));
                    nextTime = correctT(nextTime);
                }
            }
            ArrayList<Double> avArr = new ArrayList<>();
            // Calculate average values over blocks and add to array for each detection.
            for(ArrayList<Double> block : blocks) {
                double sum =0;
                ArrayList<Double> avBlock = new ArrayList<>();
                for(int i=0;i<block.size();i++) {
                    sum+=block.get(i);
                }
                double avRssi = sum/block.size();
                for(int i=0;i<block.size();i++) {
                    avArr.add(avRssi);
                }
            }
            // Create new pairs of time, averageRSSI, and add to hash map.
             
             for(int i=0;i<avArr.size();i++) {
                 Pair<BigInteger,Double> pair = new Pair(ts.get(i),avArr.get(i));
                 pairs.add(pair);
             }
            
             
            }
        // Flatten values so RSSIs can be updates correctly.
        ArrayList<Integer> inds = new ArrayList<>();
        ArrayList<Double> grs = new ArrayList<>();
        for(int i=0;i<pairs.size();i++) {
            BigInteger time = pairs.get(i).getKey();
            double r = pairs.get(i).getValue();
            if(!inds.contains(i)) {
                grs.add(r);
                inds.add(i);
            }
            for(int j=0;j<pairs.size();j++) {
                double r2 = pairs.get(j).getValue();
                if(!inds.contains(j)) {
                    if(pairs.get(j).getKey().compareTo(time)==0) {
                        grs.add(r2);
                        inds.add(j);
                    }
                }
            }
            
        }
        this.RSSIs = grs;
    }
    

    /**
     * Function to ensure the correctness of times.
     * 
     * @param currentTime: The input time.
     * 
     * @return           : The corrected time.
     */
    public BigInteger correctT(BigInteger currentTime) {
        // Ensure times are correct in relation to minutes.
        int check1 = currentTime.mod(BigInteger.valueOf(100)).compareTo(BigInteger.valueOf(60));
        if(check1==1 ||check1 ==0)  {
            currentTime = currentTime.add(BigInteger.valueOf(40));
        }
        // Ensure times are correct in relation to hours.
        int check2 = currentTime.mod(BigInteger.valueOf(10000)).compareTo(BigInteger.valueOf(6000));
        if(check2==1 || check2==0) {
            currentTime = currentTime.add(BigInteger.valueOf(4000));
        }
        return currentTime;
    }

    /**
     * Function to extract the data held in a log file.
     * 
     * @param filePath : The full file path to the log file from which data will 
     *                   be extracted.
     */
    public final void extractData(String filePath) {
        //The path of the log file to be used.
        String fileName = filePath;

        String line = null;

        try {
            
            //Open the file
            FileReader fileReader = 
                new FileReader(fileName);

            //Read the file
            BufferedReader bufferedReader = 
                new BufferedReader(fileReader);
            
            //Iterate over lines in file.
            while((line = bufferedReader.readLine()) != null) {
                //Check for valid lines.
                if ((line.toLowerCase().contains("rssi"))&&!(line.toLowerCase().contains("-infinity"))) {
                    //Times
                    //Extract date/time string from log.
                    String time = line.substring(0, 19);
                    //Convert date/time string to number.
                    BigInteger flatTime = getDT(time);
                    //Add flattened times to Times arraylist.
                    this.Times.add(flatTime);
                    //IDs
                    //Find the position of ID in line.
                    int iDind = line.indexOf("[4400");
                    //Extract the ID string from the log.
                    String iD = line.substring((iDind+8), (iDind+12));
                    //Convert the ID string to an integer.
                    long iDInt = Long.parseLong(iD);
                    //Add ID to ArrayList.
                    this.IDs.add(iDInt);
                    //Frequencies
                    //Find the position of frequency in line.
                    int fInd = line.indexOf("f=");
                    //Find the postion of Tbuffer in line.
                    int TbInd = line.indexOf(";Tbuffer");
                    //Extract the frequency string from the log.
                    String freq = line.substring((fInd+2),(TbInd));
                    //Convert the frequency string to a double.
                    double freqDb = Double.parseDouble(freq);
                    //Add frequency to ArrayList
                    this.Frequencies.add(freqDb);
                    
                    //All remaining data items are extracted in a similar 
                    //fashion to that above.
                    
                    //Tbuffers
                    int TdInd = line.indexOf(";Tdet");
                    String Tbuff = line.substring((TbInd+12),(TdInd));
                    double tBuffDb = Double.parseDouble(Tbuff);
                    this.TBuffers.add(tBuffDb);
                    //TDets
                    int SdInd = line.indexOf(";Sdet");
                    String Tdet = line.substring((TdInd+6),(SdInd));
                    double tDetDb = Double.parseDouble(Tdet);
                    this.TDets.add(tDetDb);
                    //SDets
                    int rssiInd = line.indexOf(";rssi");
                    String Sdet = line.substring((SdInd+6),(rssiInd));
                    double sDetDb = Double.parseDouble(Sdet);
                    this.SDets.add(sDetDb);
                    //RSSIs
                    int snrInd = line.indexOf(";snr");
                    String RSSI = line.substring((rssiInd+6),(snrInd));
                    double RSSIDb = Double.parseDouble(RSSI);
                    this.RSSIs.add(RSSIDb);
                    //SNRs
                    int EsnrInd = line.indexOf(";euclidean_snr");
                    String SNR = line.substring((snrInd+5),(EsnrInd));
                    double SNRDb = Double.parseDouble(SNR);
                    this.SNRs.add(SNRDb);
                    //ESNRs
                    int hRoomInd = line.indexOf(";headroom");
                    String ESNR = line.substring((EsnrInd+15),(hRoomInd));
                    double ESNRDb = Double.parseDouble(ESNR);
                    this.Euclid_SNRs.add(ESNRDb);
                    //Headrooms            
                    int gainInd = line.indexOf(";gain");
                    String headroom = line.substring((hRoomInd+10),(gainInd));
                    double hRoomDb = Double.parseDouble(headroom);
                    this.Headrooms.add(hRoomDb); 
                    //Gains
                    int valInd = line.indexOf(";value");
                    String gain = line.substring((gainInd+6),(valInd));
                    double gainDb = Double.parseDouble(gain);
                    this.Gains.add(gainDb);   
                }
            }   
            
            //Close the file
            bufferedReader.close();         
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                "Unable to open file '" + 
                fileName + "'");                
        }
        catch(IOException ex) {
            System.out.println(
                "Error reading file '" 
                + fileName + "'");                  
        }
    }
    
    
    /**
     * Function to normalise filtered RSSIs to range (x,y).
     * 
     * @param x: The high end of the new range.
     * 
     * @param y: The low end of the new range.
     */
    public void normaliseRSSIs(double x, double y) {
        ArrayList<Double> rssis = new ArrayList<>();
        rssis = this.RSSIs;
        // Negate inputs, as they will be negative.
        x = -x;
        y = -y;
        // The size of the old range.
        double oldRange = 120;
        // The required range.
        double newRange = y-x;
        // Halfway point of new range.
        double hWay = x+(y-x)/2;
        //Iterate over all RSSI values.
        for(int i=0;i<rssis.size();i++) {
            // Extract RSSI value.
            double oldVal = rssis.get(i);
            // Compute the new value;
            double newVal = (((oldVal-130)*newRange)/oldRange)+x;
            double diff = hWay - newVal;
            double normVal = hWay+diff;
            // Set this RSSI value to be the negation of the new value.
            // This reverses the negation of the input.
            this.RSSIs.set(i,-normVal);
            
            
        }
        
               
    }
    
    /**
     * Function to introduce noise to simulation data.
     */
    public void introduceNoise() {
        for(int i=0;i<this.RSSIs.size();i++) {
            double rssi = this.RSSIs.get(i);

            // Add random noise, gaussian distributed with m = 0.5 and var = 0.5
            java.util.Random r2 = new java.util.Random();
            double measurementNoise = r2.nextGaussian() * Math.sqrt(0.5) + 0.5;
            rssi -= measurementNoise;
            // Replace existing RSSI value with noisy value.
            this.RSSIs.set(i, rssi);
            }
    }
    
    
    /**
     * Function to apply filtering to the RSSI data.
     */
    public void filterRSSIs() {
        double filtRSSI = 0;
        //Create new random generator with static seed.
        RandomGenerator rand = new JDKRandomGenerator(10);
        for(int j=0; j<this.RSSIs.size(); j++) {
            // Extract current RSSI value.
            double rssiMeasurement = this.RSSIs.get(j);
            // Set measurement noise max value.
            double measurementNoise = 1;
            // Set process noise max value.
            double processNoise = 1e-5d;
            // Define Kalman matrices.
            RealMatrix A = new Array2DRowRealMatrix(new double[] { 1d });
            RealMatrix B = null;
            RealMatrix H = new Array2DRowRealMatrix(new double[] { 1d });
            RealVector x = new ArrayRealVector(new double[] { rssiMeasurement });
            RealMatrix Q = new Array2DRowRealMatrix(new double[] { processNoise });
            RealMatrix P0 = new Array2DRowRealMatrix(new double[] { 1d });
            RealMatrix R = new Array2DRowRealMatrix(new double[] { measurementNoise });
            
            // New process model and measurement model.
            ProcessModel pm = new DefaultProcessModel(A, B, Q, x, P0);
            MeasurementModel mm = new DefaultMeasurementModel(H, R);
            // Use process model and measurement model to initialise filter.
            KalmanFilter filter = new KalmanFilter(pm, mm);  
            // Vectors for process noise and measurement noise.
            RealVector pNoise = new ArrayRealVector(1);            
            RealVector mNoise = new ArrayRealVector(1);
            // Run predict/update cycle for 10 iterations.
            for (int i = 0; i < 10; i++) {
                filter.predict();
                
                pNoise.setEntry(0, processNoise * rand.nextGaussian());
                
                x = A.operate(x).add(pNoise);
                
                mNoise.setEntry(0, measurementNoise * rand.nextGaussian());
                
                RealVector z = H.operate(x).add(mNoise);
                
                filter.correct(z);
                filtRSSI = filter.getStateEstimation()[0];
            }
            
            // Replace previous RSSI value with filtered one.
            this.RSSIs.set(j,filtRSSI);
        }
    
}

    /**
     * Function to retrieve file path.
     * 
     * @return file path.
     */
    public String getFilePath() {
        return this.FilePath;
    }
    
    /**
     * Function to retrieve times array.
     * 
     * @return Times array.
     */
    public ArrayList<BigInteger> getTimes() {
        return this.Times;
    }
    
    /**
     * Function to retrieve IDs array.
     * 
     * @return IDs array.
     */
    public ArrayList<Long> getIDs() {
        return this.IDs;
    }
    
    /**
     * Function to retrieve frequencies array.
     * 
     * @return Frequencies array.
     */
    public ArrayList<Double> getFrequencies() {
        return this.Frequencies;
    }
    
    /**
     * Function to retrieve Tbuffers array.
     * 
     * @return Tbuffers array.
     */
    public ArrayList<Double> getTBuffers() {
        return this.TBuffers;
    }

    /**
     * Function to retrieve TDets array.
     * 
     * @return TDets array.
     */
    public ArrayList<Double> getTDets() {
        return this.TDets;
    }

    /**
     * Function to retrieve SDets array
     * 
     * @return SDets array.
     */
    public ArrayList<Double> getSDets() {
        return this.SDets;
    }
    
    /**
     * Function to retrieve RSSIs array.
     * 
     * @return RSSIs array
     */
    public ArrayList<Double> getRSSIs() {
        return this.RSSIs;
    }
    
    /**
     * Function to retrieve SNRs array.
     * 
     * @return SNRs array
     */
    public ArrayList<Double> getSNRs() {
        return this.SNRs;
    }
    
    /**
     * Function to retrieve ESNRs array.
     * @return ESNRs array.
     */
    public ArrayList<Double> getESNRS() {
        return this.Euclid_SNRs;
    }
    
    /**
     * Function to retrieve Headrooms array.
     * 
     * @return Headrooms array.
     */
    public ArrayList<Double> getHeadrooms() {
        return this.Headrooms;
    }

    /**
     * Function to retrieve Gains array.
     * 
     * @return Gains array.
     */
    public ArrayList<Double> getGains() {
        return this.Gains;
    }
    
    
}

