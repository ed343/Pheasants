
package Multilateration;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
    
    //Constructor can be used for both simulation + real data.
    //RSSIs must be in -dBM
    //Set noise, filter and gran on and off, gC is granularity constant.
    public LogData(String fp,int filter, int gran, int gC, int sim) {
        //sim input determines simulated data/real data
        //Set file path.
        this.FilePath = fp;
        //Automatically extract data from log file when object is created.
        if(sim==1){
            this.extractSimData(fp);
            if(gran==1) {
                if(filter==1) {
                    introduceNoise();
                    filterRSSIs();
                    granularise(this.Times,this.filtRSSIs,this.IDs,gC);
                } else
                    granularise(this.Times,this.RSSIs,this.IDs,gC);
            } else if(filter==1) {
                introduceNoise();
                filterRSSIs(); 
            }
        }
        else {
            this.extractData(fp);
            if(gran==1) {
                if(filter==1) {
                    filterRSSIs();
                    granularise(this.Times,this.filtRSSIs,this.IDs,gC);
                } else
                    granularise(this.Times,this.RSSIs,this.IDs,gC);
            } else if(filter==1) {

                filterRSSIs(); 
            }
        }
    }
    
    
    
    public LogData(String fp) {
        //Set file path.
        this.FilePath = fp;
        //Automatically extract data from log file when object is created.
        this.extractData(fp); 
        //normaliseRSSIs(-30,-70,"no");
    }
    
    public LogData(String fp, int test) {
        this.FilePath = fp;
        this.extractData(fp);
        normaliseRSSIs(-30, -70, "no");
    }
    
    
    public LogData(ArrayList<BigInteger> times, ArrayList<Long> ids, ArrayList<Double> rssis, int filter, int gran, int gC )  {
        this.Times = times;
        this.IDs = ids;
        this.RSSIs = rssis;
        if(gran==1) {
            if(filter==1) {
                introduceNoise();
                filterRSSIs();
                granularise(this.Times,this.filtRSSIs,this.IDs,gC);
            } else
                granularise(this.Times,this.RSSIs,this.IDs,gC);
        } else if(filter==1)
            introduceNoise();
            filterRSSIs(); 
    }
    
    
    /*
    *  Function to convert a date/time string into a number.
    *
    *
    *  INPUT:
    *
    *  dt: The date/time string the be converted.
    *
    *  OUTPUT:
    *
    *  The number corresponding to the converted date/time string.
    *
    */
    public BigInteger getDT(String dt) {
        String year = dt.substring(0,4);
        String month = dt.substring(5,7);
        String day = dt.substring(8,10);
        String hour = dt.substring(11,13);
        String minute = dt.substring(14,16);
        String second = dt.substring(17,19);
        String fullString = year+month+day+hour+minute+second;
        BigInteger flatDT = new BigInteger(fullString);
        
        return flatDT;
    }
    
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
        List idArray =new ArrayList(hm.keySet());
        Collections.sort(idArray);
        Set<Long> inpIDs = hm.keySet();
        // Get array of all tag IDs
        
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
    

    
    // Ensures times are consistent.
    public BigInteger correctT(BigInteger currentTime) {
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
    
    public void extractSimData(String filepath) {
        //The path of the log file to be used.
        String fileName = filepath;

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
                int sze = line.length();
                String time = line.substring(5, 19);
                System.out.println(time);
                BigInteger tm = new BigInteger(time);
                this.Times.add(tm);
                String ID = line.substring(24,34);
                System.out.println(ID);
                this.IDs.add(Long.parseLong(ID));
                String RSSI = line.substring(41,sze-1);
                System.out.println(RSSI);
                this.RSSIs.add(Double.parseDouble(RSSI));
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
                //Check for valid lines.
    }
    
    /*
    *  Function to extract the data held in a log file.
    *
    *
    *  INPUT:
    *
    *  filePath: String containing the path of the log file to be processed.
    *
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
    
    // Function to normalise filtered RSSIs to range (x,y).
    public void normaliseRSSIs(double x, double y,String f) {
        ArrayList<Double> rssis = new ArrayList<>();
        if(f=="filter")
            rssis = this.filtRSSIs;
        else
            rssis = this.RSSIs;
        x = -x;
        y = -y;
        double oldRange = 250-0;
        double newRange = y-x;
        double hWay = x+(y-x)/2;
        for(int i=0;i<rssis.size();i++) {
            double oldVal = rssis.get(i);
            double newVal = (((oldVal-0)*newRange)/oldRange)+x;
            double diff = hWay - newVal;
            double normVal = hWay+diff;
            this.normRSSIs.add(-normVal);
            
            
        }
        
               
    }
    
    public void introduceNoise() {
        for(int i=0;i<this.RSSIs.size();i++) {
            double rssi = this.RSSIs.get(i);

            // Add random noise, gaussian distributed with m = 2 and var = 2
            java.util.Random r2 = new java.util.Random();
            double measurementNoise = r2.nextGaussian() * Math.sqrt(0.5) + 0.5;
            rssi -= measurementNoise;
            this.RSSIs.set(i, rssi);
            }
    }
    
    
    //Function that applies 
    public void filterRSSIs() {
        double filtRSSI = 0;
        RandomGenerator rand = new JDKRandomGenerator(10);
        for(int j=0; j<this.RSSIs.size(); j++) {
            double rssiMeasurement = this.RSSIs.get(j);
            java.util.Random r2 = new java.util.Random();
            double measurementNoise = 1;
            double processNoise = 1e-5d;
            RealMatrix A = new Array2DRowRealMatrix(new double[] { 1d });
            RealMatrix B = null;
            RealMatrix H = new Array2DRowRealMatrix(new double[] { 1d });
            RealVector x = new ArrayRealVector(new double[] { rssiMeasurement });
            RealMatrix Q = new Array2DRowRealMatrix(new double[] { processNoise });
            RealMatrix P0 = new Array2DRowRealMatrix(new double[] { 1d });
            RealMatrix R = new Array2DRowRealMatrix(new double[] { measurementNoise });
            
            ProcessModel pm = new DefaultProcessModel(A, B, Q, x, P0);
            MeasurementModel mm = new DefaultMeasurementModel(H, R);
            KalmanFilter filter = new KalmanFilter(pm, mm);  
            RealVector pNoise = new ArrayRealVector(1);
            RealVector mNoise = new ArrayRealVector(1);
            for (int i = 0; i < 10; i++) {
                filter.predict();
                
                pNoise.setEntry(0, processNoise * rand.nextGaussian());
                
                x = A.operate(x).add(pNoise);
                
                mNoise.setEntry(0, measurementNoise * rand.nextGaussian());
                
                RealVector z = H.operate(x).add(mNoise);
                
                filter.correct(z);
                filtRSSI = filter.getStateEstimation()[0];
            }
            
            this.filtRSSIs.add(filtRSSI);
        }
    
}

    
    public String getFilePath() {
        return this.FilePath;
    }
    
     public ArrayList<BigInteger> getTimes() {
        return this.Times;
    }
    
     public ArrayList<Long> getIDs() {
        return this.IDs;
    }
    
     public ArrayList<Double> getFrequencies() {
        return this.Frequencies;
    }
    
     public ArrayList<Double> getTBuffers() {
        return this.TBuffers;
    }

     public ArrayList<Double> getTDets() {
        return this.TDets;
    }

     public ArrayList<Double> getSDets() {
        return this.SDets;
    }
    
     public ArrayList<Double> getRSSIs() {
        return this.RSSIs;
    }
    
     public ArrayList<Double> getSNRs() {
        return this.SNRs;
    }
    
     public ArrayList<Double> getESNRS() {
        return this.Euclid_SNRs;
    }
    
     public ArrayList<Double> getHeadrooms() {
        return this.Headrooms;
    }

     public ArrayList<Double> getGains() {
        return this.Gains;
    }
    
     public ArrayList<Double> getNormRSSIs() {
        return this.normRSSIs;
    }
    
}