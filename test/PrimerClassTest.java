/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Multilateration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import javafx.util.Pair;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author James
 */
public class PrimerClassTest {
    
    public PrimerClassTest() {
    }

    /**
     * Test of setNumberOfRadios method, of class PrimerClass.
     */
    @Test
    public void testSetNumberOfRadios() {
        System.out.println("Testing setNumberOfRadios method from the PrimerClass class...");
        int n = 4;
        PrimerClass instance = new PrimerClass();
        instance.setNumberOfRadios(n);
        assertEquals(4, instance.no_of_radios);
        System.out.println("Test for setNumberOfRadios passed");
    }

    /**
     * Test of setRadioCoordinates method, of class PrimerClass.
     */
    @Test
    public void testSetRadioCoordinates() {
        System.out.println("Testing setRadioCoordinates method from the PrimerClass class...");
        PrimerClass instance = new PrimerClass();
        double x = 10;
        double y = 20;
        double z = 1;
        instance.setRadioCoordinates(x, y, z);
        double resultX = instance.getRadiosCoordinates().get(0)[0];
        assertEquals(x, resultX,0);
        double resultY = instance.getRadiosCoordinates().get(0)[1];
        assertEquals(y, resultY,0);
        double resultZ = instance.getRadiosCoordinates().get(0)[2];
        assertEquals(z, resultZ,0);
        System.out.println("Test for setRadioCoordinates passed");
    }

    /**
     * Test of setRadioMeasuredPower method, of class PrimerClass.
     */
    @Test
    public void testSetRadioMeasuredPower() {
        System.out.println("Testing setRadioMeasuredPower method from the PrimerClass class...");
        PrimerClass instance = new PrimerClass();
        double mp = -54.4;
        instance.setRadioMeasuredPower(mp);
        double result = instance.measuredPower.get(0);
        assertEquals(mp, result,0);
        System.out.println("Test for setRadioMeasuredPower passed");
    }

    /**
     * Test of setTRVals method, of class PrimerClass.
     */
    @Test
    public void testSetTRVals() {
        System.out.println("Testing setTRVals method from the PrimerClass class...");
        ArrayList<Long> time = new ArrayList<Long>();
        ArrayList<Long> tagID = new ArrayList<Long>();
        ArrayList<Double> rssi = new ArrayList<Double>();
        long t1 = 1128110800;
        long t2 = 1128110804;
        time.add(t1);
        time.add(t2);
        long id1 = 4056;
        long id2 = 4067;
        tagID.add(id1);
        tagID.add(id2);
        double rs1 = 189.4;
        double rs2 = 167.9;
        rssi.add(rs1);
        rssi.add(rs2);
        PrimerClass instance = new PrimerClass();
        instance.setTRVals(time, tagID, rssi);
        ArrayList<HashMap<Long, ArrayList<Pair<Long,Double>>>> idR = instance.idRSSIs;
        Set<Long> ids = idR.get(0).keySet();
        long d1 = (long) ids.toArray()[0];
        long d2 = (long) ids.toArray()[1];
        assertEquals(id1, d2);
        assertEquals(id2, d1);
        double tm1 = idR.get(0).get(d2).get(0).getKey();
        assertEquals(t1,tm1,0);
        double tm2 = idR.get(0).get(d1).get(0).getKey();
        assertEquals(t2,tm2,0);
        double r1 = idR.get(0).get(d2).get(0).getValue();
        assertEquals(rs1,r1,0);
        double r2 = idR.get(0).get(d1).get(0).getValue();
        assertEquals(rs2,r2,0);
        System.out.println("Test for setTRVals passed");        
    }

    /**
     * Test of getRadiosCoordinates method, of class PrimerClass.
     */
    @Test
    public void testGetRadiosCoordinates() {
        System.out.println("Testing getRadioCoordinates method from the PrimerClass class...");
        PrimerClass instance = new PrimerClass();
        double x = 10;
        double y = 20;
        double z = 1;
        instance.setRadioCoordinates(x, y, z);
        Double[] result = instance.getRadiosCoordinates().get(0);
        double resultX = result[0];
        assertEquals(x, resultX,0);
        double resultY = result[1];
        assertEquals(y, resultY,0);
        double resultZ = result[2];
        assertEquals(z, resultZ,0);
        System.out.println("Test for getRadioCoordinates passed");
    }

    /**
     * Test of getMeasuredPower method, of class PrimerClass.
     */
    @Test
    public void testGetMeasuredPower() {
        System.out.println("Testing getRadioMeasuredPower method from the PrimerClass class...");
        PrimerClass instance = new PrimerClass();
        double mp = -54.4;
        instance.setRadioMeasuredPower(mp);
        ArrayList<Double> result = instance.getMeasuredPower();    
        assertEquals(mp, result.get(0),0);
        System.out.println("Test for getRadioMeasuredPower passed");
    }

    /**
     * Test of getRssiValues method, of class PrimerClass.
     */
    @Test
    public void testGetRssiValues() {
        System.out.println("Testing getRSSIValues method from the PrimerClass class...");
        ArrayList<Long> time = new ArrayList<Long>();
        ArrayList<Long> tagID = new ArrayList<Long>();
        ArrayList<Double> rssi = new ArrayList<Double>();
        long t1 = 1128110800;
        long t2 = 1128110804;
        time.add(t1);
        time.add(t2);
        long id1 = 4056;
        long id2 = 4067;
        tagID.add(id1);
        tagID.add(id2);
        double rs1 = 189.4;
        double rs2 = 167.9;
        rssi.add(rs1);
        rssi.add(rs2);
        PrimerClass instance = new PrimerClass();
        instance.setTRVals(time, tagID, rssi);
        ArrayList<HashMap<Long, ArrayList<Pair<Long,Double>>>> idR = instance.getRssiValues();
        Set<Long> ids = idR.get(0).keySet();
        long d1 = (long) ids.toArray()[0];
        long d2 = (long) ids.toArray()[1];
        double r1 = idR.get(0).get(d2).get(0).getValue();
        assertEquals(rs1,r1,0);
        double r2 = idR.get(0).get(d1).get(0).getValue();
        assertEquals(rs2,r2,0);
        System.out.println("Test for getRSSIValues passed");
        
    }

    
}
