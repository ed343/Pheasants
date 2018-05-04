
package Multilateration;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
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
    
    //Constructor
    LogData(String fp) {
        //Set file path.
        this.FilePath = fp;
        //Automatically extract data from log file when object is created.
        this.extractData(fp);
        introduceNoise();
        filterRSSIs();
    }
    
    LogData(String fp, int test) {
        this.FilePath = fp;
        this.extractData(fp);
        normaliseRSSIs(-30, -70, "no");
    }
    
    
    LogData(ArrayList<BigInteger> times, ArrayList<Long> ids, ArrayList<Double> rssis, ArrayList<Double> snrs)  {
        this.Times = times;
        this.IDs = ids;
        this.RSSIs = rssis;
        this.SNRs = snrs;
        filterRSSIs();
        normaliseRSSIs(-30, -70,"filter");
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
    BigInteger getDT(String dt) {
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
    
    
    
    /*
    *  Function to extract the data held in a log file.
    *
    *
    *  INPUT:
    *
    *  filePath: String containing the path of the log file to be processed.
    *
    */
    final void extractData(String filePath) {
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
    void normaliseRSSIs(double x, double y,String f) {
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
    
    void introduceNoise() {
        for(int i=0;i<this.RSSIs.size();i++) {
            double rssi = this.RSSIs.get(i);

            // Add random noise, gaussian distributed with m = 2 and var = 2
            java.util.Random r2 = new java.util.Random();
            double measurementNoise = r2.nextGaussian() * Math.sqrt(4) + 4;
            rssi -= measurementNoise;
            this.RSSIs.set(i, rssi);
            }
    }
    
    
    //Function that applies 
    void filterRSSIs() {
        double filtRSSI = 0;
        RandomGenerator rand = new JDKRandomGenerator(10);
        for(int j=0; j<this.RSSIs.size(); j++) {
            double rssiMeasurement = this.RSSIs.get(j);
            java.util.Random r2 = new java.util.Random();
            double measurementNoise = 10;//r2.nextGaussian() * Math.sqrt(4) + 4;
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

    
    String getFilePath() {
        return this.FilePath;
    }
    
    ArrayList<BigInteger> getTimes() {
        return this.Times;
    }
    
    ArrayList<Long> getIDs() {
        return this.IDs;
    }
    
    ArrayList<Double> getFrequencies() {
        return this.Frequencies;
    }
    
    ArrayList<Double> getTBuffers() {
        return this.TBuffers;
    }

    ArrayList<Double> getTDets() {
        return this.TDets;
    }

    ArrayList<Double> getSDets() {
        return this.SDets;
    }
    
    ArrayList<Double> getRSSIs() {
        return this.RSSIs;
    }
    
    ArrayList<Double> getSNRs() {
        return this.SNRs;
    }
    
    ArrayList<Double> getESNRS() {
        return this.Euclid_SNRs;
    }
    
    ArrayList<Double> getHeadrooms() {
        return this.Headrooms;
    }

    ArrayList<Double> getGains() {
        return this.Gains;
    }
    
    ArrayList<Double> getNormRSSIs() {
        return this.normRSSIs;
    }
    
}
