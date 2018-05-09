/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Multilateration;

import static Multilateration.Simulation.basestations;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
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
public class SimulationTest {
    
    public SimulationTest() {
    }
    

    /**
     * Test of getPheasantLocs method, of class Simulation.
     */
    @Test
    public void testGetPheasantLocs() {
        System.out.println("Test for the getPheasantLocs function from the Simulation class...");
        Simulation nc = new Simulation();
        Double[] start = {50.73848598629042, -3.531734873115414};
        Double[][] locations = nc.getPheasantLocs(start);
        double exp1 = -3.5316348731154137;
        double exp2 = -3.52993487311541;
        double exp3 = 50.73848598629042;
        assertEquals(exp1,locations[0][1],1e-10);
        assertEquals(exp3, locations[0][0],1e-10);
        assertEquals(exp2,locations[locations.length-1][1],1e-10);
        assertEquals(exp3, locations[locations.length-1][0],1e-10);
        System.out.println("Test for getPheasantLocs passed");
    }

    /**
     * Test of getDistance method, of class Simulation.
     */
    @Test
    public void testGetDistance() {
        System.out.println("Test for the getDistance function from the Simulation class...");
        Simulation nc = new Simulation();
        basestations = nc.getGeoBasestations();

        MapProcessing mp = new MapProcessing(basestations);
        ArrayList<Double[]> stations = mp.getBasestations(basestations);

        Double[] start = {50.73848598629042, -3.531734873115414};
        Double[][] locations = nc.getPheasantLocs(start);
        Double[]loc = locations[0];
        Double[] locCart = mp.getCartesianLoc(loc);
        Double dist1 = nc.getDistance(stations.get(0), locCart);
        Double dist2 = nc.getDistance(stations.get(1), locCart);
        Double dist3 = nc.getDistance(stations.get(2), locCart);
        Double dist4 = nc.getDistance(stations.get(3), locCart);
        double exp1 = 7.282890025349471;
        double exp2 = 57.02342294154303;
        double exp3 = 49.49687727300778;
        double exp4 = 50.5242133189592;
        assertEquals(dist1,exp1,0.1);
        assertEquals(dist2,exp2,0.1);
        assertEquals(dist3,exp3,0.1);
        assertEquals(dist4,exp4,0.1);
        System.out.println("Test for getDistance passed");
    }

    /**
     * Test of getRSSI method, of class Simulation.
     */
    @Test
    public void testGetRSSI() {
        System.out.println("Testing the getRSSI method from the Simulation class...");
        Simulation nc = new Simulation();
        basestations = nc.getGeoBasestations();

        MapProcessing mp = new MapProcessing(basestations);
        ArrayList<Double[]> stations = mp.getBasestations(basestations);

        Double[] start = {50.73848598629042, -3.531734873115414};
        Double[][] locations = nc.getPheasantLocs(start);
        Double[]loc = locations[0];
        Double[] locCart = mp.getCartesianLoc(loc);
        Double dist1 = nc.getDistance(stations.get(0), locCart);
        Double rssi1 = nc.getRSSI(dist1);
        Double dist2 = nc.getDistance(stations.get(1), locCart);
        Double rssi2 = nc.getRSSI(dist2);
        Double dist3 = nc.getDistance(stations.get(2), locCart);
        Double rssi3 = nc.getRSSI(dist3);
        Double dist4 = nc.getDistance(stations.get(3), locCart);
        Double rssi4 = nc.getRSSI(dist4);
        Double exp1 = -61.24500222750025;
        Double exp2 = -79.12055307647078;
        Double exp3 = -77.89095383633324;
        Double exp4 = -78.06996954223425;
        assertEquals(rssi1,exp1,0.1);
        assertEquals(rssi2,exp2,0.1);
        assertEquals(rssi3,exp3,0.1);
        assertEquals(rssi4,exp4,0.1);
        System.out.println("Test for getRSSI passed");
    }

    /**
     * Test of updateTimes method, of class Simulation.
     */
    @Test
    public void testUpdateTimes() {
        System.out.println("Testing the updateTimes method from the Simulation class...");
        BigInteger currentTime1 = new BigInteger("20170411110464");
        BigInteger expResult1 = new BigInteger("20170411110508");
        BigInteger result1 = Simulation.updateTimes(currentTime1);
        assertEquals(expResult1, result1);
        BigInteger currentTime2 = new BigInteger("20170411116400");
        BigInteger expResult2 = new BigInteger("20170411120404");
        BigInteger result2 = Simulation.updateTimes(currentTime2);
        assertEquals(expResult2, result2);
        System.out.println("Test for updateTimes passed");
    }

    /**
     * Test of getGeoBasestations method, of class Simulation.
     */
    @Test
    public void testGetGeoBasestations() {
        System.out.println("Testing the getGeoBasestations function from the Simulation class...");
        Simulation instance = new Simulation();
        Double exp11 = 50.738486;
        Double exp12 = -3.531713;
        Double exp21 = 50.738675;
        Double exp22 = -3.531101;
        Double exp31 = 50.738822;
        Double exp32 = -3.531642;
        Double exp41 = 50.738829;
        Double exp42 = -3.531627;
        ArrayList<Double[]> result = instance.getGeoBasestations();
        assertEquals(exp11, result.get(0)[0],0.01);
        assertEquals(exp12, result.get(0)[1],0.01);
        assertEquals(exp21, result.get(1)[0],0.01);
        assertEquals(exp22, result.get(1)[1],0.01);
        assertEquals(exp31, result.get(2)[0],0.01);
        assertEquals(exp32, result.get(2)[1],0.01);
        assertEquals(exp41, result.get(3)[0],0.01);
        assertEquals(exp42, result.get(3)[1],0.01);
        System.out.println("Test for getGeoBasestations passed");
    }

}
