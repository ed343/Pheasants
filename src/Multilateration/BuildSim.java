/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Multilateration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.util.Pair;
import org.apache.commons.math3.distribution.*;

/**
 *
 * @author James
 */
public class BuildSim {
    
    public static void main(String[] args) {
        //MLATSim mSim = new MLATSim();
        //HashMap<Long, HashMap<Long, Double[]>> hm = mSim.runMLAT();
        ArrayList<Long> t = gTPoisson(120,1104165200,3,1,6);
        

    }
    
    static ArrayList<HashMap<Long, HashMap<Long, Double>>> retrieveDistances(LogData beacon,int x,int y,int z, int measuredPower) {
        PrimerClass primer = new PrimerClass();
        primer.setRadioCoordinates(x, y, z);
        primer.setTRVals(beacon.getTimes(),beacon.getIDs(),beacon.getNormRSSIs());
        primer.setRadioMeasuredPower(measuredPower);
        ArrayList<HashMap<Long,ArrayList<Pair<Long, Double>>>> rssi = primer.idRSSIs;
        RssiEquation re = new RssiEquation();
        ArrayList<HashMap<Long,HashMap<Long,Double>>> dist = re.getTagDistance(rssi, primer.measuredPower);
        return dist;
    }
    
    static ArrayList<Long> gTPoisson(int iters, long startTime,int mean, int start, int end) {
        // Create times array
        ArrayList<Long> times = new ArrayList<>();
        // Add the start time to the array
        times.add(startTime);
        long currentTime = startTime;
        // Create a list of integers, ranging from start(INPUT 4) to end(INPUT 5) inclusive.
        List<Integer> range = IntStream.rangeClosed(start, end).boxed().collect(Collectors.toList());
        // Array to hold probabilities for all integers in range.
        ArrayList<Double> rProbs = new ArrayList<>();
        // Create a poisson distribution with mean of mean(INPUT 3)
        PoissonDistribution pdist = new PoissonDistribution(mean);
        double cp =0;
        for(int i=0;i<range.size();i++) {
            // Calculate probabilities for all integers in range and add to array.
            double prob = pdist.cumulativeProbability(range.get(i));
            rProbs.add(prob);
            // Calculate the cumulative probabilty for members of range.
            cp += prob;
        }
        
        for(int i=0;i<iters;i++) {
            // Generate a random double between 0 and cp.
            double p = Math.random() * cp;
            double cumulativeProbability = 0.0;
            long t2add = 0;
            // Psuedorandomly determine the next inter-detection time.
            for (int j=0;j<rProbs.size();j++) {
                cumulativeProbability += rProbs.get(j);
                if (p <= cumulativeProbability) {
                    t2add = range.get(j);
                    break;
                }    
            }
            // Add inter-detection time to current time.
            currentTime += t2add;
            // Ensure times are correct in relation to minutes.
            long minutes = currentTime % 100;
            if(minutes>=60) {
                currentTime += 40;
            }
            // Ensure times are correct in relation to hours.
            long hHold = currentTime % 10000;
            long hour = hHold - (hHold%100);
            if(hour>=6000) {
                currentTime += 4000;
            }
            // Add next time to time array.
            times.add(currentTime);
            
        }

        
        
        return times;
    }
    //Only reliable for up to 59 minutes, can extend if required.
    static ArrayList<Long> generateTimes(int minutes) {
        ArrayList<Long> times= new ArrayList<>();
        long num = 1128110000;
        times.add(num);
        for(int i=0; i<minutes; i++) {
            for(int j=0; j<14; j++) {
                num+=4;
                times.add(num);  
            }
            num+=44;
            times.add(num);
        }
        return times;
    }
    
    static ArrayList<Long> generateIDSingle(Long ID,int length) {
        ArrayList<Long> ids = new ArrayList<>();
        for(int i=0;i<length;i++) {
            ids.add(ID);
        }
        return ids;
    }


    static ArrayList<ArrayList<Double>> generateRSSIs(int length) {
        ArrayList<ArrayList<Double>> allRSSIs = new ArrayList<>();
        ArrayList<Double> aRSSI = new ArrayList<>();
        ArrayList<Double> bRSSI = new ArrayList<>();
        ArrayList<Double> cRSSI = new ArrayList<>();
        ArrayList<Double> dRSSI = new ArrayList<>();
        Random rand = new Random();
        double a = rand.nextInt(250)+0;
        double b = rand.nextInt(250)+0;
        double c1 = b-0;
        double c = 250-c1;
        double d1 = a-0;
        double d = 250-d1;
        /*
        a = -a;
        b = -b;
        c = -c;
        d = -d;
                */
        for(int i=0; i<length;i++) {
            aRSSI.add(a);
            bRSSI.add(b);
            cRSSI.add(c);
            dRSSI.add(d);
            double chance1 = rand.nextInt(100);
            double chance2 = rand.nextInt(100);
            if(chance1 <= 33) {
                a +=1;
                d -=1;
            }
            else if((33<chance1&&chance1>=66)) {
                a-=1;
                d+=1;
            }
            if(chance2 <= 33) {
                b +=1;
                c -=1;
            }
            else if((33<chance1&&chance1>=66)) {
                b-=1;
                c+=1;
            }
            
            
        }
        allRSSIs.add(aRSSI);
        allRSSIs.add(bRSSI);
        allRSSIs.add(cRSSI);
        allRSSIs.add(dRSSI);
        return allRSSIs;
    }
    
    static ArrayList<Double> genSNRs(int length) {
        ArrayList<Double> sNRs = new ArrayList<>();
        Random rand = new Random();
        for(int i=0; i<length; i++) {
            double s = rand.nextInt(50);
            sNRs.add(s);
        }
        return sNRs;
    }
    
}
