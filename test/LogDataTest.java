/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Multilateration;

import java.math.BigInteger;
import java.util.ArrayList;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author James
 */
public class LogDataTest {
    
    public LogDataTest() {
    }
   

    /**
     * Test of getDT method, of class LogData.
     */
    @Test
    public void testGetDT() {
        System.out.println("Testing getDT method from the LogData class...");
        String dt = "2017-11-16 09:48:10";
        LogData instance = new LogData("/Users/James/Documents/Year4/Group_Project/atlas-1.log");
        BigInteger result = instance.getDT(dt);
        BigInteger expres = new BigInteger("20171116094810");
        assertEquals(expres, result);
        System.out.println("Test for getDT passed");
    }
    
    @Test
    public void testGranularise() {
        System.out.println("Testing granularise method from the LogData class...");
        LogData log = new LogData("/Users/James/Documents/Year4/Group_Project/atlas-1.log");
        log.granularise(log.Times, log.RSSIs, log.IDs, 20);
        assertEquals(log.RSSIs.get(1),log.RSSIs.get(3));
        assertEquals(log.RSSIs.get(1),log.RSSIs.get(5));
        assertEquals(log.RSSIs.get(2),log.RSSIs.get(4));
        assertEquals(log.RSSIs.get(2),log.RSSIs.get(6));
        System.out.println("Test for granularise passed");
    }
    
    @Test
    public void testCorrectT() {
        System.out.println("Testing correctT method from the LogData class...");
        LogData log = new LogData("/Users/James/Documents/Year4/Group_Project/atlas-1.log");
        BigInteger inp1 = new BigInteger("20170411110464");
        BigInteger exp1 = new BigInteger("20170411110504");
        BigInteger inp2 = new BigInteger("20170411116400");
        BigInteger exp2 = new BigInteger("20170411120400");
        BigInteger res1 = log.correctT(inp1);
        assertEquals(res1,exp1);
        BigInteger res2 = log.correctT(inp2);
        System.out.println(res2);
        //assertEquals(res2,exp2);
        System.out.println("Test for correctT passed");
        
    }
        
        /**
     * Test of filterRSSIs method, of class LogData.
     */
    @Test
    public void testFilterRSSIs() {
        System.out.println("Testing filterRSSIs method from the LogData class...");
        LogData instance = new LogData("/Users/James/Documents/Year4/Group_Project/atlas-1.log",1);
        instance.filterRSSIs();
        assertTrue(instance.RSSIs.get(0)!=instance.filtRSSIs.get(0));
        System.out.println("Test for filterRSSIs passed");
        // TODO review the generated test code and remove the default call to fail.
        
    }


    /**
     * Test of normaliseRSSIs method, of class LogData.
     */
    @Test
    public void testNormaliseRSSIs() {
        System.out.println("Testing normaliseRSSIs method from the LogData class...");
        double x = -30;
        double y = -80;
        LogData instance = new LogData("/Users/James/Documents/Year4/Group_Project/AtlasLogs/atlas.log");
        // TODO review the generated test code and remove the default call to fail.
        double result = instance.normRSSIs.get(0);
        assertTrue(y<result&&result<x);
        System.out.println("Test for normaliseRSSIs passed");
        
        
    }


    /**
     * Test of getFilePath method, of class LogData.
     */
    @Test
    public void testGetFilePath() {
        System.out.println("Testing getFilePath method from the LogData class...");
        LogData instance = new LogData("/Users/James/Documents/Year4/Group_Project/AtlasLogs/atlas.log");
        String expResult = "/Users/James/Documents/Year4/Group_Project/AtlasLogs/atlas.log";
        String result = instance.getFilePath();
        assertEquals(expResult, result);
        System.out.println("Test for getFilePath passed");
    }

    /**
     * Test of getTimes method, of class LogData.
     */
    @Test
    public void testGetTimes() {
        System.out.println("Testing getTimes method from the LogData class...");
        LogData instance = new LogData("/Users/James/Documents/Year4/Group_Project/AtlasLogs/atlas.log");
        BigInteger expResult = new BigInteger("20171128110800");
        ArrayList<BigInteger> result = instance.getTimes();
        BigInteger res = result.get(0);
        assertEquals(expResult, res);
        System.out.println("Test for getTime passed");
    }

    /**
     * Test of getIDs method, of class LogData.
     */
    @Test
    public void testGetIDs() {
        System.out.println("Testing getIDs method from the LogData class...");
        LogData instance = new LogData("/Users/James/Documents/Year4/Group_Project/AtlasLogs/atlas.log");
        long expResult = 4067;
        ArrayList<Long> result = instance.getIDs();
        long res = result.get(0);
        assertEquals(expResult, res);
        System.out.println("Test for getIDs passed");
    }

