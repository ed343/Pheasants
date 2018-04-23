/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Multilateration;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
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
public class RssiEquationTest {
    
    public RssiEquationTest() {
    }
    

    /**
     * Test of getTagDistance method, of class RssiEquation.
     */
    @Test
    public void testGetTagDistance() {
        System.out.println("Testing the getTagDistance method from the RssiEquation class");
        // Create primer
        PrimerClass primer = new PrimerClass();
        // Set number of radios
        primer.setNumberOfRadios(1);
        // Set radio coordinates
        primer.setRadioCoordinates(4028807.901663863, -248650.79535150624, 4938371.316965836);
        // Set radio measured power
        primer.setRadioMeasuredPower(-44);
        
        // Extract log data
        LogData log = new LogData("/Users/James/Documents/Year4/Group_Project/AtlasLogs/atlas_Nat.log",1);
        // Get times.
        ArrayList<BigInteger> tData = log.getTimes();
        // Get IDs.
        ArrayList<Long> idData = log.getIDs();
        // Get RSSI values.
        ArrayList<Double> rssiData = log.getNormRSSIs();
        // Set values.
        primer.setTRVals(tData, idData, rssiData);
        // Create RssiEquation object
        RssiEquation eq = new RssiEquation();
        // Get tag distances for all tags at all times.
        ArrayList<HashMap<Long, HashMap<BigInteger, Double>>> dists = eq.getTagDistance(primer.idRSSIs, primer.measuredPower);
        
        
        // Tag id
        long tID = 4236;
        BigInteger time = new BigInteger("20180418170929");
        // Get distance for tag 
        double val = dists.get(0).get(tID).get(time);
        // Value calculated analytically
        double expval = 0.94972965736;
        
        // Test whether ditance estimation is accurate to within 0.1m.
        assertEquals(expval, val, 0.1);
        System.out.println("Test for getTagDistance passed.");
        

    }
    
}
