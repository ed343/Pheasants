/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Multilateration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import javafx.util.Pair;

/**
 *
 * @author James
 */
public class BuildSim {
    
    public static void main(String[] args) {
        MLATSim mSim = new MLATSim();
        HashMap<Long, HashMap<Long, Double[]>> hm = mSim.runMLAT();
        

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