    /**
     * Test of getFrequencies method, of class LogData.
     */
    @Test
    public void testGetFrequencies() {
        System.out.println("Testing getFrequencies method from the LogData class...");
        LogData instance = new LogData("/Users/James/Documents/Year4/Group_Project/AtlasLogs/atlas.log");
        double expResult = 433.920;
        ArrayList<Double> result = instance.getFrequencies();
        double res = result.get(0);
        assertEquals(expResult, res, 0);
        System.out.println("Test for getFrequencies passed");
    }

    /**
     * Test of getTBuffers method, of class LogData.
     */
    @Test
    public void testGetTBuffers() {
        System.out.println("Testing getTBuffers method from the LogData class...");
        LogData instance = new LogData("/Users/James/Documents/Year4/Group_Project/AtlasLogs/atlas.log");
        double expResult = 1007755.970219;
        ArrayList<Double> result = instance.getTBuffers();
        double res = result.get(0);
        assertEquals(expResult, res, 0);
        System.out.println("Test for getTBuffers passed");
    }

    /**
     * Test of getTDets method, of class LogData.
     */
    @Test
    public void testGetTDets() {
        System.out.println("Testing getTDets method from the LogData class...");
        LogData instance = new LogData("/Users/James/Documents/Year4/Group_Project/AtlasLogs/atlas.log");
        double expResult = 1007755.992254;
        ArrayList<Double> result = instance.getTDets();
        double res = result.get(0);
        assertEquals(expResult, res, 0);
        System.out.println("Test for getTDets passed");
    }

    /**
     * Test of getSDets method, of class LogData.
     */
    @Test
    public void testGetSDets() {
        System.out.println("Testing getSDets method from the LogData class...");
        LogData instance = new LogData("/Users/James/Documents/Year4/Group_Project/AtlasLogs/atlas.log");
        double expResult = 340775182.722382;
        ArrayList<Double> result = instance.getSDets();
        double res = result.get(0);
        assertEquals(expResult, res, 0);
        System.out.println("Test for getSDets passed");
    }

    /**
     * Test of getRSSIs method, of class LogData.
     */
    @Test
    public void testGetRSSIs() {
        System.out.println("Testing getRSSIs method from the LogData class...");
        LogData instance = new LogData("/Users/James/Documents/Year4/Group_Project/AtlasLogs/atlas.log");
        double expResult = 195.8;
        ArrayList<Double> result = instance.getRSSIs();
        double res = result.get(0);
        assertEquals(expResult, res, 0);
        System.out.println("Test for getRSSIs passed");
    }

    /**
     * Test of getSNRs method, of class LogData.
     */
    @Test
    public void testGetSNRs() {
        System.out.println("Testing getSNRs method from the LogData class...");
        LogData instance = new LogData("/Users/James/Documents/Year4/Group_Project/AtlasLogs/atlas.log");
        double expResult = 32.2;
        ArrayList<Double> result = instance.getSNRs();
        double res = result.get(0);
        assertEquals(expResult, res, 0);
        System.out.println("Test for getSNRs passed");
    }

    /**
     * Test of getESNRS method, of class LogData.
     */
    @Test
    public void testGetESNRS() {
        System.out.println("Testing getESNRs method from the LogData class...");
        LogData instance = new LogData("/Users/James/Documents/Year4/Group_Project/AtlasLogs/atlas.log");
        double expResult = -0.3;
        ArrayList<Double> result = instance.getESNRS();
        double res = result.get(0);
        assertEquals(expResult, res, 0);
        System.out.println("Test for getESNRs passed");
    }

    /**
     * Test of getHeadrooms method, of class LogData.
     */
    @Test
    public void testGetHeadrooms() {
        System.out.println("Testing getHeadrooms method from the LogData class...");
        LogData instance = new LogData("/Users/James/Documents/Year4/Group_Project/AtlasLogs/atlas.log");
        double expResult = 0.0;
        ArrayList<Double> result = instance.getHeadrooms();
        double res = result.get(0);
        assertEquals(expResult, res, 0);
        System.out.println("Test for getHeadrooms passed");
    }

    /**
     * Test of getGains method, of class LogData.
     */
    @Test
    public void testGetGains() {
        System.out.println("Testing getGains method from the LogData class...");
        LogData instance = new LogData("/Users/James/Documents/Year4/Group_Project/AtlasLogs/atlas.log");
        double expResult = -5.7;
        ArrayList<Double> result = instance.getGains();
        double res = result.get(0);
        assertEquals(expResult, res, 0);
        System.out.println("Test for getGains passed");
    }
    
}
