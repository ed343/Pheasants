/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Multilateration;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 *
 * @author James
 */
public class BuildSim {
    
    public static void main(String[] args) {
        
        ArrayList<Long> times = generateTimes(30);
        ArrayList<ArrayList<Double>> allRSSIs = generateRSSIs(times.size());
        long dis = 4067;
        ArrayList<Long> ids = generateIDSingle(dis,times.size());
        LogData beacon1 = new LogData(times,ids,allRSSIs.get(0));
        LogData beacon2 = new LogData(times,ids,allRSSIs.get(1));
        LogData beacon3 = new LogData(times,ids,allRSSIs.get(2));
        LogData beacon4 = new LogData(times,ids,allRSSIs.get(3));
        PrimerClass primer1 = new PrimerClass();
        PrimerClass primer2 = new PrimerClass();
        PrimerClass primer3 = new PrimerClass();
        PrimerClass primer4 = new PrimerClass();
        primer1.setRadioCoordinates(0, 0, 0);
        primer2.setRadioCoordinates(500, 0, 0);
        primer3.setRadioCoordinates(0, 500, 0);
        primer4.setRadioCoordinates(500, 500, 0);
        primer1.setRadioMeasuredPower(-84.4);
        primer2.setRadioMeasuredPower(-84.4);
        primer3.setRadioMeasuredPower(-84.4);
        primer4.setRadioMeasuredPower(-84.4);
        primer1.setTimeRssiValues(beacon1.getTimes(),beacon1.getIDs(),beacon1.getRSSIs());
        primer2.setTimeRssiValues(beacon2.getTimes(),beacon2.getIDs(),beacon2.getRSSIs());
        primer3.setTimeRssiValues(beacon3.getTimes(),beacon3.getIDs(),beacon3.getRSSIs());
        primer1.setTimeRssiValues(beacon4.getTimes(),beacon4.getIDs(),beacon4.getRSSIs());
        RssiEquation re1 = new RssiEquation();
        RssiEquation re2 = new RssiEquation();
        RssiEquation re3 = new RssiEquation();
        RssiEquation re4 = new RssiEquation();
        ArrayList<HashMap<Long, HashMap<Long, Double>>> rs1 = primer1.rssiValues;
        HashMap<Long, HashMap<Long, Double>> thisOne = rs1.get(0);
        HashMap dist1 = re1.getTagDistance(rs1.get(0), (int) -84.4);
        System.out.println(primer1.tagDistances);
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
        double a = rand.nextInt(40)+30;
        double b = rand.nextInt(40)+30;
        double c1 = b-30;
        double c = 70-c1;
        double d1 = a-30;
        double d = 70-d1;
        a = -a;
        b = -b;
        c = -c;
        d = -d;
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
}
